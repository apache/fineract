package org.mifosng.platform.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GroupData {
    
    private final Long id;
    private final String name;
    private final String externalId;
    private final Long officeId;
    private final String officeName;

    private Collection<ClientLookup> clientMembers = new ArrayList<ClientLookup>();
    private Collection<ClientLookup> allowedClients = new ArrayList<ClientLookup>();
    private Collection<OfficeLookup> allowedOffices = new ArrayList<OfficeLookup>();

    public GroupData(Long id, Long officeId, String officeName, String name, String externalId) {
        this.id = id;
        this.name = name;
        this.externalId = externalId;
        this.officeId = officeId;
        this.officeName = officeName;
    }

    public GroupData(Collection<ClientLookup> allowedClients, Collection<OfficeLookup> allowedOffices){
        this.id = null;
        this.name = null;
        this.externalId = null;
        this.officeId = null;
        this.officeName = null;
        this.allowedClients = allowedClients;
        this.allowedOffices = allowedOffices;
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

    public Collection<ClientLookup> getClientMembers() {
        return clientMembers;
    }

    public void setClientMembers(Collection<ClientLookup> clientMembers) {
        this.clientMembers = clientMembers;
    }

    public Collection<ClientLookup> getAllowedClients() {
        return allowedClients;
    }

    public void setAllowedClients(Collection<ClientLookup> allowedClients) {
        this.allowedClients = allowedClients;
    }

    public Collection<OfficeLookup> getAllowedOffices() {
        return allowedOffices;
    }

    public void setAllowedOffices(Collection<OfficeLookup> allowedOffices) {
        this.allowedOffices = allowedOffices;
    }
}
