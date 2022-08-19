package com.cumulus.modules.system.entity;

import com.cumulus.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 明细清单对象
 *
 * @author : shenjc
 */
@Getter
@Setter
@Entity
@Table(name = "sys_detailed_file")
public class DetailedFile extends BaseEntity implements Serializable {


    private static final long serialVersionUID = 6418158700230229623L;

    /**
     * 报表状态 0：生成中 1：生成成功
     */
    public static final int STATUS_GENERATING = 0;
    public static final int STATUS_DONE = 1;
    public static final String STATUS_GENERATING_STR = "生成中";
    public static final String STATUS_DONE_STR = "完成";
    public static final String STATUS_DEFAULT_STR = "未知";


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
    private Integer type;

    /**
     * 文件后缀 例子 .xlsx
     */
    private String fileSuffix;

    /**
     * 状态  0：生成中 1：生成成功
     */
    private Integer status;

    /**
     * 文件实际名字 不要使用中文 建议使用数字 或者英文数字
     */
    private String fileName;

    /**
     * 获取状态中文
     */
    public String statusStr() {
        switch (this.status) {
            case STATUS_GENERATING: {
                return STATUS_GENERATING_STR;
            }
            case STATUS_DONE: {
                return STATUS_DONE_STR;
            }
            default: {
            }
        }
        return STATUS_DEFAULT_STR;
    }
}
