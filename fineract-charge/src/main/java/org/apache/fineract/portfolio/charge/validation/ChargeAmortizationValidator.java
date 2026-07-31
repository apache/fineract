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
package org.apache.fineract.portfolio.charge.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.charge.api.ChargesApiConstants;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeAmortizationCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeAmortizationIncomeType;
import org.apache.fineract.portfolio.charge.domain.ChargeAmortizationPeriodType;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;

/**
 * Checks the amortization configuration of a charge definition (charge amortization FR-01 to FR-07).
 * <p>
 * The values are checked in two steps: {@link #validateValues} checks what the request sends before it is applied, and
 * {@link #validateConfiguration} checks the configuration the charge ends up with, because an update only sends the
 * fields that change. Both keep the error codes and messages of the legacy charge validation.
 */
public final class ChargeAmortizationValidator {

    private static final String RESOURCE = "charge";

    private ChargeAmortizationValidator() {}

    /**
     * Checks the amortization values of a create or update request: the enums must be known (ignoring case) and the
     * number of days must be positive.
     */
    public static void validateValues(final String periodType, final Integer days, final String calculationType, final String incomeType) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(RESOURCE);

        validateEnumValue(baseDataValidator, ChargesApiConstants.amortizationPeriodTypeParamName, periodType,
                ChargeAmortizationPeriodType.class);
        validateEnumValue(baseDataValidator, ChargesApiConstants.amortizationCalculationTypeParamName, calculationType,
                ChargeAmortizationCalculationType.class);
        validateEnumValue(baseDataValidator, ChargesApiConstants.amortizationIncomeTypeParamName, incomeType,
                ChargeAmortizationIncomeType.class);
        baseDataValidator.reset().parameter(ChargesApiConstants.amortizationDaysParamName).value(days).ignoreIfNull()
                .integerGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    /**
     * Checks that the amortization configuration of a created or updated charge is allowed.
     */
    public static void validateConfiguration(final Charge charge) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(RESOURCE);

        final ChargeAmortizationPeriodType periodType = charge.getAmortizationPeriodType();
        final Integer days = charge.getAmortizationDays();
        final ChargeAmortizationCalculationType calculationType = charge.getAmortizationCalculationType();

        if (!periodType.isAmortized()) {
            if (calculationType != null) {
                baseDataValidator.reset().parameter(ChargesApiConstants.amortizationCalculationTypeParamName).value(calculationType.name())
                        .failWithCodeAndMessage("supported.only.for.amortized.charge",
                                "Amortization calculation type can be set only when the amortization period type is not NONE");
            }
            if (charge.getAmortizationIncomeType() != null) {
                baseDataValidator.reset().parameter(ChargesApiConstants.amortizationIncomeTypeParamName)
                        .value(charge.getAmortizationIncomeType().name()).failWithCodeAndMessage("supported.only.for.amortized.charge",
                                "Amortization income type can be set only when the amortization period type is not NONE");
            }
        } else {
            if (!charge.isLoanCharge()) {
                baseDataValidator.reset().parameter(ChargesApiConstants.amortizationPeriodTypeParamName).value(periodType.name())
                        .failWithCodeAndMessage("supported.only.for.loan.charge", "Only loan charges can be amortized");
            } else {
                if (charge.isPenalty()) {
                    baseDataValidator.reset().parameter(ChargesApiConstants.amortizationPeriodTypeParamName).value(periodType.name())
                            .failWithCodeAndMessage("not.supported.for.penalty", "Penalties cannot be amortized");
                }
                if (!ChargeTimeType.fromInt(charge.getChargeTimeType()).isAllowedChargeAmortizationTime()) {
                    baseDataValidator.reset().parameter(ChargesApiConstants.amortizationPeriodTypeParamName).value(periodType.name())
                            .failWithCodeAndMessage("not.supported.for.charge.time.type",
                                    "Only Disbursement, Tranche Disbursement and Specified due date charges can be amortized");
                }
            }
            if (calculationType == ChargeAmortizationCalculationType.EFFECTIVE_RATE
                    && periodType != ChargeAmortizationPeriodType.FULL_LOAN_TERM) {
                baseDataValidator.reset().parameter(ChargesApiConstants.amortizationCalculationTypeParamName).value(calculationType.name())
                        .failWithCodeAndMessage("effective.rate.supported.only.for.full.loan.term",
                                "Effective rate amortization is supported only with the FULL_LOAN_TERM amortization period type");
            }
        }

        if (periodType == ChargeAmortizationPeriodType.NUMBER_OF_DAYS) {
            baseDataValidator.reset().parameter(ChargesApiConstants.amortizationDaysParamName).value(days).notNull()
                    .integerGreaterThanZero();
        } else if (days != null) {
            baseDataValidator.reset().parameter(ChargesApiConstants.amortizationDaysParamName).value(days).failWithCodeAndMessage(
                    "supported.only.for.number.of.days.period.type",
                    "Amortization days can be set only when the amortization period type is NUMBER_OF_DAYS");
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    /**
     * Reads an amortization enum the way {@link #validateValues} accepts it, that is ignoring case. A blank value reads
     * as {@code null}.
     */
    public static <T extends Enum<T>> T enumValue(final String value, final Class<T> enumType) {
        return StringUtils.isBlank(value) ? null : Enum.valueOf(enumType, value.trim().toUpperCase(Locale.ROOT));
    }

    private static void validateEnumValue(final DataValidatorBuilder baseDataValidator, final String parameterName, final String value,
            final Class<? extends Enum<?>> enumType) {
        baseDataValidator.reset().parameter(parameterName).value(value).ignoreIfNull().isOneOfEnumValues(enumType);
    }

    private static void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
