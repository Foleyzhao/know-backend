package com.cumulus.modules.system.dto;

import com.cumulus.annotation.DataPermission;
import com.cumulus.annotation.Query;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 系统用户查询传输对象
 */
@Data
@DataPermission(joinName = "dept", fieldName = "id")
public class UserQueryCriteria implements Serializable {

    private static final long serialVersionUID = -7922535903001652656L;

    /**
     * ID
     */
    @Query
    private Long id;

    /**
     * 用户部门
     */
    @Query(propName = "id", type = Query.Type.IN, joinName = "dept")
    private Set<Long> deptIds = new HashSet<>();

    /**
     * 模糊查询内容
     */
    @Query(blurry = "email,username,nickName")
    private String blurry;

    /**
     * 是否启用
     */
    @Query
    private Boolean enabled;

    /**
     * 用户部门ID
     */
    private Long deptId;

    /**
     * 创建时间
     */
    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;

}
