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
package org.apache.fineract.infrastructure.core.jackson3;

import static org.apache.fineract.command.core.CommandConstants.COMMAND_JSON_CLASS_ATTRIBUTE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.command.jdbc.store.mapping.JsonCommandMapper;
import org.apache.fineract.infrastructure.core.jersey.converter.DateJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.converter.ExternalIdJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.converter.JsonConverter;
import org.apache.fineract.infrastructure.core.jersey.converter.LocalDateTimeJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.converter.LocalTimeJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.converter.OffsetDateTimeJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.serializer.JacksonDeserializerAdapter;
import org.apache.fineract.infrastructure.core.jersey.serializer.JacksonSerializerAdapter;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JacksonLocalDateArrayModule;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Walks a command result through the replay path exactly as production does, and checks the client cannot tell.
 *
 * <p>
 * A command's result is written to the command table as an object by {@link JsonCommandMapper}, on Jackson 3. A repeat
 * of the request reads that row back into an object and hands it to the web layer, which renders it with the same
 * Jackson 2 mapper that rendered the first answer. Nothing re-renders stored text, so the two answers match because
 * they are the same object through the same serializer — not because two mappers were talked into agreeing.
 *
 * <p>
 * What that leaves to check is the round trip in the middle: the stored object has to come back carrying every value it
 * went in with. A decimal that loses its scale, a date that loses its precision or a credential that re-masks
 * differently would all survive storage and still change the answer.
 */
class CommandReplayRehydrationTest {

    /** What Jersey renders responses with, first time and on replay alike. */
    private final com.fasterxml.jackson.databind.ObjectMapper jersey = jerseyObjectMapper();

    private final ObjectMapper commandStoreMapper = new Jackson3ObjectMapperConfig().jackson3ObjectMapper();

    private final JsonCommandMapper commandJsonMapper = new JsonCommandMapper(commandStoreMapper);

    private static com.fasterxml.jackson.databind.ObjectMapper jerseyObjectMapper() {
        List<JsonConverter<?>> converters = List.of(new LocalDateTimeJsonConverter(), new OffsetDateTimeJsonConverter(),
                new LocalTimeJsonConverter(), new DateJsonConverter(), new ExternalIdJsonConverter());

        List<JsonSerializer<?>> serializers = new ArrayList<>(converters.stream().map(JacksonSerializerAdapter::new).toList());
        List<JsonDeserializer<?>> deserializers = new ArrayList<>(converters.stream().map(JacksonDeserializerAdapter::new).toList());

        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder() //
                .serializers(serializers.toArray(new JsonSerializer[0])) //
                .serializationInclusion(JsonInclude.Include.NON_NULL) //
                .deserializers(deserializers.toArray(new JsonDeserializer[0])) //
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) //
                .featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS) //
                .build();

        objectMapper.registerModule(new JacksonLocalDateArrayModule());

        return objectMapper;
    }

    @Test
    void aReplayedAnswerIsByteForByteTheOriginalAnswer() throws Exception {
        CommandResponseSample result = CommandResponseSample.populated();

        String originalAnswer = jersey.writeValueAsString(result);

        JsonNode stored = commandJsonMapper.map(result);
        CommandResponseSample rehydrated = commandJsonMapper.map(stored);

        assertNotNull(rehydrated, "The stored result must rehydrate; a null here means the type attribute did not resolve.");
        assertEquals(originalAnswer, jersey.writeValueAsString(rehydrated),
                "A repeat of the request must be answered with the bytes the first one was answered with.");
    }

    @Test
    void everyValueSurvivesTheRoundTrip() {
        CommandResponseSample result = CommandResponseSample.populated();

        CommandResponseSample rehydrated = commandJsonMapper.map(commandJsonMapper.map(result));

        assertEquals(result.getAmount(), rehydrated.getAmount(), "A decimal must come back with its scale intact.");
        assertEquals(result.getPrecise(), rehydrated.getPrecise(), "A high-precision decimal must not round.");
        assertEquals(result.getOffsetDateTime(), rehydrated.getOffsetDateTime(), "Nanosecond precision must survive.");
        assertEquals(result.getLocalDateTime(), rehydrated.getLocalDateTime());
        assertEquals(result.getLocalTime(), rehydrated.getLocalTime());
        assertEquals(result.getLocalDate(), rehydrated.getLocalDate());
        assertEquals(result.getDate(), rehydrated.getDate());
        assertEquals(result.getExternalId(), rehydrated.getExternalId());
        assertEquals(result.getResourceId(), rehydrated.getResourceId());
        assertEquals(result.getStatus(), rehydrated.getStatus());
        assertEquals(result.getDates(), rehydrated.getDates());
    }

    @Test
    void theStoredRowCarriesItsTypeAndTheReplayDoesNot() throws Exception {
        CommandResponseSample result = CommandResponseSample.populated();

        JsonNode stored = commandJsonMapper.map(result);

        assertTrue(stored.has(COMMAND_JSON_CLASS_ATTRIBUTE), "The store records the result's type so it can be read back.");
        assertFalse(jersey.writeValueAsString(commandJsonMapper.map(stored)).contains(COMMAND_JSON_CLASS_ATTRIBUTE),
                "That type attribute is the store's bookkeeping and was never part of an answer, so a replay must not carry it.");
    }

    @Test
    void aCredentialIsStoredMaskedAndStillReplaysUnchanged() throws Exception {
        CommandResponseSample result = CommandResponseSample.populated();

        JsonNode stored = commandJsonMapper.map(result);

        assertFalse(stored.toString().contains("secret-value"), "Credentials must not reach the command table in the clear.");
        assertEquals(jersey.writeValueAsString(result), jersey.writeValueAsString(commandJsonMapper.map(stored)),
                "The stored copy is already masked, so the replay masks it a second time. That has to be a no-op.");
    }
}
