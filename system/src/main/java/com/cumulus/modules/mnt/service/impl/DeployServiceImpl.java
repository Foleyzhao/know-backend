package com.cumulus.modules.mnt.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.mnt.dto.AppDto;
import com.cumulus.modules.mnt.dto.DeployDto;
import com.cumulus.modules.mnt.dto.DeployQueryCriteria;
import com.cumulus.modules.mnt.dto.ServerDeployDto;
import com.cumulus.modules.mnt.entity.App;
import com.cumulus.modules.mnt.entity.Deploy;
import com.cumulus.modules.mnt.entity.DeployHistory;
import com.cumulus.modules.mnt.entity.ServerDeploy;
import com.cumulus.modules.mnt.mapstruct.DeployMapper;
import com.cumulus.modules.mnt.repository.DeployRepository;
import com.cumulus.modules.mnt.service.DeployHistoryService;
import com.cumulus.modules.mnt.service.DeployService;
import com.cumulus.modules.mnt.service.ServerDeployService;
import com.cumulus.modules.mnt.util.ExecuteShellUtil;
import com.cumulus.modules.mnt.util.ScpClientUtil;
import com.cumulus.modules.mnt.websocket.MsgType;
import com.cumulus.modules.mnt.websocket.SocketMsg;
import com.cumulus.modules.mnt.websocket.WebSocketServer;
import com.cumulus.utils.FileUtils;
import com.cumulus.utils.PageUtils;
import com.cumulus.utils.QueryUtils;
import com.cumulus.utils.SecurityUtils;
import com.cumulus.utils.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 部署服务实现
 */
@Slf4j
@Service
public class DeployServiceImpl implements DeployService {

    /**
     * 文件路径分隔符
     */
    private final String FILE_SEPARATOR = "/";

    /**
     * 部署数据访问接口
     */
    @Autowired
    private DeployRepository deployRepository;

    /**
     * 部署传输对象与部署实体的映射
     */
    @Autowired
    private DeployMapper deployMapper;

    /**
     * 服务器服务接口
     */
    @Autowired
    private ServerDeployService serverDeployService;

    /**
     * 部署历史服务接口
     */
    @Autowired
    private DeployHistoryService deployHistoryService;

    /**
     * 最大循环检测服务状态次数
     */
    private final Integer count = 30;

