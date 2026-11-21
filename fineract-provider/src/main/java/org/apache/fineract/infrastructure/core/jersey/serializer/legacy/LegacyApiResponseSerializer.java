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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.apache.fineract.infrastructure.core.annotation.LegacyApiResponse;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

/** Per-response writer settings on the application's Jersey mapper; no independent mapper or annotation overrides. */
@Component
public class LegacyApiResponseSerializer {

    public static final String ATTRIBUTE = LegacyApiResponseSerializer.class.getName();
    static final String FILTER_ID = "legacyResponseFields";

    private final ObjectMapper objectMapper;

    public LegacyApiResponseSerializer(@Qualifier("objectMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ObjectWriter writer(LegacyApiResponse response, Set<String> requestedFields) {
        Set<String> skippedFields = new HashSet<>();
        if (response.value().length > 0 && !requestedFields.isEmpty()) {
            Set<String> supportedFields = Set.of(response.value());
            Set<String> unsupportedFields = new HashSet<>(requestedFields);
            unsupportedFields.removeAll(supportedFields);
            if (!unsupportedFields.isEmpty()) {
                throw new UnsupportedParameterException(new ArrayList<>(unsupportedFields));
            }
            skippedFields.addAll(supportedFields);
            skippedFields.removeAll(requestedFields);
        }
        PropertyFilter filter = new ResponseFieldsFilter(skippedFields);
        FilterProvider existingFilters = objectMapper.getSerializationConfig().getFilterProvider();
        SimpleFilterProvider filters = new SimpleFilterProvider() {

            @Override
            public PropertyFilter findPropertyFilter(Object id, Object value) {
                if (FILTER_ID.equals(id)) {
                    return filter;
                }
                return existingFilters == null ? super.findPropertyFilter(id, value) : existingFilters.findPropertyFilter(id, value);
            }
        };
        return objectMapper.writer(filters).withAttribute(ATTRIBUTE, true);
    }

    private static final class ResponseFieldsFilter extends SimpleBeanPropertyFilter {

        private final Set<String> skippedFields;

        private ResponseFieldsFilter(Set<String> skippedFields) {
            this.skippedFields = Set.copyOf(skippedFields);
        }

        @Override
        public void serializeAsField(Object bean, JsonGenerator generator, SerializerProvider provider, PropertyWriter writer)
                throws Exception {
            String name = writer.getName();
            if (skippedFields.contains(name) || (bean instanceof OfficeData && "rowIndex".equals(name))) {
                return;
            }
            // These two getters deliberately normalize values for other typed resources. Preserve their raw values
            // only for migrated responses, including an empty changes map and an absent rollbackTransaction flag.
            if (bean instanceof CommandProcessingResult && ("changes".equals(name) || "rollbackTransaction".equals(name))) {
                Field field = CommandProcessingResult.class.getDeclaredField(name);
                ReflectionUtils.makeAccessible(field);
                Object value = ReflectionUtils.getField(field, bean);
                if (value != null) {
                    provider.defaultSerializeField(name, value, generator);
                }
                return;
            }
            if (writer instanceof BeanPropertyWriter property) {
                Object value = property.get(bean);
                if (value instanceof ExternalId externalId && externalId.isEmpty()) {
                    return;
                }
            }
            writer.serializeAsField(bean, generator, provider);
        }
    }

    /** Used by Jackson's map-content inclusion support for the legacy ExternalId adapter's JSON-null result. */
    public static final class AbsentValueFilter {

        @Override
        @SuppressFBWarnings(value = { "EQ_CHECK_FOR_OPERAND_NOT_COMPATIBLE_WITH_THIS",
                "EQ_UNUSUAL" }, justification = "Jackson inclusion filters use equals as a predicate on values, including null, rather than on other filters.")
        public boolean equals(Object value) {
            return value == null || (value instanceof ExternalId externalId && externalId.isEmpty());
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }
}
