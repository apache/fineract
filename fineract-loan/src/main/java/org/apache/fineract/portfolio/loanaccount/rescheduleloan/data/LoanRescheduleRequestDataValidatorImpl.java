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
package org.apache.fineract.portfolio.loanaccount.rescheduleloan.data;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.RescheduleLoansApiConstants;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("loanRescheduleRequestDataValidator")
@AllArgsConstructor
public class LoanRescheduleRequestDataValidatorImpl implements LoanRescheduleRequestDataValidator {

    public static final Set<String> CREATE_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(RescheduleLoansApiConstants.localeParamName, RescheduleLoansApiConstants.dateFormatParamName,
                    RescheduleLoansApiConstants.graceOnPrincipalParamName,
                    RescheduleLoansApiConstants.recurringMoratoriumOnPrincipalPeriodsParamName,
                    RescheduleLoansApiConstants.graceOnInterestParamName, RescheduleLoansApiConstants.extraTermsParamName,
                    RescheduleLoansApiConstants.rescheduleFromDateParamName, RescheduleLoansApiConstants.newInterestRateParamName,
                    RescheduleLoansApiConstants.rescheduleReasonIdParamName, RescheduleLoansApiConstants.rescheduleReasonCommentParamName,
                    RescheduleLoansApiConstants.submittedOnDateParamName, RescheduleLoansApiConstants.loanIdParamName,
                    RescheduleLoansApiConstants.adjustedDueDateParamName, RescheduleLoansApiConstants.recalculateInterestParamName,
                    RescheduleLoansApiConstants.endDateParamName, RescheduleLoansApiConstants.emiParamName));
    private static final Set<String> REJECT_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(RescheduleLoansApiConstants.localeParamName, RescheduleLoansApiConstants.dateFormatParamName,
                    RescheduleLoansApiConstants.rejectedOnDateParam));
    public static final Set<String> APPROVE_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(RescheduleLoansApiConstants.localeParamName, RescheduleLoansApiConstants.dateFormatParamName,
                    RescheduleLoansApiConstants.approvedOnDateParam));
    private final FromJsonHelper fromJsonHelper;
    @Qualifier("progressiveLoanRescheduleRequestDataValidatorImpl")
    private final LoanRescheduleRequestDataValidator progressiveLoanRescheduleRequestDataValidatorDelegate;

    public static BigDecimal validateInterestRate(FromJsonHelper fromJsonHelper, JsonElement jsonElement,
            DataValidatorBuilder dataValidatorBuilder) {
        final BigDecimal interestRate = fromJsonHelper
                .extractBigDecimalWithLocaleNamed(RescheduleLoansApiConstants.newInterestRateParamName, jsonElement);
        dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.newInterestRateParamName).value(interestRate).ignoreIfNull()
                .positiveAmount();
        return interestRate;
    }

    private static void validateMultiDisburseLoan(Loan loan, DataValidatorBuilder dataValidatorBuilder) {
        if (loan.isMultiDisburmentLoan()) {
            if (!loan.loanProduct().isDisallowExpectedDisbursements()) {
                dataValidatorBuilder.reset().failWithCodeNoParameterAddedToErrorCode(
                        RescheduleLoansApiConstants.rescheduleForMultiDisbursementNotSupportedErrorCode,
                        "Loan rescheduling is not supported for multidisbursement tranche loans");
            }
        }
    }

    public static void validateEMIAndEndDate(FromJsonHelper fromJsonHelper, Loan loan, JsonElement jsonElement,
            DataValidatorBuilder dataValidatorBuilder) {
        final LocalDate endDate = fromJsonHelper.extractLocalDateNamed(RescheduleLoansApiConstants.endDateParamName, jsonElement);
        final BigDecimal emi = fromJsonHelper.extractBigDecimalWithLocaleNamed(RescheduleLoansApiConstants.emiParamName, jsonElement);
        if (emi != null || endDate != null) {
            dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.endDateParamName).value(endDate).notNull();
            dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.emiParamName).value(emi).notNull().positiveAmount();

            if (endDate != null) {
                LoanRepaymentScheduleInstallment endInstallment = loan.getRepaymentScheduleInstallment(endDate);

                if (endInstallment == null) {
                    dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.endDateParamName)
                            .failWithCode("repayment.schedule.installment.does.not.exist", "Repayment schedule installment does not exist");
                }
            }
        }
    }

    public static LocalDate validateAndRetrieveAdjustedDate(FromJsonHelper fromJsonHelper, JsonElement jsonElement,
            LocalDate rescheduleFromDate, DataValidatorBuilder dataValidatorBuilder) {
        final LocalDate adjustedDueDate = fromJsonHelper.extractLocalDateNamed(RescheduleLoansApiConstants.adjustedDueDateParamName,
                jsonElement);

        if (adjustedDueDate != null && DateUtils.isBefore(adjustedDueDate, rescheduleFromDate)) {
            dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.rescheduleFromDateParamName).failWithCode(
                    "adjustedDueDate.before.rescheduleFromDate", "Adjusted due date cannot be before the reschedule from date");
        }
        return adjustedDueDate;
    }

    public static void validateRescheduleReasonComment(FromJsonHelper fromJsonHelper, JsonElement jsonElement,
            DataValidatorBuilder dataValidatorBuilder) {
        final String rescheduleReasonComment = fromJsonHelper
                .extractStringNamed(RescheduleLoansApiConstants.rescheduleReasonCommentParamName, jsonElement);
        dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.rescheduleReasonCommentParamName).value(rescheduleReasonComment)
                .ignoreIfNull().notExceedingLengthOf(500);
    }

    public static void validateRescheduleReasonId(FromJsonHelper fromJsonHelper, JsonElement jsonElement,
            DataValidatorBuilder dataValidatorBuilder) {
        final Long rescheduleReasonId = fromJsonHelper.extractLongNamed(RescheduleLoansApiConstants.rescheduleReasonIdParamName,
                jsonElement);
        dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.rescheduleReasonIdParamName).value(rescheduleReasonId).notNull()
                .integerGreaterThanZero();
    }

    public static void validateExtraTerms(FromJsonHelper fromJsonHelper, JsonElement jsonElement,
            DataValidatorBuilder dataValidatorBuilder) {
        final Integer extraTerms = fromJsonHelper.extractIntegerWithLocaleNamed(RescheduleLoansApiConstants.extraTermsParamName,
                jsonElement);
        dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.extraTermsParamName).value(extraTerms).ignoreIfNull()
                .integerGreaterThanZero();
    }

    public static void validateGraceOnInterest(FromJsonHelper fromJsonHelper, JsonElement jsonElement,
            DataValidatorBuilder dataValidatorBuilder) {
        final Integer graceOnInterest = fromJsonHelper.extractIntegerWithLocaleNamed(RescheduleLoansApiConstants.graceOnInterestParamName,
                jsonElement);
        dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.graceOnInterestParamName).value(graceOnInterest).ignoreIfNull()
                .integerGreaterThanZero();
    }

    public static void validateGraceOnPrincipal(FromJsonHelper fromJsonHelper, JsonElement jsonElement,
            DataValidatorBuilder dataValidatorBuilder) {
        final Integer graceOnPrincipal = fromJsonHelper.extractIntegerWithLocaleNamed(RescheduleLoansApiConstants.graceOnPrincipalParamName,
                jsonElement);
        dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.graceOnPrincipalParamName).value(graceOnPrincipal).ignoreIfNull()
                .integerGreaterThanZero();
    }

    public static LocalDate validateAndRetrieveRescheduleFromDate(FromJsonHelper fromJsonHelper, JsonElement jsonElement,
            DataValidatorBuilder dataValidatorBuilder) {
        final LocalDate rescheduleFromDate = fromJsonHelper.extractLocalDateNamed(RescheduleLoansApiConstants.rescheduleFromDateParamName,
                jsonElement);
        dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.rescheduleFromDateParamName).value(rescheduleFromDate).notNull();
        return rescheduleFromDate;
    }

    public static void validateSubmittedOnDate(FromJsonHelper fromJsonHelper, Loan loan, JsonElement jsonElement,
            DataValidatorBuilder dataValidatorBuilder) {
        final LocalDate submittedOnDate = fromJsonHelper.extractLocalDateNamed(RescheduleLoansApiConstants.submittedOnDateParamName,
                jsonElement);
        dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.submittedOnDateParamName).value(submittedOnDate).notNull();

        if (submittedOnDate != null && DateUtils.isAfter(loan.getDisbursementDate(), submittedOnDate)) {
            dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.submittedOnDateParamName)
                    .failWithCode("before.loan.disbursement.date", "Submission date cannot be before the loan disbursement date");
        }
    }

    public static void validateLoanIsActive(Loan loan, DataValidatorBuilder dataValidatorBuilder) {
        if (!loan.getStatus().isActive()) {
            dataValidatorBuilder.reset().failWithCodeNoParameterAddedToErrorCode("loan.is.not.active", "Loan is not active");
        }
    }

    public static void validateSupportedParameters(JsonCommand jsonCommand, Set<String> createRequestDataParameters) {
        final String jsonString = jsonCommand.json();

        if (StringUtils.isBlank(jsonString)) {
            throw new InvalidJsonException();
        }

        final Type typeToken = new TypeToken<Map<String, Object>>() {}.getType();
        jsonCommand.checkForUnsupportedParameters(typeToken, jsonString, createRequestDataParameters);
    }

    public static void validateReschedulingInstallment(DataValidatorBuilder dataValidatorBuilder,
            LoanRepaymentScheduleInstallment installment) {
        if (installment == null) {
            dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.rescheduleFromDateParamName)
                    .failWithCode("repayment.schedule.installment.does.not.exist", "Repayment schedule installment does not exist");
        }

        if (installment != null && installment.isObligationsMet()) {
            dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.rescheduleFromDateParamName)
                    .failWithCode("repayment.schedule.installment.obligation.met", "Repayment schedule installment obligation met");
        }
    }

    public static void validateForOverdueCharges(final DataValidatorBuilder dataValidatorBuilder, final Loan loan,
            final LoanRepaymentScheduleInstallment installment) {
        if (installment != null) {
            LocalDate rescheduleFromDate = installment.getFromDate();
            Collection<LoanCharge> charges = loan.getLoanCharges();
            for (LoanCharge loanCharge : charges) {
                if (loanCharge.isOverdueInstallmentCharge() && DateUtils.isAfter(loanCharge.getDueLocalDate(), rescheduleFromDate)) {
                    dataValidatorBuilder.failWithCodeNoParameterAddedToErrorCode("not.allowed.due.to.overdue.charges");
                    break;
                }
            }
        }
    }

    /**
     * Validates the request to create a new loan reschedule entry
     *
     * @param jsonCommand
     *            the JSON command object (instance of the JsonCommand class)
     **/
    @Override
    public void validateForCreateAction(final JsonCommand jsonCommand, final Loan loan) {
        if (loan.getLoanProductRelatedDetail().getLoanScheduleType() == LoanScheduleType.PROGRESSIVE) {
            progressiveLoanRescheduleRequestDataValidatorDelegate.validateForCreateAction(jsonCommand, loan);
        } else {
            validateSupportedParameters(jsonCommand, CREATE_REQUEST_DATA_PARAMETERS);
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder dataValidatorBuilder = new DataValidatorBuilder(dataValidationErrors)
                    .resource(StringUtils.lowerCase(RescheduleLoansApiConstants.ENTITY_NAME));

            final JsonElement jsonElement = jsonCommand.parsedJson();
            validateLoanIsActive(loan, dataValidatorBuilder);
            validateSubmittedOnDate(fromJsonHelper, loan, jsonElement, dataValidatorBuilder);
            final LocalDate rescheduleFromDate = validateAndRetrieveRescheduleFromDate(fromJsonHelper, jsonElement, dataValidatorBuilder);
            validateInterestRate(fromJsonHelper, jsonElement, dataValidatorBuilder);
            validateGraceOnPrincipal(fromJsonHelper, jsonElement, dataValidatorBuilder);
            validateGraceOnInterest(fromJsonHelper, jsonElement, dataValidatorBuilder);
            validateExtraTerms(fromJsonHelper, jsonElement, dataValidatorBuilder);
            validateRescheduleReasonId(fromJsonHelper, jsonElement, dataValidatorBuilder);
            validateRescheduleReasonComment(fromJsonHelper, jsonElement, dataValidatorBuilder);
            validateAndRetrieveAdjustedDate(fromJsonHelper, jsonElement, rescheduleFromDate, dataValidatorBuilder);
            validateEMIAndEndDate(fromJsonHelper, loan, jsonElement, dataValidatorBuilder);
            validateIsThereAnyIncomingChange(fromJsonHelper, jsonElement, dataValidatorBuilder);
            validateMultiDisburseLoan(loan, dataValidatorBuilder);

            LoanRepaymentScheduleInstallment installment = loan.getRepaymentScheduleInstallment(rescheduleFromDate);
            validateReschedulingInstallment(dataValidatorBuilder, installment);
            validateForOverdueCharges(dataValidatorBuilder, loan, installment);

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
    }

    private void validateIsThereAnyIncomingChange(FromJsonHelper fromJsonHelper, JsonElement jsonElement,
            DataValidatorBuilder dataValidatorBuilder) {
        // at least one of the following must be provided => graceOnPrincipal,
        // graceOnInterest, extraTerms, newInterestRate
        if (!fromJsonHelper.parameterExists(RescheduleLoansApiConstants.graceOnPrincipalParamName, jsonElement)
                && !fromJsonHelper.parameterExists(RescheduleLoansApiConstants.graceOnInterestParamName, jsonElement)
                && !fromJsonHelper.parameterExists(RescheduleLoansApiConstants.extraTermsParamName, jsonElement)
                && !fromJsonHelper.parameterExists(RescheduleLoansApiConstants.newInterestRateParamName, jsonElement)
                && !fromJsonHelper.parameterExists(RescheduleLoansApiConstants.adjustedDueDateParamName, jsonElement)
                && !fromJsonHelper.parameterExists(RescheduleLoansApiConstants.emiParamName, jsonElement)) {
            dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.graceOnPrincipalParamName).notNull();
        }
    }

    /**
     * Validates a user request to approve a loan reschedule request
     *
     * @param jsonCommand
     *            the JSON command object (instance of the JsonCommand class)
     **/
    @Override
    public void validateForApproveAction(final JsonCommand jsonCommand, LoanRescheduleRequest loanRescheduleRequest) {
        if (loanRescheduleRequest.getLoan().getLoanRepaymentScheduleDetail().getLoanScheduleType().equals(LoanScheduleType.PROGRESSIVE)) {
            progressiveLoanRescheduleRequestDataValidatorDelegate.validateForApproveAction(jsonCommand, loanRescheduleRequest);
        } else {
            validateSupportedParameters(jsonCommand, APPROVE_REQUEST_DATA_PARAMETERS);

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder dataValidatorBuilder = new DataValidatorBuilder(dataValidationErrors)
                    .resource(StringUtils.lowerCase(RescheduleLoansApiConstants.ENTITY_NAME));

            final JsonElement jsonElement = jsonCommand.parsedJson();
            validateApprovalDate(fromJsonHelper, loanRescheduleRequest, jsonElement, dataValidatorBuilder);
            validateRescheduleRequestStatus(loanRescheduleRequest, dataValidatorBuilder);

            LocalDate rescheduleFromDate = loanRescheduleRequest.getRescheduleFromDate();
            final Loan loan = loanRescheduleRequest.getLoan();
            LoanRepaymentScheduleInstallment installment;

            validateLoanIsActive(loan, dataValidatorBuilder);
            installment = loan.getRepaymentScheduleInstallment(rescheduleFromDate);

            validateReschedulingInstallment(dataValidatorBuilder, installment);
            validateForOverdueCharges(dataValidatorBuilder, loan, installment);

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
    }

    public static void validateRescheduleRequestStatus(LoanRescheduleRequest loanRescheduleRequest,
            DataValidatorBuilder dataValidatorBuilder) {
        LoanRescheduleRequestStatusEnumData loanRescheduleRequestStatusEnumData = LoanRescheduleRequestEnumerations
                .status(loanRescheduleRequest.getStatusEnum());

        if (!loanRescheduleRequestStatusEnumData.isPendingApproval()) {
            dataValidatorBuilder.reset().failWithCodeNoParameterAddedToErrorCode("request.is.not.in.submitted.and.pending.state",
                    "Loan reschedule request approval is not allowed. "
                            + "Loan reschedule request is not in submitted and pending approval state.");
        }
    }

    public static void validateApprovalDate(FromJsonHelper fromJsonHelper, LoanRescheduleRequest loanRescheduleRequest,
            JsonElement jsonElement, DataValidatorBuilder dataValidatorBuilder) {
        final LocalDate approvedOnDate = fromJsonHelper.extractLocalDateNamed(RescheduleLoansApiConstants.approvedOnDateParam, jsonElement);
        dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.approvedOnDateParam).value(approvedOnDate).notNull();

        if (approvedOnDate != null && DateUtils.isAfter(loanRescheduleRequest.getSubmittedOnDate(), approvedOnDate)) {
            dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.approvedOnDateParam).failWithCode("before.submission.date",
                    "Approval date cannot be before the request submission date.");
        }
    }

    /**
     * Validates a user request to reject a loan reschedule request
     *
     * @param jsonCommand
     *            the JSON command object (instance of the JsonCommand class)
     **/
    @Override
    public void validateForRejectAction(final JsonCommand jsonCommand, LoanRescheduleRequest loanRescheduleRequest) {
        validateSupportedParameters(jsonCommand, REJECT_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder dataValidatorBuilder = new DataValidatorBuilder(dataValidationErrors)
                .resource(StringUtils.lowerCase(RescheduleLoansApiConstants.ENTITY_NAME));

        final JsonElement jsonElement = jsonCommand.parsedJson();

        final LocalDate rejectedOnDate = this.fromJsonHelper.extractLocalDateNamed(RescheduleLoansApiConstants.rejectedOnDateParam,
                jsonElement);
        dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.rejectedOnDateParam).value(rejectedOnDate).notNull();

        if (rejectedOnDate != null && DateUtils.isAfter(loanRescheduleRequest.getSubmittedOnDate(), rejectedOnDate)) {
            dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.rejectedOnDateParam).failWithCode("before.submission.date",
                    "Rejection date cannot be before the request submission date.");
        }

        LoanRescheduleRequestStatusEnumData loanRescheduleRequestStatusEnumData = LoanRescheduleRequestEnumerations
                .status(loanRescheduleRequest.getStatusEnum());

        if (!loanRescheduleRequestStatusEnumData.isPendingApproval()) {
            dataValidatorBuilder.reset().failWithCodeNoParameterAddedToErrorCode("request.is.not.in.submitted.and.pending.state",
                    "Loan reschedule request rejection is not allowed. "
                            + "Loan reschedule request is not in submitted and pending approval state.");
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
