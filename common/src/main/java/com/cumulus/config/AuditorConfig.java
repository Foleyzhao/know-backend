package com.cumulus.config;

import com.cumulus.utils.SecurityUtils;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JPA审计配置
 */
@Component("auditorAware")
public class AuditorConfig implements AuditorAware<String> {

    /**
     * 返回操作员信息
     *
     * @return 操作员信息
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            // 获取操作员信息
            return Optional.of(SecurityUtils.getCurrentUsername());
        } catch (Exception ignored) {
        }
        // 用户定时任务，或者无Token调用的情况
        return Optional.of("System");
    }
}
