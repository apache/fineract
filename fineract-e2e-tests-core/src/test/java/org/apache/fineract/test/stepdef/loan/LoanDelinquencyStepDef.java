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

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.fineract.avro.loan.v1.LoanAccountDelinquencyRangeDataV1;
import org.apache.fineract.avro.loan.v1.LoanInstallmentDelinquencyBucketDataV1;
import org.apache.fineract.client.models.GetDelinquencyActionsResponse;
import org.apache.fineract.client.models.GetDelinquencyRangesResponse;
import org.apache.fineract.client.models.GetDelinquencyTagHistoryResponse;
import org.apache.fineract.client.models.GetLoansLoanIdDelinquencyPausePeriod;
import org.apache.fineract.client.models.GetLoansLoanIdDelinquencySummary;
import org.apache.fineract.client.models.GetLoansLoanIdLoanInstallmentLevelDelinquency;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetUsersUserIdResponse;
import org.apache.fineract.client.models.PostLoansDelinquencyActionRequest;
import org.apache.fineract.client.models.PostLoansDelinquencyActionResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PostUsersResponse;
import org.apache.fineract.client.services.LoansApi;
import org.apache.fineract.client.services.UsersApi;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.test.api.ApiProperties;
import org.apache.fineract.test.data.DelinquencyRange;
import org.apache.fineract.test.data.LoanStatus;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.ErrorResponse;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.messaging.event.loan.delinquency.LoanDelinquencyRangeChangeEvent;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

@Slf4j
public class LoanDelinquencyStepDef extends AbstractStepDef {

    public static final String DATE_FORMAT = "dd MMMM yyyy";
    public static final String DEFAULT_LOCALE = "en";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final Gson GSON = new JSON().getGson();

    @Autowired
    private LoansApi loansApi;

    @Autowired
    private EventAssertion eventAssertion;

    @Autowired
    private EventCheckHelper eventCheckHelper;

    @Autowired
    private ApiProperties apiProperties;

    @Autowired
    private UsersApi usersApi;

