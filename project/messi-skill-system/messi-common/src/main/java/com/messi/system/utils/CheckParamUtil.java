package com.messi.system.utils;

import cn.hutool.core.util.StrUtil;

import java.util.List;

/**
 * 业务参数检查工具
 */
public class CheckParamUtil {

    public static void checkStringNotEmpty(String str, String errMsg) {
        if (StrUtil.isBlank(str)) {
            throw new RuntimeException(errMsg);
        }
    }

    public static void checkParamNotEmpty(Integer parma, String errMsg) {
        checkStringNotEmpty(parma.toString(), errMsg);
    }

}
