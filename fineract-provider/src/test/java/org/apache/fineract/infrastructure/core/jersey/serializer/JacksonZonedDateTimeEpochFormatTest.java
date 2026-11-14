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
package org.apache.fineract.infrastructure.core.jersey.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JacksonLocalDateArrayModule;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JsonZonedDateTimeEpochFormat;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * The legacy epoch format must stay scoped to the classes that ask for it: resources that were already serialized by
 * Jackson, such as the audit ones, must keep the format they have today.
 */
class JacksonZonedDateTimeEpochFormatTest {

    private static final ZonedDateTime TIMESTAMP = ZonedDateTime.of(2026, 11, 5, 10, 0, 0, 0, ZoneOffset.UTC);

    private final ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder().build().registerModule(new JacksonLocalDateArrayModule());

    @Getter
    @RequiredArgsConstructor
    @JsonZonedDateTimeEpochFormat
    static class Annotated {

        private final ZonedDateTime madeOnDate;
    }

    @Getter
    @RequiredArgsConstructor
    static class NotAnnotated {

        private final ZonedDateTime madeOnDate;
    }

    @Test
    void anAnnotatedClassWritesEpochMilliseconds() throws Exception {
        final JsonNode json = objectMapper.valueToTree(new Annotated(TIMESTAMP));

        assertTrue(json.get("madeOnDate").isNumber(), "expected a number, got " + json.get("madeOnDate"));
        assertEquals(TIMESTAMP.toInstant().toEpochMilli(), json.get("madeOnDate").asLong());
    }

    @Test
    void anUnannotatedClassIsUntouched() throws Exception {
        final JsonNode json = objectMapper.valueToTree(new NotAnnotated(TIMESTAMP));

        assertNotEquals(TIMESTAMP.toInstant().toEpochMilli(), json.get("madeOnDate").asLong(),
                "the legacy epoch format must not leak into classes that did not ask for it");
    }
}
