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

package org.apache.fineract.v3.command.service;

import static org.apache.http.HttpStatus.SC_OK;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.commands.domain.CommandProcessingResultType;
import org.apache.fineract.commands.exception.UnsupportedCommandException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.v3.command.data.CommandRequest;
import org.apache.fineract.v3.command.data.CommandResponse;
import org.apache.fineract.v3.command.domain.Command;
import org.apache.fineract.v3.command.domain.CommandRepository;
import org.apache.fineract.v3.command.mapping.CommandMapper;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DefaultCommandProcessor implements CommandProcessor {

    private final ObjectMapper objectMapper;

    private final List<CommandHandler<?, ?>> handlers;

    private final CommandRepository commandRepository;

    private final CommandMapper commandMapper;

    @Override
    public CommandResponse<?> process(CommandRequest<?> request) {

        request.setCreatedDate(DateUtils.getAuditOffsetDateTime());
        Command command = commandMapper.map(request);
        commandRepository.saveAndFlush(command);

        command.setCommandProcessingStatus(CommandProcessingResultType.UNDER_PROCESSING);

        // TODO: handle the ERROR status
        CommandResponse<?> response = handlers.stream().filter(handler -> handler.canHandle(request)) //
                .findFirst() //
                .map(handler -> ((CommandHandler<CommandRequest<?>, CommandResponse<?>>) handler).handle(request)) //
                .orElseThrow(() -> new UnsupportedCommandException("No command handler for: " + request.getClass().getCanonicalName()));

        command.setCommandProcessingStatus(CommandProcessingResultType.PROCESSED);
        command.setResultStatusCode(SC_OK);
        command.setProcessedAt(DateUtils.getAuditOffsetDateTime());
        JsonNode commandResult = objectMapper.valueToTree(response);
        command.setResult(commandResult);

        commandRepository.saveAndFlush(command);
        return response;
    }
}
