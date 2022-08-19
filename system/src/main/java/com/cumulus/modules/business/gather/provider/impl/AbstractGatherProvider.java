package com.cumulus.modules.business.gather.provider.impl;

import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.model.GatherDetail;
import com.cumulus.modules.business.gather.model.GatherDetailCommand;
import com.cumulus.modules.business.gather.provider.GatherProvider;
import com.cumulus.modules.business.gather.request.CmdTaskInfo;
import com.cumulus.modules.business.gather.request.GatherAssetRequest;
import com.cumulus.modules.business.gather.request.GatherCmdRequest;
import com.cumulus.modules.business.gather.request.GatherException;
import com.cumulus.modules.business.gather.request.TaskInfo;
import com.cumulus.modules.business.gather.service.gather.CmdOutputParser;
import com.cumulus.modules.business.gather.service.gather.GatherCenter;
import com.cumulus.modules.business.gather.service.gather.GatherDataParser;
import com.cumulus.modules.business.gather.service.gather.GatherTaskManager;
import com.cumulus.modules.business.service.AssetService;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 采集抽象类
 *
 * @author zhaoff
 */
@Slf4j
public abstract class AbstractGatherProvider implements GatherProvider {

    /**
     * (采集)指标维护类
     */
    @Autowired
    protected GatherCenter gatherCenter;

    /**
     * 采集原始输出解析器
     */
    @Autowired
    protected CmdOutputParser cmdOutputParser;

    /**
     * 采集数据解析器
     */
    @Autowired
    protected GatherDataParser dataParser;

    /**
     * 资产服务接口
     */
    @Autowired
    private AssetService assetService;

    /**
     * 采集管理器
     */
    @Autowired
    @Lazy
    private GatherTaskManager gatherTaskManager;

    /**
     * 采集前的初始化操作
     *
     * @param request 资产采集请求对象
     * @throws GatherException 采集异常
     */
    abstract void prepare(GatherAssetRequest request) throws GatherException;

    @Override
    public void execute(GatherAssetRequest request) throws GatherException {
        prepare(request);
        boolean needCache = null != (request.getOption(GatherAssetRequest.EXTRA_CMD_CACHE));
        String error = "";
        Long planId = null;
        if (null != request.getAssetLog()) {
            planId = request.getAssetLog().getPlanId();
        }
        for (GatherCmdRequest cmd : request.getCmdRequest()) {
            try {
                if (null != planId && !gatherTaskManager.isRunning(planId, false)) {
                    if (log.isInfoEnabled()) {
                        log.info(String.format("Task cancel for asset %s.", request.getAsset().getName()));
                    }
                    break;
                }
                if (StringUtils.isBlank(error)) {
                    executeCmd(request, cmd.getKey(), needCache);
                    if (null != cmd.getOutputs() && null != cmd.getOutputs().get("output")) {
                        if (cmd.getOutputs().get("output").toString().contains("not found.")
                                || cmd.getOutputs().get("output").toString().contains("command not found")
                                || cmd.getOutputs().get("output").toString().contains("Unrecognized command found")) {
                            cmd.setSuccess(false);
                            cmd.setErrorMsg("Unrecognized command");
                        }
                    } else {
                        cmd.setSuccess(true);
                    }
                } else {
                    cmd.setSuccess(false);
                    cmd.setErrorMsg(error);
                }
            } catch (Exception ex) {
                String errMsg = null == ex.getCause() ? ex.getMessage() : ex.getCause().getMessage();
                if (log.isDebugEnabled()) {
                    log.debug("Exception cmd key:" + cmd.getKey());
                }
                if (errMsg.contains("wait interrupt")) {
                    throw ex;
                }
                cmd.setSuccess(false);
                // 用户名或密码错误统一处理
                if (errMsg.startsWith("password is incorrect.") || errMsg.startsWith("ORA-01017:")) {
                    cmd.setErrorMsg("name or password is incorrect");
                    assetService.updateAssetStatus(request.getAsset().getId(), GatherConstants.GATHER_STATE_NOTACCESS);
                    request.setGatherStateUnreachable(true);
                } else if (errMsg.startsWith("failed to connect to")) {
                    error = ex.getMessage();
                    cmd.setErrorMsg(error);
                    assetService.updateAssetStatus(request.getAsset().getId(), GatherConstants.GATHER_STATE_NOTACCESS);
                    request.setGatherStateUnreachable(true);
                } else {
                    cmd.setErrorMsg(errMsg);
                }
            }
        }
    }

    /**
     * 执行一个采集项
     *
     * @param request 资产采集请求对象
     * @param key     采集项key
     * @param cached  是否对采集命令的结果进行缓存
     * @throws GatherException 采集异常
     */
    private void executeCmd(GatherAssetRequest request, String key, boolean cached) throws GatherException {
        Map<String, Object> outputVars;
        String protocol = request.getOption(GatherAssetRequest.EXTRA_PROTOCOL);
        if (supportHost() && (GatherConstants.PROTO_SSH.equals(protocol))) {
            // ssh使用cmd入口模式
            Map<String, TaskInfo> taskGroups = getTaskGroups(request, key);
            outputVars = executeTask(request, taskGroups, cached);
        } else {
            if (log.isWarnEnabled()) {
                log.warn("The agreement is not supported yet " + protocol);
            }
            throw new GatherException("The agreement is not supported yet");
        }
        boolean result = dataParser.parseResult(request, key, outputVars);
        if (!result) {
            throw new GatherException("Failed to parse the result");
        }
    }

