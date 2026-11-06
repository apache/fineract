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
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Component;

/**
 * Writes {@link ZonedDateTime} as epoch milliseconds, the format the GSON based serialization has always produced (see
 * {@code JodaDateTimeAdapter}), so that resources returning typed objects keep the same JSON as before.
 */
@Component
public class ZonedDateTimeJsonConverter implements JsonConverter<ZonedDateTime> {

    @Override
    public ZonedDateTime convertToObject(JsonParser parser) throws IOException {
        ZonedDateTime result = null;
        if (parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            result = Instant.ofEpochMilli(parser.getLongValue()).atZone(ZoneOffset.UTC);
        } else if (parser.hasToken(JsonToken.VALUE_STRING)) {
            result = ZonedDateTime.parse(parser.getText());
        }
        return result;
    }

    @Override
    public void convertToJson(ZonedDateTime value, JsonGenerator generator) throws IOException {
        if (value != null) {
            generator.writeNumber(value.toInstant().toEpochMilli());
        }
    }

    @Override
    public Class<ZonedDateTime> convertedType() {
        return ZonedDateTime.class;
    }
}
