package com.cumulus.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 日志归档文件表 DTO
 *
 * @author : shenjc
 */
@Data
public class LogFileDTO implements Serializable {
    private static final long serialVersionUID = -3562008954458887458L;

    private static final String MANUAL_ARCHIE = "手动归档";
    private static final String AUTO_ARCHIE = "自动归档";

    private static final int MANUAL_ARCHIVE_INT = 1;
    private static final int AUTO_ARCHIVE_INT = 0;

    /**
     * ID
     */
    private Long id;

    /**
     * 文件名 (用户输入的文件名)
     */
    private String fileName;

    /**
     * 归档类型 1 手动归档 0 定期归档
     */
    private String fileType;

    /**
     * 文件路径 (完整路径 nginx 可以直接获取的静态文件路径）
     */
    private String filePath;

    /**
     * 归档时间 手动归档: 时间段 如 1999-xx-xx xx:xx:xx ~ 2000-xx-xx xx:xx:xx 自动归档时间为格式化的 date_time
     */
    private String archiveTime;

    public static String getFileTypeStr(Integer fileType) {
        switch (fileType) {
            case AUTO_ARCHIVE_INT: {
                return AUTO_ARCHIE;
            }
            case MANUAL_ARCHIVE_INT: {
                return MANUAL_ARCHIE;
            }
            default:
        }
        return "";
    }
}
