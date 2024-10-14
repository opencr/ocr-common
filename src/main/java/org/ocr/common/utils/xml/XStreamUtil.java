package org.ocr.common.utils.xml;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
public class XStreamUtil {

    /**
     * xml转换成javaBean
     *
     * @param xml   xml格式的字符串
     * @param clazz 响应的bean类
     * @return 返回结果
     */
    public static Object getObjectFromXml(String xml, String eleName, Class clazz) {
        XStream xStream = new XStream();
        // 为类名节点重命名
        xStream.alias(eleName, clazz);
        // 忽略掉一些新增的字段
        xStream.ignoreUnknownElements();
        return xStream.fromXML(xml);
    }

    /**
     * java 转换成xml
     *
     * @param obj 对象实例
     * @return xml字符串
     */
    public static String toXml(Object obj) {
        XStream xStream = new XStream(new DomDriver("utf-8", new XmlFriendlyNameCoder("-_", "_")));
        // 如果没有这句，xml中的根元素会是<包.类名>;或者说注解根本没有生效，所以元素名就是类的属性
        // 通过注解的方式
        xStream.processAnnotations(obj.getClass());
        return xStream.toXML(obj);
    }

    /**
     * 将传入的xml字符串转换成java对象
     *
     * @param xmlStr xml字符串
     * @param cls    xml对应的class类
     * @param <T>    xml对应的class类的实例对象
     * @return
     */
    public static <T> T xmlStrToBean(String xmlStr, Class<T> cls) {
        long start = System.currentTimeMillis();
        XStream xstream = new XStream(new DomDriver());
        xstream.processAnnotations(cls);
        xstream.ignoreUnknownElements();
        T t = (T) xstream.fromXML(xmlStr);
        log.error("解析xml耗时：{}", (System.currentTimeMillis() - start));
        return t;
    }

    /**
     * xml流转成java Bean
     *
     * @param inputStream 文件流
     * @param cls         xml对应的class类
     * @param <T>         xml对应的class类的实例对象
     * @return
     */
    public static <T> T xmlInputStreamToBean(InputStream inputStream, Class<T> cls) {
        long start = System.currentTimeMillis();
        XStream xstream = new XStream(new DomDriver());
        xstream.processAnnotations(cls);
        xstream.ignoreUnknownElements();
        T t = (T) xstream.fromXML(inputStream);
        return t;
    }

    /**
     * 将传入的xml文件转换成java对象
     *
     * @param inputStream 文件流
     * @param cls         xml对应的class类
     * @param <T>         xml对应的class类的实例对象
     * @return
     */
    public static <T> T inputStreamToBean(InputStream inputStream, Class<T> cls) throws IOException {
        StringBuffer line = new StringBuffer();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        //开始读
        String temp;
        while ((temp = br.readLine()) != null) {
            line.append(temp);
        }

        //关闭,关闭最外层的流即可. (装饰者模式)
        br.close();
        String lineStr = line.toString();
        // 处理ZWNBSP的问题
        if (lineStr.startsWith(UTF8_BOM)) {
            lineStr = lineStr.substring(1);
        }
        return xmlStrToBean(lineStr, cls);
    }

    public static final String UTF8_BOM = "\uFEFF";


}
