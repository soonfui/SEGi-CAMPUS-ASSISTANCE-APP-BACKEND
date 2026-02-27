package com.segi.campusassistance.controller;

import com.segi.campusassistance.dto.NotificationResponse;
import com.segi.campusassistance.entity.Notification;
import com.segi.campusassistance.repository.NotificationRepository;
import com.segi.campusassistance.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    // GET /api/notifications - Get all notifications
    @GetMapping
    public ResponseEntity<Map<String, List<NotificationResponse>>> getAllNotifications() {
        List<NotificationResponse> notifications = notificationService.getLatestNotifications();
        return ResponseEntity.ok(Map.of("data", notifications));
    }
    
    // GET /api/notifications/{id} - Get notification by ID
    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        Optional<Notification> notification = notificationRepository.findById(id);
        return notification.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // POST /api/notifications - Create new notification
    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody Notification notification) {
        if (notification.getTime() == null) {
            notification.setTime(LocalDateTime.now());
        }
        Notification savedNotification = notificationRepository.save(notification);
        return ResponseEntity.ok(savedNotification);
    }
    
    // PUT /api/notifications/{id} - Update notification
    @PutMapping("/{id}")
    public ResponseEntity<Notification> updateNotification(@PathVariable Long id, @RequestBody Notification notificationDetails) {
        Optional<Notification> optionalNotification = notificationRepository.findById(id);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
        notification.setMessage(notificationDetails.getMessage());
        notification.setTime(notificationDetails.getTime() != null ? notificationDetails.getTime() : LocalDateTime.now());
            Notification updatedNotification = notificationRepository.save(notification);
            return ResponseEntity.ok(updatedNotification);
        }
        return ResponseEntity.notFound().build();
    }
    
    // DELETE /api/notifications/{id} - Delete notification
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        if (notificationRepository.existsById(id)) {
            notificationRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    // GET /api/notifications/recent - Get recent notifications (last 24 hours)
    @GetMapping("/recent")
    public ResponseEntity<List<Notification>> getRecentNotifications() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<Notification> recentNotifications = notificationRepository.findRecentNotifications(since);
        return ResponseEntity.ok(recentNotifications);
    }
    
    // GET /api/notifications/search?q=keyword - Search notifications by message content
    @GetMapping("/search")
    public ResponseEntity<List<Notification>> searchNotifications(@RequestParam String q) {
        List<Notification> notifications = notificationRepository.findByMessageContaining(q);
        return ResponseEntity.ok(notifications);
    }
}
