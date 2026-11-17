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
package org.apache.fineract.infrastructure.core.jersey;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.apache.fineract.infrastructure.core.jersey.converter.DateJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.converter.ExternalIdJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.converter.LocalDateJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.converter.LocalDateTimeJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.converter.LocalTimeJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.converter.OffsetDateTimeJsonConverter;
import org.apache.fineract.infrastructure.core.jersey.converter.OffsetTimeJsonConverter;

final class SecurityApiSerializationTestSupport {

    private SecurityApiSerializationTestSupport() {}

    static ObjectMapper newObjectMapper() {
        return new JerseyJacksonConverterConfig().objectMapper(List.of(), List.of(),
                List.of(new DateJsonConverter(), new ExternalIdJsonConverter(), new LocalDateJsonConverter(),
                        new LocalDateTimeJsonConverter(), new LocalTimeJsonConverter(), new OffsetDateTimeJsonConverter(),
                        new OffsetTimeJsonConverter()));
    }
}
