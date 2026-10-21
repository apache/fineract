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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Adjusting a Charge Adjustment replaces it with a new Charge Adjustment that stays linked to the same loan charge.
 */
public class FeignLoanChargeAdjustmentAdjustmentTest extends FeignLoanTransactionAdjustmentTestBase {

    private static final String CHARGE_ADJUSTMENT = "Charge Adjustment";
    private static final String INVALID_AMOUNT_ERROR = "error.msg.loan.charge.adjustment.invalid.amount";
    private static final String DATE_BEFORE_CHARGE_ERROR = "error.msg.loan.charge.adjustment.date.before.charge.date";
    private static final String CHARGE_DUE_DATE = "15 January 2024";
    private static final double CHARGE_AMOUNT = 250.0;

    private final Map<Long, Long> loanChargeIdByLoanId = new HashMap<>();

    @ParameterizedTest(name = "{0}: {1}")
    @MethodSource("loanVariantsAndChanges")
    void adjustChargeAdjustment(LoanVariant variant, String change, double originalAmount, String originalDate, double newAmount,
            String newDate) {
        runAt(BUSINESS_DATE, () -> verifyAdjustmentMatchesDirectPosting(variant, CHARGE_ADJUSTMENT, this::postChargeAdjustment,
                this::addCharge, originalAmount, originalDate, newAmount, newDate, twinMode(variant)));
    }

