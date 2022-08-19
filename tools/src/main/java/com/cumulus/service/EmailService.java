package com.cumulus.service;

import com.cumulus.entity.EmailConfig;
import com.cumulus.vo.EmailVo;

/**
 * 邮件服务接口
 *
 * @author zhaoff
 */
public interface EmailService {

    /**
     * 更新邮件配置
     *
     * @param emailConfig 新的邮箱配置
     * @param old         旧的邮件配置
     * @return 邮件配置
     * @throws Exception 异常
     */
    EmailConfig config(EmailConfig emailConfig, EmailConfig old) throws Exception;

    /**
     * 获取邮件配置
     *
     * @return 邮件配置
     */
    EmailConfig find();

    /**
     * 发送邮件
     *
     * @param emailVo     发送邮件对象
     * @param emailConfig 邮件配置
     */
    void send(EmailVo emailVo, EmailConfig emailConfig);
}
