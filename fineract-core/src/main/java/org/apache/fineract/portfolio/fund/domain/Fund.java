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
package org.apache.fineract.portfolio.fund.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Getter
@Setter
@Table(name = "m_fund", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "fund_name_org"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "fund_externalid_org") })
public class Fund extends AbstractPersistableCustom {

    @Column(name = "name")
    private String name;

    @Column(name = "external_id", length = 100)
    private String externalId;

    public static Fund fromJson(final JsonCommand command) {

        final String firstnameParamName = "name";
        final String name = command.stringValueOfParameterNamed(firstnameParamName);

        final String lastnameParamName = "externalId";
        final String externalId = command.stringValueOfParameterNamed(lastnameParamName);

        return new Fund(name, externalId);
    }

    public Fund() {
        //
    }

    private Fund(final String fundName, final String externalId) {
        this.name = StringUtils.defaultIfEmpty(fundName, null);
        this.externalId = StringUtils.defaultIfEmpty(externalId, null);
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String nameParamName = "name";
        if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            this.name = StringUtils.defaultIfEmpty(newValue, null);
        }

        final String externalIdParamName = "externalId";
        if (command.isChangeInStringParameterNamed(externalIdParamName, this.externalId)) {
            final String newValue = command.stringValueOfParameterNamed(externalIdParamName);
            actualChanges.put(externalIdParamName, newValue);
            this.externalId = StringUtils.defaultIfEmpty(newValue, null);
        }

        return actualChanges;
    }
}
