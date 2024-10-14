package org.ocr.common.utils.xml;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XmlFiledDescription {
    String tagName(); // 指定xml对应的标签名
    boolean subTag() default false; // 是否有子标签，false代表没有，true代表有
    String format() default ""; // 指定数据的格式，时间类型的时候需要指定
    boolean isList() default false; // 标记当前标签是否有多个
}
