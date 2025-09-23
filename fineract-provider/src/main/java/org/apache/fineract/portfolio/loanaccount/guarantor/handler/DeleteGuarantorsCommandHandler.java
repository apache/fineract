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
package org.apache.fineract.portfolio.loanaccount.guarantor.handler;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHandler;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.DeleteGuarantorsRequest;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.DeleteGuarantorsResponse;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteGuarantorsCommandHandler implements CommandHandler<DeleteGuarantorsRequest, DeleteGuarantorsResponse> {

    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Override
    public DeleteGuarantorsResponse handle(Command<DeleteGuarantorsRequest> command) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteGuarantor(command.getPayload().getLoanId(),
                command.getPayload().getGuarantorId(), command.getPayload().getGuarantorFundingId()).build();

        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return DeleteGuarantorsResponse.builder().commandId(result.getCommandId()).officeId(result.getOfficeId()).loanId(result.getLoanId())
                .resourceId(result.getResourceId()).build();
    }
}
