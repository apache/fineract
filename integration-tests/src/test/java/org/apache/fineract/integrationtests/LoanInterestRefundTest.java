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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.integrationtests.common.BusinessStepHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoanInterestRefundTest extends BaseLoanIntegrationTest {

    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;
    private static LoanTransactionHelper loanTransactionHelper;
    private static PostClientsResponse client;
    private static BusinessStepHelper businessStepHelper;

    @BeforeAll
    public static void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        ClientHelper clientHelper = new ClientHelper(requestSpec, responseSpec);
        client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
    }

    @Test
    public void verifyInterestRefundNotCreatedForPayoutRefundWhenTypesAreEmpty() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .supportedInterestRefundTypes(new ArrayList<>()) //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.9,
                    12, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000), "1 January 2021");
        });
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("PayoutRefund", "22 January 2021", 1000F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"),
                    transaction(1000.0, "Payout Refund", "22 January 2021"));
        });
    }

    @Test
    public void verifyInterestRefundNotCreatedForMerchantIssuedRefundWhenTypesAreEmpty() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .supportedInterestRefundTypes(new ArrayList<>())//
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.9,
                    12, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000), "1 January 2021");
        });
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("MerchantIssuedRefund", "22 January 2021", 1000F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"),
                    transaction(1000.0, "Merchant Issued Refund", "22 January 2021"));
        });
    }

    @Test
    public void verifyInterestRefundCreatedForPayoutRefund() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .supportedInterestRefundTypes(new ArrayList<>()).addSupportedInterestRefundTypesItem("PAYOUT_REFUND") //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    12, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000), "1 January 2021");
        });
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("PayoutRefund", "22 January 2021", 1000F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"),
                    transaction(1000.0, "Payout Refund", "22 January 2021"), transaction(5.75, "Accrual", "22 January 2021"),
                    transaction(5.75, "Interest Refund", "22 January 2021"));
        });
    }

    @Test
    public void verifyInterestRefundCreatedForMerchantIssuedRefund() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .supportedInterestRefundTypes(new ArrayList<>()).addSupportedInterestRefundTypesItem("MERCHANT_ISSUED_REFUND") //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    12, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000), "1 January 2021");
        });
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("MerchantIssuedRefund", "22 January 2021", 1000F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"),
                    transaction(1000.0, "Merchant Issued Refund", "22 January 2021"), transaction(5.75, "Accrual", "22 January 2021"),
                    transaction(5.75, "Interest Refund", "22 January 2021"));
        });
    }

    @Test
    public void verifyUC01() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .supportedInterestRefundTypes(new ArrayList<>()).addSupportedInterestRefundTypesItem("PAYOUT_REFUND") //
                            .addSupportedInterestRefundTypesItem("MERCHANT_ISSUED_REFUND") //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    12, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000), "1 January 2021");
        });
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("PayoutRefund", "22 January 2021", 1000F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"),
                    transaction(1000.0, "Payout Refund", "22 January 2021"), transaction(5.75, "Accrual", "22 January 2021"),
                    transaction(5.75, "Interest Refund", "22 January 2021"));
        });
    }

    @Test
    public void verifyUC02a() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .supportedInterestRefundTypes(new ArrayList<>()).addSupportedInterestRefundTypesItem("PAYOUT_REFUND") //
                            .addSupportedInterestRefundTypesItem("MERCHANT_ISSUED_REFUND") //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    12, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000), "1 January 2021");
        });
        runAt("1 February 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("PayoutRefund", "1 February 2021", 1000F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"),
                    transaction(1000.0, "Payout Refund", "01 February 2021"), transaction(8.48, "Accrual", "01 February 2021"),
                    transaction(8.48, "Interest Refund", "01 February 2021"));
        });
    }

    @Test
    public void verifyUC02b() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .supportedInterestRefundTypes(new ArrayList<>()).addSupportedInterestRefundTypesItem("PAYOUT_REFUND") //
                            .addSupportedInterestRefundTypesItem("MERCHANT_ISSUED_REFUND") //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    12, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000), "1 January 2021");
        });
        runAt("1 February 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment("Repayment",
                    "1 February 2021", 87.89F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"),
                    transaction(87.89, "Repayment", "01 February 2021"));
        });

        runAt("9 February 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("PayoutRefund", "9 February 2021", 1000F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"),
                    transaction(1000.0, "Payout Refund", "09 February 2021"), transaction(87.89, "Repayment", "01 February 2021"),
                    transaction(10.50, "Interest Refund", "09 February 2021"));
        });
    }

    @Test
    public void verifyUC03() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .supportedInterestRefundTypes(new ArrayList<>())//
                            .disallowExpectedDisbursements(true)//
                            .multiDisburseLoan(true)//
                            .maxTrancheCount(2).addSupportedInterestRefundTypesItem("PAYOUT_REFUND") //
                            .addSupportedInterestRefundTypesItem("MERCHANT_ISSUED_REFUND") //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    12, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(750), "1 January 2021");
            disburseLoan(loanId, BigDecimal.valueOf(250), "1 January 2021");
        });
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("PayoutRefund", "22 January 2021", 1000F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId, transaction(750.0, "Disbursement", "01 January 2021"),
                    transaction(250.0, "Disbursement", "01 January 2021"), transaction(1000.0, "Payout Refund", "22 January 2021"),
                    transaction(5.75, "Accrual", "22 January 2021"), transaction(5.75, "Interest Refund", "22 January 2021"));
        });
    }

    @Test
    public void verifyUC04() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .supportedInterestRefundTypes(new ArrayList<>()).disallowExpectedDisbursements(true).multiDisburseLoan(true)
                            .maxTrancheCount(2).addSupportedInterestRefundTypesItem("PAYOUT_REFUND") //
                            .addSupportedInterestRefundTypesItem("MERCHANT_ISSUED_REFUND") //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    12, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(250), "1 January 2021");
        });
        runAt("4 January 2021", () -> {
            Long loanId = loanIdRef.get();
            disburseLoan(loanId, BigDecimal.valueOf(750), "4 January 2021");
        });
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("PayoutRefund", "22 January 2021", 1000F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId, transaction(750.0, "Disbursement", "04 January 2021"),
                    transaction(250.0, "Disbursement", "01 January 2021"), transaction(1000.0, "Payout Refund", "22 January 2021"),
                    transaction(5.14, "Accrual", "22 January 2021"), transaction(5.14, "Interest Refund", "22 January 2021"));
        });
    }

    @Test
    public void verifyUC05() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .supportedInterestRefundTypes(new ArrayList<>()).disallowExpectedDisbursements(true).multiDisburseLoan(true)
                            .maxTrancheCount(2).addSupportedInterestRefundTypesItem("PAYOUT_REFUND") //
                            .addSupportedInterestRefundTypesItem("MERCHANT_ISSUED_REFUND") //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    12, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(500), "1 January 2021");
        });
        runAt("7 January 2021", () -> {
            Long loanId = loanIdRef.get();
            disburseLoan(loanId, BigDecimal.valueOf(500), "7 January 2021");
        });
        runAt("1 February 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment("Repayment",
                    "1 February 2021", 87.82F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId, transaction(500.0, "Disbursement", "01 January 2021"),
                    transaction(500.0, "Disbursement", "07 January 2021"), transaction(87.82, "Repayment", "01 February 2021"));
        });

        runAt("9 February 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("PayoutRefund", "9 February 2021", 1000F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId, transaction(500.0, "Disbursement", "01 January 2021"),
                    transaction(500.0, "Disbursement", "07 January 2021"), transaction(1000.0, "Payout Refund", "09 February 2021"),
                    transaction(87.82, "Repayment", "01 February 2021"), transaction(9.67, "Interest Refund", "09 February 2021"));
        });
    }

    @Test
    public void verifyUC06x3() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .supportedInterestRefundTypes(new ArrayList<>()).addSupportedInterestRefundTypesItem("PAYOUT_REFUND") //
                            .addSupportedInterestRefundTypesItem("MERCHANT_ISSUED_REFUND") //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
                    );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    3, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000), "1 January 2021");

        });
        runAt("14 January 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("PayoutRefund", "14 January 2021", 500F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(500.0, "Payout Refund", "14 January 2021"), //
                    transaction(1.78, "Interest Refund", "14 January 2021"));
        });
    }

    @Test
    public void verifyUC07x3() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .supportedInterestRefundTypes(new ArrayList<>()).addSupportedInterestRefundTypesItem("PAYOUT_REFUND") //
                            .addSupportedInterestRefundTypesItem("MERCHANT_ISSUED_REFUND") //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
                    );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    3, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000), "1 January 2021");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);
        });
        runAt("1 February 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment("Repayment",
                    "1 February 2021", 338.82F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId,
                    transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(338.82, "Repayment", "01 February 2021"));
        });
        runAt("9 February 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("PayoutRefund", "09 February 2021", 500F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(338.82, "Repayment", "01 February 2021"), //
                    transaction(500.0, "Payout Refund", "09 February 2021"), //
                    transaction(1.78, "Interest Refund", "09 February 2021"));
        });
    }

    @Test
    public void verifyUC08() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .supportedInterestRefundTypes(new ArrayList<>())//
                            .disallowExpectedDisbursements(true)//
                            .multiDisburseLoan(true)//
                            .maxTrancheCount(2).addSupportedInterestRefundTypesItem("PAYOUT_REFUND") //
                            .addSupportedInterestRefundTypesItem("MERCHANT_ISSUED_REFUND") //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
                    );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    6, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(250), "1 January 2021");
            disburseLoan(loanId, BigDecimal.valueOf(750), "1 January 2021");
        });
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("PayoutRefund", "22 January 2021", 500F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId,
                    transaction(250.0, "Disbursement", "01 January 2021"), //
                    transaction(750.0, "Disbursement", "01 January 2021"), //
                    transaction(500.0, "Payout Refund", "22 January 2021"), //
//                    transaction(5.75, "Accrual", "22 January 2021"), //
                    transaction(2.87, "Interest Refund", "22 January 2021"));
        });
    }


    @Test
    public void verifyUC09() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .supportedInterestRefundTypes(new ArrayList<>()).disallowExpectedDisbursements(true).multiDisburseLoan(true)
                            .maxTrancheCount(2).addSupportedInterestRefundTypesItem("PAYOUT_REFUND") //
                            .addSupportedInterestRefundTypesItem("MERCHANT_ISSUED_REFUND") //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
                    );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    6, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(250), "1 January 2021");
        });
        runAt("7 January 2021", () -> {
            Long loanId = loanIdRef.get();
            disburseLoan(loanId, BigDecimal.valueOf(750), "7 January 2021");
        });
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("PayoutRefund", "22 January 2021", 500F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId,
                    transaction(250.0, "Disbursement", "01 January 2021"), //
                    transaction(750.0, "Disbursement", "01 January 2021"), //
                    transaction(500.0, "Payout Refund", "22 January 2021"), //
//                    transaction(2.46, "Accrual", "22 January 2021"), //
                    transaction(2.46, "Interest Refund", "22 January 2021"));
        });
    }

    @Test
    public void verifyUC10x2() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .supportedInterestRefundTypes(new ArrayList<>()).disallowExpectedDisbursements(true).multiDisburseLoan(true)
                            .maxTrancheCount(2).addSupportedInterestRefundTypesItem("PAYOUT_REFUND") //
                            .addSupportedInterestRefundTypesItem("MERCHANT_ISSUED_REFUND") //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
                    );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    2, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(250), "1 January 2021");
        });
        runAt("7 January 2021", () -> {
            Long loanId = loanIdRef.get();
            disburseLoan(loanId, BigDecimal.valueOf(750), "7 January 2021");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);
        });
        runAt("1 February 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("Repayment", "1 February 2021", 505.55F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId,
                    transaction(250.0, "Disbursement", "01 January 2021"), //
                    transaction(750.0, "Disbursement", "07 January 2021"), //
                    transaction(505.55, "Repayment", "01 February 2021")); //
        });
        runAt("1 March 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("Repayment", "1 March 2021", 505.54F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId,
                    transaction(250.0, "Disbursement", "01 January 2021"), //
                    transaction(750.0, "Disbursement", "07 January 2021"), //
                    transaction(505.55, "Repayment", "01 February 2021"), //
                    transaction(505.54, "Repayment", "01 March 2021")); //
        });
        runAt("11 March 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("PayoutRefund", "11 March 2021", 500.0F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId,
                    transaction(250.0, "Disbursement", "01 January 2021"), //
                    transaction(750.0, "Disbursement", "07 January 2021"), //
                    transaction(505.55, "Repayment", "01 February 2021"), //
                    transaction(505.54, "Repayment", "01 March 2021"), //
                    transaction(500.0, "Payout Refund", "11 March 2021"), //
                    transaction(5.54, "Interest Refund", "11 March 2021")); //
        });
    }

    @Test
    public void verifyUC11x2() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .supportedInterestRefundTypes(new ArrayList<>()).disallowExpectedDisbursements(true).multiDisburseLoan(true)
                            .maxTrancheCount(2).addSupportedInterestRefundTypesItem("PAYOUT_REFUND") //
                            .addSupportedInterestRefundTypesItem("MERCHANT_ISSUED_REFUND") //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
                    );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    2, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000), "1 January 2021");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);
        });
        runAt("11 January 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("MerchantIssuedRefund", "11 January 2021", 500.0F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId,
                    transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(500.0, "Merchant Issued Refund", "11 January 2021"), //
                    transaction(1.37, "Interest Refund", "11 January 2021")); //
        });
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("PayoutRefund", "22 January 2021", 500.0F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logTransactions(loanId);
            verifyTransactions(loanId,
                    transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(500.0, "Merchant Issued Refund", "11 January 2021"), //
                    transaction(1.37, "Interest Refund", "11 January 2021"), //
                    transaction(500.0, "Payout Refund", "22 January 2021"), //
                    transaction(2.87, "Interest Refund", "22 January 2021")); //
        });
    }

    private void logLoanDetails(GetLoansLoanIdResponse loanDetails) {
        log.info("index, dueDate, principal, fee, penalty, interest");
        Assertions.assertNotNull(loanDetails.getRepaymentSchedule());
        Assertions.assertNotNull(loanDetails.getRepaymentSchedule().getPeriods());
        loanDetails.getRepaymentSchedule().getPeriods()
                .forEach(period -> log.info("{}, \"{}\", {}, {}, {}, {}", period.getPeriod(),
                        DateTimeFormatter.ofPattern(DATETIME_PATTERN, Locale.ENGLISH).format(Objects.requireNonNull(period.getDueDate())),
                        period.getPrincipalDue(), period.getFeeChargesDue(), period.getPenaltyChargesDue(), period.getInterestDue()));
    }

    private void logTransactions(Long loanId) {
        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
        assert loanDetails.getTransactions() != null;
        loanDetails.getTransactions()
                .forEach(tr -> log.info("Transaction {}  {}  {} ", tr.getType().getValue(), tr.getDate(), tr.getAmount()));
    }

}
