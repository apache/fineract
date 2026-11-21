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
package org.apache.fineract.infrastructure.core.serialization;

import com.google.gson.Gson;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GoogleGsonSerializerHelperTest {

    @Test
    void serializeInstantAsIso8601String() {
        Instant instant = Instant.parse("1970-01-01T00:00:00Z");

        String json = GoogleGsonSerializerHelper.createSimpleGson().toJson(instant);

        Assertions.assertEquals("\"1970-01-01T00:00:00Z\"", json);
    }

    @Test
    void serializeDateAndInstantConsistently() {
        final Instant instant = Instant.parse("2026-11-20T10:15:30.123Z");

        final Date date = Date.from(instant);

        final Gson gson = GoogleGsonSerializerHelper.createSimpleGson();

        Assertions.assertEquals(gson.toJson(date), gson.toJson(instant));
    }
}
