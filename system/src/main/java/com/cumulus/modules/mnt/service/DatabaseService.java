package com.cumulus.modules.mnt.service;

import com.cumulus.modules.mnt.entity.Database;
import com.cumulus.modules.mnt.dto.DatabaseDto;
import com.cumulus.modules.mnt.dto.DatabaseQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 数据库服务接口
 */
public interface DatabaseService {

    /**
     * 分页查询数据库
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 数据库列表
     */
    Object queryAll(DatabaseQueryCriteria criteria, Pageable pageable);

    /**
     * 查询数据库
     *
     * @param criteria 查询参数
     * @return 数据库列表
     */
    List<DatabaseDto> queryAll(DatabaseQueryCriteria criteria);

    /**
     * 根据ID查询数据库
     *
     * @param id 数据库ID
     * @return 数据库
     */
    DatabaseDto findById(String id);

    /**
     * 创建数据库
     *
     * @param resources 数据库
     */
    void create(Database resources);

    /**
     * 编辑数据库
     *
     * @param resources 数据库
     */
    void update(Database resources);

    /**
     * 删除数据库
     *
     * @param ids 数据库ID集合
     */
    void delete(Set<String> ids);

    /**
     * 测试连接数据库
     *
     * @param resources 数据库
     * @return 结果
     */
    boolean testConnection(Database resources);

    /**
     * 导出数据
     *
     * @param queryAll 数据库列表
     * @param response 响应
     * @throws IOException 异常
     */
    void download(List<DatabaseDto> queryAll, HttpServletResponse response) throws IOException;
}
