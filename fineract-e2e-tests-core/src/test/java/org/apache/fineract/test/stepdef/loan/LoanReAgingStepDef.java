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
package org.apache.fineract.test.stepdef.loan;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.loan.v1.LoanTransactionFlagsDataV1;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.LoanScheduleData;
import org.apache.fineract.client.models.LoanSchedulePeriodData;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.loan.LoanReAgeEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanReAgeTransactionEvent;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.junit.jupiter.api.Assertions;

@Slf4j
@RequiredArgsConstructor
public class LoanReAgingStepDef extends AbstractStepDef {

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    private final EventAssertion eventAssertion;
    private final FineractFeignClient fineractClient;

    @When("Admin creates a Loan re-aging transaction with the following data:")
    public void createReAgingTransaction(DataTable table) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansLoanIdTransactionsRequest reAgingRequest = setReAgeingRequestProperties(//
                LoanRequestFactory.defaultReAgingRequest(), //
                table.row(0), //
                table.row(1) //
        );

        PostLoansLoanIdTransactionsResponse response = ok(
                () -> fineractClient.loanTransactions().executeLoanTransaction(loanId, reAgingRequest, Map.of("command", "reAge")));
        testContext().set(TestContextKey.LOAN_REAGING_RESPONSE, response);
    }

    @When("Admin creates a Loan re-aging transaction by Loan external ID with the following data:")
    public void createReAgingTransactionByLoanExternalId(DataTable table) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanResponse.getResourceExternalId();

        PostLoansLoanIdTransactionsRequest reAgingRequest = setReAgeingRequestProperties(//
                LoanRequestFactory.defaultReAgingRequest(), //
                table.row(0), //
                table.row(1) //
        );

        PostLoansLoanIdTransactionsResponse response = ok(() -> fineractClient.loanTransactions().executeLoanTransaction1(loanExternalId,
                reAgingRequest, Map.of("command", "reAge")));
        testContext().set(TestContextKey.LOAN_REAGING_RESPONSE, response);
    }

    @When("Admin successfully undo Loan re-aging transaction")
    public void undoReAgingTransaction() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansLoanIdTransactionsResponse response = ok(() -> fineractClient.loanTransactions().executeLoanTransaction(loanId,
                new PostLoansLoanIdTransactionsRequest(), Map.of("command", "undoReAge")));
        testContext().set(TestContextKey.LOAN_REAGING_UNDO_RESPONSE, response);
    }

    @Then("LoanReAgeBusinessEvent is created")
    public void checkLoanReAmortizeBusinessEventCreated() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        eventAssertion.assertEventRaised(LoanReAgeEvent.class, loanId);
    }

    @When("Admin fails to create a Loan re-aging transaction with status code {int} error {string} and with the following data:")
    public void adminFailsToCreateReAgingTransactionWithError(final int statusCode, final String expectedError, final DataTable table) {
        CallFailedRuntimeException exception = getCreateLoanReAgeFailureResponse(table);
        assertThat(exception.getStatus()).isEqualTo(statusCode);

        String developerMessage = exception.getDeveloperMessage();
        if (developerMessage.contains(expectedError)) {
            assertThat(developerMessage).contains(expectedError);
        } else {
            assertThat(developerMessage).containsAnyOf("Loan cannot be re-aged as there are no outstanding balances to be re-aged",
                    "The parameter `startDate` must be greater than or equal to the provided date");
        }
    }

    @Then("Admin fails to create a Loan re-aging transaction with the following data because loan was charged-off:")
    public void reAgeChargedOffLoanFailure(final DataTable table) {
        checkCreateLoanReAgeFailure(table, ErrorMessageHelper.reAgeChargedOffLoanFailure());
    }

    @Then("Admin fails to create a Loan re-aging transaction with the following data because loan was closed:")
    public void reAgeClosedLoanFailure(final DataTable table) {
        checkCreateLoanReAgeFailure(table, ErrorMessageHelper.reAgeClosedLoanFailure());
    }

    @Then("Admin fails to create a Loan re-aging transaction with the following data because loan was contract terminated:")
    public void reAgeContractTerminatedLoanFailure(final DataTable table) {
        checkCreateLoanReAgeFailure(table, ErrorMessageHelper.reAgeContractTerminatedLoanFailure());
    }

    private Map<String, Object> resolveReAgingQueryParams(DataTable table) {
        List<String> header = table.asLists().get(0);
        List<String> data = table.asLists().get(1);
        Map<String, Object> queryParams = new HashMap<>(Map.of("dateFormat", DATE_FORMAT, "locale", "en"));
        for (int i = 0; i < header.size(); i++) {
            queryParams.put(header.get(i), data.get(i));
        }
        return queryParams;
    }

    @When("Admin creates a Loan re-aging preview with the following data:")
    public void createReAgingPreview(DataTable table) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        Map<String, Object> queryParams = resolveReAgingQueryParams(table);
        LoanScheduleData response = ok(() -> fineractClient.loanTransactions().previewReAgeSchedule(loanId, queryParams));
        testContext().set(TestContextKey.LOAN_REAGING_PREVIEW_RESPONSE, response);

        log.info("Re-aging preview created for loan ID: {} with parameters: {}", loanId, queryParams);
    }

    public LoanScheduleData reAgingPreviewByLoanExternalId(DataTable table) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanResponse.getResourceExternalId();

        Map<String, Object> queryParams = resolveReAgingQueryParams(table);
        LoanScheduleData result = ok(() -> fineractClient.loanTransactions().previewReAgeSchedule1(loanExternalId, queryParams));
        log.info("Re-aging preview is requested to be created with loan external ID: {} with parameters: {}", loanExternalId, queryParams);
        return result;
    }

    @When("Admin creates a Loan re-aging preview by Loan external ID with the following data:")
    public void createReAgingPreviewByLoanExternalId(DataTable table) {
        LoanScheduleData response = reAgingPreviewByLoanExternalId(table);
        testContext().set(TestContextKey.LOAN_REAGING_PREVIEW_RESPONSE, response);

        log.info("Re-aging preview is created with loan externalId.");
    }

    @Then("Admin fails to create a Loan re-aging preview with the following data because loan was charged-off:")
    public void reAgePreviewChargedOffLoanFailure(final DataTable table) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanResponse.getResourceExternalId();

        Map<String, Object> queryParams = resolveReAgingQueryParams(table);
        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loanTransactions().previewReAgeSchedule1(loanExternalId, queryParams));

        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.reAgeChargedOffLoanFailure());
    }

    @Then("Admin fails to create a Loan re-aging preview with the following data because loan was contract terminated:")
    public void reAgePreviewContractTerminatedLoanFailure(final DataTable table) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanResponse.getResourceExternalId();

        Map<String, Object> queryParams = resolveReAgingQueryParams(table);

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loanTransactions().previewReAgeSchedule1(loanExternalId, queryParams));

        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.reAgeContractTerminatedLoanFailure());
    }

    @Then("Admin fails to create a Loan re-aging preview with the following data because loan was closed:")
    public void reAgePreviewClosedLoanFailure(final DataTable table) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanResponse.getResourceExternalId();

        Map<String, Object> queryParams = resolveReAgingQueryParams(table);
        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loanTransactions().previewReAgeSchedule1(loanExternalId, queryParams));

        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.reAgeClosedLoanFailure());
    }

    @Then("Loan Re-Aged Repayment schedule preview has {int} periods, with the following data for periods:")
    public void loanRepaymentSchedulePreviewPeriodsCheck(int linesExpected, DataTable table) {
        LoanScheduleData scheduleResponse = testContext().get(TestContextKey.LOAN_REAGING_PREVIEW_RESPONSE);

        List<LoanSchedulePeriodData> repaymentPeriods = scheduleResponse.getPeriods();

        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String resourceId = String.valueOf(loanResponse.getLoanId());

        List<List<String>> data = table.asLists();
        int nrLines = data.size();
        int linesActual = (int) repaymentPeriods.stream().filter(r -> r.getPeriod() != null).count();
        for (int i = 1; i < nrLines; i++) {
            List<String> expectedValues = data.get(i);
            String dueDateExpected = expectedValues.get(2);

            List<List<String>> actualValuesList = repaymentPeriods.stream()
                    .filter(r -> dueDateExpected.equals(FORMATTER.format(r.getDueDate())))
                    .map(r -> fetchValuesOfRepaymentSchedule(data.get(0), r)).collect(Collectors.toList());

            boolean containsExpectedValues = actualValuesList.stream().anyMatch(actualValues -> actualValues.equals(expectedValues));
            assertThat(containsExpectedValues)
                    .as(ErrorMessageHelper.wrongValueInLineInRepaymentSchedule(resourceId, i, actualValuesList, expectedValues)).isTrue();

            assertThat(linesActual).as(ErrorMessageHelper.wrongNumberOfLinesInRepaymentSchedule(resourceId, linesActual, linesExpected))
                    .isEqualTo(linesExpected);
        }
    }

    @Then("Loan Re-Aged Repayment schedule preview has the following data in Total row:")
    public void loanRepaymentScheduleAmountCheck(DataTable table) {
        List<List<String>> data = table.asLists();
        List<String> header = data.get(0);
        List<String> expectedValues = data.get(1);
        LoanScheduleData scheduleResponse = testContext().get(TestContextKey.LOAN_REAGING_PREVIEW_RESPONSE);
        validateRepaymentScheduleTotal(header, scheduleResponse, expectedValues);
    }

    private List<String> fetchValuesOfRepaymentSchedule(List<String> header, LoanSchedulePeriodData repaymentPeriod) {
        List<String> actualValues = new ArrayList<>();
        for (String headerName : header) {
            switch (headerName) {
                case "Nr" -> actualValues.add(repaymentPeriod.getPeriod() == null ? null : String.valueOf(repaymentPeriod.getPeriod()));
                case "Days" ->
                    actualValues.add(repaymentPeriod.getDaysInPeriod() == null ? null : String.valueOf(repaymentPeriod.getDaysInPeriod()));
                case "Date" ->
                    actualValues.add(repaymentPeriod.getDueDate() == null ? null : FORMATTER.format(repaymentPeriod.getDueDate()));
                case "Paid date" -> actualValues.add(repaymentPeriod.getObligationsMetOnDate() == null ? null
                        : FORMATTER.format(repaymentPeriod.getObligationsMetOnDate()));
                case "Balance of loan" -> actualValues.add(repaymentPeriod.getPrincipalLoanBalanceOutstanding() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getPrincipalLoanBalanceOutstanding().doubleValue()).format());
                case "Principal due" -> actualValues.add(repaymentPeriod.getPrincipalDue() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getPrincipalDue().doubleValue()).format());
                case "Interest" -> actualValues.add(repaymentPeriod.getInterestDue() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getInterestDue().doubleValue()).format());
                case "Fees" -> actualValues.add(repaymentPeriod.getFeeChargesDue() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getFeeChargesDue().doubleValue()).format());
                case "Penalties" -> actualValues.add(repaymentPeriod.getPenaltyChargesDue() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getPenaltyChargesDue().doubleValue()).format());
                case "Due" -> actualValues.add(repaymentPeriod.getTotalDueForPeriod() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getTotalDueForPeriod().doubleValue()).format());
                case "Paid" -> actualValues.add(repaymentPeriod.getTotalPaidForPeriod() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getTotalPaidForPeriod().doubleValue()).format());
                case "In advance" -> actualValues.add(repaymentPeriod.getTotalPaidInAdvanceForPeriod() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getTotalPaidInAdvanceForPeriod().doubleValue()).format());
                case "Late" -> actualValues.add(repaymentPeriod.getTotalPaidLateForPeriod() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getTotalPaidLateForPeriod().doubleValue()).format());
                case "Waived" -> actualValues.add(repaymentPeriod.getTotalWaivedForPeriod() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getTotalWaivedForPeriod().doubleValue()).format());
                case "Outstanding" -> actualValues.add(repaymentPeriod.getTotalOutstandingForPeriod() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getTotalOutstandingForPeriod().doubleValue()).format());
                default -> throw new IllegalStateException(String.format("Header name %s cannot be found", headerName));
            }
        }
        return actualValues;
    }

    @SuppressFBWarnings("SF_SWITCH_NO_DEFAULT")
    private List<String> validateRepaymentScheduleTotal(List<String> header, LoanScheduleData repaymentSchedule,
            List<String> expectedAmounts) {
        List<String> actualValues = new ArrayList<>();
        Double paidActual = 0.0;
        List<LoanSchedulePeriodData> periods = repaymentSchedule.getPeriods();
        for (LoanSchedulePeriodData period : periods) {
            if (null != period.getTotalPaidForPeriod()) {
                paidActual += period.getTotalPaidForPeriod().doubleValue();
            }
        }
        BigDecimal paidActualBd = new BigDecimal(paidActual).setScale(2, RoundingMode.HALF_DOWN);

        for (int i = 0; i < header.size(); i++) {
            String headerName = header.get(i);
            String expectedValue = expectedAmounts.get(i);
            switch (headerName) {
                case "Principal due" -> assertThat(repaymentSchedule.getTotalPrincipalExpected())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentSchedulePrincipal(
                                repaymentSchedule.getTotalPrincipalExpected().doubleValue(), Double.valueOf(expectedValue)))//
                        .isEqualByComparingTo(new BigDecimal(expectedValue));//
                case "Interest" -> assertThat(repaymentSchedule.getTotalInterestCharged())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleInterest(
                                repaymentSchedule.getTotalInterestCharged().doubleValue(), Double.valueOf(expectedValue)))//
                        .isEqualByComparingTo(new BigDecimal(expectedValue));//
                case "Fees" -> assertThat(repaymentSchedule.getTotalFeeChargesCharged())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleFees(
                                repaymentSchedule.getTotalFeeChargesCharged().doubleValue(), Double.valueOf(expectedValue)))//
                        .isEqualByComparingTo(new BigDecimal(expectedValue));//
                case "Penalties" -> assertThat(repaymentSchedule.getTotalPenaltyChargesCharged())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentSchedulePenalties(
                                repaymentSchedule.getTotalPenaltyChargesCharged().doubleValue(), Double.valueOf(expectedValue)))//
                        .isEqualByComparingTo(new BigDecimal(expectedValue));//
                case "Due" -> assertThat(repaymentSchedule.getTotalRepaymentExpected())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleDue(
                                repaymentSchedule.getTotalRepaymentExpected().doubleValue(), Double.valueOf(expectedValue)))//
                        .isEqualByComparingTo(new BigDecimal(expectedValue));//
                case "Paid" -> assertThat(paidActualBd)//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentSchedulePaid(paidActualBd.doubleValue(),
                                Double.valueOf(expectedValue)))//
                        .isEqualByComparingTo(new BigDecimal(expectedValue));//
                case "In advance" -> assertThat(repaymentSchedule.getTotalPaidInAdvance())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleInAdvance(
                                repaymentSchedule.getTotalPaidInAdvance().doubleValue(), Double.valueOf(expectedValue)))//
                        .isEqualByComparingTo(new BigDecimal(expectedValue));//
                case "Late" -> assertThat(repaymentSchedule.getTotalPaidLate())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleLate(repaymentSchedule.getTotalPaidLate().doubleValue(),
                                Double.valueOf(expectedValue)))//
                        .isEqualByComparingTo(new BigDecimal(expectedValue));//
                case "Waived" -> assertThat(repaymentSchedule.getTotalWaived())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleWaived(repaymentSchedule.getTotalWaived().doubleValue(),
                                Double.valueOf(expectedValue)))//
                        .isEqualByComparingTo(new BigDecimal(expectedValue));//
                case "Outstanding" -> assertThat(repaymentSchedule.getTotalOutstanding())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleOutstanding(
                                repaymentSchedule.getTotalOutstanding().doubleValue(), Double.valueOf(expectedValue)))//
                        .isEqualByComparingTo(new BigDecimal(expectedValue));//
            }
        }
        return actualValues;
    }

    PostLoansLoanIdTransactionsRequest setReAgeingRequestProperties(PostLoansLoanIdTransactionsRequest request, List<String> headers,
            List<String> values) {
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i).toLowerCase(java.util.Locale.ROOT).trim().replaceAll(" ", "");
            switch (header) {
                case "frequencynumber" -> request.setFrequencyNumber(Integer.parseInt(values.get(i)));
                case "frequencytype" -> request.setFrequencyType(values.get(i));
                case "startdate" -> request.setStartDate(values.get(i));
                case "numberofinstallments" -> request.setNumberOfInstallments(Integer.parseInt(values.get(i)));
                case "reageinteresthandling" -> request.setReAgeInterestHandling(values.get(i));
                default -> throw new IllegalStateException("Unknown header: " + header);
            }
        }
        return request;
    }

    @When("Admin creates a Loan re-aging transaction by Loan external ID with the following data, but fails with {int} error code:")
    public void adminCreatesALoanReAgingTransactionByLoanExternalIDWithTheFollowingDataButFailsWithErrorCode(int errorCode,
            DataTable table) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanResponse.getResourceExternalId();

        PostLoansLoanIdTransactionsRequest reAgingRequest = setReAgeingRequestProperties(//
                LoanRequestFactory.defaultReAgingRequest(), //
                table.row(0), //
                table.row(1) //
        );

        CallFailedRuntimeException response = fail(() -> fineractClient.loanTransactions().executeLoanTransaction1(loanExternalId,
                reAgingRequest, Map.of("command", "reAge")));
        assertThat(response.getStatus()).isEqualTo(errorCode);
    }

    public CallFailedRuntimeException getCreateLoanReAgeFailureResponse(DataTable table) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Assertions.assertNotNull(loanResponse);
        final long loanId = loanResponse.getLoanId();

        PostLoansLoanIdTransactionsRequest reAgingRequest = setReAgeingRequestProperties(//
                LoanRequestFactory.defaultReAgingRequest(), //
                table.row(0), //
                table.row(1) //
        );

        return fail(() -> fineractClient.loanTransactions().executeLoanTransaction(loanId, reAgingRequest, Map.of("command", "reAge")));
    }

    public void checkCreateLoanReAgeFailure(DataTable table, String errorMessage) {
        CallFailedRuntimeException exception = getCreateLoanReAgeFailureResponse(table);
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    @Then("LoanReAgeTransactionBusinessEvent has changedTerms {string}")
    public void checkReAgeTransactionEventChangedTerms(final String expectedChangedTerms) {
        final PostLoansLoanIdTransactionsResponse reAgingResponse = testContext().get(TestContextKey.LOAN_REAGING_RESPONSE);
        Assertions.assertNotNull(reAgingResponse);
        final Long transactionId = reAgingResponse.getResourceId();
        Assertions.assertNotNull(transactionId);

        final Boolean expectedValue = "null".equalsIgnoreCase(expectedChangedTerms) ? null : Boolean.valueOf(expectedChangedTerms);

        eventAssertion.assertEvent(LoanReAgeTransactionEvent.class, transactionId).extractingData(loanTransactionDataV1 -> {
            final LoanTransactionFlagsDataV1 flags = loanTransactionDataV1.getFlags();
            final Boolean actualChangedTerms = flags == null ? null : flags.getChangedTerms();
            assertThat(actualChangedTerms).as("changedTerms in LoanReAgeTransactionBusinessEvent").isEqualTo(expectedValue);
            return null;
        });
    }
}
