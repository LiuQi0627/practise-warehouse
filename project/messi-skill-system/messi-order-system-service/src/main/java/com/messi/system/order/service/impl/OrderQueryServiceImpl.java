package com.messi.system.order.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.messi.system.core.ResResult;
import com.messi.system.order.converter.OrderConverter;
import com.messi.system.order.dao.OrderDAO;
import com.messi.system.order.dao.OrderItemInfoDAO;
import com.messi.system.order.dao.OrderPriceDAO;
import com.messi.system.order.domain.dto.OrderDetailDTO;
import com.messi.system.order.domain.dto.OrderQueryDTO;
import com.messi.system.order.domain.entity.OrderInfoDO;
import com.messi.system.order.domain.entity.OrderItemInfoDO;
import com.messi.system.order.domain.entity.OrderPriceDO;
import com.messi.system.order.domain.query.OrderQueryCondition;
import com.messi.system.order.elasticsearch.client.ElasticsearchClientService;
import com.messi.system.order.elasticsearch.enums.EsIndexEnums;
import com.messi.system.order.mapper.OrderInfoMapper;
import com.messi.system.order.service.OrderPushService;
import com.messi.system.order.service.OrderQueryService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.messi.system.order.elasticsearch.constants.EsQueryConstants.CREATE_TIME;
import static com.messi.system.order.elasticsearch.constants.EsQueryConstants.ORDER_ID;

/**
 * 订单查询service实现类
 */
