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

public class TopicData implements Serializable {

	private final Long id;
	private final String title;
	private final boolean enabled;
	private final Long entityId;
	private final String entityType;
	private final String memberType;

	public TopicData(Long id, String title, boolean enabled, Long entityId, String entityType,
			String memberType) {
		this.id = id;
		this.title = title;
		this.enabled = enabled;
		this.entityId = entityId;
		this.entityType = entityType;
		this.memberType = memberType;
	}

	public TopicData(Long id, String title, Long entityId, String entityType,
			String memberType) {
		this.id = id;
		this.title = title;
		this.enabled = true;
		this.entityId = entityId;
		this.entityType = entityType;
		this.memberType = memberType;
	}

	public Long getId() {
		return this.id;
	}

	public String getTitle() {
		return this.title;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public Long getEntityId() {
		return this.entityId;
	}

	public String getEntityType() {
		return this.entityType;
	}

	public String getMemberType() {
		return this.memberType;
	}

}
