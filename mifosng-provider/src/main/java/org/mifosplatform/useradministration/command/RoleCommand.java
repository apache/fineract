package org.mifosplatform.useradministration.command;

/**
 * Immutable command for creating or updating details of a role.
 */
public class RoleCommand {

    private final String name;
    private final String description;

    public RoleCommand(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}