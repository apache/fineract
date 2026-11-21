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
package org.apache.fineract.portfolio.savings.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.client.contract.ClientSavingsReadService;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsProductRepository;
import org.apache.fineract.portfolio.savings.exception.SavingsProductNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientSavingsReadServiceImpl implements ClientSavingsReadService {

    private final SavingsProductRepository savingsProductRepository;
    private final SavingsAccountRepositoryWrapper savingsRepositoryWrapper;

    @Override
    public void validateSavingsProductExists(final Long savingsProductId) {
        this.savingsProductRepository.findById(savingsProductId).orElseThrow(() -> new SavingsProductNotFoundException(savingsProductId));
    }

    @Override
    public boolean hasNonClosedSavingsAccountsForClient(final Long clientId) {
        final List<SavingsAccount> clientSavingAccounts = this.savingsRepositoryWrapper.findSavingAccountByClientId(clientId);
        for (final SavingsAccount saving : clientSavingAccounts) {
            if (saving.isActive() || saving.isSubmittedAndPendingApproval() || saving.isApproved()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSavingsAccountForClient(final Long savingsId, final Long clientId) {
        final SavingsAccount savingsAccount = this.savingsRepositoryWrapper.findOneWithNotFoundDetection(savingsId);
        return savingsAccount.getClient().identifiedBy(clientId);
    }
}
