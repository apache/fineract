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
package org.apache.fineract.infrastructure.core.jersey.serializer.legacy;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.util.NameTransformer;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Date;

/**
 * Response-scoped compatibility on the shared mapper. Cached serializers inspect a writer attribute on every write, so
 * legacy dates and filters cannot leak into an ordinary typed response or a subsequent request.
 */
public class LegacyApiResponseModule extends SimpleModule {

    public LegacyApiResponseModule() {
        super("LegacyApiResponse");
        setSerializerModifier(new ResponseModifier());
    }

    public static JsonSerializer<?> localDateSerializer(JsonSerializer<?> delegate) {
        return new ResponseSerializer(delegate, LocalDate.class, false, false);
    }

    private static final class ResponseModifier extends BeanSerializerModifier {

        @Override
        public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription bean, JsonSerializer<?> serializer) {
            Class<?> type = bean.getBeanClass();
            if (serializer instanceof ResponseSerializer) {
                return serializer;
            }
            boolean beanFilter = serializer instanceof BeanSerializerBase
                    && config.getAnnotationIntrospector().findFilterId(bean.getClassInfo()) == null;
            return beanFilter || isDateType(type) ? new ResponseSerializer(serializer, type, beanFilter, false) : serializer;
        }

        @Override
        public JsonSerializer<?> modifyMapSerializer(SerializationConfig config, MapType type, BeanDescription bean,
                JsonSerializer<?> serializer) {
            return serializer instanceof MapSerializer ? new ResponseSerializer(serializer, type.getRawClass(), false, false) : serializer;
        }
    }

    private static boolean isDateType(Class<?> type) {
        return type == LocalDate.class || type == LocalDateTime.class || type == LocalTime.class || type == MonthDay.class
                || type == ZonedDateTime.class || type == OffsetDateTime.class || type == OffsetTime.class
                || Date.class.isAssignableFrom(type);
    }

    private static final class ResponseSerializer extends JsonSerializer<Object>
            implements ContextualSerializer, ResolvableSerializer, Serializable {

        private final JsonSerializer<Object> delegate;
        private final Class<?> type;
        private final boolean beanFilter;
        private final boolean explicitFormat;

        @SuppressWarnings("unchecked")
        private ResponseSerializer(JsonSerializer<?> delegate, Class<?> type, boolean beanFilter, boolean explicitFormat) {
            this.delegate = (JsonSerializer<Object>) delegate;
            this.type = type;
            this.beanFilter = beanFilter;
            this.explicitFormat = explicitFormat;
        }

        @Override
        public void serialize(Object value, JsonGenerator generator, SerializerProvider provider) throws IOException {
            if (isLegacy(provider) && !explicitFormat && isDateType(type)) {
                writeDate(value, generator);
            } else {
                responseDelegate(provider).serialize(value, generator, provider);
            }
        }

        @Override
        public void serializeWithType(Object value, JsonGenerator generator, SerializerProvider provider, TypeSerializer typeSerializer)
                throws IOException {
            responseDelegate(provider).serializeWithType(value, generator, provider, typeSerializer);
        }

        private JsonSerializer<Object> responseDelegate(SerializerProvider provider) {
            if (isLegacy(provider)) {
                if (beanFilter && delegate instanceof BeanSerializerBase bean) {
                    return bean.withFilterId(LegacyApiResponseSerializer.FILTER_ID);
                }
                if ((JsonSerializer<?>) delegate instanceof MapSerializer map) {
                    return objectSerializer(map.withContentInclusion(new LegacyApiResponseSerializer.AbsentValueFilter(), true));
                }
            }
            return delegate;
        }

        @SuppressWarnings("unchecked")
        private JsonSerializer<Object> objectSerializer(JsonSerializer<?> serializer) {
            return (JsonSerializer<Object>) serializer;
        }

        private boolean isLegacy(SerializerProvider provider) {
            return Boolean.TRUE.equals(provider.getAttribute(LegacyApiResponseSerializer.ATTRIBUTE));
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property) throws JsonMappingException {
            JsonSerializer<?> contextual = provider.handleSecondaryContextualization(delegate, property);
            boolean hasFormat = explicitFormat || (property != null && property.getAnnotation(JsonFormat.class) != null);
            boolean useFilter = beanFilter && (property == null || property.getAnnotation(JsonFilter.class) == null);
            return new ResponseSerializer(contextual, type, useFilter, hasFormat);
        }

        @Override
        public void resolve(SerializerProvider provider) throws JsonMappingException {
            if (delegate instanceof ResolvableSerializer resolvable) {
                resolvable.resolve(provider);
            }
        }

        @Override
        public boolean isEmpty(SerializerProvider provider, Object value) {
            return delegate.isEmpty(provider, value);
        }

        @Override
        public boolean usesObjectId() {
            return delegate.usesObjectId();
        }

        @Override
        public boolean isUnwrappingSerializer() {
            return delegate.isUnwrappingSerializer();
        }

        @Override
        public JsonSerializer<Object> unwrappingSerializer(NameTransformer transformer) {
            return new ResponseSerializer(delegate.unwrappingSerializer(transformer), type, beanFilter, explicitFormat);
        }

        @Override
        public JsonSerializer<?> getDelegatee() {
            return delegate;
        }

        private void writeDate(Object value, JsonGenerator generator) throws IOException {
            if (value instanceof LocalDate date) {
                generator.writeArray(new int[] { date.get(ChronoField.YEAR_OF_ERA), date.getMonthValue(), date.getDayOfMonth() }, 0, 3);
            } else if (value instanceof LocalDateTime date) {
                generator.writeArray(new int[] { date.get(ChronoField.YEAR_OF_ERA), date.getMonthValue(), date.getDayOfMonth(),
                        date.getHour(), date.getMinute(), date.getSecond() }, 0, 6);
            } else if (value instanceof LocalTime time) {
                generator.writeArray(new int[] { time.getHour(), time.getMinute(), time.getSecond() }, 0, 3);
            } else if (value instanceof MonthDay date) {
                generator.writeArray(new int[] { date.getMonthValue(), date.getDayOfMonth() }, 0, 2);
            } else if (value instanceof ZonedDateTime date) {
                generator.writeNumber(date.toInstant().toEpochMilli());
            } else if (value instanceof OffsetDateTime date) {
                generator.writeString(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(date));
            } else if (value instanceof OffsetTime time) {
                generator.writeString(DateTimeFormatter.ISO_OFFSET_TIME.format(time));
            } else if (value instanceof Date date) {
                generator.writeString(DateTimeFormatter.ISO_INSTANT.format(date.toInstant()));
            }
        }
    }
}
