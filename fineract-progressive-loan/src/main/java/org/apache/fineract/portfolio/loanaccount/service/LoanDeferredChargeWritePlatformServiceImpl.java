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
package org.apache.fineract.portfolio.loanaccount.service;

import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanDeferredChargeTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.charge.domain.ChargeAmortizationPeriodType;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDeferredChargeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.repository.LoanDeferredChargeBalanceRepository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class LoanDeferredChargeWritePlatformServiceImpl implements LoanDeferredChargeWritePlatformService {

    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanDeferredChargeBalanceRepository deferredChargeBalanceRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final ExternalIdFactory externalIdFactory;

    @Override
    @Transactional
    public void createForEffectiveCharge(final Loan loan, final LoanCharge loanCharge, final LocalDate transactionDate) {
        // A charge waived before it became effective never defers any income.
        if (loanCharge == null || !loanCharge.isAmortized() || loanCharge.isWaived()) {
            return;
        }
        // Future-dated specified-due-date charges wait until due (e.g. a later COB run); never post a future
        // DEFERRED_CHARGE.
        if (loanCharge.isSpecifiedDueDate() && DateUtils.isAfter(loanCharge.getDueLocalDate(), DateUtils.getBusinessLocalDate())) {
            return;
        }
        if (loanCharge.getId() != null
                && deferredChargeBalanceRepository.findByLoanChargeIdAndDeletedFalseAndClosedFalse(loanCharge.getId()).isPresent()) {
            log.debug("Skipping DEFERRED_CHARGE for loan charge {}: open balance already exists", loanCharge.getId());
            return;
        }

        final Money amount = Money.of(loan.getCurrency(), loanCharge.amount());
        final LoanTransaction deferredChargeTransaction = LoanTransaction.deferredCharge(loan, amount, transactionDate,
                externalIdFactory.create());
        loan.addLoanTransaction(deferredChargeTransaction);
        loanTransactionRepository.saveAndFlush(deferredChargeTransaction);

        final LoanDeferredChargeBalance balance = new LoanDeferredChargeBalance();
        balance.setLoan(loan);
        balance.setLoanCharge(loanCharge);
        balance.setLoanTransaction(deferredChargeTransaction);
        balance.setDate(transactionDate);
        balance.setAmount(deferredChargeTransaction.getAmount());
        balance.setUnrecognizedAmount(deferredChargeTransaction.getAmount());
        balance.setPeriodType(loanCharge.getAmortizationPeriodType());
        balance.setCalculationType(loanCharge.getAmortizationCalculationType());
        balance.setIncomeType(loanCharge.getAmortizationIncomeType());
        balance.setWindowEndDate(resolveWindowEndDate(loanCharge, transactionDate));
        deferredChargeBalanceRepository.saveAndFlush(balance);

        businessEventNotifierService
                .notifyPostBusinessEvent(new LoanDeferredChargeTransactionCreatedBusinessEvent(deferredChargeTransaction));
    }

    @Override
    @Transactional
    public void resetBalance(final Long loanId) {
        deferredChargeBalanceRepository.delete((root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get("loan").get("id"), loanId));
    }

    @Override
    @Transactional
    public Optional<LoanTransaction> reverseForLoanCharge(final Long loanChargeId) {
        return deferredChargeBalanceRepository.findByLoanChargeIdAndDeletedFalseAndClosedFalse(loanChargeId).map(balance -> {
            final LoanTransaction deferredChargeTransaction = balance.getLoanTransaction();
            deferredChargeTransaction.reverse();
            balance.setDeleted(true);
            deferredChargeBalanceRepository.saveAndFlush(balance);
            return deferredChargeTransaction;
        });
    }

    private static LocalDate resolveWindowEndDate(final LoanCharge loanCharge, final LocalDate windowStart) {
        if (loanCharge.getAmortizationPeriodType() != ChargeAmortizationPeriodType.NUMBER_OF_DAYS
                || loanCharge.getAmortizationDays() == null) {
            return null;
        }
        return windowStart.plusDays(loanCharge.getAmortizationDays());
    }
}
