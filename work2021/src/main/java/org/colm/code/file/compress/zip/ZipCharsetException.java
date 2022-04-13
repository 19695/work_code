package org.colm.code.file.compress.zip;

public class ZipCharsetException extends Exception {
    public ZipCharsetException() {
        super();
    }

    public ZipCharsetException(String message) {
        super(message);
    }

    public ZipCharsetException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZipCharsetException(Throwable cause) {
        super(cause);
    }

    protected ZipCharsetException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
