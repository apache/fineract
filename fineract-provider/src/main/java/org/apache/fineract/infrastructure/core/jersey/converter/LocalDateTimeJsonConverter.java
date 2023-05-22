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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class LocalDateTimeJsonConverter implements JsonConverter<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public LocalDateTime convertToObject(JsonParser parser) throws IOException {
        LocalDateTime result = null;
        if (parser.hasToken(JsonToken.VALUE_STRING)) {
            String formattedDate = parser.getText();
            result = LocalDateTime.parse(formattedDate, FORMATTER);
        }
        return result;
    }

    @Override
    public void convertToJson(LocalDateTime value, JsonGenerator generator) throws IOException {
        if (value != null) {
            generator.writeString(FORMATTER.format(value));
        }

    }

    @Override
    public Class<LocalDateTime> convertedType() {
        return LocalDateTime.class;
    }
}
