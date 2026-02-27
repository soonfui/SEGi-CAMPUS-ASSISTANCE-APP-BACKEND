package com.segi.campusassistance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.segi.campusassistance.entity.Chat;
import java.time.LocalDateTime;
import java.util.List;

public class ChatResponse {

    private Long id;
    private Long itemId;
    private Long ownerId;
    private String ownerName;
    private String ownerPicture;
    private Long requesterId;
    private String requesterName;
    private String requesterPicture;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Long> participantIds;
    private Long unreadCount;

    public static ChatResponse fromEntity(Chat chat, String ownerName, String ownerPicture, 
                                         String requesterName, String requesterPicture, Long unreadCount) {
        ChatResponse response = new ChatResponse();
        response.setId(chat.getId());
        response.setItemId(chat.getItemId());
        response.setOwnerId(chat.getOwnerId());
        response.setOwnerName(ownerName);
        response.setOwnerPicture(ownerPicture);
        response.setRequesterId(chat.getRequesterId());
        response.setRequesterName(requesterName);
        response.setRequesterPicture(requesterPicture);
        response.setCreatedAt(chat.getCreatedAt());
        response.setUpdatedAt(chat.getUpdatedAt());
        response.setParticipantIds(List.of(chat.getOwnerId(), chat.getRequesterId()));
        response.setUnreadCount(unreadCount);
        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerPicture() {
        return ownerPicture;
    }

    public void setOwnerPicture(String ownerPicture) {
        this.ownerPicture = ownerPicture;
    }

    @JsonProperty("ownerAvatar")
    public String getOwnerAvatar() {
        return ownerPicture;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getRequesterPicture() {
        return requesterPicture;
    }

    public void setRequesterPicture(String requesterPicture) {
        this.requesterPicture = requesterPicture;
    }

    @JsonProperty("requesterAvatar")
    public String getRequesterAvatar() {
        return requesterPicture;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Long> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<Long> participantIds) {
        this.participantIds = participantIds;
    }

    public Long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }
}

