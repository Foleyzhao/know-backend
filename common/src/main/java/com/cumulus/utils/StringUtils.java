package com.cumulus.utils;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cumulus.config.PlatformProperties;
import com.cumulus.constant.PlatformConstant;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import net.dreamlu.mica.ip2region.core.IpInfo;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

/**
 * 字符串工具类
 */
@Slf4j
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    /**
     * 下划线
     */
    private static final char SEPARATOR = '_';

    /**
     * 未知IP标识
     */
    private static final String UNKNOWN = "unknown";

    /**
     * IP转化详细地址服务
     */
    private final static Ip2regionSearcher ip2regionSearcher = SpringContextHolder.getBean(Ip2regionSearcher.class);

    /**
     * UserAgent分析器
     */
    private static final UserAgentAnalyzer USER_AGENT_ANALYZER = UserAgentAnalyzer
            .newBuilder()
            .hideMatcherLoadStats()
            .withCache(10000)
            .withField(UserAgent.AGENT_NAME_VERSION)
            .build();

    /**
     * 下划线命名转驼峰命名（首字母小写）
     *
     * @return 转化结果
     */
    public static String toCamelCase(String s) {
        if (null == s) {
            return null;
        }
        s = s.toLowerCase();
        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == SEPARATOR) {
                upperCase = true;
            } else if (upperCase) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 下划线命名转驼峰命名（首字母大写）
     *
     * @return 转化结果
     */
    public static String toCapitalizeCamelCase(String s) {
        if (null == s) {
            return null;
        }
        s = toCamelCase(s);
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /**
     * 驼峰命名转下划线命名
     *
     * @return 转化结果
     */
    static String toUnderScoreCase(String s) {
        if (null == s) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean nextUpperCase = true;
            if (i < (s.length() - 1)) {
                nextUpperCase = Character.isUpperCase(s.charAt(i + 1));
            }
            if ((i > 0) && Character.isUpperCase(c)) {
                if (!upperCase || !nextUpperCase) {
                    sb.append(SEPARATOR);
                }
                upperCase = true;
            } else {
                upperCase = false;
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    /**
     * 获取IP地址
     *
     * @param request 请求
     * @return IP地址
     */
    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (null == ip || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (null == ip || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (null == ip || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        String comma = ",";
        String localhost = "127.0.0.1";
        if (ip.contains(comma)) {
            ip = ip.split(",")[0];
        }
        if (localhost.equals(ip)) {
            // 获取本机真正的ip地址
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return ip;
    }

    /**
     * 根据IP地址获取详细地址
     *
     * @param ip IP地址
     * @return 详细地址
     */
    public static String getCityInfo(String ip) {
        if (PlatformProperties.ipLocal) {
            return getLocalCityInfo(ip);
        } else {
            return getHttpCityInfo(ip);
        }
    }

    /**
     * 根据IP地址获取详细地址（第三方IP库）
     *
     * @param ip IP地址
     * @return 详细地址
     */
    public static String getHttpCityInfo(String ip) {
        String api = String.format(PlatformConstant.Url.IP_URL, ip);
        JSONObject object = JSONUtil.parseObj(HttpUtil.get(api));
        return object.get("addr", String.class);
    }

    /**
     * 根据IP地址获取详细地址（本地解析）
     *
     * @param ip IP地址
     * @return 详细地址
     */
    public static String getLocalCityInfo(String ip) {
        IpInfo ipInfo = ip2regionSearcher.memorySearch(ip);
        if (null != ipInfo) {
            return ipInfo.getAddress();
        }
        return null;
    }

    /**
     * 获取浏览器信息
     *
     * @param request http请求
     * @return 浏览器信息
     */
    public static String getBrowser(HttpServletRequest request) {
        UserAgent.ImmutableUserAgent userAgent = USER_AGENT_ANALYZER.parse(request.getHeader("User-Agent"));
        return userAgent.get(UserAgent.AGENT_NAME_VERSION).getValue();
    }

    /**
     * 获得当前的星期字符串
     *
     * @return 星期字符串
     */
    public static String getWeekDay() {
        String[] weekDays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return weekDays[w];
    }

    /**
     * 获取本地IP
     *
     * @return 本地IP
     */
    public static String getLocalIp() {
        try {
            InetAddress candidateAddress = null;
            // 遍历所有的网络接口
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                 interfaces.hasMoreElements(); ) {
                NetworkInterface anInterface = interfaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration<InetAddress> inetAddresses = anInterface.getInetAddresses();
                     inetAddresses.hasMoreElements(); ) {
                    InetAddress inetAddr = inetAddresses.nextElement();
                    // 排除loopback类型地址
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            // 判断是否是site-local地址
                            return inetAddr.getHostAddress();
                        } else if (null == candidateAddress) {
                            // 记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (null != candidateAddress) {
                return candidateAddress.getHostAddress();
            }
            // 没有发现non-loopback地址
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (null == jdkSuppliedAddress) {
                return "";
            }
            return jdkSuppliedAddress.getHostAddress();
        } catch (Exception e) {
            return "";
        }
    }
}
