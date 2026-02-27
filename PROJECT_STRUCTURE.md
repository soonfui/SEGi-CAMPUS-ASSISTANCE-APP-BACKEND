# 项目结构说明

```
SEGi Campus Assistance App/
├── pom.xml                                    # Maven配置文件
├── README.md                                  # 项目说明文档
├── API_DOCUMENTATION.md                       # API使用文档
├── PROJECT_STRUCTURE.md                       # 项目结构说明
├── run.bat                                    # Windows启动脚本
├── run.sh                                     # Linux/Mac启动脚本
└── src/
    ├── main/
    │   ├── java/com/segi/campusassistance/
    │   │   ├── CampusAssistanceApplication.java    # Spring Boot主应用类
    │   │   ├── entity/
    │   │   │   ├── Notification.java               # 通知实体类
    │   │   │   └── Item.java                       # 物品实体类
    │   │   ├── repository/
    │   │   │   ├── NotificationRepository.java     # 通知数据访问层
    │   │   │   └── ItemRepository.java             # 物品数据访问层
    │   │   ├── controller/
    │   │   │   ├── NotificationController.java     # 通知REST控制器
    │   │   │   ├── ItemController.java             # 物品REST控制器
    │   │   │   └── GlobalExceptionHandler.java     # 全局异常处理器
    │   │   └── config/
    │   │       ├── CorsConfig.java                 # CORS配置
    │   │       └── DataInitializer.java            # 数据初始化
    │   └── resources/
    │       └── application.properties              # 应用配置文件
    └── test/
        └── java/com/segi/campusassistance/
            └── CampusAssistanceApplicationTests.java # 测试类
```

## 架构说明

### 1. 实体层 (Entity)
- **Notification**: 通知实体，包含id、message、time字段
- **Item**: 物品实体，包含id、title、description、type字段

### 2. 数据访问层 (Repository)
- **NotificationRepository**: 继承JpaRepository，提供通知的CRUD操作和自定义查询
- **ItemRepository**: 继承JpaRepository，提供物品的CRUD操作和搜索功能

### 3. 控制层 (Controller)
- **NotificationController**: 处理通知相关的HTTP请求
- **ItemController**: 处理物品相关的HTTP请求
- **GlobalExceptionHandler**: 全局异常处理，统一错误响应格式

### 4. 配置层 (Config)
- **CorsConfig**: CORS跨域配置，支持FlutterFlow前端
- **DataInitializer**: 应用启动时初始化示例数据

### 5. 应用配置
- **application.properties**: 数据库连接、JPA配置、CORS配置等

## 技术特点

1. **RESTful API设计**: 遵循REST原则，使用标准HTTP方法
2. **JPA数据持久化**: 使用Spring Data JPA简化数据库操作
3. **CORS支持**: 配置跨域访问，支持FlutterFlow前端
4. **数据验证**: 使用Bean Validation进行输入验证
5. **异常处理**: 统一的异常处理机制
6. **可配置性**: 通过application.properties和环境变量进行配置
