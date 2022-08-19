package com.cumulus.modules.license.util;


import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import com.cumulus.utils.EncryptUtils;
import com.cumulus.utils.FileUtils;
import com.cumulus.utils.RsaUtils;
import org.apache.commons.io.IOUtils;

/**
 * 系统授权工具类
 *
 * @author zhangxq
 */
public class LicenseUtils {
    private LicenseUtils() {

    }

    public static final String LICENSE_PATH = "..\\temp\\license.txt";

    public static final String APPLICATION_PATH = "..\\temp\\applictaion.txt";

    /**
     * 授权文件解密
     *
     * @param file 授权文件
     * @return 授权对象json
     */
    public static String decode(File file) {
        try (FileInputStream fis = new FileInputStream(file);) {
            byte[] bytes = Base64.getDecoder().decode(IOUtils.toString(fis, StandardCharsets.UTF_8));
            String str = new String(bytes, StandardCharsets.UTF_8);
            String splitFlag = str.substring(0, 2);
            String[] data = str.split(splitFlag);
            //判断长度
            if (data.length != 4) {
                throw new Exception("授权文件已被修改");
            }
            //RSA解密
            String md5 = RsaUtils.decryptByPublicKey(data[1], data[2]);
            String json = EncryptUtils.desDecrypt(data[3]);
            //获取json md5
            String jsonMd5 = FileUtils.getMd5(json.getBytes(StandardCharsets.UTF_8));
            //比较md5判断是否被修改
            if (!jsonMd5.equals(md5)) {
                throw new Exception("授权文件已被修改");
            }
            return json;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
