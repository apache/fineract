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
package org.apache.fineract.accounting.glaccount.domain;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.accounting.glaccount.api.GLAccountJsonInputParams;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "acc_gl_account", uniqueConstraints = { @UniqueConstraint(columnNames = { "gl_code" }, name = "acc_gl_code") })
public class GLAccount extends AbstractPersistableCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private GLAccount parent;

    @Column(name = "hierarchy", nullable = true, length = 50)
    private String hierarchy;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private List<GLAccount> children = new LinkedList<>();

    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @Column(name = "gl_code", nullable = false, length = 100)
    private String glCode;

    @Column(name = "disabled", nullable = false)
    private boolean disabled = false;

    @Column(name = "manual_journal_entries_allowed", nullable = false)
    private boolean manualEntriesAllowed = true;

    @Column(name = "classification_enum", nullable = false)
    private Integer type;

    @Column(name = "account_usage", nullable = false)
    private Integer usage;

    @Column(name = "description", nullable = true, length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private CodeValue tagId;

    protected GLAccount() {
        //
    }

    private GLAccount(final GLAccount parent, final String name, final String glCode, final boolean disabled,
            final boolean manualEntriesAllowed, final Integer type, final Integer usage, final String description, final CodeValue tagId) {
        this.name = StringUtils.defaultIfEmpty(name, null);
        this.glCode = StringUtils.defaultIfEmpty(glCode, null);
        this.disabled = BooleanUtils.toBooleanDefaultIfNull(disabled, false);
        this.manualEntriesAllowed = BooleanUtils.toBooleanDefaultIfNull(manualEntriesAllowed, true);
        this.usage = usage;
        this.type = type;
        this.description = StringUtils.defaultIfEmpty(description, null);
        this.parent = parent;
        this.tagId = tagId;
    }

    public static GLAccount fromJson(final GLAccount parent, final JsonCommand command, final CodeValue glAccountTagType) {
        final String name = command.stringValueOfParameterNamed(GLAccountJsonInputParams.NAME.getValue());
        final String glCode = command.stringValueOfParameterNamed(GLAccountJsonInputParams.GL_CODE.getValue());
        final boolean disabled = command.booleanPrimitiveValueOfParameterNamed(GLAccountJsonInputParams.DISABLED.getValue());
        final boolean manualEntriesAllowed = command.booleanPrimitiveValueOfParameterNamed(GLAccountJsonInputParams.MANUAL_ENTRIES_ALLOWED
                .getValue());
        final Integer usage = command.integerValueSansLocaleOfParameterNamed(GLAccountJsonInputParams.USAGE.getValue());
        final Integer type = command.integerValueSansLocaleOfParameterNamed(GLAccountJsonInputParams.TYPE.getValue());
        final String description = command.stringValueOfParameterNamed(GLAccountJsonInputParams.DESCRIPTION.getValue());
        return new GLAccount(parent, name, glCode, disabled, manualEntriesAllowed, type, usage, description, glAccountTagType);
    }

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(15);
        handlePropertyUpdate(command, actualChanges, GLAccountJsonInputParams.DESCRIPTION.getValue(), this.description);
        handlePropertyUpdate(command, actualChanges, GLAccountJsonInputParams.DISABLED.getValue(), this.disabled);
        handlePropertyUpdate(command, actualChanges, GLAccountJsonInputParams.GL_CODE.getValue(), this.glCode);
        handlePropertyUpdate(command, actualChanges, GLAccountJsonInputParams.MANUAL_ENTRIES_ALLOWED.getValue(), this.manualEntriesAllowed);
        handlePropertyUpdate(command, actualChanges, GLAccountJsonInputParams.NAME.getValue(), this.name);
        handlePropertyUpdate(command, actualChanges, GLAccountJsonInputParams.PARENT_ID.getValue(), 0L);
        handlePropertyUpdate(command, actualChanges, GLAccountJsonInputParams.TYPE.getValue(), this.type, true);
        handlePropertyUpdate(command, actualChanges, GLAccountJsonInputParams.USAGE.getValue(), this.usage, true);
        handlePropertyUpdate(command, actualChanges, GLAccountJsonInputParams.TAGID.getValue(),
                this.tagId == null ? 0L : this.tagId.getId());
        return actualChanges;
    }

    private void handlePropertyUpdate(final JsonCommand command, final Map<String, Object> actualChanges, final String paramName,
            final Integer propertyToBeUpdated, final boolean sansLocale) {
        boolean changeDetected = false;
        if (sansLocale) {
            changeDetected = command.isChangeInIntegerSansLocaleParameterNamed(paramName, propertyToBeUpdated);
        } else {
            changeDetected = command.isChangeInIntegerParameterNamed(paramName, propertyToBeUpdated);
        }
        if (changeDetected) {
            Integer newValue = null;
            if (sansLocale) {
                newValue = command.integerValueSansLocaleOfParameterNamed(paramName);
            } else {
                newValue = command.integerValueOfParameterNamed(paramName);
            }
            actualChanges.put(paramName, newValue);
            // now update actual property
            if (paramName.equals(GLAccountJsonInputParams.TYPE.getValue())) {
                this.type = newValue;
            } else if (paramName.equals(GLAccountJsonInputParams.USAGE.getValue())) {
                this.usage = newValue;
            }
        }
    }

    private void handlePropertyUpdate(final JsonCommand command, final Map<String, Object> actualChanges, final String paramName,
            final String propertyToBeUpdated) {
        if (command.isChangeInStringParameterNamed(paramName, propertyToBeUpdated)) {
            final String newValue = command.stringValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            // now update actual property
            if (paramName.equals(GLAccountJsonInputParams.DESCRIPTION.getValue())) {
                this.description = newValue;
            } else if (paramName.equals(GLAccountJsonInputParams.GL_CODE.getValue())) {
                this.glCode = newValue;
            } else if (paramName.equals(GLAccountJsonInputParams.NAME.getValue())) {
                this.name = newValue;
            }
        }
    }

    private void handlePropertyUpdate(final JsonCommand command, final Map<String, Object> actualChanges, final String paramName,
            final Long propertyToBeUpdated) {
        if (command.isChangeInLongParameterNamed(paramName, propertyToBeUpdated)) {
            final Long newValue = command.longValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            // now update actual property
            if (paramName.equals(GLAccountJsonInputParams.PARENT_ID.getValue())) {
                // do nothing as this is a nested property
            }
        }
    }

    private void handlePropertyUpdate(final JsonCommand command, final Map<String, Object> actualChanges, final String paramName,
            final boolean propertyToBeUpdated) {
        if (command.isChangeInBooleanParameterNamed(paramName, propertyToBeUpdated)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            // now update actual property
            if (paramName.equals(GLAccountJsonInputParams.MANUAL_ENTRIES_ALLOWED.getValue())) {
                this.manualEntriesAllowed = newValue;
            } else if (paramName.equals(GLAccountJsonInputParams.DISABLED.getValue())) {
                this.disabled = newValue;
            }
        }
    }

    public boolean isHeaderAccount() {
        return GLAccountUsage.HEADER.getValue().equals(this.usage);
    }

    public Integer getUsage() {
        return this.usage;
    }

    public List<GLAccount> getChildren() {
        return this.children;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public boolean isManualEntriesAllowed() {
        return this.manualEntriesAllowed;
    }

    public String getGlCode() {
        return this.glCode;
    }

    public String getName() {
        return this.name;
    }

    public Integer getType() {
        return this.type;
    }

    public void generateHierarchy() {

        if (this.parent != null) {
            this.hierarchy = this.parent.hierarchyOf(getId());
        } else {
            this.hierarchy = ".";
        }
    }

    private String hierarchyOf(final Long id) {
        return this.hierarchy + id.toString() + ".";
    }

    public boolean isDetailAccount() {
        return GLAccountUsage.DETAIL.getValue().equals(this.usage);
    }

    public void updateTagId(final CodeValue tagID) {
        this.tagId = tagID;
    }

    public void updateParentAccount(final GLAccount parentAccount) {
        this.parent = parentAccount;
        generateHierarchy();
    }
}