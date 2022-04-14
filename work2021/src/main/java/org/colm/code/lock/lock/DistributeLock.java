package org.colm.code.lock.lock;

import org.colm.code.lock.exception.LockException;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public interface DistributeLock<T> {

    /**
     * 获取锁
     * @param key   锁字段
     * @param timeout   超时时间
     * @param timeUnit  时间单位
     * @param success   加锁成功处理
     * @param failure   加锁失败处理
     * @return
     */
    T getLock(String key, long timeout, TimeUnit timeUnit, Function<String, T> success, Function<String, T> failure);

    /**
     * 释放锁
     * @param key   锁字段
     * @param success   加锁成功处理
     * @param failure   加锁失败处理
     */
    void releaseLock(String key, Function<String, T> success, Function<String, T> failure) throws LockException;

    T getLock(String key, long timeout, TimeUnit timeUnit);

    void releaseLock(String key) throws LockException;

    void renewal(String key);

}
