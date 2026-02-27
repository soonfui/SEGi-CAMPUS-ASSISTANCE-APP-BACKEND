package com.segi.campusassistance.controller;

import com.segi.campusassistance.dto.ApiResponse;
import com.segi.campusassistance.dto.ChatRequest;
import com.segi.campusassistance.dto.ChatResponse;
import com.segi.campusassistance.dto.MessageRequest;
import com.segi.campusassistance.dto.MessageResponse;
import com.segi.campusassistance.security.UserPrincipal;
import com.segi.campusassistance.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 创建或获取聊天会话
     * POST /api/chats
     * 或者
     * GET /api/chats?itemId=xxx&ownerId=yyy&requesterId=zzz
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse>> createOrGetChat(
            @Valid @RequestBody ChatRequest request
    ) {
        try {
            UserPrincipal principal = getCurrentUserPrincipal();
            Long currentUserId = principal.getUserId();

            ChatResponse chat = chatService.getOrCreateChat(
                    request.getItemId(),
                    request.getOwnerId(),
                    request.getRequesterId(),
                    currentUserId
            );

            return ResponseEntity.ok(ApiResponse.success(chat));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create or fetch chat: " + e.getMessage()));
        }
    }

    /**
     * 获取或创建聊天（GET方式，通过查询参数）
     * GET /api/chats?itemId=xxx&ownerId=yyy&requesterId=zzz
     */
    @GetMapping
    public ResponseEntity<ApiResponse<ChatResponse>> getOrCreateChatByQuery(
            @RequestParam Long itemId,
            @RequestParam Long ownerId,
            @RequestParam Long requesterId
    ) {
        try {
            UserPrincipal principal = getCurrentUserPrincipal();
            Long currentUserId = principal.getUserId();

            ChatResponse chat = chatService.getOrCreateChat(itemId, ownerId, requesterId, currentUserId);

            return ResponseEntity.ok(ApiResponse.success(chat));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch or create chat: " + e.getMessage()));
        }
    }

    /**
     * 获取用户的聊天列表
     * GET /api/chats/my
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ChatResponse>>> getMyChats() {
        try {
            UserPrincipal principal = getCurrentUserPrincipal();
            Long currentUserId = principal.getUserId();

            List<ChatResponse> chats = chatService.getUserChats(currentUserId);

            return ResponseEntity.ok(ApiResponse.success(chats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch chat list: " + e.getMessage()));
        }
    }

    /**
     * 获取特定聊天详情
     * GET /api/chats/{chatId}
     */
    @GetMapping("/{chatId}")
    public ResponseEntity<ApiResponse<ChatResponse>> getChat(@PathVariable Long chatId) {
        try {
            UserPrincipal principal = getCurrentUserPrincipal();
            Long currentUserId = principal.getUserId();

            ChatResponse chat = chatService.getChat(chatId, currentUserId);

            return ResponseEntity.ok(ApiResponse.success(chat));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch chat: " + e.getMessage()));
        }
    }

    /**
     * 获取聊天消息（分页）
     * GET /api/chats/{chatId}/messages?page=0&size=20
     */
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getChatMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        try {
            UserPrincipal principal = getCurrentUserPrincipal();
            Long currentUserId = principal.getUserId();

            Pageable pageable = PageRequest.of(page, size);
            Page<MessageResponse> messages = chatService.getChatMessages(chatId, currentUserId, pageable);

            return ResponseEntity.ok(ApiResponse.success(messages));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch messages: " + e.getMessage()));
        }
    }

    /**
     * 获取聊天消息（不分页，返回所有）
     * GET /api/chats/{chatId}/messages/all
     */
    @GetMapping("/{chatId}/messages/all")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getAllChatMessages(@PathVariable Long chatId) {
        try {
            UserPrincipal principal = getCurrentUserPrincipal();
            Long currentUserId = principal.getUserId();

            List<MessageResponse> messages = chatService.getChatMessages(chatId, currentUserId);

            return ResponseEntity.ok(ApiResponse.success(messages));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch messages: " + e.getMessage()));
        }
    }

    /**
     * 获取聊天的最后一条消息
     * GET /api/chats/{chatId}/messages/last
     */
    @GetMapping("/{chatId}/messages/last")
    public ResponseEntity<ApiResponse<MessageResponse>> getLastChatMessage(@PathVariable Long chatId) {
        try {
            UserPrincipal principal = getCurrentUserPrincipal();
            Long currentUserId = principal.getUserId();

            MessageResponse message = chatService.getLastMessage(chatId, currentUserId);

            return ResponseEntity.ok(ApiResponse.success(message));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch messages: " + e.getMessage()));
        }
    }

    /**
     * 发送消息
     * POST /api/chats/{chatId}/messages
     */
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable Long chatId,
            @Valid @RequestBody MessageRequest request
    ) {
        try {
            UserPrincipal principal = getCurrentUserPrincipal();
            Long currentUserId = principal.getUserId();

            MessageResponse message = chatService.sendMessage(chatId, request, currentUserId);

            return ResponseEntity.ok(ApiResponse.success(message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to send message: " + e.getMessage()));
        }
    }

    /**
     * 标记消息为已读
     * PUT /api/chats/{chatId}/read
     */
    @PutMapping("/{chatId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long chatId) {
        try {
            UserPrincipal principal = getCurrentUserPrincipal();
            Long currentUserId = principal.getUserId();

            chatService.markMessagesAsRead(chatId, currentUserId);

            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to mark as read: " + e.getMessage()));
        }
    }

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new org.springframework.security.access.AccessDeniedException("User context missing");
        }
        return principal;
    }
}

