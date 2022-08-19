package com.cumulus.modules.system.service;

import com.cumulus.modules.system.dto.MenuTreeDto;
import com.cumulus.modules.system.entity.Menu;
import com.cumulus.modules.system.dto.MenuDto;
import com.cumulus.modules.system.dto.MenuQueryCriteria;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 系统菜单服务接口
 *
 * @author shenjc
 */
public interface MenuService {

    /**
     * 查询菜单
     *
     * @param criteria 查询参数
     * @param isQuery  是否是查询
     * @return 菜单列表
     * @throws Exception 异常
     */
    List<MenuDto> queryAll(MenuQueryCriteria criteria, Boolean isQuery) throws Exception;

    /**
     * 查询菜单树结构
     *
     * @return 菜单列表树结构
     */
    List<MenuTreeDto> queryAllTree();

    /**
     * 根据用户id查询菜单
     *
     * @param userId 用户id
     * @return 菜单列表
     */
    List<MenuDto> queryAllByUser(Long userId);

    /**
     * 根据菜单ID查询菜单
     *
     * @param id 菜单ID
     * @return 菜单
     */
    MenuDto findById(long id);

    /**
     * 创建菜单
     *
     * @param resources 菜单
     */
    void create(Menu resources);

    /**
     * 编辑菜单
     *
     * @param resources 菜单
     */
    void update(Menu resources);

    /**
     * 递归获取所有子节点
     *
     * @param menuList 菜单列表
     * @param menuSet  结果采集集合
     * @return 结果采集集合
     */
    Set<Menu> getChildMenus(List<Menu> menuList, Set<Menu> menuSet);

    /**
     * 构建菜单树
     *
     * @param menuDtos 采集集合
     * @return 结果菜单列表
     */
    List<MenuDto> buildTree(List<MenuDto> menuDtos);

    /**
     * 构建菜单树
     *
     * @param menuDtos 菜单列表
     * @return 结果菜单列表
     */
    Object buildMenus(List<MenuDto> menuDtos);

    /**
     * 根据菜单ID查询
     *
     * @param id 菜单ID
     * @return 菜单
     */
    Menu findOne(Long id);

    /**
     * 根据菜单集合删除菜单
     *
     * @param menuSet 采集集合
     */
    void delete(Set<Menu> menuSet);

    /**
     * 导出菜单列表
     *
     * @param queryAll 待导出的数据
     * @param response 响应
     * @throws IOException 异常
     */
    void download(List<MenuDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 根据父菜单ID获取菜单列表
     *
     * @param pid 父菜单ID
     * @return 菜单列表
     */
    List<MenuDto> getMenus(Long pid);

    /**
     * 获取菜单同级菜单与上级菜单
     *
     * @param menuDto 菜单
     * @param objects 结果菜单列表
     * @return 结果菜单列表
     */
    List<MenuDto> getSuperior(MenuDto menuDto, List<Menu> objects);

    /**
     * 根据当前用户获取菜单
     *
     * @param userId 当前用户ID
     * @return 菜单列表
     */
    List<MenuDto> findByUser(Long userId);

    /**
     * 根据当前用户获取菜单
     *
     * @param roleId 当前用户ID
     * @return 菜单列表
     */
    List<MenuDto> queryAllByRole(Long roleId);
}
