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
package org.apache.fineract.useradministration.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_permission")
public class Permission extends AbstractPersistableCustom<Long> {

    @Column(name = "grouping", nullable = false, length = 45)
    private String grouping;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "entity_name", nullable = true, length = 100)
    private String entityName;

    @Column(name = "action_name", nullable = true, length = 100)
    private String actionName;

    @Column(name = "can_maker_checker", nullable = false)
    private boolean canMakerChecker;

    public Permission(final String grouping, final String entityName, final String actionName) {
        this.grouping = grouping;
        this.entityName = entityName;
        this.actionName = actionName;
        this.code = actionName + "_" + entityName;
        this.canMakerChecker = false;
    }

    protected Permission() {
        this.grouping = null;
        this.entityName = null;
        this.actionName = null;
        this.code = null;
        this.canMakerChecker = false;
    }

    public boolean hasCode(final String checkCode) {
        return this.code.equalsIgnoreCase(checkCode);
    }

    public String getCode() {
        return this.code;
    }

    public boolean hasMakerCheckerEnabled() {
        return this.canMakerChecker;
    }

    public String getGrouping() {
        return this.grouping;
    }

    public boolean enableMakerChecker(final boolean canMakerChecker) {
        final boolean isUpdatedValueSame = this.canMakerChecker == canMakerChecker;
        this.canMakerChecker = canMakerChecker;

        return !isUpdatedValueSame;
    }
}