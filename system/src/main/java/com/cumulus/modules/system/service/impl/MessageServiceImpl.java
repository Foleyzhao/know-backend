package com.cumulus.modules.system.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cumulus.enums.MessageTypeEnum;
import com.cumulus.modules.system.dto.MessageQueryCriteria;
import com.cumulus.modules.system.entity.Message;
import com.cumulus.modules.system.entity.Role;
import com.cumulus.modules.system.entity.User;
import com.cumulus.modules.system.repository.MessageRepository;
import com.cumulus.modules.system.service.MessageService;
import com.cumulus.modules.system.service.RoleService;
import com.cumulus.modules.system.service.UserService;
import com.cumulus.utils.QueryUtils;
import com.cumulus.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统消息服务
 *
 * @author : shenjc
 */
@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    /**
     * 系统消息数据接口
     */
    @Autowired
    private MessageRepository messageRepository;

    /**
     * 用户服务
     */
    @Autowired
    private UserService userService;

    /**
     * 角色服务
     */
    @Autowired
    private RoleService roleService;

    @Override
    public Page<Message> queryAllByUser(MessageQueryCriteria messageQueryCriteria, Pageable pageable) {
        return messageRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> QueryUtils.getPredicate(root, messageQueryCriteria, criteriaBuilder),
                pageable);
    }

    @Override
    public void readMessageAll() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        messageRepository.updateMessageStatusByUserId(Message.MESSAGE_STATUS_READING, currentUserId);
    }

    @Override
    public void readMessageBatch(List<Long> messageIdList) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        messageRepository.updateMessageStatusBatch(Message.MESSAGE_STATUS_READING, currentUserId, messageIdList);
    }

    @Override
    public void sendMessage(@NotNull MessageTypeEnum messageType, long menuId, List<String> jumpParameters, @NotNull String... paramList) {
        final List<Role> roleList = roleService.findInMenuId(Collections.singletonList(menuId));
        if (roleList == null || roleList.isEmpty()) {
            return;
        }
        Set<Long> userIdList = userService.findAllByRoles(roleList.stream().map(Role::getId).collect(Collectors.toList()))
                .stream().map(User::getId).collect(Collectors.toSet());
        sendMessage(messageType, userIdList, jumpParameters, paramList);
    }

    @Override
    public void sendMessage(@NotNull MessageTypeEnum messageType, Set<Long> userList, List<String> jumpParameters, @NotNull String... paramList) {
        if (userList.isEmpty()) {
            return;
        }
        List<Message> messageList = new ArrayList<>();
        for (Long userId : userList) {
            User user = new User();
            user.setId(userId);
            Message message = new Message();
            message.setMessageStatus(Message.MESSAGE_STATUS_UNREAD);
            message.setMessageType(messageType.getType());
            message.setMessageContent(messageType.generateMessageContext(paramList));
            Map<String, Object> jumpParametersMap = messageType.generateJumpParameters(jumpParameters);
            if (jumpParametersMap != null && !jumpParametersMap.isEmpty()) {
                message.setJumpParameters(JSONObject.toJSONString(jumpParametersMap));
            } else {
                message.setJumpParameters(null);
            }
            message.setUser(user);
            messageList.add(message);
        }
        messageRepository.saveAll(messageList);
    }
}
