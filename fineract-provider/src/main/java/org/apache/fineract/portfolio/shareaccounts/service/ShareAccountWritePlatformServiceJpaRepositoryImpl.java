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
package org.apache.fineract.portfolio.shareaccounts.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormat;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.accounts.constants.ShareAccountApiConstants;
import org.apache.fineract.portfolio.client.domain.AccountNumberGenerator;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountTransactionEnumData;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccount;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccountChargePaidBy;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccountRepositoryWrapper;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccountTransaction;
import org.apache.fineract.portfolio.shareaccounts.serialization.ShareAccountDataSerializer;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class ShareAccountWritePlatformServiceJpaRepositoryImpl implements ShareAccountWritePlatformService {

    private final ShareAccountDataSerializer accountDataSerializer;

    private final ShareAccountRepositoryWrapper shareAccountRepository;

    private final AccountNumberGenerator accountNumberGenerator;

    private final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository;

    private final JournalEntryWritePlatformService journalEntryWritePlatformService;

    @Autowired
    public ShareAccountWritePlatformServiceJpaRepositoryImpl(final ShareAccountDataSerializer accountDataSerializer,
            final ShareAccountRepositoryWrapper shareAccountRepository,
            final AccountNumberGenerator accountNumberGenerator,
            final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository,
            final JournalEntryWritePlatformService journalEntryWritePlatformService) {
        this.accountDataSerializer = accountDataSerializer;
        this.shareAccountRepository = shareAccountRepository;
        this.accountNumberGenerator = accountNumberGenerator;
        this.accountNumberFormatRepository = accountNumberFormatRepository;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
    }

    @Override
    public CommandProcessingResult createShareAccount(JsonCommand jsonCommand) {
        try {
            ShareAccount account = this.accountDataSerializer.validateAndCreate(jsonCommand);
            this.shareAccountRepository.save(account);
            generateAccountNumber(account);
            //this.shareProductRepository.save(account.getShareProduct()); //subscribed shares is increased
            journalEntryWritePlatformService.createJournalEntriesForShares(populateJournalEntries(account,
                    account.getPendingForApprovalSharePurchaseTransactions()));
            return new CommandProcessingResultBuilder() //
                    .withCommandId(jsonCommand.commandId()) //
                    .withEntityId(account.getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(jsonCommand, dve);
            return CommandProcessingResult.empty();
        }
    }

    private void generateAccountNumber(final ShareAccount account) {
        if (account.isAccountNumberRequiresAutoGeneration()) {
            final AccountNumberFormat accountNumberFormat = this.accountNumberFormatRepository.findByAccountType(EntityAccountType.SHARES);
            account.updateAccountNumber(this.accountNumberGenerator.generate(account, accountNumberFormat));
            this.shareAccountRepository.save(account);
        }
    }

    private Map<String, Object> populateJournalEntries(final ShareAccount account, final Set<ShareAccountTransaction> transactions) {
        final Map<String, Object> accountingBridgeData = new HashMap<>();
        Boolean cashBasedAccounting = account.getShareProduct().getAccountingType().intValue() == 2 ? Boolean.TRUE : Boolean.FALSE;
        accountingBridgeData.put("cashBasedAccountingEnabled", cashBasedAccounting);
        accountingBridgeData.put("accrualBasedAccountingEnabled", Boolean.FALSE);
        accountingBridgeData.put("shareAccountId", account.getId());
        accountingBridgeData.put("shareProductId", account.getShareProduct().getId());
        accountingBridgeData.put("officeId", account.getOfficeId());
        MonetaryCurrency currency = account.getCurrency();
        final CurrencyData currencyData = new CurrencyData(currency.getCode(), "", currency.getDigitsAfterDecimal(),
                currency.getCurrencyInMultiplesOf(), "", "");
        accountingBridgeData.put("currency", currencyData);
        final List<Map<String, Object>> newTransactionsMap = new ArrayList<>();
        accountingBridgeData.put("newTransactions", newTransactionsMap);

        for (ShareAccountTransaction transaction : transactions) {
            final Map<String, Object> transactionDto = new HashMap<>();
            transactionDto.put("officeId", account.getOfficeId());
            transactionDto.put("id", transaction.getId());
            transactionDto.put("date", new LocalDate(transaction.getPurchasedDate()));
            final Integer status = transaction.getTransactionStatus();
            final ShareAccountTransactionEnumData statusEnum = new ShareAccountTransactionEnumData(status.longValue(), null, null);
            final Integer type = transaction.getTransactionType();
            final ShareAccountTransactionEnumData typeEnum = new ShareAccountTransactionEnumData(type.longValue(), null, null);
            transactionDto.put("status", statusEnum);
            transactionDto.put("type", typeEnum);
            transactionDto.put("amount", transaction.amount());
            transactionDto.put("chargeAmount", transaction.chargeAmount());
            transactionDto.put("paymentTypeId", null); // FIXME::make it cash
                                                       // payment
            if (transaction.getChargesPaidBy() != null && !transaction.getChargesPaidBy().isEmpty()) {
                final List<Map<String, Object>> chargesPaidData = new ArrayList<>();
                transactionDto.put("chargesPaid", chargesPaidData);
                Set<ShareAccountChargePaidBy> chargesPaidBySet = transaction.getChargesPaidBy();
                for (ShareAccountChargePaidBy chargesPaidBy : chargesPaidBySet) {
                    Map<String, Object> chargesPaidDto = new HashMap<>();
                    chargesPaidDto.put("chargeId", chargesPaidBy.getChargeId());
                    chargesPaidDto.put("sharesChargeId", chargesPaidBy.getShareChargeId());
                    chargesPaidDto.put("amount", chargesPaidBy.getAmount());
                    chargesPaidData.add(chargesPaidDto);
                }
            }
            newTransactionsMap.add(transactionDto);
        }
        return accountingBridgeData;
    }

    @Override
    public CommandProcessingResult updateShareAccount(Long accountId, JsonCommand jsonCommand) {
        try {
            ShareAccount account = this.shareAccountRepository.findOneWithNotFoundDetection(accountId);
            Map<String, Object> changes = this.accountDataSerializer.validateAndUpdate(jsonCommand, account);
            if (!changes.isEmpty()) {
                this.shareAccountRepository.save(account);
            }
            return new CommandProcessingResultBuilder() //
                    .withCommandId(jsonCommand.commandId()) //
                    .withEntityId(accountId) //
                    .with(changes) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(jsonCommand, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    public CommandProcessingResult applyAddtionalShares(final Long accountId, JsonCommand jsonCommand) {
        try {
            ShareAccount account = this.shareAccountRepository.findOneWithNotFoundDetection(accountId);
            Map<String, Object> changes = this.accountDataSerializer.validateAndApplyAddtionalShares(jsonCommand, account);
            ShareAccountTransaction transaction = null;
            if (!changes.isEmpty()) {
                this.shareAccountRepository.save(account);
                transaction = (ShareAccountTransaction) changes.get(ShareAccountApiConstants.additionalshares_paramname);
                transaction = account.getShareAccountTransaction(transaction);
                if (transaction != null) {
                    changes.clear();
                    changes.put(ShareAccountApiConstants.additionalshares_paramname, transaction.getId());
                    Set<ShareAccountTransaction> transactions = new HashSet<>();
                    transactions.add(transaction);
                    this.journalEntryWritePlatformService.createJournalEntriesForShares(populateJournalEntries(account, transactions));
                }
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(jsonCommand.commandId()) //
                    .withEntityId(accountId) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(jsonCommand, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    public CommandProcessingResult approveShareAccount(Long accountId, JsonCommand jsonCommand) {

        try {
            ShareAccount account = this.shareAccountRepository.findOneWithNotFoundDetection(accountId);
            Map<String, Object> changes = this.accountDataSerializer.validateAndApprove(jsonCommand, account);
            if (!changes.isEmpty()) {
                this.shareAccountRepository.save(account);
            }
            this.journalEntryWritePlatformService.createJournalEntriesForShares(populateJournalEntries(account,
                    account.getShareAccountTransactions()));
            return new CommandProcessingResultBuilder() //
                    .withCommandId(jsonCommand.commandId()) //
                    .withEntityId(accountId) //
                    .with(changes) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(jsonCommand, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    public CommandProcessingResult rejectShareAccount(Long accountId, JsonCommand jsonCommand) {
        try {
            ShareAccount account = this.shareAccountRepository.findOneWithNotFoundDetection(accountId);
            Map<String, Object> changes = this.accountDataSerializer.validateAndReject(jsonCommand, account);
            if (!changes.isEmpty()) {
                this.shareAccountRepository.save(account);
            }
            this.journalEntryWritePlatformService.createJournalEntriesForShares(populateJournalEntries(account,
                    account.getShareAccountTransactions()));
            return new CommandProcessingResultBuilder() //
                    .withCommandId(jsonCommand.commandId()) //
                    .withEntityId(accountId) //
                    .with(changes) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(jsonCommand, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    public CommandProcessingResult undoApproveShareAccount(Long accountId, JsonCommand jsonCommand) {
        try {
            ShareAccount account = this.shareAccountRepository.findOneWithNotFoundDetection(accountId);
            Map<String, Object> changes = this.accountDataSerializer.validateAndUndoApprove(jsonCommand, account);
            if (!changes.isEmpty()) {
                this.shareAccountRepository.save(account);
            }
            /*
             * this.journalEntryWritePlatformService.createJournalEntriesForShares
             * (populateJournalEntries(account,
             * account.getShareAccountTransactions()));
             */
            return new CommandProcessingResultBuilder() //
                    .withCommandId(jsonCommand.commandId()) //
                    .withEntityId(accountId) //
                    .with(changes) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(jsonCommand, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    public CommandProcessingResult activateShareAccount(Long accountId, JsonCommand jsonCommand) {

        try {
            ShareAccount account = this.shareAccountRepository.findOneWithNotFoundDetection(accountId);
            Map<String, Object> changes = this.accountDataSerializer.validateAndActivate(jsonCommand, account);
            if (!changes.isEmpty()) {
                this.shareAccountRepository.save(account);
            }
            this.journalEntryWritePlatformService.createJournalEntriesForShares(populateJournalEntries(account,
                    account.getChargeTransactions()));
            return new CommandProcessingResultBuilder() //
                    .withCommandId(jsonCommand.commandId()) //
                    .withEntityId(accountId) //
                    .with(changes) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(jsonCommand, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    public CommandProcessingResult approveAdditionalShares(Long accountId, JsonCommand jsonCommand) {

        try {
            ShareAccount account = this.shareAccountRepository.findOneWithNotFoundDetection(accountId);
            Map<String, Object> changes = this.accountDataSerializer.validateAndApproveAddtionalShares(jsonCommand, account);
            if (!changes.isEmpty()) {
                this.shareAccountRepository.save(account);
                ArrayList<Long> transactionIds = (ArrayList<Long>) changes.get(ShareAccountApiConstants.requestedshares_paramname);
                if (transactionIds != null) {
                    Set<ShareAccountTransaction> transactions = new HashSet<>();
                    for (Long id : transactionIds) {
                        ShareAccountTransaction transaction = account.retrievePurchasedShares(id);
                        transactions.add(transaction);
                    }
                    this.journalEntryWritePlatformService.createJournalEntriesForShares(populateJournalEntries(account, transactions));
                }
            }
            return new CommandProcessingResultBuilder() //
                    .withCommandId(jsonCommand.commandId()) //
                    .withEntityId(accountId) //
                    .with(changes) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(jsonCommand, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    public CommandProcessingResult rejectAdditionalShares(Long accountId, JsonCommand jsonCommand) {

        try {
            ShareAccount account = this.shareAccountRepository.findOneWithNotFoundDetection(accountId);
            Map<String, Object> changes = this.accountDataSerializer.validateAndRejectAddtionalShares(jsonCommand, account);
            if (!changes.isEmpty()) {
                this.shareAccountRepository.save(account);
                ArrayList<Long> transactionIds = (ArrayList<Long>) changes.get(ShareAccountApiConstants.requestedshares_paramname);
                if (transactionIds != null) {
                    Set<ShareAccountTransaction> transactions = new HashSet<>();
                    for (Long id : transactionIds) {
                        ShareAccountTransaction transaction = account.retrievePurchasedShares(id);
                        transactions.add(transaction);
                    }
                    this.journalEntryWritePlatformService.createJournalEntriesForShares(populateJournalEntries(account, transactions));
                }
            }
            return new CommandProcessingResultBuilder() //
                    .withCommandId(jsonCommand.commandId()) //
                    .withEntityId(accountId) //
                    .with(changes) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(jsonCommand, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    public CommandProcessingResult redeemShares(Long accountId, JsonCommand jsonCommand) {
        try {
            ShareAccount account = this.shareAccountRepository.findOneWithNotFoundDetection(accountId);
            Map<String, Object> changes = this.accountDataSerializer.validateAndRedeemShares(jsonCommand, account);
            if (!changes.isEmpty()) {
                this.shareAccountRepository.save(account);
                ShareAccountTransaction transaction = (ShareAccountTransaction) changes
                        .get(ShareAccountApiConstants.requestedshares_paramname);
                transaction = account.getShareAccountTransaction(transaction);
                Set<ShareAccountTransaction> transactions = new HashSet<>();
                transactions.add(transaction);
                this.journalEntryWritePlatformService.createJournalEntriesForShares(populateJournalEntries(account, transactions));
                changes.clear();
                changes.put(ShareAccountApiConstants.requestedshares_paramname, transaction.getId());

            }
            return new CommandProcessingResultBuilder() //
                    .withCommandId(jsonCommand.commandId()) //
                    .withEntityId(accountId) //
                    .with(changes) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(jsonCommand, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    public CommandProcessingResult closeShareAccount(Long accountId, JsonCommand jsonCommand) {
        try {
            ShareAccount account = this.shareAccountRepository.findOneWithNotFoundDetection(accountId);
            Map<String, Object> changes = this.accountDataSerializer.validateAndClose(jsonCommand, account);
            if (!changes.isEmpty()) {
                this.shareAccountRepository.save(account);
                ShareAccountTransaction transaction = (ShareAccountTransaction) changes
                        .get(ShareAccountApiConstants.requestedshares_paramname);
                transaction = account.getShareAccountTransaction(transaction);
                Set<ShareAccountTransaction> transactions = new HashSet<>();
                transactions.add(transaction);
                this.journalEntryWritePlatformService.createJournalEntriesForShares(populateJournalEntries(account, transactions));
                changes.clear();
                changes.put(ShareAccountApiConstants.requestedshares_paramname, transaction.getId());

            }
            return new CommandProcessingResultBuilder() //
                    .withCommandId(jsonCommand.commandId()) //
                    .withEntityId(accountId) //
                    .with(changes) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(jsonCommand, dve);
            return CommandProcessingResult.empty();
        }
    }
    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

    }
}
