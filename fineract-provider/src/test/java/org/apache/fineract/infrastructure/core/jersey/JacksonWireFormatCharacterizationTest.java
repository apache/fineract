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
package org.apache.fineract.infrastructure.core.jersey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.jersey.converter.DateJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.converter.ExternalIdJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.converter.JsonConverter;
import org.apache.fineract.infrastructure.core.jersey.converter.LocalDateTimeJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.converter.LocalTimeJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.converter.OffsetDateTimeJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.serializer.JacksonDeserializerAdapter;
import org.apache.fineract.infrastructure.core.jersey.serializer.JacksonSerializerAdapter;
import org.apache.fineract.infrastructure.core.jersey.serializer.MaskedValueSerializer;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JacksonLocalDateArrayModule;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JsonLocalDateArrayFormat;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Pins the exact JSON the REST API produces and accepts.
 *
 * <p>
 * These strings are not a specification anyone wrote down; they are what the application has always sent, and clients
 * have been built against them. Every value below was captured from the running mapper rather than reasoned out, so a
 * failure here means the wire format moved — which is a breaking change for every caller, whatever caused it.
 */
public class JacksonWireFormatCharacterizationTest {

    private final ObjectMapper mapper = webObjectMapper();

    /** Built exactly as {@link JerseyJacksonConverterConfig} builds the mapper Jersey serialises responses with. */
    private static ObjectMapper webObjectMapper() {
        List<JsonConverter<?>> converters = List.of(new LocalDateTimeJsonConverter(), new OffsetDateTimeJsonConverter(),
                new LocalTimeJsonConverter(), new DateJsonConverter(), new ExternalIdJsonConverter());

        List<JsonSerializer<?>> serializers = new ArrayList<>(converters.stream().map(JacksonSerializerAdapter::new).toList());
        List<JsonDeserializer<?>> deserializers = new ArrayList<>(converters.stream().map(JacksonDeserializerAdapter::new).toList());

        ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder() //
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
    void everyHandledTypeSerialisesToItsEstablishedShape() throws Exception {
        assertEquals("""
                {"text":"hello","flag":true,"count":7,"reference":9223372036854775807,\
                "amount":1.50,"precise":12345678901234567890.12345678901,"status":"ACTIVE",\
                "localDate":"2026-10-05","localDateTime":"2026-10-05T11:22:33.123456789",\
                "offsetDateTime":"2026-10-05T11:22:33.123456789Z","localTime":"11:22:33.123456789",\
                "date":"2026-10-05T11:22:33Z","externalId":"ext-1","secret":"s*******alue",\
                "nested":{"text":"inner","localDate":"2026-01-02"},"dates":[[2026,1,2],[2026,1,3]]}""",
                mapper.writeValueAsString(populated()), "The JSON the API produces must not change.");
        // note the asymmetry: a LocalDate property is written as a string, but a List<LocalDate> is written as arrays,
        // because the serializer modifier only reassigns properties whose raw type is LocalDate. Clients depend on
        // both shapes as they are.
    }

    @Test
    void nullsAreOmittedRatherThanWritten() throws Exception {
        assertEquals("{\"text\":\"only\"}", mapper.writeValueAsString(Everything.builderOfText("only")),
                "Null properties are left out of responses; writing them would change every payload.");
    }

    @Test
    void aBeanMarkedLegacyKeepsTheArrayShapeForLocalDate() throws Exception {
        assertEquals("{\"text\":\"legacy\",\"localDate\":[2026,10,5]}",
                mapper.writeValueAsString(new Legacy("legacy", LocalDate.of(2026, 10, 5))),
                "Beans marked for the legacy format must keep writing LocalDate as a [year, month, day] array.");
    }

    @Test
    void whatTheApiAcceptsIsUnchanged() throws Exception {
        var parsed = mapper.readValue("""
                {"text":"hello","localDate":"2026-10-05","localDateTime":"2026-10-05T11:22:33.123456789",\
                "offsetDateTime":"2026-10-05T11:22:33.123456789Z","localTime":"11:22:33.123456789",\
                "date":"2026-10-05T11:22:33Z","externalId":"ext-1","amount":1.50,"unknown":"ignored"}""", Everything.class);

        assertEquals("hello", parsed.getText());
        assertEquals(LocalDate.of(2026, 10, 5), parsed.getLocalDate());
        assertEquals(LocalDateTime.parse("2026-10-05T11:22:33.123456789"), parsed.getLocalDateTime());
        assertEquals(OffsetDateTime.parse("2026-10-05T11:22:33.123456789Z"), parsed.getOffsetDateTime());
        assertEquals(LocalTime.parse("11:22:33.123456789"), parsed.getLocalTime());
        assertEquals("ext-1", parsed.getExternalId().getValue());
        assertEquals(new BigDecimal("1.50"), parsed.getAmount(), "A decimal must keep its scale when read.");
        assertNull(parsed.getStatus(), "An absent property stays null.");
    }

    @Test
    void anUnknownPropertyIsIgnoredRatherThanRejected() throws Exception {
        assertEquals("hello", mapper.readValue("{\"text\":\"hello\",\"nope\":1}", Everything.class).getText());
    }

    @Test
    void anEnumIsAcceptedWhateverItsCase() throws Exception {
        assertEquals(Status.ACTIVE, mapper.readValue("{\"status\":\"active\"}", Everything.class).getStatus());
    }

    public static Everything populated() {
        var nested = new Nested("inner", LocalDate.of(2026, 1, 2));

        return new Everything("hello", true, 7, Long.MAX_VALUE, new BigDecimal("1.50"), new BigDecimal("12345678901234567890.12345678901"),
                Status.ACTIVE, LocalDate.of(2026, 10, 5), LocalDateTime.parse("2026-10-05T11:22:33.123456789"),
                OffsetDateTime.parse("2026-10-05T11:22:33.123456789Z"), LocalTime.parse("11:22:33.123456789"),
                Date.from(java.time.Instant.parse("2026-10-05T11:22:33Z")), new ExternalId("ext-1"), "secret-value", nested,
                List.of(LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 3)));
    }

    public enum Status {
        ACTIVE, CLOSED
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Everything {

        private String text;
        private Boolean flag;
        private Integer count;
        private Long reference;
        private BigDecimal amount;
        private BigDecimal precise;
        private Status status;
        private LocalDate localDate;
        private LocalDateTime localDateTime;
        private OffsetDateTime offsetDateTime;
        private LocalTime localTime;
        private Date date;
        private ExternalId externalId;
        @JsonSerialize(using = MaskedValueSerializer.class)
        private String secret;
        private Nested nested;
        private List<LocalDate> dates;

        public static Everything builderOfText(String text) {
            var everything = new Everything();

            everything.setText(text);

            return everything;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Nested {

        private String text;
        private LocalDate localDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonLocalDateArrayFormat
    public static class Legacy {

        private String text;
        private LocalDate localDate;
    }
}
