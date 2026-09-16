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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class BlockTransactionsOnClosedOverpaidLoansTest extends FeignLoanTestBase {

    private static final String BLOCK_TRANSACTIONS_CONFIGURATION = "block-transactions-on-closed-overpaid-loans";
    private static final String BLOCKED_ERROR = "error.msg.loan.transaction.not.allowed.on.closed.or.overpaid";

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.manageConfigurations(BLOCK_TRANSACTIONS_CONFIGURATION, false);
    }

    @Test
    public void testTransactionsOnOverpaidLoan() {
        globalConfigurationHelper.manageConfigurations(BLOCK_TRANSACTIONS_CONFIGURATION, true);

        final Long clientId = createClient();
        assertEquals(clientId, clientHelper.getClient(clientId).getId(), "ERROR IN CREATING THE CLIENT");

        final Long loanProductId = createLoanProduct();
        final Long loanId = createLoanApplication(clientId, loanProductId, 1000.0, "01 January 2024");

        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "01 January 2024"));
        disburseLoan(loanId, "01 January 2024", 1000.0);

        makeLoanRepayment(loanId, repaymentOf("01 February 2024", 2000.0));
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getOverpaid);

        assertBlockedForClosedOrOverpaid("repayment", loanId, "02 February 2024", 10.0);
        assertBlockedForClosedOrOverpaid("goodwillCredit", loanId, "02 February 2024", 10.0);
        assertBlockedForClosedOrOverpaid("merchantIssuedRefund", loanId, "02 February 2024", 10.0);
        assertBlockedForClosedOrOverpaid("payoutRefund", loanId, "02 February 2024", 10.0);
        assertBlockedForClosedOrOverpaid("waiveinterest", loanId, "02 February 2024", 10.0);

        final BigDecimal totalOverpaid = getLoanDetails(loanId).getTotalOverpaid();
        assertNotNull(totalOverpaid);
        assertTrue(totalOverpaid.compareTo(BigDecimal.ZERO) > 0);

        makeCreditBalanceRefund(loanId, repaymentOf("03 February 2024", totalOverpaid.doubleValue()));

        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getClosed);
    }

    @Test
    public void testTransactionsOnClosedLoan() {
        globalConfigurationHelper.manageConfigurations(BLOCK_TRANSACTIONS_CONFIGURATION, true);

        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct();
        final Long loanId = createLoanApplication(clientId, loanProductId, 1000.0, "01 January 2024");

        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "01 January 2024"));
        disburseLoan(loanId, "01 January 2024", 1000.0);

        final Double totalOutstanding = getLoanDetails(loanId).getSummary().getTotalOutstanding().doubleValue();
        final PostLoansLoanIdTransactionsResponse repayment = makeLoanRepayment(loanId, repaymentOf("01 February 2024", totalOutstanding));

        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getClosed);

        assertBlockedForClosedOrOverpaid("repayment", loanId, "02 February 2024", 10.0);

        reverseRepayment(loanId, repayment.getResourceId(), "03 February 2024");

        globalConfigurationHelper.manageConfigurations(BLOCK_TRANSACTIONS_CONFIGURATION, false);
        makeLoanRepayment(loanId, repaymentOf("04 February 2024", 10.0));
    }

    private void assertBlockedForClosedOrOverpaid(final String command, final Long loanId, final String date, final Double amount) {
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> makeLoanRepayment(loanId, command, date, amount));
        assertEquals(403, exception.getStatus());
        assertErrorGlobalisationCode(exception, BLOCKED_ERROR);
    }

    private Long createLoanProduct() {
        return createLoanProduct(new LoanProductTestBuilder() //
                .withPrincipal("1000.00") //
                .withShortName(Utils.uniqueRandomStringGenerator("", 4)) //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .buildRequest(null));
    }

    private Long createLoanApplication(final Long clientId, final Long loanProductId, final Double principal, final String submitDate) {
        final PostLoansRequest application = LoanRequestBuilders.applyLoan(clientId, loanProductId, submitDate, principal, 4)//
                .interestRatePerPeriod(BigDecimal.ONE);
        return loanHelper.applyForLoan(application).getLoanId();
    }

    private PostLoansLoanIdTransactionsRequest repaymentOf(final String transactionDate, final Double amount) {
        return new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate(transactionDate)
                .locale(LoanTestData.LOCALE).transactionAmount(amount);
    }
}
