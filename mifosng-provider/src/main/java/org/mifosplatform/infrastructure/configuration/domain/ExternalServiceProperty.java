package org.mifosplatform.infrastructure.configuration.domain;
/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "m_external_services", uniqueConstraints = { @UniqueConstraint(columnNames =  ("name"))})
public class ExternalServiceProperty extends AbstractPersistable<Long> {

    @SuppressWarnings("unused")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "value", nullable = false)
    private String value;

    public ExternalServiceProperty(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    public ExternalServiceProperty() {
    }

    public String getValue(){
        return this.value;
    }
}