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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.portfolio.client.data.FamilyMemberCreateRequest;
import org.apache.fineract.portfolio.client.data.FamilyMemberCreateResponse;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientFamilyMembersRepository;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
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

    private RuntimeException handleDataIntegrityIssues(final Throwable realCause, final Exception dve) {
        log.error("Error occurred.", dve);
        return ErrorHandler.getMappable(dve, "error.msg.familymember.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
