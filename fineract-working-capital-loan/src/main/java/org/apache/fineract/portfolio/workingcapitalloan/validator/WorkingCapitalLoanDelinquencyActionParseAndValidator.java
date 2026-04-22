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
import java.math.BigDecimal;
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
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.validator.ParseAndValidator;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyFrequencyType;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyMinimumPaymentType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleRepository;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class WorkingCapitalLoanDelinquencyActionParseAndValidator extends ParseAndValidator {

    private static final String MINIMUM_PAYMENT = "minimumPayment";
    private static final String MINIMUM_PAYMENT_TYPE = "minimumPaymentType";
    private static final String FREQUENCY = "frequency";
    private static final String FREQUENCY_TYPE = "frequencyType";

    private final FromJsonHelper jsonHelper;
    private final WorkingCapitalLoanDelinquencyRangeScheduleRepository rangeScheduleRepository;

    public WorkingCapitalLoanDelinquencyAction validateAndParse(final JsonCommand command, final WorkingCapitalLoan workingCapitalLoan,
            final List<WorkingCapitalLoanDelinquencyAction> existing) {
        final WorkingCapitalLoanDelinquencyAction parsedAction = parseCommand(command);
        validateLoanIsActive(workingCapitalLoan);

        if (DelinquencyAction.PAUSE.equals(parsedAction.getAction())) {
            validatePause(parsedAction, workingCapitalLoan, existing);
        } else if (DelinquencyAction.RESCHEDULE.equals(parsedAction.getAction())) {
            validateReschedule(parsedAction, workingCapitalLoan);
        }

        return parsedAction;
    }

    private void validatePause(final WorkingCapitalLoanDelinquencyAction action, final WorkingCapitalLoan workingCapitalLoan,
            final List<WorkingCapitalLoanDelinquencyAction> existing) {
        validateBothDatesProvided(action);
        validateStartBeforeEnd(action);
        validateNotBeforeDisbursement(action, workingCapitalLoan);
        validateNotInEvaluatedPeriod(action, workingCapitalLoan);
        validateNoOverlap(action, existing);
    }

    private void validateReschedule(final WorkingCapitalLoanDelinquencyAction action, final WorkingCapitalLoan workingCapitalLoan) {
        validateLoanIsDisbursed(workingCapitalLoan);
        validateScheduleExists(workingCapitalLoan);

        final boolean hasPaymentGroup = action.getMinimumPayment() != null || action.getMinimumPaymentType() != null;
        final boolean hasFrequencyGroup = action.getFrequency() != null || action.getFrequencyType() != null;

        if (!hasPaymentGroup && !hasFrequencyGroup) {
            raiseValidationError("wc-loan-delinquency-action-reschedule-no-change",
                    "At least one of payment (minimumPayment + minimumPaymentType) or frequency (frequency + frequencyType) group must be provided");
        }
        if (hasPaymentGroup) {
            validateMinimumPaymentGroupProvided(action);
        }
        if (hasFrequencyGroup) {
            validateFrequencyGroupProvided(action);
        }
    }

    private WorkingCapitalLoanDelinquencyAction parseCommand(final JsonCommand command) {
        final JsonElement json = command.parsedJson();
        final WorkingCapitalLoanDelinquencyAction action = new WorkingCapitalLoanDelinquencyAction();
        action.setAction(extractAction(json));

        if (DelinquencyAction.PAUSE.equals(action.getAction())) {
            action.setStartDate(extractDate(json, START_DATE));
            action.setEndDate(extractDate(json, END_DATE));
        } else if (DelinquencyAction.RESCHEDULE.equals(action.getAction())) {
            action.setStartDate(DateUtils.getBusinessLocalDate());
            action.setMinimumPayment(extractBigDecimal(json, MINIMUM_PAYMENT));
            action.setMinimumPaymentType(extractMinimumPaymentType(json));
            action.setFrequency(extractInteger(json, FREQUENCY));
            action.setFrequencyType(extractFrequencyType(json));
        }

        return action;
    }

    private DelinquencyAction extractAction(final JsonElement json) {
        final String actionString = jsonHelper.extractStringNamed(ACTION, json);
        if (StringUtils.isEmpty(actionString)) {
            raiseValidationError("wc-loan-delinquency-action-missing-action", "Delinquency Action must not be null or empty", ACTION);
        }
        if ("pause".equalsIgnoreCase(actionString)) {
            return DelinquencyAction.PAUSE;
        } else if ("reschedule".equalsIgnoreCase(actionString)) {
            return DelinquencyAction.RESCHEDULE;
        }
        throw new PlatformApiDataValidationException(List.of(ApiParameterError.parameterError("wc-loan-delinquency-action-invalid-action",
                "Invalid Delinquency Action: " + actionString + ". Supported actions: pause, reschedule", ACTION)));
    }

    private LocalDate extractDate(final JsonElement json, final String paramName) {
        final String dateFormat = jsonHelper.extractStringNamed(DATE_FORMAT, json);
        final String locale = jsonHelper.extractStringNamed(LOCALE, json);
        return jsonHelper.extractLocalDateNamed(paramName, json, dateFormat, JsonParserHelper.localeFromString(locale));
    }

    private BigDecimal extractBigDecimal(final JsonElement json, final String paramName) {
        if (json.getAsJsonObject().has(paramName)) {
            return jsonHelper.extractBigDecimalWithLocaleNamed(paramName, json);
        }
        return null;
    }

    private Integer extractInteger(final JsonElement json, final String paramName) {
        if (json.getAsJsonObject().has(paramName)) {
            return jsonHelper.extractIntegerWithLocaleNamed(paramName, json);
        }
        return null;
    }

    private DelinquencyMinimumPaymentType extractMinimumPaymentType(final JsonElement json) {
        final String value = jsonHelper.extractStringNamed(MINIMUM_PAYMENT_TYPE, json);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            return DelinquencyMinimumPaymentType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new PlatformApiDataValidationException(
                    List.of(ApiParameterError.parameterError("wc-loan-delinquency-action-invalid-minimum-payment-type",
                            "Invalid minimum payment type: " + value + ". Supported: PERCENTAGE, FLAT", MINIMUM_PAYMENT_TYPE)),
                    e);
        }
    }

    private DelinquencyFrequencyType extractFrequencyType(final JsonElement json) {
        final String value = jsonHelper.extractStringNamed(FREQUENCY_TYPE, json);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            return DelinquencyFrequencyType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new PlatformApiDataValidationException(
                    List.of(ApiParameterError.parameterError("wc-loan-delinquency-action-invalid-frequency-type",
                            "Invalid frequency type: " + value + ". Supported: DAYS, WEEKS, MONTHS, YEARS", FREQUENCY_TYPE)),
                    e);
        }
    }

    private void validateLoanIsActive(final WorkingCapitalLoan workingCapitalLoan) {
        if (!workingCapitalLoan.getLoanStatus().isActive()) {
            raiseValidationError("wc-loan-delinquency-action-invalid-loan-state",
                    "Delinquency actions can be created only for active Working Capital loans.");
        }
    }

    private void validateLoanIsDisbursed(final WorkingCapitalLoan workingCapitalLoan) {
        final boolean isDisbursed = workingCapitalLoan.getDisbursementDetails().stream()
                .map(WorkingCapitalLoanDisbursementDetails::getActualDisbursementDate).anyMatch(Objects::nonNull);
        if (!isDisbursed) {
            raiseValidationError("wc-loan-delinquency-action-loan-not-disbursed", "Reschedule action requires the loan to be disbursed.");
        }
    }

    private void validateScheduleExists(final WorkingCapitalLoan workingCapitalLoan) {
        final List<WorkingCapitalLoanDelinquencyRangeSchedule> periods = rangeScheduleRepository
                .findByLoanIdOrderByPeriodNumberAsc(workingCapitalLoan.getId());
        if (periods.isEmpty()) {
            raiseValidationError("wc-loan-delinquency-action-no-schedule",
                    "Reschedule action requires an existing delinquency range schedule.");
        }
    }

    private void validateMinimumPaymentGroupProvided(final WorkingCapitalLoanDelinquencyAction action) {
        if (action.getMinimumPayment() == null || action.getMinimumPayment().compareTo(BigDecimal.ZERO) <= 0) {
            raiseValidationError("wc-loan-delinquency-action-invalid-minimum-payment",
                    "The parameter `minimumPayment` must be greater than 0", MINIMUM_PAYMENT);
        }
        if (action.getMinimumPaymentType() == null) {
            raiseValidationError("wc-loan-delinquency-action-missing-minimum-payment-type",
                    "The parameter `minimumPaymentType` is mandatory when `minimumPayment` is provided", MINIMUM_PAYMENT_TYPE);
        }
    }

    private void validateFrequencyGroupProvided(final WorkingCapitalLoanDelinquencyAction action) {
        if (action.getFrequency() == null || action.getFrequency() <= 0) {
            raiseValidationError("wc-loan-delinquency-action-invalid-frequency", "The parameter `frequency` must be greater than 0",
                    FREQUENCY);
        }
        if (action.getFrequencyType() == null) {
            raiseValidationError("wc-loan-delinquency-action-missing-frequency-type",
                    "The parameter `frequencyType` is mandatory when `frequency` is provided", FREQUENCY_TYPE);
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
        final LocalDate firstDisbursementDate = workingCapitalLoan.getDisbursementDetails().stream()
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
        final List<WorkingCapitalLoanDelinquencyRangeSchedule> periods = rangeScheduleRepository
                .findByLoanIdOrderByPeriodNumberAsc(workingCapitalLoan.getId());
        final boolean startsInEvaluatedPeriod = periods.stream().filter(p -> p.getMinPaymentCriteriaMet() != null)
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
        final boolean overlaps = existing.stream().filter(e -> DelinquencyAction.PAUSE.equals(e.getAction()))
                .anyMatch(e -> isOverlapping(parsed, e));
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
