package com.cumulus.modules.mnt.dto;

import com.cumulus.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 数据库传输对象
 */
@Getter
@Setter
public class DatabaseDto extends BaseDTO implements Serializable {

	private static final long serialVersionUID = -2652870999699473545L;

	/**
	 * ID
	 */
    private String id;

	/**
	 * 数据库名称
	 */
    private String name;

	/**
	 * 数据库连接地址
	 */
    private String jdbcUrl;

	/**
	 * 数据库密码
	 */
    private String pwd;

	/**
	 * 用户名
	 */
    private String userName;
}
