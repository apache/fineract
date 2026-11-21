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

import lombok.AllArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.springframework.stereotype.Service;

/**
 * Entry point for client write operations. The work is done by {@link ClientCreateUpdateWritePlatformService},
 * {@link ClientLifecycleWritePlatformService} and {@link ClientAssignmentWritePlatformService}.
 */
@AllArgsConstructor
@Service
public class ClientWritePlatformServiceJpaRepositoryImpl implements ClientWritePlatformService {

    private final ClientCreateUpdateWritePlatformService clientCreateUpdateWritePlatformService;
    private final ClientLifecycleWritePlatformService clientLifecycleWritePlatformService;
    private final ClientAssignmentWritePlatformService clientAssignmentWritePlatformService;

    @Override
    public CommandProcessingResult deleteClient(final Long clientId) {
        return this.clientCreateUpdateWritePlatformService.deleteClient(clientId);
    }

    @Override
    public CommandProcessingResult createClient(final JsonCommand command) {
        return this.clientCreateUpdateWritePlatformService.createClient(command);
    }

    @Override
    public CommandProcessingResult updateClient(final Long clientId, final JsonCommand command) {
        return this.clientCreateUpdateWritePlatformService.updateClient(clientId, command);
    }

    @Override
    public CommandProcessingResult activateClient(final Long clientId, final JsonCommand command) {
        return this.clientLifecycleWritePlatformService.activateClient(clientId, command);
    }

    @Override
    public CommandProcessingResult unassignClientStaff(final Long clientId, final JsonCommand command) {
        return this.clientAssignmentWritePlatformService.unassignClientStaff(clientId, command);
    }

    @Override
    public CommandProcessingResult assignClientStaff(final Long clientId, final JsonCommand command) {
        return this.clientAssignmentWritePlatformService.assignClientStaff(clientId, command);
    }

    @Override
    public CommandProcessingResult closeClient(final Long clientId, final JsonCommand command) {
        return this.clientLifecycleWritePlatformService.closeClient(clientId, command);
    }

    @Override
    public CommandProcessingResult updateDefaultSavingsAccount(final Long clientId, final JsonCommand command) {
        return this.clientAssignmentWritePlatformService.updateDefaultSavingsAccount(clientId, command);
    }

    @Override
    public CommandProcessingResult rejectClient(final Long entityId, final JsonCommand command) {
        return this.clientLifecycleWritePlatformService.rejectClient(entityId, command);
    }

    @Override
    public CommandProcessingResult withdrawClient(Long entityId, JsonCommand command) {
        return this.clientLifecycleWritePlatformService.withdrawClient(entityId, command);
    }

    @Override
    public CommandProcessingResult reActivateClient(Long entityId, JsonCommand command) {
        return this.clientLifecycleWritePlatformService.reActivateClient(entityId, command);
    }

    @Override
    public CommandProcessingResult undoRejection(Long entityId, JsonCommand command) {
        return this.clientLifecycleWritePlatformService.undoRejection(entityId, command);
    }

    @Override
    public CommandProcessingResult undoWithdrawal(Long entityId, JsonCommand command) {
        return this.clientLifecycleWritePlatformService.undoWithdrawal(entityId, command);
    }
}
