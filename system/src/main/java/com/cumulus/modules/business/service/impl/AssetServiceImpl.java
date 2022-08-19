package com.cumulus.modules.business.service.impl;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.business.controller.AssetStatisticsController;
import com.cumulus.modules.business.detect.common.DetectConstant;
import com.cumulus.modules.business.dto.AssetDto;
import com.cumulus.modules.business.dto.AssetQueryCriteria;
import com.cumulus.modules.business.dto.AssetWarehouseDto;
import com.cumulus.modules.business.dto.ImportResultDto;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.entity.AssetConfig;
import com.cumulus.modules.business.entity.AssetConfirm;
import com.cumulus.modules.business.entity.AssetExtend;
import com.cumulus.modules.business.entity.AssetSysType;
import com.cumulus.modules.business.entity.AssetTag;
import com.cumulus.modules.business.entity.AssetType;
import com.cumulus.modules.business.entity.IpLibrary;
import com.cumulus.modules.business.entity.LoginGather;
import com.cumulus.modules.business.entity.RemoteScan;
import com.cumulus.modules.business.gather.annotation.HistoryAnnotation;
import com.cumulus.modules.business.gather.aspect.HistoryAspect;
import com.cumulus.modules.business.gather.common.constant.AssetPortraitConstants;
import com.cumulus.modules.business.gather.common.service.impl.CmdSendBean;
import com.cumulus.modules.business.gather.common.utils.BusinessCommon;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.HistoryEs;
import com.cumulus.modules.business.gather.mapper.AssetTransformMapper;
import com.cumulus.modules.business.gather.mapper.AssetsPortraitMapper;
import com.cumulus.modules.business.gather.model.GatherDetail;
import com.cumulus.modules.business.gather.model.GatherDetailCommand;
import com.cumulus.modules.business.gather.provider.impl.LinuxGatherProvider;
import com.cumulus.modules.business.gather.request.AccountInfo;
import com.cumulus.modules.business.gather.request.CmdTaskInfo;
import com.cumulus.modules.business.gather.request.ConnectionInfo;
import com.cumulus.modules.business.gather.request.GatherException;
import com.cumulus.modules.business.gather.request.TaskRequest;
import com.cumulus.modules.business.gather.request.TaskResponse;
import com.cumulus.modules.business.gather.request.TaskResponseWrapper;
import com.cumulus.modules.business.gather.service.AccountEsService;
import com.cumulus.modules.business.gather.service.DBEsService;
import com.cumulus.modules.business.gather.service.DiskPartitionEsService;
import com.cumulus.modules.business.gather.service.EnvironmentEsService;
import com.cumulus.modules.business.gather.service.HardwareEsService;
import com.cumulus.modules.business.gather.service.MiddlewareEsService;
import com.cumulus.modules.business.gather.service.NetworkEsService;
import com.cumulus.modules.business.gather.service.PortEsService;
import com.cumulus.modules.business.gather.service.RouteEsService;
import com.cumulus.modules.business.gather.service.ScanDBEsService;
import com.cumulus.modules.business.gather.service.ScanMiddlewareEsService;
import com.cumulus.modules.business.gather.service.ScanServicePortEsService;
import com.cumulus.modules.business.gather.service.ServiceEsService;
import com.cumulus.modules.business.gather.service.SoftwareEsService;
import com.cumulus.modules.business.gather.service.SystemProcessesEsService;
import com.cumulus.modules.business.gather.service.gather.GatherCenter;
import com.cumulus.modules.business.gather.vo.AssetPortraitVo;
import com.cumulus.modules.business.mapstruct.AssetMapper;
import com.cumulus.modules.business.mapstruct.AssetWareHouseMapper;
import com.cumulus.modules.business.repository.AssetConfirmRepository;
import com.cumulus.modules.business.repository.AssetRepository;
import com.cumulus.modules.business.repository.AssetSysTypeRepository;
import com.cumulus.modules.business.repository.AssetTagRepository;
import com.cumulus.modules.business.repository.AssetTypeRepository;
import com.cumulus.modules.business.repository.CountVulnerabilityRepository;
import com.cumulus.modules.business.repository.IpLibraryRepository;
import com.cumulus.modules.business.service.AssetService;
import com.cumulus.modules.business.vulnerability.entity.es.VulnerabilityEs;
import com.cumulus.modules.business.vulnerability.repository.VulnerabilityEsRepository;
import com.cumulus.modules.system.entity.Dept;
import com.cumulus.modules.system.repository.DeptRepository;
import com.cumulus.modules.system.service.UserService;
import com.cumulus.utils.DateUtils;
import com.cumulus.utils.EncryptUtils;
import com.cumulus.utils.ExcelResolve;
import com.cumulus.utils.FileUtils;
import com.cumulus.utils.QueryUtils;
import com.cumulus.utils.RegexUtil;
import com.cumulus.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.LongBounds;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.AggregationsContainer;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 资产服务实现
 *
 * @author zhangxq
 */
@Slf4j
@Service
public class AssetServiceImpl implements AssetService {

    /**
     * 线程-登录采集分发线程名
     */
    public static final String THREAD_TASK_RESULT_LOGIN = "Gather-task-result-login";

    /**
     * 允许的登录协议
     */
    private static final String[] PROTOCOLS = {"ssh", "SSH", "Telnet", "telnet", "Winrm", "winrm"};

    /**
     * 子查询的默认分页数据 0页 每页10条数据
     */
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 聚合名称
     */
    private static final String AGG_NAME_GROUP_BY_SOURCE = "group_by_source";
    private static final String AGG_NAME_GROUP_BY_DATE = "group_by_date";
    private static final String AGG_NAME_GROUP_BY_ASSET_ID = "group_by_assetId";

    /**
     * 聚合名称
     */
    private static final String FILED_NAME_UPDATE_TIME = "updateTime";
    private static final String FILED_NAME_SOURCE = "source";
    private static final String FILED_NAME_ASSET_CATEGORY = "assetCategory";
    private static final String FILED_NAME_ASSET_ID = "assetId";

    /**
     * 风险分隔符
     */
    private static final String RISK_SEPARATION = "-";

    /**
     * 采集方式
     */
    private static final String[] GATHERTYPE = {"agent", "登录采集", "远程扫描"};

    /**
     * 资产名称 HOST
     */
    private static final String ASSETNAME_HOST = "HOST";

    /**
     * 资产名称 APP
     */
    private static final String ASSETNAME_APP = "APP";

    /**
     * 采集任务结果队列
     */
    private final LinkedBlockingQueue<Map<Long, TaskRequest>> taskResult = new LinkedBlockingQueue<>();

    /**
     * 采集业务通用服务
     */
    @Autowired
    private BusinessCommon businessCommon;

    /**
     * 漏洞数据接口
     */
    @Autowired
    private VulnerabilityEsRepository vulnerabilityEsRepository;

    /**
     * 资产传输对象与资产实体的映射
     */
    @Autowired
    private AssetMapper mapper;

    /**
     * 资产数据访问接口
     */
    @Resource
    private AssetRepository assetRepository;

    /**
     * ip库数据访问接口
     */
    @Resource
    private IpLibraryRepository ipLibraryRepository;

    /**
     * 资产子类型数据访问接口
     */
    @Resource
    private AssetSysTypeRepository assetSysTypeRepository;

    /**
     * 资产父类型数据访问接口
     */
    @Resource
    private AssetTypeRepository assetTypeRepository;

    /**
     * 部门数据访问接口
     */
    @Resource
    private DeptRepository deptRepository;

    /**
     * 资产标签数据访问接口
     */
    @Resource
    private AssetTagRepository assetTagRepository;

    /**
     * 确认资产数据访问接口
     */
    @Resource
    private AssetConfirmRepository assetConfirmRepository;

    /**
     * 资产仓库资产Mapper
     */
    @Resource
    private AssetWareHouseMapper assetWareHouseMapper;

    /**
     * 采集指令专用消息发送器
     */
    @Autowired
    private CmdSendBean cmdSendBean;

    /**
     * 采集指标维护中心
     */
    @Autowired
    private GatherCenter gatherCenter;

    /**
     * es查询template
     */
    @Autowired
    private ElasticsearchRestTemplate esRestTemplate;

    /**
     * 风险统计服务接口
     */
    @Autowired
    private CountVulnerabilityRepository vulnerabilityService;

    /**
     * 用户服务
     */
    @Autowired
    private UserService userService;

    /**
     * Asset 转 AssetEs Mapper
     */
    @Autowired
    private AssetTransformMapper assetTransformMapper;

