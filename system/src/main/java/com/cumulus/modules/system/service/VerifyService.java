package com.cumulus.modules.system.service;

import com.cumulus.vo.EmailVo;

/**
 * 验证服务接口
 */
public interface VerifyService {

    /**
     * 发送验证码
     *
     * @param email 邮箱
     * @param key   缓存中验证码对应的key
     * @return 邮件发送对象
     */
    EmailVo sendEmail(String email, String key);


    /**
     * 校验验证码
     *
     * @param code 验证码
     * @param key  缓存中验证码对应的key
     */
    void validated(String key, String code);

}
