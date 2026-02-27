package com.segi.campusassistance.controller;

import com.segi.campusassistance.dto.ApiResponse;
import com.segi.campusassistance.dto.ItemsRequest;
import com.segi.campusassistance.dto.ItemsResponse;
import com.segi.campusassistance.security.UserPrincipal;
import com.segi.campusassistance.service.ItemsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemsController {

    private final ItemsService itemsService;

    public ItemsController(ItemsService itemsService) {
        this.itemsService = itemsService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createItem(@RequestBody ItemsRequest request) {
        try {
            itemsService.createItem(request);
            
            // 返回简单JSON格式
            java.util.Map<String, String> response = new java.util.HashMap<>();
            response.put("status", "success");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemsResponse>>> getAllItems(Authentication authentication) {
        try {
            Long currentUserId = null;
            String currentUserRole = null;
            
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                currentUserId = principal.getUserId();
                currentUserRole = principal.getRole();
                System.out.println("DEBUG: Authenticated user - userId: " + currentUserId + ", role: " + currentUserRole);
            } else {
                System.out.println("DEBUG: No authentication or invalid principal. Authentication: " + authentication);
            }
            
            List<ItemsResponse> items = itemsService.getAllItems(currentUserId, currentUserRole);
            System.out.println("DEBUG: Returning " + items.size() + " items with currentUserId: " + currentUserId);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch items: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<ApiResponse<List<ItemsResponse>>> getItemsByUserId(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            Long currentUserId = null;
            String currentUserRole = null;
            
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                currentUserId = principal.getUserId();
                currentUserRole = principal.getRole();
            }
            
            List<ItemsResponse> items = itemsService.getItemsByUserId(id, currentUserId, currentUserRole);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch user's items: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemsResponse>> getItemById(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            Long currentUserId = null;
            String currentUserRole = null;
            
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                currentUserId = principal.getUserId();
                currentUserRole = principal.getRole();
            }
            
            ItemsResponse item = itemsService.getItemById(id, currentUserId, currentUserRole);
            return ResponseEntity.ok(ApiResponse.success(item));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Item not found"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch item: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemsResponse>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemsRequest request,
            Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Unauthorized"));
            }
            
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            Long currentUserId = principal.getUserId();
            String currentUserRole = principal.getRole();
            
            ItemsResponse response = itemsService.updateItem(id, request, currentUserId, currentUserRole);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Item not found"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update item: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Unauthorized"));
            }
            
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            Long currentUserId = principal.getUserId();
            String currentUserRole = principal.getRole();
            
            itemsService.deleteItem(id, currentUserId, currentUserRole);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Item not found"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete item: " + e.getMessage()));
        }
    }
}

