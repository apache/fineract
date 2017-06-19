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
	private boolean isSystemGenerated;
	private String tenantIdentifier;
	private String createdAt;
	private Long officeId;
	private Long topicId;
	private List<Long> userIds;
	
	public NotificationData() {
	}

	public NotificationData(String objectType, Long objectId, String action, Long actorId, String content,
			boolean isSystemGenerated, String tenantIdentifier, Long officeId, Long topicId, List<Long> userIds) {
		this.objectType = objectType;
		this.objectId = objectId;
		this.action = action;
		this.actorId = actorId;
		this.content = content;
		this.isSystemGenerated = isSystemGenerated;
		this.tenantIdentifier = tenantIdentifier;
		this.officeId = officeId;
		this.topicId = topicId;
		this.userIds = userIds;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getObjectType() {
		return this.objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public Long getObjectIdentfier() {
		return this.objectId;
	}

	public void setObjectIdentfier(Long objectId) {
		this.objectId = objectId;
	}

	public String getAction() {
		return this.action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Long getActorId() {
		return this.actorId;
	}

	public void setActorId(Long actorId) {
		this.actorId = actorId;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isSystemGenerated() {
		return this.isSystemGenerated;
	}

	public void setSystemGenerated(boolean isSystemGenerated) {
		this.isSystemGenerated = isSystemGenerated;
	}

	public String getTenantIdentifier() {
		return this.tenantIdentifier;
	}

	public void setTenantIdentifier(String tenantIdentifier) {
		this.tenantIdentifier = tenantIdentifier;
	}

	public String getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public Long getOfficeId() {
		return this.officeId;
	}

	public void setOfficeId(Long officeId) {
		this.officeId = officeId;
	}

	public Long getTopicId() {
		return this.topicId;
	}

	public void setTopicId(Long topicId) {
		this.topicId = topicId;
	}

	public List<Long> getUserIds() {
		return this.userIds;
	}

	public void setUserIds(List<Long> userIds) {
		this.userIds = userIds;
	}
	
}