    /**
     * assetsPortraitMapper
     */
    @Autowired
    private AssetsPortraitMapper assetsPortraitMapper;

    /**
     * 硬件信息服务接口
     */
    @Autowired
    private HardwareEsService hardwareEsService;

    /**
     * 盘信息服务接口
     */
    @Autowired
    private DiskPartitionEsService diskPartitionEsService;

    /**
     * 账号信息服务接口
     */
    @Autowired
    private AccountEsService accountEsService;

    /**
     * 已转软件接口服务
     */
    @Autowired
    private SoftwareEsService softwareEsService;

    /**
     * 网络配置接口服务
     */
    @Autowired
    private NetworkEsService networkEsService;

    /**
     * 系统进程接口服务
     */
    @Autowired
    private SystemProcessesEsService systemProcessesEsService;

    /**
     * 服务-服务接口
     */
    @Autowired
    private ServiceEsService serviceEsService;

    /**
     * 端口接口服务
     */
    @Autowired
    private PortEsService portEsService;

    /**
     * 路由接口服务
     */
    @Autowired
    private RouteEsService routeEsService;

    /**
     * 环境变量服务接口
     */
    @Autowired
    private EnvironmentEsService environmentEsService;

    /**
     * 中间件接口服务
     */
    @Autowired
    private MiddlewareEsService middlewareEsService;

    /**
     * 数据库接口服务
     */
    @Autowired
    private DBEsService dbEsService;

    /**
     * 远程扫描-端口与服务接口服务
     */
    @Autowired
    private ScanServicePortEsService scanServicePortEsService;

    /**
     * 远程扫描-中间件接口服务
     */
    @Autowired
    private ScanMiddlewareEsService scanMiddlewareEsService;

    /**
     * 远程扫描-数据库接口服务
     */
    @Autowired
    private ScanDBEsService scanDBEsService;

    /**
     * 导入模板表头
     */
    private static final String HEADER_IP = "*IP";

    private static final String HEADER_PORT = "端口";

    private static final String HEADER_CATEGORY = "类别";

    private static final String CATEGORY_HOST = "HOST";

    private static final String CATEGORY_APP = "APP";

    private static final String HEADER_NAME = "资产名称";

    private static final String HEADER_PARENT_TYPE = "*资产类型";

    private static final String HEADER_TYPE = "*资产子类型";

    private static final String HEADER_PARENT_TAG = "资产标签";

    private static final String HEADER_TAG = "资产子标签";

    private static final String HEADER_LOGIN = "登录采集";

    private static final String HEADER_PROTOCOL = "登录协议";

    private static final String HEADER_LOGIN_PORT = "登录端口";

    private static final String HEADER_ACCOUNT = "登录账号";

    private static final String HEADER_PWD = "登录密码";

    private static final String HEADER_SCAN = "远程扫描";

    private static final String HEADER_AGENT = "Agent";

    private static final String HEADER_DEPT = "*资产归属";

    private static final String RESULT_NULL = "为空";

    private static final String RESULT_ERROR = "错误";

    private static final String YES = "是";

    private static final String NO = "否";

    /**
     * 计算百分比
     *
     * @param x   值
     * @param sum 总数
     * @return 百分比
     */
    public static String getPercent(int x, int sum) {
        double d1 = x * 1.0;
        double d2 = sum * 1.0;
        NumberFormat percentInstance = NumberFormat.getPercentInstance();
        // 设置保留几位小数，这里设置的是保留两位小数
        percentInstance.setMinimumFractionDigits(2);
        return percentInstance.format(d1 / d2);
    }

    /**
     * 查询资产
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 资产列表
     */
    @Override
    public Object queryAll(AssetQueryCriteria criteria, Pageable pageable) {
        Page<Asset> assetPage = assetRepository.findAll((root, query, criteriaBuilder) -> {
            query.distinct(true);
            return QueryUtils.getPredicate(root, criteria, criteriaBuilder);
        }, pageable);
        Page<AssetDto> dtoPage = assetPage.map(e -> {
            AssetDto dto = mapper.toDto(e);
            dto.setPortNum(assetRepository.countByParentIdEquals(e.getId()));
            dto.setAssetConfig(JSON.parseObject(e.getConfig(), AssetConfig.class));
            if (null != dto.getDept()) {
                dto.setDeptHead(userService.getDeptHead(dto.getDept().getId()));
            }
            return dto;
        });
        if (dtoPage.getSize() == 0) {
            return null;
        }
        return dtoPage;
    }

    /**
     * 查询子资产
     *
     * @param pid      父id
     * @param pageable 分页参数
     * @return 资产列表
     */
    @Override
    public Object queryChild(Long pid, Pageable pageable) {
        Asset parent = new Asset();
        parent.setId(pid);
        Page<Asset> assetPage = assetRepository.findAllByParentEquals(parent, pageable);
        return assetPage.map(e -> {
            AssetDto dto = mapper.toDto(e);
            if (null != dto.getDept()) {
                dto.setDeptHead(userService.getDeptHead(dto.getDept().getId()));
            }
            return dto;
        });
    }

    /**
     * 资产清单 新增
     *
     * @param assetDto 资产传输对象
     * @param isBatch  是否批量 批量则结果放到result 单个新增则抛异常
     */
    @HistoryAnnotation(isStart = true, type = "2")
    @Override
    public AssetDto create(AssetDto assetDto, boolean isBatch) {
        if (StringUtils.isEmpty(assetDto.getName())) {
            assetDto.setName((assetDto.getAssetCategory() == Asset.CATEGORY_HOST ? ASSETNAME_HOST : ASSETNAME_APP) +
                    assetDto.getIp());
        }
        assetDto.setLoginStatus(0);
        assetDto.setRiskLevel(Asset.RISK_SAFETY);
        assetDto.setAssetStatus(Asset.STATUS_OFFLINE);
        assetDto.setCompleteIp(RegexUtil.checkToComplete(assetDto.getIp(), true));
        boolean flag;
        //主机 端口查重
        if (assetDto.getAssetCategory() == Asset.CATEGORY_HOST) {
            flag = checkHostRepeat(assetDto.getCompleteIp());
        } else {
            flag = checkPortRepeat(assetDto.getCompleteIp(), assetDto.getPort());
        }
        if (flag) {
            Asset asset = mapper.toEntity(assetDto);
            //主机则判断后添加到ip库
            if (asset.getAssetCategory() == Asset.CATEGORY_HOST) {
                //资产采集配置信息
                AssetConfig config = assetDto.getAssetConfig();
                if (config.getGatherType().contains(DetectConstant.GATHER_TYPE_LOGIN)) {
                    encryptPwd(config);
                }
                asset.setConfig(JSON.toJSONString(config));
                addIpLibrary(assetDto);
            } else {
                Asset parent = null;
                List<Asset> assetList = assetRepository.queryAssetByCompleteIpEqualsAndAssetCategoryEquals(
                        assetDto.getCompleteIp(), Asset.CATEGORY_HOST);
                if (!assetList.isEmpty()) {
                    parent = assetList.get(0);
                }
                if (parent == null) {
                    if (isBatch) {
                        assetDto.setResult("请先录入主机资产");
                        return assetDto;
                    } else {
                        throw new BadRequestException("请先录入主机资产");
                    }
                }
                asset.setParent(parent);
            }
            assetRepository.save(asset);
        } else {
            if (isBatch) {
                assetDto.setResult("资产已存在");
                return assetDto;
            } else {
                throw new BadRequestException("添加失败，资产已存在");
            }
        }
        return assetDto;
    }

