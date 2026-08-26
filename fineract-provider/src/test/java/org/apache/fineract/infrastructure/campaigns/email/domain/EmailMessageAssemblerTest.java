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
package org.apache.fineract.infrastructure.campaigns.email.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailMessageAssemblerTest {

    @Mock
    private EmailMessageRepository emailMessageRepository;
    @Mock
    private GroupRepositoryWrapper groupRepository;
    @Mock
    private ClientRepositoryWrapper clientRepository;
    @Mock
    private StaffRepositoryWrapper staffRepository;
    @Mock
    private Client client;

    // Regression test for: EmailMessageAssembler.assembleFromJson() called EmailMessage.pendingEmail() with a
    // hardcoded null emailCampaign, and pendingEmail() unconditionally called emailCampaign.getStatus(), throwing
    // NullPointerException on every /v1/email CREATE request. Fixed by routing to EmailMessage.instance(), which
    // takes an explicit EmailMessageStatusType instead of deriving it from a (possibly null) emailCampaign.
    @Test
    void assembleFromJsonWithClientIdAndNoCampaignDoesNotThrowNpe() {
        FromJsonHelper fromApiJsonHelper = new FromJsonHelper();
        EmailMessageAssembler assembler = new EmailMessageAssembler(emailMessageRepository, groupRepository, clientRepository,
                staffRepository, fromApiJsonHelper);

        when(clientRepository.findOneWithNotFoundDetection(2L)).thenReturn(client);
        when(client.emailAddress()).thenReturn("client@example.com");

        String json = """
                {
                  "clientId": 2,
                  "emailMessage": "test message"
                }
                """;

        JsonCommand command = JsonCommand.from(json, JsonParser.parseString(json), fromApiJsonHelper, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

        EmailMessage result = assertDoesNotThrow(() -> assembler.assembleFromJson(command));

        // EmailMessage exposes no getters for status/campaign, so isPending() is the only public signal available
        // to confirm the fix actually produced a PENDING message and didn't just swallow the exception.
        assertTrue(result.isPending());
    }
}
