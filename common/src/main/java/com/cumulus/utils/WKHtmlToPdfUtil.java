package com.cumulus.utils;

import com.cumulus.config.FileProperties;
import com.cumulus.constant.FileConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * wkhtmltopdf工具类,Linux系统中将插件安装在home目录下（/home）
 *
 * @author zhutaotao
 * @date 2020-04-23
 */
@Slf4j
@Component
public class WKHtmlToPdfUtil {

    @Autowired
    private FileProperties fileProperties;

    private static final String EXE_PATH = "/wkhtmltox/bin/wkhtmltopdf.exe";

    /**
     * 将HTML文件内容输出为PDF文件
     *
     * @param htmlFilePath HTML文件路径
     * @param pdfFilePath  PDF文件路径
     */
    public void htmlToPdf(String htmlFilePath, String pdfFilePath) {
        Process process = null;
        try {//注意命令调用路径与安装路径保持一致
            process = Runtime.getRuntime().exec(getCommand(htmlFilePath, pdfFilePath));
            //为了防止waitFor因为流缓存而阻塞，启用两个线程进行流的读取
            new Thread(new ClearBufferThread("Input", process.getInputStream())).start();
            new Thread(new ClearBufferThread("Error", process.getErrorStream())).start();
            process.waitFor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * 获得HTML转PDF的命令语句,注意命令调用路径与安装路径保持一致
     * （一些命令参数可以自行去做修改，或者使用）
     *
     * @param htmlFilePath HTML文件路径
     * @param pdfFilePath  PDF文件路径
     * @return HTML转PDF的命令语句
     */
    private String getCommand(String htmlFilePath, String pdfFilePath) {
        StringBuilder cmd = new StringBuilder();
        cmd.append(fileProperties.getPath().getPath());
        cmd.append(FileConstant.PLUGINS_FOLDER);
        cmd.append(EXE_PATH);
        cmd.append(" ").append(htmlFilePath);
        cmd.append(" ").append(pdfFilePath);
        log.info(cmd.toString());
        return cmd.toString();
    }

}

