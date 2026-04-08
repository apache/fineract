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
package org.apache.fineract.portfolio.workingcapitalloan.validator;

import static org.apache.fineract.portfolio.delinquency.validator.DelinquencyActionParameters.ACTION;
import static org.apache.fineract.portfolio.delinquency.validator.DelinquencyActionParameters.DATE_FORMAT;
import static org.apache.fineract.portfolio.delinquency.validator.DelinquencyActionParameters.END_DATE;
import static org.apache.fineract.portfolio.delinquency.validator.DelinquencyActionParameters.LOCALE;
import static org.apache.fineract.portfolio.delinquency.validator.DelinquencyActionParameters.START_DATE;

import com.google.gson.JsonElement;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.validator.ParseAndValidator;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleRepository;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class WorkingCapitalLoanDelinquencyActionParseAndValidator extends ParseAndValidator {

    private final FromJsonHelper jsonHelper;
    private final WorkingCapitalLoanDelinquencyRangeScheduleRepository rangeScheduleRepository;

    public WorkingCapitalLoanDelinquencyAction validateAndParse(final JsonCommand command, final WorkingCapitalLoan workingCapitalLoan,
            final List<WorkingCapitalLoanDelinquencyAction> existing) {
        WorkingCapitalLoanDelinquencyAction parsedAction = parseCommand(command);
        validateLoanIsActive(workingCapitalLoan);
        validateBothDatesProvided(parsedAction);
        validateStartBeforeEnd(parsedAction);
        validateNotBeforeDisbursement(parsedAction, workingCapitalLoan);
        validateNotInEvaluatedPeriod(parsedAction, workingCapitalLoan);
        validateNoOverlap(parsedAction, existing);
        return parsedAction;
    }

    private WorkingCapitalLoanDelinquencyAction parseCommand(final JsonCommand command) {
        JsonElement json = command.parsedJson();
        WorkingCapitalLoanDelinquencyAction action = new WorkingCapitalLoanDelinquencyAction();
        action.setAction(extractAction(json));
        action.setStartDate(extractDate(json, START_DATE));
        action.setEndDate(extractDate(json, END_DATE));
        return action;
    }

    private DelinquencyAction extractAction(final JsonElement json) {
        String actionString = jsonHelper.extractStringNamed(ACTION, json);
        if (StringUtils.isEmpty(actionString)) {
            raiseValidationError("wc-loan-delinquency-action-missing-action", "Delinquency Action must not be null or empty", ACTION);
        }
        if (!"pause".equalsIgnoreCase(actionString)) {
            throw new PlatformApiDataValidationException(
                    List.of(ApiParameterError.parameterError("wc-loan-delinquency-action-invalid-action",
                            "Only PAUSE action is supported. Invalid action: " + actionString, ACTION)));
        }
        return DelinquencyAction.PAUSE;
    }

    private LocalDate extractDate(final JsonElement json, final String paramName) {
        String dateFormat = jsonHelper.extractStringNamed(DATE_FORMAT, json);
        String locale = jsonHelper.extractStringNamed(LOCALE, json);
        return jsonHelper.extractLocalDateNamed(paramName, json, dateFormat, JsonParserHelper.localeFromString(locale));
    }

    private void validateLoanIsActive(final WorkingCapitalLoan workingCapitalLoan) {
        if (!workingCapitalLoan.getLoanStatus().isActive()) {
            raiseValidationError("wc-loan-delinquency-action-invalid-loan-state",
                    "Delinquency actions can be created only for active Working Capital loans.");
        }
    }

    private void validateBothDatesProvided(final WorkingCapitalLoanDelinquencyAction action) {
        if (action.getStartDate() == null) {
            raiseValidationError("wc-loan-delinquency-action-pause-startDate-cannot-be-blank", "The parameter `startDate` is mandatory",
                    START_DATE);
        }
        if (action.getEndDate() == null) {
            raiseValidationError("wc-loan-delinquency-action-pause-endDate-cannot-be-blank", "The parameter `endDate` is mandatory",
                    END_DATE);
        }
    }

    private void validateStartBeforeEnd(final WorkingCapitalLoanDelinquencyAction action) {
        if (action.getStartDate() != null && action.getEndDate() != null && !action.getStartDate().isBefore(action.getEndDate())) {
            raiseValidationError("wc-loan-delinquency-action-invalid-start-date-and-end-date",
                    "Delinquency pause period must be at least one day");
        }
    }

    private void validateNotBeforeDisbursement(final WorkingCapitalLoanDelinquencyAction action,
            final WorkingCapitalLoan workingCapitalLoan) {
        if (action.getStartDate() == null) {
            return;
        }
        LocalDate firstDisbursementDate = workingCapitalLoan.getDisbursementDetails().stream()
                .map(WorkingCapitalLoanDisbursementDetails::getActualDisbursementDate).filter(Objects::nonNull).findFirst().orElse(null);
        if (firstDisbursementDate != null && firstDisbursementDate.isAfter(action.getStartDate())) {
            raiseValidationError("wc-loan-delinquency-action-invalid-start-date",
                    "Start date of pause period must be after first disbursal date", START_DATE);
        }
    }

    private void validateNotInEvaluatedPeriod(final WorkingCapitalLoanDelinquencyAction action,
            final WorkingCapitalLoan workingCapitalLoan) {
        if (action.getStartDate() == null) {
            return;
        }
        List<WorkingCapitalLoanDelinquencyRangeSchedule> periods = rangeScheduleRepository
                .findByLoanIdOrderByPeriodNumberAsc(workingCapitalLoan.getId());
        boolean startsInEvaluatedPeriod = periods.stream().filter(p -> p.getMinPaymentCriteriaMet() != null)
                .anyMatch(p -> !action.getStartDate().isAfter(p.getToDate()));
        if (startsInEvaluatedPeriod) {
            raiseValidationError("wc-loan-delinquency-action-pause-in-evaluated-period",
                    "Pause start date cannot fall within or before an already evaluated delinquency range period", START_DATE);
        }
    }

    private void validateNoOverlap(final WorkingCapitalLoanDelinquencyAction parsed,
            final List<WorkingCapitalLoanDelinquencyAction> existing) {
        if (parsed.getStartDate() == null || parsed.getEndDate() == null) {
            return;
        }
        boolean overlaps = existing.stream().anyMatch(e -> isOverlapping(parsed, e));
        if (overlaps) {
            raiseValidationError("wc-loan-delinquency-action-overlapping",
                    "Delinquency pause period cannot overlap with another pause period");
        }
    }

    private boolean isOverlapping(final WorkingCapitalLoanDelinquencyAction parsed, final WorkingCapitalLoanDelinquencyAction existing) {
        return (parsed.getEndDate().isAfter(existing.getStartDate()) && parsed.getEndDate().isBefore(existing.getEndDate()))
                || (parsed.getStartDate().isAfter(existing.getStartDate()) && parsed.getStartDate().isBefore(existing.getEndDate()))
                || (parsed.getStartDate().isEqual(existing.getStartDate()) && parsed.getEndDate().isEqual(existing.getEndDate()));
    }

    private void raiseValidationError(final String globalisationMessageCode, final String msg) throws PlatformApiDataValidationException {
        throw new PlatformApiDataValidationException(List.of(ApiParameterError.generalError(globalisationMessageCode, msg)));
    }

    private void raiseValidationError(final String globalisationMessageCode, final String msg, final String fieldName)
            throws PlatformApiDataValidationException {
        throw new PlatformApiDataValidationException(List.of(ApiParameterError.parameterError(globalisationMessageCode, msg, fieldName)));
    }

}
