package com.unilivros.dto;

public class NotificacaoStatusDTO {

    private boolean hasUnread;

    public NotificacaoStatusDTO(boolean hasUnread) {
        this.hasUnread = hasUnread;
    }

    // Getters e Setters
    public boolean isHasUnread() {
        return hasUnread;
    }

    public void setHasUnread(boolean hasUnread) {
        this.hasUnread = hasUnread;
    }
}
