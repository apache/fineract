/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.notification.domain;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "notification_generator")
public class NotificationGenerator extends AbstractPersistableCustom<Long> {

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
