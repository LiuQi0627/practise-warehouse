package com.messi.system.order.elasticsearch.annotation;

import com.messi.system.order.elasticsearch.enums.EsIndexEnums;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注是es文档的注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EsDocument {

    /**
     * es 索引名
     */
    EsIndexEnums index();
}
