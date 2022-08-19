package com.cumulus.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 平台属性配置类
 */
@Data
@Component
public class PlatformProperties {

    /**
     * IP解析是否本地解析
     */
    public static Boolean ipLocal;

    /**
     * 是否启用自定义权限
     */
    public static Boolean userCustomizePermission;

    /**
     * 设置IP解析是否本地解析
     *
     * @param ipLocal IP是否本地解析
     */
    @Value("${ip.local-parsing}")
    public void setIpLocal(Boolean ipLocal) {
        PlatformProperties.ipLocal = ipLocal;
    }

    /**
     * 设置是否启用自定义权限
     *
     * @param userCustomizePermission 是否启用自定义权限
     */
    @Value("${use-customize-permission}")
    public void setUserCustomizePermission(Boolean userCustomizePermission) {
        PlatformProperties.userCustomizePermission = userCustomizePermission;
    }
}
