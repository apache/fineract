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

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * The application's Jackson 3 mapper, configured to agree with the Jackson 2 one that serves the REST API.
 *
 * <p>
 * Defining it replaces the mapper Spring Boot would otherwise auto-configure, which is a stock Jackson 3 mapper with
 * none of this. Everything injecting a Jackson 3 {@code ObjectMapper} gets this one; the command store, which persists
 * command requests and responses and replays a stored response when a request repeats under the same idempotency key,
 * is the consumer that makes the agreement matter.
 *
 * <p>
 * The feature settings are the ones where Jackson 3 changed a default out from under Jackson 2. Each was found by
 * comparing the two mappers' output rather than by reading release notes, and each is held in place by
 * {@code JacksonWireFormatEquivalenceTest}. {@code com.fasterxml.jackson.annotation} is shared by both versions, so
 * {@link JsonInclude} below is deliberately the Jackson 2 import.
 *
 * <p>
 * The declared return type has to stay {@link JsonMapper}, not {@code ObjectMapper}. Spring Boot's own Jackson 3 mapper
 * is {@code @Primary} and backs off only on finding a bean declared as a {@code JsonMapper}; declare this one as an
 * {@code ObjectMapper} and Boot builds its stock mapper anyway, wins on {@code @Primary}, and everything here is
 * silently never used. {@code Jackson3ObjectMapperWiringTest} is what notices.
 */
@Configuration
public class Jackson3ObjectMapperConfig {

    @Bean
    public JsonMapper jackson3ObjectMapper() {
        return JsonMapper.builder() //
                .addModule(new Jackson3WebFormatModule()) //
                .changeDefaultPropertyInclusion(inclusion -> inclusion.withValueInclusion(JsonInclude.Include.NON_NULL)) //
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) //
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS) //
                // Jackson 3 turned both of these off. Any java.time value that no serializer above covers would
                // otherwise change shape: a LocalDate inside a collection from [y,m,d] to a string, a Duration from a
                // number of seconds to an ISO-8601 period.
                .enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS) //
                .enable(DateTimeFeature.WRITE_DURATIONS_AS_TIMESTAMPS) //
                .build();
    }
}
