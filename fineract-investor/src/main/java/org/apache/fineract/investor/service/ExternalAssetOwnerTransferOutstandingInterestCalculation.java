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
package org.apache.fineract.investor.service;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.investor.config.InvestorModuleIsEnabledCondition;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.mapper.CurrencyMapper;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanSummaryDataProvider;
import org.apache.fineract.portfolio.loanaccount.service.LoanSummaryProviderDelegate;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Conditional(InvestorModuleIsEnabledCondition.class)
public class ExternalAssetOwnerTransferOutstandingInterestCalculation {

    private final LoanSummaryProviderDelegate loanSummaryDataProvider;
    private final ConfigurationDomainService configurationDomainService;
    private final LoanReadPlatformService loanReadPlatformService;
    private final CurrencyMapper currencyMapper;

    private LoanSummaryDataProvider fetchLoanSummaryDataProvider(Loan loan) {
        return this.loanSummaryDataProvider.resolveLoanSummaryDataProvider(loan.getTransactionProcessingStrategyCode());
    }

    public BigDecimal calculateOutstandingInterest(Loan loan) {
        String outstandingInterestCalculationStrategy = configurationDomainService.getAssetOwnerTransferOustandingInterestStrategy();
        return switch (outstandingInterestCalculationStrategy) {
            case "TOTAL_OUTSTANDING_INTEREST" -> loan.getSummary().getTotalInterestOutstanding();
            case "PAYABLE_OUTSTANDING_INTEREST" -> {
                LoanAccountData data = loanReadPlatformService.retrieveOne(loan.getId());
                data = loanReadPlatformService.fetchRepaymentScheduleData(data);
                Money duePayableAmount = loan
                        .getRepaymentScheduleInstallments(i -> !i.getDueDate().isAfter(DateUtils.getBusinessLocalDate())).stream()
                        .map(i -> i.getInterestOutstanding(loan.getCurrency())).reduce(Money.zero(loan.getCurrency()), MathUtil::plus);
                BigDecimal notDuePayableAmount = fetchLoanSummaryDataProvider(loan)
                        .computeTotalUnpaidPayableNotDueInterestAmountOnActualPeriod(loan, data.getRepaymentSchedule().getPeriods(),
                                DateUtils.getBusinessLocalDate(), currencyMapper.map(loan.getCurrency()), duePayableAmount.getAmount());

                yield MathUtil.add(duePayableAmount.getAmount(), notDuePayableAmount);
            }
            default -> throw new UnsupportedOperationException(
                    "Unknown outstanding interest calculation: " + outstandingInterestCalculationStrategy);
        };
    }
}
