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
package org.apache.fineract.infrastructure.gcm.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.client.domain.Client;

@Entity
@Table(name = "client_device_registration")
public class DeviceRegistration extends AbstractPersistableCustom<Long> {

	@OneToOne
	@JoinColumn(name = "client_id", nullable = false, unique = true)
	private Client client;

	@Column(name = "registration_id", nullable = false, unique = true)
	private String registrationId;

	@Column(name = "updatedon_date", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedOnDate;

	private DeviceRegistration(final Client client, final String registrationId) {
		this.client = client;
		this.registrationId = registrationId;
		this.updatedOnDate = DateUtils.getLocalDateTimeOfTenant().toDate();
	}

	public static DeviceRegistration instance(final Client client,
			final String registrationId) {
		return new DeviceRegistration(client, registrationId);
	}

	public Client getClient() {
		return this.client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public String getRegistrationId() {
		return this.registrationId;
	}

	public void setRegistrationId(String registrationId) {
		this.registrationId = registrationId;
	}

	public Date getUpdatedOnDate() {
		return this.updatedOnDate;
	}

	public void setUpdatedOnDate(Date updatedOnDate) {
		this.updatedOnDate = updatedOnDate;
	}

}
