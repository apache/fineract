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
package org.apache.fineract.portfolio.address.data;

public class ClientAddressData {

	private final long clientAddressId;

	private final long client_id;

	private final long address_id;

	private final long address_type_id;

	private final boolean isActive;

	private ClientAddressData(final long clientAddressId, final long client_id, final long address_id,
			final long address_type_id, final boolean isActive) {
		this.clientAddressId = clientAddressId;
		this.client_id = client_id;
		this.address_id = address_id;
		this.address_type_id = address_type_id;
		this.isActive = isActive;
	}

	public static ClientAddressData instance(final long clientAddressId, final long client_id, final long address_id,
			final long address_type_id, final boolean isActive) {
		return new ClientAddressData(clientAddressId, client_id, address_id, address_type_id, isActive);
	}
}
