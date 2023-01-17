package com.messi.system.order.constants;

/**
 * 订单系统es常量类
 */
public class OrderElasticsearchConstants {

    /**
     * 订单es索引名称
     */
    public static final String ORDER_ES_INDEX = "order_es_index";

    /**
     * es 分片数
     */
    public static final String INDEX_NUMBER_OF_SHARDS = "index.number_of_shards";

    /**
     * es 副本数
     */
    public static final String INDEX_NUMBER_OF_REPLICAS = "index.number_of_replicas";

    /**
     * es 最大查询返回结果数
     */
    public static final String INDEX_MAX_RESULT_WINDOW = "index.max_result_window";

    /**
     * es mapping 属性名
     */
    public static final String INDEX_MAPPING_PROPERTIES = "properties";

    /**
     * es mapping 类型
     */
    public static final String INDEX_MAPPING_TYPE = "type";

    /**
     * es mapping 格式
     */
    public static final String INDEX_MAPPING_FORMAT = "format";

    /**
     * es 时间格式
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
}
