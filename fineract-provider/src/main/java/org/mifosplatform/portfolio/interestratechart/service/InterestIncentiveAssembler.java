/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.service;

import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.INCENTIVE_RESOURCE_NAME;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.amountParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.attributeNameParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.attributeValueParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.conditionTypeParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.entityTypeParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.incentiveTypeparamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.incentivesParamName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.interestratechart.domain.InterestIncentives;
import org.mifosplatform.portfolio.interestratechart.domain.InterestIncentivesFields;
import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChartSlab;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class InterestIncentiveAssembler {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public InterestIncentiveAssembler(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public Collection<InterestIncentives> assembleIncentivesFrom(final JsonElement element, InterestRateChartSlab interestRateChartSlab,
            final Locale locale) {
        final Collection<InterestIncentives> interestIncentivesSet = new HashSet<>();

        if (element.isJsonObject()) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            if (topLevelJsonElement.has(incentivesParamName) && topLevelJsonElement.get(incentivesParamName).isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get(incentivesParamName).getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject incentiveElement = array.get(i).getAsJsonObject();
                    final InterestIncentives incentives = this.assembleFrom(incentiveElement, interestRateChartSlab, locale);
                    interestIncentivesSet.add(incentives);
                }
            }
        }

        return interestIncentivesSet;
    }

    private InterestIncentives assembleFrom(final JsonElement element, final InterestRateChartSlab interestRateChartSlab,
            final Locale locale) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(INCENTIVE_RESOURCE_NAME);
        InterestIncentivesFields incentivesFields = createInterestIncentiveFields(element, baseDataValidator, locale);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
        return new InterestIncentives(interestRateChartSlab, incentivesFields);
    }

    private InterestIncentivesFields createInterestIncentiveFields(final JsonElement element, final DataValidatorBuilder baseDataValidator,
            final Locale locale) {
        Integer entityType = this.fromApiJsonHelper.extractIntegerNamed(entityTypeParamName, element, locale);
        Integer conditionType = this.fromApiJsonHelper.extractIntegerNamed(conditionTypeParamName, element, locale);
        Integer attributeName = this.fromApiJsonHelper.extractIntegerNamed(attributeNameParamName, element, locale);
        String attributeValue = this.fromApiJsonHelper.extractStringNamed(attributeValueParamName, element);
        Integer incentiveType = this.fromApiJsonHelper.extractIntegerNamed(incentiveTypeparamName, element, locale);
        BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed(amountParamName, element, locale);
        return InterestIncentivesFields.createNew(entityType, attributeName, conditionType, attributeValue, incentiveType, amount,
                baseDataValidator);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}
