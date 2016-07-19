package org.apache.fineract.notification.data;


public class NotificationMapperData {

    private Long id;
    private Long notificationId;
    private Long userId;
    private boolean isRead;
    private String createdAt;

    public NotificationMapperData(Long id, Long notificationId, Long userId, boolean isRead, String createdAt) {
        this.id = id;
        this.notificationId = notificationId;
        this.userId = userId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public NotificationMapperData() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
