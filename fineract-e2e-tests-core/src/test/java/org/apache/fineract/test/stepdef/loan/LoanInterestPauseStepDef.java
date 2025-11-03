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

import io.cucumber.java.en.Then;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.InterestPauseRequestDto;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.services.LoanInterestPauseApi;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.ErrorResponse;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.loan.LoanBalanceChangedEvent;
import org.apache.fineract.test.messaging.event.loan.LoanScheduleVariationsAddedEvent;
import org.apache.fineract.test.messaging.event.loan.LoanScheduleVariationsDeletedEvent;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.junit.jupiter.api.Assertions;
import retrofit2.Response;

@RequiredArgsConstructor
public class LoanInterestPauseStepDef extends AbstractStepDef {

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final int INTEREST_PAUSE_TERM_TYPE_ID = 11;

    private final LoanInterestPauseApi loanInterestPauseApi;
    private final EventAssertion eventAssertion;

    @Then("Create an interest pause period with start date {string} and end date {string}")
    public void interestPauseCreate(final String startDate, final String endDate) throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Assertions.assertNotNull(loanResponse.body());
        final Long loanId = loanResponse.body().getLoanId();

        final InterestPauseRequestDto interestPauseRequest = LoanRequestFactory.defaultInterestPauseRequest().startDate(startDate)
                .endDate(endDate);
        final Response<CommandProcessingResult> interestPauseResponse = loanInterestPauseApi
                .createInterestPause(loanId, interestPauseRequest).execute();
        ErrorHelper.checkSuccessfulApiCall(interestPauseResponse);

