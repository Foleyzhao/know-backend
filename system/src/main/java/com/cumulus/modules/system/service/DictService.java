package com.cumulus.modules.system.service;

import com.cumulus.modules.system.entity.Dict;
import com.cumulus.modules.system.dto.DictDto;
import com.cumulus.modules.system.dto.DictQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 系统字典服务接口
 */
public interface DictService {

    /**
     * 分页查询字典
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 字典列表
     */
    Map<String, Object> queryAll(DictQueryCriteria criteria, Pageable pageable);

    /**
     * 根据条件查询字典
     *
     * @param dict 查询参数
     * @return 字典列表
     */
    List<DictDto> queryAll(DictQueryCriteria dict);

    /**
     * 创建字典
     *
     * @param resources 字典
     */
    void create(Dict resources);

    /**
     * 编辑字典
     *
     * @param resources 字典
     */
    void update(Dict resources);

    /**
     * 根据字典ID集合删除字典
     *
     * @param ids 字典ID集合
     */
    void delete(Set<Long> ids);

    /**
     * 导出字典列表
     *
     * @param queryAll 待导出的字典列表
     * @param response 响应
     * @throws IOException 异常
     */
    void download(List<DictDto> queryAll, HttpServletResponse response) throws IOException;

}
