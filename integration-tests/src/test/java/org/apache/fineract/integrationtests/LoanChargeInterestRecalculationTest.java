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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PutChargeTransactionChangesRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The stored interest of an overdue progressive installment must not depend on whether the LoanBalanceChanged external
 * event is posted (FINERACT-2910). Posting needs FINERACT_EXTERNAL_EVENTS_ENABLED=true and the event type enabled, so
 * the test runs with the event type both enabled and disabled; the expectations are the same in every combination.
 */
public class LoanChargeInterestRecalculationTest extends FeignLoanTestBase {

    private static final String LOAN_BALANCE_CHANGED_EVENT = "LoanBalanceChangedBusinessEvent";

    @AfterEach
    public void disableLoanBalanceChangedEvent() {
        externalEventHelper.configureBusinessEvent(LOAN_BALANCE_CHANGED_EVENT, false);
    }

    @ParameterizedTest(name = "event type enabled: {0}")
    @ValueSource(booleans = { false, true })
    public void interestIsRecalculatedAfterAddingChargesToOverdueLoan(final boolean eventEnabled) {
        externalEventHelper.configureBusinessEvent(LOAN_BALANCE_CHANGED_EVENT, eventEnabled);
        final Long loanId = createLoanWithOverdueSecondInstallment();

        runAt("05 March 2024", () -> addLoanCharge(loanId, createCharge(5.0d, "EUR").getResourceId(), "05 March 2024", 5.0d));
        runAt("10 March 2024", () -> {
            final Long penaltyId = chargesHelper.createCharge(ChargeRequestBuilders.loanSpecifiedDueDatePenalty(10.0d, "EUR"))
                    .getResourceId();
            addLoanCharge(loanId, penaltyId, "10 March 2024", 10.0d);
            assertInterestDue(loanId, 3, 0.42d);
        });
    }

    @ParameterizedTest(name = "event type enabled: {0}")
    @ValueSource(booleans = { false, true })
    public void interestIsRecalculatedAfterUndoingChargeWaive(final boolean eventEnabled) {
        externalEventHelper.configureBusinessEvent(LOAN_BALANCE_CHANGED_EVENT, eventEnabled);
        final Long loanId = createLoanWithOverdueSecondInstallment();

        final Long[] waiveTransactionId = new Long[1];
        final Long[] loanChargeId = new Long[1];
        runAt("10 March 2024", () -> {
            final Long chargeId = createCharge(5.0d, "EUR").getResourceId();
            loanChargeId[0] = addLoanCharge(loanId, chargeId, "10 March 2024", 5.0d).getResourceId();
            assertInterestDue(loanId, 3, 0.42d);

            waiveLoanCharge(loanId, loanChargeId[0], 3);
            assertInterestDue(loanId, 3, 0.42d);

            final List<GetLoansLoanIdTransactions> transactions = getLoanDetails(loanId).getTransactions();
            waiveTransactionId[0] = transactions.stream().filter(t -> Boolean.TRUE.equals(t.getType().getWaiveCharges())).findFirst()
                    .orElseThrow().getId();
        });
        runAt("15 March 2024", () -> {
            undoWaiveLoanCharge(loanId, waiveTransactionId[0],
                    new PutChargeTransactionChangesRequest().id(waiveTransactionId[0]).loanId(loanId));
            assertInterestDue(loanId, 3, 0.43d);
        });
    }

    // 100 EUR, 7%, 6 monthly installments, 360/30 days, daily interest recalculation, disbursed on 01 January 2024.
    // The 1st installment is repaid, the 2nd one (due 01 March 2024) is overdue from then on.
    private Long createLoanWithOverdueSecondInstallment() {
        final Long[] loanId = new Long[1];
        runAt("01 January 2024", () -> {
            final Long clientId = createClient();
            final Long productId = createLoanProduct(create4IProgressive());
            loanId[0] = applyForLoan(applyLP2ProgressiveLoanRequest(clientId, productId, "01 January 2024", 100.0d, 7.0d, 6, null));
            approveLoan(loanId[0], LoanRequestBuilders.approveLoan(100.0d, "01 January 2024"));
            disburseLoan(loanId[0], BigDecimal.valueOf(100.0d), "01 January 2024");
        });
        runAt("01 February 2024", () -> makeLoanRepayment(loanId[0], "repayment", "01 February 2024", 17.01d));
        return loanId[0];
    }

    private void assertInterestDue(final Long loanId, final int installment, final double expected) {
        final GetLoansLoanIdRepaymentPeriod period = getLoanDetails(loanId).getRepaymentSchedule().getPeriods().stream()
                .filter(p -> Integer.valueOf(installment).equals(p.getPeriod())).findFirst().orElseThrow();
        assertEquals(expected, period.getInterestDue().doubleValue(), 0.0001d, "interest due of installment " + installment);
    }
}
