package com.cumulus.modules.business.gather.request;

import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.gather.entity.es.GatherAssetLogEs;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 资产采集请求
 *
 * @author zhaoff
 */
@Getter
@Setter
public class GatherAssetRequest {

    /**
     * 额外信息key-登陆协议
     */
    public final static String EXTRA_PROTOCOL = "protocol";

    /**
     * 额外信息key-是否需要缓存采集信息
     */
    public final static String EXTRA_CMD_CACHE = "extra_cmd_cache";

    /**
     * 额外信息key-远程连接信息
     */
    public final static String EXTRA_CONNECTION_INFO = "extra_connection_info";

    /**
     * 额外信息key-登录账号信息
     */
    public final static String EXTRA_LOGIN_ACCOUNT = "extra_login_account";

    /**
     * 额外信息key-采集任务请求
     */
    public final static String EXTRA_TASK_REQUEST = "extra_task_request";

    /**
     * 额外信息key-采集指令详情
     */
    public final static String EXTRA_GATHER_DETAIL = "extra_gather_detail";

    /**
     * 额外信息key-采集原始输出缓存后缀
     */
    public final static String EXTRA_CMD_CACHE_CONTENT_SUFFIX = "_CmdCache";

    /**
     * 资产帐号key-用户名
     */
    public final static String ASSET_ACCOUNT_NAME = "name";

    /**
     * 资产帐号key-密码
     */
    public final static String ASSET_ACCOUNT_PWD = "pwd";

    /**
     * 登录服务key-端口
     */
    public final static String LOGIN_SERVICE_PORT = "port";

    /**
     * 登录服务key-协议
     */
    public final static String LOGIN_SERVICE_PROTO = "proto";

    /**
     * 主类的名字
     */
    private String mainCategory;

    /**
     * 次类的名字
     */
    private String category;

    /**
     * 采集资产
     */
    private Asset asset;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 预采集项
     */
    private GatherAssetRequest preRequest;

    /**
     * 采集项采集请求对象集合
     */
    private Map<String, GatherCmdRequest> results;

    /**
     * 采集结果
     */
    private boolean success = true;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 资产的帐号列表
     */
    private List<Map<String, String>> assetAccounts;

    /**
     * 登录的服务
     * {
     * "proto": "ssh",
     * "port": 22
     * }
     */
    private Map<String, Object> loginService;

    /**
     * 是否是无法访问
     */
    private boolean gatherStateUnreachable = false;

    /**
     * 采集日志
     */
    private GatherAssetLogEs assetLog;

    /**
     * 预采集得到的变量值
     * {
     * "version":xxx
     * }
     */
    private Map<String, Object> requireVars;

    /**
     * 采集任务超时时间
     */
    private int tto;

    /**
     * 附加信息
     * {
     * protocol:登陆协议
     * extra_cmd_cache: 是否需要缓存采集信息
     * extra_connection_info：远程连接信息
     * extra_login_account：登录账号信息
     * extra_task_request：采集任务请求
     * extra_gather_detail：采集指令详情
     * cmd+_CmdCache：采集原始输出缓存
     * }
     */
    private Map<String, Object> extra;

    /**
     * 构造函数
     *
     * @param mainCategory 主类
     * @param category     次类
     */
    public GatherAssetRequest(String mainCategory, String category) {
        this.mainCategory = mainCategory;
        this.category = category;
        results = new HashMap<>();
        extra = new HashMap<>();
    }

    /**
     * 添加特定itemKey的采集项
     *
     * @param gatherCmdRequest 具体采集项
     */
    public void addCmdRequest(GatherCmdRequest gatherCmdRequest) {
        results.put(gatherCmdRequest.getKey(), gatherCmdRequest);
    }

    /**
     * 移除特定itemKey的采集项
     *
     * @param key itemKey
     */
    public void removeCmdRequest(String key) {
        results.remove(key);
    }

    /**
     * 返回采集项key集合
     *
     * @return 采集项key集合
     */
    public Set<String> getCmds() {
        return results.keySet();
    }

    /**
     * 根据itemKey返回采集项请求
     *
     * @param key itemKey
     * @return 对应的采集项请求
     */
    public GatherCmdRequest getCmdRequest(String key) {
        return results.get(key);
    }

    /**
     * 返回采集项请求集合
     *
     * @return 采集项请求集合
     */
    public Collection<GatherCmdRequest> getCmdRequest() {
        return results.values();
    }

    /**
     * 返回特定key的附加属性
     *
     * @param <T> 通用泛型
     * @param key key
     * @return 对应的附加属性
     */
    @SuppressWarnings("unchecked")
    public <T> T getOption(String key) {
        Object obj = extra.get(key);
        if (null == obj) {
            return null;
        }
        return (T) obj;
    }

    /**
     * 向附加属性中添加指定key的附加属性
     *
     * @param key   key
     * @param value 附加属性
     */
    public void addOption(String key, Object value) {
        extra.put(key, value);
    }

    /**
     * 清理附加信息
     */
    public void clearOption() {
        extra.clear();
    }

}
