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
package org.apache.fineract.client.adapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import org.apache.fineract.client.models.ExternalId;

/**
 * Custom Jackson adapter for ExternalId type serialization and deserialization. This adapter ensures that ExternalId
 * objects are properly serialized to their string value and deserialized from string values.
 */
public final class ExternalIdAdapter {

    private ExternalIdAdapter() {}

    /**
     * Jackson Serializer for ExternalId. Serializes an ExternalId object to its string value, or null if the ExternalId
     * or its value is null.
     */
    public static class Serializer extends JsonSerializer<ExternalId> {

        @Override
        public void serialize(ExternalId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null || value.getValue() == null) {
                gen.writeNull();
            } else {
                gen.writeString(value.getValue());
            }
        }
    }

    /**
     * Jackson Deserializer for ExternalId. Deserializes a string value to an ExternalId object, or null if the input is
     * null.
     */
    public static class Deserializer extends JsonDeserializer<ExternalId> {

        @Override
        public ExternalId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            if (value == null) {
                return null;
            }
            ExternalId externalId = new ExternalId();
            externalId.setValue(value);
            return externalId;
        }
    }

    /**
     * Creates a Jackson SimpleModule configured with the ExternalId serializer and deserializer.
     *
     * @return A configured SimpleModule ready to be registered with an ObjectMapper
     */
    public static SimpleModule createModule() {
        SimpleModule module = new SimpleModule("ExternalIdModule");
        module.addSerializer(ExternalId.class, new Serializer());
        module.addDeserializer(ExternalId.class, new Deserializer());
        return module;
    }
}
