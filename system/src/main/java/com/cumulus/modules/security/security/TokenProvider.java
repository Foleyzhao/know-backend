package com.cumulus.modules.security.security;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.cumulus.modules.security.config.bean.SecurityProperties;
import com.cumulus.utils.RedisUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * token生成器
 */
@Slf4j
@Component
public class TokenProvider implements InitializingBean {

    /**
     * 鉴权信息前缀
     */
    public static final String AUTHORITIES_KEY = "user";

    /**
     * Jwt参数配置
     */
    private final SecurityProperties properties;

    /**
     * Redis工具类
     */
    private final RedisUtils redisUtils;

    /**
     * JWT分析器
     */
    private JwtParser jwtParser;

    /**
     * JWT构造器
     */
    private JwtBuilder jwtBuilder;

    /**
     * 构造方法
     *
     * @param properties Jwt参数配置
     * @param redisUtils Redis工具类
     */
    public TokenProvider(SecurityProperties properties, RedisUtils redisUtils) {
        this.properties = properties;
        this.redisUtils = redisUtils;
    }

    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64.decode(properties.getBase64Secret());
        Key key = Keys.hmacShaKeyFor(keyBytes);
        jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
        jwtBuilder = Jwts.builder().signWith(key, SignatureAlgorithm.HS512);
    }

    /**
     * 创建永不过期的token（过期时间由redis维护）
     *
     * @param authentication 认真信息
     * @return token
     */
    public String createToken(Authentication authentication) {
        return jwtBuilder
                // 加入ID确保生成的 Token 都不一致
                .setId(IdUtil.simpleUUID())
                .claim(AUTHORITIES_KEY, authentication.getName())
                .setSubject(authentication.getName())
                .compact();
    }

    /**
     * 从token中获取鉴权信息
     *
     * @param token token
     * @return 鉴权信息
     */
    Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        User principal = new User(claims.getSubject(), "******", new ArrayList<>());
        return new UsernamePasswordAuthenticationToken(principal, token, new ArrayList<>());
    }

    /**
     * 从token获取声明
     *
     * @param token token
     * @return 声明
     */
    public Claims getClaims(String token) {
        return jwtParser.parseClaimsJws(token).getBody();
    }

    /**
     * 检查token是否续期并续期
     *
     * @param token token
     */
    public void checkRenewal(String token) {
        // 判断是否续期token,计算token的过期时间
        long time = redisUtils.getExpire(properties.getOnlineKey() + token) * 1000;
        Date expireDate = DateUtil.offset(new Date(), DateField.MILLISECOND, (int) time);
        // 判断当前时间与过期时间的时间差
        long differ = expireDate.getTime() - System.currentTimeMillis();
        // 如果在续期检查的范围内，则续期
        if (differ <= properties.getDetect()) {
            long renew = time + properties.getRenew();
            redisUtils.expire(properties.getOnlineKey() + token, renew, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 从请求中获取token
     *
     * @param request 请求
     * @return token
     */
    public String getToken(HttpServletRequest request) {
        final String requestHeader = request.getHeader(properties.getHeader());
        if (null != requestHeader && requestHeader.startsWith(properties.getTokenStartWith())) {
            return requestHeader.substring(7);
        }
        return null;
    }
}
