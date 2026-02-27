package com.segi.campusassistance.controller;

import com.segi.campusassistance.dto.ApiResponse;
import com.segi.campusassistance.dto.ItemRequest;
import com.segi.campusassistance.dto.ItemResponse;
import com.segi.campusassistance.security.UserPrincipal;
import com.segi.campusassistance.service.FileStorageService;
import com.segi.campusassistance.service.ItemService;
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

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
@Validated
public class ItemController {

    private final ItemService itemService;
    private final FileStorageService fileStorageService;

    public ItemController(ItemService itemService, FileStorageService fileStorageService) {
        this.itemService = itemService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * POST /api/items/upload
     * 上传失物报告图片
     * 需要 JWT 认证
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            // 验证用户认证
            getCurrentUserPrincipal();

            // 验证文件是否存在
            if (file == null || file.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "File cannot be empty");
                return ResponseEntity.badRequest().body(error);
            }

            // 验证文件类型（只允许图片格式：JPEG, PNG, JPG）
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "File must be an image (JPEG, PNG, JPG)");
                return ResponseEntity.badRequest().body(error);
            }

            // 验证文件扩展名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null) {
                String lowerFilename = originalFilename.toLowerCase();
                if (!lowerFilename.endsWith(".jpg") && !lowerFilename.endsWith(".jpeg") 
                    && !lowerFilename.endsWith(".png")) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Only JPEG, PNG, and JPG formats are supported");
                    return ResponseEntity.badRequest().body(error);
                }
            }

            // 验证文件大小（10MB 限制已在 application.properties 中配置）
            long maxSize = 10 * 1024 * 1024; // 10MB
            if (file.getSize() > maxSize) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "File size exceeds the maximum limit of 10MB");
                return ResponseEntity.badRequest().body(error);
            }

            // 保存文件
            String filename = fileStorageService.storeFile(file);
            if (filename == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Failed to save the file");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            }

            // 生成公共URL（使用请求的Host动态生成）
            String imageUrl = fileStorageService.generatePublicUrl(filename, request);

            // 返回响应（支持两种格式：url 和 imageUrl）
            Map<String, Object> response = new HashMap<>();
            response.put("url", imageUrl);
            response.put("imageUrl", imageUrl);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (org.springframework.security.access.AccessDeniedException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Authentication required. Please provide a valid JWT token.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "File upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "File upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping(value = "/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ItemResponse>> createItemJson(
            @RequestBody ItemRequest request) {

        try {
            Long userId = getCurrentUserId();

            ItemResponse response = itemService.createItemWithoutFile(userId, request);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create item: " + e.getMessage()));
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ItemResponse>> createItem(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String location,
            @RequestParam(required = false) String contactInfo,
            @RequestParam String category,
            @RequestParam String date,
            @RequestParam String status,
            @RequestParam(required = false) MultipartFile image) {
        try {
            // 验证必填字段
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Name cannot be empty"));
            }
            if (description == null || description.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Description cannot be empty"));
            }
            if (location == null || location.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Location cannot be empty"));
            }
            if (category == null || category.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Category cannot be empty"));
            }
            if (date == null || date.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Date cannot be empty"));
            }
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Status cannot be empty"));
            }

            // 验证status
            String upperStatus = status.trim().toUpperCase();
            if (!"LOST".equals(upperStatus) && !"FOUND".equals(upperStatus)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Status must be LOST or FOUND"));
            }

            // 解析日期
            LocalDate parsedDate;
            try {
                parsedDate = LocalDate.parse(date);
            } catch (java.time.format.DateTimeParseException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid date format, please use YYYY-MM-DD"));
            }

            // 创建ItemRequest对象
            ItemRequest request = new ItemRequest();
            request.setName(name.trim());
            request.setDescription(description.trim());
            request.setLocation(location.trim());
            request.setContactInfo(contactInfo != null ? contactInfo.trim() : null);
            request.setCategory(category.trim());
            request.setDate(parsedDate);
            request.setStatus(upperStatus);

            ItemResponse response = itemService.createItemWithFile(getCurrentUserId(), request, image);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create item: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getItems(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "dateDesc") String sort,
            Authentication authentication
    ) {
        Long currentUserId = null;
        String currentUserRole = null;
        
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            currentUserId = principal.getUserId();
            currentUserRole = principal.getRole();
        }
        
        List<ItemResponse> items = itemService.getItems(search, status, category, sort, currentUserId, currentUserRole);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> searchItems(
            @RequestParam(required = false) String q,
            Authentication authentication
    ) {
        try {
            // 如果 q 为空或只包含空白，返回400错误
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
            
            // 使用 search 参数调用现有的搜索功能
            List<ItemResponse> items = itemService.getItems(q.trim(), null, null, "dateDesc", currentUserId, currentUserRole);
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Search failed: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> getItem(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long currentUserId = null;
        String currentUserRole = null;
        
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            currentUserId = principal.getUserId();
            currentUserRole = principal.getRole();
        }
        
        ItemResponse response = itemService.getItem(id, currentUserId, currentUserRole);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ItemResponse>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequest request
    ) {
        UserPrincipal principal = getCurrentUserPrincipal();
        ItemResponse response = itemService.updateItem(id, principal.getUserId(), principal.getRole(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ItemResponse>> updateItemWithFile(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String location,
            @RequestParam(required = false) String contactInfo,
            @RequestParam String category,
            @RequestParam String date,
            @RequestParam String status,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) MultipartFile image) {
        try {
            // 验证必填字段
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Name cannot be empty"));
            }
            if (description == null || description.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Description cannot be empty"));
            }
            if (location == null || location.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Location cannot be empty"));
            }
            if (category == null || category.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Category cannot be empty"));
            }
            if (date == null || date.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Date cannot be empty"));
            }
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Status cannot be empty"));
            }

            // 验证status
            String upperStatus = status.trim().toUpperCase();
            if (!"LOST".equals(upperStatus) && !"FOUND".equals(upperStatus)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Status must be LOST or FOUND"));
            }

            // 解析日期
            LocalDate parsedDate;
            try {
                parsedDate = LocalDate.parse(date);
            } catch (java.time.format.DateTimeParseException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid date format, please use YYYY-MM-DD"));
            }

            // 创建ItemRequest对象
            ItemRequest request = new ItemRequest();
            request.setName(name.trim());
            request.setDescription(description.trim());
            request.setLocation(location.trim());
            request.setContactInfo(contactInfo != null ? contactInfo.trim() : null);
            request.setCategory(category.trim());
            request.setDate(parsedDate);
            request.setStatus(upperStatus);
            
            // 如果提供了新图片，使用新图片；否则使用原有的 imageUrl
            if (image == null || image.isEmpty()) {
                request.setImageUrl(imageUrl);
            }

            UserPrincipal principal = getCurrentUserPrincipal();
            ItemResponse response = itemService.updateItemWithFile(id, principal.getUserId(), principal.getRole(), request, image);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update item: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        UserPrincipal principal = getCurrentUserPrincipal();
        itemService.deleteItem(id, principal.getUserId(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new org.springframework.security.access.AccessDeniedException("User context missing");
        }
        return principal;
    }

    private Long getCurrentUserId() {
        return getCurrentUserPrincipal().getUserId();
    }
}
