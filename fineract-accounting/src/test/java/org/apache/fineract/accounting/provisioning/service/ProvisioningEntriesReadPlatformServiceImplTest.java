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
package org.apache.fineract.accounting.provisioning.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.accounting.provisioning.data.LoanProductProvisioningEntryData;
import org.apache.fineract.accounting.provisioning.data.ProvisioningEntryData;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * Runs the QueryDSL queries of {@link ProvisioningEntriesReadPlatformServiceImpl} through a real
 * {@link SQLQueryFactory} on top of a mocked JDBC connection, so that the generated SQL, the paging and the result
 * mapping are all verified.
 */
class ProvisioningEntriesReadPlatformServiceImplTest {

    private static final String SELECT_SQL = """
            select m_provisioning_history.id, m_provisioning_history.journal_entry_created, m_provisioning_history.createdby_id, \
            m_provisioning_history.created_date, created.username, m_provisioning_history.lastmodifiedby_id, modified.username
            from m_provisioning_history m_provisioning_history
            left join m_appuser created
            on created.id = m_provisioning_history.createdby_id
            left join m_appuser modified
            on modified.id = m_provisioning_history.lastmodifiedby_id
            order by m_provisioning_history.created_date asc""";

    private static final String COUNT_SQL = "select count(*)\nfrom m_provisioning_history m_provisioning_history";

    private static final String EXPECTED_SQL = """
            select entry.history_id, entry.office_id, office.name, entry.currency_code, entry.product_id, \
            product.name, entry.category_id, category.category_name, entry.overdue_in_days, entry.reseve_amount, \
            liability.id, liability.gl_code, liability.name, expense.id, expense.gl_code, expense.name, entry.criteria_id
            from m_loanproduct_provisioning_entry entry
            left join m_office office
            on office.id = entry.office_id
            left join m_product_loan product
            on product.id = entry.product_id
            left join m_provision_category category
            on category.id = entry.category_id
            left join acc_gl_account liability
            on liability.id = entry.liability_account
            left join acc_gl_account expense
            on expense.id = entry.expense_account
            where entry.criteria_id = ?""";

    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final List<String> executedSql = new ArrayList<>();
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

    @Test
    void retrieveAllProvisioningEntriesMapsRowsAndTotalOnPostgreSQL() throws SQLException {
        ProvisioningEntriesReadPlatformService service = service(PostgreSQLTemplates.builder().build(), 5L,
                new Object[] { 1L, true, 10L, Date.valueOf("2026-11-10"), "mifos", 11L, "admin" },
                new Object[] { 2L, false, 10L, Date.valueOf("2026-11-12"), "mifos", 12L, "other" });

        Page<ProvisioningEntryData> page = service.retrieveAllProvisioningEntries(1, 2);

        assertEquals(5, page.getTotalFilteredRecords());
        assertEquals(List.of(
                new ProvisioningEntryData().setId(1L).setJournalEntry(true).setCreatedById(10L).setCreatedUser("mifos")
                        .setCreatedDate(LocalDate.of(2026, 11, 10)).setModifiedById(11L).setModifiedUser("admin"),
                new ProvisioningEntryData().setId(2L).setJournalEntry(false).setCreatedById(10L).setCreatedUser("mifos")
                        .setCreatedDate(LocalDate.of(2026, 11, 12)).setModifiedById(12L).setModifiedUser("other")),
                page.getPageItems());

        assertEquals(2, executedSql.size());
        assertEquals(SELECT_SQL + "\nlimit ?\noffset ?", executedSql.get(0));
        assertEquals(COUNT_SQL, executedSql.get(1));
        Mockito.verify(statement).setLong(1, 2L);
        Mockito.verify(statement).setLong(2, 1L);
    }

    @Test
    void retrieveAllProvisioningEntriesGeneratesPagedSqlOnMySQL() throws SQLException {
        ProvisioningEntriesReadPlatformService service = service(MySQLTemplates.builder().build(), 1L,
                new Object[] { 1L, true, 10L, Date.valueOf("2026-11-10"), "mifos", 11L, "admin" });

        Page<ProvisioningEntryData> page = service.retrieveAllProvisioningEntries(0, 1);

        assertEquals(1, page.getPageItems().size());
        assertEquals(1, page.getTotalFilteredRecords());
        assertEquals(SELECT_SQL + "\nlimit ?", executedSql.get(0));
        assertEquals(COUNT_SQL, executedSql.get(1));
        Mockito.verify(statement).setLong(1, 1L);
    }

