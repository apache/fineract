package org.mifosplatform.commands.data;

import org.joda.time.LocalDate;

/**
 * Immutable data object representing maker-checker entry
 */
final public class CommandSourceData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String actionName;
    @SuppressWarnings("unused")
    private final String entityName;
    @SuppressWarnings("unused")
    private final Long entityId;
    @SuppressWarnings("unused")
    private final String entityHref;
    private String commandJson;
    @SuppressWarnings("unused")
    private final LocalDate madeOnDate;

    public CommandSourceData(final Long id, final String actionName, final String entityName, final Long entityId, final String entityHref,
            final String commandJson, final LocalDate madeOnDate) {
        this.id = id;
        this.actionName = actionName;
        this.entityName = entityName;
        this.entityId = entityId;
        this.entityHref = entityHref;
        this.commandJson = commandJson;
        this.madeOnDate = madeOnDate;
    }

    public String json() {
        return this.commandJson;
    }
}