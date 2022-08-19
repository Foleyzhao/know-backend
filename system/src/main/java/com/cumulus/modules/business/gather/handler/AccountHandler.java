package com.cumulus.modules.business.gather.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.common.utils.BusinessCommon;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.entity.es.AccountEs;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.repository.AccountEsRepository;
import com.cumulus.modules.business.gather.request.GatherItemLog;
import com.cumulus.utils.DateUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 账号信息处理类
 *
 * @author Shijh
 */
@Slf4j
@Component
public class AccountHandler extends ItemLogHandler {

    /**
     * 账号信息数据访问接口
     */
    @Autowired
    private AccountEsRepository repository;

    /**
     * 构造方法
     */
    public AccountHandler() {
        this.esIndex = GatherConstants.ES_INDEX_ACCOUNT;
    }

    @Override
    public List<String> supportAssetTypes() {
        return Collections.singletonList(GatherConstants.TYPE_HOST.toString());
    }

    @Override
    public void handle(GatherAssetEs asset, Set<GatherItemLog> itemLogs) {
        if (null == asset || CommUtils.isEmptyOfCollection(itemLogs)) {
            return;
        }
        List<AccountEs> accounts = new ArrayList<>();
        for (GatherItemLog itemLog : itemLogs) {
            if (null == itemLog) {
                continue;
            }
            String itemName = BusinessCommon.getNameFromItemKey(itemLog.getItemKey());
            Map<String, Object> elite = itemLog.getElite();
            if (null == elite || !elite.containsKey(itemName)) {
                continue;
            }
            List<Map<String, Object>> accountList = (List<Map<String, Object>>) elite.get(itemName);
            if (CommUtils.isEmptyOfCollection(accountList)) {
                continue;
            }
            for (Map<String, Object> data : accountList) {
                if (data.isEmpty()) {
                    continue;
                }
                AccountEs accountEs = genAccount(data, asset);
                accounts.add(accountEs);
            }
        }
        putDetails(asset, repository, accounts);
    }

    /**
     * 生成账号信息
     *
     * @param data  采集数据
     * @param asset 资产
     * @return 账号
     */
    private AccountEs genAccount(Map<String, Object> data, GatherAssetEs asset) {
        AccountEs account = new AccountEs();
        account.setId(CommUtils.createAuditId());
        account.setGatherAssetId(asset.getId());
        account.setUtime(asset.getUtime());
        account.setDetail(data);
        account.setAccountName(data.get("accountname").toString());
        // 1970 这个是
        if ("**Never logged in**".equals(data.get("lastlogontime").toString())) {
            account.setPwdLastSetTime(DateUtils.toDate(DateUtils.fromTimeStamp((long) 0)));
        } else {
            account.setPwdLastSetTime(new Date(Long.parseLong(data.get("lastlogontime").toString())));
        }
        account.setGroup(data.get("localgroup").toString());
        if (null != data.get("passwordexpires")) {
            if ("never".equals(data.get("passwordexpires").toString())) {
                account.setPwdExpireData(data.get("passwordexpires").toString());
            } else {
                account.setPwdExpireData(DateUtils.secondToDate(Long.parseLong(data.get("passwordexpires").toString()), DateUtils.YYYY_MM_DD_HH_MM_SS_STR));
            }
        } else {
            account.setPwdExpireData("-");
        }
        if (null != data.get("accountexpires")) {
            if ("never".equals(data.get("accountexpires").toString())) {
                account.setAccountExpireData(data.get("accountexpires").toString());
            } else {
                account.setAccountExpireData(DateUtils.secondToDate(Long.parseLong(data.get("accountexpires").toString()), DateUtils.YYYY_MM_DD_HH_MM_SS_STR));
            }
        } else {
            account.setAccountExpireData("-");
        }
        return account;
    }

    @Override
    public void deleteHandle(GatherAssetEs asset) {
        repository.deleteByGatherAssetId(asset.getId());
    }
}
