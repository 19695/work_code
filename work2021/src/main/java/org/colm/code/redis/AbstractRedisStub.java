package org.colm.code.redis;

import org.springframework.data.redis.core.RedisTemplate;

public abstract class AbstractRedisStub {

    protected final RedisTemplate redisTemplate;
    protected final String category;
    protected RedisClient redisClient;
    protected Integer keyExpire = 5 * 60 * 1000;
    protected Integer lockExpire = 3 * 1000;
    protected Integer lockTryTimes = 3;
    protected Integer lockTryGap = 1000;

    public AbstractRedisStub(RedisTemplate redisTemplate, String category) {
       this.redisTemplate = redisTemplate;
       this.category = category;
    }

    public AbstractRedisStub(RedisTemplate redisTemplate, Integer keyExpire, Integer lockExpire, Integer lockTryTimes, Integer lockTryGap, String category) {
        this(redisTemplate, category);
        this.keyExpire = keyExpire;
        this.lockExpire = lockExpire;
        this.lockTryTimes = lockTryTimes;
        this.lockTryGap = lockTryGap;
    }

    public final void getRedisClient() {
        this.redisClient = new RedisClient(this.keyExpire, this.lockExpire, this.lockTryTimes,
                this.lockTryGap, this.category, this.redisTemplate);
    }

}
