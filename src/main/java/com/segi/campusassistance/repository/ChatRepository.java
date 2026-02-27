package com.segi.campusassistance.repository;

import com.segi.campusassistance.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    // 根据itemId和两个用户ID查找聊天
    @Query("SELECT c FROM Chat c WHERE c.itemId = :itemId " +
           "AND ((c.ownerId = :userId1 AND c.requesterId = :userId2) " +
           "OR (c.ownerId = :userId2 AND c.requesterId = :userId1))")
    Optional<Chat> findByItemIdAndUsers(
            @Param("itemId") Long itemId,
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );

    // 查找用户参与的所有聊天
    @Query("SELECT c FROM Chat c WHERE c.ownerId = :userId OR c.requesterId = :userId ORDER BY c.updatedAt DESC")
    List<Chat> findByUserId(@Param("userId") Long userId);

    // 查找特定物品的所有聊天
    List<Chat> findByItemId(Long itemId);

    // 查找两个用户之间是否已经存在聊天（不基于itemId）
    @Query("SELECT c FROM Chat c WHERE " +
           "(c.ownerId = :userId1 AND c.requesterId = :userId2) OR " +
           "(c.ownerId = :userId2 AND c.requesterId = :userId1)")
    Optional<Chat> findChatBetweenUsers(@Param("userId1") Long userId1,
                                         @Param("userId2") Long userId2);

    List<Chat> findByOwnerIdOrRequesterId(Long ownerId, Long requesterId);
}

