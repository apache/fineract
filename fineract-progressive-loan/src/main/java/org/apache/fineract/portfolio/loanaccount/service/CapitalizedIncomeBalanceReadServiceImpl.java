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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.loanaccount.data.CapitalizedIncomeDetails;
import org.apache.fineract.portfolio.loanaccount.data.LoanCapitalizedIncomeData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
public class CapitalizedIncomeBalanceReadServiceImpl implements CapitalizedIncomeBalanceReadService {

    private final LoanRepositoryWrapper loanRepository;
    private final LoanCapitalizedIncomeBalanceRepository capitalizedIncomeBalanceRepository;

    @Override
    public LoanCapitalizedIncomeData fetchLoanCapitalizedIncomeData(final Long loanId) {
        if (loanRepository.isEnabledCapitalizedIncome(loanId)) {

            List<CapitalizedIncomeDetails> capitalizedIncomeData = new ArrayList<>();
            List<LoanCapitalizedIncomeBalance> capitalizedIncomeBalances = capitalizedIncomeBalanceRepository.findAllByLoanId(loanId);
            for (final LoanCapitalizedIncomeBalance capitalizedIncomeBalance : capitalizedIncomeBalances) {
                final BigDecimal amortizedAmount = capitalizedIncomeBalance.getAmount() //
                        .subtract(MathUtil.nullToZero(capitalizedIncomeBalance.getUnrecognizedAmount())) //
                        .subtract(MathUtil.nullToZero(capitalizedIncomeBalance.getAmountAdjustment())) //
                        .subtract(MathUtil.nullToZero(capitalizedIncomeBalance.getChargedOffAmount()));

                capitalizedIncomeData.add(new CapitalizedIncomeDetails(capitalizedIncomeBalance.getAmount(), amortizedAmount,
                        capitalizedIncomeBalance.getUnrecognizedAmount(), //
                        capitalizedIncomeBalance.getAmountAdjustment(), //
                        capitalizedIncomeBalance.getChargedOffAmount()));
            }

            return new LoanCapitalizedIncomeData(capitalizedIncomeData);
        }
        throw new GeneralPlatformDomainRuleException("error.msg.loan.is.not.enabled.capitalized.income",
                "Loan: " + loanId + " is not enabled Capitalized Income feature", loanId);
    }

    @Override
    public List<CapitalizedIncomeDetails> fetchLoanCapitalizedIncomeDetails(final Long loanId) {
        if (loanRepository.isEnabledCapitalizedIncome(loanId)) {

            List<CapitalizedIncomeDetails> capitalizedIncomeData = new ArrayList<>();
            List<LoanCapitalizedIncomeBalance> capitalizedIncomeBalances = capitalizedIncomeBalanceRepository.findAllByLoanId(loanId);
            for (final LoanCapitalizedIncomeBalance capitalizedIncomeBalance : capitalizedIncomeBalances) {
                final BigDecimal amortizedAmount = capitalizedIncomeBalance.getAmount() //
                        .subtract(MathUtil.nullToZero(capitalizedIncomeBalance.getUnrecognizedAmount())) //
                        .subtract(MathUtil.nullToZero(capitalizedIncomeBalance.getAmountAdjustment())) //
                        .subtract(MathUtil.nullToZero(capitalizedIncomeBalance.getChargedOffAmount()));

                capitalizedIncomeData.add(new CapitalizedIncomeDetails(capitalizedIncomeBalance.getAmount(), amortizedAmount,
                        capitalizedIncomeBalance.getUnrecognizedAmount(), //
                        capitalizedIncomeBalance.getAmountAdjustment(), //
                        capitalizedIncomeBalance.getChargedOffAmount()));
            }

            return capitalizedIncomeData;
        }
        throw new GeneralPlatformDomainRuleException("error.msg.loan.is.not.enabled.capitalized.income",
                "Loan: " + loanId + " is not enabled Capitalized Income feature", loanId);
    }

}