        Assertions.assertNotNull(interestPauseResponse.body());
        final Long variationId = interestPauseResponse.body().getResourceId();
        testContext().set(TestContextKey.INTEREST_PAUSE_VARIATION_ID, variationId);
    }

    @Then("Delete the interest pause period")
    public void interestPauseDelete() throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Assertions.assertNotNull(loanResponse.body());
        final Long loanId = loanResponse.body().getLoanId();

        final Long variationId = testContext().get(TestContextKey.INTEREST_PAUSE_VARIATION_ID);
        Assertions.assertNotNull(variationId, "Interest pause variation ID must be set before deletion");

        final Response<Void> deleteResponse = loanInterestPauseApi.deleteInterestPause(loanId, variationId).execute();
        ErrorHelper.checkSuccessfulApiCall(deleteResponse);
    }

    @Then("Update the interest pause period with start date {string} and end date {string}")
    public void interestPauseUpdate(final String startDate, final String endDate) throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Assertions.assertNotNull(loanResponse.body());
        final Long loanId = loanResponse.body().getLoanId();

        final Long variationId = testContext().get(TestContextKey.INTEREST_PAUSE_VARIATION_ID);
        Assertions.assertNotNull(variationId, "Interest pause variation ID must be set before update");

        final InterestPauseRequestDto interestPauseRequest = LoanRequestFactory.defaultInterestPauseRequest().startDate(startDate)
                .endDate(endDate);
        final Response<CommandProcessingResult> updateResponse = loanInterestPauseApi
                .updateInterestPause(loanId, variationId, interestPauseRequest).execute();
        ErrorHelper.checkSuccessfulApiCall(updateResponse);
    }

    @Then("Admin is not able to add an interest pause period with start date {string} and end date {string}")
    public void createInterestPauseFailure(final String startDate, final String endDate) throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Assertions.assertNotNull(loanResponse.body());
        final Long loanId = loanResponse.body().getLoanId();

        final InterestPauseRequestDto interestPauseRequest = LoanRequestFactory.defaultInterestPauseRequest().startDate(startDate)
                .endDate(endDate);
        final Response<CommandProcessingResult> interestPauseResponse = loanInterestPauseApi
                .createInterestPause(loanId, interestPauseRequest).execute();

        ErrorResponse errorDetails = ErrorResponse.from(interestPauseResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.addInterestPauseForNotInterestBearingLoanFailure())
                .isEqualTo(403);
        assertThat(errorDetails.getSingleError().getDeveloperMessage())
                .isEqualTo(ErrorMessageHelper.addInterestPauseForNotInterestBearingLoanFailure());
    }

    @Then("Admin is not able to add an interest pause period with start date {string} and end date {string} due to inactive loan status")
    public void createInterestPauseForInactiveLoanFailure(final String startDate, final String endDate) throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Assertions.assertNotNull(loanResponse.body());
        final Long loanId = loanResponse.body().getLoanId();

        final InterestPauseRequestDto interestPauseRequest = LoanRequestFactory.defaultInterestPauseRequest().startDate(startDate)
                .endDate(endDate);
        final Response<CommandProcessingResult> interestPauseResponse = loanInterestPauseApi
                .createInterestPause(loanId, interestPauseRequest).execute();

        final ErrorResponse errorDetails = ErrorResponse.from(interestPauseResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.addInterestPauseForNotInactiveLoanFailure()).isEqualTo(403);
        assertThat(errorDetails.getSingleError().getDeveloperMessage())
                .isEqualTo(ErrorMessageHelper.addInterestPauseForNotInactiveLoanFailure());
    }

    @Then("LoanScheduleVariationsAddedBusinessEvent is created for interest pause from {string} to {string}")
    public void checkLoanScheduleVariationsAddedBusinessEvent(final String start, final String end) {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Assertions.assertNotNull(loanResponse.body());
        final Long loanId = loanResponse.body().getLoanId();

        final LocalDate startDateParsed = LocalDate.parse(start, FORMATTER);
        final LocalDate endDateParsed = LocalDate.parse(end, FORMATTER);

        eventAssertion.assertEvent(LoanScheduleVariationsAddedEvent.class, loanId).extractingData(loanAccountData -> {
            assertThat(loanAccountData.getLoanTermVariations()).isNotNull();
            assertThat(loanAccountData.getLoanTermVariations()).isNotEmpty();

            boolean foundInterestPause = loanAccountData.getLoanTermVariations().stream()
                    .anyMatch(variation -> isInterestPauseWithDates(variation, startDateParsed, endDateParsed));

            assertThat(foundInterestPause)
                    .as("LoanTermVariations should contain an INTEREST_PAUSE with start date '%s' and end date '%s'", start, end).isTrue();

            return true;
        });
    }

    @Then("LoanScheduleVariationsAddedBusinessEvent is not raised on {string}")
    public void checkLoanScheduleVariationsAddedBusinessEvent(final String date) {
        eventAssertion.assertEventNotRaised(LoanScheduleVariationsAddedEvent.class,
                em -> FORMATTER.format(em.getBusinessDate()).equals(date));
    }

    @Then("LoanScheduleVariationsDeletedBusinessEvent is created for interest pause from {string} to {string}")
    public void checkLoanScheduleVariationsDeletedBusinessEvent(final String start, final String end) {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Assertions.assertNotNull(loanResponse.body());
        final Long loanId = loanResponse.body().getLoanId();

        final LocalDate startDateParsed = LocalDate.parse(start, FORMATTER);
        final LocalDate endDateParsed = LocalDate.parse(end, FORMATTER);

        eventAssertion.assertEvent(LoanScheduleVariationsDeletedEvent.class, loanId).extractingData(loanAccountData -> {
            boolean foundInterestPause = false;

            if (loanAccountData.getLoanTermVariations() != null && !loanAccountData.getLoanTermVariations().isEmpty()) {
                foundInterestPause = loanAccountData.getLoanTermVariations().stream()
                        .anyMatch(variation -> isInterestPauseWithDates(variation, startDateParsed, endDateParsed));
            }

            assertThat(foundInterestPause)
                    .as("LoanTermVariations should NOT contain an INTEREST_PAUSE with start date '%s' and end date '%s' after deletion",
                            start, end)
                    .isFalse();

            return true;
        });
    }

    @Then("LoanScheduleVariationsAddedBusinessEvent is created for interest pause update from {string} and {string} to {string} and {string}")
    public void checkLoanScheduleVariationsAddedBusinessEventForUpdate(final String oldStart, final String oldEnd, final String newStart,
            final String newEnd) {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Assertions.assertNotNull(loanResponse.body());
        final Long loanId = loanResponse.body().getLoanId();

        final LocalDate oldStartDateParsed = LocalDate.parse(oldStart, FORMATTER);
        final LocalDate oldEndDateParsed = LocalDate.parse(oldEnd, FORMATTER);
        final LocalDate newStartDateParsed = LocalDate.parse(newStart, FORMATTER);
        final LocalDate newEndDateParsed = LocalDate.parse(newEnd, FORMATTER);

        eventAssertion.assertEvent(LoanScheduleVariationsAddedEvent.class, loanId).extractingData(loanAccountData -> {
            assertThat(loanAccountData.getLoanTermVariations()).isNotNull();
            assertThat(loanAccountData.getLoanTermVariations()).isNotEmpty();

            boolean foundOldInterestPause = loanAccountData.getLoanTermVariations().stream()
                    .anyMatch(variation -> isInterestPauseWithDates(variation, oldStartDateParsed, oldEndDateParsed));

            assertThat(foundOldInterestPause)
                    .as("LoanTermVariations should NOT contain old INTEREST_PAUSE with start date '%s' and end date '%s' after update",
                            oldStart, oldEnd)
                    .isFalse();

            boolean foundUpdatedInterestPause = loanAccountData.getLoanTermVariations().stream()
                    .anyMatch(variation -> isInterestPauseWithDates(variation, newStartDateParsed, newEndDateParsed));

            assertThat(foundUpdatedInterestPause)
                    .as("LoanTermVariations should contain updated INTEREST_PAUSE with new start date '%s' and new end date '%s'", newStart,
                            newEnd)
                    .isTrue();

            return true;
        });
    }

    @Then("LoanBalanceChangedBusinessEvent is created on {string}")
    public void checkLoanBalanceChangedBusinessEvent(final String date) {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Assertions.assertNotNull(loanResponse.body());
        final Long loanId = loanResponse.body().getLoanId();

        final LocalDate expectedBusinessDateParsed = LocalDate.parse(date, FORMATTER);
        eventAssertion.assertEvent(LoanBalanceChangedEvent.class, loanId).isRaisedOnBusinessDate(expectedBusinessDateParsed);
    }

    private boolean isInterestPauseWithDates(final org.apache.fineract.avro.loan.v1.LoanTermVariationsDataV1 variation,
            final LocalDate startDate, final LocalDate endDate) {
        if (variation.getTermType() == null) {
            return false;
        }
        if (!Integer.valueOf(INTEREST_PAUSE_TERM_TYPE_ID).equals(variation.getTermType().getId())) {
            return false;
        }
        if (variation.getTermVariationApplicableFrom() == null || variation.getDateValue() == null) {
            return false;
        }
        try {
            final LocalDate variationStartDate = LocalDate.parse(variation.getTermVariationApplicableFrom());
            final LocalDate variationEndDate = LocalDate.parse(variation.getDateValue());
            return variationStartDate.equals(startDate) && variationEndDate.equals(endDate);
        } catch (Exception e) {
            return false;
        }
    }

}
