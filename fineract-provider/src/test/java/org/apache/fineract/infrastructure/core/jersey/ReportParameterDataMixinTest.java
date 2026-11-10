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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.apache.fineract.infrastructure.dataqueries.data.ReportParameterData;
import org.junit.jupiter.api.Test;

class ReportParameterDataMixinTest {

    private final ObjectMapper mapper = new JerseyJacksonConverterConfig().objectMapper(List.of(), List.of(), List.of());

    @Test
    void writesAllFields() throws Exception {
        JsonNode node = mapper.readTree(mapper.writeValueAsString(new ReportParameterData(1L, 2L, "rpn", "pn")));
        assertThat(node)
                .isEqualTo(mapper.readTree("{\"id\":1,\"parameterId\":2,\"parameterName\":\"pn\",\"reportParameterName\":\"rpn\"}"));
    }

    @Test
    void omitsNullFields() throws Exception {
        JsonNode node = mapper.readTree(mapper.writeValueAsString(new ReportParameterData(5L, null, null, "pn")));
        assertThat(node.toString()).isEqualTo("{\"id\":5,\"parameterName\":\"pn\"}");
    }
}
