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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.security.constants.TwoFactorConfigurationConstants;

@Entity
@Table(name = "twofactor_configuration", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "name_UNIQUE") })
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class TwoFactorConfiguration extends AbstractPersistableCustom<Long> {

    @Column(name = "name", nullable = false, length = 32)
    private String name;

    @Column(name = "value", nullable = true, length = 1024)
    private String value;

    public Object getObjectValue() {
        if (TwoFactorConfigurationConstants.NUMBER_PARAMETERS.contains(name)) {
            return NumberUtils.createInteger(value);
        }
        if (TwoFactorConfigurationConstants.BOOLEAN_PARAMETERS.contains(name)) {
            return BooleanUtils.toBooleanObject(value);
        }

        return getValue();
    }
}
