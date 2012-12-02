package org.mifosplatform.portfolio.group.command;

import java.util.Set;

public class GroupCommand {

    private final Long id;
    private final String externalId;
    private final String name;
    private final Long officeId;

    private final String[] clientMembers;

    private final Set<String> modifiedParameters;

    public GroupCommand(Set<String> modifiedParameters, final Long id, final String externalId, final String name,
                        final Long officeId, String[] clientMembers) {
        this.id = id;
        this.officeId = officeId;
        this.externalId = externalId;
        this.name = name;
        this.clientMembers = clientMembers;
        this.modifiedParameters = modifiedParameters;
    }

    public Long getOfficeId() {
        return officeId;
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

    public boolean isOfficeIdChanged() {
        return this.modifiedParameters.contains("officeId");
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
