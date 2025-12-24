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

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.loan.v1.LoanAccountDelinquencyRangeDataV1;
import org.apache.fineract.avro.loan.v1.LoanInstallmentDelinquencyBucketDataV1;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.DelinquencyRangeData;
import org.apache.fineract.client.models.GetDelinquencyActionsResponse;
import org.apache.fineract.client.models.GetDelinquencyTagHistoryResponse;
import org.apache.fineract.client.models.GetLoansLoanIdDelinquencyPausePeriod;
import org.apache.fineract.client.models.GetLoansLoanIdDelinquencySummary;
import org.apache.fineract.client.models.GetLoansLoanIdLoanInstallmentLevelDelinquency;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansDelinquencyActionRequest;
import org.apache.fineract.client.models.PostLoansDelinquencyActionResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.test.api.ApiProperties;
import org.apache.fineract.test.data.DelinquencyRange;
import org.apache.fineract.test.data.LoanStatus;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.messaging.event.loan.delinquency.LoanDelinquencyRangeChangeEvent;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class LoanDelinquencyStepDef extends AbstractStepDef {

    public static final String DATE_FORMAT = "dd MMMM yyyy";
    public static final String DEFAULT_LOCALE = "en";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final String PWD_USER_WITH_ROLE = "1234567890Aa!";

    @Autowired
    private FineractFeignClient fineractClient;

    @Autowired
    private ApiProperties apiProperties;

    @Autowired
    private EventAssertion eventAssertion;

    @Autowired
    private EventCheckHelper eventCheckHelper;

    private FineractFeignClient createClientForUser(String username, String password) {
        String baseUrl = apiProperties.getBaseUrl();
        String tenantId = apiProperties.getTenantId();
        long readTimeout = apiProperties.getReadTimeout();
        String apiBaseUrl = baseUrl + "/fineract-provider/api/";

        return FineractFeignClient.builder().baseUrl(apiBaseUrl).credentials(username, password).tenantId(tenantId)
                .disableSslVerification(true).connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout((int) readTimeout, java.util.concurrent.TimeUnit.SECONDS).build();
    }

    @Then("Admin checks that delinquency range is: {string} and has delinquentDate {string}")
    public void checkDelinquencyRange(String range, String delinquentDateExpected) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveLoan(loanId, Map.of("associations", "collection")));
        Integer loanStatus = loanDetails.getStatus().getId();

        if (!LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.value.equals(loanStatus) && !LoanStatus.APPROVED.value.equals(loanStatus)) {
            String delinquentDateExpectedValue = "".equals(delinquentDateExpected) ? null : delinquentDateExpected;
            eventAssertion.assertEvent(LoanDelinquencyRangeChangeEvent.class, loanId)//
                    .extractingData(LoanAccountDelinquencyRangeDataV1::getDelinquentDate)//
                    .isEqualTo(delinquentDateExpectedValue);//
        }

        DelinquencyRange expectedDelinquencyRange = DelinquencyRange.valueOf(range);
        String expectedDelinquencyRangeValue = expectedDelinquencyRange.getValue();

        String actualDelinquencyRangeValue = DelinquencyRange.NO_DELINQUENCY.value;
        DelinquencyRangeData actualDelinquencyRange = loanDetails.getDelinquencyRange();
        if (actualDelinquencyRange != null) {
            actualDelinquencyRangeValue = actualDelinquencyRange.getClassification();
        }

        assertThat(actualDelinquencyRangeValue)
                .as(ErrorMessageHelper.delinquencyRangeError(actualDelinquencyRangeValue, expectedDelinquencyRangeValue))
                .isEqualTo(expectedDelinquencyRangeValue);
    }

    @Then("Admin checks that {string}th delinquency range is: {string} and added on: {string} and has delinquentDate {string}")
    public void checkDelinquencyRange(String nthInList, String range, String addedOnDate, String delinquentDateExpected)
            throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        String delinquentDateExpectedValue = "".equals(delinquentDateExpected) ? null : delinquentDateExpected;
        eventAssertion.assertEvent(LoanDelinquencyRangeChangeEvent.class, loanId)
                .extractingData(LoanAccountDelinquencyRangeDataV1::getDelinquentDate).isEqualTo(delinquentDateExpectedValue);

        DelinquencyRange expectedDelinquencyRange = DelinquencyRange.valueOf(range);
        String expectedDelinquencyRangeValue = expectedDelinquencyRange.getValue();

        List<GetDelinquencyTagHistoryResponse> delinquencyHistoryDetails = ok(
                () -> fineractClient.loans().getDelinquencyTagHistory(loanId));

        String actualDelinquencyRangeValue = DelinquencyRange.NO_DELINQUENCY.value;
        String actualDelinquencyAddedOnDate = "";
        int i = Integer.parseInt(nthInList) - 1;
        GetDelinquencyTagHistoryResponse delinquencyTag = delinquencyHistoryDetails.get(i);
        if (delinquencyTag != null) {
            actualDelinquencyRangeValue = delinquencyTag.getDelinquencyRange().getClassification();
            actualDelinquencyAddedOnDate = formatter.format(delinquencyTag.getAddedOnDate());
        }

        assertThat(actualDelinquencyRangeValue)
                .as(ErrorMessageHelper.delinquencyRangeError(actualDelinquencyRangeValue, expectedDelinquencyRangeValue))
                .isEqualTo(expectedDelinquencyRangeValue);
        assertThat(actualDelinquencyAddedOnDate).as(ErrorMessageHelper.delinquencyRangeError(actualDelinquencyAddedOnDate, addedOnDate))
                .isEqualTo(addedOnDate);
    }

    @Then("Loan delinquency history has the following details:")
    public void delinquencyHistoryCheck(DataTable table) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        List<List<String>> dataExpected = table.asLists();

        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        List<GetDelinquencyTagHistoryResponse> body = ok(() -> fineractClient.loans().getDelinquencyTagHistory(loanId));

        for (int i = 0; i < body.size(); i++) {
            List<String> line = dataExpected.get(i + 1);
            DelinquencyRange expectedDelinquencyRange = DelinquencyRange.valueOf(line.get(0));
            String classificationExpected = expectedDelinquencyRange.getValue();
            String addedOnDateExpected = line.get(1);
            String liftedOnDateExpected = line.get(2);

            String classificationActual = body.get(i).getDelinquencyRange().getClassification();
            String addedOnDateActual = body.get(i).getAddedOnDate() == null ? null : formatter.format(body.get(i).getAddedOnDate());
            String liftedOnDateActual = body.get(i).getLiftedOnDate() == null ? null : formatter.format(body.get(i).getLiftedOnDate());

            assertThat(classificationActual)
                    .as(ErrorMessageHelper.wrongDataInDelinquencyHistoryClassification(classificationActual, classificationExpected))
                    .isEqualTo(classificationExpected);
            assertThat(addedOnDateActual)
                    .as(ErrorMessageHelper.wrongDataInDelinquencyHistoryAddedOnDate(addedOnDateActual, addedOnDateExpected))
                    .isEqualTo(addedOnDateExpected);
            assertThat(liftedOnDateActual)
                    .as(ErrorMessageHelper.wrongDataInDelinquencyHistoryLiftedOnDate(liftedOnDateActual, liftedOnDateExpected))
                    .isEqualTo(liftedOnDateExpected);
        }
    }

    @When("Admin initiate a DELINQUENCY PAUSE with startDate: {string} and endDate: {string}")
    public void delinquencyPause(String startDate, String endDate) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("pause")//
                .startDate(startDate)//
                .endDate(endDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        PostLoansDelinquencyActionResponse response = ok(() -> fineractClient.loans().createLoanDelinquencyAction(loanId, request));
        testContext().set(TestContextKey.LOAN_DELINQUENCY_ACTION_RESPONSE, response);
        eventCheckHelper.loanAccountDelinquencyPauseChangedBusinessEventCheck(loanId);
    }

    @When("Created user with CREATE_DELINQUENCY_ACTION permission initiate a DELINQUENCY PAUSE with startDate: {string} and endDate: {string}")
    public void delinquencyPauseWithCreatedUser(String startDate, String endDate) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("pause")//
                .startDate(startDate)//
                .endDate(endDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        String username = testContext().get(TestContextKey.CREATED_SIMPLE_USER_USERNAME);
        String password = testContext().get(TestContextKey.CREATED_SIMPLE_USER_PASSWORD);
        FineractFeignClient userClient = createClientForUser(username, password);

        PostLoansDelinquencyActionResponse response = ok(() -> userClient.loans().createLoanDelinquencyAction(loanId, request));
        testContext().set(TestContextKey.LOAN_DELINQUENCY_ACTION_RESPONSE, response);
        eventCheckHelper.loanAccountDelinquencyPauseChangedBusinessEventCheck(loanId);
    }

    @Then("Created user with no CREATE_DELINQUENCY_ACTION permission gets an error when initiate a DELINQUENCY PAUSE with startDate: {string} and endDate: {string}")
    public void delinquencyPauseWithCreatedUserNOPermissionError(String startDate, String endDate) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        int errorCodeExpected = 403;
        String errorMessageExpected = "User has no authority to CREATE delinquency_actions";

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("pause")//
                .startDate(startDate)//
                .endDate(endDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        String username = testContext().get(TestContextKey.CREATED_SIMPLE_USER_USERNAME);
        String password = testContext().get(TestContextKey.CREATED_SIMPLE_USER_PASSWORD);
        FineractFeignClient userClient = createClientForUser(username, password);

        CallFailedRuntimeException exception = fail(() -> userClient.loans().createLoanDelinquencyAction(loanId, request));

        assertThat(exception.getStatus()).as(ErrorMessageHelper.wrongErrorCode(exception.getStatus(), errorCodeExpected))
                .isEqualTo(errorCodeExpected);
        assertThat(exception.getDeveloperMessage())
                .as(ErrorMessageHelper.wrongErrorMessage(exception.getDeveloperMessage(), errorMessageExpected))
                .contains(errorMessageExpected);

        log.debug("ERROR CODE: {}", exception.getStatus());
        log.debug("ERROR MESSAGE: {}", exception.getDeveloperMessage());
    }

    @When("Admin initiate a DELINQUENCY RESUME with startDate: {string}")
    public void delinquencyResume(String startDate) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("resume")//
                .startDate(startDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        PostLoansDelinquencyActionResponse response = ok(() -> fineractClient.loans().createLoanDelinquencyAction(loanId, request));
        testContext().set(TestContextKey.LOAN_DELINQUENCY_ACTION_RESPONSE, response);
        eventCheckHelper.loanAccountDelinquencyPauseChangedBusinessEventCheck(loanId);
    }

    @When("Admin initiate a DELINQUENCY PAUSE by loanExternalId with startDate: {string} and endDate: {string}")
    public void delinquencyPauseByLoanExternalId(String startDate, String endDate) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanResponse.getResourceExternalId();
        long loanId = loanResponse.getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("pause")//
                .startDate(startDate)//
                .endDate(endDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        PostLoansDelinquencyActionResponse response = ok(
                () -> fineractClient.loans().createLoanDelinquencyAction1(loanExternalId, request));
        testContext().set(TestContextKey.LOAN_DELINQUENCY_ACTION_RESPONSE, response);
        eventCheckHelper.loanAccountDelinquencyPauseChangedBusinessEventCheck(loanId);
    }

    @When("Admin initiate a DELINQUENCY RESUME by loanExternalId with startDate: {string}")
    public void delinquencyResumeByLoanExternalId(String startDate) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanResponse.getResourceExternalId();
        long loanId = loanResponse.getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("resume")//
                .startDate(startDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        PostLoansDelinquencyActionResponse response = ok(
                () -> fineractClient.loans().createLoanDelinquencyAction1(loanExternalId, request));
        testContext().set(TestContextKey.LOAN_DELINQUENCY_ACTION_RESPONSE, response);
        eventCheckHelper.loanAccountDelinquencyPauseChangedBusinessEventCheck(loanId);
    }

    @Then("Delinquency-actions have the following data:")
    public void getDelinquencyActionData(DataTable table) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        List<List<String>> data = table.asLists();
        int nrOfLinesExpected = data.size() - 1;

        List<GetDelinquencyActionsResponse> response = ok(() -> fineractClient.loans().getLoanDelinquencyActions(loanId));
        int nrOfLinesActual = response.size();

        assertThat(nrOfLinesActual)//
                .as(ErrorMessageHelper.wrongNumberOfLinesInDelinquencyActions(nrOfLinesActual, nrOfLinesExpected))//
                .isEqualTo(nrOfLinesExpected);//

        for (int i = 1; i < data.size(); i++) {
            List<String> expectedValues = data.get(i);

            GetDelinquencyActionsResponse lineActual = response.get(i - 1);

            List<String> actualValues = new ArrayList<>();
            actualValues.add(Objects.requireNonNull(lineActual.getAction()));
            actualValues.add(FORMATTER.format(Objects.requireNonNull(lineActual.getStartDate())));
            actualValues.add(lineActual.getEndDate() == null ? null : FORMATTER.format(lineActual.getEndDate()));

            assertThat(actualValues)//
                    .as(ErrorMessageHelper.wrongValueInLineDelinquencyActions(i, actualValues, expectedValues))//
                    .isEqualTo(expectedValues);//
        }
    }

    @Then("Initiating a delinquency-action other than PAUSE or RESUME in action field results an error - startDate: {string}, endDate: {string}")
    public void actionFieldError(String startDate, String endDate) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("TEST")//
                .startDate(startDate)//
                .endDate(endDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        int errorCodeExpected = 400;
        String errorMessageExpected = "Invalid Delinquency Action: TEST";
        errorMessageAssertationFeign(loanId, request, errorCodeExpected, errorMessageExpected);
    }

    @Then("Initiating a DELINQUENCY PAUSE with startDate before the actual business date results an error - startDate: {string}, endDate: {string}")
    public void delinquencyPauseStartDateError(String startDate, String endDate) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("pause")//
                .startDate(startDate)//
                .endDate(endDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        int errorCodeExpected = 400;
        String errorMessageExpected = "Start date of pause period must be in the future";
        errorMessageAssertationFeign(loanId, request, errorCodeExpected, errorMessageExpected);
    }

    @Then("Initiating a DELINQUENCY PAUSE on a non-active loan results an error - startDate: {string}, endDate: {string}")
    public void delinquencyPauseNonActiveLoanError(String startDate, String endDate) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("pause")//
                .startDate(startDate)//
                .endDate(endDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        int errorCodeExpected = 400;
        String errorMessageExpected = "Delinquency actions can be created only for active loans.";
        errorMessageAssertationFeign(loanId, request, errorCodeExpected, errorMessageExpected);
    }

    @Then("Initiating a DELINQUENCY RESUME on a non-active loan results an error - startDate: {string}")
    public void delinquencyResumeNonActiveLoanError(String startDate) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("resume")//
                .startDate(startDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        int errorCodeExpected = 400;
        String errorMessageExpected = "Delinquency actions can be created only for active loans.";
        errorMessageAssertationFeign(loanId, request, errorCodeExpected, errorMessageExpected);
    }

    @Then("Overlapping PAUSE periods result an error - startDate: {string}, endDate: {string}")
    public void delinquencyPauseOverlappingError(String startDate, String endDate) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("pause")//
                .startDate(startDate)//
                .endDate(endDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        int errorCodeExpected = 400;
        String errorMessageExpected = "Delinquency pause period cannot overlap with another pause period";
        errorMessageAssertationFeign(loanId, request, errorCodeExpected, errorMessageExpected);
    }

    @Then("Initiating a DELINQUENCY RESUME without an active PAUSE period results an error - startDate: {string}")
    public void delinquencyResumeWithoutPauseError(String startDate) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("resume")//
                .startDate(startDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        int errorCodeExpected = 400;
        String errorMessageExpected = "Resume Delinquency Action can only be created during an active pause";
        errorMessageAssertationFeign(loanId, request, errorCodeExpected, errorMessageExpected);
    }

    @Then("Initiating a DELINQUENCY RESUME with start date other than actual business date results an error - startDate: {string}")
    public void delinquencyResumeStartDateError(String startDate) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("resume")//
                .startDate(startDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        int errorCodeExpected = 400;
        String errorMessageExpected = "Start date of the Resume Delinquency action must be the current business date";
        errorMessageAssertationFeign(loanId, request, errorCodeExpected, errorMessageExpected);
    }

    @Then("Initiating a DELINQUENCY RESUME with an endDate results an error - startDate: {string}, endDate: {string}")
    public void delinquencyResumeWithEndDateError(String startDate, String endDate) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("resume")//
                .startDate(startDate)//
                .endDate(endDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        int errorCodeExpected = 400;
        String errorMessageExpected = "Resume Delinquency action can not have end date";
        errorMessageAssertationFeign(loanId, request, errorCodeExpected, errorMessageExpected);
    }

    @Then("Installment level delinquency event has correct data")
    public void installmentLevelDelinquencyEventCheck() throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        eventCheckHelper.installmentLevelDelinquencyRangeChangeEventCheck(loanId);
    }

    @Then("INSTALLMENT level delinquency is null")
    public void installmentLevelDelinquencyNull() throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveLoan(loanId, Map.of("associations", "collection")));
        List<GetLoansLoanIdLoanInstallmentLevelDelinquency> installmentLevelDelinquency = loanDetails.getDelinquent()
                .getInstallmentLevelDelinquency() == null ? null : loanDetails.getDelinquent().getInstallmentLevelDelinquency();
        assertThat(installmentLevelDelinquency).isNull();
    }

    @Then("Loan has the following LOAN level delinquency data:")
    public void loanDelinquencyDataCheck(DataTable table) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        List<String> expectedValuesList = table.asLists().get(1);
        DelinquencyRange expectedDelinquencyRange = DelinquencyRange.valueOf(expectedValuesList.get(0));
        String expectedDelinquencyRangeValue = expectedDelinquencyRange.getValue();
        expectedValuesList.set(0, expectedDelinquencyRangeValue);

        GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveLoan(loanId, Map.of("associations", "collection")));
        String actualDelinquencyRangeValue = loanDetails.getDelinquencyRange() == null ? "NO_DELINQUENCY"
                : loanDetails.getDelinquencyRange().getClassification();
        GetLoansLoanIdDelinquencySummary delinquent = loanDetails.getDelinquent();
        String delinquentAmount = delinquent.getDelinquentAmount() == null ? null
                : new Utils.DoubleFormatter(delinquent.getDelinquentAmount().doubleValue()).format();
        List<String> actualValuesList = List.of(actualDelinquencyRangeValue, delinquentAmount,
                delinquent.getDelinquentDate() == null ? "null" : FORMATTER.format(delinquent.getDelinquentDate()),
                delinquent.getDelinquentDays().toString(), delinquent.getPastDueDays().toString());

        assertThat(actualValuesList).as(ErrorMessageHelper.wrongValueInLoanLevelDelinquencyData(actualValuesList, expectedValuesList))
                .isEqualTo(expectedValuesList);
    }

    @Then("Loan has the following LOAN level next payment due data:")
    public void loanNextPaymentDataCheck(DataTable table) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        List<String> expectedValuesList = table.asLists().get(1);
        DelinquencyRange expectedDelinquencyRange = DelinquencyRange.valueOf(expectedValuesList.get(0));
        String expectedDelinquencyRangeValue = expectedDelinquencyRange.getValue();
        expectedValuesList.set(0, expectedDelinquencyRangeValue);

        GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveLoan(loanId, Map.of("associations", "collection")));

        String actualDelinquencyRangeValue = loanDetails.getDelinquencyRange() == null ? "NO_DELINQUENCY"
                : loanDetails.getDelinquencyRange().getClassification();
        GetLoansLoanIdDelinquencySummary delinquent = loanDetails.getDelinquent();

        String delinquentAmount = delinquent.getNextPaymentAmount() == null ? null
                : new Utils.DoubleFormatter(delinquent.getNextPaymentAmount().doubleValue()).format();
        List<String> actualValuesList = List.of(actualDelinquencyRangeValue,
                delinquent.getNextPaymentDueDate() == null ? "null" : FORMATTER.format(delinquent.getNextPaymentDueDate()),
                delinquentAmount);

        assertThat(actualValuesList).as(ErrorMessageHelper.wrongValueInLoanLevelDelinquencyData(actualValuesList, expectedValuesList))
                .isEqualTo(expectedValuesList);
    }

    @Then("Loan has the following INSTALLMENT level delinquency data:")
    public void loanDelinquencyInstallmentLevelDataCheck(DataTable table) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveLoan(loanId, Map.of("associations", "collection")));
        List<GetLoansLoanIdLoanInstallmentLevelDelinquency> installmentLevelDelinquency = loanDetails.getDelinquent()
                .getInstallmentLevelDelinquency();

        List<List<String>> data = table.asLists();
        assertThat(installmentLevelDelinquency.size())
                .as(ErrorMessageHelper.nrOfLinesWrongInInstallmentLevelDelinquencyData(installmentLevelDelinquency.size(), data.size() - 1))
                .isEqualTo(data.size() - 1);
        for (int i = 1; i < data.size(); i++) {
            DelinquencyRange expectedDelinquencyRange = DelinquencyRange.valueOf(data.get(i).get(1));
            String expectedDelinquencyRangeValue = expectedDelinquencyRange.getValue();

            List<String> expectedValuesList = data.get(i);
            expectedValuesList.set(1, expectedDelinquencyRangeValue);

            List<String> actualValuesList = List.of(String.valueOf(installmentLevelDelinquency.get(i - 1).getRangeId()),
                    installmentLevelDelinquency.get(i - 1).getClassification(),
                    installmentLevelDelinquency.get(i - 1).getDelinquentAmount().setScale(2, RoundingMode.HALF_DOWN).toString());
            assertThat(actualValuesList)
                    .as(ErrorMessageHelper.wrongValueInLineInInstallmentLevelDelinquencyData(i, actualValuesList, expectedValuesList))
                    .isEqualTo(expectedValuesList);
        }
    }

    @Then("Loan Delinquency pause periods has the following data:")
    public void loanDelinquencyPauseDataCheck(DataTable table) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        List<List<String>> expectedData = table.asLists();
        GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveLoan(loanId, Map.of("associations", "collection")));

        List<GetLoansLoanIdDelinquencyPausePeriod> delinquencyPausePeriods = loanDetails.getDelinquent().getDelinquencyPausePeriods();

        assertThat(delinquencyPausePeriods.size())
                .as(ErrorMessageHelper.nrOfLinesWrongInLoanDelinquencyPauseData(delinquencyPausePeriods.size(), expectedData.size() - 1))
                .isEqualTo(expectedData.size() - 1);

        for (int i = 1; i < expectedData.size(); i++) {
            List<String> expectedValuesList = expectedData.get(i);

            List<List<String>> actualValuesList = delinquencyPausePeriods.stream()
                    .map(t -> fetchValuesOfDelinquencyPausePeriods(table.row(0), t)).collect(Collectors.toList());

            boolean containsExpectedValues = actualValuesList.stream().anyMatch(actualValues -> actualValues.equals(expectedValuesList));
            assertThat(containsExpectedValues)
                    .as(ErrorMessageHelper.wrongValueInLineInDelinquencyPausePeriodData(i, actualValuesList, expectedValuesList)).isTrue();
        }
    }

    @Then("Loan details delinquent.nextPaymentDueDate will be {string}")
    public void nextPaymentDueDateCheck(String expectedDate) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveLoan(loanId, Map.of("associations", "collection")));
        String actualDate = FORMATTER.format(loanDetails.getDelinquent().getNextPaymentDueDate());

        assertThat(actualDate).as(ErrorMessageHelper.wrongDataInNextPaymentDueDate(actualDate, expectedDate)).isEqualTo(expectedDate);
    }

    @Then("LoanAccountDelinquencyRangeDataV1 has delinquencyRange field with value {string}")
    public void checkDelinquencyRangeInEvent(String expectedRange) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        DelinquencyRange expectedDelinquencyRange = DelinquencyRange.valueOf(expectedRange);
        String expectedDelinquencyRangeValue = expectedDelinquencyRange.getValue();

        eventAssertion.assertEvent(LoanDelinquencyRangeChangeEvent.class, loanId)//
                .extractingData(loanAccountDelinquencyRangeDataV1 -> {
                    String actualDelinquencyRangeValue = loanAccountDelinquencyRangeDataV1.getDelinquencyRange().getClassification();//
                    assertThat(actualDelinquencyRangeValue)//
                            .as(ErrorMessageHelper.delinquencyRangeError(actualDelinquencyRangeValue, expectedDelinquencyRangeValue))//
                            .isEqualTo(expectedDelinquencyRangeValue);//
                    return null;
                });
    }

    @Then("LoanDelinquencyRangeChangeBusinessEvent has the same Delinquency range, date and amount as in LoanDetails on both loan- and installment-level")
    public void checkDelinquencyRangeInEvent() throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveLoan(loanId, Map.of("associations", "collection")));
        DelinquencyRangeData delinquencyRange = loanDetails.getDelinquencyRange();
        GetLoansLoanIdDelinquencySummary delinquent = loanDetails.getDelinquent();

        eventAssertion.assertEvent(LoanDelinquencyRangeChangeEvent.class, loanId)//
                .extractingData(loanAccountDelinquencyRangeDataV1 -> {

                    Long loanLevelDelinquencyRangeId = loanAccountDelinquencyRangeDataV1.getDelinquencyRange().getId();
                    String loanLevelDelinquencyRange = loanAccountDelinquencyRangeDataV1.getDelinquencyRange().getClassification();
                    String loanLevelDelinquentDate = loanAccountDelinquencyRangeDataV1.getDelinquentDate();
                    BigDecimal loanLevelTotalAmount = loanAccountDelinquencyRangeDataV1.getAmount().getTotalAmount();

                    Long loanLevelDelinquencyRangeIdExpected = delinquencyRange.getId();
                    String loanLevelDelinquencyRangeExpected = delinquencyRange.getClassification();
                    String loanLevelDelinquentDateExpected = FORMATTER.format(delinquent.getDelinquentDate());
                    BigDecimal loanLevelTotalAmountExpected = delinquent.getDelinquentAmount();

                    assertThat(loanLevelDelinquencyRangeId)//
                            .as(ErrorMessageHelper.wrongValueInLoanDelinquencyRangeChangeBusinessEvent4(loanLevelDelinquencyRangeId,
                                    loanLevelDelinquencyRangeIdExpected))//
                            .isEqualTo(loanLevelDelinquencyRangeIdExpected);//
                    assertThat(loanLevelDelinquencyRange)//
                            .as(ErrorMessageHelper.wrongValueInLoanDelinquencyRangeChangeBusinessEvent5(loanLevelDelinquencyRange,
                                    loanLevelDelinquencyRangeExpected))//
                            .isEqualTo(loanLevelDelinquencyRangeExpected);//
                    assertThat(loanLevelDelinquentDate)//
                            .as(ErrorMessageHelper.wrongValueInLoanDelinquencyRangeChangeBusinessEvent7(loanLevelDelinquentDate,
                                    loanLevelDelinquentDateExpected))//
                            .isEqualTo(loanLevelDelinquentDateExpected);//
                    assertThat(loanLevelTotalAmount)//
                            .as(ErrorMessageHelper.wrongValueInLoanDelinquencyRangeChangeBusinessEvent6(loanLevelTotalAmount,
                                    loanLevelTotalAmountExpected))//
                            .isEqualByComparingTo(loanLevelTotalAmountExpected);//

                    List<GetLoansLoanIdLoanInstallmentLevelDelinquency> installmentLevelDelinquencyBucketsExpected = delinquent
                            .getInstallmentLevelDelinquency();
                    List<LoanInstallmentDelinquencyBucketDataV1> installmentDelinquencyBuckets = loanAccountDelinquencyRangeDataV1
                            .getInstallmentDelinquencyBuckets();
                    for (int i = 0; i < installmentDelinquencyBuckets.size(); i++) {
                        Long installmentLevelDelinquencyRangeId = installmentDelinquencyBuckets.get(i).getDelinquencyRange().getId();
                        String installmentLevelDelinquencyRange = installmentDelinquencyBuckets.get(i).getDelinquencyRange()
                                .getClassification();
                        BigDecimal installmentLevelTotalAmount = installmentDelinquencyBuckets.get(i).getAmount().getTotalAmount();

                        Long installmentLevelDelinquencyRangeIdExpected = installmentLevelDelinquencyBucketsExpected.get(i).getRangeId();
                        String installmentLevelDelinquencyRangeExpected = installmentLevelDelinquencyBucketsExpected.get(i)
                                .getClassification();
                        BigDecimal installmentLevelTotalAmountExpected = installmentLevelDelinquencyBucketsExpected.get(i)
                                .getDelinquentAmount();

                        assertThat(installmentLevelDelinquencyRangeId)//
                                .as(ErrorMessageHelper.wrongValueInLoanDelinquencyRangeChangeBusinessEvent1(
                                        installmentLevelDelinquencyRangeId, installmentLevelDelinquencyRangeIdExpected))//
                                .isEqualTo(installmentLevelDelinquencyRangeIdExpected);//
                        assertThat(installmentLevelDelinquencyRange)//
                                .as(ErrorMessageHelper.wrongValueInLoanDelinquencyRangeChangeBusinessEvent2(
                                        installmentLevelDelinquencyRange, installmentLevelDelinquencyRangeExpected))//
                                .isEqualTo(installmentLevelDelinquencyRangeExpected);//
                        assertThat(installmentLevelTotalAmount)//
                                .as(ErrorMessageHelper.wrongValueInLoanDelinquencyRangeChangeBusinessEvent3(installmentLevelTotalAmount,
                                        installmentLevelTotalAmountExpected))//
                                .isEqualByComparingTo(installmentLevelTotalAmountExpected);//
                    }
                    return null;
                });
    }

    @Then("In Loan details delinquent.lastRepaymentAmount is {int} EUR with lastRepaymentDate {string}")
    public void delinquentLastRepaymentAmountCheck(int expectedLastRepaymentAmount, String expectedLastRepaymentDate) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveLoan(loanId, Map.of("associations", "collection")));

        Double expectedLastRepaymentAmount1 = Double.valueOf(expectedLastRepaymentAmount);
        Double actualLastRepaymentAmount = loanDetails.getDelinquent().getLastRepaymentAmount().doubleValue();
        String actualLastRepaymentDate = FORMATTER.format(loanDetails.getDelinquent().getLastRepaymentDate());

        assertThat(actualLastRepaymentAmount)//
                .as(ErrorMessageHelper.wrongDataInDelinquentLastRepaymentAmount(actualLastRepaymentAmount, expectedLastRepaymentAmount1))//
                .isEqualTo(expectedLastRepaymentAmount);//
        assertThat(actualLastRepaymentDate)//
                .as(ErrorMessageHelper.wrongDataInDelinquentLastRepaymentDate(actualLastRepaymentDate, expectedLastRepaymentDate))//
                .isEqualTo(expectedLastRepaymentDate);//

        log.debug("loanDetails.delinquent.lastRepaymentAmount: {}", actualLastRepaymentAmount);
        log.debug("loanDetails.delinquent.lastRepaymentDate: {}", actualLastRepaymentDate);
    }

    private List<String> fetchValuesOfDelinquencyPausePeriods(List<String> header, GetLoansLoanIdDelinquencyPausePeriod t) {
        List<String> actualValues = new ArrayList<>();
        for (String headerName : header) {
            switch (headerName) {
                case "active" -> actualValues.add(t.getActive() == null ? null : t.getActive().toString());
                case "pausePeriodStart" ->
                    actualValues.add(t.getPausePeriodStart() == null ? null : FORMATTER.format(t.getPausePeriodStart()));
                case "pausePeriodEnd" -> actualValues.add(t.getPausePeriodEnd() == null ? null : FORMATTER.format(t.getPausePeriodEnd()));
                default -> throw new IllegalStateException(String.format("Header name %s cannot be found", headerName));
            }
        }
        return actualValues;
    }

    private void errorMessageAssertationFeign(long loanId, PostLoansDelinquencyActionRequest request, int errorCodeExpected,
            String errorMessageExpected) {
        CallFailedRuntimeException exception = fail(() -> fineractClient.loans().createLoanDelinquencyAction(loanId, request));

        assertThat(exception.getStatus()).as(ErrorMessageHelper.wrongErrorCode(exception.getStatus(), errorCodeExpected))
                .isEqualTo(errorCodeExpected);
        assertThat(exception.getDeveloperMessage())
                .as(ErrorMessageHelper.wrongErrorMessage(exception.getDeveloperMessage(), errorMessageExpected))
                .contains(errorMessageExpected);

        log.debug("ERROR CODE: {}", exception.getStatus());
        log.debug("ERROR MESSAGE: {}", exception.getDeveloperMessage());
    }

    @Then("LoanDelinquencyRangeChangeBusinessEvent is created")
    public void checkLoanDelinquencyRangeChangeBusinessEventCreated() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        eventAssertion.assertEventRaised(LoanDelinquencyRangeChangeEvent.class, loanId);
    }
}
