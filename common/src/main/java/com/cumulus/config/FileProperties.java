package com.cumulus.config;

import com.cumulus.constant.PlatformConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 系统文件属性配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileProperties {

    /**
     * 文件大小限制
     */
    private Long maxSize;

    /**
     * 头像大小限制
     */
    private Long avatarMaxSize;

    /**
     * MAC系统文件存储路径
     */
    private Path mac;

    /**
     * Linux系统文件存储路径
     */
    private Path linux;

    /**
     * Windows系统文件存储路径
     */
    private Path windows;

    /**
     * 获取系统文件存储路径
     *
     * @return 系统文件存储路径
     */
    public Path getPath() {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith(PlatformConstant.WIN)) {
            return windows;
        } else if (os.toLowerCase().startsWith(PlatformConstant.MAC)) {
            return mac;
        }
        return linux;
    }

    /**
     * 系统文件存储路径
     */
    @Data
    public static class Path {

        /**
         * 文件路径
         */
        private String path;

        /**
         * 头像路径
         */
        private String avatar;

    }

}
