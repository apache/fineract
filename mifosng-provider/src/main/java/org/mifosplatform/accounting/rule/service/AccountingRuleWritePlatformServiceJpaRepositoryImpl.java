/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.rule.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.accounting.closure.api.GLClosureJsonInputParams;
import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.mifosplatform.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.mifosplatform.accounting.journalentry.domain.JournalEntryType;
import org.mifosplatform.accounting.rule.api.AccountingRuleJsonInputParams;
import org.mifosplatform.accounting.rule.domain.AccountingRule;
import org.mifosplatform.accounting.rule.domain.AccountingRuleRepository;
import org.mifosplatform.accounting.rule.domain.AccountingRuleRepositoryWrapper;
import org.mifosplatform.accounting.rule.domain.AccountingTagRule;
import org.mifosplatform.accounting.rule.exception.AccountingRuleDataException;
import org.mifosplatform.accounting.rule.exception.AccountingRuleDuplicateException;
import org.mifosplatform.accounting.rule.serialization.AccountingRuleCommandFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepository;
import org.mifosplatform.infrastructure.codes.exception.CodeValueNotFoundException;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.organisation.office.exception.OfficeNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountingRuleWritePlatformServiceJpaRepositoryImpl implements AccountingRuleWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(AccountingRuleWritePlatformServiceJpaRepositoryImpl.class);

    private final AccountingRuleRepositoryWrapper accountingRuleRepositoryWrapper;
    private final AccountingRuleRepository accountingRuleRepository;
    private final GLAccountRepositoryWrapper accountRepositoryWrapper;
    private final OfficeRepository officeRepository;
    private final AccountingRuleCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final CodeValueRepository codeValueRepository;

    @Autowired
    public AccountingRuleWritePlatformServiceJpaRepositoryImpl(final AccountingRuleRepositoryWrapper accountingRuleRepositoryWrapper,
            final GLAccountRepositoryWrapper accountRepositoryWrapper, final OfficeRepository officeRepository,
            final AccountingRuleRepository ruleRepository, final AccountingRuleCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final CodeValueRepository codeValueRepository) {
        this.accountRepositoryWrapper = accountRepositoryWrapper;
        this.officeRepository = officeRepository;
        this.accountingRuleRepository = ruleRepository;
        this.accountingRuleRepositoryWrapper = accountingRuleRepositoryWrapper;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.codeValueRepository = codeValueRepository;
    }

    /**
     * @param command
     * @param dve
     */
    private void handleAccountingRuleIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("accounting_rule_name_unique")) {
            throw new AccountingRuleDuplicateException(command.stringValueOfParameterNamed(AccountingRuleJsonInputParams.NAME.getValue()));
        } else if (realCause.getMessage().contains("UNIQUE_ACCOUNT_RULE_TAGS")) { throw new AccountingRuleDuplicateException(); }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.accounting.rule.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource Accounting Rule: " + realCause.getMessage());
    }

    @Transactional
    @Override
    public CommandProcessingResult createAccountingRule(final JsonCommand command) {
        try {

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            // check office is valid
            final Long officeId = command.longValueOfParameterNamed(GLClosureJsonInputParams.OFFICE_ID.getValue());
            Office office = null;
            if (officeId != null) {
                office = this.officeRepository.findOne(officeId);
                if (office == null) { throw new OfficeNotFoundException(officeId); }
            }

            final AccountingRule accountingRule = assembleAccountingRuleAndTags(office, command);
            this.accountingRuleRepository.saveAndFlush(accountingRule);
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withOfficeId(officeId)
                    .withEntityId(accountingRule.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleAccountingRuleIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    private AccountingRule assembleAccountingRuleAndTags(final Office office, final JsonCommand command) {
        // get the GL Accounts or tags to Debit and Credit
        final String[] debitTags = command.arrayValueOfParameterNamed(AccountingRuleJsonInputParams.DEBIT_ACCOUNT_TAGS.getValue());
        final String[] creditTags = command.arrayValueOfParameterNamed(AccountingRuleJsonInputParams.CREDIT_ACCOUNT_TAGS.getValue());
        final Set<String> incomingDebitTags = debitTags == null ? new HashSet<String>() : new HashSet<>(Arrays.asList(debitTags));
        final Set<String> incomingCreditTags = creditTags == null ? new HashSet<String>() : new HashSet<>(Arrays.asList(creditTags));
        final Long accountToDebitId = command.longValueOfParameterNamed(AccountingRuleJsonInputParams.ACCOUNT_TO_DEBIT.getValue());
        final Long accountToCreditId = command.longValueOfParameterNamed(AccountingRuleJsonInputParams.ACCOUNT_TO_CREDIT.getValue());

        boolean allowMultipleCreditEntries = false;
        boolean allowMultipleDebitEntries = false;
        GLAccount debitAccount = null;
        GLAccount creditAccount = null;
        List<AccountingTagRule> accountingTagRules = new ArrayList<>();

        if ((accountToDebitId != null && debitTags != null) || (accountToDebitId == null && debitTags == null)) {
            throw new AccountingRuleDataException(AccountingRuleJsonInputParams.ACCOUNT_TO_DEBIT.getValue(),
                    AccountingRuleJsonInputParams.DEBIT_ACCOUNT_TAGS.getValue());
        } else if (accountToDebitId != null) {
            debitAccount = this.accountRepositoryWrapper.findOneWithNotFoundDetection(accountToDebitId);
        } else if (debitTags != null) {
            accountingTagRules = saveDebitOrCreditTags(incomingDebitTags, JournalEntryType.DEBIT, accountingTagRules);
            allowMultipleDebitEntries = command
                    .booleanPrimitiveValueOfParameterNamed(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_DEBIT_ENTRIES.getValue());
        }

        if ((accountToCreditId != null && creditTags != null) || (accountToCreditId == null && creditTags == null)) {
            throw new AccountingRuleDataException(AccountingRuleJsonInputParams.ACCOUNT_TO_CREDIT.getValue(),
                    AccountingRuleJsonInputParams.CREDIT_ACCOUNT_TAGS.getValue());
        } else if (accountToCreditId != null) {
            creditAccount = this.accountRepositoryWrapper.findOneWithNotFoundDetection(accountToCreditId);
        } else if (creditTags != null) {
            accountingTagRules = saveDebitOrCreditTags(incomingCreditTags, JournalEntryType.CREDIT, accountingTagRules);
            allowMultipleCreditEntries = command
                    .booleanPrimitiveValueOfParameterNamed(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_CREDIT_ENTRIES.getValue());
        }

        final AccountingRule accountingRule = AccountingRule.fromJson(office, debitAccount, creditAccount, command,
                allowMultipleCreditEntries, allowMultipleDebitEntries);
        accountingRule.updateAccountingRuleForTags(accountingTagRules);

        return accountingRule;
    }

    @Transactional
    @Override
    public CommandProcessingResult updateAccountingRule(final Long accountingRuleId, final JsonCommand command) {

        try {

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            Long officeId = null;
            if (command.parameterExists(AccountingRuleJsonInputParams.OFFICE_ID.getValue())) {
                officeId = command.longValueOfParameterNamed(AccountingRuleJsonInputParams.OFFICE_ID.getValue());
            }

            Long accountToDebitId = null;
            if (command.parameterExists(AccountingRuleJsonInputParams.ACCOUNT_TO_DEBIT.getValue())) {
                accountToDebitId = command.longValueOfParameterNamed(AccountingRuleJsonInputParams.ACCOUNT_TO_DEBIT.getValue());
            }

            Long accountToCreditId = null;
            if (command.parameterExists(AccountingRuleJsonInputParams.ACCOUNT_TO_CREDIT.getValue())) {
                accountToCreditId = command.longValueOfParameterNamed(AccountingRuleJsonInputParams.ACCOUNT_TO_CREDIT.getValue());
            }

            String[] debitTags = null;
            if (command.parameterExists(AccountingRuleJsonInputParams.DEBIT_ACCOUNT_TAGS.getValue())) {
                debitTags = command.arrayValueOfParameterNamed(AccountingRuleJsonInputParams.DEBIT_ACCOUNT_TAGS.getValue());
            }

            String[] creditTags = null;
            if (command.parameterExists(AccountingRuleJsonInputParams.CREDIT_ACCOUNT_TAGS.getValue())) {
                creditTags = command.arrayValueOfParameterNamed(AccountingRuleJsonInputParams.CREDIT_ACCOUNT_TAGS.getValue());
            }

            if (accountToDebitId != null && debitTags != null) { throw new AccountingRuleDataException(
                    AccountingRuleJsonInputParams.ACCOUNT_TO_DEBIT.getValue(), AccountingRuleJsonInputParams.DEBIT_ACCOUNT_TAGS.getValue()); }

            if (accountToCreditId != null && creditTags != null) { throw new AccountingRuleDataException(
                    AccountingRuleJsonInputParams.ACCOUNT_TO_CREDIT.getValue(),
                    AccountingRuleJsonInputParams.CREDIT_ACCOUNT_TAGS.getValue()); }

            boolean allowMultipleCreditEntries = false;
            if (command.parameterExists(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_CREDIT_ENTRIES.getValue())) {
                allowMultipleCreditEntries = command
                        .booleanPrimitiveValueOfParameterNamed(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_CREDIT_ENTRIES.getValue());
            }

            boolean allowMultipleDebitEntries = false;
            if (command.parameterExists(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_DEBIT_ENTRIES.getValue())) {
                allowMultipleDebitEntries = command
                        .booleanPrimitiveValueOfParameterNamed(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_DEBIT_ENTRIES.getValue());
            }

            final AccountingRule accountingRule = this.accountingRuleRepositoryWrapper.findOneWithNotFoundDetection(accountingRuleId);
            final Map<String, Object> changesOnly = accountingRule.update(command);

            if (accountToDebitId != null && changesOnly.containsKey(AccountingRuleJsonInputParams.ACCOUNT_TO_DEBIT.getValue())) {
                final GLAccount accountToDebit = this.accountRepositoryWrapper.findOneWithNotFoundDetection(accountToDebitId);
                accountingRule.updateDebitAccount(accountToDebit);
                accountingRule.updateTags(JournalEntryType.CREDIT);
            }

            if (accountToCreditId != null && changesOnly.containsKey(AccountingRuleJsonInputParams.ACCOUNT_TO_CREDIT.getValue())) {
                final GLAccount accountToCredit = this.accountRepositoryWrapper.findOneWithNotFoundDetection(accountToCreditId);
                accountingRule.updateCreditAccount(accountToCredit);
                accountingRule.updateTags(JournalEntryType.DEBIT);
            }

            if (creditTags != null && creditTags.length > 0
                    && command.parameterExists(AccountingRuleJsonInputParams.CREDIT_ACCOUNT_TAGS.getValue())) {

                final Set<String> creditTagsToAdd = determineCreditTagToAddAndRemoveOldTags(creditTags, JournalEntryType.CREDIT,
                        accountingRule);

                if (!creditTagsToAdd.isEmpty()) {
                    List<AccountingTagRule> accountingTagRules = new ArrayList<>();
                    accountingTagRules = saveDebitOrCreditTags(creditTagsToAdd, JournalEntryType.CREDIT, accountingTagRules);
                    accountingRule.updateAccountingRuleForTags(accountingTagRules);
                    accountingRule.updateCreditAccount(null);
                    if (allowMultipleCreditEntries) {
                        accountingRule.updateAllowMultipleCreditEntries(allowMultipleCreditEntries);
                    }
                    changesOnly.put(AccountingRuleJsonInputParams.CREDIT_ACCOUNT_TAGS.getValue(), creditTagsToAdd);
                }

            }

            if (debitTags != null && debitTags.length > 0
                    && command.parameterExists(AccountingRuleJsonInputParams.DEBIT_ACCOUNT_TAGS.getValue())) {
                final Set<String> debitTagsToAdd = determineCreditTagToAddAndRemoveOldTags(debitTags, JournalEntryType.DEBIT,
                        accountingRule);
                if (!debitTagsToAdd.isEmpty()) {
                    List<AccountingTagRule> accountingTagRules = new ArrayList<>();
                    accountingTagRules = saveDebitOrCreditTags(debitTagsToAdd, JournalEntryType.DEBIT, accountingTagRules);
                    accountingRule.updateAccountingRuleForTags(accountingTagRules);
                    accountingRule.updateDebitAccount(null);
                    if (allowMultipleDebitEntries) {
                        accountingRule.updateAllowMultipleDebitEntries(allowMultipleDebitEntries);
                    }
                    changesOnly.put(AccountingRuleJsonInputParams.DEBIT_ACCOUNT_TAGS.getValue(), debitTagsToAdd);
                }
            }

            if (officeId != null && changesOnly.containsKey(AccountingRuleJsonInputParams.OFFICE_ID.getValue())) {
                final Office userOffice = this.officeRepository.findOne(officeId);
                if (userOffice == null) { throw new OfficeNotFoundException(officeId); }
                accountingRule.setOffice(userOffice);
            }

            if (!changesOnly.isEmpty()) {
                this.accountingRuleRepository.saveAndFlush(accountingRule);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(accountingRule.getId())
                    .with(changesOnly).build();
        } catch (final DataIntegrityViolationException dve) {
            handleAccountingRuleIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }

    }

    private Set<String> determineCreditTagToAddAndRemoveOldTags(final String[] creditOrDebitTags, final JournalEntryType type,
            final AccountingRule accountingRule) {

        final Set<String> incomingTags = new HashSet<>(Arrays.asList(creditOrDebitTags));
        final Set<AccountingTagRule> existingTags = accountingRule.getAccountingTagRulesByType(type);
        final Set<String> existingTagIds = retrieveExistingTagIds(existingTags);
        final Set<String> tagsToAdd = new HashSet<>();
        final Set<String> tagsToRemove = existingTagIds;
        final Map<Long, AccountingTagRule> accountsToRemove = new HashMap<>();

        for (final String tagId : incomingTags) {
            if (existingTagIds.contains(tagId)) {
                tagsToRemove.remove(tagId);
            } else {
                tagsToAdd.add(tagId);
            }
        }

        if (!tagsToRemove.isEmpty()) {
            for (final String tagId : tagsToRemove) {
                for (final AccountingTagRule accountingTagRule : existingTags) {
                    if (tagId.equals(accountingTagRule.getTagId().toString())) {
                        accountsToRemove.put(accountingTagRule.getId(), accountingTagRule);
                    }
                }
            }
            accountingRule.removeOldTags(new ArrayList<>(accountsToRemove.values()));
        }
        return tagsToAdd;
    }

    private Set<String> retrieveExistingTagIds(final Set<AccountingTagRule> existingCreditTags) {
        final Set<String> existingCreditTagIds = new HashSet<>();
        for (final AccountingTagRule accountingTagRule : existingCreditTags) {
            existingCreditTagIds.add(accountingTagRule.getTagId().toString());
        }
        return existingCreditTagIds;
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteAccountingRule(final Long accountingRuleId) {
        final AccountingRule accountingRule = this.accountingRuleRepositoryWrapper.findOneWithNotFoundDetection(accountingRuleId);
        this.accountingRuleRepository.delete(accountingRule);
        return new CommandProcessingResultBuilder().withEntityId(accountingRule.getId()).build();
    }

    private List<AccountingTagRule> saveDebitOrCreditTags(final Set<String> creditOrDebitTagArray, final JournalEntryType transactionType,
            final List<AccountingTagRule> accountingTagRules) {
        for (final String creditOrDebitTag : creditOrDebitTagArray) {
            if (creditOrDebitTag != null && StringUtils.isNotBlank(creditOrDebitTag)) {
                final Long creditOrDebitTagIdLongValue = Long.valueOf(creditOrDebitTag);
                final CodeValue creditOrDebitAccount = this.codeValueRepository.findOne(creditOrDebitTagIdLongValue);
                if (creditOrDebitAccount == null) { throw new CodeValueNotFoundException(creditOrDebitTagIdLongValue); }
                final AccountingTagRule accountingTagRule = AccountingTagRule.create(creditOrDebitAccount, transactionType.getValue());
                accountingTagRules.add(accountingTagRule);
            }
        }
        return accountingTagRules;
    }
}
