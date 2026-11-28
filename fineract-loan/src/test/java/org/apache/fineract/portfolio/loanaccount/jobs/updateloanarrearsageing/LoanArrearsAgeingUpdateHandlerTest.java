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
package org.apache.fineract.portfolio.loanaccount.jobs.updateloanarrearsageing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.apache.fineract.portfolio.loanaccount.service.LoanArrearsAgingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;

/** Verifies the loan-summary query and mapping through the public job and COB entry points. */
@ExtendWith(MockitoExtension.class)
class LoanArrearsAgeingUpdateHandlerTest {

    private static final String SUMMARY_SELECT = """
            select ml.id as loanId, ml.principal_repaid_derived as principalAmtPaid,
            ml.principal_writtenoff_derived as principalAmtWrittenoff, ml.interest_repaid_derived as interestAmtPaid,
            ml.interest_waived_derived as interestAmtWaived, ml.fee_charges_repaid_derived as feeAmtPaid,
            ml.fee_charges_waived_derived as feeAmtWaived, ml.penalty_charges_repaid_derived as penaltyAmtPaid,
            ml.penalty_charges_waived_derived as penaltyAmtWaived
            from m_loan ml
            """;

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private DatabaseSpecificSQLGenerator sqlGenerator;
    @Mock
    private LoanArrearsAgingService loanArrearsAgingService;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement statement;
    @Captor
    private ArgumentCaptor<List<Map<String, Object>>> loanSummaries;

    private final Map<Long, List<LoanSchedulePeriodData>> scheduleDate = new HashMap<>();
    private String executedSql;

    @BeforeEach
    void setUp() {
        when(sqlGenerator.currentBusinessDate()).thenReturn("DATE '2026-11-28'");
        when(sqlGenerator.subDate(anyString(), anyString(), eq("day"))).thenReturn("DATE '2026-11-25'");
    }

    @ParameterizedTest
    @MethodSource("templates")
    void bulkUpdateSelectsRequestedLoansInIdOrderAndPreservesEverySummaryValue(SQLTemplates templates) throws SQLException {
        long largeLoanId = 3_000_000_057L;
        givenBulkLoanIds(List.of(largeLoanId, 42L));
        givenSchedule();
        givenRows(new Object[] { 42L, new BigDecimal("1234567890123.123456"), new BigDecimal("20.234567"), new BigDecimal("150.345678"),
                new BigDecimal("10.456789"), new BigDecimal("3.567890"), new BigDecimal("1.678901"), new BigDecimal("2.789012"),
                new BigDecimal("0.890123") }, zeroRow(largeLoanId));

        handler(templates).updateLoanArrearsAgeingDetailsForAllLoans();

        List<Map<String, Object>> summaries = forwardedSummaries();
        assertThat(summaries).hasSize(2);
        assertThat(summaries.getFirst()).isEqualTo(Map.of("loanId", 42L, "principalAmtPaid", new BigDecimal("1234567890123.123456"),
                "principalAmtWrittenoff", new BigDecimal("20.234567"), "interestAmtPaid", new BigDecimal("150.345678"), "interestAmtWaived",
                new BigDecimal("10.456789"), "feeAmtPaid", new BigDecimal("3.567890"), "feeAmtWaived", new BigDecimal("1.678901"),
                "penaltyAmtPaid", new BigDecimal("2.789012"), "penaltyAmtWaived", new BigDecimal("0.890123")));
        assertThat(summaries.get(1)).isEqualTo(zeroSummary(largeLoanId));
        assertSummarySql("where ml.id in (?, ?) order by ml.id asc");
        verify(statement).setLong(1, largeLoanId);
        verify(statement).setLong(2, 42L);
    }

