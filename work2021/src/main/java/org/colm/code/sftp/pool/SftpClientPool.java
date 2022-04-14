package org.colm.code.sftp.pool;

import org.apache.commons.pool2.impl.DefaultPooledObject;

public class SftpClientPool {

    private final PooledObjectSftpClientPool proxy;

    public SftpClientPool(SftpClientConfigure configure) throws Exception {
        this.proxy = new PooledObjectSftpClientPool(new SftpClientFactory(configure));
    }

    public SftpClient borrowObject() throws Exception {
        return proxy.borrowObject().getObject();
    }

    public void clear() throws Exception {
        proxy.clear();
    }

    public void close() {
        proxy.close();
    }

    public int getNumActive() {
        return proxy.getNumActive();
    }

    public int getNumIdle() {
        return proxy.getNumIdle();
    }

    public void invalidateObject(SftpClient sftpClient) throws Exception {
        proxy.invalidateObject(new DefaultPooledObject<>(sftpClient));
    }

    public void returnObject(SftpClient sftpClient) throws Exception {
        proxy.returnObject(new DefaultPooledObject<>(sftpClient));
    }

}
