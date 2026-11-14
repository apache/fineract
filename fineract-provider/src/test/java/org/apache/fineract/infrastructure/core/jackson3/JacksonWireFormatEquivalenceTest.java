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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.infrastructure.core.jersey.JacksonWireFormatCharacterizationTest;
import org.apache.fineract.infrastructure.core.jersey.JacksonWireFormatCharacterizationTest.Everything;
import org.apache.fineract.infrastructure.core.jersey.JacksonWireFormatCharacterizationTest.Legacy;
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
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Holds the Jackson 3 mapper that stores commands to what the Jackson 2 mapper behind Jersey does.
 *
 * <p>
 * The two meet on the idempotent-replay path. A command's response is written to the command table by Jackson 3; when
 * the same request arrives again under the same idempotency key, that stored JSON is read back into an object and
 * returned to the client, where Jersey renders it with Jackson 2. The client is entitled to receive exactly the bytes
 * it received the first time, so a disagreement anywhere in that loop is a defect — and a quiet one, because both
 * halves succeed.
 *
 * <p>
 * Since a replay rehydrates the stored object and lets the web layer render it, correctness of the replayed bytes rests
 * on the round trip rather than on these two mappers agreeing — {@code CommandReplayRehydrationTest} is what covers
 * that. What is checked here is that the command table holds what the API would have sent, which keeps a stored row
 * legible against the answer it belongs to and keeps the text comparison in {@code matchesStoredRequest} meaningful.
 * The exception is masking, which is load-bearing: without it a credential reaches the table in the clear.
 */
class JacksonWireFormatEquivalenceTest {

    /** What Jersey renders responses with. */
    private final com.fasterxml.jackson.databind.ObjectMapper api = apiObjectMapper();

    /** What the command store reads and writes with. */
    private final ObjectMapper commandStore = new Jackson3ObjectMapperConfig().jackson3ObjectMapper();

    private static com.fasterxml.jackson.databind.ObjectMapper apiObjectMapper() {
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

    /**
     * What {@code CommandJsonMapper} does to a command's request and response: object to node on the way into the
     * table, node back to object on the way out.
     */
    private <T> T throughTheCommandStore(T value, Class<T> type) {
        ObjectNode stored = commandStore.convertValue(value, ObjectNode.class);

        return commandStore.convertValue(stored, type);
    }

    @Test
    void aReplayedResponseIsIdenticalToTheOriginal() throws Exception {
        Everything response = JacksonWireFormatCharacterizationTest.populated();

        assertEquals(api.writeValueAsString(response), api.writeValueAsString(throughTheCommandStore(response, Everything.class)),
                "A response replayed from the command table must reach the client as the same bytes it sent the first time.");
    }

    @Test
    void theLegacyArrayFormatSurvivesTheCommandStore() throws Exception {
        Legacy response = new Legacy("legacy", LocalDate.of(2026, 10, 5));

        assertEquals(api.writeValueAsString(response), api.writeValueAsString(throughTheCommandStore(response, Legacy.class)),
                "A bean on the legacy LocalDate format must replay in that format too.");
    }

    @Test
    void whatTheCommandStoreWritesIsWhatTheApiWrites() throws Exception {
        Everything response = JacksonWireFormatCharacterizationTest.populated();

        assertEquals(api.writeValueAsString(response), commandStore.writeValueAsString(response),
                "The JSON stored for a command should read the same as the JSON the client was sent.");
    }

    @Test
    void bothReadTheSamePayloadToTheSameObject() throws Exception {
        String request = api.writeValueAsString(JacksonWireFormatCharacterizationTest.populated());

        assertEquals(api.readValue(request, Everything.class), commandStore.readValue(request, Everything.class),
                "A stored request must deserialise to the same object the API would have built from it.");
    }

    @Test
    void anAbsentValueIsOmittedByBoth() throws Exception {
        Everything sparse = Everything.builderOfText("only");

        assertEquals(api.writeValueAsString(sparse), commandStore.writeValueAsString(sparse),
                "Null properties are left out of responses; the stored copy must leave them out too.");
    }

    @Test
    void credentialsAreMaskedBeforeTheyReachTheCommandTable() throws Exception {
        String stored = commandStore.writeValueAsString(JacksonWireFormatCharacterizationTest.populated());

        assertFalse(stored.contains("secret-value"),
                "@JsonSerialize(using = MaskedValueSerializer.class) is a Jackson 2 annotation, so Jackson 3 cannot see it. "
                        + "Without the module reapplying it, external-service credentials land in the command table in the clear.");
    }

    @Test
    void maskingACredentialTwiceLeavesItUnchanged() throws Exception {
        Everything response = JacksonWireFormatCharacterizationTest.populated();

        assertEquals(api.writeValueAsString(response), api.writeValueAsString(throughTheCommandStore(response, Everything.class)),
                "The stored copy is already masked, so replaying it masks it a second time. That has to be a no-op, "
                        + "or the replayed response would differ from the first one.");
    }

    @Test
    void theTypesNoConverterCoversAgreeToo() throws Exception {
        assertEquals(api.writeValueAsString(everyOtherType()), commandStore.writeValueAsString(everyOtherType()),
                "These ride on Jackson's own defaults, several of which changed between 2 and 3.");
    }

    @Test
    void theTypesNoConverterCoversSurviveTheCommandStore() throws Exception {
        UncoveredTypes response = everyOtherType();

        assertEquals(api.writeValueAsString(response), api.writeValueAsString(throughTheCommandStore(response, UncoveredTypes.class)),
                "A response built from these must replay unchanged as well.");
    }

    private static UncoveredTypes everyOtherType() {
        return new UncoveredTypes(ZonedDateTime.parse("2026-10-05T11:22:33.123456789Z"), Instant.parse("2026-10-05T11:22:33.123456789Z"),
                Duration.ofSeconds(90, 500), Period.of(1, 2, 3), MonthDay.of(10, 5), YearMonth.of(2026, 10), Year.of(2026),
                DayOfWeek.MONDAY, Month.OCTOBER, new BigDecimal("12345678901234567890.12345678901"), new BigDecimal("1.50"),
                new BigInteger("123456789012345678901234567890"), 1.5d, 2.5f, Long.MAX_VALUE, 7,
                UUID.fromString("00000000-0000-0000-0000-000000000001"), "text",
                List.of(LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 3)), Map.of("only", LocalDate.of(2026, 1, 2)));
    }

    /**
     * The types no custom converter covers, which are therefore written by whatever each Jackson version does by
     * default — {@code WRITE_DATES_AS_TIMESTAMPS} and {@code WRITE_DURATIONS_AS_TIMESTAMPS} both changed, and
     * {@link Month} changed in a way no feature flag undoes.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UncoveredTypes {

        private ZonedDateTime zonedDateTime;
        private Instant instant;
        private Duration duration;
        private Period period;
        private MonthDay monthDay;
        private YearMonth yearMonth;
        private Year year;
        private DayOfWeek dayOfWeek;
        private Month month;
        private BigDecimal precise;
        private BigDecimal amount;
        private BigInteger bigInteger;
        private Double doubleValue;
        private Float floatValue;
        private Long reference;
        private Integer count;
        private UUID uuid;
        private String text;
        private List<LocalDate> dates;
        private Map<String, LocalDate> datesByName;
    }
}
