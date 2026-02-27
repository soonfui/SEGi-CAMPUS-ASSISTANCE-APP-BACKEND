package com.segi.campusassistance.service;

import com.segi.campusassistance.dto.ChatResponse;
import com.segi.campusassistance.dto.MessageRequest;
import com.segi.campusassistance.dto.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatService {

    ChatResponse getOrCreateChat(Long itemId, Long ownerId, Long requesterId, Long currentUserId);

    ChatResponse getChat(Long chatId, Long currentUserId);

    List<ChatResponse> getUserChats(Long userId);

    Page<MessageResponse> getChatMessages(Long chatId, Long currentUserId, Pageable pageable);

    List<MessageResponse> getChatMessages(Long chatId, Long currentUserId);

    MessageResponse getLastMessage(Long chatId, Long currentUserId);

    MessageResponse sendMessage(Long chatId, MessageRequest request, Long currentUserId);

    void markMessagesAsRead(Long chatId, Long currentUserId);
}

