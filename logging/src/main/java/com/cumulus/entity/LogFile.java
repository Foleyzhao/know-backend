package com.cumulus.entity;

import com.cumulus.base.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 日志归档实体对象
 *
 * @author : shenjc
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sys_log_file")
public class LogFile extends BaseEntity {

    private static final long serialVersionUID = -560700105342006760L;

    /**
     * 默认的日志归档任务在 quartz_job 表的 id
     */
    public static final long QUARTZ_JOB_ID = 1L;

    /**
     * 日志归档任务的类型 1 手动归档 0 定期归档
     */
    public static final int FILE_TYPE_AUTO = 0;
    public static final int FILE_TYPE_HANDLE = 1;

    /**
     * 手动归档时归档时间 中间的分隔符
     */
    public static final String HANDLE_TYPE_SEPARATE = " - ";

    /**
     * 生成的日志归档文件的前缀
     */
    public static final String LOG_FILE_PREFIX = "LOG";

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 文件名 (用户输入的文件名)
     */
    private String fileName;

    /**
     * 归档类型 1 手动归档 0 定期归档
     */
    private Integer fileType;

    /**
     * 文件路径 (完整路径 nginx 可以直接获取的静态文件路径）
     */
    private String filePath;

    /**
     * 归档时间 手动归档: 时间段 如 1999-xx-xx xx:xx:xx ~ 2000-xx-xx xx:xx:xx 自动归档时间为格式化的 date_time
     */
    private String archiveTime;
}
