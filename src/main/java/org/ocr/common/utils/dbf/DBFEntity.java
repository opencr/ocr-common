package org.ocr.common.utils.dbf;

import lombok.Data;

import java.io.Serializable;

/**
 * @author DELL
 * @create 2017-9-15
 * @modify 2017-9-15
 */
@Data
public class DBFEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private String tableFieldName;  // 数据库字段名称
    private String fieldAliasName;      // DBF字段别名
    private String fieldName;      // DBF字段名称
    private Integer length = 0;     // 长度
    private char type;      // 字段类型，C:字符串,D:日期,N:数值型,L:逻辑型,M:各自字符（Memo）,I:整数型,B:二进制型,G:,各自字符Y:货币类型
    private Integer scale = 0;      // 保留小数位
    private String tb;      // 表格
    private Integer notNull;        // 是否必填，0否，1是

}
