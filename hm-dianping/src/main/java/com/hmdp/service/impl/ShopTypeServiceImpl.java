package com.hmdp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryAll() {
        // 1.从Redis中查询商铺类型
        String key = RedisConstants.CACHE_TYPE_SHOP_KEY;
        List<String> shopTypeJson = stringRedisTemplate.opsForList().range(key, 0, 9);
        // 2.判断是否命中
        if (!shopTypeJson.isEmpty()) {
            // 3.命中,反序列化并返回结果
            List<ShopType> shopTypeList = shopTypeJson.stream()
                    .map(item -> JSONUtil.toBean(item, ShopType.class))
                    .sorted(Comparator.comparingInt(ShopType::getSort))
                    .collect(Collectors.toList());
            return Result.ok(shopTypeList);
        }
        // 4.未命中，根据id查询数据库
        List<ShopType> shopTypeList = query().orderByAsc("sort").list();
        // 4.判断商铺是否存在
        if (CollectionUtil.isEmpty(shopTypeList)) {
            return Result.fail("店铺不存在");
        }
        // 6.存在，将商铺数据写入Redis
        List<String> shopTypeCache = shopTypeList.stream()
                .map(JSONUtil::toJsonStr)
                .collect(Collectors.toList());
        stringRedisTemplate.opsForList().rightPushAll(key, shopTypeCache);
        stringRedisTemplate.expire(key, RedisConstants.CACHE_TYPE_SHOP_TTL, TimeUnit.MINUTES);
        // 7.返回商铺信息
        return Result.ok(shopTypeCache);
    }
}
