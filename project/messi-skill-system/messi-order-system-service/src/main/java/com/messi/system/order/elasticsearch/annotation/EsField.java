package com.messi.system.order.elasticsearch.annotation;

import com.messi.system.order.elasticsearch.constants.EsDataTypeConstants;
import com.messi.system.order.elasticsearch.constants.EsTokenizerConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注 es 索引字段 的注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EsField {

    /**
     * es数据类型
     */
    String type() default EsDataTypeConstants.TEXT;

    /**
     * es指定的分词器
     */
    String tokenizer() default EsTokenizerConstants.STANDARD;

}
