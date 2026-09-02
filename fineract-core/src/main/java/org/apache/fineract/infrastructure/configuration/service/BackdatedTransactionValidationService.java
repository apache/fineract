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
package org.apache.fineract.infrastructure.configuration.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.springframework.stereotype.Service;

/**
 * Enforces the {@code disallow-backdated-transactions} global configuration (FINERACT-1950).
 *
 * When the configuration is enabled, portfolio transactions dated more than N days before the current business date are
 * rejected, where N is the configuration's value (0 or unset allows only current-date transactions).
 */
@RequiredArgsConstructor
@Service
public class BackdatedTransactionValidationService {

    private final ConfigurationDomainService configurationDomainService;

    public void validateTransactionDate(final LocalDate transactionDate) {
        if (transactionDate == null || !configurationDomainService.isBackdatedTransactionsDisallowed()) {
            return;
        }
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        final Long toleranceDays = configurationDomainService.retrieveBackdatedTransactionsToleranceDays();
        final LocalDate earliestAllowedDate = businessDate.minusDays(toleranceDays != null && toleranceDays > 0 ? toleranceDays : 0);
        if (transactionDate.isBefore(earliestAllowedDate)) {
            throw new GeneralPlatformDomainRuleException("error.msg.transaction.backdated.not.allowed",
                    "Backdated transactions are disallowed", transactionDate, earliestAllowedDate);
        }
    }
}
