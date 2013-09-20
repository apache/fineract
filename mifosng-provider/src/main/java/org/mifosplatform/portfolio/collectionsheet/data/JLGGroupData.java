/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collectionsheet.data;

import java.util.Collection;

/**
 * Immutable data object for groups with clients due for disbursement or
 * collection.
 */
public class JLGGroupData {

    private final Long groupId;
    private final String groupName;
    private final Long staffId;
    private final String staffName;
    private final Long levelId;
    private final String levelName;
    private Collection<JLGClientData> clients;

    public JLGGroupData(final Long groupId, final String groupName, final Long staffId, final String staffName, final Long levelId,
            final String levelName) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.staffId = staffId;
        this.staffName = staffName;
        this.levelId = levelId;
        this.levelName = levelName;
    }

    public Long getGroupId() {
        return this.groupId;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public Long getStaffId() {
        return this.staffId;
    }

    public String getStaffName() {
        return this.staffName;
    }

    public Long getLevelId() {
        return this.levelId;
    }

    public String getLevelName() {
        return this.levelName;
    }

    public Collection<JLGClientData> getClients() {
        return this.clients;
    }

    public void setClients(final Collection<JLGClientData> clients) {
        this.clients = clients;
    }
}