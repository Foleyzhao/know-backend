package com.cumulus.modules.business.detect.dto;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * 发现任务请求参数
 *
 * @author zhangxq
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DetectRequest implements Serializable {

    private static final long serialVersionUID = 7761153741773843577L;

    /**
     * 发现任务id
     */
    private String id;

    /**
     * 类型
     */
    private String type = "scan";

    /**
     * ip 单个IP、网段等 必要参数
     * <p>
     * ipv4
     * <p>
     * 正常单个ip：192.168.90.1
     * ip网段： 192.168.90.0/24
     * 以上类型用逗号相隔
     * ipv6
     * <p>
     * 简写形式
     * 全拼形式
     * 以上ipv4/ipv6 类型用逗号空格
     */
    private String assets;

    /**
     * ping扫开关
     */
    private Boolean ping;

    /**
     * 发包速率
     */
    private Integer rate = 50000;

    /**
     * 扫描操作系统
     */
    private boolean os_scan = false;

    /**
     * 记录未成功识别的操作系统指纹
     */
    private boolean os_fingerprint = false;

    /**
     * 指定扫描端口
     */
    private String port;

    /**
     * 扫描最常见的若干个端口
     */
    private Integer port_top;

    /**
     * 不扫描哪些端口
     */
    private String port_filter;

    /**
     * 仅做开放tcp端口扫描（不会有端口详细信息等）
     */
    private boolean port_only = true;

    /**
     * 用逗号或者“-”划分端口，若仅传参数则扫描1-65535的udp端口
     */
    private String udp_scan;

    /**
     * WEB应用识别级别
     * 0：不做WEB应用识别
     * 1：做简单的WEB应用识别（匹配请求的html文本）
     * 2：做复杂的WEB应用识别（访问html，加载js等资源并识别）
     */
    private Integer fp_level = 0;

//    /**
//     * 回报任务开始(用于进度条展示)
//     */
//    private boolean report_start = true;


    /**
     * 停止参数
     */
    public static class StopRequest implements Serializable {

        private static final long serialVersionUID = -7951505655078877268L;

        /**
         * 类型参数 停止 stop
         */
        private final String type = "stop";

        /**
         * 任务id
         */
        private String id;

        public StopRequest(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

}
