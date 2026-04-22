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
package org.apache.fineract.infrastructure.dataqueries.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.core.service.database.DatabaseType;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.search.service.SearchUtil;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DatatableUtilTest {

    private static final Long APP_TABLE_ID = 42L;
    private static final String OFFICE_HIERARCHY = ".1.2.";

    @Mock
    private SearchUtil searchUtil;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private SqlValidator sqlValidator;
    @Mock
    private PlatformSecurityContext context;
    @Mock
    private GenericDataService genericDataService;
    @Mock
    private DatabaseSpecificSQLGenerator sqlGenerator;
    @Mock
    private ColumnValidator columnValidator;

    private DatatableUtil underTest;

    @BeforeEach
    void setUp() {
        underTest = new DatatableUtil(searchUtil, jdbcTemplate, sqlValidator, context, genericDataService, sqlGenerator, columnValidator);
        setupSecurityContext();
    }

    private void setupSecurityContext() {
        AppUser currentUser = Mockito.mock(AppUser.class);
        Office office = Mockito.mock(Office.class);
        when(context.authenticatedUser()).thenReturn(currentUser);
        when(currentUser.getOffice()).thenReturn(office);
        when(office.getHierarchy()).thenReturn(OFFICE_HIERARCHY);
    }

    @Test
    void testDataScopedSqlLoanUsesParameterizedQuery() {
        List<Object> params = new ArrayList<>();
        String sql = underTest.dataScopedSQL(EntityTables.LOAN, APP_TABLE_ID, params);

        assertEquals(4, params.size(), "LOAN case should have 4 parameters");
        assertTrue(sql.contains("where l.id = ?"), "SQL should use ? placeholder for loan id");
        assertFalse(sql.contains(APP_TABLE_ID.toString()), "SQL should not contain concatenated appTableId");
        assertFalse(sql.contains("'" + OFFICE_HIERARCHY), "SQL should not contain concatenated officeHierarchy");
        assertEquals(OFFICE_HIERARCHY + "%", params.get(0), "First param should be hierarchy pattern");
        assertEquals(APP_TABLE_ID, params.get(1), "Second param should be appTableId");
        assertEquals(OFFICE_HIERARCHY + "%", params.get(2), "Third param should be hierarchy pattern");
        assertEquals(APP_TABLE_ID, params.get(3), "Fourth param should be appTableId");
    }

    @Test
    void testDataScopedSqlSavingsUsesParameterizedQuery() {
        List<Object> params = new ArrayList<>();
        String sql = underTest.dataScopedSQL(EntityTables.SAVINGS, APP_TABLE_ID, params);

        assertEquals(4, params.size(), "SAVINGS case should have 4 parameters");
        assertTrue(sql.contains("where s.id = ?"), "SQL should use ? placeholder for savings id");
        assertFalse(sql.contains(APP_TABLE_ID.toString()), "SQL should not contain concatenated appTableId");
    }

    @Test
    void testDataScopedSqlSavingsTransactionUsesParameterizedQuery() {
        List<Object> params = new ArrayList<>();
        String sql = underTest.dataScopedSQL(EntityTables.SAVINGS_TRANSACTION, APP_TABLE_ID, params);

        assertEquals(4, params.size(), "SAVINGS_TRANSACTION case should have 4 parameters");
        assertTrue(sql.contains("where t.id = ?"), "SQL should use ? placeholder for transaction id");
        assertFalse(sql.contains(APP_TABLE_ID.toString()), "SQL should not contain concatenated appTableId");
    }

    @Test
    void testDataScopedSqlClientUsesParameterizedQuery() {
        List<Object> params = new ArrayList<>();
        String sql = underTest.dataScopedSQL(EntityTables.CLIENT, APP_TABLE_ID, params);

        assertEquals(2, params.size(), "CLIENT case should have 2 parameters");
        assertTrue(sql.contains("where c.id = ?"), "SQL should use ? placeholder for client id");
        assertFalse(sql.contains(APP_TABLE_ID.toString()), "SQL should not contain concatenated appTableId");
        assertEquals(OFFICE_HIERARCHY + "%", params.get(0), "First param should be hierarchy pattern");
        assertEquals(APP_TABLE_ID, params.get(1), "Second param should be appTableId");
    }

    @Test
    void testDataScopedSqlGroupUsesParameterizedQuery() {
        List<Object> params = new ArrayList<>();
        String sql = underTest.dataScopedSQL(EntityTables.GROUP, APP_TABLE_ID, params);

        assertEquals(2, params.size(), "GROUP case should have 2 parameters");
        assertTrue(sql.contains("where g.id = ?"), "SQL should use ? placeholder for group id");
        assertFalse(sql.contains(APP_TABLE_ID.toString()), "SQL should not contain concatenated appTableId");
    }

    @Test
    void testDataScopedSqlOfficeUsesParameterizedQuery() {
        List<Object> params = new ArrayList<>();
        String sql = underTest.dataScopedSQL(EntityTables.OFFICE, APP_TABLE_ID, params);

        assertEquals(2, params.size(), "OFFICE case should have 2 parameters");
        assertTrue(sql.contains("o.hierarchy like ?"), "SQL should use ? placeholder for hierarchy LIKE");
        assertTrue(sql.contains("o.id = ?"), "SQL should use ? placeholder for office id");
        assertFalse(sql.contains("'" + OFFICE_HIERARCHY), "SQL should not contain concatenated officeHierarchy");
        assertFalse(sql.contains(APP_TABLE_ID.toString()), "SQL should not contain concatenated appTableId");
    }

    @Test
    void testDataScopedSqlProductUsesParameterizedQuery() {
        List<Object> params = new ArrayList<>();
        String sql = underTest.dataScopedSQL(EntityTables.LOAN_PRODUCT, APP_TABLE_ID, params);

        assertEquals(1, params.size(), "PRODUCT case should have 1 parameter");
        assertTrue(sql.contains("p.id = ?"), "SQL should use ? placeholder for product id");
        assertFalse(sql.contains(APP_TABLE_ID.toString()), "SQL should not contain concatenated appTableId");
        assertEquals(APP_TABLE_ID, params.get(0), "First param should be appTableId");
    }

    @Test
    void testCheckMainResourceExistsWithinScopePassesParamsToQuery() {
        SqlRowSet rs = Mockito.mock(SqlRowSet.class);
        when(rs.next()).thenReturn(true).thenReturn(false);
        when(rs.getObject(anyString())).thenReturn(1L);
        when(jdbcTemplate.queryForRowSet(anyString(), any(Object[].class))).thenReturn(rs);

        underTest.checkMainResourceExistsWithinScope(EntityTables.CLIENT, APP_TABLE_ID);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> paramsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).queryForRowSet(sqlCaptor.capture(), paramsCaptor.capture());

        String sql = sqlCaptor.getValue();
        Object[] capturedParams = paramsCaptor.getValue();

        assertTrue(sql.contains("?"), "SQL should contain ? placeholders");
        assertFalse(sql.contains(APP_TABLE_ID.toString()), "SQL should not contain concatenated appTableId");
        assertTrue(capturedParams.length > 0, "Parameters array should not be empty");
    }

    @Test
    void testRetrieveDataTableGenericResultSetUsesParameterizedQuery() {
        EntityTables entityTable = EntityTables.LOAN;
        String dataTableName = "test_datatable";
        Long id = 5L;

        when(sqlGenerator.escape(anyString())).thenReturn("\"" + dataTableName + "\"");
        when(genericDataService.fillResultsetColumnHeaders(anyString())).thenReturn(createMultiRowHeaders());
        when(searchUtil.findFiltered(any(), any())).thenReturn(ResultsetColumnHeaderData.basic("id", "bigint", DatabaseType.MYSQL));
        when(genericDataService.fillResultsetRowData(anyString(), any(), any(Object[].class))).thenReturn(new ArrayList<>());

        underTest.retrieveDataTableGenericResultSet(entityTable, dataTableName, APP_TABLE_ID, null, id);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> paramsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(genericDataService).fillResultsetRowData(sqlCaptor.capture(), any(), paramsCaptor.capture());

        String sql = sqlCaptor.getValue();
        Object[] capturedParams = paramsCaptor.getValue();

        assertTrue(sql.contains("= ?"), "SQL should use ? placeholder for foreign key");
        assertTrue(sql.contains("id = ?"), "SQL should use ? placeholder for id");
        assertFalse(sql.contains("= " + APP_TABLE_ID), "SQL should not contain concatenated appTableId");
        assertFalse(sql.contains("= " + id), "SQL should not contain concatenated id");
        assertEquals(2, capturedParams.length, "Should have 2 parameters (appTableId + id)");
        assertEquals(APP_TABLE_ID, capturedParams[0], "First param should be appTableId");
        assertEquals(id, capturedParams[1], "Second param should be id");
    }

    @Test
    void testOfficeJoinConditionUsesPlaceholder() {
        String joinCondition = underTest.getOfficeJoinCondition("t");

        assertTrue(joinCondition.contains("o.hierarchy like ?"), "Join condition should use ? for hierarchy");
        assertFalse(joinCondition.contains("like '"), "Join condition should not contain literal quote for hierarchy");
    }

    private List<ResultsetColumnHeaderData> createMultiRowHeaders() {
        List<ResultsetColumnHeaderData> headers = new ArrayList<>();
        headers.add(ResultsetColumnHeaderData.basic("id", "bigint", DatabaseType.MYSQL));
        headers.add(ResultsetColumnHeaderData.basic("loan_id", "bigint", DatabaseType.MYSQL));
        return headers;
    }
}
