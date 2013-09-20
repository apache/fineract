/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "c_configuration")
public class GlobalConfigurationProperty extends AbstractPersistable<Long> {

    @Column(name = "name", nullable = false)
    private final String name;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    protected GlobalConfigurationProperty() {
        this.name = null;
        this.enabled = false;
    }

    public GlobalConfigurationProperty(final String name, final boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean updateTo(final boolean value) {
        final boolean updated = this.enabled != value;
        this.enabled = value;
        return updated;
    }
}