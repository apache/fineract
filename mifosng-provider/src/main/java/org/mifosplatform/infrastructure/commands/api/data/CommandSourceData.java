package org.mifosplatform.infrastructure.commands.api.data;

import org.joda.time.LocalDate;

/**
 * Immutable data object representing maker-checker entry
 */
final public class CommandSourceData {

    @SuppressWarnings("unused")
    private Long id;
    private String apiOperation;
    private String resource;
    private final Long resourceId;
    private String commandJson;
    @SuppressWarnings("unused")
    private final LocalDate madeOnDate;

    public CommandSourceData(final Long id, final String apiOperation, final String resource, final Long resourceId,
            final String commandJson, final LocalDate madeOnDate) {
        this.id = id;
        this.apiOperation = apiOperation;
        this.resource = resource;
        this.resourceId = resourceId;
        this.commandJson = commandJson;
        this.madeOnDate = madeOnDate;
    }

    public String json() {
        return this.commandJson;
    }
    
    public boolean isUpdateRolePermissions() {
        return this.apiOperation.equalsIgnoreCase("UPDATEPERMISSIONS") && this.resourceId != null;
    }
    
    public boolean isRoleResource() {
        return this.resource.equalsIgnoreCase("ROLES");
    }
}