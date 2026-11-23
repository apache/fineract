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

import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalDelinquencyTestValidators.ExpectedTag.tag;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalDelinquencyTestValidators.validateLoanLevelDelinquency;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalDelinquencyTestValidators.validateOneActiveTagPerPeriod;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalDelinquencyTestValidators.validateTagHistory;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignWorkingCapitalTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A Working Capital transaction that changes the loan status classifies delinquency once, after the status change and
 * the save, so the tags match the loan the command leaves behind: a closing transaction lifts the tag, an undo that
 * reopens the loan tags it again, and a transaction that leaves the delinquency alone adds no tag history. With instant
 * classification off a repayment is left to the COB, unless it closes or overpays the loan, which the COB no longer
 * processes.
 *
 * <p>
 * Common setup: 15 DAYS bucket, 3% minimum payment, ranges R1 [1-30], R2 [31-60], principal 9000 disbursed 2026-01-01
 * and advanced with COB to 2026-01-16, so period 1 (01-01..01-15) is 1 day late with 270 outstanding and carries an
 * active R1 tag added on 2026-01-16. Every transaction below is made on 2026-01-16.
 *
 * <p>
 * Instant delinquency classification is pinned on for the class, so a repayment classifies straight away; the previous
 * value is restored afterwards.
 */
public class FeignWorkingCapitalLoanDelinquencyAfterStatusChangeTest extends FeignWorkingCapitalTestBase {

    private static final String DISBURSEMENT_DATE = "01 January 2026";
    private static final String DISBURSEMENT_DATE_ISO = "2026-01-01";
    private static final String TRANSACTION_DATE = "16 January 2026";
    private static final String TRANSACTION_DATE_ISO = "2026-01-16";
    private static final BigDecimal PRINCIPAL = BigDecimal.valueOf(9000);
    private static final BigDecimal OVERPAYMENT = BigDecimal.valueOf(100);
    private static final int DELINQUENCY_FREQUENCY_DAYS = 15;
    private static final BigDecimal MINIMUM_PAYMENT_PERCENT = BigDecimal.valueOf(3);

    private static final String STATUS_ACTIVE = "loanStatusType.active";
    private static final String STATUS_CLOSED_OBLIGATIONS_MET = "loanStatusType.closed.obligations.met";
    private static final String STATUS_OVERPAID = "loanStatusType.overpaid";

    private String r1;
    private Long productId;
    private boolean instantClassificationWasEnabled;

    @BeforeAll
    void setupDelinquencyAfterStatusChangeProduct() {
        r1 = Utils.uniqueRandomStringGenerator("WC_DLQ_ST_R1_", 6);
        final String r2 = Utils.uniqueRandomStringGenerator("WC_DLQ_ST_R2_", 6);
        final Long r1Id = createDelinquencyRange(r1, 1, 30);
        final Long r2Id = createDelinquencyRange(r2, 31, 60);
        productId = createWcProductWithDelinquencyBucket(DELINQUENCY_FREQUENCY_DAYS, MINIMUM_PAYMENT_PERCENT, List.of(r1Id, r2Id));

        instantClassificationWasEnabled = isConfigurationEnabled(GlobalConfigurationConstants.ENABLE_INSTANT_DELINQUENCY_CALCULATION);
        setConfigurationEnabled(GlobalConfigurationConstants.ENABLE_INSTANT_DELINQUENCY_CALCULATION, true);
    }

    @AfterAll
    void restoreInstantClassification() {
        setConfigurationEnabled(GlobalConfigurationConstants.ENABLE_INSTANT_DELINQUENCY_CALCULATION, instantClassificationWasEnabled);
    }

    @Test
    @DisplayName("a repayment that closes the loan lifts the active tag on the closed loan")
    void repaymentClosingTheLoan_liftsTheActiveTag() {
        runAt(DISBURSEMENT_DATE_ISO, () -> {
            final Long loanId = delinquentLoan();

            makeWcRepayment(loanId, PRINCIPAL, TRANSACTION_DATE);

            assertStatus(loanId, STATUS_CLOSED_OBLIGATIONS_MET);
            validateTagHistory(getDelinquencyTagHistory(loanId), //
                    tag(1, TRANSACTION_DATE_ISO, TRANSACTION_DATE_ISO, r1));
            validateOneActiveTagPerPeriod(getDelinquencyTagHistory(loanId));
        });
    }

