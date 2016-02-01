/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.data;

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