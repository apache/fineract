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

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.data.ClientDataValidator;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ClientHasNoStaffException;
import org.apache.fineract.portfolio.client.exception.InvalidClientSavingProductException;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Assigns or unassigns a client's staff member and sets the client's default savings account.
 */
@AllArgsConstructor
@Service
@Slf4j
public class ClientAssignmentWritePlatformService {

    private final PlatformSecurityContext context;
    private final ClientRepositoryWrapper clientRepository;
    private final ClientDataValidator fromApiJsonDeserializer;
    private final StaffRepositoryWrapper staffRepository;
    private final SavingsAccountRepositoryWrapper savingsRepositoryWrapper;

    @Transactional
    public CommandProcessingResult unassignClientStaff(final Long clientId, final JsonCommand command) {

        this.context.authenticatedUser();

        final Map<String, Object> actualChanges = new LinkedHashMap<>(5);

        this.fromApiJsonDeserializer.validateForUnassignStaff(command.json());

        final Client clientForUpdate = this.clientRepository.findOneWithNotFoundDetection(clientId);

        final Staff presentStaff = clientForUpdate.getStaff();
        Long presentStaffId = null;
        if (presentStaff == null) {
            throw new ClientHasNoStaffException(clientId);
        }
        presentStaffId = presentStaff.getId();
        final String staffIdParamName = ClientApiConstants.staffIdParamName;
        if (!command.isChangeInLongParameterNamed(staffIdParamName, presentStaffId)) {
            clientForUpdate.unassignStaff();
        }
        this.clientRepository.saveAndFlush(clientForUpdate);

        actualChanges.put(staffIdParamName, presentStaffId);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withOfficeId(clientForUpdate.officeId()) //
                .withEntityId(clientForUpdate.getId()) //
                .withEntityExternalId(clientForUpdate.getExternalId()) //
                .withClientId(clientId) //
                .with(actualChanges) //
                .build();
    }

    public CommandProcessingResult assignClientStaff(final Long clientId, final JsonCommand command) {

        this.context.authenticatedUser();

        final Map<String, Object> actualChanges = new LinkedHashMap<>(5);

        this.fromApiJsonDeserializer.validateForAssignStaff(command.json());

        final Client clientForUpdate = this.clientRepository.findOneWithNotFoundDetection(clientId);
        Staff staff = null;
        final Long staffId = command.longValueOfParameterNamed(ClientApiConstants.staffIdParamName);
        if (staffId != null) {
            staff = this.staffRepository.findByOfficeHierarchyWithNotFoundDetection(staffId, clientForUpdate.getOffice().getHierarchy());
            /**
             * TODO Vishwas: We maintain history of chage of loan officer w.r.t loan in a history table, should we do
             * the same for a client? Especially useful when the change happens due to a transfer etc
             **/
            clientForUpdate.assignStaff(staff);
        }

        this.clientRepository.saveAndFlush(clientForUpdate);

        actualChanges.put(ClientApiConstants.staffIdParamName, staffId);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withOfficeId(clientForUpdate.officeId()) //
                .withEntityExternalId(clientForUpdate.getExternalId()) //
                .withEntityId(clientForUpdate.getId()) //
                .withClientId(clientId) //
                .with(actualChanges) //
                .build();
    }

    public CommandProcessingResult updateDefaultSavingsAccount(final Long clientId, final JsonCommand command) {

        this.context.authenticatedUser();

        final Map<String, Object> actualChanges = new LinkedHashMap<>(5);

        this.fromApiJsonDeserializer.validateForSavingsAccount(command.json());

        final Client clientForUpdate = this.clientRepository.findOneWithNotFoundDetection(clientId);

        SavingsAccount savingsAccount = null;
        final Long savingsId = command.longValueOfParameterNamed(ClientApiConstants.savingsAccountIdParamName);
        if (savingsId != null) {
            savingsAccount = this.savingsRepositoryWrapper.findOneWithNotFoundDetection(savingsId);
            if (!savingsAccount.getClient().identifiedBy(clientId)) {
                String defaultUserMessage = "saving account must belongs to client";
                throw new InvalidClientSavingProductException("saving.account", "must.belongs.to.client", defaultUserMessage, savingsId,
                        clientForUpdate.getId());
            }
            clientForUpdate.updateSavingsAccount(savingsId);
        }

        this.clientRepository.saveAndFlush(clientForUpdate);

        actualChanges.put(ClientApiConstants.savingsAccountIdParamName, savingsId);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withOfficeId(clientForUpdate.officeId()) //
                .withEntityId(clientForUpdate.getId()) //
                .withEntityExternalId(clientForUpdate.getExternalId()) //
                .withClientId(clientId) //
                .with(actualChanges) //
                .build();
    }
}
