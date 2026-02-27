package com.segi.campusassistance.service;

import com.segi.campusassistance.dto.UserProfileResponse;
import com.segi.campusassistance.dto.UserProfileUpdateRequest;
import com.segi.campusassistance.entity.Chat;
import com.segi.campusassistance.entity.User;
import com.segi.campusassistance.repository.ChatMessageRepository;
import com.segi.campusassistance.repository.ChatRepository;
import com.segi.campusassistance.repository.ItemRepository;
import com.segi.campusassistance.repository.ItemsRepository;
import com.segi.campusassistance.repository.MarketplaceItemRepository;
import com.segi.campusassistance.repository.UserRepository;
import com.segi.campusassistance.security.UserPrincipal;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemsRepository itemsRepository;
    private final MarketplaceItemRepository marketplaceItemRepository;
    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser(UserPrincipal principal) {
        User user = getUser(principal);
        return UserProfileResponse.fromEntity(user);
    }

    @Transactional
    public UserProfileResponse updateCurrentUser(UserPrincipal principal, UserProfileUpdateRequest request) {
        User user = getUser(principal);
        user.setName(request.getFullName());
        if (request.getPhotoUrl() != null) {
            user.setPicture(StringUtils.hasText(request.getPhotoUrl()) ? request.getPhotoUrl() : null);
        }
        User saved = userRepository.save(user);
        return UserProfileResponse.fromEntity(saved);
    }

    @Transactional
    public Map<String, Object> uploadPhoto(UserPrincipal principal, MultipartFile file) throws IOException {
        User user = getUser(principal);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }

        String filename = fileStorageService.storeFile(file);
        String url = fileStorageService.generatePublicUrl(filename);

        user.setPicture(url);
        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("url", url);
        response.put("photoUrl", url);
        response.put("imageUrl", url);
        response.put("data", Map.of("url", url));

        return response;
    }

    @Transactional
    public void deleteCurrentUser(UserPrincipal principal) {
        User user = getUser(principal);
        Long userId = user.getId();

        try {
            // 1. 先删除所有 chat_messages（必须先删除子表数据）
            // 获取用户参与的所有 chats
            List<Chat> chats = chatRepository.findByOwnerIdOrRequesterId(userId, userId);
            if (!CollectionUtils.isEmpty(chats)) {
                List<Long> chatIds = chats.stream()
                        .map(Chat::getId)
                        .collect(Collectors.toList());
                
                // 先删除所有 chat_messages（子表）
                if (!chatIds.isEmpty()) {
                    chatMessageRepository.deleteByChatIdIn(chatIds);
                }
                
                // 然后删除 chats（父表）
                chatRepository.deleteAllByIdInBatch(chatIds);
            }

            // 2. 删除失物报告（items 和 items 表）
            itemRepository.deleteByUserId(userId);
            itemsRepository.deleteByUserId(userId);
            
            // 3. 删除市场商品
            marketplaceItemRepository.deleteBySellerId(userId);

            // 4. 最后删除用户本身
            userRepository.delete(user);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to delete user data: " + ex.getMessage(), ex);
        }
    }

    private User getUser(UserPrincipal principal) {
        if (principal == null || principal.getUserId() == null) {
            throw new EntityNotFoundException("User not found");
        }
        Optional<User> userOptional = userRepository.findById(principal.getUserId());
        return userOptional.orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}


