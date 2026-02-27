package com.segi.campusassistance.config;

import com.segi.campusassistance.entity.Item;
import com.segi.campusassistance.entity.Notification;
import com.segi.campusassistance.repository.ItemRepository;
import com.segi.campusassistance.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // 初始化示例通知数据
        if (notificationRepository.count() == 0) {
            notificationRepository.save(new Notification("Welcome to the SEGi Campus Lost and Found System!"));
            notificationRepository.save(new Notification("Please check the lost and found information promptly to help the owner recover their belongings."));
            notificationRepository.save(new Notification("The system has been updated and a search function has been added."));
        }
        
        // 初始化示例物品数据
        if (itemRepository.count() == 0) {
            itemRepository.save(createItem(
                    "Black Wallet",
                    "Black leather wallet with multiple cards found on library level 2.",
                    "Wallet",
                    "FOUND",
                    "Library Level 2",
                    LocalDate.now().minusDays(1),
                    null,
                    1L));
            itemRepository.save(createItem(
                    "iPhone 13",
                    "Lost black iPhone 13 with transparent case in cafeteria.",
                    "Electronics",
                    "LOST",
                    "Campus Cafeteria",
                    LocalDate.now().minusDays(3),
                    null,
                    2L));
            itemRepository.save(createItem(
                    "Student ID Card",
                    "Student ID card for Zhang San picked up near playground.",
                    "Card",
                    "FOUND",
                    "Playground",
                    LocalDate.now().minusDays(2),
                    null,
                    1L));
            itemRepository.save(createItem(
                    "Red Backpack",
                    "Red backpack with textbooks spotted on library level 3.",
                    "Bag",
                    "FOUND",
                    "Library Level 3",
                    LocalDate.now(),
                    null,
                    3L));
        }
    }

    private Item createItem(String name,
                            String description,
                            String category,
                            String status,
                            String location,
                            LocalDate date,
                            String imageUrl,
                            Long userId) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setCategory(category);
        item.setStatus(status);
        item.setLocation(location);
        item.setDate(date);
        item.setImageUrl(imageUrl);
        item.setUserId(userId);
        return item;
    }
}
