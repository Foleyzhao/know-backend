package com.cumulus.modules.system.controller;

import com.cumulus.modules.system.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统监控控制层
 */
@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    /**
     * 系统监控服务接口
     */
    @Autowired
    private MonitorService monitorService;


    /**
     * 获取系统性能相关信息
     *
     * @return 统性能相关信息
     */
    @GetMapping
    @PreAuthorize("@auth.check('monitor:list')")
    public ResponseEntity<Object> monitor() {
        return new ResponseEntity<>(monitorService.getServers(), HttpStatus.OK);
    }

}
