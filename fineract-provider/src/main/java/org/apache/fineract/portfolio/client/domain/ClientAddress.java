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
package org.apache.fineract.portfolio.client.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.address.domain.Address;

@Entity
@Table(name = "m_client_address")
public class ClientAddress extends AbstractPersistableCustom<Long> {

	@ManyToOne
	private Client client;

	@ManyToOne
	private Address address;

	@ManyToOne
	@JoinColumn(name = "address_type_id")
	private CodeValue addressType;

	@Column(name = "is_active")
	private boolean isActive;

	private ClientAddress(final Client client, final Address address, final CodeValue addressType,
			final boolean isActive) {
		this.client = client;
		this.address = address;
		this.addressType = addressType;
		this.isActive = isActive;

	}

	public ClientAddress() {

	}

	public static ClientAddress fromJson(final boolean isActive, final Client client, final Address address,
			final CodeValue address_type) {

		return new ClientAddress(client, address, address_type, isActive);
	}

	public Client getClient() {
		return this.client;
	}

	public Address getAddress() {
		return this.address;
	}

	public CodeValue getAddressType() {
		return this.addressType;
	}

	public void setAddressType(final CodeValue addressType) {
		this.addressType = addressType;
	}

	public boolean isIs_active() {
		return this.isActive;
	}

	public void setClient(final Client client) {
		this.client = client;
	}

	public void setAddress(final Address address) {
		this.address = address;
	}

	public void setIs_active(final boolean isActive) {
		this.isActive = isActive;
	}

}
