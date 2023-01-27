package com.messi.system.data.migration.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.messi.system.data.migration.entity.OrderInfoDO;
import com.messi.system.data.migration.entity.OrderItemInfoDO;
import com.messi.system.data.migration.entity.OrderPriceDetailsDO;
import com.messi.system.data.migration.service.FullDataMigrationTaskService;
import com.messi.system.data.migration.service.origindb.OrderInfoOriginService;
import com.messi.system.data.migration.service.origindb.OrderItemInfoOriginService;
import com.messi.system.data.migration.service.origindb.OrderPriceDetailsOriginService;
import com.messi.system.data.migration.service.targetdb.OrderInfoTargetService;
import com.messi.system.data.migration.service.targetdb.OrderItemInfoTargetService;
import com.messi.system.data.migration.service.targetdb.OrderPriceDetailsTargetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 执行全量迁移任务的service
 */
@Service
public class FullDataMigrationTaskServiceImpl implements FullDataMigrationTaskService {

    @Autowired
    private OrderInfoTargetService orderInfoTargetService;

    @Autowired
    private OrderItemInfoTargetService orderItemInfoTargetService;

    @Autowired
    private OrderPriceDetailsTargetService orderPriceDetailsTargetService;

    @Autowired
    private OrderInfoOriginService orderInfoOriginService;

    @Autowired
    private OrderItemInfoOriginService orderItemInfoOriginService;

    @Autowired
    private OrderPriceDetailsOriginService orderPriceDetailsOriginService;

    @Override
    public Long migrationFullDataTask(String tableName, Date fullMigrationDate,
                                      Long minId, Integer batchDataSize, FullDataMigrationTaskService fullDataMigrationTaskService) {
        if ("order_info".equalsIgnoreCase(tableName)) {
            //  批量查询出数据
            List<OrderInfoDO> orderInfoDOs =
                    orderInfoOriginService.queryByBatch(minId, fullMigrationDate, batchDataSize);

            if (CollectionUtil.isEmpty(orderInfoDOs)) {
                return null;
            }
            //  当前数据集中的最大id
            Long curDbId = orderInfoDOs.get(orderInfoDOs.size() - 1).getId();

            //  同步迁移数据
            orderInfoTargetService.syncMigration(orderInfoDOs, curDbId);

            //  同步迁移执行完后，把当前批次的id+1
            return curDbId + 1;
        }

        if ("order_item_info".equalsIgnoreCase(tableName)) {
            //  批量查询出数据
            List<OrderItemInfoDO> orderItemInfoDOs
                    = orderItemInfoOriginService.queryByBatch(minId, fullMigrationDate, batchDataSize);

            if (CollectionUtil.isEmpty(orderItemInfoDOs)) {
                return null;
            }
            //  当前数据集中的最大id
            Long curDbId = orderItemInfoDOs.get(orderItemInfoDOs.size() - 1).getId();

            //  同步迁移数据
            orderItemInfoTargetService.syncMigration(orderItemInfoDOs, curDbId);

            //  同步迁移执行完后，把当前批次的id+1
            return curDbId + 1;
        }

        if ("order_price_details".equalsIgnoreCase(tableName)) {
            //  批量查询出数据
            List<OrderPriceDetailsDO> orderPriceDetailsDOs
                    = orderPriceDetailsOriginService.queryByBatch(minId, fullMigrationDate, batchDataSize);

            if (CollectionUtil.isEmpty(orderPriceDetailsDOs)) {
                return null;
            }
            //  当前数据集中的最大id
            Long curDbId = orderPriceDetailsDOs.get(orderPriceDetailsDOs.size() - 1).getId();

            //  同步迁移数据
            orderPriceDetailsTargetService.syncMigration(orderPriceDetailsDOs, curDbId);

            //  同步迁移执行完后，把当前批次的id+1
            return curDbId + 1;
        }

        throw new RuntimeException("没有找到匹配的数据表,tableName:" + tableName);

    }
}
