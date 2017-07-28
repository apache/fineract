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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "c_external_service", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "name_UNIQUE") })
public class ExternalService extends AbstractPersistableCustom<Long> {

    @Column(name = "name", length = 50)
    private String name;

    // @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy =
    // "externalServicePropertiesPK.externalService", orphanRemoval = true)
    // private Set<ExternalServicesProperties> values;

    public static ExternalService fromJson(final JsonCommand command) {
        final String name = command.stringValueOfParameterNamed("name");
        return new ExternalService(name);
    }

    private ExternalService(final String name) {
        this.name = name;
    }

    protected ExternalService() {}

    public String name() {
        return this.name;
    }

}