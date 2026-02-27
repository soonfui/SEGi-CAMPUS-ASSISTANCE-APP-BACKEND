package com.segi.campusassistance.service.impl;

import com.segi.campusassistance.dto.MarketplaceItemRequest;
import com.segi.campusassistance.dto.MarketplaceItemResponse;
import com.segi.campusassistance.entity.MarketplaceItem;
import com.segi.campusassistance.entity.User;
import com.segi.campusassistance.repository.MarketplaceItemRepository;
import com.segi.campusassistance.repository.UserRepository;
import com.segi.campusassistance.service.MarketplaceItemService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MarketplaceItemServiceImpl implements MarketplaceItemService {

    private final MarketplaceItemRepository marketplaceItemRepository;
    private final UserRepository userRepository;

    public MarketplaceItemServiceImpl(MarketplaceItemRepository marketplaceItemRepository, UserRepository userRepository) {
        this.marketplaceItemRepository = marketplaceItemRepository;
        this.userRepository = userRepository;
    }

    private User getUserInfo(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    @Transactional
    public MarketplaceItemResponse createItem(Long sellerId, MarketplaceItemRequest request) {
        MarketplaceItem item = new MarketplaceItem();
        item.setSellerId(sellerId);
        applyRequest(item, request);

        // 设置默认值
        if (item.getStatus() == null) {
            item.setStatus("For Sale");
        }
        if (item.getIsActive() == null) {
            item.setIsActive(true);
        }
        if (item.getViews() == null) {
            item.setViews(0);
        }

        // 如果没有提供 contactEmail，从用户表获取
        if (item.getContactEmail() == null || item.getContactEmail().isEmpty()) {
            User user = getUserInfo(sellerId);
            if (user != null && user.getEmail() != null) {
                item.setContactEmail(user.getEmail());
            }
        }

        MarketplaceItem saved = marketplaceItemRepository.save(item);
        User seller = getUserInfo(saved.getSellerId());
        return MarketplaceItemResponse.fromEntity(saved, sellerId, null, 
                seller != null ? seller.getName() : null,
                seller != null ? seller.getEmail() : null);
    }

    @Override
    @Transactional
    public MarketplaceItemResponse updateItem(Long itemId, Long userId, String userRole, MarketplaceItemRequest request) {
        MarketplaceItem item = marketplaceItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));

        // 权限检查：只有商品所有者或ADMIN可以编辑
        boolean isOwner = userId != null && item.getSellerId().equals(userId);
        boolean isAdmin = userRole != null && "ADMIN".equalsIgnoreCase(userRole);

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Not authorized to edit this item");
        }

        applyRequest(item, request);
        MarketplaceItem updated = marketplaceItemRepository.save(item);
        User seller = getUserInfo(updated.getSellerId());
        return MarketplaceItemResponse.fromEntity(updated, userId, userRole,
                seller != null ? seller.getName() : null,
                seller != null ? seller.getEmail() : null);
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId, Long userId, String userRole) {
        MarketplaceItem item = marketplaceItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));

        // 权限检查：只有商品所有者或ADMIN可以删除
        boolean isOwner = userId != null && item.getSellerId().equals(userId);
        boolean isAdmin = userRole != null && "ADMIN".equalsIgnoreCase(userRole);

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Not authorized to delete this item");
        }

        // 软删除：设置 is_active = false
        item.setIsActive(false);
        marketplaceItemRepository.save(item);
    }

    @Override
    @Transactional
    public MarketplaceItemResponse getItem(Long itemId, Long currentUserId, String currentUserRole) {
        MarketplaceItem item = marketplaceItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));

        // 增加 views
        marketplaceItemRepository.incrementViews(itemId);
        // 刷新实体以获取最新的 views 值
        item = marketplaceItemRepository.findById(itemId).orElse(item);

        User seller = getUserInfo(item.getSellerId());
        return MarketplaceItemResponse.fromEntity(item, currentUserId, currentUserRole,
                seller != null ? seller.getName() : null,
                seller != null ? seller.getEmail() : null);
    }

    @Override
    public List<MarketplaceItemResponse> getItems(String filter, Long currentUserId, String currentUserRole) {
        Specification<MarketplaceItem> specification = buildSpecification(filter, currentUserId);
        Sort sortOrder = Sort.by(Sort.Direction.DESC, "datePosted");

        return marketplaceItemRepository.findAll(specification, sortOrder)
                .stream()
                .map(item -> {
                    User seller = getUserInfo(item.getSellerId());
                    return MarketplaceItemResponse.fromEntity(item, currentUserId, currentUserRole,
                            seller != null ? seller.getName() : null,
                            seller != null ? seller.getEmail() : null);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<MarketplaceItemResponse> searchItems(String query, Long currentUserId, String currentUserRole) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        Specification<MarketplaceItem> specification = buildSearchSpecification(query.trim());
        Sort sortOrder = Sort.by(Sort.Direction.DESC, "datePosted");

        return marketplaceItemRepository.findAll(specification, sortOrder)
                .stream()
                .map(item -> {
                    User seller = getUserInfo(item.getSellerId());
                    return MarketplaceItemResponse.fromEntity(item, currentUserId, currentUserRole,
                            seller != null ? seller.getName() : null,
                            seller != null ? seller.getEmail() : null);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private void applyRequest(MarketplaceItem item, MarketplaceItemRequest request) {
        if (request.getItemName() != null) {
            item.setItemName(request.getItemName());
        }
        if (request.getCategory() != null) {
            item.setCategory(request.getCategory());
        }
        if (request.getPrice() != null) {
            item.setPrice(request.getPrice());
        }
        if (request.getCondition() != null) {
            item.setCondition(request.getCondition());
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            item.setLocation(request.getLocation());
        }
        if (request.getImageUrl() != null) {
            item.setImageUrl(request.getImageUrl());
        }
        if (request.getContactEmail() != null) {
            item.setContactEmail(request.getContactEmail());
        }
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }
        if (request.getIsActive() != null) {
            item.setIsActive(request.getIsActive());
        }
    }

    private Specification<MarketplaceItem> buildSpecification(String filter, Long currentUserId) {
        Specification<MarketplaceItem> spec = Specification.where(null);

        // 默认只返回 is_active = true 的商品
        spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), true));

        // 根据 filter 参数筛选
        if (StringUtils.hasText(filter)) {
            if ("My Posts".equals(filter) && currentUserId != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("sellerId"), currentUserId));
            } else if ("Others' Posts".equals(filter) && currentUserId != null) {
                spec = spec.and((root, query, cb) -> cb.notEqual(root.get("sellerId"), currentUserId));
            }
            // "All Items" 或未提供 filter 时，返回所有商品（已过滤 is_active = true）
        }

        return spec;
    }

    private Specification<MarketplaceItem> buildSearchSpecification(String query) {
        Specification<MarketplaceItem> spec = Specification.where(null);

        // 只返回 is_active = true 的商品
        spec = spec.and((root, query1, cb) -> cb.equal(root.get("isActive"), true));

        // 在 item_name 和 description 字段中搜索
        String keyword = "%" + query.toLowerCase() + "%";
        spec = spec.and((root, query1, cb) -> {
            var nameMatch = cb.like(cb.lower(root.get("itemName")), keyword);
            var descriptionMatch = cb.like(cb.lower(root.get("description")), keyword);
            return cb.or(nameMatch, descriptionMatch);
        });

        return spec;
    }
}

