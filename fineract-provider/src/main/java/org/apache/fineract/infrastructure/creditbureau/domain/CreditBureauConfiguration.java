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
package org.apache.fineract.infrastructure.creditbureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_creditbureau_configuration")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class CreditBureauConfiguration extends AbstractPersistableCustom<Long> {

    @Column(name = "configkey")
    private String configurationKey;

    @Column(name = "value")
    private String value;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "organisation_creditbureau_id")
    private OrganisationCreditBureau organisationCreditbureau;

    public static CreditBureauConfiguration createNew(final String configurationKey, final String value, final String description,
            final OrganisationCreditBureau organisation_creditbureau) {
        return new CreditBureauConfiguration().setConfigurationKey(configurationKey).setValue(value).setDescription(description)
                .setOrganisationCreditbureau(organisation_creditbureau);
    }

    public void updateConfigurationKey(final String configurationKey) {
        this.configurationKey = StringUtils.defaultIfEmpty(configurationKey, null);
    }

    public void updateValue(final String value) {
        this.value = StringUtils.defaultIfEmpty(value, null);
    }

    public void updateDescription(final String description) {
        this.description = StringUtils.defaultIfEmpty(description, null);
    }

}
