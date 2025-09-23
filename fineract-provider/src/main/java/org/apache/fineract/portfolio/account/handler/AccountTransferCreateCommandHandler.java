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
package org.apache.fineract.portfolio.account.handler;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHandler;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.account.data.AccountTransferRequest;
import org.apache.fineract.portfolio.account.data.AccountTransferResponse;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountTransferCreateCommandHandler implements CommandHandler<AccountTransferRequest, AccountTransferResponse> {

    private final DefaultToApiJsonSerializer<AccountTransferData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Override
    public AccountTransferResponse handle(Command<AccountTransferRequest> command) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createAccountTransfer()
                .withJson(toApiJsonSerializer.serialize(command.getPayload())).build();

        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return new AccountTransferResponse(result.getSavingsId(), result.getLoanId(), result.getResourceId());
    }
}
