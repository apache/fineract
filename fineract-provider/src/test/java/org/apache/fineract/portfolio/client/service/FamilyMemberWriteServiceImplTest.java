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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.portfolio.client.data.FamilyMemberDeleteRequest;
import org.apache.fineract.portfolio.client.data.FamilyMemberUpdateRequest;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientFamilyMembers;
import org.apache.fineract.portfolio.client.domain.ClientFamilyMembersRepository;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.FamilyMemberNotFoundException;
import org.apache.fineract.portfolio.client.mapper.FamilyMemberCreateRequestMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FamilyMemberWriteServiceImplTest {

    private static final Long CLIENT_ID = 7L;
    private static final Long FAMILY_MEMBER_ID = 531L;

    @Mock
    private ClientRepositoryWrapper clientRepositoryWrapper;
    @Mock
    private ClientFamilyMembersRepository clientFamilyRepository;
    @Mock
    private CodeValueRepository codeValueRepository;
    @Mock
    private FamilyMemberCreateRequestMapper familyMemberCreateRequestMapper;

    @InjectMocks
    private FamilyMemberWriteServiceImpl service;

    @Test
    public void updateFamilyMemberAppliesRelationshipId() {
        ClientFamilyMembers familyMember = familyMemberOf(CLIENT_ID);
        CodeValue spouse = mock(CodeValue.class);
        when(codeValueRepository.getReferenceById(22L)).thenReturn(spouse);

        var response = service.updateFamilyMember(
                FamilyMemberUpdateRequest.builder().id(FAMILY_MEMBER_ID).clientId(CLIENT_ID).relationshipId(22L).build());

        verify(familyMember).setRelationship(spouse);
        verify(clientFamilyRepository).saveAndFlush(familyMember);
        assertEquals(22L, response.getChanges().get(FamilyMemberUpdateRequest.Fields.relationshipId));
    }

    @Test
    public void updateFamilyMemberTreatsZeroCodeValueIdsAsNotProvidedAndStoresZeroAge() {
        ClientFamilyMembers familyMember = familyMemberOf(CLIENT_ID);

        var response = service.updateFamilyMember(FamilyMemberUpdateRequest.builder().id(FAMILY_MEMBER_ID).clientId(CLIENT_ID).age(0L)
                .maritalStatusId(0L).genderId(0L).professionId(0L).build());

        verify(familyMember).setAge(0L);
        verify(familyMember, never()).setMaritalStatus(any());
        verify(familyMember, never()).setGender(any());
        verify(familyMember, never()).setProfession(any());
        verify(codeValueRepository, never()).getReferenceById(any());
        assertEquals(Map.of(FamilyMemberUpdateRequest.Fields.age, 0L), response.getChanges());
    }

    @Test
    public void updateFamilyMemberOfAnotherClientIsNotFound() {
        ClientFamilyMembers familyMember = familyMemberOf(CLIENT_ID + 1);

        assertThrows(FamilyMemberNotFoundException.class, () -> service.updateFamilyMember(
                FamilyMemberUpdateRequest.builder().id(FAMILY_MEMBER_ID).clientId(CLIENT_ID).firstName("Someone Else").build()));

        verify(familyMember, never()).setFirstName(any());
        verify(clientFamilyRepository, never()).saveAndFlush(any());
    }

    @Test
    public void deleteFamilyMemberOfAnotherClientIsNotFound() {
        familyMemberOf(CLIENT_ID + 1);

        assertThrows(FamilyMemberNotFoundException.class,
                () -> service.deleteFamilyMember(FamilyMemberDeleteRequest.builder().id(FAMILY_MEMBER_ID).clientId(CLIENT_ID).build()));

        verify(clientFamilyRepository, never()).delete(any(ClientFamilyMembers.class));
    }

    private ClientFamilyMembers familyMemberOf(Long clientId) {
        Client client = mock(Client.class);
        when(client.getId()).thenReturn(clientId);
        ClientFamilyMembers familyMember = mock(ClientFamilyMembers.class);
        when(familyMember.getClient()).thenReturn(client);
        when(clientFamilyRepository.findById(FAMILY_MEMBER_ID)).thenReturn(Optional.of(familyMember));
        return familyMember;
    }
}
