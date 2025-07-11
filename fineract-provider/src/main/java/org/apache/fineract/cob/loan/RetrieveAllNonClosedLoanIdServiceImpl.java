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
package org.apache.fineract.cob.loan;

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
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@RequiredArgsConstructor
public class RetrieveAllNonClosedLoanIdServiceImpl implements RetrieveLoanIdService {

    private static final Collection<LoanStatus> NON_CLOSED_LOAN_STATUSES = new ArrayList<>(
            Arrays.asList(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL, LoanStatus.APPROVED, LoanStatus.ACTIVE,
                    LoanStatus.TRANSFER_IN_PROGRESS, LoanStatus.TRANSFER_ON_HOLD));

    private final LoanRepository loanRepository;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<COBPartition> retrieveLoanCOBPartitions(Long numberOfDays, LocalDate businessDate, boolean isCatchUp, int partitionSize) {
        StringBuilder sql = new StringBuilder();
        sql.append("select min(id) as min, max(id) as max, page, count(id) as count from ");
        sql.append("  (select floor(((row_number() over(order by id))-1) / :pageSize) as page, t.* from ");
        sql.append("      (select id from m_loan where loan_status_id in (:statusIds) and ");
        if (isCatchUp) {
            sql.append("last_closed_business_date = :businessDate ");
        } else {
            sql.append("(last_closed_business_date = :businessDate or last_closed_business_date is null) ");
        }
        sql.append("order by id) t) t2 ");
        sql.append("group by page ");
        sql.append("order by page");

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("pageSize", partitionSize);
        parameters.addValue("statusIds", List.of(100, 200, 300, 303, 304));
        parameters.addValue("businessDate", businessDate.minusDays(numberOfDays));
        return namedParameterJdbcTemplate.query(sql.toString(), parameters, RetrieveAllNonClosedLoanIdServiceImpl::mapRow);
    }

    private static COBPartition mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new COBPartition(rs.getLong("min"), rs.getLong("max"), rs.getLong("page"), rs.getLong("count"));
    }

    @Override
    public List<COBIdAndLastClosedBusinessDate> retrieveLoanIdsBehindDate(LocalDate businessDate, List<Long> loanIds) {
        return loanRepository.findAllLoansBehindByLoanIdsAndStatuses(businessDate, loanIds, NON_CLOSED_LOAN_STATUSES);
    }

    @Override
    public List<COBIdAndLastClosedBusinessDate> retrieveLoanIdsBehindDateOrNull(LocalDate businessDate, List<Long> loanIds) {
        return loanRepository.findAllLoansBehindOrNullByLoanIdsAndStatuses(businessDate, loanIds, NON_CLOSED_LOAN_STATUSES);
    }

    @Override
    public List<COBIdAndLastClosedBusinessDate> retrieveLoanIdsOldestCobProcessed(LocalDate businessDate) {
        return loanRepository.findOldestCOBProcessedLoan(businessDate, NON_CLOSED_LOAN_STATUSES);
    }

    @Override
    public List<Long> retrieveAllNonClosedLoansByLastClosedBusinessDateAndMinAndMaxLoanId(COBParameter loanCOBParameter,
            boolean isCatchUp) {
        if (isCatchUp) {
            return loanRepository.findAllLoansByLastClosedBusinessDateNotNullAndMinAndMaxLoanIdAndStatuses(
                    loanCOBParameter.getMinAccountId(), loanCOBParameter.getMaxAccountId(), ThreadLocalContextUtil
                            .getBusinessDateByType(BusinessDateType.COB_DATE).minusDays(LoanCOBConstant.NUMBER_OF_DAYS_BEHIND),
                    NON_CLOSED_LOAN_STATUSES);
        } else {
            return loanRepository.findAllLoansByLastClosedBusinessDateAndMinAndMaxLoanIdAndStatuses(
                    loanCOBParameter.getMinAccountId(), loanCOBParameter.getMaxAccountId(), ThreadLocalContextUtil
                            .getBusinessDateByType(BusinessDateType.COB_DATE).minusDays(LoanCOBConstant.NUMBER_OF_DAYS_BEHIND),
                    NON_CLOSED_LOAN_STATUSES);
        }
    }

    @Override
    public List<COBIdAndExternalIdAndAccountNo> findAllStayedLockedByCobBusinessDate(LocalDate cobBusinessDate) {
        return loanRepository.findAllStayedLockedByCobBusinessDate(cobBusinessDate);
    }

}
