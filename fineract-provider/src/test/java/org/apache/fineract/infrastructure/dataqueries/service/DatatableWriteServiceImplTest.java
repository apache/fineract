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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.codes.service.CodeReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.serialization.DatatableCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.apache.fineract.infrastructure.dataqueries.data.DataTableValidator;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.search.service.SearchUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DatatableWriteServiceImplTest {

    private static final String DATATABLE_NAME = "dt_test_loan";

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private DatabaseTypeResolver databaseTypeResolver;
    @Mock
    private DatabaseSpecificSQLGenerator sqlGenerator;
    @Mock
    private PlatformSecurityContext context;
    @Mock
    private FromJsonHelper fromJsonHelper;
    @Mock
    private GenericDataService genericDataService;
    @Mock
    private DatatableCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private CodeReadPlatformService codeReadPlatformService;
    @Mock
    private DataTableValidator dataTableValidator;
    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Mock
    private DatatableKeywordGenerator datatableKeywordGenerator;
    @Mock
    private SearchUtil searchUtil;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private DatatableReadService datatableReadService;
    @Mock
    private DatatableUtil datatableUtil;

    @InjectMocks
    private DatatableWriteServiceImpl underTest;

    @Test
    void testDeregisterDatatableUsesParameterizedQueries() {
        underTest.deregisterDatatable(DATATABLE_NAME);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> paramsCaptor = ArgumentCaptor.forClass(Object[].class);

        verify(jdbcTemplate, atLeastOnce()).update(sqlCaptor.capture(), paramsCaptor.capture());

        List<String> allSql = sqlCaptor.getAllValues();
        List<Object[]> allParams = paramsCaptor.getAllValues();

        // First call: DELETE FROM m_role_permission WHERE ... IN (?, ?, ?, ?, ?, ?, ?)
        assertTrue(allSql.get(0).contains("m_role_permission"), "First DELETE should target m_role_permission");
        assertTrue(allSql.get(0).contains("?"), "SQL should use ? placeholders");
        assertFalse(allSql.get(0).contains("'" + DATATABLE_NAME), "SQL should not contain concatenated datatable name");
        assertEquals(7, allParams.get(0).length, "Should have 7 permission code parameters");

        // Second call: DELETE FROM m_permission WHERE code IN (?, ?, ?, ?, ?, ?, ?)
        assertTrue(allSql.get(1).contains("m_permission"), "Second DELETE should target m_permission");
        assertTrue(allSql.get(1).contains("?"), "SQL should use ? placeholders");
        assertEquals(7, allParams.get(1).length, "Should have 7 permission code parameters");

        // Third call: DELETE FROM x_registered_table WHERE registered_table_name = ?
        assertTrue(allSql.get(2).contains("x_registered_table"), "Third DELETE should target x_registered_table");
        assertTrue(allSql.get(2).contains("= ?"), "SQL should use ? placeholder");

        // Fourth call: DELETE FROM c_configuration WHERE name = ?
        assertTrue(allSql.get(3).contains("c_configuration"), "Fourth DELETE should target c_configuration");
        assertTrue(allSql.get(3).contains("= ?"), "SQL should use ? placeholder");
    }

    @Test
    void testDeleteColumnCodeMappingUsesParameterizedQuery() {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("test_column_1");
        columnNames.add("test_column_2");

        ReflectionTestUtils.invokeMethod(underTest, "deleteColumnCodeMapping", columnNames);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, atLeastOnce()).update(sqlCaptor.capture(), anyString());

        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("DELETE FROM x_table_column_code_mappings"), "Should delete from x_table_column_code_mappings");
        assertTrue(sql.contains("column_alias_name = ?"), "Should use ? placeholder for column_alias_name");
        assertFalse(sql.contains("test_column"), "SQL should not contain concatenated column name");
    }

    @Test
    void testGetCodeIdForColumnUsesParameterizedQuery() {
        String dataTableAlias = "dt_test";
        String columnName = "status";

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString())).thenReturn(42);

        ReflectionTestUtils.invokeMethod(underTest, "getCodeIdForColumn", dataTableAlias, columnName);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> paramCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForObject(sqlCaptor.capture(), eq(Integer.class), paramCaptor.capture());

        String sql = sqlCaptor.getValue();
        String param = paramCaptor.getValue();

        assertTrue(sql.contains("column_alias_name = ?"), "SQL should use ? placeholder");
        assertFalse(sql.contains(dataTableAlias + "_" + columnName), "SQL should not contain concatenated alias");
        assertEquals(dataTableAlias + "_" + columnName, param, "Parameter should be the combined alias_name");
    }

    @Test
    void testIsDatatableAttachedUsesParameterizedQuery() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), anyString())).thenReturn(0L);

        ReflectionTestUtils.invokeMethod(underTest, "isDatatableAttachedToEntityDatatableCheck", DATATABLE_NAME);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> paramCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForObject(sqlCaptor.capture(), eq(Long.class), paramCaptor.capture());

        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("x_registered_table_name = ?"), "SQL should use ? placeholder for datatable name");
        assertFalse(sql.contains("'" + DATATABLE_NAME + "'"), "SQL should not contain concatenated datatable name");
        assertEquals(DATATABLE_NAME, paramCaptor.getValue(), "Parameter should be the datatable name");
    }

    @Test
    void testParseDatatableColumnForDropUsesParameterizedFKLookup() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        when(sqlGenerator.escape(anyString())).thenAnswer(invocation -> "\"" + invocation.getArgument(0) + "\"");
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString(), anyString())).thenReturn(0);

        JsonObject column = new JsonObject();
        column.addProperty("name", "test_col");

        StringBuilder sqlBuilder = new StringBuilder();
        StringBuilder constrainBuilder = new StringBuilder();
        List<String> codeMappings = new ArrayList<>();

        ReflectionTestUtils.invokeMethod(underTest, "parseDatatableColumnForDrop", column, sqlBuilder, DATATABLE_NAME, constrainBuilder,
                codeMappings);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> param1Captor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> param2Captor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForObject(sqlCaptor.capture(), eq(Integer.class), param1Captor.capture(), param2Captor.capture());

        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("i.TABLE_NAME = ?"), "SQL should use ? placeholder for TABLE_NAME");
        assertTrue(sql.contains("i.CONSTRAINT_NAME = ?"), "SQL should use ? placeholder for CONSTRAINT_NAME");
        assertFalse(sql.contains("'" + DATATABLE_NAME + "'"), "SQL should not contain concatenated table name");
        assertEquals(DATATABLE_NAME, param1Captor.getValue(), "First param should be the table name");
        assertTrue(param2Captor.getValue().startsWith("fk_"), "Second param should be the FK constraint name");
    }

    @Test
    void testRegisterPermissionsUsesParameterizedQuery() {
        when(sqlGenerator.escape(anyString())).thenAnswer(invocation -> "\"" + invocation.getArgument(0) + "\"");

        ReflectionTestUtils.invokeMethod(underTest, "registerPermissions", DATATABLE_NAME, true);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> paramsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate, atLeastOnce()).update(sqlCaptor.capture(), paramsCaptor.capture());

        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("INSERT INTO m_permission"), "SQL should be an INSERT into m_permission");
        assertTrue(sql.contains("VALUES ('datatable', ?, ?, ?, ?)"), "SQL should use ? placeholders for values");
        assertFalse(sql.contains("'" + DATATABLE_NAME + "'"), "SQL should not contain concatenated datatable name");

        List<Object[]> allParams = paramsCaptor.getAllValues();
        assertEquals(7, allParams.size(), "Should create 7 permission entries");
        assertEquals("CREATE_" + DATATABLE_NAME, allParams.get(0)[0], "First permission code should be CREATE_<datatable>");
    }

    @Test
    void testRegisterColumnCodeMappingUsesParameterizedQuery() {
        Map<String, Long> codeMappings = new HashMap<>();
        codeMappings.put("dt_test_status", 10L);

        ReflectionTestUtils.invokeMethod(underTest, "registerColumnCodeMapping", codeMappings);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).update(sqlCaptor.capture(), eq("dt_test_status"), eq(10L));

        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("INSERT INTO x_table_column_code_mappings"), "SQL should insert into code mappings table");
        assertTrue(sql.contains("VALUES (?, ?)"), "SQL should use ? placeholders");
    }
}
