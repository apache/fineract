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
package org.apache.fineract.portfolio.delinquency.validator;

import static org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction.PAUSE;
import static org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction.RESUME;
import static org.apache.fineract.portfolio.delinquency.validator.DelinquencyActionParameters.ACTION;
import static org.apache.fineract.portfolio.delinquency.validator.DelinquencyActionParameters.DATE_FORMAT;
import static org.apache.fineract.portfolio.delinquency.validator.DelinquencyActionParameters.END_DATE;
import static org.apache.fineract.portfolio.delinquency.validator.DelinquencyActionParameters.LOCALE;
import static org.apache.fineract.portfolio.delinquency.validator.DelinquencyActionParameters.START_DATE;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyAction;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

class DelinquencyActionParseAndValidatorTest {

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
    private final DelinquencyActionParseAndValidator underTest = new DelinquencyActionParseAndValidator(fromJsonHelper);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US);

    @Test
    public void testParseAndValidationIsOKForPause() throws JsonProcessingException {
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);

        JsonCommand command = delinquencyAction("pause", "09 September 2022", "19 September 2022");

        LoanDelinquencyAction parsedDelinquencyAction = underTest.validateAndParseUpdate(command, loan, List.of(),
                localDate("09 September 2022"));
        Assertions.assertEquals(PAUSE, parsedDelinquencyAction.getAction());
        Assertions.assertEquals(localDate("09 September 2022"), parsedDelinquencyAction.getStartDate());
        Assertions.assertEquals(localDate("19 September 2022"), parsedDelinquencyAction.getEndDate());
    }

    @Test
    public void testParseAndValidationIsOKForResume() throws JsonProcessingException {
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);

        JsonCommand command = delinquencyAction("resume", "09 September 2022", null);

        LoanDelinquencyAction parsedDelinquencyAction = underTest.validateAndParseUpdate(command, loan,
                List.of(loanDelinquencyAction(PAUSE, "05 September 2022", "15 September 2022")), localDate("09 September 2022"));
        Assertions.assertEquals(DelinquencyAction.RESUME, parsedDelinquencyAction.getAction());
        Assertions.assertEquals(localDate("09 September 2022"), parsedDelinquencyAction.getStartDate());
        Assertions.assertNull(parsedDelinquencyAction.getEndDate());
    }

    @Test
    public void testPauseBothStartAndEndDateIsOverlappingWithAnActivePause() throws JsonProcessingException {
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);

        List<LoanDelinquencyAction> existing = List.of(loanDelinquencyAction(PAUSE, "14 September 2022", "22 September 2022"));
        JsonCommand command = delinquencyAction("pause", "09 September 2022", "15 September 2022");

        assertPlatformValidationException("Delinquency pause period cannot overlap with another pause period",
                "loan-delinquency-action-overlapping",
                () -> underTest.validateAndParseUpdate(command, loan, existing, localDate("09 September 2022")));
    }

    @Test
    public void testPauseStartIsOverlappingWithAnActivePause() throws JsonProcessingException {
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);

        List<LoanDelinquencyAction> existing = List.of(loanDelinquencyAction(PAUSE, "14 September 2022", "22 September 2022"));
        JsonCommand command = delinquencyAction("pause", "15 September 2022", "23 September 2022");

        assertPlatformValidationException("Delinquency pause period cannot overlap with another pause period",
                "loan-delinquency-action-overlapping",
                () -> underTest.validateAndParseUpdate(command, loan, existing, localDate("09 September 2022")));
    }

    @Test
    public void testNewPauseEndIsTheSameAsExistingPauseStart() throws JsonProcessingException {
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);

        List<LoanDelinquencyAction> existing = List.of(loanDelinquencyAction(PAUSE, "15 September 2022", "22 September 2022"));
        JsonCommand command = delinquencyAction("pause", "09 September 2022", "15 September 2022");

        assertPlatformValidationException("Delinquency pause period cannot overlap with another pause period",
                "loan-delinquency-action-overlapping",
                () -> underTest.validateAndParseUpdate(command, loan, existing, localDate("09 September 2022")));
    }

    @Test
    public void testNewPauseEndIsTheSameAsExistingPauseEnd() throws JsonProcessingException {
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);

        List<LoanDelinquencyAction> existing = List.of(loanDelinquencyAction(PAUSE, "15 September 2022", "22 September 2022"));
        JsonCommand command = delinquencyAction("pause", "22 September 2022", "25 September 2022");

        assertPlatformValidationException("Delinquency pause period cannot overlap with another pause period",
                "loan-delinquency-action-overlapping",
                () -> underTest.validateAndParseUpdate(command, loan, existing, localDate("09 September 2022")));
    }

    @Test
    public void testNewPauseEndIsOverlappingWithExistingPause() throws JsonProcessingException {
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);

        List<LoanDelinquencyAction> existing = List.of(loanDelinquencyAction(PAUSE, "15 September 2022", "22 September 2022"));
        JsonCommand command = delinquencyAction("pause", "13 September 2022", "20 September 2022");

        assertPlatformValidationException("Delinquency pause period cannot overlap with another pause period",
                "loan-delinquency-action-overlapping",
                () -> underTest.validateAndParseUpdate(command, loan, existing, localDate("09 September 2022")));
    }

    @Test
    public void testNewPauseIsNotOverlappingBecauseThereWasAResume() throws JsonProcessingException {
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);

        JsonCommand command = delinquencyAction("pause", "18 September 2022", "20 September 2022");

        List<LoanDelinquencyAction> existing = List.of(loanDelinquencyAction(PAUSE, "15 September 2022", "22 September 2022"), //
                loanDelinquencyAction(RESUME, "17 September 2022") //
        );

        LoanDelinquencyAction parsedDelinquencyAction = underTest.validateAndParseUpdate(command, loan, existing,
                localDate("18 September 2022"));
        Assertions.assertEquals(PAUSE, parsedDelinquencyAction.getAction());
        Assertions.assertEquals(localDate("18 September 2022"), parsedDelinquencyAction.getStartDate());
        Assertions.assertEquals(localDate("20 September 2022"), parsedDelinquencyAction.getEndDate());
    }

    @Test
    public void testResumeIsNotOverlappingWithAnActivePause() throws JsonProcessingException {
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);

        List<LoanDelinquencyAction> existing = List.of(loanDelinquencyAction(PAUSE, "05 September 2022", "08 September 2022"));
        JsonCommand command = delinquencyAction("resume", "09 September 2022", null);

        assertPlatformValidationException("Resume Delinquency Action can only be created during an active pause",
                "loan-delinquency-action-resume-should-be-on-pause",
                () -> underTest.validateAndParseUpdate(command, loan, existing, localDate("09 September 2022")));
    }

    @Test
    public void testValidationErrorWhenDelinquencyActionIsMissing() throws JsonProcessingException {
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.APPROVED);

        JsonCommand command = delinquencyAction(null, "09 September 2022", "19 September 2022");

        assertPlatformValidationException("Delinquency Action must not be null or empty", "loan-delinquency-action-missing-action",
                () -> underTest.validateAndParseUpdate(command, loan, List.of(), localDate("09 September 2022")));
    }

    @Test
    public void testValidationErrorWhenLoanIsNotActive() throws JsonProcessingException {
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.APPROVED);

        JsonCommand command = delinquencyAction("pause", "09 September 2022", "19 September 2022");

        assertPlatformValidationException("Delinquency actions can be created only for active loans.",
                "loan-delinquency-action-invalid-loan-state",
                () -> underTest.validateAndParseUpdate(command, loan, List.of(), localDate("09 September 2022")));
    }

    @Test
    public void testValidationErrorResumeShouldHaveNoEndDate() throws JsonProcessingException {
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);

        JsonCommand command = delinquencyAction("resume", "09 September 2022", "19 September 2022");

        assertPlatformValidationException("Resume Delinquency action can not have end date",
                "loan-delinquency-action-resume-should-have-no-end-date",
                () -> underTest.validateAndParseUpdate(command, loan, List.of(), localDate("09 September 2022")));
    }

    @Test
    public void testValidationErrorResumeInvalidStartDate() throws JsonProcessingException {
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);

        JsonCommand command = delinquencyAction("resume", "09 September 2022", "19 September 2022");

        assertPlatformValidationException("Start date of the Resume Delinquency action must be the current business date",
                "loan-delinquency-action-invalid-start-date",
                () -> underTest.validateAndParseUpdate(command, loan, List.of(), localDate("10 September 2022")));
    }

    @Test
    public void testValidationErrorPausePeriodShouldBeAtLeastOneDay() throws JsonProcessingException {
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);

        JsonCommand command = delinquencyAction("pause", "10 September 2022", "10 September 2022");

        assertPlatformValidationException("Delinquency pause period must be at least one day",
                "loan-delinquency-action-invalid-start-date-and-end-date",
                () -> underTest.validateAndParseUpdate(command, loan, List.of(), localDate("09 September 2022")));
    }

    @Test
    public void testValidationErrorPausePeriodMustBeInFuture() throws JsonProcessingException {
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);

        JsonCommand command = delinquencyAction("pause", "08 September 2022", "09 September 2022");

        assertPlatformValidationException("Start date of pause period must be in the future", "loan-delinquency-action-invalid-start-date",
                () -> underTest.validateAndParseUpdate(command, loan, List.of(), localDate("09 September 2022")));
    }

    @NotNull
    private JsonCommand delinquencyAction(String action, String startDate, String endDate) throws JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        map.put(ACTION, action);
        map.put(DATE_FORMAT, "dd MMMM yyyy");
        map.put(LOCALE, "en");
        map.put(START_DATE, startDate);
        map.put(END_DATE, endDate);
        return createJsonCommand(map);
    }

    private LocalDate localDate(String date) {
        return LocalDate.parse(date, DATE_TIME_FORMATTER);
    }

    @NotNull
    private JsonCommand createJsonCommand(Map<String, Object> jsonMap) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        return new JsonCommand(null, JsonParser.parseString(json));
    }

    private void assertPlatformValidationException(String message, String code, Executable executable) {
        PlatformApiDataValidationException validationException = assertThrows(PlatformApiDataValidationException.class, executable);
        assertPlatformException(message, code, validationException);
    }

    private void assertPlatformException(String expectedMessage, String expectedCode,
            PlatformApiDataValidationException platformApiDataValidationException) {
        Assertions.assertEquals(expectedMessage, platformApiDataValidationException.getErrors().get(0).getDefaultUserMessage());
        Assertions.assertEquals(expectedCode, platformApiDataValidationException.getErrors().get(0).getUserMessageGlobalisationCode());
    }

    private LoanDelinquencyAction loanDelinquencyAction(DelinquencyAction action, String startTime, String endTime) {
        return new LoanDelinquencyAction(null, action, localDate(startTime), localDate(endTime));
    }

    private LoanDelinquencyAction loanDelinquencyAction(DelinquencyAction action, String startTime) {
        return new LoanDelinquencyAction(null, action, localDate(startTime), null);
    }

}
