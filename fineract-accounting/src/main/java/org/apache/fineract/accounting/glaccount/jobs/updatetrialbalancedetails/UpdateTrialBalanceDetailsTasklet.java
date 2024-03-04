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
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.glaccount.domain.TrialBalance;
import org.apache.fineract.accounting.glaccount.domain.TrialBalanceRepositoryWrapper;
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

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourceServiceFactory.determineDataSourceService().retrieveDataSource());
        final StringBuilder tbGapSqlBuilder = new StringBuilder(500);
        tbGapSqlBuilder.append("select distinct(je.transaction_date) ").append("from acc_gl_journal_entry je ")
                .append("where je.transaction_date > (select coalesce(MAX(created_date),'2010-01-01') from m_trial_balance)");
        final List<LocalDate> tbGaps = jdbcTemplate.queryForList(tbGapSqlBuilder.toString(), LocalDate.class);
        for (LocalDate tbGap : tbGaps) {
            int days = Math.toIntExact(ChronoUnit.DAYS.between(tbGap, DateUtils.getBusinessLocalDate()));
            if (days < 1) {
                continue;
            }
            final StringBuilder sqlBuilder = new StringBuilder(600);
            sqlBuilder.append("Insert Into m_trial_balance(office_id, account_id, Amount, entry_date, created_date,closing_balance) ")
                    .append("Select je.office_id, je.account_id, SUM(CASE WHEN je.type_enum=1 THEN (-1) * je.amount ELSE je.amount END) ")
                    .append("as Amount, Date(je.entry_date) as Entry_Date, je.transaction_date as Created_Date,sum(je.amount) as closing_balance ")
                    .append("from acc_gl_journal_entry je WHERE je.transaction_date = ? ")
                    .append("group by je.account_id, je.office_id, je.transaction_date, Date(je.entry_date)");
            final int result = jdbcTemplate.update(sqlBuilder.toString(), tbGap);
            log.debug("{}: Records affected by updateTrialBalanceDetails: {}", ThreadLocalContextUtil.getTenant().getName(), result);
        }
        String distinctOfficeQuery = "select distinct(office_id) from m_trial_balance where closing_balance is null group by office_id";
        final List<Long> officeIds = jdbcTemplate.queryForList(distinctOfficeQuery, Long.class);
        for (Long officeId : officeIds) {
            String distinctAccountQuery = "select distinct(account_id) from m_trial_balance where office_id=? and closing_balance is null group by account_id";
            final List<Long> accountIds = jdbcTemplate.queryForList(distinctAccountQuery, Long.class, officeId);
            for (Long accountId : accountIds) {
                final String closingBalanceQuery = "select closing_balance from m_trial_balance where office_id=? and account_id=? and closing_balance "
                        + "is not null order by created_date desc, entry_date desc limit 1";
                List<BigDecimal> closingBalanceData = jdbcTemplate.queryForList(closingBalanceQuery, BigDecimal.class, officeId, accountId);
                List<TrialBalance> tbRows = trialBalanceRepositoryWrapper.findNewByOfficeAndAccount(officeId, accountId);
                BigDecimal closingBalance = null;
                if (!CollectionUtils.isEmpty(closingBalanceData)) {
                    closingBalance = closingBalanceData.get(0);
                }
                if (CollectionUtils.isEmpty(closingBalanceData)) {
                    closingBalance = BigDecimal.ZERO;
                    for (TrialBalance row : tbRows) {
                        closingBalance = closingBalance.add(row.getAmount());
                        row.setClosingBalance(closingBalance);
                    }
                } else {
                    for (TrialBalance tbRow : tbRows) {
                        if (closingBalance != null) {
                            closingBalance = closingBalance.add(tbRow.getAmount());
                        }
                        tbRow.setClosingBalance(closingBalance);
                    }
                }
                trialBalanceRepositoryWrapper.save(tbRows);
            }
        }
        return RepeatStatus.FINISHED;
    }
}
