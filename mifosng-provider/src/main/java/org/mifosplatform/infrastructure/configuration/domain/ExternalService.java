package org.mifosplatform.infrastructure.configuration.domain;

/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "c_external_service", uniqueConstraints = { @UniqueConstraint(columnNames = "name") })
public class ExternalService extends AbstractPersistable<Long> {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "value", nullable = false)
    private String value;

    public ExternalService(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    protected ExternalService() {}

    public String getValue() {
        return this.value;
    }
}