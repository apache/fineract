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
package org.apache.fineract.portfolio.address.data;

public class FieldConfigurationData {
	private final long fieldConfigurationId;

	private final String entity;

	private final String subentity;

	private final String field;

	private final boolean is_enabled;

	private final boolean is_mandatory;

	private final String validation_regex;

	private FieldConfigurationData(final long fieldConfigurationId, final String entity, final String subentity,
			final String field, final boolean is_enabled, final boolean is_mandatory, final String validation_regex) {
		this.fieldConfigurationId = fieldConfigurationId;
		this.entity = entity;
		this.subentity = subentity;
		this.field = field;
		this.is_enabled = is_enabled;
		this.is_mandatory = is_mandatory;
		this.validation_regex = validation_regex;
	}

	public static FieldConfigurationData instance(final long fieldConfigurationId, final String entity,
			final String subentity, final String field, final boolean is_enabled, final boolean is_mandatory,
			final String validation_regex) {
		return new FieldConfigurationData(fieldConfigurationId, entity, subentity, field, is_enabled, is_mandatory,
				validation_regex);
	}

	public long getFieldConfigurationId() {
		return this.fieldConfigurationId;
	}

	public String getEntity() {
		return this.entity;
	}

	public String getSubEntity() {
		return this.subentity;
	}

	public String getField() {
		return this.field;
	}

	public boolean isIs_enabled() {
		return this.is_enabled;
	}

	public boolean isIs_mandatory() {
		return this.is_mandatory;
	}

	public String getValidation_regex() {
		return this.validation_regex;
	}

}
