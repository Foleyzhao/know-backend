package com.cumulus.modules.system.controller;

import com.cumulus.modules.system.dto.MessageQueryCriteria;
import com.cumulus.modules.system.entity.Message;
import com.cumulus.modules.system.mapstruct.SimpMessageMapper;
import com.cumulus.modules.system.service.MessageService;
import com.cumulus.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;


/**
 * 系统信息控制层
 *
 * @author : shenjc
 */
@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 消息和 简易消息DTO mapper
     */
    @Autowired
    private SimpMessageMapper simpMessageMapper;

    /**
     * 默认的创建时间
     */
    private static final int DEFAULT_CREATE_TIME_LIMIT = -6;

    /**
     * 当前用户拥有的信息 未读的
     *
     * @return 消息列表
     */
    @GetMapping
    public ResponseEntity<Object> query(MessageQueryCriteria messageQueryCriteria, Pageable pageable) {
        messageQueryCriteria.setUserId(SecurityUtils.getCurrentUserId());
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MONTH, DEFAULT_CREATE_TIME_LIMIT);
        messageQueryCriteria.setCreateTimeGreaterThan(new Timestamp(now.getTimeInMillis()));
        Page<Message> messages = messageService.queryAllByUser(messageQueryCriteria, pageable);
        return new ResponseEntity<>(messages.map(simpMessageMapper::toDto), HttpStatus.OK);
    }

    /**
     * 批量设置消息已读
     *
     * @param messageIdList 消息id类别
     * @param all           是否全部已读
     */
    @PostMapping("readMessage")
    public ResponseEntity<Object> readMessage(@RequestBody(required = false) List<Long> messageIdList, Boolean all) {
        if (all != null && all) {
            messageService.readMessageAll();
        } else if (messageIdList != null && !messageIdList.isEmpty()) {
            messageService.readMessageBatch(messageIdList);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}