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
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

public class LoanApplicationScheduleMonthlyTest extends FeignLoanTestBase {

    public static final Integer TOTAL_REPAYMENTS = 14;
    public static final String NUMBER_OF_REPAYMENTS = String.valueOf(TOTAL_REPAYMENTS);
    public static final String DISBURSEMENT_DATE = "30 December 2022";
    public static final String CLIENT_ACTIVATION_DATE = "13 October 2022";

    @Test
    public void validateSeedDate31() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);

        String firstRepaymentDate = "31 January 2023";
        Long loanProductId = createLoanProductEntity();

        Long loanId = applyForLoanApplicationWithFirstRepaymentDate(clientId, loanProductId, firstRepaymentDate);

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 1, 31), //
                LocalDate.of(2023, 2, 28), //
                LocalDate.of(2023, 3, 31), //
                LocalDate.of(2023, 4, 30), //
                LocalDate.of(2023, 5, 31), //
                LocalDate.of(2023, 6, 30), //
                LocalDate.of(2023, 7, 31), //
                LocalDate.of(2023, 8, 31), //
                LocalDate.of(2023, 9, 30), //
                LocalDate.of(2023, 10, 31), //
                LocalDate.of(2023, 11, 30), //
                LocalDate.of(2023, 12, 31), //
                LocalDate.of(2024, 1, 31), //
                LocalDate.of(2024, 2, 29)));
    }

    @Test
    public void validateSeedDate30() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);

        String firstRepaymentDate = "30 January 2023";
        Long loanProductId = createLoanProductEntity();

        Long loanId = applyForLoanApplicationWithFirstRepaymentDate(clientId, loanProductId, firstRepaymentDate);

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 1, 30), //
                LocalDate.of(2023, 2, 28), //
                LocalDate.of(2023, 3, 30), //
                LocalDate.of(2023, 4, 30), //
                LocalDate.of(2023, 5, 30), //
                LocalDate.of(2023, 6, 30), //
                LocalDate.of(2023, 7, 30), //
                LocalDate.of(2023, 8, 30), //
                LocalDate.of(2023, 9, 30), //
                LocalDate.of(2023, 10, 30), //
                LocalDate.of(2023, 11, 30), //
                LocalDate.of(2023, 12, 30), //
                LocalDate.of(2024, 1, 30), //
                LocalDate.of(2024, 2, 29)));
    }

    @Test
    public void validateSeedDate28() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);

        String firstRepaymentDate = "28 January 2023";
        Long loanProductId = createLoanProductEntity();

        Long loanId = applyForLoanApplicationWithFirstRepaymentDate(clientId, loanProductId, firstRepaymentDate);

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 1, 28), //
                LocalDate.of(2023, 2, 28), //
                LocalDate.of(2023, 3, 28), //
                LocalDate.of(2023, 4, 28), //
                LocalDate.of(2023, 5, 28), //
                LocalDate.of(2023, 6, 28), //
                LocalDate.of(2023, 7, 28), //
                LocalDate.of(2023, 8, 28), //
                LocalDate.of(2023, 9, 28), //
                LocalDate.of(2023, 10, 28), //
                LocalDate.of(2023, 11, 28), //
                LocalDate.of(2023, 12, 28), //
                LocalDate.of(2024, 1, 28), //
                LocalDate.of(2024, 2, 28)));
    }

    @Test
    public void validateSeedDate25() {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);

        String firstRepaymentDate = "25 January 2023";
        Long loanProductId = createLoanProductEntity();

        Long loanId = applyForLoanApplicationWithFirstRepaymentDate(clientId, loanProductId, firstRepaymentDate);

        verifyDueDates(loanId, List.of(//
                LocalDate.of(2023, 1, 25), //
                LocalDate.of(2023, 2, 25), //
                LocalDate.of(2023, 3, 25), //
                LocalDate.of(2023, 4, 25), //
                LocalDate.of(2023, 5, 25), //
                LocalDate.of(2023, 6, 25), //
                LocalDate.of(2023, 7, 25), //
                LocalDate.of(2023, 8, 25), //
                LocalDate.of(2023, 9, 25), //
                LocalDate.of(2023, 10, 25), //
                LocalDate.of(2023, 11, 25), //
                LocalDate.of(2023, 12, 25), //
                LocalDate.of(2024, 1, 25), //
                LocalDate.of(2024, 2, 25)));
    }

    /**
     * Asserts the due date of every repayment period. Period 0 is the disbursement row, so the expected dates line up
     * with periods 1..n.
     */
    private void verifyDueDates(final Long loanId, final List<LocalDate> expectedDueDates) {
        final List<GetLoansLoanIdRepaymentPeriod> repaymentPeriods = getLoanDetails(loanId).getRepaymentSchedule().getPeriods();
        assertEquals(expectedDueDates.size() + 1, repaymentPeriods.size(), "Checking the number of repayment periods");
        for (int month = 1; month <= expectedDueDates.size(); month++) {
            assertEquals(expectedDueDates.get(month - 1), repaymentPeriods.get(month).getDueDate(),
                    "Checking for Due Date for " + month + " Month");
        }
    }

    /**
     * create a new loan product
     **/
    private Long createLoanProductEntity() {
        return createLoanProduct(new LoanProductTestBuilder().withPrincipal("10000").withRepaymentAfterEvery("1")
                .withNumberOfRepayments(NUMBER_OF_REPAYMENTS).withRepaymentTypeAsMonth().withInterestRateFrequencyTypeAsMonths()
                .buildRequest(null));
    }

    /**
     * Apply for a Loan
     */
    private Long applyForLoanApplicationWithFirstRepaymentDate(final Long clientId, final Long loanProductId, String firstRepaymentDate) {
        return applyForLoan(LoanRequestBuilders
                .legacyIndividualApplication(clientId, loanProductId, "10000", TOTAL_REPAYMENTS, BigDecimal.valueOf(2), DISBURSEMENT_DATE)
                .repaymentsStartingFromDate(firstRepaymentDate));
    }
}
