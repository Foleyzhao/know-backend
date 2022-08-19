package com.cumulus.modules.business.gather.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * 采集的命令列表
 *
 * @author zhaoff
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GatherDetailCommands implements Cloneable, Serializable {

    private static final long serialVersionUID = -17882842214165387L;

    /**
     * 采集命令列表
     */
    private List<GatherDetailCommand> command;

    @Override
    public GatherDetailCommands clone() throws CloneNotSupportedException {
        GatherDetailCommands ret = (GatherDetailCommands) super.clone();
        if (null != command) {
            ret.command = new LinkedList<>();
            for (GatherDetailCommand cmd : command) {
                ret.command.add(cmd.clone());
            }
        }
        return ret;
    }
}
