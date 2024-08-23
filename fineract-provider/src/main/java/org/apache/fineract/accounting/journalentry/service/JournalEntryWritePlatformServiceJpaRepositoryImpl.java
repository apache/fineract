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
package org.apache.fineract.accounting.journalentry.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.closure.domain.GLClosure;
import org.apache.fineract.accounting.closure.domain.GLClosureRepository;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccount;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.data.GLAccountDataForLookup;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.glaccount.exception.GLAccountNotFoundException;
import org.apache.fineract.accounting.glaccount.service.GLAccountReadPlatformService;
import org.apache.fineract.accounting.journalentry.api.JournalEntryJsonInputParams;
import org.apache.fineract.accounting.journalentry.command.JournalEntryCommand;
import org.apache.fineract.accounting.journalentry.command.SingleDebitOrCreditEntryCommand;
import org.apache.fineract.accounting.journalentry.data.ClientTransactionDTO;
import org.apache.fineract.accounting.journalentry.data.LoanDTO;
import org.apache.fineract.accounting.journalentry.data.SavingsDTO;
import org.apache.fineract.accounting.journalentry.data.SharesDTO;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.accounting.journalentry.exception.JournalEntriesNotFoundException;
import org.apache.fineract.accounting.journalentry.exception.JournalEntryInvalidException;
import org.apache.fineract.accounting.journalentry.exception.JournalEntryInvalidException.GlJournalEntryInvalidReason;
import org.apache.fineract.accounting.journalentry.exception.JournalEntryRuntimeException;
import org.apache.fineract.accounting.journalentry.serialization.JournalEntryCommandFromApiJsonDeserializer;
import org.apache.fineract.accounting.provisioning.domain.LoanProductProvisioningEntry;
import org.apache.fineract.accounting.provisioning.domain.ProvisioningEntry;
import org.apache.fineract.accounting.rule.domain.AccountingRule;
import org.apache.fineract.accounting.rule.domain.AccountingRuleRepository;
import org.apache.fineract.accounting.rule.exception.AccountingRuleNotFoundException;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.OrganisationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.portfolio.PortfolioProductType;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@RequiredArgsConstructor
@Slf4j
public class JournalEntryWritePlatformServiceJpaRepositoryImpl implements JournalEntryWritePlatformService {

    private final GLClosureRepository glClosureRepository;
    private final GLAccountRepository glAccountRepository;
    private final JournalEntryRepository glJournalEntryRepository;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;
    private final AccountingProcessorForLoanFactory accountingProcessorForLoanFactory;
    private final AccountingProcessorForSavingsFactory accountingProcessorForSavingsFactory;
    private final AccountingProcessorForSharesFactory accountingProcessorForSharesFactory;
    private final AccountingProcessorHelper helper;
    private final JournalEntryCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final AccountingRuleRepository accountingRuleRepository;
    private final GLAccountReadPlatformService glAccountReadPlatformService;
    private final OrganisationCurrencyRepositoryWrapper organisationCurrencyRepository;
    private final PlatformSecurityContext context;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final FinancialActivityAccountRepositoryWrapper financialActivityAccountRepositoryWrapper;
    private final CashBasedAccountingProcessorForClientTransactions accountingProcessorForClientTransactions;

