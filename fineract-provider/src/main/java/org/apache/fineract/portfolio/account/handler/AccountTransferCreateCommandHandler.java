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

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHandler;
import org.apache.fineract.portfolio.account.command.AccountTransferCreateCommand;
import org.apache.fineract.portfolio.account.data.AccountTransferCreateRequest;
import org.apache.fineract.portfolio.account.data.AccountTransferCreateResponse;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountTransferCreateCommandHandler implements CommandHandler<AccountTransferCreateRequest, AccountTransferCreateResponse> {

    private final AccountTransfersWritePlatformService writePlatformService;

    @Override
    public boolean matches(Command<AccountTransferCreateRequest> command) {
        return command instanceof AccountTransferCreateCommand;
    }

    @Retry(name = "commandAccountTransferCreate", fallbackMethod = "fallback")
    @Override
    @Transactional
    public AccountTransferCreateResponse handle(Command<AccountTransferCreateRequest> command) {
        return writePlatformService.create(command.getPayload());
    }

    @Override
    public AccountTransferCreateResponse fallback(Command<AccountTransferCreateRequest> command, Throwable t) {
        return CommandHandler.super.fallback(command, t);
    }
}
