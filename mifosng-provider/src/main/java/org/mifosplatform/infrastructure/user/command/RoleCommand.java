package org.mifosplatform.infrastructure.user.command;

import java.util.Set;

/**
 * Immutable command for creating or updating details of a role.
 * 
 * <p>Fields that are transient are intended not to be serialized into JSON.</p>
 */
public class RoleCommand {

    private final transient Long id;
    private final String name;
    private final String description;

    private final transient boolean makerCheckerApproval;
    private final transient Set<String> modifiedParameters;

    public RoleCommand(
            final Set<String> modifiedParameters,
            final boolean makerCheckerApproval,
            final Long id, 
            final String name, 
            final String description) {
        this.modifiedParameters = modifiedParameters;
        this.makerCheckerApproval = makerCheckerApproval;
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
    
    public boolean isApprovedByChecker() {
        return this.makerCheckerApproval;
    }
}