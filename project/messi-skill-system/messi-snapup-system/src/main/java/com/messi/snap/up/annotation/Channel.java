package com.messi.snap.up.annotation;

import java.lang.annotation.*;

/**
 * 自定义注解
 * 用于标注在listener，指定监听的channel
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Channel {
    String value();
}
