package com.cumulus.service;

import com.cumulus.entity.LocalStorage;
import com.cumulus.dto.LocalStorageDto;
import com.cumulus.dto.LocalStorageQueryCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 本地存储服务接口
 *
 * @author zhaoff
 */
public interface LocalStorageService {

    /**
     * 分页查询本地存储
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 本地存储列表
     */
    Object queryAll(LocalStorageQueryCriteria criteria, Pageable pageable);

    /**
     * 查询本地存储
     *
     * @param criteria 查询参数
     * @return 本地存储列表
     */
    List<LocalStorageDto> queryAll(LocalStorageQueryCriteria criteria);

    /**
     * 根据ID查询
     *
     * @param id 本地存储ID
     * @return 本地存储对象
     */
    LocalStorageDto findById(Long id);

    /**
     * 上传
     *
     * @param name 文件名称
     * @param file 文件
     * @return 本地存储对象
     */
    LocalStorage create(String name, MultipartFile file);

    /**
     * 编辑本地存储
     *
     * @param resources 本地存储
     */
    void update(LocalStorage resources);

    /**
     * 删除本地存储
     *
     * @param ids 本地存储ID数组
     */
    void deleteAll(Long[] ids);

    /**
     * 导出本地存储列表
     *
     * @param localStorageDtos 待导出的本地存储列表
     * @param response         响应
     * @throws IOException 异常
     */
    void download(List<LocalStorageDto> localStorageDtos, HttpServletResponse response) throws IOException;
}