    @Override
    public Object queryAll(DeployQueryCriteria criteria, Pageable pageable) {
        Page<Deploy> page = deployRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> QueryUtils.getPredicate(root, criteria, criteriaBuilder),
                pageable);
        return PageUtils.toPage(page.map(deployMapper::toDto));
    }

    @Override
    public List<DeployDto> queryAll(DeployQueryCriteria criteria) {
        return deployMapper.toDto(deployRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> QueryUtils.getPredicate(root, criteria, criteriaBuilder)));
    }

    @Override
    public DeployDto findById(Long id) {
        Deploy deploy = deployRepository.findById(id).orElseGet(Deploy::new);
        ValidationUtils.isNull(deploy.getId(), "Deploy", "id", id);
        return deployMapper.toDto(deploy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Deploy resources) {
        deployRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Deploy resources) {
        Deploy deploy = deployRepository.findById(resources.getId()).orElseGet(Deploy::new);
        ValidationUtils.isNull(deploy.getId(), "Deploy", "id", resources.getId());
        deploy.copy(resources);
        deployRepository.save(deploy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            deployRepository.deleteById(id);
        }
    }

    @Override
    public void deploy(String fileSavePath, Long id) {
        deployApp(fileSavePath, id);
    }

    /**
     * 部署应用
     *
     * @param fileSavePath 本机路径
     * @param id           应用ID
     */
    private void deployApp(String fileSavePath, Long id) {
        DeployDto deploy = findById(id);
        if (null == deploy) {
            sendMsg("部署信息不存在", MsgType.ERROR);
            throw new BadRequestException("部署信息不存在");
        }
        AppDto app = deploy.getApp();
        if (null == app) {
            sendMsg("包对应应用信息不存在", MsgType.ERROR);
            throw new BadRequestException("包对应应用信息不存在");
        }
        int port = app.getPort();
        //这个是服务器部署路径
        String uploadPath = app.getUploadPath();
        StringBuilder sb = new StringBuilder();
        String msg;
        Set<ServerDeployDto> deploys = deploy.getDeploys();
        for (ServerDeployDto deployDTO : deploys) {
            String ip = deployDTO.getIp();
            ExecuteShellUtil executeShellUtil = getExecuteShellUtil(ip);
            // 判断是否第一次部署
            boolean flag = checkFile(executeShellUtil, app);
            // 第一步要确认服务器上有这个目录
            executeShellUtil.execute("mkdir -p " + app.getUploadPath());
            executeShellUtil.execute("mkdir -p " + app.getBackupPath());
            executeShellUtil.execute("mkdir -p " + app.getDeployPath());
            // 上传文件
            msg = String.format("登陆到服务器:%s", ip);
            ScpClientUtil scpClientUtil = getScpClientUtil(ip);
            if (log.isInfoEnabled()) {
                log.info(msg);
            }
            sendMsg(msg, MsgType.INFO);
            msg = String.format("上传文件到服务器:%s<br>目录:%s下，请稍等...", ip, uploadPath);
            sendMsg(msg, MsgType.INFO);
            scpClientUtil.putFile(fileSavePath, uploadPath);
            if (flag) {
                sendMsg("停止原来应用", MsgType.INFO);
                // 停止应用
                stopApp(port, executeShellUtil);
                sendMsg("备份原来应用", MsgType.INFO);
                //备份应用
                backupApp(executeShellUtil, ip, app.getDeployPath() + FILE_SEPARATOR, app.getName(),
                        app.getBackupPath() + FILE_SEPARATOR, id);
            }
            sendMsg("部署应用", MsgType.INFO);
            // 部署文件,并启动应用
            String deployScript = app.getDeployScript();
            executeShellUtil.execute(deployScript);
            sleep(3);
            sendMsg("应用部署中，请耐心等待部署结果，或者稍后手动查看部署状态", MsgType.INFO);
            int i = 0;
            boolean result = false;
            // 由于启动应用需要时间，所以需要循环获取状态，如果超过30次，则认为是启动失败
            while (i++ < count) {
                result = checkIsRunningStatus(port, executeShellUtil);
                if (result) {
                    break;
                }
                // 休眠6秒
                sleep(6);
            }
            sb.append("服务器:").append(deployDTO.getName()).append("<br>应用:").append(app.getName());
            sendResultMsg(result, sb);
            executeShellUtil.close();
        }
    }

    /**
     * 休眠
     *
     * @param second 休眠时间
     */
    private void sleep(int second) {
        try {
            Thread.sleep(second * 1000L);
        } catch (InterruptedException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 备份应用
     *
     * @param executeShellUtil 执行指令客户端
     * @param ip               IP地址
     * @param fileSavePath     部署路径
     * @param appName          应用名称
     * @param backupPath       备份路径
     * @param id               应用ID
     */
    private void backupApp(ExecuteShellUtil executeShellUtil, String ip, String fileSavePath, String appName,
                           String backupPath, Long id) {
        String deployDate = DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN);
        StringBuilder sb = new StringBuilder();
        backupPath += appName + FILE_SEPARATOR + deployDate + "\n";
        sb.append("mkdir -p ").append(backupPath);
        sb.append("mv -f ").append(fileSavePath);
        sb.append(appName).append(" ").append(backupPath);
        if (log.isInfoEnabled()) {
            log.info("Backup application script: " + sb);
        }
        executeShellUtil.execute(sb.toString());
        // 保存还原信息
        DeployHistory deployHistory = new DeployHistory();
        deployHistory.setAppName(appName);
        deployHistory.setDeployUser(SecurityUtils.getCurrentUsername());
        deployHistory.setIp(ip);
        deployHistory.setDeployId(id);
        deployHistoryService.create(deployHistory);
    }

    /**
     * 停止应用
     *
     * @param port             端口
     * @param executeShellUtil 执行指令客户端
     */
    private void stopApp(int port, ExecuteShellUtil executeShellUtil) {
        // 发送停止命令
        executeShellUtil.execute(String.format("lsof -i :%d|grep -v \"PID\"|awk '{print \"kill -9\",$2}'|sh", port));
    }

    /**
     * 指定端口程序是否在运行
     *
     * @param port             端口
     * @param executeShellUtil 执行指令客户端
     * @return 是否在运行
     */
    private boolean checkIsRunningStatus(int port, ExecuteShellUtil executeShellUtil) {
        String result = executeShellUtil.executeForResult(String.format("fuser -n tcp %d", port));
        return result.indexOf("/tcp:") > 0;
    }

    /**
     * 发送Websocket消息
     *
     * @param msg     消息
     * @param msgType 消息类型
     */
    private void sendMsg(String msg, MsgType msgType) {
        try {
            WebSocketServer.sendInfo(new SocketMsg(msg, msgType), "deploy");
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public String serverStatus(Deploy resources) {
        Set<ServerDeploy> serverDeploys = resources.getDeploys();
        App app = resources.getApp();
        for (ServerDeploy serverDeploy : serverDeploys) {
            StringBuilder sb = new StringBuilder();
            ExecuteShellUtil executeShellUtil = getExecuteShellUtil(serverDeploy.getIp());
            sb.append("服务器:").append(serverDeploy.getName()).append("<br>应用:").append(app.getName());
            boolean result = checkIsRunningStatus(app.getPort(), executeShellUtil);
            if (result) {
                sb.append("<br>正在运行");
                sendMsg(sb.toString(), MsgType.INFO);
            } else {
                sb.append("<br>已停止!");
                sendMsg(sb.toString(), MsgType.ERROR);
            }
            if (log.isInfoEnabled()) {
                log.info(sb.toString());
            }
            executeShellUtil.close();
        }
        return "执行完毕";
    }

    /**
     * 判断是否是第一次部署
     *
     * @param executeShellUtil 执行指令客户端
     * @param appDTO           应用
     * @return 是否是第一次部署
     */
    private boolean checkFile(ExecuteShellUtil executeShellUtil, AppDto appDTO) {
        String result = executeShellUtil.executeForResult("find " + appDTO.getDeployPath() + " -name " +
                appDTO.getName());
        return result.indexOf(appDTO.getName()) > 0;
    }

    @Override
    public String startServer(Deploy resources) {
        Set<ServerDeploy> deploys = resources.getDeploys();
        App app = resources.getApp();
        for (ServerDeploy deploy : deploys) {
            StringBuilder sb = new StringBuilder();
            ExecuteShellUtil executeShellUtil = getExecuteShellUtil(deploy.getIp());
            // 为了防止重复启动，这里先停止应用
            stopApp(app.getPort(), executeShellUtil);
            sb.append("服务器:").append(deploy.getName()).append("<br>应用:").append(app.getName());
            sendMsg("下发启动命令", MsgType.INFO);
            executeShellUtil.execute(app.getStartScript());
            sleep(3);
            sendMsg("应用启动中，请耐心等待启动结果，或者稍后手动查看运行状态", MsgType.INFO);
            int i = 0;
            boolean result = false;
            // 由于启动应用需要时间，所以需要循环获取状态，如果超过30次，则认为是启动失败
            while (i++ < count) {
                result = checkIsRunningStatus(app.getPort(), executeShellUtil);
                if (result) {
                    break;
                }
                // 休眠6秒
                sleep(6);
            }
            sendResultMsg(result, sb);
            if (log.isInfoEnabled()) {
                log.info(sb.toString());
            }
            executeShellUtil.close();
        }
        return "执行完毕";
    }

    @Override
    public String stopServer(Deploy resources) {
        Set<ServerDeploy> deploys = resources.getDeploys();
        App app = resources.getApp();
        for (ServerDeploy deploy : deploys) {
            StringBuilder sb = new StringBuilder();
            ExecuteShellUtil executeShellUtil = getExecuteShellUtil(deploy.getIp());
            sb.append("服务器:").append(deploy.getName()).append("<br>应用:").append(app.getName());
            sendMsg("下发停止命令", MsgType.INFO);
            // 停止应用
            stopApp(app.getPort(), executeShellUtil);
            sleep(1);
            boolean result = checkIsRunningStatus(app.getPort(), executeShellUtil);
            if (result) {
                sb.append("<br>关闭失败!");
                sendMsg(sb.toString(), MsgType.ERROR);
            } else {
                sb.append("<br>关闭成功!");
                sendMsg(sb.toString(), MsgType.INFO);
            }
            if (log.isInfoEnabled()) {
                log.info(sb.toString());
            }
            executeShellUtil.close();
        }
        return "执行完毕";
    }

    @Override
    public String serverReduction(DeployHistory resources) {
        Long deployId = resources.getDeployId();
        Deploy deployInfo = deployRepository.findById(deployId).orElseGet(Deploy::new);
        String deployDate = DateUtil.format(resources.getDeployDate(), DatePattern.PURE_DATETIME_PATTERN);
        App app = deployInfo.getApp();
        if (null == app) {
            sendMsg("应用信息不存在：" + resources.getAppName(), MsgType.ERROR);
            throw new BadRequestException("应用信息不存在：" + resources.getAppName());
        }
        String backupPath = app.getBackupPath() + FILE_SEPARATOR;
        backupPath += resources.getAppName() + FILE_SEPARATOR + deployDate;
        // 服务器部署路径
        String deployPath = app.getDeployPath();
        String ip = resources.getIp();
        ExecuteShellUtil executeShellUtil = getExecuteShellUtil(ip);
        String msg;
        msg = String.format("登陆到服务器:%s", ip);
        if (log.isInfoEnabled()) {
            log.info(msg);
        }
        sendMsg(msg, MsgType.INFO);
        sendMsg("停止原来应用", MsgType.INFO);
        // 停止应用
        stopApp(app.getPort(), executeShellUtil);
        // 删除原来应用
        sendMsg("删除应用", MsgType.INFO);
        executeShellUtil.execute("rm -rf " + deployPath + FILE_SEPARATOR + resources.getAppName());
        // 还原应用
        sendMsg("还原应用", MsgType.INFO);
        executeShellUtil.execute("cp -r " + backupPath + "/. " + deployPath);
        sendMsg("启动应用", MsgType.INFO);
        executeShellUtil.execute(app.getStartScript());
        sendMsg("应用启动中，请耐心等待启动结果，或者稍后手动查看启动状态", MsgType.INFO);
        int i = 0;
        boolean result = false;
        // 由于启动应用需要时间，所以需要循环获取状态，如果超过30次，则认为是启动失败
        while (i++ < count) {
            result = checkIsRunningStatus(app.getPort(), executeShellUtil);
            if (result) {
                break;
            }
            // 休眠6秒
            sleep(6);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("服务器:").append(ip).append("<br>应用:").append(resources.getAppName());
        sendResultMsg(result, sb);
        executeShellUtil.close();
        return "";
    }

    /**
     * 获取执行指令客户端
     *
     * @param ip IP地址
     * @return 执行指令客户端
     */
    private ExecuteShellUtil getExecuteShellUtil(String ip) {
        ServerDeployDto serverDeployDTO = serverDeployService.findByIp(ip);
        if (null == serverDeployDTO) {
            sendMsg("IP对应服务器信息不存在：" + ip, MsgType.ERROR);
            throw new BadRequestException("IP对应服务器信息不存在：" + ip);
        }
        return new ExecuteShellUtil(ip, serverDeployDTO.getAccount(), serverDeployDTO.getPassword(),
                serverDeployDTO.getPort());
    }

    /**
     * SCP客户端
     *
     * @param ip IP地址
     * @return SCP客户端
     */
    private ScpClientUtil getScpClientUtil(String ip) {
        ServerDeployDto serverDeployDTO = serverDeployService.findByIp(ip);
        if (null == serverDeployDTO) {
            sendMsg("IP对应服务器信息不存在：" + ip, MsgType.ERROR);
            throw new BadRequestException("IP对应服务器信息不存在：" + ip);
        }
        return ScpClientUtil.getInstance(ip, serverDeployDTO.getPort(), serverDeployDTO.getAccount(),
                serverDeployDTO.getPassword());
    }

    /**
     * 追加执行结果信息并发送
     *
     * @param result 执行结果
     * @param sb     输出信息
     */
    private void sendResultMsg(boolean result, StringBuilder sb) {
        if (result) {
            sb.append("<br>启动成功!");
            sendMsg(sb.toString(), MsgType.INFO);
        } else {
            sb.append("<br>启动失败!");
            sendMsg(sb.toString(), MsgType.ERROR);
        }
    }

    @Override
    public void download(List<DeployDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DeployDto deployDto : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("应用名称", deployDto.getApp().getName());
            map.put("服务器", deployDto.getServers());
            map.put("部署日期", deployDto.getCreateTime());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
    }

}
