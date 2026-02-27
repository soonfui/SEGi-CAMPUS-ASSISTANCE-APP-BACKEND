package com.segi.campusassistance.service.impl;

import com.segi.campusassistance.dto.ItemRequest;
import com.segi.campusassistance.dto.ItemResponse;
import com.segi.campusassistance.entity.Item;
import com.segi.campusassistance.repository.ItemRepository;
import com.segi.campusassistance.service.FileStorageService;
import com.segi.campusassistance.service.ItemService;
import com.segi.campusassistance.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import com.segi.campusassistance.entity.User;


import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    public ItemServiceImpl(ItemRepository itemRepository, FileStorageService fileStorageService, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    private String getUserName(Long userId) {
        if (userId == null) {
            return null;
        }
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.map(User::getName).orElse(null);
    }

    @Override
    public ItemResponse createItem(Long userId, ItemRequest request) {
        Item item = new Item();
        item.setUserId(userId);
        applyRequest(item, request);
        return ItemResponse.fromEntity(itemRepository.save(item));
    }

    @Override
    public ItemResponse createItemWithFile(Long userId, ItemRequest request, MultipartFile image) {
        Item item = new Item();
        item.setUserId(userId);
        
        // 处理文件上传
        if (image != null && !image.isEmpty()) {
            try {
                String filename = fileStorageService.storeFile(image);
                String imageUrl = fileStorageService.generatePublicUrl(filename);
                request.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("File upload failed: " + e.getMessage(), e);
            }
        }
        
        applyRequest(item, request);
        return ItemResponse.fromEntity(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemResponse createItemWithoutFile(Long userId, ItemRequest request) {

        Item item = new Item();
        item.setUserId(userId);
        applyRequest(item, request);
        // createdAt 和 updatedAt 会通过 @PrePersist 自动设置

        Item saved = itemRepository.save(item);

        return ItemResponse.fromEntity(saved);
    }




    @Override
    public ItemResponse updateItem(Long itemId, Long userId, String userRole, ItemRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));
        
        // 权限检查：只有帖子所有者或ADMIN可以编辑
        boolean isOwner = userId != null && item.getUserId().equals(userId);
        boolean isAdmin = userRole != null && "ADMIN".equalsIgnoreCase(userRole);
        
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Not authorized to edit this item");
        }
        
        applyRequest(item, request);
        String userName = getUserName(item.getUserId());
        Item updatedItem = itemRepository.save(item);
        return ItemResponse.fromEntity(updatedItem, userId, userRole, userName);
    }

    @Override
    public ItemResponse updateItemWithFile(Long itemId, Long userId, String userRole, ItemRequest request, MultipartFile image) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));
        
        // 权限检查：只有帖子所有者或ADMIN可以编辑
        boolean isOwner = userId != null && item.getUserId().equals(userId);
        boolean isAdmin = userRole != null && "ADMIN".equalsIgnoreCase(userRole);
        
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Not authorized to edit this item");
        }
        
        // 处理文件上传（如果提供了新图片）
        if (image != null && !image.isEmpty()) {
            try {
                String filename = fileStorageService.storeFile(image);
                String imageUrl = fileStorageService.generatePublicUrl(filename);
                request.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("File upload failed: " + e.getMessage(), e);
            }
        }
        
        applyRequest(item, request);
        String userName = getUserName(item.getUserId());
        Item updatedItem = itemRepository.save(item);
        return ItemResponse.fromEntity(updatedItem, userId, userRole, userName);
    }

    @Override
    public void deleteItem(Long itemId, Long userId, String userRole) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));
        
        // 权限检查：只有帖子所有者或ADMIN可以删除
        boolean isOwner = userId != null && item.getUserId().equals(userId);
        boolean isAdmin = userRole != null && "ADMIN".equalsIgnoreCase(userRole);
        
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Not authorized to delete this item");
        }
        
        itemRepository.delete(item);
    }

    @Override
    public ItemResponse getItem(Long itemId, Long currentUserId, String currentUserRole) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));
        String userName = getUserName(item.getUserId());
        return ItemResponse.fromEntity(item, currentUserId, currentUserRole, userName);
    }

    @Override
    public List<ItemResponse> getItems(String search, String status, String category, String sort, Long currentUserId, String currentUserRole) {
        Specification<Item> specification = buildSpecification(search, status, category);
        Sort sortOrder = resolveSort(sort);
        return itemRepository.findAll(specification, sortOrder)
                .stream()
                .map(item -> {
                    String userName = getUserName(item.getUserId());
                    return ItemResponse.fromEntity(item, currentUserId, currentUserRole, userName);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private void applyRequest(Item item, ItemRequest request) {
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setStatus(request.getStatus().toUpperCase());
        item.setLocation(request.getLocation());
        item.setContactInfo(request.getContactInfo());
        item.setDate(request.getDate());
        item.setImageUrl(request.getImageUrl());
    }

    private Specification<Item> buildSpecification(String search, String status, String category) {
        Specification<Item> spec = Specification.where(null);

        if (StringUtils.hasText(search)) {
            String keyword = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                // 基础搜索字段
                var nameMatch = cb.like(cb.lower(root.get("name")), keyword);
                var descriptionMatch = cb.like(cb.lower(root.get("description")), keyword);
                var locationMatch = cb.like(cb.lower(root.get("location")), keyword);
                
                // contactInfo 搜索（只有当字段不为null时才搜索）
                var contactInfoMatch = cb.and(
                        cb.isNotNull(root.get("contactInfo")),
                        cb.like(cb.lower(root.get("contactInfo")), keyword)
                );
                
                // 只要任何一个字段匹配即可
                return cb.or(nameMatch, descriptionMatch, locationMatch, contactInfoMatch);
            });
        }

        if (StringUtils.hasText(status)) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.upper(root.get("status")), status.trim().toUpperCase()));
        }

        if (StringUtils.hasText(category)) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category"), category.trim()));
        }

        return spec;
    }

    private Sort resolveSort(String sort) {
        if ("dateDesc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "date");
        }
        if ("dateAsc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.ASC, "date");
        }
        return Sort.by(Sort.Direction.DESC, "date");
    }
}

