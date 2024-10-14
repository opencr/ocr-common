package org.ocr.common.utils.xml;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XmlClassDescription {
    String path() default "root"; // 指定xml文件的根目录
}
