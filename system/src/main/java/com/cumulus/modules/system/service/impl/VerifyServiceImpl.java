package com.cumulus.modules.system.service.impl;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import com.cumulus.constant.PlatformConstant;
import com.cumulus.vo.EmailVo;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.system.service.VerifyService;
import com.cumulus.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * 验证服务实现
 */
@Service
public class VerifyServiceImpl implements VerifyService {

    /**
     * 验证码过期时间
     */
    @Value("${code.expiration}")
    private Long expiration;

    /**
     * redis工具类
     */
    @Autowired
    private RedisUtils redisUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmailVo sendEmail(String email, String key) {
        EmailVo emailVo;
        String content;
        String redisKey = key + email;
        // 如果不存在有效的验证码，就创建一个新的
        TemplateEngine engine = TemplateUtil.createEngine(new TemplateConfig("template",
                TemplateConfig.ResourceMode.CLASSPATH));
        Template template = engine.getTemplate("email/email.ftl");
        Object oldCode = redisUtils.get(redisKey);
        if (null == oldCode) {
            String code = RandomUtil.randomNumbers(6);
            // 存入缓存
            if (!redisUtils.set(redisKey, code, expiration)) {
                throw new BadRequestException("服务异常，请联系管理员");
            }
            content = template.render(Dict.create().set("code", code));
            // 存在就再次发送原来的验证码
        } else {
            content = template.render(Dict.create().set("code", oldCode));
        }
        emailVo = new EmailVo(Collections.singletonList(email), PlatformConstant.PRODUCT_NAME, content);
        return emailVo;
    }

    @Override
    public void validated(String key, String code) {
        Object value = redisUtils.get(key);
        if (null == value || !value.toString().equals(code)) {
            throw new BadRequestException("无效验证码");
        } else {
            redisUtils.del(key);
        }
    }

}
