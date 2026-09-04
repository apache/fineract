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
import org.apache.fineract.infrastructure.core.exception.MultiException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.service.SavingsAccrualWritePlatformService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AddAccrualForSavingsBusinessStep implements SavingsCOBBusinessStep {

    private final SavingsAccrualWritePlatformService savingsAccrualWritePlatformService;

    @Override
    public SavingsAccount execute(SavingsAccount savingsAccount) {
        log.debug("Start AddAccrualForSavingsBusinessStep for savings account id [{}]", savingsAccount.getId());
        try {
            savingsAccrualWritePlatformService.addAccrualEntries(savingsAccount, DateUtils.getBusinessLocalDate());
        } catch (MultiException e) {
            throw new RuntimeException(
                    "Failed to add accrual transactions for savings account id [" + savingsAccount.getId() + "]: " + e.getMessage(), e);
        }
        log.debug("End AddAccrualForSavingsBusinessStep for savings account id [{}]", savingsAccount.getId());
        return savingsAccount;
    }

    @Override
    public String getEnumStyledName() {
        return "ADD_ACCRUAL_TRANSACTIONS_FOR_SAVINGS";
    }

    @Override
    public String getHumanReadableName() {
        return "Add accrual transactions for savings";
    }
}
