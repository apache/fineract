package org.mifosng.platform.api.commands;

import java.util.Set;

public class GroupCommand {

    private final String externalId;
    private final String name;

    private final Set<String> modifiedParameters;

    public GroupCommand(Set<String> modifiedParameters, String externalId, String name) {
        this.externalId = externalId;
        this.name = name;
        this.modifiedParameters = modifiedParameters;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

}
