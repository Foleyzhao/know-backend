package com.cumulus.modules.system.service;

import com.cumulus.enums.DetailedFileTypeEnum;
import com.cumulus.modules.system.entity.DetailedFile;
import org.apache.poi.POIDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.List;

/**
 * 明细清单服务接口
 *
 * @author : shenjc
 */
public interface DetailedFileService {


    /**
     * 根据参数查询明细清单
     *
     * @param pageable 分页参数
     * @param params   查询参数
     * @return 明细清单分页
     */
    Page<DetailedFile> page(Pageable pageable, MultiValueMap<String, String> params);

    /**
     * 单个删除明细清单
     *
     * @param id 明细清单id
     */
    void deleteById(Long id);

    /**
     * 批量删除明细清单
     *
     * @param list id列表
     * @return 请求响应
     */
    String deleteBatches(List<Long> list);

    /**
     * 根据id 获取明细清单的路径
     *
     * @param id id主键
     * @return 返回路径
     */
    String getDetailedFilePathById(Long id);

    /**
     * 保存明细清单文件
     *
     * @param typeEnum   文件类型
     * @param name       文件名
     * @param fileSuffix 后缀
     * @return 返回保存的对象
     */
    DetailedFile saveDetailedFile(DetailedFileTypeEnum typeEnum, String name, String fileSuffix);

    /**
     * 保存明细清单文件（excel类型）
     *
     * @param typeEnum   文件类型
     * @param name       文件名
     * @param fileSuffix 后缀
     * @param workbook   excel文件
     * @throws IOException 抛出异常
     */
    void saveDetailedFile(DetailedFileTypeEnum typeEnum, String name, String fileSuffix, POIDocument workbook) throws IOException;

    /**
     * 更新明细清单状态为完成
     *
     * @param id 明细清单id
     */
    void updateDetailedFileDone(Long id);
}
