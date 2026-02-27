package com.segi.campusassistance.service.impl;

import com.segi.campusassistance.dto.ItemsRequest;
import com.segi.campusassistance.dto.ItemsResponse;
import com.segi.campusassistance.entity.Items;
import com.segi.campusassistance.entity.User;
import com.segi.campusassistance.repository.ItemsRepository;
import com.segi.campusassistance.repository.UserRepository;
import com.segi.campusassistance.service.ItemsService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemsServiceImpl implements ItemsService {

    private final ItemsRepository itemsRepository;
    private final UserRepository userRepository;

    public ItemsServiceImpl(ItemsRepository itemsRepository, UserRepository userRepository) {
        this.itemsRepository = itemsRepository;
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
    public ItemsResponse createItem(ItemsRequest request) {
        Items items = new Items();
        items.setTitle(request.getTitle());
        items.setDescription(request.getDescription());
        items.setType(request.getType().toLowerCase()); // 确保小写
        items.setLocation(request.getLocation());
        items.setDateLost(request.getDateLost());
        items.setCategory(request.getCategory());
        items.setImage(request.getImage());
        items.setContact(request.getContact());
        items.setUserId(request.getUserId());
        
        Items savedItem = itemsRepository.save(items);
        return ItemsResponse.fromEntity(savedItem);
    }

    @Override
    public List<ItemsResponse> getAllItems(Long currentUserId, String currentUserRole) {
        return itemsRepository.findAll().stream()
                .map(item -> {
                    String userName = getUserName(item.getUserId());
                    return ItemsResponse.fromEntity(item, currentUserId, currentUserRole, userName);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemsResponse> getItemsByUserId(Long userId, Long currentUserId, String currentUserRole) {
        return itemsRepository.findByUserId(userId).stream()
                .map(item -> {
                    String userName = getUserName(item.getUserId());
                    return ItemsResponse.fromEntity(item, currentUserId, currentUserRole, userName);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemsResponse getItemById(Long id, Long currentUserId, String currentUserRole) {
        Items items = itemsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
        String userName = getUserName(items.getUserId());
        return ItemsResponse.fromEntity(items, currentUserId, currentUserRole, userName);
    }

    @Override
    public ItemsResponse updateItem(Long id, ItemsRequest request, Long currentUserId, String currentUserRole) {
        Items items = itemsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
        
        // 权限检查：只有帖子所有者或ADMIN可以编辑
        boolean isOwner = currentUserId != null && items.getUserId().equals(currentUserId);
        boolean isAdmin = currentUserRole != null && "ADMIN".equalsIgnoreCase(currentUserRole);
        
        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Not authorized to edit this item");
        }
        
        items.setTitle(request.getTitle());
        items.setDescription(request.getDescription());
        items.setType(request.getType().toLowerCase()); // 确保小写
        items.setLocation(request.getLocation());
        items.setDateLost(request.getDateLost());
        items.setCategory(request.getCategory());
        items.setImage(request.getImage());
        items.setContact(request.getContact());
        
        Items updatedItem = itemsRepository.save(items);
        String userName = getUserName(updatedItem.getUserId());
        return ItemsResponse.fromEntity(updatedItem, currentUserId, currentUserRole, userName);
    }

    @Override
    public void deleteItem(Long id, Long currentUserId, String currentUserRole) {
        Items items = itemsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
        
        // 权限检查：只有帖子所有者或ADMIN可以删除
        boolean isOwner = currentUserId != null && items.getUserId().equals(currentUserId);
        boolean isAdmin = currentUserRole != null && "ADMIN".equalsIgnoreCase(currentUserRole);
        
        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Not authorized to delete this item");
        }
        
        itemsRepository.delete(items);
    }
}

