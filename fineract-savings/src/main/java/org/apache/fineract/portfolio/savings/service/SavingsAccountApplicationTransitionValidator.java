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
import java.util.List;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;

/**
 * Checks a request to approve, reject or withdraw a savings application before the account is changed.
 */
public final class SavingsAccountApplicationTransitionValidator {

    public void validateApproval(final SavingsAccount account, final JsonCommand command) {
        validate(account, command, SavingsApiConstants.approvalAction, SavingsApiConstants.approvedOnDateParamName, false);
    }

    public void validateRejection(final SavingsAccount account, final JsonCommand command) {
        validate(account, command, SavingsApiConstants.rejectAction, SavingsApiConstants.rejectedOnDateParamName, true);
    }

    public void validateApplicantWithdrawal(final SavingsAccount account, final JsonCommand command) {
        validate(account, command, SavingsApiConstants.withdrawnByApplicantAction, SavingsApiConstants.withdrawnOnDateParamName, true);
    }

    private void validate(final SavingsAccount account, final JsonCommand command, final String action, final String dateParameter,
            final boolean futureDateErrorHasValue) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + action);

        if (!SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.hasStateOf(account.getStatus())) {
            baseDataValidator.reset().parameter(dateParameter)
                    .failWithCodeNoParameterAddedToErrorCode("not.in.submittedandpendingapproval.state");
            throwIfInvalid(dataValidationErrors);
        }

        final LocalDate date = command.localDateValueOfParameterNamed(dateParameter);

        final LocalDate submittalDate = account.getSubmittedOnDate();
        if (DateUtils.isBefore(date, submittalDate)) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(command.extractLocale());
            final String submittalDateAsString = formatter.format(submittalDate);

            baseDataValidator.reset().parameter(dateParameter).value(submittalDateAsString)
                    .failWithCodeNoParameterAddedToErrorCode("cannot.be.before.submittal.date");
            throwIfInvalid(dataValidationErrors);
        }
        if (DateUtils.isAfterBusinessDate(date)) {
            final DataValidatorBuilder futureDateValidator = baseDataValidator.reset().parameter(dateParameter);
            if (futureDateErrorHasValue) {
                futureDateValidator.value(date);
            }
            futureDateValidator.failWithCodeNoParameterAddedToErrorCode("cannot.be.a.future.date");
            throwIfInvalid(dataValidationErrors);
        }
    }

    private void throwIfInvalid(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
