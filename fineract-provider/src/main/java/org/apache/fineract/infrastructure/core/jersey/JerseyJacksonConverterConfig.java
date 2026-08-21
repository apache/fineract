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

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.jersey.converter.JsonConverter;
import org.apache.fineract.infrastructure.core.jersey.serializer.JacksonDeserializerAdapter;
import org.apache.fineract.infrastructure.core.jersey.serializer.JacksonSerializerAdapter;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JacksonLocalDateArrayModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

@Configuration
public class JerseyJacksonConverterConfig {

    @Bean
    public JsonMapper objectMapper(List<ValueSerializer<?>> serializers, List<ValueDeserializer<?>> deserializers,
            List<JsonConverter<?>> jsonConverters) {
        // Merge JsonConverters with serializers and deserializers
        List<ValueSerializer<?>> mergedSerializers = new ArrayList<>(serializers);
        mergedSerializers.addAll(jsonConverters.stream().map(JacksonSerializerAdapter::new).toList());
        List<ValueDeserializer<?>> mergedDeserializers = new ArrayList<>(deserializers);
        mergedDeserializers.addAll(jsonConverters.stream().map(JacksonDeserializerAdapter::new).toList());
        SimpleModule module = new SimpleModule();
        mergedSerializers.forEach(module::addSerializer);
        mergedDeserializers.forEach(d -> {
            @SuppressWarnings("unchecked")
            Class<Object> type = (Class<Object>) d.handledType();
            module.addDeserializer(type, (ValueDeserializer<Object>) d);
        });

        return JsonMapper.builder()
                .changeDefaultPropertyInclusion(
                        v -> JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL))
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addModule(module).addModule(new JacksonLocalDateArrayModule()).build();
    }

}