    @Then("Admin checks that delinquency range is: {string} and has delinquentDate {string}")
    public void checkDelinquencyRange(String range, String delinquentDateExpected) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetails);
        Integer loanStatus = loanDetails.body().getStatus().getId();

        if (!LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.value.equals(loanStatus) && !LoanStatus.APPROVED.value.equals(loanStatus)) {
            String delinquentDateExpectedValue = "".equals(delinquentDateExpected) ? null : delinquentDateExpected;
            eventAssertion.assertEvent(LoanDelinquencyRangeChangeEvent.class, loanId)//
                    .extractingData(LoanAccountDelinquencyRangeDataV1::getDelinquentDate)//
                    .isEqualTo(delinquentDateExpectedValue);//
        }

        DelinquencyRange expectedDelinquencyRange = DelinquencyRange.valueOf(range);
        String expectedDelinquencyRangeValue = expectedDelinquencyRange.getValue();

        String actualDelinquencyRangeValue = DelinquencyRange.NO_DELINQUENCY.value;
        GetDelinquencyRangesResponse actualDelinquencyRange = loanDetails.body().getDelinquencyRange();
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
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        String delinquentDateExpectedValue = "".equals(delinquentDateExpected) ? null : delinquentDateExpected;
        eventAssertion.assertEvent(LoanDelinquencyRangeChangeEvent.class, loanId)
                .extractingData(LoanAccountDelinquencyRangeDataV1::getDelinquentDate).isEqualTo(delinquentDateExpectedValue);

        DelinquencyRange expectedDelinquencyRange = DelinquencyRange.valueOf(range);
        String expectedDelinquencyRangeValue = expectedDelinquencyRange.getValue();

        Response<List<GetDelinquencyTagHistoryResponse>> delinquencyHistoryDetails = loansApi.getDelinquencyTagHistory(loanId).execute();
        ErrorHelper.checkSuccessfulApiCall(delinquencyHistoryDetails);

        String actualDelinquencyRangeValue = DelinquencyRange.NO_DELINQUENCY.value;
        String actualDelinquencyAddedOnDate = "";
        int i = Integer.parseInt(nthInList) - 1;
        GetDelinquencyTagHistoryResponse delinquencyTag = delinquencyHistoryDetails.body().get(i);
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

        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<List<GetDelinquencyTagHistoryResponse>> delinquencyHistoryDetails = loansApi.getDelinquencyTagHistory(loanId).execute();
        ErrorHelper.checkSuccessfulApiCall(delinquencyHistoryDetails);
        List<GetDelinquencyTagHistoryResponse> body = delinquencyHistoryDetails.body();

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
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("pause")//
                .startDate(startDate)//
                .endDate(endDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        Response<PostLoansDelinquencyActionResponse> response = loansApi.createLoanDelinquencyAction(loanId, request).execute();
        testContext().set(TestContextKey.LOAN_DELINQUENCY_ACTION_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);

        eventCheckHelper.loanAccountDelinquencyPauseChangedBusinessEventCheck(loanId);
    }

    @When("Created user with CREATE_DELINQUENCY_ACTION permission initiate a DELINQUENCY PAUSE with startDate: {string} and endDate: {string}")
    public void delinquencyPauseWithCreatedUser(String startDate, String endDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("pause")//
                .startDate(startDate)//
                .endDate(endDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        Map<String, String> headerMap = new HashMap<>();
        Response<PostUsersResponse> createUserResponse = testContext().get(TestContextKey.CREATED_SIMPLE_USER_RESPONSE);
        Long createdUserId = createUserResponse.body().getResourceId();
        Response<GetUsersUserIdResponse> user = usersApi.retrieveOne31(createdUserId).execute();
        ErrorHelper.checkSuccessfulApiCall(user);
        String authorizationString = user.body().getUsername() + ":" + apiProperties.getPassword();
        Base64 base64 = new Base64();
        headerMap.put("Authorization",
                "Basic " + new String(base64.encode(authorizationString.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));

        Response<PostLoansDelinquencyActionResponse> response = loansApi.createLoanDelinquencyAction(loanId, request, headerMap).execute();
        testContext().set(TestContextKey.LOAN_DELINQUENCY_ACTION_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);

        eventCheckHelper.loanAccountDelinquencyPauseChangedBusinessEventCheck(loanId);
    }

    @Then("Created user with no CREATE_DELINQUENCY_ACTION permission gets an error when initiate a DELINQUENCY PAUSE with startDate: {string} and endDate: {string}")
    public void delinquencyPauseWithCreatedUserNOPermissionError(String startDate, String endDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        int errorCodeExpected = 403;
        String errorMessageExpected = "User has no authority to CREATE delinquency_actions";

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("pause")//
                .startDate(startDate)//
                .endDate(endDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        Map<String, String> headerMap = new HashMap<>();
        Response<PostUsersResponse> createUserResponse = testContext().get(TestContextKey.CREATED_SIMPLE_USER_RESPONSE);
        Long createdUserId = createUserResponse.body().getResourceId();
        Response<GetUsersUserIdResponse> user = usersApi.retrieveOne31(createdUserId).execute();
        ErrorHelper.checkSuccessfulApiCall(user);
        String authorizationString = user.body().getUsername() + ":" + apiProperties.getPassword();
        Base64 base64 = new Base64();
        headerMap.put("Authorization",
                "Basic " + new String(base64.encode(authorizationString.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));

        Response<PostLoansDelinquencyActionResponse> response = loansApi.createLoanDelinquencyAction(loanId, request, headerMap).execute();
        testContext().set(TestContextKey.LOAN_DELINQUENCY_ACTION_RESPONSE, response);
        int errorCodeActual = response.code();
        String errorBody = response.errorBody().string();
        ErrorResponse errorResponse = GSON.fromJson(errorBody, ErrorResponse.class);
        String errorMessageActual = errorResponse.getErrors().get(0).getDeveloperMessage();

        assertThat(errorCodeActual).as(ErrorMessageHelper.wrongErrorCode(errorCodeActual, errorCodeExpected)).isEqualTo(errorCodeExpected);
        assertThat(errorMessageActual).as(ErrorMessageHelper.wrongErrorMessage(errorMessageActual, errorMessageExpected))
                .isEqualTo(errorMessageExpected);

        log.info("ERROR CODE: {}", errorCodeActual);
        log.info("ERROR MESSAGE: {}", errorMessageActual);
    }

    @When("Admin initiate a DELINQUENCY RESUME with startDate: {string}")
    public void delinquencyResume(String startDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("resume")//
                .startDate(startDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        Response<PostLoansDelinquencyActionResponse> response = loansApi.createLoanDelinquencyAction(loanId, request).execute();
        testContext().set(TestContextKey.LOAN_DELINQUENCY_ACTION_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);

        eventCheckHelper.loanAccountDelinquencyPauseChangedBusinessEventCheck(loanId);
    }

    @When("Admin initiate a DELINQUENCY PAUSE by loanExternalId with startDate: {string} and endDate: {string}")
    public void delinquencyPauseByLoanExternalId(String startDate, String endDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanResponse.body().getResourceExternalId();
        long loanId = loanResponse.body().getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("pause")//
                .startDate(startDate)//
                .endDate(endDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        Response<PostLoansDelinquencyActionResponse> response = loansApi.createLoanDelinquencyAction1(loanExternalId, request).execute();
        testContext().set(TestContextKey.LOAN_DELINQUENCY_ACTION_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);

        eventCheckHelper.loanAccountDelinquencyPauseChangedBusinessEventCheck(loanId);
    }

    @When("Admin initiate a DELINQUENCY RESUME by loanExternalId with startDate: {string}")
    public void delinquencyResumeByLoanExternalId(String startDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanResponse.body().getResourceExternalId();
        long loanId = loanResponse.body().getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("resume")//
                .startDate(startDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        Response<PostLoansDelinquencyActionResponse> response = loansApi.createLoanDelinquencyAction1(loanExternalId, request).execute();
        testContext().set(TestContextKey.LOAN_DELINQUENCY_ACTION_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);

        eventCheckHelper.loanAccountDelinquencyPauseChangedBusinessEventCheck(loanId);
    }

    @Then("Delinquency-actions have the following data:")
    public void getDelinquencyActionData(DataTable table) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        List<List<String>> data = table.asLists();
        int nrOfLinesExpected = data.size() - 1;

        Response<List<GetDelinquencyActionsResponse>> response = loansApi.getLoanDelinquencyActions(loanId).execute();
        int nrOfLinesActual = response.body().size();

        assertThat(nrOfLinesActual)//
                .as(ErrorMessageHelper.wrongNumberOfLinesInDelinquencyActions(nrOfLinesActual, nrOfLinesExpected))//
                .isEqualTo(nrOfLinesExpected);//

        for (int i = 1; i < data.size(); i++) {
            List<String> expectedValues = data.get(i);

            GetDelinquencyActionsResponse lineActual = response.body().get(i - 1);

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
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("TEST")//
                .startDate(startDate)//
                .endDate(endDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        Response<PostLoansDelinquencyActionResponse> response = loansApi.createLoanDelinquencyAction(loanId, request).execute();
        int errorCodeExpected = 400;
        String errorMessageExpected = "Invalid Delinquency Action: TEST";
        errorMessageAssertation(response, errorCodeExpected, errorMessageExpected);
    }

    @Then("Initiating a DELINQUENCY PAUSE with startDate before the actual business date results an error - startDate: {string}, endDate: {string}")
    public void delinquencyPauseStartDateError(String startDate, String endDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("pause")//
                .startDate(startDate)//
                .endDate(endDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        Response<PostLoansDelinquencyActionResponse> response = loansApi.createLoanDelinquencyAction(loanId, request).execute();
        int errorCodeExpected = 400;
        String errorMessageExpected = "Start date of pause period must be in the future";
        errorMessageAssertation(response, errorCodeExpected, errorMessageExpected);
    }

    @Then("Initiating a DELINQUENCY PAUSE on a non-active loan results an error - startDate: {string}, endDate: {string}")
    public void delinquencyPauseNonActiveLoanError(String startDate, String endDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("pause")//
                .startDate(startDate)//
                .endDate(endDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        Response<PostLoansDelinquencyActionResponse> response = loansApi.createLoanDelinquencyAction(loanId, request).execute();
        int errorCodeExpected = 400;
        String errorMessageExpected = "Delinquency actions can be created only for active loans.";
        errorMessageAssertation(response, errorCodeExpected, errorMessageExpected);
    }

    @Then("Initiating a DELINQUENCY RESUME on a non-active loan results an error - startDate: {string}")
    public void delinquencyResumeNonActiveLoanError(String startDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("resume")//
                .startDate(startDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        Response<PostLoansDelinquencyActionResponse> response = loansApi.createLoanDelinquencyAction(loanId, request).execute();
        int errorCodeExpected = 400;
        String errorMessageExpected = "Delinquency actions can be created only for active loans.";
        errorMessageAssertation(response, errorCodeExpected, errorMessageExpected);
    }

    @Then("Overlapping PAUSE periods result an error - startDate: {string}, endDate: {string}")
    public void delinquencyPauseOverlappingError(String startDate, String endDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("pause")//
                .startDate(startDate)//
                .endDate(endDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        Response<PostLoansDelinquencyActionResponse> response = loansApi.createLoanDelinquencyAction(loanId, request).execute();
        int errorCodeExpected = 400;
        String errorMessageExpected = "Delinquency pause period cannot overlap with another pause period";
        errorMessageAssertation(response, errorCodeExpected, errorMessageExpected);
    }

    @Then("Initiating a DELINQUENCY RESUME without an active PAUSE period results an error - startDate: {string}")
    public void delinquencyResumeWithoutPauseError(String startDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("resume")//
                .startDate(startDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        Response<PostLoansDelinquencyActionResponse> response = loansApi.createLoanDelinquencyAction(loanId, request).execute();
        int errorCodeExpected = 400;
        String errorMessageExpected = "Resume Delinquency Action can only be created during an active pause";
        errorMessageAssertation(response, errorCodeExpected, errorMessageExpected);
    }

    @Then("Initiating a DELINQUENCY RESUME with start date other than actual business date results an error - startDate: {string}")
    public void delinquencyResumeStartDateError(String startDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("resume")//
                .startDate(startDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        Response<PostLoansDelinquencyActionResponse> response = loansApi.createLoanDelinquencyAction(loanId, request).execute();
        int errorCodeExpected = 400;
        String errorMessageExpected = "Start date of the Resume Delinquency action must be the current business date";
        errorMessageAssertation(response, errorCodeExpected, errorMessageExpected);
    }

    @Then("Initiating a DELINQUENCY RESUME with an endDate results an error - startDate: {string}, endDate: {string}")
    public void delinquencyResumeWithEndDateError(String startDate, String endDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostLoansDelinquencyActionRequest request = new PostLoansDelinquencyActionRequest()//
                .action("resume")//
                .startDate(startDate)//
                .endDate(endDate)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//

        Response<PostLoansDelinquencyActionResponse> response = loansApi.createLoanDelinquencyAction(loanId, request).execute();
        int errorCodeExpected = 400;
        String errorMessageExpected = "Resume Delinquency action can not have end date";
        errorMessageAssertation(response, errorCodeExpected, errorMessageExpected);
    }

    @Then("Installment level delinquency event has correct data")
    public void installmentLevelDelinquencyEventCheck() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        eventCheckHelper.installmentLevelDelinquencyRangeChangeEventCheck(loanId);
    }

    @Then("INSTALLMENT level delinquency is null")
    public void installmentLevelDelinquencyNull() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetails);
        List<GetLoansLoanIdLoanInstallmentLevelDelinquency> installmentLevelDelinquency = loanDetails.body().getDelinquent()
                .getInstallmentLevelDelinquency() == null ? null : loanDetails.body().getDelinquent().getInstallmentLevelDelinquency();
        assertThat(installmentLevelDelinquency).isNull();
    }

    @Then("Loan has the following LOAN level delinquency data:")
    public void loanDelinquencyDataCheck(DataTable table) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        List<String> expectedValuesList = table.asLists().get(1);
        DelinquencyRange expectedDelinquencyRange = DelinquencyRange.valueOf(expectedValuesList.get(0));
        String expectedDelinquencyRangeValue = expectedDelinquencyRange.getValue();
        expectedValuesList.set(0, expectedDelinquencyRangeValue);

        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetails);
        String actualDelinquencyRangeValue = loanDetails.body().getDelinquencyRange() == null ? "NO_DELINQUENCY"
                : loanDetails.body().getDelinquencyRange().getClassification();
        GetLoansLoanIdDelinquencySummary delinquent = loanDetails.body().getDelinquent();
        List<String> actualValuesList = List.of(actualDelinquencyRangeValue, delinquent.getDelinquentAmount().toString(),
                delinquent.getDelinquentDate() == null ? "null" : FORMATTER.format(delinquent.getDelinquentDate()),
                delinquent.getDelinquentDays().toString(), delinquent.getPastDueDays().toString());

        assertThat(actualValuesList).as(ErrorMessageHelper.wrongValueInLoanLevelDelinquencyData(actualValuesList, expectedValuesList))
                .isEqualTo(expectedValuesList);
    }

    @Then("Loan has the following INSTALLMENT level delinquency data:")
    public void loanDelinquencyInstallmentLevelDataCheck(DataTable table) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetails);
        List<GetLoansLoanIdLoanInstallmentLevelDelinquency> installmentLevelDelinquency = loanDetails.body().getDelinquent()
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
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        List<List<String>> expectedData = table.asLists();
        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetails);

        List<GetLoansLoanIdDelinquencyPausePeriod> delinquencyPausePeriods = loanDetails.body().getDelinquent()
                .getDelinquencyPausePeriods();

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
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetails);
        String actualDate = FORMATTER.format(loanDetails.body().getDelinquent().getNextPaymentDueDate());

        assertThat(actualDate).as(ErrorMessageHelper.wrongDataInNextPaymentDueDate(actualDate, expectedDate)).isEqualTo(expectedDate);
    }

    @Then("LoanAccountDelinquencyRangeDataV1 has delinquencyRange field with value {string}")
    public void checkDelinquencyRangeInEvent(String expectedRange) {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        DelinquencyRange expectedDelinquencyRange = DelinquencyRange.valueOf(expectedRange);
        String expectedDelinquencyRangeValue = expectedDelinquencyRange.getValue();

        eventAssertion.assertEvent(LoanDelinquencyRangeChangeEvent.class, loanId)//
                .extractingData(loanAccountDelinquencyRangeDataV1 -> { //
                    String actualDelinquencyRangeValue = loanAccountDelinquencyRangeDataV1.getDelinquencyRange().getClassification();//
                    assertThat(actualDelinquencyRangeValue)//
                            .as(ErrorMessageHelper.delinquencyRangeError(actualDelinquencyRangeValue, expectedDelinquencyRangeValue))//
                            .isEqualTo(expectedDelinquencyRangeValue);//
                    return null;
                });
    }

    @Then("LoanDelinquencyRangeChangeBusinessEvent has the same Delinquency range, date and amount as in LoanDetails on both loan- and installment-level")
    public void checkDelinquencyRangeInEvent() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        GetDelinquencyRangesResponse delinquencyRange = loanDetails.body().getDelinquencyRange();
        GetLoansLoanIdDelinquencySummary delinquent = loanDetails.body().getDelinquent();

        eventAssertion.assertEvent(LoanDelinquencyRangeChangeEvent.class, loanId)//
                .extractingData(loanAccountDelinquencyRangeDataV1 -> { //
                    Long loanLevelDelinquencyRangeId = loanAccountDelinquencyRangeDataV1.getDelinquencyRange().getId();
                    String loanLevelDelinquencyRange = loanAccountDelinquencyRangeDataV1.getDelinquencyRange().getClassification();
                    String loanLevelDelinquentDate = loanAccountDelinquencyRangeDataV1.getDelinquentDate();
                    BigDecimal loanLevelTotalAmount = loanAccountDelinquencyRangeDataV1.getAmount().getTotalAmount();

                    Long loanLevelDelinquencyRangeIdExpected = delinquencyRange.getId();
                    String loanLevelDelinquencyRangeExpected = delinquencyRange.getClassification();
                    String loanLevelDelinquentDateExpected = FORMATTER.format(delinquent.getDelinquentDate());
                    BigDecimal loanLevelTotalAmountExpected = new BigDecimal(delinquent.getDelinquentAmount());

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
                            .isEqualTo(loanLevelTotalAmountExpected);//

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
                                .isEqualTo(installmentLevelTotalAmountExpected);//
                    }
                    return null;
                });
    }

    @Then("In Loan details delinquent.lastRepaymentAmount is {int} EUR with lastRepaymentDate {string}")
    public void delinquentLastRepaymentAmountCheck(int expectedLastRepaymentAmount, String expectedLastRepaymentDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetails);

        Double expectedLastRepaymentAmount1 = Double.valueOf(expectedLastRepaymentAmount);
        Double actualLastRepaymentAmount = loanDetails.body().getDelinquent().getLastRepaymentAmount();
        String actualLastRepaymentDate = FORMATTER.format(loanDetails.body().getDelinquent().getLastRepaymentDate());

        assertThat(actualLastRepaymentAmount)//
                .as(ErrorMessageHelper.wrongDataInDelinquentLastRepaymentAmount(actualLastRepaymentAmount, expectedLastRepaymentAmount1))//
                .isEqualTo(expectedLastRepaymentAmount);//
        assertThat(actualLastRepaymentDate)//
                .as(ErrorMessageHelper.wrongDataInDelinquentLastRepaymentDate(actualLastRepaymentDate, expectedLastRepaymentDate))//
                .isEqualTo(expectedLastRepaymentDate);//

        log.info("loanDetails.delinquent.lastRepaymentAmount: {}", actualLastRepaymentAmount);
        log.info("loanDetails.delinquent.lastRepaymentDate: {}", actualLastRepaymentDate);
    }

    @SuppressFBWarnings("SF_SWITCH_NO_DEFAULT")
    private List<String> fetchValuesOfDelinquencyPausePeriods(List<String> header, GetLoansLoanIdDelinquencyPausePeriod t) {
        List<String> actualValues = new ArrayList<>();
        for (String headerName : header) {
            switch (headerName) {
                case "active" -> actualValues.add(t.getActive() == null ? null : t.getActive().toString());
                case "pausePeriodStart" ->
                    actualValues.add(t.getPausePeriodStart() == null ? null : FORMATTER.format(t.getPausePeriodStart()));
                case "pausePeriodEnd" -> actualValues.add(t.getPausePeriodEnd() == null ? null : FORMATTER.format(t.getPausePeriodEnd()));
            }
        }
        return actualValues;
    }

    private void errorMessageAssertation(Response<PostLoansDelinquencyActionResponse> response, int errorCodeExpected,
            String errorMessageExpected) throws IOException {
        String errorToString = response.errorBody().string();
        ErrorResponse errorResponse = GSON.fromJson(errorToString, ErrorResponse.class);
        String errorMessageActual = errorResponse.getErrors().get(0).getDeveloperMessage();
        int errorCodeActual = response.code();

        assertThat(errorCodeActual).as(ErrorMessageHelper.wrongErrorCode(errorCodeActual, errorCodeExpected)).isEqualTo(errorCodeExpected);
        assertThat(errorMessageActual).as(ErrorMessageHelper.wrongErrorMessage(errorMessageActual, errorMessageExpected))
                .isEqualTo(errorMessageExpected);

        log.info("ERROR CODE: {}", errorCodeActual);
        log.info("ERROR MESSAGE: {}", errorMessageActual);
    }

    @Then("LoanDelinquencyRangeChangeBusinessEvent is created")
    public void checkLoanDelinquencyRangeChangeBusinessEventCreated() {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        eventAssertion.assertEventRaised(LoanDelinquencyRangeChangeEvent.class, loanId);
    }
}
