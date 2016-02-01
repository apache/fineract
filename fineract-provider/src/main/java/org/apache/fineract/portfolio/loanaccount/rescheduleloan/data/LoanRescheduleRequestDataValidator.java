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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.RescheduleLoansApiConstants;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.service.LoanRescheduleRequestReadPlatformService;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class LoanRescheduleRequestDataValidator {

    private final FromJsonHelper fromJsonHelper;
    private final LoanRescheduleRequestReadPlatformService loanRescheduleRequestReadPlatformService;

    @Autowired
    public LoanRescheduleRequestDataValidator(FromJsonHelper fromJsonHelper,
            LoanRescheduleRequestReadPlatformService loanRescheduleRequestReadPlatformService) {
        this.fromJsonHelper = fromJsonHelper;
        this.loanRescheduleRequestReadPlatformService = loanRescheduleRequestReadPlatformService;
    }

    /**
     * Validates the request to create a new loan reschedule entry
     * 
     * @param jsonCommand
     *            the JSON command object (instance of the JsonCommand class)
     * @return void
     **/
    public void validateForCreateAction(final JsonCommand jsonCommand, final Loan loan) {

        final String jsonString = jsonCommand.json();

        if (StringUtils.isBlank(jsonString)) { throw new InvalidJsonException(); }

        final Type typeToken = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromJsonHelper
                .checkForUnsupportedParameters(typeToken, jsonString, RescheduleLoansApiConstants.CREATE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder dataValidatorBuilder = new DataValidatorBuilder(dataValidationErrors).resource(StringUtils
                .lowerCase(RescheduleLoansApiConstants.ENTITY_NAME));

        final JsonElement jsonElement = jsonCommand.parsedJson();

        if (!loan.status().isActive()) {
            dataValidatorBuilder.reset().failWithCodeNoParameterAddedToErrorCode("loan.is.not.active", "Loan is not active");
        }

        final Long loanId = this.fromJsonHelper.extractLongNamed(RescheduleLoansApiConstants.loanIdParamName, jsonElement);
        dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.loanIdParamName).value(loanId).notNull()
                .integerGreaterThanZero();

        final LocalDate submittedOnDate = this.fromJsonHelper.extractLocalDateNamed(RescheduleLoansApiConstants.submittedOnDateParamName,
                jsonElement);
        dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.submittedOnDateParamName).value(submittedOnDate).notNull();

        if (submittedOnDate != null && loan.getDisbursementDate().isAfter(submittedOnDate)) {
            dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.submittedOnDateParamName)
                    .failWithCode("before.loan.disbursement.date", "Submission date cannot be before the loan disbursement date");
        }

        final LocalDate rescheduleFromDate = this.fromJsonHelper.extractLocalDateNamed(
                RescheduleLoansApiConstants.rescheduleFromDateParamName, jsonElement);
        dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.rescheduleFromDateParamName).value(rescheduleFromDate).notNull();

        final Integer graceOnPrincipal = this.fromJsonHelper.extractIntegerWithLocaleNamed(
                RescheduleLoansApiConstants.graceOnPrincipalParamName, jsonElement);
        dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.graceOnPrincipalParamName).value(graceOnPrincipal)
                .ignoreIfNull().integerGreaterThanZero();

        final Integer graceOnInterest = this.fromJsonHelper.extractIntegerWithLocaleNamed(
                RescheduleLoansApiConstants.graceOnInterestParamName, jsonElement);
        dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.graceOnInterestParamName).value(graceOnInterest).ignoreIfNull()
                .integerGreaterThanZero();

        final Integer extraTerms = this.fromJsonHelper.extractIntegerWithLocaleNamed(RescheduleLoansApiConstants.extraTermsParamName,
                jsonElement);
        dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.extraTermsParamName).value(extraTerms).ignoreIfNull()
                .integerGreaterThanZero();

        final Long rescheduleReasonId = this.fromJsonHelper.extractLongNamed(RescheduleLoansApiConstants.rescheduleReasonIdParamName,
                jsonElement);
        dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.rescheduleReasonIdParamName).value(rescheduleReasonId).notNull()
                .integerGreaterThanZero();

        final String rescheduleReasonComment = this.fromJsonHelper.extractStringNamed(
                RescheduleLoansApiConstants.rescheduleReasonCommentParamName, jsonElement);
        dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.rescheduleReasonCommentParamName).value(rescheduleReasonComment)
                .ignoreIfNull().notExceedingLengthOf(500);

        final LocalDate adjustedDueDate = this.fromJsonHelper.extractLocalDateNamed(RescheduleLoansApiConstants.adjustedDueDateParamName,
                jsonElement);

        if (adjustedDueDate != null && rescheduleFromDate != null && adjustedDueDate.isBefore(rescheduleFromDate)) {
            dataValidatorBuilder
                    .reset()
                    .parameter(RescheduleLoansApiConstants.rescheduleFromDateParamName)
                    .failWithCode("adjustedDueDate.before.rescheduleFromDate",
                            "Adjusted due date cannot be before the reschedule from date");
        }

        // at least one of the following must be provided => graceOnPrincipal,
        // graceOnInterest, extraTerms, newInterestRate
        if (!this.fromJsonHelper.parameterExists(RescheduleLoansApiConstants.graceOnPrincipalParamName, jsonElement)
                && !this.fromJsonHelper.parameterExists(RescheduleLoansApiConstants.graceOnInterestParamName, jsonElement)
                && !this.fromJsonHelper.parameterExists(RescheduleLoansApiConstants.extraTermsParamName, jsonElement)
                && !this.fromJsonHelper.parameterExists(RescheduleLoansApiConstants.newInterestRateParamName, jsonElement)
                && !this.fromJsonHelper.parameterExists(RescheduleLoansApiConstants.adjustedDueDateParamName, jsonElement)) {
            dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.graceOnPrincipalParamName).notNull();
        }

        if (rescheduleFromDate != null) {
            LoanRepaymentScheduleInstallment installment = loan.getRepaymentScheduleInstallment(rescheduleFromDate);

            if (installment == null) {
                dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.rescheduleFromDateParamName)
                        .failWithCode("repayment.schedule.installment.does.not.exist", "Repayment schedule installment does not exist");
            }

            if (installment != null && installment.isObligationsMet()) {
                dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.rescheduleFromDateParamName)
                        .failWithCode("repayment.schedule.installment.obligation.met", "Repayment schedule installment obligation met");
            }

            if (installment != null && installment.isPartlyPaid()) {
                dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.rescheduleFromDateParamName)
                        .failWithCode("repayment.schedule.installment.partly.paid", "Repayment schedule installment is partly paid");
            }
        }

        if (loanId != null) {
            List<LoanRescheduleRequestData> loanRescheduleRequestData = this.loanRescheduleRequestReadPlatformService
                    .readLoanRescheduleRequests(loanId, LoanStatus.APPROVED.getValue());

            if (loanRescheduleRequestData.size() > 0) {
                dataValidatorBuilder.reset().failWithCodeNoParameterAddedToErrorCode("loan.already.rescheduled",
                        "The loan can only be rescheduled once.");
            }
        }
        if(loan.isMultiDisburmentLoan()) {
            dataValidatorBuilder.reset().failWithCodeNoParameterAddedToErrorCode(RescheduleLoansApiConstants.resheduleForMultiDisbursementNotSupportedErrorCode,
                    "Loan rescheduling is not supported for multidisbursement loans");
        }
        
        if(loan.isInterestRecalculationEnabledForProduct()) {
            dataValidatorBuilder.reset().failWithCodeNoParameterAddedToErrorCode(RescheduleLoansApiConstants.resheduleWithInterestRecalculationNotSupportedErrorCode,
                    "Loan rescheduling is not supported for the loan product with interest recalculation enabled");
        }
        
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    /**
     * Validates a user request to approve a loan reschedule request
     * 
     * @param jsonCommand
     *            the JSON command object (instance of the JsonCommand class)
     * @return void
     **/
    public void validateForApproveAction(final JsonCommand jsonCommand, LoanRescheduleRequest loanRescheduleRequest) {
        final String jsonString = jsonCommand.json();

        if (StringUtils.isBlank(jsonString)) { throw new InvalidJsonException(); }

        final Type typeToken = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromJsonHelper.checkForUnsupportedParameters(typeToken, jsonString,
                RescheduleLoansApiConstants.APPROVE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder dataValidatorBuilder = new DataValidatorBuilder(dataValidationErrors).resource(StringUtils
                .lowerCase(RescheduleLoansApiConstants.ENTITY_NAME));

        final JsonElement jsonElement = jsonCommand.parsedJson();

        final LocalDate approvedOnDate = this.fromJsonHelper.extractLocalDateNamed(RescheduleLoansApiConstants.approvedOnDateParam,
                jsonElement);
        dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.approvedOnDateParam).value(approvedOnDate).notNull();

        if (approvedOnDate != null && loanRescheduleRequest.getSubmittedOnDate().isAfter(approvedOnDate)) {
            dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.approvedOnDateParam)
                    .failWithCode("before.submission.date", "Approval date cannot be before the request submission date.");
        }

        LoanRescheduleRequestStatusEnumData loanRescheduleRequestStatusEnumData = LoanRescheduleRequestEnumerations
                .status(loanRescheduleRequest.getStatusEnum());

        if (!loanRescheduleRequestStatusEnumData.isPendingApproval()) {
            dataValidatorBuilder.reset().failWithCodeNoParameterAddedToErrorCode(
                    "request.is.not.in.submitted.and.pending.state",
                    "Loan reschedule request approval is not allowed. "
                            + "Loan reschedule request is not in submitted and pending approval state.");
        }

        LocalDate rescheduleFromDate = loanRescheduleRequest.getRescheduleFromDate();
        final Loan loan = loanRescheduleRequest.getLoan();

        if (loan != null) {
            Long loanId = loan.getId();

            if (!loan.status().isActive()) {
                dataValidatorBuilder.reset().failWithCodeNoParameterAddedToErrorCode("loan.is.not.active", "Loan is not active");
            }

            if (rescheduleFromDate != null) {
                LoanRepaymentScheduleInstallment installment = loan.getRepaymentScheduleInstallment(rescheduleFromDate);

                if (installment == null) {
                    dataValidatorBuilder.reset().failWithCodeNoParameterAddedToErrorCode(
                            "loan.repayment.schedule.installment.does.not.exist", "Repayment schedule installment does not exist");
                }

                if (installment != null && installment.isObligationsMet()) {
                    dataValidatorBuilder.reset().failWithCodeNoParameterAddedToErrorCode(
                            "loan.repayment.schedule.installment." + "obligation.met", "Repayment schedule installment obligation met");
                }
            }

            if (loanId != null) {
                List<LoanRescheduleRequestData> loanRescheduleRequestData = this.loanRescheduleRequestReadPlatformService
                        .readLoanRescheduleRequests(loanId, LoanStatus.APPROVED.getValue());

                if (loanRescheduleRequestData.size() > 0) {
                    dataValidatorBuilder.reset().failWithCodeNoParameterAddedToErrorCode("loan.already.rescheduled",
                            "The loan can only be rescheduled once.");
                }
            }
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    /**
     * Validates a user request to reject a loan reschedule request
     * 
     * @param jsonCommand
     *            the JSON command object (instance of the JsonCommand class)
     * @return void
     **/
    public void validateForRejectAction(final JsonCommand jsonCommand, LoanRescheduleRequest loanRescheduleRequest) {
        final String jsonString = jsonCommand.json();

        if (StringUtils.isBlank(jsonString)) { throw new InvalidJsonException(); }

        final Type typeToken = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromJsonHelper
                .checkForUnsupportedParameters(typeToken, jsonString, RescheduleLoansApiConstants.REJECT_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder dataValidatorBuilder = new DataValidatorBuilder(dataValidationErrors).resource(StringUtils
                .lowerCase(RescheduleLoansApiConstants.ENTITY_NAME));

        final JsonElement jsonElement = jsonCommand.parsedJson();

        final LocalDate rejectedOnDate = this.fromJsonHelper.extractLocalDateNamed(RescheduleLoansApiConstants.rejectedOnDateParam,
                jsonElement);
        dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.rejectedOnDateParam).value(rejectedOnDate).notNull();

        if (rejectedOnDate != null && loanRescheduleRequest.getSubmittedOnDate().isAfter(rejectedOnDate)) {
            dataValidatorBuilder.reset().parameter(RescheduleLoansApiConstants.rejectedOnDateParam)
                    .failWithCode("before.submission.date", "Rejection date cannot be before the request submission date.");
        }

        LoanRescheduleRequestStatusEnumData loanRescheduleRequestStatusEnumData = LoanRescheduleRequestEnumerations
                .status(loanRescheduleRequest.getStatusEnum());

        if (!loanRescheduleRequestStatusEnumData.isPendingApproval()) {
            dataValidatorBuilder.reset().failWithCodeNoParameterAddedToErrorCode(
                    "request.is.not.in.submitted.and.pending.state",
                    "Loan reschedule request rejection is not allowed. "
                            + "Loan reschedule request is not in submitted and pending approval state.");
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}
