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

import java.util.List;

/**
 * Immutable data object for generic resultset data.
 */
public final class GenericResultsetData {

    private final List<ResultsetColumnHeaderData> columnHeaders;
    private final List<ResultsetRowData> data;

    public GenericResultsetData(final List<ResultsetColumnHeaderData> columnHeaders, final List<ResultsetRowData> resultsetDataRows) {
        this.columnHeaders = columnHeaders;
        this.data = resultsetDataRows;
    }

    public List<ResultsetColumnHeaderData> getColumnHeaders() {
        return this.columnHeaders;
    }

    public List<ResultsetRowData> getData() {
        return this.data;
    }

    public String getColTypeOfColumnNamed(final String columnName) {

        String colType = null;
        for (final ResultsetColumnHeaderData columnHeader : this.columnHeaders) {
            if (columnHeader.isNamed(columnName)) {
                colType = columnHeader.getColumnType();
            }
        }

        return colType;
    }

    public boolean hasNoEntries() {
        return this.data.isEmpty();
    }

    public boolean hasEntries() {
        return !hasNoEntries();
    }

    public boolean hasMoreThanOneEntry() {
        return this.data.size() > 1;
    }
}