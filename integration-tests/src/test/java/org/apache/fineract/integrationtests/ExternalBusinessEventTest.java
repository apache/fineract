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

import static org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder.DEFAULT_STRATEGY;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.GlobalConfigurationPropertyData;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostCreateRescheduleLoansRequest;
import org.apache.fineract.client.models.PostCreateRescheduleLoansResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PostUpdateRescheduleLoansRequest;
import org.apache.fineract.client.models.PutLoansLoanIdRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.integrationtests.common.BusinessStepHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.LoanRescheduleRequestHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.externalevents.ExternalEventHelper;
import org.apache.fineract.integrationtests.common.externalevents.ExternalEventsExtension;
import org.apache.fineract.integrationtests.common.externalevents.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.integrationtests.common.externalevents.LoanBusinessEvent;
import org.apache.fineract.integrationtests.common.externalevents.LoanTransactionBusinessEvent;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith({ ExternalEventsExtension.class })
public class ExternalBusinessEventTest extends BaseLoanIntegrationTest {

    private static final String DATETIME_PATTERN = "dd MMMM yyyy";
    private static PostClientsResponse client;
    private static LoanTransactionHelper loanTransactionHelper;
    private static LoanRescheduleRequestHelper loanRescheduleRequestHelper;
    private static Long loanProductId;
    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;
    Long chargeId = createCharge(111.0, "USD").getResourceId();
    private final ExternalEventHelper externalEventHelper = new ExternalEventHelper();

