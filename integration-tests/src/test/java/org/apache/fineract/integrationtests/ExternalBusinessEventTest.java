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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.infrastructure.event.external.service.validation.ExternalEventDTO;
import org.apache.fineract.integrationtests.common.BusinessStepHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.ExternalEventConfigurationHelper;
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

            verifyBusinessEvents(new BusinessEvent("LoanBalanceChangedBusinessEvent", "15 March 2023", 300, 400.0, 291.04));
        });
        runAt("1 April 2023", () -> {

            loanTransactionHelper.disburseLoan("1 April 2023", loanIdRef.get().intValue(), "600", null);

        });
        runAt("15 April 2023", () -> {
            deleteAllExternalEvents();

            loanTransactionHelper.makeLoanRepayment("15 April 2023", 125.0F, loanIdRef.get().intValue());

            verifyBusinessEvents(new BusinessEvent("LoanBalanceChangedBusinessEvent", "15 April 2023", 300, 1000.0, 770.06));

            deleteAllExternalEvents();

            Long transactionId = loanTransactionHelper.makeLoanRepayment("15 April 2023", 1000F, loanIdRef.get().intValue())
                    .getResourceId();
            Assertions.assertNotNull(transactionId);

            verifyBusinessEvents(new BusinessEvent("LoanBalanceChangedBusinessEvent", "15 April 2023", 700, 1000.0, 0.0));

            deleteAllExternalEvents();

            loanTransactionHelper.reverseRepayment(loanIdRef.get().intValue(), transactionId.intValue(), "15 April 2023");

            verifyBusinessEvents(new BusinessEvent("LoanBalanceChangedBusinessEvent", "15 April 2023", 300, 1000.0, 770.06));

            deleteAllExternalEvents();

            loanTransactionHelper.makeLoanRepayment("15 April 2023", 830.22F, loanIdRef.get().intValue());

            verifyBusinessEvents(new BusinessEvent("LoanBalanceChangedBusinessEvent", "15 April 2023", 600, 1000.0, 0.0));

            disableLoanBalanceChangedBusinessEvent();
        });
    }

    private static void enableLoanBalanceChangedBusinessEvent() {
        final Map<String, Boolean> updatedConfigurations = ExternalEventConfigurationHelper.updateExternalEventConfigurations(requestSpec,
                responseSpec, "{\"externalEventConfigurations\":{\"LoanBalanceChangedBusinessEvent\":true}}\n");
        Assertions.assertEquals(updatedConfigurations.size(), 1);
        Assertions.assertTrue(updatedConfigurations.containsKey("LoanBalanceChangedBusinessEvent"));
        Assertions.assertTrue(updatedConfigurations.get("LoanBalanceChangedBusinessEvent"));
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
        Long resourceId = loanTransactionHelper.createLoanProduct(new PostLoanProductsRequest().name(name).shortName(shortName)
                .multiDisburseLoan(true).maxTrancheCount(2).interestType(0).interestCalculationPeriodType(0)
                .disallowExpectedDisbursements(true).description("Test loan description").currencyCode("USD").digitsAfterDecimal(2)
                .daysInYearType(1).daysInMonthType(1).interestRecalculationCompoundingMethod(0).recalculationRestFrequencyType(1)
                .rescheduleStrategyMethod(1).recalculationRestFrequencyInterval(0).isInterestRecalculationEnabled(false)
                .interestRateFrequencyType(2).locale("en_GB").numberOfRepayments(4).repaymentFrequencyType(2L).interestRatePerPeriod(2.0)
                .repaymentEvery(1).minPrincipal(100.0).principal(1000.0).maxPrincipal(10000000.0).amortizationType(1)
                .dateFormat(DATETIME_PATTERN).transactionProcessingStrategyCode(DEFAULT_STRATEGY).accountingRule(1)).getResourceId();
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
            log.info("Event Received { type:'{}' businessDate:'{}' }", externalEventDTO.getType(), externalEventDTO.getBusinessDate());
            log.debug(externalEventDTO.toString());
        });
    }

    public void verifyBusinessEvents(BusinessEvent... businessEvents) {
        List<ExternalEventDTO> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
        logBusinessEvents(allExternalEvents);
        Assertions.assertNotNull(businessEvents);
        Assertions.assertNotNull(allExternalEvents);
        Assertions.assertTrue(businessEvents.length <= allExternalEvents.size(),
                "Expected business event count is less than actual. Expected: " + businessEvents.length + " Actual: "
                        + allExternalEvents.size());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN, Locale.ENGLISH);
        for (BusinessEvent businessEvent : businessEvents) {
            long count = allExternalEvents.stream().filter(externalEvent -> {
                Object summaryRes = externalEvent.getPayLoad().get("summary");
                Object statusRes = externalEvent.getPayLoad().get("status");
                Map<String, Object> summary = summaryRes instanceof Map ? (Map<String, Object>) summaryRes : Map.of();
                Map<String, Object> status = statusRes instanceof Map ? (Map<String, Object>) statusRes : Map.of();
                var principalDisbursed = summary.get("principalDisbursed");

                var principalOutstanding = summary.get("principalOutstanding");
                var businessDate = LocalDate.parse(businessEvent.getBusinessDate(), formatter);
                Double statusId = (Double) status.get("id");
                return Objects.equals(externalEvent.getType(), businessEvent.getType())
                        && Objects.equals(externalEvent.getBusinessDate(), businessDate)
                        && Objects.equals(statusId, businessEvent.getStatusId().doubleValue())
                        && Objects.equals(principalDisbursed, businessEvent.getPrincipalDisbursed())
                        && Objects.equals(principalOutstanding, businessEvent.getPrincipalOutstanding());
            }).count();
            Assertions.assertEquals(1, count, "Expected business event not found " + businessEvent);
        }
    }

    @Data
    @AllArgsConstructor
    public static class BusinessEvent {

        private String type;
        private String businessDate;
        private Integer statusId;
        private Double principalDisbursed;
        private Double principalOutstanding;
    }
}
