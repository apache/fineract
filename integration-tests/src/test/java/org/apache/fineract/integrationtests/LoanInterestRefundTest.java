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

import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum.REPLAYED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.util.CallFailedRuntimeException;
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

            logLoanTransactions(loanId);
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

            logLoanTransactions(loanId);
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
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
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

            logLoanTransactions(loanId);
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(1000.0, "Payout Refund", "22 January 2021"), //
                    transaction(5.74, "Interest Refund", "22 January 2021"), //
                    transaction(5.74, "Accrual", "22 January 2021")); //

            checkTransactionWasNotReverseReplayed(postLoansLoanIdTransactionsResponse.getLoanId(),
                    postLoansLoanIdTransactionsResponse.getResourceId());
            checkTransactionWasNotReverseReplayed(postLoansLoanIdTransactionsResponse.getLoanId(),
                    postLoansLoanIdTransactionsResponse.getSubResourceId());

            verifyTRJournalEntries(postLoansLoanIdTransactionsResponse.getResourceId(), //
                    journalEntry(1000, fundSource, "DEBIT"), //
                    journalEntry(5.74, interestReceivableAccount, "CREDIT"), //
                    journalEntry(994.26, loansReceivableAccount, "CREDIT"));

            verifyTRJournalEntries(postLoansLoanIdTransactionsResponse.getSubResourceId(),
                    journalEntry(5.74, interestIncomeAccount, "DEBIT"), //
                    journalEntry(5.74, loansReceivableAccount, "CREDIT")); //
        });
    }

    private void checkTransactionWasNotReverseReplayed(Long loanId, Long transactionId) {
        GetLoansLoanIdTransactionsTransactionIdResponse loanTransactionDetails = loanTransactionHelper.getLoanTransactionDetails(loanId,
                transactionId);
        if (loanTransactionDetails.getTransactionRelations() != null) {
            loanTransactionDetails.getTransactionRelations().forEach(transactionRelation -> {
                if (REPLAYED.name().equals(transactionRelation.getRelationType())) {
                    Assertions.fail("Transaction was replayed!");
                }
            });
        }
    }

    @Test
    public void verifyInterestRefundCreatedForMerchantIssuedRefund() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
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

            logLoanTransactions(loanId);
            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(1000.0, "Merchant Issued Refund", "22 January 2021"), //
                    transaction(5.74, "Accrual", "22 January 2021"), //
                    transaction(5.74, "Interest Refund", "22 January 2021") //
            );
        });
    }

    @Test
    public void verifyUC01() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
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

            logLoanTransactions(loanId);
            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(1000.0, "Payout Refund", "22 January 2021"), //
                    transaction(5.74, "Accrual", "22 January 2021"), //
                    transaction(5.74, "Interest Refund", "22 January 2021") //
            );
        });
    }

    @Test
    public void verifyUC02a() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
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

            logLoanTransactions(loanId);
            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(1000.0, "Payout Refund", "01 February 2021"), //
                    transaction(8.48, "Accrual", "01 February 2021"), //
                    transaction(8.48, "Interest Refund", "01 February 2021")); //
        });
    }

    @Test
    public void verifyUC02b() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
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

            logLoanTransactions(loanId);
            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"),
                    transaction(87.89, "Repayment", "01 February 2021"));
        });

        runAt("9 February 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("PayoutRefund", "9 February 2021", 1000F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logLoanTransactions(loanId);
            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(87.89, "Repayment", "01 February 2021"), //
                    transaction(1000.0, "Payout Refund", "09 February 2021"), //
                    transaction(10.49, "Interest Refund", "09 February 2021"), //
                    transaction(10.49, "Accrual", "09 February 2021") //
            );
        });
    }

    @Test
    public void verifyUC03() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            //
                            .disallowExpectedDisbursements(true)//
                            .multiDisburseLoan(true)//
                            .maxTrancheCount(2).addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
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

            logLoanTransactions(loanId);
            verifyTransactions(loanId, transaction(750.0, "Disbursement", "01 January 2021"), //
                    transaction(250.0, "Disbursement", "01 January 2021"), //
                    transaction(1000.0, "Payout Refund", "22 January 2021"), //
                    transaction(5.74, "Accrual", "22 January 2021"), //
                    transaction(5.74, "Interest Refund", "22 January 2021") //
            );
        });
    }

    @Test
    public void verifyUC04() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .disallowExpectedDisbursements(true).multiDisburseLoan(true).maxTrancheCount(2)
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
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

            logLoanTransactions(loanId);
            verifyTransactions(loanId, transaction(750.0, "Disbursement", "04 January 2021"), //
                    transaction(250.0, "Disbursement", "01 January 2021"), //
                    transaction(1000.0, "Payout Refund", "22 January 2021"), //
                    transaction(5.13, "Accrual", "22 January 2021"), //
                    transaction(5.13, "Interest Refund", "22 January 2021") //
            );
        });
    }

    @Test
    public void verifyUC05() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .multiDisburseLoan(true) //
                            .disallowExpectedDisbursements(true) //
                            .maxTrancheCount(2).addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
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

            logLoanTransactions(loanId);
            verifyTransactions(loanId, transaction(500.0, "Disbursement", "01 January 2021"),
                    transaction(500.0, "Disbursement", "07 January 2021"), transaction(87.82, "Repayment", "01 February 2021"));
        });

        runAt("9 February 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("PayoutRefund", "9 February 2021", 1000F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logLoanTransactions(loanId);
            verifyTransactions(loanId, transaction(500.0, "Disbursement", "01 January 2021"), //
                    transaction(500.0, "Disbursement", "07 January 2021"), //
                    transaction(1000.0, "Payout Refund", "09 February 2021"), //
                    transaction(87.82, "Repayment", "01 February 2021"), //
                    transaction(9.67, "Interest Refund", "09 February 2021"), //
                    transaction(9.67, "Accrual", "09 February 2021") //
            );
        });
    }

    @Test
    public void verifyUC06() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 December 2020", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 December 2020", 1000.0, 9.99,
                    6, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000), "1 December 2020");

        });
        runAt("14 December 2020", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("PayoutRefund", "14 December 2020", 500F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logLoanTransactions(loanId);
            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 December 2020"), //
                    transaction(500.0, "Payout Refund", "14 December 2020"), //
                    transaction(1.78, "Interest Refund", "14 December 2020"));
        });
    }

    @Test
    public void verifyUC07() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    12, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000), "1 January 2021");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logInstallmentsOfLoanDetails(loanDetails);
        });
        runAt("1 February 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment("Repayment",
                    "1 February 2021", 87.89F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logLoanTransactions(loanId);
            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(87.89, "Repayment", "01 February 2021"));
        });
        runAt("9 February 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("PayoutRefund", "09 February 2021", 500F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logLoanTransactions(loanId);
            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(87.89, "Repayment", "01 February 2021"), //
                    transaction(500.0, "Payout Refund", "09 February 2021"), //
                    transaction(5.34, "Interest Refund", "09 February 2021"));
        });
    }

    @Test
    public void verifyUC08() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .disallowExpectedDisbursements(true)//
                            .multiDisburseLoan(true)//
                            .maxTrancheCount(2).addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
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

            logLoanTransactions(loanId);
            verifyTransactions(loanId, transaction(250.0, "Disbursement", "01 January 2021"), //
                    transaction(750.0, "Disbursement", "01 January 2021"), //
                    transaction(500.0, "Payout Refund", "22 January 2021"), //
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
                            .disallowExpectedDisbursements(true).multiDisburseLoan(true).maxTrancheCount(2)
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
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

            logLoanTransactions(loanId);
            verifyTransactions(loanId, transaction(250.0, "Disbursement", "01 January 2021"), //
                    transaction(750.0, "Disbursement", "07 January 2021"), //
                    transaction(500.0, "Payout Refund", "22 January 2021"), //
                    transaction(2.46, "Interest Refund", "22 January 2021"));
        });
    }

    @Test
    public void verifyUC10() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .disallowExpectedDisbursements(true).multiDisburseLoan(true).maxTrancheCount(2)
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
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

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logInstallmentsOfLoanDetails(loanDetails);
        });
        runAt("1 July 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("Repayment", "1 February 2021", 171.29F, loanId.intValue());
            loanTransactionHelper.makeLoanRepayment("Repayment", "1 March 2021", 171.29F, loanId.intValue());
            loanTransactionHelper.makeLoanRepayment("Repayment", "1 April 2021", 171.29F, loanId.intValue());
            loanTransactionHelper.makeLoanRepayment("Repayment", "1 May 2021", 171.29F, loanId.intValue());
            loanTransactionHelper.makeLoanRepayment("Repayment", "1 June 2021", 171.29F, loanId.intValue());
            loanTransactionHelper.makeLoanRepayment("Repayment", "1 July 2021", 171.32F, loanId.intValue());

            logLoanTransactions(loanId);
            verifyTransactions(loanId, transaction(250.0, "Disbursement", "01 January 2021"), //
                    transaction(750.0, "Disbursement", "07 January 2021"), //
                    transaction(171.29, "Repayment", "01 February 2021"), //
                    transaction(171.29, "Repayment", "01 March 2021"), //
                    transaction(171.29, "Repayment", "01 April 2021"), //
                    transaction(171.29, "Repayment", "01 May 2021"), //
                    transaction(171.29, "Repayment", "01 June 2021"), //
                    transaction(171.32, "Repayment", "01 July 2021"), //
                    transaction(27.77, "Accrual", "01 July 2021") //
            ); //
        });
        runAt("11 July 2021", () -> {
            Long loanId = loanIdRef.get();
            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("PayoutRefund", "11 July 2021", 500.0F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            logLoanTransactions(loanId);
            verifyTransactions(loanId, transaction(250.0, "Disbursement", "01 January 2021"), //
                    transaction(750.0, "Disbursement", "07 January 2021"), //
                    transaction(171.29, "Repayment", "01 February 2021"), //
                    transaction(171.29, "Repayment", "01 March 2021"), //
                    transaction(171.29, "Repayment", "01 April 2021"), //
                    transaction(171.29, "Repayment", "01 May 2021"), //
                    transaction(171.29, "Repayment", "01 June 2021"), //
                    transaction(171.32, "Repayment", "01 July 2021"), //
                    transaction(500.0, "Payout Refund", "11 July 2021"), //
                    transaction(20.41, "Interest Refund", "11 July 2021"), //
                    transaction(27.77, "Accrual", "01 July 2021") //
            ); //
        });
    }

    @Test
    public void verifyUC11() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    6, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000), "1 January 2021");

        });
        runAt("14 January 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("MerchantIssuedRefund", "14 January 2021", 500F, loanId.intValue());

            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(500.0, "Merchant Issued Refund", "14 January 2021"), //
                    transaction(1.78, "Interest Refund", "14 January 2021"));
        });
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("PayoutRefund", "22 January 2021", 500F, loanId.intValue());
            logLoanTransactions(loanId);
            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(500.0, "Merchant Issued Refund", "14 January 2021"), //
                    transaction(1.78, "Interest Refund", "14 January 2021"), //
                    transaction(500.0, "Payout Refund", "22 January 2021"), //
                    transaction(2.88, "Interest Refund", "22 January 2021"), //
                    transaction(4.66, "Accrual", "22 January 2021") //
            );
        });
    }

    @Test
    public void verifyUC12() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    6, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000), "1 January 2021");

        });
        runAt("1 February 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("Repayment", "1 February 2021", 171.50F, loanId.intValue());

            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(171.5, "Repayment", "01 February 2021"));
        });
        runAt("9 February 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("MerchantIssuedRefund", "9 February 2021", 500F, loanId.intValue());

            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(171.5, "Repayment", "01 February 2021"), //
                    transaction(500.0, "Merchant Issued Refund", "09 February 2021"), //
                    transaction(5.34, "Interest Refund", "09 February 2021"));
        });
        runAt("25 February 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("PayoutRefund", "25 February 2021", 250F, loanId.intValue());

            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(171.5, "Repayment", "01 February 2021"), //
                    transaction(500.0, "Merchant Issued Refund", "09 February 2021"), //
                    transaction(5.34, "Interest Refund", "09 February 2021"), //
                    transaction(250.0, "Payout Refund", "25 February 2021"), //
                    transaction(3.78, "Interest Refund", "25 February 2021") //
            );
        });
    }

    @Test
    public void verifyUC13() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .multiDisburseLoan(true)//
                            .disallowExpectedDisbursements(true)//
                            .maxTrancheCount(2).addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    12, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(250), "1 January 2021");
            disburseLoan(loanId, BigDecimal.valueOf(750), "1 January 2021");

        });
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("MerchantIssuedRefund", "22 January 2021", 500F, loanId.intValue());

            verifyTransactions(loanId, transaction(250.0, "Disbursement", "01 January 2021"), //
                    transaction(750.0, "Disbursement", "01 January 2021"), //
                    transaction(500.0, "Merchant Issued Refund", "22 January 2021"), //
                    transaction(2.87, "Interest Refund", "22 January 2021") //
            );
        });
        runAt("26 January 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("PayoutRefund", "26 January 2021", 400F, loanId.intValue());

            verifyTransactions(loanId, transaction(250.0, "Disbursement", "01 January 2021"), //
                    transaction(750.0, "Disbursement", "01 January 2021"), //
                    transaction(500.0, "Merchant Issued Refund", "22 January 2021"), //
                    transaction(2.87, "Interest Refund", "22 January 2021"), //
                    transaction(400.0, "Payout Refund", "26 January 2021"), //
                    transaction(2.73, "Interest Refund", "26 January 2021") //
            );
        });
        runAt("1 February 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("Repayment", "1 February 2021", 100.86F, loanId.intValue());
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            Assertions.assertNotNull(loanDetails);
            Assertions.assertNotNull(loanDetails.getStatus());
            Assertions.assertEquals(600, loanDetails.getStatus().getId());
        });
    }

    @Test
    public void verifyUC14() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .multiDisburseLoan(true)//
                            .disallowExpectedDisbursements(true)//
                            .maxTrancheCount(3).addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    6, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(200), "1 January 2021");
            disburseLoan(loanId, BigDecimal.valueOf(300), "1 January 2021");
        });
        runAt("5 January 2021", () -> {
            Long loanId = loanIdRef.get();
            disburseLoan(loanId, BigDecimal.valueOf(500), "5 January 2021");

        });
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("MerchantIssuedRefund", "22 January 2021", 250F, loanId.intValue());

            verifyTransactions(loanId, transaction(200.0, "Disbursement", "01 January 2021"), //
                    transaction(300.0, "Disbursement", "01 January 2021"), //
                    transaction(500.0, "Disbursement", "05 January 2021"), //
                    transaction(250.0, "Merchant Issued Refund", "22 January 2021"), //
                    transaction(1.44, "Interest Refund", "22 January 2021") //
            );
        });
        runAt("26 January 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("PayoutRefund", "26 January 2021", 400F, loanId.intValue());
            logLoanTransactions(loanId);

            verifyTransactions(loanId, transaction(200.0, "Disbursement", "01 January 2021"), //
                    transaction(300.0, "Disbursement", "01 January 2021"), //
                    transaction(500.0, "Disbursement", "05 January 2021"), //
                    transaction(250.0, "Merchant Issued Refund", "22 January 2021"), //
                    transaction(1.44, "Interest Refund", "22 January 2021"), //
                    transaction(400.0, "Payout Refund", "26 January 2021"), //
                    transaction(2.57, "Interest Refund", "26 January 2021") //
            );
        });
        runAt("1 April 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("Repayment", "1 February 2021", 171.41F, loanId.intValue());
            loanTransactionHelper.makeLoanRepayment("Repayment", "1 March 2021", 171.41F, loanId.intValue());
            loanTransactionHelper.makeLoanRepayment("Repayment", "1 April 2021", 11.25F, loanId.intValue());
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

            Assertions.assertNotNull(loanDetails);
            Assertions.assertNotNull(loanDetails.getStatus());
            Assertions.assertEquals(600, loanDetails.getStatus().getId());
        });
    }

    @Test
    public void verifyUC15() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .multiDisburseLoan(true)//
                            .disallowExpectedDisbursements(true)//
                            .maxTrancheCount(3).addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    6, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(500), "1 January 2021");
        });
        runAt("5 January 2021", () -> {
            Long loanId = loanIdRef.get();
            disburseLoan(loanId, BigDecimal.valueOf(500), "5 January 2021");

        });
        runAt("1 February 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("Repayment", "1 February 2021", 171.41F, loanId.intValue());

            verifyTransactions(loanId, transaction(500.0, "Disbursement", "01 January 2021"), //
                    transaction(500.0, "Disbursement", "05 January 2021"), //
                    transaction(171.41, "Repayment", "01 February 2021"));
        });
        runAt("13 February 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("PayoutRefund", "13 February 2021", 250F, loanId.intValue());

            verifyTransactions(loanId, transaction(500.0, "Disbursement", "01 January 2021"), //
                    transaction(500.0, "Disbursement", "05 January 2021"), //
                    transaction(171.41, "Repayment", "01 February 2021"), //
                    transaction(250.0, "Payout Refund", "13 February 2021"), //
                    transaction(2.96, "Interest Refund", "13 February 2021") //
            );
        });
        runAt("24 February 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("MerchantIssuedRefund", "24 February 2021", 400F, loanId.intValue());
            logLoanTransactions(loanId);

            verifyTransactions(loanId, transaction(500.0, "Disbursement", "01 January 2021"), //
                    transaction(500.0, "Disbursement", "05 January 2021"), //
                    transaction(171.41, "Repayment", "01 February 2021"), //
                    transaction(250.0, "Payout Refund", "13 February 2021"), //
                    transaction(2.96, "Interest Refund", "13 February 2021"), //
                    transaction(400.0, "Merchant Issued Refund", "24 February 2021"), //
                    transaction(5.77, "Interest Refund", "24 February 2021") //
            );
        });
        runAt("1 April 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("Repayment", "1 March 2021", 171.41F, loanId.intValue());
            loanTransactionHelper.makeLoanRepayment("Repayment", "1 April 2021", 11.25F, loanId.intValue());
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

            Assertions.assertNotNull(loanDetails);
            Assertions.assertNotNull(loanDetails.getStatus());
            Assertions.assertEquals(600, loanDetails.getStatus().getId());
        });
    }

    @Test
    public void verifyUC16() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .multiDisburseLoan(true)//
                            .disallowExpectedDisbursements(true)//
                            .maxTrancheCount(3).addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    6, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(500), "1 January 2021");
        });
        runAt("5 January 2021", () -> {
            Long loanId = loanIdRef.get();
            disburseLoan(loanId, BigDecimal.valueOf(500), "5 January 2021");

        });
        runAt("1 February 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("Repayment", "1 February 2021", 171.41F, loanId.intValue());

            verifyTransactions(loanId, transaction(500.0, "Disbursement", "01 January 2021"), //
                    transaction(500.0, "Disbursement", "05 January 2021"), //
                    transaction(171.41, "Repayment", "01 February 2021"));
        });
        runAt("13 February 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("PayoutRefund", "13 February 2021", 250F, loanId.intValue());

            verifyTransactions(loanId, transaction(500.0, "Disbursement", "01 January 2021"), //
                    transaction(500.0, "Disbursement", "05 January 2021"), //
                    transaction(171.41, "Repayment", "01 February 2021"), //
                    transaction(250.0, "Payout Refund", "13 February 2021"), //
                    transaction(2.96, "Interest Refund", "13 February 2021") //
            );
        });

        runAt("1 April 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("Repayment", "1 March 2021", 171.41F, loanId.intValue());
            loanTransactionHelper.makeLoanRepayment("Repayment", "1 April 2021", 171.41F, loanId.intValue());
        });
        runAt("6 April 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("MerchantIssuedRefund", "6 April 2021", 400F, loanId.intValue());
            logLoanTransactions(loanId);

            verifyTransactions(loanId, transaction(500.0, "Disbursement", "01 January 2021"), //
                    transaction(500.0, "Disbursement", "05 January 2021"), //
                    transaction(171.41, "Repayment", "01 February 2021"), //
                    transaction(171.41, "Repayment", "01 March 2021"), //
                    transaction(171.41, "Repayment", "01 April 2021"), //
                    transaction(250.0, "Payout Refund", "13 February 2021"), //
                    transaction(2.96, "Interest Refund", "13 February 2021"), //
                    transaction(400.0, "Merchant Issued Refund", "06 April 2021"), //
                    transaction(10.11, "Interest Refund", "06 April 2021"), //
                    transaction(17.14, "Accrual", "06 April 2021") //
            );
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

            Assertions.assertNotNull(loanDetails);
            Assertions.assertNotNull(loanDetails.getStatus());
            Assertions.assertEquals(700, loanDetails.getStatus().getId());
            Assertions.assertEquals(160.16D, loanDetails.getTotalOverpaid());
        });
    }

    @Test
    public void verifyUC17() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .multiDisburseLoan(true)//
                            .disallowExpectedDisbursements(true)//
                            .maxTrancheCount(2).addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.99,
                    12, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000), "1 January 2021");

        });
        runAt("12 January 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("PayoutRefund", "12 January 2021", 400F, loanId.intValue());

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(400.0, "Payout Refund", "12 January 2021"), //
                    transaction(1.20, "Interest Refund", "12 January 2021") //
            );
        });
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("MerchantIssuedRefund", "17 January 2021", 150F, loanId.intValue());

            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(400.0, "Payout Refund", "12 January 2021"), //
                    transaction(1.20, "Interest Refund", "12 January 2021"), //
                    transaction(150.0, "Merchant Issued Refund", "17 January 2021"), //
                    transaction(0.66, "Interest Refund", "17 January 2021") //
            );
        });

        runAt("1 February 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("Repayment", "1 February 2021", 171.5F, loanId.intValue());
        });
        runAt("8 February 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("PayoutRefund", "8 February 2021", 250F, loanId.intValue());
            logLoanTransactions(loanId);

            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(400.0, "Payout Refund", "12 January 2021"), //
                    transaction(1.20, "Interest Refund", "12 January 2021"), //
                    transaction(150.0, "Merchant Issued Refund", "17 January 2021"), //
                    transaction(0.66, "Interest Refund", "17 January 2021"), //
                    transaction(171.5, "Repayment", "01 February 2021"), //
                    transaction(250.0, "Payout Refund", "08 February 2021"), //
                    transaction(2.61, "Interest Refund", "08 February 2021") //
            );
        });
        runAt("1 March 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("Repayment", "1 March 2021", 30.43F, loanId.intValue());
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            Assertions.assertNotNull(loanDetails);
            Assertions.assertNotNull(loanDetails.getStatus());
            Assertions.assertEquals(600, loanDetails.getStatus().getId());
        });
    }

    @Test
    public void verifyUC18S1() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .multiDisburseLoan(true)//
                            .disallowExpectedDisbursements(true)//
                            .maxTrancheCount(2)//
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
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
            loanTransactionHelper.makeLoanRepayment("MerchantIssuedRefund", "22 January 2021", 1000F, loanId.intValue());
            logLoanTransactions(loanId);

            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(1000.0, "Merchant Issued Refund", "22 January 2021"), //
                    transaction(5.70, "Interest Refund", "22 January 2021"), //
                    transaction(5.70, "Accrual", "22 January 2021") //
            );
            loanTransactionHelper.makeLoanRepayment("Repayment", "10 January 2021", 85.63F, loanId.intValue());
            logLoanTransactions(loanId);

            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(85.63, "Repayment", "10 January 2021"), //
                    transaction(5.70, "Accrual", "22 January 2021"), //
                    transaction(1000.0, "Merchant Issued Refund", "22 January 2021"), //
                    transaction(5.42, "Interest Refund", "22 January 2021"), //
                    transaction(0.28, "Accrual Adjustment", "22 January 2021") //
            );
        });
    }

    @Test
    public void verifyNoEmptyInterestRefundTransaction() {
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .multiDisburseLoan(true)//
                            .disallowExpectedDisbursements(true)//
                            .maxTrancheCount(2)//
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.9,
                    12, null);
            Assertions.assertNotNull(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(1000), "1 January 2021");
            loanTransactionHelper.makeLoanRepayment("MerchantIssuedRefund", "1 January 2021", 1000F, loanId.intValue());
            logLoanTransactions(loanId);

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(1000.0, "Merchant Issued Refund", "01 January 2021") //
            );
            logLoanTransactions(loanId);
        });
    }

    @Test
    public void verifyUC18S2() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        AtomicReference<Long> repaymentIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .multiDisburseLoan(true)//
                            .disallowExpectedDisbursements(true)//
                            .maxTrancheCount(2).addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 1000.0, 9.9,
                    12, null);
            Assertions.assertNotNull(loanId);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000), "1 January 2021");
        });
        runAt("10 January 2021", () -> {
            Long loanId = loanIdRef.get();
            Long response = loanTransactionHelper.makeLoanRepayment("Repayment", "10 January 2021", 85.63F, loanId.intValue())
                    .getResourceId();
            Assertions.assertNotNull(response);
            repaymentIdRef.set(response);

            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(85.63, "Repayment", "10 January 2021") //
            );
        });
        runAt("22 January 2021", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment("MerchantIssuedRefund", "22 January 2021", 1000F, loanId.intValue());
            logLoanTransactions(loanId);

            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    transaction(85.63, "Repayment", "10 January 2021"), //
                    transaction(1000.0, "Merchant Issued Refund", "22 January 2021"), //
                    transaction(5.42, "Interest Refund", "22 January 2021"), //
                    transaction(5.42, "Accrual", "22 January 2021") //
            );

            Long repaymentId = repaymentIdRef.get();
            loanTransactionHelper.reverseLoanTransaction(loanId.intValue(), repaymentId, "10 January 2021", responseSpec);
            logLoanTransactions(loanId);

            verifyTransactions(loanId, transaction(1000.0, "Disbursement", "01 January 2021"), //
                    reversedTransaction(85.63, "Repayment", "10 January 2021"), //
                    transaction(1000.0, "Merchant Issued Refund", "22 January 2021"), //
                    transaction(5.70, "Interest Refund", "22 January 2021"), //
                    transaction(5.42, "Accrual", "22 January 2021"), //
                    transaction(0.28, "Accrual", "22 January 2021") //
            );

        });
    }

    // UC19: Interest Refund reverse transaction only when the related transactions, Merchant Issued Refund or Payout
    // Refund are reversed
    // 1. Create a Loan Product that supports Interest Refund Types
    // 2. Submit, Approve and Disburse the loan
    // 3. Apply a Merchant Issued Refund Transaction
    // 4. Try to reverse the Interest Refund Transaction expecting to have an Exception
    // 5. Reverse the Merchant Issued Refund transaction and review the Interest Refund Transction is reversed too
    @Test
    public void verifyUC19() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2021", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL) //
                            .daysInYearType(DaysInYearType.ACTUAL) //
                            .multiDisburseLoan(true)//
                            .disallowExpectedDisbursements(true)//
                            .maxTrancheCount(2)//
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.PAYOUT_REFUND) //
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
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
            loanTransactionHelper.makeLoanRepayment("MerchantIssuedRefund", "22 January 2021", 1000F, loanId.intValue());
            logLoanTransactions(loanId);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
            Optional<GetLoansLoanIdTransactions> optInterestRefundTransaction = loanDetails.getTransactions().stream()
                    .filter(item -> Objects.equals(item.getType().getValue(), "Interest Refund")).findFirst();
            final Long interestRefundTransactionId = optInterestRefundTransaction.get().getId();

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> loanTransactionHelper.reverseLoanTransaction(loanId, interestRefundTransactionId,
                            new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN)
                                    .transactionDate("22 January 2021").transactionAmount(0.0).locale("en")));
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("error.msg.loan.transaction.update.not.allowed"));

            Optional<GetLoansLoanIdTransactions> optMerchantIssuedTransaction = loanDetails.getTransactions().stream()
                    .filter(item -> Objects.equals(item.getType().getValue(), "Merchant Issued Refund")).findFirst();
            final Long merchantIssuedTransactionId = optMerchantIssuedTransaction.get().getId();

            loanTransactionHelper.reverseLoanTransaction(loanId, merchantIssuedTransactionId,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate("22 January 2021")
                            .transactionAmount(0.0).locale("en"));

            loanDetails = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
            optInterestRefundTransaction = loanDetails.getTransactions().stream()
                    .filter(item -> Objects.equals(item.getType().getValue(), "Interest Refund")).findFirst();
            assertEquals(Boolean.TRUE, optInterestRefundTransaction.get().getManuallyReversed());
        });
    }

    private void logInstallmentsOfLoanDetails(GetLoansLoanIdResponse loanDetails) {
        log.info("index, dueDate, principal, fee, penalty, interest");
        if (loanDetails != null && loanDetails.getRepaymentSchedule() != null && loanDetails.getRepaymentSchedule().getPeriods() != null) {
            loanDetails.getRepaymentSchedule().getPeriods()
                    .forEach(period -> log.info("{}, \"{}\", {}, {}, {}, {}", period.getPeriod(),
                            DateTimeFormatter.ofPattern(DATETIME_PATTERN, Locale.ENGLISH)
                                    .format(Objects.requireNonNull(period.getDueDate())),
                            period.getPrincipalDue(), period.getFeeChargesDue(), period.getPenaltyChargesDue(), period.getInterestDue()));
        }
    }

    private void logLoanTransactions(Long loanId) {
        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
        if (loanDetails.getTransactions() != null) {
            loanDetails.getTransactions()
                    .forEach(tr -> log.info("Transaction {}  {}  {} ", tr.getType().getValue(), tr.getDate(), tr.getAmount()));
        }
    }

}