    @BeforeAll
    public static void beforeAll() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        ClientHelper clientHelper = new ClientHelper(requestSpec, responseSpec);
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        loanRescheduleRequestHelper = new LoanRescheduleRequestHelper(requestSpec, responseSpec);
        BusinessStepHelper businessStepHelper = new BusinessStepHelper();
        client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        loanProductId = createLoanProductPeriodicWithInterest();
        // setup COB Business Steps to prevent test failing due other integration test configurations
        businessStepHelper.updateSteps("LOAN_CLOSE_OF_BUSINESS", "APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION",
                "CHECK_LOAN_REPAYMENT_DUE", "CHECK_LOAN_REPAYMENT_OVERDUE", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES",
                "EXTERNAL_ASSET_OWNER_TRANSFER");
    }

    @Test()
    public void testExternalBusinessEventLoanBalanceChangedBusinessEventOnMultiDisbursedInterestBearingLoanForRepaymentAndOverpaymentAndReverseRepaymentAndFullRepayment() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 March 2023", () -> {
            enableLoanBalanceChangedBusinessEvent();
            Long loanId = applyForLoanApplicationWithInterest(client.getClientId(), loanProductId, BigDecimal.valueOf(1000), "1 March 2023",
                    "1 March 2023");
            loanIdRef.set(loanId);
            loanTransactionHelper.approveLoan("1 March 2023", loanId.intValue());

            deleteAllExternalEvents();

            loanTransactionHelper.disburseLoan("1 March 2023", loanId.intValue(), "400", null);

        });
        runAt("15 March 2023", () -> {
            deleteAllExternalEvents();

            loanTransactionHelper.makeLoanRepayment("15 March 2023", 125.0F, loanIdRef.get().intValue());

            verifyBusinessEvents(new LoanBusinessEvent("LoanBalanceChangedBusinessEvent", "15 March 2023", 300, 400.0, 291.04));
        });
        runAt("1 April 2023", () -> {

            loanTransactionHelper.disburseLoan("1 April 2023", loanIdRef.get().intValue(), "600", null);

        });
        runAt("15 April 2023", () -> {
            deleteAllExternalEvents();

            loanTransactionHelper.makeLoanRepayment("15 April 2023", 125.0F, loanIdRef.get().intValue());

            verifyBusinessEvents(new LoanBusinessEvent("LoanBalanceChangedBusinessEvent", "15 April 2023", 300, 1000.0, 770.06));

            deleteAllExternalEvents();

            Long transactionId = loanTransactionHelper.makeLoanRepayment("15 April 2023", 1000F, loanIdRef.get().intValue())
                    .getResourceId();
            Assertions.assertNotNull(transactionId);

            verifyBusinessEvents(new LoanBusinessEvent("LoanBalanceChangedBusinessEvent", "15 April 2023", 700, 1000.0, 0.0));

            deleteAllExternalEvents();

            loanTransactionHelper.reverseRepayment(loanIdRef.get().intValue(), transactionId.intValue(), "15 April 2023");

            verifyBusinessEvents(new LoanBusinessEvent("LoanBalanceChangedBusinessEvent", "15 April 2023", 300, 1000.0, 770.06));

            deleteAllExternalEvents();

            loanTransactionHelper.makeLoanRepayment("15 April 2023", 830.22F, loanIdRef.get().intValue());

            verifyBusinessEvents(new LoanBusinessEvent("LoanBalanceChangedBusinessEvent", "15 April 2023", 600, 1000.0, 0.0));

            disableLoanBalanceChangedBusinessEvent();
        });
    }

    /**
     * interest bearing progressive loan with interest recalculation enabled Verify that
     * LoanChargeAdjustmentPostBusinessEvent has loanChargePaidByList populated when Charge Adjustment posted for whole
     * charge amount
     */
    @Test
    public void verifyLoanChargeAdjustmentPostBusinessEvent01() {
        runAt("1 January 2021", () -> {
            externalEventHelper.enableBusinessEvent("LoanChargeAdjustmentPostBusinessEvent");
            externalEventHelper.enableBusinessEvent("LoanTransactionMakeRepaymentPostBusinessEvent");
            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(create4IProgressive().currencyCode("USD"));
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 600.0, 9.99,
                    4, null);
            Assertions.assertNotNull(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(600), "1 January 2021");

            PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = addLoanCharge(loanId, chargeId, "01 February 2021", 111.0);
            Long chargeId = postLoansLoanIdChargesResponse.getResourceId();

            deleteAllExternalEvents();
            // resourceId is chargeId
            Long transactionId = loanTransactionHelper
                    .chargeAdjustment(loanId, chargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(111.0).locale("en"))
                    .getSubResourceId();

            List<ExternalEventResponse> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            List<ExternalEventResponse> list = allExternalEvents.stream()
                    .filter(x -> "LoanChargeAdjustmentPostBusinessEvent".equals(x.getType())).toList();
            Assertions.assertEquals(1, list.size());
            ExternalEventResponse event = list.get(0);
            Object loanChargePaidByList = event.getPayLoad().get("loanChargePaidByList");
            Assertions.assertInstanceOf(List.class, loanChargePaidByList);
            Assertions.assertEquals(1, ((List<?>) loanChargePaidByList).size());
            Map<?, ?> chargePaidBy = (Map<?, ?>) ((List<?>) loanChargePaidByList).get(0);
            Assertions.assertInstanceOf(Map.class, chargePaidBy);
            Assertions.assertEquals(111.0D, chargePaidBy.get("amount"));
            Assertions.assertEquals(chargeId.doubleValue(), chargePaidBy.get("chargeId"));
            Assertions.assertEquals(transactionId.doubleValue(), chargePaidBy.get("transactionId"));
        });
    }

    /**
     * interest bearing progressive loan with interest recalculation enabled Verify that
     * LoanChargeAdjustmentPostBusinessEvent has loanChargePaidByList populated when Charge Adjustment posted for
     * partial charge amount Verify cant post more than charge amount
     */
    @Test
    public void verifyLoanChargeAdjustmentPostBusinessEvent02() {
        runAt("1 January 2021", () -> {
            externalEventHelper.enableBusinessEvent("LoanChargeAdjustmentPostBusinessEvent");
            externalEventHelper.enableBusinessEvent("LoanTransactionMakeRepaymentPostBusinessEvent");
            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(create4IProgressive().currencyCode("USD"));
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 600.0, 9.99,
                    4, null);
            Assertions.assertNotNull(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(600), "1 January 2021");

            PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = addLoanCharge(loanId, chargeId, "01 February 2021", 111.0);
            Long chargeId = postLoansLoanIdChargesResponse.getResourceId();

            // check first part
            deleteAllExternalEvents();
            // resourceId is chargeId
            Long transactionId = loanTransactionHelper
                    .chargeAdjustment(loanId, chargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(69.0).locale("en"))
                    .getSubResourceId();

            List<ExternalEventResponse> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            List<ExternalEventResponse> list = allExternalEvents.stream()
                    .filter(x -> "LoanChargeAdjustmentPostBusinessEvent".equals(x.getType())).toList();
            Assertions.assertEquals(1, list.size());
            ExternalEventResponse event = list.get(0);
            Object loanChargePaidByList = event.getPayLoad().get("loanChargePaidByList");
            Assertions.assertInstanceOf(List.class, loanChargePaidByList);
            Assertions.assertEquals(1, ((List<?>) loanChargePaidByList).size());
            Map<?, ?> chargePaidBy = (Map<?, ?>) ((List<?>) loanChargePaidByList).get(0);
            Assertions.assertInstanceOf(Map.class, chargePaidBy);
            Assertions.assertEquals(69.0D, chargePaidBy.get("amount"));
            Assertions.assertEquals(chargeId.doubleValue(), chargePaidBy.get("chargeId"));
            Assertions.assertEquals(transactionId.doubleValue(), chargePaidBy.get("transactionId"));

            // check second part
            deleteAllExternalEvents();
            // resourceId is chargeId
            transactionId = loanTransactionHelper
                    .chargeAdjustment(loanId, chargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(42.0).locale("en"))
                    .getSubResourceId();

            allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            list = allExternalEvents.stream().filter(x -> "LoanChargeAdjustmentPostBusinessEvent".equals(x.getType())).toList();
            Assertions.assertEquals(1, list.size());
            event = list.get(0);
            loanChargePaidByList = event.getPayLoad().get("loanChargePaidByList");
            Assertions.assertInstanceOf(List.class, loanChargePaidByList);
            Assertions.assertEquals(1, ((List<?>) loanChargePaidByList).size());
            chargePaidBy = (Map<?, ?>) ((List<?>) loanChargePaidByList).get(0);
            Assertions.assertInstanceOf(Map.class, chargePaidBy);
            Assertions.assertEquals(42.0D, chargePaidBy.get("amount"));
            Assertions.assertEquals(chargeId.doubleValue(), chargePaidBy.get("chargeId"));
            Assertions.assertEquals(transactionId.doubleValue(), chargePaidBy.get("transactionId"));
            Assertions.assertFalse((Boolean) event.getPayLoad().get("reversed"));

            // check that third part cannot post for that charge, because it already fully adjusted
            Assertions.assertThrows(RuntimeException.class, () -> loanTransactionHelper.chargeAdjustment(loanId, chargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(1.0).locale("en")));
        });
    }

    /**
     * interest bearing progressive loan with interest recalculation enabled Verify Repayment pays charge and
     * repayment's event has loanChargePaidByList populated Verify that LoanChargeAdjustmentPostBusinessEvent has
     * loanChargePaidByList not populated when Charge Adjustment posted for partial charge amount Verify cant post more
     * than charge amount
     */
    @Test
    public void verifyLoanChargeAdjustmentPostBusinessEvent03() {
        runAt("1 January 2021", () -> {
            externalEventHelper.enableBusinessEvent("LoanChargeAdjustmentPostBusinessEvent");
            externalEventHelper.enableBusinessEvent("LoanTransactionMakeRepaymentPostBusinessEvent");
            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(create4IProgressive().currencyCode("USD"));
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 600.0, 9.99,
                    4, null);
            Assertions.assertNotNull(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(600), "1 January 2021");

            PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = addLoanCharge(loanId, chargeId, "01 February 2021", 111.0);
            Long chargeId = postLoansLoanIdChargesResponse.getResourceId();

            // check repayment
            deleteAllExternalEvents();
            Long transactionId = loanTransactionHelper.makeLoanRepayment("01 January 2021", 300.0F, loanId.intValue()).getResourceId();

            List<ExternalEventResponse> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            List<ExternalEventResponse> list = allExternalEvents.stream()
                    .filter(x -> "LoanTransactionMakeRepaymentPostBusinessEvent".equals(x.getType())).toList();
            Assertions.assertEquals(1, list.size());
            ExternalEventResponse event = list.get(0);
            log.info(event.toString());
            Object loanChargePaidByList = event.getPayLoad().get("loanChargePaidByList");
            Assertions.assertEquals(111.0D, event.getPayLoad().get("feeChargesPortion"));
            Assertions.assertInstanceOf(List.class, loanChargePaidByList);
            Assertions.assertEquals(1, ((List<?>) loanChargePaidByList).size());
            Map<?, ?> chargePaidBy = (Map<?, ?>) ((List<?>) loanChargePaidByList).get(0);
            Assertions.assertInstanceOf(Map.class, chargePaidBy);
            Assertions.assertEquals(111.0D, chargePaidBy.get("amount"));
            Assertions.assertEquals(chargeId.doubleValue(), chargePaidBy.get("chargeId"));
            Assertions.assertEquals(transactionId.doubleValue(), chargePaidBy.get("transactionId"));

            // check first part
            deleteAllExternalEvents();
            // resourceId is chargeId
            transactionId = loanTransactionHelper
                    .chargeAdjustment(loanId, chargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(69.0).locale("en"))
                    .getSubResourceId();

            allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            list = allExternalEvents.stream().filter(x -> "LoanChargeAdjustmentPostBusinessEvent".equals(x.getType())).toList();
            Assertions.assertEquals(1, list.size());
            event = list.get(0);
            loanChargePaidByList = event.getPayLoad().get("loanChargePaidByList");
            Assertions.assertInstanceOf(List.class, loanChargePaidByList);
            Assertions.assertEquals(0, ((List<?>) loanChargePaidByList).size());

            // check second part
            deleteAllExternalEvents();
            // resourceId is chargeId
            transactionId = loanTransactionHelper
                    .chargeAdjustment(loanId, chargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(42.0).locale("en"))
                    .getSubResourceId();

            allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            list = allExternalEvents.stream().filter(x -> "LoanChargeAdjustmentPostBusinessEvent".equals(x.getType())).toList();
            Assertions.assertEquals(1, list.size());
            event = list.get(0);
            loanChargePaidByList = event.getPayLoad().get("loanChargePaidByList");
            Assertions.assertInstanceOf(List.class, loanChargePaidByList);
            Assertions.assertEquals(0, ((List<?>) loanChargePaidByList).size());

            // check that third part cannot post for that charge, because it already fully adjusted
            Assertions.assertThrows(RuntimeException.class, () -> loanTransactionHelper.chargeAdjustment(loanId, chargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(1.0).locale("en")));
        });
    }

    /**
     * interest bearing progressive loan without interest recalculation Verify that
     * LoanChargeAdjustmentPostBusinessEvent has loanChargePaidByList populated when Charge Adjustment posted for whole
     * charge amount
     */
    @Test
    public void verifyLoanChargeAdjustmentPostBusinessEvent04() {
        runAt("1 January 2021", () -> {
            externalEventHelper.enableBusinessEvent("LoanChargeAdjustmentPostBusinessEvent");
            externalEventHelper.enableBusinessEvent("LoanTransactionMakeRepaymentPostBusinessEvent");
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(false).currencyCode("USD"));
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 600.0, 9.99,
                    4, null);
            Assertions.assertNotNull(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(600), "1 January 2021");

            PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = addLoanCharge(loanId, chargeId, "01 February 2021", 111.0);
            Long chargeId = postLoansLoanIdChargesResponse.getResourceId();

            deleteAllExternalEvents();
            // resourceId is chargeId
            Long transactionId = loanTransactionHelper
                    .chargeAdjustment(loanId, chargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(111.0).locale("en"))
                    .getSubResourceId();

            List<ExternalEventResponse> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            List<ExternalEventResponse> list = allExternalEvents.stream()
                    .filter(x -> "LoanChargeAdjustmentPostBusinessEvent".equals(x.getType())).toList();
            Assertions.assertEquals(1, list.size());
            ExternalEventResponse event = list.get(0);
            Object loanChargePaidByList = event.getPayLoad().get("loanChargePaidByList");
            Assertions.assertInstanceOf(List.class, loanChargePaidByList);
            Assertions.assertEquals(1, ((List<?>) loanChargePaidByList).size());
            Map<?, ?> chargePaidBy = (Map<?, ?>) ((List<?>) loanChargePaidByList).get(0);
            Assertions.assertInstanceOf(Map.class, chargePaidBy);
            Assertions.assertEquals(111.0D, chargePaidBy.get("amount"));
            Assertions.assertEquals(chargeId.doubleValue(), chargePaidBy.get("chargeId"));
            Assertions.assertEquals(transactionId.doubleValue(), chargePaidBy.get("transactionId"));
        });
    }

    /**
     * interest bearing progressive loan without interest recalculation Verify that
     * LoanChargeAdjustmentPostBusinessEvent has loanChargePaidByList populated when Charge Adjustment posted for
     * partial charge amount Verify cant post more than charge amount
     */
    @Test
    public void verifyLoanChargeAdjustmentPostBusinessEvent05() {
        runAt("1 January 2021", () -> {
            externalEventHelper.enableBusinessEvent("LoanChargeAdjustmentPostBusinessEvent");
            externalEventHelper.enableBusinessEvent("LoanTransactionMakeRepaymentPostBusinessEvent");
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(false).currencyCode("USD"));
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 600.0, 9.99,
                    4, null);
            Assertions.assertNotNull(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(600), "1 January 2021");

            PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = addLoanCharge(loanId, chargeId, "01 February 2021", 111.0);
            Long chargeId = postLoansLoanIdChargesResponse.getResourceId();

            // check first part
            deleteAllExternalEvents();
            // resourceId is chargeId
            Long transactionId = loanTransactionHelper
                    .chargeAdjustment(loanId, chargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(69.0).locale("en"))
                    .getSubResourceId();

            List<ExternalEventResponse> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            List<ExternalEventResponse> list = allExternalEvents.stream()
                    .filter(x -> "LoanChargeAdjustmentPostBusinessEvent".equals(x.getType())).toList();
            Assertions.assertEquals(1, list.size());
            ExternalEventResponse event = list.get(0);
            Object loanChargePaidByList = event.getPayLoad().get("loanChargePaidByList");
            Assertions.assertInstanceOf(List.class, loanChargePaidByList);
            Assertions.assertEquals(1, ((List<?>) loanChargePaidByList).size());
            Map<?, ?> chargePaidBy = (Map<?, ?>) ((List<?>) loanChargePaidByList).get(0);
            Assertions.assertInstanceOf(Map.class, chargePaidBy);
            Assertions.assertEquals(69.0D, chargePaidBy.get("amount"));
            Assertions.assertEquals(chargeId.doubleValue(), chargePaidBy.get("chargeId"));
            Assertions.assertEquals(transactionId.doubleValue(), chargePaidBy.get("transactionId"));

            // check second part
            deleteAllExternalEvents();
            // resourceId is chargeId
            transactionId = loanTransactionHelper
                    .chargeAdjustment(loanId, chargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(42.0).locale("en"))
                    .getSubResourceId();

            allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            list = allExternalEvents.stream().filter(x -> "LoanChargeAdjustmentPostBusinessEvent".equals(x.getType())).toList();
            Assertions.assertEquals(1, list.size());
            event = list.get(0);
            loanChargePaidByList = event.getPayLoad().get("loanChargePaidByList");
            Assertions.assertInstanceOf(List.class, loanChargePaidByList);
            Assertions.assertEquals(1, ((List<?>) loanChargePaidByList).size());
            chargePaidBy = (Map<?, ?>) ((List<?>) loanChargePaidByList).get(0);
            Assertions.assertInstanceOf(Map.class, chargePaidBy);
            Assertions.assertEquals(42.0D, chargePaidBy.get("amount"));
            Assertions.assertEquals(chargeId.doubleValue(), chargePaidBy.get("chargeId"));
            Assertions.assertEquals(transactionId.doubleValue(), chargePaidBy.get("transactionId"));

            // check that third part cannot post for that charge, because it already fully adjusted
            Assertions.assertThrows(RuntimeException.class, () -> loanTransactionHelper.chargeAdjustment(loanId, chargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(1.0).locale("en")));
        });
    }

    /**
     * interest bearing progressive loan without interest recalculation Verify Repayment pays charge and repayment's
     * event has loanChargePaidByList populated Verify that LoanChargeAdjustmentPostBusinessEvent has
     * loanChargePaidByList not populated when Charge Adjustment posted for partial charge amount Verify cant post more
     * than charge amount
     */
    @Test
    public void verifyLoanChargeAdjustmentPostBusinessEvent06() {
        runAt("1 January 2021", () -> {
            externalEventHelper.enableBusinessEvent("LoanChargeAdjustmentPostBusinessEvent");
            externalEventHelper.enableBusinessEvent("LoanTransactionMakeRepaymentPostBusinessEvent");
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(false).currencyCode("USD"));
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 600.0, 9.99,
                    4, null);
            Assertions.assertNotNull(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(600), "1 January 2021");

            PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = addLoanCharge(loanId, chargeId, "01 February 2021", 111.0);
            Long chargeId = postLoansLoanIdChargesResponse.getResourceId();

            // check repayment
            deleteAllExternalEvents();
            Long transactionId = loanTransactionHelper.makeLoanRepayment("01 January 2021", 300.0F, loanId.intValue()).getResourceId();

            List<ExternalEventResponse> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            List<ExternalEventResponse> list = allExternalEvents.stream()
                    .filter(x -> "LoanTransactionMakeRepaymentPostBusinessEvent".equals(x.getType())).toList();
            Assertions.assertEquals(1, list.size());
            ExternalEventResponse event = list.get(0);
            log.info(event.toString());
            Object loanChargePaidByList = event.getPayLoad().get("loanChargePaidByList");
            Assertions.assertEquals(111.0D, event.getPayLoad().get("feeChargesPortion"));
            Assertions.assertInstanceOf(List.class, loanChargePaidByList);
            Assertions.assertEquals(1, ((List<?>) loanChargePaidByList).size());
            Map<?, ?> chargePaidBy = (Map<?, ?>) ((List<?>) loanChargePaidByList).get(0);
            Assertions.assertInstanceOf(Map.class, chargePaidBy);
            Assertions.assertEquals(111.0D, chargePaidBy.get("amount"));
            Assertions.assertEquals(chargeId.doubleValue(), chargePaidBy.get("chargeId"));
            Assertions.assertEquals(transactionId.doubleValue(), chargePaidBy.get("transactionId"));

            // check first part
            deleteAllExternalEvents();
            // resourceId is chargeId
            transactionId = loanTransactionHelper
                    .chargeAdjustment(loanId, chargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(69.0).locale("en"))
                    .getSubResourceId();

            allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            list = allExternalEvents.stream().filter(x -> "LoanChargeAdjustmentPostBusinessEvent".equals(x.getType())).toList();
            Assertions.assertEquals(1, list.size());
            event = list.get(0);
            loanChargePaidByList = event.getPayLoad().get("loanChargePaidByList");
            Assertions.assertInstanceOf(List.class, loanChargePaidByList);
            Assertions.assertEquals(0, ((List<?>) loanChargePaidByList).size());

            // check second part
            deleteAllExternalEvents();
            // resourceId is chargeId
            transactionId = loanTransactionHelper
                    .chargeAdjustment(loanId, chargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(42.0).locale("en"))
                    .getSubResourceId();

            allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            list = allExternalEvents.stream().filter(x -> "LoanChargeAdjustmentPostBusinessEvent".equals(x.getType())).toList();
            Assertions.assertEquals(1, list.size());
            event = list.get(0);
            loanChargePaidByList = event.getPayLoad().get("loanChargePaidByList");
            Assertions.assertInstanceOf(List.class, loanChargePaidByList);
            Assertions.assertEquals(0, ((List<?>) loanChargePaidByList).size());

            // check that third part cannot post for that charge, because it already fully adjusted
            Assertions.assertThrows(RuntimeException.class, () -> loanTransactionHelper.chargeAdjustment(loanId, chargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(1.0).locale("en")));
        });
    }

    /**
     * progressive loan without interest Verify that LoanChargeAdjustmentPostBusinessEvent has loanChargePaidByList
     * populated when Charge Adjustment posted for whole charge amount
     */
    @Test
    public void verifyLoanChargeAdjustmentPostBusinessEvent07() {
        runAt("1 January 2021", () -> {
            externalEventHelper.enableBusinessEvent("LoanChargeAdjustmentPostBusinessEvent");
            externalEventHelper.enableBusinessEvent("LoanTransactionMakeRepaymentPostBusinessEvent");
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(false).currencyCode("USD"));
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 600.0, 0.0, 4,
                    null);
            Assertions.assertNotNull(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(600), "1 January 2021");

            PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = addLoanCharge(loanId, chargeId, "01 February 2021", 111.0);
            Long chargeId = postLoansLoanIdChargesResponse.getResourceId();

            deleteAllExternalEvents();
            // resourceId is chargeId
            Long transactionId = loanTransactionHelper
                    .chargeAdjustment(loanId, chargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(111.0).locale("en"))
                    .getSubResourceId();

            List<ExternalEventResponse> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            List<ExternalEventResponse> list = allExternalEvents.stream()
                    .filter(x -> "LoanChargeAdjustmentPostBusinessEvent".equals(x.getType())).toList();
            Assertions.assertEquals(1, list.size());
            ExternalEventResponse event = list.get(0);
            Object loanChargePaidByList = event.getPayLoad().get("loanChargePaidByList");
            Assertions.assertInstanceOf(List.class, loanChargePaidByList);
            Assertions.assertEquals(1, ((List<?>) loanChargePaidByList).size());
            Map<?, ?> chargePaidBy = (Map<?, ?>) ((List<?>) loanChargePaidByList).get(0);
            Assertions.assertInstanceOf(Map.class, chargePaidBy);
            Assertions.assertEquals(111.0D, chargePaidBy.get("amount"));
            Assertions.assertEquals(chargeId.doubleValue(), chargePaidBy.get("chargeId"));
            Assertions.assertEquals(transactionId.doubleValue(), chargePaidBy.get("transactionId"));
        });
    }

    /**
     * progressive loan without interest Verify that LoanChargeAdjustmentPostBusinessEvent has loanChargePaidByList
     * populated when Charge Adjustment posted for partial charge amount Verify cant post more than charge amount
     */
    @Test
    public void verifyLoanChargeAdjustmentPostBusinessEvent08() {
        runAt("1 January 2021", () -> {
            externalEventHelper.enableBusinessEvent("LoanChargeAdjustmentPostBusinessEvent");
            externalEventHelper.enableBusinessEvent("LoanTransactionMakeRepaymentPostBusinessEvent");
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(false).currencyCode("USD"));
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 600.0, 0.0, 4,
                    null);
            Assertions.assertNotNull(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(600), "1 January 2021");

            PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = addLoanCharge(loanId, chargeId, "01 February 2021", 111.0);
            Long chargeId = postLoansLoanIdChargesResponse.getResourceId();

            // check first part
            deleteAllExternalEvents();
            // resourceId is chargeId
            Long transactionId = loanTransactionHelper
                    .chargeAdjustment(loanId, chargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(69.0).locale("en"))
                    .getSubResourceId();

            List<ExternalEventResponse> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            List<ExternalEventResponse> list = allExternalEvents.stream()
                    .filter(x -> "LoanChargeAdjustmentPostBusinessEvent".equals(x.getType())).toList();
            Assertions.assertEquals(1, list.size());
            ExternalEventResponse event = list.get(0);
            Object loanChargePaidByList = event.getPayLoad().get("loanChargePaidByList");
            Assertions.assertInstanceOf(List.class, loanChargePaidByList);
            Assertions.assertEquals(1, ((List<?>) loanChargePaidByList).size());
            Map<?, ?> chargePaidBy = (Map<?, ?>) ((List<?>) loanChargePaidByList).get(0);
            Assertions.assertInstanceOf(Map.class, chargePaidBy);
            Assertions.assertEquals(69.0D, chargePaidBy.get("amount"));
            Assertions.assertEquals(chargeId.doubleValue(), chargePaidBy.get("chargeId"));
            Assertions.assertEquals(transactionId.doubleValue(), chargePaidBy.get("transactionId"));

            // check second part
            deleteAllExternalEvents();
            // resourceId is chargeId
            transactionId = loanTransactionHelper
                    .chargeAdjustment(loanId, chargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(42.0).locale("en"))
                    .getSubResourceId();

            allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            list = allExternalEvents.stream().filter(x -> "LoanChargeAdjustmentPostBusinessEvent".equals(x.getType())).toList();
            Assertions.assertEquals(1, list.size());
            event = list.get(0);
            loanChargePaidByList = event.getPayLoad().get("loanChargePaidByList");
            Assertions.assertInstanceOf(List.class, loanChargePaidByList);
            Assertions.assertEquals(1, ((List<?>) loanChargePaidByList).size());
            chargePaidBy = (Map<?, ?>) ((List<?>) loanChargePaidByList).get(0);
            Assertions.assertInstanceOf(Map.class, chargePaidBy);
            Assertions.assertEquals(42.0D, chargePaidBy.get("amount"));
            Assertions.assertEquals(chargeId.doubleValue(), chargePaidBy.get("chargeId"));
            Assertions.assertEquals(transactionId.doubleValue(), chargePaidBy.get("transactionId"));

            // check that third part cannot post for that charge, because it already fully adjusted
            Assertions.assertThrows(RuntimeException.class, () -> loanTransactionHelper.chargeAdjustment(loanId, chargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(1.0).locale("en")));
        });
    }

    /**
     * progressive loan without interest Verify Repayment pays charge and repayment's event has loanChargePaidByList
     * populated Verify that LoanChargeAdjustmentPostBusinessEvent has loanChargePaidByList not populated when Charge
     * Adjustment posted for partial charge amount Verify cant post more than charge amount
     */
    @Test
    public void verifyLoanChargeAdjustmentPostBusinessEvent09() {
        runAt("1 January 2021", () -> {
            externalEventHelper.enableBusinessEvent("LoanChargeAdjustmentPostBusinessEvent");
            externalEventHelper.enableBusinessEvent("LoanTransactionMakeRepaymentPostBusinessEvent");
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(false).currencyCode("USD"));
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2021", 600.0, 0.0, 4,
                    null);
            Assertions.assertNotNull(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(600), "1 January 2021");

            PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = addLoanCharge(loanId, chargeId, "01 February 2021", 111.0);
            Long chargeId = postLoansLoanIdChargesResponse.getResourceId();

            // check repayment
            deleteAllExternalEvents();
            Long transactionId = loanTransactionHelper.makeLoanRepayment("01 January 2021", 300.0F, loanId.intValue()).getResourceId();

            List<ExternalEventResponse> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            List<ExternalEventResponse> list = allExternalEvents.stream()
                    .filter(x -> "LoanTransactionMakeRepaymentPostBusinessEvent".equals(x.getType())).toList();
            Assertions.assertEquals(1, list.size());
            ExternalEventResponse event = list.get(0);
            log.info(event.toString());
            Object loanChargePaidByList = event.getPayLoad().get("loanChargePaidByList");
            Assertions.assertEquals(111.0D, event.getPayLoad().get("feeChargesPortion"));
            Assertions.assertInstanceOf(List.class, loanChargePaidByList);
            Assertions.assertEquals(1, ((List<?>) loanChargePaidByList).size());
            Map<?, ?> chargePaidBy = (Map<?, ?>) ((List<?>) loanChargePaidByList).get(0);
            Assertions.assertInstanceOf(Map.class, chargePaidBy);
            Assertions.assertEquals(111.0D, chargePaidBy.get("amount"));
            Assertions.assertEquals(chargeId.doubleValue(), chargePaidBy.get("chargeId"));
            Assertions.assertEquals(transactionId.doubleValue(), chargePaidBy.get("transactionId"));

            // check first part
            deleteAllExternalEvents();
            // resourceId is chargeId
            transactionId = loanTransactionHelper
                    .chargeAdjustment(loanId, chargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(69.0).locale("en"))
                    .getSubResourceId();

            allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            list = allExternalEvents.stream().filter(x -> "LoanChargeAdjustmentPostBusinessEvent".equals(x.getType())).toList();
            Assertions.assertEquals(1, list.size());
            event = list.get(0);
            loanChargePaidByList = event.getPayLoad().get("loanChargePaidByList");
            Assertions.assertInstanceOf(List.class, loanChargePaidByList);
            Assertions.assertEquals(0, ((List<?>) loanChargePaidByList).size());

            // check second part
            deleteAllExternalEvents();
            // resourceId is chargeId
            transactionId = loanTransactionHelper
                    .chargeAdjustment(loanId, chargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(42.0).locale("en"))
                    .getSubResourceId();

            allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            list = allExternalEvents.stream().filter(x -> "LoanChargeAdjustmentPostBusinessEvent".equals(x.getType())).toList();
            Assertions.assertEquals(1, list.size());
            event = list.get(0);
            loanChargePaidByList = event.getPayLoad().get("loanChargePaidByList");
            Assertions.assertInstanceOf(List.class, loanChargePaidByList);
            Assertions.assertEquals(0, ((List<?>) loanChargePaidByList).size());

            // check that third part cannot post for that charge, because it already fully adjusted
            Assertions.assertThrows(RuntimeException.class, () -> loanTransactionHelper.chargeAdjustment(loanId, chargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(1.0).locale("en")));
        });
    }

    /**
     * Using Interest bearing Progressive Loan, Accrual Activity Posting, InterestRecalculation, 25% yearly interest 6
     * repayment 450 USD principal.
     * <li>apply, approve and disburse backdated on 17 August 2024</li>
     * <li>repay 600 on 17 January 2025</li>
     * <li>verify Accrual and Accrual Activity transaction creation</li>
     * <li>verify that the loan become overpaid</li>
     * <li>reverse repayment on same day</li>
     * <li>verify there is no reverse replayed transaction during reversing the repayment</li>
     * <li>verify transaction reversals</li>
     */
    @Test
    public void testInterestBearingProgressiveInterestRecalculationReopenDueReverseRepayment() {
        runAt("17 January 2025", () -> {
            externalEventHelper.enableBusinessEvent("LoanAdjustTransactionBusinessEvent");
            final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive() //
                    .description("Interest bearing Progressive Loan USD, Accrual Activity Posting, NO InterestRecalculation") //
                    .enableAccrualActivityPosting(true) //
                    .daysInMonthType(DaysInMonthType.ACTUAL) //
                    .daysInYearType(DaysInYearType.ACTUAL) //
                    .isInterestRecalculationEnabled(false));//
            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applyLP2ProgressiveLoanRequest(client.getClientId(),
                    loanProductsResponse.getResourceId(), "17 August 2024", 450.0, 25.0, 6, null));
            Long loanId = postLoansResponse.getLoanId();
            Assertions.assertNotNull(loanId);
            loanTransactionHelper.approveLoan(loanId, approveLoanRequest(450.0, "17 August 2024"));
            disburseLoan(loanId, BigDecimal.valueOf(450.0), "17 August 2024");
            verifyTransactions(loanId, //
                    transaction(450.0, "Disbursement", "17 August 2024") //
            );
            Long repaymentId = loanTransactionHelper.makeLoanRepayment("17 January 2025", 600.0f, loanId.intValue()).getResourceId();
            Assertions.assertNotNull(repaymentId);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            verifyLoanStatus(loanDetails, GetLoansLoanIdStatus::getOverpaid);
            verifyTransactions(loanId, //
                    transaction(450.0, "Disbursement", "17 August 2024"), //
                    transaction(600.0, "Repayment", "17 January 2025"), //
                    transaction(33.52, "Accrual", "17 January 2025"), //
                    transaction(9.53, "Accrual Activity", "17 September 2024"), //
                    transaction(7.77, "Accrual Activity", "17 October 2024"), //
                    transaction(6.48, "Accrual Activity", "17 November 2024"), //
                    transaction(4.75, "Accrual Activity", "17 December 2024"), //
                    transaction(4.99, "Accrual Activity", "17 January 2025")); //
            deleteAllExternalEvents();
            loanTransactionHelper.reverseRepayment(loanId.intValue(), repaymentId.intValue(), "17 January 2025");

            List<ExternalEventResponse> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            // Verify that there were no reverse-replay event
            List<ExternalEventResponse> list = allExternalEvents.stream() //
                    .filter(x -> "LoanAdjustTransactionBusinessEvent".equals(x.getType()) //
                            && x.getPayLoad().get("newTransactionDetail") != null //
                            && x.getPayLoad().get("transactionToAdjust") != null) //
                    .toList(); //
            Assertions.assertEquals(0, list.size());

            // verify that there were 2 transaction reversal event
            list = allExternalEvents.stream() //
                    .filter(x -> "LoanAdjustTransactionBusinessEvent".equals(x.getType()) //
                            && x.getPayLoad().get("newTransactionDetail") == null //
                            && x.getPayLoad().get("transactionToAdjust") != null) //
                    .toList(); //
            Assertions.assertEquals(2, list.size());
            Assertions.assertTrue((Boolean) ((Map) list.get(0).getPayLoad().get("transactionToAdjust")).get("reversed"));
            Assertions.assertTrue((Boolean) ((Map) list.get(1).getPayLoad().get("transactionToAdjust")).get("reversed"));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            verifyLoanStatus(loanDetails, GetLoansLoanIdStatus::getActive);
            verifyTransactions(loanId, transaction(450.0, "Disbursement", "17 August 2024"), //
                    transaction(33.52, "Accrual", "17 January 2025"), //
                    reversedTransaction(600.0, "Repayment", "17 January 2025"), //
                    transaction(9.53, "Accrual Activity", "17 September 2024"), //
                    transaction(7.77, "Accrual Activity", "17 October 2024"), //
                    transaction(6.48, "Accrual Activity", "17 November 2024"), //
                    transaction(4.75, "Accrual Activity", "17 December 2024")); //
        });
    }

    @Test
    public void verifyInterestRefundPostBusinessEventCreatedForMerchantIssuedRefundWithInterestRefund() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        enableLoanInterestRefundPstBusinessEvent(true);
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

            deleteAllExternalEvents();

            PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper
                    .makeLoanRepayment("MerchantIssuedRefund", "22 January 2021", 1000F, loanId.intValue());
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse);
            Assertions.assertNotNull(postLoansLoanIdTransactionsResponse.getResourceId());

            verifyBusinessEvents(new LoanTransactionBusinessEvent("LoanTransactionInterestRefundPostBusinessEvent", "22 January 2021", 5.75,
                    0.0, 5.75, 0.0, 0.0, 0.0));
        });
        enableLoanInterestRefundPstBusinessEvent(false);
    }

    @Test
    public void testExternalBusinessEventLoanRescheduledDueAdjustScheduleBusinessEventInterestChange() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("01 March 2024", () -> {
            enableLoanRescheduledDueAdjustScheduleBusinessEvent();
            Long loanId = applyForLoanApplicationWithInterest(client.getClientId(), loanProductId, BigDecimal.valueOf(4000), "1 March 2023",
                    "1 March 2024");
            loanIdRef.set(loanId);
            loanTransactionHelper.approveLoan("1 March 2024", loanId.intValue());

            loanTransactionHelper.disburseLoan("1 March 2024", loanId.intValue(), "400", null);

            PostCreateRescheduleLoansResponse rescheduleLoansResponse = loanRescheduleRequestHelper
                    .createLoanRescheduleRequest(new PostCreateRescheduleLoansRequest().loanId(loanIdRef.get()).dateFormat(DATETIME_PATTERN)
                            .locale("en").submittedOnDate("1 March 2024").newInterestRate(BigDecimal.ONE).rescheduleReasonId(1L)
                            .rescheduleFromDate("1 April 2024"));

            deleteAllExternalEvents();

            loanRescheduleRequestHelper.approveLoanRescheduleRequest(rescheduleLoansResponse.getResourceId(),
                    new PostUpdateRescheduleLoansRequest().approvedOnDate("1 March 2024").locale("en").dateFormat(DATETIME_PATTERN));

            verifyBusinessEvents(new LoanBusinessEvent("LoanRescheduledDueAdjustScheduleBusinessEvent", "01 March 2024", 300, 400.0, 400.0,
                    List.of("interestRateForInstallment")));
        });
    }

    @Test
    public void testProgressiveLoanReverseReplayChargeOffEvents() {
        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> repaymentTransactionIdRef = new AtomicReference<>();

        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());

        runAt("01 January 2025", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "01 January 2025",
                    1000.0, 7.0, 6, null);

            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(1000), "01 January 2025");
        });
        runAt("01 February 2025", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);
            final PostLoansLoanIdTransactionsResponse postLoansLoanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(loanId,
                    "Repayment", "01 February 2025", 260.0);
            repaymentTransactionIdRef.set(postLoansLoanIdTransactionsResponse.getResourceId());
        });
        runAt("04 February 2025", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);
            chargeOffLoan(loanId, "04 February 2025");
        });
        runAt("05 February 2025", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            configureLoanAdjustTransactionBusinessEvent(true);
            configureLoanAccrualTransactionCreatedBusinessEvent(true);
            deleteAllExternalEvents();

            loanTransactionHelper.reverseLoanTransaction(loanId, repaymentTransactionIdRef.get(), "01 February 2025");

            List<ExternalEventResponse> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            // Verify no BulkEvent was created
            Assertions.assertEquals(0, allExternalEvents.stream().filter(e -> e.getType().equals("BulkBusinessEvent")).count());
            verifyBusinessEvents(//
                    new LoanAdjustTransactionBusinessEvent("LoanAdjustTransactionBusinessEvent", "05 February 2025",
                            "loanTransactionType.chargeOff", "2025-02-04"), //
                    new LoanAdjustTransactionBusinessEvent("LoanAdjustTransactionBusinessEvent", "05 February 2025",
                            "loanTransactionType.repayment", "2025-02-01"), //
                    new LoanTransactionBusinessEvent("LoanAccrualTransactionCreatedBusinessEvent", "05 February 2025", 0.15, 0.0, 0.0, 0.15,
                            0.0, 0.0)//
            );
        });
    }

    @Test
    public void verifyLoanApplicationModifiedBusinessEvent01() {
        runAt("1 March 2024", () -> {

            externalEventHelper.enableBusinessEvent("LoanApplicationModifiedBusinessEvent");

            PostLoansRequest loanRequest = applyForLoanApplication(client.getClientId(), loanProductId, BigDecimal.valueOf(4000),
                    "1 March 2023", "1 March 2024");
            PostLoansResponse applicationResponse = loanTransactionHelper.applyLoan(loanRequest);
            Long loanId = applicationResponse.getResourceId();
            Assertions.assertNotNull(loanId);

            PutLoansLoanIdRequest modification = new PutLoansLoanIdRequest().clientId(client.getClientId()).productId(loanProductId)
                    .transactionProcessingStrategyCode(DEFAULT_STRATEGY).interestRatePerPeriod(BigDecimal.valueOf(2)).repaymentEvery(1)
                    .principal(550L).amortizationType(1).interestType(1).interestCalculationPeriodType(0)
                    .expectedDisbursementDate("1 March 2024").repaymentFrequencyType(2).numberOfRepayments(4).loanTermFrequency(4)
                    .loanTermFrequencyType(2).loanType("individual").dateFormat("dd MMMM yyyy").locale("en_GB");

            loanTransactionHelper.modifyApplicationForLoan(loanId, "modify", modification);

            List<ExternalEventResponse> modifiedEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec).stream()
                    .filter(e -> "LoanApplicationModifiedBusinessEvent".equals(e.getType())).toList();

            Assertions.assertEquals(1, modifiedEvents.size());
            ExternalEventResponse event = modifiedEvents.get(0);

            Assertions.assertEquals(550.0D, event.getPayLoad().get("principal")); // the new principal
        });
    }

    @Test
    public void verifyLoanWithdrawnByApplicantBusinessEvent01() {
        runAt("01 March 2024", () -> {

            externalEventHelper.enableBusinessEvent("LoanWithdrawnByApplicantBusinessEvent");

            PostLoansRequest loanRequest = applyForLoanApplication(client.getClientId(), loanProductId, BigDecimal.valueOf(4000),
                    "1 March 2023", "01 March 2024");
            PostLoansResponse applicationResponse = loanTransactionHelper.applyLoan(loanRequest);
            Long loanId = applicationResponse.getLoanId();
            Assertions.assertNotNull(loanId);

            loanTransactionHelper.withdrawLoanApplicationByClient("01 March 2024", loanId.intValue());

            List<ExternalEventResponse> events = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec).stream()
                    .filter(e -> "LoanWithdrawnByApplicantBusinessEvent".equals(e.getType())).toList();

            Assertions.assertEquals(1, events.size());
        });
    }

    @Nested
    class ExternalIdGenerationTest {

        Boolean actualConfiguration = null;

        @BeforeEach
        void setUpEnableExternalIdGenerationIfActuallyDisabled() {
            if (actualConfiguration == null) {
                GlobalConfigurationPropertyData globalConfigurationByName = globalConfigurationHelper
                        .getGlobalConfigurationByName(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID);
                if (globalConfigurationByName != null) {
                    actualConfiguration = globalConfigurationByName.getEnabled();
                    Assertions.assertNotNull(actualConfiguration);
                }
            }
            if (!actualConfiguration) {
                globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            }
        }

        @AfterEach
        void tearDownDisableExternalIdGenerationIfPreviouslyDisabled() {
            if (!actualConfiguration) {
                globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
            }
        }

        @Test
        public void testInterestPaymentWaiverNotReverseReplayOnCreationAndHasGeneratedExternalId() {
            externalEventHelper.enableBusinessEvent("LoanAdjustTransactionBusinessEvent");
            AtomicReference<Long> loanIdRef = new AtomicReference<>();
            runAt("15 January 2025", () -> {
                PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(create4IProgressive()
                        .isInterestRecalculationEnabled(true).recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY)
                        .recalculationRestFrequencyInterval(1));

                Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductResponse.getResourceId(), "15 January 2025",
                        430.0, 9.9, 4, null);
                loanIdRef.set(loanId);

                loanTransactionHelper.disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("15 January 2025")
                        .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(430.0)).locale("en"));

                verifyTransactions(loanId, transaction(430.0, "Disbursement", "15 January 2025") //
                );

                verifyRepaymentSchedule(loanId, //
                        installment(430.0, null, "15 January 2025"), //
                        unpaidInstallment(106.18, 3.55, "15 February 2025"), //
                        unpaidInstallment(107.06, 2.67, "15 March 2025"), //
                        unpaidInstallment(107.94, 1.79, "15 April 2025"), //
                        unpaidInstallment(108.82, 0.9, "15 May 2025") //
                );
            });
            runAt("16 January 2025", () -> {
                Long loanId = loanIdRef.get();
                executeInlineCOB(loanId);
                verifyTransactions(loanId, transaction(430.0, "Disbursement", "15 January 2025") //
                );
            });
            runAt("17 January 2025", () -> {
                Long loanId = loanIdRef.get();
                executeInlineCOB(loanId);
                verifyTransactions(loanId, transaction(430.0, "Disbursement", "15 January 2025"), //
                        transaction(0.11, "Accrual", "16 January 2025"));
                deleteAllExternalEvents();
                PostLoansLoanIdTransactionsResponse interestPaymentWaiverResponse = loanTransactionHelper.makeLoanRepayment(loanId,
                        "InterestPaymentWaiver", "17 January 2025", 10.0);

                List<ExternalEventResponse> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
                List<ExternalEventResponse> adjustments = allExternalEvents.stream()
                        .filter(e -> "LoanAdjustTransactionBusinessEvent".equals(e.getType())).toList();
                Assertions.assertEquals(0, adjustments.size());
                Assertions.assertNotNull(interestPaymentWaiverResponse);
                Assertions.assertNotNull(interestPaymentWaiverResponse.getResourceExternalId());

                verifyTransactions(loanId, transaction(430.0, "Disbursement", "15 January 2025"), //
                        transaction(10.0, "Interest Payment Waiver", "17 January 2025"), //
                        transaction(0.11, "Accrual", "16 January 2025"));
                verifyRepaymentSchedule(loanId, //
                        installment(430.0, null, "15 January 2025"), //
                        installment(106.26, 3.47, 99.73, false, "15 February 2025"), //
                        unpaidInstallment(107.06, 2.67, "15 March 2025"), //
                        unpaidInstallment(107.94, 1.79, "15 April 2025"), //
                        unpaidInstallment(108.74, 0.9, "15 May 2025") //
                );
            });
        }
    }

    private void enableLoanBalanceChangedBusinessEvent() {
        externalEventHelper.enableBusinessEvent("LoanBalanceChangedBusinessEvent");
    }

    private void enableLoanRescheduledDueAdjustScheduleBusinessEvent() {
        externalEventHelper.enableBusinessEvent("LoanRescheduledDueAdjustScheduleBusinessEvent");
    }

    private void disableLoanBalanceChangedBusinessEvent() {
        externalEventHelper.disableBusinessEvent("LoanBalanceChangedBusinessEvent");
    }

    private static Long createLoanProductPeriodicWithInterest() {
        String name = Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6);
        String shortName = Utils.uniqueRandomStringGenerator("", 4);
        Long resourceId = loanTransactionHelper.createLoanProduct(new PostLoanProductsRequest() //
                .name(name) //
                .shortName(shortName) //
                .multiDisburseLoan(true) //
                .maxTrancheCount(2) //
                .interestType(InterestType.DECLINING_BALANCE) //
                .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY) //
                .disallowExpectedDisbursements(true) //
                .description("Test loan description") //
                .currencyCode("USD") //
                .digitsAfterDecimal(2) //
                .daysInYearType(DaysInYearType.ACTUAL) //
                .daysInMonthType(DaysInYearType.ACTUAL) //
                .interestRecalculationCompoundingMethod(0) //
                .recalculationRestFrequencyType(1) //
                .rescheduleStrategyMethod(1) //
                .recalculationRestFrequencyInterval(0) //
                .isInterestRecalculationEnabled(false) //
                .interestRateFrequencyType(2) //
                .locale("en_GB") //
                .numberOfRepayments(4) //
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue()) //
                .interestRatePerPeriod(2.0) //
                .repaymentEvery(1) //
                .minPrincipal(100.0) //
                .principal(1000.0) //
                .maxPrincipal(10000000.0) //
                .amortizationType(AmortizationType.EQUAL_INSTALLMENTS) //
                .dateFormat(DATETIME_PATTERN) //
                .transactionProcessingStrategyCode(DEFAULT_STRATEGY) //
                .accountingRule(1)) //
                .getResourceId();
        log.info("Test MultiDisburse Loan Product With Interest. loanProductId: {}", resourceId);
        return resourceId;
    }

    private static Long applyForLoanApplicationWithInterest(final Long clientId, final Long loanProductId, BigDecimal principal,
            String submittedOnDate, String expectedDisburmentDate) {
        final PostLoansRequest loanRequest = new PostLoansRequest() //
                .loanTermFrequency(4).locale("en_GB").loanTermFrequencyType(2).numberOfRepayments(4).repaymentFrequencyType(2)
                .interestRatePerPeriod(BigDecimal.valueOf(2)).repaymentEvery(1).principal(principal).amortizationType(1).interestType(1)
                .interestCalculationPeriodType(0).dateFormat("dd MMMM yyyy").transactionProcessingStrategyCode(DEFAULT_STRATEGY)
                .loanType("individual").submittedOnDate(submittedOnDate).expectedDisbursementDate(expectedDisburmentDate).clientId(clientId)
                .productId(loanProductId);
        Long loanId = loanTransactionHelper.applyLoan(loanRequest).getLoanId();
        log.info("Test MultiDisbursed Loan with Interest. clientId: {} loanId: {}", client.getClientId(), loanId);
        return loanId;
    }

    private static PostLoansRequest applyForLoanApplication(final Long clientId, final Long loanProductId, BigDecimal principal,
            String submittedOnDate, String expectedDisburmentDate) {
        final PostLoansRequest loanRequest = new PostLoansRequest() //
                .loanTermFrequency(4).locale("en_GB").loanTermFrequencyType(2).numberOfRepayments(4).repaymentFrequencyType(2)
                .interestRatePerPeriod(BigDecimal.valueOf(2)).repaymentEvery(1).principal(principal).amortizationType(1).interestType(1)
                .interestCalculationPeriodType(0).dateFormat("dd MMMM yyyy").transactionProcessingStrategyCode(DEFAULT_STRATEGY)
                .loanType("individual").submittedOnDate(submittedOnDate).expectedDisbursementDate(expectedDisburmentDate).clientId(clientId)
                .productId(loanProductId);
        return loanRequest;
    }

    private void enableLoanInterestRefundPstBusinessEvent(boolean enabled) {
        externalEventHelper.configureBusinessEvent("LoanTransactionInterestRefundPostBusinessEvent", enabled);
    }

    private void configureLoanAdjustTransactionBusinessEvent(boolean enabled) {
        externalEventHelper.configureBusinessEvent("LoanAdjustTransactionBusinessEvent", enabled);
    }

    private void configureLoanAccrualTransactionCreatedBusinessEvent(boolean enabled) {
        externalEventHelper.configureBusinessEvent("LoanAccrualTransactionCreatedBusinessEvent", enabled);
    }
}
