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
package org.apache.fineract.useradministration.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_password_validation_policy")
public class PasswordValidationPolicy extends AbstractPersistableCustom<Long> {

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