package com.cumulus.modules.business.detect.common.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.alibaba.fastjson.JSON;
import com.cumulus.modules.business.detect.common.DetectConstant;
import com.cumulus.modules.business.detect.common.DetectManager;
import com.cumulus.modules.business.detect.dto.DetectReponse;
import com.cumulus.modules.business.detect.entity.DetectRecord;
import com.cumulus.modules.business.detect.entity.DetectTask;
import com.cumulus.modules.business.detect.entity.RecordDetail;
import com.cumulus.modules.business.detect.repository.DetectRecordRepository;
import com.cumulus.modules.business.detect.repository.DetectTaskRepository;
import com.cumulus.modules.business.detect.repository.RecordDetailRepository;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.entity.AssetConfirm;
import com.cumulus.modules.business.entity.AssetExtend;
import com.cumulus.modules.business.entity.IpLibrary;
import com.cumulus.modules.business.repository.AssetConfirmRepository;
import com.cumulus.modules.business.repository.AssetRepository;
import com.cumulus.modules.business.repository.AssetSysTypeRepository;
import com.cumulus.modules.business.repository.AssetTypeRepository;
import com.cumulus.modules.business.repository.IpLibraryRepository;
import com.cumulus.utils.RegexUtil;
import com.cumulus.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 发现任务接收服务实现
 *
 * @author zhangxq
 */
@Slf4j
@Service
public class DetectReceiveServiceImpl implements DetectReceiveService {


    /**
     * 登录协议
     */
    private static final String PROTOCOL = "ssh|telnet|winrm";

    /**
     * 资产数据访问接口
     */
    @Autowired
    private AssetRepository assetRepository;

    /**
     * 确认资产数据访问接口
     */
    @Autowired
    private AssetConfirmRepository assetConfirmRepository;

    /**
     * 发现任务数据访问接口
     */
    @Autowired
    private DetectTaskRepository detectTaskRepository;

    /**
     * 资产类型数据访问接口
     */
    @Autowired
    private AssetTypeRepository assetTypeRepository;

    /**
     * 资产子类型数据访问接口
     */
    @Autowired
    private AssetSysTypeRepository assetSysTypeRepository;

    /**
     * ip库数据访问接口
     */
    @Autowired
    private IpLibraryRepository ipLibraryRepository;

    /**
     * 发现任务记录数据访问接口
     */
    @Autowired
    private DetectRecordRepository detectRecordRepository;

    /**
     * 发现任务记录详情数据访问接口
     */
    @Autowired
    private RecordDetailRepository recordDetailRepository;

    /**
     * redis模板
     */
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发现任务管理
     */
    @Autowired
    private DetectManager detectManager;

    /**
     * 端口分隔符
     */
    private static final String SEPARATE_STR = ",";

    /**
     * 返回是否对特定响应的消息感兴趣
     *
     * @param id 响应id
     * @return 是否感兴趣
     */
    @Override
    public boolean interest(String id) {
        return false;
    }

    /**
     * 接收并处理响应消息
     *
     * @param response 响应消息
     */
    @Override
    public void receiveAndHandle(Map<String, Object> response) {
        String json = JSON.toJSONString(response);
        log.info("receive response:" + json);
        //主机资产
        saveIp(JSON.parseObject(json, DetectReponse.DetectReponseIp.class));
        //查看进度
        checkEnd(JSON.parseObject(json, DetectReponse.class));
    }

    /**
     * 检查单个ip进度 当前任务进度
     *
     * @param response 引擎响应消息
     */
    private void checkEnd(DetectReponse response) {
        Long id = Long.parseLong(response.getId());
        DetectTask detectTask = detectTaskRepository.findById(id).orElse(null);
        if (null == detectTask) {
            log.info("checkEnd 当前任务不存在");
            return;
        }
        detectManager.redisAdd(id, DetectConstant.REDIS_KEY_DONE);
        //判断是否完成整个任务 num 和 done判断
        if (redisTemplate.boundHashOps(DetectConstant.REDIS_KEY_DETECT + id).
                get(DetectConstant.REDIS_KEY_DONE).toString().equals(
                redisTemplate.boundHashOps(DetectConstant.REDIS_KEY_DETECT + id).
                        get(DetectConstant.REDIS_KEY_NUM).toString())
        ) {
            detectManager.endRecord(detectTask);
        }
    }

