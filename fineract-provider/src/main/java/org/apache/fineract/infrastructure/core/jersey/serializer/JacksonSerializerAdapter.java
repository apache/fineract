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

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.jersey.converter.JsonConverter;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

@RequiredArgsConstructor
public class JacksonSerializerAdapter<T> extends ValueSerializer<T> implements Serializable {

    private final JsonConverter<T> converter;

    @Override
    public void serialize(T value, JsonGenerator gen, SerializationContext serializers) {
        try {
            converter.convertToJson(value, gen);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Class<T> handledType() {
        return converter.convertedType();
    }
}
