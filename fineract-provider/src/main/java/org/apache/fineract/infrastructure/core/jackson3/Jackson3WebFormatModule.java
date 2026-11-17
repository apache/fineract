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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.jersey.serializer.MaskedValueSerializer;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JsonLocalDateArrayFormat;
import org.apache.fineract.infrastructure.core.service.StringUtil;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.ValueSerializerModifier;

/**
 * Teaches a Jackson 3 mapper to read and write exactly what the Jackson 2 mapper behind Jersey does.
 *
 * <p>
 * Commands are persisted by {@code fineract-command-jdbc}, which runs on Jackson 3, while responses reach the client
 * through Jersey, which can only run on Jackson 2. A command's stored response is replayed when the same request
 * arrives again under the same idempotency key, so the two have to agree: whatever Jackson 3 writes into the command
 * table has to come back out as an object that Jackson 2 then renders byte for byte like the original response.
 *
 * <p>
 * This mirrors the converters in {@code JerseyJacksonConverterConfig} rather than sharing them, because that converter
 * interface is typed against Jackson 2's {@code JsonParser} and {@code JsonGenerator}. The formats are the same
 * {@link DateTimeFormatter} constants, and {@code JacksonWireFormatEquivalenceTest} fails if the two ever drift apart.
 */
public class Jackson3WebFormatModule extends SimpleModule {

    public Jackson3WebFormatModule() {
        addFormatted(LocalDateTime.class, DateTimeFormatter.ISO_LOCAL_DATE_TIME, LocalDateTime::parse);
        addFormatted(LocalTime.class, DateTimeFormatter.ISO_LOCAL_TIME, LocalTime::parse);
        addFormatted(OffsetDateTime.class, DateTimeFormatter.ISO_OFFSET_DATE_TIME, OffsetDateTime::parse);

        addSerializer(Date.class, new DateSerializer());
        addDeserializer(Date.class, new DateDeserializer());
        addSerializer(ExternalId.class, new ExternalIdSerializer());
        addDeserializer(ExternalId.class, new ExternalIdDeserializer());

        // Jackson 2 has no Month handler, so it falls through to ordinary enum handling and writes "OCTOBER". Jackson 3
        // added one that always writes a number, one-based by default. That is worse than a reformatting: a client
        // reading 10 back through Jackson 2 resolves it as an ordinal and gets NOVEMBER, silently. No feature flag
        // restores the name.
        addSerializer(Month.class, new MonthNameSerializer());
        addDeserializer(Month.class, new MonthNameDeserializer());

        setSerializerModifier(new LocalDateAndMaskedPropertyModifier());
    }

    private <T extends TemporalAccessor> void addFormatted(Class<T> type, DateTimeFormatter formatter,
            BiFunction<String, DateTimeFormatter, T> parser) {
        addSerializer(type, new FormattedSerializer<>(type, formatter));
        addDeserializer(type, new FormattedDeserializer<>(type, formatter, parser));
    }

    private static final class FormattedSerializer<T extends TemporalAccessor> extends ValueSerializer<T> {

        private final Class<T> type;
        private final DateTimeFormatter formatter;

        private FormattedSerializer(Class<T> type, DateTimeFormatter formatter) {
            this.type = type;
            this.formatter = formatter;
        }

        @Override
        public void serialize(T value, JsonGenerator gen, SerializationContext context) {
            gen.writeString(formatter.format(value));
        }

        @Override
        public Class<T> handledType() {
            return type;
        }
    }

    private static final class FormattedDeserializer<T> extends ValueDeserializer<T> {

        private final Class<T> type;
        private final DateTimeFormatter formatter;
        private final BiFunction<String, DateTimeFormatter, T> parser;

        private FormattedDeserializer(Class<T> type, DateTimeFormatter formatter, BiFunction<String, DateTimeFormatter, T> parser) {
            this.type = type;
            this.formatter = formatter;
            this.parser = parser;
        }

        @Override
        public T deserialize(JsonParser jsonParser, DeserializationContext context) {
            // The Jackson 2 converters read a string and yield null for anything else, rather than failing.
            return jsonParser.hasToken(JsonToken.VALUE_STRING) ? parser.apply(jsonParser.getString(), formatter) : null;
        }

        @Override
        public Class<T> handledType() {
            return type;
        }
    }

    private static final class DateSerializer extends ValueSerializer<Date> {

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializationContext context) {
            gen.writeString(DateTimeFormatter.ISO_INSTANT.format(value.toInstant()));
        }

