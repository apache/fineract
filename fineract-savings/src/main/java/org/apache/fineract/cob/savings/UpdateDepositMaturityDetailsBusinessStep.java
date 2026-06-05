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
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.service.DepositAccountWritePlatformService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateDepositMaturityDetailsBusinessStep implements SavingsCOBBusinessStep {

    private final DepositAccountWritePlatformService depositAccountWritePlatformService;

    @Override
    public SavingsAccount execute(SavingsAccount savingsAccount) {
        log.debug("Start UpdateDepositMaturityDetailsBusinessStep for savings account id [{}]", savingsAccount.getId());
        final DepositAccountType depositAccountType = savingsAccount.depositAccountType();
        // Only active fixed and recurring deposit accounts carry a maturity to update; the COB also feeds
        // non-active accounts (submitted, approved, transfer states) and updateMaturityStatus rejects them.
        if (savingsAccount.isActive() && depositAccountType != null && depositAccountType != DepositAccountType.SAVINGS_DEPOSIT) {
            depositAccountWritePlatformService.updateMaturityDetails(savingsAccount.getId(), depositAccountType);
        }
        log.debug("End UpdateDepositMaturityDetailsBusinessStep for savings account id [{}]", savingsAccount.getId());
        return savingsAccount;
    }

    @Override
    public String getEnumStyledName() {
        return "UPDATE_DEPOSITS_ACCOUNT_MATURITY_DETAILS";
    }

    @Override
    public String getHumanReadableName() {
        return "Update deposit accounts maturity details";
    }
}
