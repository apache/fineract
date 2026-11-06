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
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.core.jersey.converter.LocalDateJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JacksonLocalDateArraySerializer;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JsonLocalDateArrayFormat;

public class JacksonLocalDateBeanSerializerModifier extends BeanSerializerModifier {

    // Set while a response is written whose resource method is annotated with @JsonLocalDateArrayFormat
    private static final ThreadLocal<Boolean> LOCAL_DATE_ARRAY_FORMAT = new ThreadLocal<>();

    private final JsonSerializer<LocalDate> localDateSerializer = new JacksonSerializerAdapter<>(new LocalDateJsonConverter());
    private final JsonSerializer<LocalDate> localDateArraySerializer = new JacksonLocalDateArraySerializer();
    private final JsonSerializer<LocalDate> localDateDefaultSerializer = new JsonSerializer<>() {

        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (Boolean.TRUE.equals(LOCAL_DATE_ARRAY_FORMAT.get())) {
                localDateArraySerializer.serialize(value, gen, provider);
            } else {
                localDateSerializer.serialize(value, gen, provider);
            }
        }
    };

    @Override
    @SuppressWarnings("unchecked")
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
            List<BeanPropertyWriter> beanProperties) {
        if (beanDesc.getBeanClass().isAnnotationPresent(JsonLocalDateArrayFormat.class)) {
            assignLocalDateSerializer(beanProperties, (JsonSerializer<Object>) (JsonSerializer<?>) localDateArraySerializer);
        } else {
            assignLocalDateSerializer(beanProperties, (JsonSerializer<Object>) (JsonSerializer<?>) localDateDefaultSerializer);
        }

        return beanProperties;
    }

    /**
     * Runs {@code write} with every date written as an array, including the dates of classes without
     * {@link JsonLocalDateArrayFormat}.
     */
    public static void writeWithLocalDateArrayFormat(final IORunnable write) throws IOException {
        final Boolean previous = LOCAL_DATE_ARRAY_FORMAT.get();
        LOCAL_DATE_ARRAY_FORMAT.set(Boolean.TRUE);
        try {
            write.run();
        } finally {
            if (previous == null) {
                LOCAL_DATE_ARRAY_FORMAT.remove();
            } else {
                LOCAL_DATE_ARRAY_FORMAT.set(previous);
            }
        }
    }

    @FunctionalInterface
    public interface IORunnable {

        void run() throws IOException;
    }

    private void assignLocalDateSerializer(List<BeanPropertyWriter> beanProperties, JsonSerializer<Object> serializer) {
        for (BeanPropertyWriter writer : beanProperties) {
            if (LocalDate.class.equals(writer.getType().getRawClass())) {
                writer.assignSerializer(serializer);
            }
        }
    }
}
