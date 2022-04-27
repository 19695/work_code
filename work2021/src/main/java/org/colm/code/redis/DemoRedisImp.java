package org.colm.code.redis;

import org.springframework.data.redis.core.RedisTemplate;

public class DemoRedisImp extends AbstractRedisStub {

    public DemoRedisImp(RedisTemplate redisTemplate) {
        this(redisTemplate, "skt");
    }

    public DemoRedisImp(RedisTemplate redisTemplate, String category) {
        super(redisTemplate, category);
    }

    public void setValue(String key, String value) {
        redisClient.setValue(key, value);
    }

}
