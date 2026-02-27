# Marketplace CRUD 后端实现 Prompt

## 概述

需要为 SEGi Campus Assistance App 实现 Marketplace（二手市场）功能的完整 CRUD 后端 API。这是一个 Spring Boot 项目，使用 MySQL 数据库，JWT 认证。

## 数据库表结构

```sql
CREATE TABLE marketplace_items (
  item_id INT AUTO_INCREMENT PRIMARY KEY,
  seller_id INT NOT NULL,
  item_name VARCHAR(100) NOT NULL,
  category VARCHAR(50),
  price DECIMAL(10,2),
  item_condition ENUM('New','Like New','Good','Fair','Poor') DEFAULT 'Good',
  description TEXT,
  location VARCHAR(200),
  image_url VARCHAR(255),
  contact_email VARCHAR(100),
  status ENUM('For Sale','Sold') DEFAULT 'For Sale',
  views INT DEFAULT 0,
  is_active BOOLEAN DEFAULT 1,
  date_posted TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (seller_id) REFERENCES users(user_id)
);
```

**重要说明**：
- 数据库表 `users` 的主键字段是 `user_id`（根据外键约束推断）
- 但项目中的 `User` 实体类使用 `id` 字段，JPA 会自动映射
- 如果数据库表使用 `user_id` 作为主键，需要在 `User` 实体中添加 `@Column(name = "user_id")` 注解，或者确保数据库表的主键字段名与实体类一致
- `condition` 字段的 ENUM 值：'New', 'Like New', 'Good', 'Fair', 'Poor'（注意：SQL 中默认值是 'Used'，但 ENUM 中没有这个值，需要修正为 'Good' 或其他有效值）

## 项目结构参考

请参考以下现有代码文件，保持代码风格一致：

- **Controller**: `src/main/java/com/segi/campusassistance/controller/ItemController.java`
- **Service**: `src/main/java/com/segi/campusassistance/service/ItemService.java` 和 `ItemServiceImpl.java`
- **Entity**: `src/main/java/com/segi/campusassistance/entity/Item.java`
- **DTO**: `src/main/java/com/segi/campusassistance/dto/ItemRequest.java` 和 `ItemResponse.java`
- **Repository**: `src/main/java/com/segi/campusassistance/repository/ItemRepository.java`
- **文件上传**: `src/main/java/com/segi/campusassistance/service/FileStorageService.java`
- **响应格式**: `src/main/java/com/segi/campusassistance/dto/ApiResponse.java`
- **认证**: `src/main/java/com/segi/campusassistance/security/UserPrincipal.java`

## 需要的 API 端点

### 1. GET /api/marketplace/items

**功能**：获取所有商品列表（支持筛选）

**请求参数**：
- `filter` (可选, query parameter): 
  - `All Items` - 返回所有商品
  - `My Posts` - 只返回当前登录用户的商品
  - `Others' Posts` - 只返回其他用户的商品

**请求头**：
- `Authorization: Bearer <JWT_TOKEN>` (可选，但建议提供以获取权限信息)

**响应格式**：
```json
{
  "success": true,
  "data": [
    {
      "itemId": 1,
      "item_id": 1,
      "sellerId": 5,
      "seller_id": 5,
      "itemName": "iPhone 13 Pro Max",
      "item_name": "iPhone 13 Pro Max",
      "category": "Electronics",
      "price": 3500.00,
      "condition": "Like New",
      "description": "iPhone 13 Pro Max in excellent condition...",
      "location": "Kota Damansara, Selangor",
      "imageUrl": "https://example.com/image.jpg",
      "image_url": "https://example.com/image.jpg",
      "contactEmail": "seller@example.com",
      "contact_email": "seller@example.com",
      "status": "For Sale",
      "views": 42,
      "isActive": true,
      "is_active": true,
      "datePosted": "2024-01-15T10:30:00",
      "date_posted": "2024-01-15T10:30:00",
      "canEdit": true,
      "canDelete": true,
      "sellerName": "Ahmad Hassan",
      "seller_name": "Ahmad Hassan",
      "sellerEmail": "ahmad@students.segi.edu.my",
      "seller_email": "ahmad@students.segi.edu.my"
    }
  ]
}
```

**权限逻辑**：
- 如果提供了 JWT token，计算 `canEdit` 和 `canDelete`：
  - `canEdit = true` 如果当前用户是商品所有者 OR 用户角色是 ADMIN
  - `canDelete = true` 如果当前用户是商品所有者 OR 用户角色是 ADMIN
