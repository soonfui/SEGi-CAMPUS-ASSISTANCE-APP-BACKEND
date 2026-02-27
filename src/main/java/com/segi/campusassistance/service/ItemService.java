package com.segi.campusassistance.service;

import com.segi.campusassistance.dto.ItemRequest;
import com.segi.campusassistance.dto.ItemResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ItemService {

    ItemResponse createItem(Long userId, ItemRequest request);

    ItemResponse createItemWithFile(Long userId, ItemRequest request, MultipartFile image);

    ItemResponse createItemWithoutFile(Long userId, ItemRequest request);


    ItemResponse updateItem(Long itemId, Long userId, String userRole, ItemRequest request);

    ItemResponse updateItemWithFile(Long itemId, Long userId, String userRole, ItemRequest request, MultipartFile image);

    void deleteItem(Long itemId, Long userId, String userRole);

    ItemResponse getItem(Long itemId, Long currentUserId, String currentUserRole);

    List<ItemResponse> getItems(String search, String status, String category, String sort, Long currentUserId, String currentUserRole);
}

