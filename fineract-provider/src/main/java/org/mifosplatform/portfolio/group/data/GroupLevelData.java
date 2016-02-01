/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.data;

/**
 * Immutable data object representing groups.
 */
public class GroupLevelData {

    private final Long levelId;
    private final String levelName;
    private final Long parentLevelId;
    private final String parentLevelName;
    private final Long childLevelId;
    private final String childLevelName;
    private final boolean superParent;
    private final boolean recursable;
    private final boolean canHaveClients;

    public GroupLevelData(final Long levelId, final String levelName, final Long parentLevelId, final String parentLevelName,
            final Long childLevelId, final String childLevelName, final boolean superParent, final boolean recursable,
            final boolean canHaveClients) {
        super();
        this.levelId = levelId;
        this.levelName = levelName;
        this.parentLevelId = parentLevelId;
        this.parentLevelName = parentLevelName;
        this.childLevelId = childLevelId;
        this.childLevelName = childLevelName;
        this.superParent = superParent;
        this.recursable = recursable;
        this.canHaveClients = canHaveClients;
    }

    public String getParentLevelName() {
        return this.parentLevelName;
    }

    public Long getChildLevelId() {
        return this.childLevelId;
    }

    public String getChildLevelName() {
        return this.childLevelName;
    }

    public Long getLevelId() {
        return this.levelId;
    }

    public Long getParentLevelId() {
        return this.parentLevelId;
    }

    public String getLevelName() {
        return this.levelName;
    }

    public boolean isSuperParent() {
        return this.superParent;
    }

    public boolean isRecursable() {
        return this.recursable;
    }

    public boolean isCanHaveClients() {
        return this.canHaveClients;
    }

}