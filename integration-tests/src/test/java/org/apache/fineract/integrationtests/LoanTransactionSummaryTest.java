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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.UUID;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdSummary;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDelinquencyHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

public class LoanTransactionSummaryTest extends FeignLoanTestBase {

    private final FeignDelinquencyHelper delinquencyHelper = new FeignDelinquencyHelper(FineractFeignClientHelper.getFineractFeignClient());

    private final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();

    @Test
    public void loanTransactionSummaryTest() {
        final String loanExternalIdStr = UUID.randomUUID().toString();
        final Long loanId = createClientWithLoan(loanExternalIdStr);

        makeLoanRepayment(loanExternalIdStr, transactionOf("5 September 2022", 100.0));
        makeLoanRepayment(loanExternalIdStr, transactionOf("6 September 2022", 100.0));
        final PostLoansLoanIdTransactionsResponse repaymentTransaction3 = makeLoanRepayment(loanExternalIdStr,
                transactionOf("7 September 2022", 50.0));
        reverseRepayment(loanId, repaymentTransaction3.getResourceId(), "7 September 2022");

        makeMerchantIssuedRefund(loanId, transactionOf("8 September 2022", 100.0));
        final PostLoansLoanIdTransactionsResponse merchantIssuedRefund2 = makeMerchantIssuedRefund(loanId,
                transactionOf("8 September 2022", 50.0));
        reverseRepayment(loanId, merchantIssuedRefund2.getResourceId(), "8 September 2022");

        makePayoutRefund(loanId, transactionOf("9 September 2022", 100.0));
        final PostLoansLoanIdTransactionsResponse payoutRefund2 = makePayoutRefund(loanId, transactionOf("9 September 2022", 50.0));
        reverseRepayment(loanId, payoutRefund2.getResourceId(), "9 September 2022");

        makeGoodwillCredit(loanId, transactionOf("10 September 2022", 100.0));
        final PostLoansLoanIdTransactionsResponse goodwillCredit2 = makeGoodwillCredit(loanId, transactionOf("10 September 2022", 50.0));
        reverseRepayment(loanId, goodwillCredit2.getResourceId(), "10 September 2022");

        final PostLoansLoanIdTransactionsResponse repaymentTransaction4 = makeLoanRepayment(loanExternalIdStr,
                transactionOf("11 September 2022", 150.0));
        chargebackLoanTransaction(loanExternalIdStr, repaymentTransaction4.getResourceId(),
                new PostLoansLoanIdTransactionsTransactionIdRequest().locale(LoanTestData.LOCALE).transactionAmount(50.0)
                        .paymentTypeId(1L));

        final Long penalty = chargesHelper.createLoanSpecifiedDueDatePenalty(10.0).getResourceId();
        final String penaltyChargeAddedDate = dateFormatter.format(LocalDate.of(2022, 9, 10));
        final Long penaltyLoanChargeId = addLoanCharge(loanId, penalty, penaltyChargeAddedDate, 10.0).getResourceId();

        chargeAdjustment(loanId, penaltyLoanChargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(10.0).locale(LoanTestData.LOCALE));

        final GetLoansLoanIdSummary loanSummary = getLoanDetails(loanId).getSummary();
        assertNotNull(loanSummary);

        assertEquals(350.00, Utils.getDoubleValue(loanSummary.getTotalRepaymentTransaction()));
        assertEquals(50.00, Utils.getDoubleValue(loanSummary.getTotalRepaymentTransactionReversed()));
        assertEquals(100.00, Utils.getDoubleValue(loanSummary.getTotalMerchantRefund()));
        assertEquals(50.00, Utils.getDoubleValue(loanSummary.getTotalMerchantRefundReversed()));
        assertEquals(100.00, Utils.getDoubleValue(loanSummary.getTotalPayoutRefund()));
        assertEquals(50.00, Utils.getDoubleValue(loanSummary.getTotalPayoutRefundReversed()));
        assertEquals(100.00, Utils.getDoubleValue(loanSummary.getTotalGoodwillCredit()));
        assertEquals(50.00, Utils.getDoubleValue(loanSummary.getTotalGoodwillCreditReversed()));
        assertEquals(10.00, Utils.getDoubleValue(loanSummary.getTotalChargeAdjustment()));
        assertEquals(50.00, Utils.getDoubleValue(loanSummary.getTotalChargeback()));
    }

    @Test
    public void lastRepaymentAmountTest() {
        final String loanExternalIdStr = UUID.randomUUID().toString();
        final Long loanId = createClientWithLoan(loanExternalIdStr);

        makeMerchantIssuedRefund(loanId, transactionOf("8 September 2022", 20.0));
        makeLoanRepayment(loanExternalIdStr, transactionOf("7 September 2022", 100.0));
        makeLoanRepayment(loanExternalIdStr, transactionOf("6 September 2022", 50.0));

        final GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

        assertEquals(20.0, Utils.getDoubleValue(loanDetails.getDelinquent().getLastPaymentAmount()));
        assertEquals(LocalDate.of(2022, 9, 8), loanDetails.getDelinquent().getLastPaymentDate());

        assertEquals(100.0, Utils.getDoubleValue(loanDetails.getDelinquent().getLastRepaymentAmount()));
        assertEquals(LocalDate.of(2022, 9, 7), loanDetails.getDelinquent().getLastRepaymentDate());
    }

    private Long createClientWithLoan(final String loanExternalIdStr) {
        final Long delinquencyBucketId = delinquencyHelper.createDefaultBucket();

        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder().buildRequest(null, delinquencyBucketId));
        assertNotNull(loanProductId);

        return createLoanAccount(clientId, loanProductId, loanExternalIdStr);
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

    private PostLoansLoanIdTransactionsRequest transactionOf(final String transactionDate, final Double amount) {
        return new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate(transactionDate)
                .locale(LoanTestData.LOCALE).transactionAmount(amount);
    }
}
