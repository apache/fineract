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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdSummary;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.junit.jupiter.api.Test;

public class FeignLoanOriginalScheduleArrearsTest extends FeignLoanTestBase {

    @Test
    void ageingJobUsesOriginalScheduleAndEachLoansRepaymentAndWaiverTotals() {
        runAt("01 January 2026", () -> {
            Long clientId = createClient("01 January 2026");
            PostLoanProductsRequest product = fourInstallmentsCumulativeWithInterestRecalculation().numberOfRepayments(2)
                    .interestRatePerPeriod(12.0).amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)
                    .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY).isArrearsBasedOnOriginalSchedule(true)
                    .graceOnArrearsAgeing(3).enableAccrualActivityPosting(false);
            Long productId = createLoanProduct(product);
            Long principalRepaidLoanId = createLoan(clientId, productId, 2000.0);
            Long interestRepaidLoanId = createLoan(clientId, productId, 3000.0);

            // Capture the schedule before transactions can recalculate it. Only its first instalment will be overdue.
            GetLoansLoanIdRepaymentPeriod firstLoanOriginalPeriod = getLoanDetails(principalRepaidLoanId).getRepaymentSchedule()
                    .getPeriods().get(1);
            GetLoansLoanIdRepaymentPeriod secondLoanOriginalPeriod = getLoanDetails(interestRepaidLoanId).getRepaymentSchedule()
                    .getPeriods().get(1);
            assertThat(firstLoanOriginalPeriod.getDueDate()).isEqualTo(LocalDate.of(2026, 2, 1));
            assertThat(secondLoanOriginalPeriod.getDueDate()).isEqualTo(LocalDate.of(2026, 2, 1));

            updateBusinessDate("01 February 2026");
            addInterestWaiver(principalRepaidLoanId, waiveInterest(5.25, "01 February 2026"));
            addRepayment(principalRepaidLoanId, repayment(150.50, "01 February 2026"));
            addRepayment(interestRepaidLoanId, repayment(10.50, "01 February 2026"));

            GetLoansLoanIdSummary firstLoanSummary = getLoanDetails(principalRepaidLoanId).getSummary();
            GetLoansLoanIdSummary secondLoanSummary = getLoanDetails(interestRepaidLoanId).getSummary();
            assertThat(firstLoanSummary.getPrincipalPaid()).isPositive();
            assertThat(firstLoanSummary.getInterestPaid()).isPositive();
            assertThat(firstLoanSummary.getInterestWaived()).isEqualByComparingTo("5.25");
            assertThat(secondLoanSummary.getPrincipalPaid()).isZero();
            assertThat(secondLoanSummary.getInterestPaid()).isEqualByComparingTo("10.50");

            List<ExpectedArrears> expected = List.of(expectedArrears(principalRepaidLoanId, firstLoanOriginalPeriod, firstLoanSummary),
                    expectedArrears(interestRepaidLoanId, secondLoanOriginalPeriod, secondLoanSummary));
            updateBusinessDate("05 February 2026");

            // This job clears the previous ageing rows, then rebuilds both loans through the summary query.
            schedulerHelper.executeAndAwaitJob("Update Loan Arrears Ageing");
            expected.forEach(this::assertArrears);

            // Rebuilding again must not count repayments or waivers twice.
            schedulerHelper.executeAndAwaitJob("Update Loan Arrears Ageing");
            expected.forEach(this::assertArrears);
        });
    }

    private Long createLoan(Long clientId, Long productId, double principal) {
        Long loanId = applyForLoan(LoanRequestBuilders.applyCumulativeLoan(clientId, productId, "01 January 2026", principal, 2, 12.0)
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL));
        approveLoan(loanId, LoanRequestBuilders.approveLoan(principal, "01 January 2026"));
        disburseLoan(loanId, LoanRequestBuilders.disburseLoan(principal, "01 January 2026"));
        return loanId;
    }

    private static ExpectedArrears expectedArrears(Long loanId, GetLoansLoanIdRepaymentPeriod originalPeriod,
            GetLoansLoanIdSummary summary) {
        BigDecimal principal = originalPeriod.getPrincipalDue().subtract(summary.getPrincipalPaid())
                .subtract(summary.getPrincipalWrittenOff());
        BigDecimal interest = originalPeriod.getInterestDue().subtract(summary.getInterestPaid()).subtract(summary.getInterestWaived());
        assertThat(principal).isPositive();
        assertThat(interest).isNotNegative();
        return new ExpectedArrears(loanId, principal, interest, originalPeriod.getDueDate());
    }

    private void assertArrears(ExpectedArrears expected) {
        GetLoansLoanIdSummary actual = getLoanDetails(expected.loanId()).getSummary();
        assertThat(actual.getPrincipalOverdue()).as("loan %s principal overdue", expected.loanId())
                .isEqualByComparingTo(expected.principal());
        assertThat(actual.getInterestOverdue()).as("loan %s interest overdue", expected.loanId()).isEqualByComparingTo(expected.interest());
        assertThat(actual.getFeeChargesOverdue()).isZero();
        assertThat(actual.getPenaltyChargesOverdue()).isZero();
        assertThat(actual.getTotalOverdue()).as("loan %s total overdue", expected.loanId())
                .isEqualByComparingTo(expected.principal().add(expected.interest()));
        assertThat(actual.getOverdueSinceDate()).isEqualTo(expected.overdueSince());
    }

    private record ExpectedArrears(Long loanId, BigDecimal principal, BigDecimal interest, LocalDate overdueSince) {
    }
}
