package com.segi.campusassistance.service;

import com.segi.campusassistance.dto.MarketplaceItemRequest;
import com.segi.campusassistance.dto.MarketplaceItemResponse;

import java.util.List;

public interface MarketplaceItemService {

    MarketplaceItemResponse createItem(Long sellerId, MarketplaceItemRequest request);

    MarketplaceItemResponse updateItem(Long itemId, Long userId, String userRole, MarketplaceItemRequest request);

    void deleteItem(Long itemId, Long userId, String userRole);

    MarketplaceItemResponse getItem(Long itemId, Long currentUserId, String currentUserRole);

    List<MarketplaceItemResponse> getItems(String filter, Long currentUserId, String currentUserRole);

    List<MarketplaceItemResponse> searchItems(String query, Long currentUserId, String currentUserRole);
}

