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
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.adhocquery.service;

import static org.apache.fineract.query.QMAdhoc.mAdhoc;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.adhocquery.data.AdHocData;
import org.apache.fineract.adhocquery.domain.AdHocProjection;
import org.apache.fineract.adhocquery.exception.AdHocNotFoundException;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.query.QMAppuser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Exercises the QueryDSL read queries against a mocked JDBC connection to verify SQL, bindings, and row mapping.
 */
class AdHocReadPlatformServiceImplTest {

    private static final String EXPECTED_SQL = """
            select m_adhoc.id, m_adhoc.name, m_adhoc.query, m_adhoc.table_name, m_adhoc.table_fields, m_adhoc.is_active, m_adhoc.email, \
            m_adhoc.report_run_frequency_code, m_adhoc.report_run_every, m_adhoc.last_run, m_adhoc.created_date, m_adhoc.createdby_id, \
            cb.username, m_adhoc.lastmodifiedby_id, m_adhoc.lastmodified_date
            from m_adhoc m_adhoc
            left join m_appuser cb
            on cb.id = m_adhoc.createdby_id
            where m_adhoc.id = ?
            limit ?""";

    private static final String EXPECTED_ALL_SQL = """
            select m_adhoc.id, m_adhoc.name, m_adhoc.query, m_adhoc.table_name, m_adhoc.table_fields, m_adhoc.is_active, m_adhoc.email, \
            m_adhoc.report_run_frequency_code, m_adhoc.report_run_every, m_adhoc.last_run, m_adhoc.created_date, m_adhoc.createdby_id, \
            cb.username, m_adhoc.lastmodifiedby_id, m_adhoc.lastmodified_date
            from m_adhoc m_adhoc
            left join m_appuser cb
            on cb.id = m_adhoc.createdby_id
            order by m_adhoc.id asc""";

