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

package org.apache.fineract.v3.command.mapping;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.v3.command.data.CommandRequest;
import org.apache.fineract.v3.command.domain.Command;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapstructMapperConfig.class, nullValueCheckStrategy = ALWAYS)
public interface CommandMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requestIdempotencyKey", source = "requestIdempotencyKey", qualifiedByName = "mapRequestIdempotencyKey")
    @Mapping(target = "body", source = "body", qualifiedByName = "mapBody")
    Command map(CommandRequest<?> source);

    @Named("mapRequestIdempotencyKey")
    default String mapRequestIdempotencyKey(UUID requestIdempotencyKey) {
        return requestIdempotencyKey != null ? requestIdempotencyKey.toString() : null;
    }

    @Named("mapBody")
    default JsonNode mapBody(Object body) {
        if (body instanceof JsonNode) {
            return (JsonNode) body;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.valueToTree(body);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error mapping body to JsonNode: " + body.getClass().getName(), e);
        }
    }
}
