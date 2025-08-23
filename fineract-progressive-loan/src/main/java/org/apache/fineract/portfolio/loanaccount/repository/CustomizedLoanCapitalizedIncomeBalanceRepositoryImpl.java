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
package org.apache.fineract.portfolio.loanaccount.repository;

import com.google.common.collect.Lists;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepaymentPeriodData;
import org.apache.fineract.util.StreamUtil;

@RequiredArgsConstructor
public class CustomizedLoanCapitalizedIncomeBalanceRepositoryImpl implements CustomizedLoanCapitalizedIncomeBalanceRepository {

    private final EntityManager entityManager;

    @Override
    public Map<Long, List<LoanTransactionRepaymentPeriodData>> findRepaymentPeriodDataByLoanIds(List<Long> loanIds) {
        List<List<Long>> partitions = Lists.partition(loanIds, DatabaseSpecificSQLGenerator.IN_CLAUSE_MAX_PARAMS);
        return partitions.stream().map(this::doFindRepaymentPeriodDataByLoanIds).collect(StreamUtil.mergeMapsOfLists());
    }

    private Map<Long, List<LoanTransactionRepaymentPeriodData>> doFindRepaymentPeriodDataByLoanIds(List<Long> loanIds) {
        // making the List serializable since sometimes it's just not
        // Caused by: java.lang.IllegalArgumentException: You have attempted to set a value of type
        // class java.util.ImmutableCollections$SubList for parameter loanIds with expected type of
        // interface java.io.Serializable from query string ...
        loanIds = new ArrayList<>(loanIds);

        TypedQuery<LoanTransactionRepaymentPeriodData> query = entityManager.createQuery(
                LoanCapitalizedIncomeBalanceRepository.FIND_BALANCE_REPAYMENT_SCHEDULE_DATA + " WHERE lcib.loan.id IN (:loanIds)",
                LoanTransactionRepaymentPeriodData.class);
        query.setParameter("loanIds", loanIds);
        List<LoanTransactionRepaymentPeriodData> result = query.getResultList();
        return result.stream().collect(Collectors.groupingBy(LoanTransactionRepaymentPeriodData::getLoanId));
    }
}