    @Transactional
    @Override
    public CommandProcessingResult createJournalEntry(final JsonCommand command) {
        try {
            final JournalEntryCommand journalEntryCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            journalEntryCommand.validateForCreate();

            // check office is valid
            final Long officeId = command.longValueOfParameterNamed(JournalEntryJsonInputParams.OFFICE_ID.getValue());
            final Office office = this.officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);
            final Long accountRuleId = command.longValueOfParameterNamed(JournalEntryJsonInputParams.ACCOUNTING_RULE.getValue());
            final String currencyCode = command.stringValueOfParameterNamed(JournalEntryJsonInputParams.CURRENCY_CODE.getValue());

            validateBusinessRulesForJournalEntries(journalEntryCommand);

            /** Capture payment details **/
            final Map<String, Object> changes = new LinkedHashMap<>();
            final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

            /** Set a transaction Id and save these Journal entries **/
            final LocalDate transactionDate = command
                    .localDateValueOfParameterNamed(JournalEntryJsonInputParams.TRANSACTION_DATE.getValue());
            final String transactionId = generateTransactionId(officeId);
            final String referenceNumber = command.stringValueOfParameterNamed(JournalEntryJsonInputParams.REFERENCE_NUMBER.getValue());

            if (accountRuleId != null) {

                final AccountingRule accountingRule = this.accountingRuleRepository.findById(accountRuleId)
                        .orElseThrow(() -> new AccountingRuleNotFoundException(accountRuleId));

                if (accountingRule.getAccountToCredit() == null) {
                    if (journalEntryCommand.getCredits() == null) {
                        throw new JournalEntryInvalidException(GlJournalEntryInvalidReason.NO_DEBITS_OR_CREDITS, null, null, null);
                    }
                    if (journalEntryCommand.getDebits() != null) {
                        checkDebitOrCreditAccountsAreValid(accountingRule, journalEntryCommand.getCredits(),
                                journalEntryCommand.getDebits());
                        checkDebitAndCreditAmounts(journalEntryCommand.getCredits(), journalEntryCommand.getDebits());
                    }

                    saveAllDebitOrCreditEntries(journalEntryCommand, office, paymentDetail, currencyCode, transactionDate,
                            journalEntryCommand.getCredits(), transactionId, JournalEntryType.CREDIT, referenceNumber);
                } else {
                    final GLAccount creditAccountHead = accountingRule.getAccountToCredit();
                    validateGLAccountForTransaction(creditAccountHead);
                    validateDebitOrCreditArrayForExistingGLAccount(creditAccountHead, journalEntryCommand.getCredits());
                    saveAllDebitOrCreditEntries(journalEntryCommand, office, paymentDetail, currencyCode, transactionDate,
                            journalEntryCommand.getCredits(), transactionId, JournalEntryType.CREDIT, referenceNumber);
                }

                if (accountingRule.getAccountToDebit() == null) {
                    if (journalEntryCommand.getDebits() == null) {
                        throw new JournalEntryInvalidException(GlJournalEntryInvalidReason.NO_DEBITS_OR_CREDITS, null, null, null);
                    }
                    if (journalEntryCommand.getCredits() != null) {
                        checkDebitOrCreditAccountsAreValid(accountingRule, journalEntryCommand.getCredits(),
                                journalEntryCommand.getDebits());
                        checkDebitAndCreditAmounts(journalEntryCommand.getCredits(), journalEntryCommand.getDebits());
                    }

                    saveAllDebitOrCreditEntries(journalEntryCommand, office, paymentDetail, currencyCode, transactionDate,
                            journalEntryCommand.getDebits(), transactionId, JournalEntryType.DEBIT, referenceNumber);
                } else {
                    final GLAccount debitAccountHead = accountingRule.getAccountToDebit();
                    validateGLAccountForTransaction(debitAccountHead);
                    validateDebitOrCreditArrayForExistingGLAccount(debitAccountHead, journalEntryCommand.getDebits());
                    saveAllDebitOrCreditEntries(journalEntryCommand, office, paymentDetail, currencyCode, transactionDate,
                            journalEntryCommand.getDebits(), transactionId, JournalEntryType.DEBIT, referenceNumber);
                }
            } else {

                saveAllDebitOrCreditEntries(journalEntryCommand, office, paymentDetail, currencyCode, transactionDate,
                        journalEntryCommand.getDebits(), transactionId, JournalEntryType.DEBIT, referenceNumber);

                saveAllDebitOrCreditEntries(journalEntryCommand, office, paymentDetail, currencyCode, transactionDate,
                        journalEntryCommand.getCredits(), transactionId, JournalEntryType.CREDIT, referenceNumber);

            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withOfficeId(officeId)
                    .withTransactionId(transactionId).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            throw handleJournalEntryDataIntegrityIssues(throwable, dve);
        }
    }

    private void validateDebitOrCreditArrayForExistingGLAccount(final GLAccount glaccount,
            final SingleDebitOrCreditEntryCommand[] creditOrDebits) {
        /**
         * If a glaccount is assigned for a rule the credits or debits array should have only one entry and it must be
         * same as existing account
         */
        if (creditOrDebits.length != 1) {
            throw new JournalEntryInvalidException(GlJournalEntryInvalidReason.INVALID_DEBIT_OR_CREDIT_ACCOUNTS, null, null, null);
        }
        for (final SingleDebitOrCreditEntryCommand creditOrDebit : creditOrDebits) {
            if (glaccount == null || creditOrDebit == null || !Objects.equals(glaccount.getId(), creditOrDebit.getGlAccountId())) {
                throw new JournalEntryInvalidException(GlJournalEntryInvalidReason.INVALID_DEBIT_OR_CREDIT_ACCOUNTS, null, null, null);
            }
        }
    }

