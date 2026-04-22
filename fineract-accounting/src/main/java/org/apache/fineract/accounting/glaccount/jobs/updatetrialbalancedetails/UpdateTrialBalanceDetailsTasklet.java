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
package org.apache.fineract.accounting.glaccount.jobs.updatetrialbalancedetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.glaccount.domain.TrialBalance;
import org.apache.fineract.accounting.glaccount.domain.TrialBalanceRepository;
import org.apache.fineract.accounting.glaccount.domain.TrialBalanceRepositoryWrapper;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.database.RoutingDataSourceServiceFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

@Slf4j
@RequiredArgsConstructor
public class UpdateTrialBalanceDetailsTasklet implements Tasklet {

    private final RoutingDataSourceServiceFactory dataSourceServiceFactory;
    private final TrialBalanceRepositoryWrapper trialBalanceRepositoryWrapper;
    private final TrialBalanceRepository trialBalanceRepository;
    private final JournalEntryRepository journalEntryRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourceServiceFactory.determineDataSourceService().retrieveDataSource());

        processTrialBalanceGaps(jdbcTemplate);
        updateClosingBalances(jdbcTemplate);

        return RepeatStatus.FINISHED;
    }

    private void processTrialBalanceGaps(JdbcTemplate jdbcTemplate) {
        LocalDate maxCreatedDate = trialBalanceRepository.findMaxCreatedDate();
        LocalDate baselineDate = maxCreatedDate != null ? maxCreatedDate : LocalDate.of(2010, 1, 1);
        List<LocalDate> tbGaps = journalEntryRepository.findTransactionDatesAfter(baselineDate);
        for (LocalDate tbGap : tbGaps) {
            if (DateUtils.getExactDifferenceInDays(tbGap, DateUtils.getBusinessLocalDate()) < 1) {
                continue;
            }
            insertTrialBalanceForDate(tbGap);
        }
    }

    private void insertTrialBalanceForDate(LocalDate tbGap) {
        List<Object[]> rows = journalEntryRepository.findTrialBalanceLinesForDate(tbGap);

        List<TrialBalance> trialBalances = rows.stream().map(row -> {
            TrialBalance tb = new TrialBalance();
            tb.setOfficeId((Long) row[0]);
            tb.setGlAccountId((Long) row[1]);
            tb.setAmount((BigDecimal) row[2]);
            tb.setEntryDate((LocalDate) row[3]);
            tb.setTransactionDate((LocalDate) row[4]);
            tb.setClosingBalance((BigDecimal) row[5]);
            return tb;
        }).toList();

        trialBalanceRepositoryWrapper.save(trialBalances);

        log.debug("{}: Records affected by updateTrialBalanceDetails: {}", ThreadLocalContextUtil.getTenant().getName(),
                trialBalances.size());
    }

    private void updateClosingBalances(JdbcTemplate jdbcTemplate) {
        final List<Long> officeIds = trialBalanceRepository.findDistinctOfficeIdsWithNullClosingBalance();

        for (Long officeId : officeIds) {
            updateClosingBalancesForOffice(jdbcTemplate, officeId);
        }
    }

    private void updateClosingBalancesForOffice(JdbcTemplate jdbcTemplate, Long officeId) {
        final List<Long> accountIds = trialBalanceRepository.findDistinctAccountIdsWithNullClosingBalanceByOfficeId(officeId);

        for (Long accountId : accountIds) {
            updateClosingBalanceForAccount(jdbcTemplate, officeId, accountId);
        }
    }

    private void updateClosingBalanceForAccount(JdbcTemplate jdbcTemplate, Long officeId, Long accountId) {
        BigDecimal closingBalance = getPreviousClosingBalance(officeId, accountId);
        List<TrialBalance> tbRows = trialBalanceRepositoryWrapper.findNewByOfficeAndAccount(officeId, accountId);

        updateTrialBalanceRows(tbRows, closingBalance);
    }

    private BigDecimal getPreviousClosingBalance(Long officeId, Long accountId) {
        List<BigDecimal> closingBalanceData = trialBalanceRepository.findLastClosingBalance(officeId, accountId);
        return CollectionUtils.isEmpty(closingBalanceData) ? BigDecimal.ZERO : closingBalanceData.getFirst();
    }

    private void updateTrialBalanceRows(List<TrialBalance> tbRows, BigDecimal initialClosingBalance) {
        BigDecimal closingBalance = initialClosingBalance;

        for (TrialBalance row : tbRows) {
            if (closingBalance != null) {
                closingBalance = closingBalance.add(row.getAmount());
            }
            row.setClosingBalance(closingBalance);
        }
    }
}
