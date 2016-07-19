package org.apache.fineract.notification.domain;


import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

@Entity
@Table(name = "notification_mapper")
public class NotificationMapper extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "notification_id")
    private NotificationGenerator notificationGenerator;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser userId;

    @Column(name = "is_read")
    private boolean isRead;

    @Column(name = "created_at")
    private String createdAt;

    public NotificationMapper() {}

    public NotificationMapper(NotificationMapperBuilder builder) {
        this.notificationGenerator = builder.notificationGenerator;
        this.userId = builder.userId;
        this.isRead = builder.isRead;
        this.createdAt = builder.createdAt;
    }

    @Override
    public String toString() {
        return "NotificationMapper{" +
                "id=" + getId() +
                ", notificationGenerator=" + notificationGenerator +
                ", userId=" + userId +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }

    public NotificationGenerator getNotificationGenerator() {
        return notificationGenerator;
    }

    public void setNotificationGenerator(NotificationGenerator notificationGenerator) {
        this.notificationGenerator = notificationGenerator;
    }

    public AppUser getUserId() {
        return userId;
    }

    public void setUserId(AppUser userId) {
        this.userId = userId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public static class NotificationMapperBuilder {
        private NotificationGenerator notificationGenerator;
        private AppUser userId;
        private boolean isRead;
        private String createdAt;

        public NotificationMapperBuilder() {}

        public NotificationMapperBuilder withNotification(NotificationGenerator notificationGenerator) {
            this.notificationGenerator = notificationGenerator;
            return this;
        }

        public NotificationMapperBuilder withUser(AppUser userId) {
            this.userId = userId;
            return this;
        }

        public NotificationMapperBuilder withIsRead(boolean read) {
            isRead = read;
            return this;
        }

        public NotificationMapperBuilder withCreatedAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public NotificationMapper build() {
            return new NotificationMapper(this);
        }
    }
}
