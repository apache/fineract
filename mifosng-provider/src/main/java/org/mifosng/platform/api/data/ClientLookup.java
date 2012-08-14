package org.mifosng.platform.api.data;

import org.apache.commons.lang.StringUtils;

public class ClientLookup {

    private final Long id;
    private final String displayName;

    public ClientLookup(final Long id, final String firstname, final String lastname) {
        this.id = id;
        
        StringBuilder displayNameBuilder = new StringBuilder(firstname);
        if (StringUtils.isNotBlank(displayNameBuilder.toString())) {
            displayNameBuilder.append(' ');
        }
        displayNameBuilder.append(lastname);

        this.displayName = displayNameBuilder.toString();
        
    }

    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean equals(Object obj) {
        ClientLookup clientLookup = (ClientLookup) obj;
        return this.id.equals(clientLookup.getId());
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}