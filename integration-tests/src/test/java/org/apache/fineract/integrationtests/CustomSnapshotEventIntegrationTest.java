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

import static org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.BUSINESS_DATE;

import com.google.gson.Gson;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.BusinessDateRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.infrastructure.event.external.service.validation.ExternalEventDTO;
import org.apache.fineract.integrationtests.common.BusinessStepHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.ExternalEventConfigurationHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.externalevents.ExternalEventHelper;
import org.apache.fineract.integrationtests.common.externalevents.ExternalEventsExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith({ LoanTestLifecycleExtension.class, ExternalEventsExtension.class })
public class CustomSnapshotEventIntegrationTest extends BaseLoanIntegrationTest {

    private final SchedulerJobHelper schedulerJobHelper = new SchedulerJobHelper(this.requestSpec);

    @Test
    public void testSnapshotEventGenerationWhenLoanInstallmentIsNotPayed() {
        runAt("31 January 2023", () -> {
            // Enable Business Step
            enableCOBBusinessStep("APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION", "CHECK_LOAN_REPAYMENT_DUE",
                    "CHECK_LOAN_REPAYMENT_OVERDUE", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES",
                    "EXTERNAL_ASSET_OWNER_TRANSFER", "CHECK_DUE_INSTALLMENTS");

            enableLoanAccountCustomSnapshotBusinessEvent();

            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            PostLoanProductsRequest loanProductsRequest = create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
                    InterestType.FLAT, AmortizationType.EQUAL_INSTALLMENTS);
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductResponse.getResourceId(), "01 January 2023", 1250.0, 4);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250), "01 January 2023");

            // Verify Repayment Schedule and Due Dates
            verifyRepaymentSchedule(loanId, //
                    installment(1250.0, null, "01 January 2023"), //
                    installment(312.0, false, "31 January 2023"), //
                    installment(312.0, false, "02 March 2023"), //
                    installment(312.0, false, "01 April 2023"), //
                    installment(314.0, false, "01 May 2023") //
            );

            // delete all external events
            deleteAllExternalEvents();

            // run cob
            updateBusinessDateAndExecuteCOBJob("01 February 2023");

            // verify external events
            List<ExternalEventDTO> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            Assertions.assertEquals(1, allExternalEvents.size());
            Assertions.assertEquals("LoanAccountCustomSnapshotBusinessEvent", allExternalEvents.get(0).getType());
            Assertions.assertEquals(loanId, allExternalEvents.get(0).getAggregateRootId());

            // Loan Delinquency data validation
            Map<String, Object> payLoad = (Map<String, Object>) allExternalEvents.get(0).getPayLoad().get("delinquent");
            log.info("Payload: {}", payLoad.toString());

            Assertions.assertNotNull(payLoad.get("delinquentPrincipal"));
            Assertions.assertEquals(312.0, payLoad.get("delinquentPrincipal"));
            Assertions.assertNotNull(payLoad.get("delinquentInterest"));
            Assertions.assertEquals(0.0, payLoad.get("delinquentInterest"));
            Assertions.assertNotNull(payLoad.get("delinquentFee"));
            Assertions.assertEquals(0.0, payLoad.get("delinquentFee"));
            Assertions.assertNotNull(payLoad.get("delinquentPenalty"));
            Assertions.assertEquals(0.0, payLoad.get("delinquentPenalty"));

            payLoad = (Map<String, Object>) allExternalEvents.get(0).getPayLoad().get("summary");
            log.info("Payload: {}", payLoad.toString());
            Assertions.assertNotNull(payLoad.get("totalInterestPaymentWaiver"));
            Assertions.assertEquals(0.0, payLoad.get("totalInterestPaymentWaiver"));
            Assertions.assertNotNull(payLoad.get("totalRepaymentTransactionReversed"));
            Assertions.assertEquals(0.0, payLoad.get("totalRepaymentTransactionReversed"));
        });
    }

    @Test
    public void testNoSnapshotEventGenerationWhenLoanInstallmentIsPayed() {
        runAt("31 January 2023", () -> {
            // Enable Business Step
            enableCOBBusinessStep("APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION", "CHECK_LOAN_REPAYMENT_DUE",
                    "CHECK_LOAN_REPAYMENT_OVERDUE", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES",
                    "EXTERNAL_ASSET_OWNER_TRANSFER", "CHECK_DUE_INSTALLMENTS");

            enableLoanAccountCustomSnapshotBusinessEvent();

            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            PostLoanProductsRequest loanProductsRequest = create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
                    InterestType.FLAT, AmortizationType.EQUAL_INSTALLMENTS);
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductResponse.getResourceId(), "01 January 2023", 1250.0, 4);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250), "01 January 2023");

            // Verify Repayment Schedule and Due Dates
            verifyRepaymentSchedule(loanId, //
                    installment(1250.0, null, "01 January 2023"), //
                    installment(312.0, false, "31 January 2023"), //
                    installment(312.0, false, "02 March 2023"), //
                    installment(312.0, false, "01 April 2023"), //
                    installment(314.0, false, "01 May 2023") //
            );

            addRepaymentForLoan(loanId, 313.0, "31 January 2023");

            // Verify Repayment Schedule and Due Dates
            verifyRepaymentSchedule(loanId, //
                    installment(1250.0, null, "01 January 2023"), //
                    installment(312.0, true, "31 January 2023"), //
                    installment(312.0, false, "02 March 2023"), //
                    installment(312.0, false, "01 April 2023"), //
                    installment(314.0, false, "01 May 2023") //
            );

            // delete all external events
            deleteAllExternalEvents();

            // run cob
            updateBusinessDateAndExecuteCOBJob("01 February 2023");

            // verify external events
            List<ExternalEventDTO> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            Assertions.assertEquals(0, allExternalEvents.size());
        });
    }

    @Test
    public void testNoSnapshotEventGenerationWhenWhenCustomSnapshotEventCOBTaskIsNotActive() {
        runAt("31 January 2023", () -> {
            // Enable Business Step
            enableCOBBusinessStep("APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION", "CHECK_LOAN_REPAYMENT_DUE",
                    "CHECK_LOAN_REPAYMENT_OVERDUE", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES",
                    "EXTERNAL_ASSET_OWNER_TRANSFER");

            enableLoanAccountCustomSnapshotBusinessEvent();

            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            PostLoanProductsRequest loanProductsRequest = create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
                    InterestType.FLAT, AmortizationType.EQUAL_INSTALLMENTS);
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductResponse.getResourceId(), "01 January 2023", 1250.0, 4);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250), "01 January 2023");

            // Verify Repayment Schedule and Due Dates
            verifyRepaymentSchedule(loanId, //
                    installment(1250.0, null, "01 January 2023"), //
                    installment(312.0, false, "31 January 2023"), //
                    installment(312.0, false, "02 March 2023"), //
                    installment(312.0, false, "01 April 2023"), //
                    installment(314.0, false, "01 May 2023") //
            );

            // delete all external events
            deleteAllExternalEvents();

            // run cob
            updateBusinessDateAndExecuteCOBJob("01 February 2023");

            // verify external events
            List<ExternalEventDTO> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            Assertions.assertEquals(0, allExternalEvents.size());
        });
    }

    @Test
    public void testNoSnapshotEventGenerationWhenCOBDateIsNotMatchingWithInstallmentDueDate() {
        runAt("30 January 2023", () -> {
            // Enable Business Step
            enableCOBBusinessStep("APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION", "CHECK_LOAN_REPAYMENT_OVERDUE",
                    "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES", "EXTERNAL_ASSET_OWNER_TRANSFER", "CHECK_DUE_INSTALLMENTS");

            enableLoanAccountCustomSnapshotBusinessEvent();

            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            PostLoanProductsRequest loanProductsRequest = create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
                    InterestType.FLAT, AmortizationType.EQUAL_INSTALLMENTS);
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductResponse.getResourceId(), "01 January 2023", 1250.0, 4);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250), "01 January 2023");

            // Verify Repayment Schedule and Due Dates
            verifyRepaymentSchedule(loanId, //
                    installment(1250.0, null, "01 January 2023"), //
                    installment(312.0, false, "31 January 2023"), //
                    installment(312.0, false, "02 March 2023"), //
                    installment(312.0, false, "01 April 2023"), //
                    installment(314.0, false, "01 May 2023") //
            );

            // delete all external events
            deleteAllExternalEvents();

            // run cob
            updateBusinessDateAndExecuteCOBJob("31 January 2023");

            // verify external events
            List<ExternalEventDTO> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            Assertions.assertEquals(0, allExternalEvents.size());
        });
    }

    @Test
    public void testNoSnapshotEventGenerationWhenCustomSnapshotEventIsDisabled() {
        runAt("31 January 2023", () -> {
            // disable custom snapshot event
            disableLoanAccountCustomSnapshotBusinessEvent();
            // Enable Business Step
            enableCOBBusinessStep("APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION", "CHECK_LOAN_REPAYMENT_DUE",
                    "CHECK_LOAN_REPAYMENT_OVERDUE", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES",
                    "EXTERNAL_ASSET_OWNER_TRANSFER", "CHECK_DUE_INSTALLMENTS");

            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            PostLoanProductsRequest loanProductsRequest = create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
                    InterestType.FLAT, AmortizationType.EQUAL_INSTALLMENTS);
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductResponse.getResourceId(), "01 January 2023", 1250.0, 4);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250), "01 January 2023");

            // Verify Repayment Schedule and Due Dates
            verifyRepaymentSchedule(loanId, //
                    installment(1250.0, null, "01 January 2023"), //
                    installment(312.0, false, "31 January 2023"), //
                    installment(312.0, false, "02 March 2023"), //
                    installment(312.0, false, "01 April 2023"), //
                    installment(314.0, false, "01 May 2023") //
            );

            // delete all external events
            deleteAllExternalEvents();

            // run cob
            updateBusinessDateAndExecuteCOBJob("01 February 2023");

            // verify external events
            List<ExternalEventDTO> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
            Assertions.assertEquals(0, allExternalEvents.size());
        });
    }

    private void deleteAllExternalEvents() {
        ExternalEventHelper.deleteAllExternalEvents(requestSpec, createResponseSpecification(Matchers.is(204)));
        List<ExternalEventDTO> allExternalEvents = ExternalEventHelper.getAllExternalEvents(requestSpec, responseSpec);
        Assertions.assertEquals(0, allExternalEvents.size());
    }

    private void enableCOBBusinessStep(String... steps) {
        new BusinessStepHelper().updateSteps("LOAN_CLOSE_OF_BUSINESS", steps);

    }

    public static String getExternalEventConfigurationsForUpdateJSON() {
        Map<String, Map<String, Boolean>> configurationsForUpdate = new HashMap<>();
        Map<String, Boolean> configurations = new HashMap<>();
        configurations.put("CentersCreateBusinessEvent", true);
        configurations.put("ClientActivateBusinessEvent", true);
        configurationsForUpdate.put("externalEventConfigurations", configurations);
        return new Gson().toJson(configurationsForUpdate);
    }

    private void enableLoanAccountCustomSnapshotBusinessEvent() {
        final Map<String, Boolean> updatedConfigurations = ExternalEventConfigurationHelper.updateExternalEventConfigurations(requestSpec,
                responseSpec, "{\"externalEventConfigurations\":{\"LoanAccountCustomSnapshotBusinessEvent\":true}}\n");
        Assertions.assertEquals(updatedConfigurations.size(), 1);
        Assertions.assertTrue(updatedConfigurations.containsKey("LoanAccountCustomSnapshotBusinessEvent"));
        Assertions.assertTrue(updatedConfigurations.get("LoanAccountCustomSnapshotBusinessEvent"));
    }

    private void disableLoanAccountCustomSnapshotBusinessEvent() {
        final Map<String, Boolean> updatedConfigurations = ExternalEventConfigurationHelper.updateExternalEventConfigurations(requestSpec,
                responseSpec, "{\"externalEventConfigurations\":{\"LoanAccountCustomSnapshotBusinessEvent\":false}}\n");
        Assertions.assertEquals(updatedConfigurations.size(), 1);
        Assertions.assertTrue(updatedConfigurations.containsKey("LoanAccountCustomSnapshotBusinessEvent"));
        Assertions.assertFalse(updatedConfigurations.get("LoanAccountCustomSnapshotBusinessEvent"));
    }

    private void updateBusinessDateAndExecuteCOBJob(String date) {
        businessDateHelper.updateBusinessDate(
                new BusinessDateRequest().type(BUSINESS_DATE.getName()).date(date).dateFormat(DATETIME_PATTERN).locale("en"));
        schedulerJobHelper.executeAndAwaitJob("Loan COB");
    }

}
