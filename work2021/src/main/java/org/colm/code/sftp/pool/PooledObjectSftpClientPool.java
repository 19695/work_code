package org.colm.code.sftp.pool;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;

import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.colm.code.sftp.constant.SftpPoolDefaultConstant.*;

/**
 * 2.4.2 和 2.11.1 有一些 api 差别
 *
 * 2.4.2 中 实现 ObjectPool<SftpClient> 直接持有目标对象
 */
public class PooledObjectSftpClientPool implements ObjectPool<PooledObject<SftpClient>> {

    private final BlockingQueue<PooledObject<SftpClient>> pool;
    private final SftpClientFactory factory;
    private volatile int active = 0;
    private volatile int coreSize;
    private volatile int maxSize;

    public PooledObjectSftpClientPool(SftpClientFactory factory) throws Exception {
        this(POOL_MAX_SIZE, factory);
    }

    public PooledObjectSftpClientPool(int maxSize, SftpClientFactory factory) throws Exception {
        this(POOL_CORE_SIZE, Math.max(maxSize, POOL_MAX_SIZE), factory);
    }

    public PooledObjectSftpClientPool(int coreSize, int maxSize, SftpClientFactory factory) throws Exception {
        this.factory =factory;
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        pool = new ArrayBlockingQueue<>(maxSize);
        
        initPool();
    }

    private void initPool() throws Exception {
        while(maxSize-- > 0) {
            addObject();
        }
    }

    private PooledObject<SftpClient> addAndGetObject() throws Exception {
        PooledObject<SftpClient> makeObject = null;
        offerObject(makeObject);
        makeObject = factory.makeObject();
        return makeObject;
    }

    private boolean offerObject(PooledObject<SftpClient> makeObject) throws InterruptedException {
        return pool.offer(makeObject, POOL_OFFER_TIMEOUT, TimeUnit.SECONDS);
    }


    @Override
    public void addObject() throws Exception, IllegalStateException, UnsupportedOperationException {
        addAndGetObject();
    }

    @Override
    public PooledObject<SftpClient> borrowObject() throws Exception, NoSuchElementException, IllegalStateException {
        PooledObject<SftpClient> pooledObject = pool.take();
        if (pooledObject == null) {
            pooledObject = addAndGetObject();
        } else if (!factory.validateObject(pooledObject)){
            invalidateObject(pooledObject);
            pooledObject = addAndGetObject();
        }
        active++;
        return pooledObject;
    }

    @Override
    public void clear() throws Exception, UnsupportedOperationException {
        if (CollectionUtils.isNotEmpty(pool)) {
            for (PooledObject<SftpClient> pooledObject : pool) {
                factory.destroyObject(pooledObject);
            }
        }
        if (pool != null)
            pool.clear();
    }

    @Override
    public void close() {
        if (CollectionUtils.isNotEmpty(pool)) {
            try {
                for (PooledObject<SftpClient> pooledObject : pool) {
                    factory.destroyObject(pooledObject);
                }
            } catch (Exception e) {}
        }
    }

    @Override
    public int getNumActive() {
        return active;
    }

    @Override
    public int getNumIdle() {
        return pool.size();
    }

    @Override
    public void invalidateObject(PooledObject<SftpClient> pooledObject) throws Exception {
        if (pooledObject != null) {
            factory.destroyObject(pooledObject);
        }
    }

    @Override
    public void returnObject(PooledObject<SftpClient> pooledObject) throws Exception {
        if (pooledObject != null && !offerObject(pooledObject)) {
            factory.destroyObject(pooledObject);
        }
        active--;
    }
}
