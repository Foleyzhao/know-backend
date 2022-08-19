package com.cumulus.modules.business.gather.handler;

import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.common.utils.BusinessCommon;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.entity.es.SoftwareEs;
import com.cumulus.modules.business.gather.repository.SoftwareEsRepository;
import com.cumulus.modules.business.gather.request.GatherItemLog;
import com.cumulus.utils.DateUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 已装软件处理类
 *
 * @author zhaoff
 */
@Slf4j
@Component
public class SoftWareHandler extends ItemLogHandler {

    /**
     * 已装软件数据访问接口
     */
    @Autowired
    private SoftwareEsRepository repository;

    /**
     * 构造方法
     */
    public SoftWareHandler() {
        this.esIndex = GatherConstants.ES_INDEX_SW;
    }

    @Override
    public List<String> supportAssetTypes() {
        return Collections.singletonList(GatherConstants.TYPE_HOST.toString());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(GatherAssetEs asset, Set<GatherItemLog> itemLogs) throws Exception {
        if (null == asset || CommUtils.isEmptyOfCollection(itemLogs)) {
            return;
        }
        List<SoftwareEs> softWares = new ArrayList<>();
        for (GatherItemLog itemLog : itemLogs) {
            if (null == itemLog) {
                continue;
            }
            String itemName = BusinessCommon.getNameFromItemKey(itemLog.getItemKey());
            Map<String, Object> elite = itemLog.getElite();
            if (null == elite || !elite.containsKey(itemName)) {
                continue;
            }
            List<Map<String, Object>> softwareList = (List<Map<String, Object>>) elite.get(itemName);
            if (CommUtils.isEmptyOfCollection(softwareList)) {
                continue;
            }
            for (Map<String, Object> data : softwareList) {
                if (data.isEmpty()) {
                    continue;
                }
                SoftwareEs software = genSoftware(data, asset);
                softWares.add(software);
            }
        }
        putDetails(asset, repository, softWares);
    }

    @Override
    public void deleteHandle(GatherAssetEs asset) {
        repository.deleteByGatherAssetId(asset.getId());
    }

    /**
     * 生成软件信息
     *
     * @param data  采集数据
     * @param asset 资产
     * @return 软件
     */
    private SoftwareEs genSoftware(Map<String, Object> data, GatherAssetEs asset) {
        SoftwareEs software = new SoftwareEs();
        // 生成ID
        software.setId(CommUtils.createAuditId());
        software.setGatherAssetId(asset.getId());
        software.setAssetId(asset.getAssetId());
        software.setDetail(data);
        software.setUtime(asset.getUtime());
        String swName = data.get("name") == null ? "" : String.valueOf(data.get("name"));
        String swVersion = "";
        String riskSoftwareName = "";
        Object company = data.get("vendor");
        software.setCompany(company == null ? "无" : data.get("vendor").toString());
        if (data.get("installTime") != null) {
            try {
                if (data.get("installTime").toString().contains("月")) {
                    software.setCreateTime(data.get("installTime").toString());
                } else {
                    software.setCreateTime(DateUtils.secondToDate(Long.parseLong(data.get("installTime").toString()), DateUtils.YYYY_MM_DD_HH_MM_SS_STR));
                }
            } catch (NumberFormatException e) {
                software.setCreateTime(data.get("installTime").toString());
            }
        } else {
            software.setCreateTime("-");
        }

        Object version = data.get("version");
        if (null != version) {
            swVersion = version.toString();
            // 若版本含有字母，则切割掉字母及后面字符串
            Pattern pattern1 = Pattern.compile("[a-z-A-Z]");
            Matcher matcher1 = pattern1.matcher(swVersion);
            if (matcher1.find()) {
                String str1 = swVersion.trim();
                int length = str1.length();
                int index = 0;
                for (; index < length; index++) {
                    Pattern pattern = Pattern.compile("[a-z-A-Z]");
                    char val = str1.charAt(index);
                    String vals = val + "";
                    Matcher matcher = pattern.matcher(vals);
                    if (matcher.find()) {
                        break;
                    }
                }
                swVersion = str1.substring(0, index);
            }
        }
        String softwareVersion = "";
        // 若名称或版本为空，则不计入统计
        if (!StringUtils.isEmpty(swName)) {
            softwareVersion += swName;
        }
        if (!StringUtils.isEmpty(swVersion)) {
            softwareVersion += " " + swVersion;
        }
        if (!StringUtils.isEmpty(softwareVersion)) {
            software.setName(swName);
            software.setVersion(swVersion);
        }
        return software;
    }

}
