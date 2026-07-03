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
package org.apache.fineract.portfolio.address.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.address.data.AddressCreateRequest;
import org.apache.fineract.portfolio.address.data.AddressCreateResponse;
import org.apache.fineract.portfolio.address.data.AddressUpdateRequest;
import org.apache.fineract.portfolio.address.data.AddressUpdateResponse;
import org.apache.fineract.portfolio.address.data.FieldConfigurationData;
import org.apache.fineract.portfolio.address.domain.Address;
import org.apache.fineract.portfolio.address.domain.AddressRepository;
import org.apache.fineract.portfolio.address.exception.AddressNotFoundException;
import org.apache.fineract.portfolio.address.mapper.AddressMapper;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientAddress;
import org.apache.fineract.portfolio.client.domain.ClientAddressRepository;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnMissingBean(value = AddressDomainService.class, ignored = AddressDomainServiceImpl.class)
public class AddressDomainServiceImpl implements AddressDomainService {

    private static final String FIELD_CONFIGURATION_ENTITY = "ADDRESS";
    private static final String ADDRESS_TYPE_FIELD = "addressType";
    // Accepted even when their field configuration is disabled, as in the legacy validator.
    private static final Set<String> ALWAYS_SUPPORTED_PARAMETERS = Set.of(AddressCreateRequest.Fields.street,
            AddressCreateRequest.Fields.townVillage, AddressCreateRequest.Fields.countyDistrict);

    private final Validator validator;
    private final FieldConfigurationReadPlatformService fieldConfigurationReadPlatformService;
    private final AddressRepository addressRepository;
    private final ClientAddressRepository clientAddressRepository;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final CodeValueRepository codeValueRepository;
    private final AddressMapper addressMapper;

