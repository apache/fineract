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
package org.apache.fineract.cob.savings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountSubStatusEnum;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateSavingsDormantStatusBusinessStep implements SavingsCOBBusinessStep {

    private final SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    private final SavingsDormancyService savingsDormancyService;

    @Override
    public SavingsAccount execute(SavingsAccount savingsAccount) {
        log.debug("Start UpdateSavingsDormantStatusBusinessStep for savings account id [{}]", savingsAccount.getId());
        final SavingsAccountSubStatusEnum transition = savingsDormancyService.deriveDormancyTransition(savingsAccount,
                DateUtils.getBusinessLocalDate());
        if (transition != null) {
            final Long savingsId = savingsAccount.getId();
            switch (transition) {
                case INACTIVE -> savingsAccountWritePlatformService.setSubStatusInactive(savingsId);
                case DORMANT -> savingsAccountWritePlatformService.setSubStatusDormant(savingsId);
                case ESCHEAT -> savingsAccountWritePlatformService.escheat(savingsId);
                default -> log.warn("Unexpected dormancy transition [{}] for savings account id [{}]", transition, savingsId);
            }
        }
        log.debug("End UpdateSavingsDormantStatusBusinessStep for savings account id [{}]", savingsAccount.getId());
        return savingsAccount;
    }

    @Override
    public String getEnumStyledName() {
        return "UPDATE_SAVINGS_DORMANT_STATUS";
    }

    @Override
    public String getHumanReadableName() {
        return "Update savings dormant status";
    }
}
