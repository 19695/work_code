package org.colm.code.lock.lock.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.colm.code.DateTimeUtil;
import org.colm.code.lock.exception.LockException;
import org.colm.code.lock.lock.AbstractDistributeLock;
import org.colm.code.lock.mapper.LockConflictMapper;
import org.colm.code.lock.mapper.LockMapper;
import org.colm.code.lock.po.LockWithDatabase;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class DatabaseDistributeLock extends AbstractDistributeLock<Boolean> {

    // 为 0 禁用重试
    private static final int RETRY_TIMES = 0;
    private static ThreadLocal<AtomicInteger> retryThreadLocal = ThreadLocal.withInitial(
            () -> new AtomicInteger(RETRY_TIMES));

    @Autowired
    private LockMapper lockMapper;

    @Autowired
    private LockConflictMapper lockConflictMapper;

    @Override
    protected void doLock(String key, long timeout, TimeUnit timeUnit) {
        synchronized (key) {
            LocalDateTime now = LocalDateTime.now();
            Date invalidDate = DateTimeUtil.asDate(now.plusSeconds(timeUnit.toSeconds(timeout)));
            Date date = DateTimeUtil.asDate(now);
            lockMapper.insert(new LockWithDatabase(key, 0L, LockStatus.LOCK.getStatus(), invalidDate, date, date));
        }
    }

    @Override
    protected boolean doRelease(String key) {
        synchronized (key) {
            moveConflict(key);
            LambdaQueryWrapper<LockWithDatabase> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LockWithDatabase::getLockKey, key);
            return lockMapper.delete(wrapper) > 0;
        }
    }

    @Override
    protected Function<String, Boolean> getLockSuccess(String key) {
        return t -> true;
    }

    @Override
    protected Function<String, Boolean> getLockFailure(String key, long timeout, TimeUnit timeUnit) {
        synchronized (key) {
            return t -> {
                LambdaQueryWrapper<LockWithDatabase> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(LockWithDatabase::getLockKey, key);
                LockWithDatabase lockWithDatabase = lockMapper.selectOne(wrapper);
                Date current = new Date();
                boolean invalid = current.after(lockWithDatabase.getInvalidTime());
                if (!invalid && canRetry()) {
                    return getLock(key, timeout, timeUnit);
                } else if (invalid) {
                    moveConflict(key);
                    lockMapper.delete(wrapper);
                    return getLock(key, timeout, timeUnit);
                } else {
                    lockMapper.updateCountByKey(key, current);
                    return false;
                }
            };
        }
    }

    @Override
    protected Function<String, Boolean> releaseLockSuccess(String key) {
        return t -> true;
    }

    @Override
    protected Function<String, Boolean> releaseLockFailure(String key) throws LockException {
        if (!this.doRelease(key)) {
            throw new LockException(String.format("释放锁[%s]失败", key));
        }
        return t -> false;
    }

    @Override
    public void renewal(String key) {
        throw new UnsupportedOperationException();
    }

    private void moveConflict(String key) {
        lockConflictMapper.copyFromLock(key);
    }

    private boolean canRetry() {
        return retryThreadLocal.get().getAndDecrement() > 0;
    }

}
