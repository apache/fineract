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
package org.apache.fineract.infrastructure.configuration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.configuration.exception.GlobalConfigurationPropertyCannotBeModfied;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.security.exception.ForcePasswordResetException;

@Entity
@Table(name = "c_configuration")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class GlobalConfigurationProperty extends AbstractPersistableCustom<Long> {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "value", nullable = true)
    private Long value;

    @Column(name = "date_value", nullable = true)
    private LocalDate dateValue;

    @Column(name = "string_value", nullable = true)
    private String stringValue;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "is_trap_door", nullable = false)
    private boolean isTrapDoor;

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        if (this.isTrapDoor) {
            throw new GlobalConfigurationPropertyCannotBeModfied(this.getId());
        }

        final String enabledParamName = "enabled";
        if (command.isChangeInBooleanParameterNamed(enabledParamName, this.enabled)) {
            final Boolean newValue = command.booleanPrimitiveValueOfParameterNamed(enabledParamName);
            actualChanges.put(enabledParamName, newValue);
            this.enabled = newValue;
        }

        final String valueParamName = "value";
        final Long previousValue = this.value;
        if (command.isChangeInLongParameterNamed(valueParamName, this.value)) {
            final Long newValue = command.longValueOfParameterNamed(valueParamName);
            actualChanges.put(valueParamName, newValue);
            this.value = newValue;
        }

        final String dateValueParamName = "dateValue";
        if (command.isChangeInDateParameterNamed(dateValueParamName, this.dateValue)) {
            final LocalDate newDateValue = command.localDateValueOfParameterNamed(dateValueParamName);
            actualChanges.put(dateValueParamName, newDateValue);
            this.dateValue = newDateValue;
        }

        final String stringValueParamName = "stringValue";
        if (command.isChangeInStringParameterNamed(stringValueParamName, this.stringValue)) {
            final String newStringValue = command.stringValueOfParameterNamed(stringValueParamName);
            actualChanges.put(stringValueParamName, newStringValue);
            this.stringValue = newStringValue;
        }

        final String passwordPropertyName = "force-password-reset-days";
        if (this.name.equalsIgnoreCase(passwordPropertyName)) {
            if ((this.enabled == true && command.hasParameter(valueParamName) && (this.value == 0))
                    || (this.enabled == true && !command.hasParameter(valueParamName) && (previousValue == 0))) {
                throw new ForcePasswordResetException();
            }
        }

        return actualChanges;
    }

    public static GlobalConfigurationProperty newSurveyConfiguration(final String name) {
        return new GlobalConfigurationProperty().setName(name);
    }

    public GlobalConfigurationPropertyData toData() {
        return new GlobalConfigurationPropertyData().setName(getName()).setEnabled(isEnabled()).setValue(getValue())
                .setDateValue(getDateValue()).setStringValue(getStringValue()).setId(this.getId()).setDescription(this.description)
                .setTrapDoor(this.isTrapDoor);
    }
}
