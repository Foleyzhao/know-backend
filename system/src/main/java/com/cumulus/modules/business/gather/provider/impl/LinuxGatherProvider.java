package com.cumulus.modules.business.gather.provider.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.entity.AssetConfig;
import com.cumulus.modules.business.entity.LoginGather;
import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.common.service.impl.CmdSendBean;
import com.cumulus.modules.business.gather.common.utils.BusinessCommon;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.request.AccountInfo;
import com.cumulus.modules.business.gather.request.CmdResponseData;
import com.cumulus.modules.business.gather.request.CmdTaskInfo;
import com.cumulus.modules.business.gather.request.ConnectionInfo;
import com.cumulus.modules.business.gather.request.GatherAssetRequest;
import com.cumulus.modules.business.gather.request.GatherException;
import com.cumulus.modules.business.gather.request.TaskRequest;
import com.cumulus.modules.business.gather.request.TaskResponse;
import com.cumulus.modules.business.gather.request.TaskResponseWrapper;
import com.cumulus.modules.business.gather.service.gather.CmdOutputParser;
import com.cumulus.utils.EncryptUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Linux采集类
 *
 * @author zhaoff
 */
@Slf4j
@Component
public class LinuxGatherProvider extends AbstractGatherProvider {

    /**
     * 采集类支持的操作系统分类
     */
    public final static String SUPPORT_OS = "Linux";

    /**
     * 采集业务通用服务
     */
    @Autowired
    private BusinessCommon businessCommon;

    /**
     * 采集指令专用消息发送器
     */
    @Autowired
    private CmdSendBean cmdSendBean;

    /**
     * 命令行解析工具
     */
    @Autowired
    private CmdOutputParser cmdOutputParser;

    @Override
    public String getMainCategory() {
        return GatherConstants.TYPE_HOST.toString();
    }

    @Override
    public String getCategory() {
        return GatherConstants.TYPE_SYS_CENTOS_REDHAT.toString();
    }

    @Override
    void prepare(GatherAssetRequest request) throws GatherException {
        // 获取登录的服务名
        generateLoginService(request);
        // 生成远程连接信息
        generateConnectionInfo(request);
        // 获取采集帐号列表
        generateAssetAccountsList(request);
        // 生成登录帐号信息
        generateLoginAccount(request);
        ConnectionInfo connectionInfo = request.getOption(GatherAssetRequest.EXTRA_CONNECTION_INFO);
        AccountInfo accountInfo = request.getOption(GatherAssetRequest.EXTRA_LOGIN_ACCOUNT);
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setConn(connectionInfo);
        taskRequest.setAccount(accountInfo);
        taskRequest.setType(TaskRequest.TYPE_GATHER);
        taskRequest.setMode(TaskRequest.MODE_ASYNC);
        taskRequest.setTto(TaskRequest.EXEC_TTO);
        request.addOption(GatherAssetRequest.EXTRA_TASK_REQUEST, taskRequest);
        // 默认缓存采集信息
        request.addOption(GatherAssetRequest.EXTRA_CMD_CACHE, true);
    }

    @Override
    Object postProcessOutput(Object retValue) {
        return cmdOutputParser.findCommandOutput((String) retValue);
    }

    @Override
    protected Object inspectOneCommands(GatherAssetRequest request, CmdTaskInfo task)
            throws GatherException {
        TaskRequest taskRequest = request.getOption(GatherAssetRequest.EXTRA_TASK_REQUEST);
        UUID id = UUID.randomUUID();
        taskRequest.setId(id);
        taskRequest.setTask(task);
        taskRequest.setTto(TaskRequest.EXEC_TTO);
        taskRequest.setExpiration(TaskRequest.MQ_TTL);
        if (log.isDebugEnabled()) {
            log.debug("inspect send:" + CommUtils.toJson(taskRequest));
        }
        TaskResponseWrapper taskResponseWrapper = new TaskResponseWrapper();
        Object monitor = businessCommon.addTaskListener(taskResponseWrapper, taskRequest.getId());
        cmdSendBean.sendRequestForAsyncResponse(taskRequest);
        try {
            businessCommon.waitForResponse(taskResponseWrapper, monitor, taskRequest.getTto() + 15);
        } catch (Exception e) {
            throw new GatherException(e.getMessage());
        }
        TaskResponse resp = taskResponseWrapper.getTaskResponse();
        if (resp.getRes() != 0) {
            throw new GatherException(resp.getErrorMsg());
        }
        String output = ((CmdResponseData) taskResponseWrapper.getTaskResponse().getResponseData()).getStdout();
        if (null != output) {
            output = output.trim();
        }
        return output;
    }

