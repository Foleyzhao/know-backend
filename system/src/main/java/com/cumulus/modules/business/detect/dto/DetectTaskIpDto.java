package com.cumulus.modules.business.detect.dto;

import java.util.List;

import lombok.Data;

/**
 * 解析ip DTO
 *
 * @author Shijh
 */
@Data
public class DetectTaskIpDto {

    /**
     * 多段ip格式
     */
    private List<IpSegmentList> ipSegmentList;

    /**
     * 其他ip 如:单个ip , ip段(1.1.1.1-10)
     */
    private List<String> ipList;

    /**
     * 掩码格式
     */
    private List<String> ipMaskList;

    /**
     * 记录当前循环的IP
     */
    private String ip;

    /**
     * ip 库 标记
     */
    private boolean ipLib = false;

    private int temp = 0;
    private boolean OK = true;
    private boolean TO = true;
    private boolean FLAG = false;

    @Data
    public static class IpSegmentList {
        private int one;
        private int two;
        private int three;
        private int four;
        private IpSegmentInterval ip1;
        private IpSegmentInterval ip2;
        private IpSegmentInterval ip3;
        private IpSegmentInterval ip4;
        private String IpSegment;
    }

    @Data
    public static class IpSegmentInterval {
        private int min;
        private int max;
    }
}
