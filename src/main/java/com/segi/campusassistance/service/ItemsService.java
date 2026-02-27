package com.segi.campusassistance.service;

import com.segi.campusassistance.dto.ItemsRequest;
import com.segi.campusassistance.dto.ItemsResponse;

import java.util.List;

public interface ItemsService {

    ItemsResponse createItem(ItemsRequest request);

    List<ItemsResponse> getAllItems(Long currentUserId, String currentUserRole);

    List<ItemsResponse> getItemsByUserId(Long userId, Long currentUserId, String currentUserRole);

    ItemsResponse getItemById(Long id, Long currentUserId, String currentUserRole);

    ItemsResponse updateItem(Long id, ItemsRequest request, Long currentUserId, String currentUserRole);

    void deleteItem(Long id, Long currentUserId, String currentUserRole);
}

