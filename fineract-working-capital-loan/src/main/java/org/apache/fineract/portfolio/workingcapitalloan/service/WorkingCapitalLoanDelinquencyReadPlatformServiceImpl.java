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

package org.apache.fineract.portfolio.workingcapitalloan.service;

import static org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyPauseUtils.isPauseActiveOnDate;
import static org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyPauseUtils.resolveEffectivePauseEnd;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.loanaccount.data.DelinquencyPausePeriod;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanCollectionData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanDelinquencyTagHistoryData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanRangeScheduleDelinquencyData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeScheduleTagHistory;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionFinder;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryMapper;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class WorkingCapitalLoanDelinquencyReadPlatformServiceImpl implements WorkingCapitalLoanDelinquencyReadPlatformService {

    private final WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryMapper delinquencyRangeScheduleTagHistoryMapper;
    private final WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryRepository delinquencyRangeScheduleTagHistoryRepository;
    private final WorkingCapitalLoanDelinquencyRangeScheduleRepository delinquencyRangeScheduleRepository;
    private final WorkingCapitalLoanDelinquencyActionRepository delinquencyActionRepository;
    private final WorkingCapitalLoanTransactionFinder transactionFinder;

    @Override
    public WorkingCapitalLoanCollectionData getCollectionData(Long loanId, LocalDate businessDate) {
        final WorkingCapitalLoanCollectionData template = WorkingCapitalLoanCollectionData.initializeEmptyData();
        List<WorkingCapitalLoanDelinquencyRangeScheduleTagHistory> byLoanIdOrderByAddedOnDateDesc = delinquencyRangeScheduleTagHistoryRepository
                .findByLoanIdOrderByAddedOnDateDesc(loanId);
        List<WorkingCapitalLoanRangeScheduleDelinquencyData> list = byLoanIdOrderByAddedOnDateDesc.stream()
                // get active delinquency tags
                .filter(x -> x.getLiftedOnDate() == null).map(delinquencyRangeScheduleTagHistoryMapper::mapForCollectionData).toList();

        Optional<WorkingCapitalLoanDelinquencyRangeScheduleTagHistory> oldestDelinquentTag = byLoanIdOrderByAddedOnDateDesc.stream()
                .filter(x -> x.getLiftedOnDate() == null)
                .min(Comparator.comparing(WorkingCapitalLoanDelinquencyRangeScheduleTagHistory::getAddedOnDate));

        if (oldestDelinquentTag.isPresent()) {
            template.setDelinquentDays(DateUtils.getDifferenceInDays(oldestDelinquentTag.get().getAddedOnDate(), businessDate) + 1);
            template.setDelinquentDate(oldestDelinquentTag.get().getAddedOnDate());
            BigDecimal delinquentAmount = delinquencyRangeScheduleRepository.getTotalDelinquentAmount(loanId);
            template.setDelinquentAmount(delinquentAmount);
            template.setDelinquentPrincipal(delinquentAmount);
        }

        delinquencyRangeScheduleRepository.findTopByLoanIdAndMinPaymentCriteriaMetFalseOrderByFromDateAsc(loanId)
                .map(WorkingCapitalLoanDelinquencyRangeSchedule::getDelinquentDays).ifPresent(template::setPastDueDays);

        template.setInstallmentLevelDelinquency(list);
        template.setDelinquencyPausePeriods(retrieveDelinquencyPausePeriods(loanId, businessDate));

        // Unlike everything above, the last payment / repayment are not evaluated as of businessDate: they answer
        // "when was this loan last paid" over the whole ledger, the way Loan#getLastPaymentTransaction() does for
        // core loans, rather than describing the delinquency picture on a given day.
        transactionFinder.findLastPayment(loanId).ifPresent(lastPayment -> {
            template.setLastPaymentDate(lastPayment.transactionDate());
            template.setLastPaymentAmount(lastPayment.transactionAmount());
        });
        transactionFinder.findLastRepayment(loanId).ifPresent(lastRepayment -> {
            template.setLastRepaymentDate(lastRepayment.transactionDate());
            template.setLastRepaymentAmount(lastRepayment.transactionAmount());
        });

        return template;
    }

    private List<DelinquencyPausePeriod> retrieveDelinquencyPausePeriods(final Long loanId, final LocalDate businessDate) {
        final List<WorkingCapitalLoanDelinquencyAction> pauses = delinquencyActionRepository
                .findByWorkingCapitalLoanIdAndActionOrderByStartDateAsc(loanId, DelinquencyAction.PAUSE);
        final List<WorkingCapitalLoanDelinquencyAction> resumes = delinquencyActionRepository
                .findByWorkingCapitalLoanIdAndActionOrderByStartDateAsc(loanId, DelinquencyAction.RESUME);
        return pauses.stream().map(pause -> toPausePeriod(pause, resumes, businessDate)).toList();
    }

    private DelinquencyPausePeriod toPausePeriod(final WorkingCapitalLoanDelinquencyAction pause,
            final List<WorkingCapitalLoanDelinquencyAction> resumes, final LocalDate businessDate) {
        final LocalDate effectiveEnd = resolveEffectivePauseEnd(pause, resumes);
        return new DelinquencyPausePeriod(isPauseActiveOnDate(pause.getStartDate(), effectiveEnd, businessDate), pause.getStartDate(),
                effectiveEnd);
    }

    @Override
    public List<WorkingCapitalLoanDelinquencyTagHistoryData> retrieveDelinquencyRangeScheduleTagHistory(Long loanId) {
        List<WorkingCapitalLoanDelinquencyRangeScheduleTagHistory> byLoanIdOrderByAddedOnDateDesc = delinquencyRangeScheduleTagHistoryRepository
                .findByLoanIdOrderByAddedOnDateDesc(loanId);
        return delinquencyRangeScheduleTagHistoryMapper.map(byLoanIdOrderByAddedOnDateDesc);
    }
}
