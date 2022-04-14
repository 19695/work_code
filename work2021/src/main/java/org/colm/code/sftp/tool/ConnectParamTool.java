package org.colm.code.sftp.tool;

import org.colm.code.sftp.constant.SftpConnectParam;

public class ConnectParamTool {

    private static ConnectParamTool instance;

    /*
        接口的不同实现类完成不同的存储介质读取
     */
    private final ConnectParam connectParam;

    private ConnectParamTool(ConnectParam connectParam) {
        this.connectParam = connectParam;
    }

    /**
     * 通过此方法将依赖注入
     * @param connectParam
     * @return
     */
    public static ConnectParamTool from(ConnectParam connectParam) {
        if (instance == null) {
            instance = new ConnectParamTool(connectParam);
        }
        return instance;
    }

    public static SftpConnectParam getParam(String clientId) {
        return instance.connectParam.getConnectParam(clientId);
    }

}
