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

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.address.data.AddressCreateRequest;
import org.apache.fineract.portfolio.address.data.AddressCreateResponse;
import org.apache.fineract.portfolio.address.data.AddressUpdateRequest;
import org.apache.fineract.portfolio.address.data.AddressUpdateResponse;
import org.apache.fineract.portfolio.address.domain.Address;
import org.apache.fineract.portfolio.address.domain.AddressRepository;
import org.apache.fineract.portfolio.address.exception.AddressNotFoundException;
import org.apache.fineract.portfolio.address.mapper.AddressMapper;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientAddress;
import org.apache.fineract.portfolio.client.domain.ClientAddressRepository;
import org.apache.fineract.portfolio.client.domain.ClientAddressRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnMissingBean(value = AddressDomainService.class, ignored = AddressDomainServiceImpl.class)
public class AddressDomainServiceImpl implements AddressDomainService {

    private final AddressRepository addressRepository;
    private final ClientAddressRepository clientAddressRepository;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final CodeValueRepository codeValueRepository;
    private final AddressMapper addressMapper;
    private final ClientAddressRepositoryWrapper clientAddressRepositoryWrapper;

    @Override
    @Transactional
    public AddressCreateResponse create(final AddressCreateRequest request) {
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

        final ClientAddress clientAddress = clientAddressRepositoryWrapper.findOneByClientIdAndAddressId(request.getClientId(),
                request.getAddressId());
        if (clientAddress == null) {
            throw new AddressNotFoundException(request.getClientId());
        }

        final Address address = addressRepository.getReferenceById(request.getAddressId());
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
}
