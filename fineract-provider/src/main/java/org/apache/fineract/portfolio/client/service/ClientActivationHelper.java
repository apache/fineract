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

import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.LegalForm;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.exception.GroupMemberCountNotInPermissibleRangeException;
import org.apache.fineract.portfolio.savings.data.SavingsAccountDataDTO;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.service.SavingsApplicationProcessWritePlatformService;
import org.springframework.stereotype.Component;

/**
 * Steps shared by activating an existing client and creating a client that is active straight away.
 */
@AllArgsConstructor
@Component
public class ClientActivationHelper {

    private final SavingsAccountRepositoryWrapper savingsRepositoryWrapper;
    private final SavingsApplicationProcessWritePlatformService savingsApplicationProcessWritePlatformService;
    private final ConfigurationDomainService configurationDomainService;
    private final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;

    public CommandProcessingResult openSavingsAccount(final Client client, final DateTimeFormatter fmt) {
        CommandProcessingResult commandProcessingResult = CommandProcessingResult.empty();
        if (client.isActive() && client.savingsProductId() != null) {
            SavingsAccountDataDTO savingsAccountDataDTO = new SavingsAccountDataDTO(client, null, client.savingsProductId(),
                    client.getActivationDate(), client.activatedBy(), fmt);
            commandProcessingResult = this.savingsApplicationProcessWritePlatformService.createActiveApplication(savingsAccountDataDTO);
            if (commandProcessingResult.getSavingsId() != null) {
                this.savingsRepositoryWrapper.findOneWithNotFoundDetection(commandProcessingResult.getSavingsId());
                client.updateSavingsAccount(commandProcessingResult.getSavingsId());
                client.updateSavingsProduct(null);
            }
        }
        return commandProcessingResult;
    }

    /*
     * To become a part of a group, group may have set of criteria to be m et before client can become member of it.
     */
    public void validateParentGroupRulesBeforeClientActivation(Client client) {
        Integer minNumberOfClients = configurationDomainService.retrieveMinAllowedClientsInGroup();
        Integer maxNumberOfClients = configurationDomainService.retrieveMaxAllowedClientsInGroup();
        if (client.getGroups() != null && maxNumberOfClients != null) {
            for (Group group : client.getGroups()) {
                /**
                 * Since this Client has not yet been associated with the group, reduce maxNumberOfClients by 1
                 **/
                final boolean validationsuccess = group.isGroupsClientCountWithinMaxRange(maxNumberOfClients - 1);
                if (!validationsuccess) {
                    throw new GroupMemberCountNotInPermissibleRangeException(group.getId(), minNumberOfClients, maxNumberOfClients);
                }
            }
        }
    }

    public void runEntityDatatableCheck(final Long clientId, final Integer legalFormId) {
        final LegalForm legalForm = LegalForm.fromInt(legalFormId);
        entityDatatableChecksWritePlatformService.runTheCheck(clientId, EntityTables.CLIENT.getName(), StatusEnum.ACTIVATE.getValue(),
                EntityTables.CLIENT.getForeignKeyColumnNameOnDatatable(), legalForm.getLabel());
    }
}