    @Test
    void retrieveAllProvisioningEntriesWithoutPagingAddsNoLimitOrOffset() throws SQLException {
        ProvisioningEntriesReadPlatformService service = service(PostgreSQLTemplates.builder().build(), 1L,
                new Object[] { 1L, true, 10L, Date.valueOf("2026-11-10"), "mifos", 11L, "admin" });

        Page<ProvisioningEntryData> page = service.retrieveAllProvisioningEntries(null, null);

        assertEquals(1, page.getPageItems().size());
        assertEquals(SELECT_SQL, executedSql.get(0));
    }

    @Test
    void retrieveAllProvisioningEntriesKeepsLegacyDefaultsForMissingValues() throws SQLException {
        ProvisioningEntriesReadPlatformService service = service(PostgreSQLTemplates.builder().build(), 1L,
                new Object[] { 3L, null, null, null, null, null, null });

        Page<ProvisioningEntryData> page = service.retrieveAllProvisioningEntries(null, null);

        assertEquals(1, page.getPageItems().size());
        ProvisioningEntryData entry = page.getPageItems().get(0);
        assertEquals(3L, entry.getId());
        assertEquals(Boolean.FALSE, entry.getJournalEntry());
        assertEquals(0L, entry.getCreatedById());
        assertEquals(0L, entry.getModifiedById());
        assertEquals(null, entry.getCreatedDate());
        assertEquals(null, entry.getCreatedUser());
        assertEquals(null, entry.getModifiedUser());
    }

    @Test
    void retrieveAllProvisioningEntriesReturnsEmptyPageWhenThereAreNoEntries() throws SQLException {
        ProvisioningEntriesReadPlatformService service = service(PostgreSQLTemplates.builder().build(), 0L);

        Page<ProvisioningEntryData> page = service.retrieveAllProvisioningEntries(null, null);

        assertTrue(page.getPageItems().isEmpty());
        assertEquals(0, page.getTotalFilteredRecords());
    }

    @Test
    void retrieveAllProvisioningEntriesWithZeroLimitReturnsEmptyPageWithTotal() throws SQLException {
        ProvisioningEntriesReadPlatformService service = service(PostgreSQLTemplates.builder().build(), 5L,
                new Object[] { 1L, true, 10L, Date.valueOf("2026-11-10"), "mifos", 11L, "admin" });

        Page<ProvisioningEntryData> page = service.retrieveAllProvisioningEntries(0, 0);

        assertTrue(page.getPageItems().isEmpty());
        assertEquals(5, page.getTotalFilteredRecords());
        assertEquals(List.of(COUNT_SQL), executedSql);
    }

    @Test
    void retrieveAllProvisioningEntriesIgnoresNegativeOffsetAndLimit() throws SQLException {
        ProvisioningEntriesReadPlatformService service = service(PostgreSQLTemplates.builder().build(), 1L,
                new Object[] { 1L, true, 10L, Date.valueOf("2026-11-10"), "mifos", 11L, "admin" });

        Page<ProvisioningEntryData> page = service.retrieveAllProvisioningEntries(-1, -5);

        assertEquals(1, page.getPageItems().size());
        assertEquals(SELECT_SQL, executedSql.get(0));
    }

