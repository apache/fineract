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
package org.apache.fineract.infrastructure.security.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.security.constants.TwoFactorConfigurationConstants;

@Entity
@Table(name = "twofactor_configuration",
        uniqueConstraints = {@UniqueConstraint(columnNames = { "name" }, name = "name_UNIQUE")})
public class TwoFactorConfiguration extends AbstractPersistableCustom<Long> {

    @Column(name = "name", nullable = false, length = 32)
    private String name;

    @Column(name = "value", nullable = true, length = 1024)
    private String value;

    public String getName() {
        return name;
    }

    public String getStringValue() {
        return value;
    }

    public Boolean getBooleanValue() {
        return BooleanUtils.toBooleanObject(value);
    }

    public Integer getIntegerValue() {
        try {
            return NumberUtils.createInteger(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Object getObjectValue() {
        if(TwoFactorConfigurationConstants.NUMBER_PARAMETERS.contains(name)) {
            return getIntegerValue();
        }
        if(TwoFactorConfigurationConstants.BOOLEAN_PARAMETERS.contains(name)) {
            return getBooleanValue();
        }

        return getStringValue();
    }

    public void setStringValue(String value) {
        this.value = value;
    }

    public void setBooleanValue(boolean value) {
        this.value = String.valueOf(value);
    }

    public void setIntegerValue(long value) {
        this.value = String.valueOf(value);
    }
}
