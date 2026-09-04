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

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountCharge;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplyAnnualFeeForSavingsBusinessStep implements SavingsCOBBusinessStep {

    private final SavingsAccountWritePlatformService savingsAccountWritePlatformService;

    @Override
    public SavingsAccount execute(SavingsAccount savingsAccount) {
        log.debug("Start ApplyAnnualFeeForSavingsBusinessStep for savings account id [{}]", savingsAccount.getId());
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        for (final SavingsAccountCharge charge : savingsAccount.charges()) {
            if (charge.isAnnualFee() && charge.isActive() && charge.isNotFullyPaid() && charge.getDueDate() != null
                    && !DateUtils.isAfter(charge.getDueDate(), businessDate)) {
                savingsAccountWritePlatformService.applyAnnualFee(charge.getId(), savingsAccount.getId());
            }
        }
        log.debug("End ApplyAnnualFeeForSavingsBusinessStep for savings account id [{}]", savingsAccount.getId());
        return savingsAccount;
    }

    @Override
    public String getEnumStyledName() {
        return "APPLY_ANNUAL_FEE_FOR_SAVINGS";
    }

    @Override
    public String getHumanReadableName() {
        return "Apply annual fee for savings";
    }
}
