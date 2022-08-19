package com.cumulus.utils;

/**
 * Redis Key前缀
 */
public interface CacheKey {

    /**
     * 用户-ID
     */
    String USER_ID = "user::id:";

    /**
     * 数据-用户
     */
    String DATA_USER = "data::user:";

    /**
     * 菜单-ID
     */
    String MENU_ID = "menu::id:";

    /**
     * 菜单-用户
     */
    String MENU_USER = "menu::user:";

    /**
     * 角色-授权
     */
    String ROLE_AUTH = "role::auth:";

    /**
     * 系统角色-ID
     */
    String ROLE_ID = "role::id:";

    /**
     * 部门-ID
     */
    String DEPT_ID = "dept::id:";

    /**
     * 岗位-ID
     */
    String JOB_ID = "job::id:";

    /**
     * 系统字典-名称
     */
    String DICT_NAME = "dict::name:";

}
