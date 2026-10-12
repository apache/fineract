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

import static org.apache.fineract.portfolio.delinquency.validator.DelinquencyActionParameters.ACTION;
import static org.apache.fineract.portfolio.delinquency.validator.DelinquencyActionParameters.END_DATE;
import static org.apache.fineract.portfolio.delinquency.validator.DelinquencyActionParameters.START_DATE;

import com.google.gson.JsonElement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.validator.ParseAndValidator;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyAction;
import org.apache.fineract.portfolio.delinquency.helper.DelinquencyEffectivePauseHelper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DelinquencyActionParseAndValidator extends ParseAndValidator {

    private static final String VALIDATION_RESOURCE = "loanDelinquencyAction";

    private final FromJsonHelper jsonHelper;
    private final DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper;

    public LoanDelinquencyAction validateAndParseUpdate(@NonNull final JsonCommand command, Loan loan,
            List<LoanDelinquencyAction> savedDelinquencyActions, LocalDate businessDate) {
        final DataValidatorBuilder dataValidator = createValidator();
        final List<LoanDelinquencyActionData> effectiveDelinquencyList = delinquencyEffectivePauseHelper
                .calculateEffectiveDelinquencyList(savedDelinquencyActions);
        final LoanDelinquencyAction parsedDelinquencyAction = parseCommand(command, dataValidator);
        validateLoanIsActive(loan, dataValidator);
        throwExceptionIfValidationWarningsExist(dataValidator);
        if (DelinquencyAction.PAUSE.equals(parsedDelinquencyAction.getAction())) {
            validatePause(parsedDelinquencyAction, loan, effectiveDelinquencyList, dataValidator);
        } else if (DelinquencyAction.RESUME.equals(parsedDelinquencyAction.getAction())) {
            validateResume(parsedDelinquencyAction, savedDelinquencyActions, effectiveDelinquencyList, businessDate, dataValidator);
        }
        throwExceptionIfValidationWarningsExist(dataValidator);
        return parsedDelinquencyAction;
    }

    private void validatePause(final LoanDelinquencyAction parsedDelinquencyAction, final Loan loan,
            final List<LoanDelinquencyActionData> effectiveDelinquencyList, final DataValidatorBuilder dataValidator) {
        validateBothStartAndEndDatesAreProvided(parsedDelinquencyAction, dataValidator);
        throwExceptionIfValidationWarningsExist(dataValidator);
        validatePauseStartAndEndDate(parsedDelinquencyAction, dataValidator);
        throwExceptionIfValidationWarningsExist(dataValidator);
        validatePauseStartDateNotBeforeDisbursementDate(parsedDelinquencyAction, loan.getDisbursementDate(), dataValidator);
        throwExceptionIfValidationWarningsExist(dataValidator);
        validatePauseShallNotOverlap(parsedDelinquencyAction, effectiveDelinquencyList, dataValidator);
    }

    private void validateResume(final LoanDelinquencyAction parsedDelinquencyAction,
            final List<LoanDelinquencyAction> savedDelinquencyActions, final List<LoanDelinquencyActionData> effectiveDelinquencyList,
            final LocalDate businessDate, final DataValidatorBuilder dataValidator) {
        validateResumeStartDate(parsedDelinquencyAction, businessDate, dataValidator);
        throwExceptionIfValidationWarningsExist(dataValidator);
        validateResumeNoEndDate(parsedDelinquencyAction, dataValidator);
        throwExceptionIfValidationWarningsExist(dataValidator);
        validateResumeDoesNotExist(parsedDelinquencyAction, savedDelinquencyActions, dataValidator);
        throwExceptionIfValidationWarningsExist(dataValidator);
        validateResumeShouldBeOnActivePause(parsedDelinquencyAction, effectiveDelinquencyList, dataValidator);
    }

    private void validateBothStartAndEndDatesAreProvided(final LoanDelinquencyAction parsedDelinquencyAction,
            final DataValidatorBuilder dataValidator) {
        dataValidator.reset().parameter(START_DATE).value(parsedDelinquencyAction.getStartDate()).notNull();
        dataValidator.reset().parameter(END_DATE).value(parsedDelinquencyAction.getEndDate()).notNull();
    }

    private void validateResumeShouldBeOnActivePause(final LoanDelinquencyAction parsedDelinquencyAction,
            final List<LoanDelinquencyActionData> savedDelinquencyActions, final DataValidatorBuilder dataValidator) {
        final boolean match = savedDelinquencyActions.stream()
                .anyMatch(lda -> !DateUtils.isBefore(parsedDelinquencyAction.getStartDate(), lda.getStartDate())
                        && !DateUtils.isAfter(parsedDelinquencyAction.getStartDate(), lda.getEndDate()));
        if (!match) {
            failGeneralValidation(dataValidator, "resume.should.be.on.pause",
                    "Resume Delinquency Action can only be created during an active pause");
        }
    }

    private void validateResumeDoesNotExist(final LoanDelinquencyAction parsedDelinquencyAction,
            final List<LoanDelinquencyAction> savedDelinquencyActions, final DataValidatorBuilder dataValidator) {
        final boolean match = savedDelinquencyActions.stream() //
                .filter(action -> DelinquencyAction.RESUME.equals(action.getAction())) //
                .anyMatch(action -> parsedDelinquencyAction.getStartDate().isEqual(action.getStartDate()));
        if (match) {
            failGeneralValidation(dataValidator, "resume.should.be.unique", "There is an existing Resume Delinquency Action on this date");
        }
    }

    private void validateResumeNoEndDate(final LoanDelinquencyAction parsedDelinquencyAction, final DataValidatorBuilder dataValidator) {
        if (parsedDelinquencyAction.getEndDate() != null) {
            failParameterValidation(dataValidator, END_DATE, "resume.should.have.no.end.date",
                    "Resume Delinquency action can not have end date");
        }
    }

    private void validateResumeStartDate(final LoanDelinquencyAction parsedDelinquencyAction, final LocalDate businessDate,
            final DataValidatorBuilder dataValidator) {
        dataValidator.reset().parameter(START_DATE).value(parsedDelinquencyAction.getStartDate()).notNull();
        if (parsedDelinquencyAction.getStartDate() != null && !parsedDelinquencyAction.getStartDate().equals(businessDate)) {
            failParameterValidation(dataValidator, START_DATE, "resume.invalid.start.date",
                    "Start date of the Resume Delinquency action must be the current business date");
        }
    }

    private void validatePauseStartAndEndDate(final LoanDelinquencyAction parsedDelinquencyAction,
            final DataValidatorBuilder dataValidator) {
        if (parsedDelinquencyAction.getStartDate().equals(parsedDelinquencyAction.getEndDate())) {
            failGeneralValidation(dataValidator, "pause.period.must.be.at.least.one.day",
                    "Delinquency pause period must be at least one day");
        }
    }

    private void validatePauseStartDateNotBeforeDisbursementDate(final LoanDelinquencyAction parsedDelinquencyAction,
            final LocalDate firstDisbursalDate, final DataValidatorBuilder dataValidator) {
        if (firstDisbursalDate == null) {
            return;
        }
        if (firstDisbursalDate.isAfter(parsedDelinquencyAction.getStartDate())) {
            failParameterValidation(dataValidator, START_DATE, "before.disbursement",
                    "Start date of pause period must be after first disbursal date");
        }
    }

    private void validateLoanIsActive(final Loan loan, final DataValidatorBuilder dataValidator) {
        if (!loan.getStatus().isActive()) {
            failGeneralValidation(dataValidator, "invalid.loan.state", "Delinquency actions can be created only for active loans.");
        }
    }

    private void validatePauseShallNotOverlap(final LoanDelinquencyAction parsedDelinquencyAction,
            final List<LoanDelinquencyActionData> delinquencyActions, final DataValidatorBuilder dataValidator) {
        if (delinquencyActions.stream().filter(lda -> lda.getAction().equals(DelinquencyAction.PAUSE))
                .anyMatch(lda -> isOverlapping(parsedDelinquencyAction, lda))) {
            failGeneralValidation(dataValidator, "overlapping", "Delinquency pause period cannot overlap with another pause period");
        }
    }

    /**
     * <pre>
     *  we have an overlap when
     *  (parsed.endDate &gt; existing.startDate AND parsed.endDate &lt; existing.endDate)
     *
     *  existing       |------------|
     *  parsed               |----------------|
     *
     *  we also  have an overlap when
     *  (parsed.startDate &gt; existing.startDate AND parsed.startDate &lt; existing.endDate)
     *
     *  existing            |------------|
     *  parsed    |----------------|
     *
     *  There is no overlap like when they are right after each other:
     *
     *  existing  |------------|
     *  parsed                 |----------------|
     *
     *  or
     *
     *  existing               |------------|
     *  parsed   |-------------|
     * </pre>
     *
     * @param parsed
     * @param existing
     * @return
     */
    private boolean isOverlapping(final LoanDelinquencyAction parsed, final LoanDelinquencyActionData existing) {
        return (parsed.getEndDate().isAfter(existing.getStartDate()) && parsed.getEndDate().isBefore(existing.getEndDate()))
                || (parsed.getStartDate().isAfter(existing.getStartDate()) && parsed.getStartDate().isBefore(existing.getEndDate()))
                || (parsed.getStartDate().isEqual(existing.getStartDate()) && parsed.getEndDate().isEqual(existing.getEndDate()));
    }

    @NonNull
    private LoanDelinquencyAction parseCommand(@NonNull final JsonCommand command, final DataValidatorBuilder dataValidator) {
        final LoanDelinquencyAction parsedDelinquencyAction = new LoanDelinquencyAction();
        parsedDelinquencyAction.setAction(extractAction(command.parsedJson(), dataValidator));
        parsedDelinquencyAction.setStartDate(extractStartDate(command.parsedJson()));
        parsedDelinquencyAction.setEndDate(extractEndDate(command.parsedJson()));
        return parsedDelinquencyAction;
    }

    private DelinquencyAction extractAction(final JsonElement json, final DataValidatorBuilder dataValidator) {
        final String actionString = jsonHelper.extractStringNamed(DelinquencyActionParameters.ACTION, json);
        if (StringUtils.isEmpty(actionString)) {
            failParameterValidation(dataValidator, ACTION, "cannot.be.blank", "Delinquency Action must not be null or empty");
            return null;
        }
        if ("pause".equalsIgnoreCase(actionString)) {
            return DelinquencyAction.PAUSE;
        } else if ("resume".equalsIgnoreCase(actionString)) {
            return DelinquencyAction.RESUME;
        }
        failParameterValidation(dataValidator, ACTION, "invalid.action", "Invalid Delinquency Action: " + actionString);
        return null;
    }

    private LocalDate extractStartDate(final JsonElement json) {
        final String dateFormat = jsonHelper.extractStringNamed(DelinquencyActionParameters.DATE_FORMAT, json);
        final String locale = jsonHelper.extractStringNamed(DelinquencyActionParameters.LOCALE, json);
        return jsonHelper.extractLocalDateNamed(START_DATE, json, dateFormat, JsonParserHelper.localeFromString(locale));
    }

    private LocalDate extractEndDate(final JsonElement json) {
        final String dateFormat = jsonHelper.extractStringNamed(DelinquencyActionParameters.DATE_FORMAT, json);
        final String locale = jsonHelper.extractStringNamed(DelinquencyActionParameters.LOCALE, json);
        return jsonHelper.extractLocalDateNamed(DelinquencyActionParameters.END_DATE, json, dateFormat,
                JsonParserHelper.localeFromString(locale));
    }

    private DataValidatorBuilder createValidator() {
        return new DataValidatorBuilder(new ArrayList<>()).resource(VALIDATION_RESOURCE);
    }

    private void failParameterValidation(final DataValidatorBuilder dataValidator, final String parameter, final String errorCode,
            final String message) {
        dataValidator.getDataValidationErrors().add(ApiParameterError
                .parameterError("validation.msg." + VALIDATION_RESOURCE + "." + parameter + "." + errorCode, message, parameter));
    }

    private void failGeneralValidation(final DataValidatorBuilder dataValidator, final String errorCode, final String message) {
        dataValidator.getDataValidationErrors()
                .add(ApiParameterError.generalError("validation.msg." + VALIDATION_RESOURCE + "." + errorCode, message));
    }

}
