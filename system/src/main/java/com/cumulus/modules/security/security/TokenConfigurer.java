package com.cumulus.modules.security.security;

import com.cumulus.modules.security.config.bean.SecurityProperties;
import com.cumulus.modules.security.service.OnlineUserService;
import com.cumulus.modules.security.service.UserCacheClean;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Token配置类
 */
@RequiredArgsConstructor
public class TokenConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    /**
     * Jwt参数配置
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

    @Override
    public void configure(HttpSecurity http) {
        TokenFilter customFilter = new TokenFilter(tokenProvider, properties, onlineUserService, userCacheClean);
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
