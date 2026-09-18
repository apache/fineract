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

import java.io.Serializable;
import java.util.List;

/**
 * Immutable data object representing datatable data.
 */
public final class DatatableData implements Serializable {

    @SuppressWarnings("unused")
    private final String applicationTableName;
    @SuppressWarnings("unused")
    private final String registeredTableName;
    @SuppressWarnings("unused")
    private final String entitySubType;
    @SuppressWarnings("unused")
    private final List<ResultsetColumnHeaderData> columnHeaderData;
    @SuppressWarnings("unused")
    private final boolean multiRow;

    public static DatatableData create(final String applicationTableName, final String registeredTableName, final String entitySubType,
            final List<ResultsetColumnHeaderData> columnHeaderData, final boolean multiRow) {
        return new DatatableData(applicationTableName, registeredTableName, entitySubType, columnHeaderData, multiRow);
    }

    private DatatableData(final String applicationTableName, final String registeredTableName, final String entitySubType,
            final List<ResultsetColumnHeaderData> columnHeaderData, final boolean multiRow) {
        this.applicationTableName = applicationTableName;
        this.registeredTableName = registeredTableName;
        this.entitySubType = entitySubType;
        this.columnHeaderData = columnHeaderData;
        this.multiRow = multiRow;

    }

    public boolean hasColumn(final String columnName) {

        for (ResultsetColumnHeaderData c : this.columnHeaderData) {

            if (c.getColumnName().equals(columnName)) {
                return true;
            }
        }

        return false;
    }

    public String getRegisteredTableName() {
        return registeredTableName;
    }

    public List<ResultsetColumnHeaderData> getColumnHeaderData() {
        return columnHeaderData;
    }

    public boolean isMultiRow() {
        return multiRow;
    }

}
