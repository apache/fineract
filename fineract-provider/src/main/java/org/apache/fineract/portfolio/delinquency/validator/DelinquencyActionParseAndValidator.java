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
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.validator.ParseAndValidator;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyAction;
import org.apache.fineract.portfolio.delinquency.helper.DelinquencyEffectivePauseHelper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DelinquencyActionParseAndValidator extends ParseAndValidator {

    private final FromJsonHelper jsonHelper;
    private final DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper;

    public LoanDelinquencyAction validateAndParseUpdate(@NotNull final JsonCommand command, Loan loan,
            List<LoanDelinquencyAction> savedDelinquencyActions, LocalDate businessDate) {
        List<LoanDelinquencyActionData> effectiveDelinquencyList = delinquencyEffectivePauseHelper
                .calculateEffectiveDelinquencyList(savedDelinquencyActions);
        LoanDelinquencyAction parsedDelinquencyAction = parseCommand(command);
        validateLoanIsActive(loan);
        if (DelinquencyAction.PAUSE.equals(parsedDelinquencyAction.getAction())) {
            validateBothStartAndEndDatesAreProvided(parsedDelinquencyAction);
            validatePauseStartAndEndDate(parsedDelinquencyAction);
            validatePauseStartDateNotBeforeDisbursementDate(parsedDelinquencyAction, loan.getDisbursementDate());
            validatePauseShallNotOverlap(parsedDelinquencyAction, effectiveDelinquencyList);
        } else if (DelinquencyAction.RESUME.equals(parsedDelinquencyAction.getAction())) {
            validateResumeStartDate(parsedDelinquencyAction, businessDate);
            validateResumeNoEndDate(parsedDelinquencyAction);
            validateResumeDoesNotExist(parsedDelinquencyAction, savedDelinquencyActions);
            validateResumeShouldBeOnActivePause(parsedDelinquencyAction, effectiveDelinquencyList);
        }
        return parsedDelinquencyAction;
    }

    private void validateBothStartAndEndDatesAreProvided(LoanDelinquencyAction parsedDelinquencyAction) {
        if (parsedDelinquencyAction.getStartDate() == null) {
            raiseValidationError("loan-delinquency-action-pause-startDate-cannot-be-blank", "The parameter `startDate` is mandatory",
                    START_DATE);
        }
        if (parsedDelinquencyAction.getEndDate() == null) {
            raiseValidationError("loan-delinquency-action-pause-endDate-cannot-be-blank", "The parameter `endDate` is mandatory", END_DATE);
        }
    }

    private void validateResumeShouldBeOnActivePause(LoanDelinquencyAction parsedDelinquencyAction,
            List<LoanDelinquencyActionData> savedDelinquencyActions) {
        boolean match = savedDelinquencyActions.stream()
                .anyMatch(lda -> !DateUtils.isBefore(parsedDelinquencyAction.getStartDate(), lda.getStartDate())
                        && !DateUtils.isAfter(parsedDelinquencyAction.getStartDate(), lda.getEndDate()));
        if (!match) {
            raiseValidationError("loan-delinquency-action-resume-should-be-on-pause",
                    "Resume Delinquency Action can only be created during an active pause");
        }
    }

    private void validateResumeDoesNotExist(LoanDelinquencyAction parsedDelinquencyAction,
            List<LoanDelinquencyAction> savedDelinquencyActions) {
        boolean match = savedDelinquencyActions.stream() //
                .filter(action -> DelinquencyAction.RESUME.equals(action.getAction())) //
                .anyMatch(action -> parsedDelinquencyAction.getStartDate().isEqual(action.getStartDate()));
        if (match) {
            raiseValidationError("loan-delinquency-action-resume-should-be-unique",
                    "There is an existing Resume Delinquency Action on this date");
        }
    }

    private void validateResumeNoEndDate(LoanDelinquencyAction parsedDelinquencyAction) {
        if (parsedDelinquencyAction.getEndDate() != null) {
            raiseValidationError("loan-delinquency-action-resume-should-have-no-end-date",
                    "Resume Delinquency action can not have end date", END_DATE);
        }
    }

    private void validateResumeStartDate(LoanDelinquencyAction parsedDelinquencyAction, LocalDate businessDate) {
        if (parsedDelinquencyAction.getStartDate() == null) {
            raiseValidationError("loan-delinquency-action-resume-startDate-cannot-be-blank", "The parameter `startDate` is mandatory",
                    START_DATE);
        }
        if (!parsedDelinquencyAction.getStartDate().equals(businessDate)) {
            raiseValidationError("loan-delinquency-action-invalid-start-date",
                    "Start date of the Resume Delinquency action must be the current business date", START_DATE);
        }
    }

    private void validatePauseStartAndEndDate(LoanDelinquencyAction parsedDelinquencyAction) {
        if (parsedDelinquencyAction.getStartDate().equals(parsedDelinquencyAction.getEndDate())) {
            raiseValidationError("loan-delinquency-action-invalid-start-date-and-end-date",
                    "Delinquency pause period must be at least one day");
        }
    }

    private void validatePauseStartDateNotBeforeDisbursementDate(LoanDelinquencyAction parsedDelinquencyAction,
            LocalDate firstDisbursalDate) {
        if (firstDisbursalDate.isAfter(parsedDelinquencyAction.getStartDate())) {
            raiseValidationError("loan-delinquency-action-invalid-start-date",
                    "Start date of pause period must be after first disbursal date", START_DATE);
        }
    }

    private void validateLoanIsActive(Loan loan) {
        if (!loan.getStatus().isActive()) {
            raiseValidationError("loan-delinquency-action-invalid-loan-state", "Delinquency actions can be created only for active loans.");
        }
    }

    private void validatePauseShallNotOverlap(LoanDelinquencyAction parsedDelinquencyAction,
            List<LoanDelinquencyActionData> delinquencyActions) {
        if (delinquencyActions.stream().filter(lda -> lda.getAction().equals(DelinquencyAction.PAUSE))
                .anyMatch(lda -> isOverlapping(parsedDelinquencyAction, lda))) {
            raiseValidationError("loan-delinquency-action-overlapping",
                    "Delinquency pause period cannot overlap with another pause period");
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
    private boolean isOverlapping(LoanDelinquencyAction parsed, LoanDelinquencyActionData existing) {
        return (parsed.getEndDate().isAfter(existing.getStartDate()) && parsed.getEndDate().isBefore(existing.getEndDate()))
                || (parsed.getStartDate().isAfter(existing.getStartDate()) && parsed.getStartDate().isBefore(existing.getEndDate()))
                || (parsed.getStartDate().isEqual(existing.getStartDate()) && parsed.getEndDate().isEqual(existing.getEndDate()));
    }

    @org.jetbrains.annotations.NotNull
    private LoanDelinquencyAction parseCommand(@org.jetbrains.annotations.NotNull JsonCommand command) {
        LoanDelinquencyAction parsedDelinquencyAction = new LoanDelinquencyAction();
        parsedDelinquencyAction.setAction(extractAction(command.parsedJson()));
        parsedDelinquencyAction.setStartDate(extractStartDate(command.parsedJson()));
        parsedDelinquencyAction.setEndDate(extractEndDate(command.parsedJson()));
        return parsedDelinquencyAction;
    }

    private DelinquencyAction extractAction(JsonElement json) {
        String actionString = jsonHelper.extractStringNamed(DelinquencyActionParameters.ACTION, json);
        validateActionString(actionString);
        if ("pause".equalsIgnoreCase(actionString)) {
            return DelinquencyAction.PAUSE;
        } else if ("resume".equalsIgnoreCase(actionString)) {
            return DelinquencyAction.RESUME;
        } else {
            throw new PlatformApiDataValidationException(List.of(ApiParameterError.parameterError("loan-delinquency-action-invalid-action",
                    "Invalid Delinquency Action: " + actionString, ACTION)));
        }
    }

    private void validateActionString(String actionString) {
        if (StringUtils.isEmpty(actionString)) {
            raiseValidationError("loan-delinquency-action-missing-action", "Delinquency Action must not be null or empty", ACTION);
        }
    }

    private LocalDate extractStartDate(JsonElement json) {
        String dateFormat = jsonHelper.extractStringNamed(DelinquencyActionParameters.DATE_FORMAT, json);
        String locale = jsonHelper.extractStringNamed(DelinquencyActionParameters.LOCALE, json);
        return jsonHelper.extractLocalDateNamed(START_DATE, json, dateFormat, JsonParserHelper.localeFromString(locale));
    }

    private LocalDate extractEndDate(JsonElement json) {
        String dateFormat = jsonHelper.extractStringNamed(DelinquencyActionParameters.DATE_FORMAT, json);
        String locale = jsonHelper.extractStringNamed(DelinquencyActionParameters.LOCALE, json);
        return jsonHelper.extractLocalDateNamed(DelinquencyActionParameters.END_DATE, json, dateFormat,
                JsonParserHelper.localeFromString(locale));
    }

    private void raiseValidationError(String globalisationMessageCode, String msg) throws PlatformApiDataValidationException {
        throw new PlatformApiDataValidationException(List.of(ApiParameterError.generalError(globalisationMessageCode, msg)));
    }

    private void raiseValidationError(String globalisationMessageCode, String msg, String fieldName)
            throws PlatformApiDataValidationException {
        throw new PlatformApiDataValidationException(List.of(ApiParameterError.parameterError(globalisationMessageCode, msg, fieldName)));
    }

}
