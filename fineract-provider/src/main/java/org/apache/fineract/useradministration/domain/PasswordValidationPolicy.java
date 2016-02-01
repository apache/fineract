/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_password_validation_policy")
public class PasswordValidationPolicy extends AbstractPersistable<Long> {

    // private final static Logger logger =
    // LoggerFactory.getLogger(PasswordValidationPolicy.class);

    @Column(name = "regex", nullable = false)
    private String regex;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active;

    public PasswordValidationPolicy(final String regex, final String description, final boolean active) {
        this.description = description;
        this.regex = regex;
        this.active = active;
    }

    public PasswordValidationPolicy() {
        this.active = false;
    }

    public String getDescription() {
        return description;
    }

    public String getRegex() {
        return regex;
    }

    public boolean getActive() {
        return this.active;
    }

    public Map<String, Object> activate() {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(1);

        final String active = "active";

        if (!this.active) {

            actualChanges.put(active, true);
            this.active = true;
        }

        return actualChanges;
    }

    public boolean isActive() {
        return this.active;
    }

    public void deActivate() {
        this.active = false;
    }

}