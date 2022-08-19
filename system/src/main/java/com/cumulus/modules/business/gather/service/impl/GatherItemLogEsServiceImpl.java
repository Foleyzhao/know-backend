package com.cumulus.modules.business.gather.service.impl;

import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.repository.GatherItemLogEsRepository;
import com.cumulus.modules.business.gather.service.GatherItemLogEsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

/**
 * 资产采集项日志服务实现
 *
 * @author zhaoff
 */
@Service
public class GatherItemLogEsServiceImpl implements GatherItemLogEsService {

    /**
     * 资产采集项数据访问接口
     */
    @Autowired
    private GatherItemLogEsRepository gatherItemLogEsRepository;

    @Override
    public void deleteItemLogByGatherId(String gatherId) {
        gatherItemLogEsRepository.deleteAllByGatherId(gatherId);
    }

    @Override
    public void deleteItemLogOneDayAgo() {
        Date date = CommUtils.getDate(new Date(), Calendar.DAY_OF_MONTH, -1);
        gatherItemLogEsRepository.deleteByTimeBefore(date);
    }

}
