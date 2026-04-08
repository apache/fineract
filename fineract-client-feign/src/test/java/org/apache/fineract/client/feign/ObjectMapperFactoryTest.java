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
package org.apache.fineract.client.feign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ObjectMapperFactoryTest {

    @Test
    void testSharedObjectMapperNotNull() {
        ObjectMapper mapper = ObjectMapperFactory.getShared();
        assertNotNull(mapper);
    }

    @Test
    void testSharedObjectMapperSingleton() {
        ObjectMapper mapper1 = ObjectMapperFactory.getShared();
        ObjectMapper mapper2 = ObjectMapperFactory.getShared();
        assertSame(mapper1, mapper2);
    }

    @Test
    void testJava8DateTimeSerialization() throws JsonProcessingException {
        ObjectMapper mapper = ObjectMapperFactory.getShared();
        LocalDate date = LocalDate.of(2024, 1, 15);

        String json = mapper.writeValueAsString(new TestObject(date));
        assertNotNull(json);
    }

    @Test
    void testJava8DateTimeDeserialization() throws IOException {
        ObjectMapper mapper = ObjectMapperFactory.getShared();
        String json = "{\"date\":\"2024-01-15\"}";

        TestObject obj = mapper.readValue(json, TestObject.class);
        assertNotNull(obj);
        assertNotNull(obj.getDate());
        assertEquals(LocalDate.of(2024, 1, 15), obj.getDate());
    }

    @Test
    void testNullHandling() throws JsonProcessingException {
        ObjectMapper mapper = ObjectMapperFactory.getShared();
        TestObject obj = new TestObject(null);

        String json = mapper.writeValueAsString(obj);
        assertNotNull(json);
    }

    @Test
    void testDeserializeNullValue() throws IOException {
        ObjectMapper mapper = ObjectMapperFactory.getShared();
        String json = "{\"date\":null}";

        TestObject obj = mapper.readValue(json, TestObject.class);
        assertNotNull(obj);
        assertNull(obj.getDate());
    }

    @Test
    void testUnknownPropertiesIgnored() throws IOException {
        ObjectMapper mapper = ObjectMapperFactory.getShared();
        String json = "{\"date\":\"2024-01-15\",\"unknownField\":\"value\"}";

        TestObject obj = mapper.readValue(json, TestObject.class);
        assertNotNull(obj);
        assertEquals(LocalDate.of(2024, 1, 15), obj.getDate());
    }

    @Test
    void testCreateObjectMapperNotNull() {
        ObjectMapper mapper = ObjectMapperFactory.createObjectMapper();
        assertNotNull(mapper);
    }

    static class TestObject {

        private LocalDate date;

        TestObject() {}

        TestObject(LocalDate date) {
            this.date = date;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }
}
