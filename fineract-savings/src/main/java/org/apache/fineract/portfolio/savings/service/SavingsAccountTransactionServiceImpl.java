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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsEvent;

/**
 * Default implementation of {@link SavingsAccountTransactionService}. The {@code withdraw} body was extracted from
 * {@code SavingsAccount.withdraw}; behaviour is intentionally unchanged.
 */
@RequiredArgsConstructor
public class SavingsAccountTransactionServiceImpl implements SavingsAccountTransactionService {

    private final SavingsAccountChargeProcessingService savingsAccountChargeProcessingService;

    @Override
    public SavingsAccountTransaction withdraw(final SavingsAccount account, final SavingsAccountTransactionDTO transactionDTO,
            final boolean applyWithdrawFee, final boolean backdatedTxnsAllowedTill, final Long relaxingDaysConfigForPivotDate,
            final String refNo) {
        if (!account.isTransactionsAllowed()) {

            final String defaultUserMessage = "Transaction is not allowed. Account is not active.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.account.is.not.active",
                    defaultUserMessage, "transactionDate", transactionDTO.getTransactionDate().format(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (DateUtils.isDateInTheFuture(transactionDTO.getTransactionDate())) {
            final String defaultUserMessage = "Transaction date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.in.the.future",
                    defaultUserMessage, "transactionDate", transactionDTO.getTransactionDate().format(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (DateUtils.isBefore(transactionDTO.getTransactionDate(), account.getActivatedOnDate())) {
            final Object[] defaultUserArgs = Arrays.asList(transactionDTO.getTransactionDate().format(transactionDTO.getFormatter()),
                    account.getActivatedOnDate().format(transactionDTO.getFormatter())).toArray();
            final String defaultUserMessage = "Transaction date cannot be before accounts activation date.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.savingsaccount.transaction.before.activation.date",
                    defaultUserMessage, "transactionDate", defaultUserArgs);

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (account.isAccountLocked(transactionDTO.getTransactionDate())) {
            final String defaultUserMessage = "Withdrawal is not allowed. No withdrawals are allowed until after "
                    + account.getLockedInUntilDate().format(transactionDTO.getFormatter());
            final ApiParameterError error = ApiParameterError.parameterError(
                    "error.msg.savingsaccount.transaction.withdrawals.blocked.during.lockin.period", defaultUserMessage, "transactionDate",
                    transactionDTO.getTransactionDate().format(transactionDTO.getFormatter()),
                    account.getLockedInUntilDate().format(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        account.validatePivotDateTransaction(transactionDTO.getTransactionDate(), backdatedTxnsAllowedTill, relaxingDaysConfigForPivotDate,
                "savingsaccount");
        account.validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_WITHDRAWAL, transactionDTO.getTransactionDate());

        if (applyWithdrawFee) {
            // auto pay withdrawal fee
            this.savingsAccountChargeProcessingService.payWithdrawalFee(account, transactionDTO.getTransactionAmount(),
                    transactionDTO.getTransactionDate(), transactionDTO.getPaymentDetail(), backdatedTxnsAllowedTill, refNo);
        }

        final Money transactionAmountMoney = Money.of(account.getCurrency(), transactionDTO.getTransactionAmount());
        final SavingsAccountTransaction transaction = SavingsAccountTransaction.withdrawal(account, account.office(),
                transactionDTO.getPaymentDetail(), transactionDTO.getTransactionDate(), transactionAmountMoney, refNo);

        if (backdatedTxnsAllowedTill) {
            account.addTransactionToExisting(transaction);
        } else {
            account.accrualsForSavingsReverse(transactionDTO, backdatedTxnsAllowedTill);
            account.addTransaction(transaction);
        }

        account.resetDormancySubStatusOnTransaction();
        if (backdatedTxnsAllowedTill) {
            account.getSummary().updateSummaryWithPivotConfig(account.getCurrency(), transaction,
                    account.getSavingsAccountTransactionsWithPivotConfig());
        }
        return transaction;
    }

    @Override
    public SavingsAccountTransaction deposit(final SavingsAccount account, final SavingsAccountTransactionDTO transactionDTO,
            final boolean backdatedTxnsAllowedTill, final Long relaxingDaysConfigForPivotDate, final String refNo) {
        // sub-type specific guard rails (e.g. recurring-deposit maturity/start-date checks); no-op for plain savings
        account.validateDepositTransaction(transactionDTO);
        return deposit(account, transactionDTO, SavingsAccountTransactionType.DEPOSIT, backdatedTxnsAllowedTill,
                relaxingDaysConfigForPivotDate, refNo);
    }

    @Override
    public SavingsAccountTransaction deposit(final SavingsAccount account, final SavingsAccountTransactionDTO transactionDTO,
            final SavingsAccountTransactionType savingsAccountTransactionType, final boolean backdatedTxnsAllowedTill,
            final Long relaxingDaysConfigForPivotDate, final String refNo) {
        final String resourceTypeName = account.depositAccountType().resourceName();
        if (account.isNotActive()) {
            final String defaultUserMessage = "Transaction is not allowed. Account is not active.";
            final ApiParameterError error = ApiParameterError.parameterError(
                    "error.msg." + resourceTypeName + ".transaction.account.is.not.active", defaultUserMessage, "transactionDate",
                    transactionDTO.getTransactionDate().format(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (DateUtils.isDateInTheFuture(transactionDTO.getTransactionDate())) {
            final String defaultUserMessage = "Transaction date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg." + resourceTypeName + ".transaction.in.the.future",
                    defaultUserMessage, "transactionDate", transactionDTO.getTransactionDate().format(transactionDTO.getFormatter()));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (DateUtils.isBefore(transactionDTO.getTransactionDate(), account.getActivatedOnDate())) {
            final Object[] defaultUserArgs = Arrays.asList(transactionDTO.getTransactionDate().format(transactionDTO.getFormatter()),
                    account.getActivatedOnDate().format(transactionDTO.getFormatter())).toArray();
            final String defaultUserMessage = "Transaction date cannot be before accounts activation date.";
            final ApiParameterError error = ApiParameterError.parameterError(
                    "error.msg." + resourceTypeName + ".transaction.before.activation.date", defaultUserMessage, "transactionDate",
                    defaultUserArgs);

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        account.validatePivotDateTransaction(transactionDTO.getTransactionDate(), backdatedTxnsAllowedTill, relaxingDaysConfigForPivotDate,
                resourceTypeName);

        account.validateActivityNotBeforeClientOrGroupTransferDate(SavingsEvent.SAVINGS_DEPOSIT, transactionDTO.getTransactionDate());

        final Money amount = Money.of(account.getCurrency(), transactionDTO.getTransactionAmount());

        final SavingsAccountTransaction transaction = SavingsAccountTransaction.deposit(account, account.office(),
                transactionDTO.getPaymentDetail(), transactionDTO.getTransactionDate(), amount, savingsAccountTransactionType, refNo);

        if (backdatedTxnsAllowedTill) {
            account.addTransactionToExisting(transaction);
        } else {
            account.accrualsForSavingsReverse(transactionDTO, backdatedTxnsAllowedTill);
            account.addTransaction(transaction);
        }

        account.resetDormancySubStatusOnTransaction();

        if (backdatedTxnsAllowedTill) {
            account.getSummary().updateSummaryWithPivotConfig(account.getCurrency(), transaction,
                    account.getSavingsAccountTransactionsWithPivotConfig());
        }

        return transaction;
    }
}
