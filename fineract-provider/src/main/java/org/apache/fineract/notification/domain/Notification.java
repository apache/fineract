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
public class Notification extends AbstractPersistableCustom<Long> {

    @Column(name = "object_type")
    private String objectType;

    @Column(name = "object_identifier")
    private Long objectIdentifier;

    @Column(name = "action")
    private String action;

    @Column(name = "actor")
    private Long actorId;

    @Column(name = "is_system_generated")
    private boolean isSystemGenerated;

    @Column(name = "notification_content")
    private String notificationContent;

    @Column(name = "created_at")
    private String createdAt;

    public Notification() {}

    public Notification(String objectType, Long objectIdentifier, String action, Long actorId, boolean isSystemGenerated,
                        String notificationContent, String createdAt) {
        this.objectType = objectType;
        this.objectIdentifier = objectIdentifier;
        this.action = action;
        this.actorId = actorId;
        this.isSystemGenerated = isSystemGenerated;
        this.notificationContent = notificationContent;
        this.createdAt = createdAt;
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

    public Long getActor() {
        return actorId;
    }

    public void setActor(Long actor) {
        this.actorId = actorId;
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
}