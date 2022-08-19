package com.cumulus.utils;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelUtil;
import com.cumulus.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.POIDocument;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件工具类
 *
 * @author shenjc
 */
@Slf4j
public class FileUtils extends cn.hutool.core.io.FileUtil {

    /**
     * 临时文件的存放路径
     */
    private static File tempDir = null;

    /**
     * 系统临时目录
     */
    public static final String SYS_TEM_DIR = System.getProperty("java.io.tmpdir") + File.separator;

    /**
     * 定义GB的计算常量
     */
    private static final int GB = 1024 * 1024 * 1024;

    /**
     * 定义MB的计算常量
     */
    private static final int MB = 1024 * 1024;

    /**
     * 定义KB的计算常量
     */
    private static final int KB = 1024;

    /**
     * 缓存大小
     */
    private static final int BUFFER_SIZE = 2 * 1024;

    /**
     * 小数格式
     */
    private static final DecimalFormat DF = new DecimalFormat("0.00");

    /**
     * 文件类型-图片
     */
    public static final String IMAGE = "图片";

    /**
     * 文件类型-文档
     */
    public static final String TXT = "文档";

    /**
     * 文件类型-音乐
     */
    public static final String MUSIC = "音乐";

    /**
     * 文件类型-视频
     */
    public static final String VIDEO = "视频";

    /**
     * 文件类型-其他
     */
    public static final String OTHER = "其他";

    /**
     * 主机资产
     */
    public static final String MAINFRAME_ASSET = "主机资产";

    /**
     * 默认文件名称
     */
    public static final String DEFAULT_FILE_NAME = "file";

    /**
     * 默认文件下载的类型
     */
    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    /**
     * 后缀是xlsx的文件下载类型
     */
    public static final String CONTENT_TYPE_SUFFIX_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * 后缀是xlsx的文件下载类型
     */
    public static final String DEFAULT_SHEET_NAME = "sheet";

    /**
     * 直接查看文件inline
     */
    public static final String DISPOSITION_INLINE = "inline";

    /**
     * 一律下载
     */
    public static final String DISPOSITION_ATTACHMENT = "attachment";

    /**
     * cmdPath
     */
    public static final String RAR_CMD = "cmd /c %s X -o+ %s %s";

