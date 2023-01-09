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
package org.apache.fineract.portfolio.client.mapper;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.data.ClientTimelineData;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientEnumerations;
import org.apache.fineract.useradministration.domain.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapstructMapperConfig.class)
public interface ClientMapper {

    @Mapping(target = "accountNo", source = "source.accountNumber")
    @Mapping(target = "status", source = "source", qualifiedByName = "clientStatusEnum")
    @Mapping(target = "subStatus", source = "source", qualifiedByName = "clientSubStatusCode")
    @Mapping(target = "officeId", source = "source.office.id")
    @Mapping(target = "officeName", source = "source.office.name")
    @Mapping(target = "transferToOfficeId", source = "source.transferToOffice.id")
    @Mapping(target = "transferToOfficeName", source = "source.transferToOffice.name")
    @Mapping(target = "externalId", source = "source.externalId")
    @Mapping(target = "gender", source = "source", qualifiedByName = "clientGenderCode")
    @Mapping(target = "imageId", source = "source.image.id")
    @Mapping(target = "staffId", source = "source.staff.id")
    @Mapping(target = "staffName", source = "source.staff.displayName")
    @Mapping(target = "timeline", source = "source", qualifiedByName = "clientTimelineData")
    @Mapping(target = "savingsProductId", source = "source.savingsProductId")
    @Mapping(target = "savingsProductName", source = "source.id")
    @Mapping(target = "savingsAccountId", source = "source.savingsAccountId")
    @Mapping(target = "clientType", source = "source", qualifiedByName = "clientTypeCode")
    @Mapping(target = "clientClassification", source = "source", qualifiedByName = "clientClassificationCode")
    @Mapping(target = "legalForm", source = "source", qualifiedByName = "clientLegalFormEnum")
    @Mapping(target = "isStaff", source = "source", qualifiedByName = "clientIsStaff")
    @Mapping(target = "imagePresent", ignore = true)
    @Mapping(target = "officeOptions", ignore = true)
    @Mapping(target = "staffOptions", ignore = true)
    @Mapping(target = "narrations", ignore = true)
    @Mapping(target = "savingProductOptions", ignore = true)
    @Mapping(target = "savingAccountOptions", ignore = true)
    @Mapping(target = "genderOptions", ignore = true)
    @Mapping(target = "clientTypeOptions", ignore = true)
    @Mapping(target = "clientClassificationOptions", ignore = true)
    @Mapping(target = "clientNonPersonConstitutionOptions", ignore = true)
    @Mapping(target = "clientNonPersonMainBusinessLineOptions", ignore = true)
    @Mapping(target = "clientLegalFormOptions", ignore = true)
    @Mapping(target = "familyMemberOptions", ignore = true)
    @Mapping(target = "clientNonPersonDetails", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "isAddressEnabled", ignore = true)
    @Mapping(target = "datatables", ignore = true)
    @Mapping(target = "rowIndex", ignore = true)
    @Mapping(target = "dateFormat", ignore = true)
    @Mapping(target = "locale", ignore = true)
    @Mapping(target = "clientTypeId", ignore = true)
    @Mapping(target = "genderId", ignore = true)
    @Mapping(target = "clientClassificationId", ignore = true)
    @Mapping(target = "legalFormId", ignore = true)
    @Mapping(target = "clientCollateralManagements", ignore = true)
    @Mapping(target = "groups", ignore = true)
    ClientData map(Client source);

    @Named("clientTypeCode")
    default CodeValueData clientTypeCode(Client client) {
        final CodeValue code = client.getClientType();
        if (code == null) {
            return null;
        }
        return CodeValueData.instance(code.getId(), code.getLabel());
    }

    @Named("clientClassificationCode")
    default CodeValueData clientClassificationCode(Client client) {
        final CodeValue code = client.getClientClassification();
        if (code == null) {
            return null;
        }
        return CodeValueData.instance(code.getId(), code.getLabel());
    }

    @Named("clientSubStatusCode")
    default CodeValueData clientSubStatusCode(Client client) {
        final CodeValue code = client.getSubStatus();
        if (code == null) {
            return null;
        }
        return CodeValueData.instance(code.getId(), code.getLabel());
    }

    @Named("clientGenderCode")
    default CodeValueData clientGenderCode(Client client) {
        final CodeValue code = client.getGender();
        if (code == null) {
            return null;
        }
        return CodeValueData.instance(code.getId(), code.getLabel());
    }

    @Named("clientLegalFormEnum")
    default EnumOptionData clientLegalFormEnum(Client client) {
        return ClientEnumerations.legalForm(client.getLegalForm());
    }

    @Named("clientStatusEnum")
    default EnumOptionData clientStatusEnum(Client client) {
        return ClientEnumerations.status(client.getStatus());
    }

    @Named("clientTimelineData")
    default ClientTimelineData clientTimelineData(Client client) {
        if (client.isClosed()) {
            final AppUser activatedBy = client.getActivatedBy();
            if (activatedBy != null) {
                return new ClientTimelineData(client.getSubmittedOnDate(), null, null, null, client.getActivationDate(),
                        activatedBy.getUsername(), activatedBy.getFirstname(), activatedBy.getLastname(), client.getClosureDate(),
                        client.getClosedBy().getUsername(), client.getClosedBy().getFirstname(), client.getClosedBy().getLastname());
            } else {
                return new ClientTimelineData(client.getSubmittedOnDate(), null, null, null, client.getActivationDate(), null, null, null,
                        client.getClosureDate(), client.getClosedBy().getUsername(), client.getClosedBy().getFirstname(),
                        client.getClosedBy().getLastname());

            }
        } else if (client.isActive()) {
            return new ClientTimelineData(client.getSubmittedOnDate(), null, null, null, client.getActivationDate(),
                    client.getActivatedBy().getUsername(), client.getActivatedBy().getFirstname(), client.getActivatedBy().getLastname(),
                    null, null, null, null);
        } else {
            return new ClientTimelineData(client.getSubmittedOnDate(), null, null, null, null, null, null, null, null, null, null, null);
        }
    }

    @Named("clientIsStaff")
    default Boolean clientIsStaff(Client client) {
        return Boolean.valueOf(client.isStaff());
    }

}
