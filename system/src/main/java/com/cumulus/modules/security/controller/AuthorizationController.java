package com.cumulus.modules.security.controller;

import cn.hutool.core.util.IdUtil;
import com.cumulus.annotation.Log;
import com.cumulus.annotation.rest.AnonymousDeleteMapping;
import com.cumulus.annotation.rest.AnonymousGetMapping;
import com.cumulus.annotation.rest.AnonymousPostMapping;
import com.cumulus.config.RsaProperties;
import com.cumulus.enums.LogTypeEnum;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.security.config.bean.LoginCodeEnum;
import com.cumulus.modules.security.config.bean.LoginProperties;
import com.cumulus.modules.security.config.bean.SecurityProperties;
import com.cumulus.modules.security.dto.AuthUserDto;
import com.cumulus.modules.security.dto.JwtUserDto;
import com.cumulus.modules.security.security.TokenProvider;
import com.cumulus.modules.security.service.OnlineUserService;
import com.cumulus.utils.RedisUtils;
import com.cumulus.utils.RsaUtils;
import com.cumulus.utils.SecurityUtils;
import com.cumulus.utils.StringUtils;
import com.wf.captcha.base.Captcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证鉴权控制层
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthorizationController {

    /**
     * Jwt参数配置
     */
    @Autowired
    private SecurityProperties properties;

    /**
     * reids工具类
     */
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 在线用户服务
     */
    @Autowired
    private OnlineUserService onlineUserService;

    /**
     * token生成器
     */
    @Autowired
    private TokenProvider tokenProvider;

    /**
     * 认证鉴权管理构建器
     */
    @Autowired
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    /**
     * 登录配置信息
     */
    @Autowired
    private LoginProperties loginProperties;

    /**
     * 登录授权
     *
     * @param authUser 认证用户传输对象
     * @param request  请求
     * @return token及用户信息
     * @throws Exception 异常
     */
    @Log(value = "登录", logType = LogTypeEnum.LOGIN)
    @AnonymousPostMapping(value = "/login")
    public ResponseEntity<Object> login(@Validated @RequestBody AuthUserDto authUser, HttpServletRequest request)
            throws Exception {
        // 密码解密
        String password = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, authUser.getPassword());
        // 查询验证码
        String code = (String) redisUtils.get(authUser.getUuid());
        // 清除验证码
        redisUtils.del(authUser.getUuid());
        if (StringUtils.isBlank(code)) {
            throw new BadRequestException("验证码不存在或已过期");
        }
        if (StringUtils.isBlank(authUser.getCode()) || !authUser.getCode().equalsIgnoreCase(code)) {
            throw new BadRequestException("验证码错误");
        }
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(authUser.getUsername(), password);
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 生成令牌
        String token = tokenProvider.createToken(authentication);
        final JwtUserDto jwtUserDto = (JwtUserDto) authentication.getPrincipal();
        // 保存在线信息
        onlineUserService.save(jwtUserDto, token, request);
        // 返回 token 与 用户信息
        Map<String, Object> authInfo = new HashMap<String, Object>(2) {

            private static final long serialVersionUID = -4002306315794829513L;

            {
                put("token", properties.getTokenStartWith() + token);
                put("user", jwtUserDto);
            }
        };
        if (loginProperties.isSingleLogin()) {
            // 配置单用户登录，则需踢掉之前已经登录的token
            onlineUserService.checkLoginOnUser(authUser.getUsername(), token);
        }
        return ResponseEntity.ok(authInfo);
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    @GetMapping(value = "/info")
    public ResponseEntity<Object> getUserInfo() {
        return ResponseEntity.ok(SecurityUtils.getCurrentUser());
    }

    /**
     * 获取验证码
     *
     * @return 验证码
     */
    @AnonymousGetMapping(value = "/code")
    public ResponseEntity<Object> getCode() {
        // 获取运算的结果
        Captcha captcha = loginProperties.getCaptcha();
        String uuid = properties.getCodeKey() + IdUtil.simpleUUID();
        // 当验证码类型为arithmetic时且长度 >= 2 时，captcha.text()的结果有几率为浮点型
        String captchaValue = captcha.text();
        if (captcha.getCharType() - 1 == LoginCodeEnum.arithmetic.ordinal() && captchaValue.contains(".")) {
            captchaValue = captchaValue.split("\\.")[0];
        }
        // 保存
        redisUtils.set(uuid, captchaValue, loginProperties.getLoginCode().getExpiration(), TimeUnit.MINUTES);
        // 验证码信息
        Map<String, Object> imgResult = new HashMap<String, Object>(2) {

            private static final long serialVersionUID = 7265529269225525907L;

            {
                put("img", captcha.toBase64());
                put("uuid", uuid);
            }
        };
        return ResponseEntity.ok(imgResult);
    }

    /**
     * 退出登录
     *
     * @param request 请求
     * @return 响应
     */
    @Log(value = "退出", logType = LogTypeEnum.LOGIN)
    @DeleteMapping(value = "/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request) {
        onlineUserService.logout(tokenProvider.getToken(request));
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
