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
package org.apache.fineract.infrastructure.configuration.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.apache.fineract.infrastructure.configuration.exception.GlobalConfigurationPropertyCannotBeModfied;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.security.exception.ForcePasswordResetException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GlobalConfigurationPropertyUpdateService {

    private final JdbcTemplate jdbcTemplate;

    public Map<String, Object> update(final GlobalConfigurationProperty property, final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        if (property.isTrapDoor() && isAnyProductAlreadyCreated()) {
            throw new GlobalConfigurationPropertyCannotBeModfied(property.getId());
        }

        final String enabledParamName = "enabled";
        if (command.isChangeInBooleanParameterNamed(enabledParamName, property.isEnabled())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(enabledParamName);
            actualChanges.put(enabledParamName, newValue);
            property.setEnabled(newValue);
        }

        final String valueParamName = "value";
        final Long previousValue = property.getValue();
        if (command.isChangeInLongParameterNamed(valueParamName, property.getValue())) {
            final Long newValue = command.longValueOfParameterNamed(valueParamName);
            actualChanges.put(valueParamName, newValue);
            property.setValue(newValue);
        }

        final String dateValueParamName = "dateValue";
        if (command.isChangeInDateParameterNamed(dateValueParamName, property.getDateValue())) {
            final LocalDate newDateValue = command.localDateValueOfParameterNamed(dateValueParamName);
            actualChanges.put(dateValueParamName, newDateValue);
            property.setDateValue(newDateValue);
        }

        final String stringValueParamName = "stringValue";
        if (command.isChangeInStringParameterNamed(stringValueParamName, property.getStringValue())) {
            final String newStringValue = command.stringValueOfParameterNamed(stringValueParamName);
            actualChanges.put(stringValueParamName, newStringValue);
            property.setStringValue(newStringValue);
        }

        final String passwordPropertyName = GlobalConfigurationConstants.FORCE_PASSWORD_RESET_DAYS;
        if (property.getName().equalsIgnoreCase(passwordPropertyName)) {
            if ((property.isEnabled() && command.hasParameter(valueParamName) && (property.getValue() == 0))
                    || (property.isEnabled() && !command.hasParameter(valueParamName) && (previousValue == 0))) {
                throw new ForcePasswordResetException();
            }
        }

        return actualChanges;
    }

    private boolean isAnyProductAlreadyCreated() {
        String productExistenceSql = "SELECT EXISTS (SELECT 1 FROM m_loan) OR EXISTS (SELECT 1 FROM m_savings_account) OR"
                + " EXISTS (SELECT 1 FROM m_share_account) AS products_exist";
        Boolean productsExist = this.jdbcTemplate.queryForObject(productExistenceSql, Boolean.class);
        return BooleanUtils.isTrue(productsExist);
    }
}
