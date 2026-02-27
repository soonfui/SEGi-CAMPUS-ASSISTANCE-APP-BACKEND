# SEGi Campus Assistance API 文档

## 基础信息

- **基础URL**: `http://localhost:8081`
- **内容类型**: `application/json`
- **字符编码**: UTF-8

## 通知 API

### 获取所有通知
```http
GET /api/notifications
```

**响应示例**:
```json
[
  {
    "id": 1,
    "message": "欢迎使用SEGi校园失物招领系统！",
    "time": "2024-01-15T10:30:00"
  }
]
```

### 获取特定通知
```http
GET /api/notifications/{id}
```

### 创建新通知
```http
POST /api/notifications
Content-Type: application/json

{
  "message": "新的系统通知"
}
```

### 搜索通知
```http
GET /api/notifications/search?q=关键词
```

### 获取最近通知
```http
GET /api/notifications/recent
```

## 物品 API

### 获取所有物品
```http
GET /api/items
```

**响应示例**:
```json
[
  {
    "id": 1,
    "title": "黑色钱包",
    "description": "在图书馆二楼发现的黑色皮质钱包，内有身份证和银行卡",
    "type": "钱包"
  }
]
```

### 搜索物品
```http
GET /api/items/search?q=关键词
```

**示例**:
```http
GET /api/items/search?q=钱包
```

### 按类型获取物品
```http
GET /api/items/type/{type}
```

**示例**:
```http
GET /api/items/type/手机
```

### 创建新物品
```http
POST /api/items
Content-Type: application/json

{
  "title": "丢失的钥匙",
  "description": "在宿舍楼门口发现的钥匙串",
  "type": "钥匙"
}
```

### 更新物品
```http
PUT /api/items/{id}
Content-Type: application/json

{
  "title": "更新的标题",
  "description": "更新的描述",
  "type": "更新的类型"
}
```

### 删除物品
```http
DELETE /api/items/{id}
```

## 错误响应

### 验证错误 (400 Bad Request)
```json
{
  "title": "标题不能为空",
  "description": "描述不能为空"
}
```

### 未找到 (404 Not Found)
```json
{
  "error": "资源未找到"
}
```

### 服务器错误 (500 Internal Server Error)
```json
{
  "error": "服务器内部错误"
}
```

## 使用示例

### 使用curl测试API

1. **获取所有物品**:
```bash
curl -X GET http://localhost:8081/api/items
```

2. **搜索物品**:
```bash
curl -X GET "http://localhost:8081/api/items/search?q=钱包"
```

3. **创建新物品**:
```bash
curl -X POST http://localhost:8081/api/items \
  -H "Content-Type: application/json" \
  -d '{
    "title": "测试物品",
    "description": "这是一个测试物品",
    "type": "测试"
  }'
```

4. **获取所有通知**:
```bash
curl -X GET http://localhost:8081/api/notifications
```

## FlutterFlow集成

在FlutterFlow中，您可以使用以下配置：

1. **API基础URL**: `http://localhost:8081` (开发环境)
2. **CORS已启用**: 支持FlutterFlow的域名
3. **响应格式**: JSON
4. **认证**: 当前版本无需认证（可根据需要添加）

### FlutterFlow HTTP请求配置示例

**获取物品列表**:
- Method: GET
- URL: `http://localhost:8081/api/items`
- Headers: `Content-Type: application/json`

**搜索物品**:
- Method: GET  
- URL: `http://localhost:8081/api/items/search?q={{searchKeyword}}`
- Headers: `Content-Type: application/json`