    private static final ZoneId TENANT_ZONE = ZoneId.of("Asia/Kolkata");

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final DatabaseSpecificSQLGenerator sqlGenerator = mock(DatabaseSpecificSQLGenerator.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private String executedSql;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", TENANT_ZONE.getId(), null));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void retrieveOneMapsAllFieldsOnPostgreSQL() throws SQLException {
        final AdHocReadPlatformService service = service(PostgreSQLTemplates.builder().build(), fullRow());

        final AdHocData adHoc = service.retrieveOne(7L);

        assertFullRow(adHoc);
        assertEquals(EXPECTED_SQL, executedSql);
        verify(statement).setLong(1, 7L);
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void retrieveOneMapsAllFieldsOnMySQL() throws SQLException {
        final AdHocReadPlatformService service = service(MySQLTemplates.builder().build(), fullRow());

        final AdHocData adHoc = service.retrieveOne(7L);

        assertFullRow(adHoc);
        assertEquals(EXPECTED_SQL, executedSql);
        verify(statement).setLong(1, 7L);
    }

    @Test
    void retrieveOneKeepsNullableColumnsNull() throws SQLException {
        final AdHocReadPlatformService service = service(PostgreSQLTemplates.builder().build(),
                new Object[] { 3L, "minimal", "select 1", "m_temp", "a", false, null, null, null, null, null, 1L, null, 1L, null });

        final AdHocData adHoc = service.retrieveOne(3L);

        assertEquals(3L, adHoc.getId());
        assertFalse(adHoc.isActive());
        assertNull(adHoc.getEmail());
        assertNull(adHoc.getReportRunFrequency());
        assertNull(adHoc.getReportRunEvery());
        assertNull(adHoc.getLastRun());
        assertNull(adHoc.getCreatedOn());
        assertNull(adHoc.getUpdatedOn());
        assertNull(adHoc.getCreatedBy());
    }

    @Test
    void retrieveOneThrowsAdHocNotFoundWhenIdDoesNotExist() throws SQLException {
        final AdHocReadPlatformService service = service(PostgreSQLTemplates.builder().build());

        final AdHocNotFoundException exception = assertThrows(AdHocNotFoundException.class, () -> service.retrieveOne(99L));

        assertEquals("error.msg.adhocquery.adhoc.id.invalid", exception.getGlobalisationMessageCode());
        assertEquals("Adhoc Record with identifier 99 does not exist", exception.getDefaultUserMessage());
        verify(statement).setLong(1, 99L);
    }

    @Test
    void retrieveAllMapsAllFieldsOnPostgreSQL() throws SQLException {
        final AdHocReadPlatformService service = service(PostgreSQLTemplates.builder().build(), fullRow());

        final List<AdHocData> result = service.retrieveAllAdHocQuery();

        assertEquals(1, result.size());
        assertFullRow(result.get(0));
        assertEquals(EXPECTED_ALL_SQL, executedSql);
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void retrieveAllMapsAllFieldsOnMySQL() throws SQLException {
        final AdHocReadPlatformService service = service(MySQLTemplates.builder().build(), fullRow());

        final List<AdHocData> result = service.retrieveAllAdHocQuery();

        assertEquals(1, result.size());
        assertFullRow(result.get(0));
        assertEquals(EXPECTED_ALL_SQL, executedSql);
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void retrieveAllReturnsEmptyListWhenNoRows() throws SQLException {
        final AdHocReadPlatformService service = service(PostgreSQLTemplates.builder().build());

        final List<AdHocData> result = service.retrieveAllAdHocQuery();

        assertTrue(result.isEmpty());
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void retrieveAllKeepsInactiveRecordsAndNullableFields() throws SQLException {
        final Object[] inactiveRow = { 3L, "minimal", "select 1", "m_temp", "a", false, null, null, null, null, null, 1L, "mifos", 2L,
                null };
        final AdHocReadPlatformService service = service(PostgreSQLTemplates.builder().build(), inactiveRow, fullRow());

        final List<AdHocData> result = service.retrieveAllAdHocQuery();

        assertEquals(2, result.size());
        final AdHocData inactive = result.get(0);
        assertEquals(3L, inactive.getId());
        assertEquals("minimal", inactive.getName());
        assertFalse(inactive.isActive());
        assertNull(inactive.getEmail());
        assertNull(inactive.getReportRunFrequency());
        assertNull(inactive.getReportRunEvery());
        assertNull(inactive.getLastRun());
        assertNull(inactive.getCreatedOn());
        assertNull(inactive.getUpdatedOn());
        assertEquals("mifos", inactive.getCreatedBy());
        assertEquals(AdHocData.template().getReportRunFrequencies(), inactive.getReportRunFrequencies());
        assertFullRow(result.get(1));
        assertEquals(EXPECTED_ALL_SQL, executedSql);
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void retrieveAllActiveAdHocQueryFiltersActiveRowsAndOrdersById() throws SQLException {
        final List<Object[]> rows = List.of(adHocRow(1L, "first", true, null, null, null, 1L, 3, 1, "creator"),
                adHocRow(4L, "fourth", true, null, null, null, 2L, null, null, null));
        final AdHocReadPlatformService service = service(PostgreSQLTemplates.builder().build(), rows.toArray(Object[][]::new));

        final List<AdHocData> result = service.retrieveAllActiveAdHocQuery();

        assertEquals(List.of(1L, 4L), result.stream().map(AdHocData::getId).toList());
        assertTrue(result.stream().allMatch(AdHocData::isActive));
        final String sql = normalize(executedSql);
        assertTrue(sql.contains("left join m_appuser cb on cb.id = m_adhoc.createdby_id"));
        assertTrue(sql.contains("where m_adhoc.is_active = ?"));
        assertTrue(sql.contains("order by m_adhoc.id asc"));
        assertFalse(sql.contains("mb"));
        verify(statement).setBoolean(1, true);
    }

    @Test
    void adHocProjectionMapsAllFieldsAndTenantZonedTimestamps() throws SQLException {
        final Timestamp createdDate = Timestamp.valueOf(LocalDateTime.of(2026, 2, 3, 4, 5, 6));
        final Timestamp updatedDate = Timestamp.valueOf(LocalDateTime.of(2026, 2, 4, 5, 6, 7));
        final Timestamp lastRun = Timestamp.valueOf(LocalDateTime.of(2026, 2, 5, 6, 7, 8));
        final QMAppuser creatingUser = new QMAppuser("cb");
        final SQLQueryFactory queryFactory = queryFactory(PostgreSQLTemplates.builder().build(),
                adHocRow(12L, "daily", true, lastRun, createdDate, updatedDate, 21L, 2, 7, "reporter"));

        final AdHocData result = queryFactory.select(new AdHocProjection(creatingUser)).from(mAdhoc).leftJoin(creatingUser)
                .on(creatingUser.id.eq(mAdhoc.createdbyId)).fetchOne();

        assertEquals(12L, result.getId());
        assertEquals("daily", result.getName());
        assertEquals("select 1", result.getQuery());
        assertEquals("client", result.getTableName());
        assertEquals("id,name", result.getTableFields());
        assertEquals("daily@example.org", result.getEmail());
        assertTrue(result.isActive());
        assertEquals(21L, result.getCreatedById());
        assertEquals(22L, result.getUpdatedById());
        assertEquals("reporter", result.getCreatedBy());
        assertEquals(AdHocData.template().getReportRunFrequencies(), result.getReportRunFrequencies());
        assertEquals(2L, result.getReportRunFrequency());
        assertEquals(7L, result.getReportRunEvery());
        assertEquals(expectedTenantDateTime(createdDate), result.getCreatedOn());
        assertEquals(TENANT_ZONE, result.getCreatedOn().getZone());
        final ResultSet jdbcResultSet = mock(ResultSet.class);
        when(jdbcResultSet.getTimestamp("createdDate")).thenReturn(createdDate);
        assertEquals(JdbcSupport.getDateTime(jdbcResultSet, "createdDate"), result.getCreatedOn());
        assertEquals(expectedTenantDateTime(updatedDate), result.getUpdatedOn());
        assertEquals(expectedTenantDateTime(lastRun), result.getLastRun());
    }

    @Test
    void adHocProjectionHandlesNullableValues() throws SQLException {
        final QMAppuser creatingUser = new QMAppuser("cb");
        final SQLQueryFactory queryFactory = queryFactory(PostgreSQLTemplates.builder().build(),
                adHocRow(13L, "nullable", null, null, null, null, null, null, null, null));

        final AdHocData result = queryFactory.select(new AdHocProjection(creatingUser)).from(mAdhoc).leftJoin(creatingUser)
                .on(creatingUser.id.eq(mAdhoc.createdbyId)).fetchOne();

        assertFalse(result.isActive());
        assertNull(result.getCreatedBy());
        assertNull(result.getCreatedById());
        assertNull(result.getUpdatedById());
        assertNull(result.getCreatedOn());
        assertNull(result.getUpdatedOn());
        assertNull(result.getLastRun());
        assertNull(result.getReportRunFrequency());
        assertNull(result.getReportRunEvery());
        assertEquals(JdbcSupport.getDateTime(mock(ResultSet.class), "createdDate"), result.getCreatedOn());
    }

    private static Object[] fullRow() {
        return new Object[] { 7L, "Loans to report", "select * from m_loan", "m_loan_report", "id,principal", true, "ops@example.org", 2, 5,
                Timestamp.valueOf(LocalDateTime.of(2026, 11, 20, 6, 0)), Timestamp.valueOf(LocalDateTime.of(2026, 11, 1, 9, 30, 15)), 1L,
                "mifos", 2L, Timestamp.valueOf(LocalDateTime.of(2026, 11, 2, 10, 45)) };
    }

    private static Object[] adHocRow(Long id, String name, Boolean isActive, Timestamp lastRun, Timestamp createdDate,
            Timestamp updatedDate, Long createdById, Integer reportRunFrequency, Integer reportRunEvery, String createdBy) {
        return new Object[] { id, name, "select 1", "client", "id,name", isActive, "daily@example.org", reportRunFrequency, reportRunEvery,
                lastRun, createdDate, createdById, createdBy, createdById == null ? null : 22L, updatedDate };
    }

    private static void assertFullRow(AdHocData adHoc) {
        assertEquals(7L, adHoc.getId());
        assertEquals("Loans to report", adHoc.getName());
        assertEquals("select * from m_loan", adHoc.getQuery());
        assertEquals("m_loan_report", adHoc.getTableName());
        assertEquals("id,principal", adHoc.getTableFields());
        assertTrue(adHoc.isActive());
        assertEquals("ops@example.org", adHoc.getEmail());
        assertEquals(2L, adHoc.getReportRunFrequency());
        assertEquals(5L, adHoc.getReportRunEvery());
        assertEquals(ZonedDateTime.of(LocalDateTime.of(2026, 11, 20, 6, 0), TENANT_ZONE), adHoc.getLastRun());
        assertEquals(ZonedDateTime.of(LocalDateTime.of(2026, 11, 1, 9, 30, 15), TENANT_ZONE), adHoc.getCreatedOn());
        assertEquals(1L, adHoc.getCreatedById());
        assertEquals("mifos", adHoc.getCreatedBy());
        assertEquals(2L, adHoc.getUpdatedById());
        assertEquals(ZonedDateTime.of(LocalDateTime.of(2026, 11, 2, 10, 45), TENANT_ZONE), adHoc.getUpdatedOn());
        assertEquals(AdHocData.template().getReportRunFrequencies(), adHoc.getReportRunFrequencies());
    }

    private AdHocReadPlatformService service(SQLTemplates templates, Object[]... rows) throws SQLException {
        final SQLQueryFactory queryFactory = queryFactory(templates, rows);
        final AdHocReadPlatformServiceImpl.AdHocMapper adHocRowMapper = new AdHocReadPlatformServiceImpl.AdHocMapper(sqlGenerator);
        return new AdHocReadPlatformServiceImpl(jdbcTemplate, adHocRowMapper, queryFactory);
    }

    private SQLQueryFactory queryFactory(SQLTemplates templates, Object[]... rows) throws SQLException {
        final ResultSet resultSet = resultSet(rows);
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            executedSql = invocation.getArgument(0);
            return statement;
        });
        when(statement.executeQuery()).thenReturn(resultSet);
        return new SQLQueryFactory(new Configuration(templates), () -> connection);
    }

    /**
     * A {@link ResultSet} that serves the given rows by column index, including {@link ResultSet#wasNull()}.
     */
    private static ResultSet resultSet(Object[]... rows) {
        final AtomicInteger current = new AtomicInteger(-1);
        final AtomicInteger lastColumn = new AtomicInteger();

        final Answer<Object> answer = invocation -> {
            final String method = invocation.getMethod().getName();
            if ("next".equals(method)) {
                return current.incrementAndGet() < rows.length;
            }
            if ("wasNull".equals(method)) {
                return rows[current.get()][lastColumn.get() - 1] == null;
            }
            if (method.startsWith("get") && invocation.getArguments().length > 0 && invocation.getArgument(0) instanceof Integer column) {
                lastColumn.set(column);
                final Object value = rows[current.get()][column - 1];
                return value == null ? Mockito.RETURNS_DEFAULTS.answer(invocation) : value;
            }
            return Mockito.RETURNS_DEFAULTS.answer(invocation);
        };

        return mock(ResultSet.class, answer);
    }

    private static ZonedDateTime expectedTenantDateTime(Timestamp timestamp) {
        return ZonedDateTime.of(timestamp.toLocalDateTime(), TENANT_ZONE);
    }

    private static String normalize(String sql) {
        return sql.replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
    }
}
