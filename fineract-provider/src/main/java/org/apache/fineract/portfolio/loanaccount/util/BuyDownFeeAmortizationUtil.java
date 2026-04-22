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
package org.apache.fineract.portfolio.loanaccount.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeStrategy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;

public final class BuyDownFeeAmortizationUtil {

    private BuyDownFeeAmortizationUtil() {}

    public static Money calculateTotalAmortizationTillDate(final LoanBuyDownFeeBalance buyDownFeeBalance,
            final List<LoanTransaction> adjustmentTransactions, final LocalDate maturityDate,
            final LoanBuyDownFeeStrategy buyDownFeeStrategy, final LocalDate tillDate, final MonetaryCurrency currency) {
        return switch (buyDownFeeStrategy) {
            case EQUAL_AMORTIZATION -> calculateTotalAmortizationTillDateEqualAmortization(buyDownFeeBalance, adjustmentTransactions,
                    maturityDate, tillDate, currency);
        };
    }

    private static Money calculateTotalAmortizationTillDateEqualAmortization(LoanBuyDownFeeBalance balance,
            List<LoanTransaction> adjustmentTransactions, LocalDate maturityDate, LocalDate tillDate, MonetaryCurrency currency) {

        BigDecimal unrecognizedAmount = balance.getAmount();
        BigDecimal totalAmortizationAmount = BigDecimal.ZERO;
        BigDecimal overAmortizationCorrection = BigDecimal.ZERO;

        List<LoanTransaction> sortedAdjustmentTransactions = adjustmentTransactions.stream()
                .sorted(Comparator.comparing(LoanTransaction::getDateOf)).toList();
        LocalDate periodStart = balance.getDate();
        for (LoanTransaction adjustmentTransaction : sortedAdjustmentTransactions) {
            long daysUntilMaturity = DateUtils.getDifferenceInDays(periodStart, maturityDate);
            long daysOfPeriod = DateUtils.getDifferenceInDays(periodStart, adjustmentTransaction.getDateOf());
            BigDecimal periodAmortization = daysUntilMaturity == 0L ? BigDecimal.ZERO
                    : unrecognizedAmount.multiply(BigDecimal.valueOf(daysOfPeriod)).divide(BigDecimal.valueOf(daysUntilMaturity),
                            MoneyHelper.getMathContext());

            totalAmortizationAmount = totalAmortizationAmount.add(periodAmortization);
            unrecognizedAmount = unrecognizedAmount.subtract(periodAmortization).subtract(adjustmentTransaction.getAmount());
            if (MathUtil.isLessThanZero(unrecognizedAmount)) {
                overAmortizationCorrection = overAmortizationCorrection.add(unrecognizedAmount);
                unrecognizedAmount = BigDecimal.ZERO;
            }
            periodStart = adjustmentTransaction.getDateOf();
        }
        if (periodStart.isBefore(tillDate)) {
            long daysUntilMaturity = DateUtils.getDifferenceInDays(periodStart, maturityDate);
            long daysOfPeriod = DateUtils.getDifferenceInDays(periodStart, tillDate);
            BigDecimal periodAmortization = unrecognizedAmount.multiply(BigDecimal.valueOf(daysOfPeriod))
                    .divide(BigDecimal.valueOf(daysUntilMaturity), MoneyHelper.getMathContext());
            totalAmortizationAmount = totalAmortizationAmount.add(periodAmortization);
        } else if (balance.getDate().equals(maturityDate)) {
            totalAmortizationAmount = totalAmortizationAmount.add(unrecognizedAmount);
        }

        return Money.of(currency, totalAmortizationAmount.add(overAmortizationCorrection));
    }
}
