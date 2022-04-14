package org.colm.code.sftp.pool;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.colm.code.sftp.exception.SftpPoolException;

/**
 * 2.4.2 和 2.11.1 有一些 api 差别
 *
 * 2.4.2 中 implements PooledObjectFactory<SftpClient>
 */
public class SftpClientFactory extends BasePooledObjectFactory<SftpClient> {

    private final SftpClientConfigure configure;

    public SftpClientFactory(SftpClientConfigure configure) {
        this.configure = configure;
    }

    @Override
    public boolean validateObject(PooledObject<SftpClient> pooledObject) {
        SftpClient sftpClient = pooledObject.getObject();
        if (sftpClient != null)
            return sftpClient.isConnected();
        return false;
    }

    @Override
    public PooledObject<SftpClient> wrap(SftpClient sftpClient) {
        return new DefaultPooledObject<>(sftpClient);
    }

    @Override
    public void destroyObject(PooledObject<SftpClient> pooledObject) throws Exception {
        SftpClient sftpClient = pooledObject.getObject();
        if (sftpClient != null)
            sftpClient.disconnect();
    }

    @Override
    public void activateObject(PooledObject<SftpClient> pooledObject) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public SftpClient create() throws Exception {
        SftpClient sftpClient = new SftpClient(configure);
        if (!sftpClient.isSessionConnected()) {
            sftpClient.disconnect();
            throw new SftpPoolException("host server connect failure");
        }
        if (!sftpClient.connect()) {
            throw new SftpPoolException("sftp connect failure");
        }
        return sftpClient;
    }

    @Override
    public void passivateObject(PooledObject<SftpClient> pooledObject) throws Exception {
        throw new UnsupportedOperationException();
    }

}
