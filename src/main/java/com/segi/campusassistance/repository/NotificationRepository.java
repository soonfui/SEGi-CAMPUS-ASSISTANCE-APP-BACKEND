package com.segi.campusassistance.repository;

import com.segi.campusassistance.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find notifications by time range
    List<Notification> findByTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // Find recent notifications (last 24 hours)
    @Query("SELECT n FROM Notification n WHERE n.time >= :since ORDER BY n.time DESC")
    List<Notification> findRecentNotifications(@Param("since") LocalDateTime since);
    
    // Find notifications containing specific text
    @Query("SELECT n FROM Notification n WHERE LOWER(n.message) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY n.time DESC")
    List<Notification> findByMessageContaining(@Param("keyword") String keyword);
}