- 如果没有 token，`canEdit` 和 `canDelete` 都是 `false`

**查询逻辑**：
- 如果 `filter = "My Posts"`：只返回 `seller_id = 当前用户ID` 的商品
- 如果 `filter = "Others' Posts"`：只返回 `seller_id != 当前用户ID` 的商品
- 如果 `filter = "All Items"` 或未提供：返回所有商品
- 默认只返回 `is_active = true` 的商品
- 按 `date_posted DESC` 排序（最新的在前）

**实现参考**：
```java
@GetMapping
public ResponseEntity<ApiResponse<List<MarketplaceItemResponse>>> getItems(
        @RequestParam(required = false) String filter,
        Authentication authentication
) {
    Long currentUserId = null;
    String currentUserRole = null;
    
    if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        currentUserId = principal.getUserId();
        currentUserRole = principal.getRole();
    }
    
    List<MarketplaceItemResponse> items = marketplaceItemService.getItems(
        filter, currentUserId, currentUserRole
    );
    return ResponseEntity.ok(ApiResponse.success(items));
}
```

---

### 2. GET /api/marketplace/items/{itemId}

**功能**：获取单个商品详情

**路径参数**：
- `itemId` (int): 商品 ID

**请求头**：
- `Authorization: Bearer <JWT_TOKEN>` (可选)

**响应格式**：
```json
{
  "success": true,
  "data": {
    "itemId": 1,
    "sellerId": 5,
    "itemName": "iPhone 13 Pro Max",
    "category": "Electronics",
    "price": 3500.00,
    "condition": "Like New",
    "description": "iPhone 13 Pro Max in excellent condition...",
    "location": "Kota Damansara, Selangor",
    "imageUrl": "https://example.com/image.jpg",
    "contactEmail": "seller@example.com",
    "status": "For Sale",
    "views": 42,
    "isActive": true,
    "datePosted": "2024-01-15T10:30:00",
    "canEdit": true,
    "canDelete": true,
    "sellerName": "Ahmad Hassan",
    "sellerEmail": "ahmad@students.segi.edu.my"
  }
}
```

**权限逻辑**：同列表接口

**其他逻辑**：
- 访问商品详情时，`views` 字段自动 +1（使用原子操作：`UPDATE marketplace_items SET views = views + 1 WHERE item_id = ?`）
- 如果商品不存在，返回 404

