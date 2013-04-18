/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_group_level")
public class GroupLevel extends AbstractPersistable<Long> {

    @Column(name = "parent_id")
    private final Long parentId;

    @Column(name = "super_parent", nullable = false)
    private final boolean superParent;

    @Column(name = "level_name", nullable = false, length = 100, unique = true)
    private final String levelName;

    @Column(name = "recursable", nullable = false)
    private boolean recursable = false;

    @Column(name = "can_have_clients", nullable = false)
    private boolean canHaveClients = false;

    public GroupLevel() {

        this.parentId = null;
        this.superParent = false;
        this.levelName = null;
        this.recursable = false;
        this.canHaveClients = false;

    }

    public GroupLevel(final Long parentId, final boolean isSuperParent, final String levelName, final boolean recursable,
            final boolean canHaveClients) {

        this.superParent = isSuperParent;
        this.parentId = parentId;
        this.levelName = levelName;
        this.recursable = recursable;
        this.canHaveClients = canHaveClients;

    }

    public Long getParentId() {
        return this.parentId;
    }

    public String getLevelName() {
        return this.levelName;
    }

    public boolean isRecursable() {
        return this.recursable;
    }

    public boolean canHaveClients() {
        return this.canHaveClients;
    }

    public boolean isSuperParent() {
        return this.superParent;
    }

    public boolean isIdentifiedByParentId(final Long parentLevelId) {
        return this.parentId.equals(parentLevelId);
    }
}