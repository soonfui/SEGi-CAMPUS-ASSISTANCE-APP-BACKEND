package com.segi.campusassistance.repository;

import com.segi.campusassistance.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 根据chatId查找所有消息，按时间排序
    List<ChatMessage> findByChatIdOrderByCreatedAtAsc(Long chatId);

    // 分页查询消息
    Page<ChatMessage> findByChatIdOrderByCreatedAtDesc(Long chatId, Pageable pageable);

    // 查询聊天的最后一条消息
    java.util.Optional<ChatMessage> findTopByChatIdOrderByCreatedAtDesc(Long chatId);

    // 标记消息为已读
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.chatId = :chatId AND m.senderId != :userId")
    int markAsReadByChatIdAndUserId(@Param("chatId") Long chatId, @Param("userId") Long userId);

    // 统计未读消息数量
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatId = :chatId AND m.senderId != :userId AND m.isRead = false")
    Long countUnreadMessages(@Param("chatId") Long chatId, @Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ChatMessage m WHERE m.chatId IN :chatIds")
    void deleteByChatIdIn(@Param("chatIds") List<Long> chatIds);
}

