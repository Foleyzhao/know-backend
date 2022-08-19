package com.cumulus.modules.business.gather.handler;

import java.util.Map;
/**
 * 远程扫描 headler
 *
 * @author shijh
 */
public interface ScanHandlerService {

    /**
     * 采集日志处理方法
     *
     * @param take 返回的数据
     */
    void handle(Map<String, Object> take);

}
