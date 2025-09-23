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

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.batch.command.CommandHandlerRegistry;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHandler;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.CommandParameterUtil;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.account.data.StandingInstructionUpdateRequest;
import org.apache.fineract.portfolio.account.data.StandingInstructionUpdateResponse;
import org.apache.fineract.portfolio.account.mapper.StandingInstructionUpdateRequestMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StandingInstructionUpdateCommandHandler
        implements CommandHandler<StandingInstructionUpdateRequest, StandingInstructionUpdateResponse> {

    private static final CommandHandlerRegistry<String, Long, String, CommandWrapper> COMMAND_HANDLER_REGISTRY = new CommandHandlerRegistry<>(
            Map.of(CommandParameterUtil.UPDATE_COMMAND_VALUE,
                    (id, json) -> new CommandWrapperBuilder().updateStandingInstruction(id).withJson(json).build(),
                    CommandParameterUtil.DELETE_COMMAND_VALUE,
                    (id, json) -> new CommandWrapperBuilder().deleteStandingInstruction(id).withJson(json).build()));

    private final DefaultToApiJsonSerializer<AccountTransferData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final StandingInstructionUpdateRequestMapper mapper;

    @Override
    public StandingInstructionUpdateResponse handle(Command<StandingInstructionUpdateRequest> command) {
        final String commandParam = command.getPayload().getCommandParam();

        final StandingInstructionUpdateRequest payload = command.getPayload();
        final StandingInstructionUpdateRequest serviceRequest = mapper.toServiceRequest(payload);

        final String serializedUpdatesRequest = toApiJsonSerializer.serialize(serviceRequest);
        final CommandWrapper commandRequest = COMMAND_HANDLER_REGISTRY.execute(commandParam,
                command.getPayload().getStandingInstructionId(), serializedUpdatesRequest,
                new UnrecognizedQueryParamException("command", commandParam));

        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return new StandingInstructionUpdateResponse(result.getResourceId(), result.getChanges());
    }
}
