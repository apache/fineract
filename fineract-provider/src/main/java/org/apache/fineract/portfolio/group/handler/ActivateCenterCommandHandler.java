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
package org.apache.fineract.portfolio.group.handler;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.annotation.CommandType;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.portfolio.group.data.GroupActivateRequest;
import org.apache.fineract.portfolio.group.data.GroupActivateResponse;
import org.apache.fineract.portfolio.group.service.GroupingTypesWritePlatformService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CommandType(entity = "CENTER", action = "ACTIVATE")
@RequiredArgsConstructor
public class ActivateCenterCommandHandler implements NewCommandSourceHandler {

    private final GroupingTypesWritePlatformService writePlatformService;

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {
        final GroupActivateRequest request = GroupActivateRequest.builder() //
                .groupId(command.entityId()) //
                .activationDate(command.stringValueOfParameterNamed("activationDate")) //
                .locale(command.locale()) //
                .dateFormat(command.dateFormat()) //
                .build();

        final GroupActivateResponse response = this.writePlatformService.activateGroup(request);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withOfficeId(response.getOfficeId()) //
                .withGroupId(response.getGroupId()) //
                .withEntityId(response.getResourceId()) //
                .build();
    }
}
