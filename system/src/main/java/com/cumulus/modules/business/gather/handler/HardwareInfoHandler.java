package com.cumulus.modules.business.gather.handler;


import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.common.utils.BusinessCommon;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.HardwareEs;
import com.cumulus.modules.business.gather.repository.HardwareEsRepository;
import com.cumulus.modules.business.gather.request.GatherItemLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 硬件信息处理类
 *
 * @author zhaoff
 */
@Component
public class HardwareInfoHandler extends ItemLogHandler {

    /**
     * cpu核数
     */
    private static final String CPU_CORES = "hardware_hardware_cpucores";

    /**
     * cpu主频
     */
    private static final String CPU_FREQUENCY = "hardware_hardware_cpufrequency";

    /**
     * cpu型号
     */
    private static final String CPU_MODEL = "hardware_hardware_cpumodel";

    /**
     * 硬件型号
     */
    private static final String SYSTEM_MODEL = "hardware_hardware_systemmodel";

    /**
     * 内存数量
     */
    private static final String MEMORY_NUM = "hardware_hardware_memorynum";

    /**
     * 厂商
     */
    private static final String VENDOR = "hardware_hardware_vendor";

    /**
     * 电源功率
     */
    private static final String POWER_SUPPLY = "hardware_hardware_powersupply";

    /**
     * UUID
     */
    private static final String UUID = "hardware_hardware_uuid";

    /**
     * 上线时间
     */
    private static final String RUNTIME = "hardware_hardware_runtime";

    /**
     * 硬盘大小
     */
    private static final String DISK_SIZE = "hardware_hardware_disksize";

    /**
     * 内存大小
     */
    private static final String PHYSICAL_MEMORY = "hardware_hardware_physicalmemory";

    /**
     * 电源数量
     */
    private static final String PS_NUM = "hardware_hardware_psnum";

    /**
     * runtime原始数据被分隔后应该有的长度
     */
    private static final int RUNTIME_LENGTH = 3;

    /**
     * 0天字符串
     */
    private static final String ZERO_DAY = "0";

    /**
     * 天 英文字符串
     */
    private static final String DAY = "day";

    /**
     * 硬件ES 数据接口
     */
    @Autowired
    private HardwareEsRepository repository;

    /**
     * 构造方法
     */
    public HardwareInfoHandler() {
        this.esIndex = GatherConstants.ES_INDEX_HARDWARE_INFO;
    }

    @Override
    public List<String> supportAssetTypes() {
        return Collections.singletonList(GatherConstants.TYPE_HOST.toString());
    }

    /**
     * 采集日志处理方法
     *
     * @param asset    资产
     * @param itemLogs 采集日志
     */
    @Override
    public void handle(GatherAssetEs asset, Set<GatherItemLog> itemLogs) throws Exception {
        if (null == asset || CommUtils.isEmptyOfCollection(itemLogs)) {
            return;
        }
        HardwareEs hardwareEs = new HardwareEs();
        hardwareEs.setId(CommUtils.createAuditId());
        hardwareEs.setAssetId(asset.getAssetId());
        hardwareEs.setGatherAssetId(asset.getId());
        hardwareEs.setUtime(asset.getUtime());
        Map<String, Object> detail = new HashMap<>(itemLogs.size());
        for (GatherItemLog itemLog : itemLogs) {
            if (Objects.equals(itemLog.getResult(), 0)) {
                String replaceKey = BusinessCommon.eraseSysFromItemkey(itemLog.getItemKey());
                replaceKey = replaceKey.replace(".", "_");
                String itemName = BusinessCommon.getNameFromItemKey(itemLog.getItemKey());
                Object value = itemLog.getElite().get(itemName);
                if (null != itemLog.getElite() && itemLog.getElite().containsKey(itemName)) {
                    handlerHardware(value, hardwareEs, replaceKey, detail, asset);
                }
            }
        }
        hardwareEs.setDetail(detail);
        putDetails(asset, repository, Collections.singletonList(hardwareEs));
    }

    @Override
    public void deleteHandle(GatherAssetEs asset) {
        repository.deleteByGatherAssetId(asset.getId());
    }

    /**
     * 处理硬件信息
     *
     * @param value      解析出的结果
     * @param hardware   硬件对象
     * @param replaceKey 具体的key
     * @param detail     细节
     * @param asset      资产对象
     */
    public void handlerHardware(Object value, HardwareEs hardware, String replaceKey,
                                Map<String, Object> detail, GatherAssetEs asset) {
        switch (replaceKey) {
            case CPU_CORES: {
                hardware.setCpuCores((String) value);
                detail.put(replaceKey, value);
                break;
            }
            case CPU_FREQUENCY: {
                hardware.setCpuFrequency((String) value);
                detail.put(replaceKey, value + "MHz");
                break;
            }
            case CPU_MODEL: {
                hardware.setCpuModel((String) value);
                detail.put(replaceKey, value);
                break;
            }
            case SYSTEM_MODEL: {
                hardware.setSystemModel((String) value);
                asset.getGatherInfo().put("systemmodel", detail.get(replaceKey));
                break;
            }
            case MEMORY_NUM: {
                hardware.setMemoryNum((String) value);
                detail.put(replaceKey, value);
                break;
            }
            case POWER_SUPPLY: {
                hardware.setPower((String) value);
                detail.put(replaceKey, value);
                break;
            }
            case UUID: {
                asset.setUuid((String) value);
                detail.put(replaceKey, value);
                break;
            }
            case RUNTIME: {
                hardware.setStartTime(getRuntime((String) value));
                detail.put(replaceKey, value);
                break;
            }
            case DISK_SIZE: {
                //实际数据 disksize -> 644.2GB
                String diskSize = (String) value;
                hardware.setDiskSize(Double.parseDouble(diskSize.replace("GB", "")));
                detail.put(replaceKey, diskSize);
                break;
            }
            case PHYSICAL_MEMORY: {
                hardware.setMemorySize((String) value);
                detail.put(replaceKey, value);
                break;
            }
            case PS_NUM: {
                hardware.setPowerNum((String) value);
                detail.put(replaceKey, value);
                break;
            }
            default: {
                //VENDOR 暂时不存入库
                detail.put(replaceKey, value);
                break;
            }
        }
    }

    /**
     * 解析 runtime
     *
     * @param runtime 原始数据
     * @return 返回结果
     */
    public static String getRuntime(String runtime) {
        //runtime ： 79 days 0 或者 79 days 11:11 或者 5:38 2 users
        String[] runtimes = runtime.split(" ");
        if (runtimes.length != RUNTIME_LENGTH) {
            return runtime;
        }
        String days = runtimes[0];
        String hourAndMinutes = runtimes[2];
        if (!runtimes[1].contains(DAY)) {
            hourAndMinutes = runtimes[0];
            days = ZERO_DAY;
        }
        StringBuilder result = new StringBuilder();
        String[] time = hourAndMinutes.split(":");
        if (time.length == 2) {
            result.append(time[0]).append("小时").append(time[1]).append("分钟");
        } else if (time.length == 1) {
            result.append(time[0]).append("分钟");
        } else {
            return runtime;
        }
        if (!ZERO_DAY.equals(days)) {
            result.insert(0, "天").insert(0, days);
        }

        return result.toString();
    }
}