**实现参考**：
```java
@GetMapping("/{itemId}")
public ResponseEntity<ApiResponse<MarketplaceItemResponse>> getItem(
        @PathVariable Long itemId,
        Authentication authentication
) {
    Long currentUserId = null;
    String currentUserRole = null;
    
    if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        currentUserId = principal.getUserId();
        currentUserRole = principal.getRole();
    }
    
    MarketplaceItemResponse response = marketplaceItemService.getItem(
        itemId, currentUserId, currentUserRole
    );
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

---

### 3. GET /api/marketplace/items/search?q={query}

**功能**：搜索商品

**查询参数**：
- `q` (string): 搜索关键词

**请求头**：
- `Authorization: Bearer <JWT_TOKEN>` (可选)

**响应格式**：同列表接口（返回匹配的商品列表）

**搜索逻辑**：
- 在 `item_name` 和 `description` 字段中搜索关键词
- 使用 LIKE 查询：`%query%`
- 只返回 `is_active = true` 的商品
- 按 `date_posted DESC` 排序

**实现参考**：
```java
@GetMapping("/search")
public ResponseEntity<ApiResponse<List<MarketplaceItemResponse>>> searchItems(
        @RequestParam(required = false) String q,
        Authentication authentication
) {
    if (q == null || q.trim().isEmpty()) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("搜索关键词不能为空"));
    }
    
    Long currentUserId = null;
    String currentUserRole = null;
    
    if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        currentUserId = principal.getUserId();
        currentUserRole = principal.getRole();
    }
    
    List<MarketplaceItemResponse> items = marketplaceItemService.searchItems(
        q.trim(), currentUserId, currentUserRole
    );
    return ResponseEntity.ok(ApiResponse.success(items));
}
```

---

### 4. POST /api/marketplace/items/upload

**功能**：上传商品图片

**请求类型**：`multipart/form-data`

**请求头**：
- `Authorization: Bearer <JWT_TOKEN>` (必需)

**请求体**：
- `file` (File): 图片文件

**响应格式**：
```json
{
  "url": "http://localhost:8081/uploads/marketplace_image_123456.jpg",
  "imageUrl": "http://localhost:8081/uploads/marketplace_image_123456.jpg"
}
```

**逻辑**：
- 验证文件类型（只允许图片：image/jpeg, image/png, image/gif, image/webp）
- 验证文件大小（限制在 10MB 以内，参考 `application.properties` 中的配置）
- 使用 `FileStorageService.storeFile()` 保存文件
- 使用 `FileStorageService.generatePublicUrl()` 生成公共 URL
- 返回可访问的 URL

**实现参考**：
```java
@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
    try {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("文件不能为空"));
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("文件必须是图片类型"));
        }
        
        String filename = fileStorageService.storeFile(file);
        if (filename == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("文件保存失败"));
        }
        
        String imageUrl = fileStorageService.generatePublicUrl(filename);
        
        Map<String, String> response = new HashMap<>();
        response.put("url", imageUrl);
        response.put("imageUrl", imageUrl);
        
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("文件上传失败: " + e.getMessage()));
    }
}
```

---

### 5. POST /api/marketplace/items

**功能**：创建新商品

**请求头**：
- `Content-Type: application/json`
- `Authorization: Bearer <JWT_TOKEN>` (必需)

**请求体**：
```json
{
  "itemName": "iPhone 13 Pro Max",
  "category": "Electronics",
  "price": 3500.00,
  "condition": "Like New",
  "description": "iPhone 13 Pro Max in excellent condition...",
  "location": "Kota Damansara, Selangor",
  "imageUrl": "http://localhost:8081/uploads/image.jpg",
  "contactEmail": "seller@example.com",
  "status": "For Sale",
  "isActive": true
}
```

**响应格式**：
```json
{
  "success": true,
  "data": {
    "itemId": 1,
    "sellerId": 5,
    "itemName": "iPhone 13 Pro Max",
    ...
  }
}
```

**逻辑**：
- 从 JWT token 中获取当前用户 ID 作为 `seller_id`（使用 `UserPrincipal`）
- 验证必填字段：`itemName`（必需）
- 设置默认值：
  - `status = "For Sale"`（如果未提供）
  - `is_active = true`（如果未提供）
  - `views = 0`
  - `date_posted = CURRENT_TIMESTAMP`（数据库自动设置）
- 如果提供了 `contactEmail`，使用提供的值；否则可以从 `User` 表获取用户的 email
- 返回创建的商品对象（包含所有字段）

**实现参考**：
```java
@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<ApiResponse<MarketplaceItemResponse>> createItem(
        @Valid @RequestBody MarketplaceItemRequest request
) {
    try {
        UserPrincipal principal = getCurrentUserPrincipal();
        MarketplaceItemResponse response = marketplaceItemService.createItem(
            principal.getUserId(), request
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("创建商品失败: " + e.getMessage()));
    }
}

