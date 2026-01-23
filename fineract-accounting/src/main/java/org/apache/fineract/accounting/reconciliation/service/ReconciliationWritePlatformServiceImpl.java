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
package org.apache.fineract.accounting.reconciliation.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import jakarta.persistence.PersistenceException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.accounting.journalentry.exception.JournalEntryNotFoundException;
import org.apache.fineract.accounting.reconciliation.api.ReconciliationJsonInputParams;
import org.apache.fineract.accounting.reconciliation.domain.AdjustmentType;
import org.apache.fineract.accounting.reconciliation.domain.BankStatementImport;
import org.apache.fineract.accounting.reconciliation.domain.BankStatementTransaction;
import org.apache.fineract.accounting.reconciliation.domain.MatchType;
import org.apache.fineract.accounting.reconciliation.domain.ReconciliationAdjustment;
import org.apache.fineract.accounting.reconciliation.domain.ReconciliationAuditLog;
import org.apache.fineract.accounting.reconciliation.domain.ReconciliationMatch;
import org.apache.fineract.accounting.reconciliation.domain.ReconciliationRule;
import org.apache.fineract.accounting.reconciliation.domain.repository.BankStatementImportRepository;
import org.apache.fineract.accounting.reconciliation.domain.repository.BankStatementTransactionRepository;
import org.apache.fineract.accounting.reconciliation.domain.repository.ReconciliationAdjustmentRepository;
import org.apache.fineract.accounting.reconciliation.domain.repository.ReconciliationAuditLogRepository;
import org.apache.fineract.accounting.reconciliation.domain.repository.ReconciliationMatchRepository;
import org.apache.fineract.accounting.reconciliation.domain.repository.ReconciliationRuleRepository;
import org.apache.fineract.accounting.reconciliation.exception.ReconciliationNotFoundException;
import org.apache.fineract.accounting.reconciliation.exception.ReconciliationMatchNotFoundException;
import org.apache.fineract.accounting.reconciliation.exception.ReconciliationRuleNotFoundException;
import org.apache.fineract.accounting.reconciliation.serialization.ReconciliationDataValidator;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReconciliationWritePlatformServiceImpl implements ReconciliationWritePlatformService {

    private final PlatformSecurityContext context;
    private final FromJsonHelper fromApiJsonHelper;
    private final ReconciliationDataValidator dataValidator;
    private final BankStatementImportRepository bankStatementImportRepository;
    private final BankStatementTransactionRepository bankStatementTransactionRepository;
    private final ReconciliationMatchRepository reconciliationMatchRepository;
    private final ReconciliationAdjustmentRepository reconciliationAdjustmentRepository;
    private final ReconciliationRuleRepository reconciliationRuleRepository;
    private final ReconciliationAuditLogRepository reconciliationAuditLogRepository;
    private final GLAccountRepositoryWrapper glAccountRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final OfficeRepository officeRepository;
    private final ReconciliationMatchingService matchingService;

    @Override
    @Transactional
    public CommandProcessingResult createImport(JsonCommand command) {
        try {
            this.dataValidator.validateForCreateImport(command.json());

            final AppUser currentUser = this.context.authenticatedUser();
            final Long glAccountId = command.longValueOfParameterNamed(ReconciliationJsonInputParams.GL_ACCOUNT_ID.getValue());
            final GLAccount glAccount = this.glAccountRepository.findOneWithNotFoundDetection(glAccountId);

            final String fileName = command.stringValueOfParameterNamed(ReconciliationJsonInputParams.FILE_NAME.getValue());
            final LocalDate fromDate = command.localDateValueOfParameterNamed(ReconciliationJsonInputParams.FROM_DATE.getValue());
            final LocalDate toDate = command.localDateValueOfParameterNamed(ReconciliationJsonInputParams.TO_DATE.getValue());
            final BigDecimal openingBalance = command
                    .bigDecimalValueOfParameterNamed(ReconciliationJsonInputParams.OPENING_BALANCE.getValue());
            final BigDecimal closingBalance = command
                    .bigDecimalValueOfParameterNamed(ReconciliationJsonInputParams.CLOSING_BALANCE.getValue());

            final Office office = this.officeRepository.findById(currentUser.getOffice().getId()).orElseThrow();

            final BankStatementImport importRecord = BankStatementImport.create(glAccount, office, fromDate, openingBalance,
                    closingBalance, fileName, "CSV", currentUser.getId(), DateUtils.getAuditOffsetDateTime(), null);

            this.bankStatementImportRepository.saveAndFlush(importRecord);

            createAuditLog(importRecord.getId(), "CREATED", "Bank statement import created", currentUser.getId());

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(importRecord.getId()).build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException ee) {
            final Throwable throwable = ExceptionUtils.getRootCause(ee.getCause());
            handleDataIntegrityIssues(command, throwable, ee);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult importTransactions(Long importId, JsonCommand command) {
        try {
            this.dataValidator.validateForImportTransactions(command.json());

            final AppUser currentUser = this.context.authenticatedUser();
            final BankStatementImport importRecord = this.bankStatementImportRepository.findById(importId)
                    .orElseThrow(() -> new ReconciliationNotFoundException(importId));

            final JsonArray transactions = command.arrayOfParameterNamed(ReconciliationJsonInputParams.TRANSACTIONS.getValue());

            int totalTransactions = 0;
            BigDecimal totalDebits = BigDecimal.ZERO;
            BigDecimal totalCredits = BigDecimal.ZERO;

            final List<BankStatementTransaction> transactionList = new ArrayList<>();

            for (JsonElement element : transactions) {
                final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element.getAsJsonObject());
                final LocalDate valueDate = this.fromApiJsonHelper.extractLocalDateNamed("valueDate", element.getAsJsonObject());
                final String description = this.fromApiJsonHelper.extractStringNamed("description", element.getAsJsonObject());
                final String referenceNumber = this.fromApiJsonHelper.extractStringNamed("referenceNumber", element.getAsJsonObject());
                final BigDecimal debitAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("debitAmount",
                        element.getAsJsonObject());
                final BigDecimal creditAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("creditAmount",
                        element.getAsJsonObject());
                final BigDecimal balance = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("balance", element.getAsJsonObject());

                final BankStatementTransaction transaction = BankStatementTransaction.create(importRecord, transactionDate, valueDate,
                        description, referenceNumber, null, debitAmount, creditAmount, balance, null, null);

                transactionList.add(transaction);

                totalTransactions++;
                if (debitAmount != null) {
                    totalDebits = totalDebits.add(debitAmount);
                }
                if (creditAmount != null) {
                    totalCredits = totalCredits.add(creditAmount);
                }
            }

            this.bankStatementTransactionRepository.saveAll(transactionList);

            this.bankStatementImportRepository.saveAndFlush(importRecord);

            createAuditLog(importId, "TRANSACTIONS_IMPORTED", totalTransactions + " transactions imported", currentUser.getId());

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(importId).build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException ee) {
            final Throwable throwable = ExceptionUtils.getRootCause(ee.getCause());
            handleDataIntegrityIssues(command, throwable, ee);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult autoMatch(Long importId) {
        final AppUser currentUser = this.context.authenticatedUser();
        final BankStatementImport importRecord = this.bankStatementImportRepository.findById(importId)
                .orElseThrow(() -> new ReconciliationNotFoundException(importId));

        int exactMatches = this.matchingService.matchExact(importId);
        int ruleMatches = this.matchingService.matchWithRules(importId);
        int totalMatches = exactMatches + ruleMatches;

        this.bankStatementImportRepository.saveAndFlush(importRecord);

        createAuditLog(importId, "AUTO_MATCHED", totalMatches + " automatic matches created (" + exactMatches + " exact, " + ruleMatches
                + " rule-based)", currentUser.getId());

        return new CommandProcessingResultBuilder().withEntityId(importId).build();
    }

    @Override
    @Transactional
    public CommandProcessingResult createMatch(Long importId, JsonCommand command) {
        try {
            this.dataValidator.validateForCreateMatch(command.json());

            final AppUser currentUser = this.context.authenticatedUser();
            final BankStatementImport importRecord = this.bankStatementImportRepository.findById(importId)
                    .orElseThrow(() -> new ReconciliationNotFoundException(importId));

            final Long bankTransactionId = command
                    .longValueOfParameterNamed(ReconciliationJsonInputParams.BANK_TRANSACTION_ID.getValue());
            final Long glEntryId = command.longValueOfParameterNamed(ReconciliationJsonInputParams.GL_ENTRY_ID.getValue());

            final BankStatementTransaction bankTransaction = this.bankStatementTransactionRepository.findById(bankTransactionId)
                    .orElseThrow(() -> new ReconciliationNotFoundException(bankTransactionId));

            final JournalEntry journalEntry = this.journalEntryRepository.findById(glEntryId)
                    .orElseThrow(() -> new JournalEntryNotFoundException(glEntryId));

            final BigDecimal amount = bankTransaction.getAmount();
            final ReconciliationMatch match = ReconciliationMatch.create(importRecord, bankTransaction, journalEntry.getId(),
                    MatchType.MANUAL, BigDecimal.valueOf(100), amount, null, currentUser.getId(), DateUtils.getAuditOffsetDateTime());

            this.reconciliationMatchRepository.saveAndFlush(match);

            bankTransaction.markAsMatched(currentUser.getId(), DateUtils.getAuditOffsetDateTime());
            this.bankStatementTransactionRepository.saveAndFlush(bankTransaction);

            this.bankStatementImportRepository.saveAndFlush(importRecord);

            createAuditLog(importId, "MATCH_CREATED", "Manual match created for transaction " + bankTransactionId, currentUser.getId());

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(match.getId()).build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException ee) {
            final Throwable throwable = ExceptionUtils.getRootCause(ee.getCause());
            handleDataIntegrityIssues(command, throwable, ee);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult removeMatch(Long importId, Long matchId) {
        final AppUser currentUser = this.context.authenticatedUser();
        final BankStatementImport importRecord = this.bankStatementImportRepository.findById(importId)
                .orElseThrow(() -> new ReconciliationNotFoundException(importId));

        final ReconciliationMatch match = this.reconciliationMatchRepository.findById(matchId)
                .orElseThrow(() -> new ReconciliationMatchNotFoundException(matchId));

        final BankStatementTransaction bankTransaction = match.getBankTransaction();
        bankTransaction.unmarkAsMatched();
        this.bankStatementTransactionRepository.saveAndFlush(bankTransaction);

        this.reconciliationMatchRepository.delete(match);

        this.bankStatementImportRepository.saveAndFlush(importRecord);

        createAuditLog(importId, "MATCH_REMOVED", "Match removed for transaction " + bankTransaction.getId(), currentUser.getId());

        return new CommandProcessingResultBuilder().withEntityId(matchId).build();
    }

    @Override
    @Transactional
    public CommandProcessingResult createAdjustment(Long importId, JsonCommand command) {
        try {
            this.dataValidator.validateForCreateAdjustment(command.json());

            final AppUser currentUser = this.context.authenticatedUser();
            final BankStatementImport importRecord = this.bankStatementImportRepository.findById(importId)
                    .orElseThrow(() -> new ReconciliationNotFoundException(importId));

            final LocalDate adjustmentDate = command
                    .localDateValueOfParameterNamed(ReconciliationJsonInputParams.ADJUSTMENT_DATE.getValue());
            final String description = command.stringValueOfParameterNamed(ReconciliationJsonInputParams.DESCRIPTION.getValue());
            final Long debitAccountId = command
                    .longValueOfParameterNamed(ReconciliationJsonInputParams.DEBIT_ACCOUNT_ID.getValue());
            final Long creditAccountId = command
                    .longValueOfParameterNamed(ReconciliationJsonInputParams.CREDIT_ACCOUNT_ID.getValue());
            final BigDecimal amount = command.bigDecimalValueOfParameterNamed(ReconciliationJsonInputParams.AMOUNT.getValue());

            final GLAccount debitAccount = this.glAccountRepository.findOneWithNotFoundDetection(debitAccountId);
            final GLAccount creditAccount = this.glAccountRepository.findOneWithNotFoundDetection(creditAccountId);

            final Office office = this.officeRepository.findById(currentUser.getOffice().getId()).orElseThrow();

            final JournalEntry debitEntry = JournalEntry.createNew(office, null, debitAccount, "USD", null, false, adjustmentDate,
                    JournalEntryType.DEBIT, amount, description, null, null, null, null, null, null, null);

            final JournalEntry creditEntry = JournalEntry.createNew(office, null, creditAccount, "USD", null, false, adjustmentDate,
                    JournalEntryType.CREDIT, amount, description, null, null, null, null, null, null, null);

            this.journalEntryRepository.saveAndFlush(debitEntry);
            this.journalEntryRepository.saveAndFlush(creditEntry);

            final ReconciliationAdjustment adjustment = ReconciliationAdjustment.create(importRecord, AdjustmentType.OTHER, description,
                    amount, debitAccount.getId(), creditAccount.getId(), currentUser.getId(), DateUtils.getAuditOffsetDateTime());

            adjustment.linkJournalEntry(debitEntry.getId());
            this.reconciliationAdjustmentRepository.saveAndFlush(adjustment);

            createAuditLog(importId, "ADJUSTMENT_CREATED", "Adjustment created: " + description, currentUser.getId());

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(adjustment.getId()).build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException ee) {
            final Throwable throwable = ExceptionUtils.getRootCause(ee.getCause());
            handleDataIntegrityIssues(command, throwable, ee);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult completeReconciliation(Long importId, JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();
        final BankStatementImport importRecord = this.bankStatementImportRepository.findById(importId)
                .orElseThrow(() -> new ReconciliationNotFoundException(importId));

        importRecord.complete(currentUser.getId(), DateUtils.getAuditOffsetDateTime());
        this.bankStatementImportRepository.saveAndFlush(importRecord);

        createAuditLog(importId, "COMPLETED", "Reconciliation completed", currentUser.getId());

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(importId).build();
    }

    @Override
    @Transactional
    public CommandProcessingResult approveReconciliation(Long importId, JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();
        final BankStatementImport importRecord = this.bankStatementImportRepository.findById(importId)
                .orElseThrow(() -> new ReconciliationNotFoundException(importId));

        importRecord.approve(currentUser.getId(), DateUtils.getAuditOffsetDateTime());
        this.bankStatementImportRepository.saveAndFlush(importRecord);

        createAuditLog(importId, "APPROVED", "Reconciliation approved", currentUser.getId());

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(importId).build();
    }

    @Override
    @Transactional
    public CommandProcessingResult deleteImport(Long importId) {
        final AppUser currentUser = this.context.authenticatedUser();
        final BankStatementImport importRecord = this.bankStatementImportRepository.findById(importId)
                .orElseThrow(() -> new ReconciliationNotFoundException(importId));

        this.reconciliationMatchRepository.deleteByImportId(importId);
        this.reconciliationAdjustmentRepository.deleteByImportId(importId);
        this.bankStatementTransactionRepository.deleteByImportId(importId);
        this.bankStatementImportRepository.delete(importRecord);

        createAuditLog(importId, "DELETED", "Import and all related data deleted", currentUser.getId());

        return new CommandProcessingResultBuilder().withEntityId(importId).build();
    }

    @Override
    @Transactional
    public CommandProcessingResult createRule(JsonCommand command) {
        try {
            this.dataValidator.validateForCreateRule(command.json());

            final AppUser currentUser = this.context.authenticatedUser();
            final Long glAccountId = command.longValueOfParameterNamed(ReconciliationJsonInputParams.GL_ACCOUNT_ID.getValue());
            final GLAccount glAccount = this.glAccountRepository.findOneWithNotFoundDetection(glAccountId);

            final String ruleName = command.stringValueOfParameterNamed(ReconciliationJsonInputParams.RULE_NAME.getValue());
            final String ruleType = command.stringValueOfParameterNamed(ReconciliationJsonInputParams.RULE_TYPE.getValue());
            final String matchField = command.stringValueOfParameterNamed(ReconciliationJsonInputParams.MATCH_FIELD.getValue());
            final String matchPattern = command.stringValueOfParameterNamed(ReconciliationJsonInputParams.MATCH_PATTERN.getValue());
            final Integer dateToleranceDays = command
                    .integerValueOfParameterNamed(ReconciliationJsonInputParams.DATE_TOLERANCE_DAYS.getValue());
            final BigDecimal amountTolerance = command
                    .bigDecimalValueOfParameterNamed(ReconciliationJsonInputParams.AMOUNT_TOLERANCE.getValue());
            final Integer priority = command.integerValueOfParameterNamed(ReconciliationJsonInputParams.PRIORITY.getValue());

            final ReconciliationRule rule = ReconciliationRule.create(ruleName, null, glAccount.getId(), matchField, matchPattern,
                    dateToleranceDays, amountTolerance, priority, currentUser.getId(), DateUtils.getAuditOffsetDateTime());

            this.reconciliationRuleRepository.saveAndFlush(rule);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(rule.getId()).build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException ee) {
            final Throwable throwable = ExceptionUtils.getRootCause(ee.getCause());
            handleDataIntegrityIssues(command, throwable, ee);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult updateRule(Long ruleId, JsonCommand command) {
        try {
            this.dataValidator.validateForUpdateRule(command.json());

            final ReconciliationRule rule = this.reconciliationRuleRepository.findById(ruleId)
                    .orElseThrow(() -> new ReconciliationRuleNotFoundException(ruleId));

            final Map<String, Object> changes = new HashMap<>();

            if (command.isChangeInStringParameterNamed(ReconciliationJsonInputParams.RULE_NAME.getValue(), rule.getName())) {
                final String newValue = command.stringValueOfParameterNamed(ReconciliationJsonInputParams.RULE_NAME.getValue());
                changes.put(ReconciliationJsonInputParams.RULE_NAME.getValue(), newValue);
                rule.update(newValue, null, null, null, null, null, null);
            }

            if (command.isChangeInStringParameterNamed(ReconciliationJsonInputParams.MATCH_PATTERN.getValue(),
                    rule.getConditionValue())) {
                final String newValue = command.stringValueOfParameterNamed(ReconciliationJsonInputParams.MATCH_PATTERN.getValue());
                changes.put(ReconciliationJsonInputParams.MATCH_PATTERN.getValue(), newValue);
                rule.update(null, null, null, newValue, null, null, null);
            }

            if (command.isChangeInIntegerParameterNamed(ReconciliationJsonInputParams.DATE_TOLERANCE_DAYS.getValue(),
                    rule.getDateToleranceDays())) {
                final Integer newValue = command.integerValueOfParameterNamed(ReconciliationJsonInputParams.DATE_TOLERANCE_DAYS.getValue());
                changes.put(ReconciliationJsonInputParams.DATE_TOLERANCE_DAYS.getValue(), newValue);
                rule.update(null, null, null, null, newValue, null, null);
            }

            if (command.isChangeInBigDecimalParameterNamed(ReconciliationJsonInputParams.AMOUNT_TOLERANCE.getValue(),
                    rule.getAmountTolerance())) {
                final BigDecimal newValue = command
                        .bigDecimalValueOfParameterNamed(ReconciliationJsonInputParams.AMOUNT_TOLERANCE.getValue());
                changes.put(ReconciliationJsonInputParams.AMOUNT_TOLERANCE.getValue(), newValue);
                rule.update(null, null, null, null, null, newValue, null);
            }

            if (command.isChangeInIntegerParameterNamed(ReconciliationJsonInputParams.PRIORITY.getValue(), rule.getPriority())) {
                final Integer newValue = command.integerValueOfParameterNamed(ReconciliationJsonInputParams.PRIORITY.getValue());
                changes.put(ReconciliationJsonInputParams.PRIORITY.getValue(), newValue);
                rule.update(null, null, null, null, null, null, newValue);
            }

            if (command.isChangeInBooleanParameterNamed(ReconciliationJsonInputParams.IS_ACTIVE.getValue(), rule.isActive())) {
                final Boolean newValue = command.booleanPrimitiveValueOfParameterNamed(ReconciliationJsonInputParams.IS_ACTIVE.getValue());
                changes.put(ReconciliationJsonInputParams.IS_ACTIVE.getValue(), newValue);
                if (newValue) {
                    rule.activate();
                } else {
                    rule.deactivate();
                }
            }

            if (!changes.isEmpty()) {
                this.reconciliationRuleRepository.saveAndFlush(rule);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(ruleId).with(changes).build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException ee) {
            final Throwable throwable = ExceptionUtils.getRootCause(ee.getCause());
            handleDataIntegrityIssues(command, throwable, ee);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult deleteRule(Long ruleId) {
        final ReconciliationRule rule = this.reconciliationRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ReconciliationRuleNotFoundException(ruleId));

        this.reconciliationRuleRepository.delete(rule);

        return new CommandProcessingResultBuilder().withEntityId(ruleId).build();
    }

    private void createAuditLog(Long importId, String action, String details, Long userId) {
        final BankStatementImport importRecord = this.bankStatementImportRepository.findById(importId)
                .orElseThrow(() -> new ReconciliationNotFoundException(importId));

        final ReconciliationAuditLog auditLog = ReconciliationAuditLog.create(importRecord, action, details, userId,
                DateUtils.getAuditOffsetDateTime(), null, null);
        this.reconciliationAuditLogRepository.saveAndFlush(auditLog);
    }

    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        log.error("Error occurred.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.reconciliation.unknown.data.integrity.issue",
                "Unknown data integrity issue with reconciliation: " + realCause.getMessage());
    }
}
