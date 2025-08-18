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
package org.apache.fineract.portfolio.delinquency.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.ProgressiveLoanModel;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.ProgressiveTransactionCtx;
import org.apache.fineract.portfolio.loanaccount.service.InterestScheduleModelRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanproduct.calc.data.RepaymentPeriod;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgressivePossibleNextRepaymentCalculationServiceImpl extends AbstractPossibleNextRepaymentCalculationService {

    private final InterestScheduleModelRepositoryWrapper interestScheduleModelRepository;
    private final AdvancedPaymentScheduleTransactionProcessor advancedPaymentScheduleTransactionProcessor;

    @Override
    public BigDecimal calculateInterestRecalculationFutureOutstandingValue(Loan loan, LocalDate nextPaymentDueDate,
            LoanRepaymentScheduleInstallment nextInstallment) {
        MonetaryCurrency currency = loan.getCurrency();
        Optional<ProgressiveLoanModel> progressiveLoanModel = interestScheduleModelRepository.findOneByLoan(loan);
        Optional<ProgressiveLoanInterestScheduleModel> optionalScheduleModel = interestScheduleModelRepository
                .extractModel(progressiveLoanModel);
        ProgressiveLoanInterestScheduleModel scheduleModel = optionalScheduleModel.orElseGet(
                () -> advancedPaymentScheduleTransactionProcessor.calculateInterestScheduleModel(loan.getId(), nextPaymentDueDate));
        if (scheduleModel == null) {
            return BigDecimal.ZERO;
        }
        List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = loan.getRepaymentScheduleInstallments();
        ProgressiveTransactionCtx ctx = new ProgressiveTransactionCtx(loan.getCurrency(), repaymentScheduleInstallments, Set.of(),
                new MoneyHolder(loan.getTotalOverpaidAsMoney()), new ChangedTransactionDetail(), scheduleModel);
        ctx.setChargedOff(loan.isChargedOff());
        ctx.setWrittenOff(loan.isClosedWrittenOff());
        ctx.setContractTerminated(loan.isContractTermination());
        advancedPaymentScheduleTransactionProcessor.recalculateInterestForDate(nextPaymentDueDate, ctx, false);
        RepaymentPeriod repaymentPeriod = scheduleModel.findRepaymentPeriodByDueDate(nextPaymentDueDate)
                .orElseGet(scheduleModel::getLastRepaymentPeriod);

        return repaymentPeriod.getOutstandingPrincipal().add(repaymentPeriod.getOutstandingInterest())
                .add(nextInstallment.getFeeChargesOutstanding(currency)).add(nextInstallment.getPenaltyChargesOutstanding(currency))
                .getAmount();
    }

    @Override
    public boolean canAccept(Loan loan) {
        return loan.isProgressiveSchedule();
    }
}
