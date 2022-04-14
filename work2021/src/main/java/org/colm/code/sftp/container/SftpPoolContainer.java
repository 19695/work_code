package org.colm.code.sftp.container;

import org.colm.code.sftp.constant.SftpConnectParam;
import org.colm.code.sftp.constant.SftpDefaultConstant;
import org.colm.code.sftp.pool.SftpClientConfigure;
import org.colm.code.sftp.pool.SftpClientPool;
import org.colm.code.sftp.tool.ConnectParamTool;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

public class SftpPoolContainer {

    private static final ConcurrentHashMap<String, SftpClientPool> POOL_CONTAINER = new ConcurrentHashMap<>();
    private static volatile LocalDateTime cursor = LocalDateTime.now();
    private static final long gap = 10L; // minute

    public synchronized static void remove(String clientId) throws Exception {
        SftpClientPool clientPool = POOL_CONTAINER.get(clientId);
        if (clientPool != null)
            clientPool.clear();
    }

    public synchronized static SftpClientPool get(String clientId) {
        refresh();
        return POOL_CONTAINER.computeIfAbsent(clientId, id -> {
            try {
                SftpConnectParam param = ConnectParamTool.getParam(id);
                SftpClientConfigure configure = SftpClientConfigure.newInstance(param.getHost(), param.getPort(),
                        SftpDefaultConstant.SFTP_USER,
                        SftpDefaultConstant.SFTP_PASSWORD,
                        SftpDefaultConstant.REMOTE_HOME_PATH,
                        SftpDefaultConstant.CONNECT_TIMEOUT);
                return new SftpClientPool(configure);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void refresh() {
        LocalDateTime now = LocalDateTime.now();
        Duration between = Duration.between(cursor, now);
        if (between.toMillis() > gap) {
            cursor = now;
            clear();
        }
    }

    public synchronized static void clear() {
        if (!POOL_CONTAINER.isEmpty()) {
            POOL_CONTAINER.forEachKey(1, t -> {
                try {
                    remove(t);
                } catch (Exception e) {}
            });
        }
        POOL_CONTAINER.clear();
    }

}