    /**
     * The COB does not process a closed loan, so a repayment that closes the loan classifies it even with instant
     * classification off; otherwise the tag would stay active on the closed loan.
     */
    @Test
    @DisplayName("with instant classification off, a closing repayment still lifts the tag")
    void repaymentClosingTheLoan_withInstantClassificationOff_stillLiftsTheTag() {
        runAt(DISBURSEMENT_DATE_ISO, () -> {
            final Long loanId = delinquentLoan();

            repayWithInstantClassificationOff(loanId, PRINCIPAL);

            assertStatus(loanId, STATUS_CLOSED_OBLIGATIONS_MET);
            validateTagHistory(getDelinquencyTagHistory(loanId), //
                    tag(1, TRANSACTION_DATE_ISO, TRANSACTION_DATE_ISO, r1));
            validateOneActiveTagPerPeriod(getDelinquencyTagHistory(loanId));
        });
    }

    @Test
    @DisplayName("with instant classification off, an overpaying repayment still lifts the tag")
    void repaymentOverpayingTheLoan_withInstantClassificationOff_stillLiftsTheTag() {
        runAt(DISBURSEMENT_DATE_ISO, () -> {
            final Long loanId = delinquentLoan();

            repayWithInstantClassificationOff(loanId, PRINCIPAL.add(OVERPAYMENT));

            assertStatus(loanId, STATUS_OVERPAID);
            validateTagHistory(getDelinquencyTagHistory(loanId), //
                    tag(1, TRANSACTION_DATE_ISO, TRANSACTION_DATE_ISO, r1));
            validateOneActiveTagPerPeriod(getDelinquencyTagHistory(loanId));
        });
    }

    @Test
    @DisplayName("with instant classification off, a repayment that leaves the loan active leaves the tag to the COB")
    void partialRepayment_withInstantClassificationOff_leavesTheTagToTheCob() {
        runAt(DISBURSEMENT_DATE_ISO, () -> {
            final Long loanId = delinquentLoan();

            repayWithInstantClassificationOff(loanId, BigDecimal.valueOf(270));

            assertStatus(loanId, STATUS_ACTIVE);
            validateTagHistory(getDelinquencyTagHistory(loanId), //
                    tag(1, TRANSACTION_DATE_ISO, null, r1));

            advanceBusinessDateWithCob(loanId, TRANSACTION_DATE_ISO, "2026-01-17");

            validateTagHistory(getDelinquencyTagHistory(loanId), //
                    tag(1, TRANSACTION_DATE_ISO, "2026-01-17", r1));
        });
    }

    @Test
    @DisplayName("undoing the repayment that closed the loan reopens it and tags the late period again")
    void undoReopeningAClosedLoan_tagsTheLatePeriodAgain() {
        runAt(DISBURSEMENT_DATE_ISO, () -> {
            final Long loanId = delinquentLoan();
            final Long repaymentId = makeWcRepayment(loanId, PRINCIPAL, TRANSACTION_DATE);
            assertStatus(loanId, STATUS_CLOSED_OBLIGATIONS_MET);

            undoWcTransaction(loanId, repaymentId);

            assertStatus(loanId, STATUS_ACTIVE);
            validateTagHistory(getDelinquencyTagHistory(loanId), //
                    tag(1, TRANSACTION_DATE_ISO, TRANSACTION_DATE_ISO, r1), //
                    tag(1, TRANSACTION_DATE_ISO, null, r1));
            validateOneActiveTagPerPeriod(getDelinquencyTagHistory(loanId), 1);
            validateLoanLevelDelinquency(getDelinquentData(loanId), "270", 1, TRANSACTION_DATE_ISO);
        });
    }

