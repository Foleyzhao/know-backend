package com.cumulus.modules.business.gather.aspect;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSONObject;
import com.cumulus.modules.business.detect.common.DetectConstant;
import com.cumulus.modules.business.dto.AssetDto;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.entity.AssetSysType;
import com.cumulus.modules.business.entity.AssetTag;
import com.cumulus.modules.business.entity.AssetType;
import com.cumulus.modules.business.gather.annotation.HistoryAnnotation;
import com.cumulus.modules.business.gather.entity.es.HistoryEs;
import com.cumulus.modules.business.gather.repository.HistoryEsRepository;
import com.cumulus.modules.business.mapstruct.AssetMapper;
import com.cumulus.modules.business.repository.AssetRepository;
import com.cumulus.modules.business.repository.AssetSysTypeRepository;
import com.cumulus.modules.business.repository.AssetTagRepository;
import com.cumulus.modules.business.repository.AssetTypeRepository;
import com.cumulus.modules.business.service.AssetService;
import com.cumulus.modules.system.entity.Dept;
import com.cumulus.modules.system.repository.DeptRepository;
import com.cumulus.utils.SecurityUtils;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

/**
 * es????????????????????????
 *
 * @author Shijh
 */
@Slf4j
@Component
@Aspect
public class HistoryAspect {

    public static final String HISTORY_TYPE_CREATE = "Create";
    public static final String HISTORY_TYPE_DELETE = "Delete";
    public static final String HISTORY_TYPE_UPDATE = "update";

    /**
     * ???????????? 1-agent 2-?????? 3-?????????
     */
    public static final String GATHER_TYPE_STRING_AGENT = "agent";
    public static final String GATHER_TYPE_STRING_LOGIN = "??????";
    public static final String GATHER_TYPE_STRING_NONE = "?????????";

    /**
     * ??????????????????
     */
    @Autowired
    private AssetService assetService;
    /**
     * ??????????????????????????????????????????
     */
    @Autowired
    private AssetMapper assetMapper;
    /**
     * ??????????????????????????????
     */
    @Autowired
    private HistoryEsRepository historyEsRepository;
    /**
     * ????????????????????????
     */
    @Resource
    private AssetRepository assetRepository;

    /**
     * ??????????????????????????????
     */
    @Resource
    private AssetTypeRepository assetTypeRepository;

    /**
     * ??????????????????????????????
     */
    @Resource
    private DeptRepository deptRepository;

    /**
     * ??????????????????????????????
     */
    @Resource
    private AssetTagRepository assetTagRepository;

    /**
     * ????????????????????????
     */
    @Resource
    private AssetSysTypeRepository assetSysTypeRepository;


