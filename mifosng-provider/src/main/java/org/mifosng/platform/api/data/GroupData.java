package org.mifosng.platform.api.data;

import java.util.ArrayList;
import java.util.Collection;

public class GroupData {
    
    private final Long id;
    private final String name;
    private final String externalId;
    
    private Collection<ClientLookup> clientMembers = new ArrayList<ClientLookup>();
    private Collection<ClientLookup> allowedClients = new ArrayList<ClientLookup>();
    
    public GroupData(Long id, String name, String externalId) {
        this.id = id;
        this.name = name;
        this.externalId = externalId;
    }

    public GroupData(Collection<ClientLookup> allowedClients) {
        this.id = null;
        this.name = null;
        this.externalId = null;
        this.allowedClients = allowedClients;
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
}
