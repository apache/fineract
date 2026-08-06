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
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.validator.ParseAndValidator;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachScheduleEvaluationUtils;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
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
        final DataValidatorBuilder dataValidator = new DataValidatorBuilder(new ArrayList<>()).resource("workingCapitalLoanBreachAction");
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
            return parseAndValidateReschedule(json, workingCapitalLoan, dataValidator);
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
            final DataValidatorBuilder dataValidator) {
        final WorkingCapitalLoanBreachAction action = new WorkingCapitalLoanBreachAction();
        action.setAction(WorkingCapitalLoanBreachActionType.RESCHEDULE);
        action.setStartDate(DateUtils.getBusinessLocalDate());
        action.setMinimumPayment(extractBigDecimal(json, MINIMUM_PAYMENT));
        action.setMinimumPaymentType(extractMinimumPaymentType(json, dataValidator));
        action.setFrequency(extractInteger(json, FREQUENCY));
        action.setFrequencyType(extractFrequencyType(json, dataValidator));
        action.setWorkingCapitalLoan(workingCapitalLoan);
        validateReschedule(action, workingCapitalLoan, dataValidator);

        throwExceptionIfValidationWarningsExist(dataValidator);
        return action;
    }

    private WorkingCapitalLoanBreachAction parseAndValidateResume(final JsonElement json, WorkingCapitalLoan workingCapitalLoan,
            final List<WorkingCapitalLoanBreachAction> existing, final DataValidatorBuilder dataValidator) {
        final LocalDate resumeDate = extractDate(json, START_DATE);
        dataValidator.reset().parameter(START_DATE).value(resumeDate).notNull();

        final LocalDate endDate = extractDate(json, END_DATE);
        if (endDate != null) {
            dataValidator.reset().parameter(END_DATE).value(endDate).failWithCode("must.not.be.provided.for.resume");
        }

        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        if (resumeDate != null && !resumeDate.isEqual(businessDate)) {
            dataValidator.reset().parameter(START_DATE).value(resumeDate).failWithCode("must.be.current.business.date");
        }

        if (resumeDate != null && findActivePause(resumeDate, existing).isEmpty()) {
            dataValidator.reset().failWithCodeNoParameterAddedToErrorCode("resume.not.during.active.pause");
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
            dataValidator.reset().failWithCodeNoParameterAddedToErrorCode("no.breach.schedule");
        }
        if (WorkingCapitalLoanBreachScheduleEvaluationUtils.resolveEvaluationPeriod(periods, resetDate).isEmpty()) {
            dataValidator.reset().failWithCodeNoParameterAddedToErrorCode("no.breach.evaluation.period");
        }
        validateNoResetInCurrentPeriod(workingCapitalLoan, resetDate, periods, dataValidator);

        throwExceptionIfValidationWarningsExist(dataValidator);

        final WorkingCapitalLoanBreachAction action = new WorkingCapitalLoanBreachAction();
        action.setAction(WorkingCapitalLoanBreachActionType.RESET);
        action.setStartDate(resetDate);
        action.setWorkingCapitalLoan(workingCapitalLoan);
        return action;
    }

    private WorkingCapitalLoanBreachAction parseAndValidateUndoReset(final WorkingCapitalLoan workingCapitalLoan,
            final DataValidatorBuilder dataValidator) {
        validateLoanIsDisbursed(workingCapitalLoan, dataValidator);
        validateScheduleExists(workingCapitalLoan, dataValidator);
        final boolean hasActiveReset = activeBreachResetResolver.hasActiveReset(workingCapitalLoan.getId());
        if (!hasActiveReset) {
            dataValidator.reset().failWithCodeNoParameterAddedToErrorCode("no.breach.reset.to.undo");
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
            dataValidator.reset().parameter(END_DATE).value(endDate).failWithCode("must.not.be.provided.for.disable.or.enable");
        }

        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        if (startDate != null && !startDate.isEqual(businessDate)) {
            dataValidator.reset().parameter(START_DATE).value(startDate).failWithCode("must.be.current.business.date");
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
            dataValidator.reset().failWithCodeNoParameterAddedToErrorCode("no.active.breach.disable.to.enable");
        } else if (!isEnable && alreadyDisabled) {
            dataValidator.reset().failWithCodeNoParameterAddedToErrorCode("breach.already.disabled");
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

    private WorkingCapitalBreachAmountCalculationType extractMinimumPaymentType(final JsonElement json,
            final DataValidatorBuilder dataValidator) {
        final String value = jsonHelper.extractStringNamed(MINIMUM_PAYMENT_TYPE, json);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            return WorkingCapitalBreachAmountCalculationType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            dataValidator.reset().parameter(MINIMUM_PAYMENT_TYPE).value(value).failWithCode("invalid.minimumPaymentType");
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
            dataValidator.reset().parameter(FREQUENCY_TYPE).value(value).failWithCode("invalid.frequencyType");
            return null;
        }
    }

    private void validateLoanIsActive(final DataValidatorBuilder dataValidator, final WorkingCapitalLoan workingCapitalLoan) {
        if (!workingCapitalLoan.getLoanStatus().isActive()) {
            dataValidator.reset().failWithCodeNoParameterAddedToErrorCode("loan.is.not.active");
        }
    }

    private void validateBreachConfigurationExists(final DataValidatorBuilder dataValidator, final WorkingCapitalLoan workingCapitalLoan) {
        final WorkingCapitalLoanProductRelatedDetails details = workingCapitalLoan.getLoanProductRelatedDetails();
        if (details == null || details.getBreach() == null) {
            dataValidator.reset().failWithCodeNoParameterAddedToErrorCode("no.breach.configuration");
        }
    }

    private void validateBreachNotDisabled(final DataValidatorBuilder dataValidator, final Long loanId) {
        if (breachActionRepository.isBreachDisabledAsOf(loanId, DateUtils.getBusinessLocalDate())) {
            dataValidator.reset().failWithCodeNoParameterAddedToErrorCode("breach.is.disabled");
        }
    }

    private void validateStartBeforeEnd(final DataValidatorBuilder dataValidator, final LocalDate startDate, final LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            dataValidator.reset().parameter(END_DATE).value(endDate).failWithCode("must.be.on.or.after.startDate");
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
                .anyMatch(pause -> isOverlapping(startDate, endDate, pause.getStartDate(), effectivePauseEnd(pause, existing)));
        if (overlaps) {
            dataValidator.reset().failWithCodeNoParameterAddedToErrorCode("overlapping.pause.periods");
        }
    }

    private LocalDate effectivePauseEnd(final WorkingCapitalLoanBreachAction pause, final List<WorkingCapitalLoanBreachAction> existing) {
        // A resumed pause effectively ends on the (inclusive) resume date, so a later pause may start the next day
        return existing.stream().filter(action -> WorkingCapitalLoanBreachActionType.RESUME.equals(action.getAction()))
                .map(WorkingCapitalLoanBreachAction::getStartDate)
                .filter(resumeDate -> !pause.getStartDate().isAfter(resumeDate) && !resumeDate.isAfter(pause.getEndDate()))
                .min(LocalDate::compareTo).orElse(pause.getEndDate());
    }

    private boolean isOverlapping(final LocalDate startDate, final LocalDate endDate, final LocalDate otherStart,
            final LocalDate otherEnd) {
        return !startDate.isAfter(otherEnd) && !otherStart.isAfter(endDate);
    }

    private void validateReschedule(final WorkingCapitalLoanBreachAction action, final WorkingCapitalLoan workingCapitalLoan,
            final DataValidatorBuilder dataValidator) {
        validateLoanIsDisbursed(workingCapitalLoan, dataValidator);
        validateScheduleExists(workingCapitalLoan, dataValidator);

        final boolean hasPaymentGroup = action.getMinimumPayment() != null || action.getMinimumPaymentType() != null;
        final boolean hasFrequencyGroup = action.getFrequency() != null || action.getFrequencyType() != null;

        if (!hasPaymentGroup && !hasFrequencyGroup) {
            dataValidator.reset().failWithCodeNoParameterAddedToErrorCode("reschedule.no.change.parameters");
        }
        if (hasPaymentGroup) {
            validateMinimumPaymentGroupProvided(action, dataValidator);
        }
        if (hasFrequencyGroup) {
            validateFrequencyGroupProvided(action, dataValidator);
        }
    }

    private void validateLoanIsDisbursed(final WorkingCapitalLoan workingCapitalLoan, final DataValidatorBuilder dataValidator) {
        final boolean isDisbursed = workingCapitalLoan.getDisbursementDetails().stream()
                .map(WorkingCapitalLoanDisbursementDetails::getActualDisbursementDate).anyMatch(Objects::nonNull);
        if (!isDisbursed) {
            dataValidator.reset().failWithCodeNoParameterAddedToErrorCode("loan.not.disbursed");
        }
    }

    private void validateScheduleExists(final WorkingCapitalLoan workingCapitalLoan, final DataValidatorBuilder dataValidator) {
        final List<WorkingCapitalLoanBreachSchedule> periods = breachScheduleRepository
                .findByLoanIdOrderByPeriodNumberAsc(workingCapitalLoan.getId());
        if (periods.isEmpty()) {
            dataValidator.reset().failWithCodeNoParameterAddedToErrorCode("no.breach.schedule");
        }
    }

    private void validateMinimumPaymentGroupProvided(final WorkingCapitalLoanBreachAction action,
            final DataValidatorBuilder dataValidator) {
        if (action.getMinimumPayment() == null || action.getMinimumPayment().compareTo(BigDecimal.ZERO) <= 0) {
            dataValidator.reset().parameter(MINIMUM_PAYMENT).value(action.getMinimumPayment()).failWithCode("must.be.greater.than.zero");
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
            dataValidator.reset().failWithCodeNoParameterAddedToErrorCode("reset.already.exists.in.current.period");
        }
    }
}
