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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PutLoansLoanIdResponse;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanAccountFraudTest {

    private ResponseSpecification responseSpec;
    private ResponseSpecification responseSpecError;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private final String amountVal = "1000";
    private LocalDate todaysDate;
    private String operationDate;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpecError = new ResponseSpecBuilder().expectStatusCode(403).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        this.todaysDate = Utils.getLocalDateOfTenant();
        this.operationDate = Utils.dateFormatter.format(this.todaysDate);
    }

    @Test
    public void testMarkLoanAsFraud() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, todaysDate);
            final String command = "markAsFraud";
            // Client and Loan account creation
            final Integer loanId = createAccounts(15, 1);

            GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);

            // Default values Not Null and False
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.FALSE, getLoansLoanIdResponse.getFraud());

            String payload = loanTransactionHelper.getLoanFraudPayloadAsJSON("fraud", "true");
            // Send the request, not expecting any errors (because only open loan restriction removed)
            PutLoansLoanIdResponse putLoansLoanIdResponse = loanTransactionHelper.modifyLoanCommand(loanId, command, payload,
                    this.responseSpecError);

            String statusCode = getLoansLoanIdResponse.getStatus().getCode();
            log.info("Loan with Id {} is with Status {}", getLoansLoanIdResponse.getId(), statusCode);

            // Approve the Loan active
            loanTransactionHelper.approveLoan(operationDate, this.amountVal, loanId, null);
            putLoansLoanIdResponse = loanTransactionHelper.modifyLoanCommand(loanId, command, payload, this.responseSpecError);

            // Default values Not Null and False
            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.FALSE, getLoansLoanIdResponse.getFraud());
            statusCode = getLoansLoanIdResponse.getStatus().getCode();
            log.info("Loan with Id {} is with Status {}", getLoansLoanIdResponse.getId(), statusCode);

            loanTransactionHelper.disburseLoanWithNetDisbursalAmount(operationDate, loanId, this.amountVal);

            // Mark On the Fraud
            putLoansLoanIdResponse = loanTransactionHelper.modifyLoanCommand(loanId, command, payload, this.responseSpec);
            assertNotNull(putLoansLoanIdResponse);

            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.TRUE, getLoansLoanIdResponse.getFraud());

            // Mark Off the Fraud
            payload = loanTransactionHelper.getLoanFraudPayloadAsJSON("fraud", "false");
            putLoansLoanIdResponse = loanTransactionHelper.modifyLoanCommand(loanId, command, payload, this.responseSpec);
            assertNotNull(putLoansLoanIdResponse);

            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);
            assertNotNull(getLoansLoanIdResponse.getFraud());
            assertEquals(Boolean.FALSE, getLoansLoanIdResponse.getFraud());
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    private Integer createAccounts(final Integer daysToSubtract, final Integer numberOfRepayments) {
        // Client and Loan account creation
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper, null);

        // Older date to have more than one overdue installment
        final LocalDate transactionDate = this.todaysDate.minusDays(daysToSubtract + (30 * (numberOfRepayments - 1)));
        String operationDate = Utils.dateFormatter.format(transactionDate);

        return createLoanAccount(loanTransactionHelper, clientId.toString(), getLoanProductsProductResponse.getId().toString(),
                operationDate, amountVal, numberOfRepayments.toString());
    }

    private GetLoanProductsProductIdResponse createLoanProduct(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId) {
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().build(null, delinquencyBucketId);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private Integer createLoanAccount(final LoanTransactionHelper loanTransactionHelper, final String clientId, final String loanProductId,
            final String operationDate, final String principalAmount, final String numberOfRepayments) {
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(principalAmount)
                .withLoanTermFrequency(numberOfRepayments).withLoanTermFrequencyAsMonths().withNumberOfRepayments(numberOfRepayments)
                .withRepaymentEveryAfter("1").withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("0") //
                .withExpectedDisbursementDate(operationDate) //
                .withInterestTypeAsDecliningBalance() //
                .withSubmittedOnDate(operationDate) //
                .build(clientId, loanProductId, null);
        return loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

}
