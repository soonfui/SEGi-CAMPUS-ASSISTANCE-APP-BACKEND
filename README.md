# SEGi Campus Assistance API

This is a lost and found system REST API based on Spring Boot, designed specifically for the FlutterFlow frontend.

## Functions Features

- **Notification Management:** Create, read, update, and delete notifications
- **Item Management:** Create, read, update, and delete lost and found items
- **Search Functionality:** Supports keyword search for items
- **CORS Support:** Configured to support the FlutterFlow frontend
- **MySQL Database:** Uses JPA for data persistence

## technology stack

- Java 17
- Spring Boot 3.2.0
- Spring Web
- Spring Data JPA
- MySQL 8.0
- Maven

## Database config

at `src/main/resources/application.properties` Configure MySQL connection：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/campus_assistance?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## APIendpoint

### (Notifications)

- `GET /api/notifications` - Get all notifications
- `GET /api/notifications/{id}` - Get notification based on ID
- `POST /api/notifications` - Create new notification
- `PUT /api/notifications/{id}` - Update notification
- `DELETE /api/notifications/{id}` - Delete notification
- `GET /api/notifications/recent` - Get notifications from the last 24 hours
- `GET /api/notifications/search?q=keyword` - Search notifications

### 物品 (Items)

`GET /api/items` - Retrieves all items
`GET /api/items/{id}` - Retrieves items by ID
`GET /api/items/search?q=keyword` - Searches for items
`GET /api/items/type/{type}` - Retrieves items by type
`POST /api/items` - Creates a new item
`PUT /api/items/{id}` - Updates an item
`DELETE /api/items/{id}` - Deletes an item
`GET /api/items/search/type?q=keyword&type=type` - Searches for items by keyword and type

## Data Model

### Notification

- `id` (Long) - Primary Key
- `message` (String) - Notification Message
- `time` (LocalDateTime) - Notification Time

### Item

- `id` (Long) - Primary Key
- `title` (String) - Item Title
- `description` (String) - Item Description
- `type` (String) - Item Type

## Running the Project

1. Ensure Java 17 and Maven are installed.
2. Configure the MySQL database.
3. Run the following command:

```bash
mvn spring-boot:run
```

The API will run at `http://localhost:8081`.

## CORS Configuration

The API is configured with CORS to support the following sources:

- `http://localhost:3000` (local development)
- `https://app.flutterflow.io` (FlutterFlow)

Allowed sources can be customized via the environment variable `CORS_ORIGINS`.

## Environment Variables

- `DB_USERNAME` - Database username (default: root)
- `DB_PASSWORD` - Database password (default: password)
- `CORS_ORIGINS` - Allowed CORS sources (default: http://localhost:3000, https://app.flutterflow.io)
