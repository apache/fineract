/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.rule.service;

import java.util.Map;

import org.mifosplatform.accounting.closure.api.GLClosureJsonInputParams;
import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.mifosplatform.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.mifosplatform.accounting.rule.api.AccountingRuleJsonInputParams;
import org.mifosplatform.accounting.rule.command.AccountingRuleCommand;
import org.mifosplatform.accounting.rule.domain.AccountingRule;
import org.mifosplatform.accounting.rule.domain.AccountingRuleRepository;
import org.mifosplatform.accounting.rule.domain.AccountingRuleRepositoryWrapper;
import org.mifosplatform.accounting.rule.exception.AccountingRuleDuplicateException;
import org.mifosplatform.accounting.rule.serialization.AccountingRuleCommandFromApiJsonDeserializer;
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

    @Autowired
    public AccountingRuleWritePlatformServiceJpaRepositoryImpl(final AccountingRuleRepositoryWrapper accountingRuleRepositoryWrapper,
            final GLAccountRepositoryWrapper accountRepositoryWrapper, final OfficeRepository officeRepository,
            final AccountingRuleCommandFromApiJsonDeserializer fromApiJsonDeserializer, final AccountingRuleRepository ruleRepository) {
        this.accountRepositoryWrapper = accountRepositoryWrapper;
        this.officeRepository = officeRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.accountingRuleRepository = ruleRepository;
        this.accountingRuleRepositoryWrapper = accountingRuleRepositoryWrapper;
    }

    /**
     * @param command
     * @param dve
     */
    private void handleAccountingRuleIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("accounting_rule_name_unique")) { throw new AccountingRuleDuplicateException(
                command.stringValueOfParameterNamed(AccountingRuleJsonInputParams.NAME.getValue())); }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.accounting.rule.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource Accounting Rule: " + realCause.getMessage());
    }

    @Transactional
    @Override
    public CommandProcessingResult createAccountingRule(JsonCommand command) {
        try {
            final AccountingRuleCommand accountingRuleCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            accountingRuleCommand.validateForCreate();

            // check office is valid
            final Long officeId = command.longValueOfParameterNamed(GLClosureJsonInputParams.OFFICE_ID.getValue());
            Office office = null;
            if (officeId != null) {
                office = this.officeRepository.findOne(officeId);
                if (office == null) { throw new OfficeNotFoundException(officeId); }
            }

            // get the GL Accounts to Debit and Credit
            final Long accountToDebitId = command.longValueOfParameterNamed(AccountingRuleJsonInputParams.ACCOUNT_TO_DEBIT.getValue());
            final Long accountToCreditId = command.longValueOfParameterNamed(AccountingRuleJsonInputParams.ACCOUNT_TO_CREDIT.getValue());
            final GLAccount accountToDebit = accountRepositoryWrapper.findOneWithNotFoundDetection(accountToDebitId);
            final GLAccount accountToCredit = accountRepositoryWrapper.findOneWithNotFoundDetection(accountToCreditId);

            final AccountingRule accountingRule = AccountingRule.fromJson(office, accountToDebit, accountToCredit, command);
            this.accountingRuleRepository.saveAndFlush(accountingRule);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withOfficeId(officeId)
                    .withEntityId(accountingRule.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleAccountingRuleIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateAccountingRule(Long accountingRuleId, JsonCommand command) {
        final AccountingRuleCommand accountingRuleCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
        accountingRuleCommand.validateForUpdate();

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

        final AccountingRule accountingRule = this.accountingRuleRepositoryWrapper.findOneWithNotFoundDetection(accountingRuleId);
        final Map<String, Object> changesOnly = accountingRule.update(command);

        if (changesOnly.containsKey(AccountingRuleJsonInputParams.ACCOUNT_TO_DEBIT.getValue())) {
            final GLAccount accountToDebit = accountRepositoryWrapper.findOneWithNotFoundDetection(accountToDebitId);
            accountingRule.setAccountToDebit(accountToDebit);
        }

        if (changesOnly.containsKey(AccountingRuleJsonInputParams.ACCOUNT_TO_CREDIT.getValue())) {
            final GLAccount accountToCredit = accountRepositoryWrapper.findOneWithNotFoundDetection(accountToCreditId);
            accountingRule.setAccountToCredit(accountToCredit);
        }

        if (changesOnly.containsKey(AccountingRuleJsonInputParams.OFFICE_ID.getValue())) {
            final Office userOffice = this.officeRepository.findOne(officeId);
            if (userOffice == null) { throw new OfficeNotFoundException(officeId); }
            accountingRule.setOffice(userOffice);
        }

        if (!changesOnly.isEmpty()) {
            this.accountingRuleRepository.saveAndFlush(accountingRule);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(accountingRule.getId())
                .with(changesOnly).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteAccountingRule(Long accountingRuleId) {
        final AccountingRule accountingRule = this.accountingRuleRepositoryWrapper.findOneWithNotFoundDetection(accountingRuleId);
        this.accountingRuleRepository.delete(accountingRule);
        return new CommandProcessingResultBuilder().withEntityId(accountingRule.getId()).build();
    }
}
