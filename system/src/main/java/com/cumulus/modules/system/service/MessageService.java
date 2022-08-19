package com.cumulus.modules.system.service;

import com.cumulus.enums.MessageTypeEnum;
import com.cumulus.modules.system.dto.MessageQueryCriteria;
import com.cumulus.modules.system.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * 系统服务接口
 *
 * @author : shenjc
 */
public interface MessageService {

    /**
     * 根据条件查询用户
     *
     * @param messageQueryCriteria 查询对象
     * @param pageable             分页参数
     * @return 用户列表
     */
    Page<Message> queryAllByUser(MessageQueryCriteria messageQueryCriteria, Pageable pageable);

    /**
     * 根据用户id设置已读
     */
    void readMessageAll();

    /**
     * 根据用户id 和消息id 设置已读
     *
     * @param messageIdList 消息id列表
     */
    void readMessageBatch(List<Long> messageIdList);

    /**
     * 保存系统消息
     *
     * @param messageType    消息类型
     * @param menuId         需要的权限id
     * @param jumpParameters 跳转用消息
     * @param paramList      消息的参数
     */
    void sendMessage(MessageTypeEnum messageType, long menuId, List<String> jumpParameters, String... paramList);

    /**
     * 保存系统消息
     *
     * @param messageType    消息类型
     * @param userList       用户列表
     * @param jumpParameters 跳转用消息
     * @param paramList      消息内容 不进行填充
     */
    void sendMessage(MessageTypeEnum messageType, Set<Long> userList, List<String> jumpParameters, String... paramList);
}
