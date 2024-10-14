package org.ocr.common.utils.dbf;

import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFUtils;
import nl.knaw.dans.common.dbflib.*;
import org.apache.commons.io.FilenameUtils;
import org.ocr.common.utils.FileUtil;
import org.ocr.common.utils.IdUtil;
import org.ocr.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NUMBER,0, FLOAT,1, CHARACTER,2, LOGICAL,3, DATE,4, MEMO,5, GENERAL,6,
 * PICTURE,7, BINARY,8,
 *
 * @author liuhh
 * @create 2017-9-15
 * @modify 2017-9-15
 */
public class DBFUtil {
    private static Logger logger = LoggerFactory.getLogger(DBFUtil.class);
    public static final String TXT = ".txt";
    public static final String DBF = "dbf";
    public static final String DBF_FPT = ".fpt";
    public static final String CHARSET_GBK = "GBK";
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String FIELDS_TERMINATED_BY = ",";
    public static final String ENCLOSED_BY = "'";
    public static final String LINES_TERMINATED_BY = ";";

    /**
     * @param datas
     * @param txtFilePath
     * @param fields
     * @param extraValueList 额外的常量值，依次加入的txt每一行数据的结尾处
     * @throws IOException
     */
    public static void writeTxt(List<Map<String, Object>> datas, String txtFilePath, List<DBFEntity> fields, List<String> extraValueList) throws IOException {
        int fieldSize = fields.size();
        StringBuffer txtContent = new StringBuffer();
        for (int i = 0; i < datas.size(); i++) {
            Map<String, Object> map = datas.get(i);
            for (int j = 0; j < fieldSize; j++) {
                DBFEntity dbf = fields.get(j);
                txtContent.append(ENCLOSED_BY);
                Object value = map.get(dbf.getFieldName().toLowerCase());
                if (value == null) {
                    value = map.get(dbf.getFieldAliasName().toLowerCase());
                }
                if (value != null) {
                    value = value.toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("(\r\n|\r|\n|\n\r)", "<br>");
                } else {
                    value = "";
                }
                txtContent.append(value);
                txtContent.append(ENCLOSED_BY);
                if (j < (fieldSize - 1)) {
                    txtContent.append(FIELDS_TERMINATED_BY);
                }
            }
            if (extraValueList != null && extraValueList.size() > 0) {
                for (String extraValue : extraValueList) {
                    txtContent.append(FIELDS_TERMINATED_BY);
                    txtContent.append(ENCLOSED_BY);
                    txtContent.append(extraValue);
                    txtContent.append(ENCLOSED_BY);
                }
            }
            txtContent.append(LINES_TERMINATED_BY);
        }
        FileUtil.write(new File(txtFilePath), txtContent.toString(), CHARSET_UTF8, Boolean.TRUE);
    }

    /**
     * @param dbfFile
     * @param txtFilePath
     * @param fields
     * @param extraValueList 额外的常量值，依次加入的txt每一行数据的结尾处
     * @return
     * @throws Exception
     */
    public static int readDBFToFile(String dbfFile, String txtFilePath, List<DBFEntity> fields, List<String> extraValueList) throws Exception {
        List<Map<String, Object>> datas = new ArrayList<>();
        int count = 0;
        List<String> listRandom = new ArrayList<>(); //存放随机数的list
        DBFReader reader = null;
        try {
            reader = new DBFReader(new FileInputStream(dbfFile));
            reader.setCharset(Charset.forName(CHARSET_GBK));
            count = reader.getRecordCount();
            Object[] rowObjects;
            while ((rowObjects = reader.nextRecord()) != null) {
                Map<String, Object> dataMap = new HashMap<>();
                for (int i = 0; i < rowObjects.length; i++) {
                    DBFField field = reader.getField(i);
                    String key = StringUtil.trim(field.getName().toLowerCase());
                    dataMap.put(key, getValue(field, rowObjects[i]));
                }
                datas.add(dataMap);
                if (datas.size() >= 1000) {
                    writeTxt(datas, txtFilePath, fields, extraValueList);
                    datas = new ArrayList<>();
                }
            }
            if (datas.size() > 0) {
                writeTxt(datas, txtFilePath, fields, extraValueList);
            }
        } finally {
            DBFUtils.close(reader);
        }
        return count;
    }

