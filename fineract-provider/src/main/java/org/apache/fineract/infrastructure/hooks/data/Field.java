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
package org.apache.fineract.infrastructure.hooks.data;

@SuppressWarnings("unused")
public class Field {

	private final String fieldName;
	private final String fieldValue;
	private final String fieldType;
	private final Boolean optional;
	private final String placeholder;

	public static Field fromConfig(final String fieldName,
			final String fieldValue) {
		return new Field(null, fieldName, fieldValue, null, null);
	}

	public static Field fromSchema(final String fieldType,
			final String fieldName, final Boolean optional,
			final String placeholder) {
		return new Field(fieldType, fieldName, null, optional, placeholder);
	}

	private Field(final String fieldType, final String fieldName,
			final String fieldValue, final Boolean optional,
			final String placeholder) {
		this.fieldType = fieldType;
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
		this.optional = optional;
		this.placeholder = placeholder;
	}

	public String getFieldName() {
		return this.fieldName;
	}

	public String getFieldValue() {
		return this.fieldValue;
	}

	public String getFieldType() {
		return this.fieldType;
	}

}
