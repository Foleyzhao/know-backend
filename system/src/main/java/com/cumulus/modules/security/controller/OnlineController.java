package com.cumulus.modules.security.controller;

import com.cumulus.modules.security.service.OnlineUserService;
import com.cumulus.utils.EncryptUtils;
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
 * 在线用户控制层
 */
@RestController
@RequestMapping("/auth/online")
public class OnlineController {

    /**
     * 在线用户服务
     */
    @Autowired
    private OnlineUserService onlineUserService;

    /**
     * 查询在线用户
     *
     * @param filter   查询参数
     * @param pageable 分页参数
     * @return 在线用户列表
     */
    @GetMapping
    @PreAuthorize("@auth.check()")
    public ResponseEntity<Object> query(String filter, Pageable pageable) {
        return new ResponseEntity<>(onlineUserService.getAll(filter, pageable), HttpStatus.OK);
    }

    /**
     * 导出数据
     *
     * @param response 响应
     * @param filter   查询参数
     * @throws IOException 异常
     */
    @GetMapping(value = "/download")
    @PreAuthorize("@auth.check()")
    public void download(HttpServletResponse response, String filter) throws IOException {
        onlineUserService.download(onlineUserService.getAll(filter), response);
    }

    /**
     * 踢出用户
     *
     * @param keys token列表
     * @return 响应
     * @throws Exception 异常
     */
    @DeleteMapping
    @PreAuthorize("@auth.check()")
    public ResponseEntity<Object> delete(@RequestBody Set<String> keys) throws Exception {
        for (String key : keys) {
            // 解密Key
            key = EncryptUtils.desDecrypt(key);
            onlineUserService.kickOut(key);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
