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
package org.apache.fineract.organisation.teller.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccount;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.teller.domain.Cashier;
import org.apache.fineract.organisation.teller.domain.CashierRepository;
import org.apache.fineract.organisation.teller.domain.CashierSession;
import org.apache.fineract.organisation.teller.domain.CashierSessionRepository;
import org.apache.fineract.organisation.teller.domain.CashierSessionStatus;
import org.apache.fineract.organisation.teller.domain.CashierTransactionRepository;
import org.apache.fineract.organisation.teller.domain.CashierTxnType;
import org.apache.fineract.organisation.teller.domain.Teller;
import org.apache.fineract.organisation.teller.domain.TellerRepositoryWrapper;
import org.apache.fineract.organisation.teller.exception.CashierNotFoundException;
import org.apache.fineract.organisation.teller.exception.CashierSessionAlreadyOpenException;
import org.apache.fineract.organisation.teller.exception.CashierSessionNotFoundException;
import org.apache.fineract.organisation.teller.exception.CashierSessionUnsettledPriorDayException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class CashierSessionWritePlatformServiceImpl implements CashierSessionWritePlatformService {

    private static final String GL_CODE_CASH_SHORTAGE_TELLER = "53920";
    private static final String GL_CODE_MISCELLANEOUS_INCOME = "43210";

    private final PlatformSecurityContext context;
    private final CashierSessionRepository cashierSessionRepository;
    private final CashierRepository cashierRepository;
    private final TellerRepositoryWrapper tellerRepositoryWrapper;
    private final CashierTransactionRepository cashierTransactionRepository;
    private final JournalEntryRepository glJournalEntryRepository;
    private final FinancialActivityAccountRepositoryWrapper financialActivityAccountRepositoryWrapper;
    private final GLAccountRepositoryWrapper glAccountRepositoryWrapper;

    @Override
    @Transactional
    public CommandProcessingResult openSession(final Long tellerId, final Long cashierId, final String currencyCode) {
        final AppUser currentUser = context.authenticatedUser();

        final Cashier cashier = cashierRepository.findById(cashierId)
                .orElseThrow(() -> new CashierNotFoundException(cashierId));
        final Teller teller = tellerRepositoryWrapper.findOneWithNotFoundDetection(tellerId);

        final LocalDate today = DateUtils.getBusinessLocalDate();

        // Business rule: one OPEN session per cashier per teller per day
        cashierSessionRepository.findOpenSession(cashierId, tellerId, today).ifPresent(s -> {
            throw new CashierSessionAlreadyOpenException(cashierId, tellerId);
        });

        // Business rule: cannot open if prior day has unsettled session
        final List<CashierSession> unsettled = cashierSessionRepository.findUnsettledPriorSessions(currentUser.getId(), today);
        if (!unsettled.isEmpty()) {
            throw new CashierSessionUnsettledPriorDayException();
        }

        final LocalDateTime now = LocalDateTime.now();
        final CashierSession session = new CashierSession()
                .setCashier(cashier)
                .setTeller(teller)
                .setUserId(currentUser.getId())
                .setOffice(teller.getOffice())
                .setSessionDate(today)
                .setOpenedAt(now)
                .setStatus(CashierSessionStatus.OPEN)
                .setCurrencyCode(currencyCode != null ? currencyCode : "")
                .setCreatedBy(currentUser.getId())
                .setCreatedDate(now);

        cashierSessionRepository.save(session);

        return new CommandProcessingResultBuilder()
                .withEntityId(session.getId())
                .withOfficeId(teller.getOffice().getId())
                .build();
    }

    @Override
    @Transactional
    public CommandProcessingResult closeSession(final Long sessionId) {
        return closeSession(sessionId, null, null);
    }

    @Override
    @Transactional
    public CommandProcessingResult closeSession(final Long sessionId, final BigDecimal settledAmount, final String supervisorNote) {
        context.authenticatedUser();

        final CashierSession session = cashierSessionRepository.findById(sessionId)
                .orElseThrow(() -> new CashierSessionNotFoundException(sessionId));

        final BigDecimal resolvedSettledAmount = settledAmount != null ? settledAmount : BigDecimal.ZERO;

        // Compute expected cash: openingAllocation + sumCashIn - sumCashOut
        final BigDecimal sumCashIn = cashierTransactionRepository.sumAmountBySessionAndTxnType(
                sessionId, CashierTxnType.INWARD_CASH_TXN.getId());
        final BigDecimal sumCashOut = cashierTransactionRepository.sumAmountBySessionAndTxnType(
                sessionId, CashierTxnType.OUTWARD_CASH_TXN.getId());

        final BigDecimal openingAllocation = session.getOpeningAllocation() != null ? session.getOpeningAllocation() : BigDecimal.ZERO;
        final BigDecimal safeCashIn = sumCashIn != null ? sumCashIn : BigDecimal.ZERO;
        final BigDecimal safeCashOut = sumCashOut != null ? sumCashOut : BigDecimal.ZERO;

        final BigDecimal expectedCash = openingAllocation.add(safeCashIn).subtract(safeCashOut);
        final BigDecimal variance = resolvedSettledAmount.subtract(expectedCash);
        final boolean hasVariance = variance.compareTo(BigDecimal.ZERO) != 0;

        // Validate: supervisor note required when variance != 0
        if (hasVariance && (supervisorNote == null || supervisorNote.isBlank())) {
            throw new PlatformApiDataValidationException(
                    "validation.msg.cashierSession.supervisorNote.required",
                    "A supervisor note is required when a variance exists between settled amount and expected cash.",
                    "supervisorNote");
        }

        // Post GL variance journal entry if needed
        if (hasVariance) {
            postVarianceJournalEntry(session, variance, supervisorNote);
        }

        // Update session fields
        session.setTotalSettled(resolvedSettledAmount);
        if (hasVariance) {
            session.setSupervisorNote(supervisorNote);
        }
        session.setStatus(CashierSessionStatus.SETTLED);
        session.setClosedAt(LocalDateTime.now());

        cashierSessionRepository.save(session);

        return new CommandProcessingResultBuilder()
                .withEntityId(sessionId)
                .build();
    }

    private void postVarianceJournalEntry(final CashierSession session, final BigDecimal variance, final String note) {

        final FinancialActivityAccount tellerCashAccount = financialActivityAccountRepositoryWrapper
                .findByFinancialActivityTypeWithNotFoundDetection(FinancialActivity.CASH_AT_TELLER.getValue());
        final GLAccount tellerCashGlAccount = tellerCashAccount.getGlAccount();

        final Office office = session.getOffice();
        final String currencyCode = session.getCurrencyCode();
        final LocalDate entryDate = session.getSessionDate();

        final String transactionId = java.util.UUID.randomUUID().toString().replace("-", "");

        final String description = note != null ? note : "Session variance adjustment";

        final GLAccount debitAccount;
        final GLAccount creditAccount;

        if (variance.compareTo(BigDecimal.ZERO) < 0) {
            // Short settlement: cashier returned less than expected
            // DEBIT Cash Shortage - Teller (53920) / CREDIT Teller Cash (11140)
            debitAccount = glAccountRepositoryWrapper.findOneByGlCodeWithNotFoundDetection(GL_CODE_CASH_SHORTAGE_TELLER);
            creditAccount = tellerCashGlAccount;
        } else {
            // Over settlement: cashier returned more than expected
            // DEBIT Teller Cash (11140) / CREDIT Miscellaneous Income (43210)
            debitAccount = tellerCashGlAccount;
            creditAccount = glAccountRepositoryWrapper.findOneByGlCodeWithNotFoundDetection(GL_CODE_MISCELLANEOUS_INCOME);
        }

        final BigDecimal absVariance = variance.abs();

        final JournalEntry debitEntry = JournalEntry.createNew(office, null, debitAccount, currencyCode,
                transactionId, false, entryDate, JournalEntryType.DEBIT, absVariance, description,
                null, null, null, null, null, null, null);

        final JournalEntry creditEntry = JournalEntry.createNew(office, null, creditAccount, currencyCode,
                transactionId, false, entryDate, JournalEntryType.CREDIT, absVariance, description,
                null, null, null, null, null, null, null);

        glJournalEntryRepository.saveAndFlush(debitEntry);
        glJournalEntryRepository.saveAndFlush(creditEntry);
    }
}
