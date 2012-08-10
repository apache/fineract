package org.mifosng.platform.api.data;

import org.apache.commons.lang.StringUtils;

public class ClientMemberData {

    private final Long id;
    private final String displayName;

    public ClientMemberData(final Long id, final String firstname, final String lastname) {
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

}