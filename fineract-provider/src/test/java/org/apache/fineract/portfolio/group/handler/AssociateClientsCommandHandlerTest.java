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
package org.apache.fineract.portfolio.group.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Set;
import org.apache.fineract.portfolio.group.command.AssociateClientsCommand;
import org.apache.fineract.portfolio.group.data.AssociateClientsRequest;
import org.apache.fineract.portfolio.group.data.AssociateClientsResponse;
import org.apache.fineract.portfolio.group.service.GroupingTypesWritePlatformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssociateClientsCommandHandlerTest {

    @Mock
    private GroupingTypesWritePlatformService service;

    @InjectMocks
    private AssociateClientsCommandHandler handler;

    @Test
    void shouldAssociateClients() {
        AssociateClientsRequest request = AssociateClientsRequest.builder() //
                .groupId(1L) //
                .clientMembers(Set.of(10L, 20L, 30L)) //
                .build();
        AssociateClientsResponse expected = AssociateClientsResponse.builder() //
                .resourceId(1L) //
                .groupId(1L) //
                .officeId(2L) //
                .changes(new HashMap<>()) //
                .build();
        when(service.associateClientsToGroup(any(AssociateClientsRequest.class))).thenReturn(expected);

        AssociateClientsCommand command = new AssociateClientsCommand();
        command.setPayload(request);
        AssociateClientsResponse result = handler.handle(command);

        assertThat(result.getGroupId()).isEqualTo(1L);
        assertThat(result.getResourceId()).isEqualTo(1L);
    }
}
