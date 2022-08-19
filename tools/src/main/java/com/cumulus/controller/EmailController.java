package com.cumulus.controller;

import com.cumulus.annotation.Log;
import com.cumulus.entity.EmailConfig;
import com.cumulus.service.EmailService;
import com.cumulus.vo.EmailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邮件控制层
 *
 * @author zhaoff
 */
@RestController
@RequestMapping("api/email")
public class EmailController {

    /**
     * 邮件服务接口
     */
    @Autowired
    private EmailService emailService;

    /**
     * 查询邮件配置
     *
     * @return 邮件配置
     */
    @GetMapping
    public ResponseEntity<Object> queryConfig() {
        return new ResponseEntity<>(emailService.find(), HttpStatus.OK);
    }

    /**
     * 配置邮件
     *
     * @param emailConfig 邮件配置
     * @return 响应
     * @throws Exception 异常
     */
    @Log("配置邮件")
    @PutMapping
    public ResponseEntity<Object> updateConfig(@Validated @RequestBody EmailConfig emailConfig) throws Exception {
        emailService.config(emailConfig, emailService.find());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 发送邮件
     *
     * @param emailVo 发送邮件vo类
     * @return 响应
     */
    @Log("发送邮件")
    @PostMapping
    public ResponseEntity<Object> sendEmail(@Validated @RequestBody EmailVo emailVo) {
        emailService.send(emailVo, emailService.find());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
