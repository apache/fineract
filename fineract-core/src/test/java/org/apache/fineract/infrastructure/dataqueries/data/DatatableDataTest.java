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
package org.apache.fineract.infrastructure.dataqueries.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.service.database.DatabaseType;
import org.junit.jupiter.api.Test;

class DatatableDataTest {

    @Test
    void testSingleRowDatatable() {
        // Given: A single-row datatable (no "id" column)
        String appTableName = "m_client";
        String registeredTableName = "extra_client_details";
        String entitySubType = null;
        List<ResultsetColumnHeaderData> columnHeaders = new ArrayList<>();
        // Single-row datatable has client_id as primary key, no "id" column
        columnHeaders.add(ResultsetColumnHeaderData.basic("client_id", "bigint", DatabaseType.POSTGRESQL));
        columnHeaders.add(ResultsetColumnHeaderData.basic("field1", "text", DatabaseType.POSTGRESQL));
        boolean multiRow = false;

        // When: Creating DatatableData
        DatatableData datatableData = DatatableData.create(appTableName, registeredTableName, entitySubType, columnHeaders, multiRow);

        // Then: It should be identified as single-row
        assertFalse(datatableData.isMultiRow(), "Single-row datatable should return false for isMultiRow()");
        assertEquals(registeredTableName, datatableData.getRegisteredTableName());
        assertEquals(columnHeaders, datatableData.getColumnHeaderData());
    }

    @Test
    void testMultiRowDatatable() {
        // Given: A multi-row datatable (has "id" column)
        String appTableName = "m_client";
        String registeredTableName = "extra_family_details";
        String entitySubType = null;
        List<ResultsetColumnHeaderData> columnHeaders = new ArrayList<>();
        // Multi-row datatable has "id" as primary key
        columnHeaders.add(ResultsetColumnHeaderData.basic("id", "bigint", DatabaseType.POSTGRESQL));
        columnHeaders.add(ResultsetColumnHeaderData.basic("client_id", "bigint", DatabaseType.POSTGRESQL));
        columnHeaders.add(ResultsetColumnHeaderData.basic("field1", "text", DatabaseType.POSTGRESQL));
        boolean multiRow = true;

        // When: Creating DatatableData
        DatatableData datatableData = DatatableData.create(appTableName, registeredTableName, entitySubType, columnHeaders, multiRow);

        // Then: It should be identified as multi-row
        assertTrue(datatableData.isMultiRow(), "Multi-row datatable should return true for isMultiRow()");
        assertEquals(registeredTableName, datatableData.getRegisteredTableName());
        assertEquals(columnHeaders, datatableData.getColumnHeaderData());
    }

    @Test
    void testHasColumn() {
        // Given: A datatable with columns
        List<ResultsetColumnHeaderData> columnHeaders = new ArrayList<>();
        columnHeaders.add(ResultsetColumnHeaderData.basic("client_id", "bigint", DatabaseType.POSTGRESQL));
        columnHeaders.add(ResultsetColumnHeaderData.basic("field1", "text", DatabaseType.POSTGRESQL));
        DatatableData datatableData = DatatableData.create("m_client", "test_table", null, columnHeaders, false);

        // When/Then: Checking for column existence
        assertTrue(datatableData.hasColumn("client_id"), "Should find existing column");
        assertTrue(datatableData.hasColumn("field1"), "Should find existing column");
        assertFalse(datatableData.hasColumn("nonexistent"), "Should not find non-existent column");
    }
}
