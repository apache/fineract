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
package org.apache.fineract.portfolio.charge.handler;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHandler;
import org.apache.fineract.portfolio.charge.data.ChargeCreateRequest;
import org.apache.fineract.portfolio.charge.data.ChargeCreateResponse;
import org.apache.fineract.portfolio.charge.service.ChargeWriteService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChargeCreateCommandHandler implements CommandHandler<ChargeCreateRequest, ChargeCreateResponse> {

    private final ChargeWriteService writeService;

    @Retry(name = "commandChargeCreate", fallbackMethod = "fallback")
    @Override
    @Transactional
    public ChargeCreateResponse handle(Command<ChargeCreateRequest> command) {
        return writeService.create(command.getPayload());
    }

    @Override
    public ChargeCreateResponse fallback(Command<ChargeCreateRequest> command, Throwable t) {
        // NOTE: fallback method needs to be in the same class
        return CommandHandler.super.fallback(command, t);
    }
}
