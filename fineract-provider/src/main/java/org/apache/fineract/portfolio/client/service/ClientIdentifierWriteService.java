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
package org.apache.fineract.portfolio.client.service;

import org.apache.fineract.portfolio.client.data.ClientIdentifierCreateRequest;
import org.apache.fineract.portfolio.client.data.ClientIdentifierCreateResponse;
import org.apache.fineract.portfolio.client.data.ClientIdentifierDeleteRequest;
import org.apache.fineract.portfolio.client.data.ClientIdentifierDeleteResponse;
import org.apache.fineract.portfolio.client.data.ClientIdentifierUpdateRequest;
import org.apache.fineract.portfolio.client.data.ClientIdentifierUpdateResponse;

public interface ClientIdentifierWriteService {

    ClientIdentifierCreateResponse createClientIdentifier(ClientIdentifierCreateRequest request);

    ClientIdentifierUpdateResponse updateClientIdentifier(ClientIdentifierUpdateRequest request);

    ClientIdentifierDeleteResponse deleteClientIdentifier(ClientIdentifierDeleteRequest request);
}
