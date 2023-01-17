package com.messi.system.order.elasticsearch.enums;

import com.messi.system.order.elasticsearch.index.OrderEsIndex;

/**
 * es索引枚举
 */
public enum EsIndexEnums {

    ORDER_ES_INDEX("order_es_index", OrderEsIndex.class);

    private final String indexName;

    private final Class<?> clazz;

    EsIndexEnums(String indexName, Class<?> clazz) {
        this.indexName = indexName;
        this.clazz = clazz;
    }

    public String getIndexName() {
        return indexName;
    }

    public Class<?> getClazz() {
        return clazz;
    }
}