    /**
     * 资产密码加密
     *
     * @param config 资产采集配置
     */
    private void encryptPwd(AssetConfig config) {
        try {
            //密码加密
            if (config.getLogin() != null && !StringUtils.isEmpty(config.getLogin().getPwd())) {
                config.getLogin().setPwd(EncryptUtils.desEncrypt(config.getLogin().getPwd()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 确认资产 录入资产
     *
     * @param assetDto 资产传输对象
     * @param isBatch  是否批量 批量则结果放到result 单个新增则抛异常
     */
    @HistoryAnnotation(isStart = true, type = "2")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public AssetDto createByAssetConfirm(AssetDto assetDto, boolean isBatch) {
        if (StringUtils.isEmpty(assetDto.getName())) {
            assetDto.setName(ASSETNAME_HOST + assetDto.getIp());
        }
        assetDto.setCompleteIp(RegexUtil.checkToComplete(assetDto.getIp(), true));
        assetDto.setLoginStatus(0);
        assetDto.setRiskLevel(Asset.RISK_SAFETY);
        assetDto.setAssetStatus(Asset.STATUS_SURVIVE);
        Asset newAsset = mapper.toEntity(assetDto);
        //资产采集配置信息
        AssetConfig config = assetDto.getAssetConfig();
        if (config.getGatherType().contains(DetectConstant.GATHER_TYPE_LOGIN)) {
            encryptPwd(config);
        }
        newAsset.setConfig(JSON.toJSONString(config));
        //无则新增
        List<Asset> assetList = assetRepository.queryAssetByCompleteIpEqualsAndAssetCategoryEquals(
                assetDto.getCompleteIp(), Asset.CATEGORY_HOST);
        if (assetList.isEmpty()) {
            addChildAsset(assetRepository.save(newAsset));
            addIpLibrary(assetDto);
        } else {
            if (isBatch) {
                assetDto.setResult("已存在资产");
                return assetDto;
            } else {
                throw new BadRequestException("已存在资产");
            }
        }
        //删除确认资产
        assetConfirmRepository.deleteByCompleteIp(assetDto.getCompleteIp());
        return assetDto;
    }

    /**
     * 根据父资产添加子资产
     *
     * @param parent 父资产
     */
    private void addChildAsset(Asset parent) {
        List<AssetConfirm> list = assetConfirmRepository.queryAssetConfirmByCompleteIpEqualsAndAssetCategoryEquals(
                parent.getCompleteIp(), Asset.CATEGORY_PORT);
        List<Asset> assetList = new ArrayList<>();
        list.forEach(assetConfirm -> {
            Asset asset = new Asset();
            asset.setName(ASSETNAME_APP + assetConfirm.getIp());
            asset.setLoginStatus(0);
            asset.setRiskLevel(Asset.RISK_SAFETY);
            asset.setAssetCategory(Asset.CATEGORY_PORT);
            asset.setIp(assetConfirm.getIp());
            asset.setCompleteIp(assetConfirm.getCompleteIp());
            asset.setPort(assetConfirm.getPort());
            asset.setAssetStatus(assetConfirm.getOnline() ? Asset.STATUS_SURVIVE : Asset.STATUS_OFFLINE);
            asset.setParent(parent);
            asset.setDept(parent.getDept());
            assetList.add(asset);
        });
        assetRepository.saveAll(assetList);
    }

    /**
     * 复制属性
     *
     * @param newExtend 新扩展属性
     * @param oldExtend 旧扩展属性
     */
    private void copyExtend(AssetExtend newExtend, AssetExtend oldExtend) {
        newExtend.setHostName(oldExtend.getHostName());
        newExtend.setUuid(oldExtend.getUuid());
        newExtend.setLocation(oldExtend.getLocation());
        newExtend.setLastDetectTime(oldExtend.getLastDetectTime());
        newExtend.setServer(oldExtend.getServer());
        newExtend.setServerComponent(oldExtend.getServerComponent());
        newExtend.setJson(oldExtend.getJson());
        newExtend.setType(oldExtend.getType());
        newExtend.setState(oldExtend.getState());
        newExtend.setReason(oldExtend.getReason());
        newExtend.setConf(oldExtend.getConf());
        newExtend.setName(oldExtend.getName());
        newExtend.setProduct(oldExtend.getProduct());
        newExtend.setVersion(oldExtend.getVersion());
        newExtend.setBanner(oldExtend.getBanner());
        newExtend.setExtrainfo(oldExtend.getExtrainfo());
        newExtend.setCpe(oldExtend.getCpe());
    }

    /**
     * 添加到ip库
     *
     * @param assetDto 资产数据传输对象
     */
    private void addIpLibrary(AssetDto assetDto) {
        String ip = assetDto.getIp();
        if (ipLibraryRepository.countByCompleteIpEquals(RegexUtil.checkToComplete(ip, true)) == 0) {
            IpLibrary ipLibrary = new IpLibrary();
            ipLibrary.setIp(ip);
            ipLibrary.setCompleteIp(RegexUtil.checkToComplete(assetDto.getIp(), true));
            ipLibrary.setDept(assetDto.getDept());
            ipLibraryRepository.save(ipLibrary);
            log.info("添加到ip库：" + ip);
        }
    }

    /**
     * 批量新增资产
     *
     * @param file      资产传输对象
     * @param fromAsset 判断入口是否为资产清单
     * @return 资产传输对象
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Object createBatch(MultipartFile file, boolean fromAsset) {
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray = new ExcelResolve().readExcel(file, 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        ImportResultDto importResultDto = new ImportResultDto();
        importResultDto.getResult().setSum(jsonArray.size());
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        List<AssetDto> assetDtoList = new ArrayList<>();
        if (jsonArray.isEmpty()) {
            log.warn("excel为空");
            return assetDtoList;
        }
        flag:
        for (Object obj : jsonArray) {
            Map map = objectMapper.convertValue(obj, Map.class);
            if (!map.containsKey("登录采集")) {
                throw new BadRequestException("模板错误");
            }
            AssetDto assetDto = mapToAssetDto(map);
            if (null != assetDto.getResult()) {
                assetDtoList.add(assetDto);
                fail.getAndIncrement();
                continue;
            }
            //ip格式校验
            String completeIp = RegexUtil.checkToComplete(assetDto.getIp(), false);
            if (StringUtils.isEmpty(completeIp)) {
                assetDto.setResult(HEADER_IP + RESULT_ERROR);
                assetDtoList.add(assetDto);
                fail.getAndIncrement();
                continue;
            } else {
                assetDto.setCompleteIp(completeIp);
            }
            //父类型
            AssetType assetType = assetTypeRepository.findAssetTypeByNameEquals(assetDto.getParentTypeName());
            if (null != assetType) {
                assetDto.setAssetType(assetType);
            } else {
                assetDto.setResult(HEADER_PARENT_TYPE + RESULT_ERROR);
                assetDtoList.add(assetDto);
                fail.getAndIncrement();
                continue;
            }
            //子类型
            AssetSysType assetSysType = assetSysTypeRepository.
                    findAssetSysTypeByNameEqualsAndAssetTypeEquals(assetDto.getTypeName(), assetType);
            if (null != assetSysType) {
                assetDto.setAssetSysType(assetSysType);
            } else {
                assetDto.setResult(HEADER_TYPE + RESULT_ERROR);
                assetDtoList.add(assetDto);
                fail.getAndIncrement();
                continue;
            }
            //端口校验
            if (assetDto.getAssetCategory() == Asset.CATEGORY_PORT &&
                    (1 > assetDto.getPort() || assetDto.getPort() > 65535)) {
                assetDto.setResult(HEADER_PORT + RESULT_ERROR);
                assetDtoList.add(assetDto);
                fail.getAndIncrement();
                continue;
            }
            //登录端口校验
            if (assetDto.getAssetConfig().getGatherType().contains(DetectConstant.GATHER_TYPE_LOGIN) &&
                    (1 > assetDto.getAssetConfig().getLogin().getPort() ||
                            assetDto.getAssetConfig().getLogin().getPort() > 65535)) {
                assetDto.setResult(HEADER_LOGIN_PORT + RESULT_ERROR);
                assetDtoList.add(assetDto);
                fail.getAndIncrement();
                continue;
            }
            //部门校验
            if (null != assetDto.getDeptName()) {
                List<Dept> depts = deptRepository.findAllByName(assetDto.getDeptName());
                if (depts.isEmpty()) {
                    assetDto.setResult(HEADER_DEPT + RESULT_ERROR);
                    assetDtoList.add(assetDto);
                    fail.getAndIncrement();
                    continue;
                } else {
                    assetDto.setDept(depts.get(0));
                }
            }
            //子标签校验
            if (null != assetDto.getTagName()) {
                String[] tags = StringUtils.split(assetDto.getTagName(), RegexUtil.SEPARATOR_COMMA);
                Set<AssetTag> tagList = new HashSet<>();
                for (String o : tags) {
                    AssetTag tag = assetTagRepository.findAssetTagByNameEquals(o);
                    if (null == tag || tag.getParent() == null) {
                        assetDto.setResult(HEADER_TAG + o + RESULT_ERROR);
                        assetDtoList.add(assetDto);
                        fail.getAndIncrement();
                        continue flag;
                    } else {
                        tagList.add(tag);
                    }
                }
                assetDto.setAssetTags(tagList);
            }
            AssetDto newAssetDto;
            //判断新增入口
            if (fromAsset) {
                newAssetDto = create(assetDto, true);
            } else {
                newAssetDto = createByAssetConfirm(assetDto, true);
            }
            if (null == newAssetDto.getResult() || "".equals(newAssetDto.getResult())) {
                success.getAndIncrement();
                newAssetDto.setResult("添加成功");
            } else {
                fail.getAndIncrement();
            }
            assetDtoList.add(newAssetDto);
        }
        importResultDto.getResult().setSuccess(success.get());
        importResultDto.getResult().setFail(fail.get());
        importResultDto.setObjectList(assetDtoList);
        return importResultDto;
    }

    /**
     * map转资产数据传输对象并判空
     *
     * @param map map
     * @return dto
     */
    private AssetDto mapToAssetDto(Map map) {
        AssetDto assetDto = new AssetDto();
        if (!StringUtils.isEmpty(map.get(HEADER_CATEGORY).toString())) {
            String category = map.get(HEADER_CATEGORY).toString();
            if (CATEGORY_HOST.equals(category)) {
                assetDto.setAssetCategory(Asset.CATEGORY_HOST);
            } else if (CATEGORY_APP.equals(category)) {
                assetDto.setAssetCategory(Asset.CATEGORY_PORT);
                //端口
                if (!StringUtils.isEmpty(map.get(HEADER_PORT).toString())) {
                    assetDto.setPort(Integer.parseInt(map.get(HEADER_PORT).toString()));
                } else {
                    assetDto.setResult(HEADER_PORT + RESULT_NULL);
                }
            }
        } else {
            assetDto.setResult(HEADER_CATEGORY + RESULT_NULL);
        }
        //ip
        if (!StringUtils.isEmpty(map.get(HEADER_IP).toString())) {
            assetDto.setIp(map.get(HEADER_IP).toString());
        } else {
            assetDto.setResult(HEADER_IP + RESULT_NULL);
        }
        //名称
        if (!StringUtils.isEmpty(map.get(HEADER_NAME).toString())) {
            assetDto.setName(map.get(HEADER_NAME).toString());
        }
        //父类型
        if (!StringUtils.isEmpty(map.get(HEADER_PARENT_TYPE).toString())) {
            assetDto.setParentTypeName(map.get(HEADER_PARENT_TYPE).toString());
        } else {
            assetDto.setResult(HEADER_PARENT_TYPE + RESULT_NULL);
        }
        //子类型
        if (!StringUtils.isEmpty(map.get(HEADER_TYPE).toString())) {
            assetDto.setTypeName(map.get(HEADER_TYPE).toString());
        } else {
            assetDto.setResult(HEADER_TYPE + RESULT_NULL);
        }
        //父标签
        if (!StringUtils.isEmpty(map.get(HEADER_PARENT_TAG).toString())) {
            assetDto.setParentTagName(map.get(HEADER_PARENT_TAG).toString());
        }
        //子标签
        if (!StringUtils.isEmpty(map.get(HEADER_TAG).toString())) {
            assetDto.setTagName(map.get(HEADER_TAG).toString());
        }
        //部门
        if (!StringUtils.isEmpty(map.get(HEADER_DEPT).toString())) {
            assetDto.setDeptName(map.get(HEADER_DEPT).toString());
        } else {
            assetDto.setResult(HEADER_DEPT + RESULT_NULL);
        }
        //采集配置
        AssetConfig config = new AssetConfig();
        List<Integer> gatherTypeList = new ArrayList<>();
        //登录采集
        if (!StringUtils.isEmpty(map.get(HEADER_LOGIN).toString()) && YES.equals(map.get(HEADER_LOGIN).toString())) {
            gatherTypeList.add(DetectConstant.GATHER_TYPE_LOGIN);
            LoginGather loginGather = new LoginGather();
            //登录协议
            if (!StringUtils.isEmpty(map.get(HEADER_PROTOCOL).toString()) &&
                    Arrays.stream(PROTOCOLS).anyMatch(e -> e.equals(map.get(HEADER_PROTOCOL).toString()))) {
                loginGather.setProtocol(map.get(HEADER_PROTOCOL).toString());
            } else {
                assetDto.setResult(HEADER_PROTOCOL + RESULT_ERROR);
            }
            //登录端口
            if (!StringUtils.isEmpty(map.get(HEADER_LOGIN_PORT).toString())) {
                loginGather.setPort(Integer.parseInt(map.get(HEADER_LOGIN_PORT).toString()));
            } else {
                assetDto.setResult(HEADER_LOGIN_PORT + RESULT_NULL);
            }
            //登录账号
            if (!StringUtils.isEmpty(map.get(HEADER_ACCOUNT).toString())) {
                loginGather.setAccount(map.get(HEADER_ACCOUNT).toString());
            } else {
                assetDto.setResult(HEADER_ACCOUNT + RESULT_NULL);
            }
            //登录密码
            if (!StringUtils.isEmpty(map.get(HEADER_PWD).toString())) {
                loginGather.setPwd(map.get(HEADER_PWD).toString());
            } else {
                assetDto.setResult(HEADER_PWD + RESULT_NULL);
            }
            config.setLogin(loginGather);
        }
        //远程扫描
        if (!StringUtils.isEmpty(map.get(HEADER_SCAN).toString()) && YES.equals(map.get(HEADER_SCAN).toString())) {
            gatherTypeList.add(DetectConstant.GATHER_TYPE_SCAN);
            config.setRemoteScan(new RemoteScan());
        }
        config.setGatherType(gatherTypeList);
        assetDto.setAssetConfig(config);
        return assetDto;
    }

    /**
     * 查询ip相关资产
     *
     * @param assetIp 资产ip
     * @return true 无 false 有
     */
    @Override
    public List<AssetDto> queryByCompleteIp(String assetIp) {
        List<Asset> assetList = assetRepository.queryAssetsByCompleteIpEquals(RegexUtil.checkToComplete(assetIp, true));
        List<AssetDto> list = mapper.toDto(assetList.stream().filter(asset -> asset.getAssetCategory() == 1).collect(Collectors.toList()));
        List<Integer> portList = new ArrayList<>();
        assetList.forEach(asset -> {
            if (asset.getAssetCategory() == 2) {
                portList.add(asset.getPort());
            }
        });
        if (!list.isEmpty()) {
            list.get(0).setPortList(portList);
        }
        return list;
    }

    /**
     * 根据id删除
     *
     * @param id
     */
    @HistoryAnnotation(isStart = true, type = "3")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeById(Long id) {
        Asset asset = assetRepository.findById(id).orElse(null);
        if (asset == null) {
            return;
        }
        if (asset.getLoginStatus() == 1) {
            throw new BadRequestException("删除失败，当前资产正常执行登录测试");
        }
        assetRepository.deleteByCompleteIpEquals(asset.getCompleteIp());
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeBatch(Set<Long> ids, Boolean delAll) {
        if (delAll) {
            assetRepository.findAll().forEach(e -> {
                if (e.getAssetCategory() == 1) {
                    removeById(e.getId());
                }
            });
        } else {
            ids.forEach(this::removeById);
        }
    }

    /**
     * 根据id修改
     *
     * @param assetDtos 资产传输对象
     */
//    @HistoryAnnotation(isStart = true, type = "1")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateById(List<AssetDto> assetDtos) {
        for (AssetDto assetDto : assetDtos) {
            Asset asset = assetRepository.findById(assetDto.getId()).orElse(null);
            if (null == asset) {
                log.info("当前资产不存在");
                continue;
            }
            asset.setName(assetDto.getName());
            asset.setDept(assetDto.getDept());
            asset.setAssetType(assetDto.getAssetType());
            asset.setAssetSysType(assetDto.getAssetSysType());
            asset.setAssetTags(assetDto.getAssetTags());
            if (asset.getAssetCategory() == Asset.CATEGORY_HOST) {
                //更新ip库部门
                IpLibrary ipLibrary = ipLibraryRepository.findByCompleteIpEquals(asset.getCompleteIp());
                ipLibrary.setDept(asset.getDept());
                ipLibraryRepository.save(ipLibrary);
                //dto资产采集配置
                AssetConfig config = assetDto.getAssetConfig();
                //原资产采集配置
                AssetConfig oldConfig = JSONObject.parseObject(asset.getConfig(), AssetConfig.class);
                if (config.getGatherType().contains(DetectConstant.GATHER_TYPE_LOGIN) &&
                        oldConfig.getGatherType().contains(DetectConstant.GATHER_TYPE_LOGIN) &&
                        !config.getLogin().getPwd().equals(oldConfig.getLogin().getPwd())) {
                    encryptPwd(config);
                }
                asset.setConfig(JSON.toJSONString(config));
            } else {
                if (assetDto.getWebAddress() != null &&
                        !assetDto.getAssetExtend().getWebsite().matches(RegexUtil.URL) &&
                        !assetDto.getAssetExtend().getWebsite().matches(RegexUtil.WWW)) {
                    throw new BadRequestException("网址格式错误");
                }
                asset.setWebAddress(assetDto.getWebAddress());
            }
            assetRepository.save(asset);
        }
    }

    /**
     * 登录测试
     *
     * @param ids
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Object loginTest(Set<Long> ids, boolean all) {
        if (all) {
            assetRepository.findAll().forEach(e -> login(e.getId()));
        } else {
            ids.forEach(this::login);
        }
        return "";
    }

    /**
     * 登录测试
     *
     * @param id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void login(Long id) {
        //执行中 目前为成功
        Asset asset = assetRepository.findById(id).orElse(null);
        if (null != asset) {
            if (LogTest(asset)) {
                assetRepository.updateLoginStatusById(id, 1);
            } else {
                assetRepository.updateLoginStatusById(id, 3);
            }
        }
    }

    @Override
    public Asset getHostAssetByIp(String ip) {
        List<Asset> list = assetRepository.findAllByIpAndAssetCategory(ip, Asset.CATEGORY_HOST);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public Asset getWebAssetByIpAndPort(String ip, Integer port) {
        List<Asset> list = assetRepository.findAllByIpAndAssetCategoryAndPort(ip, Asset.CATEGORY_PORT, port);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public Asset getWebAssetByWebSite(String webSite) {
        List<Asset> list = assetRepository.findAllByWebSite(webSite);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 根据名称查重
     *
     * @param name 名称
     * @return true不重复 false重复
     */
    @Override
    public boolean checkName(String name) {
        return assetRepository.countByNameEquals(name) == 0;
    }

    @Override
    public List<Map<String, Long>> countAssetUpdate(Integer dateType, Integer assetCategory) {
        List<Map<String, Long>> listMap = new ArrayList<>();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        switch (dateType) {
            case AssetStatisticsController.DATE_TYPE_DAY:
                calendar.add(Calendar.DATE, -1);
                break;
            case AssetStatisticsController.DATE_TYPE_WEEK:
                calendar.add(Calendar.DATE, -7);
                break;
            case AssetStatisticsController.DATE_TYPE_MONTH:
                calendar.add(Calendar.DATE, -30);
                break;
            case AssetStatisticsController.DATE_TYPE_HALF_YEAR:
                calendar.add(Calendar.DATE, -180);
                break;
            default:
                break;
        }
        if (AssetStatisticsController.DATE_TYPE_ALL != dateType) {
            RangeQueryBuilder updateDate = QueryBuilders.rangeQuery(FILED_NAME_UPDATE_TIME)
                    .gte(calendar.getTime())
                    .lte(date);
            boolQueryBuilder.must(updateDate);
        }
        boolQueryBuilder.must(QueryBuilders.termQuery(FILED_NAME_ASSET_CATEGORY, assetCategory));
        boolQueryBuilder.must(QueryBuilders.termQuery(FILED_NAME_SOURCE, HistoryAspect.HISTORY_TYPE_UPDATE));
        TermsAggregationBuilder agg = AggregationBuilders.terms(AGG_NAME_GROUP_BY_ASSET_ID).field(FILED_NAME_ASSET_ID).size(30);
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder().withAggregations(agg);
        Query query = queryBuilder.withQuery(boolQueryBuilder).build();
        SearchHits<HistoryEs> search = esRestTemplate.search(query, HistoryEs.class);
        AggregationsContainer<?> aggregations = search.getAggregations();
        if (aggregations != null) {
            Terms name = ((Aggregations) aggregations.aggregations()).get(AGG_NAME_GROUP_BY_ASSET_ID);
            for (Terms.Bucket bucket : name.getBuckets()) {
                long assetId = Long.parseLong(bucket.getKey().toString());
                Optional<Asset> assetOpt = assetRepository.findById(assetId);
                if (assetOpt.isPresent()) {
                    Map<String, Long> item = new HashMap<>();
                    if (Asset.CATEGORY_HOST == assetCategory) {
                        item.put(assetOpt.get().getIp(), bucket.getDocCount());
                    } else {
                        item.put(assetOpt.get().getIp() + Asset.IP_PORT_SEPARATE_STR + assetOpt.get().getPort(), bucket.getDocCount());
                    }
                    listMap.add(item);
                }
                if (listMap.size() >= 10) {
                    break;
                }
            }
        }
        return listMap;
    }

    @Override
    public Map<String, List<Map<String, Long>>> findAssetUpdate() {
        Map<String, List<Map<String, Long>>> listMap = new HashMap<>();
        getCount(listMap, 7);
        return listMap;
    }

    @Override
    public void updateRiskStatus(Long assetId) {
        Optional<Asset> assetOpt = assetRepository.findById(assetId);
        if (!assetOpt.isPresent()) {
            return;
        }
        Asset asset = assetOpt.get();
        PageRequest risk = PageRequest.of(0, 1, Sort.Direction.DESC, "riskLevel");
        List<Integer> handleStatus = Arrays.asList(VulnerabilityEs.HANDLE_STATUS_REPAIR, VulnerabilityEs.HANDLE_STATUS_IGNORED, VulnerabilityEs.HANDLE_STATUS_CLOSED);
        Page<VulnerabilityEs> page = vulnerabilityEsRepository.findAllByAssetIdAndLatestAndHandleStatusNotIn(String.valueOf(asset.getId()), true, handleStatus, risk);
        if (page.getContent().isEmpty()) {
            asset.setRiskLevel(Asset.RISK_SAFETY);
        } else {
            asset.setRiskLevel(page.getContent().get(0).getRiskLevel());
        }
        assetRepository.save(asset);
    }

    private void getCount(Map<String, List<Map<String, Long>>> listMap, Integer days) {
        List<Map<String, Long>> createMap = new ArrayList<>();
        List<Map<String, Long>> deleteMap = new ArrayList<>();
        List<Map<String, Long>> updateMap = new ArrayList<>();
        listMap.put(HistoryAspect.HISTORY_TYPE_CREATE, createMap);
        listMap.put(HistoryAspect.HISTORY_TYPE_UPDATE, updateMap);
        listMap.put(HistoryAspect.HISTORY_TYPE_DELETE, deleteMap);
        Calendar endTimeCal = Calendar.getInstance();
        DateUtils.setDayEndSec(endTimeCal);
        long endTime = endTimeCal.getTimeInMillis();
        long startTime = endTime - days * DateUtils.HOUR_TO_DAY * DateUtils.MINUTE_TO_HOUR * DateUtils.SEC_TO_MINUTE * DateUtils.MILLISECOND_TO_SEC + DateUtils.MILLISECOND_TO_SEC;
        DateHistogramAggregationBuilder timeAgg = AggregationBuilders.dateHistogram(AGG_NAME_GROUP_BY_DATE).field(FILED_NAME_UPDATE_TIME).fixedInterval(DateHistogramInterval.DAY).format(DateUtils.MM_DD_STR).extendedBounds(new LongBounds(startTime, endTime)).timeZone(ZoneId.systemDefault());
        final TermsAggregationBuilder sourceAgg = AggregationBuilders.terms(AGG_NAME_GROUP_BY_SOURCE).field(FILED_NAME_SOURCE);
        timeAgg.subAggregation(sourceAgg);
        RangeQueryBuilder dateRange = QueryBuilders.rangeQuery(FILED_NAME_UPDATE_TIME).gte(startTime).lte(endTime).timeZone(TimeZone.getDefault().getID());
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder().withQuery(dateRange).withAggregations(timeAgg).withPageable(PageRequest.of(0, 1));
        SearchHits<HistoryEs> search = esRestTemplate.search(queryBuilder.build(), HistoryEs.class);
        AggregationsContainer<?> aggregations = search.getAggregations();
        if (aggregations != null) {
            Aggregation groupByDate = ((Aggregations) aggregations.aggregations()).get(AGG_NAME_GROUP_BY_DATE);
            if (groupByDate instanceof ParsedDateHistogram) {
                List<? extends Histogram.Bucket> buckets = ((ParsedDateHistogram) groupByDate).getBuckets();
                for (Histogram.Bucket bucket : buckets) {
                    Map<String, Long> updateItem = new HashMap<>();
                    Map<String, Long> createItem = new HashMap<>();
                    Map<String, Long> deleteItem = new HashMap<>();
                    updateItem.put(bucket.getKeyAsString(), 0L);
                    createItem.put(bucket.getKeyAsString(), 0L);
                    deleteItem.put(bucket.getKeyAsString(), 0L);
                    createMap.add(createItem);
                    updateMap.add(updateItem);
                    deleteMap.add(deleteItem);
                    ParsedStringTerms sourceResult = bucket.getAggregations().get(AGG_NAME_GROUP_BY_SOURCE);
                    if (!sourceResult.getBuckets().isEmpty()) {
                        for (Terms.Bucket sourceBucket : sourceResult.getBuckets()) {
                            String key = (String) sourceBucket.getKey();
                            System.out.println(key);
                            switch (key) {
                                case HistoryAspect.HISTORY_TYPE_CREATE:
                                    createItem.put(bucket.getKeyAsString(), sourceBucket.getDocCount());
                                    break;
                                case HistoryAspect.HISTORY_TYPE_DELETE:
                                    deleteItem.put(bucket.getKeyAsString(), sourceBucket.getDocCount());
                                    break;
                                case HistoryAspect.HISTORY_TYPE_UPDATE:
                                    updateItem.put(bucket.getKeyAsString(), sourceBucket.getDocCount());
                                    break;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<Map<Object, Object>> findAssetSum() {
        List<Map<Object, Object>> assetNum = assetRepository.findAssetSum();
        HashMap<Object, Object> map = new HashMap<>();
        Query querySum = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("latest", true))
                        .mustNot(QueryBuilders.matchQuery("handleStatus", VulnerabilityEs.HANDLE_STATUS_CLOSED)))
                .build();
        long countSum = esRestTemplate.count(querySum, VulnerabilityEs.class);
        long countHost = getCountVul(VulnerabilityEs.CATEGORY_HOST);
        long countWeb = getCountVul(VulnerabilityEs.CATEGORY_WEB);
        map.put("levelSum", countSum);
        map.put("host", countHost);
        map.put("web", countWeb);
        assetNum.add(map);
        return assetNum;
    }

    /**
     * 获取主机,应用风险
     *
     * @param categoryWeb
     * @return 数量集
     */
    public long getCountVul(int categoryWeb) {
        Query queryWeb = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("vulCategory", categoryWeb))
                        .must(QueryBuilders.matchQuery("latest", true))
                        .mustNot(QueryBuilders.matchQuery("handleStatus", VulnerabilityEs.HANDLE_STATUS_CLOSED))
                ).build();
        return esRestTemplate.count(queryWeb, VulnerabilityEs.class);
    }

    @Override
    public List<Map<String, Object>> findHighLeak() {
        List<Map<String, Object>> listMap = new ArrayList<>();
        TermsAggregationBuilder resultAgg = AggregationBuilders.terms("resultAgg").field("name").size(5);
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("latest", true)))
                .withAggregations(resultAgg).build();
        SearchHits<VulnerabilityEs> search = esRestTemplate.search(query, VulnerabilityEs.class);
        Aggregations aggregations = (Aggregations) Objects.requireNonNull(search.getAggregations()).aggregations();
        Terms agg = aggregations.get("resultAgg");
        List<Terms.Bucket> buckets = (List<Terms.Bucket>) agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", bucket.getKey().toString());
            map.put("num", Integer.parseInt(String.valueOf(bucket.getDocCount())));
            listMap.add(map);
        }
        return listMap;
    }

    @Override
    public Map<String, List<Map<String, Long>>> findAssetUpdateTrend() {
        Map<String, List<Map<String, Long>>> listMap = new HashMap<>();
        getCount(listMap, 15);
        return listMap;
    }

    @Override
    public List<Map<Object, Object>> findRealtimeAlarm() {
        List<Map<Object, Object>> listMap = new ArrayList<>();
        int size = 10;
        int page = 0;
        SearchHits<VulnerabilityEs> search = getSearchHits(size, page);
        List<SearchHit<VulnerabilityEs>> searchHits = search.getSearchHits();
        for (SearchHit<VulnerabilityEs> searchHit : searchHits) {
            VulnerabilityEs content = searchHit.getContent();
            Map<Object, Object> map = new HashMap<>();
            map.put("level", getLevel(content.getRiskLevel()));
            map.put("name", content.getName());
            map.put("status", content.getHandleStatus());
            map.put("ip", content.getIp());
            map.put("time", content.getFindTime());
            listMap.add(map);
        }
        return listMap;
    }

    /**
     * 字典转换
     *
     * @param level 风险等级
     * @return 风险名称
     */
    public String getLevel(int level) {
        if (level == Asset.RISK_LOW) {
            return Asset.RISK_LOW_NAME;
        } else if (level == Asset.RISK_MIDDLE) {
            return Asset.RISK_MIDDLE_NAME;
        } else if (level == Asset.RISK_HIGH) {
            return Asset.RISK_HIGH_NAME;
        } else if (level == Asset.RISK_SAFETY) {
            return Asset.RISK_SAFETY_NAME;
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> findRisk() {
        List<Map<String, Object>> maps = new ArrayList<>();
        List<Map<String, Object>> mapList = assetRepository.countRisk();
        long count = assetRepository.count();
        for (Map<String, Object> stringStringMap : mapList) {
            for (Map.Entry<String, Object> entry : stringStringMap.entrySet()) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", entry.getKey());
                map.put("num", entry.getValue());
                map.put("per", getPercent(Integer.parseInt(entry.getValue().toString()), (int) count));
                maps.add(map);
            }
        }
        return maps;
    }

    @Override
    public List<Map<String, Object>> findAssetNum() {
        List<Map<String, Object>> maps = new ArrayList<>();
        List<Map<String, Object>> assetNum = assetRepository.findAssetNum();
        List<Map<String, Object>> growthNum = assetRepository.findGrowth();
        for (Map<String, Object> objectObjectMap : assetNum) {
            for (Map.Entry<String, Object> objectObjectEntry : objectObjectMap.entrySet()) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("name", objectObjectEntry.getKey());
                map.put("num", objectObjectEntry.getValue());
                maps.add(map);
            }
        }
        for (Map<String, Object> map : growthNum) {
            for (Map.Entry<String, Object> stringObjectEntry : map.entrySet()) {
                for (Map<String, Object> stringObjectMap : maps) {
                    if (stringObjectEntry.getKey().equals("growthHost")) {
                        stringObjectMap.put("growth", stringObjectMap.get("growthHost") == null ? 0 : null);
                    }
                    if (stringObjectEntry.getKey().equals("growthPort")) {
                        stringObjectMap.put("growth", stringObjectMap.get("growthPort") == null ? 0 : null);
                    }
                    if (stringObjectEntry.getKey().equals("growthWeb")) {
                        stringObjectMap.put("growth", stringObjectMap.get("growthWeb") == null ? 0 : null);
                    }
                }
            }
        }
        return maps;
    }

    @Override
    public List<Map<String, Object>> findDeptNum() {
        List<Map<String, Object>> maps = new ArrayList<>();
        List<Map<Object, Object>> deptNum = assetRepository.findDeptNum();
        for (Map<Object, Object> objectObjectMap : deptNum) {
            HashMap<String, Object> map = new HashMap<>();
            for (Map.Entry<Object, Object> objectObjectEntry : objectObjectMap.entrySet()) {
                map.put(objectObjectEntry.getKey().toString(), objectObjectEntry.getValue());
            }
            maps.add(map);
        }
        return maps;
    }

    @Override
    public List<Map<String, Object>> findMap() {
        List<String> fileContext = null;
        try {
            fileContext = new FileUtils().getFileContext("coordinate.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Random random = new Random();
        List<Map<String, Object>> maps = new ArrayList<>();
        List<Map<String, Object>> mapList = assetRepository.findMap();
        for (int i = 0; i < mapList.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            Map<String, Object> map1 = mapList.get(i);
            for (Map.Entry<String, Object> entry : map1.entrySet()) {
                if ("ip".equals(entry.getKey())) {
                    map.put("ip", entry.getValue());
                }
                if ("level".equals(entry.getKey())) {
                    map.put("level", getLevel(Integer.parseInt(entry.getValue().toString())));
                }
                if ("lat".equals(entry.getKey()) || "lon".equals(entry.getKey())) {
                    map.put("coordinate", fileContext.get(random.nextInt(fileContext.size())));
                }
            }
            maps.add(map);
        }
        return maps;
    }

    @Override
    public List<Map<String, Object>> findRiskTrend() {
        List<Map<String, Object>> maps = new ArrayList<>();
        List<Map<String, Object>> riskTrend = vulnerabilityService.findRiskTrend();
        for (Map<String, Object> objectObjectMap : riskTrend) {
            Map<String, Object> map = new HashMap<>();
            for (Map.Entry<String, Object> objectObjectEntry : objectObjectMap.entrySet()) {
                if ("click_date".equals(objectObjectEntry.getKey())) {
                    map.put(objectObjectEntry.getKey(), objectObjectEntry.getValue().toString().substring(6));
                    continue;
                }
                map.put(objectObjectEntry.getKey(), objectObjectEntry.getValue());
            }
            maps.add(map);
        }
        long countHigh = getCountVulLatest(3);
        long countMiddle = getCountVulLatest(2);
        long countLow = getCountVulLatest(1);
        Map<String, Object> objectHash = new HashMap<>();
        objectHash.put("低危", countLow);
        objectHash.put("中危", countMiddle);
        objectHash.put("高危", countHigh);
        objectHash.put("click_date", DateUtils.localDateTimeFormatyMd(LocalDateTime.now()).substring(6));
        maps.add(objectHash);
        return maps;

    }

    @Override
    public void updateAssetTag(Long assetId, AssetTag... assetTagList) {
        Set<AssetTag> tagSet = new HashSet<>();
        for (AssetTag assetTag : assetTagList) {
            if (assetTag != null){
                tagSet.add(assetTag);
            }
        }
        if (tagSet.isEmpty()){
            return;
        }
        Optional<Asset> assetOpt = assetRepository.findById(assetId);
        if (assetOpt.isPresent()) {
            Asset asset = assetOpt.get();
            Set<AssetTag> assetTags = asset.getAssetTags();
            if (assetTags != null){
                assetTags.addAll(tagSet);
            }else {
                asset.setAssetTags(tagSet);
            }
            assetRepository.save(asset);
        }
    }

    /**
     * 获取统计风险
     *
     * @param riskLevel 风险
     * @return 结果
     */
    public long getCountVulLatest(int riskLevel) {
        Query queryLow = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("latest", true))
                        .must(QueryBuilders.matchQuery("riskLevel", riskLevel))
                        .mustNot(QueryBuilders.matchQuery("handleStatus", VulnerabilityEs.HANDLE_STATUS_CLOSED)))
                .build();
        return esRestTemplate.count(queryLow, VulnerabilityEs.class);
    }

    /**
     * 获取es数据
     *
     * @param size 条数
     * @param page 页数
     * @return 结果集
     */
    private SearchHits<VulnerabilityEs> getSearchHits(Integer size, Integer page) {
        Query query = new NativeSearchQueryBuilder()
                .withSorts(SortBuilders.fieldSort("findTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(page, size)).build();
        return esRestTemplate.search(query, VulnerabilityEs.class);
    }

    /**
     * 取消
     *
     * @param id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancel(Long id) {
        //取消回到 未开始状态
        assetRepository.updateLoginStatusById(id, 0);

    }

    /**
     * 检查主机资产是否重复
     *
     * @param assetIp 资产ip
     * @return true 不重复 false 重复
     */
    private boolean checkHostRepeat(String assetIp) {
        String ip = RegexUtil.checkToComplete(assetIp, false);
        return assetRepository.queryAssetByCompleteIpEqualsAndAssetCategoryEquals(ip, Asset.CATEGORY_HOST).isEmpty();
    }

    /**
     * 检查应用资产是否重复
     *
     * @param assetIp 资产ip
     * @param port    端口
     * @return true 不重复 false 重复
     */
    private boolean checkPortRepeat(String assetIp, Integer port) {
        String ip = RegexUtil.checkToComplete(assetIp, false);
        return null == assetRepository.
                queryAssetByCompleteIpEqualsAndPortEqualsAndAssetCategoryEquals(ip, port, Asset.CATEGORY_PORT);
    }

    @Override
    public Asset findById(Long id) {
        return assetRepository.findById(id).orElse(null);
    }

    @Override
    public AssetPortraitVo findGatherById(Long assetId) {

        GatherAssetEs findAsset = null;
        Optional<Asset> assetOpt = assetRepository.findById(assetId);
        if (assetOpt.isPresent()) {
            findAsset = assetTransformMapper.toDto(assetOpt.get());
        }
        String name = assetOpt.get().getAssetType().getName();
        String assetTypeName = name == null ? "" : name;
        String assetSysTypeName = findAsset.getAssetSysTypeName() == null ? "" : findAsset.getAssetSysTypeName();
        if ("".equals(assetSysTypeName)) {
            findAsset.setMergeAssetType(assetTypeName);
        } else {
            findAsset.setMergeAssetType(assetTypeName + "-" + assetSysTypeName);
        }
        AssetPortraitVo assetPortraitVo = assetsPortraitMapper.toDto(findAsset);
        String config = assetOpt.get().getConfig();
        AssetConfig assetConfig = JSONObject.parseObject(config, AssetConfig.class);
        //登录采集
        if (assetConfig.getGatherType().contains(DetectConstant.GATHER_TYPE_LOGIN) || assetConfig.getGatherType().contains(DetectConstant.GATHER_TYPE_AGENT)) {
            assetPortraitVo.setLoginGatherHeader(AssetPortraitConstants.LOGIN_GATHER);
            //远程登录
        } else if (assetConfig.getGatherType().contains(DetectConstant.GATHER_TYPE_SCAN)) {
            assetPortraitVo.setRemoteScanHeader(AssetPortraitConstants.REMOTE_SCAN);
        }

        return assetPortraitVo;
    }

    @Override
    public List<String> getHeader(Long assetId) {
        List<String> list = new ArrayList<>();
        Asset asset = assetRepository.findById(assetId).orElse(null);
        if (null != asset) {
            AssetConfig assetConfig = JSONObject.parseObject(asset.getConfig(), AssetConfig.class);
            //登录采集
            if (assetConfig.getGatherType().contains(DetectConstant.GATHER_TYPE_LOGIN) || assetConfig.getGatherType().contains(DetectConstant.GATHER_TYPE_AGENT)) {
                list.add(AssetPortraitConstants.BASIC_INFORMATION);
                if (asset.getGatherAssetId() != null) {
                    //硬件信息
                    if (null != hardwareEsService.findListRecent(asset.getGatherAssetId(), null)) {
                        list.add(AssetPortraitConstants.GATHER_HARDWARE);
                    }
                    //磁盘信息
                    if (null != diskPartitionEsService.findListRecent(asset.getGatherAssetId(), null)) {
                        list.add(AssetPortraitConstants.GATHER_DISK_PARTITION);
                    }
                    //账号信息
                    if (null != accountEsService.findListRecent(asset.getGatherAssetId(), null)) {
                        list.add(AssetPortraitConstants.GATHER_ACCOUNT);
                    }
                    //已转软件
                    if (null != softwareEsService.findListRecent(asset.getGatherAssetId(), null)) {
                        list.add(AssetPortraitConstants.GATHER_SOFTWARE);
                    }
                    //网络配置
                    if (null != networkEsService.findListRecent(asset.getGatherAssetId(), null)) {
                        list.add(AssetPortraitConstants.GATHER_NETWORK);
                    }
                    // 系统进程
                    if (null != systemProcessesEsService.findListRecent(asset.getGatherAssetId(), null)) {
                        list.add(AssetPortraitConstants.GATHER_SYSTEM_PROCESSES);
                    }
                    // 服务
                    if (null != serviceEsService.findListRecent(asset.getGatherAssetId(), null)) {
                        list.add(AssetPortraitConstants.GATHER_SERVICE);
                    }
                    // 端口
                    if (null != portEsService.findListRecent(asset.getGatherAssetId(), null)) {
                        list.add(AssetPortraitConstants.GATHER_PORT);
                    }
                    // 路由
                    if (null != routeEsService.findListRecent(asset.getGatherAssetId(), null)) {
                        list.add(AssetPortraitConstants.GATHER_ROUTE);
                    }
                    // 环境变量
                    if (null != environmentEsService.findListRecent(asset.getGatherAssetId(), null)) {
                        list.add(AssetPortraitConstants.GATHER_ENVIRONMENT);
                    }
                    //中间件
                    if (null != middlewareEsService.findListRecent(asset.getGatherAssetId(), null)) {
                        list.add(AssetPortraitConstants.GATHER_MIDDLEWARE);
                    }
                    //数据库
                    if (null != dbEsService.findListRecent(asset.getGatherAssetId(), null)) {
                        list.add(AssetPortraitConstants.GATHER_DB);
                    }
                }
                //远程登录
            } else if (assetConfig.getGatherType().contains(DetectConstant.GATHER_TYPE_SCAN)) {
                list.add(AssetPortraitConstants.SCAN_BASICINFO);
                //服务与端口
                if (null != scanServicePortEsService.findListRecent(asset.getGatherAssetId(), null)) {
                    list.add(AssetPortraitConstants.SCAN_SERVICE_PORT);
                }
                //中间件
                if (null != scanMiddlewareEsService.findListRecent(asset.getGatherAssetId(), null)) {
                    list.add(AssetPortraitConstants.SCAN_MIDDLEWARE);
                }
                //数据库
                if (null != scanDBEsService.findListRecent(asset.getGatherAssetId(), null)) {
                    list.add(AssetPortraitConstants.SCAN_DB);
                }
            }
            return list;
        }
        return null;
    }

    @Override
    public void updateAssetStatus(Long assetId, Integer assetStatus) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Asset:%d, new asset status:%d", assetId, assetStatus));
        }
        assetRepository.updateAssetStatus(assetId, assetStatus);
    }

    @Override
    public Page<AssetWarehouseDto> findList(AssetQueryCriteria asset, Pageable pageable) {
        List<AssetWarehouseDto> list = assetRepository.findList(asset, pageable)
                .stream().map(assetWareHouseMapper::toDto).collect(Collectors.toList());
        Integer listCount = assetRepository.findListCount(asset);
        for (AssetWarehouseDto assetWarehouseDto : list) {
            Pageable childPageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_PAGE_SIZE);
            asset.setIp(assetWarehouseDto.getIp());
            assetWarehouseDto.setChildAssets(findChildList(asset, childPageable));
            AssetType assetType = assetWarehouseDto.getAssetType();
            AssetSysType assetSysType = assetWarehouseDto.getAssetSysType();
            String assetTypeName = assetType == null ? "" : assetType.getName();
            String assetSysTypeName = assetSysType == null ? "" : assetSysType.getName();
            if ("".equals(assetSysTypeName)) {
                assetWarehouseDto.setMergeAssetType(assetTypeName);
            } else {
                assetWarehouseDto.setMergeAssetType(assetTypeName + "-" + assetSysTypeName);
            }
            if (assetWarehouseDto.getChildAssets().getContent().isEmpty()) {
                assetWarehouseDto.setAllRiskLevel(assetWarehouseDto.getRiskLevel() + RISK_SEPARATION + Asset.RISK_SAFETY);
            } else {
                assetWarehouseDto.setAllRiskLevel(assetWarehouseDto.getRiskLevel() + RISK_SEPARATION + assetRepository.findChildMaxRisk(asset));
            }
        }
        return new PageImpl<>(list, pageable, listCount);
    }

    @Override
    public Page<Asset> findChildList(AssetQueryCriteria asset, Pageable pageable) {
        asset.setAssetCategory(Asset.CATEGORY_PORT);
        return assetRepository.findAll((root, query, criteriaBuilder) ->
                QueryUtils.getPredicate(root, asset, criteriaBuilder), pageable);
    }

    @Override
    public Map<String, Long> assetCount() {
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("safety", assetRepository.countAllByRiskLevelIn(Collections.singletonList(Asset.RISK_SAFETY)));
        result.put("lowRisk", assetRepository.countAllByRiskLevelIn(Collections.singletonList(Asset.RISK_LOW)));
        result.put("middleRisk", assetRepository.countAllByRiskLevelIn(Collections.singletonList(Asset.RISK_MIDDLE)));
        result.put("highRisk", assetRepository.countAllByRiskLevelIn(Collections.singletonList(Asset.RISK_HIGH)));
        result.put("survive", assetRepository.countAllByAssetStatusIn(Collections.singletonList(Asset.STATUS_SURVIVE)));
        result.put("downLine", assetRepository.countAllByAssetStatusIn(Collections.singletonList(Asset.STATUS_OFFLINE)));
        result.put("abnormal", assetRepository.countAllByAssetStatusIn(Collections.singletonList(Asset.STATUS_ABNORMAL)));
        result.put("all", assetRepository.count());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGatherAssetIdById(Long id, String gatherAssetId) {
        assetRepository.updateGatherAssetIdById(id, gatherAssetId);
    }

    /**
     * 登陆测试
     *
     * @param asset 资产
     * @return 结果集
     */
    private boolean LogTest(Asset asset) {
        List<GatherDetail> details = null;
        String type, sysType;
        //TODO Shijh先这样写死后期再做修改
        try {
            // 资产一级分类
            type = asset.getAssetType().getId().toString();
            // 资产二级分类
            sysType = asset.getAssetSysType().getId().toString();
            if (!"2".equals(sysType) || !"1".equals(type)) {
                log.error("Incorrect asset type id = " + asset.getId());
                return false;
            }
            details = gatherCenter.getIndicatorsByMainAndCate(type, sysType);
        } catch (NullPointerException e) {
            log.error("Asset type is not supported. type Asset =" + asset.getAssetType().getId() + "-" + asset.getAssetSysType().getId() + "-" + asset.getId());
            return false;
        }
        if (CommUtils.isEmptyOfCollection(details)) {
            if (log.isDebugEnabled()) {
                log.debug("Asset type is not supported. type=" + type + "-" + sysType + "-" + asset.getId());
            }
            return false;
        }
        ConnectionInfo info = new ConnectionInfo();
        info.setIp(asset.getIp());
        info.setPort(asset.getPort());
        info.setProto(asset.getProtocol());
        info.setSysType(LinuxGatherProvider.SUPPORT_OS);
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setUsername(asset.getAccount());
        accountInfo.setPassword(asset.getPwd());
        String cmd = null;
        for (GatherDetail detail : details) {
            if ("Linuxtest".equals(detail.getName())) {
                List<GatherDetailCommand> command = detail.getCommands().getCommand();
                for (GatherDetailCommand gatherDetailCommand : command) {
                    cmd = gatherDetailCommand.getCommand();
                }
            }
        }
        CmdTaskInfo cmdTaskInfo = new CmdTaskInfo();
        assert cmd != null;
        cmdTaskInfo.setCmd(StringUtils.isEmpty(cmd) ? cmd : cmd.trim());
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setId(UUID.randomUUID());
        taskRequest.setConn(info);
        taskRequest.setTask(cmdTaskInfo);
        taskRequest.setAccount(accountInfo);
        taskRequest.setType(TaskRequest.TYPE_GATHER);
        taskRequest.setMode(TaskRequest.MODE_ASYNC);
        taskRequest.setTto(TaskRequest.EXEC_TTO);
        HashMap<Long, TaskRequest> map = new HashMap<>();
        map.put(asset.getId(), taskRequest);
        taskResult.add(map);
        return true;
    }

    /**
     * 获取采集回来的结果
     *
     * @param taskRequest taskRequest
     * @return 结果
     */
    private boolean getResult(TaskRequest taskRequest) {
        cmdSendBean.sendRequestForAsyncResponse(taskRequest);
        TaskResponseWrapper taskResponseWrapper = new TaskResponseWrapper();
        Object monitor = businessCommon.addTaskListener(taskResponseWrapper, taskRequest.getId());
        cmdSendBean.sendRequestForAsyncResponse(taskRequest);
        try {
            businessCommon.waitForResponse(taskResponseWrapper, monitor, taskRequest.getTto() + 15);
        } catch (Exception e) {
            throw new GatherException(e.getMessage());
        }
        TaskResponse resp = taskResponseWrapper.getTaskResponse();
        return resp.getRes() == 0;
    }

    @PostConstruct
    public void init() {
        Thread gatherThread = new Thread(() -> {
            while (true) {
                try {
                    Map<Long, TaskRequest> take = taskResult.take();
                    for (Long aLong : take.keySet()) {
                        boolean result = getResult(take.get(aLong));
                        if (result) {
                            assetRepository.updateLoginStatusById(aLong, 2);
                        } else {
                            assetRepository.updateLoginStatusById(aLong, 3);
                        }
                    }
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("Login GatherThread error result", e);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                        if (log.isErrorEnabled()) {
                            log.error("Login GatherThread sleep error result", ex);
                        }
                    }
                }
            }
        });
        gatherThread.setDaemon(true);
        gatherThread.setName(THREAD_TASK_RESULT_LOGIN);
        gatherThread.start();
    }

}
