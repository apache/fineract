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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;
import org.apache.fineract.client.models.GetDelinquencyBucketsResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoanAccountOverpaidDateStatusTest extends BaseLoanIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;
    private LoanTransactionHelper loanTransactionHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void loanOverpaidDateStatusTest() {
        // Set business date
        try {
            final LocalDate todaysDate = Utils.getLocalDateOfTenant();

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, todaysDate);

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Delinquency Bucket
            final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
            final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                    delinquencyBucketId);

            // Client and Loan account creation

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper,
                    delinquencyBucketId);
            assertNotNull(getLoanProductsProductResponse);

            final Integer loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), loanExternalIdStr);

            // make Repayments
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("5 September 2022").locale("en")
                            .transactionAmount(200.0));

            final PostLoansLoanIdTransactionsResponse repaymentTransaction_2 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("6 September 2022").locale("en")
                            .transactionAmount(200.0));

            final PostLoansLoanIdTransactionsResponse repaymentTransaction_3 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 September 2022").locale("en")
                            .transactionAmount(500.0));

            // make repayment to make loan overpaid
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_4 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("9 September 2022").locale("en")
                            .transactionAmount(200.0));

            // check loan overpaid date is not null and is set as Business date and loan status
            GetLoansLoanIdResponse loanDetailsOverpaid = loanTransactionHelper.getLoanDetails((long) loanId);
            assertTrue(loanDetailsOverpaid.getStatus().getOverpaid());
            assertNotNull(loanDetailsOverpaid.getOverpaidOnDate());
            assertEquals(loanDetailsOverpaid.getOverpaidOnDate(), LocalDate.of(2022, 9, 9));

            // reverse repayment to make loan not overpaid and overpaid date is reset
            loanTransactionHelper.reverseRepayment(loanId, repaymentTransaction_4.getResourceId().intValue(), "10 September 2022");
            GetLoansLoanIdResponse loanDetailsNotOverpaidAfterReversal = loanTransactionHelper.getLoanDetails((long) loanId);
            assertFalse(loanDetailsNotOverpaidAfterReversal.getStatus().getOverpaid());
            assertNull(loanDetailsNotOverpaidAfterReversal.getOverpaidOnDate());

            // make repayment to make loan overpaid again
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_5 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("11 September 2022").locale("en")
                            .transactionAmount(200.0));

            // check loan overpaid date is not null and is set as Business date and loan status
            GetLoansLoanIdResponse loanDetailsOverpaid_1 = loanTransactionHelper.getLoanDetails((long) loanId);
            assertTrue(loanDetailsOverpaid_1.getStatus().getOverpaid());
            assertNotNull(loanDetailsOverpaid_1.getOverpaidOnDate());
            assertEquals(loanDetailsOverpaid_1.getOverpaidOnDate(), LocalDate.of(2022, 9, 11));

            // Credit balance refund to reset overpaid status
            loanTransactionHelper.creditBalanceRefund("12 September 2022", Float.valueOf(100), null, loanId, "");
            GetLoansLoanIdResponse loanDetailsNotOverpaidAfterCBR = loanTransactionHelper.getLoanDetails((long) loanId);
            assertFalse(loanDetailsNotOverpaidAfterCBR.getStatus().getOverpaid());
            assertNull(loanDetailsNotOverpaidAfterCBR.getOverpaidOnDate());

            // reverse repayment to make loan active again
            loanTransactionHelper.reverseRepayment(loanId, repaymentTransaction_2.getResourceId().intValue(), "13 September 2022");
            GetLoansLoanIdResponse loanDetailsNotOverpaidAfterReversal_1 = loanTransactionHelper.getLoanDetails((long) loanId);
            assertFalse(loanDetailsNotOverpaidAfterReversal_1.getStatus().getOverpaid());
            assertNull(loanDetailsNotOverpaidAfterReversal_1.getOverpaidOnDate());

            // make repayment to make loan overpaid again
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_6 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("14 September 2022").locale("en")
                            .transactionAmount(300.0));

            // check loan overpaid date is not null and is set as Business date and loan status
            GetLoansLoanIdResponse loanDetailsOverpaid_3 = loanTransactionHelper.getLoanDetails((long) loanId);
            assertTrue(loanDetailsOverpaid_3.getStatus().getOverpaid());
            assertNotNull(loanDetailsOverpaid_3.getOverpaidOnDate());
            assertEquals(loanDetailsOverpaid_3.getOverpaidOnDate(), LocalDate.of(2022, 9, 14));
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }

    }

    private GetLoanProductsProductIdResponse createLoanProduct(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId) {
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().build(null, delinquencyBucketId);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private Integer createLoanAccount(final Integer clientID, final Long loanProductID, final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("1")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("1").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("03 September 2022").withSubmittedOnDate("01 September 2022").withLoanType("individual")
                .withExternalId(externalId).build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("02 September 2022", "1000", loanId, null);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount("03 September 2022", loanId, "1000");
        return loanId;
    }
}
