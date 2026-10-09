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
package org.apache.fineract.infrastructure.core.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.database.DatabaseType;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DatatableCommandFromApiJsonDeserializerTest {

    private DatatableCommandFromApiJsonDeserializer underTest;

    @BeforeEach
    void setUp() {
        DatabaseTypeResolver databaseTypeResolver = mock(DatabaseTypeResolver.class);
        when(databaseTypeResolver.databaseType()).thenReturn(DatabaseType.POSTGRESQL);
        underTest = new DatatableCommandFromApiJsonDeserializer(new FromJsonHelper(), databaseTypeResolver);
    }

    @Test
    void stringColumnWithoutLengthNamesTheColumnInAReadableMessage() {
        String json = "{\"datatableName\":\"dt_loan_note\",\"apptableName\":\"m_loan\",\"multiRow\":false,"
                + "\"columns\":[{\"name\":\"note\",\"type\":\"String\",\"mandatory\":true}]}";

        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.validateForCreate(json));

        List<ApiParameterError> errors = exception.getErrors();
        assertEquals(1, errors.size());
        ApiParameterError error = errors.get(0);
        assertEquals("length", error.getParameterName());
        assertEquals("Column length is required for String columns (note).", error.getDefaultUserMessage());
        assertEquals("validation.msg.datatable.length.must.be.provided.when.type.is.String", error.getUserMessageGlobalisationCode());
    }
}
