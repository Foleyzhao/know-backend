package com.cumulus.modules.system.dto;

import com.cumulus.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * 明细清单对象DTO
 *
 * @author : shenjc
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DetailedFileDto extends BaseEntity {

    private static final long serialVersionUID = -4034682460869233402L;
    /**
     * 主键id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 名称 显示用
     */
    private String name;

    /**
     * 文件类型指定文件夹
     */
    private String type;

    /**
     * 文件后缀 例子 .xlsx
     */
    private String fileSuffix;

    /**
     * 状态  0：生成中 1：生成成功
     */
    private String status;
}
