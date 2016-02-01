/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.group.data;

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