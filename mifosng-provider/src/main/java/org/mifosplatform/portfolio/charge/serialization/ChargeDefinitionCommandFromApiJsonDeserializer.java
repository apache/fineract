/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.MonthDay;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.charge.domain.ChargeAppliesTo;
import org.mifosplatform.portfolio.charge.domain.ChargeTimeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class ChargeDefinitionCommandFromApiJsonDeserializer {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("name", "amount", "locale", "currencyCode",
            "currencyOptions", "chargeAppliesTo", "chargeTimeType", "chargeCalculationType", "chargeCalculationTypeOptions", "penalty",
            "active", "chargePaymentMode", "feeOnMonthDay", "feeInterval", "monthDayFormat"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public ChargeDefinitionCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charge");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String name = this.fromApiJsonHelper.extractStringNamed("name", element);
        baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(100);

        final String currencyCode = this.fromApiJsonHelper.extractStringNamed("currencyCode", element);
        baseDataValidator.reset().parameter("currencyCode").value(currencyCode).notBlank().notExceedingLengthOf(3);

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element.getAsJsonObject());
        baseDataValidator.reset().parameter("amount").value(amount).notNull().positiveAmount();

        final Integer chargeAppliesTo = this.fromApiJsonHelper.extractIntegerNamed("chargeAppliesTo", element, Locale.getDefault());
        baseDataValidator.reset().parameter("chargeAppliesTo").value(chargeAppliesTo).notNull()
                .inMinMaxRange(ChargeAppliesTo.minValue(), ChargeAppliesTo.maxValue());

        final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerNamed("chargeTimeType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType).notNull()
                .inMinMaxRange(ChargeTimeType.minValue(), ChargeTimeType.maxValue());

        final Integer chargeCalculationType = this.fromApiJsonHelper.extractIntegerNamed("chargeCalculationType", element,
                Locale.getDefault());
        baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType).notNull().inMinMaxRange(1, 4);

        final Integer chargePaymentMode = this.fromApiJsonHelper.extractIntegerNamed("chargePaymentMode", element, Locale.getDefault());
        baseDataValidator.reset().parameter("chargePaymentMode").value(chargePaymentMode).notNull().inMinMaxRange(0, 1);

        if (this.fromApiJsonHelper.parameterExists("penalty", element)) {
            final Boolean penalty = this.fromApiJsonHelper.extractBooleanNamed("penalty", element);
            baseDataValidator.reset().parameter("penalty").value(penalty).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists("active", element)) {
            final Boolean active = this.fromApiJsonHelper.extractBooleanNamed("active", element);
            baseDataValidator.reset().parameter("active").value(active).notNull();
        }
        
        boolean isMonthlyFee = ChargeTimeType.fromInt(chargeTimeType).isMonthlyFee();
        if(isMonthlyFee){
            final MonthDay monthDay = this.fromApiJsonHelper.extractMonthDayNamed("feeOnMonthDay", element); 
            baseDataValidator.reset().parameter("feeOnMonthDay").value(monthDay).notNull();
            
            final Integer feeInterval = this.fromApiJsonHelper.extractIntegerNamed("feeInterval", element, Locale.getDefault());
            baseDataValidator.reset().parameter("feeInterval").value(feeInterval).notNull().inMinMaxRange(1, 12);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
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

        if (this.fromApiJsonHelper.parameterExists("chargeAppliesTo", element)) {
            final Integer chargeAppliesTo = this.fromApiJsonHelper.extractIntegerNamed("chargeAppliesTo", element, Locale.getDefault());
            baseDataValidator.reset().parameter("chargeAppliesTo").value(chargeAppliesTo).notNull()
                    .inMinMaxRange(ChargeAppliesTo.minValue(), ChargeAppliesTo.maxValue());
        }

        if (this.fromApiJsonHelper.parameterExists("chargeTimeType", element)) {
            final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerNamed("chargeTimeType", element, Locale.getDefault());
            baseDataValidator.reset().parameter("chargeTimeType").value(chargeTimeType).notNull()
                    .inMinMaxRange(ChargeTimeType.minValue(), ChargeTimeType.maxValue());
        }
        
        if (this.fromApiJsonHelper.parameterExists("feeOnMonthDay", element)) {
            final MonthDay monthDay = this.fromApiJsonHelper.extractMonthDayNamed("feeOnMonthDay", element); 
            baseDataValidator.reset().parameter("feeOnMonthDay").value(monthDay).notNull();
        }
        
        if (this.fromApiJsonHelper.parameterExists("feeInterval", element)) {
            final Integer feeInterval = this.fromApiJsonHelper.extractIntegerNamed("feeInterval", element, Locale.getDefault());
            baseDataValidator.reset().parameter("feeInterval").value(feeInterval).notNull().inMinMaxRange(1, 12);
        }

        if (this.fromApiJsonHelper.parameterExists("chargeCalculationType", element)) {
            final Integer chargeCalculationType = this.fromApiJsonHelper.extractIntegerNamed("chargeCalculationType", element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter("chargeCalculationType").value(chargeCalculationType).notNull().inMinMaxRange(1, 4);
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

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}