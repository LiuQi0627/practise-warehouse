package com.messi.system.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.messi.system.order.domain.dto.OrderQueryDTO;
import com.messi.system.order.domain.entity.OrderInfoDO;
import com.messi.system.order.domain.query.OrderQueryCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单信息表 Mapper 接口
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfoDO> {

    /**
     * 联表查询订单分页
     *
     * @param page  分页信息
     * @param query 查询条件
     */
    Page<OrderQueryDTO> queryOrderPageByJoinTable(Page<OrderInfoDO> page, @Param("query") OrderQueryCondition query);

}
