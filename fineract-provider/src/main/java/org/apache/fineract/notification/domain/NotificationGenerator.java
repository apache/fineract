package org.apache.fineract.notification.domain;


import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "notification_generator")
public class NotificationGenerator extends AbstractPersistable<Long> {

    @Column(name = "object_type")
    private String objectType;

    @Column(name = "object_identifier")
    private Long objectIdentifier;

    @Column(name = "action")
    private String action;

    @Column(name = "actor")
    private String actor;

    @Column(name = "is_system_generated")
    private boolean isSystemGenerated;

    @Column(name = "notification_content")
    private String notificationContent;

    @Column(name = "created_at")
    private String createdAt;

    public NotificationGenerator() {}

    public NotificationGenerator(NotificationGeneratorBuilder builder) {
        this.objectType = builder.objectType;
        this.objectIdentifier = builder.objectIdentifier;
        this.action = builder.action;
        this.actor = builder.actor;
        this.isSystemGenerated = builder.isSystemGenerated;
        this.notificationContent = builder.notificationContent;
        this.createdAt = builder.createdAt;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public Long getObjectIdentifier() {
        return objectIdentifier;
    }

    public void setObjectIdentifier(Long objectIdentifier) {
        this.objectIdentifier = objectIdentifier;
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

    public boolean isSystemGenerated() {
        return isSystemGenerated;
    }

    public void setSystemGenerated(boolean systemGenerated) {
        isSystemGenerated = systemGenerated;
    }

    public String getNotificationContent() {
        return notificationContent;
    }

    public void setNotificationContent(String notificationContent) {
        this.notificationContent = notificationContent;
    }


    @Override
    public String toString() {
        return "NotificationGenerator{" +
                "id=" + getId() +
                ", objectType='" + objectType + '\'' +
                ", objectIdentifier=" + objectIdentifier +
                ", action='" + action + '\'' +
                ", actor='" + actor + '\'' +
                ", isSystemGenerated=" + isSystemGenerated +
                ", notificationContent='" + notificationContent + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    public static class NotificationGeneratorBuilder {
        private String objectType;
        private Long objectIdentifier;
        private String action;
        private String actor;
        private boolean isSystemGenerated;
        private String notificationContent;
        private String createdAt;

        public NotificationGeneratorBuilder() {}

        public NotificationGeneratorBuilder withObjectType(String objectType) {
            this.objectType = objectType;
            return this;
        }

        public NotificationGeneratorBuilder withObjectIdentifier(Long objectIdentifier) {
            this.objectIdentifier = objectIdentifier;
            return this;
        }

        public NotificationGeneratorBuilder withAction(String action) {
            this.action = action;
            return this;
        }

        public NotificationGeneratorBuilder withActor(String actor) {
            this.actor = actor;
            return this;
        }

        public NotificationGeneratorBuilder withSystemGenerated(boolean systemGenerated) {
            isSystemGenerated = systemGenerated;
            return this;
        }

        public NotificationGeneratorBuilder withNotificationContent(String notificationContent) {
            this.notificationContent = notificationContent;
            return this;
        }

        public NotificationGeneratorBuilder withCreatedAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public NotificationGenerator build() {
            return new NotificationGenerator(this);
        }

    }
}