    @SuppressWarnings("null")
    private void checkDebitOrCreditAccountsAreValid(final AccountingRule accountingRule, final SingleDebitOrCreditEntryCommand[] credits,
            final SingleDebitOrCreditEntryCommand[] debits) {
        // Validate the debit and credit arrays are appropriate accounts
        List<GLAccountDataForLookup> allowedCreditGLAccounts;
        List<GLAccountDataForLookup> allowedDebitGLAccounts;
        int validCreditsNo = 0;
        int validDebitsNo = 0;

        if (credits != null && credits.length > 0) {
            allowedCreditGLAccounts = this.glAccountReadPlatformService.retrieveAccountsByTagId(accountingRule.getId(),
                    JournalEntryType.CREDIT.getValue());
            for (final GLAccountDataForLookup accountDataForLookup : allowedCreditGLAccounts) {
                for (final SingleDebitOrCreditEntryCommand credit : credits) {
                    if (credit.getGlAccountId().equals(accountDataForLookup.getId())) {
                        validCreditsNo++;
                    }
                }
            }
            if (credits.length != validCreditsNo) {
                throw new JournalEntryRuntimeException("error.msg.glJournalEntry.invalid.credits", "Invalid Credits.");
            }
        }

        if (debits != null && debits.length > 0) {
            allowedDebitGLAccounts = this.glAccountReadPlatformService.retrieveAccountsByTagId(accountingRule.getId(),
                    JournalEntryType.DEBIT.getValue());
            for (final GLAccountDataForLookup accountDataForLookup : allowedDebitGLAccounts) {
                for (final SingleDebitOrCreditEntryCommand debit : debits) {
                    if (debit.getGlAccountId().equals(accountDataForLookup.getId())) {
                        validDebitsNo++;
                    }
                }
            }
            if (debits.length != validDebitsNo) {
                throw new JournalEntryRuntimeException("error.msg.glJournalEntry.invalid.debits", "Invalid Debits");
            }
        }
    }

    private void checkDebitAndCreditAmounts(final SingleDebitOrCreditEntryCommand[] credits,
            final SingleDebitOrCreditEntryCommand[] debits) {
        // sum of all debits must be = sum of all credits
        BigDecimal creditsSum = BigDecimal.ZERO;
        BigDecimal debitsSum = BigDecimal.ZERO;
        for (final SingleDebitOrCreditEntryCommand creditEntryCommand : credits) {
            if (creditEntryCommand.getAmount() == null || creditEntryCommand.getGlAccountId() == null) {
                throw new JournalEntryInvalidException(GlJournalEntryInvalidReason.DEBIT_CREDIT_ACCOUNT_OR_AMOUNT_EMPTY, null, null, null);
            }
            creditsSum = creditsSum.add(creditEntryCommand.getAmount());
        }
        for (final SingleDebitOrCreditEntryCommand debitEntryCommand : debits) {
            if (debitEntryCommand.getAmount() == null || debitEntryCommand.getGlAccountId() == null) {
                throw new JournalEntryInvalidException(GlJournalEntryInvalidReason.DEBIT_CREDIT_ACCOUNT_OR_AMOUNT_EMPTY, null, null, null);
            }
            debitsSum = debitsSum.add(debitEntryCommand.getAmount());
        }
        if (creditsSum.compareTo(debitsSum) != 0) {
            throw new JournalEntryInvalidException(GlJournalEntryInvalidReason.DEBIT_CREDIT_SUM_MISMATCH, null, null, null);
        }
    }

