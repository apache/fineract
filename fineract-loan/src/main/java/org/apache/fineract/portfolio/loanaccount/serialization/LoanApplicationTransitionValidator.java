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
package org.apache.fineract.portfolio.loanaccount.serialization;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.exception.GroupNotActiveException;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanStateTransitionException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class LoanApplicationTransitionValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final LoanLifecycleStateMachine defaultLoanLifecycleStateMachine;

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public void validateApproval(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> disbursementParameters = new HashSet<>(
                Arrays.asList(LoanApiConstants.loanIdTobeApproved, LoanApiConstants.approvedLoanAmountParameterName,
                        LoanApiConstants.approvedOnDateParameterName, LoanApiConstants.disbursementNetDisbursalAmountParameterName,
                        LoanApiConstants.noteParameterName, LoanApiConstants.localeParameterName, LoanApiConstants.dateFormatParameterName,
                        LoanApiConstants.disbursementDataParameterName, LoanApiConstants.expectedDisbursementDateParameterName));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanapplication");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final BigDecimal principal = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanApiConstants.approvedLoanAmountParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.approvedLoanAmountParameterName).value(principal).ignoreIfNull()
                .positiveAmount();

        final BigDecimal netDisbursalAmount = this.fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(LoanApiConstants.disbursementNetDisbursalAmountParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.disbursementNetDisbursalAmountParameterName).value(netDisbursalAmount)
                .ignoreIfNull().positiveAmount();

        final LocalDate approvedOnDate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.approvedOnDateParameterName,
                element);
        baseDataValidator.reset().parameter(LoanApiConstants.approvedOnDateParameterName).value(approvedOnDate).notNull();

        final LocalDate expectedDisbursementDate = this.fromApiJsonHelper
                .extractLocalDateNamed(LoanApiConstants.expectedDisbursementDateParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.expectedDisbursementDateParameterName).value(expectedDisbursementDate)
                .ignoreIfNull();

        final String note = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.noteParameterName).value(note).notExceedingLengthOf(1000);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateRejection(final JsonCommand command, final Loan loan) {
        // validate request body
        final String json = command.json();
        validateLoanRejectionRequestBody(json);

        // validate loan rejection
        validateLoanRejection(command, loan);
    }

    private void validateLoanRejectionRequestBody(String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> disbursementParameters = new HashSet<>(Arrays.asList(LoanApiConstants.rejectedOnDateParameterName,
                LoanApiConstants.noteParameterName, LoanApiConstants.localeParameterName, LoanApiConstants.dateFormatParameterName));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanapplication");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate rejectedOnDate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.rejectedOnDateParameterName,
                element);
        baseDataValidator.reset().parameter(LoanApiConstants.rejectedOnDateParameterName).value(rejectedOnDate).notNull();

        final String note = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.noteParameterName).value(note).notExceedingLengthOf(1000);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validateLoanRejection(final JsonCommand command, final Loan loan) {
        // validate client or group is active
        checkClientOrGroupActive(loan);

        // validate Account Status
        validateAccountStatus(loan, LoanEvent.LOAN_REJECTED);

        // validate loan state transition

        final LoanStatus statusEnum = defaultLoanLifecycleStateMachine.dryTransition(LoanEvent.LOAN_REJECTED, loan);
        if (!statusEnum.hasStateOf(LoanStatus.fromInt(loan.getLoanStatus()))) {
            final LocalDate rejectedOn = command.localDateValueOfParameterNamed(Loan.REJECTED_ON_DATE);
            if (DateUtils.isBefore(rejectedOn, loan.getSubmittedOnDate())) {
                final String errorMessage = "The date on which a loan is rejected cannot be before its submittal date: "
                        + loan.getSubmittedOnDate().toString();
                throw new InvalidLoanStateTransitionException("reject", "cannot.be.before.submittal.date", errorMessage, rejectedOn,
                        loan.getSubmittedOnDate());
            }

            validateActivityNotBeforeClientOrGroupTransferDate(loan, LoanEvent.LOAN_REJECTED, rejectedOn);

            if (DateUtils.isDateInTheFuture(rejectedOn)) {
                final String errorMessage = "The date on which a loan is rejected cannot be in the future.";
                throw new InvalidLoanStateTransitionException("reject", "cannot.be.a.future.date", errorMessage, rejectedOn);
            }
        } else {
            final String errorMessage = "Only the loan applications with status 'Submitted and pending approval' are allowed to be rejected.";
            throw new InvalidLoanStateTransitionException("reject", "cannot.reject", errorMessage);
        }
    }

    public void validateApplicantWithdrawal(final JsonCommand command, final Loan loan) {
        // validate request body
        final String json = command.json();
        validateApplicantWithdrawalRequestBody(json);

        // validate Loan application withdrawal by applicant
        validateLoanApplicantWithdrawal(command, loan);
    }

    private void validateApplicantWithdrawalRequestBody(String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> disbursementParameters = new HashSet<>(Arrays.asList(LoanApiConstants.withdrawnOnDateParameterName,
                LoanApiConstants.noteParameterName, LoanApiConstants.localeParameterName, LoanApiConstants.dateFormatParameterName));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, disbursementParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanapplication");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final LocalDate withdrawnOnDate = this.fromApiJsonHelper.extractLocalDateNamed(LoanApiConstants.withdrawnOnDateParameterName,
                element);
        baseDataValidator.reset().parameter(LoanApiConstants.withdrawnOnDateParameterName).value(withdrawnOnDate).notNull();

        final String note = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParameterName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.noteParameterName).value(note).notExceedingLengthOf(1000);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validateLoanApplicantWithdrawal(final JsonCommand command, final Loan loan) {
        // validate client or group is active
        checkClientOrGroupActive(loan);

        // validate loan state transition
        final LoanStatus statusEnum = defaultLoanLifecycleStateMachine.dryTransition(LoanEvent.LOAN_WITHDRAWN, loan);
        if (!statusEnum.hasStateOf(LoanStatus.fromInt(loan.getLoanStatus()))) {
            LocalDate withdrawnOn = command.localDateValueOfParameterNamed(Loan.WITHDRAWN_ON_DATE);
            if (withdrawnOn == null) {
                withdrawnOn = command.localDateValueOfParameterNamed(Loan.EVENT_DATE);
            }
            if (DateUtils.isBefore(withdrawnOn, loan.getSubmittedOnDate())) {
                final String errorMessage = "The date on which a loan is withdrawn cannot be before its submittal date: "
                        + loan.getSubmittedOnDate().toString();
                throw new InvalidLoanStateTransitionException("withdraw", "cannot.be.before.submittal.date", errorMessage, command,
                        loan.getSubmittedOnDate());
            }

            validateActivityNotBeforeClientOrGroupTransferDate(loan, LoanEvent.LOAN_WITHDRAWN, withdrawnOn);

            if (DateUtils.isDateInTheFuture(withdrawnOn)) {
                final String errorMessage = "The date on which a loan is withdrawn cannot be in the future.";
                throw new InvalidLoanStateTransitionException("withdraw", "cannot.be.a.future.date", errorMessage, command);
            }
        } else {
            final String errorMessage = "Only the loan applications with status 'Submitted and pending approval' are allowed to be withdrawn by applicant.";
            throw new InvalidLoanStateTransitionException("withdraw", "cannot.withdraw", errorMessage);
        }

    }

    private void validateActivityNotBeforeClientOrGroupTransferDate(final Loan loan, final LoanEvent event, final LocalDate activityDate) {
        if (loan.getClient() != null && loan.getClient().getOfficeJoiningDate() != null) {
            final LocalDate clientOfficeJoiningDate = loan.getClient().getOfficeJoiningDate();
            if (DateUtils.isBefore(activityDate, clientOfficeJoiningDate)) {
                String errorMessage = null;
                String action = null;
                String postfix = null;
                switch (event) {
                    case LOAN_REJECTED -> {
                        errorMessage = "The date on which a loan is rejected cannot be earlier than client's transfer date to this office";
                        action = "reject";
                        postfix = "cannot.be.before.client.transfer.date";
                    }
                    case LOAN_WITHDRAWN -> {
                        errorMessage = "The date on which a loan is withdrawn cannot be earlier than client's transfer date to this office";
                        action = "withdraw";
                        postfix = "cannot.be.before.client.transfer.date";
                    }
                    default -> {
                    }
                }
                throw new InvalidLoanStateTransitionException(action, postfix, errorMessage, clientOfficeJoiningDate);
            }
        }
    }

    public void validateAccountStatus(final Loan loan, final LoanEvent event) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        switch (event) {
            case LOAN_REJECTED -> {
                if (!loan.isSubmittedAndPendingApproval()) {
                    final String defaultUserMessage = "Loan application cannot be rejected. Loan Account is not in Submitted and Pending approval state.";
                    final ApiParameterError error = ApiParameterError
                            .generalError("error.msg.loan.reject.account.is.not.submitted.pending.approval.state", defaultUserMessage);
                    dataValidationErrors.add(error);
                }
            }
            default -> {
            }
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

    }

    public void checkClientOrGroupActive(final Loan loan) {
        final Client client = loan.client();
        if (client != null && client.isNotActive()) {
            throw new ClientNotActiveException(client.getId());
        }
        final Group group = loan.group();
        if (group != null && group.isNotActive()) {
            throw new GroupNotActiveException(group.getId());
        }
    }
}