    /**
     * MultipartFile类型文件转File类型文件
     *
     * @param multipartFile MultipartFile类型文件
     * @return File类型文件
     */
    public static File toFile(MultipartFile multipartFile) {
        // 获取文件名
        String fileName = multipartFile.getOriginalFilename();
        // 获取文件后缀
        String prefix = "." + getExtensionName(fileName);
        File file = null;
        try {
            // 用uuid作为文件名，防止生成的临时文件重复
            file = new File(SYS_TEM_DIR + IdUtil.simpleUUID() + prefix);
            // MultipartFile to File
            multipartFile.transferTo(file);
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
        return file;
    }

    /**
     * 创建一个临时文件
     *
     * @param prefix 文件名前缀
     * @param suffix 文件名后缀
     * @return 临时文件
     * @throws IOException 异常
     */
    public static File createTempFile(String prefix, String suffix) throws IOException {
        return File.createTempFile(prefix, suffix, getSharedTempDir());
    }

    /**
     * 返回共享的临时路径。该临时路径在共享路径下（/var/shtermdata/share/tmp），集群中不同节点都能访问到。
     *
     * @return 共享的临时路径。
     */
    public static synchronized File getSharedTempDir() {
        if (tempDir == null) {
            tempDir = new File(System.getProperty("java.io.tmpdir"), "share/tmp");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
        }
        return tempDir;
    }

    /**
     * 获取文件扩展名（不带.）
     *
     * @param filename 文件名
     * @return 文件扩展名
     */
    public static String getExtensionName(String filename) {
        if ((null != filename) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    /**
     * 获取不带扩展名的文件名
     *
     * @param filename 文件名
     * @return 不带扩展名的文件名
     */
    public static String getFileNameNoEx(String filename) {
        if ((null != filename) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    /**
     * 文件大小转换
     *
     * @param size 文件大小
     * @return 带单位的文件大小字符串
     */
    public static String getSize(long size) {
        String resultSize;
        if (size / GB >= 1) {
            // 如果当前Byte的值大于等于1GB
            resultSize = DF.format(size / (float) GB) + "GB   ";
        } else if (size / MB >= 1) {
            // 如果当前Byte的值大于等于1MB
            resultSize = DF.format(size / (float) MB) + "MB   ";
        } else if (size / KB >= 1) {
            // 如果当前Byte的值大于等于1KB
            resultSize = DF.format(size / (float) KB) + "KB   ";
        } else {
            resultSize = size + "B   ";
        }
        return resultSize;
    }

    /**
     * 输入流写文件
     *
     * @param ins  输入流
     * @param name 文件
     * @return 文件
     */
    static File inputStreamToFile(InputStream ins, String name) {
        File file = new File(SYS_TEM_DIR + name);
        if (file.exists()) {
            return file;
        }
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            int bytesRead;
            int len = 8192;
            byte[] buffer = new byte[len];
            while ((bytesRead = ins.read(buffer, 0, len)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseUtils.close(os);
            CloseUtils.close(ins);
        }
        return file;
    }

    /**
     * 解析文件的上传路径
     *
     * @param file     文件
     * @param filePath 文件上传路径
     * @return 文件上传路径
     */
    public static File upload(MultipartFile file, String filePath) {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmssS");
        String name = getFileNameNoEx(file.getOriginalFilename());
        String suffix = getExtensionName(file.getOriginalFilename());
        String nowStr = "-" + format.format(date);
        try {
            String fileName = name + nowStr + "." + suffix;
            String path = filePath + fileName;
            // getCanonicalFile 可解析正确各种路径
            File dest = new File(path).getCanonicalFile();
            // 检测是否存在目录
            if (!dest.getParentFile().exists()) {
                if (!dest.getParentFile().mkdirs()) {
                    if (log.isInfoEnabled()) {
                        log.info("was not successful.");
                    }
                }
            }
            // 文件写入
            file.transferTo(dest);
            return dest;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * 导出excel
     *
     * @param list     excel数据
     * @param response 响应
     * @throws IOException 读写异常
     */
    public static void downloadExcel(List<Map<String, Object>> list, HttpServletResponse response, String fileName) throws IOException {
        if (StringUtils.isBlank(fileName)) {
            fileName = DEFAULT_FILE_NAME;
        }
        String tempPath = SYS_TEM_DIR + IdUtil.fastSimpleUUID() + ".xlsx";
        File file = new File(tempPath);
        BigExcelWriter writer = ExcelUtil.getBigWriter(file);
        // 一次性写出内容，使用默认样式，强制输出标题
        writer.write(list, true);
        SXSSFSheet sheet = (SXSSFSheet) writer.getSheet();
        // 上面需要强转SXSSFSheet，否则没有trackAllColumnsForAutoSizing方法
        sheet.trackAllColumnsForAutoSizing();
        // 列宽自适应
        writer.autoSizeColumnAll();
        // response为HttpServletResponse对象
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        // 下载对话框的文件名 可以为中文 但是部分浏览器可能会乱码
        String fileNameNet = URLEncoder.encode(fileName + ".xlsx", "utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileNameNet);
        ServletOutputStream out = response.getOutputStream();
        // 终止后删除临时文件
        file.deleteOnExit();
        writer.flush(out, true);
        // 关闭输出Servlet流
        IoUtil.close(out);
    }

    /**
     * 导出excel
     *
     * @param list     excel数据
     * @param response 响应
     * @throws IOException 读写异常
     */
    public static void downloadExcel(List<Map<String, Object>> list, HttpServletResponse response) throws IOException {
        downloadExcel(list, response, DEFAULT_FILE_NAME);
    }

    /**
     * 获取上传文件类型
     *
     * @param type 实际文件类型
     * @return 上传文件类型
     */
    public static String getFileType(String type) {
        String documents = "txt doc pdf ppt pps xlsx xls docx";
        String music = "mp3 wav wma mpa ram ra aac aif m4a";
        String video = "avi mpg mpe mpeg asf wmv mov qt rm mp4 flv m4v webm ogv ogg";
        String image = "bmp dib pcp dif wmf gif jpg tif eps psd cdr iff tga pcd mpt png jpeg";
        if (image.contains(type)) {
            return IMAGE;
        } else if (documents.contains(type)) {
            return TXT;
        } else if (music.contains(type)) {
            return MUSIC;
        } else if (video.contains(type)) {
            return VIDEO;
        } else {
            return OTHER;
        }
    }

    /**
     * 文件大小校验
     *
     * @param maxSize 文件最大大小
     * @param size    文件大小
     */
    public static void checkSize(long maxSize, long size) {
        // 1M
        int len = 1024 * 1024;
        if (size > (maxSize * len)) {
            throw new BadRequestException("文件超出规定大小");
        }
    }

    /**
     * 判断两个文件是否相同
     *
     * @param file1 文件1
     * @param file2 文件2
     * @return 是否相同
     */
    public static boolean check(File file1, File file2) {
        String img1Md5 = getMd5(file1);
        String img2Md5 = getMd5(file2);
        if (null != img1Md5) {
            return img1Md5.equals(img2Md5);
        }
        return false;
    }

    /**
     * 判断两个文件是否相同
     *
     * @param file1Md5 文件1MD5值
     * @param file2Md5 文件2MD5值
     * @return 是否相同
     */
    public static boolean check(String file1Md5, String file2Md5) {
        return file1Md5.equals(file2Md5);
    }

    /**
     * 文件转字节数组
     *
     * @param file 文件
     * @return 字节数组
     */
    private static byte[] getByte(File file) {
        // 得到文件长度
        byte[] b = new byte[(int) file.length()];
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            try {
                int result = in.read(b);
                if (log.isInfoEnabled()) {
                    log.info(String.valueOf(result));
                }
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            return null;
        } finally {
            CloseUtils.close(in);
        }
        return b;
    }

    /**
     * 获取字节数组的MD5值
     *
     * @param bytes 字节数组
     * @return MD5值
     */
    public static String getMd5(byte[] bytes) {
        // 16进制字符
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(bytes);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char[] str = new char[j * 2];
            int k = 0;
            // 移位 输出字符串
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * 下载文件 默认不删除
     *
     * @param request  请求
     * @param response 响应
     * @param file     文件
     */
    public static void downloadFile(HttpServletRequest request, HttpServletResponse response, File file) {
        downloadFile(request, response, file, false);
    }

    /**
     * 下载文件
     *
     * @param request      请求
     * @param response     响应
     * @param file         文件
     * @param deleteOnExit 下载后是否删除
     */
    public static void downloadFile(HttpServletRequest request, HttpServletResponse response, File file,
                                    boolean deleteOnExit) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new BadRequestException("文件不存在");
        }
        response.setCharacterEncoding(request.getCharacterEncoding());
        response.setContentType("application/octet-stream");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
            IOUtils.copy(fis, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                    if (deleteOnExit) {
                        file.deleteOnExit();
                    }
                } catch (IOException e) {
                    if (log.isErrorEnabled()) {
                        log.error(e.getMessage(), e);
                    }

                }
            }
        }
    }

    /**
     * 根据路径下载文件 下载的文件名就是实际的文件名
     *
     * @param file     文件
     * @param response 请求响应
     * @param request  请求
     */
    public static void downloadFile(File file, HttpServletResponse response, HttpServletRequest request) {
        downloadFile(file, response, request, file.getName());
    }

    public static void downloadFile(File file, HttpServletResponse response, HttpServletRequest request, String fileName) {
        downloadFile(file, response, request, fileName, false);
    }

    /**
     * 根据路径下载文件
     *
     * @param file     文件
     * @param response 请求响应
     * @param request  请求
     * @param fileName 返回的文件名 可以和实际的文件名不同
     */
    public static void downloadFile(File file, HttpServletResponse response, HttpServletRequest request, String fileName, boolean show) {
        if (file.exists() && file.isFile()) {
            try {
                downloadFile(new FileInputStream(file), response, request, fileName, Files.probeContentType(file.toPath()), show);
                return;
            } catch (IOException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Method: downloadFile: create new FileInputStream fail, filePath:" + file.getPath());
                }
                throw new BadRequestException("文件下载失败,文件无法无法转换为流");
            }
        }
        if (log.isWarnEnabled()) {
            log.warn("Method: downloadFile: file not exists, filePath:" + file.getPath());
        }
        throw new BadRequestException("文件下载失败,文件不存在");
    }


    public static void downloadFile(InputStream inputStream, HttpServletResponse response, HttpServletRequest request, String fileName, String contentType) {
        downloadFile(inputStream, response, request, fileName, contentType, false);
    }

    /**
     * 使用文件输入流下载文件
     * 文件名的问题:使用 utf-8 当中文文字超过17个时，IE6 无法下载文件 原因可能是IE在处理 Response Header 的时候，对header的长度限制在150字节左右
     * 在确保附件文件名都是简 体中文字的情况下 使用
     * response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes("gb2312"), "ISO8859-1"));
     *
     * @param inputStream 文件输入流
     * @param response    请求响应
     * @param request     请求
     * @param fileName    返回的文件名 可以和实际的文件名不同
     * @param contentType 返回头的文件类型
     * @param show        是否直接展示
     */
    public static void downloadFile(InputStream inputStream, HttpServletResponse response, HttpServletRequest request, String fileName, String contentType, boolean show) {
        if (inputStream == null) {
            throw new BadRequestException("文件不存在");
        }
        //设置 contentType 空为默认
        if (StringUtils.isNotBlank(contentType)) {
            response.setContentType(contentType);
        } else {
            response.setContentType(DEFAULT_CONTENT_TYPE);
        }
        //设置编码格式
        response.setCharacterEncoding(request.getCharacterEncoding());
        try {
            //1、下载方式：attachment:无论什么文件都下载，inline:像txt，jpg 等文件不进行下载而是再页面中显示 假如使用的是默认的contentType 则无论什么文件都是attachment形式下载 2、filename：设置下载文件的名字
            if (show) {
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            } else {
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            }
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            if (log.isWarnEnabled()) {
                log.warn("Method: downloadFile: response stream fail, fileName:" + fileName);
            }
            throw new BadRequestException("下载文件失败, 文件源出现问题");
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Method: downloadFile: fileInputStream close fail, fileName:" + fileName);
                }
            }
        }
    }

    /**
     * 使用文件输入流下载文件
     * 文件名的问题:使用 utf-8 当中文文字超过17个时，IE6 无法下载文件 原因可能是IE在处理 Response Header 的时候，对header的长度限制在150字节左右
     * 在确保附件文件名都是简 体中文字的情况下 使用
     * response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes("gb2312"), "ISO8859-1"));
     *
     * @param poiDocument poi的抽象类
     * @param response    请求响应
     * @param request     请求
     * @param fileName    返回的文件名 可以和实际的文件名不同
     * @param contentType 返回头的文件类型
     */
    public static void downloadFile(POIDocument poiDocument, HttpServletResponse response, HttpServletRequest request, String fileName, String contentType) {
        if (poiDocument == null) {
            throw new BadRequestException("文件不存在");
        }
        //设置 contentType 空为默认
        if (StringUtils.isNotBlank(contentType)) {
            response.setContentType(contentType);
        } else {
            response.setContentType(DEFAULT_CONTENT_TYPE);
        }
        //设置编码格式
        response.setCharacterEncoding(request.getCharacterEncoding());
        try {
            //1、下载方式：attachment:无论什么文件都下载，inline:像txt，jpg 等文件不进行下载而是再页面中显示 假如使用的是默认的contentType 则无论什么文件都是attachment形式下载 2、filename：设置下载文件的名字
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            poiDocument.write(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            if (log.isWarnEnabled()) {
                log.warn("Method: downloadFile: response stream fail, fileName:" + fileName);
            }
            throw new BadRequestException("下载文件失败, poiDocument生成出现问题");
        } finally {
            try {
                poiDocument.close();
            } catch (IOException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Method: downloadFile: fileInputStream close fail, fileName:" + fileName);
                }
            }
        }
    }

    /**
     * 获取文件MD5值
     *
     * @param file 文件
     * @return MD5值
     */
    public static String getMd5(File file) {
        return getMd5(getByte(file));
    }

    /**
     * 生成excel
     *
     * @param list     excel数据
     * @param filePath 文件路径
     * @return 返回生成的文件路径
     */
    public static String saveExcelFile(List<Map<String, Object>> list, String filePath) {
        if (StringUtils.isBlank(filePath)) {
            if (log.isInfoEnabled()) {
                log.info("filePath is Blank");
            }
            throw new BadRequestException("文件路径不存在");
        }
        File file = new File(filePath);
        if (file.exists()) {
            if (log.isInfoEnabled()) {
                log.info("file already exists filePath :{}", filePath);
            }
            throw new BadRequestException("文件已存在");
        }
        BigExcelWriter writer = ExcelUtil.getBigWriter(file);
        // 一次性写出内容，使用默认样式，强制输出标题
        writer.write(list, true);
        SXSSFSheet sheet = (SXSSFSheet) writer.getSheet();
        // 上面需要强转SXSSFSheet，否则没有trackAllColumnsForAutoSizing方法
        sheet.trackAllColumnsForAutoSizing();
        // 列宽自适应
        writer.autoSizeColumnAll();
        writer.close();
        return file.getPath();
    }

    /**
     * 导出到excel
     *
     * @param workbook
     * @param sheetNum   sheet下标
     * @param sheetTitle sheet名称
     * @param headers    列头
     * @param result     数据
     * @param merge      是否开启合并单元格
     * @return
     */
    public static Boolean exportExcel(HSSFWorkbook workbook, int sheetNum,
                                      String sheetTitle, String[] headers, List<List<String>> result, boolean merge) {
        Boolean res = false;
        try {
            // 生成一个表格
            HSSFSheet sheet = workbook.createSheet();
            workbook.setSheetName(sheetNum, sheetTitle);
            // 设置表格默认列宽度为20个字节
            sheet.setDefaultColumnWidth((short) 20);
            // 生成一个样式
            CellStyle style = workbook.createCellStyle();
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            // 指定当单元格内容显示不下时自动换行
            style.setWrapText(true);
            // 产生表格标题行
            HSSFRow row = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                HSSFCell cell = row.createCell((short) i);
                style.setVerticalAlignment(VerticalAlignment.CENTER);
                cell.setCellStyle(style);
                HSSFRichTextString text = new HSSFRichTextString(headers[i]);
                cell.setCellValue(text.toString());
            }
            // 遍历集合数据，产生数据行
            if (result != null) {
                int index = 1;
                for (List<String> m : result) {
                    row = sheet.createRow(index);
                    int cellIndex = 0;
                    for (Object str : m) {
                        HSSFCell cell = row.createCell((short) cellIndex);
                        if (null != str) {
                            cell.setCellValue(str.toString());
                        }
                        // 垂直居中
                        style.setVerticalAlignment(VerticalAlignment.CENTER);
                        cell.setCellStyle(style);
                        cellIndex++;
                    }
                    index++;
                }
            }
            //是否开启合并单元格
            if (merge) {
                int lastRowNum = sheet.getLastRowNum();
                if (MAINFRAME_ASSET.equals(sheet.getSheetName())) {
                    lastRowNum = 0;
                }
                // 合并相同列中的数据
                // 处理相同的数据合并单元格
                //获取最后一行行标，比行数小  满足合并的条件，2是数据行的开始，0 1 行为表头
                if (lastRowNum > 0) {
                    HSSFRow row_1 = sheet.getRow(1);
                    HSSFCell cell_1 = row_1.getCell(0);
                    String departname = cell_1.getStringCellValue();
                    for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                        //除第一个外，循环将内容相同的单元格设为""，这里体现出为什么原数据要有序！
                        HSSFRow rows = sheet.getRow(i);
                        //此处表示对单元格进行内容相同合并处理，我这里获取的是每行的第2列进行对比，要多列对比的，这里自行增加
                        HSSFCell cells_1 = rows.getCell(0);
                        //这里值相同则设置位空，方便之后的合并
                        if (departname.equals(cells_1.getStringCellValue())) {
                            cells_1.setCellValue("");
                            // 垂直居中
                            style.setVerticalAlignment(VerticalAlignment.CENTER);
                            cells_1.setCellStyle(style);
                        } else {
                            departname = cells_1.getStringCellValue();
                        }
                    }
                }
                int sk;
                //将为空的单元格与之前不为空的合并
                if (sheet.getLastRowNum() > 1) {
                    sk = 1;
                    for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                        HSSFRow rows = sheet.getRow(i);
                        HSSFCell cell_0 = rows.getCell(0);
                        //如果为空但还没对比到最后一行，继续循环
                        if (cell_0.getStringCellValue() == "") {
                            if (i == sheet.getLastRowNum()) {
                                //如果已经对比到最后一行，开始合并
                                sheet.addMergedRegion(new CellRangeAddress(sk, i, 0, 0));
                                // 垂直居中
                                style.setVerticalAlignment(VerticalAlignment.CENTER);
                                cell_0.setCellStyle(style);
                            }
                        } else {
                            //不为空且i-1不为sk则合并
                            if (sk != i - 1) {
                                //起始行号，终止行号， 起始列号，终止列号
                                sheet.addMergedRegion(new CellRangeAddress(sk, i - 1, 0, 0));
                                // 垂直居中
                                style.setVerticalAlignment(VerticalAlignment.CENTER);
                                cell_0.setCellStyle(style);
                            }
                            sk = i;
                        }
                    }
                }
                res = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * 保存文件
     *
     * @param fileInput 文件输入
     * @param path      文件路径
     * @throws IOException 报错信息
     */
    public static void saveFile(POIDocument fileInput, String path) throws IOException {
        try {
            File file = new File(path);
            if (file.exists()) {
                throw new IOException("文件已存在");
            }
            if (!file.createNewFile()) {
                throw new IOException("文件生成失败");
            }
            fileInput.write(file);
        } catch (IOException e) {
            if (log.isInfoEnabled()) {
                log.info("FileUtils saveFile Error");
            }
            throw e;
        } finally {
            try {
                fileInput.close();
            } catch (IOException e) {
                if (log.isInfoEnabled()) {
                    log.info("FileUtils fileInput close Error");
                }
            }
        }

    }

    /**
     * 根据源文件 和替换map 生成目标文件
     *
     * @param sourceFilePath 源文件路径
     * @param targetFilePath 目标文件路径
     * @param replaceMap     替换Map
     * @return 返回成功与否
     */
    public static boolean generateReplaceFile(String sourceFilePath, String targetFilePath, Map<String, String> replaceMap) {
        File sourceFile = new File(sourceFilePath);
        if (!sourceFile.exists() || !sourceFile.isFile()) {
            return false;
        }
        File targetFile = new File(targetFilePath);
        if (targetFile.exists()) {
            return false;
        }
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        CharArrayWriter charArrayWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileInputStream = new FileInputStream(sourceFile);
            inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(inputStreamReader);
            charArrayWriter = new CharArrayWriter();
            String string;
            while ((string = bufferedReader.readLine()) != null) {
                //判断是否包含目标字符，包含则替换
                for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
                    if (string.contains(entry.getKey()) && entry.getValue() != null) {
                        string = string.replace(entry.getKey(), entry.getValue());
                    }
                }
                charArrayWriter.write(string);
                charArrayWriter.write("\r\n");
            }
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8));
            charArrayWriter.writeTo(bufferedWriter);
        } catch (IOException e) {
            if (log.isInfoEnabled()) {
                log.info("generateReplaceFile close error");
            }
            return false;
        } finally {
            saveClose(fileInputStream, inputStreamReader, bufferedReader, bufferedWriter);
            if (null != charArrayWriter) {
                charArrayWriter.close();
            }

        }
        return true;
    }

    /**
     * 解决 closeable 关闭产生的异常
     */
    public static void saveClose(Closeable... closeableList) {
        for (Closeable closeable : closeableList) {
            try {
                if (null != closeable) {
                    closeable.close();
                }
            } catch (IOException exception) {
                if (log.isInfoEnabled()) {
                    log.info("closeable close error");
                }
            }
        }
    }


    public static void zipFiles(Set<File> srcFiles, String targetPath) throws FileNotFoundException {
        zipFiles(srcFiles, targetPath, new HashMap<>(0));
    }

    /**
     * 压缩文件到指定路径
     *
     * @param srcFiles   文件列表
     * @param targetPath 生成压缩文件路径
     */
    public static void zipFiles(Set<File> srcFiles, String targetPath, Map<String, String> nameMap) throws FileNotFoundException {
        File targetFile = new File(targetPath);
        OutputStream outputStream = new FileOutputStream(targetFile);
        long start = System.currentTimeMillis();
        ZipOutputStream zipOutputStream = null;
        FileInputStream fileInputStream = null;
        boolean hasFile = false;
        try {
            zipOutputStream = new ZipOutputStream(outputStream);
            for (File srcFile : srcFiles) {
                if (srcFile.exists()) {
                    hasFile = true;
                    byte[] buffer = new byte[BUFFER_SIZE];
                    zipOutputStream.putNextEntry(new ZipEntry(nameMap.get(srcFile.getName())));
                    int len;
                    fileInputStream = new FileInputStream(srcFile);
                    while ((len = fileInputStream.read(buffer)) != -1) {
                        zipOutputStream.write(buffer, 0, len);
                    }
                    zipOutputStream.closeEntry();
                    fileInputStream.close();
                }
            }
            if (!hasFile) {
                throw new RuntimeException("zip error no file");
            }
            long end = System.currentTimeMillis();
            if (log.isInfoEnabled()) {
                log.info("压缩完成，耗时：{} ms", +(end - start));
            }
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils", e);
        } finally {
            saveClose(zipOutputStream, outputStream, fileInputStream);
        }
    }


    /**
     * 读取文件信息
     *
     * @param path 路径
     * @return 集合
     * @throws Exception 异常信息
     */
    public List<String> getFileContext(String path) throws Exception {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(path);
        assert inputStream != null;
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        List<String> list = new ArrayList<String>();
        String str = null;
        while ((str = bufferedReader.readLine()) != null) {
            if (str.trim().length() > 2) {
                String replace = str.trim();
                list.add(replace);
            }
        }
        return list;
    }
}
