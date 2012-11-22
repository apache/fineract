package org.mifosng.platform.api.commands;

import java.util.Set;

/**
 * Immutable command for creating or updating details of a role.
 */
public class RoleCommand {

    private final Long id;
    private final String name;
    private final String description;

    private final Set<String> modifiedParameters;

    public RoleCommand(Set<String> modifiedParameters, final Long id, final String name, final String description) {
        this.modifiedParameters = modifiedParameters;
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isNameChanged() {
        return this.modifiedParameters.contains("name");
    }

    public boolean isDescriptionChanged() {
        return this.modifiedParameters.contains("description");
    }
}