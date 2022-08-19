package com.cumulus.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.cumulus.exception.BadRequestException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.connection.stream.StreamInfo;

/**
 * 校验IP
 *
 * @author shenjc
 */
@Slf4j
public class RegexUtil {

    /**
     * 单个ip最长的大小 包括ipv4 ipv6 和其ip段长度暂定为64
     */
    public static final int MAX_IP_LENGTH = 64;

    /**
     * 多个字段的分隔符号 支持 中文逗号 、 英文逗号 空格
     */
    public static final String IP_LIST_SEPARATE_STR = ",|，| ";

    /**
     * IPV6 分隔符
     */
    public static final String IPV6_SEPARATE_STR = ":";

    /**
     * IPV4 分隔符
     */
    public static final String IPV4_SEPARATE_STR = ".";

    /**
     * 掩码分隔符
     */
    public static final String MASK_SEPARATE_STR = "/";

    /**
     * IPV4段分隔符
     */
    public static final String IPV4_PARAGRAPH_STR = "-";

    /**
     * 逗号分隔符
     */
    public static final String SEPARATOR_COMMA = ",";

    /**
     * 完整IPV6 正则 0:0:0:0:0:0:0:0
     */
    public static final String COMPLETE_IPV6_PATTERN = "([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4})";

    /**
     * 单个Ipv6 正则 1111
     */
    public static final String SINGLE_IPV6_PATTERN = "[0-9A-Fa-f]{1,4}";

    /**
     * IPV4段 1.1.1.1-1 格式
     */
    public static final String IPV4_PARAGRAPH_PATTERN = "^((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}-((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})$";

    /**
     * ipv4 正则
     */
    public static final String IPV4_PATTERN = "^((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}$";

    /**
     * 网址正则 可能使用 @Url校验代替
     */
    public static final String URL = "^(https?|ftp):\\/\\/([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?)(\\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}|([a-zA-Z0-9-]+\\.)*[a-zA-Z0-9-]+\\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(:[0-9]+)*(\\/($|[a-zA-Z0-9.,?'\\\\+&%$#=~_-]+))*$";
    public static final String WWW = "^(((ht|f)tps?):\\/\\/)?[\\w-]+(\\.[\\w-]+)+([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?$";

    /**
     * 数字 字母
     */
    public static final String NUMBER_CHARACTER = "[0-9A-Za-z]+";

    /**
     * 字母
     */
    public static final String CHARACTER = "[A-Za-z]+";

    /**
     * 数字
     */
    public static final String NUMBER = "[0-9]+";

    /**
     * 中文 数字 字母 正则
     */
    public static final String CHINESE_NUMBER_CHARACTER = "[\\u4e00-\\u9fa50-9A-Za-z]+";

    /**
     * 默认密码正则 中文 数字 特殊符号 需要2种 长度另作限制
     */
    public static final String DEFAULT_PWD_PATTERN = "(?!^(\\d+|[a-zA-Z]+|[~!@#$%^&*?]+)$)^[\\w~!@#$%^&*?]+$";
    /**
     * IP分隔符
     */
    public static final String IP_SEPARATOR = "[,|，|\\s]+";
    /**
     * 中文16进制区间
     */
    private static final String HEX_CHINESE_SCOPE = "[\u4e00-\u9fa5]";

    /**
     * @param ip ipv6的校验
     */
    public static boolean regexIPV6(String ip) {
        String regexIPV6 = "((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|:)))$";
        if (ip.matches(regexIPV6)) {
            log.info("The match is successful = " + ip);
            return true;
        } else {
            log.info("Matching failure = " + ip);
            return false;
        }
    }

    /**
     * @param ip ipv4的掩码校验
     */
    public static boolean regexIPV4Mask(String ip) {
        String IPv4Mask = "^((?:(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(?:(/([1-9]|[1-2]\\d|3[0-1])))?)$";
        if (ip.matches(IPv4Mask)) {
            log.info("The match is successful = " + ip);
            return true;
        } else {
            log.info("Matching failure = " + ip);
            return false;
        }
    }

    /**
     * ipv4的校验
     *
     * @param ip
     */
    public static boolean regexIPV4(String ip) {
        String regexIPV4 = "^(((?:(?:1[0-9][0-9]\\.)|(?:2[0-4][0-9]\\.)|(?:25[0-5]\\.)|(?:[1-9][0-9]\\.)|(?:[0-9]\\.)){3}(?:(?:1[0-9][0-9])|(?:2[0-4][0-9])|(?:25[0-5])|(?:[1-9][0-9])|(?:[0-9]))))(\\,((?:(?:1[0-9][0-9]\\.)|(?:2[0-4][0-9]\\.)|(?:25[0-5]\\.)|(?:[1-9][0-9]\\.)|(?:[0-9]\\.)){3}(?:(?:1[0-9][0-9])|(?:2[0-4][0-9])|(?:25[0-5])|(?:[1-9][0-9])|(?:[0-9]))))*$";
        if (ip.matches(regexIPV4)) {
            log.info("The match is successful = " + ip);
            return true;
        } else {
            log.info("Matching failure = " + ip);
            return false;
        }
    }

