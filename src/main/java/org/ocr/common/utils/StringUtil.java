package org.ocr.common.utils;

import org.apache.commons.lang3.StringUtils;

public class StringUtil extends StringUtils {

    public static <T> T cast(Object obj) {
        return (T) obj;
    }
}