    /**
     * 保存主机资产
     *
     * @param ip 响应消息ip类型
     */
    private void saveIp(DetectReponse.DetectReponseIp ip) {
        String completeIp = RegexUtil.checkToComplete(ip.getTarget(), false);
        DetectTask detectTask = detectTaskRepository.findById(Long.parseLong(ip.getId())).orElse(null);
        if (null == detectTask) {
            log.info("当前任务不存在");
            return;
        }
        //是否在线
        detectManager.redisAdd(detectTask.getId(), ip.is_online() ?
                DetectConstant.REDIS_KEY_ONLINE : DetectConstant.REDIS_KEY_OFFLINE);
        List<Asset> assetList = assetRepository.queryAssetByCompleteIpEqualsAndAssetCategoryEquals(
                completeIp, Asset.CATEGORY_HOST);
        //资产清单存在则直接更新 不存在则新增
        if (ip.is_online()) {
            if (assetList.isEmpty()) {
                addAssetConfirm(ip, completeIp, detectTask);
            } else {
                updateAsset(assetList.get(0), ip, completeIp);
            }
        }
        Long recordId = Long.parseLong(redisTemplate.boundHashOps(
                DetectConstant.REDIS_KEY_DETECT + detectTask.getId())
                .get(DetectConstant.REDIS_KEY_RECORDID).toString());
        DetectRecord record = detectRecordRepository.findById(recordId).orElse(null);
        if (null == record) {
            log.info("当前任务记录不存在");
            return;
        }
        //新增任务记录详情
        RecordDetail recordDetail = new RecordDetail();
        recordDetail.setOnline(ip.is_online());
        recordDetail.setIp(completeIp);
        recordDetail.setDetectRecord(record);
        recordDetailRepository.save(recordDetail);
    }

    /**
     * 清单不存在 新增确认资产
     *
     * @param ip
     * @param completeIp
     * @param detectTask
     */
    private void addAssetConfirm(DetectReponse.DetectReponseIp ip, String completeIp, DetectTask detectTask) {
        AssetConfirm assetConfirm;
        List<AssetConfirm> assetConfirms = assetConfirmRepository.
                queryAssetConfirmByCompleteIpEqualsAndAssetCategoryEquals(completeIp, Asset.CATEGORY_HOST);
        if (assetConfirms.isEmpty()) {
            assetConfirm = new AssetConfirm();
        } else {
            assetConfirm = assetConfirms.get(0);
        }
        //todo 查看是否可以agent redis查  采集方式
        assetConfirm.setOnline(ip.is_online());
        assetConfirm.setGatherType(DetectConstant.GATHER_TYPE_LOGIN);
        //存入发现任务信息
        assetConfirm.setDetectTask(detectTask);
        assetConfirm.setDetectTaskName(detectTask.getDetectTaskName());
        assetConfirm.setIp(ip.getTarget());
        assetConfirm.setCompleteIp(completeIp);
        assetConfirm.setAssetCategory(Asset.CATEGORY_HOST);
        List<String> ports = new ArrayList<>();
        //开放端口
        if ("".equals(ip.getPorts_list())) {
            assetConfirm.setOpenPort("0");
        } else {
            ports = Arrays.asList(StringUtils.split(ip.getPorts_list(), SEPARATE_STR));
            assetConfirm.setOpenPort(String.valueOf(ports.size()));
        }
        //端口资产
        AssetConfirm portAsset = new AssetConfirm();
        IpLibrary ipLibrary = ipLibraryRepository.findByCompleteIpEquals(assetConfirm.getCompleteIp());
        if (ipLibrary != null) {
            assetConfirm.setDept(ipLibrary.getDept());
            portAsset.setDept(ipLibrary.getDept());
        }
        assetConfirmRepository.save(assetConfirm);
        //确认资产表已有端口
        List<AssetConfirm> assetConfirmList = assetConfirmRepository.
                queryAssetConfirmByCompleteIpEqualsAndAssetCategoryEquals(completeIp, Asset.CATEGORY_PORT);
        //存入发现任务信息
        portAsset.setDetectTask(detectTask);
        portAsset.setDetectTaskName(detectTask.getDetectTaskName());
        portAsset.setIp(ip.getTarget());
        portAsset.setCompleteIp(completeIp);
        portAsset.setAssetCategory(Asset.CATEGORY_PORT);
        //存入端口信息
        ports.forEach(port -> {
            AssetConfirm oldPortAsset =
                    assetConfirmRepository.queryAssetConfirmByCompleteIpEqualsAndPortEquals(completeIp,
                            Integer.parseInt(port));
            if (oldPortAsset == null) {
                oldPortAsset = AssetConfirm.copy(portAsset);
            }
            oldPortAsset.setPort(Integer.parseInt(port));
            oldPortAsset.setOnline(true);
            assetConfirmRepository.save(oldPortAsset);
        });
        for (AssetConfirm e : assetConfirmList) {
            //最新扫描结果如果没有则为下线
            if (!ports.contains(e.getPort())) {
                e.setOnline(false);
                assetConfirmRepository.save(e);
            }
        }
    }

