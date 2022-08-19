package com.cumulus.modules.business.gather.request;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 采集引擎响应
 *
 * @author zhaoff
 */
@Getter
@Setter
public class TaskResponse implements Serializable {

    private static final long serialVersionUID = 1958228213202351307L;

    /**
     * 唯一标识
     */
    private String id;

    /**
     * 结果（0：成功，-1：连接超时，-2：认证失败（登录失败），-3：手动取消，-4：EOF TIME OUT，-5：解码方式错误，-999：未知错误）
     */
    private Integer res;

    /**
     * 编码
     */
    private String encoding;

    /**
     * Cmd命令回应消息
     */
    private ResponseData responseData;

    /**
     * 开始时间
     */
    private Date stm;

    /**
     * 结束时间
     */
    private Date etm;

    /**
     * 错误信息
     */
    private String errorMsg;

}
