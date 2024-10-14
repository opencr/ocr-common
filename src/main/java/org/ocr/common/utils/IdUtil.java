package org.ocr.common.utils;

import java.util.UUID;

/**
 * ID生成器工具类
 *
 * @author admin
 */
public class IdUtil {
    /**
     * 获取随机UUID
     *
     * @return 随机UUID
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    public static String uuid() {
        return java.util.UUID.randomUUID().toString().replaceAll("-", "");
    }

}
