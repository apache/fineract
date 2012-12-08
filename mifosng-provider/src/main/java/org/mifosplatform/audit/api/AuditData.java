package org.mifosplatform.audit.api;

import org.joda.time.LocalDate;

/**
 * Immutable data object representing client data.
 */
final public class AuditData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String apiOperation;
    @SuppressWarnings("unused")
    private final String resource;
    @SuppressWarnings("unused")
    private final Long resourceId;
    @SuppressWarnings("unused")
    private final String maker;
    @SuppressWarnings("unused")
    private final LocalDate madeOnDate;
    @SuppressWarnings("unused")
    private final String checker;
    @SuppressWarnings("unused")
    private final LocalDate checkedOnDate;
    @SuppressWarnings("unused")
    private final String commandAsJson;

    public AuditData(final Long id, final String apiOperation, final String resource, final Long resourceId, final String maker,
            final LocalDate madeOnDate, final String checker, final LocalDate checkedOnDate, final String commandAsJson) {

        this.id = id;
        this.apiOperation = apiOperation;
        this.resource = resource;
        this.resourceId = resourceId;
        this.maker = maker;
        this.madeOnDate = madeOnDate;
        this.checker = checker;
        this.checkedOnDate = checkedOnDate;
        this.commandAsJson = commandAsJson;
    }
}