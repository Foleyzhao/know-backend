package com.cumulus.dto;

import com.cumulus.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 本地存储传输对象
 *
 * @author zhaoff
 */
@Getter
@Setter
public class LocalStorageDto extends BaseDTO implements Serializable {

    private static final long serialVersionUID = 2341548305349618218L;

    /**
     * ID
     */
    private Long id;

    /**
     * 真实文件名
     */
    private String realName;

    /**
     * 文件名
     */
    private String name;

    /**
     * 文件后缀
     */
    private String suffix;

    /**
     * 文件类型
     */
    private String type;

    /**
     * 文件大小
     */
    private String size;
}
