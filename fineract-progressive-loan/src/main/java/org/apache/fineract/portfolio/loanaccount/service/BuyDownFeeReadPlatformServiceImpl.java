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
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.loanaccount.data.BuyDownFeeAmortizationDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.repository.LoanBuyDownFeeBalanceRepository;

@RequiredArgsConstructor
public class BuyDownFeeReadPlatformServiceImpl implements BuyDownFeeReadPlatformService {

    private final LoanBuyDownFeeBalanceRepository loanBuyDownFeeBalanceRepository;
    private final LoanRepository loanRepository;

    @Override
    public List<BuyDownFeeAmortizationDetails> retrieveLoanBuyDownFeeAmortizationDetails(final Long loanId) {
        if (!loanRepository.existsById(loanId)) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.is.not.found", "Loan: %s is not found".formatted(loanId), loanId);
        }

        if (!loanRepository.isEnabledBuyDownFee(loanId)) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.is.not.enabled.buydown.fee",
                    "Loan: %s is not enabled Buydown fee feature".formatted(loanId), loanId);
        }
        final List<LoanBuyDownFeeBalance> buyDownFeeBalances = loanBuyDownFeeBalanceRepository.findAllByLoanId(loanId);

        return buyDownFeeBalances.stream().map(this::mapToLoanBuyDownFeeAmortizationData).collect(Collectors.toList());
    }

    private BuyDownFeeAmortizationDetails mapToLoanBuyDownFeeAmortizationData(final LoanBuyDownFeeBalance balance) {
        final BigDecimal amortizedAmount = balance.getAmount() //
                .subtract(MathUtil.nullToZero(balance.getUnrecognizedAmount())) //
                .subtract(MathUtil.nullToZero(balance.getAmountAdjustment())) //
                .subtract(MathUtil.nullToZero(balance.getChargedOffAmount()));

        return new BuyDownFeeAmortizationDetails(balance.getId(), balance.getLoan().getId(), balance.getLoanTransaction().getId(),
                balance.getDate(), balance.getAmount(), amortizedAmount, balance.getUnrecognizedAmount(), balance.getAmountAdjustment(),
                balance.getChargedOffAmount());
    }

}
