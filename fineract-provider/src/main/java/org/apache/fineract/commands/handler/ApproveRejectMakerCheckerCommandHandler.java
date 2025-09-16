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
package org.apache.fineract.commands.handler;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHandler;
import org.apache.fineract.commands.data.ApproveRejectMakerCheckerRequest;
import org.apache.fineract.commands.data.ApproveRejectMakerCheckerResponse;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ApproveRejectMakerCheckerCommandHandler
        implements CommandHandler<ApproveRejectMakerCheckerRequest, ApproveRejectMakerCheckerResponse> {

    private final PortfolioCommandSourceWritePlatformService writePlatformService;

    @Transactional
    @Override
    public ApproveRejectMakerCheckerResponse handle(Command<ApproveRejectMakerCheckerRequest> command) {

        if (command.getPayload().getCommandParam().equalsIgnoreCase("approve")) {
            CommandProcessingResult response = writePlatformService.approveEntry(command.getPayload().getAuditId());
            return ApproveRejectMakerCheckerResponse.builder().commandId(response.getCommandId()).officeId(response.getOfficeId())
                    .clientId(response.getClientId()).resourceId(response.getResourceId()).build();
        } else if (command.getPayload().getCommandParam().equalsIgnoreCase("reject")) {
            final Long id = writePlatformService.rejectEntry(command.getPayload().getAuditId());
            return ApproveRejectMakerCheckerResponse.builder().commandId(id).build();
        } else {
            throw new UnrecognizedQueryParamException("command", command.getPayload().getCommandParam());
        }
    }
}
