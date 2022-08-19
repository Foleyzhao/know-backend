package com.cumulus.modules.business.gather.request;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * CMD任务信息
 *
 * @author zhaoff
 */
@Getter
@Setter
public class CmdTaskInfo implements Serializable, TaskInfo {

    private static final long serialVersionUID = -6276548443798318497L;

    /**
     * 执行指令
     */
    private String cmd;

}
