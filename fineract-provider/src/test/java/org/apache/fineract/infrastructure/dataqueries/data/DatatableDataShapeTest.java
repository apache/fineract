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
package org.apache.fineract.infrastructure.dataqueries.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.apache.fineract.infrastructure.core.jersey.JerseyJacksonConverterConfig;
import org.junit.jupiter.api.Test;

class DatatableDataShapeTest {

    private final ObjectMapper mapper = new JerseyJacksonConverterConfig().objectMapper(List.of(), List.of(), List.of());

    @Test
    void writesAllFieldsLikeGson() throws Exception {
        DatatableData data = DatatableData.create("m_client", "extra_client_details", "PERSON", List.of());

        JsonNode actual = mapper.readTree(mapper.writeValueAsString(data));

        JsonNode expected = mapper.readTree(
                "{\"applicationTableName\":\"m_client\",\"registeredTableName\":\"extra_client_details\",\"entitySubType\":\"PERSON\",\"columnHeaderData\":[]}");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void omitsNullEntitySubTypeLikeGson() throws Exception {
        DatatableData data = DatatableData.create("m_loan", "dt_loan", null, List.of());

        JsonNode actual = mapper.readTree(mapper.writeValueAsString(data));

        JsonNode expected = mapper
                .readTree("{\"applicationTableName\":\"m_loan\",\"registeredTableName\":\"dt_loan\",\"columnHeaderData\":[]}");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void doesNotWriteDerivedGetters() throws Exception {
        DatatableData data = DatatableData.create("m_client", "extra_client_details", null, List.of());

        JsonNode actual = mapper.readTree(mapper.writeValueAsString(data));

        assertThat(actual.has("registeredTableName")).isTrue();
        assertThat(actual.fieldNames()).toIterable().containsExactlyInAnyOrder("applicationTableName", "registeredTableName",
                "columnHeaderData");
    }
}
