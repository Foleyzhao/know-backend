package com.cumulus.modules.security.config.bean;

import com.cumulus.exception.BadConfigurationException;
import com.cumulus.utils.StringUtils;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.ChineseCaptcha;
import com.wf.captcha.ChineseGifCaptcha;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import lombok.Data;

import java.awt.Font;
import java.util.Objects;

/**
 * 登录配置信息
 *
 * @author shenjc
 */
@Data
public class LoginProperties {

    /**
     * 是否是单用户登录
     */
    private boolean useCustomizePermission = false;

    /**
     * 是否是单用户登录
     */
    private boolean singleLogin = false;

    /**
     * 登录验证码配置信息
     */
    private LoginCode loginCode;

    /**
     * 用户登录信息是否缓存
     */
    private boolean cacheEnable;

    /**
     * 是否是单用户登录
     *
     * @return 是否是单用户登录
     */
    public boolean isSingleLogin() {
        return singleLogin;
    }

    /**
     * 用户登录信息是否缓存
     *
     * @return 用户登录信息是否缓存
     */
    public boolean isCacheEnable() {
        return cacheEnable;
    }

    /**
     * 获取验证码生成类
     *
     * @return 验证码生成类
     */
    public Captcha getCaptcha() {
        if (Objects.isNull(loginCode)) {
            loginCode = new LoginCode();
            if (Objects.isNull(loginCode.getCodeType())) {
                loginCode.setCodeType(LoginCodeEnum.arithmetic);
            }
        }
        return switchCaptcha(loginCode);
    }

    /**
     * 依据配置信息配置验证码生成类
     *
     * @param loginCode 验证码配置信息
     * @return 验证码生成类
     */
    private Captcha switchCaptcha(LoginCode loginCode) {
        Captcha captcha;
        synchronized (this) {
            switch (loginCode.getCodeType()) {
                case arithmetic:
                    // 算术类型
                    captcha = new FixedArithmeticCaptcha(loginCode.getWidth(), loginCode.getHeight());
                    break;
                case chinese:
                    captcha = new ChineseCaptcha(loginCode.getWidth(), loginCode.getHeight());
                    break;
                case chinese_gif:
                    captcha = new ChineseGifCaptcha(loginCode.getWidth(), loginCode.getHeight());
                    break;
                case gif:
                    captcha = new GifCaptcha(loginCode.getWidth(), loginCode.getHeight());
                    captcha.setCharType(loginCode.getCharType().getValue());
                    break;
                case spec:
                    captcha = new SpecCaptcha(loginCode.getWidth(), loginCode.getHeight());
                    captcha.setCharType(loginCode.getCharType().getValue());
                    break;
                default:
                    throw new BadConfigurationException("验证码配置信息错误！");
            }
        }
        if (StringUtils.isNotBlank(loginCode.getFontName())) {
            captcha.setFont(new Font(loginCode.getFontName(), Font.PLAIN, loginCode.getFontSize()));
        }
        captcha.setLen(loginCode.getLength());
        return captcha;
    }

    /**
     * 算术类型验证码生成类
     */
    static class FixedArithmeticCaptcha extends ArithmeticCaptcha {

        /**
         * 构造方法
         *
         * @param width  宽度
         * @param height 长度
         */
        public FixedArithmeticCaptcha(int width, int height) {
            super(width, height);
        }

        @Override
        protected char[] alphas() {
            // 生成随机数字和运算符
            int n1 = num(1, 10), n2 = num(1, 10);
            int opt = num(3);
            // 计算结果
            int res = new int[]{n1 + n2, n1 - n2, n1 * n2}[opt];
            // 转换为字符运算符
            char optChar = "+-x".charAt(opt);
            this.setArithmeticString(String.format("%s%c%s=?", n1, optChar, n2));
            this.chars = String.valueOf(res);
            return chars.toCharArray();
        }
    }
}