    /**
     * 移除字符串中的 换行 回车 制表符
     *
     * @param originalStr
     * @return
     */
    public static String removeRNT(String originalStr) {
        if (originalStr == null || originalStr.isEmpty()) {
            return originalStr;
        }
        return originalStr.replaceAll("[\t\n\r\\s]", "");
    }

    /**
     * 将 ipv6 转换为8位全小写的地址
     *
     * @param ipv6 ipv6
     * @return 返回的全写小且8位的ipv6
     */
    public static String completIpv6(String ipv6) {
        if (RegexUtil.regexIPV4(ipv6)) {
            return ipv6;
        }
        StringBuffer str = new StringBuffer();
        String ip = null;
        try {
            ip = InetAddress.getByName(ipv6).toString().replace("/", "");
            System.out.println("ip =>" + ip);
        } catch (UnknownHostException e) {
            log.error("ipv6 Convert exceptions" + ip);
        }
        String[] info = ip.split(":");
        for (int i = 0; i < info.length; i++) {
            switch (info[i].length()) {
                case 1:
                    info[i] = "000" + info[i];
                    break;
                case 2:
                    info[i] = "00" + info[i];
                    break;
                case 3:
                    info[i] = "0" + info[i];
                    break;
                default:
                    break;
            }
            if (i < 7) {
                str.append(info[i] + ":");
            } else {
                str.append(info[i]);
            }
        }
        return str.toString();
    }

    /**
     * 将Ipv4 的ip段分解为3段 例：1.1.1.10-20 ,0：1.1.1 ，1：10， 2：20
     */
    public static List<Object> getIpv4ParagraphInfo(String ipv4Paragraph) {
        ipv4Paragraph = ipv4Paragraph.trim();
        if (!ipv4Paragraph.matches(IPV4_PARAGRAPH_PATTERN)) {
            return null;
        }
        List<Object> result = new ArrayList<>();
        String ipv4Prefix = ipv4Paragraph.substring(0, ipv4Paragraph.lastIndexOf(IPV4_SEPARATE_STR));
        String[] ipv4Suffix = ipv4Paragraph.substring(ipv4Paragraph.lastIndexOf(IPV4_SEPARATE_STR) + 1).split(IPV4_PARAGRAPH_STR);
        int minIpv4Suffix = Integer.parseInt(ipv4Suffix[0]);
        int maxIpv4Suffix = Integer.parseInt(ipv4Suffix[1]);
        if (minIpv4Suffix >= maxIpv4Suffix) {
            return null;
        }
        result.add(ipv4Prefix);
        result.add(minIpv4Suffix);
        result.add(maxIpv4Suffix);
        return result;
    }

    /**
     * 检查IP格式并返回全写
     *
     * @param srcIP     ipv4/ipv6
     * @param exception 是否抛出异常
     * @return 格式正确返回全写ip，错误返回null
     */
    public static String checkToComplete(String srcIP, boolean exception) {
        if (srcIP == null || "".equals(srcIP)) {
            return null;
        }
        String ip = null;
        try {
            ip = InetAddress.getByName(srcIP).getHostAddress();
        } catch (UnknownHostException e) {
            log.error("ip format error :" + srcIP);
            if (exception) {
                throw new BadRequestException("ip格式错误");
            }
        }
        return ip;
    }


    /**
     * ip段变ip列表
     *
     * @param ipStr
     * @return
     */
    public static List<String> ipStrToList(String ipStr) {
        String[] ips = ipStr.split(IP_SEPARATOR);
        List<String> resList = new ArrayList<>();
        String tempIp;
        for (String ip : ips) {
            //首先判断是否是ipv6
            boolean ipv6 = ip.contains(RegexUtil.IPV6_SEPARATE_STR);
            if (ipv6) {
                //ipv6 直接加入
                if (checkToComplete(ip, false) != null) {
                    resList.add(ip);
                }
            } else {
                //ipv4判断是否为ip段
                if (!ip.contains(RegexUtil.IPV4_PARAGRAPH_STR)) {
                    //单个ipv4
                    if (ip.matches(RegexUtil.IPV4_PATTERN)) {
                        resList.add(ip);
                    }
                } else {
                    //ipv4 段
                    List<Object> ipv4ParagraphInfo = RegexUtil.getIpv4ParagraphInfo(ip);
                    if (ipv4ParagraphInfo == null) {
                        continue;
                    }
                    ListIterator<Object> ipv4ParagraphIterator = ipv4ParagraphInfo.listIterator();
                    String ipv4Prefix = (String) ipv4ParagraphIterator.next();
                    int ipv4SuffixMin = (int) ipv4ParagraphIterator.next();
                    int ipv4SuffixMax = (int) ipv4ParagraphIterator.next();
                    for (int i = ipv4SuffixMin; i <= ipv4SuffixMax; i++) {
                        tempIp = ipv4Prefix + RegexUtil.IPV4_SEPARATE_STR + i;
                        resList.add(tempIp);
                    }
                }
            }
        }
        return resList;
    }

