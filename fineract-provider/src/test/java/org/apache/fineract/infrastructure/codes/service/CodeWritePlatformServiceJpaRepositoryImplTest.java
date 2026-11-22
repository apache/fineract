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
package org.apache.fineract.infrastructure.codes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.codes.domain.Code;
import org.apache.fineract.infrastructure.codes.domain.CodeRepository;
import org.apache.fineract.infrastructure.codes.exception.SystemDefinedCodeCannotBeChangedException;
import org.apache.fineract.infrastructure.codes.serialization.CodeCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CodeWritePlatformServiceJpaRepositoryImplTest {

    private static final Long CODE_ID = 7L;

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private CodeRepository codeRepository;
    @Mock
    private CodeCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    private CodeWritePlatformServiceJpaRepositoryImpl service;

    @BeforeEach
    void setUp() {
        service = new CodeWritePlatformServiceJpaRepositoryImpl(context, codeRepository, fromApiJsonDeserializer);
    }

    @Test
    void createCodeTakesTheNameFromTheCommand() {
        service.createCode(command("""
                {"name": "Customer type"}
                """));

        final ArgumentCaptor<Code> saved = ArgumentCaptor.forClass(Code.class);
        verify(codeRepository).saveAndFlush(saved.capture());
        assertEquals("Customer type", saved.getValue().getName());
    }

    @Test
    void updateCodeAppliesAndReportsTheNewName() {
        final Code code = Code.createNew("Customer type");
        when(codeRepository.findById(CODE_ID)).thenReturn(Optional.of(code));

        final CommandProcessingResult result = service.updateCode(CODE_ID, command("""
                {"name": "Client type"}
                """));

        assertEquals(Map.of("name", "Client type"), result.getChanges());
        assertEquals("Client type", code.getName());
        verify(codeRepository).saveAndFlush(code);
    }

    @Test
    void updateCodeWithoutChangesDoesNotSave() {
        final Code code = Code.createNew("Customer type");
        when(codeRepository.findById(CODE_ID)).thenReturn(Optional.of(code));

        final CommandProcessingResult result = service.updateCode(CODE_ID, command("""
                {"name": "Customer type"}
                """));

        // CommandProcessingResult reports an empty change set as null
        assertNull(result.getChanges());
        verify(codeRepository, never()).saveAndFlush(any(Code.class));
    }

    @Test
    void updateOfASystemDefinedCodeIsRejectedEvenWithoutAChange() {
        final Code code = Code.createNew("Customer type").setSystemDefined(true);
        when(codeRepository.findById(CODE_ID)).thenReturn(Optional.of(code));

        assertThrows(SystemDefinedCodeCannotBeChangedException.class, () -> service.updateCode(CODE_ID, command("""
                {"name": "Customer type"}
                """)));

        assertEquals("Customer type", code.getName());
        verify(codeRepository, never()).saveAndFlush(any(Code.class));
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JsonParser.parseString(json), new FromJsonHelper(), "codes", CODE_ID, null, null, null, null, null,
                null, null, null, null, null, null, null);
    }
}
