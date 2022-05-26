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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.transaction.Transactional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagementRepositoryWrapper;
import org.apache.fineract.portfolio.collateralmanagement.domain.CollateralManagementDomain;
import org.apache.fineract.portfolio.collateralmanagement.domain.CollateralManagementRepositoryWrapper;
import org.apache.fineract.portfolio.collateralmanagement.exception.ClientCollateralCannotBeDeletedException;
import org.apache.fineract.portfolio.collateralmanagement.exception.ClientCollateralNotFoundException;
import org.apache.fineract.portfolio.collateralmanagement.exception.CollateralNotFoundException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientCollateralManagementWritePlatformServiceImpl implements ClientCollateralManagementWritePlatformService {

    private final ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper;
    private final CollateralManagementRepositoryWrapper collateralManagementRepositoryWrapper;
    private final ClientRepositoryWrapper clientRepositoryWrapper;

    @Autowired
    public ClientCollateralManagementWritePlatformServiceImpl(
            final ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper,
            final CollateralManagementRepositoryWrapper collateralManagementRepositoryWrapper,
            final ClientRepositoryWrapper clientRepositoryWrapper) {
        this.clientCollateralManagementRepositoryWrapper = clientCollateralManagementRepositoryWrapper;
        this.collateralManagementRepositoryWrapper = collateralManagementRepositoryWrapper;
        this.clientRepositoryWrapper = clientRepositoryWrapper;
    }

    @Transactional
    @Override
    public CommandProcessingResult addClientCollateralProduct(final JsonCommand command) {

        validateForCreation(command);

        Long collateralId = command.longValueOfParameterNamed("collateralId");
        BigDecimal quantity = command.bigDecimalValueOfParameterNamed("quantity");

        final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(command.getClientId(), false);

        final CollateralManagementDomain collateralManagementData = this.collateralManagementRepositoryWrapper.getCollateral(collateralId);
        final ClientCollateralManagement clientCollateralManagement = ClientCollateralManagement.createNew(quantity, client,
                collateralManagementData);
        this.clientCollateralManagementRepositoryWrapper.saveAndFlush(clientCollateralManagement);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withClientId(command.getClientId())
                .withEntityId(clientCollateralManagement.getId()).build();
    }

    private void validateForCreation(final JsonCommand command) {

        String errorCode = "parameter.";
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("client-collateral");

        if (!command.parameterExists("collateralId")) {
            errorCode += "collateralId.not.exists";
            baseDataValidator.reset().parameter("collateralId").failWithCode(errorCode);
        }

        if (!command.parameterExists("quantity")) {
            errorCode += ".quantity.not.exists";
            baseDataValidator.reset().parameter("quantity").failWithCode(errorCode);
        } else {
            BigDecimal quantity = command.bigDecimalValueOfParameterNamed("quantity");
            baseDataValidator.reset().parameter("quantity").value(quantity).notNull().positiveAmount();
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateClientCollateralProduct(final JsonCommand command) {
        validateForUpdate(command);
        final ClientCollateralManagement collateral = this.clientCollateralManagementRepositoryWrapper.getCollateral(command.entityId());
        final Map<String, Object> changes = collateral.update(command);
        this.clientCollateralManagementRepositoryWrapper.updateClientCollateralProduct(collateral);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(command.entityId())
                .withClientId(command.getClientId()).with(changes).build();
    }

    private void validateForUpdate(final JsonCommand command) {
        final Long clientCollateralId = command.entityId();
        String errorCode = "parameter.";
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("client-collateral");
        BigDecimal quantity = null;

        if (!command.parameterExists("quantity")) {
            errorCode += ".quantity.not.exists";
            baseDataValidator.reset().parameter("quantity").failWithCode(errorCode);
        } else {
            quantity = command.bigDecimalValueOfParameterNamed("quantity");
            baseDataValidator.reset().parameter("quantity").value(quantity).notNull().positiveAmount();
        }

        final ClientCollateralManagement clientCollateralManagement = this.clientCollateralManagementRepositoryWrapper
                .getCollateral(clientCollateralId);

        if (clientCollateralManagement == null) {
            throw new ClientCollateralNotFoundException(clientCollateralId);
        }

        BigDecimal totalQuantity = BigDecimal.ZERO;
        if (clientCollateralManagement.getLoanCollateralManagementSet().size() > 0) {
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
    public CommandProcessingResult deleteClientCollateralProduct(final Long collateralId) {
        final ClientCollateralManagement clientCollateralManagement = this.clientCollateralManagementRepositoryWrapper
                .getCollateral(collateralId);
        validateForDeletion(clientCollateralManagement, collateralId);
        this.clientCollateralManagementRepositoryWrapper.deleteClientCollateralProduct(collateralId);
        return new CommandProcessingResultBuilder().withEntityId(collateralId).build();
    }

    private void validateForDeletion(final ClientCollateralManagement clientCollateralManagement, final Long clientCollateralId) {
        if (clientCollateralManagement == null) {
            throw new CollateralNotFoundException(clientCollateralId);
        }

        if (clientCollateralManagement.getLoanCollateralManagementSet().size() > 0) {
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
