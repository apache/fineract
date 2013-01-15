package org.mifosplatform.portfolio.client.data;

import org.apache.commons.lang.StringUtils;

public class ClientLookup {

    private final Long id;
    private final String displayName;
    private final Long officeId;
    private final String officeName;
    
    public static ClientLookup template(final Long id, final String displayName, final Long officeId, final String officeName) {
        return new ClientLookup(id, displayName, officeId, officeName);
    }
    
    public static ClientLookup create(final Long id, final String displayName) {
        return new ClientLookup(id, displayName, null, null);
    }

    public ClientLookup(final Long id, final String firstname, final String lastname, final Long officeId, final String officeName) {
        this.id = id;

        StringBuilder displayNameBuilder = new StringBuilder(firstname);
        if (StringUtils.isNotBlank(displayNameBuilder.toString())) {
            displayNameBuilder.append(' ');
        }
        displayNameBuilder.append(lastname);

        this.displayName = displayNameBuilder.toString();

        this.officeId = officeId;
        this.officeName = officeName;
    }
    
    public ClientLookup(final Long id, final String displayName, final Long officeId, final String officeName) {
        this.id = id;
        this.displayName = displayName;
        this.officeId = officeId;
        this.officeName = officeName;
    }

    public ClientLookup(final Long id, final String firstname, final String lastname) {
        this.id = id;

        StringBuilder displayNameBuilder = new StringBuilder(firstname);
        if (StringUtils.isNotBlank(displayNameBuilder.toString())) {
            displayNameBuilder.append(' ');
        }
        displayNameBuilder.append(lastname);

        this.displayName = displayNameBuilder.toString();

        this.officeId = null;
        this.officeName = null;
    }

    @Override
    public boolean equals(Object obj) {
        ClientLookup clientLookup = (ClientLookup) obj;
        return this.id.equals(clientLookup.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Long getOfficeId() {
        return officeId;
    }

    public String getOfficeName() {
        return officeName;
    }
}