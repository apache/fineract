package org.mifosng.platform.api.data;

public class GroupData {
    
    private final Long id;
    private final String name;
    private final String externalId;
    
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

}
