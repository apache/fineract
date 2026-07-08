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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(1)
public class LoanMultipleDisbursementRepaymentScheduleTest extends FeignLoanTestBase {

    @Test
    public void loanNoDuplicateRepaymentScheduleWithMultipleDisbursementTest() {
        runAt("07 July 2023", () -> {
            // Client and multi-disburse periodic-accrual loan account creation
            Long clientId = createClient();
            // Accounts oof periodic accrual
            Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct());

            Long loanId = applyForLoan(applyLoanRequest(clientId, loanProductId, "07 July 2023", 1000.0, 1));
            approveLoan(loanId, approveLoanRequest(1000.0, "07 July 2023"));
            disburseLoan(loanId, BigDecimal.valueOf(370.55), "07 July 2023");

            // Add Charge fee
            addCharge(loanId, false, 5.15, "11 July 2023");

            // run cob
            updateBusinessDate("12 July 2023");
            schedulerHelper.executeAndAwaitJob("Loan COB");

            // verify accrual transaction created for charge due date
            checkAccrualTransaction("11 July 2023", 0.0, 5.15, 0.0, loanId);

            // make Merchant Issued Refund
            updateBusinessDate("21 July 2023");
            PostLoansLoanIdTransactionsResponse merchantIssuedRefund = makeMerchantIssuedRefund(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("21 July 2023")
                            .locale(LoanTestData.LOCALE).transactionAmount(167.4));
            assertNotNull(merchantIssuedRefund);

            // run cob
            schedulerHelper.executeAndAwaitJob("Loan COB");

            // make another disbursement
            updateBusinessDate("24 July 2023");
            disburseLoan(loanId, BigDecimal.valueOf(18), "24 July 2023");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());

            List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
            assertNotNull(periods);

            // verify only one active period
            long activePeriods = periods.stream().filter(p -> p.getPeriod() != null && p.getPeriod().equals(1)).count();
            assertEquals(1, activePeriods);
        });
    }

    private void checkAccrualTransaction(final String transactionDate, final double interestPortion, final double feePortion,
            final double penaltyPortion, final Long loanId) {
        final LocalDate accrualDate = LocalDate.parse(transactionDate, dateTimeFormatter);
        final GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        boolean isTransactionFound = false;
        for (GetLoansLoanIdTransactions transaction : loanDetails.getTransactions()) {
            if (Boolean.TRUE.equals(transaction.getType().getAccrual()) && accrualDate.equals(transaction.getDate())) {
                isTransactionFound = true;
                assertEquals(interestPortion, Utils.getDoubleValue(transaction.getInterestPortion()), "Mismatch in transaction amounts");
                assertEquals(feePortion, Utils.getDoubleValue(transaction.getFeeChargesPortion()), "Mismatch in transaction amounts");
                assertEquals(penaltyPortion, Utils.getDoubleValue(transaction.getPenaltyChargesPortion()),
                        "Mismatch in transaction amounts");
                break;
            }
        }
        assertTrue(isTransactionFound, "No Accrual entries are posted");
    }
}
