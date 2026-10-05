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
package org.apache.fineract.integrationtests.client.feign.tests;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessStepHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.InternalExternalEventsApi;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * When bulk COB events are enabled, one COB run over one loan must reach the consumer as exactly one
 * {@code BulkBusinessEvent}.
 */
@Slf4j
public class FeignCobBulkEventRecordingWindowTest extends FeignLoanTestBase {

    private static final String FEE_FREQUENCY_DAYS = "0";
    private static final String BULK_BUSINESS_EVENT = "BulkBusinessEvent";

    /**
     * External event types default to disabled, so the scenario has to switch on the ones it depends on rather than
     * inheriting whatever a previous test left behind. These are the events this COB chain raises: the replay event
     * comes from inside the inner recording window, the other two from business steps that run after it.
     */
    private static final String LOAN_ADJUST_TRANSACTION_EVENT = "LoanAdjustTransactionBusinessEvent";
    private static final String LOAN_REPAYMENT_DUE_EVENT = "LoanRepaymentDueBusinessEvent";
    private static final String LOAN_ACCRUAL_TRANSACTION_EVENT = "LoanAccrualTransactionCreatedBusinessEvent";
    private static final List<String> REQUIRED_EVENT_TYPES = List.of(LOAN_ADJUST_TRANSACTION_EVENT, LOAN_REPAYMENT_DUE_EVENT,
            LOAN_ACCRUAL_TRANSACTION_EVENT);

    @BeforeEach
    public void configureCob() {
        new FeignBusinessStepHelper(FineractFeignClientHelper.getFineractFeignClient()).updateSteps("LOAN_CLOSE_OF_BUSINESS",
                "APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION", "CHECK_LOAN_REPAYMENT_DUE",
                "CHECK_LOAN_REPAYMENT_OVERDUE", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES", "CHECK_DUE_INSTALLMENTS");
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.PENALTY_WAIT_PERIOD,
                new PutGlobalConfigurationsRequest().value(0L).enabled(true));
        REQUIRED_EVENT_TYPES.forEach(externalEventHelper::enableBusinessEvent);
    }

