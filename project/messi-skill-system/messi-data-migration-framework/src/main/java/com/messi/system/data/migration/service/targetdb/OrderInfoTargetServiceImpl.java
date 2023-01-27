package com.messi.system.data.migration.service.targetdb;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.messi.system.data.migration.entity.OrderInfoDO;
import com.messi.system.data.migration.mapper.targetdb.TargetOrderInfoTableMapper;
import com.messi.system.data.migration.snowflake.SnowflakeShardingKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 迁移order_info表的service
 */
@Slf4j
@Service
public class OrderInfoTargetServiceImpl implements OrderInfoTargetService {

    @Autowired
    private TargetOrderInfoTableMapper targetOrderInfoTableMapper;

    /**
     * 获取最大ID
     */
    @Override
    public Long getMaxId() {
        return targetOrderInfoTableMapper.getMaxId();
    }

    /**
     * 同步迁移数据
     *
     * @param orderInfoDOs 待迁移的批量数据
     */
    @Override
    @Transactional
    public void syncMigration(List<OrderInfoDO> orderInfoDOs, Long maxId) {
        for (OrderInfoDO orderInfoDO : orderInfoDOs) {
            //  这里是为了简化模拟主键id,避免在插入分库时产生主键冲突
            //  在实际业务中，是不会这么处理的，主键id是会在外部系统生成好的，在迁移数据时不会出现还需要修改数据主键的情况
            orderInfoDO.setId(SnowflakeShardingKeyGenerator.getInstance().generateShardKey());
        }
        targetOrderInfoTableMapper.batchInsert(orderInfoDOs);
        log.info("完成同步迁移数据,maxId:{}", maxId);
    }

    /**
     * 消费binlog
     *
     * @param eventType  binlog的事件类型
     * @param beforeData 旧数据
     * @param afterData  新数据
     */
    @Override
    @Transactional
    public void consumeBinlog(CanalEntry.EventType eventType, JSONObject beforeData, JSONObject afterData) {
        if (CanalEntry.EventType.INSERT.equals(eventType) || CanalEntry.EventType.UPDATE.equals(eventType)) {
            OrderInfoDO orderInfoDO = afterData.toJavaObject(OrderInfoDO.class);

            //  新增或更新
            insertOrUpdate(orderInfoDO.getId(), orderInfoDO);
            return;
        }

        if (CanalEntry.EventType.DELETE.equals(eventType)) {
            OrderInfoDO orderInfoDO = beforeData.toJavaObject(OrderInfoDO.class);

            //  删除
            delete(orderInfoDO.getId(), orderInfoDO.getOrderId());
        }
    }

    private void insertOrUpdate(Long id, OrderInfoDO orderInfoDO) {
        String orderId = orderInfoDO.getOrderId();

        //  检查是否已存在数据
        Date modified = targetOrderInfoTableMapper.getModified(id, orderId);
        if (modified == null) {
            //  目前不存在这条数据，
            //  新数据就做新增保存
            targetOrderInfoTableMapper.insert(orderInfoDO);
            log.info("order_info表新增记录,id:{},orderId:{}", id, orderId);
            return;
        }

        //  已经存在数据，比较一下modified_time字段
        if (orderInfoDO.getModifiedTime().after(modified)) {
            //  如果orderInfoDO中的modified_time比数据库中记录的时间要新
            //  就做更新操作
            LambdaUpdateWrapper<OrderInfoDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(OrderInfoDO::getId, id);
            updateWrapper.eq(OrderInfoDO::getOrderId, orderId);
            targetOrderInfoTableMapper.update(orderInfoDO, updateWrapper);

            log.info("order_info表更新记录,id:{},orderId:{}", id, orderId);
            return;
        }

        //  否则就不对这条数据做任何处理
        log.info("order_info表当前记录不做处理,id:{},orderId:{}", id, orderId);
    }

    private void delete(Long id, String orderId) {
        LambdaQueryWrapper<OrderInfoDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfoDO::getId, id);
        queryWrapper.eq(OrderInfoDO::getOrderId, orderId);
        targetOrderInfoTableMapper.delete(queryWrapper);
    }
}
