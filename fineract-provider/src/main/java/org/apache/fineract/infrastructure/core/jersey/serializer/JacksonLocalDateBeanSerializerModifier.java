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

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import org.apache.fineract.infrastructure.core.jersey.converter.LocalDateJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JacksonLocalDateArraySerializer;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JacksonZonedDateTimeEpochSerializer;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JsonLocalDateArrayFormat;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JsonZonedDateTimeEpochFormat;

public class JacksonLocalDateBeanSerializerModifier extends BeanSerializerModifier {

    private final JsonSerializer<?> localDateSerializer = new JacksonSerializerAdapter<>(new LocalDateJsonConverter());
    private final JsonSerializer<?> localDateArraySerializer = new JacksonLocalDateArraySerializer();
    private final JsonSerializer<?> zonedDateTimeEpochSerializer = new JacksonZonedDateTimeEpochSerializer();

    @Override
    @SuppressWarnings("unchecked")
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
            List<BeanPropertyWriter> beanProperties) {
        if (beanDesc.getBeanClass().isAnnotationPresent(JsonLocalDateArrayFormat.class)) {
            assignSerializer(beanProperties, LocalDate.class, (JsonSerializer<Object>) localDateArraySerializer);
        } else {
            assignSerializer(beanProperties, LocalDate.class, (JsonSerializer<Object>) localDateSerializer);
        }
        if (beanDesc.getBeanClass().isAnnotationPresent(JsonZonedDateTimeEpochFormat.class)) {
            assignSerializer(beanProperties, ZonedDateTime.class, (JsonSerializer<Object>) zonedDateTimeEpochSerializer);
        }

        return beanProperties;
    }

    private void assignSerializer(List<BeanPropertyWriter> beanProperties, Class<?> propertyType, JsonSerializer<Object> serializer) {
        for (BeanPropertyWriter writer : beanProperties) {
            if (propertyType.equals(writer.getType().getRawClass())) {
                writer.assignSerializer(serializer);
            }
        }
    }
}
