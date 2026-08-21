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

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.core.jersey.converter.LocalDateJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JacksonLocalDateArraySerializer;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JsonLocalDateArrayFormat;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.ValueSerializerModifier;

public class JacksonLocalDateBeanSerializerModifier extends ValueSerializerModifier {

    private final ValueSerializer<?> localDateSerializer = new JacksonSerializerAdapter<>(new LocalDateJsonConverter());
    private final ValueSerializer<?> localDateArraySerializer = new JacksonLocalDateArraySerializer();

    @Override
    @SuppressWarnings("unchecked")
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription.Supplier beanDesc,
            List<BeanPropertyWriter> beanProperties) {
        if (beanDesc.getBeanClass().isAnnotationPresent(JsonLocalDateArrayFormat.class)) {
            assignLocalDateSerializer(beanProperties, (ValueSerializer<Object>) localDateArraySerializer);
        } else {
            assignLocalDateSerializer(beanProperties, (ValueSerializer<Object>) localDateSerializer);
        }

        return beanProperties;
    }

    private void assignLocalDateSerializer(List<BeanPropertyWriter> beanProperties, ValueSerializer<Object> serializer) {
        for (BeanPropertyWriter writer : beanProperties) {
            if (LocalDate.class.equals(writer.getType().getRawClass())) {
                writer.assignSerializer(serializer);
            }
        }
    }
}
