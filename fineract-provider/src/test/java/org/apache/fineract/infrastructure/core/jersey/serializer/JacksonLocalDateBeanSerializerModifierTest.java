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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JacksonLocalDateArrayModule;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.JsonLocalDateArrayFormat;
import org.junit.jupiter.api.Test;

class JacksonLocalDateBeanSerializerModifierTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JacksonLocalDateArrayModule());

    @Test
    void writesDatesAsIsoStringsByDefault() throws Exception {
        assertThat(objectMapper.writeValueAsString(new Plain(LocalDate.of(2026, 1, 2)))).isEqualTo("{\"date\":\"2026-01-02\"}");
    }

    @Test
    void writesDatesOfAnnotatedClassesAsArrays() throws Exception {
        assertThat(objectMapper.writeValueAsString(new Annotated(LocalDate.of(2026, 1, 2), null)))
                .isEqualTo("{\"date\":[2026,1,2],\"nested\":null}");
    }

    @Test
    void keepsIsoStringsInClassesNestedInAnAnnotatedClass() throws Exception {
        assertThat(objectMapper.writeValueAsString(new Annotated(LocalDate.of(2026, 1, 2), new Plain(LocalDate.of(2026, 3, 4)))))
                .isEqualTo("{\"date\":[2026,1,2],\"nested\":{\"date\":\"2026-03-04\"}}");
    }

    @Test
    void writesEveryDateAsAnArrayWithTheLocalDateArrayFormat() throws Exception {
        final StringBuilder json = new StringBuilder();
        JacksonLocalDateBeanSerializerModifier.writeWithLocalDateArrayFormat(() -> json
                .append(objectMapper.writeValueAsString(new Annotated(LocalDate.of(2026, 1, 2), new Plain(LocalDate.of(2026, 3, 4))))));

        assertThat(json.toString()).isEqualTo("{\"date\":[2026,1,2],\"nested\":{\"date\":[2026,3,4]}}");
    }

    @Test
    void writesIsoStringsAgainAfterTheLocalDateArrayFormat() throws Exception {
        JacksonLocalDateBeanSerializerModifier
                .writeWithLocalDateArrayFormat(() -> objectMapper.writeValueAsString(new Plain(LocalDate.of(2026, 1, 2))));

        assertThat(objectMapper.writeValueAsString(new Plain(LocalDate.of(2026, 1, 2)))).isEqualTo("{\"date\":\"2026-01-02\"}");
    }

    public record Plain(LocalDate date) {
    }

    @JsonLocalDateArrayFormat
    public record Annotated(LocalDate date, Plain nested) {
    }
}
