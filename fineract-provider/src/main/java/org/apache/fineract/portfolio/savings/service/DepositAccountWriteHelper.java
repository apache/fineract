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

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.exception.GroupNotActiveException;
import org.apache.fineract.portfolio.savings.domain.FixedDepositAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountCharge;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;

/**
 * Steps shared by the deposit account write services.
 */
@RequiredArgsConstructor
public class DepositAccountWriteHelper {

    private final SavingsAccountTransactionRepository savingsAccountTransactionRepository;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;

    public Money getActivationCharge(final FixedDepositAccount account) {
        Money activationChargeAmount = Money.zero(account.getCurrency());
        for (SavingsAccountCharge savingsAccountCharge : account.charges()) {
            if (savingsAccountCharge.isSavingsActivation()) {
                activationChargeAmount = activationChargeAmount.plus(savingsAccountCharge.getAmount(account.getCurrency()));
            }
        }
        return activationChargeAmount;
    }

    public void payActivationCharge(final FixedDepositAccount account) {
        for (SavingsAccountCharge savingsAccountCharge : account.charges()) {
            if (savingsAccountCharge.isSavingsActivation()) {
                account.payCharge(savingsAccountCharge, savingsAccountCharge.getAmount(account.getCurrency()), account.getActivationDate(),
                        false, null);
            }
        }
    }

    public Long saveTransactionToGenerateTransactionId(final SavingsAccountTransaction transaction) {
        this.savingsAccountTransactionRepository.saveAndFlush(transaction);
        return transaction.getId();
    }

    /**
     *
     */
    public void throwValidationForActiveStatus(final String actionName) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + actionName);
        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("account.is.not.active");
        throw new PlatformApiDataValidationException(dataValidationErrors);
    }

    public void checkClientOrGroupActive(final SavingsAccount account) {
        final Client client = account.getClient();
        if (client != null && client.isNotActive()) {
            throw new ClientNotActiveException(client.getId());
        }
        final Group group = account.group();
        if (group != null && group.isNotActive()) {
            throw new GroupNotActiveException(group.getId());
        }
    }

    public void updateExistingTransactionsDetails(SavingsAccount account, Set<Long> existingTransactionIds,
            Set<Long> existingReversedTransactionIds) {
        existingTransactionIds.addAll(account.findExistingTransactionIds());
        existingReversedTransactionIds.addAll(account.findExistingReversedTransactionIds());
    }

    public void postJournalEntries(final SavingsAccount savingsAccount, final Set<Long> existingTransactionIds,
            final Set<Long> existingReversedTransactionIds) {

        boolean isAccountTransfer = false;
        final Map<String, Object> accountingBridgeData = savingsAccount.deriveAccountingBridgeData(savingsAccount.getCurrency().getCode(),
                existingTransactionIds, existingReversedTransactionIds, isAccountTransfer, false);
        this.journalEntryWritePlatformService.createJournalEntriesForSavings(accountingBridgeData);
    }
}
