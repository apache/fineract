/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.command;

import java.util.Set;

public class GroupCommand {

    private final Long id;
    private final String externalId;
    private final String name;
    private final Long officeId;
    private final Long loanOfficerId;

    private final String[] clientMembers;

    private final Set<String> modifiedParameters;

    public GroupCommand(Set<String> modifiedParameters, final Long id, final String externalId, final String name,
                        final Long officeId, final Long loanOfficerId, String[] clientMembers) {
        this.id = id;
        this.officeId = officeId;
        this.loanOfficerId = loanOfficerId;
        this.externalId = externalId;
        this.name = name;
        this.clientMembers = clientMembers;
        this.modifiedParameters = modifiedParameters;
        
    }

    public Long getOfficeId() {
        return officeId;
    }

    public Long getLoanOfficeId() {
        return loanOfficerId;
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

    public boolean isLoanOfficerChanged() {
        return this.modifiedParameters.contains("loanOfficerId");
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
