package com.segi.campusassistance.service;

import com.segi.campusassistance.dto.NotificationResponse;
import com.segi.campusassistance.entity.Notification;
import com.segi.campusassistance.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationResponse> getLatestNotifications() {
        List<Notification> notifications = notificationRepository.findAll(
                Sort.by(Sort.Direction.DESC, "time"));

        if (notifications.isEmpty()) {
            return fallback();
        }

        return notifications.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private List<NotificationResponse> fallback() {
        LocalDateTime now = LocalDateTime.now();
        return List.of(
                NotificationResponse.of(
                        "Lost & Found: Someone claimed the item you posted",
                        now.minusMinutes(5),
                        "lost_found",
                        "search"),
                NotificationResponse.of(
                        "Marketplace: Your listing received a new comment",
                        now.minusMinutes(20),
                        "marketplace",
                        "shopping_bag"),
                NotificationResponse.of(
                        "System Notice: Please remember to update the semester calendar",
                        now.minusHours(1),
                        "general",
                        "event")
        );
    }
}


