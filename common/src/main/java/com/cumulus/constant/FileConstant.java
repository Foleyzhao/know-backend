package com.cumulus.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件常量类
 *
 * @author : shenjc
 */
public class FileConstant {

    /**
     * 文件夹名
     */
    public static final String TEMPLATE_FILE_FOLDER = "template";
    public static final String LOG_FILE_FOLDER = "logfile";
    public static final String DETAILED_FILE_FOLDER = "detailedFile";
    public static final String SCAN_RESULT_REPORT_FOLDER = "scanResultReport";
    public static final String RISK_REPORT_FOLDER = "riskReport";
    public static final String PLUGINS_FOLDER = "plugins";

    /**
     * excel文件后缀
     */
    public static final String EXCEL_SUFFIX_XLSX = ".xlsx";

    /**
     * excel文件后缀
     */
    public static final String EXCEL_SUFFIX_XLS = ".xls";

    /**
     * zip文件后缀
     */
    public static final String ZIP_SUFFIX_ZIP = ".zip";

    /**
     *
     */
    public static final String RAR_SUFFIX_RAR = ".rar";

    /**
     * excel文件后缀
     */
    public static final String HTML_SUFFIX_HTML = ".html";

    /**
     * pdf文件后缀
     */
    public static final String PDF_SUFFIX = ".pdf";

    /**
     * 文件后缀风格符号
     */
    public static final String FILE_SUFFIX_SEPARATE = ".";

    /**
     * 文件系统分隔符
     */
    public static final String FILE_SEPARATE = "/";

    /**
     * 扫描报告模板文件的替换字符
     */
    public static final String SCAN_RESULT_REPORT_TIME = "<%=REPORT_TIME%>";
    public static final String SCAN_RESULT_IP_RANGE = "<%=IP_RANGE%>";
    public static final String SCAN_RESULT_MONITORING_TYPE = "<%=MONITORING_TYPE%>";
    public static final String SCAN_RESULT_START_TIME = "<%=START_TIME%>";
    public static final String SCAN_RESULT_END_TIME = "<%=END_TIME%>";
    public static final String SCAN_RESULT_TOOK_TIME = "<%=TOOK_TIME%>";
    public static final String SCAN_RESULT_VUL_TABLE = "<%=VUL_TABLE%>";
    public static final String SCAN_RESULT_RESULT_STR = "<%=RESULT_STR%>";
    public static final String SCAN_RESULT_PRODUCT = "<%=PRODUCT%>";
    public static final String SCAN_RESULT_IP_NUM = "<%=IP_NUM%>";
    public static final String SCAN_RESULT_VUL_NUM = "<%=VUL_NUM%>";
    public static final String SCAN_RESULT_PLAN_NAME = "<%=PLAN_NAME%>";
    public static final String SCAN_RESULT_HOST_VUL_NUM = "<%=HOST_VUL_NUM%>";
    public static final String SCAN_RESULT_WEB_VUL_NUM = "<%=WEB_VUL_NUM%>";

    /**
     * 为了防止存在服务器上的文件名为中文导致的一系列问题 这里进行文件名中英文的枚举
     */
    @Getter
    @AllArgsConstructor
    public enum TemplateFile {

        /**
         * 文件导入模板 中英文对应
         */
        ASSET_TYPE_FILE("assetTypeTemplate", "资产类型导入模板", EXCEL_SUFFIX_XLSX),
        ASSET_TAG_FILE("assetTagTemplate", "资产标签导入模板", EXCEL_SUFFIX_XLSX),
        TO_BE_ENTERED_ASSET_FILE("toBeEnteredAssetTemplate", "待录入资产导入模板", EXCEL_SUFFIX_XLSX),
        IP_LIBRARY_FILE("ipLibraryTemplate", "IP导入模板", EXCEL_SUFFIX_XLSX),
        APPLY_ASSET_FILE("applyAssetTemplate", "应用资产导入模板", EXCEL_SUFFIX_XLSX),
        HOST_ASSET_FILE("hostAssetTemplate", "主机资产导入模板", EXCEL_SUFFIX_XLSX),
        VULNERABILITY_FILE("vulnerabilityTemplate", "漏洞导入模板", EXCEL_SUFFIX_XLSX),
        SCAN_RESULT_TEMPLE_FILE("scanResultTempleFile", "漏洞扫描模板", HTML_SUFFIX_HTML),
        RISK_REPORT_TEMPLE_FILE("riskReportTempleFile", "风评报告模板", HTML_SUFFIX_HTML);

        /**
         * 文件英文名 不包括后缀
         */
        private final String fileName;

        /**
         * 文件中文名 不包括后缀
         */
        private final String fileNameChinese;

        /**
         * 文件后缀
         */
        private final String fileSuffix;
    }
}
