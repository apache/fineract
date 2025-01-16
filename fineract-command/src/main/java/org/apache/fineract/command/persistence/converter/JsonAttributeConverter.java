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
package org.apache.fineract.command.persistence.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Converter
public class JsonAttributeConverter implements AttributeConverter<JsonNode, String> {
    // TODO: it would be nicer to use a native JSON type on the database side, but not every system supports this;
    // string/text are the lowest common denominator that should work on every database

    private final ObjectMapper mapper;

    @Override
    @SneakyThrows
    public String convertToDatabaseColumn(JsonNode source) {
        if (source != null) {
            return mapper.writeValueAsString(source);
        }

        // TODO: throw exception?
        return null;
    }

    @Override
    @SneakyThrows
    public JsonNode convertToEntityAttribute(String source) {
        if (source != null) {
            return mapper.readTree(source);
        }

        // TODO: throw exception?
        return null;
    }
}
