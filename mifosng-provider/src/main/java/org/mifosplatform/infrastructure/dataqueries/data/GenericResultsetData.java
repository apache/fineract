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
public class GenericResultsetData {

    private final List<ResultsetColumnHeader> columnHeaders;
    private final List<ResultsetDataRow> data;

    public GenericResultsetData(final List<ResultsetColumnHeader> columnHeaders, final List<ResultsetDataRow> resultsetDataRows) {
        this.columnHeaders = columnHeaders;
        this.data = resultsetDataRows;
    }

    public List<ResultsetColumnHeader> getColumnHeaders() {
        return columnHeaders;
    }

    public List<ResultsetDataRow> getData() {
        return data;
    }
}