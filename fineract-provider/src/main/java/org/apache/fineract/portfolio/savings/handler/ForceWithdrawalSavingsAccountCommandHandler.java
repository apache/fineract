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
import org.apache.fineract.commands.domain.SavingsTransactionCommandEnvelope;
import org.apache.fineract.commands.domain.SavingsTransactionExecutionContext;
import org.apache.fineract.commands.domain.SavingsTransactionKind;
import org.apache.fineract.commands.handler.SavingsTransactionCommandHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDataValidator;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsWithdrawalAuthorityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CommandType(entity = "SAVINGSACCOUNT", action = "FORCE_WITHDRAWAL")
@RequiredArgsConstructor
public class ForceWithdrawalSavingsAccountCommandHandler implements SavingsTransactionCommandHandler {

    private final SavingsAccountWritePlatformService writePlatformService;
    private final SavingsWithdrawalAuthorityService authority;
    private final SavingsAccountRepositoryWrapper accounts;
    private final SavingsAccountTransactionDataValidator validator;

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {
        throw SavingsTransactionCommandEnvelope.untrustedOrigin(SavingsTransactionKind.FORCE_WITHDRAWAL);
    }

    @Transactional
    @Override
    public CommandProcessingResult processTransaction(JsonCommand command, SavingsTransactionExecutionContext context) {
        if (context == null) {
            throw SavingsTransactionCommandEnvelope.untrustedOrigin(SavingsTransactionKind.FORCE_WITHDRAWAL);
        }
        context.requireKind(SavingsTransactionKind.FORCE_WITHDRAWAL);
        validator.validate(command);
        authority.require(context, SavingsTransactionKind.FORCE_WITHDRAWAL, accounts.findOneWithNotFoundDetection(command.getSavingsId()),
                command.bigDecimalValueOfParameterNamed("transactionAmount"));
        return this.writePlatformService.forceWithdrawal(command.getSavingsId(), command);
    }
}
