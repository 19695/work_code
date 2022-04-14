package org.colm.code.lock.lock;

import org.colm.code.lock.exception.LockException;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public abstract class AbstractDistributeLock<T> implements DistributeLock<T> {

    @Override
    public T getLock(String key, long timeout, TimeUnit timeUnit, Function<String, T> success, Function<String, T> failure) {
        T t = null;
        try {
            doLock(key, timeout, timeUnit);
            t = success.apply(key);
        } catch (Exception e) {
            t = failure.apply(key);
        }
        return t;
    }

    @Override
    public void releaseLock(String key, Function<String, T> success, Function<String, T> failure) throws LockException {
        try {
            doRelease(key);
            success.apply(key);
        } catch (Exception e) {
            failure.apply(key);
        }
    }

    @Override
    public T getLock(String key, long timeout, TimeUnit timeUnit) {
        return getLock(key, timeout, timeUnit, getLockSuccess(key), getLockFailure(key, timeout, timeUnit));
    }

    @Override
    public void releaseLock(String key) throws LockException {
        releaseLock(key, releaseLockSuccess(key), releaseLockFailure(key));
    }

    protected abstract void doLock(String key, long timeout, TimeUnit timeUnit);

    protected abstract boolean doRelease(String key);

    protected abstract Function<String,T> getLockSuccess(String key);

    protected abstract Function<String,T> getLockFailure(String key, long timeout, TimeUnit timeUnit);

    protected abstract Function<String,T> releaseLockSuccess(String key);

    protected abstract Function<String,T> releaseLockFailure(String key) throws LockException;

}
