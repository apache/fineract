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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetPaymentTypesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.PaymentTypeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoanTransactionChargebackTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void applyLoanTransactionChargeback() {
        final LoanTransactionHelper loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        // Client and Loan account creation
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper, null);
        assertNotNull(getLoanProductsProductResponse);
        log.info("Loan Product Bucket Name: {}", getLoanProductsProductResponse.getDelinquencyBucket().getName());

        final LocalDate todaysDate = Utils.getLocalDateOfTenant();
        // Older date to have more than one overdue installment
        final LocalDate transactionDate = todaysDate.minusDays(45);
        String operationDate = Utils.dateFormatter.format(transactionDate);

        final Integer loanId = createLoanAccount(loanTransactionHelper, clientId.toString(),
                getLoanProductsProductResponse.getId().toString(), operationDate, "1000");

        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);

        loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

        operationDate = Utils.dateFormatter.format(todaysDate);
        Float amount = Float.valueOf("1100.0");
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanId);
        assertNotNull(loanIdTransactionsResponse);
        final Integer transactionId = loanIdTransactionsResponse.getResourceId();

        List<GetPaymentTypesResponse> paymentTypeList = PaymentTypeHelper.getSystemPaymentType(requestSpec, responseSpec);
        assertTrue(!paymentTypeList.isEmpty());

        amount = Float.valueOf("800.0");
        final String payload = createChargebackPayload(amount.toString(), paymentTypeList.get(0).getId());
        PostLoansLoanIdTransactionsTransactionIdRequest postLoansTransactionCommandRequest = loanTransactionHelper
                .applyLoanTransactionCommand(loanId, transactionId, "chargeback", payload);
        assertNotNull(postLoansTransactionCommandRequest);

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
    }

    private String createChargebackPayload(final String transactionAmount, final Integer paymentTypeId) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("transactionAmount", transactionAmount);
        map.put("paymentTypeId", paymentTypeId);
        map.put("locale", CommonConstants.LOCALE);
        final String chargebackPayload = new Gson().toJson(map);
        log.info("{}", chargebackPayload);
        return chargebackPayload;
    }

    private GetLoanProductsProductIdResponse createLoanProduct(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId) {
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().build(null, delinquencyBucketId);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private Integer createLoanAccount(final LoanTransactionHelper loanTransactionHelper, final String clientId, final String loanProductId,
            final String operationDate, final String principalAmount) {
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(principalAmount).withLoanTermFrequency("12")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("12").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withExpectedDisbursementDate(operationDate) //
                .withInterestTypeAsDecliningBalance() //
                .withSubmittedOnDate(operationDate) //
                .build(clientId, loanProductId, null);
        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan(operationDate, principalAmount, loanId, null);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount(operationDate, loanId, principalAmount);
        return loanId;
    }
}
