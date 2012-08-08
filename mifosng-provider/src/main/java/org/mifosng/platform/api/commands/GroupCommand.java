package org.mifosng.platform.api.commands;

import java.util.Set;

public class GroupCommand {

    private final Long id;
    private final String externalId;
    private final String name;
    private final String[] clientMembers;

    private final Set<String> modifiedParameters;

    public GroupCommand(Set<String> modifiedParameters, final Long id, String externalId, String name, String[] clientMembers) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.clientMembers = clientMembers;
        this.modifiedParameters = modifiedParameters;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public String[] getClientMembers() {
        return clientMembers;
    }

    public boolean isNameChanged() {
        return this.modifiedParameters.contains("name");
    }
    
    public boolean isExternalIdChanged() {
        return this.modifiedParameters.contains("externalId");
    }
    
    public boolean isClientMembersChanged() {
        return this.modifiedParameters.contains("clientMembers");
    }
    
}
