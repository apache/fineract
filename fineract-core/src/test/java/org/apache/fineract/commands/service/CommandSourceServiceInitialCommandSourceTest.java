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
package org.apache.fineract.commands.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Set;
import org.apache.fineract.commands.domain.CommandProcessingResultType;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Test;

/**
 * Tests how {@link CommandSourceService#getInitialCommandSource} fills a new command source from the command wrapper
 * and the command.
 */
class CommandSourceServiceInitialCommandSourceTest {

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
    private final CommandSourceService underTest = new CommandSourceService(mock(ConfigurationDomainService.class),
            mock(CommandSourceRepository.class), mock(ErrorHandler.class), fromJsonHelper);

    @Test
    void fillsTheEntryFromTheWrapperAndTheCommand() {
        final String json = "{\"name\": \"value\"}";
        final CommandWrapper wrapper = wrapper(json, null);
        final JsonCommand command = command(json);
        final AppUser maker = mock(AppUser.class);

        final CommandSource source = underTest.getInitialCommandSource(wrapper, command, maker, "key-1");

        assertEquals("UPDATE", source.getActionName());
        assertEquals("LOAN", source.getEntityName());
        assertEquals("/loans/5", source.getResourceGetUrl());
        assertEquals(10L, source.getOfficeId());
        assertEquals(1L, source.getResourceId());
        assertEquals(2L, source.getSubResourceId());
        assertEquals(json, source.getCommandAsJson());
        assertSame(maker, source.getMaker());
        assertNotNull(source.getMadeOnDate());
        assertEquals(CommandProcessingResultType.UNDER_PROCESSING.getValue(), source.getStatus());
        assertEquals("key-1", source.getIdempotencyKey());
        assertEquals(3L, source.getGroupId());
        assertEquals(4L, source.getClientId());
        assertEquals(5L, source.getLoanId());
        assertEquals(6L, source.getSavingsId());
        assertEquals("7", source.getTransactionId());
        assertEquals(8L, source.getProductId());
        assertEquals(11L, source.getCreditBureauId());
        assertEquals(12L, source.getOrganisationCreditBureauId());
        assertEquals(new ExternalId("loan-ext"), source.getLoanExternalId());
        assertFalse(source.isSanitized());
        assertTrue(source.isSubmittedIdsKept());
        assertFalse(source.isResultIdsWritten());
        assertNull(source.getChecker());
    }

    @Test
    void masksTheSanitizedKeys() {
        final String json = "{\"password\": \"secret\", \"name\": \"value\"}";

        final CommandSource source = underTest.getInitialCommandSource(wrapper(json, Set.of("password")), command(json),
                mock(AppUser.class), "key-1");

        assertEquals("{\"password\":\"***\",\"name\":\"value\"}", source.getCommandAsJson());
        assertTrue(source.isSanitized());
    }

    @Test
    void storesAnEmptyObjectWhenTheCommandHasNoJson() {
        final CommandSource source = underTest.getInitialCommandSource(wrapper(null, null), command(null), mock(AppUser.class), null);

        assertEquals("{}", source.getCommandAsJson());
        assertNull(source.getIdempotencyKey());
        assertNull(source.getLoanId());
    }

    private static CommandWrapper wrapper(final String json, final Set<String> sanitizeKeys) {
        return new CommandWrapper(10L, 30L, 40L, 50L, 60L, "UPDATE", "LOAN", 1L, 2L, "/loans/5", json, "70", 80L, null, 110L, 120L, null,
                "key-1", new ExternalId("wrapper-ext"), sanitizeKeys);
    }

    private JsonCommand command(final String json) {
        if (json == null) {
            return JsonCommand.from(null);
        }
        return JsonCommand.from(json, fromJsonHelper.parse(json), fromJsonHelper, "LOAN", 1L, 2L, 3L, 4L, 5L, 6L, "7", "/loans/5", 8L, 11L,
                12L, null, new ExternalId("loan-ext"));
    }
}
