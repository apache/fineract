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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.useradministration.data.RoleData;

@Entity
@Table(name = "m_role", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "unq_name") })
public class Role extends AbstractPersistableCustom<Long> {

    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "is_disabled", nullable = false)
    private Boolean disabled;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "m_role_permission", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new HashSet<>();

    public static Role fromJson(final JsonCommand command) {
        final String name = command.stringValueOfParameterNamed("name");
        final String description = command.stringValueOfParameterNamed("description");
        return new Role(name, description);
    }

    protected Role() {
        //
    }

    public Role(final String name, final String description) {
        this.name = name.trim();
        this.description = description.trim();
        this.disabled = false;
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String nameParamName = "name";
        if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            this.name = newValue;
        }

        final String descriptionParamName = "description";
        if (command.isChangeInStringParameterNamed(descriptionParamName, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            actualChanges.put(descriptionParamName, newValue);
            this.description = newValue;
        }

        return actualChanges;
    }

    public boolean updatePermission(final Permission permission, final boolean isSelected) {
        boolean changed = false;
        if (isSelected) {
            changed = addPermission(permission);
        } else {
            changed = removePermission(permission);
        }

        return changed;
    }

    private boolean addPermission(final Permission permission) {
        return this.permissions.add(permission);
    }

    private boolean removePermission(final Permission permission) {
        return this.permissions.remove(permission);
    }

    public Collection<Permission> getPermissions() {
        return this.permissions;
    }

    public boolean hasPermissionTo(final String permissionCode) {
        boolean match = false;
        for (final Permission permission : this.permissions) {
            if (permission.hasCode(permissionCode)) {
                match = true;
                break;
            }
        }
        return match;
    }

    public RoleData toData() {
        return new RoleData(getId(), this.name, this.description, this.disabled);
    }

    public String getName() {
    	return this.name;
    }
    
    public void disableRole() {
        this.disabled = true;
    }

    public Boolean isDisabled() {
        return this.disabled;
    }

    public void enableRole() {
        this.disabled = false;
    }

    public Boolean isEnabled() {
        return this.disabled;
    }
}