package org.ocr.common.utils.dll;

import com.sun.jna.Library;
import com.sun.jna.Native;

import java.nio.ByteBuffer;

/**
 * 通用安全模块
 *
 * @author: liuhh
 * @since: 1.0
 * @date: 2019/8/29 14:08
 */
public interface SecModule64 extends Library {
    SecModule64 INSTANCE = Native.load("SecModule_x64", SecModule64.class);

    /**
     * 文件加密
     *
     * @param szPass       密码
     * @param szSourceFile 要加密的文件
     * @return 0-成功 <0-失败
     */
    int FileSign(String szPass, String szSourceFile);

    /**
     * 文件解密
     *
     * @param szPass       密码
     * @param szSourceFile 要加密的文件
     * @return 0-成功 <0-失败
     */
    int FileVerify(String szPass, String szSourceFile);

    /**
     * @param szSource 需要加密的字符串
     * @param szTarget 加密后的字符串，1个字母加密后为2个字符，1个汉字加密后为4个字符
     * @return 0-成功
     */
    int EncrypString(String szSource, ByteBuffer szTarget);

    /**
     * 字符串解密
     *
     * @param szSource 需要解密的字符串
     * @param szTarget 解密后的字符串
     * @return 0-成功
     */
    int DecrypString(String szSource, ByteBuffer szTarget);

    /**
     * 生成个人考试补时密码，生成个人迟到密码，整场考试密码，单机考试密码
     *
     * @param strExamID 每个考场场次唯一ID
     * @param iType     1-个人补时 2-整场考试密码 3-个人迟到密码 4-单机考试密码
     * @param iTime     补时时长,分钟，最小0
     * @param strPass   考试密码
     * @return 0-成功
     */
    int GenExamPass(String strExamID, int iType, int iTime, ByteBuffer strPass);

    /**
     * 验证个人考试补时密码、个人迟到密码、整场考试密码、单机考试密码
     *
     * @param strPass   考试密码
     * @param strExamID 每个考场场次唯
     * @param iType     1-个人补时 2-整场考试密码 3-个人迟到密码 4-单机考试密码
     * @param strTime   补时时长，分钟，可为负数
     * @return 0-验证成功  1-验证失败
     */
    int CheckExamPass(String strPass, String strExamID, int iType, ByteBuffer strTime);

    /**
     * 得到计算机唯一序号
     *
     * @param strSN 计算机序号
     * @return 0:成功   其它-失败
     */
    int GetJSJSN(String strSN);

    /**
     * @param FileName1   主路径
     * @param FileName2   split包路径
     * @param strFileName 最总包
     * @return 0:成功   其它-失败
     */
    int mergeFile(String FileName1, String FileName2, String strFileName);

    /**
     * 拆分zip包
     *
     * @param strFileName 最总包
     * @param FileName1   split路径
     * @param FileName2   主包路径
     * @return 0:成功   其它-失败
     */
    int splitFile(String strFileName, String FileName1, String FileName2);

    /**
     * 字符串加密
     *
     * @param toMsg              加密后的字符串
     * @param nOutEncryptBufSize 空间大小参数 暂定1024
     * @param pszPublicKey       公钥
     * @param fromMsg            需要加密的字符串，1个字母加密后为2个字符，1个汉字加密后为4个字符
     * @return 0-成功
     */
    int GCE_Encrypt(ByteBuffer toMsg, int nOutEncryptBufSize, String pszPublicKey, String fromMsg);

    /**
     * 字符串解密
     *
     * @param toMsg              解密后的字符串
     * @param nOutDecryptBufSize 空间大小参数 暂定1024
     * @param pszPrivateKey      解密私钥 需要解密的字符串
     * @param fromMsg            需要解密的字符串
     * @return 0-成功
     */
    int GCE_Decrypt(ByteBuffer toMsg, int nOutDecryptBufSize, String pszPrivateKey, String fromMsg);
}
