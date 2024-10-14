package org.ocr.common.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;

public class FileUtil extends FileUtils {
    public static final String ENV = "java.io.tmpdir";
    public static File createDir(String tempFilePath) {
        // 获取临时目录并打印。
        String tempDir = System.getProperty(ENV);
        File tempFile = new File(tempDir + File.separator + tempFilePath + File.separator + IdUtil.uuid());
        // 判断是否存在 不存在创建
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }
        return tempFile;
    }
}
