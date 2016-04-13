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
package org.apache.fineract.portfolio.charge.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.charge.api.ChargesApiConstants;
import org.apache.fineract.portfolio.charge.domain.ChargeAppliesTo;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.joda.time.MonthDay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class ChargeDefinitionCommandFromApiJsonDeserializer {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<>(Arrays.asList("name", "amount", "locale", "currencyCode",
            "currencyOptions", "chargeAppliesTo", "chargeTimeType", "chargeCalculationType", "chargeCalculationTypeOptions", "penalty",
            "active", "chargePaymentMode", "feeOnMonthDay", "feeInterval", "monthDayFormat", "minCap", "maxCap", "feeFrequency",
            ChargesApiConstants.glAccountIdParamName, ChargesApiConstants.taxGroupIdParamName));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public ChargeDefinitionCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charge");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Integer chargeAppliesTo = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargeAppliesTo", element);
        baseDataValidator.reset().parameter("chargeAppliesTo").value(chargeAppliesTo).notNull();
        if (chargeAppliesTo != null) {
            baseDataValidator.reset().parameter("chargeAppliesTo").value(chargeAppliesTo).isOneOfTheseValues(ChargeAppliesTo.validValues());
        }

        final Integer chargeCalculationType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargeCalculationType", element);
        baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType).notNull();

        final Integer feeInterval = this.fromApiJsonHelper.extractIntegerNamed("feeInterval", element, Locale.getDefault());
        baseDataValidator.reset().parameter("feeInterval").value(feeInterval).integerGreaterThanZero();

        final Integer feeFrequency = this.fromApiJsonHelper.extractIntegerNamed("feeFrequency", element, Locale.getDefault());
        baseDataValidator.reset().parameter("feeFrequency").value(feeFrequency).inMinMaxRange(0, 3);

        if (feeFrequency != null) {
            baseDataValidator.reset().parameter("feeInterval").value(feeInterval).notNull();
        }

        final ChargeAppliesTo appliesTo = ChargeAppliesTo.fromInt(chargeAppliesTo);
        if (appliesTo.isLoanCharge()) {
            // loan applicable validation
            final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargeTimeType", element);
            baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType).notNull();
            if (chargeTimeType != null) {
                baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType)
                        .isOneOfTheseValues(ChargeTimeType.validLoanValues());
            }

            final Integer chargePaymentMode = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargePaymentMode", element);
            baseDataValidator.reset().parameter("chargePaymentMode").value(chargePaymentMode).notNull()
                    .isOneOfTheseValues(ChargePaymentMode.validValues());
            if (chargePaymentMode != null) {
                baseDataValidator.reset().parameter("chargePaymentMode").value(chargePaymentMode)
                        .isOneOfTheseValues(ChargePaymentMode.validValues());
            }

            if (chargeCalculationType != null) {
                baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType)
                        .isOneOfTheseValues(ChargeCalculationType.validValuesForLoan());
            }

            if (chargeTimeType != null && chargeCalculationType != null) {
                performChargeTimeNCalculationTypeValidation(baseDataValidator, chargeTimeType, chargeCalculationType);
            }

        } else if (appliesTo.isSavingsCharge()) {
            // savings applicable validation
            final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargeTimeType", element);
            baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType).notNull();
            if (chargeTimeType != null) {
                baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType)
                        .isOneOfTheseValues(ChargeTimeType.validSavingsValues());
            }

            final ChargeTimeType ctt = ChargeTimeType.fromInt(chargeTimeType);

            if (ctt.isWeeklyFee()) {
                final String monthDay = this.fromApiJsonHelper.extractStringNamed("feeOnMonthDay", element);
                baseDataValidator.reset().parameter("feeOnMonthDay").value(monthDay)
                        .mustBeBlankWhenParameterProvidedIs("chargeTimeType", chargeTimeType);
            }

            if (ctt.isMonthlyFee()) {
                final MonthDay monthDay = this.fromApiJsonHelper.extractMonthDayNamed("feeOnMonthDay", element);
                baseDataValidator.reset().parameter("feeOnMonthDay").value(monthDay).notNull();

                baseDataValidator.reset().parameter("feeInterval").value(feeInterval).notNull().inMinMaxRange(1, 12);
            }

            if (ctt.isAnnualFee()) {
                final MonthDay monthDay = this.fromApiJsonHelper.extractMonthDayNamed("feeOnMonthDay", element);
                baseDataValidator.reset().parameter("feeOnMonthDay").value(monthDay).notNull();
            }

            if (chargeCalculationType != null) {
                baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType)
                        .isOneOfTheseValues(ChargeCalculationType.validValuesForSavings());
            }
        } else if (appliesTo.isClientCharge()) {
            // client applicable validation
            final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargeTimeType", element);
            baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType).notNull();
            if (chargeTimeType != null) {
                baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType)
                        .isOneOfTheseValues(ChargeTimeType.validClientValues());
            }

            if (chargeCalculationType != null) {
                baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType)
                        .isOneOfTheseValues(ChargeCalculationType.validValuesForClients());
            }

            // GL Account can be linked to clients
            if (this.fromApiJsonHelper.parameterExists(ChargesApiConstants.glAccountIdParamName, element)) {
                final Long glAccountId = this.fromApiJsonHelper.extractLongNamed(ChargesApiConstants.glAccountIdParamName, element);
                baseDataValidator.reset().parameter(ChargesApiConstants.glAccountIdParamName).value(glAccountId).notNull()
                        .longGreaterThanZero();
            }

        }else if(appliesTo.isSharesCharge()) {
            final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargeTimeType", element);
            baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType).notNull();
            if (chargeTimeType != null) {
                baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType)
                        .isOneOfTheseValues(ChargeTimeType.validShareValues());
            }
            
            if (chargeCalculationType != null) {
                baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType)
                        .isOneOfTheseValues(ChargeCalculationType.validValuesForShares());
            }
            
            if(chargeTimeType != null && chargeTimeType.equals(ChargeTimeType.SHAREACCOUNT_ACTIVATION.getValue())) {
                if(chargeCalculationType != null) {
                    baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType)
                    .isOneOfTheseValues(ChargeCalculationType.validValuesForShareAccountActivation());
                }
            }
        }

        final String name = this.fromApiJsonHelper.extractStringNamed("name", element);
        baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(100);

        final String currencyCode = this.fromApiJsonHelper.extractStringNamed("currencyCode", element);
        baseDataValidator.reset().parameter("currencyCode").value(currencyCode).notBlank().notExceedingLengthOf(3);

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element.getAsJsonObject());
        baseDataValidator.reset().parameter("amount").value(amount).notNull().positiveAmount();

        if (this.fromApiJsonHelper.parameterExists("penalty", element)) {
            final Boolean penalty = this.fromApiJsonHelper.extractBooleanNamed("penalty", element);
            baseDataValidator.reset().parameter("penalty").value(penalty).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists("active", element)) {
            final Boolean active = this.fromApiJsonHelper.extractBooleanNamed("active", element);
            baseDataValidator.reset().parameter("active").value(active).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists("minCap", element)) {
            final BigDecimal minCap = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minCap", element.getAsJsonObject());
            baseDataValidator.reset().parameter("minCap").value(minCap).notNull().positiveAmount();
        }
        if (this.fromApiJsonHelper.parameterExists("maxCap", element)) {
            final BigDecimal maxCap = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maxCap", element.getAsJsonObject());
            baseDataValidator.reset().parameter("maxCap").value(maxCap).notNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(ChargesApiConstants.taxGroupIdParamName, element)) {
            final Long taxGroupId = this.fromApiJsonHelper.extractLongNamed(ChargesApiConstants.taxGroupIdParamName, element);
            baseDataValidator.reset().parameter(ChargesApiConstants.taxGroupIdParamName).value(taxGroupId).notNull().longGreaterThanZero();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charge");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists("name", element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed("name", element);
            baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists("currencyCode", element)) {
            final String currencyCode = this.fromApiJsonHelper.extractStringNamed("currencyCode", element);
            baseDataValidator.reset().parameter("currencyCode").value(currencyCode).notBlank().notExceedingLengthOf(3);
        }

        if (this.fromApiJsonHelper.parameterExists("amount", element)) {
            final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element.getAsJsonObject());
            baseDataValidator.reset().parameter("amount").value(amount).notNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists("minCap", element)) {
            final BigDecimal minCap = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minCap", element.getAsJsonObject());
            baseDataValidator.reset().parameter("minCap").value(minCap).notNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists("maxCap", element)) {
            final BigDecimal maxCap = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maxCap", element.getAsJsonObject());
            baseDataValidator.reset().parameter("maxCap").value(maxCap).notNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists("chargeAppliesTo", element)) {
            final Integer chargeAppliesTo = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargeAppliesTo", element);
            baseDataValidator.reset().parameter("chargeAppliesTo").value(chargeAppliesTo).notNull()
                    .isOneOfTheseValues(ChargeAppliesTo.validValues());
        }

        if (this.fromApiJsonHelper.parameterExists("chargeTimeType", element)) {

            final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("chargeTimeType", element);

            final Collection<Object> validLoanValues = Arrays.asList(ChargeTimeType.validLoanValues());
            final Collection<Object> validSavingsValues = Arrays.asList(ChargeTimeType.validSavingsValues());
            final Collection<Object> validClientValues = Arrays.asList(ChargeTimeType.validClientValues());
            final Collection<Object> validShareValues = Arrays.asList(ChargeTimeType.validShareValues()) ;
            final Collection<Object> allValidValues = new ArrayList<>(validLoanValues);
            allValidValues.addAll(validSavingsValues);
            allValidValues.addAll(validClientValues);
            allValidValues.addAll(validShareValues) ;
            baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType).notNull()
                    .isOneOfTheseValues(allValidValues.toArray(new Object[allValidValues.size()]));
        }

        if (this.fromApiJsonHelper.parameterExists("feeOnMonthDay", element)) {
            final MonthDay monthDay = this.fromApiJsonHelper.extractMonthDayNamed("feeOnMonthDay", element);
            baseDataValidator.reset().parameter("feeOnMonthDay").value(monthDay).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists("feeInterval", element)) {
            final Integer feeInterval = this.fromApiJsonHelper.extractIntegerNamed("feeInterval", element, Locale.getDefault());
            baseDataValidator.reset().parameter("feeInterval").value(feeInterval).integerGreaterThanZero();
        }
        
        if (this.fromApiJsonHelper.parameterExists("chargeCalculationType", element)) {
            final Integer chargeCalculationType = this.fromApiJsonHelper.extractIntegerNamed("chargeCalculationType", element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType).notNull().inMinMaxRange(1, 5);
        }
        
        if (this.fromApiJsonHelper.parameterExists("chargePaymentMode", element)) {
            final Integer chargePaymentMode = this.fromApiJsonHelper.extractIntegerNamed("chargePaymentMode", element, Locale.getDefault());
            baseDataValidator.reset().parameter("chargePaymentMode").value(chargePaymentMode).notNull().inMinMaxRange(0, 1);
        }

        if (this.fromApiJsonHelper.parameterExists("penalty", element)) {
            final Boolean penalty = this.fromApiJsonHelper.extractBooleanNamed("penalty", element);
            baseDataValidator.reset().parameter("penalty").value(penalty).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists("active", element)) {
            final Boolean active = this.fromApiJsonHelper.extractBooleanNamed("active", element);
            baseDataValidator.reset().parameter("active").value(active).notNull();
        }
        if (this.fromApiJsonHelper.parameterExists("minCap", element)) {
            final BigDecimal minCap = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minCap", element.getAsJsonObject());
            baseDataValidator.reset().parameter("minCap").value(minCap).notNull().positiveAmount();
        }
        if (this.fromApiJsonHelper.parameterExists("maxCap", element)) {
            final BigDecimal maxCap = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maxCap", element.getAsJsonObject());
            baseDataValidator.reset().parameter("maxCap").value(maxCap).notNull().positiveAmount();
        }
        if (this.fromApiJsonHelper.parameterExists("feeFrequency", element)) {
            final Integer feeFrequency = this.fromApiJsonHelper.extractIntegerNamed("feeFrequency", element, Locale.getDefault());
            baseDataValidator.reset().parameter("feeFrequency").value(feeFrequency).inMinMaxRange(0, 3);
        }

        if (this.fromApiJsonHelper.parameterExists(ChargesApiConstants.glAccountIdParamName, element)) {
            final Long glAccountId = this.fromApiJsonHelper.extractLongNamed(ChargesApiConstants.glAccountIdParamName, element);
            baseDataValidator.reset().parameter(ChargesApiConstants.glAccountIdParamName).value(glAccountId).notNull()
                    .longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(ChargesApiConstants.taxGroupIdParamName, element)) {
            final Long taxGroupId = this.fromApiJsonHelper.extractLongNamed(ChargesApiConstants.taxGroupIdParamName, element);
            baseDataValidator.reset().parameter(ChargesApiConstants.taxGroupIdParamName).value(taxGroupId).notNull().longGreaterThanZero();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateChargeTimeNCalculationType(Integer chargeTimeType, Integer ChargeCalculationType) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charge");
        performChargeTimeNCalculationTypeValidation(baseDataValidator, chargeTimeType, ChargeCalculationType);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void performChargeTimeNCalculationTypeValidation(DataValidatorBuilder baseDataValidator, final Integer chargeTimeType,
            final Integer chargeCalculationType) {
        if(chargeTimeType.equals(ChargeTimeType.SHAREACCOUNT_ACTIVATION.getValue())){
            baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType)
            .isOneOfTheseValues(ChargeCalculationType.validValuesForShareAccountActivation());
        }
        
        if (chargeTimeType.equals(ChargeTimeType.TRANCHE_DISBURSEMENT.getValue())) {
            baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType)
                    .isOneOfTheseValues(ChargeCalculationType.validValuesForTrancheDisbursement());
        } else {
            baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType)
                    .isNotOneOfTheseValues(ChargeCalculationType.PERCENT_OF_DISBURSEMENT_AMOUNT.getValue());
        }
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}