    @Test
    void retrieveProvisioningEntryDataByCriteriaIdMapsAllFieldsOnPostgreSQL() throws SQLException {
        Object[] row = new Object[] { 100L, 1L, "Head Office", "USD", 2L, "Personal Loan", 3L, "Standard", 15L,
                new BigDecimal("500.000000"), 20L, "2000", "Provision Liability", 30L, "5000", "Provision Expense", 10L };
        ProvisioningEntriesReadPlatformService service = service(PostgreSQLTemplates.builder().build(), row);

        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(100L))).thenReturn(new ProvisioningEntryData().setId(100L));

        ProvisioningEntryData result = service.retrieveProvisioningEntryDataByCriteriaId(10L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertNotNull(result.getProvisioningEntries());
        assertEquals(1, result.getProvisioningEntries().size());

        LoanProductProvisioningEntryData entryData = result.getProvisioningEntries().iterator().next();
        assertEquals(100L, entryData.getHistoryId());
        assertEquals(1L, entryData.getOfficeId());
        assertEquals("Head Office", entryData.getOfficeName());
        assertEquals("USD", entryData.getCurrencyCode());
        assertEquals(2L, entryData.getProductId());
        assertEquals("Personal Loan", entryData.getProductName());
        assertEquals(3L, entryData.getCategoryId());
        assertEquals("Standard", entryData.getCategoryName());
        assertEquals(15L, entryData.getOverdueInDays());
        assertEquals(new BigDecimal("500.000000"), entryData.getAmountreserved());
        assertEquals(20L, entryData.getLiablityAccount());
        assertEquals("2000", entryData.getLiabilityAccountCode());
        assertEquals("Provision Liability", entryData.getLiabilityAccountName());
        assertEquals(30L, entryData.getExpenseAccount());
        assertEquals("5000", entryData.getExpenseAccountCode());
        assertEquals("Provision Expense", entryData.getExpenseAccountName());
        assertEquals(10L, entryData.getCriteriaId());

        assertEquals(EXPECTED_SQL, executedSql.get(0));
        verify(statement).setLong(1, 10L);
    }

    @Test
    void retrieveProvisioningEntryDataByCriteriaIdMapsAllFieldsOnMySQL() throws SQLException {
        Object[] row = new Object[] { 101L, 2L, "Regional Branch", "EUR", 5L, "Auto Loan", 4L, "Substandard", 30L,
                new BigDecimal("750.500000"), 25L, "2100", "Auto Liability", 35L, "5100", "Auto Expense", 20L };
        ProvisioningEntriesReadPlatformService service = service(MySQLTemplates.builder().build(), row);

        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(101L))).thenReturn(new ProvisioningEntryData().setId(101L));

        ProvisioningEntryData result = service.retrieveProvisioningEntryDataByCriteriaId(20L);

        assertNotNull(result);
        assertEquals(101L, result.getId());
        assertEquals(EXPECTED_SQL, executedSql.get(0));
        verify(statement).setLong(1, 20L);
    }

    @Test
    void retrieveProvisioningEntryDataByCriteriaIdReturnsNullWhenNoEntriesFound() throws SQLException {
        ProvisioningEntriesReadPlatformService service = service(PostgreSQLTemplates.builder().build());

        ProvisioningEntryData result = service.retrieveProvisioningEntryDataByCriteriaId(999L);

        assertNull(result);
        assertEquals(EXPECTED_SQL, executedSql.get(0));
        verify(statement).setLong(1, 999L);
    }

    @Test
    void retrieveProvisioningEntryDataByCriteriaIdKeepsNullableFieldsNull() throws SQLException {
        Object[] row = new Object[] { 200L, null, null, "USD", null, null, null, null, null, null, null, null, null, null, null, null,
                50L };
        ProvisioningEntriesReadPlatformService service = service(PostgreSQLTemplates.builder().build(), row);

        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(200L))).thenReturn(new ProvisioningEntryData().setId(200L));

        ProvisioningEntryData result = service.retrieveProvisioningEntryDataByCriteriaId(50L);

        assertNotNull(result);
        LoanProductProvisioningEntryData entryData = result.getProvisioningEntries().iterator().next();
        assertEquals(200L, entryData.getHistoryId());
        assertNull(entryData.getOfficeId());
        assertNull(entryData.getOfficeName());
        assertEquals("USD", entryData.getCurrencyCode());
        assertNull(entryData.getProductId());
        assertNull(entryData.getProductName());
        assertNull(entryData.getCategoryId());
        assertNull(entryData.getCategoryName());
        assertNull(entryData.getOverdueInDays());
        assertNull(entryData.getAmountreserved());
        assertNull(entryData.getLiablityAccount());
        assertNull(entryData.getLiabilityAccountCode());
        assertNull(entryData.getLiabilityAccountName());
        assertNull(entryData.getExpenseAccount());
        assertNull(entryData.getExpenseAccountCode());
        assertNull(entryData.getExpenseAccountName());
        assertEquals(50L, entryData.getCriteriaId());
    }

    private ProvisioningEntriesReadPlatformService service(SQLTemplates templates, Long total, Object[]... rows) throws SQLException {
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            executedSql.add(invocation.getArgument(0));
            return statement;
        });
        when(statement.executeQuery()).thenAnswer(invocation -> {
            boolean isCount = executedSql.get(executedSql.size() - 1).startsWith("select count(");
            return isCount ? resultSet(new Object[] { total }) : resultSet(rows);
        });

        return new ProvisioningEntriesReadPlatformServiceImpl(jdbcTemplate, mock(PaginationHelper.class),
                mock(DatabaseSpecificSQLGenerator.class), new SQLQueryFactory(new Configuration(templates), () -> connection));
    }

    private ProvisioningEntriesReadPlatformService service(SQLTemplates templates, Object[]... rows) throws SQLException {
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            executedSql.add(invocation.getArgument(0));
            return statement;
        });
        when(statement.executeQuery()).thenReturn(resultSet(rows));

        return new ProvisioningEntriesReadPlatformServiceImpl(jdbcTemplate, mock(PaginationHelper.class),
                mock(DatabaseSpecificSQLGenerator.class), new SQLQueryFactory(new Configuration(templates), () -> connection));
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
