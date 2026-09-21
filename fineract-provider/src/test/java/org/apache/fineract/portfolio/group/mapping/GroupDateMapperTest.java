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
package org.apache.fineract.portfolio.group.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.junit.jupiter.api.Test;

class GroupDateMapperTest {

    private final GroupDateMapper mapper = new GroupDateMapper();

    @Test
    void parsesLegacyFormatWithLocale() {
        assertEquals(LocalDate.of(2024, 1, 5), mapper.toLocalDate("05 January 2024", "activationDate", "dd MMMM yyyy", "en"));
    }

    @Test
    void parsesNonEnglishLocale() {
        assertEquals(LocalDate.of(2024, 1, 5), mapper.toLocalDate("05 janvier 2024", "activationDate", "dd MMMM yyyy", "fr"));
    }

    @Test
    void blankIsNull() {
        assertNull(mapper.toLocalDate(" ", "activationDate", "dd MMMM yyyy", "en"));
        assertNull(mapper.toLocalDate(null, "activationDate", "dd MMMM yyyy", "en"));
    }

    @Test
    void unparsableThrowsLegacyValidationException() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> mapper.toLocalDate("2024-01-05", "activationDate", "dd MMMM yyyy", "en"));
        assertEquals("activationDate", ex.getErrors().get(0).getParameterName());
    }

    @Test
    void missingDateFormatThrowsLegacyValidationException() {
        assertThrows(PlatformApiDataValidationException.class, () -> mapper.toLocalDate("05 January 2024", "activationDate", null, "en"));
    }
}
