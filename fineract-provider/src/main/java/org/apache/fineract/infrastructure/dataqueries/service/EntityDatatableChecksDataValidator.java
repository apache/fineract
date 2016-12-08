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
package org.apache.fineract.infrastructure.dataqueries.service;

import java.lang.reflect.Type;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class EntityDatatableChecksDataValidator {

	/**
	 * The parameters supported for this command.
	 */
	private final Set<String> supportedParameters = new HashSet<>(
			Arrays.asList("entity", "datatableName", "status", "systemDefined", "productId"));

	private final FromJsonHelper fromApiJsonHelper;

	@Autowired
	public EntityDatatableChecksDataValidator(final FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}

	public void validateForCreate(final String json) {
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
				.resource("entityDatatableChecks");

		final JsonElement element = this.fromApiJsonHelper.parse(json);

		final String entity = this.fromApiJsonHelper.extractStringNamed("entity", element);
		baseDataValidator.reset().parameter("entity").value(entity).notBlank()
				.isOneOfTheseStringValues(EntityTables.getEntitiesList());

		final Integer status = this.fromApiJsonHelper.extractIntegerSansLocaleNamed("status", element);
		final Object[] entityTablesStatuses = EntityTables.getStatus(entity);

		baseDataValidator.reset().parameter("status").value(status).isOneOfTheseValues(entityTablesStatuses);

		final String datatableName = this.fromApiJsonHelper.extractStringNamed("datatableName", element);
		baseDataValidator.reset().parameter("datatableName").value(datatableName).notBlank();

		if (this.fromApiJsonHelper.parameterExists("systemDefined", element)) {
			final String systemDefined = this.fromApiJsonHelper.extractStringNamed("systemDefined", element);
			baseDataValidator.reset().parameter("systemDefined").value(systemDefined).validateForBooleanValue();
		}

		if (this.fromApiJsonHelper.parameterExists("productId", element)) {
			final long productId = this.fromApiJsonHelper.extractLongNamed("productId", element);
			baseDataValidator.reset().parameter("productId").value(productId).integerZeroOrGreater();
		}

		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}

	private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException(dataValidationErrors);
		}
	}
}