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
package org.apache.fineract.portfolio.account.domain;

import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.amountParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.instructionTypeParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.nameParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.priorityParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceFrequencyParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceIntervalParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceOnMonthDayParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceTypeParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.statusParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.validFromParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.validTillParamName;

import java.math.BigDecimal;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StandingInstructionAssembler {

    private final AccountTransferDetailAssembler accountTransferDetailAssembler;

    @Autowired
    public StandingInstructionAssembler(final AccountTransferDetailAssembler accountTransferDetailAssembler) {

        this.accountTransferDetailAssembler = accountTransferDetailAssembler;
    }

    public AccountTransferDetails assembleSavingsToSavingsTransfer(final JsonCommand command) {
        final AccountTransferDetails accountTransferDetails = this.accountTransferDetailAssembler.assembleSavingsToSavingsTransfer(command);
        assembleStandingInstruction(command, accountTransferDetails);
        return accountTransferDetails;
    }

    public void assembleStandingInstruction(final JsonCommand command, final AccountTransferDetails accountTransferDetails) {
        final LocalDate validFrom = command.localDateValueOfParameterNamed(validFromParamName);
        final LocalDate validTill = command.localDateValueOfParameterNamed(validTillParamName);
        BigDecimal amount = null;
        final BigDecimal transferAmount = command.bigDecimalValueOfParameterNamed(amountParamName);
        if (transferAmount != null) {
            final Money monetaryAmount = Money.of(accountTransferDetails.fromSavingsAccount().getCurrency(), transferAmount);
            amount = monetaryAmount.getAmount();
        }
        final Integer status = command.integerValueOfParameterNamed(statusParamName);
        final Integer priority = command.integerValueOfParameterNamed(priorityParamName);
        final Integer standingInstructionType = command.integerValueOfParameterNamed(instructionTypeParamName);
        final Integer recurrenceType = command.integerValueOfParameterNamed(recurrenceTypeParamName);
        final Integer recurrenceFrequency = command.integerValueOfParameterNamed(recurrenceFrequencyParamName);
        final MonthDay recurrenceOnMonthDay = command.extractMonthDayNamed(recurrenceOnMonthDayParamName);
        final Integer recurrenceInterval = command.integerValueOfParameterNamed(recurrenceIntervalParamName);
        final String name = command.stringValueOfParameterNamed(nameParamName);
        AccountTransferStandingInstruction accountTransferStandingInstruction = AccountTransferStandingInstruction.create(
                accountTransferDetails, name, priority, standingInstructionType, status, amount, validFrom, validTill, recurrenceType,
                recurrenceFrequency, recurrenceInterval, recurrenceOnMonthDay);
        accountTransferDetails.updateAccountTransferStandingInstruction(accountTransferStandingInstruction);
    }

    public AccountTransferDetails assembleSavingsToLoanTransfer(final JsonCommand command) {
        final AccountTransferDetails accountTransferDetails = this.accountTransferDetailAssembler.assembleSavingsToLoanTransfer(command);
        assembleStandingInstruction(command, accountTransferDetails);
        return accountTransferDetails;
    }

    public AccountTransferDetails assembleLoanToSavingsTransfer(final JsonCommand command) {
        final AccountTransferDetails accountTransferDetails = this.accountTransferDetailAssembler.assembleLoanToSavingsTransfer(command);
        assembleStandingInstruction(command, accountTransferDetails);
        return accountTransferDetails;
    }

}