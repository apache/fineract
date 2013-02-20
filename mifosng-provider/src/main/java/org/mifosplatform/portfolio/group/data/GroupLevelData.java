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
    private final Long parentLevelId;
    private final String levelName;
    private final boolean superParent;
    private final boolean recursable;
    private final boolean canHaveClients;
    
    public GroupLevelData(Long levelId, Long parentLevelId, String levelName, boolean superParent, boolean recursable,
            boolean canHaveClients) {
        this.levelId = levelId;
        this.parentLevelId = parentLevelId;
        this.levelName = levelName;
        this.superParent = superParent;
        this.recursable = recursable;
        this.canHaveClients = canHaveClients;
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