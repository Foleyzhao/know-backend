package com.cumulus.repository;

import com.cumulus.entity.EmailConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 邮件配置类数据访问接口
 *
 * @author zhaoff
 */
public interface EmailRepository extends JpaRepository<EmailConfig, Long> {
}
