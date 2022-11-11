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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientFamilyMembers;
import org.apache.fineract.portfolio.client.domain.ClientFamilyMembersRepository;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.serialization.ClientFamilyMemberCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientFamilyMembersWritePlatformServiceImpl implements ClientFamilyMembersWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(ClientFamilyMembersWritePlatformServiceImpl.class);
    private final PlatformSecurityContext context;
    private final CodeValueRepository codeValueRepository;
    private final ClientFamilyMembersRepository clientFamilyRepository;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final ClientFamilyMemberCommandFromApiJsonDeserializer apiJsonDeserializer;

    @Autowired
    public ClientFamilyMembersWritePlatformServiceImpl(final PlatformSecurityContext context, final CodeValueRepository codeValueRepository,
            final ClientFamilyMembersRepository clientFamilyRepository, final ClientRepositoryWrapper clientRepositoryWrapper,
            final ClientFamilyMemberCommandFromApiJsonDeserializer apiJsonDeserializer) {
        this.context = context;
        this.codeValueRepository = codeValueRepository;
        this.clientFamilyRepository = clientFamilyRepository;
        this.clientRepositoryWrapper = clientRepositoryWrapper;
        this.apiJsonDeserializer = apiJsonDeserializer;

    }

    @Override
    public CommandProcessingResult addFamilyMember(final long clientId, final JsonCommand command) {

        Long relationshipId = null;
        CodeValue relationship = null;
        CodeValue maritalStatus = null;
        Long maritalStatusId = null;
        Long genderId = null;
        CodeValue gender = null;
        Long professionId = null;
        CodeValue profession = null;
        String firstName = "";
        String middleName = "";
        String lastName = "";
        String qualification = "";
        String mobileNumber = "";
        Long age = null;
        Boolean isDependent = false;
        LocalDate dateOfBirth = null;
        String address1 = "";
        String address2 = "";
        String address3 = "";
        String postalCode = "";
        String email = "";
        long cityId;
        long stateId;
        long countryId;
        long addressTypeId;
        CodeValue stateIdObj = null;
        CodeValue countryIdObj = null;
        CodeValue cityIdObj = null;
        CodeValue addressTypeIdObj = null;

        this.context.authenticatedUser();
        apiJsonDeserializer.validateForCreate(clientId, command.json());

        Client client = clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
        firstName = command.stringValueOfParameterNamed("firstName");
        middleName = command.stringValueOfParameterNamed("middleName");
        lastName = command.stringValueOfParameterNamed("lastName");
        qualification = command.stringValueOfParameterNamed("qualification");
        mobileNumber = command.stringValueOfParameterNamed("mobileNumber");
        age = command.longValueOfParameterNamed("age");
        isDependent = command.booleanObjectValueOfParameterNamed("isDependent");

        if (command.longValueOfParameterNamed("relationshipId") != null) {
            relationshipId = command.longValueOfParameterNamed("relationshipId");
            relationship = this.codeValueRepository.getReferenceById(relationshipId);
        }

        if (command.longValueOfParameterNamed("maritalStatusId") != null) {
            maritalStatusId = command.longValueOfParameterNamed("maritalStatusId");
            maritalStatus = this.codeValueRepository.getReferenceById(maritalStatusId);
        }

        if (command.longValueOfParameterNamed("genderId") != null) {
            genderId = command.longValueOfParameterNamed("genderId");
            gender = this.codeValueRepository.getReferenceById(genderId);
        }

        if (command.longValueOfParameterNamed("professionId") != null) {
            professionId = command.longValueOfParameterNamed("professionId");
            profession = this.codeValueRepository.getReferenceById(professionId);
        }

        dateOfBirth = command.localDateValueOfParameterNamed("dateOfBirth");

        address1 = command.stringValueOfParameterNamed("address1");

        address2 = command.stringValueOfParameterNamed("address2");

        address3 = command.stringValueOfParameterNamed("address3");

        postalCode = command.stringValueOfParameterNamed("postalCode");
        postalCode = command.stringValueOfParameterNamed("email");

        if (command.longValueOfParameterNamed("addressTypeId") != null) {
            if (command.longValueOfParameterNamed("addressTypeId") != 0) {
                addressTypeId = command.longValueOfParameterNamed("addressTypeId");
                addressTypeIdObj = codeValueRepository.getReferenceById(addressTypeId);
            }
        }

        if (command.longValueOfParameterNamed("cityId") != null) {
            if (command.longValueOfParameterNamed("cityId") != 0) {
                cityId = command.longValueOfParameterNamed("cityId");
                cityIdObj = this.codeValueRepository.getReferenceById(cityId);
            }
        }

        if (command.longValueOfParameterNamed("stateProvinceId") != null) {
            if (command.longValueOfParameterNamed("stateProvinceId") != 0) {
                stateId = command.longValueOfParameterNamed("stateProvinceId");
                stateIdObj = this.codeValueRepository.getReferenceById(stateId);
            }
        }

        if (command.longValueOfParameterNamed("countryId") != null) {
            if (command.longValueOfParameterNamed("countryId") != 0) {
                countryId = command.longValueOfParameterNamed("countryId");
                countryIdObj = this.codeValueRepository.getReferenceById(countryId);
            }
        }

        ClientFamilyMembers clientFamilyMembers = ClientFamilyMembers.fromJson(client, firstName, middleName, lastName, qualification,
                mobileNumber, age, isDependent, relationship, maritalStatus, gender, dateOfBirth, profession, email, addressTypeIdObj,
                address1, address2, address3, cityIdObj, stateIdObj, countryIdObj, postalCode);

        this.clientFamilyRepository.saveAndFlush(clientFamilyMembers);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(clientFamilyMembers.getId()).build();

    }

    @Override
    public CommandProcessingResult addClientFamilyMember(final Client client, final JsonCommand command) {

        Long relationshipId = null;
        CodeValue relationship = null;
        CodeValue maritalStatus = null;
        Long maritalStatusId = null;
        Long genderId = null;
        CodeValue gender = null;
        Long professionId = null;
        CodeValue profession = null;
        String firstName = "";
        String middleName = "";
        String lastName = "";
        String qualification = "";
        LocalDate dateOfBirth = null;
        String mobileNumber = "";
        Long age = null;
        Boolean isDependent = false;
        String email = "";

        this.context.authenticatedUser();

        // Client
        // client=clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);

        ClientFamilyMembers familyMember = new ClientFamilyMembers();

        // apiJsonDeserializer.validateForCreate(command.json());

        JsonArray familyMembers = command.arrayOfParameterNamed("familyMembers");

        for (JsonElement members : familyMembers) {

            apiJsonDeserializer.validateForCreate(members.toString());

            JsonObject member = members.getAsJsonObject();

            if (member.get("firstName") != null) {
                firstName = member.get("firstName").getAsString();
            }

            if (member.get("middleName") != null) {
                middleName = member.get("middleName").getAsString();
            }

            if (member.get("lastName") != null) {
                lastName = member.get("lastName").getAsString();
            }

            if (member.get("qualification") != null) {
                qualification = member.get("qualification").getAsString();
            }

            if (member.get("mobileNumber") != null) {
                mobileNumber = member.get("mobileNumber").getAsString();
            }

            if (member.get("age") != null) {
                age = member.get("age").getAsLong();
            }

            if (member.get("isDependent") != null) {
                isDependent = member.get("isDependent").getAsBoolean();
            }

            if (member.get("relationshipId") != null) {
                relationshipId = member.get("relationshipId").getAsLong();
                relationship = this.codeValueRepository.getReferenceById(relationshipId);
            }

            if (member.get("maritalStatusId") != null) {
                maritalStatusId = member.get("maritalStatusId").getAsLong();
                maritalStatus = this.codeValueRepository.getReferenceById(maritalStatusId);
            }

            if (member.get("genderId") != null) {
                genderId = member.get("genderId").getAsLong();
                gender = this.codeValueRepository.getReferenceById(genderId);
            }

            if (member.get("professionId") != null) {
                professionId = member.get("professionId").getAsLong();
                profession = this.codeValueRepository.getReferenceById(professionId);
            }

            if (member.get("dateOfBirth") != null) {

                DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(member.get("dateFormat").getAsString())
                        .toFormatter();
                LocalDate date;
                try {
                    date = LocalDate.parse(member.get("dateOfBirth").getAsString(), formatter);
                    dateOfBirth = date;
                } catch (DateTimeParseException e) {
                    // TODO Auto-generated catch block
                    LOG.error("Problem occurred in addClientFamilyMember function", e);
                }

                /*
                 * this.fromApiJsonHelper.extractDateFormatParameter(member.get( "dateOfBirth").getAsJsonObject());
                 */

            }

            CodeValue addressTypeIdCodeValue = null;
            if (member.get("addressTypeId") != null) {
                long addressTypeId = member.get("addressTypeId").getAsLong();
                addressTypeIdCodeValue = codeValueRepository.getReferenceById(addressTypeId);
            }
            if (member.get("email") != null) {
                email = member.get("email").getAsString();
            }
            String address1 = "";
            if (member.get("address1") != null) {
                address1 = member.get("address1").getAsString();
            }

            String address2 = "";
            if (member.get("address2") != null) {
                address2 = member.get("address2").getAsString();
            }

            String address3 = "";
            if (member.get("address3") != null) {
                address3 = member.get("address3").getAsString();
            }

            String postalCode = "";
            if (member.get("postalCode") != null) {
                postalCode = member.get("postalCode").getAsString();
            }
            CodeValue stateIdCodeValue = null;
            if (member.get("stateProvinceId") != null) {
                long stateId = member.get("stateProvinceId").getAsLong();
                stateIdCodeValue = codeValueRepository.getReferenceById(stateId);
            }

            CodeValue countryIdCodeValue = null;
            if (member.get("countryId") != null) {
                long countryId = member.get("countryId").getAsLong();
                countryIdCodeValue = codeValueRepository.getReferenceById(countryId);
            }
            CodeValue cityIdCodeValue = null;
            if (member.get("cityId") != null) {
                long cityId = member.get("cityId").getAsLong();
                cityIdCodeValue = codeValueRepository.getById(cityId);
            }

            familyMember = ClientFamilyMembers.fromJson(client, firstName, middleName, lastName, qualification, mobileNumber, age,
                    isDependent, relationship, maritalStatus, gender, dateOfBirth, profession, email, addressTypeIdCodeValue, address1,
                    address2, address3, cityIdCodeValue, stateIdCodeValue, countryIdCodeValue, postalCode);

            this.clientFamilyRepository.saveAndFlush(familyMember);

        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(familyMember.getId()).build();

    }

    @Override
    public CommandProcessingResult updateFamilyMember(Long familyMemberId, JsonCommand command) {

        Long relationshipId = null;
        CodeValue relationship = null;
        CodeValue maritalStatus = null;
        Long maritalStatusId = null;
        Long genderId = null;
        CodeValue gender = null;
        Long professionId = null;
        CodeValue profession = null;
        String firstName = "";
        String middleName = "";
        String lastName = "";
        String qualification = "";
        LocalDate dateOfBirth = null;
        String mobileNumber = "";
        Long age = null;
        Boolean isDependent = false;
        String address1 = "";
        String address2 = "";
        String address3 = "";
        String postalCode = "";
        String email = "";
        long cityId;
        long stateId;
        long countryId;
        long addressTypeId;
        CodeValue stateIdObj = null;
        CodeValue countryIdObj = null;
        CodeValue cityIdObj = null;
        CodeValue addressTypeIdObj = null;
        // long clientFamilyMemberId=0;

        this.context.authenticatedUser();

        apiJsonDeserializer.validateForUpdate(familyMemberId, command.json());

        /*
         * if (command.stringValueOfParameterNamed("clientFamilyMemberId") != null) { clientFamilyMemberId =
         * command.longValueOfParameterNamed("clientFamilyMemberId"); }
         */

        ClientFamilyMembers clientFamilyMember = clientFamilyRepository.getReferenceById(familyMemberId);

        // Client
        // client=clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);

        if (command.stringValueOfParameterNamed("firstName") != null) {
            firstName = command.stringValueOfParameterNamed("firstName");
            clientFamilyMember.setFirstName(firstName);
        }

        if (command.stringValueOfParameterNamed("middleName") != null) {
            middleName = command.stringValueOfParameterNamed("middleName");
            clientFamilyMember.setMiddleName(middleName);
        }

        if (command.stringValueOfParameterNamed("lastName") != null) {
            lastName = command.stringValueOfParameterNamed("lastName");
            clientFamilyMember.setLastName(lastName);
        }

        if (command.stringValueOfParameterNamed("qualification") != null) {
            qualification = command.stringValueOfParameterNamed("qualification");
            clientFamilyMember.setQualification(qualification);
        }

        if (command.stringValueOfParameterNamed("mobileNumber") != null) {
            mobileNumber = command.stringValueOfParameterNamed("mobileNumber");
            clientFamilyMember.setMobileNumber(mobileNumber);
        }

        if (command.longValueOfParameterNamed("age") != null) {
            age = command.longValueOfParameterNamed("age");
            clientFamilyMember.setAge(age);
        }

        if (command.booleanObjectValueOfParameterNamed("isDependent") != null) {
            isDependent = command.booleanObjectValueOfParameterNamed("isDependent");
            clientFamilyMember.setIsDependent(isDependent);
        }

        if (command.longValueOfParameterNamed("relationShipId") != null) {
            relationshipId = command.longValueOfParameterNamed("relationShipId");
            relationship = this.codeValueRepository.getReferenceById(relationshipId);
            clientFamilyMember.setRelationship(relationship);
        }

        if (command.longValueOfParameterNamed("maritalStatusId") != 0) {
            maritalStatusId = command.longValueOfParameterNamed("maritalStatusId");
            maritalStatus = this.codeValueRepository.getReferenceById(maritalStatusId);
            clientFamilyMember.setMaritalStatus(maritalStatus);
        }

        if (command.longValueOfParameterNamed("genderId") != 0) {
            genderId = command.longValueOfParameterNamed("genderId");
            gender = this.codeValueRepository.getReferenceById(genderId);
            clientFamilyMember.setGender(gender);
        }

        if (command.longValueOfParameterNamed("professionId") != 0) {
            professionId = command.longValueOfParameterNamed("professionId");
            profession = this.codeValueRepository.getReferenceById(professionId);
            clientFamilyMember.setProfession(profession);
        }

        if (command.localDateValueOfParameterNamed("dateOfBirth") != null) {
            dateOfBirth = command.localDateValueOfParameterNamed("dateOfBirth");
            clientFamilyMember.setDateOfBirth(dateOfBirth);

        }
        if (command.stringValueOfParameterNamed("email") != null) {
            email = command.stringValueOfParameterNamed("email");
            clientFamilyMember.setEmail(email);
        }
        if (command.stringValueOfParameterNamed("address1") != null) {
            address1 = command.stringValueOfParameterNamed("address1");
            clientFamilyMember.setAddress1(address1);
        }

        if (command.stringValueOfParameterNamed("address2") != null) {
            address2 = command.stringValueOfParameterNamed("address2");
            clientFamilyMember.setAddress2(address2);
        }

        if (command.stringValueOfParameterNamed("address3") != null) {
            address3 = command.stringValueOfParameterNamed("address3");
            clientFamilyMember.setAddress3(address3);
        }

        if (command.stringValueOfParameterNamed("postalCode") != null) {
            postalCode = command.stringValueOfParameterNamed("postalCode");
            clientFamilyMember.setPostalCode(postalCode);
        }

        if (command.longValueOfParameterNamed("addressTypeId") != null) {
            if (command.longValueOfParameterNamed("addressTypeId") != 0) {
                addressTypeId = command.longValueOfParameterNamed("addressTypeId");
                addressTypeIdObj = this.codeValueRepository.getById(addressTypeId);
                clientFamilyMember.setAddressType(addressTypeIdObj);
            }

        }

        if (command.longValueOfParameterNamed("cityId") != null) {
            if (command.longValueOfParameterNamed("cityId") != 0) {
                cityId = command.longValueOfParameterNamed("cityId");
                cityIdObj = this.codeValueRepository.getById(cityId);
                clientFamilyMember.setCity(cityIdObj);
            }

        }

        if (command.longValueOfParameterNamed("stateProvinceId") != null) {
            if (command.longValueOfParameterNamed("stateProvinceId") != 0) {
                stateId = command.longValueOfParameterNamed("stateProvinceId");
                stateIdObj = this.codeValueRepository.getById(stateId);
                clientFamilyMember.setStateProvince(stateIdObj);
            }

        }

        if (command.longValueOfParameterNamed("countryId") != null) {
            if (command.longValueOfParameterNamed("countryId") != 0) {
                countryId = command.longValueOfParameterNamed("countryId");
                countryIdObj = this.codeValueRepository.getById(countryId);
                clientFamilyMember.setCountry(countryIdObj);
            }

        }

        // ClientFamilyMembers
        // clientFamilyMembers=ClientFamilyMembers.fromJson(client, firstName,
        // middleName, lastName, qualification, relationship, maritalStatus,
        // gender, dateOfBirth, profession);

        this.clientFamilyRepository.saveAndFlush(clientFamilyMember);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(clientFamilyMember.getId()).build();
    }

    @Override
    public CommandProcessingResult deleteFamilyMember(Long clientFamilyMemberId, JsonCommand command) {
        // TODO Auto-generated method stub

        this.context.authenticatedUser();

        apiJsonDeserializer.validateForDelete(clientFamilyMemberId);

        ClientFamilyMembers clientFamilyMember = null;

        if (clientFamilyMemberId != null) {
            clientFamilyMember = clientFamilyRepository.getReferenceById(clientFamilyMemberId);
            clientFamilyRepository.delete(clientFamilyMember);

        }

        if (clientFamilyMember != null) {
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(clientFamilyMember.getId()).build();
        } else {
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(Long.valueOf(clientFamilyMemberId))
                    .build();
        }

    }

}
