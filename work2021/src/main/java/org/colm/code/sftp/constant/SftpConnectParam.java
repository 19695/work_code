package org.colm.code.sftp.constant;

import lombok.Data;

@Data
public class SftpConnectParam {

    private String clientId;
    private String host;
    private int port;

}