    @Override
    @Transactional
    public AddressCreateResponse create(final AddressCreateRequest request) {
        validate(request);

        final Client client = clientRepositoryWrapper.findOneWithNotFoundDetection(request.getClientId());
        final CodeValue addressTypeIdCodeValue = codeValueRepository.getReferenceById(request.getAddressTypeId());

        CodeValue stateIdCodeValue = null;
        if (request.getStateProvinceId() != null) {
            stateIdCodeValue = codeValueRepository.getReferenceById(request.getStateProvinceId());
        }

        CodeValue countryIdCodeValue = null;
        if (request.getCountryId() != null) {
            countryIdCodeValue = codeValueRepository.getReferenceById(request.getCountryId());
        }

        final Address address = addressMapper.toAddress(request);
        address.setStateProvince(stateIdCodeValue);
        address.setCountry(countryIdCodeValue);
        address.setCreatedOn(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()));
        address.setUpdatedOn(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()));
        addressRepository.save(address);

        final boolean isActive = Boolean.TRUE.equals(request.getIsActive());
        final ClientAddress clientAddress = ClientAddress.fromJson(isActive, client, address, addressTypeIdCodeValue);
        clientAddressRepository.saveAndFlush(clientAddress);

        return AddressCreateResponse.builder().resourceId(clientAddress.getId()).clientId(client.getId()).build();
    }

    @Override
    @Transactional
    public AddressUpdateResponse update(final AddressUpdateRequest request) {
        validate(request);

        final ClientAddress clientAddress = clientAddressRepository.findByClientIdAndAddressId(request.getClientId(),
                request.getAddressId());
        if (clientAddress == null) {
            throw new AddressNotFoundException(request.getClientId());
        }

        final Address address = clientAddress.getAddress();
        addressMapper.updateAddressFromRequest(request, address);
        if (request.getStateProvinceId() != null) {
            address.setStateProvince(codeValueRepository.getReferenceById(request.getStateProvinceId()));
        }
        if (request.getCountryId() != null) {
            address.setCountry(codeValueRepository.getReferenceById(request.getCountryId()));
        }
        address.setUpdatedOn(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()));

        if (request.getIsActive() != null) {
            clientAddress.setIs_active(request.getIsActive());
        }

        return AddressUpdateResponse.builder().resourceId(clientAddress.getId()).clientId(request.getClientId()).build();
    }

    // Both create paths (the REST endpoint and client creation) call this service directly or through the dispatcher,
    // so the request is validated here rather than on the REST resource.
    private void validate(final AddressCreateRequest request) {
        final Set<ConstraintViolation<AddressCreateRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        validateAgainstFieldConfiguration(parameterValues(request), true);
    }

    private void validate(final AddressUpdateRequest request) {
        final Set<ConstraintViolation<AddressUpdateRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        // Like the legacy update validator: mandatory fields are not enforced, a field not sent keeps its value.
        validateAgainstFieldConfiguration(parameterValues(request), false);
    }

    // Applies the tenant's ADDRESS field configuration (m_field_configuration): a disabled field must not be sent, a
    // mandatory field must not be blank (when enforceMandatory is set) and a configured regex must match.
    private void validateAgainstFieldConfiguration(final Map<String, Object> values, final boolean enforceMandatory) {
        final List<String> unsupportedParameters = new ArrayList<>();
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("Address");

        for (final FieldConfigurationData fieldConfiguration : fieldConfigurationReadPlatformService
                .retrieveFieldConfigurationList(FIELD_CONFIGURATION_ENTITY)) {
            final String parameter = ADDRESS_TYPE_FIELD.equals(fieldConfiguration.field()) ? AddressCreateRequest.Fields.addressTypeId
                    : fieldConfiguration.field();
            if (!values.containsKey(parameter)) {
                // createdOn and updatedOn are set by the server and are not part of the request, and the address type
                // cannot be changed on update
                continue;
            }
            final Object value = values.get(parameter);
            if (!fieldConfiguration.isEnabled()) {
                if (value != null && !ALWAYS_SUPPORTED_PARAMETERS.contains(parameter)) {
                    unsupportedParameters.add(parameter);
                }
                continue;
            }
            if (enforceMandatory && fieldConfiguration.isMandatory()) {
                baseDataValidator.reset().parameter(parameter).value(value).notBlank();
            }
            if (StringUtils.isNotEmpty(fieldConfiguration.validationRegex())) {
                baseDataValidator.reset().parameter(parameter).value(value).matchesRegularExpression(fieldConfiguration.validationRegex());
            }
        }

        if (!unsupportedParameters.isEmpty()) {
            throw new UnsupportedParameterException(unsupportedParameters);
        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    private static Map<String, Object> parameterValues(final AddressCreateRequest request) {
        final Map<String, Object> values = new HashMap<>();
        values.put(AddressCreateRequest.Fields.addressTypeId, request.getAddressTypeId());
        values.put(AddressCreateRequest.Fields.street, request.getStreet());
        values.put(AddressCreateRequest.Fields.addressLine1, request.getAddressLine1());
        values.put(AddressCreateRequest.Fields.addressLine2, request.getAddressLine2());
        values.put(AddressCreateRequest.Fields.addressLine3, request.getAddressLine3());
        values.put(AddressCreateRequest.Fields.townVillage, request.getTownVillage());
        values.put(AddressCreateRequest.Fields.city, request.getCity());
        values.put(AddressCreateRequest.Fields.countyDistrict, request.getCountyDistrict());
        values.put(AddressCreateRequest.Fields.stateProvinceId, request.getStateProvinceId());
        values.put(AddressCreateRequest.Fields.countryId, request.getCountryId());
        values.put(AddressCreateRequest.Fields.postalCode, request.getPostalCode());
        values.put(AddressCreateRequest.Fields.latitude, request.getLatitude());
        values.put(AddressCreateRequest.Fields.longitude, request.getLongitude());
        values.put(AddressCreateRequest.Fields.isActive, request.getIsActive());
        values.put(AddressCreateRequest.Fields.createdBy, request.getCreatedBy());
        values.put(AddressCreateRequest.Fields.updatedBy, request.getUpdatedBy());
        return values;
    }

    private static Map<String, Object> parameterValues(final AddressUpdateRequest request) {
        final Map<String, Object> values = new HashMap<>();
        values.put(AddressUpdateRequest.Fields.street, request.getStreet());
        values.put(AddressUpdateRequest.Fields.addressLine1, request.getAddressLine1());
        values.put(AddressUpdateRequest.Fields.addressLine2, request.getAddressLine2());
        values.put(AddressUpdateRequest.Fields.addressLine3, request.getAddressLine3());
        values.put(AddressUpdateRequest.Fields.townVillage, request.getTownVillage());
        values.put(AddressUpdateRequest.Fields.city, request.getCity());
        values.put(AddressUpdateRequest.Fields.countyDistrict, request.getCountyDistrict());
        values.put(AddressUpdateRequest.Fields.stateProvinceId, request.getStateProvinceId());
        values.put(AddressUpdateRequest.Fields.countryId, request.getCountryId());
        values.put(AddressUpdateRequest.Fields.postalCode, request.getPostalCode());
        values.put(AddressUpdateRequest.Fields.latitude, request.getLatitude());
        values.put(AddressUpdateRequest.Fields.longitude, request.getLongitude());
        values.put(AddressUpdateRequest.Fields.isActive, request.getIsActive());
        return values;
    }
}
