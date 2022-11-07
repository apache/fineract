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

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class ChargeDefinitionCommandFromApiJsonDeserializer {

    public static final String NAME = "name";
    public static final String AMOUNT = "amount";
    public static final String LOCALE = "locale";
    public static final String CURRENCY_CODE = "currencyCode";
    public static final String PENALTY = "penalty";
    public static final String CHARGE_CALCULATION_TYPE_OPTIONS = "chargeCalculationTypeOptions";
    public static final String CHARGE_CALCULATION_TYPE = "chargeCalculationType";
    public static final String CHARGE_TIME_TYPE = "chargeTimeType";
    public static final String CHARGE_APPLIES_TO = "chargeAppliesTo";
    public static final String CURRENCY_OPTIONS = "currencyOptions";
    public static final String ACTIVE = "active";
    public static final String CHARGE_PAYMENT_MODE = "chargePaymentMode";
    public static final String FEE_ON_MONTH_DAY = "feeOnMonthDay";
    public static final String FEE_INTERVAL = "feeInterval";
    public static final String MONTH_DAY_FORMAT = "monthDayFormat";
    public static final String MIN_CAP = "minCap";
    public static final String MAX_CAP = "maxCap";
    public static final String FEE_FREQUENCY = "feeFrequency";
    public static final String ENABLE_FREE_WITHDRAWAL_CHARGE = "enableFreeWithdrawalCharge";
    public static final String FREE_WITHDRAWAL_FREQUENCY = "freeWithdrawalFrequency";
    public static final String RESTART_COUNT_FREQUENCY = "restartCountFrequency";
    public static final String COUNT_FREQUENCY_TYPE = "countFrequencyType";
    public static final String ENABLE_PAYMENT_TYPE = "enablePaymentType";
    public static final String PAYMENT_TYPE_ID = "paymentTypeId";
    public static final String CHARGE = "charge";
    /**
     * The parameters supported for this command.
     */
    private static final Set<String> SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList(NAME, AMOUNT, LOCALE, CURRENCY_CODE,
            CURRENCY_OPTIONS, CHARGE_APPLIES_TO, CHARGE_TIME_TYPE, CHARGE_CALCULATION_TYPE, CHARGE_CALCULATION_TYPE_OPTIONS, PENALTY,
            ACTIVE, CHARGE_PAYMENT_MODE, FEE_ON_MONTH_DAY, FEE_INTERVAL, MONTH_DAY_FORMAT, MIN_CAP, MAX_CAP, FEE_FREQUENCY,
            ENABLE_FREE_WITHDRAWAL_CHARGE, FREE_WITHDRAWAL_FREQUENCY, RESTART_COUNT_FREQUENCY, COUNT_FREQUENCY_TYPE, PAYMENT_TYPE_ID,
            ENABLE_PAYMENT_TYPE, ChargesApiConstants.glAccountIdParamName, ChargesApiConstants.taxGroupIdParamName));
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public ChargeDefinitionCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(CHARGE);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Integer chargeAppliesTo = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CHARGE_APPLIES_TO, element);
        baseDataValidator.reset().parameter(CHARGE_APPLIES_TO).value(chargeAppliesTo).notNull();
        if (chargeAppliesTo != null) {
            baseDataValidator.reset().parameter(CHARGE_APPLIES_TO).value(chargeAppliesTo).isOneOfTheseValues(ChargeAppliesTo.validValues());
        }

        final Integer chargeCalculationType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CHARGE_CALCULATION_TYPE, element);
        baseDataValidator.reset().parameter(CHARGE_CALCULATION_TYPE).value(chargeCalculationType).notNull();

        final Integer feeInterval = this.fromApiJsonHelper.extractIntegerNamed(FEE_INTERVAL, element, Locale.getDefault());
        baseDataValidator.reset().parameter(FEE_INTERVAL).value(feeInterval).integerGreaterThanZero();

        final Integer feeFrequency = this.fromApiJsonHelper.extractIntegerNamed(FEE_FREQUENCY, element, Locale.getDefault());
        baseDataValidator.reset().parameter(FEE_FREQUENCY).value(feeFrequency).inMinMaxRange(0, 3);

        if (this.fromApiJsonHelper.parameterExists(ENABLE_FREE_WITHDRAWAL_CHARGE, element)) {

            final Boolean enableFreeWithdrawalCharge = this.fromApiJsonHelper.extractBooleanNamed(ENABLE_FREE_WITHDRAWAL_CHARGE, element);
            baseDataValidator.reset().parameter(ENABLE_FREE_WITHDRAWAL_CHARGE).value(enableFreeWithdrawalCharge).notNull();

            if (enableFreeWithdrawalCharge) {

                final Integer freeWithdrawalFrequency = this.fromApiJsonHelper.extractIntegerNamed(FREE_WITHDRAWAL_FREQUENCY, element,
                        Locale.getDefault());
                baseDataValidator.reset().parameter(FREE_WITHDRAWAL_FREQUENCY).value(freeWithdrawalFrequency).integerGreaterThanZero();

                final Integer restartCountFrequency = this.fromApiJsonHelper.extractIntegerNamed(RESTART_COUNT_FREQUENCY, element,
                        Locale.getDefault());
                baseDataValidator.reset().parameter(RESTART_COUNT_FREQUENCY).value(restartCountFrequency).integerGreaterThanZero();

                final Integer countFrequencyType = this.fromApiJsonHelper.extractIntegerNamed(COUNT_FREQUENCY_TYPE, element,
                        Locale.getDefault());
                baseDataValidator.reset().parameter(COUNT_FREQUENCY_TYPE).value(countFrequencyType);

            }
        }

        if (this.fromApiJsonHelper.parameterExists(ENABLE_PAYMENT_TYPE, element)) {

            final boolean enablePaymentType = this.fromApiJsonHelper.extractBooleanNamed(ENABLE_PAYMENT_TYPE, element);
            baseDataValidator.reset().parameter(ENABLE_PAYMENT_TYPE).value(enablePaymentType).notNull();

            if (enablePaymentType) {
                final Integer paymentTypeId = this.fromApiJsonHelper.extractIntegerNamed(PAYMENT_TYPE_ID, element, Locale.getDefault());
                baseDataValidator.reset().parameter(PAYMENT_TYPE_ID).value(paymentTypeId).integerGreaterThanZero();
            }
        }

        if (feeFrequency != null) {
            baseDataValidator.reset().parameter(FEE_INTERVAL).value(feeInterval).notNull();
        }

        final ChargeAppliesTo appliesTo = ChargeAppliesTo.fromInt(chargeAppliesTo);
        if (appliesTo.isLoanCharge()) {
            // loan applicable validation
            final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CHARGE_TIME_TYPE, element);
            baseDataValidator.reset().parameter(CHARGE_TIME_TYPE).value(chargeTimeType).notNull();
            if (chargeTimeType != null) {
                baseDataValidator.reset().parameter(CHARGE_TIME_TYPE).value(chargeTimeType)
                        .isOneOfTheseValues(ChargeTimeType.validLoanValues());
            }

            final Integer chargePaymentMode = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CHARGE_PAYMENT_MODE, element);
            baseDataValidator.reset().parameter(CHARGE_PAYMENT_MODE).value(chargePaymentMode).notNull()
                    .isOneOfTheseValues(ChargePaymentMode.validValues());
            if (chargePaymentMode != null) {
                baseDataValidator.reset().parameter(CHARGE_PAYMENT_MODE).value(chargePaymentMode)
                        .isOneOfTheseValues(ChargePaymentMode.validValues());
            }

            if (chargeCalculationType != null) {
                baseDataValidator.reset().parameter(CHARGE_CALCULATION_TYPE).value(chargeCalculationType)
                        .isOneOfTheseValues(ChargeCalculationType.validValuesForLoan());
            }

            if (chargeTimeType != null && chargeCalculationType != null) {
                performChargeTimeNCalculationTypeValidation(baseDataValidator, chargeTimeType, chargeCalculationType);
            }

        } else if (appliesTo.isSavingsCharge()) {
            // savings applicable validation
            final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CHARGE_TIME_TYPE, element);
            baseDataValidator.reset().parameter(CHARGE_TIME_TYPE).value(chargeTimeType).notNull();
            if (chargeTimeType != null) {
                baseDataValidator.reset().parameter(CHARGE_TIME_TYPE).value(chargeTimeType)
                        .isOneOfTheseValues(ChargeTimeType.validSavingsValues());
            }

            final ChargeTimeType ctt = ChargeTimeType.fromInt(chargeTimeType);

            if (ctt.isWeeklyFee()) {
                final String monthDay = this.fromApiJsonHelper.extractStringNamed(FEE_ON_MONTH_DAY, element);
                baseDataValidator.reset().parameter(FEE_ON_MONTH_DAY).value(monthDay).mustBeBlankWhenParameterProvidedIs(CHARGE_TIME_TYPE,
                        chargeTimeType);
            }

            if (ctt.isMonthlyFee()) {
                final MonthDay monthDay = this.fromApiJsonHelper.extractMonthDayNamed(FEE_ON_MONTH_DAY, element);
                baseDataValidator.reset().parameter(FEE_ON_MONTH_DAY).value(monthDay).notNull();

                baseDataValidator.reset().parameter(FEE_INTERVAL).value(feeInterval).notNull().inMinMaxRange(1, 12);
            }

            if (ctt.isAnnualFee()) {
                final MonthDay monthDay = this.fromApiJsonHelper.extractMonthDayNamed(FEE_ON_MONTH_DAY, element);
                baseDataValidator.reset().parameter(FEE_ON_MONTH_DAY).value(monthDay).notNull();
            }

            if (chargeCalculationType != null) {
                baseDataValidator.reset().parameter(CHARGE_CALCULATION_TYPE).value(chargeCalculationType)
                        .isOneOfTheseValues(ChargeCalculationType.validValuesForSavings());
            }

        } else if (appliesTo.isClientCharge()) {
            // client applicable validation
            final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CHARGE_TIME_TYPE, element);
            baseDataValidator.reset().parameter(CHARGE_TIME_TYPE).value(chargeTimeType).notNull();
            if (chargeTimeType != null) {
                baseDataValidator.reset().parameter(CHARGE_TIME_TYPE).value(chargeTimeType)
                        .isOneOfTheseValues(ChargeTimeType.validClientValues());
            }

            if (chargeCalculationType != null) {
                baseDataValidator.reset().parameter(CHARGE_CALCULATION_TYPE).value(chargeCalculationType)
                        .isOneOfTheseValues(ChargeCalculationType.validValuesForClients());
            }

            // GL Account can be linked to clients
            if (this.fromApiJsonHelper.parameterExists(ChargesApiConstants.glAccountIdParamName, element)) {
                final Long glAccountId = this.fromApiJsonHelper.extractLongNamed(ChargesApiConstants.glAccountIdParamName, element);
                baseDataValidator.reset().parameter(ChargesApiConstants.glAccountIdParamName).value(glAccountId).notNull()
                        .longGreaterThanZero();
            }

        } else if (appliesTo.isSharesCharge()) {
            final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CHARGE_TIME_TYPE, element);
            baseDataValidator.reset().parameter(CHARGE_TIME_TYPE).value(chargeTimeType).notNull();
            if (chargeTimeType != null) {
                baseDataValidator.reset().parameter(CHARGE_TIME_TYPE).value(chargeTimeType)
                        .isOneOfTheseValues(ChargeTimeType.validShareValues());
            }

            if (chargeCalculationType != null) {
                baseDataValidator.reset().parameter(CHARGE_CALCULATION_TYPE).value(chargeCalculationType)
                        .isOneOfTheseValues(ChargeCalculationType.validValuesForShares());
            }

            if (chargeTimeType != null && chargeTimeType.equals(ChargeTimeType.SHAREACCOUNT_ACTIVATION.getValue())
                    && chargeCalculationType != null) {
                baseDataValidator.reset().parameter(CHARGE_CALCULATION_TYPE).value(chargeCalculationType)
                        .isOneOfTheseValues(ChargeCalculationType.validValuesForShareAccountActivation());
            }
        }

        final String name = this.fromApiJsonHelper.extractStringNamed(NAME, element);
        baseDataValidator.reset().parameter(NAME).value(name).notBlank().notExceedingLengthOf(100);

        final String currencyCode = this.fromApiJsonHelper.extractStringNamed(CURRENCY_CODE, element);
        baseDataValidator.reset().parameter(CURRENCY_CODE).value(currencyCode).notBlank().notExceedingLengthOf(3);

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(AMOUNT, element.getAsJsonObject());
        baseDataValidator.reset().parameter(AMOUNT).value(amount).notNull().positiveAmount();

        if (this.fromApiJsonHelper.parameterExists(PENALTY, element)) {
            final Boolean penalty = this.fromApiJsonHelper.extractBooleanNamed(PENALTY, element);
            baseDataValidator.reset().parameter(PENALTY).value(penalty).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(ACTIVE, element)) {
            final Boolean active = this.fromApiJsonHelper.extractBooleanNamed(ACTIVE, element);
            baseDataValidator.reset().parameter(ACTIVE).value(active).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(MIN_CAP, element)) {
            final BigDecimal minCap = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(MIN_CAP, element.getAsJsonObject());
            baseDataValidator.reset().parameter(MIN_CAP).value(minCap).notNull().positiveAmount();
        }
        if (this.fromApiJsonHelper.parameterExists(MAX_CAP, element)) {
            final BigDecimal maxCap = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(MAX_CAP, element.getAsJsonObject());
            baseDataValidator.reset().parameter(MAX_CAP).value(maxCap).notNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(ChargesApiConstants.taxGroupIdParamName, element)) {
            final Long taxGroupId = this.fromApiJsonHelper.extractLongNamed(ChargesApiConstants.taxGroupIdParamName, element);
            baseDataValidator.reset().parameter(ChargesApiConstants.taxGroupIdParamName).value(taxGroupId).notNull().longGreaterThanZero();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(CHARGE);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(NAME, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(NAME, element);
            baseDataValidator.reset().parameter(NAME).value(name).notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(CURRENCY_CODE, element)) {
            final String currencyCode = this.fromApiJsonHelper.extractStringNamed(CURRENCY_CODE, element);
            baseDataValidator.reset().parameter(CURRENCY_CODE).value(currencyCode).notBlank().notExceedingLengthOf(3);
        }

        if (this.fromApiJsonHelper.parameterExists(AMOUNT, element)) {
            final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(AMOUNT, element.getAsJsonObject());
            baseDataValidator.reset().parameter(AMOUNT).value(amount).notNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(MIN_CAP, element)) {
            final BigDecimal minCap = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(MIN_CAP, element.getAsJsonObject());
            baseDataValidator.reset().parameter(MIN_CAP).value(minCap).notNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(MAX_CAP, element)) {
            final BigDecimal maxCap = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(MAX_CAP, element.getAsJsonObject());
            baseDataValidator.reset().parameter(MAX_CAP).value(maxCap).notNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(CHARGE_APPLIES_TO, element)) {
            final Integer chargeAppliesTo = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CHARGE_APPLIES_TO, element);
            baseDataValidator.reset().parameter(CHARGE_APPLIES_TO).value(chargeAppliesTo).notNull()
                    .isOneOfTheseValues(ChargeAppliesTo.validValues());
        }

        Boolean enableFreeWithdrawalCharge = false;
        if (this.fromApiJsonHelper.parameterExists(ENABLE_FREE_WITHDRAWAL_CHARGE, element)) {
            enableFreeWithdrawalCharge = this.fromApiJsonHelper.extractBooleanNamed(ENABLE_FREE_WITHDRAWAL_CHARGE, element);
            baseDataValidator.reset().parameter(ENABLE_FREE_WITHDRAWAL_CHARGE).value(enableFreeWithdrawalCharge).notNull();

            if (enableFreeWithdrawalCharge) {

                final Integer freeWithdrawalFrequency = this.fromApiJsonHelper.extractIntegerNamed(FREE_WITHDRAWAL_FREQUENCY, element,
                        Locale.getDefault());
                baseDataValidator.reset().parameter(FREE_WITHDRAWAL_FREQUENCY).value(freeWithdrawalFrequency).integerGreaterThanZero();

                final Integer restartCountFrequency = this.fromApiJsonHelper.extractIntegerNamed(RESTART_COUNT_FREQUENCY, element,
                        Locale.getDefault());
                baseDataValidator.reset().parameter(RESTART_COUNT_FREQUENCY).value(restartCountFrequency).integerGreaterThanZero();

                final Integer countFrequencyType = this.fromApiJsonHelper.extractIntegerNamed(COUNT_FREQUENCY_TYPE, element,
                        Locale.getDefault());
                baseDataValidator.reset().parameter(COUNT_FREQUENCY_TYPE).value(countFrequencyType);
            }

            Boolean enablePaymentType = false;
            if (this.fromApiJsonHelper.parameterExists(ENABLE_PAYMENT_TYPE, element)) {
                enablePaymentType = this.fromApiJsonHelper.extractBooleanNamed(ENABLE_PAYMENT_TYPE, element);
                baseDataValidator.reset().parameter(ENABLE_PAYMENT_TYPE).value(enablePaymentType).notNull();

                if (enablePaymentType) {
                    final Integer paymentTypeId = this.fromApiJsonHelper.extractIntegerNamed(PAYMENT_TYPE_ID, element, Locale.getDefault());
                    baseDataValidator.reset().parameter(PAYMENT_TYPE_ID).value(paymentTypeId).integerGreaterThanZero();
                }
            }
        }

        if (this.fromApiJsonHelper.parameterExists(CHARGE_APPLIES_TO, element)) {
            final Integer chargeAppliesTo = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CHARGE_APPLIES_TO, element);
            baseDataValidator.reset().parameter(CHARGE_APPLIES_TO).value(chargeAppliesTo).notNull()
                    .isOneOfTheseValues(ChargeAppliesTo.validValues());
        }

        if (this.fromApiJsonHelper.parameterExists(CHARGE_APPLIES_TO, element)) {
            final Integer chargeAppliesTo = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CHARGE_APPLIES_TO, element);
            baseDataValidator.reset().parameter(CHARGE_APPLIES_TO).value(chargeAppliesTo).notNull()
                    .isOneOfTheseValues(ChargeAppliesTo.validValues());
        }

        if (this.fromApiJsonHelper.parameterExists(CHARGE_TIME_TYPE, element)) {

            final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CHARGE_TIME_TYPE, element);

            final Collection<Object> validLoanValues = Arrays.asList(ChargeTimeType.validLoanValues());
            final Collection<Object> validSavingsValues = Arrays.asList(ChargeTimeType.validSavingsValues());
            final Collection<Object> validClientValues = Arrays.asList(ChargeTimeType.validClientValues());
            final Collection<Object> validShareValues = Arrays.asList(ChargeTimeType.validShareValues());
            final Collection<Object> allValidValues = new ArrayList<>(validLoanValues);
            allValidValues.addAll(validSavingsValues);
            allValidValues.addAll(validClientValues);
            allValidValues.addAll(validShareValues);
            baseDataValidator.reset().parameter(CHARGE_TIME_TYPE).value(chargeTimeType).notNull()
                    .isOneOfTheseValues(allValidValues.toArray(new Object[allValidValues.size()]));
        }

        if (this.fromApiJsonHelper.parameterExists(FEE_ON_MONTH_DAY, element)) {
            final MonthDay monthDay = this.fromApiJsonHelper.extractMonthDayNamed(FEE_ON_MONTH_DAY, element);
            baseDataValidator.reset().parameter(FEE_ON_MONTH_DAY).value(monthDay).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(FEE_INTERVAL, element)) {
            final Integer feeInterval = this.fromApiJsonHelper.extractIntegerNamed(FEE_INTERVAL, element, Locale.getDefault());
            baseDataValidator.reset().parameter(FEE_INTERVAL).value(feeInterval).integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(CHARGE_CALCULATION_TYPE, element)) {
            final Integer chargeCalculationType = this.fromApiJsonHelper.extractIntegerNamed(CHARGE_CALCULATION_TYPE, element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter(CHARGE_CALCULATION_TYPE).value(chargeCalculationType).notNull().inMinMaxRange(1, 5);
        }

        if (this.fromApiJsonHelper.parameterExists(CHARGE_PAYMENT_MODE, element)) {
            final Integer chargePaymentMode = this.fromApiJsonHelper.extractIntegerNamed(CHARGE_PAYMENT_MODE, element, Locale.getDefault());
            baseDataValidator.reset().parameter(CHARGE_PAYMENT_MODE).value(chargePaymentMode).notNull().inMinMaxRange(0, 1);
        }

        if (this.fromApiJsonHelper.parameterExists(PENALTY, element)) {
            final Boolean penalty = this.fromApiJsonHelper.extractBooleanNamed(PENALTY, element);
            baseDataValidator.reset().parameter(PENALTY).value(penalty).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(ACTIVE, element)) {
            final Boolean active = this.fromApiJsonHelper.extractBooleanNamed(ACTIVE, element);
            baseDataValidator.reset().parameter(ACTIVE).value(active).notNull();
        }
        if (this.fromApiJsonHelper.parameterExists(MIN_CAP, element)) {
            final BigDecimal minCap = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(MIN_CAP, element.getAsJsonObject());
            baseDataValidator.reset().parameter(MIN_CAP).value(minCap).notNull().positiveAmount();
        }
        if (this.fromApiJsonHelper.parameterExists(MAX_CAP, element)) {
            final BigDecimal maxCap = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(MAX_CAP, element.getAsJsonObject());
            baseDataValidator.reset().parameter(MAX_CAP).value(maxCap).notNull().positiveAmount();
        }
        if (this.fromApiJsonHelper.parameterExists(FEE_FREQUENCY, element)) {
            final Integer feeFrequency = this.fromApiJsonHelper.extractIntegerNamed(FEE_FREQUENCY, element, Locale.getDefault());
            baseDataValidator.reset().parameter(FEE_FREQUENCY).value(feeFrequency).inMinMaxRange(0, 3);
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

    public void validateChargeTimeNCalculationType(Integer chargeTimeType, Integer chargeCalculationType) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(CHARGE);
        performChargeTimeNCalculationTypeValidation(baseDataValidator, chargeTimeType, chargeCalculationType);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void performChargeTimeNCalculationTypeValidation(DataValidatorBuilder baseDataValidator, final Integer chargeTimeType,
            final Integer chargeCalculationType) {
        if (chargeTimeType.equals(ChargeTimeType.SHAREACCOUNT_ACTIVATION.getValue())) {
            baseDataValidator.reset().parameter(CHARGE_CALCULATION_TYPE).value(chargeCalculationType)
                    .isOneOfTheseValues(ChargeCalculationType.validValuesForShareAccountActivation());
        }

        if (chargeTimeType.equals(ChargeTimeType.TRANCHE_DISBURSEMENT.getValue())) {
            baseDataValidator.reset().parameter(CHARGE_CALCULATION_TYPE).value(chargeCalculationType)
                    .isOneOfTheseValues(ChargeCalculationType.validValuesForTrancheDisbursement());
        } else {
            baseDataValidator.reset().parameter(CHARGE_CALCULATION_TYPE).value(chargeCalculationType)
                    .isNotOneOfTheseValues(ChargeCalculationType.PERCENT_OF_DISBURSEMENT_AMOUNT.getValue());
        }
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
