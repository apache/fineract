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
package org.apache.fineract.portfolio.creditscorecard.handler;

import org.apache.fineract.commands.annotation.CommandType;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.apache.fineract.portfolio.creditscorecard.provider.ScorecardServiceProvider;
import org.apache.fineract.portfolio.creditscorecard.service.CreditScorecardWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CommandType(entity = "CREDIT_SCORECARD_FEATURE", action = "CREATE")
public class CreateCreditScorecardFeatureDefinitionCommandHandler implements NewCommandSourceHandler {

    private final ScorecardServiceProvider serviceProvider;

    @Autowired
    public CreateCreditScorecardFeatureDefinitionCommandHandler(final ScorecardServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {
        final String serviceName = "CreditScorecardWritePlatformService";
        final CreditScorecardWritePlatformService scorecardWritePlatformService = (CreditScorecardWritePlatformService) this.serviceProvider
                .getScorecardService(serviceName);
        if (scorecardWritePlatformService == null) {
            throw new PlatformServiceUnavailableException("err.msg.credit.scorecard.service.implementation.missing",
                    ScorecardServiceProvider.SERVICE_MISSING + serviceName, serviceName);
        }

        return scorecardWritePlatformService.createScoringFeature(command);
    }
}
