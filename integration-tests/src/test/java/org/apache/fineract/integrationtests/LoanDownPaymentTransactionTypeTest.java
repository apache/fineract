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
import java.util.HashMap;
import java.util.UUID;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.junit.jupiter.api.Test;

public class LoanDownPaymentTransactionTypeTest extends FeignLoanTestBase {

    @Test
    public void loanDownPaymentTransactionTypeTest() {

        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();

        String loanExternalIdStr = UUID.randomUUID().toString();

        final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();

        final Long clientId = createClient();

        GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(delinquencyBucketId);
        assertNotNull(getLoanProductsProductResponse);

        final Long loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), loanExternalIdStr);

        final PostLoansLoanIdTransactionsResponse downPaymentTransaction_1 = makeLoanDownPayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("5 September 2022").locale("en")
                        .transactionAmount(100.0));
        assertNotNull(downPaymentTransaction_1);

        GetLoansLoanIdTransactionsTransactionIdResponse loanDownPaymentTransaction = getLoanTransaction(loanId,
                downPaymentTransaction_1.getResourceId());

        assertNotNull(loanDownPaymentTransaction);
        assertEquals(loanDownPaymentTransaction.getAmount(), 100.0);
        assertEquals(loanDownPaymentTransaction.getPrincipalPortion(), 100.0);
        assertEquals("loanTransactionType.downPayment", loanDownPaymentTransaction.getType().getCode());

        LocalDate adjustmentDate = LocalDate.of(2022, 9, 7);
        String formattedDate = dateFormatter.format(adjustmentDate);
        PostLoansLoanIdTransactionsResponse adjustmentResult = reverseLoanTransaction(loanExternalIdStr, loanDownPaymentTransaction.getId(),
                new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate(formattedDate).locale("en").dateFormat("dd MMMM yyyy")
                        .transactionAmount(0.0));

        assertNotNull(adjustmentResult);
        assertEquals(loanDownPaymentTransaction.getId(), adjustmentResult.getResourceId());

        String downPaymentExternalIdStr = UUID.randomUUID().toString();

        final PostLoansLoanIdTransactionsResponse downPaymentTransaction_2 = makeLoanDownPayment(loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("9 September 2022").locale("en")
                        .transactionAmount(200.0).externalId(downPaymentExternalIdStr));
        assertNotNull(downPaymentTransaction_2);
        assertEquals(downPaymentExternalIdStr, downPaymentTransaction_2.getResourceExternalId());

        GetLoansLoanIdTransactionsTransactionIdResponse loanDownPaymentTransaction_1 = getLoanTransaction(loanId,
                downPaymentTransaction_2.getResourceId());

        assertNotNull(loanDownPaymentTransaction_1);
        assertEquals(loanDownPaymentTransaction_1.getAmount(), 200.0);
        assertEquals(loanDownPaymentTransaction_1.getPrincipalPortion(), 200.0);
        assertEquals("loanTransactionType.downPayment", loanDownPaymentTransaction_1.getType().getCode());

        adjustmentDate = LocalDate.of(2022, 9, 12);
        formattedDate = dateFormatter.format(adjustmentDate);
        PostLoansLoanIdTransactionsResponse adjustmentResult_1 = reverseLoanTransaction(loanExternalIdStr, downPaymentExternalIdStr,
                new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate(formattedDate).locale("en").dateFormat("dd MMMM yyyy")
                        .transactionAmount(0.0));

        assertNotNull(adjustmentResult_1);
        assertEquals(loanDownPaymentTransaction_1.getId(), adjustmentResult_1.getResourceId());
        assertEquals(downPaymentExternalIdStr, adjustmentResult_1.getResourceExternalId());
    }

    private GetLoanProductsProductIdResponse createLoanProduct(final Long delinquencyBucketId) {
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().build(null, delinquencyBucketId);
        final Long loanProductId = createLoanProductFromJson(Utils.convertToJson(loanProductMap));
        return retrieveLoanProduct(loanProductId);
    }

    private Long createLoanAccount(final Long clientId, final Long loanProductId, final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("1")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("1").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("03 September 2022").withSubmittedOnDate("01 September 2022").withLoanType("individual")
                .withExternalId(externalId).build(clientId.toString(), loanProductId.toString(), null);

        final Long loanId = applyForLoanFromJson(loanApplicationJSON);
        approveLoan(loanId, approveLoanRequest(1000.0, "02 September 2022"));
        disburseLoanWithNetDisbursalAmount(loanId, "03 September 2022", "1000");
        return loanId;
    }

}
