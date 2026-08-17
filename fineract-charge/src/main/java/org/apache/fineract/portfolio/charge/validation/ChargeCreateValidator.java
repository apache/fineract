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

import static org.apache.fineract.portfolio.charge.validation.ChargeRulesChecker.CHARGE_APPLIES_TO;
import static org.apache.fineract.portfolio.charge.validation.ChargeRulesChecker.CHARGE_CALCULATION_TYPE;
import static org.apache.fineract.portfolio.charge.validation.ChargeRulesChecker.CHARGE_PAYMENT_MODE;
import static org.apache.fineract.portfolio.charge.validation.ChargeRulesChecker.CHARGE_TIME_TYPE;
import static org.apache.fineract.portfolio.charge.validation.ChargeRulesChecker.FEE_INTERVAL;
import static org.apache.fineract.portfolio.charge.validation.ChargeRulesChecker.FEE_ON_MONTH_DAY;
import static org.apache.fineract.portfolio.charge.validation.ChargeRulesChecker.checkChargeTimeAndCalculationType;
import static org.apache.fineract.portfolio.charge.validation.ChargeRulesChecker.isOneOf;
import static org.apache.fineract.portfolio.charge.validation.ChargeRulesChecker.validChargeCalculationValuesFor;
import static org.apache.fineract.portfolio.charge.validation.ChargeRulesChecker.validChargeTimeValuesFor;
import static org.apache.fineract.portfolio.charge.validation.ChargeRulesChecker.violation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.portfolio.charge.data.ChargeCreateRequest;
import org.apache.fineract.portfolio.charge.domain.ChargeAppliesTo;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.springframework.stereotype.Component;

@Component
final class ChargeCreateValidator implements ConstraintValidator<ValidChargeCreate, ChargeCreateRequest> {

    private static final String MSG_PREFIX = "{org.apache.fineract.portfolio.charge.";

    @Override
    public boolean isValid(final ChargeCreateRequest request, final ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        boolean valid = true;

        final Integer chargeAppliesTo = request.getChargeAppliesTo();
        final ChargeAppliesTo appliesTo = ChargeAppliesTo.fromInt(chargeAppliesTo);
        if (chargeAppliesTo != null && appliesTo == ChargeAppliesTo.INVALID) {
            // an unknown chargeAppliesTo makes every dependent rule below meaningless, so report it and stop
            return violation(context, MSG_PREFIX + "charge-applies-to.invalid}", CHARGE_APPLIES_TO);
        }

        valid &= checkFeeFrequency(request, context);
        valid &= checkFeeOnMonthDay(request, context);

        if (appliesTo == ChargeAppliesTo.INVALID) {
            return valid;
        }

        final Integer chargeTimeType = request.getChargeTimeType();
        if (chargeTimeType == null) {
            valid = violation(context, MSG_PREFIX + "charge-time-type.not-null}", CHARGE_TIME_TYPE);
        } else if (!isOneOf(chargeTimeType, validChargeTimeValuesFor(chargeAppliesTo))) {
            valid = violation(context, MSG_PREFIX + "charge-time-type.invalid}", CHARGE_TIME_TYPE);
        }

        final Integer chargeCalculationType = request.getChargeCalculationType();
        if (chargeCalculationType != null
                && !isOneOf(chargeCalculationType, validChargeCalculationValuesFor(chargeAppliesTo, chargeTimeType))) {
            valid = violation(context, MSG_PREFIX + "charge-calculation-type.invalid}", CHARGE_CALCULATION_TYPE);
        }

        if (appliesTo.isLoanCharge()) {
            valid &= checkLoanCharge(request, context);
        } else if (appliesTo.isSavingsCharge()) {
            valid &= checkSavingsCharge(request, chargeTimeType, context);
        }

        return valid;
    }

    private boolean checkLoanCharge(final ChargeCreateRequest request, final ConstraintValidatorContext context) {
        boolean valid = true;
        final Integer chargePaymentMode = request.getChargePaymentMode();
        if (chargePaymentMode == null) {
            valid = violation(context, MSG_PREFIX + "charge-payment-mode.not-null}", CHARGE_PAYMENT_MODE);
        } else if (!isOneOf(chargePaymentMode, ChargePaymentMode.validValues())) {
            valid = violation(context, MSG_PREFIX + "charge-payment-mode.invalid}", CHARGE_PAYMENT_MODE);
        }
        valid &= checkChargeTimeAndCalculationType(request.getChargeTimeType(), request.getChargeCalculationType(), context);
        return valid;
    }

    private boolean checkSavingsCharge(final ChargeCreateRequest request, final Integer chargeTimeType,
            final ConstraintValidatorContext context) {
        final ChargeTimeType timeType = ChargeTimeType.fromInt(chargeTimeType);
        boolean valid = true;

        if (timeType.isWeeklyFee() && StringUtils.isNotBlank(request.getFeeOnMonthDay())) {
            valid = violation(context, MSG_PREFIX + "fee-on-month-day.not-supported}", FEE_ON_MONTH_DAY);
        }

        if (timeType.isMonthlyFee() || timeType.isAnnualFee()) {
            if (request.feeOnMonthDayAsMonthDay() == null) {
                valid = violation(context, MSG_PREFIX + "fee-on-month-day.not-null}", FEE_ON_MONTH_DAY);
            }
            if (timeType.isMonthlyFee()) {
                final Integer feeInterval = request.getFeeInterval();
                if (feeInterval == null) {
                    valid = violation(context, MSG_PREFIX + "fee-interval.not-null}", FEE_INTERVAL);
                } else if (feeInterval < 1 || feeInterval > 12) {
                    valid = violation(context, MSG_PREFIX + "fee-interval.monthly-range}", FEE_INTERVAL);
                }
            }
        }
        return valid;
    }

    private boolean checkFeeFrequency(final ChargeCreateRequest request, final ConstraintValidatorContext context) {
        // a fee frequency is only meaningful together with the interval it repeats over
        return request.getFeeFrequency() == null || request.getFeeInterval() != null
                || violation(context, MSG_PREFIX + "fee-interval.not-null}", FEE_INTERVAL);
    }

    private boolean checkFeeOnMonthDay(final ChargeCreateRequest request, final ConstraintValidatorContext context) {
        return StringUtils.isBlank(request.getFeeOnMonthDay()) || request.feeOnMonthDayAsMonthDay() != null
                || violation(context, MSG_PREFIX + "fee-on-month-day.invalid}", FEE_ON_MONTH_DAY);
    }
}
