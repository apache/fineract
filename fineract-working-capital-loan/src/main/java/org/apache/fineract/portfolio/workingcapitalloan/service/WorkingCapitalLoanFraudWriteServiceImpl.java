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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkingCapitalLoanFraudWriteServiceImpl implements WorkingCapitalLoanFraudWriteService {

    private final WorkingCapitalLoanRepository loanRepository;
    private final FromJsonHelper fromApiJsonHelper;

    @Transactional
    @Override
    public CommandProcessingResult markAsFraud(final Long loanId, final JsonCommand command) {
        final boolean fraud = validateAndExtractFraud(command);

        final WorkingCapitalLoan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));

        // A loan can only be marked as fraud while it is active. Clearing the flag and idempotent re-marks are
        // allowed regardless of status.
        final boolean markingAsFraud = fraud && !loan.isFraud();
        if (markingAsFraud && loan.getLoanStatus() != LoanStatus.ACTIVE) {
            throw new PlatformApiDataValidationException("validation.msg.wc.loan.mark.as.fraud.not.allowed",
                    "Marking a loan as fraud is allowed only for active loans", "loanStatus");
        }

        final Map<String, Object> changes = new LinkedHashMap<>();
        if (loan.isFraud() != fraud) {
            loan.setFraud(fraud);
            this.loanRepository.saveAndFlush(loan);
            changes.put(WorkingCapitalLoanConstants.fraudParamName, fraud);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loan.getId()) //
                .withEntityExternalId(loan.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withLoanId(loan.getId()) //
                .with(changes) //
                .build();
    }

    private boolean validateAndExtractFraud(final JsonCommand command) {
        final String json = command.json();
        final Set<String> supportedParameters = Set.of(WorkingCapitalLoanConstants.fraudParamName);
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final Boolean fraud = this.fromApiJsonHelper.extractBooleanNamed(WorkingCapitalLoanConstants.fraudParamName, element);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        new DataValidatorBuilder(dataValidationErrors).resource("workingcapitalloan.markasfraud")
                .parameter(WorkingCapitalLoanConstants.fraudParamName).value(fraud).notNull();
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        return fraud;
    }
}
