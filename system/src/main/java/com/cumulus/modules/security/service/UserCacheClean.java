package com.cumulus.modules.security.service;

import com.cumulus.utils.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 用户缓存清理服务
 */
@Component
public class UserCacheClean {

    /**
     * 清理特定用户缓存信息
     *
     * @param userName 用户名
     */
    public void cleanUserCache(String userName) {
        if (StringUtils.isNotEmpty(userName)) {
            UserDetailsServiceImpl.USER_DTO_CACHE.remove(userName);
        }
    }

    /**
     * 清理所有用户的缓存信息
     */
    public void cleanAll() {
        UserDetailsServiceImpl.USER_DTO_CACHE.clear();
    }
    
}
