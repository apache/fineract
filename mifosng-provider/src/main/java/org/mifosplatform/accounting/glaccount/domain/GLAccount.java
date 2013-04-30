/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.glaccount.domain;

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
import org.mifosplatform.accounting.glaccount.api.GLAccountJsonInputParams;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "acc_gl_account", uniqueConstraints = { @UniqueConstraint(columnNames = { "gl_code" }, name = "acc_gl_code") })
public class GLAccount extends AbstractPersistable<Long> {

    @SuppressWarnings("unused")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private GLAccount parent;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private final List<GLAccount> children = new LinkedList<GLAccount>();

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

    protected GLAccount() {
        //
    }

    private GLAccount(final GLAccount parent, final String name, final String glCode, final boolean disabled,
            final boolean manualEntriesAllowed, final Integer type, final Integer usage, final String description) {
        this.name = StringUtils.defaultIfEmpty(name, null);
        this.glCode = StringUtils.defaultIfEmpty(glCode, null);
        this.disabled = BooleanUtils.toBooleanDefaultIfNull(disabled, false);
        this.manualEntriesAllowed = BooleanUtils.toBooleanDefaultIfNull(manualEntriesAllowed, true);
        this.usage = usage;
        this.type = type;
        this.description = StringUtils.defaultIfEmpty(description, null);
        this.parent = parent;
    }

    public static GLAccount fromJson(final GLAccount parent, final JsonCommand command) {
        final String name = command.stringValueOfParameterNamed(GLAccountJsonInputParams.NAME.getValue());
        final String glCode = command.stringValueOfParameterNamed(GLAccountJsonInputParams.GL_CODE.getValue());
        final boolean disabled = command.booleanPrimitiveValueOfParameterNamed(GLAccountJsonInputParams.DISABLED.getValue());
        final boolean manualEntriesAllowed = command.booleanPrimitiveValueOfParameterNamed(GLAccountJsonInputParams.MANUAL_ENTRIES_ALLOWED
                .getValue());
        final Integer usage = command.integerValueSansLocaleOfParameterNamed(GLAccountJsonInputParams.USAGE.getValue());
        final Integer type = command.integerValueSansLocaleOfParameterNamed(GLAccountJsonInputParams.TYPE.getValue());
        final String description = command.stringValueOfParameterNamed(GLAccountJsonInputParams.DESCRIPTION.getValue());
        return new GLAccount(parent, name, glCode, disabled, manualEntriesAllowed, type, usage, description);
    }

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(15);
        handlePropertyUpdate(command, actualChanges, GLAccountJsonInputParams.DESCRIPTION.getValue(), this.description);
        handlePropertyUpdate(command, actualChanges, GLAccountJsonInputParams.DISABLED.getValue(), this.disabled);
        handlePropertyUpdate(command, actualChanges, GLAccountJsonInputParams.GL_CODE.getValue(), this.glCode);
        handlePropertyUpdate(command, actualChanges, GLAccountJsonInputParams.MANUAL_ENTRIES_ALLOWED.getValue(), this.manualEntriesAllowed);
        handlePropertyUpdate(command, actualChanges, GLAccountJsonInputParams.NAME.getValue(), this.name);
        handlePropertyUpdate(command, actualChanges, GLAccountJsonInputParams.PARENT_ID.getValue(), 0L);
        handlePropertyUpdate(command, actualChanges, GLAccountJsonInputParams.TYPE.getValue(), this.type, true);
        handlePropertyUpdate(command, actualChanges, GLAccountJsonInputParams.USAGE.getValue(), this.usage, true);
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

}