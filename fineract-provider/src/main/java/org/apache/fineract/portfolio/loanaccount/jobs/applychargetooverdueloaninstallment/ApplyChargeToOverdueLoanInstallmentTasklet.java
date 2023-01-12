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
package org.apache.fineract.portfolio.loanaccount.jobs.applychargetooverdueloaninstallment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.event.business.annotation.BulkEventSupport;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.OverdueLoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
@BulkEventSupport
public class ApplyChargeToOverdueLoanInstallmentTasklet implements Tasklet {

    private final ConfigurationDomainService configurationDomainService;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanChargeWritePlatformService loanChargeWritePlatformService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        final Long penaltyWaitPeriodValue = configurationDomainService.retrievePenaltyWaitPeriod();
        final Boolean backdatePenalties = configurationDomainService.isBackdatePenaltiesEnabled();
        final Collection<OverdueLoanScheduleData> overdueLoanScheduledInstallments = loanReadPlatformService
                .retrieveAllLoansWithOverdueInstallments(penaltyWaitPeriodValue, backdatePenalties);

        if (!overdueLoanScheduledInstallments.isEmpty()) {
            final Map<Long, Collection<OverdueLoanScheduleData>> overdueScheduleData = new HashMap<>();
            for (final OverdueLoanScheduleData overdueInstallment : overdueLoanScheduledInstallments) {
                if (overdueScheduleData.containsKey(overdueInstallment.getLoanId())) {
                    overdueScheduleData.get(overdueInstallment.getLoanId()).add(overdueInstallment);
                } else {
                    Collection<OverdueLoanScheduleData> loanData = new ArrayList<>();
                    loanData.add(overdueInstallment);
                    overdueScheduleData.put(overdueInstallment.getLoanId(), loanData);
                }
            }

            List<Throwable> exceptions = new ArrayList<>();
            for (final Long loanId : overdueScheduleData.keySet()) {
                try {
                    loanChargeWritePlatformService.applyOverdueChargesForLoan(loanId, overdueScheduleData.get(loanId));

                } catch (final PlatformApiDataValidationException e) {
                    final List<ApiParameterError> errors = e.getErrors();
                    for (final ApiParameterError error : errors) {
                        log.error("Apply Charges due for overdue loans failed for account {} with message: {}", loanId,
                                error.getDeveloperMessage(), e);
                    }
                    exceptions.add(e);
                } catch (final AbstractPlatformDomainRuleException e) {
                    log.error("Apply Charges due for overdue loans failed for account {} with message: {}", loanId,
                            e.getDefaultUserMessage(), e);
                    exceptions.add(e);
                } catch (Exception e) {
                    log.error("Apply Charges due for overdue loans failed for account {}", loanId, e);
                    exceptions.add(e);
                }
            }
            if (!exceptions.isEmpty()) {
                throw new JobExecutionException(exceptions);
            }
        }
        return RepeatStatus.FINISHED;
    }
}
