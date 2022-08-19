package com.cumulus.modules.business.entity;

import com.cumulus.base.BaseEntity;
import com.cumulus.modules.system.entity.Dept;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * IP库实体类
 *
 * @author zhangxq
 */
@Setter
@Getter
@Entity
@Table(name = "tbl_ip_library")
@DynamicUpdate
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IpLibrary extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -1028046578476023277L;

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * IP
     */
    private String ip;

    /**
     * IP全写
     */
    private String completeIp;

    /**
     * 所属部门
     */
    @ManyToOne
    @JoinColumn(name = "dept_id")
    private Dept dept;

}
