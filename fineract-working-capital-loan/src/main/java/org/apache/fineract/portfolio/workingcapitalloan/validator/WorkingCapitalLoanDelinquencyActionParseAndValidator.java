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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
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
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyPauseUtils;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleRepository;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class WorkingCapitalLoanDelinquencyActionParseAndValidator extends ParseAndValidator {

    private static final String VALIDATION_RESOURCE = "workingCapitalLoanDelinquencyAction";
    private static final String MINIMUM_PAYMENT = "minimumPayment";
    private static final String MINIMUM_PAYMENT_TYPE = "minimumPaymentType";
    private static final String FREQUENCY = "frequency";
    private static final String FREQUENCY_TYPE = "frequencyType";
    private static final String START_NEW_PERIOD = "startNewPeriod";

    private final FromJsonHelper jsonHelper;
    private final WorkingCapitalLoanDelinquencyRangeScheduleRepository rangeScheduleRepository;
    private final WorkingCapitalLoanDelinquencyActionRepository actionRepository;

    public WorkingCapitalLoanDelinquencyAction validateAndParse(final JsonCommand command, final WorkingCapitalLoan workingCapitalLoan,
            final List<WorkingCapitalLoanDelinquencyAction> existing) {
        final DataValidatorBuilder dataValidator = createValidator();
        final WorkingCapitalLoanDelinquencyAction parsedAction = parseCommand(command, workingCapitalLoan, dataValidator);
        validateLoanIsActive(workingCapitalLoan, dataValidator);
        validateDelinquencyConfigurationExists(workingCapitalLoan, dataValidator);

        final DelinquencyAction action = parsedAction.getAction();
        if (DelinquencyAction.DISABLE.equals(action) || DelinquencyAction.ENABLE.equals(action)) {
            return parseAndValidateDisableOrEnable(command.parsedJson(), workingCapitalLoan, DelinquencyAction.ENABLE.equals(action),
                    dataValidator);
        }

        validateDelinquencyNotDisabled(workingCapitalLoan.getId(), dataValidator);
        if (DelinquencyAction.PAUSE.equals(action)) {
            validatePause(parsedAction, workingCapitalLoan, existing, dataValidator);
        } else if (DelinquencyAction.RESCHEDULE.equals(action)) {
            validateReschedule(parsedAction, workingCapitalLoan, dataValidator);
        } else if (DelinquencyAction.RESUME.equals(action)) {
            validateResume(parsedAction, existing, dataValidator);
        } else if (DelinquencyAction.RESET.equals(parsedAction.getAction())) {
            validateReset(parsedAction, dataValidator);
        } else if (DelinquencyAction.UNDO_RESET.equals(parsedAction.getAction())) {
            validateUndoReset(parsedAction, dataValidator);
        }

        throwExceptionIfValidationWarningsExist(dataValidator);
        return parsedAction;
    }

    /**
     * Validates a disable or enable action. Both are always applied as of the current business date (backdating is not
     * permitted) and must not carry an end date. A disable is rejected when one is already active; an enable is
     * rejected when there is no active disable. Each action is persisted as its own row (mirroring the breach action
     * management): a disable yields a new DISABLE action and an enable yields a new ENABLE action; the caller closes
     * the currently open disable window.
     */
    private WorkingCapitalLoanDelinquencyAction parseAndValidateDisableOrEnable(final JsonElement json,
            final WorkingCapitalLoan workingCapitalLoan, final boolean isEnable, final DataValidatorBuilder dataValidator) {
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

        validateDisableState(dataValidator, workingCapitalLoan.getId(), isEnable);
        throwExceptionIfValidationWarningsExist(dataValidator);

        final WorkingCapitalLoanDelinquencyAction action = new WorkingCapitalLoanDelinquencyAction();
        action.setAction(isEnable ? DelinquencyAction.ENABLE : DelinquencyAction.DISABLE);
        action.setStartDate(startDate);
        action.setWorkingCapitalLoan(workingCapitalLoan);
        return action;
    }

    private void validateDisableState(final DataValidatorBuilder dataValidator, final Long loanId, final boolean isEnable) {
        final boolean alreadyDisabled = actionRepository.isDelinquencyDisabledAsOf(loanId, DateUtils.getBusinessLocalDate());
        if (isEnable && !alreadyDisabled) {
            failGeneralValidation(dataValidator, "no.active.delinquency.disable.to.enable",
                    "There is no active delinquency disable to enable for this Working Capital loan.");
        } else if (!isEnable && alreadyDisabled) {
            failGeneralValidation(dataValidator, "delinquency.already.disabled",
                    "Delinquency evaluation is already disabled for this Working Capital loan. It must be enabled before disabling again.");
        }
    }

    private void validateDelinquencyNotDisabled(final Long loanId, final DataValidatorBuilder dataValidator) {
        if (actionRepository.isDelinquencyDisabledAsOf(loanId, DateUtils.getBusinessLocalDate())) {
            failGeneralValidation(dataValidator, "delinquency.is.disabled",
                    "Delinquency pause, resume and reschedule actions are not allowed while delinquency evaluation is disabled for this Working Capital loan.");
        }
    }

    /**
     * Returns the currently open (not-yet-enabled) disable so the caller can close its window on enable. There is at
     * most one such row (guaranteed by the singleton disable invariant).
     */
    public WorkingCapitalLoanDelinquencyAction findActiveDisable(final List<WorkingCapitalLoanDelinquencyAction> existing) {
        return existing.stream().filter(action -> DelinquencyAction.DISABLE.equals(action.getAction()))
                .filter(action -> action.getEndDate() == null).findFirst().orElse(null);
    }

    public WorkingCapitalLoanDelinquencyAction findActivePauseForResume(final List<WorkingCapitalLoanDelinquencyAction> existing,
            final LocalDate businessDate) {
        return existing.stream().filter(action -> DelinquencyAction.PAUSE.equals(action.getAction()))
                .filter(action -> !isPauseAlreadyResumed(action, existing)).filter(action -> WorkingCapitalLoanDelinquencyPauseUtils
                        .isPauseActiveOnDate(action.getStartDate(), action.getEndDate(), businessDate))
                .findFirst().orElseThrow(() -> {
                    final DataValidatorBuilder dataValidator = createValidator();
                    failParameterValidation(dataValidator, START_DATE, "resume.should.be.on.pause",
                            "Resume Delinquency Action can only be created during an active pause");
                    return buildValidationException(dataValidator);
                });
    }

    private boolean isPauseAlreadyResumed(final WorkingCapitalLoanDelinquencyAction pause,
            final List<WorkingCapitalLoanDelinquencyAction> existing) {
        return existing.stream().filter(action -> DelinquencyAction.RESUME.equals(action.getAction())).anyMatch(
                resume -> !pause.getStartDate().isAfter(resume.getStartDate()) && !resume.getStartDate().isAfter(pause.getEndDate()));
    }

    private void validatePause(final WorkingCapitalLoanDelinquencyAction action, final WorkingCapitalLoan workingCapitalLoan,
            final List<WorkingCapitalLoanDelinquencyAction> existing, final DataValidatorBuilder dataValidator) {
        validateBothDatesProvided(action, dataValidator);
        validateStartBeforeEnd(action, dataValidator);
        validateNotBeforeDisbursement(action, workingCapitalLoan, dataValidator);
        validateNoOverlap(action, existing, dataValidator);
    }

    private void validateResume(final WorkingCapitalLoanDelinquencyAction action, final List<WorkingCapitalLoanDelinquencyAction> existing,
            final DataValidatorBuilder dataValidator) {
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        validateResumeStartDate(action, businessDate, dataValidator);
        if (!dataValidator.hasError()) {
            final WorkingCapitalLoanDelinquencyAction activePause = findActivePauseForResume(existing, businessDate);
            validateResumeShortensActivePause(action, activePause, dataValidator);
        }
    }

    private void validateReset(WorkingCapitalLoanDelinquencyAction action, DataValidatorBuilder dataValidator) {
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        dataValidator.reset().parameter(START_DATE).value(action.getStartDate()).ignoreIfNull().isOneOfTheseValues(businessDate);
        if (action.getEndDate() == null) {
            action.setStartDate(businessDate);
        }
    }

    private void validateUndoReset(WorkingCapitalLoanDelinquencyAction action, DataValidatorBuilder dataValidator) {
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        dataValidator.reset().parameter(START_DATE).value(action.getStartDate()).ignoreIfNull().isOneOfTheseValues(businessDate);
        if (action.getEndDate() == null) {
            action.setStartDate(businessDate);
        }
    }

    private void validateResumeStartDate(final WorkingCapitalLoanDelinquencyAction action, final LocalDate businessDate,
            final DataValidatorBuilder dataValidator) {
        dataValidator.reset().parameter(START_DATE).value(action.getStartDate()).notNull();
        if (action.getStartDate() != null && !action.getStartDate().equals(businessDate)) {
            failParameterValidation(dataValidator, START_DATE, "resume.invalid.start.date",
                    "Start date of the Resume Delinquency action must be the current business date");
        }
    }

    private void validateResumeShortensActivePause(final WorkingCapitalLoanDelinquencyAction resumeAction,
            final WorkingCapitalLoanDelinquencyAction activePause, final DataValidatorBuilder dataValidator) {
        if (!resumeAction.getStartDate().isAfter(activePause.getStartDate())) {
            failParameterValidation(dataValidator, START_DATE, "resume.must.be.after.pause.start.date",
                    "Resume date must be after the active pause start date");
        }
        if (resumeAction.getStartDate().isAfter(activePause.getEndDate())) {
            failParameterValidation(dataValidator, START_DATE, "resume.should.be.on.pause",
                    "Resume Delinquency Action can only be created during an active pause");
        }
    }

    private void validateReschedule(final WorkingCapitalLoanDelinquencyAction action, final WorkingCapitalLoan workingCapitalLoan,
            final DataValidatorBuilder dataValidator) {
        validateLoanIsDisbursed(workingCapitalLoan, dataValidator);
        validateScheduleExists(workingCapitalLoan, dataValidator);

        final boolean hasPaymentGroup = action.getMinimumPayment() != null || action.getMinimumPaymentType() != null;
        final boolean hasFrequencyGroup = action.getFrequency() != null || action.getFrequencyType() != null;

        if (!hasPaymentGroup && !hasFrequencyGroup) {
            failGeneralValidation(dataValidator, "reschedule.no.change",
                    "At least one of payment (minimumPayment + minimumPaymentType) or frequency (frequency + frequencyType) group must be provided");
        }
        if (hasPaymentGroup) {
            validateMinimumPaymentGroupProvided(action, dataValidator);
        }
        if (hasFrequencyGroup) {
            validateFrequencyGroupProvided(action, dataValidator);
        }
    }

    private WorkingCapitalLoanDelinquencyAction parseCommand(final JsonCommand command, WorkingCapitalLoan workingCapitalLoan,
            final DataValidatorBuilder dataValidator) {
        final JsonElement json = command.parsedJson();
        final WorkingCapitalLoanDelinquencyAction action = new WorkingCapitalLoanDelinquencyAction();
        action.setAction(extractAction(json, dataValidator));
        action.setWorkingCapitalLoan(workingCapitalLoan);

        if (DelinquencyAction.PAUSE.equals(action.getAction())) {
            action.setStartDate(extractDate(json, START_DATE));
            action.setEndDate(extractDate(json, END_DATE));
        } else if (DelinquencyAction.RESCHEDULE.equals(action.getAction())) {
            action.setStartDate(DateUtils.getBusinessLocalDate());
            action.setMinimumPayment(extractBigDecimal(json, MINIMUM_PAYMENT));
            action.setMinimumPaymentType(extractMinimumPaymentType(json, dataValidator));
            action.setFrequency(extractInteger(json, FREQUENCY));
            action.setFrequencyType(extractFrequencyType(json, dataValidator));
        } else if (DelinquencyAction.RESUME.equals(action.getAction())) {
            action.setStartDate(extractDate(json, START_DATE));
        } else if (DelinquencyAction.RESET.equals(action.getAction())) {
            action.setStartDate(extractDate(json, START_DATE));
            action.setStartNewPeriod(jsonHelper.extractBooleanNamed(START_NEW_PERIOD, json));
        } else if (DelinquencyAction.UNDO_RESET.equals(action.getAction())) {
            action.setStartDate(extractDate(json, START_DATE));
        }

        return action;
    }

    private DelinquencyAction extractAction(final JsonElement json, final DataValidatorBuilder dataValidator) {
        final String actionString = jsonHelper.extractStringNamed(ACTION, json);
        dataValidator.reset().parameter(ACTION).value(actionString).notBlank();
        if (StringUtils.isEmpty(actionString)) {
            return null;
        }
        if ("pause".equalsIgnoreCase(actionString)) {
            return DelinquencyAction.PAUSE;
        } else if ("reschedule".equalsIgnoreCase(actionString)) {
            return DelinquencyAction.RESCHEDULE;
        } else if ("resume".equalsIgnoreCase(actionString)) {
            return DelinquencyAction.RESUME;
        } else if ("reset".equalsIgnoreCase(actionString)) {
            return DelinquencyAction.RESET;
        } else if ("undo_reset".equalsIgnoreCase(actionString)) {
            return DelinquencyAction.UNDO_RESET;
        } else if ("disable".equalsIgnoreCase(actionString)) {
            return DelinquencyAction.DISABLE;
        } else if ("enable".equalsIgnoreCase(actionString)) {
            return DelinquencyAction.ENABLE;
        }
        failParameterValidation(dataValidator, ACTION, "invalid.action",
                "Invalid Delinquency Action: " + actionString + ". Supported actions: pause, reschedule, resume, disable, enable");
        return null;
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

    private DelinquencyMinimumPaymentType extractMinimumPaymentType(final JsonElement json, final DataValidatorBuilder dataValidator) {
        final String value = jsonHelper.extractStringNamed(MINIMUM_PAYMENT_TYPE, json);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            return DelinquencyMinimumPaymentType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            failParameterValidation(dataValidator, MINIMUM_PAYMENT_TYPE, "invalid.minimum.payment.type",
                    "Invalid minimum payment type: " + value + ". Supported: PERCENTAGE, FLAT");
            return null;
        }
    }

    private DelinquencyFrequencyType extractFrequencyType(final JsonElement json, final DataValidatorBuilder dataValidator) {
        final String value = jsonHelper.extractStringNamed(FREQUENCY_TYPE, json);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            return DelinquencyFrequencyType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            failParameterValidation(dataValidator, FREQUENCY_TYPE, "invalid.frequency.type",
                    "Invalid frequency type: " + value + ". Supported: DAYS, WEEKS, MONTHS, YEARS");
            return null;
        }
    }

    private void validateLoanIsActive(final WorkingCapitalLoan workingCapitalLoan, final DataValidatorBuilder dataValidator) {
        if (!workingCapitalLoan.getLoanStatus().isActive()) {
            failGeneralValidation(dataValidator, "invalid.loan.state",
                    "Delinquency actions can be created only for active Working Capital loans.");
        }
    }

    private void validateDelinquencyConfigurationExists(final WorkingCapitalLoan workingCapitalLoan,
            final DataValidatorBuilder dataValidator) {
        if (workingCapitalLoan.getLoanProductRelatedDetails() == null
                || workingCapitalLoan.getLoanProductRelatedDetails().getDelinquencyBucket() == null) {
            failGeneralValidation(dataValidator, "no.delinquency.configuration",
                    "Delinquency actions require a delinquency bucket configured for the Working Capital loan.");
        }
    }

    private void validateLoanIsDisbursed(final WorkingCapitalLoan workingCapitalLoan, final DataValidatorBuilder dataValidator) {
        final boolean isDisbursed = workingCapitalLoan.getDisbursementDetails().stream()
                .map(WorkingCapitalLoanDisbursementDetails::getActualDisbursementDate).anyMatch(Objects::nonNull);
        if (!isDisbursed) {
            failGeneralValidation(dataValidator, "loan.not.disbursed", "Reschedule action requires the loan to be disbursed.");
        }
    }

    private void validateScheduleExists(final WorkingCapitalLoan workingCapitalLoan, final DataValidatorBuilder dataValidator) {
        final List<WorkingCapitalLoanDelinquencyRangeSchedule> periods = rangeScheduleRepository
                .findByLoanIdOrderByPeriodNumberAsc(workingCapitalLoan.getId());
        if (periods.isEmpty()) {
            failGeneralValidation(dataValidator, "no.schedule", "Reschedule action requires an existing delinquency range schedule.");
        }
    }

    private void validateMinimumPaymentGroupProvided(final WorkingCapitalLoanDelinquencyAction action,
            final DataValidatorBuilder dataValidator) {
        dataValidator.reset().parameter(MINIMUM_PAYMENT).value(action.getMinimumPayment()).notNull().positiveAmount();
        if (action.getMinimumPaymentType() == null) {
            failParameterValidation(dataValidator, MINIMUM_PAYMENT_TYPE, "mandatory.when.minimum.payment.provided",
                    "The parameter `minimumPaymentType` is mandatory when `minimumPayment` is provided");
        }
    }

    private void validateFrequencyGroupProvided(final WorkingCapitalLoanDelinquencyAction action,
            final DataValidatorBuilder dataValidator) {
        dataValidator.reset().parameter(FREQUENCY).value(action.getFrequency()).notNull().integerGreaterThanZero();
        if (action.getFrequencyType() == null) {
            failParameterValidation(dataValidator, FREQUENCY_TYPE, "mandatory.when.frequency.provided",
                    "The parameter `frequencyType` is mandatory when `frequency` is provided");
        }
    }

    private void validateBothDatesProvided(final WorkingCapitalLoanDelinquencyAction action, final DataValidatorBuilder dataValidator) {
        dataValidator.reset().parameter(START_DATE).value(action.getStartDate()).notNull();
        dataValidator.reset().parameter(END_DATE).value(action.getEndDate()).notNull();
    }

    private void validateStartBeforeEnd(final WorkingCapitalLoanDelinquencyAction action, final DataValidatorBuilder dataValidator) {
        if (action.getStartDate() != null && action.getEndDate() != null && action.getStartDate().isAfter(action.getEndDate())) {
            failGeneralValidation(dataValidator, "invalid.start.date.and.end.date",
                    "Delinquency pause start date must not be after end date");
        }
    }

    private void validateNotBeforeDisbursement(final WorkingCapitalLoanDelinquencyAction action,
            final WorkingCapitalLoan workingCapitalLoan, final DataValidatorBuilder dataValidator) {
        if (action.getStartDate() == null) {
            return;
        }
        final LocalDate firstDisbursementDate = workingCapitalLoan.getDisbursementDetails().stream()
                .map(WorkingCapitalLoanDisbursementDetails::getActualDisbursementDate).filter(Objects::nonNull).findFirst().orElse(null);
        if (firstDisbursementDate != null && firstDisbursementDate.isAfter(action.getStartDate())) {
            failParameterValidation(dataValidator, START_DATE, "must.be.after.first.disbursal.date",
                    "Start date of pause period must be after first disbursal date");
        }
    }

    private void validateNoOverlap(final WorkingCapitalLoanDelinquencyAction parsed,
            final List<WorkingCapitalLoanDelinquencyAction> existing, final DataValidatorBuilder dataValidator) {
        if (parsed.getStartDate() == null || parsed.getEndDate() == null) {
            return;
        }
        final boolean overlaps = existing.stream().filter(e -> DelinquencyAction.PAUSE.equals(e.getAction()))
                .anyMatch(e -> WorkingCapitalLoanDelinquencyPauseUtils.inclusivePausePeriodsOverlap(parsed.getStartDate(),
                        parsed.getEndDate(), e.getStartDate(),
                        WorkingCapitalLoanDelinquencyPauseUtils.resolveEffectivePauseEnd(e, existing)));
        if (overlaps) {
            failGeneralValidation(dataValidator, "overlapping", "Delinquency pause period cannot overlap with another pause period");
        }
    }

    private DataValidatorBuilder createValidator() {
        return new DataValidatorBuilder(new ArrayList<>()).resource(VALIDATION_RESOURCE);
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

    private PlatformApiDataValidationException buildValidationException(final DataValidatorBuilder dataValidator) {
        return new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                dataValidator.getDataValidationErrors());
    }

}
