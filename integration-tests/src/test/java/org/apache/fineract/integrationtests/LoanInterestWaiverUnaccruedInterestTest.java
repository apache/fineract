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
import java.time.LocalDate;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

/**
 * An interest waiver on a periodic-accrual loan only reverses interest that was accrued and not yet received. The part
 * of the waiver that was never accrued is unrecognized income: it must not be credited to Interest Receivable, which
 * would otherwise go negative.
 */
public class LoanInterestWaiverUnaccruedInterestTest extends FeignLoanTestBase {

    private static final double PRINCIPAL = 1000.0;
    private static final double WAIVED = 5.0;

    /**
     * Nothing is accrued yet in the middle of the first period, so the whole waiver is unrecognized income. The accrual
     * at the end of the period then accrues only the interest left after the waiver, and the repayment of the
     * instalment receives exactly that, so Interest Receivable ends at zero.
     */
    @Test
    public void testWaiverOfInterestNotYetAccruedLeavesInterestReceivableAtZero() {
        final Long clientId = createClient();
        final Long[] loanId = new Long[1];
        runAt("01 January 2036", () -> {
            final Long productId = createLoanProduct(fourInstallmentsCumulative());
            loanId[0] = applyAndApproveCumulativeLoan(clientId, productId, "01 January 2036", PRINCIPAL, 12.0, 4, null);
            disburseLoan(loanId[0], BigDecimal.valueOf(PRINCIPAL), "01 January 2036");
        });
        runAt("15 January 2036", () -> {
            transactionHelper.addInterestWaiver(loanId[0], waiveInterest(WAIVED, "15 January 2036"));

            assertEquals(0.0, interestReceivable(loanId[0], LocalDate.of(2036, 1, 15)),
                    "Nothing was accrued, so the waiver must not touch Interest Receivable");
        });
        runAt("01 February 2036", () -> {
            runPeriodicAccrualAccounting("01 February 2036");
            addRepaymentForLoan(loanId[0], Utils.getDoubleValue(firstInstalment(loanId[0]).getTotalOutstandingForPeriod()),
                    "01 February 2036");

            assertEquals(0.0, interestReceivable(loanId[0], LocalDate.of(2036, 2, 1)),
                    "The interest accrued after the waiver is the interest received for the first instalment");
        });
    }

    /**
     * The first instalment is repaid on its due date before the accrual job posts its interest. That accrual is then
     * already received, so nothing is receivable and a waiver of later interest is entirely unrecognized income.
     */
    @Test
    public void testWaiverAfterInterestReceivedBeforeItsAccrualLeavesInterestReceivableAtZero() {
        final Long clientId = createClient();
        final Long[] loanId = new Long[1];
        runAt("01 January 2036", () -> {
            final Long productId = createLoanProduct(fourInstallmentsCumulative());
            loanId[0] = applyAndApproveCumulativeLoan(clientId, productId, "01 January 2036", PRINCIPAL, 12.0, 4, null);
            disburseLoan(loanId[0], BigDecimal.valueOf(PRINCIPAL), "01 January 2036");
        });
        runAt("01 February 2036", () -> {
            addRepaymentForLoan(loanId[0], Utils.getDoubleValue(firstInstalment(loanId[0]).getTotalDueForPeriod()), "01 February 2036");
            runPeriodicAccrualAccounting("01 February 2036");
            assertEquals(0.0, interestReceivable(loanId[0], LocalDate.of(2036, 2, 1)),
                    "The accrued interest of the first instalment was already received");

            transactionHelper.addInterestWaiver(loanId[0], waiveInterest(WAIVED, "01 February 2036"));

            assertEquals(0.0, interestReceivable(loanId[0], LocalDate.of(2036, 2, 1)),
                    "Nothing was receivable, so the waiver must not touch Interest Receivable");
        });
    }

    private GetLoansLoanIdRepaymentPeriod firstInstalment(final Long loanId) {
        return getLoanDetails(loanId).getRepaymentSchedule().getPeriods().stream()
                .filter(period -> Integer.valueOf(1).equals(period.getPeriod())).findFirst().orElseThrow();
    }

    private double interestReceivable(final Long loanId, final LocalDate cutoff) {
        return interimJournalBalance(loanId, getAssetAccountId("interestReceivable"), cutoff, true);
    }
}
