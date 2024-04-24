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
package org.apache.fineract.infrastructure.core.mvc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.api.mvc.JacksonExternalIdExcludeFilter;
import org.apache.fineract.infrastructure.core.api.mvc.ProfileMvc;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.jersey.converter.JsonConverter;
import org.apache.fineract.infrastructure.core.jersey.serializer.JacksonDeserializerAdapter;
import org.apache.fineract.infrastructure.core.jersey.serializer.JacksonSerializerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@ProfileMvc
@Configuration
public class MvcJacksonConverterConfig {

    @Primary
    @Bean
    public MappingJackson2HttpMessageConverter jacksonHttpConverter(final ObjectMapper objectMapper) {
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }

    @Primary
    @Bean
    public ObjectMapper objectMapper(List<JsonSerializer<?>> serializers, List<JsonDeserializer<?>> deserializers,
            List<JsonConverter<?>> jsonConverters) {
        List<JsonSerializer<?>> mergedSerializers = new ArrayList<>(serializers);
        mergedSerializers.addAll(jsonConverters.stream().map(JacksonSerializerAdapter::new).toList());

        List<JsonDeserializer<?>> mergedDeserializers = new ArrayList<>(deserializers);
        mergedDeserializers.addAll(jsonConverters.stream().map(JacksonDeserializerAdapter::new).toList());

        final ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder().indentOutput(true)
                .serializers(mergedSerializers.toArray(new JsonSerializer[0]))
                .deserializers(mergedDeserializers.toArray(new JsonDeserializer[0])).serializationInclusion(JsonInclude.Include.NON_NULL)
                .failOnUnknownProperties(true).featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .modulesToInstall(new ParameterNamesModule()).build();

        objectMapper.configOverride(ExternalId.class)
                .setInclude(JsonInclude.Value.empty().withValueFilter(JacksonExternalIdExcludeFilter.class));

        return objectMapper;
    }
}
