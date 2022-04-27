package org.colm.code.redis;

import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Collections;
import java.util.Formatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RedisClient {

    /**
     * format – A format string as described in {@link Formatter#format}
     *
     * <p>The format specifiers for general, character, and numeric types have
     * the following syntax:
     *
     * <blockquote>%[argument_index$][flags][width][.precision]conversion</blockquote>
     *
     * @see java.util.Formatter
     */
    private static final String LOCK_KEY_FORMAT = "lock_%1$s";
    private static final RedisSerializer ARGS_SERIALIZER = new StringRedisSerializer();
    private static final RedisSerializer RESULT_SERIALIZER = new GenericJackson2JsonRedisSerializer();
    private String keyFormat;
    private Integer keyExpire = 120000;
    private Integer lockExpire = 3000;
    private Integer lockTryTimes = 3;
    private Integer lockTryGap = 1000;

    private RedisTemplate redisTemplate;

    private DefaultRedisScript<Long> setNXWithExpireTimeScript;
    private DefaultRedisScript<Long> unlockScript;

    public RedisClient(Integer keyExpire, Integer lockExpire, Integer lockTryTimes, Integer lockTryGap,
                       String category, RedisTemplate redisTemplate) {
        this.keyExpire = keyExpire;
        this.lockExpire = lockExpire;
        this.lockTryTimes = lockTryTimes;
        this.lockTryGap = lockTryGap;
        this.redisTemplate = redisTemplate;
        this.keyFormat = category + "_%1$s";
        initScript();
    }

    private void initScript() {
        this.setNXWithExpireTimeScript = new DefaultRedisScript<>();
        this.setNXWithExpireTimeScript.setResultType(Long.class);
        this.setNXWithExpireTimeScript.setScriptText(
                "local res=redis.call('set', KEYS[1] ARGV[1], 'NX', 'PX', ARGV[2]) "
                + "if res then return 1 else return 0 end");
        this.unlockScript = new DefaultRedisScript<>();
        this.unlockScript.setResultType(Long.class);
        this.unlockScript.setScriptText(
                "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end");
    }

    public boolean unlock(String key, String value) {
        String redisKey = buildRedisKey(key);
        String lockKey = buildLockKey(redisKey);
        // Collections.singletonList 只有一个元素
        Long result = (Long) redisTemplate.execute(unlockScript, Collections.singletonList(lockKey), value);
        if (result == 0L) {
            return false;
        }
        return true;
    }

    public String lock(String key) throws InterruptedException, RedisLockException {
        String redisKey = buildRedisKey(key);
        String lockKey = buildLockKey(redisKey);
        String lockValue = UUID.randomUUID().toString().replaceAll("-", "");
        while (this.lockTryTimes-- > 0) {
            // 脚本等同于使用如下方式
            // redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, lockExpire, TimeUnit.MILLISECONDS);
            Long result = (Long) redisTemplate.execute(setNXWithExpireTimeScript, ARGS_SERIALIZER, RESULT_SERIALIZER,
                    Collections.singletonList(lockKey), lockValue, lockExpire.toString());
            if (result == 1L) {
                return lockValue;
            }
            TimeUnit.MILLISECONDS.sleep(this.lockTryGap);
        }
        throw new RedisLockException("");
    }

    public void setValue(String key, String value) {
        setValue(key, value, this.keyExpire);
    }

    public void setValue(String key, String value, long keyExpire) {
        String redisKey = buildRedisKey(key);
        BoundValueOperations valueOps = redisTemplate.boundValueOps(redisKey);
        valueOps.set(value, keyExpire, TimeUnit.MILLISECONDS);
    }

    public String getValue(String key) {
        return getValue(key, false);
    }

    public String getValue(String key, boolean renewal) {
        String redisKey = buildRedisKey(key);
        BoundValueOperations valueOps = redisTemplate.boundValueOps(redisKey);
        String value = (String) valueOps.get();
        if (renewal && value != null) {
            valueOps.expire(this.keyExpire, TimeUnit.MILLISECONDS);
        }
        return value;
    }

    public void delete(String key) {
        String redisKey = buildRedisKey(key);
        redisTemplate.delete(redisKey);
    }

    private String buildRedisKey(String key) {
        return String.format(keyFormat, key);
    }

    private String buildLockKey(String key) {
        return String.format(LOCK_KEY_FORMAT, key);
    }

}
