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

import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import org.apache.fineract.portfolio.charge.domain.ChargeAppliesTo;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;

/**
 * Charge rules shared by the create and update charge constraints.
 */
final class ChargeRulesChecker {

    static final String CHARGE_APPLIES_TO = "chargeAppliesTo";
    static final String CHARGE_TIME_TYPE = "chargeTimeType";
    static final String CHARGE_CALCULATION_TYPE = "chargeCalculationType";
    static final String CHARGE_PAYMENT_MODE = "chargePaymentMode";
    static final String FEE_ON_MONTH_DAY = "feeOnMonthDay";
    static final String FEE_INTERVAL = "feeInterval";
    static final String TAX_GROUP_ID = "taxGroupId";

    private static final String MSG_PREFIX = "{org.apache.fineract.portfolio.charge.";

    private ChargeRulesChecker() {}

    /**
     * Mirrors the legacy {@code performChargeTimeNCalculationTypeValidation}, which the legacy write service ran
     * against the charge state <i>after</i> the update had been applied. Callers therefore pass the effective
     * (post-update) values.
     */
    static boolean checkChargeTimeAndCalculationType(final Integer chargeTimeType, final Integer chargeCalculationType,
            final ConstraintValidatorContext context) {
        if (chargeTimeType == null || chargeCalculationType == null) {
            return true;
        }
        if (chargeTimeType.equals(ChargeTimeType.SHAREACCOUNT_ACTIVATION.getValue())) {
            return isOneOf(chargeCalculationType, ChargeCalculationType.validValuesForShareAccountActivation()) || violation(context,
                    MSG_PREFIX + "charge-calculation-type.invalid-for-share-account-activation}", CHARGE_CALCULATION_TYPE);
        }
        if (chargeTimeType.equals(ChargeTimeType.TRANCHE_DISBURSEMENT.getValue())) {
            return isOneOf(chargeCalculationType, ChargeCalculationType.validValuesForTrancheDisbursement()) || violation(context,
                    MSG_PREFIX + "charge-calculation-type.invalid-for-tranche-disbursement}", CHARGE_CALCULATION_TYPE);
        }
        return !chargeCalculationType.equals(ChargeCalculationType.PERCENT_OF_DISBURSEMENT_AMOUNT.getValue())
                || violation(context, MSG_PREFIX + "charge-calculation-type.percent-of-disbursement-not-allowed}", CHARGE_CALCULATION_TYPE);
    }

    /**
     * The charge time / calculation type combinations permitted for a given {@code chargeAppliesTo}.
     */
    static Object[] validChargeTimeValuesFor(final Integer chargeAppliesTo) {
        return switch (ChargeAppliesTo.fromInt(chargeAppliesTo)) {
            case LOAN -> ChargeTimeType.validLoanValues();
            case SAVINGS -> ChargeTimeType.validSavingsValues();
            case CLIENT -> ChargeTimeType.validClientValues();
            case SHARES -> ChargeTimeType.validShareValues();
            case WORKING_CAPITAL_LOAN -> ChargeTimeType.validWorkingCapitalLoanValues();
            case INVALID -> new Object[] {};
        };
    }

    static Object[] validChargeCalculationValuesFor(final Integer chargeAppliesTo, final Integer chargeTimeType) {
        return switch (ChargeAppliesTo.fromInt(chargeAppliesTo)) {
            case LOAN -> ChargeCalculationType.validValuesForLoan();
            case SAVINGS -> ChargeCalculationType.validValuesForSavings();
            case CLIENT -> ChargeCalculationType.validValuesForClients();
            case SHARES -> ChargeCalculationType.validValuesForShares();
            // the legacy validator only permitted a calculation type for working capital loan specified-due-date
            // charges
            case WORKING_CAPITAL_LOAN -> ChargeTimeType.SPECIFIED_DUE_DATE.getValue().equals(chargeTimeType)
                    ? ChargeCalculationType.validValuesForWorkingCapitalLoanSpecifiedDueDate()
                    : new Object[] {};
            case INVALID -> new Object[] {};
        };
    }

    static boolean isOneOf(final Object value, final Object[] validValues) {
        return value != null && Arrays.asList(validValues).contains(value);
    }

    /**
     * Records a constraint violation on {@code property} and always returns {@code false}, so callers can write
     * {@code condition || violation(...)}.
     */
    static boolean violation(final ConstraintValidatorContext context, final String template, final String property) {
        context.buildConstraintViolationWithTemplate(template).addPropertyNode(property).addConstraintViolation();
        return false;
    }
}
