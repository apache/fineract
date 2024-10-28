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
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostCreateRescheduleLoansRequest;
import org.apache.fineract.client.models.PostCreateRescheduleLoansResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostUpdateRescheduleLoansRequest;
import org.apache.fineract.infrastructure.event.external.service.validation.ExternalEventDTO;
import org.apache.fineract.integrationtests.common.BusinessStepHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.ExternalEventConfigurationHelper;
import org.apache.fineract.integrationtests.common.LoanRescheduleRequestHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.externalevents.ExternalEventHelper;
import org.apache.fineract.integrationtests.common.externalevents.ExternalEventsExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith({ LoanTestLifecycleExtension.class, ExternalEventsExtension.class })
public class ExternalBusinessEventTest extends BaseLoanIntegrationTest {

    private static final String DATETIME_PATTERN = "dd MMMM yyyy";
    private static PostClientsResponse client;
    private static LoanTransactionHelper loanTransactionHelper;
    private static LoanRescheduleRequestHelper loanRescheduleRequestHelper;
    private static Long loanProductId;
    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;

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

    private static void enableLoanBalanceChangedBusinessEvent() {
        final Map<String, Boolean> updatedConfigurations = ExternalEventConfigurationHelper.updateExternalEventConfigurations(requestSpec,
                responseSpec, "{\"externalEventConfigurations\":{\"LoanBalanceChangedBusinessEvent\":true}}\n");
        Assertions.assertEquals(updatedConfigurations.size(), 1);
        Assertions.assertTrue(updatedConfigurations.containsKey("LoanBalanceChangedBusinessEvent"));
        Assertions.assertTrue(updatedConfigurations.get("LoanBalanceChangedBusinessEvent"));
    }

    private static void enableLoanRescheduledDueAdjustScheduleBusinessEvent() {
        final Map<String, Boolean> updatedConfigurations = ExternalEventConfigurationHelper.updateExternalEventConfigurations(requestSpec,
                responseSpec, "{\"externalEventConfigurations\":{\"LoanRescheduledDueAdjustScheduleBusinessEvent\":true}}\n");
        Assertions.assertEquals(updatedConfigurations.size(), 1);
        Assertions.assertTrue(updatedConfigurations.containsKey("LoanRescheduledDueAdjustScheduleBusinessEvent"));
        Assertions.assertTrue(updatedConfigurations.get("LoanRescheduledDueAdjustScheduleBusinessEvent"));
    }

    private static void disableLoanBalanceChangedBusinessEvent() {
        final Map<String, Boolean> updatedConfigurations = ExternalEventConfigurationHelper.updateExternalEventConfigurations(requestSpec,
                responseSpec, "{\"externalEventConfigurations\":{\"LoanBalanceChangedBusinessEvent\":false}}\n");
        Assertions.assertEquals(updatedConfigurations.size(), 1);
        Assertions.assertTrue(updatedConfigurations.containsKey("LoanBalanceChangedBusinessEvent"));
        Assertions.assertFalse(updatedConfigurations.get("LoanBalanceChangedBusinessEvent"));
    }

