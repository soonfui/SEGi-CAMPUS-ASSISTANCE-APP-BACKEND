package com.segi.campusassistance.dto;

import jakarta.validation.constraints.NotNull;

public class ChatRequest {

    @NotNull(message = "Item ID cannot be null")
    private Long itemId;

    @NotNull(message = "Owner ID cannot be null")
    private Long ownerId;

    @NotNull(message = "Requester ID cannot be null")
    private Long requesterId;

    // Getters and Setters
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

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }
}

