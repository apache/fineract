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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import org.apache.fineract.TestConfiguration;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
public class CommandProcessingResultSerializationTest {

    @Autowired
    private MappingJackson2HttpMessageConverter converter;

    @Test
    public void testCommandProcessingResultSerialization() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        CommandProcessingResult commandProcessingResult = CommandProcessingResult.fromDetails(1L, null, null, null, null, null,
                "resourceIdentifier", null, null, null, null, null, null, null, Boolean.TRUE, null, new ExternalId("externalId"),
                ExternalId.empty(), null);
        SimpleHttpOutputMessage outputMessage = new SimpleHttpOutputMessage(os, headers);
        converter.write(commandProcessingResult, MediaType.APPLICATION_JSON, outputMessage);
        Map<String, Object> result = new Gson().fromJson(os.toString(Charset.defaultCharset()), Map.class);
        assertEquals(4, result.size());
        assertEquals("resourceIdentifier", result.get("resourceIdentifier"));
        assertEquals("externalId", result.get("resourceExternalId"));
        assertEquals(1.0, result.get("commandId"));
        assertEquals(true, result.get("rollbackTransaction"));
    }

}
