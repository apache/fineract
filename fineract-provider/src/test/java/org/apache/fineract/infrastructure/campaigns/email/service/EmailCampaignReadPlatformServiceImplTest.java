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
package org.apache.fineract.infrastructure.campaigns.email.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailBusinessRulesData;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Runs the QueryDSL queries of {@link EmailCampaignReadPlatformServiceImpl} through a real {@link SQLQueryFactory} on
 * top of a mocked JDBC connection, so that the generated SQL and result mapping are all verified.
 */
class EmailCampaignReadPlatformServiceImplTest {

    private static final String EXPECTED_SQL = """
            select sr.id, sr.report_name, sr.report_type, sr.report_subtype, sr.description, sp.parameter_variable, \
            sp.parameter_FormatType, sp.parameter_label, sp.parameter_name
            from stretchy_report sr
            left join stretchy_report_parameter srp
            on srp.report_id = sr.id
            left join stretchy_parameter sp
            on sp.id = srp.parameter_id
            where sr.report_type = ?""";

    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final List<String> executedSql = new ArrayList<>();
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

    @Test
    void retrieveAllGeneratesExpectedSqlAndBindsParameterOnPostgreSQL() throws SQLException {
        EmailCampaignReadPlatformServiceImpl service = service(PostgreSQLTemplates.builder().build(),
                new Object[] { 1, "Client Campaign", "Email", "ClientSub", "Description 1", "paramVar1", "text", "Office", "officeId" });

        Collection<EmailBusinessRulesData> result = service.retrieveAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, executedSql.size());
        assertEquals(EXPECTED_SQL, executedSql.get(0));
        verify(statement).setString(1, "Email");
    }

    @Test
    void retrieveAllGeneratesExpectedSqlAndBindsParameterOnMySQL() throws SQLException {
        EmailCampaignReadPlatformServiceImpl service = service(MySQLTemplates.builder().build(),
                new Object[] { 1, "Client Campaign", "Email", "ClientSub", "Description 1", "paramVar1", "text", "Office", "officeId" });

        Collection<EmailBusinessRulesData> result = service.retrieveAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, executedSql.size());
        assertEquals(EXPECTED_SQL, executedSql.get(0));
        verify(statement).setString(1, "Email");
    }

    @Test
    void retrieveAllGroupsMultipleParametersPerReport() throws SQLException {
        Object[] row1 = new Object[] { 1, "Client Campaign", "Email", "ClientSub", "Description 1", "paramVar1", "text", "Office",
                "officeId" };
        Object[] row2 = new Object[] { 1, "Client Campaign", "Email", "ClientSub", "Description 1", "paramVar2", "text", "Date",
                "dateParam" };
        Object[] row3 = new Object[] { 2, "Loan Campaign", "Email", "LoanSub", "Description 2", "paramVar3", "text", "LoanId", "loanId" };

        EmailCampaignReadPlatformServiceImpl service = service(PostgreSQLTemplates.builder().build(), row1, row2, row3);

        Collection<EmailBusinessRulesData> result = service.retrieveAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        Iterator<EmailBusinessRulesData> iterator = result.iterator();
        assertTrue(iterator.hasNext());
        EmailBusinessRulesData report1 = iterator.next();
        assertEquals(1L, report1.getReportId());
        assertEquals("Client Campaign", report1.getReportName());
        assertEquals("Email", report1.getReportType());
        assertEquals("ClientSub", report1.getReportSubType());
        assertEquals("Description 1", report1.getReportDescription());
        assertNotNull(report1.getReportParamName());
        assertEquals(2, report1.getReportParamName().size());
        assertEquals("officeId", report1.getReportParamName().get("Office"));
        assertEquals("dateParam", report1.getReportParamName().get("Date"));

        assertTrue(iterator.hasNext());
        EmailBusinessRulesData report2 = iterator.next();
        assertEquals(2L, report2.getReportId());
        assertEquals("Loan Campaign", report2.getReportName());
        assertEquals("Email", report2.getReportType());
        assertEquals("LoanSub", report2.getReportSubType());
        assertEquals("Description 2", report2.getReportDescription());
        assertNotNull(report2.getReportParamName());
        assertEquals(1, report2.getReportParamName().size());
        assertEquals("loanId", report2.getReportParamName().get("LoanId"));

        assertEquals(EXPECTED_SQL, executedSql.get(0));
        verify(statement).setString(1, "Email");
    }

    @Test
    void retrieveAllMapsReportWithoutParameters() throws SQLException {
        Object[] row = new Object[] { 3, "No Params Campaign", "Email", "NoSub", "No description", null, null, null, null };
        EmailCampaignReadPlatformServiceImpl service = service(PostgreSQLTemplates.builder().build(), row);

        Collection<EmailBusinessRulesData> result = service.retrieveAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        EmailBusinessRulesData data = result.iterator().next();
        assertEquals(3L, data.getReportId());
        assertEquals("No Params Campaign", data.getReportName());
        assertEquals("Email", data.getReportType());
        assertEquals("NoSub", data.getReportSubType());
        assertEquals("No description", data.getReportDescription());
        assertNotNull(data.getReportParamName());
        assertTrue(data.getReportParamName().isEmpty());

        assertEquals(EXPECTED_SQL, executedSql.get(0));
        verify(statement).setString(1, "Email");
    }

    @Test
    void retrieveAllReturnsEmptyCollectionWhenNoResults() throws SQLException {
        EmailCampaignReadPlatformServiceImpl service = service(PostgreSQLTemplates.builder().build());

        Collection<EmailBusinessRulesData> result = service.retrieveAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(1, executedSql.size());
        assertEquals(EXPECTED_SQL, executedSql.get(0));
        verify(statement).setString(1, "Email");
    }

    private EmailCampaignReadPlatformServiceImpl service(SQLTemplates templates, Object[]... rows) throws SQLException {
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            executedSql.add(invocation.getArgument(0));
            return statement;
        });
        when(statement.executeQuery()).thenReturn(resultSet(rows));

        return new EmailCampaignReadPlatformServiceImpl(jdbcTemplate,
                new SQLQueryFactory(new Configuration(templates), () -> connection));
    }

    /**
     * A {@link ResultSet} that serves the given rows by column index, including {@link ResultSet#wasNull()}.
     */
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
