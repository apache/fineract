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

public final class ClientAddressData {

    private final long clientAddressID;

    private final long clientID;

    private final long addressID;

    private final long addressTypeID;

    private final boolean isActive;

    private ClientAddressData(final long clientAddressID, final long client_id, final long address_id, final long address_type_id,
            final boolean isActive) {
        this.clientAddressID = clientAddressID;
        this.clientID = client_id;
        this.addressID = address_id;
        this.addressTypeID = address_type_id;
        this.isActive = isActive;
    }

    public static ClientAddressData instance(final long clientAddressID, final long client_id, final long address_id,
            final long address_type_id, final boolean isActive) {
        return new ClientAddressData(clientAddressID, client_id, address_id, address_type_id, isActive);
    }
}
