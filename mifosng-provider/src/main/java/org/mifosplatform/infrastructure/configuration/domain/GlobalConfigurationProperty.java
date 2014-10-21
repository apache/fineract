/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "c_configuration")
public class GlobalConfigurationProperty extends AbstractPersistable<Long> {

    @Column(name = "name", nullable = false)
    private final String name;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "value", nullable = true)
    private Long value;
    
    @Column(name = "description", nullable = true)
    private final String description;

    protected GlobalConfigurationProperty() {
        this.name = null;
        this.enabled = false;
        this.value = null;
        this.description = null;
    }

    public GlobalConfigurationProperty(final String name, final boolean enabled, final Long value,
    		final String description) {
        this.name = name;
        this.enabled = enabled;
        this.value = value;
        this.description = description;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public Long getValue() {
        return this.value;
    }

    public boolean updateTo(final boolean value) {
        final boolean updated = this.enabled != value;
        this.enabled = value;
        return updated;
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String enabledParamName = "enabled";
        if (command.isChangeInBooleanParameterNamed(enabledParamName, this.enabled)) {
            final Boolean newValue = command.booleanPrimitiveValueOfParameterNamed(enabledParamName);
            actualChanges.put(enabledParamName, newValue);
            this.enabled = newValue;
        }

        final String valueParamName = "value";
        if (command.isChangeInLongParameterNamed(valueParamName, this.value)) {
            final Long newValue = command.longValueOfParameterNamed(valueParamName);
            actualChanges.put(valueParamName, newValue);
            this.value = newValue;
        }

        return actualChanges;

    }

    public static GlobalConfigurationProperty newSurveyConfiguration(final String name)
    {
        return new GlobalConfigurationProperty(name, false, null, null);
    }

}