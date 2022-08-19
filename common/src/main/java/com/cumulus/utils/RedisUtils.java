package com.cumulus.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 */
@Slf4j
@Component
@SuppressWarnings({"unchecked", "all"})
public class RedisUtils {

    /**
     * redis模板
     */
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 在线用户redis key
     */
    @Value("${jwt.online-key}")
    private String onlineKey;

    /**
     * 构造方法
     *
     * @param redisTemplate redis模板
     */
    public RedisUtils(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间（秒）
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return false;
        }
        return true;
    }

    /**
     * 指定缓存失效时间
     *
     * @param key      键
     * @param time     时间（秒）
     * @param timeUnit 单位
     */
    public boolean expire(String key, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, timeUnit);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return false;
        }
        return true;
    }

    /**
     * 根据键获取过期时间
     *
     * @param key 键
     * @return 时间（秒）（返回0代表为永久有效）
     */
    public long getExpire(Object key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 查找匹配键
     *
     * @param pattern 键
     * @return 键列表
     */
    public List<String> scan(String pattern) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).build();
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        RedisConnection rc = Objects.requireNonNull(factory).getConnection();
        Cursor<byte[]> cursor = rc.scan(options);
        List<String> result = new ArrayList<>();
        while (cursor.hasNext()) {
            result.add(new String(cursor.next()));
        }
        try {
            RedisConnectionUtils.releaseConnection(rc, factory);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
        return result;
    }

    /**
     * 分页查询键
     *
     * @param patternKey 键
     * @param page       页码
     * @param size       每页数目
     * @return 键列表
     */
    public List<String> findKeysForPage(String patternKey, int page, int size) {
        ScanOptions options = ScanOptions.scanOptions().match(patternKey).build();
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        RedisConnection rc = Objects.requireNonNull(factory).getConnection();
        Cursor<byte[]> cursor = rc.scan(options);
        List<String> result = new ArrayList<>(size);
        int tmpIndex = 0;
        int fromIndex = page * size;
        int toIndex = page * size + size;
        while (cursor.hasNext()) {
            if (tmpIndex >= fromIndex && tmpIndex < toIndex) {
                result.add(new String(cursor.next()));
                tmpIndex++;
                continue;
            }
            // 获取到满足条件的数据后,就可以退出了
            if (tmpIndex >= toIndex) {
                break;
            }
            tmpIndex++;
            cursor.next();
        }
        try {
            RedisConnectionUtils.releaseConnection(rc, factory);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
        return result;
    }

    /**
     * 判断键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 键（可以传一个或多个）
     */
    public void del(String... keys) {
        if (null != keys && keys.length > 0) {
            if (keys.length == 1) {
                boolean result = redisTemplate.delete(keys[0]);
                if (log.isDebugEnabled()) {
                    log.debug(new StringBuilder("delete cache: ").append(keys[0]).append(", result: ").append(result)
                            .toString());
                }
            } else {
                Set<Object> keySet = new HashSet<>();
                for (String key : keys) {
                    keySet.addAll(redisTemplate.keys(key));
                }
                long count = redisTemplate.delete(keySet);
                if (log.isDebugEnabled()) {
                    log.debug("Cache deleted successfully: " + keySet.toString() + "Number of cache deletes: " + count);
                }
            }
        }
    }

    // ============================String=============================

    /**
     * 获取缓存
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return null == key ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 批量获取缓存
     *
     * @param keys 键列表
     * @return 值列表
     */
    public List<Object> multiGet(List<String> keys) {
        List list = redisTemplate.opsForValue().multiGet(Sets.newHashSet(keys));
        List resultList = Lists.newArrayList();
        Optional.ofNullable(list).ifPresent(e -> list.forEach(ele -> Optional.ofNullable(ele)
                .ifPresent(resultList::add)));
        return resultList;
    }

    /**
     * 存入缓存
     *
     * @param key   键
     * @param value 值
     * @return 操作结果
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * 存入缓存并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间（秒）（小于等于0将设置无限期）
     * @return 操作结果
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * 存入缓存并设置时间
     *
     * @param key      键
     * @param value    值
     * @param time     时间
     * @param timeUnit 时间单位
     * @return 操作结果
     */
    public boolean set(String key, Object value, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, timeUnit);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return false;
        }
    }

    // ================================Map=================================

    /**
     * 获取HashMap格式缓存
     *
     * @param key  键
     * @param item 项
     * @return 值
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取HashMap格式缓存
     *
     * @param key 键
     * @return 键值对
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 存入HashMap格式缓存
     *
     * @param key 键
     * @param map 键值对
     * @return 操作结果
     */
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * 存入HashMap格式缓存并设置时间
     *
     * @param key  键
     * @param map  键值对
     * @param time 时间（秒）
     * @return 操作结果
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * 存入HashMap格式缓存
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return 操作结果
     */
    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * 存入HashMap格式缓存
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间（秒）
     * @return 操作结果
     */
    public boolean hset(String key, String item, Object value, long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * 删除HashMap格式缓存的值
     *
     * @param key  键
     * @param item 项（可以多个，不能为空）
     */
    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断HashMap格式缓存中是否有该项的值
     *
     * @param key  键
     * @param item 项
     * @return 操作结果
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * HashMap格式缓存递增
     *
     * @param key  键
     * @param item 项
     * @param by   增加数值（大于0）
     * @return 结果
     */
    public double hincr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * HashMap格式缓存递减
     *
     * @param key  键
     * @param item 项
     * @param by   减少数值（小于0）
     * @return 结果
     */
    public double hdecr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    // ============================set=============================

    /**
     * 根据键获取HashSet格式缓存中的所有值
     *
     * @param key 键
     * @return 值集合
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * 查询HashSet格式缓存是否存在值
     *
     * @param key   键
     * @param value 值
     * @return 操作结果
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * 存入HashSet格式缓存
     *
     * @param key    键
     * @param values 值（可以多个）
     * @return 操作成功个数
     */
    public long sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 存入HashSet格式缓存
     *
     * @param key    键
     * @param time   时间（秒）
     * @param values 值（可以多个）
     * @return 操作成功个数
     */
    public long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return count;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return 0;
        }
    }

    /**
     * 获取HashSet格式缓存的长度
     *
     * @param key 键
     * @return 长度
     */
    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return 0;
        }
    }

    /**
     * 移除HashSet格式缓存的值
     *
     * @param key    键
     * @param values 值（可以多个）
     * @return 移除的个数
     */
    public long setRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return 0;
        }
    }

    // ===============================list=================================

    /**
     * 获取List格式缓存
     *
     * @param key   键
     * @param start 开始
     * @param end   结束
     * @return 值列表
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * 获取List格式缓存的长度
     *
     * @param key 键
     * @return 长度
     */
    public long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return 0;
        }
    }

    /**
     * 通过索引获取List格式缓存的值
     *
     * @param key   键
     * @param index 索引
     * @return 值
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * 存入List格式缓存
     *
     * @param key   键
     * @param value 值
     * @return 操作结果
     */
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * 存入List格式缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间（秒）
     * @return 操作结果
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * 存入List格式缓存
     *
     * @param key   键
     * @param value 值
     * @return 操作结果
     */
    public boolean lSet(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * 存入List格式缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间（秒）
     * @return 操作结果
     */
    public boolean lSet(String key, List<Object> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * 根据索引修改List格式缓存中的值
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return 操作结果
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * 从List格式缓存中移除多个指定值
     *
     * @param key   键
     * @param count 移除个数
     * @param value 值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return 0;
        }
    }

    /**
     * 批量删除相同redis键前缀的缓存
     *
     * @param prefix redis键前缀
     * @param ids    redis键后缀集合
     */
    public void delByKeys(String prefix, Set<Long> ids) {
        Set<Object> keys = new HashSet<>();
        for (Long id : ids) {
            keys.addAll(redisTemplate.keys(new StringBuffer(prefix).append(id).toString()));
        }
        long count = redisTemplate.delete(keys);
        if (log.isDebugEnabled()) {
            log.debug("Cache deleted successfully: " + keys.toString() + "Number of cache deletes: " + count);
        }
    }
}
