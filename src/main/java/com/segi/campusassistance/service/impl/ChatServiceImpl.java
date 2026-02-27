package com.segi.campusassistance.service.impl;

import com.segi.campusassistance.dto.ChatResponse;
import com.segi.campusassistance.dto.MessageRequest;
import com.segi.campusassistance.dto.MessageResponse;
import com.segi.campusassistance.entity.Chat;
import com.segi.campusassistance.entity.ChatMessage;
import com.segi.campusassistance.entity.Item;
import com.segi.campusassistance.entity.User;
import com.segi.campusassistance.repository.ChatMessageRepository;
import com.segi.campusassistance.repository.ChatRepository;
import com.segi.campusassistance.repository.ItemRepository;
import com.segi.campusassistance.repository.UserRepository;
import com.segi.campusassistance.service.ChatService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ChatServiceImpl(ChatRepository chatRepository,
                          ChatMessageRepository chatMessageRepository,
                          ItemRepository itemRepository,
                          UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ChatResponse getOrCreateChat(Long itemId, Long ownerId, Long requesterId, Long currentUserId) {
        // 验证物品存在
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));

        // 验证当前用户是owner或requester
        if (!currentUserId.equals(ownerId) && !currentUserId.equals(requesterId)) {
            throw new AccessDeniedException("Not authorized to access this chat");
        }

        // 验证owner是物品所有者
        if (!item.getUserId().equals(ownerId)) {
            throw new IllegalArgumentException("Owner ID does not match the item");
        }

        // 第一步：优先查找两个用户之间是否已经存在聊天（不基于itemId）
        Optional<Chat> existingChatBetweenUsers = chatRepository.findChatBetweenUsers(ownerId, requesterId);

        Chat chat;
        if (existingChatBetweenUsers.isPresent()) {
            // 如果找到了现有聊天，直接返回它（不管itemId是什么）
            chat = existingChatBetweenUsers.get();
        } else {
            // 第二步：如果没有找到用户之间的聊天，再查找基于itemId的聊天（保持向后兼容）
            Optional<Chat> existingChatByItem = chatRepository.findByItemIdAndUsers(itemId, ownerId, requesterId);

            if (existingChatByItem.isPresent()) {
                chat = existingChatByItem.get();
            } else {
                // 第三步：如果都没有找到，创建新的聊天
                chat = new Chat();
                chat.setItemId(itemId);
                chat.setOwnerId(ownerId);
                chat.setRequesterId(requesterId);
                chat = chatRepository.save(chat);
            }
        }

        // 获取用户信息
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found"));
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new EntityNotFoundException("Requester not found"));

        // 获取未读消息数（对于当前用户）
        Long unreadCount = chatMessageRepository.countUnreadMessages(chat.getId(), currentUserId);

        return ChatResponse.fromEntity(
                chat,
                owner.getName(),
                owner.getPicture(),
                requester.getName(),
                requester.getPicture(),
                unreadCount
        );
    }

    @Override
    public ChatResponse getChat(Long chatId, Long currentUserId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        // 权限检查：只有参与者可以访问
        if (!chat.getOwnerId().equals(currentUserId) && !chat.getRequesterId().equals(currentUserId)) {
            throw new AccessDeniedException("Not authorized to access this chat");
        }

        User owner = userRepository.findById(chat.getOwnerId())
                .orElseThrow(() -> new EntityNotFoundException("Owner not found"));
        User requester = userRepository.findById(chat.getRequesterId())
                .orElseThrow(() -> new EntityNotFoundException("Requester not found"));

        Long unreadCount = chatMessageRepository.countUnreadMessages(chatId, currentUserId);

        return ChatResponse.fromEntity(
                chat,
                owner.getName(),
                owner.getPicture(),
                requester.getName(),
                requester.getPicture(),
                unreadCount
        );
    }

    @Override
    public List<ChatResponse> getUserChats(Long userId) {
        List<Chat> chats = chatRepository.findByUserId(userId);
        return chats.stream()
                .map(chat -> {
                    User owner = userRepository.findById(chat.getOwnerId()).orElse(null);
                    User requester = userRepository.findById(chat.getRequesterId()).orElse(null);
                    Long unreadCount = chatMessageRepository.countUnreadMessages(chat.getId(), userId);
                    
                    return ChatResponse.fromEntity(
                            chat,
                            owner != null ? owner.getName() : "Unknown",
                            owner != null ? owner.getPicture() : null,
                            requester != null ? requester.getName() : "Unknown",
                            requester != null ? requester.getPicture() : null,
                            unreadCount
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<MessageResponse> getChatMessages(Long chatId, Long currentUserId, Pageable pageable) {
        // 验证聊天存在和权限
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        if (!chat.getOwnerId().equals(currentUserId) && !chat.getRequesterId().equals(currentUserId)) {
            throw new AccessDeniedException("Not authorized to access this chat");
        }

        Page<ChatMessage> messages = chatMessageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable);
        
        return messages.map(message -> {
            User sender = userRepository.findById(message.getSenderId()).orElse(null);
            return MessageResponse.fromEntity(
                    message,
                    sender != null ? sender.getName() : "Unknown"
            );
        });
    }

    @Override
    public List<MessageResponse> getChatMessages(Long chatId, Long currentUserId) {
        // 验证聊天存在和权限
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        if (!chat.getOwnerId().equals(currentUserId) && !chat.getRequesterId().equals(currentUserId)) {
            throw new AccessDeniedException("Not authorized to access this chat");
        }

        List<ChatMessage> messages = chatMessageRepository.findByChatIdOrderByCreatedAtAsc(chatId);
        
        return messages.stream()
                .map(message -> {
                    User sender = userRepository.findById(message.getSenderId()).orElse(null);
                    return MessageResponse.fromEntity(
                            message,
                            sender != null ? sender.getName() : "Unknown"
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public MessageResponse getLastMessage(Long chatId, Long currentUserId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        if (!chat.getOwnerId().equals(currentUserId) && !chat.getRequesterId().equals(currentUserId)) {
            throw new AccessDeniedException("Not authorized to access this chat");
        }

        ChatMessage lastMessage = chatMessageRepository.findTopByChatIdOrderByCreatedAtDesc(chatId)
                .orElseThrow(() -> new EntityNotFoundException("No chat messages available"));

        User sender = userRepository.findById(lastMessage.getSenderId())
                .orElseThrow(() -> new EntityNotFoundException("Sender not found"));

        return MessageResponse.fromEntity(lastMessage, sender.getName());
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(Long chatId, MessageRequest request, Long currentUserId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        // 权限检查：只有参与者可以发送消息
        if (!chat.getOwnerId().equals(currentUserId) && !chat.getRequesterId().equals(currentUserId)) {
            throw new AccessDeniedException("Not authorized to send messages in this chat");
        }

        // 验证发送者是当前用户
        if (!request.getSenderId().equals(currentUserId)) {
            throw new IllegalArgumentException("Sender ID does not match the current user");
        }

        ChatMessage message = new ChatMessage();
        message.setChatId(chatId);
        message.setSenderId(request.getSenderId());
        message.setContent(request.getContent());
        message.setIsRead(false);

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 更新聊天更新时间
        chatRepository.save(chat);

        User sender = userRepository.findById(savedMessage.getSenderId())
                .orElseThrow(() -> new EntityNotFoundException("Sender not found"));

        return MessageResponse.fromEntity(savedMessage, sender.getName());
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long chatId, Long currentUserId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        // 权限检查
        if (!chat.getOwnerId().equals(currentUserId) && !chat.getRequesterId().equals(currentUserId)) {
            throw new AccessDeniedException("Not authorized to access this chat");
        }

        // 标记当前用户收到的所有消息为已读
        chatMessageRepository.markAsReadByChatIdAndUserId(chatId, currentUserId);
    }
}

