package org.mifosplatform.infrastructure.configuration.domain;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;

/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "c_external_service", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "name_UNIQUE") })
public class ExternalService extends AbstractPersistable<Long> {

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