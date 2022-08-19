package com.cumulus.modules.business.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 导入结果数据传输对象
 *
 * @author zhangxq
 */
@Getter
@Setter
@AllArgsConstructor
public class ImportResultDto<T> {

    /**
     * 初始为0
     */
    public ImportResultDto() {
        this.result = new ImportResult();
        this.result.sum = 0;
        this.result.success = 0;
        this.result.fail = 0;
    }

    /**
     * 导入结果 成功 失败 总数
     */
    private ImportResult result;

    /**
     * 数据列表
     */
    private List<T> objectList;

    /**
     * 导入结果对象
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportResult {

        /**
         * 总条数
         */
        private Integer sum;

        /**
         * 成功条数
         */
        private Integer success;

        /**
         * 失败条数
         */
        private Integer fail;
    }
}
