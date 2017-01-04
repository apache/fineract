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

import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientAddressRepositoryWrapper {
	private final ClientAddressRepository clientAddressRepository;

	@Autowired
	public ClientAddressRepositoryWrapper(final ClientAddressRepository clientAddressRepository) {
		this.clientAddressRepository = clientAddressRepository;
	}

	public ClientAddress findOneByClientIdAndAddressTypeAndIsActive(final long clientId, final CodeValue addressType,
			final boolean isActive) {
		final ClientAddress clientAddress = this.clientAddressRepository
				.findByClientIdAndAddressTypeAndIsActive(clientId, addressType, isActive);
		// if (clientAddress == null) { throw new
		// AddressNotFoundException(clientId, addressType); }
		return clientAddress;
	}

	public ClientAddress findOneByClientIdAndAddressId(final long clientId, final long addressId) {
		final ClientAddress clientAddress = this.clientAddressRepository.findByClientIdAndAddressId(clientId,
				addressId);

		return clientAddress;
	}

}
