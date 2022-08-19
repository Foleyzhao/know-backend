package com.cumulus.config;

import com.cumulus.utils.SecurityUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义接口权限校验配置
 */
@Service(value = "auth")
public class CustomizePermissionConfig {

    public static final String NO_PERMISSION = "none";

    /**
     * 自定义权限校验
     *
     * @param permissions 权限字符串
     * @return 是否包含接口上定义的权限
     */
    public Boolean check(String... permissions) {
        if (!PlatformProperties.userCustomizePermission) {
            return true;
        }
        if (permissions.length == 1) {
            if (Arrays.stream(permissions).anyMatch(NO_PERMISSION::contains)) {
                return true;
            }
        }
        // 获取当前用户的所有权限
        List<String> customizePermissions = SecurityUtils.getCurrentUser().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        // 判断当前用户的所有权限是否包含接口上定义的权限
        return customizePermissions.contains("admin")
                || Arrays.stream(permissions).anyMatch(customizePermissions::contains);
    }

}
