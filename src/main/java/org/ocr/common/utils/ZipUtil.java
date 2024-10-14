package org.ocr.common.utils;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.*;

@Slf4j
public class ZipUtil {
    public static final int BUFFER_SIZE = 1024 * 1024;

    /**
     * Deflate
     * 压缩字节数组
     *
     * @param byteArray
     * @return
     * @author change
     * @create 2018-1-14
     * @modify 2018-1-14
     */
    public static byte[] compress(byte[] byteArray) {
        byte[] ba = null;
        Deflater compressor = new Deflater();
        compressor.reset();
        compressor.setInput(byteArray);
        compressor.finish();
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int length = 0;
            while (!compressor.finished()) {
                length = compressor.deflate(buffer);
                baos.write(buffer, 0, length);
            }
            ba = baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        compressor.end();
        return ba;
    }

    /**
     * Deflate
     * 解压字节数组
     *
     * @param byteArray
     * @return
     * @author change
     * @create 2018-1-14
     * @modify 2018-1-14
     */
    public static byte[] decompress(byte[] byteArray) {
        byte[] ba = null;
        Inflater decompressor = new Inflater();
        decompressor.reset();
        decompressor.setInput(byteArray);
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int length = 0;
            while (!decompressor.finished()) {
                length = decompressor.inflate(buffer);
                baos.write(buffer, 0, length);
            }
            ba = baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        decompressor.end();
        return ba;
    }

    private static void compress(File sourceFile, ZipOutputStream outputStream, String name, boolean KeepDirStructure) throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        if (sourceFile.isFile()) {
            outputStream.putNextEntry(new ZipEntry(name));
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if (KeepDirStructure) {
                    // 空文件夹的处理
                    outputStream.putNextEntry(new ZipEntry(name + "/"));
                    // 没有文件，不需要文件的copy
                    outputStream.closeEntry();
                }
            } else {
                for (File file : listFiles) {
                    // 判断是否需要保留原来的文件结构
                    if (KeepDirStructure) {
                        compress(file, outputStream, name + "/" + file.getName(), KeepDirStructure);
                    } else {
                        compress(file, outputStream, file.getName(), KeepDirStructure);
                    }

                }
            }
        }
    }

    /**
     * GZIP
     * 压缩字节数组
     *
     * @return
     * @author change
     * @create 2018-1-14
     * @modify 2018-1-14
     */
    public static byte[] gzip(byte[] byteArray) {
        byte[] ba = null;
        ByteArrayOutputStream baos = null;
        GZIPOutputStream gzip = null;
        try {
            baos = new ByteArrayOutputStream();
            gzip = new GZIPOutputStream(baos);
            gzip.write(byteArray);
            gzip.finish();
            ba = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (gzip != null) {
                try {
                    gzip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ba;
    }

    /**
     * GZIP
     * 解压字符数组
     *
     * @param byteArray
     * @return
     * @author change
     * @create 2018-1-14
     * @modify 2018-1-14
     */
    public static byte[] unGzip(byte[] byteArray) {
        int length = 0;
        byte[] ba = null;
        ByteArrayInputStream bais = null;
        ByteArrayOutputStream baos = null;
        GZIPInputStream gzip = null;
        try {
            bais = new ByteArrayInputStream(byteArray);
            gzip = new GZIPInputStream(bais);
            baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((length = gzip.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            ba = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bais != null) {
                try {
                    bais.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (gzip != null) {
                try {
                    gzip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ba;
    }

    public static boolean unzip(File targetFile, File outFile, String password) throws IOException {
        return unzip(targetFile, outFile, password, null);
    }

    public static boolean unzip(File targetFile, File outFile, Charset charset) throws IOException {
        return unzip(targetFile, outFile, null, charset);
    }

    public static boolean unzip(File targetFile, File outFile) throws IOException {
        return unzip(targetFile, outFile, null, null);
    }

    public static boolean unzip(File targetFile, File outFile, String password, Charset charset) throws IOException {
        if (targetFile == null || outFile == null) {
            throw new NullPointerException("文件不能为空");
        }

        if (!targetFile.exists()) {
            throw new FileNotFoundException(targetFile.getAbsolutePath());
        }

        if (!outFile.exists()) {
            outFile.mkdirs();
        }

        ZipFile zipFile = new ZipFile(targetFile);
        if (charset != null) {
            zipFile.setCharset(charset);
        }
        if (!zipFile.isValidZipFile()) {
            throw new IllegalArgumentException("zip文件格式不正确");
        }
        if (zipFile.isEncrypted()) {
            if (StringUtil.isEmpty(password)) {
                throw new NullPointerException("解压密码不能为空");
            }
            zipFile.setPassword(password.toCharArray());
        }
        try {
            UnzipParameters parameter = new UnzipParameters();
            zipFile.extractAll(outFile.getAbsolutePath(), parameter);
            return true;
        } catch (ZipException e) {
            return false;
        }
    }

    public static void zipByFileList(List<File> srcFiles, OutputStream out) throws IOException {
        long start = System.currentTimeMillis();
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(out);
            for (File srcFile : srcFiles) {
                byte[] buf = new byte[BUFFER_SIZE];
                zos.putNextEntry(new ZipEntry(srcFile.getName()));
                int len;
                FileInputStream in = new FileInputStream(srcFile);
                while ((len = in.read(buf)) != -1) {
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
                in.close();
            }
            long end = System.currentTimeMillis();
            log.info("压缩完成，耗时：{}", (end - start) + " ms");
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void zipByDir(File targetDir, File outFile, boolean includeRootFolder) throws ZipException {
        zipByDir(targetDir, outFile, null, includeRootFolder);
    }

    public static void zipByDir(File targetDir, File outFile, String password, boolean includeRootFolder) throws ZipException {
        // 创建ZipParameters对象，用于设置压缩参数
        ZipParameters zipParameters = new ZipParameters();
        // 设置压缩方法
        zipParameters.setCompressionMethod(CompressionMethod.DEFLATE);
        // 设置压缩级别
        zipParameters.setCompressionLevel(CompressionLevel.NORMAL);
        zipParameters.setIncludeRootFolder(includeRootFolder);
        // 创建ZipFile对象，并执行压缩操作
        ZipFile zipFile;
        if (StringUtil.isBlank(password)) {
            zipFile = new ZipFile(outFile);
        } else {
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
            zipFile = new ZipFile(outFile, password.toCharArray());
        }
        zipFile.setCharset(Charset.forName("GBK"));
        zipFile.addFolder(targetDir, zipParameters);
    }

}
