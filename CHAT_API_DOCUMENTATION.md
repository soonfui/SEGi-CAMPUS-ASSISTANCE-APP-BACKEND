# 聊天功能 API 文档

## 概述

本文档描述失物招领应用的聊天功能 REST API。所有 API 都需要 JWT 认证（Authorization header: `Bearer <token>`）。

## 数据库表

### chats 表
- `id`: 聊天ID
- `item_id`: 物品ID
- `owner_id`: 贴文作者（物品所有者）
- `requester_id`: 联系者
- `created_at`: 创建时间
- `updated_at`: 更新时间

### chat_messages 表
- `id`: 消息ID
- `chat_id`: 聊天ID
- `sender_id`: 发送者用户ID
- `content`: 消息内容（最大2000字符）
- `is_read`: 是否已读
- `created_at`: 发送时间

## API 端点

### 1. 创建或获取聊天会话

**POST** `/api/chats`

**请求体:**
```json
{
  "itemId": 1,
  "ownerId": 2,
  "requesterId": 3
}
```

**或使用 GET:**
**GET** `/api/chats?itemId=1&ownerId=2&requesterId=3`

**响应:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "itemId": 1,
    "ownerId": 2,
    "ownerName": "张三",
    "ownerPicture": "https://...",
    "requesterId": 3,
    "requesterName": "李四",
    "requesterPicture": "https://...",
    "createdAt": "2024-12-15T10:30:00",
    "updatedAt": "2024-12-15T10:30:00",
    "participantIds": [2, 3],
    "unreadCount": 0
  }
}
```

**权限:**
- 当前用户必须是 ownerId 或 requesterId 之一
- 验证 ownerId 是物品的所有者

---

### 2. 获取用户的聊天列表

**GET** `/api/chats/my`

**响应:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "itemId": 1,
      "ownerId": 2,
      "ownerName": "张三",
      "requesterId": 3,
      "requesterName": "李四",
      "unreadCount": 2,
      ...
    }
  ]
}
```

---

### 3. 获取特定聊天详情

**GET** `/api/chats/{chatId}`

**响应:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "itemId": 1,
    "ownerId": 2,
    "ownerName": "张三",
    "requesterId": 3,
    "requesterName": "李四",
    "unreadCount": 2,
    ...
  }
}
```

**权限:**
- 只有聊天的参与者可以访问

---

### 4. 获取聊天消息（分页）

**GET** `/api/chats/{chatId}/messages?page=0&size=50`

**查询参数:**
- `page`: 页码（默认: 0）
- `size`: 每页大小（默认: 50）

**响应:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "chatId": 1,
        "senderId": 2,
        "senderName": "张三",
        "content": "你好，我想了解这个物品",
        "isRead": true,
        "timestamp": "2024-12-15T10:30:00"
      }
    ],
    "totalElements": 10,
    "totalPages": 1,
    "number": 0,
    "size": 50
  }
}
```

---

### 5. 获取所有聊天消息（不分页）

**GET** `/api/chats/{chatId}/messages/all`

**响应:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "chatId": 1,
      "senderId": 2,
      "senderName": "张三",
      "content": "你好",
      "isRead": true,
      "timestamp": "2024-12-15T10:30:00"
    }
  ]
}
```

---

### 6. 发送消息

**POST** `/api/chats/{chatId}/messages`

**请求体:**
```json
{
  "senderId": 3,
  "content": "你好，我想了解这个物品"
}
```

**响应:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "chatId": 1,
    "senderId": 3,
    "senderName": "李四",
    "content": "你好，我想了解这个物品",
    "isRead": false,
    "timestamp": "2024-12-15T10:30:00"
  }
}
```

**权限:**
- 只有聊天的参与者可以发送消息
- senderId 必须是当前登录用户

---

### 7. 标记消息为已读

**PUT** `/api/chats/{chatId}/read`

**响应:**
```json
{
  "success": true,
  "data": null
}
```

**功能:**
- 将当前用户收到的所有消息标记为已读

---

## 错误响应

### 401 Unauthorized
```json
{
  "success": false,
  "message": "User context missing"
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": "无权访问此聊天"
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "聊天不存在"
}
```

### 400 Bad Request
```json
{
  "success": false,
  "message": "物品ID不能为空"
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "message": "创建或获取聊天失败: ..."
}
```

---

## 权限验证规则

1. **所有 API 都需要 JWT 认证**
2. **聊天访问权限:**
   - 只有 `ownerId` 或 `requesterId` 可以访问聊天
3. **消息发送权限:**
   - 只有聊天参与者可以发送消息
   - `senderId` 必须是当前登录用户
4. **创建聊天权限:**
   - `ownerId` 必须是物品的所有者
   - 当前用户必须是 `ownerId` 或 `requesterId` 之一

---

## 前端集成示例

### 创建或获取聊天
```dart
// 方式1: POST
final response = await http.post(
  Uri.parse('$baseUrl/api/chats'),
  headers: {
    'Authorization': 'Bearer $token',
    'Content-Type': 'application/json',
  },
  body: json.encode({
    'itemId': item.id,
    'ownerId': item.userId,
    'requesterId': currentUserId,
  }),
);

// 方式2: GET
final response = await http.get(
  Uri.parse('$baseUrl/api/chats?itemId=${item.id}&ownerId=${item.userId}&requesterId=$currentUserId'),
  headers: {
    'Authorization': 'Bearer $token',
  },
);
```

### 获取消息
```dart
final response = await http.get(
  Uri.parse('$baseUrl/api/chats/$chatId/messages/all'),
  headers: {
    'Authorization': 'Bearer $token',
  },
);
```

### 发送消息
```dart
final response = await http.post(
  Uri.parse('$baseUrl/api/chats/$chatId/messages'),
  headers: {
    'Authorization': 'Bearer $token',
    'Content-Type': 'application/json',
  },
  body: json.encode({
    'senderId': currentUserId,
    'content': messageText,
  }),
);
```

---

## WebSocket（待实现）

WebSocket 端点将在后续版本中实现，用于实时消息推送。

预期端点: `ws://<host>/ws/chats/{chatId}`

事件类型:
- `MESSAGE_SEND`: 发送消息
- `MESSAGE_RECEIVED`: 接收消息
- `MESSAGE_READ`: 消息已读
- `USER_TYPING`: 用户正在输入（可选）

---

## 数据库初始化

请执行 `database_chat_tables.sql` 脚本来创建所需的数据库表。

```sql
-- 执行脚本
source database_chat_tables.sql;
```

---

## 注意事项

1. 所有时间字段使用 `LocalDateTime`，格式为 ISO 8601
2. 消息内容最大长度为 2000 字符
3. 聊天会自动更新 `updatedAt` 字段（最后一条消息的时间）
4. 未读消息计数在每次查询时动态计算