    @ParameterizedTest
    @MethodSource("templates")
    void cobUpdateBindsSingleLoanAndPreservesZeroBalances(SQLTemplates templates) throws SQLException {
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class), any(int[].class), eq(Long.class)))
                .thenReturn(List.of(42L));
        when(jdbcTemplate.batchUpdate(anyString(), anyList())).thenReturn(new int[0]);
        givenSchedule();
        givenRows(zeroRow(42L));

        handler(templates).updateLoanArrearsAgeingDetails(List.of(42L));

        assertThat(forwardedSummaries()).containsExactly(zeroSummary(42L));
        assertSummarySql("where ml.id = ? order by ml.id asc");
        verify(statement).setLong(1, 42L);
    }

    @ParameterizedTest
    @MethodSource("templates")
    void bulkUpdateForwardsEmptySummaryWhenSelectedLoansHaveNoSummaryRows(SQLTemplates templates) throws SQLException {
        givenBulkLoanIds(List.of(42L));
        givenSchedule();
        givenRows();

        handler(templates).updateLoanArrearsAgeingDetailsForAllLoans();

        assertThat(forwardedSummaries()).isEmpty();
        assertSummarySql("where ml.id = ? order by ml.id asc");
        verify(statement).setLong(1, 42L);
    }

    @Test
    void bulkUpdateSkipsSummaryQueryWhenNoLoansUseOriginalSchedule() {
        givenBulkLoanIds(List.of());

        handler(PostgreSQLTemplates.builder().build()).updateLoanArrearsAgeingDetailsForAllLoans();

        verifyNoInteractions(connection, statement, loanArrearsAgingService);
    }

    private void givenBulkLoanIds(List<Long> loanIds) {
        when(jdbcTemplate.queryForList(anyString(), eq(Long.class))).thenReturn(loanIds);
        when(jdbcTemplate.batchUpdate(any(String[].class))).thenReturn(new int[0]);
    }

    private void givenSchedule() {
        when(jdbcTemplate.query(any(PreparedStatementCreator.class),
                ArgumentMatchers.<ResultSetExtractor<Map<Long, List<LoanSchedulePeriodData>>>>any())).thenReturn(scheduleDate);
    }

    private void givenRows(Object[]... rows) throws SQLException {
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            executedSql = invocation.getArgument(0);
            return statement;
        });
        when(statement.executeQuery()).thenReturn(resultSet(rows));
    }

    private LoanArrearsAgeingUpdateHandler handler(SQLTemplates templates) {
        return new LoanArrearsAgeingUpdateHandler(jdbcTemplate, sqlGenerator, loanArrearsAgingService,
                new SQLQueryFactory(new Configuration(templates), () -> connection));
    }

    private List<Map<String, Object>> forwardedSummaries() {
        verify(loanArrearsAgingService).updateScheduleWithPaidDetail(same(scheduleDate), loanSummaries.capture());
        return loanSummaries.getValue();
    }

    private void assertSummarySql(String filterAndOrder) {
        assertThat(executedSql.replaceAll("\\s+", " ").trim()).isEqualTo((SUMMARY_SELECT + filterAndOrder).replaceAll("\\s+", " ").trim());
    }

    private static Stream<SQLTemplates> templates() {
        // MariaDB uses the same QueryDSL templates as MySQL.
        return Stream.of(PostgreSQLTemplates.builder().build(), MySQLTemplates.builder().build());
    }

    private static Object[] zeroRow(long loanId) {
        return new Object[] { loanId, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO };
    }

    private static Map<String, Object> zeroSummary(long loanId) {
        return Map.of("loanId", loanId, "principalAmtPaid", BigDecimal.ZERO, "principalAmtWrittenoff", BigDecimal.ZERO, "interestAmtPaid",
                BigDecimal.ZERO, "interestAmtWaived", BigDecimal.ZERO, "feeAmtPaid", BigDecimal.ZERO, "feeAmtWaived", BigDecimal.ZERO,
                "penaltyAmtPaid", BigDecimal.ZERO, "penaltyAmtWaived", BigDecimal.ZERO);
    }

    private static ResultSet resultSet(Object[]... rows) {
        AtomicInteger current = new AtomicInteger(-1);
        AtomicInteger lastColumn = new AtomicInteger();
        Answer<Object> answer = invocation -> {
            String method = invocation.getMethod().getName();
            if ("next".equals(method)) {
                return current.incrementAndGet() < rows.length;
            }
            if ("wasNull".equals(method)) {
                return rows[current.get()][lastColumn.get() - 1] == null;
            }
            if (method.startsWith("get") && invocation.getArguments().length > 0 && invocation.getArgument(0) instanceof Integer column) {
                lastColumn.set(column);
                Object value = rows[current.get()][column - 1];
                return value == null ? Mockito.RETURNS_DEFAULTS.answer(invocation) : value;
            }
            return Mockito.RETURNS_DEFAULTS.answer(invocation);
        };
        return mock(ResultSet.class, answer);
    }
}
