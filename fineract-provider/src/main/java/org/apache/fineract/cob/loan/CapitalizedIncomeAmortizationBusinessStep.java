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
package org.apache.fineract.cob.loan;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.data.AccountingBridgeDataDTO;
import org.apache.fineract.portfolio.loanaccount.data.AccountingBridgeLoanTransactionDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.mapper.LoanAccountingBridgeMapper;
import org.apache.fineract.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import org.apache.fineract.portfolio.loanaccount.util.CapitalizedIncomeAmortizationUtil;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CapitalizedIncomeAmortizationBusinessStep implements LoanCOBBusinessStep {

    private final LoanCapitalizedIncomeBalanceRepository capitalizedIncomeBalanceRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final LoanAccountingBridgeMapper loanAccountingBridgeMapper;
    private final LoanTransactionRepository loanTransactionRepository;
    private final ExternalIdFactory externalIdFactory;

    @Transactional
    @Override
    public Loan execute(Loan loan) {
        MathContext mc = MoneyHelper.getMathContext();
        LocalDate businessDate = DateUtils.getBusinessLocalDate();
        List<LoanCapitalizedIncomeBalance> balances = capitalizedIncomeBalanceRepository.findAllByLoanId(loan.getId());

        BigDecimal totalAmortizationAmount = BigDecimal.ZERO;
        for (LoanCapitalizedIncomeBalance balance : balances) {
            long daysUntilMaturity = businessDate.until(loan.getMaturityDate(), ChronoUnit.DAYS);
            Money dailyAmortization = CapitalizedIncomeAmortizationUtil.calculateDailyAmortization(
                    loan.getLoanProductRelatedDetail().getCapitalizedIncomeStrategy(), daysUntilMaturity, balance.getUnrecognizedAmount(),
                    loan.getCurrency().toData());
            totalAmortizationAmount = totalAmortizationAmount.add(dailyAmortization.getAmount(), mc);
            balance.setUnrecognizedAmount(balance.getUnrecognizedAmount().subtract(dailyAmortization.getAmount(), mc));
        }

        capitalizedIncomeBalanceRepository.saveAll(balances);

        if (MathUtil.isGreaterThanZero(totalAmortizationAmount)) {
            LoanTransaction transaction = LoanTransaction.capitalizedIncomeAmortization(loan, loan.getOffice(), businessDate,
                    totalAmortizationAmount, externalIdFactory.create());
            loan.addLoanTransaction(transaction);

            transaction = loanTransactionRepository.save(transaction);
            loanTransactionRepository.flush();

            final AccountingBridgeLoanTransactionDTO transactionDTO = loanAccountingBridgeMapper.mapToLoanTransactionData(transaction,
                    loan.getCurrency().getCode());
            final AccountingBridgeDataDTO accountingBridgeData = new AccountingBridgeDataDTO(loan.getId(), loan.getLoanProduct().getId(),
                    loan.getOfficeId(), loan.getCurrencyCode(), loan.getSummary().getTotalInterestCharged(),
                    loan.isNoneOrCashOrUpfrontAccrualAccountingEnabledOnLoanProduct(),
                    loan.isUpfrontAccrualAccountingEnabledOnLoanProduct(), loan.isPeriodicAccrualAccountingEnabledOnLoanProduct(), false,
                    false, false, null, loan.isClosedWrittenOff(), List.of(transactionDTO));
            this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);

            businessEventNotifierService
                    .notifyPostBusinessEvent(new LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent(transaction));
        }

        return loan;
    }

    @Override
    public String getEnumStyledName() {
        return "CAPITALIZED_INCOME_AMORTIZATION";
    }

    @Override
    public String getHumanReadableName() {
        return "Capitalized income amortization";
    }
}
