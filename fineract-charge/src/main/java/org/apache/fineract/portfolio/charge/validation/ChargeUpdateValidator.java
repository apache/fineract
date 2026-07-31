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
import static org.apache.fineract.portfolio.charge.validation.ChargeRulesChecker.CHARGE_TIME_TYPE;
import static org.apache.fineract.portfolio.charge.validation.ChargeRulesChecker.FEE_INTERVAL;
import static org.apache.fineract.portfolio.charge.validation.ChargeRulesChecker.FEE_ON_MONTH_DAY;
import static org.apache.fineract.portfolio.charge.validation.ChargeRulesChecker.TAX_GROUP_ID;
import static org.apache.fineract.portfolio.charge.validation.ChargeRulesChecker.checkChargeTimeAndCalculationType;
import static org.apache.fineract.portfolio.charge.validation.ChargeRulesChecker.isOneOf;
import static org.apache.fineract.portfolio.charge.validation.ChargeRulesChecker.validChargeCalculationValuesFor;
import static org.apache.fineract.portfolio.charge.validation.ChargeRulesChecker.violation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.portfolio.charge.data.ChargeUpdateRequest;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeAppliesTo;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeRepository;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.springframework.stereotype.Component;

/**
 * Validates a charge update against the <i>merged</i> state — the values the charge will have once the (partial)
 * request has been applied — which is why it needs the persisted charge. The legacy write path achieved the same by
 * mutating the entity first and validating afterwards.
 */
@RequiredArgsConstructor
@Component
final class ChargeUpdateValidator implements ConstraintValidator<ValidChargeUpdate, ChargeUpdateRequest> {

    private static final String MSG_PREFIX = "{org.apache.fineract.portfolio.charge.";

    private final ChargeRepository chargeRepository;

    @Override
    public boolean isValid(final ChargeUpdateRequest request, final ConstraintValidatorContext context) {
        if (request == null || request.getId() == null) {
            // the resource-level @Valid pass runs before the path id is bound; the id-aware pass happens in the
            // write service
            return true;
        }
        final Charge charge = this.chargeRepository.findById(request.getId()).orElse(null);
        if (charge == null) {
            // a missing charge is reported as a not-found error by the write service
            return true;
        }
        context.disableDefaultConstraintViolation();
        boolean valid = true;

        if (request.getChargeAppliesTo() != null && ChargeAppliesTo.fromInt(request.getChargeAppliesTo()) == ChargeAppliesTo.INVALID) {
            valid = violation(context, MSG_PREFIX + "charge-applies-to.invalid}", CHARGE_APPLIES_TO);
        }

        final Integer effectiveChargeTimeType = request.getChargeTimeType() != null ? request.getChargeTimeType()
                : charge.getChargeTimeType();
        final Integer effectiveChargeCalculationType = request.getChargeCalculationType() != null ? request.getChargeCalculationType()
                : charge.getChargeCalculation();
        final Integer effectiveFeeInterval = request.getFeeInterval() != null ? request.getFeeInterval() : charge.getFeeInterval();
        final Integer effectiveFeeFrequency = request.getFeeFrequency() != null ? request.getFeeFrequency() : charge.getFeeFrequency();

        if (StringUtils.isNotBlank(request.getFeeOnMonthDay()) && request.feeOnMonthDayAsMonthDay() == null) {
            valid = violation(context, MSG_PREFIX + "fee-on-month-day.invalid}", FEE_ON_MONTH_DAY);
        }

        if (effectiveFeeFrequency != null && effectiveFeeInterval == null) {
            valid = violation(context, MSG_PREFIX + "fee-interval.not-null}", FEE_INTERVAL);
        }

        if (request.getChargeTimeType() != null && !isOneOf(request.getChargeTimeType(), allChargeTimeValues())) {
            valid = violation(context, MSG_PREFIX + "charge-time-type.invalid}", CHARGE_TIME_TYPE);
        }

        if (isChanged(request.getChargeTimeType(), charge.getChargeTimeType())) {
            valid &= checkChangedChargeTimeType(request, charge, effectiveChargeTimeType, effectiveChargeCalculationType,
                    effectiveFeeInterval, context);
        }

        if (isChanged(request.getChargeCalculationType(), charge.getChargeCalculation())) {
            valid &= checkChangedChargeCalculationType(charge, effectiveChargeTimeType, effectiveChargeCalculationType, context);
        }

        valid &= checkChargeTimeAndCalculationType(effectiveChargeTimeType, effectiveChargeCalculationType, context);

        if (isChanged(request.getTaxGroupId(), charge.getTaxGroup() == null ? null : charge.getTaxGroup().getId())
                && charge.getTaxGroup() != null) {
            valid = violation(context, MSG_PREFIX + "tax-group-id.modification-not-supported}", TAX_GROUP_ID);
        }

        return valid;
    }

