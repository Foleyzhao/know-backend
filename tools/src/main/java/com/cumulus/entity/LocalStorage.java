package com.cumulus.entity;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.cumulus.base.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 本地存储实体
 *
 * @author zhaoff
 */
@Getter
@Setter
@Entity
@Table(name = "tool_local_storage")
@NoArgsConstructor
public class LocalStorage extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 4623803357176109257L;

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
     * 文件路径
     */
    private String path;

    /**
     * 文件类型
     */
    private String type;

    /**
     * 文件大小
     */
    private String size;

    /**
     * 构造方法
     *
     * @param realName 真实文件名
     * @param name     文件名
     * @param suffix   文件后缀
     * @param path     文件路径
     * @param type     文件类型
     * @param size     文件大小
     */
    public LocalStorage(String realName, String name, String suffix, String path, String type, String size) {
        this.realName = realName;
        this.name = name;
        this.suffix = suffix;
        this.path = path;
        this.type = type;
        this.size = size;
    }

    /**
     * 拷贝方法
     *
     * @param source 本地存储实体
     */
    public void copy(LocalStorage source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }

}
