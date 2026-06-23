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
package org.apache.fineract.portfolio.savings.service;

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.apache.fineract.portfolio.savings.domain.SavingsEvent;
import org.apache.fineract.portfolio.savings.domain.SavingsOfficerAssignmentHistory;
import org.apache.fineract.useradministration.domain.AppUser;

/**
 * Default implementation of {@link SavingsAccountApplicationService}. The method bodies were extracted from
 * {@code SavingsAccount}; behaviour is intentionally unchanged.
 */
@RequiredArgsConstructor
public class SavingsAccountApplicationServiceImpl implements SavingsAccountApplicationService {

    private final SavingsAccountApplicationDataValidator savingsAccountApplicationDataValidator;

    @Override
    public Map<String, Object> approveApplication(final SavingsAccount account, final AppUser currentUser, final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.approvalAction);

        final SavingsAccountStatusType currentStatus = account.getStatus();
        if (!SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.hasStateOf(currentStatus)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.approvedOnDateParamName)
                    .failWithCodeNoParameterAddedToErrorCode("not.in.submittedandpendingapproval.state");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        account.setStatus(SavingsAccountStatusType.APPROVED.getValue());
        actualChanges.put(SavingsApiConstants.statusParamName, SavingsEnumerations.status(SavingsAccountStatusType.APPROVED.getValue()));

        // only do below if status has changed in the 'approval' case
        final LocalDate approvedOn = command.localDateValueOfParameterNamed(SavingsApiConstants.approvedOnDateParamName);
        final String approvedOnDateChange = command.stringValueOfParameterNamed(SavingsApiConstants.approvedOnDateParamName);

        account.setApprovedOnDate(approvedOn);
        account.setApprovedBy(currentUser);
        actualChanges.put(SavingsApiConstants.localeParamName, command.locale());
        actualChanges.put(SavingsApiConstants.dateFormatParamName, command.dateFormat());
        actualChanges.put(SavingsApiConstants.approvedOnDateParamName, approvedOnDateChange);

        final LocalDate submittalDate = account.getSubmittedOnDate();
        if (DateUtils.isBefore(approvedOn, submittalDate)) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String submittalDateAsString = formatter.format(submittalDate);

            baseDataValidator.reset().parameter(SavingsApiConstants.approvedOnDateParamName).value(submittalDateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.submittal.date");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
        if (DateUtils.isAfterBusinessDate(approvedOn)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.approvedOnDateParamName)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.a.future.date");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
        account.validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_APPLICATION_APPROVED, approvedOn);

