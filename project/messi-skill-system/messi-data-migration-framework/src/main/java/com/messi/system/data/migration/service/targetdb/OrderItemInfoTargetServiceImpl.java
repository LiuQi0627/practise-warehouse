package com.messi.system.data.migration.service.targetdb;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.messi.system.data.migration.entity.OrderItemInfoDO;
import com.messi.system.data.migration.mapper.targetdb.TargetOrderItemInfoTableMapper;
import com.messi.system.data.migration.snowflake.SnowflakeShardingKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 迁移order_item_info表的service
 */
@Slf4j
@Service
public class OrderItemInfoTargetServiceImpl implements OrderItemInfoTargetService {

    @Autowired
    private TargetOrderItemInfoTableMapper targetOrderItemInfoTableMapper;

    /**
     * 获取最大ID
     */
    @Override
    public Long getMaxId() {
        return targetOrderItemInfoTableMapper.getMaxId();
    }

    @Override
    public void syncMigration(List<OrderItemInfoDO> orderItemInfoDOs, Long maxId) {
        for (OrderItemInfoDO orderItemInfoDO : orderItemInfoDOs) {
            //  这里是为了简化模拟主键id
            //  避免在插入分库时产生主键冲突
            //  在实际业务中，是不会这么处理的，主键id是会在外部系统生成好的，在迁移数据时不会出现还需要修改数据主键的情况
            orderItemInfoDO.setId(SnowflakeShardingKeyGenerator.getInstance().generateShardKey());
        }
        targetOrderItemInfoTableMapper.batchInsert(orderItemInfoDOs);
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
            OrderItemInfoDO orderItemInfoDO = afterData.toJavaObject(OrderItemInfoDO.class);

            //  新增或更新
            insertOrUpdate(orderItemInfoDO.getId(), orderItemInfoDO);
            return;
        }

        if (CanalEntry.EventType.DELETE.equals(eventType)) {
            OrderItemInfoDO orderItemInfoDO = beforeData.toJavaObject(OrderItemInfoDO.class);

            //  删除
            delete(orderItemInfoDO.getId(), orderItemInfoDO.getOrderId());
        }

    }

    private void insertOrUpdate(Long id, OrderItemInfoDO orderItemInfoDO) {
        String orderId = orderItemInfoDO.getOrderId();

        //  检查是否已存在数据
        Date modified = targetOrderItemInfoTableMapper.getModified(id, orderId);
        if (modified == null) {
            //  目前不存在这条数据，
            //  新数据就做新增保存
            targetOrderItemInfoTableMapper.insert(orderItemInfoDO);
            log.info("order_item_info表新增记录,id:{},orderId:{}", id, orderId);
            return;
        }

        //  已经存在数据，比较一下modified_time字段
        if (orderItemInfoDO.getModifiedTime().after(modified)) {
            //  如果OrderItemInfoDO中的modified_time比数据库中记录的时间要新
            //  就做更新操作
            LambdaUpdateWrapper<OrderItemInfoDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(OrderItemInfoDO::getId, id);
            updateWrapper.eq(OrderItemInfoDO::getOrderId, orderId);
            targetOrderItemInfoTableMapper.update(orderItemInfoDO, updateWrapper);

            log.info("order_item_info表更新记录,id:{},orderId:{}", id, orderId);
            return;
        }

        //  否则就不对这条数据做任何处理
        log.info("order_item_info表当前记录不做处理,id:{},orderId:{}", id, orderId);
    }

    private void delete(Long id, String orderId) {
        LambdaQueryWrapper<OrderItemInfoDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItemInfoDO::getId, id);
        queryWrapper.eq(OrderItemInfoDO::getOrderId, orderId);
        targetOrderItemInfoTableMapper.delete(queryWrapper);
    }
}
