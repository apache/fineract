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

import static org.mockito.Mockito.times;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.cob.data.LoanCOBPartition;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

@ExtendWith(MockitoExtension.class)
public class RetrieveAllNonClosedLoanIdServiceImplTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Captor
    private ArgumentCaptor<String> sqlCaptor;
    @Captor
    private ArgumentCaptor<SqlParameterSource> paramsCaptor;
    @Captor
    private ArgumentCaptor<RowMapper<LoanCOBPartition>> rowMapper;

    @Test
    public void testRetrieveLoanCOBPartitionsNoCatchup() {
        String expectedSQL = """
                select min(id) as min, max(id) as max, page, count(id) as count from
                  (select  floor(((row_number() over(order by id))-1) / :pageSize) as page, t.* from
                        (select id from m_loan where loan_status_id in (:statusIds) and (last_closed_business_date = :businessDate or last_closed_business_date is null) order by id) t) t2
                 group by page
                 order by page
                """;
        testRetrieveLoanCOBPartitions(expectedSQL, false);
    }

    @Test
    public void testRetrieveLoanCOBPartitionsCatchup() {
        String expectedSQL = """
                select min(id) as min, max(id) as max, page, count(id) as count from
                 (select  floor(((row_number() over(order by id))-1) / :pageSize) as page, t.* from
                        (select id from m_loan where loan_status_id in (:statusIds) and last_closed_business_date = :businessDate order by id) t) t2
                 group by page
                 order by page
                """;
        testRetrieveLoanCOBPartitions(expectedSQL, true);
    }

    private void testRetrieveLoanCOBPartitions(String expectedSQL, boolean isCatchup) {
        RetrieveAllNonClosedLoanIdServiceImpl service = new RetrieveAllNonClosedLoanIdServiceImpl(loanRepository,
                namedParameterJdbcTemplate);
        LocalDate businessDate = LocalDate.parse("2023-06-28");
        service.retrieveLoanCOBPartitions(1L, businessDate, isCatchup, 5);
        Mockito.verify(namedParameterJdbcTemplate, times(1)).query(sqlCaptor.capture(), paramsCaptor.capture(), rowMapper.capture());
        Assertions.assertEquals(normalize(expectedSQL), normalize(sqlCaptor.getValue()));
        Assertions.assertEquals(5, paramsCaptor.getValue().getValue("pageSize"));
        Assertions.assertEquals(List.of(100, 200, 300, 303, 304), paramsCaptor.getValue().getValue("statusIds"));
        Assertions.assertEquals(LocalDate.parse("2023-06-27"), paramsCaptor.getValue().getValue("businessDate"));

    }

    private String normalize(String str) {
        return str.replaceAll(" +", " ").replaceAll("\r?\n", "");
    }
}
