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
 * ?????????????????????
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthorizationController {

    /**
     * Jwt????????????
     */
    @Autowired
    private SecurityProperties properties;

    /**
     * reids?????????
     */
    @Autowired
    private RedisUtils redisUtils;

    /**
     * ??????????????????
     */
    @Autowired
    private OnlineUserService onlineUserService;

    /**
     * token?????????
     */
    @Autowired
    private TokenProvider tokenProvider;

    /**
     * ???????????????????????????
     */
    @Autowired
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    /**
     * ??????????????????
     */
    @Autowired
    private LoginProperties loginProperties;

    /**
     * ????????????
     *
     * @param authUser ????????????????????????
     * @param request  ??????
     * @return token???????????????
     * @throws Exception ??????
     */
    @Log(value = "??????", logType = LogTypeEnum.LOGIN)
    @AnonymousPostMapping(value = "/login")
    public ResponseEntity<Object> login(@Validated @RequestBody AuthUserDto authUser, HttpServletRequest request)
            throws Exception {
        // ????????????
        String password = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, authUser.getPassword());
        // ???????????????
        String code = (String) redisUtils.get(authUser.getUuid());
        // ???????????????
        redisUtils.del(authUser.getUuid());
        if (StringUtils.isBlank(code)) {
            throw new BadRequestException("??????????????????????????????");
        }
        if (StringUtils.isBlank(authUser.getCode()) || !authUser.getCode().equalsIgnoreCase(code)) {
            throw new BadRequestException("???????????????");
        }
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(authUser.getUsername(), password);
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // ????????????
        String token = tokenProvider.createToken(authentication);
        final JwtUserDto jwtUserDto = (JwtUserDto) authentication.getPrincipal();
        // ??????????????????
        onlineUserService.save(jwtUserDto, token, request);
        // ?????? token ??? ????????????
        Map<String, Object> authInfo = new HashMap<String, Object>(2) {

            private static final long serialVersionUID = -4002306315794829513L;

            {
                put("token", properties.getTokenStartWith() + token);
                put("user", jwtUserDto);
            }
        };
        if (loginProperties.isSingleLogin()) {
            // ?????????????????????????????????????????????????????????token
            onlineUserService.checkLoginOnUser(authUser.getUsername(), token);
        }
        return ResponseEntity.ok(authInfo);
    }

    /**
     * ????????????????????????
     *
     * @return ??????????????????
     */
    @GetMapping(value = "/info")
    public ResponseEntity<Object> getUserInfo() {
        return ResponseEntity.ok(SecurityUtils.getCurrentUser());
    }

    /**
     * ???????????????
     *
     * @return ?????????
     */
    @AnonymousGetMapping(value = "/code")
    public ResponseEntity<Object> getCode() {
        // ?????????????????????
        Captcha captcha = loginProperties.getCaptcha();
        String uuid = properties.getCodeKey() + IdUtil.simpleUUID();
        // ?????????????????????arithmetic???????????? >= 2 ??????captcha.text()??????????????????????????????
        String captchaValue = captcha.text();
        if (captcha.getCharType() - 1 == LoginCodeEnum.arithmetic.ordinal() && captchaValue.contains(".")) {
            captchaValue = captchaValue.split("\\.")[0];
        }
        // ??????
        redisUtils.set(uuid, captchaValue, loginProperties.getLoginCode().getExpiration(), TimeUnit.MINUTES);
        // ???????????????
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
     * ????????????
     *
     * @param request ??????
     * @return ??????
     */
    @Log(value = "??????", logType = LogTypeEnum.LOGIN)
    @DeleteMapping(value = "/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request) {
        onlineUserService.logout(tokenProvider.getToken(request));
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
