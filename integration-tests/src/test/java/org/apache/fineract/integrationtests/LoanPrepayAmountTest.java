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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTemplateResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoanPrepayAmountTest extends BaseLoanIntegrationTest {

    Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    Long loanId;

    @Test
    public void testLoanPrepayAmountProgressive() {
        runAt("1 January 2024", () -> {
            final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());
            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applyLP2ProgressiveLoanRequest(clientId,
                    loanProductsResponse.getResourceId(), "01 January 2024", 1000.0, 9.99, 6, null));
            loanId = postLoansResponse.getLoanId();
            loanTransactionHelper.approveLoan(loanId, approveLoanRequest(1000.0, "01 January 2024"));
            disburseLoan(loanId, BigDecimal.valueOf(250.0), "01 January 2024");
        });
        runAt("7 january 2024", () -> {
            disburseLoan(loanId, BigDecimal.valueOf(350.0), "04 January 2024");
            disburseLoan(loanId, BigDecimal.valueOf(400.0), "05 January 2024");
        });
        for (int i = 7; i <= 31; i++) {
            runAt(i + " January 2024", () -> {
                GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
                GetLoansLoanIdTransactionsTemplateResponse prepayAmountResponse = loanTransactionHelper.getPrepaymentAmount(loanId, null,
                        DATETIME_PATTERN);
                Assertions.assertEquals(BigDecimal.valueOf(prepayAmountResponse.getInterestPortion()).stripTrailingZeros(),
                        loanDetails.getSummary().getTotalUnpaidPayableNotDueInterest().stripTrailingZeros());
            });
        }
    }

    @Test
    public void testLoanPrepayAmountProgressivePartialRepayment() {
        runAt("15 March 2025", () -> {
            final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(
                    create4IProgressive().interestRatePerPeriod(35.99).numberOfRepayments(12).isInterestRecalculationEnabled(true));
            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applyLP2ProgressiveLoanRequest(clientId,
                    loanProductsResponse.getResourceId(), "15 March 2025", 296.79, 35.99, 12, null));
            loanId = postLoansResponse.getLoanId();
            loanTransactionHelper.approveLoan(loanId, approveLoanRequest(296.79, "15 March 2025"));
            disburseLoan(loanId, BigDecimal.valueOf(296.79), "15 March 2025");
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));
        });
        runAt("16 March 2025", () -> {
            loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "16 March 2025", 59.0);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            Assertions.assertEquals(BigDecimal.ZERO, loanDetails.getSummary().getTotalUnpaidPayableDueInterest().stripTrailingZeros());
            Assertions.assertEquals(BigDecimal.ZERO, loanDetails.getSummary().getTotalUnpaidPayableNotDueInterest().stripTrailingZeros());
            GetLoansLoanIdTransactionsTemplateResponse prepayAmountResponse = loanTransactionHelper.getPrepaymentAmount(loanId,
                    "16 March 2025", DATETIME_PATTERN);
            Assertions.assertEquals(BigDecimal.valueOf(prepayAmountResponse.getInterestPortion()).stripTrailingZeros(),
                    loanDetails.getSummary().getTotalUnpaidPayableNotDueInterest());
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));
        });
        for (int i = 0; i <= 45; i++) {
            LocalDate date = LocalDate.of(2025, 3, 17).plusDays(i);
            String formattedDate = DateTimeFormatter.ofPattern(DATETIME_PATTERN).format(date);
            runAt(formattedDate, () -> {
                GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
                GetLoansLoanIdTransactionsTemplateResponse prepayAmountResponse = loanTransactionHelper.getPrepaymentAmount(loanId,
                        formattedDate, DATETIME_PATTERN);
                Assertions.assertEquals(loanDetails.getSummary().getTotalUnpaidPayableNotDueInterest().stripTrailingZeros(),
                        BigDecimal.valueOf(prepayAmountResponse.getInterestPortion()).stripTrailingZeros());
                inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));
            });
        }
    }

    @Test
    public void testLoanPrepayAmountProgressivePartialRepaymentNoInterestRecalculation() {
        runAt("15 March 2025", () -> {
            final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(
                    create4IProgressive().interestRatePerPeriod(35.99).numberOfRepayments(12).isInterestRecalculationEnabled(false));
            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applyLP2ProgressiveLoanRequest(clientId,
                    loanProductsResponse.getResourceId(), "15 March 2025", 296.79, 35.99, 12, null));
            loanId = postLoansResponse.getLoanId();
            loanTransactionHelper.approveLoan(loanId, approveLoanRequest(296.79, "15 March 2025"));
            disburseLoan(loanId, BigDecimal.valueOf(296.79), "15 March 2025");
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));
        });
        runAt("16 March 2025", () -> {
            loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "16 March 2025", 59.0);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            Assertions.assertEquals(BigDecimal.ZERO, loanDetails.getSummary().getTotalUnpaidPayableDueInterest().stripTrailingZeros());
            Assertions.assertEquals(BigDecimal.ZERO, loanDetails.getSummary().getTotalUnpaidPayableNotDueInterest().stripTrailingZeros());
            GetLoansLoanIdTransactionsTemplateResponse prepayAmountResponse = loanTransactionHelper.getPrepaymentAmount(loanId,
                    "16 March 2025", DATETIME_PATTERN);
            Assertions.assertEquals(BigDecimal.valueOf(44.43).stripTrailingZeros(),
                    BigDecimal.valueOf(prepayAmountResponse.getInterestPortion()).stripTrailingZeros());
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));
        });
    }
}
