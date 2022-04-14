package org.colm.code.sftp.tool;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.colm.code.sftp.container.SftpPoolContainer;
import org.colm.code.sftp.pool.SftpClient;
import org.colm.code.sftp.pool.SftpClientPool;

@AllArgsConstructor
@Data
public class Assistant {

    private SftpClient client;
    private SftpClientPool pool;
    private String clientId;

    public static Assistant of(String clientId) throws Exception {
        SftpClientPool clientPool = SftpPoolContainer.get(clientId);
        return new Assistant(clientPool.borrowObject(), clientPool, clientId);
    }

    public void returnConnect() throws Exception {
        pool.returnObject(client);
    }

}
