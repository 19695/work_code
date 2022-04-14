package org.colm.code.sftp.tool;

import org.colm.code.sftp.constant.SftpConnectParam;

public interface ConnectParam {

    SftpConnectParam getConnectParam(String clientId);

    /**
     * 实现此方法，内部声明实现类
     * @param <T>
     * @return
     */
    <T extends ConnectParam> T get();

    default void inject() {
        ConnectParamTool.from(get());
    }
}
