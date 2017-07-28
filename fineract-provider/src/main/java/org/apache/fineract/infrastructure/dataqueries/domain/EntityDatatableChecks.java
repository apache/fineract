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
package org.apache.fineract.infrastructure.dataqueries.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_entity_datatable_check")

public class EntityDatatableChecks extends AbstractPersistableCustom<Long> {

	@Column(name = "application_table_name", nullable = false)
	private String entity;

	@Column(name = "x_registered_table_name", nullable = false)
	private String datatableName;

	@Column(name = "status_enum", nullable = false)
	private Long status;

	@Column(name = "system_defined")
	private boolean systemDefined = false;

	@Column(name = "product_id", nullable = true)
	private Long productId;

	public EntityDatatableChecks() {
	}

	public EntityDatatableChecks(final String entity, final String datatableName, final Long status,
			final boolean systemDefined, final Long productId) {

		this.entity = entity;
		this.status = status;
		this.datatableName = datatableName;
		this.systemDefined = systemDefined;
		this.productId = productId;
	}

	public static EntityDatatableChecks fromJson(final JsonCommand command) {

		final String entity = command.stringValueOfParameterNamed("entity");
		final Long status = command.longValueOfParameterNamed("status");
		final String datatableName = command.stringValueOfParameterNamed("datatableName");

		boolean systemDefined = false;
		if (command.parameterExists("systemDefined")) {
			systemDefined = command.booleanObjectValueOfParameterNamed("systemDefined");
		} else {
			systemDefined = false;
		}

		Long productId = null;
		if (command.parameterExists("productId")) {
			productId = command.longValueOfParameterNamed("productId");
		}

		return new EntityDatatableChecks(entity, datatableName, status, systemDefined, productId);

	}

	public String getEntity() {
		return this.entity;
	}

	public Long getStatus() {
		return this.status;
	}

	public String getDatatableName() {
		return this.datatableName;
	}

	public boolean isSystemDefined() {
		return this.systemDefined;
	}

	public Long getProductId() {
		return productId;
	}
}
