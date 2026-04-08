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
package org.apache.fineract.client.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.models.ExternalId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExternalIdAdapterTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = ObjectMapperFactory.getShared();
    }

    @Test
    void testSerializeExternalId() throws Exception {
        ExternalId externalId = new ExternalId().value("EXT-12345");

        String json = mapper.writeValueAsString(externalId);

        assertThat(json).isEqualTo("\"EXT-12345\"");
    }

    @Test
    void testSerializeExternalIdWithSpecialCharacters() throws Exception {
        ExternalId externalId = new ExternalId().value("EXT-ID-2024/01/15");

        String json = mapper.writeValueAsString(externalId);

        assertThat(json).isEqualTo("\"EXT-ID-2024/01/15\"");
    }

    @Test
    void testSerializeNullExternalId() throws Exception {
        ExternalId externalId = new ExternalId().value(null);

        String json = mapper.writeValueAsString(externalId);

        assertThat(json).isEqualTo("null");
    }

    @Test
    void testSerializeExternalIdObjectNull() throws Exception {
        ExternalId externalId = null;

        String json = mapper.writeValueAsString(externalId);

        assertThat(json).isEqualTo("null");
    }

    @Test
    void testDeserializeExternalId() throws Exception {
        String json = "\"EXT-67890\"";

        ExternalId externalId = mapper.readValue(json, ExternalId.class);

        assertThat(externalId).isNotNull();
        assertThat(externalId.getValue()).isEqualTo("EXT-67890");
    }

    @Test
    void testDeserializeExternalIdWithSpecialCharacters() throws Exception {
        String json = "\"CLIENT-2024-001/ABC\"";

        ExternalId externalId = mapper.readValue(json, ExternalId.class);

        assertThat(externalId).isNotNull();
        assertThat(externalId.getValue()).isEqualTo("CLIENT-2024-001/ABC");
    }

    @Test
    void testDeserializeNullExternalId() throws Exception {
        String json = "null";

        ExternalId externalId = mapper.readValue(json, ExternalId.class);

        assertThat(externalId).isNull();
    }

    @Test
    void testRoundTripSerialization() throws Exception {
        ExternalId original = new ExternalId().value("TEST-ID-001");

        String json = mapper.writeValueAsString(original);
        ExternalId deserialized = mapper.readValue(json, ExternalId.class);

        assertThat(deserialized.getValue()).isEqualTo(original.getValue());
    }

    @Test
    void testRoundTripSerializationWithUUID() throws Exception {
        ExternalId original = new ExternalId().value("550e8400-e29b-41d4-a716-446655440000");

        String json = mapper.writeValueAsString(original);
        ExternalId deserialized = mapper.readValue(json, ExternalId.class);

        assertThat(deserialized.getValue()).isEqualTo(original.getValue());
    }

    @Test
    void testRoundTripSerializationWithNumericString() throws Exception {
        ExternalId original = new ExternalId().value("123456789");

        String json = mapper.writeValueAsString(original);
        ExternalId deserialized = mapper.readValue(json, ExternalId.class);

        assertThat(deserialized.getValue()).isEqualTo(original.getValue());
    }

    @Test
    void testDeserializeEmptyString() throws Exception {
        String json = "\"\"";

        ExternalId externalId = mapper.readValue(json, ExternalId.class);

        assertThat(externalId).isNotNull();
        assertThat(externalId.getValue()).isEmpty();
    }

    @Test
    void testSerializeExternalIdWithEmptyValue() throws Exception {
        ExternalId externalId = new ExternalId().value("");

        String json = mapper.writeValueAsString(externalId);

        assertThat(json).isEqualTo("\"\"");
    }

    @Test
    void testModuleCreation() {
        assertThat(ExternalIdAdapter.createModule()).isNotNull();
        assertThat(ExternalIdAdapter.createModule().getModuleName()).isEqualTo("ExternalIdModule");
    }

    @Test
    void testDeserializeExternalIdWithWhitespace() throws Exception {
        String json = "\"  EXT-001  \"";

        ExternalId externalId = mapper.readValue(json, ExternalId.class);

        assertThat(externalId).isNotNull();
        assertThat(externalId.getValue()).isEqualTo("  EXT-001  ");
    }

    @Test
    void testSerializeExternalIdInObject() throws Exception {
        TestObject obj = new TestObject();
        obj.externalId = new ExternalId().value("TEST-123");

        String json = mapper.writeValueAsString(obj);

        assertThat(json).contains("\"externalId\":\"TEST-123\"");
    }

    @Test
    void testDeserializeExternalIdInObject() throws Exception {
        String json = "{\"externalId\":\"TEST-456\"}";

        TestObject obj = mapper.readValue(json, TestObject.class);

        assertThat(obj.externalId).isNotNull();
        assertThat(obj.externalId.getValue()).isEqualTo("TEST-456");
    }

    static class TestObject {

        public ExternalId externalId;
    }
}
