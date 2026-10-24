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
package org.apache.fineract.portfolio.accounts.api.v2;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountsV2ApiDelegate implements AccountsV2Api {

    private final PlatformSecurityContext platformSecurityContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Override
    public CommandProcessingResult redeemShares(String accountType, Long accountId, String apiRequestBodyAsJson) {
        this.platformSecurityContext.authenticatedUser();
        CommandWrapper commandWrapper = new CommandWrapperBuilder().createAccountCommand(accountType, accountId, "redeemshares")
                .withJson(apiRequestBodyAsJson).build();
        return this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
    }

    @Override
    public CommandProcessingResult applyAdditionalShares(String accountType, Long accountId, String apiRequestBodyAsJson) {
        this.platformSecurityContext.authenticatedUser();
        CommandWrapper commandWrapper = new CommandWrapperBuilder().createAccountCommand(accountType, accountId, "applyadditionalshares")
                .withJson(apiRequestBodyAsJson).build();
        return this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
    }

    @Override
    public CommandProcessingResult approveAdditionalShares(String accountType, Long accountId, String apiRequestBodyAsJson) {
        this.platformSecurityContext.authenticatedUser();
        CommandWrapper commandWrapper = new CommandWrapperBuilder().createAccountCommand(accountType, accountId, "approveadditionalshares")
                .withJson(apiRequestBodyAsJson).build();
        return this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
    }

    @Override
    public CommandProcessingResult rejectAdditionalShares(String accountType, Long accountId, String apiRequestBodyAsJson) {
        this.platformSecurityContext.authenticatedUser();
        CommandWrapper commandWrapper = new CommandWrapperBuilder().createAccountCommand(accountType, accountId, "rejectadditionalshares")
                .withJson(apiRequestBodyAsJson).build();
        return this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
    }
}
