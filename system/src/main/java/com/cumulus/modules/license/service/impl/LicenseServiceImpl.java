package com.cumulus.modules.license.service.impl;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.List;
import com.alibaba.fastjson.JSON;
import com.cumulus.modules.license.model.Application;
import com.cumulus.modules.license.model.License;
import com.cumulus.modules.license.model.Network;
import com.cumulus.modules.license.model.SystemInfo;
import com.cumulus.modules.license.service.LicenseService;
import com.cumulus.modules.license.service.SystemInfoService;
import com.cumulus.modules.license.util.LicenseUtils;
import com.cumulus.modules.mnt.websocket.MsgType;
import com.cumulus.modules.mnt.websocket.SocketMsg;
import com.cumulus.modules.mnt.websocket.WebSocketServer;
import com.cumulus.utils.EncryptUtils;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 授权服务实现
 */
@Slf4j
@Service
public class LicenseServiceImpl implements LicenseService {

    /**
     * 系统信息服务
     */
    @Autowired
    private SystemInfoService systemInfoService;

    /**
     * 生成授权申请文件
     */
    @Override
    public void generateApplication() {
        Application application = new Application();
        application.setProductName("ASM");
        application.setSystemInfo(systemInfoService.getSystemInfo());
        File applicationFile = new File(LicenseUtils.APPLICATION_PATH);

        try (FileWriter fw = new FileWriter(applicationFile);
             BufferedWriter bw = new BufferedWriter(fw);) {
            //对象转json
            String src = JSON.toJSONString(application);
            //des加密内容
            String dec = EncryptUtils.desEncrypt(src);
            //写入文件
            bw.write(dec);
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 上传授权文件
     *
     * @param bytes
     */
    @Override
    public void getLicense(byte[] bytes) {
        File file = new File(LicenseUtils.LICENSE_PATH);
        try (FileOutputStream fos = new FileOutputStream(file);) {
            //写入license文件
            fos.write(bytes);
            //验证
            this.auth();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 验证授权
     */
    @Override
    public void auth() throws Exception {
        File file = new File(LicenseUtils.LICENSE_PATH);
        String res = LicenseUtils.decode(file);
        License license = JSON.parseObject(res, License.class);
        List<String> ipList = license.getIpAddress();
        List<String> macList = license.getMacAddress();
        String cpuSerial = license.getCpuSerial();
        String mbSerial = license.getMainBoardSerial();
        SystemInfo systemInfo = systemInfoService.getSystemInfo();
        List<Network> networkList = systemInfo.getNetworkList();
        boolean ipFlag = false;
        boolean macFlag = false;
        for (Network n : networkList) {
            //ipv4
            if (!ipFlag && !StrUtil.isBlankIfStr(n.getIpv4()) && ipList.contains(n.getIpv4())) {
                ipFlag = true;
            }
            //ipv6
            if (!ipFlag && !StrUtil.isBlankIfStr(n.getIpv6()) && ipList.contains(n.getIpv6())) {
                ipFlag = true;
            }
            //mac
            if (!macFlag && !StrUtil.isBlankIfStr(n.getMac()) && macList.contains(n.getMac())) {
                macFlag = true;
            }
            if (ipFlag && macFlag) {
                break;
            }
        }
        if (!ipFlag) {
            throw new Exception("未授权ip");
        }
        if (!macFlag) {
            throw new Exception("未授权mac");
        }
        if (!cpuSerial.equals(systemInfo.getCpuSerial())) {
            throw new Exception("未授权cpu序列号");
        }
        if (!mbSerial.equals(systemInfo.getMainBoardSerial())) {
            throw new Exception("未授权主板序列号");
        }
        long curr = System.currentTimeMillis();
        if (license.getExpireTime() < curr) {
            throw new Exception("授权已过期");
        }
        //websocket推送消息
        SocketMsg socketMsg = new SocketMsg("授权验证失败", MsgType.ERROR);
        WebSocketServer.sendInfo(socketMsg, null);

    }

}
