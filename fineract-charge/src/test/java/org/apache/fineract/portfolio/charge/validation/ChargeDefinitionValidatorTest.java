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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.charge.domain.ChargeAppliesTo;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.exception.ChargeDueAtDisbursementCannotBePenaltyException;
import org.apache.fineract.portfolio.charge.exception.ChargeMustBePenaltyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests {@link ChargeDefinitionValidator}. The error codes, messages and parameters are the ones the {@code Charge}
 * constructor used to report.
 */
class ChargeDefinitionValidatorTest {

    private static final String NAME = "Fee";

    @ParameterizedTest
    @EnumSource(value = ChargeTimeType.class, names = { "SPECIFIED_DUE_DATE", "SAVINGS_ACTIVATION", "SAVINGS_CLOSURE", "ANNUAL_FEE",
            "MONTHLY_FEE", "OVERDRAFT_FEE", "WEEKLY_FEE" })
    void savingsPercentageOfAmountChargeIsRejectedUnlessWithdrawalOrNoActivityFee(final ChargeTimeType chargeTime) {
        final PlatformApiDataValidationException rejection = assertThrows(PlatformApiDataValidationException.class,
                () -> validate(ChargeAppliesTo.SAVINGS, chargeTime, ChargeCalculationType.PERCENT_OF_AMOUNT, false));

        assertEquals("validation.msg.validation.errors.exist", rejection.getGlobalisationMessageCode());
        assertEquals(1, rejection.getErrors().size());
        final ApiParameterError error = rejection.getErrors().get(0);
        assertEquals("validation.msg.charges.savings.charge.calculation.type.percentage.allowed.only.for.withdrawal.or.NoActivity",
                error.getUserMessageGlobalisationCode());
        assertEquals("Failed data validation due to: savings.charge.calculation.type.percentage.allowed.only.for.withdrawal.or.NoActivity.",
                error.getDefaultUserMessage());
        assertEquals("chargeCalculationType", error.getParameterName());
        assertEquals(ChargeCalculationType.PERCENT_OF_AMOUNT.getValue(), error.getArgs().get(0).getValue());
    }

    @Test
    void savingsPercentageOfAmountPenaltyIsRejectedWithTheSameError() {
        final PlatformApiDataValidationException rejection = assertThrows(PlatformApiDataValidationException.class,
                () -> validate(ChargeAppliesTo.SAVINGS, ChargeTimeType.SPECIFIED_DUE_DATE, ChargeCalculationType.PERCENT_OF_AMOUNT, true));

        assertEquals("validation.msg.charges.savings.charge.calculation.type.percentage.allowed.only.for.withdrawal.or.NoActivity",
                rejection.getErrors().get(0).getUserMessageGlobalisationCode());
    }

    @ParameterizedTest
    @EnumSource(value = ChargeTimeType.class, names = { "WITHDRAWAL_FEE", "SAVINGS_NOACTIVITY_FEE" })
    void savingsPercentageOfAmountWithdrawalOrNoActivityFeeIsAccepted(final ChargeTimeType chargeTime) {
        assertDoesNotThrow(() -> validate(ChargeAppliesTo.SAVINGS, chargeTime, ChargeCalculationType.PERCENT_OF_AMOUNT, false));
    }

    @ParameterizedTest
    @EnumSource(value = ChargeTimeType.class, names = { "SPECIFIED_DUE_DATE", "SAVINGS_ACTIVATION", "SAVINGS_CLOSURE", "WITHDRAWAL_FEE",
            "ANNUAL_FEE", "MONTHLY_FEE", "OVERDRAFT_FEE", "WEEKLY_FEE", "SAVINGS_NOACTIVITY_FEE" })
    void savingsFlatChargeIsAccepted(final ChargeTimeType chargeTime) {
        assertDoesNotThrow(() -> validate(ChargeAppliesTo.SAVINGS, chargeTime, ChargeCalculationType.FLAT, false));
        assertDoesNotThrow(() -> validate(ChargeAppliesTo.SAVINGS, chargeTime, ChargeCalculationType.FLAT, true));
    }