    private void deleteAllExternalEvents() {
        ExternalEventHelper.deleteAllExternalEvents(requestSpec, createResponseSpecification(Matchers.is(204)));
        List<ExternalEventDTO> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
        Assertions.assertEquals(0, allExternalEvents.size());
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

    private void logBusinessEvents(List<ExternalEventDTO> allExternalEvents) {
        allExternalEvents.forEach(externalEventDTO -> {
            Object amount = externalEventDTO.getPayLoad().get("amount");
            Object outstandingLoanBalance = externalEventDTO.getPayLoad().get("outstandingLoanBalance");
            Object principalPortion = externalEventDTO.getPayLoad().get("principalPortion");
            Object interestPortion = externalEventDTO.getPayLoad().get("interestPortion");
            Object feePortion = externalEventDTO.getPayLoad().get("feeChargesPortion");
            Object penaltyPortion = externalEventDTO.getPayLoad().get("penaltyChargesPortion");
            log.info("Event Received\n type:'{}'\n businessDate:'{}'", externalEventDTO.getType(), externalEventDTO.getBusinessDate());
            log.info(
                    "Values\n amount: {}\n outstandingLoanBalance: {}\n principalPortion: {}\n interestPortion: {}\n feePortion: {}\n penaltyPortion: {}",
                    amount, outstandingLoanBalance, principalPortion, interestPortion, feePortion, penaltyPortion);
        });
    }

    private static void enableLoanInterestRefundPstBusinessEvent(boolean enabled) {
        final Map<String, Boolean> updatedConfigurations = ExternalEventConfigurationHelper.updateExternalEventConfigurations(requestSpec,
                responseSpec, "{\"externalEventConfigurations\":{\"LoanTransactionInterestRefundPostBusinessEvent\":"
                        + (enabled ? "true" : "false") + "}}\n");
        Assertions.assertEquals(updatedConfigurations.size(), 1);
        Assertions.assertTrue(updatedConfigurations.containsKey("LoanTransactionInterestRefundPostBusinessEvent"));
        Assertions.assertEquals(enabled, updatedConfigurations.get("LoanTransactionInterestRefundPostBusinessEvent"));
    }

    public void verifyBusinessEvents(BusinessEvent... businessEvents) {
        List<ExternalEventDTO> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
        logBusinessEvents(allExternalEvents);
        Assertions.assertNotNull(businessEvents);
        Assertions.assertNotNull(allExternalEvents);
        Assertions.assertTrue(businessEvents.length <= allExternalEvents.size(),
                "Expected business event count is less than actual. Expected: " + businessEvents.length + " Actual: "
                        + allExternalEvents.size());
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN, Locale.ENGLISH);
        for (BusinessEvent businessEvent : businessEvents) {
            long count = allExternalEvents.stream().filter(externalEvent -> businessEvent.verify(externalEvent, formatter)).count();
            Assertions.assertEquals(1, count, "Expected business event not found " + businessEvent);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BusinessEvent {

        String type;
        String businessDate;

        boolean verify(@NotNull ExternalEventDTO externalEvent, DateTimeFormatter formatter) {
            var businessDate = LocalDate.parse(getBusinessDate(), formatter);

            return Objects.equals(externalEvent.getType(), getType()) && Objects.equals(externalEvent.getBusinessDate(), businessDate);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    public static class LoanTransactionBusinessEvent extends BusinessEvent {

        private Double amount;
        private Double outstandingLoanBalance;
        private Double principalPortion;
        private Double interestPortion;
        private Double feeChargesPortion;
        private Double penaltyChargesPortion;

        public LoanTransactionBusinessEvent(String type, String businessDate, Double amount, Double outstandingLoanBalance,
                Double principalPortion, Double interestPortion, Double feeChargesPortion, Double penaltyChargesPortion) {
            super(type, businessDate);
            this.amount = amount;
            this.outstandingLoanBalance = outstandingLoanBalance;
            this.principalPortion = principalPortion;
            this.interestPortion = interestPortion;
            this.feeChargesPortion = feeChargesPortion;
            this.penaltyChargesPortion = penaltyChargesPortion;
        }

        @Override
        boolean verify(ExternalEventDTO externalEvent, DateTimeFormatter formatter) {
            Object amount = externalEvent.getPayLoad().get("amount");
            Object outstandingLoanBalance = externalEvent.getPayLoad().get("outstandingLoanBalance");
            Object principalPortion = externalEvent.getPayLoad().get("principalPortion");
            Object interestPortion = externalEvent.getPayLoad().get("interestPortion");
            Object feePortion = externalEvent.getPayLoad().get("feeChargesPortion");
            Object penaltyPortion = externalEvent.getPayLoad().get("penaltyChargesPortion");

            return super.verify(externalEvent, formatter) && Objects.equals(amount, getAmount())
                    && Objects.equals(outstandingLoanBalance, getOutstandingLoanBalance())
                    && Objects.equals(principalPortion, getPrincipalPortion()) && Objects.equals(interestPortion, getInterestPortion())
                    && Objects.equals(feePortion, getFeeChargesPortion()) && Objects.equals(penaltyPortion, getPenaltyChargesPortion());
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    public static class LoanBusinessEvent extends BusinessEvent {

        private Integer statusId;
        private Double principalDisbursed;
        private Double principalOutstanding;
        private List<String> loanTermVariationType;

        public LoanBusinessEvent(String type, String businessDate, Integer statusId, Double principalDisbursed,
                Double principalOutstanding) {
            super(type, businessDate);
            this.statusId = statusId;
            this.principalDisbursed = principalDisbursed;
            this.principalOutstanding = principalOutstanding;
        }

        public LoanBusinessEvent(String type, String businessDate, Integer statusId, Double principalDisbursed, Double principalOutstanding,
                List<String> loanTermVariationType) {
            super(type, businessDate);
            this.statusId = statusId;
            this.principalDisbursed = principalDisbursed;
            this.principalOutstanding = principalOutstanding;
            this.loanTermVariationType = loanTermVariationType;
        }

        @Override
        public boolean verify(ExternalEventDTO externalEvent, DateTimeFormatter formatter) {
            Object summaryRes = externalEvent.getPayLoad().get("summary");
            Object statusRes = externalEvent.getPayLoad().get("status");
            Map<String, Object> summary = summaryRes instanceof Map ? (Map<String, Object>) summaryRes : Map.of();
            Map<String, Object> status = statusRes instanceof Map ? (Map<String, Object>) statusRes : Map.of();
            var principalDisbursed = summary.get("principalDisbursed");

            var principalOutstanding = summary.get("principalOutstanding");
            Double statusId = (Double) status.get("id");
            return super.verify(externalEvent, formatter) && Objects.equals(statusId, getStatusId().doubleValue())
                    && Objects.equals(principalDisbursed, getPrincipalDisbursed())
                    && Objects.equals(principalOutstanding, getPrincipalOutstanding()) && loanTermVariationsMatch(
                            (List<Map<String, Object>>) externalEvent.getPayLoad().get("loanTermVariations"), loanTermVariationType);
        }

        private boolean loanTermVariationsMatch(final List<Map<String, Object>> loanTermVariations, final List<String> expectedTypes) {
            if (CollectionUtils.isEmpty(expectedTypes)) {
                return true;
            }
            final long numberOfMatches = expectedTypes.stream().filter(expectedType -> loanTermVariations.stream().anyMatch(
                    variation -> StringUtils.equals((String) ((Map<String, Object>) variation.get("termType")).get("value"), expectedType)))
                    .count();

            return numberOfMatches == expectedTypes.size();
        }
    }
}
