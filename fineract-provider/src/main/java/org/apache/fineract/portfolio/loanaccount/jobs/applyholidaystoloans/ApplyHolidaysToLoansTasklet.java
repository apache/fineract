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
package org.apache.fineract.portfolio.loanaccount.jobs.applyholidaystoloans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.service.LoanUtilService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class ApplyHolidaysToLoansTasklet implements Tasklet {

    private final ConfigurationDomainService configurationDomainService;
    private final HolidayRepositoryWrapper holidayRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanUtilService loanUtilService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        final boolean isHolidayEnabled = configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();

        if (!isHolidayEnabled) {
            return RepeatStatus.FINISHED;
        }

        final Collection<Integer> loanStatuses = new ArrayList<>(Arrays.asList(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue(),
                LoanStatus.APPROVED.getValue(), LoanStatus.ACTIVE.getValue()));
        final List<Holiday> holidays = holidayRepository.findUnprocessed();

        for (final Holiday holiday : holidays) {
            final Set<Office> offices = holiday.getOffices();
            final Collection<Long> officeIds = new ArrayList<>(offices.size());
            for (final Office office : offices) {
                officeIds.add(office.getId());
            }

            final List<Loan> loans = new ArrayList<>();
            loans.addAll(loanRepositoryWrapper.findByClientOfficeIdsAndLoanStatus(officeIds, loanStatuses));
            // FIXME: AA optimize to get all client and group loans belongs to an office id
            loans.addAll(loanRepositoryWrapper.findByGroupOfficeIdsAndLoanStatus(officeIds, loanStatuses));

            for (final Loan loan : loans) {
                loan.applyHolidayToRepaymentScheduleDates(holiday, loanUtilService);
            }
            loanRepositoryWrapper.save(loans);
            holiday.processed();
        }
        holidayRepository.save(holidays);
        return RepeatStatus.FINISHED;
    }
}
