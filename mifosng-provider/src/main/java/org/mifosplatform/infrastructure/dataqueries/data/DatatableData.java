/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.data;

/**
 * Immutable data object representing datatable data.
 */
public class DatatableData {

    @SuppressWarnings("unused")
    private final String applicationTableName;
    @SuppressWarnings("unused")
    private final String registeredTableName;

    public static DatatableData create(final String applicationTableName, final String registeredTableName) {
        return new DatatableData(applicationTableName, registeredTableName);
    }

    private DatatableData(final String applicationTableName, final String registeredTableName) {
        this.applicationTableName = applicationTableName;
        this.registeredTableName = registeredTableName;
    }
}