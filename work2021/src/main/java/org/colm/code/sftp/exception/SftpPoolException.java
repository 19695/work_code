package org.colm.code.sftp.exception;

public class SftpPoolException extends Exception{

    public SftpPoolException() {
        super();
    }

    public SftpPoolException(String message) {
        super(message);
    }

    public SftpPoolException(String message, Throwable cause) {
        super(message, cause);
    }

    public SftpPoolException(Throwable cause) {
        super(cause);
    }

    protected SftpPoolException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
