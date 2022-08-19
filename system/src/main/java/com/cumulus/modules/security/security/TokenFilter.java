package com.cumulus.modules.security.security;

import cn.hutool.core.util.StrUtil;
import com.cumulus.modules.security.config.bean.SecurityProperties;
import com.cumulus.modules.security.dto.OnlineUserDto;
import com.cumulus.modules.security.service.OnlineUserService;
import com.cumulus.modules.security.service.UserCacheClean;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Objects;

/**
 * 自定义Token过滤器
 */
@Slf4j
public class TokenFilter extends GenericFilterBean {

    /**
     * token生成器
     */
    private final TokenProvider tokenProvider;

    /**
     * Jwt参数配置
     */
    private final SecurityProperties properties;

    /**
     * 在线用户服务
     */
    private final OnlineUserService onlineUserService;

    /**
     * 在线用户清理服务
     */
    private final UserCacheClean userCacheClean;

    /**
     * 构造方法
     *
     * @param tokenProvider     token生成器
     * @param properties        Jwt参数配置
     * @param onlineUserService 在线用户服务
     * @param userCacheClean    在线用户清理服务
     */
    public TokenFilter(TokenProvider tokenProvider, SecurityProperties properties, OnlineUserService onlineUserService, UserCacheClean userCacheClean) {
        this.properties = properties;
        this.onlineUserService = onlineUserService;
        this.tokenProvider = tokenProvider;
        this.userCacheClean = userCacheClean;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String token = resolveToken(httpServletRequest);
        // 对于 Token 为空的不需要去查 Redis
        if (StrUtil.isNotBlank(token)) {
            OnlineUserDto onlineUserDto = null;
            boolean cleanUserCache = false;
            try {
                onlineUserDto = onlineUserService.getOne(properties.getOnlineKey() + token);
            } catch (ExpiredJwtException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage());
                }
                cleanUserCache = true;
            } finally {
                if (cleanUserCache || Objects.isNull(onlineUserDto)) {
                    userCacheClean.cleanUserCache(
                            String.valueOf(tokenProvider.getClaims(token).get(TokenProvider.AUTHORITIES_KEY)));
                }
            }
            if (null != onlineUserDto && StringUtils.hasText(token)) {
                Authentication authentication = tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // Token 续期
                tokenProvider.checkRenewal(token);
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * 从请求中检测token是否合法，并提取token
     *
     * @param request 请求
     * @return token
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(properties.getHeader());
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(properties.getTokenStartWith())) {
            // 去掉令牌前缀
            return bearerToken.replace(properties.getTokenStartWith(), "");
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Illegal Token: {}", bearerToken);
            }
        }
        return null;
    }

}
