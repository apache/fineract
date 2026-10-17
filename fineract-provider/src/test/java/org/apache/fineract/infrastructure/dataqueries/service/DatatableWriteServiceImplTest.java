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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.codes.service.CodeReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.DatatableCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.core.service.database.DatabaseType;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.apache.fineract.infrastructure.dataqueries.data.DataTableValidator;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetRowData;
import org.apache.fineract.infrastructure.dataqueries.exception.DatatableNotFoundException;
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

    @Test
    void testCreateDatatableUsesUtf8mb4UnicodeCiForMySql() {
        final JsonElement payload = JsonParser.parseString("""
                {
                  "datatableName": "dt_charset_test",
                  "apptableName": "m_client",
                  "entitySubType": "PERSON",
                  "multiRow": false,
                  "columns": [
                    {
                      "name": "itsAString",
                      "type": "String",
                      "mandatory": true,
                      "length": 10
                    }
                  ]
                }
                """);

        final JsonCommand command = mock(JsonCommand.class);
        when(command.json()).thenReturn(payload.toString());
        when(command.commandId()).thenReturn(1L);

        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        when(databaseTypeResolver.databaseType()).thenReturn(DatabaseType.MYSQL);
        when(configurationDomainService.isConstraintApproachEnabledForDatatables()).thenReturn(false);
        when(sqlGenerator.currentSchema()).thenReturn("database()");
        when(sqlGenerator.escape(anyString())).thenAnswer(invocation -> "`" + invocation.getArgument(0) + "`");
        when(datatableUtil.resolveEntity("m_client")).thenReturn(EntityTables.CLIENT);
        when(datatableUtil.getFKField(EntityTables.CLIENT)).thenReturn("client_id");
        when(fromJsonHelper.parse(anyString())).thenReturn(payload);
        when(fromJsonHelper.extractJsonArrayNamed(eq("columns"), eq(payload)))
                .thenReturn(payload.getAsJsonObject().getAsJsonArray("columns"));
        when(fromJsonHelper.extractStringNamed(eq("datatableName"), eq(payload))).thenReturn("dt_charset_test");
        when(fromJsonHelper.extractStringNamed(eq("entitySubType"), eq(payload))).thenReturn("PERSON");
        when(fromJsonHelper.extractStringNamed(eq("apptableName"), eq(payload))).thenReturn("m_client");
        when(fromJsonHelper.extractBooleanNamed(eq("multiRow"), eq(payload))).thenReturn(false);
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq("dt_charset_test"))).thenReturn("true");

        underTest.createDatatable(command);

        final ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).execute(sqlCaptor.capture());
        assertTrue(sqlCaptor.getValue().contains("ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4 COLLATE=UTF8MB4_UNICODE_CI;"),
                "MySQL table creation must include utf8mb4 charset and utf8mb4_unicode_ci collation");
    }

    @Test
    void testDeleteOneToManyEntryIsScopedToApplicationTableId() {
        setupDeleteEntryMocks(true);
        when(jdbcTemplate.update(anyString(), eq(7L), eq(3L))).thenReturn(1);

        underTest.deleteDatatableEntry(DATATABLE_NAME, 3L, 7L, mock(JsonCommand.class));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).update(sqlCaptor.capture(), eq(7L), eq(3L));
        assertEquals("DELETE FROM \"" + DATATABLE_NAME + "\" WHERE \"id\" = ? AND \"client_id\" = ?", sqlCaptor.getValue());
    }

    @Test
    void testDeleteOneToManyEntryOfAnotherEntityIsNotFound() {
        setupDeleteEntryMocks(true);
        when(jdbcTemplate.update(anyString(), eq(7L), eq(3L))).thenReturn(0);

        assertThrows(DatatableNotFoundException.class,
                () -> underTest.deleteDatatableEntry(DATATABLE_NAME, 3L, 7L, mock(JsonCommand.class)));
        verify(jdbcTemplate).update(anyString(), eq(7L), eq(3L));
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
    }

    @Test
    void testDeleteOneToOneEntryWithIdIsRejected() {
        setupDeleteEntryMocks(false);

        PlatformDataIntegrityException exception = assertThrows(PlatformDataIntegrityException.class,
                () -> underTest.deleteDatatableEntry(DATATABLE_NAME, 3L, 4L, mock(JsonCommand.class)));
        assertEquals("error.msg.datatable.entry.id.not.allowed.for.one.to.one", exception.getGlobalisationMessageCode());
        verify(jdbcTemplate, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void testUpdateOneToOneEntryWithIdIsRejected() {
        setupUpdateEntryMocks(false, 3L, 4L);

        PlatformDataIntegrityException exception = assertThrows(PlatformDataIntegrityException.class,
                () -> underTest.updateDatatableEntryOneToMany(DATATABLE_NAME, 3L, 4L, mock(JsonCommand.class)));
        assertEquals("error.msg.datatable.entry.id.not.allowed.for.one.to.one", exception.getGlobalisationMessageCode());
        verify(jdbcTemplate, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void testUpdateKeysOnTheReadRowsPrimaryKey() {
        // a registered table whose primary key is client_id and which also has a plain id column reads as one to many
        setupUpdateEntryMocks(true, 3L, 4L);
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        CommandProcessingResult result = underTest.updateDatatableEntryOneToMany(DATATABLE_NAME, 3L, 4L, mock(JsonCommand.class));

        ArgumentCaptor<Object[]> paramsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).update(anyString(), paramsCaptor.capture());
        Object[] params = paramsCaptor.getValue();
        assertEquals(3L, params[params.length - 1], "the UPDATE must key on the primary key of the row read for appTableId");
        assertEquals(3L, result.getResourceId());
    }

    @Test
    void testUpdateOneToManyEntryKeysOnItsId() {
        ResultsetColumnHeaderData id = ResultsetColumnHeaderData.detailed("id", "bigint", null, false, true, List.of(), null, false, false,
                DatabaseType.POSTGRESQL);
        ResultsetColumnHeaderData fk = ResultsetColumnHeaderData.basic("client_id", "bigint", DatabaseType.POSTGRESQL);
        ResultsetColumnHeaderData note = ResultsetColumnHeaderData.basic("note", "varchar", DatabaseType.POSTGRESQL);
        setupUpdateEntryMocks(true, List.of(id, fk, note), id, note, List.of(4L, 3L, "old"));
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        CommandProcessingResult result = underTest.updateDatatableEntryOneToMany(DATATABLE_NAME, 3L, 4L, mock(JsonCommand.class));

        ArgumentCaptor<Object[]> paramsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).update(anyString(), paramsCaptor.capture());
        Object[] params = paramsCaptor.getValue();
        assertEquals(4L, params[params.length - 1], "the UPDATE must key on the entry's id");
        assertEquals(4L, result.getResourceId());
    }

    private void setupDeleteEntryMocks(boolean multiRow) {
        when(sqlGenerator.escape(anyString())).thenAnswer(invocation -> "\"" + invocation.getArgument(0) + "\"");
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), anyString())).thenReturn(0L);
        when(datatableUtil.queryForApplicationEntity(DATATABLE_NAME)).thenReturn(EntityTables.CLIENT);
        when(datatableUtil.getFKField(EntityTables.CLIENT)).thenReturn("client_id");
        when(datatableUtil.checkMainResourceExistsWithinScope(eq(EntityTables.CLIENT), any()))
                .thenReturn(mock(CommandProcessingResult.class));
        when(genericDataService.fillResultsetColumnHeaders(DATATABLE_NAME)).thenReturn(List.of());
        when(datatableUtil.isMultirowDatatable(any())).thenReturn(multiRow);
    }

    private void setupUpdateEntryMocks(boolean multiRow, Long primaryKey, Long idColumnValue) {
        ResultsetColumnHeaderData pk = ResultsetColumnHeaderData.detailed("client_id", "bigint", null, false, true, List.of(), null, false,
                false, DatabaseType.POSTGRESQL);
        ResultsetColumnHeaderData id = ResultsetColumnHeaderData.basic("id", "bigint", DatabaseType.POSTGRESQL);
        ResultsetColumnHeaderData note = ResultsetColumnHeaderData.basic("note", "varchar", DatabaseType.POSTGRESQL);
        setupUpdateEntryMocks(multiRow, List.of(pk, id, note), pk, note, List.of(primaryKey, idColumnValue, "old"));
    }

    private void setupUpdateEntryMocks(boolean multiRow, List<ResultsetColumnHeaderData> headers, ResultsetColumnHeaderData pk,
            ResultsetColumnHeaderData note, List<Object> row) {
        GenericResultsetData read = new GenericResultsetData(headers, List.of(ResultsetRowData.create(new ArrayList<>(row))));

        when(datatableUtil.queryForApplicationEntity(DATATABLE_NAME)).thenReturn(EntityTables.CLIENT);
        when(datatableUtil.checkMainResourceExistsWithinScope(eq(EntityTables.CLIENT), any()))
                .thenReturn(mock(CommandProcessingResult.class));
        when(datatableUtil.retrieveDataTableGenericResultSet(eq(EntityTables.CLIENT), eq(DATATABLE_NAME), any(), any(), any()))
                .thenReturn(read);
        when(datatableUtil.isMultirowDatatable(headers)).thenReturn(multiRow);
        Map<String, ResultsetColumnHeaderData> headersByName = new HashMap<>();
        headers.forEach(header -> headersByName.put(header.getColumnName(), header));
        when(searchUtil.mapHeadersToName(headers)).thenReturn(headersByName);
        when(searchUtil.getFiltered(eq(headers), any())).thenReturn(pk);
        when(searchUtil.validateToJdbcColumn(eq("note"), any(), eq(false))).thenReturn(note);
        when(searchUtil.parseColumnValue(eq(note), eq("new"), any(), any(), any(), eq(false), any())).thenReturn("new");
        when(fromJsonHelper.extractDataMap(any(), any())).thenReturn(new HashMap<>(Map.of("note", "new")));
        when(sqlGenerator.getDialect()).thenReturn(DatabaseType.POSTGRESQL);
        when(sqlGenerator.buildUpdate(eq(DATATABLE_NAME), any(), any())).thenReturn("UPDATE \"" + DATATABLE_NAME + "\" SET x = ?");
    }
}
