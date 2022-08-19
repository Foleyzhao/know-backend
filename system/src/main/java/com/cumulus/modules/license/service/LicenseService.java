package com.cumulus.modules.license.service;

/**
 * 授权服务接口
 */
public interface LicenseService {

    /**
     * 生成授权申请文件
     */
    void generateApplication();

    /**
     * 获取授权文件
     */
    void getLicense(byte[] bytes);

    /**
     * 验证授权
     */
    void auth() throws Exception;

}
