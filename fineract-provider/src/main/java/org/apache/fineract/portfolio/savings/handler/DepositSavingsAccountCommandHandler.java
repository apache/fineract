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
package org.apache.fineract.portfolio.savings.handler;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.annotation.CommandType;
import org.apache.fineract.commands.domain.SavingsDepositCommandEnvelope;
import org.apache.fineract.commands.domain.SavingsDepositExecutionContext;
import org.apache.fineract.commands.domain.SavingsDepositOrigin;
import org.apache.fineract.commands.handler.SavingsDepositCommandHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.nsimbi.userroles.domain.MonetaryAuthorityType;
import org.apache.fineract.nsimbi.userroles.service.NsimbiMonetaryAuthorityPolicyService;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDataValidator;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CommandType(entity = "SAVINGSACCOUNT", action = "DEPOSIT")
@RequiredArgsConstructor
public class DepositSavingsAccountCommandHandler implements SavingsDepositCommandHandler {

    private final SavingsAccountWritePlatformService writePlatformService;

    private final NsimbiMonetaryAuthorityPolicyService monetaryAuthority;
    private final SavingsAccountRepositoryWrapper accounts;
    private final SavingsAccountTransactionDataValidator validator;

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {
        throw SavingsDepositCommandEnvelope.untrustedOrigin();
    }

    @Transactional
    @Override
    public CommandProcessingResult processDeposit(JsonCommand command, SavingsDepositExecutionContext context) {
        if (context == null || context.origin() == null || context.maker() == null) {
            throw SavingsDepositCommandEnvelope.untrustedOrigin();
        }
        if (context.origin() != SavingsDepositOrigin.SPREADSHEET_IMPORT) {
            if (context.origin() != SavingsDepositOrigin.STAFF_API) {
                throw SavingsDepositCommandEnvelope.untrustedOrigin();
            }
            validator.validate(command);
            String currency = accounts.findOneWithNotFoundDetection(command.getSavingsId()).getCurrency().getCode();
            if (!monetaryAuthority.allows(context.maker().getId(), MonetaryAuthorityType.DEPOSITS, currency,
                    command.bigDecimalValueOfParameterNamed("transactionAmount"))) {
                throw new GeneralPlatformDomainRuleException("error.msg.savings.deposit.monetary.authority.denied",
                        "The original submitter does not have DEPOSITS monetary authority for this amount and account currency.");
            }
        }
        return this.writePlatformService.deposit(command.getSavingsId(), command);
    }
}
