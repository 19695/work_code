package org.colm.code.sftp.constant;

import org.springframework.beans.factory.annotation.Value;

public class SftpDefaultConstant {

    public static String SFTP_USER;
    public static String SFTP_PASSWORD;
    public static String REMOTE_HOME_PATH;
    public static int CONNECT_TIMEOUT;

    @Value("")
    public void setUser(String user) {
        SFTP_USER = user;
    }

    @Value("")
    public void setPassword(String password) {
        SFTP_PASSWORD = password;
    }

    @Value("")
    public void setPath(String path) {
        REMOTE_HOME_PATH = path;
    }

    @Value("")
    public void setTimeout(int timeout) {
        CONNECT_TIMEOUT = timeout;
    }

}
