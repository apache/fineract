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
package org.apache.fineract.portfolio.address.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_field_configuration")
public class FieldConfiguration extends AbstractPersistableCustom<Long> {

	private String entity;

	private String table;

	private String field;

	private boolean is_enabled;

	public FieldConfiguration() {

	}

	private FieldConfiguration(final String entity, final String table, final String field, final boolean is_enabled) {
		this.entity = entity;
		this.table = table;
		this.field = field;
		this.is_enabled = is_enabled;

	}

	private static FieldConfiguration fromJson(final JsonCommand command) {
		final String entity = command.stringValueOfParameterNamed("entity");
		final String table = command.stringValueOfParameterNamed("table");
		final String field = command.stringValueOfParameterNamed("field");
		final boolean is_enabled = command.booleanPrimitiveValueOfParameterNamed("implementationKey");

		return new FieldConfiguration(entity, table, field, is_enabled);
	}
}
