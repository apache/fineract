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
package org.apache.fineract.infrastructure.campaigns.email.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailDataValidator;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailMessage;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailMessageAssembler;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailMessageRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailWritePlatformServiceJpaRepositoryImplTest {

    private static final FromJsonHelper JSON_HELPER = new FromJsonHelper();
    private static final Long MESSAGE_ID = 8L;

    @Mock
    private EmailMessageAssembler assembler;
    @Mock
    private EmailMessageRepository repository;
    @Mock
    private EmailDataValidator validator;

    private EmailWritePlatformServiceJpaRepositoryImpl service;
    private EmailMessage message;

    @BeforeEach
    void setUp() {
        service = new EmailWritePlatformServiceJpaRepositoryImpl(assembler, repository, validator);
        message = new EmailMessage().setMessage("Dear client");
        when(assembler.assembleFromResourceId(MESSAGE_ID)).thenReturn(message);
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JSON_HELPER.parse(json), JSON_HELPER, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
    }

    // the result builder leaves the changes null when nothing changed
    private static Map<String, Object> changesOf(final CommandProcessingResult result) {
        return result.getChanges() == null ? Map.of() : result.getChanges();
    }

    @Test
    void updateAppliesAndReportsTheChangedMessage() {
        final CommandProcessingResult result = service.update(MESSAGE_ID, command("""
                {"emailMessage": "Hi there"}
                """));

        assertEquals(Map.of("emailMessage", "Hi there"), result.getChanges());
        assertEquals("Hi there", message.getMessage());
        verify(repository).save(message);
    }

    @Test
    void updateWithTheSameMessageReportsNothingAndDoesNotSave() {
        final CommandProcessingResult result = service.update(MESSAGE_ID, command("""
                {"emailMessage": "Dear client"}
                """));

        assertTrue(changesOf(result).isEmpty());
        verify(repository, never()).save(any());
    }

    @Test
    void updateWithAnEmptyMessageClearsItAndReportsTheEmptyString() {
        final CommandProcessingResult result = service.update(MESSAGE_ID, command("""
                {"emailMessage": ""}
                """));

        assertEquals(Map.of("emailMessage", ""), result.getChanges());
        assertNull(message.getMessage());
    }
}
