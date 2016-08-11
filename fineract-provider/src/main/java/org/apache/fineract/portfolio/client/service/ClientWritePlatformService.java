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

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

public interface ClientWritePlatformService {

    CommandProcessingResult createClient(JsonCommand command);

    CommandProcessingResult updateClient(Long clientId, JsonCommand command);

    CommandProcessingResult activateClient(Long clientId, JsonCommand command);

    CommandProcessingResult deleteClient(Long clientId);

    CommandProcessingResult unassignClientStaff(Long clientId, JsonCommand command);

    CommandProcessingResult closeClient(final Long clientId, final JsonCommand command);

    CommandProcessingResult assignClientStaff(Long clientId, JsonCommand command);

    CommandProcessingResult updateDefaultSavingsAccount(Long clientId, JsonCommand command);

    CommandProcessingResult rejectClient(Long entityId, JsonCommand command);

    CommandProcessingResult withdrawClient(Long entityId, JsonCommand command);

    CommandProcessingResult reActivateClient(Long entityId, JsonCommand command);

	CommandProcessingResult undoRejection(Long entityId, JsonCommand command);
	
	CommandProcessingResult undoWithdrawal(Long entityId, JsonCommand command);


}