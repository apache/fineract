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
package org.apache.fineract.portfolio.address.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class AddressNotFoundException extends AbstractPlatformResourceNotFoundException {

	public AddressNotFoundException(final long clientId) {
		super("error.msg.address.client.Identifier.not.found",
				"Client with client ID `" + clientId + "` is not mapped with any address", clientId);
	}

	public AddressNotFoundException(final long clientId, final long addressTypeId) {
		super("error.msg.address.client.addresstype.not.found",
				"Client with client ID`" + clientId + "` does not have address" + " type with id", addressTypeId);
	}
}
