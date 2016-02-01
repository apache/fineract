/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.data;

import java.util.List;

public class ResultsetRowData {

    private final List<String> row;

    public static ResultsetRowData create(final List<String> rowValues) {
        return new ResultsetRowData(rowValues);
    }

    private ResultsetRowData(final List<String> rowValues) {
        this.row = rowValues;
    }

    public List<String> getRow() {
        return this.row;
    }
}