        if (account.getSavingsOfficer() != null) {
            final SavingsOfficerAssignmentHistory savingsOfficerAssignmentHistory = SavingsOfficerAssignmentHistory.createNew(account,
                    account.getSavingsOfficer(), approvedOn);
            account.getSavingsOfficerHistory().add(savingsOfficerAssignmentHistory);
        }
        return actualChanges;
    }

    @Override
    public Map<String, Object> undoApplicationApproval(final SavingsAccount account) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.undoApprovalAction);

        final SavingsAccountStatusType currentStatus = account.getStatus();
        if (!SavingsAccountStatusType.APPROVED.hasStateOf(currentStatus)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.approvedOnDateParamName)
                    .failWithCodeNoParameterAddedToErrorCode("not.in.approved.state");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        account.setStatus(SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getValue());
        actualChanges.put(SavingsApiConstants.statusParamName,
                SavingsEnumerations.status(SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getValue()));

        account.setApprovedOnDate(null);
        account.setApprovedBy(null);
        account.setRejectedOnDate(null);
        account.setRejectedBy(null);
        account.setWithdrawnOnDate(null);
        account.setWithdrawnBy(null);
        account.setClosedOnDate(null);
        account.setClosedBy(null);
        actualChanges.put(SavingsApiConstants.approvedOnDateParamName, "");

        return actualChanges;
    }

    @Override
    public Map<String, Object> rejectApplication(final SavingsAccount account, final AppUser currentUser, final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.rejectAction);

        final SavingsAccountStatusType currentStatus = account.getStatus();
        if (!SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.hasStateOf(currentStatus)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.rejectedOnDateParamName)
                    .failWithCodeNoParameterAddedToErrorCode("not.in.submittedandpendingapproval.state");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        account.setStatus(SavingsAccountStatusType.REJECTED.getValue());
        actualChanges.put(SavingsApiConstants.statusParamName, SavingsEnumerations.status(SavingsAccountStatusType.REJECTED.getValue()));

        final LocalDate rejectedOn = command.localDateValueOfParameterNamed(SavingsApiConstants.rejectedOnDateParamName);
        final String rejectedOnAsString = command.stringValueOfParameterNamed(SavingsApiConstants.rejectedOnDateParamName);

        account.setRejectedOnDate(rejectedOn);
        account.setRejectedBy(currentUser);
        account.setWithdrawnOnDate(null);
        account.setWithdrawnBy(null);
        account.setClosedOnDate(rejectedOn);
        account.setClosedBy(currentUser);

        actualChanges.put(SavingsApiConstants.localeParamName, command.locale());
        actualChanges.put(SavingsApiConstants.dateFormatParamName, command.dateFormat());
        actualChanges.put(SavingsApiConstants.rejectedOnDateParamName, rejectedOnAsString);
        actualChanges.put(SavingsApiConstants.closedOnDateParamName, rejectedOnAsString);

        final LocalDate submittalDate = account.getSubmittedOnDate();
        if (DateUtils.isBefore(rejectedOn, submittalDate)) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String submittalDateAsString = formatter.format(submittalDate);

            baseDataValidator.reset().parameter(SavingsApiConstants.rejectedOnDateParamName).value(submittalDateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.submittal.date");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
        if (DateUtils.isAfterBusinessDate(rejectedOn)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.rejectedOnDateParamName).value(rejectedOn)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.a.future.date");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
        account.validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_APPLICATION_REJECTED, rejectedOn);

        return actualChanges;
    }

    @Override
    public Map<String, Object> applicantWithdrawsFromApplication(final SavingsAccount account, final AppUser currentUser,
            final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.withdrawnByApplicantAction);

        final SavingsAccountStatusType currentStatus = account.getStatus();
        if (!SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.hasStateOf(currentStatus)) {

            baseDataValidator.reset().parameter(SavingsApiConstants.withdrawnOnDateParamName)
                    .failWithCodeNoParameterAddedToErrorCode("not.in.submittedandpendingapproval.state");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        account.setStatus(SavingsAccountStatusType.WITHDRAWN_BY_APPLICANT.getValue());
        actualChanges.put(SavingsApiConstants.statusParamName,
                SavingsEnumerations.status(SavingsAccountStatusType.WITHDRAWN_BY_APPLICANT.getValue()));

        final LocalDate withdrawnOn = command.localDateValueOfParameterNamed(SavingsApiConstants.withdrawnOnDateParamName);
        final String withdrawnOnAsString = command.stringValueOfParameterNamed(SavingsApiConstants.withdrawnOnDateParamName);

        account.setRejectedOnDate(null);
        account.setRejectedBy(null);
        account.setWithdrawnOnDate(withdrawnOn);
        account.setWithdrawnBy(currentUser);
        account.setClosedOnDate(withdrawnOn);
        account.setClosedBy(currentUser);

        actualChanges.put(SavingsApiConstants.localeParamName, command.locale());
        actualChanges.put(SavingsApiConstants.dateFormatParamName, command.dateFormat());
        actualChanges.put(SavingsApiConstants.withdrawnOnDateParamName, withdrawnOnAsString);
        actualChanges.put(SavingsApiConstants.closedOnDateParamName, withdrawnOnAsString);

        final LocalDate submittalDate = account.getSubmittedOnDate();
        if (DateUtils.isBefore(withdrawnOn, submittalDate)) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String submittalDateAsString = formatter.format(submittalDate);

            baseDataValidator.reset().parameter(SavingsApiConstants.withdrawnOnDateParamName).value(submittalDateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.submittal.date");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
        if (DateUtils.isAfterBusinessDate(withdrawnOn)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.withdrawnOnDateParamName).value(withdrawnOn)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.a.future.date");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
        account.validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_APPLICATION_WITHDRAWAL_BY_CUSTOMER, withdrawnOn);

        return actualChanges;
    }

    @Override
    public Map<String, Object> activate(final SavingsAccount account, final AppUser currentUser, final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        final LocalDate activationDate = command.localDateValueOfParameterNamed(SavingsApiConstants.activatedOnDateParamName);

        this.savingsAccountApplicationDataValidator.validateActivation(account, command, activationDate);

        account.setStatus(SavingsAccountStatusType.ACTIVE.getValue());
        actualChanges.put(SavingsApiConstants.statusParamName, SavingsEnumerations.status(SavingsAccountStatusType.ACTIVE.getValue()));
        actualChanges.put(SavingsApiConstants.localeParamName, command.locale());
        actualChanges.put(SavingsApiConstants.dateFormatParamName, command.dateFormat());
        actualChanges.put(SavingsApiConstants.activatedOnDateParamName, activationDate.format(fmt));

        account.setRejectedOnDate(null);
        account.setRejectedBy(null);
        account.setWithdrawnOnDate(null);
        account.setWithdrawnBy(null);
        account.setClosedOnDate(null);
        account.setClosedBy(null);
        account.setActivatedOnDate(activationDate);
        account.setActivatedBy(currentUser);
        account.setLockedInUntilDate(account.calculateDateAccountIsLockedUntil(activationDate));

        return actualChanges;
    }
}
