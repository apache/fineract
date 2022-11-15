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

package org.apache.fineract.portfolio.client.serialization;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class ClientBusinessOwnerCommandFromApiJsonDeserializer {

    private final FromJsonHelper fromApiJsonHelper;
    private final Set<String> supportedParameters = new HashSet<>(
            Arrays.asList("id", "clientId", "firstName", "titleId", "middleName", "lastName", "ownership", "email", "mobileNumber",
                    "businessOwnerNumber", "cityId", "streetNumberAndName", "address1", "address2", "address3", "postalCode", "landmark",
                    "typeId", "stateProvinceId", "countryId", "bvn", "nin", "locale", "dateFormat", "isActive"));

    @Autowired
    public ClientBusinessOwnerCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final long clientId, String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("BusinessOwners");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        baseDataValidator.reset().value(clientId).notBlank().integerGreaterThanZero();

        final String firstName = this.fromApiJsonHelper.extractStringNamed("firstName", element);
        baseDataValidator.reset().parameter("firstName").value(firstName).notNull().notBlank().notExceedingLengthOf(100);

        final String lastName = this.fromApiJsonHelper.extractStringNamed("lastName", element);
        baseDataValidator.reset().parameter("lastName").value(lastName).notNull().notBlank().notExceedingLengthOf(100);

        final Long titleId = this.fromApiJsonHelper.extractLongNamed("titleId", element);
        baseDataValidator.reset().parameter("titleId").value(titleId).notNull().integerGreaterThanZero();

        if (this.fromApiJsonHelper.extractStringNamed("ownership", element) != null) {
            final BigDecimal ownership = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("ownership", element);
            baseDataValidator.reset().parameter("ownership").value(ownership).notNull().positiveAmount();

            if (this.fromApiJsonHelper.extractStringNamed("middleName", element) != null) {
                final String middleName = this.fromApiJsonHelper.extractStringNamed("middleName", element);
                baseDataValidator.reset().parameter("middleName").value(middleName).notNull().notBlank().notExceedingLengthOf(100);

            }

            final String email = this.fromApiJsonHelper.extractStringNamed("email", element);
            baseDataValidator.reset().parameter("email").value(email).notNull().notBlank().notExceedingLengthOf(50);

            final String mobileNumber = this.fromApiJsonHelper.extractStringNamed("mobileNumber", element);
            baseDataValidator.reset().parameter("mobileNumber").value(mobileNumber).notNull().notBlank().notExceedingLengthOf(100);

            if (this.fromApiJsonHelper.extractStringNamed("businessOwnerNumber", element) != null) {
                final String businessOwnerNumber = this.fromApiJsonHelper.extractStringNamed("businessOwnerNumber", element);
                baseDataValidator.reset().parameter("businessOwnerNumber").value(businessOwnerNumber).notNull().notBlank()
                        .notExceedingLengthOf(100);
            }

            if (this.fromApiJsonHelper.extractStringNamed("address1", element) != null) {
                final String address1 = this.fromApiJsonHelper.extractStringNamed("address1", element);
                baseDataValidator.reset().parameter("address1").value(address1).notNull().notBlank().notExceedingLengthOf(100);
            }

            if (this.fromApiJsonHelper.extractStringNamed("address2", element) != null) {
                final String address2 = this.fromApiJsonHelper.extractStringNamed("address2", element);
                baseDataValidator.reset().parameter("address2").value(address2).notNull().notBlank().notExceedingLengthOf(100);
            }

            if (this.fromApiJsonHelper.extractStringNamed("address3", element) != null) {
                final String address3 = this.fromApiJsonHelper.extractStringNamed("address3", element);
                baseDataValidator.reset().parameter("address3").value(address3).notNull().notBlank().notExceedingLengthOf(100);
            }

            if (this.fromApiJsonHelper.extractStringNamed("postalCode", element) != null) {
                final String postalCode = this.fromApiJsonHelper.extractStringNamed("postalCode", element);
                baseDataValidator.reset().parameter("postalCode").value(postalCode).notNull().notBlank().notExceedingLengthOf(100);
            }

            if (this.fromApiJsonHelper.extractStringNamed("alterMobileNumber", element) != null) {
                final String alterMobileNumber = this.fromApiJsonHelper.extractStringNamed("alterMobileNumber", element);
                baseDataValidator.reset().parameter("alterMobileNumber").value(alterMobileNumber).notNull().notBlank()
                        .notExceedingLengthOf(100);
            }

            final LocalDate dateOfBirth = this.fromApiJsonHelper.extractLocalDateNamed("dateOfBirth", element);
            baseDataValidator.reset().parameter("dateOfBirth").value(dateOfBirth).value(dateOfBirth).notNull()
                    .validateDateBefore(DateUtils.getLocalDateOfTenant());

            final String street = this.fromApiJsonHelper.extractStringNamed("streetNumberAndName", element);
            baseDataValidator.reset().parameter("streetNumberAndName").value(street).notNull().notBlank().notExceedingLengthOf(100);

            if (this.fromApiJsonHelper.extractStringNamed("nin", element) != null) {
                final String bvn = this.fromApiJsonHelper.extractStringNamed("nin", element);
                baseDataValidator.reset().parameter("nin").value(bvn).notNull().notBlank().notExceedingLengthOf(100);
            }

            if (this.fromApiJsonHelper.extractLongNamed("typeId", element) != null) {
                final Long tyepId = this.fromApiJsonHelper.extractLongNamed("typeId", element);
                baseDataValidator.reset().parameter("typeId").value(tyepId).notNull().integerGreaterThanZero();
            }

            final Long cityId = this.fromApiJsonHelper.extractLongNamed("cityId", element);
            baseDataValidator.reset().parameter("cityId").value(cityId).notNull().integerGreaterThanZero();

            final String lga = this.fromApiJsonHelper.extractStringNamed("lga", element);
            baseDataValidator.reset().parameter("lga").value(lga).notNull().notBlank().notExceedingLengthOf(100);

            if (this.fromApiJsonHelper.extractStringNamed("bvn", element) != null) {
                final String bvn = this.fromApiJsonHelper.extractStringNamed("bvn", element);
                baseDataValidator.reset().parameter("bvn").value(bvn).notNull().notBlank().notExceedingLengthOf(100);
            }

            final Long stateProvinceId = this.fromApiJsonHelper.extractLongNamed("stateProvinceId", element);
            baseDataValidator.reset().parameter("stateProvinceId").value(stateProvinceId).notNull().integerGreaterThanZero();

            final Long countryId = this.fromApiJsonHelper.extractLongNamed("countryId", element);
            baseDataValidator.reset().parameter("countryId").value(countryId).notNull().integerGreaterThanZero();

            if (this.fromApiJsonHelper.extractBooleanNamed("isActive", element) != null) {
                final Boolean isActive = this.fromApiJsonHelper.extractBooleanNamed("isActive", element);
                baseDataValidator.reset().parameter("isActive").value(isActive).notNull().notBlank().notExceedingLengthOf(100);
            }

            throwExceptionIfValidationWarningsExist(dataValidationErrors);

        }

    }

    public void validateForUpdate(final long clientId, String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("BusinessOwners");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        baseDataValidator.reset().value(clientId).notBlank().integerGreaterThanZero();

        if (this.fromApiJsonHelper.extractStringNamed("firstName", element) != null) {
            final String firstName = this.fromApiJsonHelper.extractStringNamed("firstName", element);
            baseDataValidator.reset().parameter("firstName").value(firstName).notNull().notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed("lastName", element) != null) {
            final String lastName = this.fromApiJsonHelper.extractStringNamed("lastName", element);
            baseDataValidator.reset().parameter("lastName").value(lastName).notNull().notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed("middleName", element) != null) {
            final String middleName = this.fromApiJsonHelper.extractStringNamed("middleName", element);
            baseDataValidator.reset().parameter("middleName").value(middleName).notNull().notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed("email", element) != null) {
            final String email = this.fromApiJsonHelper.extractStringNamed("email", element);
            baseDataValidator.reset().parameter("email").value(email).notNull().notBlank().notExceedingLengthOf(50);
        }

        if (this.fromApiJsonHelper.extractStringNamed("mobileNumber", element) != null) {
            final String mobileNumber = this.fromApiJsonHelper.extractStringNamed("mobileNumber", element);
            baseDataValidator.reset().parameter("mobileNumber").value(mobileNumber).notNull().notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed("alterMobileNumber", element) != null) {
            final String alterMobileNumber = this.fromApiJsonHelper.extractStringNamed("alterMobileNumber", element);
            baseDataValidator.reset().parameter("alterMobileNumber").value(alterMobileNumber).notNull().notBlank()
                    .notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed("dateOfBirth", element) != null) {
            final LocalDate dateOfBirth = this.fromApiJsonHelper.extractLocalDateNamed("dateOfBirth", element);
            baseDataValidator.reset().parameter("dateOfBirth").value(dateOfBirth).value(dateOfBirth).notNull()
                    .validateDateBefore(DateUtils.getLocalDateOfTenant());
        }

        if (this.fromApiJsonHelper.extractStringNamed("city", element) != null) {
            final String city = this.fromApiJsonHelper.extractStringNamed("city", element);
            baseDataValidator.reset().parameter("city").value(city).notNull().notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed("streetNumberAndName", element) != null) {
            final String street = this.fromApiJsonHelper.extractStringNamed("streetNumberAndName", element);
            baseDataValidator.reset().parameter("streetNumberAndName").value(street).notNull().notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed("landmark", element) != null) {
            final String landmark = this.fromApiJsonHelper.extractStringNamed("landmark", element);
            baseDataValidator.reset().parameter("landmark").value(landmark).notNull().notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed("lga", element) != null) {
            final String lga = this.fromApiJsonHelper.extractStringNamed("lga", element);
            baseDataValidator.reset().parameter("lga").value(lga).notNull().notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed("bvn", element) != null) {
            final String bvn = this.fromApiJsonHelper.extractStringNamed("bvn", element);
            baseDataValidator.reset().parameter("bvn").value(bvn).notNull().notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed("stateProvinceId", element) != null) {
            final Long stateProvinceId = this.fromApiJsonHelper.extractLongNamed("stateProvinceId", element);
            baseDataValidator.reset().parameter("stateProvinceId").value(stateProvinceId).notNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractStringNamed("countryId", element) != null) {
            final Long countryId = this.fromApiJsonHelper.extractLongNamed("countryId", element);
            baseDataValidator.reset().parameter("countryId").value(countryId).notNull().integerGreaterThanZero();
        }
        if (this.fromApiJsonHelper.extractBooleanNamed("isActive", element) != null) {
            final Boolean isActive = this.fromApiJsonHelper.extractBooleanNamed("isActive", element);
            baseDataValidator.reset().parameter("isActive").value(isActive).notNull().notBlank().notExceedingLengthOf(100);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

}
