package com.cumulus.modules.business.service;

import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import com.cumulus.modules.business.dto.IpLibraryDto;
import com.cumulus.modules.business.dto.IpLibraryQueryCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * ip库服务接口
 *
 * @author zhangxq
 */
public interface IpLibraryService {

    /**
     * 查询ip库
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return ip列表
     */
    Object queryAll(IpLibraryQueryCriteria criteria, Pageable pageable);

    /**
     * 根据ip段添加
     *
     * @param ipLibraryDto ip库传输对象
     */
    void createByIpRange(IpLibraryDto ipLibraryDto);

    /**
     * 批量新增
     *
     * @param file ip列表
     * @return 导入结果
     */
    Object createBatch(MultipartFile file);

    /**
     * 根据id修改
     *
     * @param ipLibraryDto IP库传输对象
     */
    void updateById(IpLibraryDto ipLibraryDto);

    /**
     * 根据id删除
     *
     * @param id
     */
    void removeById(Long id);

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    void removeBatch(Set<Long> ids, Boolean delAll);

    /**
     * 导出
     *
     * @param ids      id列表
     * @param all      是否全部
     * @param response 响应
     */
    void export(Set<Long> ids, Boolean all, HttpServletResponse response);
}
