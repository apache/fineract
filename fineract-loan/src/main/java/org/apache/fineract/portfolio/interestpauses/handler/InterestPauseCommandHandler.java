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
package org.apache.fineract.portfolio.interestpauses.handler;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.interestpauses.service.InterestPauseWritePlatformService;
import org.springframework.stereotype.Component;

@Component("interestPauseCommandHandler")
@RequiredArgsConstructor
public class InterestPauseCommandHandler implements NewCommandSourceHandler {

    private final InterestPauseWritePlatformService interestPauseService;

    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {
        CommandProcessingResult result;

        final String startDate = command.stringValueOfParameterNamed("startDate");
        final String endDate = command.stringValueOfParameterNamed("endDate");
        final String dateFormat = command.stringValueOfParameterNamed("dateFormat");
        final String locale = command.stringValueOfParameterNamed("locale");

        if (command.getLoanId() != null) {
            final Long loanId = command.getLoanId();
            result = interestPauseService.createInterestPause(loanId, startDate, endDate, dateFormat, locale);
        } else if (command.getLoanExternalId() != null) {
            final ExternalId loanExternalId = command.getLoanExternalId();
            result = interestPauseService.createInterestPause(loanExternalId, startDate, endDate, dateFormat, locale);
        } else {
            throw new PlatformApiDataValidationException("validation.msg.missing.loan.id.or.external.id",
                    "Either loanId or loanExternalId must be provided.", "loanId");
        }

        return result;
    }
}
