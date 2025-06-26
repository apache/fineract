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
package org.apache.fineract.command.persistence.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
final class CommandJsonMapper {

    private static final String CLASS_ATTRIBUTE = "@class";
    private final ObjectMapper mapper;

    public <T> T map(JsonNode source) {
        if (source == null) {
            return null;
        }

        var canonicalName = source.get(CLASS_ATTRIBUTE).asText();

        try {
            return (T) mapper.convertValue(source, Class.forName(canonicalName));
        } catch (Exception e) {
            log.error("Error while mapping json node", e);
        }

        return null;
    }

    public JsonNode map(Object source) {
        if (source == null) {
            return null;
        }

        var json = mapper.convertValue(source, ObjectNode.class);

        json.set(CLASS_ATTRIBUTE, new TextNode(source.getClass().getCanonicalName()));

        return json;
    }
}