@Slf4j
@Service
public class OrderQueryServiceImpl implements OrderQueryService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderConverter orderConverter;

    @Autowired
    private ElasticsearchClientService elasticsearchClientService;

    @Autowired
    private OrderDAO orderDAO;

    @Autowired
    private OrderItemInfoDAO orderItemInfoDAO;

    @Autowired
    private OrderPriceDAO orderPriceDAO;

    @Autowired
    private OrderPushService orderPushService;

    /**
     * 查询订单单表分页
     */
    @Override
    public ResResult<Page<OrderQueryDTO>> queryOrderPageBySingleTable(OrderQueryCondition orderQueryCondition) {
        Page<OrderInfoDO> page = new Page<>(orderQueryCondition.getPageNo(), orderQueryCondition.getPageSize());
        Page<OrderInfoDO> orderInfoDOPage = orderInfoMapper.selectPage(page, null);
        //  数据转换
        Page<OrderQueryDTO> orderQueryDTOPage = orderConverter.orderInfoDOPage2OrderQueryDTOPage(orderInfoDOPage);
        return ResResult.buildSuccess(orderQueryDTOPage);
    }

    /**
     * 查询订单联表分页 联合
     */
    @Override
    public ResResult<Page<OrderQueryDTO>> queryOrderPageByJoinTable(OrderQueryCondition orderQueryCondition) {
        Page<OrderInfoDO> page = new Page<>(orderQueryCondition.getPageNo(), orderQueryCondition.getPageSize());
        Page<OrderQueryDTO> orderQueryDTOPage = orderInfoMapper.queryOrderPageByJoinTable(page, orderQueryCondition);
        log.info("查询到订单分页,orderQueryDTOPage：{}", orderQueryDTOPage);
        return ResResult.buildSuccess(orderQueryDTOPage);
    }

    /**
     * 查询订单详情
     */
    @Override
    public ResResult<OrderDetailDTO> queryOrderDetailByEs(String orderId) {
        ResResult<OrderDetailDTO> orderDetailResResult;

        //  1、首先从es查询
        orderDetailResResult = getByElasticsearch(orderId);
        if (orderDetailResResult.getData() != null) {
            log.info("elasticsearch查询到此订单详情,orderId:{}", orderId);
            return orderDetailResResult;
        }

        //  2、es没有查到，从数据库查询
        orderDetailResResult = getByDatabase(orderId);
        if (orderDetailResResult.getData() == null) {
            log.error("数据库查询不到此订单,orderId:{}", orderId);
            return null;
        }
        log.info("数据库查询到此订单详情,orderId:{}", orderId);

        //  3、通过MQ推送数据上报到es
        mqPushDataToElasticsearch(orderDetailResResult.getData());

        //  4、返回查询结果
        return orderDetailResResult;
    }

    @Override
    public ResResult<OrderDetailDTO> queryOrderDetailByTable(String orderId) {
        ResResult<OrderDetailDTO> resResult = getByDatabase(orderId);
        log.info("数据库查询到此订单详情,orderId:{}", orderId);
        return resResult;
    }

    private void mqPushDataToElasticsearch(OrderDetailDTO orderDetailDTO) {
        orderPushService.sendOrderToEs(orderDetailDTO);
    }

    private ResResult<OrderDetailDTO> getByDatabase(String orderId) {
        //  查询数据库
        OrderInfoDO orderInfoDO = orderDAO.getOrder(orderId);
        List<OrderItemInfoDO> orderItemInfoDOList = orderItemInfoDAO.getItemList(orderId);
        OrderPriceDO orderPriceDO = orderPriceDAO.getOrderPrice(orderId);

        //  检查参数
        if (orderInfoDO == null || orderItemInfoDOList == null || orderPriceDO == null) {
            throw new RuntimeException("getByDatabase查询数据失败,数据是空");
        }

        //  封装数据构造器对象
        OrderDetailDTO detailDTO = OrderDetailDTO.builder()
                .orderId(orderId)
                .channel(orderInfoDO.getChannel())
                .orderType(orderInfoDO.getOrderType())
                .orderStatus(orderInfoDO.getOrderStatus())
                .orderCancelTime(orderInfoDO.getOrderCancelTime())
                .sellerId(orderInfoDO.getSellerId())
                .userId(orderInfoDO.getUserId())
                .totalAmount(orderInfoDO.getTotalAmount())
                .actualAmount(orderInfoDO.getActualAmount())
                .orderPayType(orderInfoDO.getOrderPayType())
                .payTime(orderInfoDO.getPayTime())
                .couponId(orderInfoDO.getCouponId())
                .appraiseStatus(orderInfoDO.getAppraiseStatus())
                .orderTotalPrice(orderPriceDO.getOrderTotalPrice())
                .createTime(orderInfoDO.getCreateTime())
                .modifiedTime(new Date())
                .build();

        List<OrderDetailDTO.OrderItemDTO> orderItemDTOS = new LinkedList<>();
        for (OrderItemInfoDO orderItemInfoDO : orderItemInfoDOList) {
            OrderDetailDTO.OrderItemDTO orderItemDTO = new OrderDetailDTO.OrderItemDTO();
            orderItemDTO.setOrderItemId(orderItemInfoDO.getOrderItemId());
            orderItemDTO.setProductId(orderItemInfoDO.getProductId());
            orderItemDTO.setSkuId(orderItemInfoDO.getSkuId());
            orderItemDTO.setSaleNum(orderItemInfoDO.getSaleNum());
            orderItemDTO.setSalePrice(orderItemInfoDO.getSalePrice());
            orderItemDTO.setOrderItemPrice(orderItemInfoDO.getSalePrice());

            orderItemDTOS.add(orderItemDTO);
        }
        detailDTO.setOrderItemDTOs(orderItemDTOS);

        //  返回结果
        return ResResult.buildSuccess(detailDTO);
    }

    private ResResult<OrderDetailDTO> getByElasticsearch(String orderId) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //  设置查询条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.termsQuery(ORDER_ID, orderId));

        //  设置排序条件
        searchSourceBuilder.sort(CREATE_TIME, SortOrder.DESC);
        searchSourceBuilder.query(boolQueryBuilder);

        //  执行查询
        OrderDetailDTO orderDetailDTO = elasticsearchClientService.search(EsIndexEnums.ORDER_ES_INDEX, searchSourceBuilder);
        return ResResult.buildSuccess(orderDetailDTO);
    }

}