    private boolean checkChangedChargeTimeType(final ChargeUpdateRequest request, final Charge charge,
            final Integer effectiveChargeTimeType, final Integer effectiveChargeCalculationType, final Integer effectiveFeeInterval,
            final ConstraintValidatorContext context) {
        final ChargeTimeType timeType = ChargeTimeType.fromInt(effectiveChargeTimeType);
        boolean valid = true;

        if (charge.isSavingsCharge()) {
            if (!timeType.isAllowedSavingsChargeTime()) {
                valid = violation(context, MSG_PREFIX + "charge-time-type.not-allowed-for-savings}", CHARGE_TIME_TYPE);
            }
            // when the charge time becomes monthly, feeOnMonthDay and feeInterval become mandatory
            if (timeType.isMonthlyFee()) {
                if (request.feeOnMonthDayAsMonthDay() == null) {
                    valid = violation(context, MSG_PREFIX + "fee-on-month-day.not-null}", FEE_ON_MONTH_DAY);
                }
                if (effectiveFeeInterval == null) {
                    valid = violation(context, MSG_PREFIX + "fee-interval.not-null}", FEE_INTERVAL);
                } else if (effectiveFeeInterval < 1 || effectiveFeeInterval > 12) {
                    valid = violation(context, MSG_PREFIX + "fee-interval.monthly-range}", FEE_INTERVAL);
                }
            }
        } else if (charge.isLoanCharge()) {
            if (!timeType.isAllowedLoanChargeTime()) {
                valid = violation(context, MSG_PREFIX + "charge-time-type.not-allowed-for-loan}", CHARGE_TIME_TYPE);
            }
        } else if (ChargeAppliesTo.WORKING_CAPITAL_LOAN.getValue().equals(charge.getChargeAppliesTo())) {
            if (!isOneOf(effectiveChargeTimeType, ChargeTimeType.validWorkingCapitalLoanValues())) {
                valid = violation(context, MSG_PREFIX + "charge-time-type.invalid}", CHARGE_TIME_TYPE);
            }
            if (!isOneOf(effectiveChargeCalculationType,
                    validChargeCalculationValuesFor(charge.getChargeAppliesTo(), effectiveChargeTimeType))) {
                valid = violation(context, MSG_PREFIX + "charge-calculation-type.invalid}", CHARGE_CALCULATION_TYPE);
            }
        } else if (charge.isClientCharge() && !timeType.isAllowedLoanChargeTime()) {
            // NOTE: the legacy update path checked the loan (not the client) charge times here; kept as-is
            valid = violation(context, MSG_PREFIX + "charge-time-type.not-allowed-for-client}", CHARGE_TIME_TYPE);
        }
        return valid;
    }

    private boolean checkChangedChargeCalculationType(final Charge charge, final Integer effectiveChargeTimeType,
            final Integer effectiveChargeCalculationType, final ConstraintValidatorContext context) {
        final ChargeCalculationType calculationType = ChargeCalculationType.fromInt(effectiveChargeCalculationType);
        final ChargeTimeType timeType = ChargeTimeType.fromInt(effectiveChargeTimeType);
        boolean valid = true;

        if (charge.isSavingsCharge()) {
            if (!calculationType.isAllowedSavingsChargeCalculationType()) {
                valid = violation(context, MSG_PREFIX + "charge-calculation-type.not-allowed-for-savings}", CHARGE_CALCULATION_TYPE);
            }
            if (!(timeType.isWithdrawalFee() || timeType.isSavingsNoActivityFee()) && calculationType.isPercentageOfAmount()) {
                valid = violation(context, MSG_PREFIX + "charge-calculation-type.percentage-only-for-withdrawal-or-no-activity}",
                        CHARGE_CALCULATION_TYPE);
            }
        } else if (charge.isClientCharge() && !calculationType.isAllowedClientChargeCalculationType()) {
            valid = violation(context, MSG_PREFIX + "charge-calculation-type.not-allowed-for-client}", CHARGE_CALCULATION_TYPE);
        }
        return valid;
    }

    private static boolean isChanged(final Object requestValue, final Object currentValue) {
        return requestValue != null && !Objects.equals(requestValue, currentValue);
    }

    private static Object[] allChargeTimeValues() {
        final Collection<Object> allValidValues = new ArrayList<>(Arrays.asList(ChargeTimeType.validLoanValues()));
        allValidValues.addAll(Arrays.asList(ChargeTimeType.validSavingsValues()));
        allValidValues.addAll(Arrays.asList(ChargeTimeType.validClientValues()));
        allValidValues.addAll(Arrays.asList(ChargeTimeType.validShareValues()));
        return allValidValues.toArray(new Object[0]);
    }
}
