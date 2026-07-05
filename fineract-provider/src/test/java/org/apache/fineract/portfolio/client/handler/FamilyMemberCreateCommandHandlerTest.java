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
package org.apache.fineract.portfolio.client.handler;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import org.apache.fineract.portfolio.client.command.FamilyMemberCreateCommand;
import org.apache.fineract.portfolio.client.data.FamilyMemberCreateRequest;
import org.apache.fineract.portfolio.client.data.FamilyMemberCreateResponse;
import org.apache.fineract.portfolio.client.service.FamilyMemberWriteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FamilyMemberCreateCommandHandlerTest {

    @Mock
    FamilyMemberWriteService writeService;

    @InjectMocks
    FamilyMemberCreateCommandHandler handler;

    @Test
    void handleDelegatesToWriteService() {
        FamilyMemberCreateRequest req = new FamilyMemberCreateRequest();
        req.setClientId(1L);
        req.setFirstName("Ada");
        req.setLastName("Lovelace");
        req.setRelationshipId(42L);

        FamilyMemberCreateCommand cmd = new FamilyMemberCreateCommand();
        cmd.setPayload(req);

        FamilyMemberCreateResponse expected = FamilyMemberCreateResponse.builder().clientId(1L).resourceId(99L).build();
        when(writeService.createFamilyMember(req)).thenReturn(expected);

        assertSame(expected, handler.handle(cmd));
    }
}
