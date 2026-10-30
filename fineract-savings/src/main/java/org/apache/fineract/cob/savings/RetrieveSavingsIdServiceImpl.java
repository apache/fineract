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
package org.apache.fineract.cob.savings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.data.COBIdAndExternalIdAndAccountNo;
import org.apache.fineract.cob.data.COBIdAndLastClosedBusinessDate;
import org.apache.fineract.cob.data.COBParameter;
import org.apache.fineract.cob.data.COBPartition;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RetrieveSavingsIdServiceImpl implements RetrieveSavingsIdService {

    private static final Collection<Integer> NON_CLOSED_SAVINGS_STATUSES = new ArrayList<>(
            Arrays.asList(SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getValue(), SavingsAccountStatusType.APPROVED.getValue(),
                    SavingsAccountStatusType.ACTIVE.getValue(), SavingsAccountStatusType.TRANSFER_IN_PROGRESS.getValue(),
                    SavingsAccountStatusType.TRANSFER_ON_HOLD.getValue()));

    private final SavingsAccountRepository savingsAccountRepository;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<COBPartition> retrieveSavingsCOBPartitions(Long numberOfDays, LocalDate businessDate, boolean isCatchUp,
            int partitionSize) {
        String sql = """
                    select min(id) as min, max(id) as max, page, count(id) as count from
                    (select floor(((row_number() over(order by id))-1) / :pageSize) as page, t.* from
                    (select id from m_savings_account where status_enum in (:statusIds) and
                """;
        if (isCatchUp) {
            sql = sql + " last_closed_business_date = :businessDate ";
        } else {
            sql = sql + " (last_closed_business_date = :businessDate or last_closed_business_date is null) ";
        }
        sql = sql + " order by id) t) t2 group by page order by page";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("pageSize", partitionSize);
        parameters.addValue("statusIds", NON_CLOSED_SAVINGS_STATUSES);
        parameters.addValue("businessDate", businessDate.minusDays(numberOfDays));
        return namedParameterJdbcTemplate.query(sql.toString(), parameters, RetrieveSavingsIdServiceImpl::mapRow);
    }

    private static COBPartition mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new COBPartition(rs.getLong("min"), rs.getLong("max"), rs.getLong("page"), rs.getLong("count"));
    }

    @Override
    public List<COBIdAndLastClosedBusinessDate> retrieveSavingsIdsBehindDate(LocalDate businessDate, List<Long> savingsIds) {
        return savingsAccountRepository.findAllSavingsIdsBehindDate(businessDate, savingsIds);
    }

    @Override
    public List<COBIdAndLastClosedBusinessDate> retrieveSavingsIdsBehindDateOrNull(LocalDate businessDate, List<Long> savingsIds) {
        return savingsAccountRepository.findAllSavingsIdsBehindDateOrNull(businessDate, savingsIds);
    }

    @Override
    public List<COBIdAndLastClosedBusinessDate> retrieveSavingsIdsOldestCobProcessed(LocalDate businessDate) {
        return savingsAccountRepository.findAllSavingsIdsOldestCobProcessed();
    }

    @Override
    public List<Long> retrieveAllNonClosedSavingsByLastClosedBusinessDateAndMinAndMaxSavingsId(COBParameter savingsCOBParameter,
            boolean isCatchUp) {
        LocalDate cobBusinessDate = ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE)
                .minusDays(SavingsCOBConstant.NUMBER_OF_DAYS_BEHIND);

        if (isCatchUp) {
            return savingsAccountRepository.findAllSavingsByLastClosedBusinessDateNotNullAndMinAndMaxSavingsIdAndStatuses(
                    savingsCOBParameter.getMinAccountId(), savingsCOBParameter.getMaxAccountId(), cobBusinessDate,
                    NON_CLOSED_SAVINGS_STATUSES);
        } else {
            return savingsAccountRepository.findAllSavingsByLastClosedBusinessDateAndMinAndMaxSavingsIdAndStatuses(
                    savingsCOBParameter.getMinAccountId(), savingsCOBParameter.getMaxAccountId(), cobBusinessDate,
                    NON_CLOSED_SAVINGS_STATUSES);
        }
    }

    @Override
    public List<COBIdAndExternalIdAndAccountNo> findAllStayedLockedByCobBusinessDate(LocalDate cobBusinessDate) {
        // This will be implemented when we add the query to join with SavingsAccountLock
        // For now, return empty list as the lock table doesn't exist yet
        return List.of();
    }
}
