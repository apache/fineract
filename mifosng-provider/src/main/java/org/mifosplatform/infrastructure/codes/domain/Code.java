/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.codes.domain;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.codes.exception.SystemDefinedCodeCannotBeChangedException;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_code", uniqueConstraints = { @UniqueConstraint(columnNames = { "code_name" }, name = "code_name") })
public class Code extends AbstractPersistable<Long> {

    @Column(name = "code_name", length = 100)
    private String name;

    @Column(name = "is_system_defined")
    private final boolean systemDefined;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "code", orphanRemoval = true)
    private Set<CodeValue> values;

    public static Code fromJson(final JsonCommand command) {
        final String name = command.stringValueOfParameterNamed("name");
        return new Code(name);
    }

    protected Code() {
        this.systemDefined = false;
    }

    private Code(final String name) {
        this.name = name;
        this.systemDefined = false;
    }

    public String name() {
        return this.name;
    }

    public boolean isSystemDefined() {
        return this.systemDefined;
    }

    public Map<String, Object> update(final JsonCommand command) {

        if (this.systemDefined) { throw new SystemDefinedCodeCannotBeChangedException(); }

        final Map<String, Object> actualChanges = new LinkedHashMap<>(1);

        final String firstnameParamName = "name";
        if (command.isChangeInStringParameterNamed(firstnameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(firstnameParamName);
            actualChanges.put(firstnameParamName, newValue);
            this.name = StringUtils.defaultIfEmpty(newValue, null);
        }

        return actualChanges;
    }

    public boolean remove(final CodeValue codeValueToDelete) {
        return this.values.remove(codeValueToDelete);
    }
}