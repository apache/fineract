package org.mifosplatform.infrastructure.dataqueries.data;

/**
 * Immutable data object representing datatable data.
 */
public class DatatableData {

    @SuppressWarnings("unused")
    private final String applicationTableName;
    @SuppressWarnings("unused")
    private final String registeredTableName;

    public DatatableData(final String applicationTableName, final String registeredTableName) {
        this.applicationTableName = applicationTableName;
        this.registeredTableName = registeredTableName;
    }
}