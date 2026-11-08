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

import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;

/**
 * Entry point for fixed and recurring deposit account write operations. The work is done by
 * {@link FixedDepositAccountWritePlatformService}, {@link RecurringDepositAccountWritePlatformService},
 * {@link DepositAccountClosureWritePlatformService} and {@link DepositAccountTransactionWritePlatformService}.
 */
@RequiredArgsConstructor
public class DepositAccountWritePlatformServiceJpaRepositoryImpl implements DepositAccountWritePlatformService {

    private final FixedDepositAccountWritePlatformService fixedDepositAccountWritePlatformService;
    private final RecurringDepositAccountWritePlatformService recurringDepositAccountWritePlatformService;
    private final DepositAccountClosureWritePlatformService depositAccountClosureWritePlatformService;
    private final DepositAccountTransactionWritePlatformService depositAccountTransactionWritePlatformService;

    @Override
    public CommandProcessingResult activateFDAccount(final Long savingsId, final JsonCommand command) {
        return this.fixedDepositAccountWritePlatformService.activateFDAccount(savingsId, command);
    }

    @Override
    public CommandProcessingResult activateRDAccount(final Long savingsId, final JsonCommand command) {
        return this.recurringDepositAccountWritePlatformService.activateRDAccount(savingsId, command);
    }

    @Override
    public CommandProcessingResult updateDepositAmountForRDAccount(final Long savingsId, final JsonCommand command) {
        return this.recurringDepositAccountWritePlatformService.updateDepositAmountForRDAccount(savingsId, command);
    }

    @Override
    public CommandProcessingResult depositToFDAccount(final Long savingsId, final JsonCommand command) {
        return this.fixedDepositAccountWritePlatformService.depositToFDAccount(savingsId, command);
    }

    @Override
    public CommandProcessingResult depositToRDAccount(final Long savingsId, final JsonCommand command) {
        return this.recurringDepositAccountWritePlatformService.depositToRDAccount(savingsId, command);
    }

    @Override
    public CommandProcessingResult withdrawal(final Long savingsId, final JsonCommand command,
            final DepositAccountType depositAccountType) {
        return this.depositAccountTransactionWritePlatformService.withdrawal(savingsId, command, depositAccountType);
    }

    @Override
    public CommandProcessingResult calculateInterest(final Long savingsId, final DepositAccountType depositAccountType) {
        return this.depositAccountTransactionWritePlatformService.calculateInterest(savingsId, depositAccountType);
    }

    @Override
    public CommandProcessingResult postInterest(final Long savingsId, final DepositAccountType depositAccountType) {
        return this.depositAccountTransactionWritePlatformService.postInterest(savingsId, depositAccountType);
    }

    @Override
    public CommandProcessingResult undoFDTransaction(final Long savingsId, final Long transactionId,
            final boolean allowAccountTransferModification) {
        return this.fixedDepositAccountWritePlatformService.undoFDTransaction(savingsId, transactionId, allowAccountTransferModification);
    }

    @Override
    public CommandProcessingResult undoRDTransaction(final Long savingsId, final Long transactionId,
            final boolean allowAccountTransferModification) {
        return this.recurringDepositAccountWritePlatformService.undoRDTransaction(savingsId, transactionId,
                allowAccountTransferModification);
    }

    @Override
    public CommandProcessingResult adjustFDTransaction(final Long savingsId, final Long transactionId, final JsonCommand command) {
        return this.fixedDepositAccountWritePlatformService.adjustFDTransaction(savingsId, transactionId, command);
    }

    @Override
    public CommandProcessingResult adjustRDTransaction(final Long savingsId, final Long transactionId, final JsonCommand command) {
        return this.recurringDepositAccountWritePlatformService.adjustRDTransaction(savingsId, transactionId, command);
    }

    @Override
    public CommandProcessingResult closeFDAccount(final Long savingsId, final JsonCommand command) {
        return this.depositAccountClosureWritePlatformService.closeFDAccount(savingsId, command);
    }

    @Override
    public CommandProcessingResult closeRDAccount(final Long savingsId, final JsonCommand command) {
        return this.depositAccountClosureWritePlatformService.closeRDAccount(savingsId, command);
    }

    @Override
    public CommandProcessingResult prematureCloseFDAccount(final Long savingsId, final JsonCommand command) {
        return this.depositAccountClosureWritePlatformService.prematureCloseFDAccount(savingsId, command);
    }

    @Override
    public CommandProcessingResult prematureCloseRDAccount(final Long savingsId, final JsonCommand command) {
        return this.depositAccountClosureWritePlatformService.prematureCloseRDAccount(savingsId, command);
    }

    @Override
    public CommandProcessingResult waiveCharge(final Long savingsAccountId, final Long savingsAccountChargeId,
            final DepositAccountType depositAccountType) {
        return this.depositAccountTransactionWritePlatformService.waiveCharge(savingsAccountId, savingsAccountChargeId, depositAccountType);
    }

    @Override
    public CommandProcessingResult payCharge(final Long savingsAccountId, final Long savingsAccountChargeId, final JsonCommand command,
            final DepositAccountType depositAccountType) {
        return this.depositAccountTransactionWritePlatformService.payCharge(savingsAccountId, savingsAccountChargeId, command,
                depositAccountType);
    }

    @Override
    public void updateMaturityDetails(final Long depositAccountId, final DepositAccountType depositAccountType) {
        this.depositAccountClosureWritePlatformService.updateMaturityDetails(depositAccountId, depositAccountType);
    }

    @Override
    public SavingsAccountTransaction mandatorySavingsAccountDeposit(final SavingsAccountTransactionDTO accountTransactionDTO) {
        return this.depositAccountTransactionWritePlatformService.mandatorySavingsAccountDeposit(accountTransactionDTO);
    }
}
