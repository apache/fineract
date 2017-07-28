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
package org.apache.fineract.portfolio.address.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.address.data.FieldConfigurationData;
import org.apache.fineract.portfolio.address.service.FieldConfigurationReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class AddressCommandFromApiJsonDeserializer {
	private final FromJsonHelper fromApiJsonHelper;
	private final FieldConfigurationReadPlatformService readservice;

	@Autowired
	public AddressCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper,
			final FieldConfigurationReadPlatformService readservice) {
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.readservice = readservice;
	}

	public void validateForUpdate(final String json) {
		validate(json, false);
	}

	public void validateForCreate(final String json, final boolean fromNewClient) {
		validate(json, fromNewClient);
	}

	public void validate(final String json, final boolean fromNewClient) {
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();

		final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
				.resource("Address");

		final JsonElement element = this.fromApiJsonHelper.parse(json);
		Set<String> supportedParameters = new HashSet<>();

		final List<FieldConfigurationData> configurationData = new ArrayList<>(
				this.readservice.retrieveFieldConfigurationList("ADDRESS"));
		final List<String> enabledFieldList = new ArrayList<>();

		final Map<String, Boolean> madatoryFieldsMap = new HashMap<String, Boolean>();
		final Map<String, Boolean> enabledFieldsMap = new HashMap<String, Boolean>();
		final Map<String, String> regexFieldsMap = new HashMap<String, String>();

		// validate the json fields from the configuration data fields

		for (final FieldConfigurationData data : configurationData) {
			madatoryFieldsMap.put(data.getField(), data.isIs_mandatory());
			enabledFieldsMap.put(data.getField(), data.isIs_enabled());
			regexFieldsMap.put(data.getField(), data.getValidation_regex());
			if (data.isIs_enabled()) {
				enabledFieldList.add(data.getField());
			}
		}
		if (fromNewClient) {

			enabledFieldList.add("addressTypeId");
			enabledFieldList.add("locale");
			enabledFieldList.add("dateFormat");
			supportedParameters = new HashSet<>(enabledFieldList);
			// enabledFieldList.add("address");

			madatoryFieldsMap.put("addressTypeId", true);

		}
		if (!fromNewClient) {
			enabledFieldList.add("locale");
			enabledFieldList.add("dateFormat");
			enabledFieldList.add("addressId");
			madatoryFieldsMap.put("addressId", true);
			supportedParameters = new HashSet<>(enabledFieldList);
		}
		// final Set<String> supportedParameters = new
		// HashSet<>(enabledFieldList);
		this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

		final String street = this.fromApiJsonHelper.extractStringNamed("street", element);

		if (enabledFieldsMap.get("street")) {
			if (madatoryFieldsMap.get("street") && fromNewClient) {

				baseDataValidator.reset().parameter("street").value(street).notBlank();

			}
			if (!regexFieldsMap.get("street").isEmpty()) {
				baseDataValidator.reset().parameter("street").value(street)
						.matchesRegularExpression(regexFieldsMap.get("street"));
			}

		}
		final String addressLine1 = this.fromApiJsonHelper.extractStringNamed("addressLine1", element);
		if (enabledFieldsMap.get("addressLine1")) {
			if (madatoryFieldsMap.get("addressLine1") && fromNewClient) {
				baseDataValidator.reset().parameter("addressLine1").value(addressLine1).notBlank();
			}
			if (!regexFieldsMap.get("addressLine1").isEmpty()) {
				baseDataValidator.reset().parameter("addressLine1").value(addressLine1)
						.matchesRegularExpression(regexFieldsMap.get("addressLine1"));
			}

		}
		final String addressLine2 = this.fromApiJsonHelper.extractStringNamed("addressLine2", element);
		if (enabledFieldsMap.get("addressLine2")) {
			if (madatoryFieldsMap.get("addressLine2") && fromNewClient) {
				baseDataValidator.reset().parameter("addressLine2").value(addressLine2).notBlank();
			}
			if (!regexFieldsMap.get("addressLine2").isEmpty()) {
				baseDataValidator.reset().parameter("addressLine2").value(addressLine2)
						.matchesRegularExpression(regexFieldsMap.get("addressLine2"));
			}
		}
		final String addressLine3 = this.fromApiJsonHelper.extractStringNamed("addressLine3", element);
		if (enabledFieldsMap.get("addressLine3")) {
			if (madatoryFieldsMap.get("addressLine3") && fromNewClient) {
				baseDataValidator.reset().parameter("addressLine3").value(addressLine3).notBlank();
			}
			if (!regexFieldsMap.get("addressLine3").isEmpty()) {
				baseDataValidator.reset().parameter("addressLine3").value(addressLine3)
						.matchesRegularExpression(regexFieldsMap.get("addressLine3"));
			}
		}
		final String townVillage = this.fromApiJsonHelper.extractStringNamed("townVillage", element);
		if (enabledFieldsMap.get("townVillage")) {
			if (madatoryFieldsMap.get("townVillage") && fromNewClient) {
				baseDataValidator.reset().parameter("townVillage").value(townVillage).notBlank();
			}
			if (!regexFieldsMap.get("townVillage").isEmpty()) {
				baseDataValidator.reset().parameter("townVillage").value(townVillage)
						.matchesRegularExpression(regexFieldsMap.get("townVillage"));
			}
		}
		final String city = this.fromApiJsonHelper.extractStringNamed("city", element);

		if (enabledFieldsMap.get("city")) {
			if (madatoryFieldsMap.get("city") && fromNewClient) {
				baseDataValidator.reset().parameter("city").value(city).notBlank();
			}
			if (!regexFieldsMap.get("city").isEmpty()) {
				baseDataValidator.reset().parameter("city").value(city)
						.matchesRegularExpression(regexFieldsMap.get("city"));
			}
		}
		final String countyDistrict = this.fromApiJsonHelper.extractStringNamed("countyDistrict", element);
		if (enabledFieldsMap.get("countyDistrict")) {
			if (madatoryFieldsMap.get("countyDistrict") && fromNewClient) {
				baseDataValidator.reset().parameter("countyDistrict").value(countyDistrict).notBlank();
			}
			if (!regexFieldsMap.get("countyDistrict").isEmpty()) {
				baseDataValidator.reset().parameter("countyDistrict").value(countyDistrict)
						.matchesRegularExpression(regexFieldsMap.get("countyDistrict"));
			}
		}

		if (this.fromApiJsonHelper.extractLongNamed("stateProvinceId", element) != null) {

			final long stateProvinceId = this.fromApiJsonHelper.extractLongNamed("stateProvinceId", element);
			if (enabledFieldsMap.get("stateProvinceId")) {
				if (madatoryFieldsMap.get("stateProvinceId") && fromNewClient) {
					baseDataValidator.reset().parameter("stateProvinceId").value(stateProvinceId).notBlank();
				}
				if (!regexFieldsMap.get("stateProvinceId").isEmpty()) {
					baseDataValidator.reset().parameter("stateProvinceId").value(stateProvinceId)
							.matchesRegularExpression(regexFieldsMap.get("stateProvinceId"));
				}
			}
		}

		if (this.fromApiJsonHelper.extractLongNamed("countryId", element) != null) {
			final long countryId = this.fromApiJsonHelper.extractLongNamed("countryId", element);
			if (enabledFieldsMap.get("countryId")) {
				if (madatoryFieldsMap.get("countryId") && fromNewClient) {
					baseDataValidator.reset().parameter("countryId").value(countryId).notBlank();
				}
				if (!regexFieldsMap.get("countryId").isEmpty()) {
					baseDataValidator.reset().parameter("countryId").value(countryId)
							.matchesRegularExpression(regexFieldsMap.get("countryId"));
				}
			}
		}

		final String postalCode = this.fromApiJsonHelper.extractStringNamed("postalCode", element);
		if (enabledFieldsMap.get("postalCode")) {
			if (madatoryFieldsMap.get("postalCode") && fromNewClient) {
				baseDataValidator.reset().parameter("postalCode").value(postalCode).notBlank();
			}
			if (!regexFieldsMap.get("postalCode").isEmpty()) {
				baseDataValidator.reset().parameter("postalCode").value(postalCode)
						.matchesRegularExpression(regexFieldsMap.get("postalCode"));
			}
		}

		if (this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("latitude", element) != null) {
			final BigDecimal latitude = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("latitude", element);
			if (enabledFieldsMap.get("latitude")) {
				if (madatoryFieldsMap.get("latitude") && fromNewClient) {
					baseDataValidator.reset().parameter("latitude").value(latitude).notBlank();
				}
				if (!regexFieldsMap.get("latitude").isEmpty()) {
					baseDataValidator.reset().parameter("latitude").value(latitude)
							.matchesRegularExpression(regexFieldsMap.get("latitude"));
				}
			}
		}

		if (this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("longitude", element) != null) {
			final BigDecimal longitude = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("longitude", element);
			if (enabledFieldsMap.get("longitude")) {
				if (madatoryFieldsMap.get("longitude") && fromNewClient) {
					baseDataValidator.reset().parameter("longitude").value(longitude).notBlank();
				}
				if (!regexFieldsMap.get("longitude").isEmpty()) {
					baseDataValidator.reset().parameter("longitude").value(longitude)
							.matchesRegularExpression(regexFieldsMap.get("longitude"));
				}
			}
		}

		throwExceptionIfValidationWarningsExist(dataValidationErrors);

	}

	private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}

}
