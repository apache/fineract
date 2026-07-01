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

import java.util.UUID;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.junit.jupiter.api.Test;

public class LoanAccountCreditRefundPayoutWithChargebackTest extends FeignLoanTestBase {

    @Test
    public void loanCreditRefundPayoutGetCreatedWithChargebackTest() {
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();

        final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
        final Long loanProductId = createLoanProduct(delinquencyBucketId);

        final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

        final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("5 September 2022")
                        .locale(LoanTestData.LOCALE).transactionAmount(1000.0));

        PostLoansLoanIdTransactionsResponse chargebackResponse = chargebackLoanTransaction(loanExternalIdStr,
                repaymentTransaction_1.getResourceId(), new PostLoansLoanIdTransactionsTransactionIdRequest().locale(LoanTestData.LOCALE)
                        .transactionAmount(1000.0).paymentTypeId(1L));

        GetLoansLoanIdTransactionsTransactionIdResponse chargebackTransactionResponse = getLoanTransactionDetails(
                chargebackResponse.getLoanId(), chargebackResponse.getResourceId());
        assertEquals(1L, chargebackTransactionResponse.getPaymentDetailData().getPaymentType().getId());

        final PostLoansLoanIdTransactionsResponse goodwillCredit_1 = makeGoodwillCredit(loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("8 September 2022")
                        .locale(LoanTestData.LOCALE).transactionAmount(1000.0));
        assertNotNull(goodwillCredit_1);
        assertEquals(goodwillCredit_1.getLoanId(), loanId);

        final PostLoansLoanIdTransactionsResponse goodwillCredit_2 = makeGoodwillCredit(loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("9 September 2022")
                        .locale(LoanTestData.LOCALE).transactionAmount(10.0));
        assertNotNull(goodwillCredit_2);
        assertEquals(goodwillCredit_2.getLoanId(), loanId);

        final PostLoansLoanIdTransactionsResponse payoutRefund_1 = makePayoutRefund(loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("9 September 2022")
                        .locale(LoanTestData.LOCALE).transactionAmount(10.0));
        assertNotNull(payoutRefund_1);
        assertEquals(payoutRefund_1.getLoanId(), loanId);

        final PostLoansLoanIdTransactionsResponse merchantIssuedRefund_1 = makeMerchantIssuedRefund(loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("10 September 2022")
                        .locale(LoanTestData.LOCALE).transactionAmount(10.0));
        assertNotNull(merchantIssuedRefund_1);
        assertEquals(merchantIssuedRefund_1.getLoanId(), loanId);
    }

    private Long createLoanProduct(final Long delinquencyBucketId) {
        return createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct().principal(1000.0).numberOfRepayments(1)
                .repaymentEvery(1).repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L)
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.MONTHS)
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL).interestType(LoanTestData.InterestType.FLAT)
                .daysInMonthType(LoanTestData.DaysInMonthType.DAYS_30).daysInYearType(LoanTestData.DaysInYearType.DAYS_365)
                .delinquencyBucketId(delinquencyBucketId));
    }

    private Long createLoanAccount(final Long clientId, final Long loanProductId, final String externalId) {
        PostLoansRequest request = applyLoanRequest(clientId, loanProductId, "01 September 2022", 1000.0, 1,
                req -> req.externalId(externalId).expectedDisbursementDate("03 September 2022").repaymentEvery(1)
                        .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS).loanTermFrequency(1)
                        .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)
                        .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL).interestType(LoanTestData.InterestType.FLAT));

        Long loanId = applyForLoan(request);
        approveLoan(loanId, approveLoanRequest(1000.0, "02 September 2022", "03 September 2022"));
        disburseLoan(loanId, "03 September 2022", 1000.0);
        return loanId;
    }
}