    private void validateGLAccountForTransaction(final GLAccount creditOrDebitAccountHead) {
        /***
         * validate that the account allows manual adjustments and is not disabled
         **/
        if (creditOrDebitAccountHead.isDisabled()) {
            throw new JournalEntryInvalidException(GlJournalEntryInvalidReason.GL_ACCOUNT_DISABLED, null,
                    creditOrDebitAccountHead.getName(), creditOrDebitAccountHead.getGlCode());
        } else if (!creditOrDebitAccountHead.isManualEntriesAllowed()) {
            throw new JournalEntryInvalidException(GlJournalEntryInvalidReason.GL_ACCOUNT_MANUAL_ENTRIES_NOT_PERMITTED, null,
                    creditOrDebitAccountHead.getName(), creditOrDebitAccountHead.getGlCode());
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult revertJournalEntry(final JsonCommand command) {
        // is the transaction Id valid
        final List<JournalEntry> journalEntries = this.glJournalEntryRepository
                .findUnReversedManualJournalEntriesByTransactionId(command.getTransactionId());
        String reversalComment = command.stringValueOfParameterNamed("comments");

        if (journalEntries.size() <= 1) {
            throw new JournalEntriesNotFoundException(command.getTransactionId());
        }
        final String reversalTransactionId = revertJournalEntry(journalEntries, reversalComment);
        return new CommandProcessingResultBuilder().withTransactionId(reversalTransactionId).build();
    }

    public String revertJournalEntry(final List<JournalEntry> journalEntries, String reversalComment) {
        final Long officeId = journalEntries.get(0).getOffice().getId();
        final String reversalTransactionId = generateTransactionId(officeId);
        final boolean manualEntry = true;

        final boolean useDefaultComment = StringUtils.isBlank(reversalComment);

        validateCommentForReversal(reversalComment);

        // Before reversal validate accounting closure is done for that branch
        // or not.
        final LocalDate journalEntriesTransactionDate = journalEntries.get(0).getTransactionDate();
        final GLClosure latestGLClosureByBranch = this.glClosureRepository.getLatestGLClosureByBranch(officeId);
        if (latestGLClosureByBranch != null) {
            if (!DateUtils.isBefore(latestGLClosureByBranch.getClosingDate(), journalEntriesTransactionDate)) {
                final String accountName = null;
                final String accountGLCode = null;
                throw new JournalEntryInvalidException(GlJournalEntryInvalidReason.ACCOUNTING_CLOSED,
                        latestGLClosureByBranch.getClosingDate(), accountName, accountGLCode);
            }
        }

        for (final JournalEntry journalEntry : journalEntries) {
            JournalEntry reversalJournalEntry;
            if (useDefaultComment) {
                reversalComment = "Reversal entry for Journal Entry with Entry Id  :" + journalEntry.getId() + " and transaction Id "
                        + journalEntry.getTransactionId();
            }
            if (journalEntry.isDebitEntry()) {
                reversalJournalEntry = JournalEntry.createNew(journalEntry.getOffice(), journalEntry.getPaymentDetail(),
                        journalEntry.getGlAccount(), journalEntry.getCurrencyCode(), reversalTransactionId, manualEntry,
                        journalEntry.getTransactionDate(), JournalEntryType.CREDIT, journalEntry.getAmount(), reversalComment, null, null,
                        journalEntry.getReferenceNumber(), journalEntry.getLoanTransactionId(), journalEntry.getSavingsTransactionId(),
                        journalEntry.getClientTransactionId(), journalEntry.getShareTransactionId());
            } else {
                reversalJournalEntry = JournalEntry.createNew(journalEntry.getOffice(), journalEntry.getPaymentDetail(),
                        journalEntry.getGlAccount(), journalEntry.getCurrencyCode(), reversalTransactionId, manualEntry,
                        journalEntry.getTransactionDate(), JournalEntryType.DEBIT, journalEntry.getAmount(), reversalComment, null, null,
                        journalEntry.getReferenceNumber(), journalEntry.getLoanTransactionId(), journalEntry.getSavingsTransactionId(),
                        journalEntry.getClientTransactionId(), journalEntry.getShareTransactionId());
            }
            // save the reversal entry
            helper.persistJournalEntry(reversalJournalEntry);
            journalEntry.setReversed(true);
            journalEntry.setReversalJournalEntry(reversalJournalEntry);
            // save the updated journal entry
            helper.persistJournalEntry(journalEntry);
        }
        return reversalTransactionId;
    }

    @Override
    public String revertProvisioningJournalEntries(final LocalDate reversalTransactionDate, final Long entityId, final Integer entityType) {
        List<JournalEntry> journalEntries = this.glJournalEntryRepository.findProvisioningJournalEntriesByEntityId(entityId, entityType);
        final String reversalTransactionId = journalEntries.get(0).getTransactionId();
        for (final JournalEntry journalEntry : journalEntries) {
            JournalEntry reversalJournalEntry;
            String reversalComment = "Reversal entry for Journal Entry with Entry Id  :" + journalEntry.getId() + " and transaction Id "
                    + journalEntry.getTransactionId();
            if (journalEntry.isDebitEntry()) {
                reversalJournalEntry = JournalEntry.createNew(journalEntry.getOffice(), journalEntry.getPaymentDetail(),
                        journalEntry.getGlAccount(), journalEntry.getCurrencyCode(), journalEntry.getTransactionId(), Boolean.FALSE,
                        reversalTransactionDate, JournalEntryType.CREDIT, journalEntry.getAmount(), reversalComment,
                        journalEntry.getEntityType(), journalEntry.getEntityId(), journalEntry.getReferenceNumber(),
                        journalEntry.getLoanTransactionId(), journalEntry.getSavingsTransactionId(), journalEntry.getClientTransactionId(),
                        journalEntry.getShareTransactionId());
            } else {
                reversalJournalEntry = JournalEntry.createNew(journalEntry.getOffice(), journalEntry.getPaymentDetail(),
                        journalEntry.getGlAccount(), journalEntry.getCurrencyCode(), journalEntry.getTransactionId(), Boolean.FALSE,
                        reversalTransactionDate, JournalEntryType.DEBIT, journalEntry.getAmount(), reversalComment,
                        journalEntry.getEntityType(), journalEntry.getEntityId(), journalEntry.getReferenceNumber(),
                        journalEntry.getLoanTransactionId(), journalEntry.getSavingsTransactionId(), journalEntry.getClientTransactionId(),
                        journalEntry.getShareTransactionId());
            }
            // save the reversal entry
            helper.persistJournalEntry(reversalJournalEntry);
            journalEntry.setReversalJournalEntry(reversalJournalEntry);
            journalEntry.setReversed(true);
            // save the updated journal entry
            helper.persistJournalEntry(journalEntry);
        }
        return reversalTransactionId;

    }

    @Override
    public String createProvisioningJournalEntries(ProvisioningEntry provisioningEntry) {
        Collection<LoanProductProvisioningEntry> provisioningEntries = provisioningEntry.getLoanProductProvisioningEntries();
        Map<OfficeCurrencyKey, List<LoanProductProvisioningEntry>> officeMap = new HashMap<>();

        for (LoanProductProvisioningEntry entry : provisioningEntries) {
            OfficeCurrencyKey key = new OfficeCurrencyKey(entry.getOffice(), entry.getCurrencyCode());
            if (officeMap.containsKey(key)) {
                List<LoanProductProvisioningEntry> list = officeMap.get(key);
                list.add(entry);
            } else {
                List<LoanProductProvisioningEntry> list = new ArrayList<>();
                list.add(entry);
                officeMap.put(key, list);
            }
        }

        Map<GLAccount, BigDecimal> liabilityMap = new HashMap<>();
        Map<GLAccount, BigDecimal> expenseMap = new HashMap<>();

        for (Map.Entry<OfficeCurrencyKey, List<LoanProductProvisioningEntry>> entry : officeMap.entrySet()) {
            liabilityMap.clear();
            expenseMap.clear();
            for (LoanProductProvisioningEntry lppEntry : entry.getValue()) {
                if (liabilityMap.containsKey(lppEntry.getLiabilityAccount())) {
                    BigDecimal amount = liabilityMap.get(lppEntry.getLiabilityAccount());
                    amount = amount.add(lppEntry.getReservedAmount());
                    liabilityMap.put(lppEntry.getLiabilityAccount(), amount);
                } else {
                    BigDecimal amount = BigDecimal.ZERO.add(lppEntry.getReservedAmount());
                    liabilityMap.put(lppEntry.getLiabilityAccount(), amount);
                }

                if (expenseMap.containsKey(lppEntry.getExpenseAccount())) {
                    BigDecimal amount = expenseMap.get(lppEntry.getExpenseAccount());
                    amount = amount.add(lppEntry.getReservedAmount());
                    expenseMap.put(lppEntry.getExpenseAccount(), amount);
                } else {
                    BigDecimal amount = BigDecimal.ZERO.add(lppEntry.getReservedAmount());
                    expenseMap.put(lppEntry.getExpenseAccount(), amount);
                }
            }
            createJournalEntry(provisioningEntry.getCreatedDate(), provisioningEntry.getId(), entry.getKey().office,
                    entry.getKey().currency, liabilityMap, expenseMap);
        }
        return "P" + provisioningEntry.getId();
    }

    private void createJournalEntry(LocalDate transactionDate, Long entryId, Office office, String currencyCode,
            Map<GLAccount, BigDecimal> liabilityMap, Map<GLAccount, BigDecimal> expenseMap) {
        for (Map.Entry<GLAccount, BigDecimal> entry : liabilityMap.entrySet()) {
            this.helper.createProvisioningCreditJournalEntry(transactionDate, entryId, office, currencyCode, entry.getKey(),
                    entry.getValue());
        }
        for (Map.Entry<GLAccount, BigDecimal> entry : expenseMap.entrySet()) {
            this.helper.createProvisioningDebitJournalEntry(transactionDate, entryId, office, currencyCode, entry.getKey(),
                    entry.getValue());
        }
    }

    private void validateCommentForReversal(final String reversalComment) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("GLJournalEntry");

        baseDataValidator.reset().parameter("comments").value(reversalComment).notExceedingLengthOf(500);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    @Transactional
    @Override
    public void createJournalEntriesForLoan(final Map<String, Object> accountingBridgeData) {

        final boolean cashBasedAccountingEnabled = (Boolean) accountingBridgeData.get("cashBasedAccountingEnabled");
        final boolean upfrontAccrualBasedAccountingEnabled = (Boolean) accountingBridgeData.get("upfrontAccrualBasedAccountingEnabled");
        final boolean periodicAccrualBasedAccountingEnabled = (Boolean) accountingBridgeData.get("periodicAccrualBasedAccountingEnabled");

        if (cashBasedAccountingEnabled || upfrontAccrualBasedAccountingEnabled || periodicAccrualBasedAccountingEnabled) {
            final LoanDTO loanDTO = this.helper.populateLoanDtoFromMap(accountingBridgeData, cashBasedAccountingEnabled,
                    upfrontAccrualBasedAccountingEnabled, periodicAccrualBasedAccountingEnabled);
            final AccountingProcessorForLoan accountingProcessorForLoan = this.accountingProcessorForLoanFactory
                    .determineProcessor(loanDTO);
            accountingProcessorForLoan.createJournalEntriesForLoan(loanDTO);
        }
    }

    @Transactional
    @Override
    public void createJournalEntriesForSavings(final Map<String, Object> accountingBridgeData) {

        final boolean cashBasedAccountingEnabled = (Boolean) accountingBridgeData.get("cashBasedAccountingEnabled");
        final boolean accrualBasedAccountingEnabled = (Boolean) accountingBridgeData.get("accrualBasedAccountingEnabled");

        if (cashBasedAccountingEnabled || accrualBasedAccountingEnabled) {
            final SavingsDTO savingsDTO = this.helper.populateSavingsDtoFromMap(accountingBridgeData, cashBasedAccountingEnabled,
                    accrualBasedAccountingEnabled);
            final AccountingProcessorForSavings accountingProcessorForSavings = this.accountingProcessorForSavingsFactory
                    .determineProcessor(savingsDTO);
            accountingProcessorForSavings.createJournalEntriesForSavings(savingsDTO);
        }
    }

    @Transactional
    @Override
    public void createJournalEntriesForShares(final Map<String, Object> accountingBridgeData) {

        final boolean cashBasedAccountingEnabled = (Boolean) accountingBridgeData.get("cashBasedAccountingEnabled");
        final boolean accrualBasedAccountingEnabled = (Boolean) accountingBridgeData.get("accrualBasedAccountingEnabled");

        if (cashBasedAccountingEnabled) {
            final SharesDTO sharesDTO = this.helper.populateSharesDtoFromMap(accountingBridgeData, cashBasedAccountingEnabled,
                    accrualBasedAccountingEnabled);
            final AccountingProcessorForShares accountingProcessorForShares = this.accountingProcessorForSharesFactory
                    .determineProcessor(sharesDTO);
            accountingProcessorForShares.createJournalEntriesForShares(sharesDTO);
        }

    }

    @Override
    public void revertShareAccountJournalEntries(final ArrayList<Long> transactionIds, final LocalDate transactionDate) {
        for (Long shareTransactionId : transactionIds) {
            String transactionId = AccountingProcessorHelper.SHARE_TRANSACTION_IDENTIFIER + shareTransactionId;
            List<JournalEntry> journalEntries = this.glJournalEntryRepository.findJournalEntries(transactionId,
                    PortfolioProductType.SHARES.getValue());
            if (journalEntries == null || journalEntries.isEmpty()) {
                continue;
            }
            final Long officeId = journalEntries.get(0).getOffice().getId();
            final String reversalTransactionId = generateTransactionId(officeId);
            for (final JournalEntry journalEntry : journalEntries) {
                JournalEntry reversalJournalEntry;
                String reversalComment = "Reversal entry for Journal Entry with id  :" + journalEntry.getId() + " and transaction Id "
                        + journalEntry.getTransactionId();
                if (journalEntry.isDebitEntry()) {
                    reversalJournalEntry = JournalEntry.createNew(journalEntry.getOffice(), journalEntry.getPaymentDetail(),
                            journalEntry.getGlAccount(), journalEntry.getCurrencyCode(), reversalTransactionId, Boolean.FALSE,
                            transactionDate, JournalEntryType.CREDIT, journalEntry.getAmount(), reversalComment,
                            journalEntry.getEntityType(), journalEntry.getEntityId(), journalEntry.getReferenceNumber(),
                            journalEntry.getLoanTransactionId(), journalEntry.getSavingsTransactionId(),
                            journalEntry.getClientTransactionId(), journalEntry.getShareTransactionId());
                } else {
                    reversalJournalEntry = JournalEntry.createNew(journalEntry.getOffice(), journalEntry.getPaymentDetail(),
                            journalEntry.getGlAccount(), journalEntry.getCurrencyCode(), reversalTransactionId, Boolean.FALSE,
                            transactionDate, JournalEntryType.DEBIT, journalEntry.getAmount(), reversalComment,
                            journalEntry.getEntityType(), journalEntry.getEntityId(), journalEntry.getReferenceNumber(),
                            journalEntry.getLoanTransactionId(), journalEntry.getSavingsTransactionId(),
                            journalEntry.getClientTransactionId(), journalEntry.getShareTransactionId());
                }
                // save the reversal entry
                helper.persistJournalEntry(reversalJournalEntry);
                journalEntry.setReversalJournalEntry(reversalJournalEntry);
                journalEntry.setReversed(true);
                // save the updated journal entry
                helper.persistJournalEntry(journalEntry);
            }
        }
    }

    private void validateBusinessRulesForJournalEntries(final JournalEntryCommand command) {
        // check if date of Journal entry is valid
        final LocalDate transactionDate = command.getTransactionDate();
        // shouldn't be in the future
        if (DateUtils.isDateInTheFuture(transactionDate)) {
            throw new JournalEntryInvalidException(GlJournalEntryInvalidReason.FUTURE_DATE, transactionDate, null, null);
        }
        // shouldn't be before an accounting closure
        final GLClosure latestGLClosure = this.glClosureRepository.getLatestGLClosureByBranch(command.getOfficeId());
        if (latestGLClosure != null) {
            if (!DateUtils.isBefore(latestGLClosure.getClosingDate(), transactionDate)) {
                throw new JournalEntryInvalidException(GlJournalEntryInvalidReason.ACCOUNTING_CLOSED, latestGLClosure.getClosingDate(),
                        null, null);
            }
        }

        // check if credits and debits are valid
        final SingleDebitOrCreditEntryCommand[] credits = command.getCredits();
        final SingleDebitOrCreditEntryCommand[] debits = command.getDebits();

        // atleast one debit or credit must be present
        if (credits == null || credits.length == 0 || debits == null || debits.length == 0) {
            throw new JournalEntryInvalidException(GlJournalEntryInvalidReason.NO_DEBITS_OR_CREDITS, null, null, null);
        }

        checkDebitAndCreditAmounts(credits, debits);
    }

    private void saveAllDebitOrCreditEntries(final JournalEntryCommand command, final Office office, final PaymentDetail paymentDetail,
            final String currencyCode, final LocalDate transactionDate,
            final SingleDebitOrCreditEntryCommand[] singleDebitOrCreditEntryCommands, final String transactionId,
            final JournalEntryType type, final String referenceNumber) {
        final boolean manualEntry = true;
        for (final SingleDebitOrCreditEntryCommand singleDebitOrCreditEntryCommand : singleDebitOrCreditEntryCommands) {
            final GLAccount glAccount = this.glAccountRepository.findById(singleDebitOrCreditEntryCommand.getGlAccountId())
                    .orElseThrow(() -> new GLAccountNotFoundException(singleDebitOrCreditEntryCommand.getGlAccountId()));

            validateGLAccountForTransaction(glAccount);

            String comments = command.getComments();
            if (!StringUtils.isBlank(singleDebitOrCreditEntryCommand.getComments())) {
                comments = singleDebitOrCreditEntryCommand.getComments();
            }

            /** Validate current code is appropriate **/
            this.organisationCurrencyRepository.findOneWithNotFoundDetection(currencyCode);

            final JournalEntry glJournalEntry = JournalEntry.createNew(office, paymentDetail, glAccount, currencyCode, transactionId,
                    manualEntry, transactionDate, type, singleDebitOrCreditEntryCommand.getAmount(), comments, null, null, referenceNumber,
                    null, null, null, null);
            helper.persistJournalEntry(glJournalEntry);
        }
    }

    /**
     * TODO: Need a better implementation with guaranteed uniqueness (but not a long UUID)...maybe something tied to
     * system clock..
     */
    private String generateTransactionId(final Long officeId) {
        final AppUser user = this.context.authenticatedUser();
        final Long time = System.currentTimeMillis();
        final String uniqueVal = String.valueOf(time) + user.getId() + officeId;
        return Long.toHexString(Long.parseLong(uniqueVal));
    }

    private PlatformDataIntegrityException handleJournalEntryDataIntegrityIssues(final Throwable realCause,
            final NonTransientDataAccessException dve) {
        log.error("Error occurred.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.glJournalEntry.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource Journal Entry: " + realCause.getMessage());
    }

    @Transactional
    @Override
    public CommandProcessingResult defineOpeningBalance(final JsonCommand command) {
        try {
            final JournalEntryCommand journalEntryCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            journalEntryCommand.validateForCreate();

            final FinancialActivityAccount financialActivityAccountId = this.financialActivityAccountRepositoryWrapper
                    .findByFinancialActivityTypeWithNotFoundDetection(300);
            final Long contraId = financialActivityAccountId.getGlAccount().getId();
            if (contraId == null) {
                throw new GeneralPlatformDomainRuleException(
                        "error.msg.financial.activity.mapping.opening.balance.contra.account.cannot.be.null",
                        "office-opening-balances-contra-account value can not be null", "office-opening-balances-contra-account");
            }

            validateJournalEntriesArePostedBefore(contraId);

            // check office is valid
            final Long officeId = command.longValueOfParameterNamed(JournalEntryJsonInputParams.OFFICE_ID.getValue());
            final Office office = this.officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);
            final String currencyCode = command.stringValueOfParameterNamed(JournalEntryJsonInputParams.CURRENCY_CODE.getValue());

            validateBusinessRulesForJournalEntries(journalEntryCommand);

            /**
             * revert old journal entries
             */
            final List<String> transactionIdsToBeReversed = this.glJournalEntryRepository.findNonReversedContraTransactionIds(contraId,
                    officeId);
            for (String transactionId : transactionIdsToBeReversed) {
                final List<JournalEntry> journalEntries = this.glJournalEntryRepository
                        .findUnReversedManualJournalEntriesByTransactionId(transactionId);
                revertJournalEntry(journalEntries, "defining opening balance");
            }

            /** Set a transaction Id and save these Journal entries **/
            final LocalDate transactionDate = command
                    .localDateValueOfParameterNamed(JournalEntryJsonInputParams.TRANSACTION_DATE.getValue());
            final String transactionId = generateTransactionId(officeId);

            saveAllDebitOrCreditOpeningBalanceEntries(journalEntryCommand, office, currencyCode, transactionDate,
                    journalEntryCommand.getDebits(), transactionId, JournalEntryType.DEBIT, contraId);

            saveAllDebitOrCreditOpeningBalanceEntries(journalEntryCommand, office, currencyCode, transactionDate,
                    journalEntryCommand.getCredits(), transactionId, JournalEntryType.CREDIT, contraId);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withOfficeId(officeId)
                    .withTransactionId(transactionId).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            throw handleJournalEntryDataIntegrityIssues(throwable, dve);
        }
    }

    private void saveAllDebitOrCreditOpeningBalanceEntries(final JournalEntryCommand command, final Office office,
            final String currencyCode, final LocalDate transactionDate,
            final SingleDebitOrCreditEntryCommand[] singleDebitOrCreditEntryCommands, final String transactionId,
            final JournalEntryType type, final Long contraAccountId) {

        final boolean manualEntry = true;
        final GLAccount contraAccount = this.glAccountRepository.findById(contraAccountId)
                .orElseThrow(() -> new GLAccountNotFoundException(contraAccountId));
        if (!GLAccountType.fromInt(contraAccount.getType()).isEquityType()) {
            throw new GeneralPlatformDomainRuleException(
                    "error.msg.configuration.opening.balance.contra.account.value.is.invalid.account.type",
                    "Global configuration 'office-opening-balances-contra-account' value is not an equity type account", contraAccountId);
        }
        validateGLAccountForTransaction(contraAccount);
        final JournalEntryType contraType = getContraType(type);
        String comments = command.getComments();

        /** Validate current code is appropriate **/
        this.organisationCurrencyRepository.findOneWithNotFoundDetection(currencyCode);

        for (final SingleDebitOrCreditEntryCommand singleDebitOrCreditEntryCommand : singleDebitOrCreditEntryCommands) {
            final GLAccount glAccount = this.glAccountRepository.findById(singleDebitOrCreditEntryCommand.getGlAccountId())
                    .orElseThrow(() -> new GLAccountNotFoundException(singleDebitOrCreditEntryCommand.getGlAccountId()));

            validateGLAccountForTransaction(glAccount);

            if (!StringUtils.isBlank(singleDebitOrCreditEntryCommand.getComments())) {
                comments = singleDebitOrCreditEntryCommand.getComments();
            }

            final JournalEntry glJournalEntry = JournalEntry.createNew(office, null, glAccount, currencyCode, transactionId, manualEntry,
                    transactionDate, type, singleDebitOrCreditEntryCommand.getAmount(), comments, null, null, null, null, null, null, null);
            helper.persistJournalEntry(glJournalEntry);

            final JournalEntry contraEntry = JournalEntry.createNew(office, null, contraAccount, currencyCode, transactionId, manualEntry,
                    transactionDate, contraType, singleDebitOrCreditEntryCommand.getAmount(), comments, null, null, null, null, null, null,
                    null);
            helper.persistJournalEntry(contraEntry);
        }
    }

    private JournalEntryType getContraType(final JournalEntryType type) {
        final JournalEntryType contraType;
        if (type.isCreditType()) {
            contraType = JournalEntryType.DEBIT;
        } else {
            contraType = JournalEntryType.CREDIT;
        }
        return contraType;
    }

    private void validateJournalEntriesArePostedBefore(final Long contraId) {
        final List<String> transactionIds = this.glJournalEntryRepository.findNonContraTransactionIds(contraId);
        if (!CollectionUtils.isEmpty(transactionIds)) {
            throw new GeneralPlatformDomainRuleException("error.msg.journalentry.defining.openingbalance.not.allowed",
                    "Defining Opening balances not allowed after journal entries posted", transactionIds);
        }
    }

    @Override
    public void createJournalEntriesForClientTransactions(Map<String, Object> accountingBridgeData) {
        final ClientTransactionDTO clientTransactionDTO = this.helper.populateClientTransactionDtoFromMap(accountingBridgeData);
        accountingProcessorForClientTransactions.createJournalEntriesForClientTransaction(clientTransactionDTO);
    }

    private static class OfficeCurrencyKey {

        final Office office;
        final String currency;

        OfficeCurrencyKey(Office office, String currency) {
            this.office = office;
            this.currency = currency;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof OfficeCurrencyKey copy)) {
                return false;
            }
            return Objects.equals(this.office.getId(), copy.office.getId()) && this.currency.equals(copy.currency);
        }

        @Override
        public int hashCode() {
            return this.office.hashCode() + this.currency.hashCode();
        }
    }

}
