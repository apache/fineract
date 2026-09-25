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

import jakarta.persistence.PersistenceException;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.portfolio.client.data.FamilyMemberCreateRequest;
import org.apache.fineract.portfolio.client.data.FamilyMemberCreateResponse;
import org.apache.fineract.portfolio.client.data.FamilyMemberDeleteRequest;
import org.apache.fineract.portfolio.client.data.FamilyMemberDeleteResponse;
import org.apache.fineract.portfolio.client.data.FamilyMemberUpdateRequest;
import org.apache.fineract.portfolio.client.data.FamilyMemberUpdateResponse;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientFamilyMembers;
import org.apache.fineract.portfolio.client.domain.ClientFamilyMembersRepository;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.FamilyMemberNotFoundException;
import org.apache.fineract.portfolio.client.mapper.FamilyMemberCreateRequestMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class FamilyMemberWriteServiceImpl implements FamilyMemberWriteService {

    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final ClientFamilyMembersRepository clientFamilyRepository;
    private final CodeValueRepository codeValueRepository;
    private final FamilyMemberCreateRequestMapper familyMemberCreateRequestMapper;

    @Transactional
    @Override
    public FamilyMemberCreateResponse createFamilyMember(final FamilyMemberCreateRequest request) {
        try {
            var entity = familyMemberCreateRequestMapper.map(request);

            Client client = clientRepositoryWrapper.findOneWithNotFoundDetection(request.getClientId());
            entity.setClient(client);

            entity.setRelationship(codeValueRepository.getReferenceById(request.getRelationshipId()));

            if (request.getMaritalStatusId() != null) {
                entity.setMaritalStatus(codeValueRepository.getReferenceById(request.getMaritalStatusId()));
            }
            if (request.getGenderId() != null) {
                entity.setGender(codeValueRepository.getReferenceById(request.getGenderId()));
            }
            if (request.getProfessionId() != null) {
                entity.setProfession(codeValueRepository.getReferenceById(request.getProfessionId()));
            }

            clientFamilyRepository.saveAndFlush(entity);

            return FamilyMemberCreateResponse.builder().resourceId(entity.getId()).clientId(request.getClientId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw handleDataIntegrityIssues(dve.getMostSpecificCause(), dve);
        } catch (final PersistenceException dve) {
            var throwable = ExceptionUtils.getRootCause(dve.getCause());
            throw handleDataIntegrityIssues(throwable, dve);
        }
    }

    @Transactional
    @Override
    public FamilyMemberUpdateResponse updateFamilyMember(final FamilyMemberUpdateRequest request) {
        try {
            ClientFamilyMembers entity = findFamilyMemberForClient(request.getId(), request.getClientId());

            var changes = new HashMap<String, Object>();

            if (StringUtils.isNotEmpty(request.getFirstName())) {
                entity.setFirstName(request.getFirstName());
                changes.put(FamilyMemberUpdateRequest.Fields.firstName, request.getFirstName());
            }
            if (StringUtils.isNotEmpty(request.getMiddleName())) {
                entity.setMiddleName(request.getMiddleName());
                changes.put(FamilyMemberUpdateRequest.Fields.middleName, request.getMiddleName());
            }
            if (StringUtils.isNotEmpty(request.getLastName())) {
                entity.setLastName(request.getLastName());
                changes.put(FamilyMemberUpdateRequest.Fields.lastName, request.getLastName());
            }
            if (StringUtils.isNotEmpty(request.getQualification())) {
                entity.setQualification(request.getQualification());
                changes.put(FamilyMemberUpdateRequest.Fields.qualification, request.getQualification());
            }
            if (StringUtils.isNotEmpty(request.getMobileNumber())) {
                entity.setMobileNumber(request.getMobileNumber());
                changes.put(FamilyMemberUpdateRequest.Fields.mobileNumber, request.getMobileNumber());
            }
            if (request.getAge() != null) {
                entity.setAge(request.getAge());
                changes.put(FamilyMemberUpdateRequest.Fields.age, request.getAge());
            }
            if (request.getIsDependent() != null) {
                entity.setIsDependent(request.getIsDependent());
                changes.put(FamilyMemberUpdateRequest.Fields.isDependent, request.getIsDependent());
            }
            if (request.getRelationshipId() != null) {
                entity.setRelationship(codeValueRepository.getReferenceById(request.getRelationshipId()));
                changes.put(FamilyMemberUpdateRequest.Fields.relationshipId, request.getRelationshipId());
            }
            if (request.getMaritalStatusId() != null) {
                entity.setMaritalStatus(codeValueRepository.getReferenceById(request.getMaritalStatusId()));
                changes.put(FamilyMemberUpdateRequest.Fields.maritalStatusId, request.getMaritalStatusId());
            }
            if (request.getGenderId() != null) {
                entity.setGender(codeValueRepository.getReferenceById(request.getGenderId()));
                changes.put(FamilyMemberUpdateRequest.Fields.genderId, request.getGenderId());
            }
            if (request.getProfessionId() != null) {
                entity.setProfession(codeValueRepository.getReferenceById(request.getProfessionId()));
                changes.put(FamilyMemberUpdateRequest.Fields.professionId, request.getProfessionId());
            }
            if (request.getDateOfBirth() != null) {
                entity.setDateOfBirth(request.getDateOfBirth());
                changes.put(FamilyMemberUpdateRequest.Fields.dateOfBirth, request.getDateOfBirth());
            }

            var response = FamilyMemberUpdateResponse.builder().resourceId(entity.getId()).clientId(request.getClientId());
            if (!changes.isEmpty()) {
                response.changes(changes);
                clientFamilyRepository.saveAndFlush(entity);
            }
            return response.build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw handleDataIntegrityIssues(dve.getMostSpecificCause(), dve);
        } catch (final PersistenceException dve) {
            var throwable = ExceptionUtils.getRootCause(dve.getCause());
            throw handleDataIntegrityIssues(throwable, dve);
        }
    }

    @Transactional
    @Override
    public FamilyMemberDeleteResponse deleteFamilyMember(final FamilyMemberDeleteRequest request) {
        ClientFamilyMembers entity = findFamilyMemberForClient(request.getId(), request.getClientId());
        clientFamilyRepository.delete(entity);
        return FamilyMemberDeleteResponse.builder().resourceId(request.getId()).clientId(request.getClientId()).build();
    }

    private ClientFamilyMembers findFamilyMemberForClient(final Long familyMemberId, final Long clientId) {
        ClientFamilyMembers entity = clientFamilyRepository.findById(familyMemberId)
                .orElseThrow(() -> new FamilyMemberNotFoundException(familyMemberId, clientId, null));
        if (entity.getClient() == null || !entity.getClient().getId().equals(clientId)) {
            throw new FamilyMemberNotFoundException(familyMemberId, clientId, null);
        }
        return entity;
    }

    private RuntimeException handleDataIntegrityIssues(final Throwable realCause, final Exception dve) {
        log.error("Error occurred.", dve);
        return ErrorHandler.getMappable(dve, "error.msg.familymember.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
