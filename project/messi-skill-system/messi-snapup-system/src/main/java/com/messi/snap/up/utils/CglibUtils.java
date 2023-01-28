package com.messi.snap.up.utils;

public class CglibUtils {

    public static Class<?> filterCglibProxyClass(Class<?> clazz) {
        while (isCglibProxyClass(clazz)) {
            clazz = clazz.getSuperclass();
        }
        return clazz;
    }

    public static boolean isCglibProxyClass(Class<?> clazz) {
        return clazz != null && clazz.getName().contains("$$");
    }

}
