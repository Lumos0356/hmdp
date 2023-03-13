package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


@Slf4j
@Component
public class CacheClient {
    private final StringRedisTemplate stringRedisTemplate;
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }
    public <R, ID> R queryWithPassThrough(
            String keyPreFix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        // 1.从Redis中查询商铺缓存
        String key = keyPreFix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否命中，只有shopJson对象不为null或者""时为命中
        if (StrUtil.isNotBlank(json)) {
            // 3.命中，返回结果
            return JSONUtil.toBean(json, type);
        }
        // 判断是否为空值（""）
        if (json != null) {
            return null;
        }
        // 4.未命中，根据id查询数据库
        R r = dbFallback.apply(id);
        // 5.判断商铺是否存在
        if (r == null) {
            // 6.不存在，将空值写入Redis
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        // 7.存在，将商铺数据写入Redis
        this.set(key, r, time, unit);
        // 8.返回商铺信息
        return r;
    }
    public <R, ID> R queryWithLogicalExpire(
            String keyPreFix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        // 1.从Redis中查询商铺缓存
        String key = keyPreFix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否命中，只有shopJson对象不为null或者""时为命中
        if (StrUtil.isBlank(json)) {
            // 3.未命中，返回null
            return null;
        }
        // 4.命中，把json反序列化为对象
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        // 5.判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 5.1未过期，直接返回结果
            return r;
        }
        // 5.2过期，缓存重建
        // 6.缓存重建
        // 6.1获取互斥锁
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        // 6.2判断是否获取锁
        if (isLock) {
            // 6.3获取成功，开启独立线程，实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 查询数据库
                    R r1 = dbFallback.apply(id);
                    // 封装逻辑过期时间，写入Redis
                    this.setWithLogicalExpire(key, r1, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 6.4释放锁
                    unlock(lockKey);
                }
            });
        }
        // 7.返回过期的商铺信息
        return r;
    }
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
//        return flag;   // 不能直接返回基本类型，flag拆箱后可能结果为null
        return BooleanUtil.isTrue(flag); // 如果flag为null或者false都返回false
    }
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}