    /**
     * 清单存在 更新清单
     *
     * @param oldAsset
     * @param ip
     * @param completeIp
     */
    private void updateAsset(Asset oldAsset, DetectReponse.DetectReponseIp ip, String completeIp) {
        oldAsset.setAssetStatus(ip.is_online() ? Asset.STATUS_SURVIVE : Asset.STATUS_OFFLINE);
        assetRepository.save(oldAsset);
        List<String> ports = new ArrayList<>();
        //开放端口
        if (!"".equals(ip.getPorts_list())) {
            ports = Arrays.asList(StringUtils.split(ip.getPorts_list(), SEPARATE_STR));
        }
        //资产清单已有端口
        List<Asset> assetList = assetRepository.
                queryAssetByCompleteIpEqualsAndAssetCategoryEquals(completeIp, Asset.CATEGORY_PORT);
        //存入最新扫描结果
        for (String e : ports) {
            Asset obj = assetRepository.queryAssetByCompleteIpEqualsAndPortEqualsAndAssetCategoryEquals(
                    completeIp, Integer.parseInt(e), Asset.CATEGORY_PORT);
            if (obj == null) {
                obj = new Asset();
                obj.setIp(oldAsset.getIp());
                obj.setCompleteIp(oldAsset.getCompleteIp());
                obj.setAssetCategory(Asset.CATEGORY_PORT);
                obj.setParent(oldAsset);
                obj.setPort(Integer.parseInt(e));
                obj.setDept(oldAsset.getDept());
            }
            obj.setAssetStatus(Asset.STATUS_SURVIVE);
            assetRepository.save(obj);
        }
        for (Asset e : assetList) {
            //最新扫描结果如果没有则为下线
            if (!ports.contains(e.getPort())) {
                e.setAssetStatus(Asset.STATUS_OFFLINE);
                assetRepository.save(e);
            }
        }
    }


    /**
     * 保存应用资产
     *
     * @param web 响应消息web类型
     */
    private void saveWeb(DetectReponse.DetectReponseWeb web) {
        String ip = RegexUtil.checkToComplete(web.getDomainIp(), false);
        Integer port = web.getPort();
        //查资产是否存在
        Asset asset = assetRepository.
                queryAssetByCompleteIpEqualsAndPortEqualsAndAssetCategoryEquals(ip, port, 2);
        AssetConfirm assetConfirm =
                assetConfirmRepository.queryAssetConfirmByCompleteIpEqualsAndPortEquals(ip, port);
        //确认资产不为空
        if (assetConfirm != null) {
            AssetExtend assetExtend = assetConfirm.getAssetExtend();
            assetExtend.setJson(JSON.toJSONString(web));
            assetExtend.setWebsite(web.getTarget());
            assetExtend.setTitle(web.getTitle());
            assetConfirm.setAssetExtend(assetExtend);
            assetConfirmRepository.save(assetConfirm);
            log.info("web update assetConfirm：" + web.getDomainIp() + ":" + port);
        } else {
            if (asset == null) {
                DetectTask detectTask = detectTaskRepository.findById(Long.parseLong(web.getId())).orElse(null);
                if (null == detectTask) {
                    log.info("当前任务不存在");
                    return;
                }
                AssetConfirm newAssetConfirm = new AssetConfirm();
                newAssetConfirm.setIp(web.getDomainIp());
                newAssetConfirm.setCompleteIp(RegexUtil.checkToComplete(web.getDomainIp(), false));
                newAssetConfirm.setPort(web.getPort());
                newAssetConfirm.setAssetCategory(2);
                //添加发现任务信息
                newAssetConfirm.setDetectTask(detectTask);
                newAssetConfirm.setDetectTaskName(detectTask.getDetectTaskName());
                //扩展信息
                AssetExtend assetExtend = new AssetExtend();
                assetExtend.setWebsite(web.getTarget());
                assetExtend.setTitle(web.getTitle());
                newAssetConfirm.setAssetExtend(assetExtend);
                assetConfirmRepository.save(newAssetConfirm);
                log.info("web add assetConfirm：" + web.getDomainIp() + ":" + port);
            }
        }
    }

}
