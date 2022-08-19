package com.cumulus.enums;

import com.cumulus.constant.FileConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 明细清单类型
 *
 * @author : shenjc
 */
@Getter
@AllArgsConstructor
public enum DetailedFileTypeEnum {


    /**
     * 明细清单类型
     */
    SCAN_RESULT(1, "漏洞扫描结果", FileConstant.DETAILED_FILE_FOLDER + FileConstant.FILE_SEPARATE + "scanResult"),

    /**
     * ip库
     */
    IPLIBRARY_RESULT(2, "IP库导出", FileConstant.DETAILED_FILE_FOLDER + FileConstant.FILE_SEPARATE + "ipLibrary"),

    /**
     * 漏洞清单
     */
    VULNERABILITY_LIST(3, "漏洞清单", FileConstant.DETAILED_FILE_FOLDER + FileConstant.FILE_SEPARATE + "vulnerability"),

    /**
     * 资产视图
     */
    VIEW_LIST(4, "资产视图", FileConstant.DETAILED_FILE_FOLDER + FileConstant.FILE_SEPARATE + "view");


    /**
     * 类型
     */
    private final int type;

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 文件夹
     */
    private final String folder;

    public static final String DETAILED_FILE_FOLDER = "detailedFile";

    /**
     * 根据id 获取漏扫器名称
     */
    public static DetailedFileTypeEnum getByType(Integer type) {
        if (type == null) {
            return null;
        }
        for (DetailedFileTypeEnum value : DetailedFileTypeEnum.values()) {
            if (value.getType() == type) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据id 获取漏扫器名称
     */
    public static String getNameByType(Integer type) {
        if (type == null) {
            return null;
        }
        for (DetailedFileTypeEnum value : DetailedFileTypeEnum.values()) {
            if (value.getType() == type) {
                return value.getName();
            }
        }
        return null;
    }
}
