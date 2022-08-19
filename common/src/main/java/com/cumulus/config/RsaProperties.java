package com.cumulus.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Rsa加密配置类
 */
@Data
@Component
public class RsaProperties {

    /**
     * Rsa加密密钥
     */
    public static String privateKey;

    /**
     * 设置Rsa加密密钥
     *
     * @param privateKey Rsa加密密钥
     */
    @Value("${rsa.private_key}")
    public void setPrivateKey(String privateKey) {
        RsaProperties.privateKey = privateKey;
    }

}
