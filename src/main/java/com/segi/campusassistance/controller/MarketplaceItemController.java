package com.segi.campusassistance.controller;

import com.segi.campusassistance.dto.ApiResponse;
import com.segi.campusassistance.dto.MarketplaceItemRequest;
import com.segi.campusassistance.dto.MarketplaceItemResponse;
import com.segi.campusassistance.security.UserPrincipal;
import com.segi.campusassistance.service.FileStorageService;
import com.segi.campusassistance.service.MarketplaceItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/marketplace/items")
@Validated
public class MarketplaceItemController {

    private final MarketplaceItemService marketplaceItemService;
    private final FileStorageService fileStorageService;

    public MarketplaceItemController(MarketplaceItemService marketplaceItemService, FileStorageService fileStorageService) {
        this.marketplaceItemService = marketplaceItemService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * GET /api/marketplace/items
     * 获取所有商品列表（支持筛选）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MarketplaceItemResponse>>> getItems(
            @RequestParam(required = false) String filter,
            Authentication authentication
    ) {
        try {
            Long currentUserId = null;
            String currentUserRole = null;

            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                currentUserId = principal.getUserId();
                currentUserRole = principal.getRole();
            }

            List<MarketplaceItemResponse> items = marketplaceItemService.getItems(filter, currentUserId, currentUserRole);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch marketplace items: " + e.getMessage()));
        }
    }

    /**
     * GET /api/marketplace/items/{itemId}
     * 获取单个商品详情
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<ApiResponse<MarketplaceItemResponse>> getItem(
            @PathVariable Long itemId,
            Authentication authentication
    ) {
        try {
            Long currentUserId = null;
            String currentUserRole = null;

            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                currentUserId = principal.getUserId();
                currentUserRole = principal.getRole();
            }

            MarketplaceItemResponse response = marketplaceItemService.getItem(itemId, currentUserId, currentUserRole);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Item not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch item details: " + e.getMessage()));
        }
    }

    /**
     * GET /api/marketplace/items/search?q={query}
     * 搜索商品
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MarketplaceItemResponse>>> searchItems(
            @RequestParam(required = false) String q,
            Authentication authentication
    ) {
        try {
            if (q == null || q.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Search keyword cannot be empty"));
            }

            Long currentUserId = null;
            String currentUserRole = null;

            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                currentUserId = principal.getUserId();
                currentUserRole = principal.getRole();
            }

            List<MarketplaceItemResponse> items = marketplaceItemService.searchItems(q.trim(), currentUserId, currentUserRole);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Search failed: " + e.getMessage()));
        }
    }

    /**
     * POST /api/marketplace/items/upload
     * 上传商品图片
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            // 验证文件是否存在
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File cannot be empty"));
            }

            // 验证文件类型（只允许图片）
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File must be an image"));
            }

            // 保存文件
            String filename = fileStorageService.storeFile(file);
            if (filename == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to save the file"));
            }

            // 生成公共URL（使用请求的Host动态生成）
            String imageUrl = fileStorageService.generatePublicUrl(filename, request);

            // 返回响应
            Map<String, String> response = new HashMap<>();
            response.put("url", imageUrl);
            response.put("imageUrl", imageUrl);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("File upload failed: " + e.getMessage()));
        }
    }

    /**
     * POST /api/marketplace/items
     * 创建新商品
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<MarketplaceItemResponse>> createItem(
            @Valid @RequestBody MarketplaceItemRequest request
    ) {
        try {
            UserPrincipal principal = getCurrentUserPrincipal();
            MarketplaceItemResponse response = marketplaceItemService.createItem(principal.getUserId(), request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not logged in or session expired"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create item: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/marketplace/items/{itemId}
     * 更新商品
     */
    @PutMapping(value = "/{itemId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<MarketplaceItemResponse>> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody MarketplaceItemRequest request
    ) {
        try {
            UserPrincipal principal = getCurrentUserPrincipal();
            MarketplaceItemResponse response = marketplaceItemService.updateItem(
                    itemId, principal.getUserId(), principal.getRole(), request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Item not found"));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Not authorized to edit this item"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update item: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/marketplace/items/{itemId}
     * 删除商品（软删除）
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long itemId) {
        try {
            UserPrincipal principal = getCurrentUserPrincipal();
            marketplaceItemService.deleteItem(itemId, principal.getUserId(), principal.getRole());
            return ResponseEntity.ok(ApiResponse.success(null, "Item deleted successfully"));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Item not found"));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Not authorized to delete this item"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete item: " + e.getMessage()));
        }
    }

    /**
     * 获取当前用户信息
     */
    private UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new org.springframework.security.access.AccessDeniedException("User context missing");
        }
        return principal;
    }
}

