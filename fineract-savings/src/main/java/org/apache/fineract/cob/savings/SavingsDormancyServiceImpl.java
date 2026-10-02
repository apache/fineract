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
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountSubStatusEnum;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SavingsDormancyServiceImpl implements SavingsDormancyService {

    // Only customer-initiated activity resets the dormancy clock, as in the legacy UPDATE_SAVINGS_DORMANT_ACCOUNTS job.
    private static final List<Integer> ACTIVITY_TRANSACTION_TYPES = List.of(SavingsAccountTransactionType.DEPOSIT.getValue(),
            SavingsAccountTransactionType.WITHDRAWAL.getValue());

    private final SavingsAccountTransactionRepository savingsAccountTransactionRepository;

    @Override
    public SavingsAccountSubStatusEnum deriveDormancyTransition(final SavingsAccount savingsAccount, final LocalDate businessDate) {
        final SavingsProduct product = savingsAccount.savingsProduct();
        if (!savingsAccount.isActive() || product == null || !product.isDormancyTrackingActive()) {
            return null;
        }
        final long daysSinceLastActivity = ChronoUnit.DAYS.between(retrieveLastActiveTransactionDate(savingsAccount), businessDate);
        final SavingsAccountSubStatusEnum currentSubStatus = SavingsAccountSubStatusEnum.fromInt(savingsAccount.getSubStatus());
        if (SavingsAccountSubStatusEnum.NONE.hasStateOf(currentSubStatus) && product.getDaysToInactive() != null
                && daysSinceLastActivity >= product.getDaysToInactive()) {
            return SavingsAccountSubStatusEnum.INACTIVE;
        }
        if (SavingsAccountSubStatusEnum.INACTIVE.hasStateOf(currentSubStatus) && product.getDaysToDormancy() != null
                && daysSinceLastActivity >= product.getDaysToDormancy()) {
            return SavingsAccountSubStatusEnum.DORMANT;
        }
        if (SavingsAccountSubStatusEnum.DORMANT.hasStateOf(currentSubStatus) && product.getDaysToEscheat() != null
                && daysSinceLastActivity >= product.getDaysToEscheat()) {
            return SavingsAccountSubStatusEnum.ESCHEAT;
        }
        return null;
    }

    private LocalDate retrieveLastActiveTransactionDate(final SavingsAccount savingsAccount) {
        final LocalDate activationDate = savingsAccount.getActivationDate();
        final LocalDate lastTransactionDate = savingsAccountTransactionRepository
                .findLastTransactionDateBySavingsIdAndTypes(savingsAccount.getId(), ACTIVITY_TRANSACTION_TYPES);
        if (lastTransactionDate == null || (activationDate != null && !DateUtils.isAfter(lastTransactionDate, activationDate))) {
            return activationDate;
        }
        return lastTransactionDate;
    }
}
