package com.cumulus.modules.business.dto;

import java.io.Serializable;
import com.cumulus.base.BaseDTO;
import com.cumulus.modules.system.entity.Dept;
import lombok.Getter;
import lombok.Setter;

/**
 * ip库
 *
 * @author zhangxq
 */
@Getter
@Setter
public class IpLibraryDto extends BaseDTO implements Serializable {

    private static final long serialVersionUID = 2863377018295099619L;

    /**
     * ID
     */
    private Long id;

    /**
     * ip
     */
    private String ip;

    /**
     * 部门
     */
    private Dept dept;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * ip段
     */
    private String ipRange;

    /**
     * 导入结果
     */
    private String result;
}
