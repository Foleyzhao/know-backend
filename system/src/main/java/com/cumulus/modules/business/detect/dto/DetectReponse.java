package com.cumulus.modules.business.detect.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * 发现任务响应实体
 *
 * @author zhangxq
 */
@Getter
@Setter
public class DetectReponse implements Serializable {

    private static final long serialVersionUID = -1907610066248127655L;

    /**
     * 任务id
     */
    private String id;

    /**
     * 类型 web/ip
     */
    private String type;

    /**
     * ip目标
     */
    private String target;

    /**
     * web+ip总消息数分片，例：'1/5'
     */
    private String fragment;

    /**
     * 经纬度
     */
    private Location location;

    /**
     * 地区ASN码（自治系统号）
     */
    private String asn;

    /**
     * 互联网服务提供商
     */
    private String isp;

    /**
     * 国家-省份-城市
     */
    private String geo;

    /**
     * 该网址的对应ip地址
     */
    private String domainIp;


    /**
     * 地理位置
     */
    @Getter
    @Setter
    public static class Location implements Serializable {

        private static final long serialVersionUID = 3366514297138393809L;

        /**
         * 纬度
         */
        private float lat;

        /**
         * 经度
         */
        private float lon;

        @Override
        public String toString() {
            return "Location{" +
                    "lat=" + lat +
                    ", lon=" + lon +
                    '}';
        }
    }

    /**
     * 端口信息
     */
    @Getter
    @Setter
    public static class Port implements Serializable {

        private static final long serialVersionUID = -8416212431811894108L;

        /**
         * 端口号
         */
        private int port;

        /**
         * TCP/UDP
         */
        private String type;

        /**
         * 状态：'open'/'closed'/'filtered'
         */
        private String state;

        /**
         * 状态判断依据
         */
        private String reason;

        /**
         * 可信度，10确信，7大致可相信，3不可信
         */
        private int conf;

        /**
         * 服务名称
         */
        private String name;

        /**
         * 服务产品名称
         */
        private String product;

        /**
         * 版本号
         */
        private String version;

        /**
         * 端口banner信息
         */
        private String banner;

        /**
         * 额外信息
         */
        private String extrainfo;

        /**
         * CPE信息
         */
        private String cpe;

        /**
         * 脚本信息（可能会有，暂不作处理
         */
        private Map script;

        @Override
        public String toString() {
            return "Port{" +
                    "port=" + port +
                    ", type='" + type + '\'' +
                    ", state='" + state + '\'' +
                    ", reason='" + reason + '\'' +
                    ", conf=" + conf +
                    ", name='" + name + '\'' +
                    ", product='" + product + '\'' +
                    ", version='" + version + '\'' +
                    ", banner='" + banner + '\'' +
                    ", extrainfo='" + extrainfo + '\'' +
                    ", cpe='" + cpe + '\'' +
                    ", script=" + script +
                    '}';
        }
    }

    /**
     * ip信息回报数据
     */
    @Getter
    @Setter
    public static class DetectReponseIp extends DetectReponse {

        private static final long serialVersionUID = -3835881115753974411L;

        /**
         * 时间戳，用来标记扫描完成时间
         */
        private float timestamp;

        /**
         * 省份
         */
        private String pro;

        /**
         * 市
         */
        private String city;

        /**
         * 操作系统
         */
        private String os;

        /**
         * MAC地址【局域网特有】
         */
        private String mac;

        /**
         * 是否是ipv6
         */
        private boolean is_ipv6;

        /**
         * 是否在线
         */
        private boolean is_online;

        /**
         * 反查的IP所属域名列表
         */
        private List<String> domains;

        /**
         * 所有TCP端口号的列表,例:[80,111,443]
         */
        private String ports_list;

        /**
         * 端口列表
         */
        private List<Port> ports;

        @Override
        public String toString() {
            return "DetectReponseIp{" +
                    "id='" + getId() + '\'' +
                    ", type='" + getType() + '\'' +
                    ", target='" + getTarget() + '\'' +
                    ", fragment='" + getFragment() + '\'' +
                    ", location=" + getLocation() +
                    ", asn='" + getAsn() + '\'' +
                    ", isp='" + getIsp() + '\'' +
                    ", geo='" + getGeo() + '\'' +
                    ", timestamp=" + timestamp +
                    ", pro='" + pro + '\'' +
                    ", city='" + city + '\'' +
                    ", os='" + os + '\'' +
                    ", mac='" + mac + '\'' +
                    ", is_ipv6=" + is_ipv6 +
                    ", is_online=" + is_online +
                    ", domains=" + domains +
                    ", ports_list=" + ports_list +
                    ", ports=" + ports +
                    '}';
        }
    }

    /**
     * web信息回报数据
     */
    @Getter
    @Setter
    public static class DetectReponseWeb extends DetectReponse {

        private static final long serialVersionUID = 8545173748027843609L;

        /**
         * 该网址的对应ip地址
         */
        private String domainIp;

        /**
         * 端口
         */
        private Integer port;

        /**
         * 该网址的返回状态码
         */
        private String statusCode;

        /**
         * 该网址标题
         */
        private String title;

        /**
         * 该网址的html文本
         */
        private String html;

        /**
         * 该网址的相应头
         */
        private Map headers;

        /**
         * 该网址的证书信息
         */
        private Map cert;

        /**
         * 指纹
         */
        private List<Fingerprint> fingerprints;

        @Override
        public String toString() {
            return "DetectReponseWeb{" +
                    "id='" + getId() + '\'' +
                    ", type='" + getType() + '\'' +
                    ", target='" + getTarget() + '\'' +
                    ", fragment='" + getFragment() + '\'' +
                    ", location=" + getLocation() +
                    ", asn='" + getAsn() + '\'' +
                    ", isp='" + getIsp() + '\'' +
                    ", geo='" + getGeo() + '\'' +
                    ", domainIp='" + domainIp + '\'' +
                    ", port=" + port +
                    ", statusCode='" + statusCode + '\'' +
                    ", title='" + title + '\'' +
                    ", html='" + html + '\'' +
                    ", headers=" + headers +
                    ", cert=" + cert +
                    ", fingerprint=" + fingerprints +
                    '}';
        }
    }

    /**
     * 指纹
     */
    @Getter
    @Setter
    private static class Fingerprint implements Serializable {

        private static final long serialVersionUID = 3846168488960162477L;

        /**
         * slug形式的指纹名称（无空格，符号，全小写）
         */
        private String slug;

        /**
         * 应用名称
         */
        private String name;

        /**
         * 可信度
         */
        private int confidence;

        /**
         * 版本
         */
        private String version;

        /**
         * cpe
         */
        private String cpe;

        /**
         * 应用分类
         */
        private List<Categorie> categories;

        @Override
        public String toString() {
            return "Fingerprint{" +
                    "slug='" + slug + '\'' +
                    ", name='" + name + '\'' +
                    ", confidence=" + confidence +
                    ", version='" + version + '\'' +
                    ", cpe='" + cpe + '\'' +
                    ", categories=" + categories +
                    '}';
        }
    }

    /**
     * 应用分类
     */
    @Getter
    @Setter
    private static class Categorie implements Serializable {

        private static final long serialVersionUID = -7663936328105207897L;

        /**
         * id
         */
        private int id;

        /**
         * slug形式的指纹名称（无空格，符号，全小写）
         */
        private String slug;

        /**
         * 应用名称
         */
        private String name;

        @Override
        public String toString() {
            return "Categorie{" +
                    "id=" + id +
                    ", slug='" + slug + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }


}
