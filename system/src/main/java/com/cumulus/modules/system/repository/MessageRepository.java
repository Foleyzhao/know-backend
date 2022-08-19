package com.cumulus.modules.system.repository;

import com.cumulus.modules.system.entity.Message;
import com.cumulus.modules.system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统信息表 数据库操作接口
 *
 * @author : shenjc
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long>, JpaSpecificationExecutor<Message> {

    /**
     * 更具用户获取所有信息
     *
     * @param user          用户主要是 user的Id
     * @param messageStatus 消息状态
     * @param pageable      分页信息
     * @return 返回分页数据
     */
    Page<Message> getAllByUserAndMessageStatus(User user, Integer messageStatus, Pageable pageable);

    /**
     * 跟新某个用户的所有消息已读
     *
     * @param messageStatus 消息状态
     * @param userId        用户id
     */
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query(value = "update sys_message set message_status = ?1 where user_id = ?2 ", nativeQuery = true)
    void updateMessageStatusByUserId(Integer messageStatus, Long userId);

    /**
     * 跟新某个用户的所有消息已读
     *
     * @param messageStatus 消息状态
     * @param userId        用户id 只能删除当前用户的消息
     * @param messageIdList 消息列表id
     */
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query(value = "update sys_message set message_status = ?1 where user_id = ?2 and id in ?3", nativeQuery = true)
    void updateMessageStatusBatch(Integer messageStatus, Long userId, List<Long> messageIdList);
}
