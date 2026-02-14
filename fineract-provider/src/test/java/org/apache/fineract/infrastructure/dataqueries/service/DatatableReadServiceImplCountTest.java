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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.dataqueries.data.DataTableValidator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.portfolio.search.service.SearchUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class DatatableReadServiceImplCountTest {

    private static final Long APP_TABLE_ID = 42L;
    private static final String DATATABLE_NAME = "dt_loan_test";
    private static final String FK_COLUMN = "loan_id";

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private DatabaseSpecificSQLGenerator sqlGenerator;
    @Mock
    private PlatformSecurityContext context;
    @Mock
    private GenericDataService genericDataService;
    @Mock
    private DataTableValidator dataTableValidator;
    @Mock
    private SqlValidator sqlValidator;
    @Mock
    private SearchUtil searchUtil;
    @Mock
    private DatatableUtil datatableUtil;

    private DatatableReadServiceImpl underTest;

    @BeforeEach
    void setUp() {
        underTest = new DatatableReadServiceImpl(jdbcTemplate, sqlGenerator, context, genericDataService, dataTableValidator, sqlValidator,
                searchUtil, datatableUtil);
    }

    @Test
    void testCountDatatableEntriesUsesParameterizedQuery() {
        when(sqlGenerator.escape(anyString())).thenAnswer(invocation -> "\"" + invocation.getArgument(0) + "\"");
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(APP_TABLE_ID))).thenReturn(3L);

        Long count = underTest.countDatatableEntries(DATATABLE_NAME, APP_TABLE_ID, FK_COLUMN);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForObject(sqlCaptor.capture(), eq(Long.class), eq(APP_TABLE_ID));

        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("= ?"), "SQL should use ? placeholder for appTableId");
        assertFalse(sql.contains("= " + APP_TABLE_ID), "SQL should not contain concatenated appTableId");
        assertTrue(sql.contains("\"" + FK_COLUMN + "\""), "SQL should contain escaped FK column name");
        assertTrue(sql.contains("\"" + DATATABLE_NAME + "\""), "SQL should contain escaped datatable name");
        assertEquals(3L, count, "Should return the count from the query");
    }
}
