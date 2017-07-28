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
package org.apache.fineract.infrastructure.hooks.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_hook_schema")
public class Schema extends AbstractPersistableCustom<Long> {

	@ManyToOne(optional = false)
	@JoinColumn(name = "hook_template_id", referencedColumnName = "id", nullable = false)
	private HookTemplate template;

	@Column(name = "field_type", nullable = false, length = 20)
	private String fieldType;

	@Column(name = "field_name", nullable = false, length = 100)
	private String fieldName;

	@Column(name = "placeholder", length = 100)
	private String placeholder;

	@Column(name = "optional", nullable = false)
	private boolean optional = false;

	public Schema() {
		//
	}

	public String getFieldName() {
		return this.fieldName;
	}

	public String getFieldType() {
		return this.fieldType;
	}

	public boolean isOptional() {
		return this.optional;
	}

}
