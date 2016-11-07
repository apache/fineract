package org.apache.fineract.notification.data;

import org.apache.fineract.useradministration.domain.AppUser;

import java.io.Serializable;
import java.util.List;

public class NotificationData implements Serializable {

    private Long id;
    private Long userId;
    private String objectType;
    private Long objectId;
    private String action;
    private String actor;
    private String content;
    private boolean isSystemGenerated;
    private String tenantIdentifier;
    private String createdAt;
    private Long officeId;
    private List<Long> userIds;

    public NotificationData() {

    }

    public NotificationData(NotificationBuilder builder) {
        this.userId = builder.userId;
        this.objectType = builder.objectType;
        this.objectId = builder.objectId;
        this.action = builder.action;
        this.actor = builder.actor;
        this.content = builder.notificationContent;
        this.isSystemGenerated = builder.isSystemGenerated;
        this.tenantIdentifier = builder.tenantIdentifier;
        this.userIds = builder.userIds;
        this.officeId = builder.officeId;
    }

    public Long getOfficeId() {
        return officeId;
    }

    public void setOfficeId(Long officeId) {
        this.officeId = officeId;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserId(List<Long> userIds) {
        this.userIds = userIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public Long getObjectIdentfier() {
        return objectId;
    }

    public void entifier(Long objectIdentifier) {
        this.objectId = objectIdentifier;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSystemGenerated() {
        return isSystemGenerated;
    }

    public void setSystemGenerated(boolean systemGenerated) {
        isSystemGenerated = systemGenerated;
    }

    public String getTenantIdentifier() {
        return tenantIdentifier;
    }

    public void setTenantIdentifier(String tenantIdentifier) {
        this.tenantIdentifier = tenantIdentifier;
    }

    public static class NotificationBuilder {
        private Long userId;
        private String objectType;
        private Long objectId;
        private String action;
        private String actor;
        private String notificationContent;
        private boolean isSystemGenerated;
        private String tenantIdentifier;
        private Long officeId;
        private List<Long> userIds;

        public NotificationBuilder() {}

        public NotificationBuilder withUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public NotificationBuilder withUserIds(List<Long> userIds) {
            this.userIds = userIds;
            return this;
        }

        public NotificationBuilder withObjectType(String objectType) {
            this.objectType = objectType;
            return this;
        }

        public NotificationBuilder withObjectIdentifier(Long objectIdentifier) {
            this.objectId = objectIdentifier;
            return this;
        }

        public NotificationBuilder withAction(String action) {
            this.action = action;
            return this;
        }

        public NotificationBuilder withActor(String actor) {
            this.actor = actor;
            return this;
        }

        public NotificationBuilder withNotificationContent(String notificationContent) {
            this.notificationContent = notificationContent;
            return this;
        }

        public NotificationBuilder withSystemGenerated(boolean systemGenerated) {
            isSystemGenerated = systemGenerated;
            return this;
        }

        public NotificationBuilder withTenantIdentifier(String tenantIdentifier) {
            this.tenantIdentifier = tenantIdentifier;
            return this;
        }

        public NotificationBuilder withOfficeId(Long officeId) {
            this.officeId = officeId;
            return this;
        }

        public NotificationData build() {
            return new NotificationData(this);
        }
    }
}
