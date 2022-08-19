package com.cumulus.modules.mnt.dto;

import com.cumulus.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 应用传输对象
 */
@Getter
@Setter
public class AppDto extends BaseDTO implements Serializable {

	private static final long serialVersionUID = -7057292411587080035L;

	/**
	 * ID
	 */
    private Long id;

	/**
	 * 应用名称
	 */
	private String name;

	/**
	 * 端口
	 */
	private Integer port;

	/**
	 * 上传目录
	 */
	private String uploadPath;

	/**
	 * 部署目录
	 */
	private String deployPath;

	/**
	 * 备份目录
	 */
	private String backupPath;

	/**
	 * 启动脚本
	 */
	private String startScript;

	/**
	 * 部署脚本
	 */
	private String deployScript;

}
