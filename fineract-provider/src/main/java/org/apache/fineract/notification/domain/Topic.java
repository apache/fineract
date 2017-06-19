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
package org.apache.fineract.notification.domain;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.useradministration.domain.Permission;

@Entity
@Table(name = "topic")
public class Topic extends AbstractPersistableCustom<Long> {
	
	
	@Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;
	
	@OneToOne
	@JoinColumn(name = "permission_id")
	private Permission permission;
	
	@Column(name = "activation_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date activationDate;
	
	@Column(name = "is_active", nullable = false)
    private Boolean active;
	
	public static Topic fromJson(final Permission permission, final JsonCommand command) {

        final String name = command.stringValueOfParameterNamed("name");
        boolean isActive = true;
        if (command.hasParameter("active")) {
        	isActive = command.booleanPrimitiveValueOfParameterNamed("active");
        }
        Date localActivationDate = new Date();
        if (command.hasParameter("activationDate")) {
        	localActivationDate = command.DateValueOfParameterNamed("activationDate");
        }
        return new Topic(name, permission, localActivationDate, isActive);
    }

	public Topic() {
	}

	public Topic(String name, Permission permission, Date activationDate, Boolean isActive) {
		
		if (StringUtils.isNotBlank(name)) {
            this.name = name.trim();
        } else {
            this.name = null;
        }
		this.permission = permission;
		if (activationDate != null) {
			this.activationDate = activationDate;
		} else {
			this.activationDate = new Date();
		}
		this.active = isActive;
		
	}
	
	public Map<String, Object> update(final JsonCommand command) {
		final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

		final String nameParamName = "name";
        if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            this.name = newValue;
        }

        final String permissionIdParamName = "permissionId";
        if (command.isChangeInLongParameterNamed(permissionIdParamName, this.permission.getId())) {
            final Long newValue = command.longValueOfParameterNamed(permissionIdParamName);
            actualChanges.put(permissionIdParamName, newValue);
        }

        return actualChanges;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Permission getPermission() {
		return this.permission;
	}

	public void setPermission(Permission permission) {
		this.permission = permission;
	}

	public Date getActivationDate() {
		return this.activationDate;
	}

	public void setActivationDate(Date activationDate) {
		this.activationDate = activationDate;
	}

	public Boolean getActive() {
		return this.active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
	
}
