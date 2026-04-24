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
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.address.data.ClientAddressCreateResponse;
import org.apache.fineract.portfolio.address.data.ClientAddressUpdateResponse;
import org.apache.fineract.portfolio.address.domain.Address;
import org.apache.fineract.portfolio.address.domain.AddressRepository;
import org.apache.fineract.portfolio.address.exception.AddressNotFoundException;
import org.apache.fineract.portfolio.client.data.ClientAddressCreateRequest;
import org.apache.fineract.portfolio.client.data.ClientAddressUpdateRequest;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientAddress;
import org.apache.fineract.portfolio.client.domain.ClientAddressRepository;
import org.apache.fineract.portfolio.client.domain.ClientAddressRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;

@RequiredArgsConstructor
public class ClientAddressWriteServiceImpl implements ClientAddressWriteService {

    private final CodeValueRepository codeValueRepository;
    private final ClientAddressRepository clientAddressRepository;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final AddressRepository addressRepository;
    private final ClientAddressRepositoryWrapper clientAddressRepositoryWrapper;

    @Override
    public ClientAddressCreateResponse createClientAddress(ClientAddressCreateRequest request) {
        final CodeValue addressTypeIdCodeValue = codeValueRepository.getReferenceById(request.getAddressTypeId());
        final Client client = clientRepositoryWrapper.findOneWithNotFoundDetection(request.getClientId());

        CodeValue stateIdCodeValue = null;
        if (request.getStateProvinceId() != null) {
            stateIdCodeValue = codeValueRepository.getReferenceById(request.getStateProvinceId());
        }

        CodeValue countryIdCodeValue = null;
        if (request.getCountryId() != null) {
            countryIdCodeValue = codeValueRepository.getReferenceById(request.getCountryId());
        }

        final Address address = new Address();
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setAddressLine3(request.getAddressLine3());
        address.setTownVillage(request.getTownVillage());
        address.setCity(request.getCity());
        address.setCountyDistrict(request.getCountyDistrict());
        address.setStateProvince(stateIdCodeValue);
        address.setCountry(countryIdCodeValue);
        address.setPostalCode(request.getPostalCode());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        address.setCreatedBy(request.getCreatedBy());
        address.setUpdatedBy(request.getUpdatedBy());
        address.setCreatedOn(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()));
        address.setUpdatedOn(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()));
        addressRepository.save(address);

        boolean isActive = request.getIsActive() != null && request.getIsActive();
        final ClientAddress clientAddress = ClientAddress.create(isActive, client, address, addressTypeIdCodeValue);
        clientAddressRepository.saveAndFlush(clientAddress);

        return ClientAddressCreateResponse.builder().resourceId(clientAddress.getId()).build();
    }

    @Override
    public ClientAddressUpdateResponse updateClientAddress(ClientAddressUpdateRequest request) {
        final long addressId = request.getAddressId();

        final ClientAddress clientAddressObj = clientAddressRepositoryWrapper.findOneByClientIdAndAddressId(request.getClientId(),
                addressId);

        if (clientAddressObj == null) {
            throw new AddressNotFoundException(request.getClientId());
        }

        final Address addObj = addressRepository.getReferenceById(addressId);

        boolean isAddressUpdate = false;

        if (request.getAddressLine1() != null && !request.getAddressLine1().isEmpty()) {
            isAddressUpdate = true;
            addObj.setAddressLine1(request.getAddressLine1());
        }

        if (request.getAddressLine2() != null && !request.getAddressLine2().isEmpty()) {
            isAddressUpdate = true;
            addObj.setAddressLine2(request.getAddressLine2());
        }

        if (request.getAddressLine3() != null && !request.getAddressLine3().isEmpty()) {
            isAddressUpdate = true;
            addObj.setAddressLine3(request.getAddressLine3());
        }

        if (request.getTownVillage() != null && !request.getTownVillage().isEmpty()) {
            isAddressUpdate = true;
            addObj.setTownVillage(request.getTownVillage());
        }

        if (request.getCity() != null && !request.getCity().isEmpty()) {
            isAddressUpdate = true;
            addObj.setCity(request.getCity());
        }

        if (request.getCountyDistrict() != null && !request.getCountyDistrict().isEmpty()) {
            isAddressUpdate = true;
            addObj.setCountyDistrict(request.getCountyDistrict());
        }

        if (request.getStateProvinceId() != null && request.getStateProvinceId() != 0) {
            isAddressUpdate = true;
            CodeValue stateIdobj = codeValueRepository.getReferenceById(request.getStateProvinceId());
            addObj.setStateProvince(stateIdobj);
        }

        if (request.getCountryId() != null && request.getCountryId() != 0) {
            isAddressUpdate = true;
            CodeValue countryIdObj = codeValueRepository.getReferenceById(request.getCountryId());
            addObj.setCountry(countryIdObj);
        }

        if (request.getPostalCode() != null && !request.getPostalCode().isEmpty()) {
            isAddressUpdate = true;
            addObj.setPostalCode(request.getPostalCode());
        }

        if (request.getLatitude() != null) {
            isAddressUpdate = true;
            addObj.setLatitude(request.getLatitude());
        }

        if (request.getLongitude() != null) {
            isAddressUpdate = true;
            addObj.setLongitude(request.getLongitude());
        }

        if (isAddressUpdate) {
            addObj.setUpdatedOn(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()));
            addressRepository.save(addObj);
        }

        if (request.getIsActive() != null) {
            clientAddressObj.setIs_active(request.getIsActive());
        }

        return ClientAddressUpdateResponse.builder().resourceId(clientAddressObj.getId()).build();
    }

}
