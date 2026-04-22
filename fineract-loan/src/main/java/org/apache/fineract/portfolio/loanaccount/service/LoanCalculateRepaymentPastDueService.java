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
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.loanaccount.data.LoanRepaymentPastDueData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;

@NoArgsConstructor
public class LoanCalculateRepaymentPastDueService {

    public LoanRepaymentPastDueData retrieveLoanRepaymentPastDueAmountTillDate(Loan loan) {
        List<LoanRepaymentScheduleInstallment> pastDueRepayments = getPastDueRepayments(loan);
        MonetaryCurrency loanCurrency = loan.getCurrency();
        LoanRepaymentPastDueData pastDueData = calculatePastDueAmountsForRepayments(pastDueRepayments, loanCurrency);
        return pastDueData;
    }

    private LoanRepaymentPastDueData calculatePastDueAmountsForRepayments(List<LoanRepaymentScheduleInstallment> pastDueRepayments,
            MonetaryCurrency currency) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal principalAmount = BigDecimal.ZERO;
        BigDecimal interestAmount = BigDecimal.ZERO;
        BigDecimal feeAmount = BigDecimal.ZERO;
        BigDecimal penaltyAmount = BigDecimal.ZERO;
        if (!pastDueRepayments.isEmpty()) {
            totalAmount = pastDueRepayments.stream().map(repayment -> repayment.getTotalOutstanding(currency).getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            principalAmount = pastDueRepayments.stream().map(repayment -> repayment.getPrincipalOutstanding(currency).getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            interestAmount = pastDueRepayments.stream().map(repayment -> repayment.getInterestOutstanding(currency).getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            feeAmount = pastDueRepayments.stream().map(repayment -> repayment.getFeeChargesOutstanding(currency).getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            penaltyAmount = pastDueRepayments.stream().map(repayment -> repayment.getPenaltyChargesOutstanding(currency).getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return new LoanRepaymentPastDueData(totalAmount, principalAmount, interestAmount, feeAmount, penaltyAmount);
    }

    private List<LoanRepaymentScheduleInstallment> getPastDueRepayments(Loan loan) {
        List<LoanRepaymentScheduleInstallment> loanRepayments = loan.getRepaymentScheduleInstallments();
        LocalDate currentBusinessDate = DateUtils.getBusinessLocalDate();
        return loanRepayments.stream()
                .filter(repayment -> (!repayment.isObligationsMet() && !DateUtils.isAfter(repayment.getDueDate(), currentBusinessDate)))
                .collect(Collectors.toList());
    }
}
