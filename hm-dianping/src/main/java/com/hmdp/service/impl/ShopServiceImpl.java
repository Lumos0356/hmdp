package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RedisData;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
    @Override
    public Result queryById(Long id) {
        // 缓存穿透
//         Shop shop = queryWithPassThrough(id);
        // 缓存击穿 互斥锁解决
//        Shop shop = queryWithMutex(id);
        //缓存穿透 逻辑过期
        Shop shop = queryWithLogicalExpire(id);
        if (shop == null) {
            return Result.fail("店铺不存在！");
        }
        // 7.返回商铺信息
        return Result.ok(shop);
    }

    public Shop queryWithPassThrough(Long id) {
        // 1.从Redis中查询商铺缓存
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否命中，只有shopJson对象不为null或者""时为命中
        if (StrUtil.isNotBlank(shopJson)) {
            // 3.命中，返回结果
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        // 判断是否为空值（""）
        if (shopJson != null) {
            return null;
        }
        // 4.未命中，根据id查询数据库
        Shop shop = getById(id);
        // 5.判断商铺是否存在
        if (shop == null) {
            // 6.不存在，将空值写入Redis
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        // 7.存在，将商铺数据写入Redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        // 8.返回商铺信息
        return shop;
    }

    
    public Shop queryWithMutex(Long id) {
        // 1.从Redis中查询商铺缓存
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否命中
        if (StrUtil.isNotBlank(shopJson)) {
            // 3.命中，返回结果
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        // 判断是否为空值
        if (shopJson != null) {
            return null;
        }
        // 4.未命中，实现缓存重建
        // 4.1尝试获取互斥锁
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        Shop shop = null;
        try {
            boolean islock = trylock(lockKey);
            // 4.2判断是否获取锁
            // TODO 递归实现，可能存在爆栈风险
            if (!islock) {
                // 4.3失败，则休眠并重试
                Thread.sleep(50);
                return queryWithMutex(id);
            }
            // 4.4成功，根据id查询数据库
            shop = getById(id);
            // 模拟重建延迟
            Thread.sleep(200);
            // 5.判断商铺是否存在
            if (shop == null) {
                // 6.不存在，将空值写入Redis
                stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            // 7.存在，将商铺数据写入Redis
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 8.释放互斥锁
            unlock(lockKey);
        }
        // 9.返回商铺信息
        return shop;
    }

    public Shop queryWithLogicalExpire(Long id) {
        // 1.从Redis中查询商铺缓存
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否命中，只有shopJson对象不为null或者""时为命中
        if (StrUtil.isBlank(shopJson)) {
            // 3.未命中，返回null
            return null;
        }
        // 4.命中，把json反序列化为对象
        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        // 5.判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 5.1未过期，直接返回结果
            return shop;
        }
        // 5.2过期，缓存重建
        // 6.缓存重建
        // 6.1获取互斥锁
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        boolean isLock = trylock(lockKey);
        // 6.2判断是否获取锁
        if (isLock) {
            // 6.3获取成功，开启独立线程，实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    saveShopToRedis(id, 20L);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 6.4释放锁
                    unlock(lockKey);
                }
            });
        }
        // 7返回过期的商铺信息
        return shop;
    }
    private boolean trylock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
//        return flag;   // 不能直接返回基本类型，flag拆箱后可能结果为null
        return BooleanUtil.isTrue(flag); // 如果flag为null或者false都返回false
    }
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

    public void saveShopToRedis(Long id, Long expireSeconds) throws InterruptedException {
        // 1.查询店铺数据
        Shop shop = getById(id);
        Thread.sleep(200);
        // 2.封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        // 3.写入Redis
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
    }
    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空");
        }
        // 1.更新数据库
        updateById(shop);
        // 2.删除缓存
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        stringRedisTemplate.delete(key);
        return Result.ok();
    }
}
