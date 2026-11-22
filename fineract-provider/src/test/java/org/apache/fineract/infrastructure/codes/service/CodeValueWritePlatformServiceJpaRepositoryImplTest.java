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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.codes.domain.Code;
import org.apache.fineract.infrastructure.codes.domain.CodeRepository;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.codes.serialization.CodeValueCommandFromApiJsonDeserializer;
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
class CodeValueWritePlatformServiceJpaRepositoryImplTest {

    private static final Long CODE_ID = 7L;
    private static final Long CODE_VALUE_ID = 70L;

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private CodeRepository codeRepository;
    @Mock
    private CodeValueRepositoryWrapper codeValueRepositoryWrapper;
    @Mock
    private CodeValueRepository codeValueRepository;
    @Mock
    private CodeValueCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    private CodeValueWritePlatformServiceJpaRepositoryImpl service;

    @BeforeEach
    void setUp() {
        service = new CodeValueWritePlatformServiceJpaRepositoryImpl(context, codeRepository, codeValueRepositoryWrapper,
                codeValueRepository, fromApiJsonDeserializer);
    }

    @Test
    void createCodeValueTakesItsValuesFromTheCommand() {
        final Code code = Code.createNew("Customer type");
        when(codeRepository.findById(CODE_ID)).thenReturn(Optional.of(code));

        service.createCodeValue(command("""
                {"name": "Retail", "description": "Retail customers", "position": 3, "isActive": false, "isMandatory": true}
                """));

        final CodeValue saved = savedCodeValue();
        assertSame(code, saved.getCode());
        assertEquals("Retail", saved.getLabel());
        assertEquals("Retail customers", saved.getDescription());
        assertEquals(3, saved.getPosition());
        assertFalse(saved.isActive());
        assertTrue(saved.isMandatory());
    }

    @Test
    void createCodeValueDefaultsTheOptionalValues() {
        when(codeRepository.findById(CODE_ID)).thenReturn(Optional.of(Code.createNew("Customer type")));

        service.createCodeValue(command("""
                {"name": "Retail"}
                """));

        final CodeValue saved = savedCodeValue();
        assertEquals(0, saved.getPosition());
        assertTrue(saved.isActive());
        assertFalse(saved.isMandatory());
        // a missing description is stored as an empty string, not as null
        assertEquals("", saved.getDescription());
    }

    @Test
    void updateCodeValueAppliesAndReportsTheChangedValuesInOrder() {
        final CodeValue codeValue = existingCodeValue();

        final CommandProcessingResult result = service.updateCodeValue(CODE_VALUE_ID, command("""
                {"isActive": false, "position": 5, "description": "Small companies", "name": "SME"}
                """));

        assertEquals(List.of("name", "description", "position", "isActive"), List.copyOf(result.getChanges().keySet()));
        assertEquals(Map.of("name", "SME", "description", "Small companies", "position", 5, "isActive", false), result.getChanges());
        assertEquals("SME", codeValue.getLabel());
        assertEquals("Small companies", codeValue.getDescription());
        assertEquals(5, codeValue.getPosition());
        assertFalse(codeValue.isActive());
        verify(codeValueRepository).saveAndFlush(codeValue);
    }

    @Test
    void updateCodeValueStoresAnEmptyDescriptionAsNull() {
        final CodeValue codeValue = existingCodeValue();

        final CommandProcessingResult result = service.updateCodeValue(CODE_VALUE_ID, command("""
                {"description": ""}
                """));

        // the change set reports the value as sent, while the code value stores an empty description as null
        assertEquals(Map.of("description", ""), result.getChanges());
        assertNull(codeValue.getDescription());
        verify(codeValueRepository).saveAndFlush(codeValue);
    }

    /**
     * Pins an existing defect: the update validator accepts an empty position, which is read as null and cannot be
     * stored in the primitive position field, so the update fails with a NullPointerException (a 500 through the API).
     */
    @Test
    void updateWithAnEmptyPositionFails() {
        final CodeValue codeValue = existingCodeValue();

        assertThrows(NullPointerException.class, () -> service.updateCodeValue(CODE_VALUE_ID, command("""
                {"position": null}
                """)));

        assertEquals(3, codeValue.getPosition());
        verify(codeValueRepository, never()).saveAndFlush(any(CodeValue.class));
    }

    @Test
    void updateCodeValueWithoutChangesDoesNotSave() {
        existingCodeValue();

        final CommandProcessingResult result = service.updateCodeValue(CODE_VALUE_ID, command("""
                {"name": "Retail", "description": "Retail customers", "position": 3, "isActive": true}
                """));

        // CommandProcessingResult reports an empty change set as null
        assertNull(result.getChanges());
        verify(codeValueRepository, never()).saveAndFlush(any(CodeValue.class));
    }

    private CodeValue existingCodeValue() {
        final CodeValue codeValue = CodeValue.createNew(Code.createNew("Customer type"), "Retail", 3, "Retail customers", true, false);
        when(codeValueRepositoryWrapper.findOneWithNotFoundDetection(CODE_VALUE_ID)).thenReturn(codeValue);
        return codeValue;
    }

    private CodeValue savedCodeValue() {
        final ArgumentCaptor<CodeValue> saved = ArgumentCaptor.forClass(CodeValue.class);
        verify(codeValueRepository).saveAndFlush(saved.capture());
        return saved.getValue();
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JsonParser.parseString(json), new FromJsonHelper(), "codevalues", CODE_ID, null, null, null, null,
                null, null, null, null, null, null, null, null);
    }
}
