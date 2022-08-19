package com.cumulus.modules.business.gather.request;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Cmd命令回应消息
 *
 * @author zhaoff
 */
@Getter
@Setter
public class CmdResponseData implements Serializable, ResponseData {

    private static final long serialVersionUID = -8968070185265172632L;

    /**
     * 执行的命令
     */
    private String cmd;

    /**
     * 返回码
     */
    private Integer returnCode;

    /**
     * 错误输出流
     */
    private String stderr;

    /**
     * 标准输出流
     */
    private String stdout;

}
