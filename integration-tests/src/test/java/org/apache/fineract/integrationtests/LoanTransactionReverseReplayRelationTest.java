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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.GetLoanTransactionRelation;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoanTransactionReverseReplayRelationTest extends FeignLoanTestBase {

    private static Long clientId;

    @BeforeEach
    public void setup() {
        clientId = createClient("01 September 2022");
    }

    @Test
    public void loanTransactionReverseReplayRelationTest() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        // Delinquency Bucket
        final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();

        // Client and Loan account creation
        final Long productId = createLoanProduct(new LoanProductTestBuilder().buildRequest(null, delinquencyBucketId));
        assertNotNull(productId);

        final Long loanId = applyForLoan(new PostLoansRequest()//
                .clientId(clientId)//
                .productId(productId)//
                .principal(new BigDecimal("1000"))//
                .loanTermFrequency(1)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(1)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(BigDecimal.ZERO)//
                .interestType(LoanTestData.InterestType.FLAT)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .transactionProcessingStrategyCode(LoanTestData.TransactionProcessingStrategyCode.MIFOS_STANDARD_STRATEGY)//
                .expectedDisbursementDate("03 September 2022")//
                .submittedOnDate("01 September 2022")//
                .loanType("individual")//
                .externalId(loanExternalIdStr)//
                .maxOutstandingLoanBalance(new BigDecimal("36000"))//
                .collateral(List.of())//
                .locale("en_GB")//
                .dateFormat(LoanTestData.DATETIME_PATTERN));
        approveLoan(loanId, approveLoanRequest(1000.0, "02 September 2022"));
        disburseLoanWithNetDisbursalAmount(loanId, "03 September 2022", "1000");

        // Add Charge
        Long penaltyChargeId = chargesHelper.createLoanSpecifiedDueDatePenalty(10.0).getResourceId();

        addLoanCharge(loanId, penaltyChargeId, "07 September 2022", 10.0);

        // make repayment — Set Loan transaction externalId for transaction getting reversed and replayed
        String loanTransactionExternalIdStr = UUID.randomUUID().toString();

        final PostLoansLoanIdTransactionsResponse repaymentTransaction = makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("15 September 2022").locale("en")
                        .transactionAmount(11.0).externalId(loanTransactionExternalIdStr));

        // make backdated repayment for reverse replay
        final PostLoansLoanIdTransactionsResponse backDatedRepaymentTransaction = makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("10 September 2022").locale("en")
                        .transactionAmount(5.0));

        // get transaction relationship for new transaction using externalId of reversed loan transaction
        Long reversedAndReplayedTransactionId = repaymentTransaction.getResourceId();

        GetLoansLoanIdTransactionsTransactionIdResponse getLoansTransactionResponse = getLoanTransactionDetails(loanId,
                loanTransactionExternalIdStr);
        assertNotNull(getLoansTransactionResponse);
        assertNotNull(getLoansTransactionResponse.getTransactionRelations());

        // test replayed relationship
        GetLoanTransactionRelation transactionRelation = getLoansTransactionResponse.getTransactionRelations().iterator().next();
        assertEquals(reversedAndReplayedTransactionId, transactionRelation.getToLoanTransaction());
        assertEquals("REPLAYED", transactionRelation.getRelationType());
    }
}
