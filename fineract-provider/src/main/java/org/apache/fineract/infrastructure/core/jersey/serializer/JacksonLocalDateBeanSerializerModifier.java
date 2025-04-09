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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.core.jersey.converter.LocalDateJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JacksonLocalDateArraySerializer;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JsonLocalDateArrayFormat;

@SuppressFBWarnings({ "SE_BAD_FIELD", "SE_BAD_FIELD_STORE" })
public class JacksonLocalDateBeanSerializerModifier extends BeanSerializerModifier {

    private final JsonSerializer<?> localDateSerializer = new JacksonSerializerAdapter<>(new LocalDateJsonConverter());
    private final JsonSerializer<?> localDateArraySerializer = new JacksonLocalDateArraySerializer();

    @Override
    @SuppressWarnings("unchecked")
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
            List<BeanPropertyWriter> beanProperties) {
        if (beanDesc.getBeanClass().isAnnotationPresent(JsonLocalDateArrayFormat.class)) {
            assignLocalDateSerializer(beanProperties, (JsonSerializer<Object>) localDateArraySerializer);
        } else {
            assignLocalDateSerializer(beanProperties, (JsonSerializer<Object>) localDateSerializer);
        }

        return beanProperties;
    }

    private void assignLocalDateSerializer(List<BeanPropertyWriter> beanProperties, JsonSerializer<Object> serializer) {
        for (BeanPropertyWriter writer : beanProperties) {
            if (LocalDate.class.equals(writer.getPropertyType())) {
                writer.assignSerializer(serializer);
            }
        }
    }
}
