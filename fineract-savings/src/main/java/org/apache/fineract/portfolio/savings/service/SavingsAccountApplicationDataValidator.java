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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.savings.DepositsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.domain.RecurringDepositAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.apache.fineract.portfolio.savings.domain.SavingsEvent;

/**
 * Validates the domain rules that guard the application-lifecycle status transitions of a {@link SavingsAccount}. The
 * checks were extracted from the entity's transition methods so the validation lives in one place and out of both the
 * JPA entity and the orchestrating service.
 */
public class SavingsAccountApplicationDataValidator {

    public void validateActivation(final SavingsAccount account, final JsonCommand command, final LocalDate activationDate) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(account.depositAccountType().resourceName() + SavingsApiConstants.activateAction);

        final SavingsAccountStatusType currentStatus = account.getStatus();
        if (!SavingsAccountStatusType.APPROVED.hasStateOf(currentStatus)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.activatedOnDateParamName)
                    .failWithCodeNoParameterAddedToErrorCode("not.in.approved.state");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (account.getClient() != null && account.getClient().isActivatedAfter(activationDate)) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String dateAsString = formatter.format(account.getClient().getActivationDate());
            baseDataValidator.reset().parameter(SavingsApiConstants.activatedOnDateParamName).value(dateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.client.activation.date");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (account.getGroup() != null && account.getGroup().isActivatedAfter(activationDate)) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String dateAsString = formatter.format(account.getClient().getActivationDate());
            baseDataValidator.reset().parameter(SavingsApiConstants.activatedOnDateParamName).value(dateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.group.activation.date");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        final LocalDate approvalDate = account.getApprovedOnDate();
        if (DateUtils.isBefore(activationDate, approvalDate)) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String dateAsString = formatter.format(approvalDate);
            baseDataValidator.reset().parameter(SavingsApiConstants.activatedOnDateParamName).value(dateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.approval.date");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (DateUtils.isAfterBusinessDate(activationDate)) {
            baseDataValidator.reset().parameter(SavingsApiConstants.activatedOnDateParamName).value(activationDate)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.a.future.date");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        account.validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_ACTIVATE, activationDate);

        if (account instanceof RecurringDepositAccount recurringDepositAccount
                && recurringDepositAccount.getAccountTermAndPreClosure().isAfterExpectedFirstDepositDate(activationDate)) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String dateAsString = formatter
                    .format(recurringDepositAccount.getAccountTermAndPreClosure().getExpectedFirstDepositOnDate());
            baseDataValidator.reset().parameter(DepositsApiConstants.activatedOnDateParamName).value(dateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.expected.first.deposit.date");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
    }
}
