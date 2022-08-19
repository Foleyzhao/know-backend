package com.cumulus.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;

/**
 * 加密工具类
 */
public class EncryptUtils {

    /**
     * DES密钥字符串
     */
    private static final String STR_PARAM = "Passw0rd";

    /**
     * 加/解密器
     */
    private static Cipher cipher;

    /**
     * 初始向量
     */
    private static final IvParameterSpec IV = new IvParameterSpec(STR_PARAM.getBytes(StandardCharsets.UTF_8));

    /**
     * 获取DES密钥
     *
     * @param source 待加/解密字符串
     * @return DES密钥
     * @throws Exception 异常
     */
    private static DESKeySpec getDesKeySpec(String source) throws Exception {
        if (null == source || source.length() == 0) {
            return null;
        }
        cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        return new DESKeySpec(STR_PARAM.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 对称加密
     *
     * @param source 待加密字符串
     * @return 加密结果
     * @throws Exception 异常
     */
    public static String desEncrypt(String source) throws Exception {
        DESKeySpec desKeySpec = getDesKeySpec(source);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IV);
        return byte2hex(cipher.doFinal(source.getBytes(StandardCharsets.UTF_8))).toUpperCase();
    }

    /**
     * 对称解密
     *
     * @param source 待解密字符串
     * @return 解密结果
     * @throws Exception 异常
     */
    public static String desDecrypt(String source) throws Exception {
        byte[] src = hex2byte(source.getBytes(StandardCharsets.UTF_8));
        DESKeySpec desKeySpec = getDesKeySpec(source);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IV);
        byte[] retByte = cipher.doFinal(src);
        return new String(retByte);
    }

    /**
     * byte数组转换为16进制值的字符串
     *
     * @param inStr byte数组
     * @return 16进制值的字符串
     */
    private static String byte2hex(byte[] inStr) {
        String stmp;
        StringBuilder out = new StringBuilder(inStr.length * 2);
        for (byte b : inStr) {
            stmp = Integer.toHexString(b & 0xFF);
            if (stmp.length() == 1) {
                // 如果是0至F的单位字符串，则添加0
                out.append("0").append(stmp);
            } else {
                out.append(stmp);
            }
        }
        return out.toString();
    }

    /**
     * 16进制值的字符串转换为byte数组
     *
     * @param b 16进制值的字符串
     * @return byte数组
     */
    private static byte[] hex2byte(byte[] b) {
        int size = 2;
        if ((b.length % size) != 0) {
            throw new IllegalArgumentException("长度不是偶数");
        }
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += size) {
            String item = new String(b, n, 2);
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        return b2;
    }
}
