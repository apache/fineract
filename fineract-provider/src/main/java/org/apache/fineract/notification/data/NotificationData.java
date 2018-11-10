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
package org.apache.fineract.notification.data;

import java.io.Serializable;
import java.util.List;

public class NotificationData implements Serializable {

    private Long id;
    private String objectType;
    private Long objectId;
    private String action;
    private Long actorId;
    private String content;
    private boolean isRead;
    private boolean isSystemGenerated;
    private String tenantIdentifier;
    private String createdAt;
    private Long officeId;
    private List<Long> userIds;

    public NotificationData() {

    }

    public NotificationData(String objectType, Long objectId, String action, Long actorId, String content, boolean isSystemGenerated,
    		boolean isRead, String tenantIdentifier, Long officeId, List<Long> userIds) {
        this.objectType = objectType;
        this.objectId = objectId;
        this.action = action;
        this.actorId = actorId;
        this.content = content;
        this.isRead = isRead;
        this.isSystemGenerated = isSystemGenerated;
        this.tenantIdentifier = tenantIdentifier;
        this.officeId = officeId;
        this.userIds = userIds;
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

    public Long getActor() {
        return actorId;
    }

    public void setActor(Long actorId) {
        this.actorId = actorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
	public boolean isRead() {
		return this.isRead;
	}

	public void setRead(boolean isRead) {
		this.isRead = isRead;
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

}
