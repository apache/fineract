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
package org.apache.fineract.portfolio.loanaccount.rescheduleloan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.jdbc.core.JdbcTemplate;

/** Verifies SQL generation, parameter binding and result mapping through a real QueryDSL factory. */
@ExtendWith(MockitoExtension.class)
class LoanRescheduleRequestReadPlatformServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private LoanRepositoryWrapper loanRepositoryWrapper;
    @Mock
    private CodeValueReadPlatformService codeValueReadPlatformService;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement statement;

    private String executedSql;

    @BeforeEach
    void setUp() throws SQLException {
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            executedSql = invocation.getArgument(0);
            return statement;
        });
    }

    @ParameterizedTest
    @MethodSource("templates")
    void retrievesAndMapsAllRequests(SQLTemplates templates) throws SQLException {
        LoanRescheduleRequestReadPlatformService service = service(templates,
                new Object[] { 7L, 100, Date.valueOf("2026-01-10"), 3, "Financial difficulty", 42L, "LN42", 9L, "Jane Doe" },
                new Object[] { 8L, 200, Date.valueOf("2026-02-10"), 4, "Seasonal income", 43L, "LN43", 10L, "John Doe" });

        List<LoanRescheduleRequestData> requests = service.retrieveAllRescheduleRequests(null, null);

        assertThat(requests).extracting(LoanRescheduleRequestData::getId).containsExactly(7L, 8L);
        LoanRescheduleRequestData request = requests.getFirst();
        assertThat(request.getLoanId()).isEqualTo(42L);
        assertThat(request.getLoanAccountNumber()).isEqualTo("LN42");
        assertThat(request.getClientId()).isEqualTo(9L);
        assertThat(request.getClientName()).isEqualTo("Jane Doe");
        assertThat(request.getRescheduleFromDate()).isEqualTo(LocalDate.of(2026, 1, 10));
        assertThat(request.getStatusEnum().id()).isEqualTo(100L);
        assertThat(request.getStatusEnum().value()).isEqualTo("Submitted and pending approval");
        assertThat(request.getRescheduleReasonCodeValue().getId()).isEqualTo(3L);
        assertThat(request.getRescheduleReasonCodeValue().getName()).isEqualTo("Financial difficulty");
        assertThat(requests.get(1).getStatusEnum().id()).isEqualTo(200L);
        assertThat(requests.get(1).getRescheduleReasonCodeValue().getId()).isEqualTo(4L);
        assertThat(executedSql).contains("left join m_loan ", "left join m_client ", "left join m_code_value ").doesNotContain("where");
        verifyNoInteractions(jdbcTemplate, loanRepositoryWrapper, codeValueReadPlatformService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "all", "ALL" })
    void omitsStatusFilterWhenAllStatusesAreRequested(String command) throws SQLException {
        LoanRescheduleRequestReadPlatformService service = service(PostgreSQLTemplates.builder().build());

        assertThat(service.retrieveAllRescheduleRequests(command, null)).isEmpty();

        assertThat(executedSql).doesNotContain("where");
    }

    @ParameterizedTest
    @MethodSource("statusFilters")
    void bindsExistingStatusFilter(SQLTemplates templates, String command, int status) throws SQLException {
        LoanRescheduleRequestReadPlatformService service = service(templates);

        assertThat(service.retrieveAllRescheduleRequests(command, null)).isEmpty();

        assertThat(executedSql).contains("where m_loan_reschedule_request.status_enum = ?");
        verify(statement).setInt(1, status);
    }

    @ParameterizedTest
    @MethodSource("templates")
    void combinesStatusAndLoanFilters(SQLTemplates templates) throws SQLException {
        LoanRescheduleRequestReadPlatformService service = service(templates);

        service.retrieveAllRescheduleRequests("pending", 42L);

        assertThat(executedSql).contains("where m_loan_reschedule_request.status_enum = ? and m_loan.id = ?");
        verify(statement).setInt(1, 100);
        verify(statement).setLong(2, 42L);
    }

    @ParameterizedTest
    @MethodSource("templates")
    void filtersByLoanWithoutAddingAStatusFilter(SQLTemplates templates) throws SQLException {
        LoanRescheduleRequestReadPlatformService service = service(templates);

        service.retrieveAllRescheduleRequests("all", 42L);

        assertThat(executedSql).contains("where m_loan.id = ?").doesNotContain("status_enum = ?");
        verify(statement).setLong(1, 42L);
    }

    @ParameterizedTest
    @MethodSource("templates")
    void preservesMissingClientAndReasonValues(SQLTemplates templates) throws SQLException {
        LoanRescheduleRequestReadPlatformService service = service(templates,
                new Object[] { 7L, 100, Date.valueOf("2026-01-10"), null, null, 42L, "LN42", null, null });

        LoanRescheduleRequestData request = service.retrieveAllRescheduleRequests(null, null).getFirst();

        assertThat(request.getLoanId()).isEqualTo(42L);
        assertThat(request.getClientId()).isZero();
        assertThat(request.getClientName()).isNull();
        assertThat(request.getRescheduleReasonCodeValue().getId()).isNull();
        assertThat(request.getRescheduleReasonCodeValue().getName()).isNull();
    }

    private static Stream<SQLTemplates> templates() {
        // The application also uses MySQLTemplates for MariaDB.
        return Stream.of(PostgreSQLTemplates.builder().build(), MySQLTemplates.builder().build());
    }

    private static Stream<Arguments> statusFilters() {
        return templates().flatMap(templates -> Stream.of(Arguments.of(templates, "pending", 100), Arguments.of(templates, "APPROVE", 200),
                Arguments.of(templates, "reject", 300)));
    }

    private LoanRescheduleRequestReadPlatformService service(SQLTemplates templates, Object[]... rows) throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet(rows));
        return new LoanRescheduleRequestReadPlatformServiceImpl(jdbcTemplate, loanRepositoryWrapper, codeValueReadPlatformService,
                new SQLQueryFactory(new Configuration(templates), () -> connection));
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