    /**
     * 是否是支持该主机类型采集主机类型
     *
     * @return 是否支持
     */
    protected boolean supportHost() {
        boolean support = false;
        String category = getCategory();
        if (null == category) {
            return false;
        }
        switch (category) {
            case GatherConstants.TYPE_SYS_CENTOS_REDHAT_STR:
            case GatherConstants.TYPE_SYS_HP_UX_STR:
            case GatherConstants.TYPE_SYS_DEBIAN_STR:
            case GatherConstants.TYPE_SYS_SUSE_STR:
            case GatherConstants.TYPE_SYS_SOLARIS_STR:
                support = true;
                break;
            default:
                break;
        }
        return support;
    }

    /**
     * 生成采集命令组
     *
     * @param request 资产采集请求对象
     * @param key     itemKey
     * @return 采集命令组
     * @throws GatherException 采集异常
     */
    @SuppressWarnings("unchecked")
    private Map<String, TaskInfo> getTaskGroups(GatherAssetRequest request, String key) throws GatherException {
        GatherDetail detail = gatherCenter.getGatherDetail(key);
        request.addOption(GatherAssetRequest.EXTRA_GATHER_DETAIL, detail);
        Map<String, TaskInfo> taskGroups = new LinkedHashMap<>();
        try {
            int commandCnt = detail.getCommands().getCommand().size();
            if (commandCnt < 1) {
                throw new GatherException("Command is empty: " + key);
            }
        } catch (Exception ex) {
            throw new GatherException("Invalid gather command (configuration error): " + key);
        }
        // 预采集得到的变量值
        Map<String, Object> vars = request.getRequireVars();
        // ssh访问的预采集
        for (GatherDetailCommand cmd : detail.getCommands().getCommand()) {
            String ifParam = cmd.getIfParam();
            String output = cmd.getOutput();
            if (null != ifParam) {
                // 条件控制的命令 例如版本控制
                if (null == vars) {
                    continue;
                }
                Boolean ok = false;
                try {
                    ok = cmdOutputParser.evaluate(ifParam, vars, Boolean.class);
                } catch (Exception ex) {
                    if (log.isWarnEnabled()) {
                        log.warn("cmdOutputParser failed:", ex);
                    }
                }
                if (!ok) {
                    continue;
                }
            }
            // 替换命令中的占位符
            String stepCmd = doReplaceCmd(request, cmd.getCommand());
            String execCmd = stepCmd.trim();
            if (execCmd.endsWith(";")) {
                execCmd = "echo asset_flag_start;" + execCmd + "echo asset_flag_end;";
            } else {
                execCmd = "echo asset_flag_start;" + execCmd + ";echo asset_flag_end;";
            }
            CmdTaskInfo task = new CmdTaskInfo();
            task.setCmd(execCmd);
            if (null == output) {
                output = "output";
            }
            taskGroups.put(output, task);
        }
        return taskGroups;
    }

    /**
     * 执行指令，生成采集结果
     *
     * @param request    资产采集请求对象
     * @param taskGroups 采集命令组
     * @param cached     是否对采集命令进行缓存
     * @return 采集结果
     * @throws GatherException 采集异常
     */
    private Map<String, Object> executeTask(GatherAssetRequest request, Map<String, TaskInfo> taskGroups,
                                            boolean cached) throws GatherException {
        Map<String, Object> cmdOutputVars = new HashMap<>();
        for (Map.Entry<String, TaskInfo> entry : taskGroups.entrySet()) {
            Object output;
            CmdTaskInfo task = (CmdTaskInfo) entry.getValue();
            String command = task.getCmd();
            Object outputOrigin;
            if (cached) {
                String cacheKey = command.trim() + GatherAssetRequest.EXTRA_CMD_CACHE_CONTENT_SUFFIX;
                outputOrigin = request.getOption(cacheKey);
                if (null == outputOrigin) {
                    outputOrigin = inspectOneCommands(request, task);
                    request.addOption(cacheKey, outputOrigin);
                }
            } else {
                outputOrigin = inspectOneCommands(request, task);
            }
            output = postProcessOutput(outputOrigin);
            if (output instanceof String) {
                output = ((String) output).replace("\u000F", "");
            }
            cmdOutputVars.put(entry.getKey(), output);
        }
        return cmdOutputVars;
    }

    /**
     * 对返回的采集数据进行处理
     *
     * @param retValue 采集返回的结果
     * @return 处理后的结果
     */
    abstract Object postProcessOutput(Object retValue);

    /**
     * 执行一个命令组
     *
     * @param request 资产采集请求对象
     * @param task    采集命令
     * @return 采集结果
     * @throws GatherException 采集异常
     */
    protected Object inspectOneCommands(GatherAssetRequest request, CmdTaskInfo task)
            throws GatherException {
        return null;
    }

    /**
     * 替换命令中的占位符
     *
     * @param request 资产采集请求
     * @param cmd     替换命令
     * @return 替换后的命令
     */
    @SuppressWarnings("unchecked")
    protected String doReplaceCmd(GatherAssetRequest request, String cmd) {
        if (StringUtils.isEmpty(cmd)) {
            return cmd;
        }
        cmd = removeCmdTabs(cmd);
        return cmd;
    }

    /**
     * 删除命令中的制表符、换行符，以及命令前后的空格
     *
     * @param cmd 目标命令
     * @return 处理过的命令
     */
    protected String removeCmdTabs(String cmd) {
        if (cmd != null) {
            Pattern pattern = Pattern.compile("\t|\r|\n");
            Matcher matcher = pattern.matcher(cmd);
            cmd = matcher.replaceAll("");
            cmd = cmd.trim();
        }
        return cmd;
    }
}
