/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.command;

public class GroupCommand {

    private final String externalId;
    private final String name;
    private final Long officeId;
    private final Long staffId;
    private final Long parentId;
    private final String[] clientMembers;
    private final String[] childGroups;

    public GroupCommand(final String externalId, final String name, final Long officeId, final Long staffId, final String[] clientMembers,
            final String[] childGroups, final Long parentId) {
        this.officeId = officeId;
        this.staffId = staffId;
        this.parentId = parentId;
        this.externalId = externalId;
        this.name = name;
        this.clientMembers = clientMembers;
        this.childGroups = childGroups;

    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public Long getParentId() {
        return this.parentId;
    }

    public Long getStaffId() {
        return this.staffId;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public String getName() {
        return this.name;
    }

    public String[] getClientMembers() {
        return this.clientMembers;
    }

    public String[] getChildGroups() {
        return this.childGroups;
    }

}