    @Test
    @DisplayName("undoing the repayment that overpaid the loan reopens it and tags the late period again")
    void undoReopeningAnOverpaidLoan_tagsTheLatePeriodAgain() {
        runAt(DISBURSEMENT_DATE_ISO, () -> {
            final Long loanId = delinquentLoan();
            final Long repaymentId = makeWcRepayment(loanId, PRINCIPAL.add(OVERPAYMENT), TRANSACTION_DATE);
            assertStatus(loanId, STATUS_OVERPAID);

            undoWcTransaction(loanId, repaymentId);

            assertStatus(loanId, STATUS_ACTIVE);
            validateTagHistory(getDelinquencyTagHistory(loanId), //
                    tag(1, TRANSACTION_DATE_ISO, TRANSACTION_DATE_ISO, r1), //
                    tag(1, TRANSACTION_DATE_ISO, null, r1));
            validateOneActiveTagPerPeriod(getDelinquencyTagHistory(loanId), 1);
            validateLoanLevelDelinquency(getDelinquentData(loanId), "270", 1, TRANSACTION_DATE_ISO);
        });
    }

    @Test
    @DisplayName("a credit balance refund that closes an overpaid loan adds no tag history")
    void creditBalanceRefundClosingAnOverpaidLoan_addsNoTagHistory() {
        runAt(DISBURSEMENT_DATE_ISO, () -> {
            final Long loanId = delinquentLoan();
            makeWcRepayment(loanId, PRINCIPAL.add(OVERPAYMENT), TRANSACTION_DATE);
            assertStatus(loanId, STATUS_OVERPAID);

            wcLoanHelper.creditBalanceRefund(loanId, WorkingCapitalLoanRequestBuilders.creditBalanceRefund(OVERPAYMENT, TRANSACTION_DATE));

            assertStatus(loanId, STATUS_CLOSED_OBLIGATIONS_MET);
            validateTagHistory(getDelinquencyTagHistory(loanId), //
                    tag(1, TRANSACTION_DATE_ISO, TRANSACTION_DATE_ISO, r1));
            validateOneActiveTagPerPeriod(getDelinquencyTagHistory(loanId));
        });
    }

    @Test
    @DisplayName("a charge that reopens a closed loan adds no tag for the period the repayment settled")
    void chargeReopeningAClosedLoan_addsNoTagForTheSettledPeriod() {
        runAt(DISBURSEMENT_DATE_ISO, () -> {
            final Long loanId = delinquentLoan();
            makeWcRepayment(loanId, PRINCIPAL, TRANSACTION_DATE);
            assertStatus(loanId, STATUS_CLOSED_OBLIGATIONS_MET);

            final double chargeAmount = 100;
            final Long chargeDefinitionId = wcLoanHelper
                    .createGlobalCharge(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(false, chargeAmount));
            wcLoanHelper.addCharge(loanId, WorkingCapitalLoanRequestBuilders.addCharge(chargeDefinitionId, chargeAmount, TRANSACTION_DATE));

            assertStatus(loanId, STATUS_ACTIVE);
            validateTagHistory(getDelinquencyTagHistory(loanId), //
                    tag(1, TRANSACTION_DATE_ISO, TRANSACTION_DATE_ISO, r1));
            validateOneActiveTagPerPeriod(getDelinquencyTagHistory(loanId));
        });
    }

    /** A disbursed loan whose period 1 carries an active R1 tag as of {@value #TRANSACTION_DATE_ISO}. */
    private Long delinquentLoan() {
        final Long clientId = createClient(DISBURSEMENT_DATE);
        final Long loanId = createApproveAndDisburseWcLoan(clientId, productId, PRINCIPAL, DISBURSEMENT_DATE);
        runInlineWcCob(loanId);
        advanceBusinessDateWithCob(loanId, DISBURSEMENT_DATE_ISO, TRANSACTION_DATE_ISO);
        validateTagHistory(getDelinquencyTagHistory(loanId), //
                tag(1, TRANSACTION_DATE_ISO, null, r1));
        return loanId;
    }

    /**
     * Repays with instant classification off: the repayment classifies only if it takes the loan out of the COB scope.
     */
    private void repayWithInstantClassificationOff(final Long loanId, final BigDecimal amount) {
        setConfigurationEnabled(GlobalConfigurationConstants.ENABLE_INSTANT_DELINQUENCY_CALCULATION, false);
        try {
            makeWcRepayment(loanId, amount, TRANSACTION_DATE);
        } finally {
            setConfigurationEnabled(GlobalConfigurationConstants.ENABLE_INSTANT_DELINQUENCY_CALCULATION, true);
        }
    }

    private void assertStatus(final Long loanId, final String expectedStatusCode) {
        assertEquals(expectedStatusCode, getWcLoanDetails(loanId).getStatus().getCode(), "loan status");
    }
}
