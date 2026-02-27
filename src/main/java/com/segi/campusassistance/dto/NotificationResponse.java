package com.segi.campusassistance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.segi.campusassistance.entity.Notification;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {

    private Long id;
    private String message;
    private LocalDateTime time;
    private String type;
    private String icon;

    public static NotificationResponse fromEntity(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setMessage(notification.getMessage());
        response.setTime(notification.getTime());
        response.setType(deriveType(notification.getMessage()));
        response.setIcon(iconForType(response.getType()));
        return response;
    }

    public static NotificationResponse of(String message, LocalDateTime time, String type, String icon) {
        NotificationResponse response = new NotificationResponse();
        response.setMessage(message);
        response.setTime(time);
        response.setType(type);
        response.setIcon(icon != null ? icon : iconForType(type));
        return response;
    }

    private static String deriveType(String message) {
        if (message == null) {
            return "general";
        }
        String lower = message.toLowerCase();
        if (lower.contains("lost") || lower.contains("found")) {
            return "lost_found";
        }
        if (lower.contains("marketplace") || lower.contains("item") || lower.contains("sale")) {
            return "marketplace";
        }
        if (lower.contains("chat") || lower.contains("message")) {
            return "chat";
        }
        return "general";
    }

    private static String iconForType(String type) {
        if (type == null) {
            return "info";
        }
        return switch (type) {
            case "lost_found" -> "search";
            case "marketplace" -> "shopping_bag";
            case "chat" -> "chat_bubble";
            default -> "info";
        };
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}


