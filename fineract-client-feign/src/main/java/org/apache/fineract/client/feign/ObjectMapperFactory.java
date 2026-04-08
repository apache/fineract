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
package org.apache.fineract.client.feign;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.fineract.client.adapter.ExternalIdAdapter;

/**
 * Factory for creating and configuring Jackson ObjectMapper instances.
 */
public final class ObjectMapperFactory {

    private static final ObjectMapper INSTANCE = createObjectMapper();

    private ObjectMapperFactory() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates and configures a new ObjectMapper instance.
     *
     * @return A new configured ObjectMapper instance
     */
    public static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Configure the ObjectMapper
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // Register Java 8 date/time support
        mapper.registerModule(new JavaTimeModule());

        // Register ExternalId adapter
        mapper.registerModule(ExternalIdAdapter.createModule());

        // Disable FAIL_ON_EMPTY_BEANS for empty responses
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        return mapper;
    }

    /**
     * Returns a shared, pre-configured ObjectMapper instance.
     *
     * @return A shared ObjectMapper instance
     */
    public static ObjectMapper getShared() {
        return INSTANCE;
    }
}
