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
package org.apache.fineract.portfolio.collateralmanagement.service;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.collateralmanagement.api.CollateralManagementJsonInputParams;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralManagementCreateRequest;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralManagementDeleteResponse;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralManagementResponse;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralManagementUpdateRequest;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagementRepositoryWrapper;
import org.apache.fineract.portfolio.collateralmanagement.domain.CollateralManagementDomain;
import org.apache.fineract.portfolio.collateralmanagement.domain.CollateralManagementRepositoryWrapper;
import org.apache.fineract.portfolio.collateralmanagement.exception.ClientCollateralCannotBeDeletedException;
import org.apache.fineract.portfolio.collateralmanagement.exception.ClientCollateralNotFoundException;
import org.apache.fineract.portfolio.collateralmanagement.exception.CollateralNotFoundException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagement;

@RequiredArgsConstructor
public class ClientCollateralManagementWritePlatformServiceImpl implements ClientCollateralManagementWritePlatformService {

    private final ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper;
    private final CollateralManagementRepositoryWrapper collateralManagementRepositoryWrapper;
    private final ClientRepositoryWrapper clientRepositoryWrapper;

    @Transactional
    @Override
    public ClientCollateralManagementResponse addClientCollateralProduct(final ClientCollateralManagementCreateRequest request) {
        final JsonCommand command;

        Long collateralId = request.getCollateralId();
        BigDecimal quantity = request.getQuantity();

        final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(request.getClientId(), false);

        final CollateralManagementDomain collateralManagementData = this.collateralManagementRepositoryWrapper.getCollateral(collateralId);
        final ClientCollateralManagement clientCollateralManagement = ClientCollateralManagement.createNew(quantity, client,
                collateralManagementData);
        this.clientCollateralManagementRepositoryWrapper.saveAndFlush(clientCollateralManagement);

        return ClientCollateralManagementResponse.builder().clientId(client.getId()).resourceId(clientCollateralManagement.getId()).build();
    }

    @Transactional
    @Override
    public ClientCollateralManagementResponse updateClientCollateralProduct(final ClientCollateralManagementUpdateRequest request) {
        Long entityId = request.getCollateralId();

        validateForUpdate(request);

        BigDecimal quantity = request.getQuantity();
        final ClientCollateralManagement collateral = this.clientCollateralManagementRepositoryWrapper.getCollateral(entityId);
        final Map<String, Object> changes = this.update(quantity, collateral);
        this.clientCollateralManagementRepositoryWrapper.updateClientCollateralProduct(collateral);

        return ClientCollateralManagementResponse.builder().resourceId(collateral.getId()).clientId(request.getClientId()).changes(changes)
                .build();
    }

    private Map<String, Object> update(BigDecimal newValue, ClientCollateralManagement collateral) {
        final Map<String, Object> changes = new LinkedHashMap<>(3);
        final String quantityName = CollateralManagementJsonInputParams.QUANTITY.getValue();
        if (newValue.compareTo(collateral.getQuantity()) != 0) {
            collateral.updateQuantity(newValue);
            changes.put(quantityName, collateral.getQuantity());
        }
        return changes;
    }

    private void validateForUpdate(ClientCollateralManagementUpdateRequest request) {
        final Long clientCollateralId = request.getCollateralId();
        BigDecimal quantity = request.getQuantity();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("client-collateral");

        final ClientCollateralManagement clientCollateralManagement = this.clientCollateralManagementRepositoryWrapper
                .getCollateral(clientCollateralId);

        if (clientCollateralManagement == null) {
            throw new ClientCollateralNotFoundException(clientCollateralId);
        }

        BigDecimal totalQuantity = BigDecimal.ZERO;
        if (!clientCollateralManagement.getLoanCollateralManagementSet().isEmpty()) {
            for (LoanCollateralManagement loanCollateralManagement : clientCollateralManagement.getLoanCollateralManagementSet()) {
                totalQuantity = totalQuantity.add(loanCollateralManagement.getQuantity());
            }
        }

        if (totalQuantity.compareTo(quantity) >= 0) {
            baseDataValidator.reset().parameter("quantity").value(quantity).notLessThanMin(totalQuantity);
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

    }

    @Transactional
    @Override
    public ClientCollateralManagementDeleteResponse deleteClientCollateralProduct(final Long collateralId) {
        final ClientCollateralManagement clientCollateralManagement = this.clientCollateralManagementRepositoryWrapper
                .getCollateral(collateralId);

        validateForDeletion(clientCollateralManagement, collateralId);
        this.clientCollateralManagementRepositoryWrapper.deleteClientCollateralProduct(collateralId);

        return ClientCollateralManagementDeleteResponse.builder().resourceId(collateralId).build();
    }

    private void validateForDeletion(final ClientCollateralManagement clientCollateralManagement, final Long clientCollateralId) {
        if (clientCollateralManagement == null) {
            throw new CollateralNotFoundException(clientCollateralId);
        }

        if (!clientCollateralManagement.getLoanCollateralManagementSet().isEmpty()) {
            for (LoanCollateralManagement loanCollateralManagement : clientCollateralManagement.getLoanCollateralManagementSet()) {
                if (!loanCollateralManagement.isReleased()) {
                    throw new ClientCollateralCannotBeDeletedException(
                            ClientCollateralCannotBeDeletedException.ClientCollateralCannotBeDeletedReason.CLIENT_COLLATERAL_IS_ALREADY_ATTACHED,
                            clientCollateralId);
                }
            }
        }
    }

}
