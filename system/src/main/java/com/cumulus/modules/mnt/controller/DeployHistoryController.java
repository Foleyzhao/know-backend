package com.cumulus.modules.mnt.controller;

import com.cumulus.annotation.Log;
import com.cumulus.modules.mnt.dto.DeployHistoryQueryCriteria;
import com.cumulus.modules.mnt.service.DeployHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * 部署历史控制层
 */
@RestController
@RequestMapping("/api/deployHistory")
public class DeployHistoryController {

    /**
     * 部署历史服务接口
     */
    @Autowired
    private DeployHistoryService deployhistoryService;

    /**
     * 导出部署历史数据
     *
     * @param response 响应
     * @param criteria 查询参数
     * @throws IOException 异常
     */
    @GetMapping(value = "/download")
    @PreAuthorize("@auth.check('deployHistory:list')")
    public void download(HttpServletResponse response, DeployHistoryQueryCriteria criteria) throws IOException {
        deployhistoryService.download(deployhistoryService.queryAll(criteria), response);
    }

    /**
     * 查询部署历史
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 部署历史列表
     */
    @GetMapping
    @PreAuthorize("@auth.check('deployHistory:list')")
    public ResponseEntity<Object> query(DeployHistoryQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(deployhistoryService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    /**
     * 删除部署历史
     *
     * @param ids 部署历史ID集合
     * @return 响应
     */
    @Log("删除DeployHistory")
    @DeleteMapping
    @PreAuthorize("@auth.check('deployHistory:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<String> ids) {
        deployhistoryService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