private UserPrincipal getCurrentUserPrincipal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
        throw new AccessDeniedException("User context missing");
    }
    return principal;
}
```

---

### 6. PUT /api/marketplace/items/{itemId}

**功能**：更新商品

**路径参数**：
- `itemId` (int): 商品 ID

**请求头**：
- `Content-Type: application/json`
- `Authorization: Bearer <JWT_TOKEN>` (必需)

**请求体**：
```json
{
  "itemName": "iPhone 13 Pro Max",
  "category": "Electronics",
  "price": 3200.00,
  "condition": "Like New",
  "description": "Updated description...",
  "location": "Kota Damansara, Selangor",
  "imageUrl": "http://localhost:8081/uploads/new-image.jpg",
  "contactEmail": "seller@example.com",
  "status": "For Sale",
  "isActive": true
}
```

**响应格式**：同创建接口

**权限检查**：
- 只有商品所有者（`seller_id = 当前用户ID`）或 ADMIN 可以更新
- 如果权限不足，返回 403 Forbidden

**逻辑**：
- 验证商品是否存在
- 只更新提供的字段（部分更新，使用 `@DynamicUpdate` 或手动设置）
- 返回更新后的商品对象

**实现参考**：
```java
@PutMapping(value = "/{itemId}", consumes = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<ApiResponse<MarketplaceItemResponse>> updateItem(
        @PathVariable Long itemId,
        @Valid @RequestBody MarketplaceItemRequest request
) {
    try {
        UserPrincipal principal = getCurrentUserPrincipal();
        MarketplaceItemResponse response = marketplaceItemService.updateItem(
            itemId, principal.getUserId(), principal.getRole(), request
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    } catch (EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("商品不存在"));
    } catch (AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("无权编辑此商品"));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("更新商品失败: " + e.getMessage()));
    }
}
```

---

### 7. DELETE /api/marketplace/items/{itemId}

**功能**：删除商品（软删除）

**路径参数**：
- `itemId` (int): 商品 ID

**请求头**：
- `Authorization: Bearer <JWT_TOKEN>` (必需)

**响应格式**：
```json
{
  "success": true,
  "message": "Item deleted successfully"
}
```

**权限检查**：
- 只有商品所有者（`seller_id = 当前用户ID`）或 ADMIN 可以删除
- 如果权限不足，返回 403 Forbidden

**逻辑**：
- 验证商品是否存在
- **使用软删除**：设置 `is_active = false`（不物理删除数据，这样数据可以恢复）
- 返回成功消息

**实现参考**：
```java
@DeleteMapping("/{itemId}")
public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long itemId) {
    try {
        UserPrincipal principal = getCurrentUserPrincipal();
        marketplaceItemService.deleteItem(itemId, principal.getUserId(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success(null, "Item deleted successfully"));
    } catch (EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("商品不存在"));
    } catch (AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("无权删除此商品"));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("删除商品失败: " + e.getMessage()));
    }
}
```

---

## 错误处理

所有 API 应该返回统一的错误格式（使用 `ApiResponse`）：

```json
{
  "success": false,
  "message": "Error message here"
}
```

**HTTP 状态码**：
- `200` - 成功
- `201` - 创建成功（可选，也可以使用 200）
- `400` - 请求参数错误
- `401` - 未认证（缺少或无效的 JWT token）
- `403` - 权限不足
- `404` - 资源不存在
- `500` - 服务器内部错误

---

## 数据验证

### 创建/更新商品时的验证规则：

1. **itemName**：
   - 必需（`@NotBlank`）
   - 长度：1-100 字符（`@Size(min = 1, max = 100)`）

2. **category**：
   - 可选
   - 建议值：'Electronics', 'Books', 'Clothing', 'Sports', 'Home & Garden', 'Other'

3. **price**：
   - 可选
   - 如果提供，必须 >= 0（`@Min(0)`）

4. **condition**：
   - 可选
   - 值必须是：'New', 'Like New', 'Good', 'Fair', 'Poor'
   - 使用 `@Pattern` 或自定义验证器

5. **description**：
   - 可选
   - 最大长度：建议 2000 字符（`@Size(max = 2000)`）

6. **location**：
   - 可选
   - 最大长度：200 字符（`@Size(max = 200)`）

7. **imageUrl**：
   - 可选
   - 必须是有效的 URL 格式（`@URL`）

8. **contactEmail**：
   - 可选
   - 必须是有效的邮箱格式（`@Email`）

9. **status**：
   - 可选
   - 值必须是：'For Sale', 'Sold'
   - 默认：'For Sale'

10. **isActive**：
    - 可选
    - 布尔值
    - 默认：true

---

## 需要创建的类文件

### 1. Entity 类
- `src/main/java/com/segi/campusassistance/entity/MarketplaceItem.java`

### 2. Repository 接口
- `src/main/java/com/segi/campusassistance/repository/MarketplaceItemRepository.java`

### 3. DTO 类
- `src/main/java/com/segi/campusassistance/dto/MarketplaceItemRequest.java`
- `src/main/java/com/segi/campusassistance/dto/MarketplaceItemResponse.java`

### 4. Service 接口和实现
- `src/main/java/com/segi/campusassistance/service/MarketplaceItemService.java`
- `src/main/java/com/segi/campusassistance/service/impl/MarketplaceItemServiceImpl.java`

### 5. Controller 类
- `src/main/java/com/segi/campusassistance/controller/MarketplaceItemController.java`

---

## Entity 类实现要点

```java
@Entity
@Table(name = "marketplace_items")
public class MarketplaceItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;
    
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;
    
    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;
    
    @Column(name = "category", length = 50)
    private String category;
    
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "condition")
    private Condition condition;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "location", length = 200)
    private String location;
    
    @Column(name = "image_url", length = 255)
    private String imageUrl;
    
    @Column(name = "contact_email", length = 100)
    private String contactEmail;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;
    
    @Column(name = "views")
    private Integer views = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "date_posted", updatable = false)
    private LocalDateTime datePosted;
    
    @PrePersist
    protected void onCreate() {
        if (datePosted == null) {
            datePosted = LocalDateTime.now();
        }
        if (views == null) {
            views = 0;
        }
        if (isActive == null) {
            isActive = true;
        }
        if (status == null) {
            status = Status.FOR_SALE;
        }
    }
    
    public enum Condition {
        New, Like_New, Good, Fair, Poor
    }
    
    public enum Status {
        FOR_SALE, SOLD
    }
}
```

**注意**：
- `condition` 枚举值在数据库中存储为 'Like New'，但在 Java 中需要使用 `Like_New`（因为 Java 枚举不能有空格），需要在 `@Enumerated` 时处理映射
- 或者使用 `@Column(name = "condition")` 配合 `@PostLoad` 和 `@PrePersist` 方法进行转换

---

## Response DTO 实现要点

`MarketplaceItemResponse` 需要同时支持 `camelCase` 和 `snake_case` 字段名，以便前端兼容：

```java
public class MarketplaceItemResponse {
    