        @Override
        public Class<Date> handledType() {
            return Date.class;
        }
    }

    private static final class DateDeserializer extends ValueDeserializer<Date> {

        @Override
        public Date deserialize(JsonParser parser, DeserializationContext context) {
            return parser.hasToken(JsonToken.VALUE_STRING)
                    ? Date.from(Instant.from(DateTimeFormatter.ISO_INSTANT.parse(parser.getString())))
                    : null;
        }

        @Override
        public Class<Date> handledType() {
            return Date.class;
        }
    }

    private static final class ExternalIdSerializer extends ValueSerializer<ExternalId> {

        @Override
        public void serialize(ExternalId value, JsonGenerator gen, SerializationContext context) {
            if (value != null && !value.isEmpty()) {
                gen.writeString(value.getValue());
            } else {
                gen.writeNull();
            }
        }

        @Override
        public Class<ExternalId> handledType() {
            return ExternalId.class;
        }
    }

    private static final class ExternalIdDeserializer extends ValueDeserializer<ExternalId> {

        @Override
        public ExternalId deserialize(JsonParser parser, DeserializationContext context) {
            return parser.hasToken(JsonToken.VALUE_STRING) ? new ExternalId(parser.getString()) : ExternalId.empty();
        }

        @Override
        public Class<ExternalId> handledType() {
            return ExternalId.class;
        }
    }

    private static final class MonthNameSerializer extends ValueSerializer<Month> {

        @Override
        public void serialize(Month value, JsonGenerator gen, SerializationContext context) {
            gen.writeString(value.name());
        }

        @Override
        public Class<Month> handledType() {
            return Month.class;
        }
    }

    private static final class MonthNameDeserializer extends ValueDeserializer<Month> {

        @Override
        public Month deserialize(JsonParser parser, DeserializationContext context) {
            if (parser.currentToken() == JsonToken.VALUE_NUMBER_INT) {
                // Jackson 2 reads a number as the enum's ordinal, so a zero-based index, and so does this.
                return Month.values()[parser.getIntValue()];
            }
            return Month.valueOf(parser.getString().toUpperCase(Locale.ROOT));
        }

        @Override
        public Class<Month> handledType() {
            return Month.class;
        }
    }

    /**
     * The Jackson 3 counterpart of {@code JacksonLocalDateBeanSerializerModifier}, which also carries the masking that
     * Jackson 3 would otherwise drop.
     *
     * <p>
     * Both jobs belong here rather than in the module's type registrations. Registering a {@link LocalDate} serializer
     * outright would also catch the ones inside collections, and Jackson 2 does not: a {@code LocalDate} property is
     * written as a string while a {@code List<LocalDate>} is written as {@code [year, month, day]} arrays. Clients
     * depend on both shapes as they are.
     *
     * <p>
     * Masking has to be reapplied because {@code @JsonSerialize(using = MaskedValueSerializer.class)} is a <em>Jackson
     * 2</em> annotation, which Jackson 3 neither sees nor could use the serializer it names. Left alone, a command
     * carrying external-service credentials would write them to the command table in the clear.
     */
    private static final class LocalDateAndMaskedPropertyModifier extends ValueSerializerModifier {

        private final ValueSerializer<Object> localDateAsString = cast(
                new FormattedSerializer<>(LocalDate.class, DateTimeFormatter.ISO_LOCAL_DATE));
        private final ValueSerializer<Object> localDateAsArray = cast(new LocalDateArraySerializer());
        private final ValueSerializer<Object> masked = cast(new MaskedSerializer());

        @SuppressWarnings("unchecked")
        private static ValueSerializer<Object> cast(ValueSerializer<?> serializer) {
            return (ValueSerializer<Object>) serializer;
        }

        @Override
        public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription.Supplier beanDesc,
                List<BeanPropertyWriter> beanProperties) {
            boolean legacyArrayFormat = beanDesc.getBeanClass().isAnnotationPresent(JsonLocalDateArrayFormat.class);

            for (BeanPropertyWriter writer : beanProperties) {
                if (LocalDate.class.equals(writer.getType().getRawClass())) {
                    writer.assignSerializer(legacyArrayFormat ? localDateAsArray : localDateAsString);
                } else if (isMasked(writer)) {
                    writer.assignSerializer(masked);
                }
            }

            return beanProperties;
        }

        private static boolean isMasked(BeanPropertyWriter writer) {
            var annotation = writer.getAnnotation(com.fasterxml.jackson.databind.annotation.JsonSerialize.class);

            return annotation != null && MaskedValueSerializer.class.equals(annotation.using());
        }
    }

    private static final class LocalDateArraySerializer extends ValueSerializer<LocalDate> {

        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializationContext context) {
            if (value == null) {
                gen.writeNull();
                return;
            }
            gen.writeStartArray();
            gen.writeNumber(value.getYear());
            gen.writeNumber(value.getMonthValue());
            gen.writeNumber(value.getDayOfMonth());
            gen.writeEndArray();
        }

        @Override
        public Class<LocalDate> handledType() {
            return LocalDate.class;
        }
    }

    private static final class MaskedSerializer extends ValueSerializer<String> {

        @Override
        public void serialize(String value, JsonGenerator gen, SerializationContext context) {
            gen.writeString(value == null || value.isBlank() ? value : StringUtil.maskValue(value));
        }

        @Override
        public Class<String> handledType() {
            return String.class;
        }
    }
}
