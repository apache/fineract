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
package org.apache.fineract.portfolio.client.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientBusinessOwnerRepository;
import org.apache.fineract.portfolio.client.domain.ClientBusinessOwners;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ClientBusinessOwnerNotFoundException;
import org.apache.fineract.portfolio.client.exception.EmailAlreadyExistsException;
import org.apache.fineract.portfolio.client.serialization.ClientBusinessOwnerCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BusinessOwnerWritePlatformServiceImpl implements BusinessOwnerWritePlatformService {

    private final PlatformSecurityContext context;
    private final CodeValueRepository codeValueRepository;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final ClientBusinessOwnerRepository businessOwnerRepository;
    private final ClientBusinessOwnerCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private static final Logger LOG = LoggerFactory.getLogger(BusinessOwnerWritePlatformServiceImpl.class);

    @Override
    public CommandProcessingResult addBusinessOwner(final Long clientId, final JsonCommand command) {
        JsonObject jsonObject = command.parsedJson().getAsJsonObject();
        context.authenticatedUser();
        fromApiJsonDeserializer.validateForCreate(clientId, jsonObject.toString());
        
        ClientBusinessOwners data =  this.businessOwnerRepository.findByEmail(jsonObject.get("email").getAsString());
        if(data != null) {
        	throw new EmailAlreadyExistsException(data.getEmail());
        }
        final Client client = clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);

        ClientBusinessOwners businessOwner = createBusinessOwner(jsonObject, client);
        businessOwnerRepository.save(businessOwner);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(businessOwner.getId()).build();
    }

    @Override
    public CommandProcessingResult addNewBusinessOwner(final Client client, final JsonCommand command) {
        ClientBusinessOwners businessOwner = new ClientBusinessOwners();
        final JsonArray ownersArray = command.arrayOfParameterNamed("businessOwners");

        if (ownersArray != null) {
            for (int i = 0; i < ownersArray.size(); i++) {
                final JsonObject jsonObject = ownersArray.get(i).getAsJsonObject();

                fromApiJsonDeserializer.validateForCreate(client.getId(), jsonObject.toString());
                ClientBusinessOwners data =  this.businessOwnerRepository.findByEmail(jsonObject.get("email").getAsString());
                if(data != null) {
                	throw new EmailAlreadyExistsException(data.getEmail());
                }else {
                  businessOwner = createBusinessOwner(jsonObject, client);
                  businessOwnerRepository.save(businessOwner);
                }
            }
        }

        // This is confusing because only the last client address id is returned

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(businessOwner.getId()).build();
    }

    private ClientBusinessOwners createBusinessOwner(JsonObject jsonObject, Client client) {

        CodeValue stateIdCodeValue = null;
        long stateId = jsonObject.get("stateProvinceId").getAsLong();
        stateIdCodeValue = codeValueRepository.getById(stateId);

        CodeValue countryIdCodeValue = null;
        long countryId = jsonObject.get("countryId").getAsLong();
        countryIdCodeValue = codeValueRepository.getById(countryId);

        ClientBusinessOwners owner = ClientBusinessOwners.fromJsonObject(jsonObject, client, stateIdCodeValue, countryIdCodeValue);
        owner.setCreatedOn(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()));
        owner.setUpdatedOn(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()));
        return owner;
    }

    @Override
    public CommandProcessingResult updateBusinessOwner(Long businessOwnerId, JsonCommand command) {

        String firstName = "";
        String middleName = "";
        BigDecimal ownership = null;
        String title = "";
        String email = "";
        Date dateOfBirth = null;
        String mobileNumber = "";
        String alterMobileNumber = "";
        String username = "";
        Boolean isActive = false;
        String streetNumberAndName = "";
        String lga = "";
        String city = "";
        String bvn = "";
        long stateId;
        long countryId;
        CodeValue stateIdobj;
        CodeValue countryIdObj;

        boolean is_owner_update = false;

        this.context.authenticatedUser();

        fromApiJsonDeserializer.validateForUpdate(businessOwnerId, command.json());

        ClientBusinessOwners clientBusinessOwner = this.businessOwnerRepository.findById(businessOwnerId)
                .orElseThrow(() -> new ClientBusinessOwnerNotFoundException(businessOwnerId));

        if (command.stringValueOfParameterNamed("firstName") != null) {
            firstName = command.stringValueOfParameterNamed("firstName");
            clientBusinessOwner.setFirstName(firstName);
            is_owner_update = true;
        }

        if (command.stringValueOfParameterNamed("middleName") != null) {
            middleName = command.stringValueOfParameterNamed("middleName");
            clientBusinessOwner.setTitle(middleName);
            is_owner_update = true;
        }

        if (command.stringValueOfParameterNamed("title") != null) {
            title = command.stringValueOfParameterNamed("title");
            clientBusinessOwner.setTitle(title);
            is_owner_update = true;
        }

        if (command.stringValueOfParameterNamed("ownership") != null) {
            ownership = command.bigDecimalValueOfParameterNamed("ownership");
            clientBusinessOwner.setOwnership(ownership);
            is_owner_update = true;
        }

        if (command.stringValueOfParameterNamed("email") != null) {
            email = command.stringValueOfParameterNamed("email");
            clientBusinessOwner.setEmail(email);
            is_owner_update = true;
        }

        if (command.stringValueOfParameterNamed("mobileNumber") != null) {
            mobileNumber = command.stringValueOfParameterNamed("mobileNumber");
            clientBusinessOwner.setMobileNumber(mobileNumber);
            is_owner_update = true;
        }

        if (command.stringValueOfParameterNamed("alterMobileNumber") != null) {
            alterMobileNumber = command.stringValueOfParameterNamed("alterMobileNumber");
            clientBusinessOwner.setAlterMobileNumber(alterMobileNumber);
            is_owner_update = true;
        }

        if (command.booleanObjectValueOfParameterNamed("isActive") != null) {
            isActive = command.booleanObjectValueOfParameterNamed("isActive");
            clientBusinessOwner.setActive(isActive);
            is_owner_update = true;
        }

        if (command.stringValueOfParameterNamed("streetNumberAndName") != null) {
            streetNumberAndName = command.stringValueOfParameterNamed("streetNumberAndName");
            clientBusinessOwner.setStreetNumberAndName(streetNumberAndName);
            is_owner_update = true;
        }

        if (command.stringValueOfParameterNamed("username") != null) {
            alterMobileNumber = command.stringValueOfParameterNamed("username");
            clientBusinessOwner.setUsername(username);
            is_owner_update = true;
        }

        if (command.dateValueOfParameterNamed("dateOfBirth") != null) {
            dateOfBirth = command.dateValueOfParameterNamedFromLocalDate("dateOfBirth");
            clientBusinessOwner.setDateOfBirth(dateOfBirth);
            is_owner_update = true;

        }

        if (command.stringValueOfParameterNamed("lga") != null) {
            lga = command.stringValueOfParameterNamed("lga");
            clientBusinessOwner.setLga(lga);
            is_owner_update = true;
        }

        if (command.stringValueOfParameterNamed("bvn") != null) {
            bvn = command.stringValueOfParameterNamed("bvn");
            clientBusinessOwner.setBvn(bvn);
            is_owner_update = true;
        }

        if (command.stringValueOfParameterNamed("city") != null) {
            city = command.stringValueOfParameterNamed("city");
            clientBusinessOwner.setCity(city);
            is_owner_update = true;
        }

        if (command.longValueOfParameterNamed("stateProvinceId") != null) {
            if (command.longValueOfParameterNamed("stateProvinceId") != 0) {
                is_owner_update = true;
                stateId = command.longValueOfParameterNamed("stateProvinceId");
                stateIdobj = this.codeValueRepository.getById(stateId);
                clientBusinessOwner.setStateProvince(stateIdobj);
            }

        }
        if (command.longValueOfParameterNamed("countryId") != null) {
            if (command.longValueOfParameterNamed("countryId") != 0) {
                is_owner_update = true;
                countryId = command.longValueOfParameterNamed("countryId");
                countryIdObj = this.codeValueRepository.getById(countryId);
                clientBusinessOwner.setCountry(countryIdObj);
            }

        }

        if (is_owner_update) {
            clientBusinessOwner.setUpdatedOn(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()));
            this.businessOwnerRepository.save(clientBusinessOwner);

        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(clientBusinessOwner.getId()).build();
    }

    @Override
    public ClientBusinessOwners updateBusinessOwnerStatus(Long businessOwnerId, Boolean status) {

        this.context.authenticatedUser();

        ClientBusinessOwners clientBusinessOwner = this.businessOwnerRepository.findById(businessOwnerId)
                .orElseThrow(() -> new ClientBusinessOwnerNotFoundException(businessOwnerId));

        if (status != null) {
            clientBusinessOwner.setActive(status);
        }

        clientBusinessOwner.setUpdatedOn(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()));
        this.businessOwnerRepository.save(clientBusinessOwner);

        return clientBusinessOwner;
    }

}
