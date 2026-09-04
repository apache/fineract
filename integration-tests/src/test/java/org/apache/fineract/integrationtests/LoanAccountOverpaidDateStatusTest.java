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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.UUID;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDelinquencyHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

public class LoanAccountOverpaidDateStatusTest extends FeignLoanTestBase {

    private final FeignDelinquencyHelper delinquencyHelper = new FeignDelinquencyHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void loanOverpaidDateStatusTest() {
        try {
            final LocalDate todaysDate = Utils.getLocalDateOfTenant();

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate(todaysDate.toString());

            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long delinquencyBucketId = delinquencyHelper.createDefaultBucket();

            // Client and Loan account creation
            final Long clientId = createClient();
            final Long loanProductId = createLoanProduct(new LoanProductTestBuilder().buildRequest(null, delinquencyBucketId));
            assertNotNull(loanProductId);

            final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

            // make Repayments
            makeLoanRepayment(loanExternalIdStr, repaymentOf("5 September 2022", 200.0));
            final PostLoansLoanIdTransactionsResponse repaymentTransaction2 = makeLoanRepayment(loanExternalIdStr,
                    repaymentOf("6 September 2022", 200.0));
            makeLoanRepayment(loanExternalIdStr, repaymentOf("7 September 2022", 500.0));

            // make repayment to make loan overpaid
            final PostLoansLoanIdTransactionsResponse repaymentTransaction4 = makeLoanRepayment(loanExternalIdStr,
                    repaymentOf("9 September 2022", 200.0));

            // check loan overpaid date is not null and is set as Business date and loan status
            GetLoansLoanIdResponse loanDetailsOverpaid = getLoanDetails(loanId);
            assertTrue(loanDetailsOverpaid.getStatus().getOverpaid());
            assertNotNull(loanDetailsOverpaid.getOverpaidOnDate());
            assertEquals(loanDetailsOverpaid.getOverpaidOnDate(), LocalDate.of(2022, 9, 9));

            // reverse repayment to make loan not overpaid and overpaid date is reset
            reverseRepayment(loanId, repaymentTransaction4.getResourceId(), "10 September 2022");
            GetLoansLoanIdResponse loanDetailsNotOverpaidAfterReversal = getLoanDetails(loanId);
            assertFalse(loanDetailsNotOverpaidAfterReversal.getStatus().getOverpaid());
            assertNull(loanDetailsNotOverpaidAfterReversal.getOverpaidOnDate());

            // make repayment to make loan overpaid again
            makeLoanRepayment(loanExternalIdStr, repaymentOf("11 September 2022", 200.0));

            // check loan overpaid date is not null and is set as Business date and loan status
            GetLoansLoanIdResponse loanDetailsOverpaid1 = getLoanDetails(loanId);
            assertTrue(loanDetailsOverpaid1.getStatus().getOverpaid());
            assertNotNull(loanDetailsOverpaid1.getOverpaidOnDate());
            assertEquals(loanDetailsOverpaid1.getOverpaidOnDate(), LocalDate.of(2022, 9, 11));

            // Credit balance refund to reset overpaid status
            makeCreditBalanceRefund(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN)
                    .transactionDate("12 September 2022").locale(LoanTestData.LOCALE).transactionAmount(100.0));
            GetLoansLoanIdResponse loanDetailsNotOverpaidAfterCBR = getLoanDetails(loanId);
            assertFalse(loanDetailsNotOverpaidAfterCBR.getStatus().getOverpaid());
            assertNull(loanDetailsNotOverpaidAfterCBR.getOverpaidOnDate());

            // reverse repayment to make loan active again
            reverseRepayment(loanId, repaymentTransaction2.getResourceId(), "13 September 2022");
            GetLoansLoanIdResponse loanDetailsNotOverpaidAfterReversal1 = getLoanDetails(loanId);
            assertFalse(loanDetailsNotOverpaidAfterReversal1.getStatus().getOverpaid());
            assertNull(loanDetailsNotOverpaidAfterReversal1.getOverpaidOnDate());

            // make repayment to make loan overpaid again
            makeLoanRepayment(loanExternalIdStr, repaymentOf("14 September 2022", 300.0));

            // check loan overpaid date is not null and is set as Business date and loan status
            GetLoansLoanIdResponse loanDetailsOverpaid3 = getLoanDetails(loanId);
            assertTrue(loanDetailsOverpaid3.getStatus().getOverpaid());
            assertNotNull(loanDetailsOverpaid3.getOverpaidOnDate());
            assertEquals(loanDetailsOverpaid3.getOverpaidOnDate(), LocalDate.of(2022, 9, 14));
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    private PostLoansLoanIdTransactionsRequest repaymentOf(final String transactionDate, final Double amount) {
        return new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate(transactionDate)
                .locale(LoanTestData.LOCALE).transactionAmount(amount);
    }

    private Long createLoanAccount(final Long clientId, final Long loanProductId, final String externalId) {
        final PostLoansRequest application = LoanRequestBuilders.applyLoan(clientId, loanProductId, "01 September 2022", 1000.0, 1)//
                .expectedDisbursementDate("03 September 2022")//
                .interestType(LoanTestData.InterestType.FLAT)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .externalId(externalId);

        final Long loanId = loanHelper.applyForLoan(application).getLoanId();
        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "02 September 2022"));
        disburseLoan(loanId, "03 September 2022", 1000.0);
        return loanId;
    }
}