    private static Object getValue(DBFField field, Object value) {
        char charcode = field.getType().getCharCode();
        if (value == null || StringUtil.trim(value.toString()) == "") {
            if ('C' == charcode || 'V' == charcode) {
                value = "";
            } else if ('D' == charcode || 'T' == charcode || '@' == charcode) {
                value = null;
            } else {
                value = 0;
            }
        } else {
            if ('C' == charcode || 'V' == charcode || 'M' == charcode) {
                value = ((String) value).trim();
            } else if ('L' == charcode) {
                value = (Boolean) value;
            } else if ('D' == charcode || 'T' == charcode || '@' == charcode) {
                value = (Date) value;
            } else if ('N' == charcode) {
                BigDecimal bg = new BigDecimal(value.toString()).setScale(field.getDecimalCount(), BigDecimal.ROUND_HALF_UP);
                if (field.getDecimalCount() > 0) {
                    value = bg.doubleValue();
                } else {
                    value = bg.longValue();
                }
            } else if ('F' == charcode || 'O' == charcode) {
                value = ((BigDecimal) value).doubleValue();
            } else if ('I' == charcode || '+' == charcode) {
                value = ((Double) value).longValue();
            } else {
                value = (byte[]) value;
            }
        }
        return value;
    }

    /**
     * @param dbfPath   文件的绝对路径
     * @param fieldList 数据中的key值与上面字段名一致，区分大小写
     * @throws Exception
     * @author DELL
     * @create 2017-8-24
     * @modify 2017-8-24
     */
    public static Table getTableByDans(String dbfPath, List<DBFEntity> fieldList) throws Exception {
        if (fieldList == null || fieldList.size() == 0) {
            throw new Exception("要构建的dbf字段为空");
        }
        Table personTable = null;
        try {
            boolean isWithMemo = false;
            File filePath = new File(dbfPath);
            File parentPath = new File(filePath.getParent());
            if (!parentPath.exists()) {
                parentPath.mkdirs();
            }
            List<Field> tableFields = new ArrayList<Field>();
            for (DBFEntity dbfEntity : fieldList) {
                if (dbfEntity.getType() == 'C') {
                    tableFields.add(new Field(dbfEntity.getFieldName(), Type.CHARACTER, dbfEntity.getLength()));
                } else if (dbfEntity.getType() == 'N') {
                    if (dbfEntity.getScale() > 0) {
                        tableFields
                                .add(new Field(dbfEntity.getFieldName(), Type.NUMBER, dbfEntity.getLength(), dbfEntity.getScale()));
                    } else {
                        tableFields.add(new Field(dbfEntity.getFieldName(), Type.NUMBER, dbfEntity.getLength()));
                    }
                } else if (dbfEntity.getType() == 'M') {
                    isWithMemo = true;
                    tableFields.add(new Field(dbfEntity.getFieldName(), Type.MEMO, dbfEntity.getLength()));
                } else if (dbfEntity.getType() == 'D') {
                    tableFields.add(new Field(dbfEntity.getFieldName(), Type.DATE));
                } else if (dbfEntity.getType() == 'F') {
                    if (dbfEntity.getScale() > 0) {
                        tableFields
                                .add(new Field(dbfEntity.getFieldName(), Type.FLOAT, dbfEntity.getLength(), dbfEntity.getScale()));
                    } else {
                        tableFields.add(new Field(dbfEntity.getFieldName(), Type.FLOAT, dbfEntity.getLength()));
                    }
                } else if (dbfEntity.getType() == 'L') {
                    tableFields.add(new Field(dbfEntity.getFieldName(), Type.LOGICAL));
                }
            }
            // 默认生成不存在备注类型字段的dbf
            if (isWithMemo) {
                // 针对表格中存在memo类型字段
                Database database = new Database(parentPath, Version.FOXPRO_26, CHARSET_GBK);
                personTable = database.addTable(filePath.getName(), tableFields);
                if (filePath.exists()) {
                    personTable.open();
                } else {
                    personTable.open(IfNonExistent.CREATE);
                    //先随便生成一条数据，然后再删除
                    personTable.addRecord(createExampleData(fieldList));
                    personTable.pack();
                    personTable.deleteRecordAt(0);
                    personTable.pack();
                }
            } else {
                personTable = new Table(filePath, Version.FOXPRO_26, tableFields, CHARSET_GBK);
                personTable.open(IfNonExistent.CREATE);
            }
        } catch (Exception e) {
            try {
                if (personTable != null) {
                    personTable.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            throw e;
        }
        return personTable;
    }

    /**
     * 此方法针对大数据量分批次输出到dbf文件，可以循环调用该方法
     * 需要配合上面的getTableByDans()获取Table对象，然后循环向dbf输出数据，
     * 此方法慎用，如果是最后一组数据输出，必须isFinish设置为true，否则设置为false；以便关闭Table流
     *
     * @param personTable
     * @param dbfFieldNameList
     * @param datas
     * @param isFinish
     * @throws Exception
     * @author DELL
     * @create 2017-8-24
     * @modify 2017-8-24
     */
    public static void writeDatasByDans(Table personTable, List<DBFEntity> dbfFieldNameList,
                                        List<Map<String, Object>> datas, boolean isFinish) throws Exception {
        try {
            if (datas == null || datas.size() == 0) {
                isFinish = true;
            } else {
                Map<String, Value> map;
                String fieldName;
                Object value;
                Value v;
                Record record;
                for (Map<String, Object> dataMap : datas) {
                    map = new HashMap<>();

                    for (DBFEntity dbfEntity : dbfFieldNameList) {
                        fieldName = dbfEntity.getFieldName().toLowerCase();
                        value = dataMap.get(fieldName);
                        v = null;
                        if (value != null) {
                            if (dbfEntity.getType() == 'C' || dbfEntity.getType() == 'M') {
                                //删除时间后面的.0字符
                                if ("kssj".equals(fieldName) && value.toString().contains(".0")) {
                                    int length = value.toString().length();
                                    v = new StringValue(value.toString().substring(0, length - 2), CHARSET_GBK);
                                } else {
                                    v = new StringValue(value.toString(), CHARSET_GBK);
                                }
                            } else if (dbfEntity.getType() == 'N' || dbfEntity.getType() == 'F') {
                                BigDecimal bg = new BigDecimal(value.toString()).setScale(dbfEntity.getScale(),
                                        BigDecimal.ROUND_HALF_UP);
                                if (dbfEntity.getScale() > 0) {
                                    v = new NumberValue(bg.doubleValue());
                                } else {
                                    v = new NumberValue(bg.longValue());
                                }
                            } else if (dbfEntity.getType() == 'D') {
                                v = new DateValue((Date) value);
                            } else if (dbfEntity.getType() == 'L') {
                                v = new BooleanValue((boolean) value);
                            }
                            map.put(dbfEntity.getFieldName(), v);
                        }
                    }
                    record = new Record(map);
                    personTable.addRecord(record);
                }
            }
        } catch (Exception e) {
            isFinish = true;
            e.printStackTrace();
            throw e;
        } finally {
            if (isFinish) {
                try {
                    personTable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Record createExampleData(List<DBFEntity> dbfList) {
        Map<String, Value> map = new HashMap<String, Value>();
        for (DBFEntity dbfEntity : dbfList) {
            Value v = null;
            if (dbfEntity.getType() == 'C' || dbfEntity.getType() == 'M') {
                v = new StringValue("test");
            } else if (dbfEntity.getType() == 'N' || dbfEntity.getType() == 'F') {
                if (dbfEntity.getScale() > 0) {
                    v = new NumberValue(1.0d);
                } else {
                    v = new NumberValue(1);
                }
            } else if (dbfEntity.getType() == 'D') {
                v = new DateValue(new Date());
            } else if (dbfEntity.getType() == 'L') {
                v = new BooleanValue(true);
            }
            map.put(dbfEntity.getFieldName(), v);
        }
        return new Record(map);
    }

    /**
     * @param dbfPath
     * @param txtFilePath
     * @param fields
     * @param extraValueList 额外的常量值，依次加入的txt每一行数据的结尾处
     * @throws Exception
     */
    public static void readWithMemo(String dbfPath, String txtFilePath, List<DBFEntity> fields, List<String> extraValueList) throws Exception {
        List<Map<String, Object>> datas = new ArrayList<>();
        DBFReader reader = null;
        try {
            File file = new File(dbfPath);
            String memofilename = FilenameUtils.getBaseName(file.getName()) + DBF_FPT;
            reader = new DBFReader(new FileInputStream(file));
            reader.setCharset(Charset.forName(CHARSET_GBK));
            reader.setMemoFile(new File(file.getParent() + File.separator + memofilename));

            Object[] rowObjects;
            while ((rowObjects = reader.nextRecord()) != null) {
                Map<String, Object> dataMap = new HashMap<>();
                for (int i = 0; i < rowObjects.length; i++) {
                    DBFField field = reader.getField(i);
                    String key = StringUtil.trim(field.getName().toLowerCase());
                    dataMap.put(key, getValue(field, rowObjects[i]));
                }
                datas.add(dataMap);
                if (datas.size() >= 1000) {
                    writeTxt(datas, txtFilePath, fields, extraValueList);
                    datas = new ArrayList<>();
                }
            }
            if (datas.size() > 0) {
                writeTxt(datas, txtFilePath, fields, extraValueList);
            }
        } finally {
            DBFUtils.close(reader);
        }
    }

    public static int getDBFDataSize(String dbfFile) {
        DBFReader reader = null;
        try {
            reader = new DBFReader(new FileInputStream(dbfFile));
            reader.setCharset(Charset.forName(CHARSET_GBK));
            return reader.getRecordCount();
        } catch (FileNotFoundException e) {
            logger.error("", e);
        } finally {
            DBFUtils.close(reader);
        }
        return -1;
    }

    public static File readFileToLocal(InputStream inputStream, String targetFile) {
        File file = new File(targetFile);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        String filePath = file.getParentFile().getAbsolutePath() + File.separator + IdUtil.uuid() + "_" + file.getName();
        File local = new File(filePath);
        return local;
    }

    public static List<Map<String, Object>> parseFile(File file, String[] titles) throws Exception {
        FileInputStream inputStream = new FileInputStream(file);
        DBFReader dbfReader = new DBFReader(inputStream, Charset.forName("UTF-8"), false);
        Object[] rowVales = null;
        List<Map<String, Object>> rowList = new ArrayList<>();
        while ((rowVales = dbfReader.nextRecord()) != null) {
            Map<String, Object> rowMap = new HashMap();
            for (int i = 0; i < rowVales.length; i++) {
                rowMap.put(titles[i], rowVales[i]);
            }
            rowList.add(rowMap);
        }
        return rowList;

    }

    /**
     * @param fields
     * @param extraFieldList 额外的数据库字段，字段必须是表的字段
     * @return
     */
    public static String getTableFieldStr(List<DBFEntity> fields, List<String> extraFieldList) {
        StringBuffer fieldStr = new StringBuffer();
        int fieldSize = fields.size();
        for (int j = 0; j < fieldSize; j++) {
            fieldStr.append(fields.get(j).getTableFieldName().toLowerCase());
            if (j < (fieldSize - 1)) {
                fieldStr.append(",");
            }
        }
        if (extraFieldList != null && extraFieldList.size() > 0) {
            for (String extraField : extraFieldList) {
                fieldStr.append(",");
                fieldStr.append(extraField);
            }
        }
        return fieldStr.toString();
    }

    /**
     * @param sqlFilePath
     * @param tableName
     * @param fields
     * @param extraFieldList 额外的数据库字段，字段必须是表的字段
     * @return
     */
    public static String generateSql(String sqlFilePath, String tableName, List<DBFEntity> fields, List<String> extraFieldList) {
        StringBuffer sql = new StringBuffer();
        sql.append("LOAD DATA LOCAL INFILE '");
        sql.append(sqlFilePath.replaceAll("\\\\", "\\\\\\\\"));
        sql.append("' ");
        sql.append(" INTO TABLE ");
        sql.append(tableName);
        // 指定编码格式会报错，不知道为何
        // sql.append(" CHARACTER SET UTF8MB4 FIELDS TERMINATED BY '");
        sql.append(" FIELDS TERMINATED BY '");
        sql.append(FIELDS_TERMINATED_BY);
        sql.append("' ENCLOSED BY '\\");
        sql.append(ENCLOSED_BY);
        sql.append("' LINES TERMINATED BY '");
        sql.append(LINES_TERMINATED_BY);
        sql.append("' (");
        sql.append(getTableFieldStr(fields, extraFieldList));
        sql.append(");");
        logger.info("sql语句: {}", sql);
        return sql.toString();
    }

    /**
     * 不支持带memo类型字段
     *
     * @param dbfPath
     * @param clazz
     * @param extraFieldMap 额外的字段赋值，key为对象属性值（必须大小写一致），value为值
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> List<T> readDbfByAnnotation(String dbfPath, Class<T> clazz, Map<String, String> extraFieldMap) throws Exception {
        List<T> dataLit = new ArrayList<>();
        DBFReader reader = null;
        try {
            File file = new File(dbfPath);
            reader = new DBFReader(new FileInputStream(file));
            reader.setCharset(Charset.forName(CHARSET_GBK));
            Object[] rowObjects;
            while ((rowObjects = reader.nextRecord()) != null) {
                T obj = null;
                for (int i = 0; i < rowObjects.length; i++) {
                    DBFField dbfField = reader.getField(i);
                    String key = StringUtil.trim(dbfField.getName().toLowerCase());
                    for (java.lang.reflect.Field declaredField : clazz.getDeclaredFields()) {
                        if (declaredField.isAnnotationPresent(DBFColumn.class)) {
                            DBFColumn annotation = declaredField.getAnnotation(DBFColumn.class);
                            if (annotation.value().equalsIgnoreCase(key)) {
                                Object value = getValue(dbfField, rowObjects[i]);
                                if (value != null) {
                                    if (obj == null) {
                                        obj = clazz.newInstance();
                                    }
                                    declaredField.setAccessible(true);
                                    // 判断是否是基本类型的
                                    if (declaredField.getType().isPrimitive()) {
                                        value = StringUtil.cast(value);
                                        declaredField.set(obj, value);
                                    } else {
                                        Class<?> type = declaredField.getType();
                                        String typeName = type.getSimpleName();
                                        switch (typeName) {
                                            case "Object":
                                                // 处理 String 类型的逻辑
                                                declaredField.set(obj, value);
                                                break;
                                            case "String":
                                                // 处理 String 类型的逻辑
                                                declaredField.set(obj, value);
                                                break;
                                            case "Integer":
                                                // 处理 Integer 类型的逻辑
                                                declaredField.set(obj, value);
                                                break;
                                            case "Double":
                                                // 处理 Double 类型的逻辑
                                                declaredField.set(obj, value);
                                                break;
                                            case "Long":
                                                // 处理 Long 类型的逻辑
                                                declaredField.set(obj, value);
                                                break;
                                            case "BigDecimal":
                                                // 处理 BigDecimal 类型的逻辑
                                                declaredField.set(obj, new BigDecimal(value.toString()));
                                                break;
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                if (obj != null) {
                    if (extraFieldMap != null && !extraFieldMap.isEmpty()) {
                        for (String key : extraFieldMap.keySet()) {
                            java.lang.reflect.Field declaredField = clazz.getDeclaredField(key);
                            if (declaredField != null) {
                                String value = extraFieldMap.get(key);
                                declaredField.setAccessible(true);
                                declaredField.set(obj, value);
                            }
                        }
                    }
                    dataLit.add(obj);
                }
            }
        } finally {
            DBFUtils.close(reader);
        }
        return dataLit;
    }

    public static void main(String[] args) throws Exception {
        String input = "55≤分值<=60";
        Pattern pattern = Pattern.compile("(\\d+|≤|＜|<=)");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }

}
