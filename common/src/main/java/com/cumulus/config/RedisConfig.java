package com.cumulus.config;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import reactor.util.annotation.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Redis配置类
 */
@Slf4j
@Configuration
@EnableCaching
@ConditionalOnClass(RedisOperations.class)
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig extends CachingConfigurerSupport {

    @Bean
    public RedisCacheManager redisCacheManager(RedisTemplate<Object, Object> redisTemplate) {
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(Objects.requireNonNull(redisTemplate.getConnectionFactory()));
        FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class);
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig();
        // 设置redis数据默认过期时间2小时
        // 设置序列化方式
        configuration = configuration.serializeValuesWith(RedisSerializationContext.
                SerializationPair.fromSerializer(fastJsonRedisSerializer)).entryTtl(Duration.ofHours(2));
        return new CustomRedisCacheManager(redisCacheWriter, configuration);
    }

    @Bean(name = "redisTemplate")
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        // 序列化
        FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class);
        // value值的序列化采用fastJsonRedisSerializer
        template.setValueSerializer(fastJsonRedisSerializer);
        template.setHashValueSerializer(fastJsonRedisSerializer);
        // 全局开启AutoType
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        // 小范围指定白名单
        // ParserConfig.getGlobalInstance().addAccept("com.cumulus.entity");
        // key的序列化采用StringRedisSerializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        // 自定义缓存key生成策略
        return (target, method, params) -> {
            Map<String, Object> container = new HashMap<>();
            Class<?> targetClassClass = target.getClass();
            // 类地址
            container.put("class", targetClassClass.toGenericString());
            // 方法名称
            container.put("methodName", method.getName());
            // 包名称
            container.put("package", targetClassClass.getPackage());
            // 参数列表
            for (int i = 0; i < params.length; i++) {
                container.put(String.valueOf(i), params[i]);
            }
            // 转为JSON字符串
            String jsonString = JSON.toJSONString(container);
            // 做SHA256 Hash计算，得到一个SHA256摘要作为Key
            return DigestUtils.sha256Hex(jsonString);
        };
    }

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        // 异常处理，当Redis发生异常时，打印日志
        if (log.isInfoEnabled()) {
            log.info("initialization -> [{}]", "Redis CacheErrorHandler");
        }
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                if (log.isErrorEnabled()) {
                    log.error("Redis occur handleCacheGetError：key -> [{}]", key, e);
                }
            }

            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                if (log.isErrorEnabled()) {
                    log.error("Redis occur handleCachePutError：key -> [{}]；value -> [{}]", key, value, e);
                }
            }

            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                if (log.isErrorEnabled()) {
                    log.error("Redis occur handleCacheEvictError：key -> [{}]", key, e);
                }
            }

            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                if (log.isErrorEnabled()) {
                    log.error("Redis occur handleCacheClearError：", e);
                }
            }
        };
    }

    /**
     * 自定义的redis 管理器
     */
    public static class CustomRedisCacheManager extends RedisCacheManager {

        /**
         * 用于分隔cacheName 和超时时间
         * 例子：
         * 1、scan-task#60 代表超时时间为60分钟
         * 2、scan-task#0代表没用超时时间 为 -1 永不超时
         * 3、scan-task#-1、scan-task#asd、scan-task 使用的是在 RedisCacheManager 这个 bean 定义的时候的默认超时时间 这里是 2 小时
         */
        public static final String KEY_OUT_TIME_SEPARATOR = "#";

        public CustomRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
            super(cacheWriter, defaultCacheConfiguration);
        }

        @Override
        protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
            //名称中存在#标记进行到期时间配置
            int indexOf = StringUtils.lastIndexOf(name, KEY_OUT_TIME_SEPARATOR);
            if (indexOf != -1) {
                String time = name.substring(indexOf + KEY_OUT_TIME_SEPARATOR.length());
                String key = name.substring(0, indexOf);
                if (StringUtils.isNumeric(time)) {
                    //配置缓存到期时间
                    int cycle = Integer.parseInt(time);
                    return super.createRedisCache(key, cacheConfig.entryTtl(Duration.ofMinutes(cycle)));
                }
            }
            return super.createRedisCache(name, cacheConfig);
        }
    }
}

/**
 * Value 序列化
 *
 * @param <T> 序列化值对象类型
 */
class FastJsonRedisSerializer<T> implements RedisSerializer<T> {

    /**
     * 序列化值对象类
     */
    private final Class<T> clazz;

    FastJsonRedisSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) {
        if (null == t) {
            return new byte[0];
        }
        return JSON.toJSONString(t, SerializerFeature.WriteClassName).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public T deserialize(byte[] bytes) {
        if (null == bytes || bytes.length <= 0) {
            return null;
        }
        String str = new String(bytes, StandardCharsets.UTF_8);
        return JSON.parseObject(str, clazz);
    }

}

/**
 * 自定义序列化器
 */
class StringRedisSerializer implements RedisSerializer<Object> {

    /**
     * 编码
     */
    private final Charset charset;

    StringRedisSerializer() {
        this(StandardCharsets.UTF_8);
    }

    private StringRedisSerializer(Charset charset) {
        Assert.notNull(charset, "Charset must not be null!");
        this.charset = charset;
    }

    @Override
    public String deserialize(byte[] bytes) {
        return (null == bytes ? null : new String(bytes, charset));
    }

    @Override
    public @Nullable
    byte[] serialize(Object object) {
        String string = JSON.toJSONString(object);
        if (org.apache.commons.lang3.StringUtils.isBlank(string)) {
            return null;
        }
        string = string.replace("\"", "");
        return string.getBytes(charset);
    }
}