    /**
     * ??????????????????????????????
     *
     * @param oldObject ??????????????????
     * @param newObject ????????????
     * @return ??????
     */
    public static Map<String, HashMap<String, Object>> compareFields(Object oldObject, Object newObject) {
        Map<String, HashMap<String, Object>> map = null;
        try {
            /**
             * ??????????????????????????????????????????????????????
             */
            if (oldObject.getClass() == newObject.getClass()) {
                map = new HashMap<String, HashMap<String, Object>>();
                Class clazz = oldObject.getClass();
                //??????object???????????????
                PropertyDescriptor[] pds = Introspector.getBeanInfo(clazz, Object.class).getPropertyDescriptors();
                for (PropertyDescriptor pd : pds) {
                    //?????????????????????
                    String name = pd.getName();

                    //???????????????get??????
                    Method readMethod = pd.getReadMethod();

                    // ???oldObject?????????get?????????????????????oldObject????????????
                    Object oldValue = readMethod.invoke(oldObject);
                    // ???newObject?????????get?????????????????????newObject????????????
                    Object newValue = readMethod.invoke(newObject);

                    if (oldValue instanceof List) {
                        continue;
                    }

                    if (newValue instanceof List) {
                        continue;
                    }

                    if (oldValue instanceof Timestamp) {
                        oldValue = new Date(((Timestamp) oldValue).getTime());
                    }

                    if (newValue instanceof Timestamp) {
                        newValue = new Date(((Timestamp) newValue).getTime());
                    }

                    if (oldValue == null && newValue == null) {
                        continue;
                    } else if (oldValue == null && newValue != null) {
                        HashMap<String, Object> valueMap = new HashMap<String, Object>();
                        valueMap.put("oldValue", oldValue);
                        valueMap.put("newValue", newValue);

                        map.put(name, valueMap);

                        continue;
                    }
                    // ??????????????????????????????,?????????????????????map???
                    if (!oldValue.equals(newValue)) {
                        HashMap<String, Object> valueMap = new HashMap<String, Object>();
                        valueMap.put("oldValue", oldValue);
                        valueMap.put("newValue", newValue);

                        map.put(name, valueMap);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    @Pointcut("@annotation(com.cumulus.modules.business.gather.annotation.HistoryAnnotation)")
    public void webLog() {
    }

    @Around("webLog()")
    @Transactional(rollbackFor = Exception.class)
    public Object doAround(ProceedingJoinPoint pjp) {
        //????????????
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        // ??????AspectAnnotation??????
        HistoryAnnotation aspectAnnotation = method.getAnnotation(HistoryAnnotation.class);
        Object proceed = null;
        String type = aspectAnnotation.type();

        boolean flag = aspectAnnotation.isStart();
        Date date = new Date();
        try {
            switch (type) {
                case "1":
                    getHistoryUpdate(pjp, flag, date);
                    break;
                case "2":
                    getHistoryCreate(pjp, flag, date);
                    break;
                case "3":
                    getHistoryDelete(pjp, flag, date);
                    break;
                default:
            }
            proceed = pjp.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return proceed;
    }

    /**
     * ??????????????????
     *
     * @param pjp  ProceedingJoinPoint
     * @param flag ????????????
     * @param date ??????
     */
    private void getHistoryUpdate(ProceedingJoinPoint pjp, boolean flag, Date date) {
        if (flag) {
            AssetDto newAssetDto = (AssetDto) pjp.getArgs()[0];
            Asset newAsset = assetMapper.toEntity(newAssetDto);
            Asset oldAsset = assetService.findById(newAsset.getId());
            Set<AssetTag> oldTagSet = oldAsset.getAssetTags() == null ? new HashSet<>() : oldAsset.getAssetTags();
            Set<AssetTag> newTagSet = newAssetDto.getAssetTags() == null ? new HashSet<>() : newAssetDto.getAssetTags();
            Set<Long> oldTagIdSet = oldTagSet.stream().map(AssetTag::getId).collect(Collectors.toSet());
            Set<Long> newTagIdSet = newTagSet.stream().map(AssetTag::getId).collect(Collectors.toSet());
            List<AssetTag> newTagList = assetTagRepository.findAllById(newTagIdSet);
            oldTagIdSet = oldTagIdSet.stream().filter(oldTagId -> {
                if (newTagIdSet.contains(oldTagId)) {
                    newTagIdSet.remove(oldTagId);
                    return true;
                }
                return false;
            }).collect(Collectors.toSet());
            if (!oldTagIdSet.isEmpty() || !newTagIdSet.isEmpty()) {
                HistoryEs historyEs = new HistoryEs();
                historyEs.setSource(HISTORY_TYPE_UPDATE);
                historyEs.setBefore(oldTagSet.isEmpty() ? null : JSONObject.toJSONString(oldTagSet.stream().map(AssetTag::getName).collect(Collectors.toList())));
                historyEs.setAfter(newTagList.isEmpty() ? null : JSONObject.toJSONString(newTagList.stream().map(AssetTag::getName).collect(Collectors.toList())));
                historyEs.setDateSource(SecurityUtils.getCurrentUsername());
                historyEs.setAssetCategory(oldAsset.getAssetCategory());
                historyEs.setItem("????????????");
                historyEs.setUpdateTime(date);
                historyEs.setAssetId(Long.toString(newAsset.getId()));
                historyEsRepository.save(historyEs);
            }
            List<String> stringList = Arrays.asList("assetSysType", "dept", "assetType", "name", "port", "protocol", "gatherType", "account");
            Map<String, HashMap<String, Object>> fieldsChangesMap = compareFields(oldAsset, newAsset);
            if (!CollectionUtils.isEmpty(fieldsChangesMap)) {
                for (Map.Entry<String, HashMap<String, Object>> me : fieldsChangesMap.entrySet()) {
                    HistoryEs historyEs = new HistoryEs();
                    HashMap<String, Object> value = me.getValue();
                    historyEs.setSource(HISTORY_TYPE_UPDATE);
                    if (!stringList.contains(me.getKey())) {
                        continue;
                    }
                    if ("assetSysType".equals(me.getKey())) {
                        AssetSysType oldValue = (AssetSysType) value.get("oldValue");
                        AssetSysType newValue = (AssetSysType) value.get("newValue");
                        if (!ObjectUtils.isEmpty(oldValue) && !ObjectUtils.isEmpty(newValue)) {
                            if (oldValue.getId().equals(newValue.getId())) {
                                continue;
                            }
                        }
                        if (null == newValue) {
                            historyEs.setBefore(ObjectUtils.isEmpty(oldValue) ? "" : oldValue.getName() == null ? "" : oldValue.getName());
                            historyEs.setAfter(" ");
                        } else {
                            AssetSysType assetSysType = assetSysTypeRepository.findById(newValue.getId()).get();
                            historyEs.setBefore(ObjectUtils.isEmpty(oldValue) ? "" : oldValue.getName() == null ? "" : oldValue.getName());
                            historyEs.setAfter(ObjectUtils.isEmpty(assetSysType) ? " " : assetSysType.getName());
                        }
                    } else if ("dept".equals(me.getKey())) {
                        Dept oldValue = (Dept) value.get("oldValue");
                        Dept newValue = (Dept) value.get("newValue");
                        if (!ObjectUtils.isEmpty(oldValue) && !ObjectUtils.isEmpty(newValue)) {
                            if (oldValue.getId().equals(newValue.getId())) {
                                continue;
                            }
                        }
                        if (null == newValue) {
                            historyEs.setBefore(ObjectUtils.isEmpty(oldValue) ? "" : oldValue.getName() == null ? "" : oldValue.getName());
                            historyEs.setAfter(" ");
                        } else {
                            Dept dept = deptRepository.findById(newValue.getId()).get();
                            historyEs.setBefore(ObjectUtils.isEmpty(oldValue) ? "" : oldValue.getName() == null ? "" : oldValue.getName());
                            historyEs.setAfter(ObjectUtils.isEmpty(dept) ? " " : dept.getName());
                        }
                    } else if ("assetType".equals(me.getKey())) {
                        AssetType oldValue = (AssetType) value.get("oldValue");
                        AssetType newValue = (AssetType) value.get("newValue");
                        if (!ObjectUtils.isEmpty(oldValue) && !ObjectUtils.isEmpty(newValue)) {
                            if (oldValue.getId().equals(newValue.getId())) {
                                continue;
                            }
                        }
                        if (null == newValue) {
                            historyEs.setBefore(ObjectUtils.isEmpty(oldValue) ? "" : oldValue.getName() == null ? "" : oldValue.getName());
                            historyEs.setAfter(" ");
                        } else {
                            AssetType assetType = assetTypeRepository.findById(newValue.getId()).get();
                            historyEs.setBefore(ObjectUtils.isEmpty(oldValue) ? "" : oldValue.getName());
                            historyEs.setAfter(ObjectUtils.isEmpty(assetType) ? " " : assetType.getName());
                        }
                    } else if ("gatherType".equals(me.getKey())) {
                        historyEs.setBefore(value.get("oldValue") == null ? "" : getGatherType(value.get("oldValue").toString()));
                        historyEs.setAfter(value.get("newValue") == null ? "" : getGatherType(value.get("newValue").toString()));
                    } else {
                        historyEs.setBefore(value.get("oldValue") == null ? "" : value.get("oldValue").toString());
                        historyEs.setAfter(value.get("newValue") == null ? "" : value.get("newValue").toString());
                    }
                    historyEs.setDateSource(SecurityUtils.getCurrentUsername());
                    historyEs.setAssetCategory(oldAsset.getAssetCategory());
                    getDict(me, historyEs);
                    historyEs.setUpdateTime(date);
                    historyEs.setAssetId(Long.toString(newAsset.getId()));
                    historyEsRepository.save(historyEs);
                }
            }
        }
    }

    /**
     * ?????????????????????
     *
     * @param me
     * @param historyEs
     */
    private void getDict(Map.Entry<String, HashMap<String, Object>> me, HistoryEs historyEs) {
        switch (me.getKey()) {
            case "name":
                historyEs.setItem("????????????");
                break;
            case "port":
                historyEs.setItem("??????");
                break;
            case "protocol":
                historyEs.setItem("??????");
                break;
            case "account":
                historyEs.setItem("??????");
                break;
            case "assetSysType":
                historyEs.setItem("????????????-??????");
                break;
            case "dept":
                historyEs.setItem("??????");
                break;
            case "assetType":
                historyEs.setItem("????????????-??????");
                break;
            case "gatherType":
                historyEs.setItem("????????????");
                break;
            default:
        }
    }

    /**
     * ??????????????????
     *
     * @param gatherType 1-agent 2-?????? 3-?????????
     * @return ??????
     */
    private String getGatherType(String gatherType) {
        if (Integer.parseInt(gatherType) == DetectConstant.GATHER_TYPE_SCAN) {
            return HistoryAspect.GATHER_TYPE_STRING_NONE;
        } else if (Integer.parseInt(gatherType) == DetectConstant.GATHER_TYPE_LOGIN) {
            return HistoryAspect.GATHER_TYPE_STRING_LOGIN;
        } else if (Integer.parseInt(gatherType) == DetectConstant.GATHER_TYPE_AGENT) {
            return HistoryAspect.GATHER_TYPE_STRING_AGENT;
        }
        return null;
    }

    /**
     * ??????????????????
     *
     * @param pjp  ProceedingJoinPoint
     * @param flag ????????????
     * @param date ??????
     */
    private void getHistoryCreate(ProceedingJoinPoint pjp, boolean flag, Date date) {
        if (flag) {
            AssetDto assetDto = (AssetDto) pjp.getArgs()[0];
            Asset newAsset = assetMapper.toEntity(assetDto);
            HistoryEs historyEs = new HistoryEs();
            historyEs.setAssetCategory(newAsset.getAssetCategory());
            historyEs.setSource(HISTORY_TYPE_CREATE);
            historyEs.setUpdateTime(date);
            historyEs.setAssetId(newAsset.getIp() + "-" + newAsset.getPort() + "-" + newAsset.getAssetCategory());
            historyEsRepository.save(historyEs);
        }
    }

    /**
     * ??????????????????
     *
     * @param pjp  ProceedingJoinPoint
     * @param flag ????????????
     * @param date ??????
     */
    private void getHistoryDelete(ProceedingJoinPoint pjp, boolean flag, Date date) {
        if (flag) {
            Long arg = (Long) pjp.getArgs()[0];
            Asset asset = assetRepository.findById(arg).orElse(null);
            HistoryEs historyEs = new HistoryEs();
            historyEs.setDateSource(SecurityUtils.getCurrentUsername());
            historyEs.setSource(HISTORY_TYPE_DELETE);
            historyEs.setAssetCategory(asset.getAssetCategory());
            historyEs.setUpdateTime(date);
            historyEs.setAssetId(Long.toString(asset.getId()));
            historyEsRepository.save(historyEs);
        }
    }
}
