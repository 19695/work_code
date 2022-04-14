package org.colm.code.sftp.constant;

import org.springframework.beans.factory.annotation.Value;

public class SftpPoolDefaultConstant {

    public static int POOL_CORE_SIZE = 1;
    public static int POOL_MAX_SIZE = 1;
    public static int POOL_OFFER_TIMEOUT = 3;

    @Value("${sftp.pool.defaultSize:1}")
    public void setPoolDefaultSize(int size) {
        POOL_CORE_SIZE = size;
    }

    @Value("${sftp.pool.maxSize:1}")
    public void setPoolMaxSize(int size) {
        POOL_MAX_SIZE = size;
    }

    @Value("${sftp.pool.timeout:3}")
    public void setPoolTimeout(int size) {
        POOL_OFFER_TIMEOUT = size;
    }

}