    /**
     * 获取资产的帐号列表
     *
     * @param asset 资产
     * @return 资产的帐号列表
     */
    @SuppressWarnings("unchecked")
    protected List<Map<String, String>> generateAccountList(Asset asset) {
        List<Map<String, String>> accounts = new LinkedList<>();
        Map<String, String> account = new HashMap<>();
        AssetConfig config = JSON.parseObject(asset.getConfig(), AssetConfig.class);
        LoginGather loginGather = config.getLogin();
        account.put(GatherAssetRequest.ASSET_ACCOUNT_NAME, loginGather.getAccount());
        try {
            //密码解密
            if (!"".equals(loginGather.getPwd())) {
                loginGather.setPwd(EncryptUtils.desDecrypt(loginGather.getPwd()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        account.put(GatherAssetRequest.ASSET_ACCOUNT_PWD, loginGather.getPwd());
        accounts.add(account);
        return accounts;
    }

    /**
     * 生成采集帐号列表
     *
     * @param request 采集请求对象
     * @throws GatherException 采集异常
     */
    protected void generateAssetAccountsList(GatherAssetRequest request) throws GatherException {
        List<Map<String, String>> accounts = request.getAssetAccounts();
        if (null == accounts) {
            accounts = generateAccountList(request.getAsset());
            if (null == accounts || accounts.isEmpty()) {
                throw new GatherException("No account available");
            }
            request.setAssetAccounts(accounts);
        }
    }

    /**
     * 获取登录的服务名
     *
     * @param request 采集请求对象
     * @throws GatherException 采集异常
     */
    protected void generateLoginService(GatherAssetRequest request) throws GatherException {
        // 默认使用 ssh
        String protocol = request.getAsset().getProtocol();
        request.addOption(GatherAssetRequest.EXTRA_PROTOCOL, protocol);
        Map<String, Object> service = new HashMap<>();
        service.put(GatherAssetRequest.LOGIN_SERVICE_PROTO, protocol);
        service.put(GatherAssetRequest.LOGIN_SERVICE_PORT, request.getAsset().getPort());
        request.setLoginService(service);
    }

    /**
     * 生成远程连接信息
     *
     * @param request 资产采集请求对象
     * @throws GatherException 采集异常
     */
    @SuppressWarnings("unchecked")
    protected void generateConnectionInfo(GatherAssetRequest request) throws GatherException {
        Map<String, Object> service = request.getLoginService();
        ConnectionInfo info = new ConnectionInfo();
        if (null != request.getAsset().getCharset()) {
            info.setEncoding(request.getAsset().getCharset());
        }
        info.setIp(request.getAsset().getIp());
        info.setPort((Integer) service.get(GatherAssetRequest.LOGIN_SERVICE_PORT));
        info.setProto((String) service.get(GatherAssetRequest.LOGIN_SERVICE_PROTO));
        info.setSysType(SUPPORT_OS);
        request.addOption(GatherAssetRequest.EXTRA_CONNECTION_INFO, info);
    }

    /**
     * 生成登录帐号信息
     *
     * @param request 资产采集请求对象
     * @throws GatherException 采集异常
     */
    @SuppressWarnings("unchecked")
    protected void generateLoginAccount(GatherAssetRequest request) throws GatherException {
        List<Map<String, String>> loginAccounts = request.getAssetAccounts();
        AccountInfo accountInfo = new AccountInfo();
        if (null != loginAccounts && loginAccounts.size() > 0) {
            Map<String, String> loginAccount = loginAccounts.get(0);
            accountInfo.setUsername(loginAccount.get(GatherAssetRequest.ASSET_ACCOUNT_NAME));
            accountInfo.setPassword(loginAccount.get(GatherAssetRequest.ASSET_ACCOUNT_PWD));
        }
        request.addOption(GatherAssetRequest.EXTRA_LOGIN_ACCOUNT, accountInfo);
    }

}
