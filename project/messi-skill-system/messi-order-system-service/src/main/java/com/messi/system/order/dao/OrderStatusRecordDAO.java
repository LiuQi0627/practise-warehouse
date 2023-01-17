package com.messi.system.order.dao;

import com.messi.system.mybatis.BaseDAO;
import com.messi.system.order.domain.entity.OrderInfoDO;
import com.messi.system.order.domain.entity.OrderStatusRecordDO;
import com.messi.system.order.mapper.OrderInfoMapper;
import com.messi.system.order.mapper.OrderStatusRecordMapper;
import org.springframework.stereotype.Repository;

/**
 * 操作order_status_record DAO
 */
@Repository
public class OrderStatusRecordDAO extends BaseDAO<OrderStatusRecordMapper, OrderStatusRecordDO> {

}
