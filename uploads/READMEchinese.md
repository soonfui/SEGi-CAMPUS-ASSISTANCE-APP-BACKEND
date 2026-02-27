# SEGi Campus Assistance API

This is a lost and found system REST API based on Spring Boot, designed specifically for the FlutterFlow frontend.

## Functions Features

- **通知管理**: 创建、读取、更新、删除通知
- **物品管理**: 创建、读取、更新、删除失物招领物品
- **搜索功能**: 支持关键词搜索物品
- **CORS支持**: 配置为支持FlutterFlow前端
- **MySQL数据库**: 使用JPA进行数据持久化

## 技术栈

- Java 17
- Spring Boot 3.2.0
- Spring Web
- Spring Data JPA
- MySQL 8.0
- Maven

## 数据库配置

在 `src/main/resources/application.properties` 中配置MySQL连接：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/campus_assistance?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## API端点

### 通知 (Notifications)

- `GET /api/notifications` - 获取所有通知
- `GET /api/notifications/{id}` - 根据ID获取通知
- `POST /api/notifications` - 创建新通知
- `PUT /api/notifications/{id}` - 更新通知
- `DELETE /api/notifications/{id}` - 删除通知
- `GET /api/notifications/recent` - 获取最近24小时的通知
- `GET /api/notifications/search?q=keyword` - 搜索通知

### 物品 (Items)

- `GET /api/items` - 获取所有物品
- `GET /api/items/{id}` - 根据ID获取物品
- `GET /api/items/search?q=keyword` - 搜索物品
- `GET /api/items/type/{type}` - 根据类型获取物品
- `POST /api/items` - 创建新物品
- `PUT /api/items/{id}` - 更新物品
- `DELETE /api/items/{id}` - 删除物品
- `GET /api/items/search/type?q=keyword&type=type` - 根据关键词和类型搜索物品

## 数据模型

### Notification (通知)
- `id` (Long) - 主键
- `message` (String) - 通知消息
- `time` (LocalDateTime) - 通知时间

### Item (物品)
- `id` (Long) - 主键
- `title` (String) - 物品标题
- `description` (String) - 物品描述
- `type` (String) - 物品类型

## 运行项目

1. 确保已安装Java 17和Maven
2. 配置MySQL数据库
3. 运行以下命令：

```bash
mvn spring-boot:run
```

API将在 `http://localhost:8081` 上运行。

## CORS配置

API已配置CORS以支持以下来源：
- `http://localhost:3000` (本地开发)
- `https://app.flutterflow.io` (FlutterFlow)

可以通过环境变量 `CORS_ORIGINS` 自定义允许的来源。

## 环境变量

- `DB_USERNAME` - 数据库用户名 (默认: root)
- `DB_PASSWORD` - 数据库密码 (默认: password)
- `CORS_ORIGINS` - 允许的CORS来源 (默认: http://localhost:3000,https://app.flutterflow.io)