    @ParameterizedTest
    @EnumSource(value = ChargeTimeType.class, names = { "DISBURSEMENT", "TRANCHE_DISBURSEMENT" })
    void loanPenaltyDueAtDisbursementIsRejected(final ChargeTimeType chargeTime) {
        final ChargeDueAtDisbursementCannotBePenaltyException rejection = assertThrows(
                ChargeDueAtDisbursementCannotBePenaltyException.class,
                () -> validate(ChargeAppliesTo.LOAN, chargeTime, ChargeCalculationType.FLAT, true));

        assertEquals("error.msg.charge.due.at.disbursement.cannot.be.penalty", rejection.getGlobalisationMessageCode());
        assertEquals("Charge 'Fee' is invalid.", rejection.getDefaultUserMessage());
        assertArrayEquals(new Object[] { NAME, NAME }, rejection.getDefaultUserMessageArgs());
    }

    @ParameterizedTest
    @EnumSource(value = ChargeTimeType.class, names = { "DISBURSEMENT", "TRANCHE_DISBURSEMENT" })
    void loanFeeDueAtDisbursementIsAccepted(final ChargeTimeType chargeTime) {
        assertDoesNotThrow(() -> validate(ChargeAppliesTo.LOAN, chargeTime, ChargeCalculationType.FLAT, false));
    }

    @Test
    void loanOverdueFeeThatIsNotAPenaltyIsRejected() {
        final ChargeMustBePenaltyException rejection = assertThrows(ChargeMustBePenaltyException.class,
                () -> validate(ChargeAppliesTo.LOAN, ChargeTimeType.OVERDUE_INSTALLMENT, ChargeCalculationType.FLAT, false));

        assertEquals("error.msg.charge.must.be.penalty", rejection.getGlobalisationMessageCode());
        assertEquals("Charge 'Fee' is invalid.", rejection.getDefaultUserMessage());
        assertArrayEquals(new Object[] { NAME, NAME }, rejection.getDefaultUserMessageArgs());
    }

    @Test
    void loanOverduePenaltyIsAccepted() {
        assertDoesNotThrow(() -> validate(ChargeAppliesTo.LOAN, ChargeTimeType.OVERDUE_INSTALLMENT, ChargeCalculationType.FLAT, true));
    }

    @ParameterizedTest
    @EnumSource(value = ChargeTimeType.class, names = { "SPECIFIED_DUE_DATE", "INSTALMENT_FEE", "DOWN_PAYMENT" })
    void loanChargeAtOtherTimesIsAcceptedAsFeeOrPenalty(final ChargeTimeType chargeTime) {
        assertDoesNotThrow(() -> validate(ChargeAppliesTo.LOAN, chargeTime, ChargeCalculationType.FLAT, false));
        assertDoesNotThrow(() -> validate(ChargeAppliesTo.LOAN, chargeTime, ChargeCalculationType.FLAT, true));
    }

    @Test
    void loanPercentageOfAmountChargeIsNotCheckedByTheSavingsRule() {
        assertDoesNotThrow(
                () -> validate(ChargeAppliesTo.LOAN, ChargeTimeType.SPECIFIED_DUE_DATE, ChargeCalculationType.PERCENT_OF_AMOUNT, false));
    }

    @Test
    void workingCapitalLoanChargeIsNotCheckedByTheLoanRules() {
        assertDoesNotThrow(
                () -> validate(ChargeAppliesTo.WORKING_CAPITAL_LOAN, ChargeTimeType.DISBURSEMENT, ChargeCalculationType.FLAT, true));
    }

    @Test
    void clientChargeIsNotCheckedByTheLoanOrSavingsRules() {
        assertDoesNotThrow(() -> validate(ChargeAppliesTo.CLIENT, ChargeTimeType.SPECIFIED_DUE_DATE, ChargeCalculationType.FLAT, true));
        assertDoesNotThrow(
                () -> validate(ChargeAppliesTo.CLIENT, ChargeTimeType.SPECIFIED_DUE_DATE, ChargeCalculationType.PERCENT_OF_AMOUNT, false));
    }

    private static void validate(final ChargeAppliesTo chargeAppliesTo, final ChargeTimeType chargeTime,
            final ChargeCalculationType chargeCalculationType, final boolean penalty) {
        ChargeDefinitionValidator.validateForCreate(NAME, chargeAppliesTo, chargeTime, chargeCalculationType, penalty);
    }
}
