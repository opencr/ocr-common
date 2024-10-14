package org.ocr.common.utils.xml;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.ocr.common.exception.XmlException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class Dom4JUtil {
    public static final String ROOT_PATH = "//";
    public static final String SUB_PATH = "./";

    public static <T> T xml2Object(InputStream inputStream, Class<T> clazz) throws DocumentException, InstantiationException, IllegalAccessException {
        SAXReader saxReader = new SAXReader(false);
        Document document = null;
        saxReader.setEntityResolver(new IgnoreDTDEntityResolver()); // 跳过DTD验证
        document = saxReader.read(inputStream);
        return xml2Object(document, clazz);
    }

    public static <T> T xml2Object(String filePath, Class<T> clazz) throws DocumentException, InstantiationException, IllegalAccessException {
        SAXReader saxReader = new SAXReader(false);
        Document document = null;
        saxReader.setEntityResolver(new IgnoreDTDEntityResolver()); // 跳过DTD验证
        document = saxReader.read(new File(filePath));
        return xml2Object(document, clazz);
    }

    public static <T> T xmlStr2Object(String xml, Class<T> clazz) throws DocumentException, InstantiationException, IllegalAccessException {
        SAXReader saxReader = new SAXReader(false);
        Document document = null;
        saxReader.setEntityResolver(new IgnoreDTDEntityResolver()); // 跳过DTD验证
        document = saxReader.read(new ByteArrayInputStream(xml.getBytes()));
        return xml2Object(document, clazz);
    }

    /**
     * 将xml文件转化成对应的实体类
     *
     * @param document
     * @param clazz    对应实体的Class
     * @param <T>      泛型
     * @return 返回赋值的java实体
     */
    public static <T> T xml2Object(Document document, Class<T> clazz) throws InstantiationException, IllegalAccessException {
        // 获取当前类的注解
        XmlClassDescription declaredAnnotation = clazz.getDeclaredAnnotation(XmlClassDescription.class);
        // 获取xml的根标签名称（从对应的实体类上自定义注解找）
        Optional.ofNullable(declaredAnnotation)
                .orElseThrow(() -> new XmlException("获取xml文件的根根标签名失败"));
        String path = declaredAnnotation.path();
        Node rootNode = document.selectSingleNode(ROOT_PATH + path);
        return handle(rootNode, clazz, clazz.newInstance());
    }

    /**
     * 该方法主要是将xml文件解析的字符串值转化成相关类型数据（感觉写的不好，暂时这样先实现）
     *
     * @param value  tag标签里面解析的值
     * @param tClass 标签对应实体的Class
     * @param format 转化格式
     * @return 返回对应的数据类型
     */
    private static Object transferData(String value, Class tClass, String format) {
        try {
            String name = tClass.getSimpleName();
            switch (name) {
                case "Integer":
                case "int":
                    return Integer.valueOf(value);
                case "Double":
                case "double":
                    return Double.valueOf(value);
                case "Float":
                case "float":
                    return Float.valueOf(value);
                case "Byte":
                case "byte":
                    return Byte.valueOf(value);
                case "Short":
                case "short":
                    return Short.valueOf(value);
                case "Long":
                case "long":
                    return Long.valueOf(value);
                case "Character":
                case "char":
                    return value.charAt(0);
                case "Boolean":
                case "boolean":
                    return Boolean.valueOf(value);
                case "Date":
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
                    Date parse = simpleDateFormat.parse(value);
                    return parse;
                default:
                    return value;
            }
        } catch (ParseException e) {
            throw new XmlException("数据转化异常,请核对");
        }
    }

    /**
     * 详细的转化过程，采用递归方式去进行
     *
     * @param parentNode 父标签节点
     * @param clazz      父标签对应当前实体的Class
     * @param t          父标签对应的实体（未赋值）
     * @param <T>        泛型
     * @return 返回构建的实体对象（赋值）
     */
    private static <T> T handle(Node parentNode, Class<T> clazz, T t) {
        // 获取当前对象的所有属性
        Field[] fields = clazz.getDeclaredFields();
        Arrays.stream(fields).forEach(field -> {
            field.setAccessible(true);

            // 获取属性的XmlFiledDescription注解
            XmlFiledDescription xmlFiled = field.getDeclaredAnnotation(XmlFiledDescription.class);
            if (xmlFiled != null) {
                // 获取当前属性对应xml的标签名
                String tagName = xmlFiled.tagName();
                // 获取数据格式
                String format = xmlFiled.format();
                // 获取是否是一个集合
                boolean isList = xmlFiled.isList();
                // 判断当前属性是否有子标签
                boolean isSubTag = xmlFiled.subTag();
                Object value = null;
                if (isList) {
                    try {
                        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                        Class actualTypeArgument = (Class) parameterizedType.getActualTypeArguments()[0];
                        List<Node> nodes = parentNode.selectNodes(SUB_PATH + tagName);
                        if (isSubTag) {
                            value = nodes.parallelStream()
                                    .map(node -> {
                                        try {
                                            return handle(node, actualTypeArgument, actualTypeArgument.newInstance());
                                        } catch (InstantiationException e) {
                                            throw new RuntimeException(e);
                                        } catch (IllegalAccessException e) {
                                            throw new RuntimeException(e);
                                        }
                                    })
                                    .collect(Collectors.toList());
                        } else {
                            value = nodes.parallelStream()
                                    .map(node -> transferData(node.getText(), actualTypeArgument, format))
                                    .collect(Collectors.toList());
                        }
                        field.set(t, value);
                    } catch (IllegalAccessException e) {
                        throw new XmlException(clazz + "获取实例失败，访问权限不足");
                    }
                } else {
                    try {
                        // 获取当前标签
                        Node currentNode = parentNode.selectSingleNode(SUB_PATH + tagName);
                        if (isSubTag) {
                            Class aClass = field.getType();
                            Object instance = aClass.newInstance();
                            value = handle(currentNode, aClass, instance);
                        } else {
                            value = transferData(currentNode.getText(), field.getType(), format);
                        }
                        field.set(t, value);
                    } catch (InstantiationException e) {
                        throw new XmlException(clazz + "获取实例失败，请提供无参构造");
                    } catch (IllegalAccessException e) {
                        throw new XmlException(clazz + "获取实例失败，访问权限不足");
                    }
                }
            } else {
                XmlAttribute xmlAttribute = field.getDeclaredAnnotation(XmlAttribute.class);
                if (xmlAttribute != null) {
                    try {
                        Element element = (Element) parentNode;
                        Attribute attribute = element.attribute(field.getName());
                        if (attribute != null) {
                            Object value = transferData(element.attribute(field.getName()).getValue(), field.getType(), "");
                            if (value != null) {
                                field.set(t, value);
                            }
                        }
                    } catch (IllegalAccessException e) {
                        throw new XmlException(clazz + "获取实例失败，访问权限不足");
                    }
                }
            }
        });
        return t;
    }
}
