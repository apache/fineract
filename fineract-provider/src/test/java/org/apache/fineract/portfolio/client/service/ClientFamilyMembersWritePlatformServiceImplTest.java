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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.ClientFamilyMembers;
import org.apache.fineract.portfolio.client.domain.ClientFamilyMembersRepository;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.serialization.ClientFamilyMemberCommandFromApiJsonDeserializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClientFamilyMembersWritePlatformServiceImplTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private CodeValueRepository codeValueRepository;
    @Mock
    private ClientFamilyMembersRepository clientFamilyRepository;
    @Mock
    private ClientRepositoryWrapper clientRepositoryWrapper;
    @Mock
    private ClientFamilyMemberCommandFromApiJsonDeserializer apiJsonDeserializer;

    @InjectMocks
    private ClientFamilyMembersWritePlatformServiceImpl service;

    @Test
    public void updateFamilyMemberAppliesRelationshipId() {
        Long familyMemberId = 531L;
        ClientFamilyMembers familyMember = mock(ClientFamilyMembers.class);
        CodeValue spouse = mock(CodeValue.class);
        when(clientFamilyRepository.getReferenceById(familyMemberId)).thenReturn(familyMember);
        when(codeValueRepository.getReferenceById(22L)).thenReturn(spouse);

        JsonCommand command = JsonCommand.fromJsonElement(familyMemberId,
                JsonParser.parseString("{\"relationshipId\":22,\"maritalStatusId\":0,\"genderId\":0,\"professionId\":0}"),
                new FromJsonHelper());

        service.updateFamilyMember(familyMemberId, command);

        verify(familyMember).setRelationship(spouse);
        verify(clientFamilyRepository).saveAndFlush(familyMember);
    }
}
