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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanCollectionData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanDelinquencyTagHistoryData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanRangeScheduleDelinquencyData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeScheduleTagHistory;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryMapper;
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

        template.setRangeLevelDelinquency(list);

        return template;
    }

    @Override
    public List<WorkingCapitalLoanDelinquencyTagHistoryData> retrieveDelinquencyRangeScheduleTagHistory(Long loanId) {
        List<WorkingCapitalLoanDelinquencyRangeScheduleTagHistory> byLoanIdOrderByAddedOnDateDesc = delinquencyRangeScheduleTagHistoryRepository
                .findByLoanIdOrderByAddedOnDateDesc(loanId);
        return delinquencyRangeScheduleTagHistoryMapper.map(byLoanIdOrderByAddedOnDateDesc);
    }
}
