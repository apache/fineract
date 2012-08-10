package org.mifosng.platform.api.data;

import java.util.ArrayList;
import java.util.Collection;

public class GroupData {
    
    private final Long id;
    private final String name;
    private final String externalId;
    
    private Collection<ClientMemberData> clientMembers = new ArrayList<ClientMemberData>();
    
    public GroupData(Long id, String name, String externalId) {
        super();
        this.id = id;
        this.name = name;
        this.externalId = externalId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getExternalId() {
        return externalId;
    }

    public Collection<ClientMemberData> getClientMembers() {
        return clientMembers;
    }

    public void setClientMembers(Collection<ClientMemberData> clientMembers) {
        this.clientMembers = clientMembers;
    }

}
