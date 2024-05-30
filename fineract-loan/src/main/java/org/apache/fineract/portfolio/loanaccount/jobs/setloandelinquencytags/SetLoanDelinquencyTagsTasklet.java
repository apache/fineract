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
package org.apache.fineract.portfolio.loanaccount.jobs.setloandelinquencytags;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyAction;
import org.apache.fineract.portfolio.delinquency.helper.DelinquencyEffectivePauseHelper;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyWritePlatformService;
import org.apache.fineract.portfolio.delinquency.validator.LoanDelinquencyActionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanScheduleDelinquencyData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallmentRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class SetLoanDelinquencyTagsTasklet implements Tasklet {

    private final DelinquencyWritePlatformService delinquencyWritePlatformService;
    private final LoanRepaymentScheduleInstallmentRepository loanRepaymentScheduleInstallmentRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper;
    private final DelinquencyReadPlatformService delinquencyReadPlatformService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // Set DEFAULT action context to use the business step instead of COB date
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);

        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        log.debug("Run job for date {}", businessDate);

        // Read Loan Ids with Loan Transaction Charge back
        Collection<LoanScheduleDelinquencyData> loanScheduleDelinquencyData = this.loanTransactionRepository
                .fetchLoanTransactionsByTypeAndLessOrEqualDate(LoanTransactionType.CHARGEBACK.getValue(), businessDate);
        List<Long> processedLoans = applyDelinquencyTagToLoans(loanScheduleDelinquencyData);
        log.debug("{}: Records affected by setLoanDelinquencyTags: {}", ThreadLocalContextUtil.getTenant().getName(),
                processedLoans.size());

        // Read Loan Ids with overdue installments
        if (processedLoans.isEmpty()) {
            loanScheduleDelinquencyData = this.loanRepaymentScheduleInstallmentRepository
                    .fetchLoanScheduleDataByDueDateAndObligationsMet(LoanStatus.ACTIVE.getValue(), businessDate, false);
        } else {
            loanScheduleDelinquencyData = this.loanRepaymentScheduleInstallmentRepository
                    .fetchLoanScheduleDataByDueDateAndObligationsMet(LoanStatus.ACTIVE.getValue(), businessDate, false, processedLoans);
        }
        applyDelinquencyTagToLoans(loanScheduleDelinquencyData);

        return RepeatStatus.FINISHED;
    }

    private List<Long> applyDelinquencyTagToLoans(Collection<LoanScheduleDelinquencyData> loanScheduleDelinquencyData) {
        List<Long> processedLoans = new ArrayList<>();

        log.debug("Were found {} items", loanScheduleDelinquencyData.size());
        for (LoanScheduleDelinquencyData loanDelinquencyData : loanScheduleDelinquencyData) {
            // Set the data used by Delinquency Classification method
            List<LoanDelinquencyAction> savedDelinquencyList = delinquencyReadPlatformService
                    .retrieveLoanDelinquencyActions(loanDelinquencyData.getLoanId());
            List<LoanDelinquencyActionData> effectiveDelinquencyList = delinquencyEffectivePauseHelper
                    .calculateEffectiveDelinquencyList(savedDelinquencyList);

            loanDelinquencyData = this.delinquencyWritePlatformService.calculateDelinquencyData(loanDelinquencyData,
                    effectiveDelinquencyList);
            log.debug("Processing Loan {} with {} overdue days since date {}", loanDelinquencyData.getLoanId(),
                    loanDelinquencyData.getOverdueDays(), loanDelinquencyData.getOverdueSinceDate());
            // Set or Unset the Delinquency Classification Tag
            if (loanDelinquencyData.getOverdueDays() > 0) {
                this.delinquencyWritePlatformService.applyDelinquencyTagToLoan(loanDelinquencyData, effectiveDelinquencyList);
            } else {
                this.delinquencyWritePlatformService.removeDelinquencyTagToLoan(loanDelinquencyData.getLoan());
            }

            processedLoans.add(loanDelinquencyData.getLoanId());
        }
        return processedLoans;
    }

}
