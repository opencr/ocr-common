package org.ocr.common.utils.dll;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * html转图片
 *
 * @author: liuhh
 * @since: 1.0
 * @date: 2019/8/29 14:08
 */
public interface HtmlToImage extends Library {

    HtmlToImage INSTANCE = Native.load("HtmlToImage", HtmlToImage.class);

    /**
     * 1、初始化控件
     *
     * @param iWidth：图片宽度[100,2000]； iMinHeight：最少高度[100,2000]；
     *                               iBodyContentAdjustWidth：内容边框调整宽度(默认：20)；
     *                               szImageFormat:图片输出格式，可选(bmp, jpg,gif,png)；
     *                               iMinImageFileLength：最少文件长度(默认：1024);
     *                               bDebugLog:日志输出标记（0，1）；
     *                               iLogLevel：保留；
     * @return 0： 初始化成功；>0 初始化失败；
     * 1：未知；
     * 2：申请对话框资源失败；
     * 3：新建对话框失败；
     * 4：初始化GDI失败；
     * 5：获得图片转换格式失败；
     * 6：初始化异常；
     */
    int WebInit(int iWidth, int iMinHeight, int iBodyContentAdjustWidth,
                String szImageFormat, int iMinImageFileLength, boolean bDebugLog, int iLogLevel);

    /**
     * 2.Html文件转图片
     *
     * @param pUrl        html文件路径；
     * @param pFileName   图片输出路径；
     * @param bRealResult
     * @return 0：转换成功；>0 转换失败；
     * 1：未知；
     * 2：动态库没有初始化
     * 3：参数有误
     * 4：解析通道忙
     * 5：html文件不存在
     */
    int WebHtmlToImage(String pUrl, String pFileName, boolean bRealResult);

    /**
     * 获得文件转换状态值
     *
     * @param iFileID 当前没有收到文件转换状态值；
     * @return
     */
    int WebGetHtmlToImageResult(int iFileID);

    /**
     * 3.释放控件资源
     *
     * @return
     */
    int WebRelease();

    /**
     * 获取动态模块缓存中未生成图片的个数
     *
     * @param QueueSize
     * @return
     */
    int WebGetQueueSize(int QueueSize);
}
