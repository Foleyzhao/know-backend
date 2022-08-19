package com.cumulus.aspect;

import com.cumulus.annotation.Limit;
import com.cumulus.exception.BadRequestException;
import com.cumulus.utils.RequestHolder;
import com.cumulus.utils.StringUtils;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 自定义限流切面
 */
@Slf4j
@Aspect
@Component
public class LimitAspect {

    /**
     * redis模板
     */
    private final RedisTemplate<Object, Object> redisTemplate;

    /**
     * 构造方法
     *
     * @param redisTemplate redis模板
     */
    public LimitAspect(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Pointcut("@annotation(com.cumulus.annotation.Limit)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = RequestHolder.getHttpServletRequest();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method signatureMethod = signature.getMethod();
        Limit limit = signatureMethod.getAnnotation(Limit.class);
        LimitType limitType = limit.limitType();
        String key = limit.key();
        if (StringUtils.isEmpty(key)) {
            if (limitType == LimitType.IP) {
                key = StringUtils.getIp(request);
            } else {
                key = signatureMethod.getName();
            }
        }
        ImmutableList<Object> keys = ImmutableList.of(StringUtils.join(limit.prefix(), "_", key, "_",
                request.getRequestURI().replace("/", "_")));
        String luaScript = buildLuaScript();
        RedisScript<Number> redisScript = new DefaultRedisScript<>(luaScript, Number.class);
        Number count = redisTemplate.execute(redisScript, keys, limit.count(), limit.period());
        if (null != count && count.intValue() <= limit.count()) {
            if (log.isInfoEnabled()) {
                log.info("The {}th access key is {}, description is [{}] Interface", count, keys, limit.name());
            }
            return joinPoint.proceed();
        } else {
            throw new BadRequestException("访问次数受限制");
        }
    }

    /**
     * 构建限流脚本
     *
     * @return 限流脚本
     */
    private String buildLuaScript() {
        return "local c" +
                "\nc = redis.call('get',KEYS[1])" +
                "\nif c and tonumber(c) > tonumber(ARGV[1]) then" +
                "\nreturn c;" +
                "\nend" +
                "\nc = redis.call('incr',KEYS[1])" +
                "\nif tonumber(c) == 1 then" +
                "\nredis.call('expire',KEYS[1],ARGV[2])" +
                "\nend" +
                "\nreturn c;";
    }

}
