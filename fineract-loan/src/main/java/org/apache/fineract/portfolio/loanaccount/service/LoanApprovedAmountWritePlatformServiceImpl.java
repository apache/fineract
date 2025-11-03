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
package org.apache.fineract.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanApprovedAmountChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanApprovedAmountHistory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanApprovedAmountHistoryRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanApprovedAmountValidator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanApprovedAmountWritePlatformServiceImpl implements LoanApprovedAmountWritePlatformService {

    private final LoanAssembler loanAssembler;
    private final LoanApprovedAmountValidator loanApprovedAmountValidator;
    private final LoanApprovedAmountHistoryRepository loanApprovedAmountHistoryRepository;
    private final BusinessEventNotifierService businessEventNotifierService;

    @Override
    public CommandProcessingResult modifyLoanApprovedAmount(final Long loanId, final JsonCommand command) {
        // API rule validations
        this.loanApprovedAmountValidator.validateLoanApprovedAmountModification(command);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("newApprovedAmount", command.stringValueOfParameterNamed(LoanApiConstants.amountParameterName));
        changes.put("locale", command.locale());

        Loan loan = this.loanAssembler.assembleFrom(loanId);
        changes.put("oldApprovedAmount", loan.getApprovedPrincipal());

        BigDecimal newApprovedAmount = command.bigDecimalValueOfParameterNamed(LoanApiConstants.amountParameterName);

        LoanApprovedAmountHistory loanApprovedAmountHistory = new LoanApprovedAmountHistory(loan.getId(), newApprovedAmount,
                loan.getApprovedPrincipal());

        loan.setApprovedPrincipal(newApprovedAmount);
        loanApprovedAmountHistoryRepository.saveAndFlush(loanApprovedAmountHistory);

        businessEventNotifierService.notifyPostBusinessEvent(new LoanApprovedAmountChangedBusinessEvent(loan));
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()) //
                .withEntityId(loan.getId()) //
                .withEntityExternalId(loan.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .with(changes) //
                .build();
    }

    @Override
    public CommandProcessingResult modifyLoanAvailableDisbursementAmount(Long loanId, JsonCommand command) {
        // API rule validations
        this.loanApprovedAmountValidator.validateLoanAvailableDisbursementAmountModification(command);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("newAvailableDisbursementAmount", command.stringValueOfParameterNamed(LoanApiConstants.amountParameterName));
        changes.put("locale", command.locale());

        Loan loan = this.loanAssembler.assembleFrom(loanId);
        changes.put("oldApprovedAmount", loan.getApprovedPrincipal());

        BigDecimal expectedDisbursementAmount = loan.getDisbursementDetails().stream().filter(t -> t.actualDisbursementDate() == null)
                .map(LoanDisbursementDetails::principal).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal oldAvailableDisbursement = loan.getApprovedPrincipal().subtract(loan.getSummary().getTotalPrincipal())
                .subtract(expectedDisbursementAmount);
        changes.put("oldAvailableDisbursementAmount", oldAvailableDisbursement);

        BigDecimal newAvailableDisbursementAmount = command.bigDecimalValueOfParameterNamed(LoanApiConstants.amountParameterName);
        BigDecimal newApprovedAmount = loan.getSummary().getTotalPrincipal().add(expectedDisbursementAmount)
                .add(newAvailableDisbursementAmount);
        changes.put("newApprovedAmount", newApprovedAmount);

        LoanApprovedAmountHistory loanApprovedAmountHistory = new LoanApprovedAmountHistory(loan.getId(), newApprovedAmount,
                loan.getApprovedPrincipal());

        loan.setApprovedPrincipal(newApprovedAmount);
        loanApprovedAmountHistoryRepository.saveAndFlush(loanApprovedAmountHistory);

        businessEventNotifierService.notifyPostBusinessEvent(new LoanApprovedAmountChangedBusinessEvent(loan));
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()) //
                .withEntityId(loan.getId()) //
                .withEntityExternalId(loan.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .with(changes) //
                .build();
    }
}