    @JsonProperty("itemId")
    @JsonProperty("item_id")
    private Long itemId;
    
    @JsonProperty("sellerId")
    @JsonProperty("seller_id")
    private Long sellerId;
    
    @JsonProperty("itemName")
    @JsonProperty("item_name")
    private String itemName;
    
    // ... 其他字段
    
    @JsonProperty("canEdit")
    private Boolean canEdit;
    
    @JsonProperty("canDelete")
    private Boolean canDelete;
    
    @JsonProperty("sellerName")
    @JsonProperty("seller_name")
    private String sellerName;
    
    @JsonProperty("sellerEmail")
    @JsonProperty("seller_email")
    private String sellerEmail;
}
```

**注意**：Jackson 不支持同一个字段有多个 `@JsonProperty`。更好的方法是：
1. 使用 `@JsonAlias` 支持多个字段名
2. 或者使用自定义序列化器
3. 或者前端统一使用一种命名风格

**推荐方案**：使用 `@JsonAlias`：

```java
@JsonProperty("itemId")
@JsonAlias("item_id")
private Long itemId;
```

这样 JSON 输出时使用 `itemId`，但反序列化时同时接受 `itemId` 和 `item_id`。

---

## Service 实现要点

1. **获取用户信息**：
   - 使用 `UserRepository` 通过 `sellerId` 查询用户
   - 获取 `name` 和 `email` 字段填充到 Response

2. **权限检查**：
   ```java
   boolean isOwner = currentUserId != null && item.getSellerId().equals(currentUserId);
   boolean isAdmin = currentUserRole != null && "ADMIN".equalsIgnoreCase(currentUserRole);
   boolean canEdit = isOwner || isAdmin;
   boolean canDelete = isOwner || isAdmin;
   ```

3. **增加 views**：
   ```java
   @Transactional
   public void incrementViews(Long itemId) {
       marketplaceItemRepository.incrementViews(itemId);
   }
   ```
   
   在 Repository 中：
   ```java
   @Modifying
   @Query("UPDATE MarketplaceItem m SET m.views = m.views + 1 WHERE m.itemId = :itemId")
   void incrementViews(@Param("itemId") Long itemId);
   ```

4. **筛选逻辑**：
   ```java
   if ("My Posts".equals(filter) && currentUserId != null) {
       // 只返回 sellerId == currentUserId 的商品
   } else if ("Others' Posts".equals(filter) && currentUserId != null) {
       // 只返回 sellerId != currentUserId 的商品
   }
   // 否则返回所有商品
   ```

---

## 数据库查询优化建议

1. **列表查询**：
   - 使用 JOIN 查询获取卖家信息（sellerName, sellerEmail）
   - 使用索引：`seller_id`, `is_active`, `date_posted`
   - 考虑分页（如果数据量大，使用 `Pageable`）

2. **搜索查询**：
   - 在 `item_name` 和 `description` 字段上使用全文索引（如果可能）
   - 限制返回结果数量（如最多 50 条）

3. **详情查询**：
   - 使用 JOIN 获取卖家信息
   - 更新 views 时使用原子操作

---

## 测试要求

请确保以下场景都能正常工作：

1. ✅ 未登录用户可以查看商品列表（但 canEdit/canDelete 为 false）
2. ✅ 登录用户可以查看所有商品
3. ✅ 登录用户可以筛选自己的商品（filter=My Posts）
4. ✅ 登录用户可以筛选其他人的商品（filter=Others' Posts）
5. ✅ 登录用户可以创建商品
6. ✅ 只有商品所有者可以编辑/删除自己的商品
7. ✅ ADMIN 可以编辑/删除任何商品
8. ✅ 搜索功能正常工作
9. ✅ 图片上传功能正常工作
10. ✅ 商品详情访问时 views 自动增加
11. ✅ 软删除的商品不会出现在列表中

---

## 前端期望的字段映射

前端使用以下字段名，后端应该同时支持两种命名（snake_case 和 camelCase）：

- `itemId` / `item_id`
- `sellerId` / `seller_id`
- `itemName` / `item_name`
- `imageUrl` / `image_url`
- `contactEmail` / `contact_email`
- `datePosted` / `date_posted`
- `isActive` / `is_active`
- `sellerName` / `seller_name`
- `sellerEmail` / `seller_email`

**实现方案**：使用 `@JsonAlias` 注解，输出时使用 camelCase，但反序列化时同时接受两种格式。

---

## 实现步骤建议

1. ✅ 创建 `MarketplaceItem` Entity 类
2. ✅ 创建 `MarketplaceItemRepository` 接口
3. ✅ 创建 `MarketplaceItemRequest` 和 `MarketplaceItemResponse` DTO 类
4. ✅ 创建 `MarketplaceItemService` 接口和实现类
5. ✅ 创建 `MarketplaceItemController` 控制器
6. ✅ 实现图片上传功能（可以复用现有的 `FileStorageService`）
7. ✅ 添加数据验证和错误处理
8. ✅ 实现权限检查逻辑
9. ✅ 测试所有 API 端点

---

## 注意事项

1. **安全性**：
   - 所有修改操作（CREATE, UPDATE, DELETE）都需要 JWT 认证
   - 验证用户权限，防止未授权访问
   - 验证输入数据，防止 SQL 注入和 XSS 攻击
   - 文件上传验证文件类型和大小

2. **性能**：
   - 列表查询考虑添加分页（使用 `Pageable`）
   - 图片上传考虑使用云存储（如 AWS S3, Cloudinary）
   - 使用数据库索引优化查询

3. **数据一致性**：
   - 使用 `@Transactional` 确保数据一致性
   - 删除商品时使用软删除（设置 `is_active = false`）

4. **兼容性**：
   - 确保字段命名同时支持 camelCase 和 snake_case（使用 `@JsonAlias`）
   - 保持与现有 API 的响应格式一致（使用 `ApiResponse<T>`）

5. **数据库字段映射**：
   - 注意 `condition` 枚举值的处理（数据库中是 'Like New'，Java 中需要处理）
   - 确保 `seller_id` 外键正确映射到 `users` 表

---

## 前端已实现的页面

前端已经创建了以下页面，等待后端 API：

1. **MarketplacePage2** - 商品列表页（需要 GET /api/marketplace/items）
2. **MarketplaceAddPage** - 添加商品页（需要 POST /api/marketplace/items 和 POST /api/marketplace/items/upload）
3. **MarketplaceEditPage** - 编辑商品页（需要 GET /api/marketplace/items/{id} 和 PUT /api/marketplace/items/{id}）
4. **MarketplaceItemDetails** - 商品详情页（需要 GET /api/marketplace/items/{id}）

所有页面都已经准备好调用这些 API，只需要后端实现对应的端点即可。

---

## 完成标志

当以下所有功能都实现并通过测试后，即可认为完成：

- [ ] 所有 7 个 API 端点都已实现
- [ ] Entity、Repository、Service、Controller 类都已创建
- [ ] 数据验证和错误处理都已实现
- [ ] 权限检查逻辑正确实现
- [ ] 文件上传功能正常工作
- [ ] 搜索和筛选功能正常工作
- [ ] 软删除功能正常工作
- [ ] views 自动增加功能正常工作
- [ ] 所有测试场景都通过

---

**祝开发顺利！如有问题，请参考现有的 `ItemController` 和 `ItemService` 实现。**

