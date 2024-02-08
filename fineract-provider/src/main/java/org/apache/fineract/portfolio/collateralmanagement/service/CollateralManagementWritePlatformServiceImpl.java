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

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepository;
import org.apache.fineract.portfolio.collateralmanagement.api.CollateralManagementJsonInputParams;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.apache.fineract.portfolio.collateralmanagement.domain.CollateralManagementDomain;
import org.apache.fineract.portfolio.collateralmanagement.domain.CollateralManagementRepositoryWrapper;
import org.apache.fineract.portfolio.collateralmanagement.exception.CollateralCannotBeDeletedException;
import org.apache.fineract.portfolio.collateralmanagement.exception.CollateralNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class CollateralManagementWritePlatformServiceImpl implements CollateralManagementWritePlatformService {

    private final CollateralManagementRepositoryWrapper collateralManagementRepositoryWrapper;
    private final ApplicationCurrencyRepository applicationCurrencyRepository;
    private final FromJsonHelper fromApiJsonHelper;

    @Transactional
    @Override
    public CommandProcessingResult createCollateral(final JsonCommand jsonCommand) {
        validateForCreation(jsonCommand);
        final String currencyParamName = jsonCommand.stringValueOfParameterNamed(CollateralManagementJsonInputParams.CURRENCY.getValue());
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneByCode(currencyParamName);

        final CollateralManagementDomain collateral = CollateralManagementDomain.createNew(jsonCommand, applicationCurrency);
        this.collateralManagementRepositoryWrapper.create(collateral);
        return new CommandProcessingResultBuilder().withCommandId(jsonCommand.commandId()).withEntityId(collateral.getId()).build();
    }

    private void validateForCreation(JsonCommand jsonCommand) {
        final JsonElement jsonElement = this.fromApiJsonHelper.parse(jsonCommand.json());
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("collateral-management");

        if (!jsonCommand.parameterExists("locale")) {
            baseDataValidator.reset().parameter("locale").notNull().failWithCode("locale.not.exists");
        } else {
            final String locale = this.fromApiJsonHelper.extractStringNamed("locale", jsonElement);
            baseDataValidator.reset().parameter("locale").value(locale).notNull();
        }

        if (!jsonCommand.parameterExists(CollateralManagementJsonInputParams.CURRENCY.getValue())) {
            baseDataValidator.reset().parameter(CollateralManagementJsonInputParams.CURRENCY.getValue()).notNull()
                    .failWithCode("currency.not.exists");
        } else {
            final String currency = this.fromApiJsonHelper.extractStringNamed("currency", jsonElement);
            baseDataValidator.reset().parameter("currency").value(currency).notNull();
        }

        if (!jsonCommand.parameterExists("quality")) {
            baseDataValidator.reset().parameter(CollateralManagementJsonInputParams.QUALITY.getValue()).notNull()
                    .failWithCode("quality.not.exists");
        } else {
            final String quality = this.fromApiJsonHelper.extractStringNamed("quality", jsonElement);
            baseDataValidator.reset().parameter("quality").value(quality).notNull();
        }

        if (!jsonCommand.parameterExists("basePrice")) {
            baseDataValidator.reset().parameter("basePrice").notNull().notBlank().failWithCode("basePrice.not.exists");
        } else {
            final BigDecimal basePrice = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("basePrice", jsonElement);
            baseDataValidator.reset().parameter("basePrice").value(basePrice).notNull().positiveAmount();
        }

        if (!jsonCommand.parameterExists("unitType")) {
            baseDataValidator.reset().parameter("unitType").notNull().notBlank().failWithCode("unitType.not.exists");
        } else {
            final String unitType = this.fromApiJsonHelper.extractStringNamed("unitType", jsonElement);
            baseDataValidator.reset().parameter("unitType").value(unitType).notNull();
        }

        if (!jsonCommand.parameterExists("pctToBase")) {
            baseDataValidator.reset().parameter("pctToBase").notNull().notBlank().failWithCode("pctToBase.not.exists");
        } else {
            final BigDecimal basePrice = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("pctToBase", jsonElement);
            baseDataValidator.reset().parameter("pctToBase").value(basePrice).notNull().positiveAmount();
        }

        if (!jsonCommand.parameterExists("name")) {
            baseDataValidator.reset().parameter("name").notNull().notBlank().failWithCode("name.not.exists");
        } else {
            final String unitType = this.fromApiJsonHelper.extractStringNamed("name", jsonElement);
            baseDataValidator.reset().parameter("name").value(unitType).notNull();
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateCollateral(final Long collateralId, JsonCommand jsonCommand) {
        final CollateralManagementDomain collateral = this.collateralManagementRepositoryWrapper.getCollateral(collateralId);
        final String currencyParamName = CollateralManagementJsonInputParams.CURRENCY.getValue();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository
                .findOneByCode(jsonCommand.stringValueOfParameterNamed(currencyParamName));
        if (jsonCommand.isChangeInStringParameterNamed(currencyParamName, applicationCurrency.getCode())) {
            final String newValue = jsonCommand.stringValueOfParameterNamed(currencyParamName);
            applicationCurrency.setCode(newValue);
        }
        final Map<String, Object> changes = collateral.update(jsonCommand, applicationCurrency);
        this.collateralManagementRepositoryWrapper.update(collateral);
        return new CommandProcessingResultBuilder().withCommandId(jsonCommand.commandId()).withEntityId(jsonCommand.entityId())
                .with(changes).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteCollateral(final Long collateralId) {
        final CollateralManagementDomain collateralManagementDomain = this.collateralManagementRepositoryWrapper
                .getCollateral(collateralId);
        validateForDeletion(collateralManagementDomain, collateralId);
        this.collateralManagementRepositoryWrapper.delete(collateralId);
        return new CommandProcessingResultBuilder().withEntityId(collateralId).build();
    }

    private void validateForDeletion(final CollateralManagementDomain collateralManagementDomain, final Long collateralId) {
        if (collateralManagementDomain == null) {
            throw new CollateralNotFoundException(collateralId);
        }

        if (collateralManagementDomain.getClientCollateralManagements().size() > 0) {
            for (ClientCollateralManagement clientCollateralManagement : collateralManagementDomain.getClientCollateralManagements()) {
                if (clientCollateralManagement.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                    throw new CollateralCannotBeDeletedException(
                            CollateralCannotBeDeletedException.CollateralCannotBeDeletedReason.COLLATERAL_IS_ALREADY_ATTACHED,
                            collateralId);
                }
            }
        }
    }

}
