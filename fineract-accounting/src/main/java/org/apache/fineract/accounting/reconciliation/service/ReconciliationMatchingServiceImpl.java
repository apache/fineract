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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.accounting.reconciliation.data.ReconciliationMatchData;
import org.apache.fineract.accounting.reconciliation.domain.BankStatementImport;
import org.apache.fineract.accounting.reconciliation.domain.BankStatementTransaction;
import org.apache.fineract.accounting.reconciliation.domain.MatchType;
import org.apache.fineract.accounting.reconciliation.domain.ReconciliationAuditLog;
import org.apache.fineract.accounting.reconciliation.domain.ReconciliationMatch;
import org.apache.fineract.accounting.reconciliation.domain.ReconciliationRule;
import org.apache.fineract.accounting.reconciliation.domain.repository.BankStatementImportRepository;
import org.apache.fineract.accounting.reconciliation.domain.repository.BankStatementTransactionRepository;
import org.apache.fineract.accounting.reconciliation.domain.repository.ReconciliationAuditLogRepository;
import org.apache.fineract.accounting.reconciliation.domain.repository.ReconciliationMatchRepository;
import org.apache.fineract.accounting.reconciliation.domain.repository.ReconciliationRuleRepository;
import org.apache.fineract.accounting.reconciliation.exception.ReconciliationNotFoundException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReconciliationMatchingServiceImpl implements ReconciliationMatchingService {

    private final PlatformSecurityContext context;
    private final BankStatementImportRepository bankStatementImportRepository;
    private final BankStatementTransactionRepository bankStatementTransactionRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final ReconciliationMatchRepository reconciliationMatchRepository;
    private final ReconciliationRuleRepository reconciliationRuleRepository;
    private final ReconciliationAuditLogRepository reconciliationAuditLogRepository;

    @Override
    @Transactional
    public int matchExact(Long importId) {
        final AppUser currentUser = this.context.authenticatedUser();
        final BankStatementImport importRecord = this.bankStatementImportRepository.findById(importId)
                .orElseThrow(() -> new ReconciliationNotFoundException(importId));

        final List<BankStatementTransaction> unmatchedTransactions = this.bankStatementTransactionRepository
                .findByImportIdAndMatchStatus(importId, false);

        int matchCount = 0;

        for (BankStatementTransaction bankTransaction : unmatchedTransactions) {
            final BigDecimal amount = getTransactionAmount(bankTransaction);
            final LocalDate transactionDate = bankTransaction.getTransactionDate();

            final List<JournalEntry> potentialMatches = new ArrayList<>();

            if (potentialMatches.size() == 1) {
                final JournalEntry journalEntry = potentialMatches.get(0);

                final List<ReconciliationMatch> existingMatches = this.reconciliationMatchRepository
                        .findByGlJournalEntryId(journalEntry.getId());
                final boolean alreadyMatched = !existingMatches.isEmpty();
                if (!alreadyMatched) {
                    final ReconciliationMatch match = ReconciliationMatch.create(importRecord, bankTransaction, journalEntry.getId(),
                            MatchType.AUTO_EXACT, BigDecimal.valueOf(100), amount, null, currentUser.getId(),
                            DateUtils.getAuditOffsetDateTime());

                    this.reconciliationMatchRepository.save(match);

                    bankTransaction.markAsMatched(currentUser.getId(), DateUtils.getAuditOffsetDateTime());
                    this.bankStatementTransactionRepository.save(bankTransaction);

                    matchCount++;
                }
            }
        }

        if (matchCount > 0) {
            createAuditLog(importRecord, "EXACT_MATCH_COMPLETED", matchCount + " exact matches found", currentUser.getId());
        }

        return matchCount;
    }

    @Override
    @Transactional
    public int matchWithRules(Long importId) {
        final AppUser currentUser = this.context.authenticatedUser();
        final BankStatementImport importRecord = this.bankStatementImportRepository.findById(importId)
                .orElseThrow(() -> new ReconciliationNotFoundException(importId));

        final List<ReconciliationRule> activeRules = this.reconciliationRuleRepository
                .findActiveByGlAccount(importRecord.getGlAccount().getId());

        final List<BankStatementTransaction> unmatchedTransactions = this.bankStatementTransactionRepository
                .findByImportIdAndMatchStatus(importId, false);

        int matchCount = 0;

        for (BankStatementTransaction bankTransaction : unmatchedTransactions) {
            for (ReconciliationRule rule : activeRules) {
                if (tryMatchWithRule(importRecord, bankTransaction, rule, currentUser)) {
                    matchCount++;
                    break;
                }
            }
        }

        if (matchCount > 0) {
            createAuditLog(importRecord, "RULE_MATCH_COMPLETED", matchCount + " rule-based matches found", currentUser.getId());
        }

        return matchCount;
    }

    @Override
    public List<ReconciliationMatchData> suggestMatches(Long bankTransactionId) {
        final BankStatementTransaction bankTransaction = this.bankStatementTransactionRepository.findById(bankTransactionId)
                .orElseThrow(() -> new ReconciliationNotFoundException(bankTransactionId));

        final BankStatementImport importRecord = bankTransaction.getStatementImport();
        final BigDecimal amount = getTransactionAmount(bankTransaction);
        final LocalDate transactionDate = bankTransaction.getTransactionDate();

        final List<ReconciliationMatchData> suggestions = new ArrayList<>();

        final LocalDate fromDate = transactionDate.minusDays(7);
        final LocalDate toDate = transactionDate.plusDays(7);

        final List<JournalEntry> potentialMatches = new ArrayList<>();

        for (JournalEntry journalEntry : potentialMatches) {
            final List<ReconciliationMatch> existingMatches = this.reconciliationMatchRepository
                    .findByGlJournalEntryId(journalEntry.getId());
            final boolean alreadyMatched = !existingMatches.isEmpty();
            if (!alreadyMatched) {
                final int confidence = calculateMatchConfidence(bankTransaction, journalEntry);
                if (confidence >= 60) {
                    final ReconciliationMatchData suggestion = new ReconciliationMatchData().setBankTransactionId(bankTransaction.getId())
                            .setGlJournalEntryId(journalEntry.getId()).setMatchType(MatchType.SUGGESTED.name())
                            .setMatchConfidence(BigDecimal.valueOf(confidence)).setAmount(getTransactionAmount(bankTransaction));
                    suggestions.add(suggestion);
                }
            }
        }

        suggestions.sort((a, b) -> b.getMatchConfidence().compareTo(a.getMatchConfidence()));

        return suggestions;
    }

    private boolean tryMatchWithRule(BankStatementImport importRecord, BankStatementTransaction bankTransaction, ReconciliationRule rule,
            AppUser currentUser) {

        final BigDecimal amount = getTransactionAmount(bankTransaction);
        final LocalDate transactionDate = bankTransaction.getTransactionDate();

        final LocalDate fromDate = transactionDate.minusDays(rule.getDateToleranceDays() != null ? rule.getDateToleranceDays() : 0);
        final LocalDate toDate = transactionDate.plusDays(rule.getDateToleranceDays() != null ? rule.getDateToleranceDays() : 0);

        final List<JournalEntry> potentialMatches = new ArrayList<>();

        for (JournalEntry journalEntry : potentialMatches) {
            final List<ReconciliationMatch> existingMatches = this.reconciliationMatchRepository
                    .findByGlJournalEntryId(journalEntry.getId());
            final boolean alreadyMatched = !existingMatches.isEmpty();
            if (!alreadyMatched) {
                if (matchesRule(bankTransaction, journalEntry, rule, amount)) {
                    final int confidence = calculateMatchConfidence(bankTransaction, journalEntry);

                    final ReconciliationMatch match = ReconciliationMatch.create(importRecord, bankTransaction, journalEntry.getId(),
                            MatchType.AUTO_FUZZY, BigDecimal.valueOf(confidence), amount, null, currentUser.getId(),
                            DateUtils.getAuditOffsetDateTime());

                    this.reconciliationMatchRepository.save(match);

                    bankTransaction.markAsMatched(currentUser.getId(), DateUtils.getAuditOffsetDateTime());
                    this.bankStatementTransactionRepository.save(bankTransaction);

                    return true;
                }
            }
        }

        return false;
    }

    private boolean matchesRule(BankStatementTransaction bankTransaction, JournalEntry journalEntry, ReconciliationRule rule,
            BigDecimal bankAmount) {

        final BigDecimal amountTolerance = rule.getAmountTolerance() != null ? rule.getAmountTolerance() : BigDecimal.ZERO;
        final BigDecimal journalAmount = journalEntry.getAmount();

        final BigDecimal difference = bankAmount.subtract(journalAmount).abs();
        if (difference.compareTo(amountTolerance) > 0) {
            return false;
        }

        if ("REFERENCE".equals(rule.getMatchCondition()) && rule.getConditionValue() != null) {
            final String referenceNumber = bankTransaction.getReferenceNumber();
            if (referenceNumber != null) {
                final Pattern pattern = Pattern.compile(rule.getConditionValue(), Pattern.CASE_INSENSITIVE);
                final String glReference = journalEntry.getReferenceNumber();
                if (glReference != null && pattern.matcher(glReference).find()) {
                    return true;
                }
            }
        }

        if ("DESCRIPTION".equals(rule.getMatchCondition()) && rule.getConditionValue() != null) {
            final String description = bankTransaction.getDescription();
            if (description != null) {
                final Pattern pattern = Pattern.compile(rule.getConditionValue(), Pattern.CASE_INSENSITIVE);
                final String glDescription = journalEntry.getDescription();
                if (glDescription != null && pattern.matcher(glDescription).find()) {
                    return true;
                }
            }
        }

        return false;
    }

    private int calculateMatchConfidence(BankStatementTransaction bankTransaction, JournalEntry journalEntry) {
        int confidence = 0;

        final BigDecimal bankAmount = getTransactionAmount(bankTransaction);
        final BigDecimal journalAmount = journalEntry.getAmount();

        if (bankAmount.compareTo(journalAmount) == 0) {
            confidence += 50;
        } else {
            final BigDecimal difference = bankAmount.subtract(journalAmount).abs();
            final BigDecimal percentDifference = difference.divide(bankAmount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            if (percentDifference.compareTo(BigDecimal.ONE) <= 0) {
                confidence += 40;
            } else if (percentDifference.compareTo(BigDecimal.valueOf(5)) <= 0) {
                confidence += 20;
            }
        }

        final LocalDate bankDate = bankTransaction.getTransactionDate();
        final LocalDate journalDate = journalEntry.getTransactionDate();
        if (bankDate.equals(journalDate)) {
            confidence += 30;
        } else {
            final long daysDifference = Math.abs(bankDate.toEpochDay() - journalDate.toEpochDay());
            if (daysDifference <= 1) {
                confidence += 20;
            } else if (daysDifference <= 3) {
                confidence += 10;
            }
        }

        if (bankTransaction.getReferenceNumber() != null && journalEntry.getReferenceNumber() != null) {
            if (bankTransaction.getReferenceNumber().equalsIgnoreCase(journalEntry.getReferenceNumber())) {
                confidence += 20;
            }
        }

        return Math.min(confidence, 100);
    }

    private BigDecimal getTransactionAmount(BankStatementTransaction transaction) {
        if (transaction.getDebitAmount() != null && transaction.getDebitAmount().compareTo(BigDecimal.ZERO) > 0) {
            return transaction.getDebitAmount();
        }
        if (transaction.getCreditAmount() != null && transaction.getCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
            return transaction.getCreditAmount();
        }
        return BigDecimal.ZERO;
    }

    private void createAuditLog(BankStatementImport importRecord, String action, String details, Long userId) {
        final ReconciliationAuditLog auditLog = ReconciliationAuditLog.create(importRecord, action, details, userId,
                DateUtils.getAuditOffsetDateTime(), null, null);
        this.reconciliationAuditLogRepository.save(auditLog);
    }
}
