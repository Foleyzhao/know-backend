package com.cumulus.modules.system.controller;

import com.cumulus.vo.EmailVo;
import com.cumulus.enums.CodeBiEnum;
import com.cumulus.enums.CodeEnum;
import com.cumulus.modules.system.service.VerifyService;
import com.cumulus.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 验证码控制层
 */
@RestController
@RequestMapping("/api/code")
public class VerifyController {

    /**
     * 验证码服务接口
     */
    @Autowired
    private VerifyService verificationCodeService;

    /**
     * 邮箱服务接口
     */
    @Autowired
    private EmailService emailService;

    /**
     * 重置邮箱，发送验证码
     *
     * @param email 邮箱地址
     * @return 响应
     */
    @PostMapping(value = "/resetEmail")
    public ResponseEntity<Object> resetEmail(@RequestParam String email) {
        EmailVo emailVo = verificationCodeService.sendEmail(email, CodeEnum.EMAIL_RESET_EMAIL_CODE.getKey());
        emailService.send(emailVo, emailService.find());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 重置密码，发送验证码
     *
     * @param email 邮箱地址
     * @return 响应
     */
    @PostMapping(value = "/email/resetPass")
    public ResponseEntity<Object> resetPass(@RequestParam String email) {
        EmailVo emailVo = verificationCodeService.sendEmail(email, CodeEnum.EMAIL_RESET_PWD_CODE.getKey());
        emailService.send(emailVo, emailService.find());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 验证码验证
     *
     * @param email  邮箱地址
     * @param code   验证码
     * @param codeBi 业务场景
     * @return 响应
     */
    @GetMapping(value = "/validated")
    public ResponseEntity<Object> validated(@RequestParam String email, @RequestParam String code,
                                            @RequestParam Integer codeBi) {
        CodeBiEnum biEnum = CodeBiEnum.find(codeBi);
        switch (Objects.requireNonNull(biEnum)) {
            case ONE:
                verificationCodeService.validated(CodeEnum.EMAIL_RESET_EMAIL_CODE.getKey() + email, code);
                break;
            case TWO:
                verificationCodeService.validated(CodeEnum.EMAIL_RESET_PWD_CODE.getKey() + email, code);
                break;
            default:
                break;
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
