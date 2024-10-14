package org.ocr.common.exception;

/**
 * XML全局异常
 *
 * @author admin
 */
public class ZipException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误提示
     */
    private String message;

    /**
     * 错误明细，内部调试错误
     */
    private String detailMessage;

    /**
     * 空构造方法，避免反序列化问题
     */
    public ZipException() {
    }

    public ZipException(String message) {
        this.message = message;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public ZipException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ZipException setMessage(String message) {
        this.message = message;
        return this;
    }
}