    @ParameterizedTest
    @EnumSource(LoanVariant.class)
    void adjustChargeAdjustmentToOverpayLoan(LoanVariant variant) {
        runAt(BUSINESS_DATE, () -> {
            Long loanId = verifyAdjustmentMatchesDirectPosting(variant, CHARGE_ADJUSTMENT, this::postChargeAdjustment,
                    addChargeAndRepayDownTo(100.0), 30.0, LATER_DATE, 200.0, LATER_DATE, twinMode(variant));
            verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getOverpaid);
        });
    }

    @ParameterizedTest
    @EnumSource(LoanVariant.class)
    void adjustChargeAdjustmentToReopenOverpaidLoan(LoanVariant variant) {
        runAt(BUSINESS_DATE, () -> {
            Long loanId = verifyAdjustmentMatchesDirectPosting(variant, CHARGE_ADJUSTMENT, this::postChargeAdjustment,
                    addChargeAndRepayDownTo(100.0), 200.0, LATER_DATE, 30.0, LATER_DATE, twinMode(variant));
            verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getActive);
        });
    }

    @Test
    void adjustRejectedAboveAvailableChargeAmount() {
        runAt(BUSINESS_DATE, () -> {
            Long loanId = createLoanWithCharge();
            postChargeAdjustment(loanId, LATER_DATE, 100.0);
            Long secondId = postChargeAdjustment(loanId, LATER_DATE, 100.0);

            // 250 charge - 100 of the other adjustment; the adjustment being replaced counts as available again
            assertAdjustRejected(loanId, secondId, LATER_DATE, 150.01, INVALID_AMOUNT_ERROR);
            Long newId = adjustTransaction(loanId, secondId, LATER_DATE, 150.0);
            assertReplacedWithSameType(loanId, secondId, newId, CHARGE_ADJUSTMENT, 150.0, LATER_DATE);
        });
    }

    @Test
    void adjustRejectedAboveChargeAmount() {
        runAt(BUSINESS_DATE, () -> {
            Long loanId = createLoanWithCharge();
            Long originalId = postChargeAdjustment(loanId, LATER_DATE, 100.0);

            assertAdjustRejected(loanId, originalId, LATER_DATE, CHARGE_AMOUNT + 1, INVALID_AMOUNT_ERROR);
            Long newId = adjustTransaction(loanId, originalId, LATER_DATE, CHARGE_AMOUNT);
            assertReplacedWithSameType(loanId, originalId, newId, CHARGE_ADJUSTMENT, CHARGE_AMOUNT, LATER_DATE);
        });
    }

    @Test
    void adjustRejectedBeforeChargeDate() {
        runAt(BUSINESS_DATE, () -> {
            Long loanId = createLoanWithCharge();
            Long originalId = postChargeAdjustment(loanId, LATER_DATE, 100.0);

            // the charge is due on 15 January and was added on the business date, so it exists from 15 January
            assertAdjustRejected(loanId, originalId, "14 January 2024", 100.0, DATE_BEFORE_CHARGE_ERROR);
            Long newId = adjustTransaction(loanId, originalId, CHARGE_DUE_DATE, 100.0);
            assertReplacedWithSameType(loanId, originalId, newId, CHARGE_ADJUSTMENT, 100.0, CHARGE_DUE_DATE);
        });
    }

    @Test
    void newChargeAdjustmentAfterAdjustCountsTheReplacement() {
        runAt(BUSINESS_DATE, () -> {
            Long loanId = createLoanWithCharge();
            Long originalId = postChargeAdjustment(loanId, LATER_DATE, 100.0);
            adjustTransaction(loanId, originalId, LATER_DATE, 200.0);

            // the replacement keeps its link to the charge, so only 250 - 200 is left for adjustment
            assertErrorGlobalisationCode(
                    assertThrows(CallFailedRuntimeException.class,
                            () -> chargeAdjustment(loanId, loanChargeIdByLoanId.get(loanId), LoanRequestBuilders.adjustLoanCharge(50.01))),
                    INVALID_AMOUNT_ERROR);
            postChargeAdjustment(loanId, BUSINESS_DATE, 50.0);
        });
    }

    @Test
    void adjustedChargeAdjustmentPostsToFeeIncomeAccount() {
        runAt(BUSINESS_DATE, () -> {
            Long clientId = createClient(DISBURSEMENT_DATE);
            Long productId = createProduct(
                    customizeProduct(fourInstallmentsProgressiveWithAdvancedAllocation(), product -> product.interestRatePerPeriod(0.0)));
            Long loanId = applyAndApproveProgressiveLoan(clientId, productId, DISBURSEMENT_DATE, PRINCIPAL, 0.0, 4, null);
            disburseLoan(loanId, BigDecimal.valueOf(PRINCIPAL), DISBURSEMENT_DATE);
            addCharge(loanId);

            Long originalId = postChargeAdjustment(loanId, LATER_DATE, 100.0);
            Long newId = adjustTransaction(loanId, originalId, LATER_DATE, 60.0);

            assertReplacedWithSameType(loanId, originalId, newId, CHARGE_ADJUSTMENT, 60.0, LATER_DATE);
            verifyTRJournalEntries(originalId, //
                    journalEntry(100.0, getAccounts().getFeeIncomeAccount(), "DEBIT"), //
                    journalEntry(100.0, getAccounts().getFeeReceivableAccount(), "CREDIT"), //
                    journalEntry(100.0, getAccounts().getFeeIncomeAccount(), "CREDIT"), //
                    journalEntry(100.0, getAccounts().getFeeReceivableAccount(), "DEBIT"));
            verifyTRJournalEntries(newId, //
                    journalEntry(60.0, getAccounts().getFeeIncomeAccount(), "DEBIT"), //
                    journalEntry(60.0, getAccounts().getFeeReceivableAccount(), "CREDIT"));
        });
    }

    private static TwinMode twinMode(LoanVariant variant) {
        if (variant.isCumulativeWithInterestRecalculation()) {
            return TwinMode.REPAYMENT;
        }
        return variant.isProgressiveWithInterestRecalculation() ? TwinMode.REPLAYED : TwinMode.DIRECT;
    }

    private Long createLoanWithCharge() {
        Long clientId = createClient(DISBURSEMENT_DATE);
        Long productId = createProduct(LoanVariant.PROGRESSIVE);
        Long loanId = createDisbursedLoan(clientId, productId, LoanVariant.PROGRESSIVE);
        addCharge(loanId);
        return loanId;
    }

    private void addCharge(Long loanId) {
        Long chargeId = chargesHelper.createCharge(ChargeRequestBuilders.loanSpecifiedDueDateFee(CHARGE_AMOUNT).currencyCode("EUR"))
                .getResourceId();
        loanChargeIdByLoanId.put(loanId,
                loanHelper.addSpecifiedDueDateCharge(loanId, chargeId, CHARGE_AMOUNT, CHARGE_DUE_DATE).getResourceId());
    }

    private LoanPreparation addChargeAndRepayDownTo(double remainingOutstanding) {
        LoanPreparation repayment = repayDownTo(remainingOutstanding);
        return loanId -> {
            addCharge(loanId);
            repayment.prepare(loanId);
        };
    }

    /**
     * Charge adjustments are always booked on the business date, so the business date is moved to the requested date.
     */
    private Long postChargeAdjustment(Long loanId, String date, double amount) {
        updateBusinessDate(date);
        try {
            return chargeAdjustment(loanId, loanChargeIdByLoanId.get(loanId), LoanRequestBuilders.adjustLoanCharge(amount))
                    .getSubResourceId();
        } finally {
            updateBusinessDate(BUSINESS_DATE);
        }
    }
}
