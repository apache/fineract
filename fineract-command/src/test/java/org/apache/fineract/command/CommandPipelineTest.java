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
package org.apache.fineract.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Locale;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.command.sample.command.DummyCommand;
import org.apache.fineract.command.sample.data.DummyRequest;
import org.apache.fineract.command.sample.data.DummyResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
class CommandPipelineTest extends CommandBaseTest {

    @Autowired
    private CommandPipeline pipeline;

    @Test
    void processCommand() {
        var content = "hello";
        var command = new DummyCommand();
        command.setId(UUID.randomUUID());
        command.setPayload(DummyRequest.builder().content(content).build());

        var result = pipeline.send(command);

        assertNotNull(result, "Response should not be null.");

        Object response = result.get();

        assertNotNull(response, "Response should not be null.");

        assertInstanceOf(DummyResponse.class, response, "Response is of wrong type.");

        if (response instanceof DummyResponse dummyResponse) {
            log.warn("Result: {}", dummyResponse);

            assertNotNull(dummyResponse.getContent(), "Response body should not be null.");
            assertNotNull(dummyResponse.getRequestId(), "Request ID should not be null.");
            assertEquals(content.toUpperCase(Locale.ROOT), dummyResponse.getContent(), "Wrong response content.");
        }
    }
}
