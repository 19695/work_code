package org.colm.code.sftp.pool;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SftpClientConfigure {

    private String host;
    private int port;
    private String username;
    private String password;
    private String hostPath;
    private int retryTimes;
    private int clientTimeout;
    private int connectTimeout;

    public SftpClientConfigure(String host, int port, String username, String password, String hostPath,
                               int retryTimes, int clientTimeout, int connectTimeout) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.hostPath = hostPath;
        this.retryTimes = retryTimes;
        this.clientTimeout = clientTimeout;
        this.connectTimeout = connectTimeout;
    }

   public static SftpClientConfigure newInstance(String host, int port, String username, String password,
                                                 String hostPath, int connectTimeout) {
        return newInstance(host, port, username, password, hostPath, connectTimeout, 0, 0);
   }

    public static SftpClientConfigure newInstance(String host, int port, String username, String password,
                                                  String hostPath, int connectTimeout, int retryTimes, int clientTimeout) {
        return SftpClientConfigure.builder().host(host).port(port).username(username).password(password)
                .host(hostPath).connectTimeout(connectTimeout).retryTimes(retryTimes).clientTimeout(clientTimeout)
                .build();
    }

}