    @AfterEach
    public void restoreDefaults() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_COB_BULK_EVENT,
                new PutGlobalConfigurationsRequest().enabled(false));
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.PENALTY_WAIT_PERIOD,
                new PutGlobalConfigurationsRequest().value(2L).enabled(true));
        REQUIRED_EVENT_TYPES.forEach(externalEventHelper::disableBusinessEvent);
    }

    /**
     * Control: with bulk events off, the COB run is expected to raise its loan events individually. This pins down that
     * the scenario really does drive event-raising COB steps, so the assertion in the test below cannot pass simply
     * because nothing happened.
     */
    @Test
    public void withBulkEventsDisabledTheCobRunRaisesLoanEventsIndividually() {
        setBulkCobEvents(false);

        List<ExternalEventResponse> loanEvents = runCobAndFetchLoanCategoryEvents();

        assertThat(loanEvents).as("the COB run must raise loan events for this scenario to be meaningful").isNotEmpty();
        // Only the events raised outside any recording window are expected individually here. The replay events are
        // raised inside the reprocessing window, which posts them as its own bulk event even when COB's is off.
        Assertions.assertThat(loanEvents).extracting(ExternalEventResponse::getType)
                .as("the events that escape COB's window when the bug is present must actually be raised")
                .contains(LOAN_REPAYMENT_DUE_EVENT, LOAN_ACCRUAL_TRANSACTION_EVENT);
    }

    /**
     * With bulk events on, every one of those same events belongs inside the single bulk event. Any that show up
     * individually escaped a recording window that was closed early.
     */
    @Test
    public void withBulkEventsEnabledNoLoanEventEscapesTheBulkEvent() {
        setBulkCobEvents(true);

        List<ExternalEventResponse> escaped = runCobAndFetchLoanCategoryEvents();

        assertThat(escaped).as(
                "these events were raised during the COB run but posted individually instead of inside the "
                        + "bulk event, so COB's recording window was closed before the chain finished: %s",
                escaped.stream().map(ExternalEventResponse::getType).toList()).isEmpty();

        // Nothing escaping is only half the story: the events must actually be in the bulk event rather than lost.
        List<ExternalEventResponse> bulkEvents = externalEventHelper.getExternalEventsByType(BULK_BUSINESS_EVENT);
        assertThat(bulkEvents).as("the COB run must produce exactly one bulk event").hasSize(1);
        Assertions.assertThat(bulkEventItemTypes(bulkEvents.getFirst()))
                .as("the bulk event must carry both the events raised inside the inner recording window and the ones "
                        + "raised by the business steps that ran after it")
                .contains(LOAN_ADJUST_TRANSACTION_EVENT, LOAN_REPAYMENT_DUE_EVENT, LOAN_ACCRUAL_TRANSACTION_EVENT);
    }

    @SuppressWarnings("unchecked")
    private List<String> bulkEventItemTypes(ExternalEventResponse bulkEvent) {
        final Object items = bulkEvent.getPayLoad().get("datas");
        assertThat(items).as("the bulk event payload must list the events it carries").isInstanceOf(List.class);
        return ((List<Map<String, Object>>) items).stream().map(item -> String.valueOf(item.get("type"))).toList();
    }

    /**
     * Isolates what actually trips the bug. Same loan, same overdue penalty, same COB run - only the repayment is gone,
     * so the penalty has no earlier transaction to re-allocate, the reprocessing produces no transaction changes, and
     * the inner recording window is therefore never opened. Nothing escapes.
     */
    @Test
    public void withNoRepaymentToReplayNothingEscapesTheBulkEvent() {
        setBulkCobEvents(true);

        List<ExternalEventResponse> escaped = runCobAndFetchLoanCategoryEvents(false);

        assertThat(escaped).as(
                "with no transaction to replay the inner recording window never opens, so nothing should escape, " + "but these did: %s",
                escaped.stream().map(ExternalEventResponse::getType).toList()).isEmpty();
    }

    private List<ExternalEventResponse> runCobAndFetchLoanCategoryEvents() {
        return runCobAndFetchLoanCategoryEvents(true);
    }

    private List<ExternalEventResponse> runCobAndFetchLoanCategoryEvents(boolean withRepayment) {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        AtomicReference<List<ExternalEventResponse>> result = new AtomicReference<>();

        runAt("01 January 2023", () -> loanIdRef.set(createOverduePenaltyLoan()));

        // A repayment so that applying the overdue penalty has something to replay.
        if (withRepayment) {
            runAt("07 January 2023", () -> addRepaymentForLoan(loanIdRef.get(), 1000.0, "07 January 2023"));
        }

        runAt("09 January 2023", () -> {
            Long loanId = loanIdRef.get();
            externalEventHelper.deleteAllExternalEvents();

            executeInlineCOB(loanId);

            // Deliberately filtered to the Loan category so that the bulk event itself, which is category "Bulk",
            // is not counted as an escapee. Its contents are asserted separately via bulkEventItemTypes.
            List<ExternalEventResponse> loanEvents = FineractFeignClientHelper.getFineractFeignClient()
                    .create(InternalExternalEventsApi.class).getAllExternalEvents(Map.of("category", "Loan", "aggregateRootId", loanId));
            result.set(loanEvents);
        });
        return result.get();
    }

    private void setBulkCobEvents(boolean enabled) {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_COB_BULK_EVENT,
                new PutGlobalConfigurationsRequest().enabled(enabled));
    }

    private Long createOverduePenaltyLoan() {
        Long clientId = createClient();

        int numberOfRepayments = 3;
        int repaymentEvery = 4;
        Long chargeId = createOverduePenaltyPercentageCharge(1.0, FEE_FREQUENCY_DAYS, 1);

        PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct() //
                .graceOnArrearsAgeing(0).numberOfRepayments(numberOfRepayments) //
                .repaymentEvery(repaymentEvery) //
                .installmentAmountInMultiplesOf(null) //
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS_L) //
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE) //
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY) //
                .interestRecalculationCompoundingMethod(LoanTestData.InterestRecalculationCompoundingMethod.NONE) //
                .isInterestRecalculationEnabled(true) //
                .recalculationRestFrequencyInterval(1) //
                .recalculationRestFrequencyType(LoanTestData.RecalculationRestFrequencyType.DAILY) //
                .rescheduleStrategyMethod(LoanTestData.RescheduleStrategyMethod.REDUCE_EMI_AMOUNT) //
                .allowPartialPeriodInterestCalculation(false) //
                .disallowExpectedDisbursements(false) //
                .allowApprovedDisbursedAmountsOverApplied(false) //
                .overAppliedNumber(null) //
                .overAppliedCalculationType(null) //
                .multiDisburseLoan(null) //
                .charges(List.of(new LoanProductChargeData().id(chargeId)));

        Long loanProductId = createLoanProduct(product);

        double amount = 5000.0;
        PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", amount, numberOfRepayments) //
                .repaymentEvery(repaymentEvery) //
                .loanTermFrequency(numberOfRepayments * repaymentEvery) //
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS) //
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS) //
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE) //
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY);

        Long loanId = applyForLoan(applicationRequest);
        approveLoan(loanId, approveLoanRequest(amount, "01 January 2023"));
        disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("01 January 2023").dateFormat(DATETIME_PATTERN)
                .transactionAmount(java.math.BigDecimal.valueOf(amount)).locale("en"));
        return loanId;
    }

    private Long createOverduePenaltyPercentageCharge(double percentageAmount, String feeFrequency, int feeInterval) {
        ChargeRequest chargeRequest = ChargeRequestBuilders.loanOverdueFee(percentageAmount) //
                .chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST.getValue()) //
                .feeFrequency(feeFrequency) //
                .feeInterval(String.valueOf(feeInterval));
        return chargesHelper.createCharge(chargeRequest).getResourceId();
    }
}
