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
package org.apache.fineract.infrastructure.core.jersey.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.IOException;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.springframework.stereotype.Component;

@Component
public class ExternalIdJsonConverter implements JsonConverter<ExternalId> {

    @Override
    public ExternalId convertToObject(JsonParser parser) throws IOException {
        ExternalId result = ExternalId.empty();
        if (parser.hasToken(JsonToken.VALUE_STRING)) {
            String externalId = parser.getText();
            result = new ExternalId(externalId);
        }
        return result;
    }

    @Override
    public void convertToJson(ExternalId value, JsonGenerator generator) throws IOException {
        if (value != null && !value.isEmpty()) {
            generator.writeString(value.getValue());
        } else {
            generator.writeNull();
        }
    }

    @Override
    public Class<ExternalId> convertedType() {
        return ExternalId.class;
    }
}
