package com.messi.system.data.migration.service.targetdb;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.messi.system.data.migration.entity.OrderPriceDetailsDO;
import com.messi.system.data.migration.mapper.targetdb.TargetOrderPriceDetailsTableMapper;
import com.messi.system.data.migration.snowflake.SnowflakeShardingKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


/**
 * 迁移order_price_details表的service
 */
@Slf4j
@Service
public class OrderPriceDetailsTargetServiceImpl implements OrderPriceDetailsTargetService {

    @Autowired
    private TargetOrderPriceDetailsTableMapper targetOrderPriceDetailsTableMapper;

    /**
     * 获取最大ID
     */
    @Override
    public Long getMaxId() {
        return targetOrderPriceDetailsTableMapper.getMaxId();
    }

    @Override
    public void syncMigration(List<OrderPriceDetailsDO> orderPriceDetailsDOs, Long maxId) {
        for (OrderPriceDetailsDO orderPriceDetailsDO : orderPriceDetailsDOs) {
            //  这里是为了简化模拟主键id
            //  避免在插入分库时产生主键冲突
            //  在实际业务中，是不会这么处理的，主键id是会在外部系统生成好的，在迁移数据时不会出现还需要修改数据主键的情况
            orderPriceDetailsDO.setId(SnowflakeShardingKeyGenerator.getInstance().generateShardKey());
        }
        targetOrderPriceDetailsTableMapper.batchInsert(orderPriceDetailsDOs);
        log.info("完成同步迁移数据,maxId:{}", maxId);
    }

    @Override
    public void consumeBinlog(CanalEntry.EventType eventType, JSONObject beforeData, JSONObject afterData) {
        if (CanalEntry.EventType.INSERT.equals(eventType) || CanalEntry.EventType.UPDATE.equals(eventType)) {
            OrderPriceDetailsDO orderPriceDetailsDO = afterData.toJavaObject(OrderPriceDetailsDO.class);

            //  新增或更新
            insertOrUpdate(orderPriceDetailsDO.getId(), orderPriceDetailsDO);
            return;
        }

        if (CanalEntry.EventType.DELETE.equals(eventType)) {
            OrderPriceDetailsDO orderPriceDetailsDO = beforeData.toJavaObject(OrderPriceDetailsDO.class);

            //  删除
            delete(orderPriceDetailsDO.getId(), orderPriceDetailsDO.getOrderId());
        }
    }

    private void insertOrUpdate(Long id, OrderPriceDetailsDO OrderPriceDetailsDO) {
        String orderId = OrderPriceDetailsDO.getOrderId();

        //  检查是否已存在数据
        Date modified = targetOrderPriceDetailsTableMapper.getModified(id, orderId);
        if (modified == null) {
            //  目前不存在这条数据，
            //  新数据就做新增保存
            targetOrderPriceDetailsTableMapper.insert(OrderPriceDetailsDO);
            log.info("order_price_details表新增记录,id:{},orderId:{}", id, orderId);
            return;
        }

        //  已经存在数据，比较一下modified_time字段
        if (OrderPriceDetailsDO.getModifiedTime().after(modified)) {
            //  如果OrderPriceDetailsDO中的modified_time比数据库中记录的时间要新
            //  就做更新操作
            LambdaUpdateWrapper<OrderPriceDetailsDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(com.messi.system.data.migration.entity.OrderPriceDetailsDO::getId,id);
            updateWrapper.eq(com.messi.system.data.migration.entity.OrderPriceDetailsDO::getOrderId, orderId);
            targetOrderPriceDetailsTableMapper.update(OrderPriceDetailsDO, updateWrapper);

            log.info("order_price_details表更新记录,id:{},orderId:{}", id, orderId);
            return;
        }

        //  否则就不对这条数据做任何处理
        log.info("order_price_details表当前记录不做处理,id:{},orderId:{}", id, orderId);
    }

    private void delete(Long id, String orderId) {
        LambdaQueryWrapper<OrderPriceDetailsDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderPriceDetailsDO::getId, id);
        queryWrapper.eq(OrderPriceDetailsDO::getOrderId, orderId);
        targetOrderPriceDetailsTableMapper.delete(queryWrapper);
    }

}
