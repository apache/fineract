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
package org.apache.fineract.portfolio.savings.domain;

import java.time.LocalDate;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.springframework.stereotype.Service;

/**
 * Thin service exposing the DB-backed interest-transaction lookups for a {@link SavingsAccount}. The stateless interest
 * posting-period math previously living here now sits in the static {@link SavingsInterestCalculationUtil}.
 */
@Service
@RequiredArgsConstructor
public final class SavingsHelper {

    private final AccountTransfersReadPlatformService accountTransfersReadPlatformService;

    public Collection<Long> fetchPostInterestTransactionIds(Long accountId) {
        return this.accountTransfersReadPlatformService.fetchPostInterestTransactionIds(accountId);
    }

    public Collection<Long> fetchPostInterestTransactionIds(Long accountId, LocalDate pivotDate) {
        return this.accountTransfersReadPlatformService.fetchPostInterestTransactionIdsWithPivotDate(accountId, pivotDate);
    }

}