    /**
     * URL的限制
     *
     * @param scanAddress 接口地址
     * @param maxSize     最大长度
     * @return 返回校验是否成功
     */
    public static boolean regexPortAddress(String scanAddress, int maxSize) {
        if (scanAddress.length() > maxSize) {
            if (log.isInfoEnabled()) {
                log.info("Matching failure = " + scanAddress);
            }
            return false;
        }
        if (scanAddress.matches(URL) || scanAddress.matches(WWW)) {
            if (log.isInfoEnabled()) {
                log.info("The match with the regular expression succeeded = " + scanAddress);
            }
            return true;
        }
        if (log.isInfoEnabled()) {
            log.info("Matching failure = " + scanAddress);
        }
        return false;
    }

    /**
     * 限制长度 不可输入中文等特殊字符
     *
     * @param str     字符串
     * @param maxSize 最大长度
     * @return 返回校验是否成功
     */
    public static boolean regexNumAndChar(String str, int maxSize) {
        if (StringUtils.isNotBlank(str) && str.length() < maxSize && str.matches(NUMBER_CHARACTER)) {
            log.info("The match with the regular expression succeeded = " + str);
            return true;
        } else {
            log.warn("Matching failure = " + str);
            return false;
        }
    }

    /**
     * 限制长度 不可输入特殊字符
     *
     * @param str     字符串
     * @param maxSize 最大长度
     * @return 返回校验是否成功
     */
    public static boolean regexNumAndCharAndChinese(String str, int maxSize) {
        if (StringUtils.isNotBlank(str) && str.length() < maxSize && str.matches(CHINESE_NUMBER_CHARACTER)) {
            log.info("The match with the regular expression succeeded = " + str);
            return true;
        } else {
            log.warn("Matching failure = " + str);
            return false;
        }
    }

    /**
     * 将字符串进行校验
     *
     * @param strList    进行校验的字符串
     * @param regularStr 正则表达式
     * @return 返回校验结果 列表为空也返回true
     */
    public static boolean regexRegular(String regularStr, String... strList) {
        for (String str : strList) {
            if (str != null && str.matches(regularStr)) {
                log.info("The match with the regular expression succeeded = " + str);
            } else {
                log.warn("Matching failure = " + str);
                return false;
            }
        }
        return true;
    }

    /**
     * 校验字段长度
     *
     * @param maxLength 字符串最大长度
     * @param minLength 字符串最小长度
     * @param nullAble  是否可以为 null
     * @param strList   字符串列表
     * @return 返回结果 list为空返回true
     */
    public static boolean regexHexLengthAndBlank(int maxLength, int minLength, boolean nullAble, String... strList) {
        for (String str : strList) {
            if (str == null) {
                if (nullAble) {
                    continue;
                } else {
                    return false;
                }
            }
            if (StringUtils.isBlank(str)) {
                return false;
            }
            final int size = hexLength(str);
            if (size > maxLength || size < minLength) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取16进制字符长度 中文算2 英文算1
     *
     * @param value 字符串
     */
    public static int hexLength(String value) {
        int valueLength = 0;
        for (int i = 0; i < value.length(); i++) {
            String temp = value.substring(i, i + 1);
            if (temp.matches(HEX_CHINESE_SCOPE)) {
                valueLength += 2;
            } else {
                valueLength += 1;
            }
        }
        return valueLength;
    }

    /**
     * 获取ip的个数
     *
     * @param ipList ipList
     * @return 数量
     */
    public static int getNum(String ipList) {
        int multiSegment = 0;
        int one = 1;
        int two = 1;
        int three = 1;
        int mask = 0;
        int size = 0;
        String[] ips = ipList.split(",");
        for (String ip : ips) {
            String[] split = ip.split("\\.");
            if (split[0].contains("-") || split[1].contains("-") || split[2].contains("-")) {
                if (split[0].contains("-")) {
                    String[] split1 = split[0].split("-");
                    one = Integer.parseInt(split1[1]) - Integer.parseInt(split1[0]) + 1;
                }
                if (split[1].contains("-")) {
                    String[] split1 = split[0].split("-");
                    two = Integer.parseInt(split1[1]) - Integer.parseInt(split1[0]) + 1;
                }
                if (split[2].contains("-")) {
                    String[] split1 = split[0].split("-");
                    three = Integer.parseInt(split1[1]) - Integer.parseInt(split1[0]) + 1;
                }
                multiSegment = one * two * three;
            } else if (ip.contains("/")) {
                String[] split1 = ip.split("/");
                if ("24".equals(split1[1])) {
                    mask += 256;
                }
                if ("16".equals(split1[1])) {
                    mask += 256 * 256;
                }
                if ("8".equals(split1[1])) {
                    mask += 256 * 256 * 256;
                }
            } else {
                List<String> list = RegexUtil.ipStrToList(ip);
                size += list.size();
            }
        }
        return multiSegment + size + mask;
    }
}