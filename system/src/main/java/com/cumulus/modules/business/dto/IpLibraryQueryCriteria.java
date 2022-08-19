package com.cumulus.modules.business.dto;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.cumulus.annotation.Query;
import lombok.Getter;
import lombok.Setter;

/**
 * ip库查询参数对象
 *
 * @author zhangxq
 */
@Getter
@Setter
public class IpLibraryQueryCriteria {

    /**
     * 模糊查询内容
     */
    @Query(blurry = "ip")
    private String blurry;

    /**
     * ip
     */
    @Query(type = Query.Type.INNER_LIKE)
    private String ip;

    /**
     * 资产部门
     */
    @Query(propName = "dept", type = Query.Type.IN)
    private Set<Long> deptIds = new HashSet<>();

    /**
     * 创建时间
     */
    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;

}
