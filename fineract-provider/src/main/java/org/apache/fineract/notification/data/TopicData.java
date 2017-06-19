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

import org.joda.time.LocalDate;

public class TopicData implements Serializable {

	private final Long id;
	private final String name;
	private final Long permissionId;
	private final Boolean active;
	private final LocalDate activationDate;

	public TopicData(Long id, String name, Long permissionId, Boolean active, LocalDate activationDate) {
		this.id = id;
		this.name = name;
		this.permissionId = permissionId;
		this.active = active;
		this.activationDate = activationDate;
	}	

	public TopicData(Long id, String name, Long permissionId, LocalDate activationDate) {
		this.id = id;
		this.name = name;
		this.permissionId = permissionId;
		this.active = true;
		this.activationDate = activationDate;
	}

	public TopicData(Long id, String name, Long permissionId, Boolean active) {
		this.id = id;
		this.name = name;
		this.permissionId = permissionId;
		this.active = active;
		this.activationDate = new LocalDate();
	}

	public TopicData(Long id, String name, Long permissionId) {
		this.id = id;
		this.name = name;
		this.permissionId = permissionId;
		this.active = true;
		this.activationDate = new LocalDate();
	}

	public Boolean getActive() {
		return this.active;
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public Long getPermissionId() {
		return this.permissionId;
	}

	public LocalDate getActivationDate() {
		return this.activationDate;
	}
	
}
