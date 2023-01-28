package com.messi.snap.up.reactor.listener.snapup;

import com.messi.snap.up.annotation.Channel;
import com.messi.snap.up.enums.SnapupProcessEnums;
import com.messi.snap.up.reactor.context.SnapupContext;
import com.messi.snap.up.reactor.event.BaseEvent;
import com.messi.snap.up.reactor.listener.BaseSnapupListener;
import com.messi.snap.up.reactor.process.Process3LockStock;
import com.messi.snap.up.reactor.process.Process4CreateSnapUpOrder;
import com.messi.snap.up.redis.SnapupKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@Channel("Process3")   //  和yaml配置文件中的自定义配置相对应
public class ProcessListener3 extends BaseSnapupListener<Process3LockStock> {

    private static final AtomicLong sequencer = new AtomicLong();
    //  lua脚本执行扣减，保证原子性
    private static final String SCRIPT = "local stockKey = '%s';"
            + "local salableStock = redis.call('hget', stockKey, 'salableStock') + 0;"
            + "local lockedStock = redis.call('hget', stockKey, 'lockedStock') + 0;"
            + "if(salableStock > 0) "
            + "then "
            + "redis.call('hset', stockKey, 'salableStock', salableStock - 1);"
            + "redis.call('hset', stockKey, 'lockedStock', lockedStock + 1);"
            + "return 'success';"
            + "else "
            + "return 'failure';"
            + "end;";

    //  lua脚本执行成功
    public static final String lua_exec_success = "success";

    /**
     * 这个方法中主要有2部分组成：匹配redis和lua脚本原子执行redis命令
     * 其中匹配redis的部分是可以做很大优化的，目前为了快速实现双总线的高并发架构，先使用这种方式查找redis实例
     */
    @Override
    protected void doProcess(Process3LockStock event, SnapupContext snapupContext) {
        workerThreadExecutor.execute(
                SnapupProcessEnums.SNAPUP_PROCESS_3.getChannel(),
                new Runnable() {
                    @Override
                    public void run() {
                        Long promotionId = snapupContext.getPromotionId();
                        String userId = snapupContext.getUserId();
                        String skuId = snapupContext.getSkuId();

                        //  自定义自增序列，redis实例每执行一次，自增序列+1
                        long sequence = sequencer.incrementAndGet();

                        //  将key写到lua脚本
                        String snapUpKey = SnapupKey.buildStockKey(promotionId, skuId);
                        String lua = String.format(SCRIPT, snapUpKey);

                        //  当前redis没有库存了，顺延至下一台做扣减
                        int redisCount = redisCommand.getRedisCount();
                        //  记录redis实例序列
                        long maxSequence = sequence + redisCount - 1;
                        String result;
                        for (long i = sequence; i <= maxSequence; i++) {
                            //  在匹配到的redis实例上执行lua脚本做扣减
                            result = (String) redisCommand.eval(i, lua);
                            //  扣减成功
                            if (StringUtils.equals(result, lua_exec_success)) {
                                //  更新当前用户抢购记录，用作幂等校验，避免同一用户多次抢购
                                String skuKey = SnapupKey.buildCheckSkuKey(promotionId, skuId, userId);
                                redisCommand.set(skuKey, "1");
                                //  2个小时内不允许重复抢购同一sku,随着业务变化
                                redisCommand.expire(skuKey, 7200);
                                // 需要记录下是在哪台redis上扣的库存，可以用于业务扩展统计库存
                                snapupContext.setSequence(i);
                                log.info("用户:{}在活动:{}中抢购商品:{}成功", userId, promotionId, skuId);

                                log.info("ProcessListener3执行完成,用户ID:{}", userId);
                                bossEventBus.publishSnapUpEvent(
                                        SnapupProcessEnums.SNAPUP_PROCESS_4.getChannel(),
                                        new Process4CreateSnapUpOrder(),
                                        snapupContext
                                );
                                return;
                            }
                        }
                        response(snapupContext.getAsyncContext(), false, "当前商品已售罄");
                    }
                }
        );
    }

    @Override
    public Boolean accept(BaseEvent baseEvent) {
        return baseEvent instanceof Process3LockStock;
    }
}
