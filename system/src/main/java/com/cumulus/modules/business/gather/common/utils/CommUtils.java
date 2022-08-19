package com.cumulus.modules.business.gather.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.InetAddresses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.io.AnsiOutputStream;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用工具类
 *
 * @author zhaoff
 */
@Slf4j
public class CommUtils {

    /**
     * 类与对象映射对象
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 数据存放路径
     */
    private static String dataDir = null;

    /**
     * 资管配置文件内容（/etc/yuan/asset.conf）
     */
    private static Properties platformConfig = null;

    /**
     * 配置文件位置
     */
    //public static final String CONFIG_FILE = "/etc/yuan/asset.conf";
    public static final String CONFIG_FILE = "C:\\Users\\FoleyZhao\\Desktop\\asset.conf";

    /**
     * 读取平台配置文件
     *
     * @return 配置属性内容
     */
    public static synchronized Properties getAssetConfigurations() {
        if (platformConfig != null && !platformConfig.isEmpty()) {
            return platformConfig;
        }
        platformConfig = new Properties();
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            Reader reader = null;
            try {
                reader = new BufferedReader(new FileReader(configFile));
                platformConfig.load(reader);
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("Failed to read config: " + CONFIG_FILE, e);
                }
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }
        return platformConfig;
    }

    /**
     * 处理ansi字符。
     *
     * @param str 需要处理的字符串。
     * @return 返回值
     */
    public static String stripAnsi(String str) {
        if (str == null) {
            return "";
        }

        String result = "";
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); AnsiOutputStream aos = new AnsiOutputStream(baos, null, null, null, null, null, null, null, null, false)) {
            aos.write(str.getBytes());
            result = baos.toString();
        } catch (Exception e) {
            log.warn(null, e);
        }
        // ignore
        return result;
    }

    /**
     * 返回数据存放路径
     *
     * @return 数据存放路径。
     */
    public static String getDataDir() {
        if (dataDir == null) {
            dataDir = System.getProperty("asset.datadir");
            if (dataDir == null) {
                if (new File(CONFIG_FILE).exists()) {
                    try {
                        dataDir = getAssetConfigurations().getProperty("asset.datadir");
                    } catch (Exception e) {
                        log.warn("Failed to read config file: " + CONFIG_FILE, e);
                    }
                }
            }
            if (dataDir == null) {
                File dir = new File(System.getProperty("user.home"), "asset_data");
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        log.warn("Failed to mkdirs config dir: " + CONFIG_FILE);
                    }
                }
                dataDir = dir.getAbsolutePath();
            }
        }
        return dataDir;
    }

    /**
     * 采集任务的最大并发数
     *
     * @return 采集任务的最大并发数
     */
    public static int getGatherTaskNum() {
        return getNumInternal("business.gather.taskNum", 4);
    }

    /**
     * 获取数字类型的属性
     *
     * @param propStr    属性的键
     * @param defaultNum 默认的值
     * @return 指定属性的值
     */
    private static int getNumInternal(String propStr, int defaultNum) {
        int ret = defaultNum;
        try {
            String str = getAssetConfigurations().getProperty(propStr);
            ret = Integer.parseInt(str);
            if (ret <= 0) {
                ret = defaultNum;
            }
        } catch (Exception e) {
            // ignore
        }
        return ret;
    }

    /**
     * 获取巡检的最大连接数, processor小于4，默认最大限制为50，否则为100
     *
     * @return 巡检的最大连接数
     */
    public static int getGatherJobNum() {
        int processor = Runtime.getRuntime().availableProcessors();
        if (processor <= 4) {
            return getNumInternal("business.blchk.jobNum", 50);
        }
        return getNumInternal("business.blchk.jobNum", 100);
    }

    /**
     * 判断集合是否为Null或者Empty
     *
     * @param collection 集合
     * @return Null或者Empty返回true, 否则返回false
     */
    public static boolean isEmptyOfCollection(Collection<?>... collection) {
        for (Collection<?> coll : collection) {
            if (coll != null && coll.size() > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断Map是否为Null或者Empty
     *
     * @param maps Map集合
     * @return Null或者Empty返回true, 否则返回false
     */
    public static boolean isEmptyOfMap(Map<?, ?>... maps) {
        for (Map<?, ?> map : maps) {
            if (map != null && map.size() > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取不常变化类采集任务超时时间，单位秒
     *
     * @return 超时时间
     */
    public static int getGatherSeldomTimeout() {
        return getNumInternal("business.gather.infrequent.timeout", 300);
    }

    /**
     * 获取实时类采集任务超时时间，单位秒
     *
     * @return 超时时间
     */
    public static int getGatherFrequentlyTimeout() {
        return getNumInternal("business.gather.realtime.timeout", 300);
    }

    /**
     * 获取耗时类采集任务超时时间，单位秒
     *
     * @return 超时时间
     */
    public static int getGatherStationaryTimeout() {
        return getNumInternal("business.gather.consumer.timeout", 900);
    }


    /**
     * 创建一个唯一性 ID
     *
     * @return 唯一性ID
     */
    public static String createAuditId() {
        StringBuilder sb = new StringBuilder("S");
        String id = Long.toUnsignedString(UUID.randomUUID().getMostSignificantBits(), 36);
        if (id.length() < 13) {
            for (int i = 0; i < (13 - id.length()); i++) {
                sb.append("0");
            }
        }
        sb.append(id.toUpperCase());
        return sb.toString();
    }

    /**
     * 将对象转换为json字符串，主要是方便记录日志
     *
     * @param obj 要转换的对象
     * @return json格式的字符串
     */
    public static String toJson(Object obj) {
        String ret;
        try {
            ret = mapper.writeValueAsString(obj);
        } catch (Exception e) {
            ret = "<class:" + obj.getClass().toString() + ">";
        }
        return ret;
    }

    /**
     * 判断Map集合是否有一个Null或者Empty
     *
     * @param maps Map集合
     * @return true，false
     */
    public static boolean isEmptyAnyMap(Map<?, ?>... maps) {
        for (Map<?, ?> map : maps) {
            if (map == null || map.size() == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据日期、域、间隔
     *
     * @param date    日期
     * @param field   日历的月、天等域
     * @param inteval 间隔
     * @return 日期的long型值
     */
    public static Date getDate(Date date, int field, Integer inteval) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(field, inteval);
        return cal.getTime();
    }

    /**
     * 检测map对象是否存在对应key值
     *
     * @param map 对象 （不检测空）
     * @param key 键值  （不检测空）
     * @return true 存在 fasle 不存在
     */
    public static Boolean checkNull(Map<String, Object> map, String key) {
        return map.containsKey(key) && map.get(key) != null && StringUtils.isNotBlank(map.get(key).toString());
    }

    /**
     * 去除字符串中的unicode字符，用空格代替代替
     *
     * @param source 源字符串
     * @return 去除后的字符串
     */
    public static String eraseUnicodeChar(String source) {
        if (null == source) {
            return null;
        }
        String result = source;
        Pattern pattern = Pattern.compile("[\u0000|\u000F]");
        Matcher matcher = pattern.matcher(result);
        if (matcher.find()) {
            result = matcher.replaceAll(" ");
        }
        return result;
    }

    /**
     * 判断ip地址是否是IPV6
     *
     * @param ip ip地址
     * @return 结果
     */
    public static boolean checkIPV6(String ip) {
        try {
            InetAddress address = InetAddresses.forString(ip);
            return address instanceof Inet6Address;
        } catch (Exception e) {
            return false;
        }
    }

}
