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

import static org.apache.fineract.portfolio.workingcapitalloan.validator.WorkingCapitalLoanBreachActionParameters.ACTION;
import static org.apache.fineract.portfolio.workingcapitalloan.validator.WorkingCapitalLoanBreachActionParameters.DATE_FORMAT;
import static org.apache.fineract.portfolio.workingcapitalloan.validator.WorkingCapitalLoanBreachActionParameters.END_DATE;
import static org.apache.fineract.portfolio.workingcapitalloan.validator.WorkingCapitalLoanBreachActionParameters.FREQUENCY;
import static org.apache.fineract.portfolio.workingcapitalloan.validator.WorkingCapitalLoanBreachActionParameters.FREQUENCY_TYPE;
import static org.apache.fineract.portfolio.workingcapitalloan.validator.WorkingCapitalLoanBreachActionParameters.LOCALE;
import static org.apache.fineract.portfolio.workingcapitalloan.validator.WorkingCapitalLoanBreachActionParameters.MINIMUM_PAYMENT;
import static org.apache.fineract.portfolio.workingcapitalloan.validator.WorkingCapitalLoanBreachActionParameters.MINIMUM_PAYMENT_TYPE;
import static org.apache.fineract.portfolio.workingcapitalloan.validator.WorkingCapitalLoanBreachActionParameters.RESTART_PERIOD_FROM_RESET_DATE;
import static org.apache.fineract.portfolio.workingcapitalloan.validator.WorkingCapitalLoanBreachActionParameters.START_DATE;

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.validator.ParseAndValidator;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachPauseUtils;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachScheduleEvaluationUtils;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPausePeriodUtils;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanActiveBreachResetResolver;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalBreachAmountCalculationType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanBreachStartType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class WorkingCapitalLoanBreachActionParseAndValidator extends ParseAndValidator {

    private static final String VALIDATION_RESOURCE = "workingCapitalLoanBreachAction";
    private static final String PAUSE_ACTION = "pause";
    private static final String RESCHEDULE_ACTION = "reschedule";
    private static final String RESUME_ACTION = "resume";
    private static final String RESET_ACTION = "reset";
    private static final String UNDO_RESET_ACTION = "undo_reset";
    private static final String DISABLE_ACTION = "disable";
    private static final String ENABLE_ACTION = "enable";

    private final FromJsonHelper jsonHelper;
    private final WorkingCapitalLoanBreachScheduleRepository breachScheduleRepository;
    private final WorkingCapitalLoanActiveBreachResetResolver activeBreachResetResolver;
    private final WorkingCapitalLoanBreachActionRepository breachActionRepository;

    public WorkingCapitalLoanBreachAction validateAndParse(final JsonCommand command, final WorkingCapitalLoan workingCapitalLoan,
            final List<WorkingCapitalLoanBreachAction> existing) {
        final DataValidatorBuilder dataValidator = new DataValidatorBuilder(new ArrayList<>()).resource(VALIDATION_RESOURCE);
        final JsonElement json = command.parsedJson();

        final String actionString = jsonHelper.extractStringNamed(ACTION, json);
        dataValidator.reset().parameter(ACTION).value(actionString).notBlank();
        if (StringUtils.isNotBlank(actionString)) {
            dataValidator.reset().parameter(ACTION).value(actionString).isOneOfTheseStringValues(PAUSE_ACTION, RESCHEDULE_ACTION,
                    RESUME_ACTION, RESET_ACTION, UNDO_RESET_ACTION, DISABLE_ACTION, ENABLE_ACTION);
        }
        throwExceptionIfValidationWarningsExist(dataValidator);

        validateLoanIsActive(dataValidator, workingCapitalLoan);
        validateBreachConfigurationExists(dataValidator, workingCapitalLoan);
        if (!isDisableStateChange(actionString)) {
            validateBreachNotDisabled(dataValidator, workingCapitalLoan.getId());
        }

        if (RESCHEDULE_ACTION.equalsIgnoreCase(actionString)) {
            return parseAndValidateReschedule(json, workingCapitalLoan, existing, dataValidator);
        }
        if (RESUME_ACTION.equalsIgnoreCase(actionString)) {
            return parseAndValidateResume(json, workingCapitalLoan, existing, dataValidator);
        }
        if (RESET_ACTION.equalsIgnoreCase(actionString)) {
            return parseAndValidateReset(json, workingCapitalLoan, dataValidator);
        }
        if (UNDO_RESET_ACTION.equalsIgnoreCase(actionString)) {
            return parseAndValidateUndoReset(workingCapitalLoan, dataValidator);
        }
        if (isDisableStateChange(actionString)) {
            return parseAndValidateDisableOrEnable(json, workingCapitalLoan, actionString, dataValidator);
        }
        return parseAndValidatePause(json, workingCapitalLoan, existing, dataValidator);
    }

    private boolean isDisableStateChange(final String actionString) {
        return DISABLE_ACTION.equalsIgnoreCase(actionString) || ENABLE_ACTION.equalsIgnoreCase(actionString);
    }

    private WorkingCapitalLoanBreachAction parseAndValidatePause(final JsonElement json, final WorkingCapitalLoan workingCapitalLoan,
            final List<WorkingCapitalLoanBreachAction> existing, final DataValidatorBuilder dataValidator) {
        final LocalDate startDate = extractDate(json, START_DATE);
        dataValidator.reset().parameter(START_DATE).value(startDate).notNull();

        final LocalDate endDate = extractDate(json, END_DATE);
        dataValidator.reset().parameter(END_DATE).value(endDate).notNull();

        validateStartBeforeEnd(dataValidator, startDate, endDate);
        validateNotBeforeScheduleStart(dataValidator, startDate, workingCapitalLoan);
        validateNoOverlap(dataValidator, startDate, endDate, existing);

        throwExceptionIfValidationWarningsExist(dataValidator);

        final WorkingCapitalLoanBreachAction action = new WorkingCapitalLoanBreachAction();
        action.setAction(WorkingCapitalLoanBreachActionType.PAUSE);
        action.setStartDate(startDate);
        action.setEndDate(endDate);
        action.setWorkingCapitalLoan(workingCapitalLoan);
        return action;
    }

    private WorkingCapitalLoanBreachAction parseAndValidateReschedule(final JsonElement json, final WorkingCapitalLoan workingCapitalLoan,
            final List<WorkingCapitalLoanBreachAction> existing, final DataValidatorBuilder dataValidator) {
        final WorkingCapitalLoanBreachAction action = new WorkingCapitalLoanBreachAction();
        action.setAction(WorkingCapitalLoanBreachActionType.RESCHEDULE);
        action.setStartDate(DateUtils.getBusinessLocalDate());
        action.setMinimumPayment(extractBigDecimal(json, MINIMUM_PAYMENT));
        action.setMinimumPaymentType(extractMinimumPaymentType(json, dataValidator));
        action.setFrequency(extractInteger(json, FREQUENCY));
        action.setFrequencyType(extractFrequencyType(json, dataValidator));
        action.setWorkingCapitalLoan(workingCapitalLoan);
        validateReschedule(action, workingCapitalLoan, existing, dataValidator);

        throwExceptionIfValidationWarningsExist(dataValidator);
        return action;
    }

    private WorkingCapitalLoanBreachAction parseAndValidateResume(final JsonElement json, WorkingCapitalLoan workingCapitalLoan,
            final List<WorkingCapitalLoanBreachAction> existing, final DataValidatorBuilder dataValidator) {
        final LocalDate resumeDate = extractDate(json, START_DATE);
        dataValidator.reset().parameter(START_DATE).value(resumeDate).notNull();

        final LocalDate endDate = extractDate(json, END_DATE);
        if (endDate != null) {
            failParameterValidation(dataValidator, END_DATE, "must.not.be.provided.for.resume",
                    "End date must not be provided for a resume action");
        }

        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        if (resumeDate != null && !resumeDate.isEqual(businessDate)) {
            failParameterValidation(dataValidator, START_DATE, "must.be.current.business.date",
                    "Start date of a resume action must be the current business date");
        }

        if (resumeDate != null && findActivePause(resumeDate, existing).isEmpty()) {
            failGeneralValidation(dataValidator, "resume.not.during.active.pause",
                    "Resume breach action can only be created during an active pause");
        }

        throwExceptionIfValidationWarningsExist(dataValidator);

        final WorkingCapitalLoanBreachAction action = new WorkingCapitalLoanBreachAction();
        action.setAction(WorkingCapitalLoanBreachActionType.RESUME);
        action.setStartDate(resumeDate);
        action.setWorkingCapitalLoan(workingCapitalLoan);
        return action;
    }

    private WorkingCapitalLoanBreachAction parseAndValidateReset(final JsonElement json, final WorkingCapitalLoan workingCapitalLoan,
            final DataValidatorBuilder dataValidator) {
        final LocalDate resetDate = DateUtils.getBusinessLocalDate();
        validateLoanIsDisbursed(workingCapitalLoan, dataValidator);
        final List<WorkingCapitalLoanBreachSchedule> periods = breachScheduleRepository
                .findByLoanIdOrderByPeriodNumberAsc(workingCapitalLoan.getId());
        if (periods.isEmpty()) {
            failGeneralValidation(dataValidator, "no.breach.schedule", "Breach action requires an existing breach schedule.");
        }
        if (WorkingCapitalLoanBreachScheduleEvaluationUtils.resolveEvaluationPeriod(periods, resetDate).isEmpty()) {
            failGeneralValidation(dataValidator, "no.breach.evaluation.period",
                    "There is no breach evaluation period covering the current business date.");
        }
        validateNoResetInCurrentPeriod(workingCapitalLoan, resetDate, periods, dataValidator);

        throwExceptionIfValidationWarningsExist(dataValidator);

        final WorkingCapitalLoanBreachAction action = new WorkingCapitalLoanBreachAction();
        action.setAction(WorkingCapitalLoanBreachActionType.RESET);
        action.setStartDate(resetDate);
        action.setRestartPeriodFromResetDate(extractBoolean(json, RESTART_PERIOD_FROM_RESET_DATE));
        action.setWorkingCapitalLoan(workingCapitalLoan);
        return action;
    }

    private WorkingCapitalLoanBreachAction parseAndValidateUndoReset(final WorkingCapitalLoan workingCapitalLoan,
            final DataValidatorBuilder dataValidator) {
        validateLoanIsDisbursed(workingCapitalLoan, dataValidator);
        validateScheduleExists(workingCapitalLoan, dataValidator);
        final boolean hasActiveReset = activeBreachResetResolver.hasActiveReset(workingCapitalLoan.getId());
        if (!hasActiveReset) {
            failGeneralValidation(dataValidator, "no.breach.reset.to.undo",
                    "There is no active breach reset to undo for this Working Capital loan.");
        }

        throwExceptionIfValidationWarningsExist(dataValidator);

        final WorkingCapitalLoanBreachAction action = new WorkingCapitalLoanBreachAction();
        action.setAction(WorkingCapitalLoanBreachActionType.UNDO_RESET);
        action.setStartDate(DateUtils.getBusinessLocalDate());
        action.setWorkingCapitalLoan(workingCapitalLoan);
        return action;
    }

    private WorkingCapitalLoanBreachAction parseAndValidateDisableOrEnable(final JsonElement json,
            final WorkingCapitalLoan workingCapitalLoan, final String actionString, final DataValidatorBuilder dataValidator) {
        final LocalDate startDate = extractDate(json, START_DATE);
        dataValidator.reset().parameter(START_DATE).value(startDate).notNull();

        final LocalDate endDate = extractDate(json, END_DATE);
        if (endDate != null) {
            failParameterValidation(dataValidator, END_DATE, "must.not.be.provided.for.disable.or.enable",
                    "End date must not be provided for a disable or enable action");
        }

        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        if (startDate != null && !startDate.isEqual(businessDate)) {
            failParameterValidation(dataValidator, START_DATE, "must.be.current.business.date",
                    "Start date of a disable or enable action must be the current business date");
        }

        final boolean isEnable = ENABLE_ACTION.equalsIgnoreCase(actionString);
        validateDisableState(dataValidator, workingCapitalLoan.getId(), isEnable);

        throwExceptionIfValidationWarningsExist(dataValidator);

        final WorkingCapitalLoanBreachAction action = new WorkingCapitalLoanBreachAction();
        action.setAction(isEnable ? WorkingCapitalLoanBreachActionType.ENABLE : WorkingCapitalLoanBreachActionType.DISABLE);
        action.setStartDate(startDate);
        action.setWorkingCapitalLoan(workingCapitalLoan);
        return action;
    }

    private void validateDisableState(final DataValidatorBuilder dataValidator, final Long loanId, final boolean isEnable) {
        final boolean alreadyDisabled = breachActionRepository.isBreachDisabledAsOf(loanId, DateUtils.getBusinessLocalDate());
        if (isEnable && !alreadyDisabled) {
            failGeneralValidation(dataValidator, "no.active.breach.disable.to.enable",
                    "There is no active breach disable to enable for this Working Capital loan.");
        } else if (!isEnable && alreadyDisabled) {
            failGeneralValidation(dataValidator, "breach.already.disabled",
                    "Breach evaluation is already disabled for this Working Capital loan. It must be enabled before disabling again.");
        }
    }

    private Optional<WorkingCapitalLoanBreachAction> findActivePause(final LocalDate resumeDate,
            final List<WorkingCapitalLoanBreachAction> existing) {
        if (resumeDate == null) {
            return Optional.empty();
        }
        return existing.stream().filter(action -> WorkingCapitalLoanBreachActionType.PAUSE.equals(action.getAction()))
                .filter(pause -> !isAlreadyResumed(pause, existing))
                .filter(pause -> !resumeDate.isBefore(pause.getStartDate()) && !resumeDate.isAfter(pause.getEndDate())).findFirst();
    }

    private boolean isAlreadyResumed(final WorkingCapitalLoanBreachAction pause, final List<WorkingCapitalLoanBreachAction> existing) {
        return existing.stream().filter(action -> WorkingCapitalLoanBreachActionType.RESUME.equals(action.getAction())).anyMatch(
                resume -> !pause.getStartDate().isAfter(resume.getStartDate()) && !resume.getStartDate().isAfter(pause.getEndDate()));
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

    private Boolean extractBoolean(final JsonElement json, final String paramName) {
        if (json.getAsJsonObject().has(paramName)) {
            return jsonHelper.extractBooleanNamed(paramName, json);
        }
        return false;
    }

    private WorkingCapitalBreachAmountCalculationType extractMinimumPaymentType(final JsonElement json,
            final DataValidatorBuilder dataValidator) {
        final String value = jsonHelper.extractStringNamed(MINIMUM_PAYMENT_TYPE, json);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            return WorkingCapitalBreachAmountCalculationType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            failParameterValidation(dataValidator, MINIMUM_PAYMENT_TYPE, "invalid.minimumPaymentType",
                    "Invalid minimum payment type: " + value + ". Supported: PERCENTAGE, FLAT");
            return null;
        }
    }

    private WorkingCapitalLoanPeriodFrequencyType extractFrequencyType(final JsonElement json, final DataValidatorBuilder dataValidator) {
        final String value = jsonHelper.extractStringNamed(FREQUENCY_TYPE, json);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            return WorkingCapitalLoanPeriodFrequencyType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            failParameterValidation(dataValidator, FREQUENCY_TYPE, "invalid.frequencyType",
                    "Invalid frequency type: " + value + ". Supported: DAYS, WEEKS, MONTHS, YEARS");
            return null;
        }
    }

    private void validateLoanIsActive(final DataValidatorBuilder dataValidator, final WorkingCapitalLoan workingCapitalLoan) {
        if (!workingCapitalLoan.getLoanStatus().isActive()) {
            failGeneralValidation(dataValidator, "loan.is.not.active",
                    "Breach actions can be created only for active Working Capital loans.");
        }
    }

    private void validateBreachConfigurationExists(final DataValidatorBuilder dataValidator, final WorkingCapitalLoan workingCapitalLoan) {
        final WorkingCapitalLoanProductRelatedDetails details = workingCapitalLoan.getLoanProductRelatedDetails();
        if (details == null || details.getBreach() == null) {
            failGeneralValidation(dataValidator, "no.breach.configuration",
                    "Breach actions require a breach configuration on the Working Capital loan.");
        }
    }

    private void validateBreachNotDisabled(final DataValidatorBuilder dataValidator, final Long loanId) {
        if (breachActionRepository.isBreachDisabledAsOf(loanId, DateUtils.getBusinessLocalDate())) {
            failGeneralValidation(dataValidator, "breach.is.disabled",
                    "Breach pause, resume, reschedule and reset actions are not allowed while breach evaluation is disabled.");
        }
    }

    private void validateStartBeforeEnd(final DataValidatorBuilder dataValidator, final LocalDate startDate, final LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            failParameterValidation(dataValidator, END_DATE, "must.be.on.or.after.startDate",
                    "End date of pause period must be on or after the start date");
        }
    }

    private void validateNotBeforeScheduleStart(final DataValidatorBuilder dataValidator, final LocalDate startDate,
            final WorkingCapitalLoan workingCapitalLoan) {
        breachScheduleRepository.findTopByLoanIdOrderByPeriodNumberAsc(workingCapitalLoan.getId())
                .map(WorkingCapitalLoanBreachSchedule::getFromDate).or(() -> resolveExpectedScheduleStart(workingCapitalLoan))
                .ifPresent(scheduleStartDate -> dataValidator.reset().parameter(START_DATE).value(startDate)
                        .validateDateAfterOrEqual(scheduleStartDate));
    }

    /**
     * Mirrors the anchor resolution of the breach schedule generation for loans whose schedule has not been generated
     * yet (e.g. a pause placed between disbursement and the first COB run).
     */
    private Optional<LocalDate> resolveExpectedScheduleStart(final WorkingCapitalLoan workingCapitalLoan) {
        final WorkingCapitalLoanProductRelatedDetails details = workingCapitalLoan.getLoanProductRelatedDetails();
        final WorkingCapitalLoanBreachStartType breachStartType = (details == null || details.getBreachStartType() == null)
                ? WorkingCapitalLoanBreachStartType.DISBURSEMENT
                : details.getBreachStartType();
        final Optional<LocalDate> anchorDate = WorkingCapitalLoanBreachStartType.LOAN_CREATION.equals(breachStartType)
                ? Optional.ofNullable(workingCapitalLoan.getSubmittedOnDate())
                : firstActualDisbursementDate(workingCapitalLoan);
        return anchorDate.map(anchor -> anchor.plusDays(getBreachGraceDays(workingCapitalLoan)));
    }

    private Optional<LocalDate> firstActualDisbursementDate(final WorkingCapitalLoan workingCapitalLoan) {
        return workingCapitalLoan.getDisbursementDetails().stream().map(WorkingCapitalLoanDisbursementDetails::getActualDisbursementDate)
                .filter(Objects::nonNull).min(LocalDate::compareTo);
    }

    private int getBreachGraceDays(final WorkingCapitalLoan workingCapitalLoan) {
        final WorkingCapitalLoanProductRelatedDetails details = workingCapitalLoan.getLoanProductRelatedDetails();
        return (details == null || details.getBreachGraceDays() == null) ? 0 : details.getBreachGraceDays();
    }

    private void validateNoOverlap(final DataValidatorBuilder dataValidator, final LocalDate startDate, final LocalDate endDate,
            final List<WorkingCapitalLoanBreachAction> existing) {
        if (startDate == null || endDate == null) {
            return;
        }
        final boolean overlaps = existing.stream().filter(action -> WorkingCapitalLoanBreachActionType.PAUSE.equals(action.getAction()))
                .anyMatch(pause -> WorkingCapitalLoanPausePeriodUtils.inclusivePausePeriodsOverlap(startDate, endDate, pause.getStartDate(),
                        WorkingCapitalLoanBreachPauseUtils.resolveEffectivePauseEnd(pause, existing)));
        if (overlaps) {
            failGeneralValidation(dataValidator, "overlapping.pause.periods",
                    "Breach pause period cannot overlap with another pause period");
        }
    }

    private void validateReschedule(final WorkingCapitalLoanBreachAction action, final WorkingCapitalLoan workingCapitalLoan,
            final List<WorkingCapitalLoanBreachAction> existing, final DataValidatorBuilder dataValidator) {
        validateLoanIsDisbursed(workingCapitalLoan, dataValidator);
        validateScheduleExists(workingCapitalLoan, dataValidator);

        final boolean hasPaymentGroup = action.getMinimumPayment() != null || action.getMinimumPaymentType() != null;
        final boolean hasFrequencyGroup = action.getFrequency() != null || action.getFrequencyType() != null;

        if (!hasPaymentGroup && !hasFrequencyGroup) {
            failGeneralValidation(dataValidator, "reschedule.no.change.parameters",
                    "At least one of payment (minimumPayment + minimumPaymentType) or frequency (frequency + frequencyType) group must be provided");
        }
        if (hasPaymentGroup) {
            validateMinimumPaymentGroupProvided(action, dataValidator);
        }
        if (hasFrequencyGroup) {
            validateFrequencyGroupProvided(action, dataValidator);
            if (action.getFrequency() != null && action.getFrequency() > 0 && action.getFrequencyType() != null) {
                validateFrequencyDoesNotEndBeforeBusinessDate(action, workingCapitalLoan, existing, dataValidator);
            }
        }
    }

    /**
     * Rejects a frequency change whose resulting period end date falls before the business date. The candidate end date
     * is derived exactly as the re-date derives it: from the current open period fromDate, extended by the pauses.
     */
    private void validateFrequencyDoesNotEndBeforeBusinessDate(final WorkingCapitalLoanBreachAction action,
            final WorkingCapitalLoan workingCapitalLoan, final List<WorkingCapitalLoanBreachAction> existing,
            final DataValidatorBuilder dataValidator) {
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        final Optional<LocalDate> candidateToDate = breachScheduleRepository.findCurrentOpenPeriod(workingCapitalLoan.getId(), businessDate)
                .map(currentPeriod -> WorkingCapitalLoanBreachScheduleEvaluationUtils.calculateRescheduledToDate(
                        currentPeriod.getFromDate(), action.getFrequency(), action.getFrequencyType(), existing));
        if (candidateToDate.filter(toDate -> toDate.isBefore(businessDate)).isPresent()) {
            failGeneralValidation(dataValidator, "reschedule.frequency.results.endDate.before.businessDate",
                    "Frequency change results a breach period endDate before current businessDate is not allowed");
        }
    }

    private void failParameterValidation(final DataValidatorBuilder dataValidator, final String parameter, final String errorCodeSuffix,
            final String defaultUserMessage) {
        dataValidator.getDataValidationErrors().add(ApiParameterError.parameterError(
                "validation.msg." + VALIDATION_RESOURCE + "." + parameter + "." + errorCodeSuffix, defaultUserMessage, parameter));
    }

    private void failGeneralValidation(final DataValidatorBuilder dataValidator, final String errorCodeSuffix,
            final String defaultUserMessage) {
        dataValidator.getDataValidationErrors()
                .add(ApiParameterError.generalError("validation.msg." + VALIDATION_RESOURCE + "." + errorCodeSuffix, defaultUserMessage));
    }

    private void validateLoanIsDisbursed(final WorkingCapitalLoan workingCapitalLoan, final DataValidatorBuilder dataValidator) {
        final boolean isDisbursed = workingCapitalLoan.getDisbursementDetails().stream()
                .map(WorkingCapitalLoanDisbursementDetails::getActualDisbursementDate).anyMatch(Objects::nonNull);
        if (!isDisbursed) {
            failGeneralValidation(dataValidator, "loan.not.disbursed", "Breach action requires the loan to be disbursed.");
        }
    }

    private void validateScheduleExists(final WorkingCapitalLoan workingCapitalLoan, final DataValidatorBuilder dataValidator) {
        if (!breachScheduleRepository.existsByLoanId(workingCapitalLoan.getId())) {
            failGeneralValidation(dataValidator, "no.breach.schedule", "Breach action requires an existing breach schedule.");
        }
    }

    private void validateMinimumPaymentGroupProvided(final WorkingCapitalLoanBreachAction action,
            final DataValidatorBuilder dataValidator) {
        if (action.getMinimumPayment() == null || action.getMinimumPayment().compareTo(BigDecimal.ZERO) <= 0) {
            failParameterValidation(dataValidator, MINIMUM_PAYMENT, "must.be.greater.than.zero",
                    "The parameter `minimumPayment` must be greater than zero");
        }
        if (action.getMinimumPaymentType() == null) {
            dataValidator.reset().parameter(MINIMUM_PAYMENT_TYPE).value(action.getMinimumPaymentType()).notNull();
        }
    }

    private void validateFrequencyGroupProvided(final WorkingCapitalLoanBreachAction action, final DataValidatorBuilder dataValidator) {
        if (action.getFrequency() == null || action.getFrequency() <= 0) {
            dataValidator.reset().parameter(FREQUENCY).value(action.getFrequency()).integerGreaterThanZero();
        }
        if (action.getFrequencyType() == null) {
            dataValidator.reset().parameter(FREQUENCY_TYPE).value(action.getFrequencyType()).notNull();
        }
    }

    private void validateNoResetInCurrentPeriod(final WorkingCapitalLoan workingCapitalLoan, final LocalDate actionDate,
            final List<WorkingCapitalLoanBreachSchedule> periods, final DataValidatorBuilder dataValidator) {
        if (workingCapitalLoan == null || workingCapitalLoan.getId() == null || actionDate == null) {
            return;
        }
        final Optional<WorkingCapitalLoanBreachSchedule> evaluationPeriod = WorkingCapitalLoanBreachScheduleEvaluationUtils
                .resolveEvaluationPeriod(periods, actionDate);
        if (evaluationPeriod.isEmpty()) {
            return;
        }
        final WorkingCapitalLoanBreachSchedule period = evaluationPeriod.get();
        final LocalDate fromDate = period.getFromDate();
        final LocalDate toDate = period.getToDate();
        final boolean resetExistsInPeriod = activeBreachResetResolver.existsActiveResetInPeriod(workingCapitalLoan.getId(), fromDate,
                toDate);
        if (resetExistsInPeriod) {
            failGeneralValidation(dataValidator, "reset.already.exists.in.current.period",
                    "A breach reset already exists in the current breach period.");
        }
    }
}
