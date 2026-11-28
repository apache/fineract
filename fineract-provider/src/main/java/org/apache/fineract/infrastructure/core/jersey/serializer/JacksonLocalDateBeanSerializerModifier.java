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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.jersey.converter.LocalDateJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JacksonLocalDateArraySerializer;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JacksonZonedDateTimeEpochSerializer;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JsonLocalDateArrayFormat;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JsonZonedDateTimeEpochFormat;

public class JacksonLocalDateBeanSerializerModifier extends BeanSerializerModifier {

    // Set while a response is written whose resource method is annotated with @JsonLocalDateArrayFormat
    private static final ThreadLocal<Boolean> LOCAL_DATE_ARRAY_FORMAT = new ThreadLocal<>();

    private final JsonSerializer<LocalDate> localDateSerializer = new JacksonSerializerAdapter<>(new LocalDateJsonConverter());
    private final JsonSerializer<LocalDate> localDateArraySerializer = new JacksonLocalDateArraySerializer();
    private final JsonSerializer<LocalDate> localDateDefaultSerializer = new LocalDateDefaultSerializer(localDateSerializer,
            localDateArraySerializer);
    private final JsonSerializer<?> zonedDateTimeEpochSerializer = new JacksonZonedDateTimeEpochSerializer();

    @Override
    @SuppressWarnings("unchecked")
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
            List<BeanPropertyWriter> beanProperties) {
        if (beanDesc.getBeanClass().isAnnotationPresent(JsonLocalDateArrayFormat.class)) {
            assignSerializer(beanProperties, LocalDate.class, (JsonSerializer<Object>) (JsonSerializer<?>) localDateArraySerializer);
        } else {
            assignSerializer(beanProperties, LocalDate.class, (JsonSerializer<Object>) (JsonSerializer<?>) localDateDefaultSerializer);
        }
        if (beanDesc.getBeanClass().isAnnotationPresent(JsonZonedDateTimeEpochFormat.class)) {
            assignSerializer(beanProperties, ZonedDateTime.class, (JsonSerializer<Object>) zonedDateTimeEpochSerializer);
        }

        return beanProperties;
    }

    /**
     * Runs {@code action} with every date written as an array, including the dates of classes without
     * {@link JsonLocalDateArrayFormat}, and returns its result.
     */
    public static <T, E extends Exception> T withLocalDateArrayFormat(final ThrowingSupplier<T, E> action) throws E {
        final Boolean previous = LOCAL_DATE_ARRAY_FORMAT.get();
        LOCAL_DATE_ARRAY_FORMAT.set(Boolean.TRUE);
        try {
            return action.get();
        } finally {
            if (previous == null) {
                LOCAL_DATE_ARRAY_FORMAT.remove();
            } else {
                LOCAL_DATE_ARRAY_FORMAT.set(previous);
            }
        }
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T, E extends Exception> {

        T get() throws E;
    }

    /** Writes a date as an array while {@link #withLocalDateArrayFormat} runs, and as a string otherwise. */
    @RequiredArgsConstructor
    private static final class LocalDateDefaultSerializer extends JsonSerializer<LocalDate> implements Serializable {

        private final JsonSerializer<LocalDate> localDateSerializer;
        private final JsonSerializer<LocalDate> localDateArraySerializer;

        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (Boolean.TRUE.equals(LOCAL_DATE_ARRAY_FORMAT.get())) {
                localDateArraySerializer.serialize(value, gen, provider);
            } else {
                localDateSerializer.serialize(value, gen, provider);
            }
        }
    }

    private void assignSerializer(List<BeanPropertyWriter> beanProperties, Class<?> propertyType, JsonSerializer<Object> serializer) {
        for (BeanPropertyWriter writer : beanProperties) {
            if (propertyType.equals(writer.getType().getRawClass())) {
                writer.assignSerializer(serializer);
            }
        }
    }
}
