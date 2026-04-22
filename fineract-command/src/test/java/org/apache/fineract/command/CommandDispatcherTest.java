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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.command.test.sample.command.DummyCommand;
import org.apache.fineract.command.test.sample.command.DummyErrorCommand;
import org.apache.fineract.command.test.sample.data.DummyErrorRequest;
import org.apache.fineract.command.test.sample.data.DummyErrorResponse;
import org.apache.fineract.command.test.sample.data.DummyRequest;
import org.apache.fineract.command.test.sample.data.DummyResponse;
import org.apache.fineract.command.test.sample.exception.DummyException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
class CommandDispatcherTest {

    @Autowired
    private CommandDispatcher dispatcher;

    @Test
    void process() {
        var content = "hello";
        var command = new DummyCommand();

        command.setPayload(DummyRequest.builder().content(content).build());
        command.setIdempotencyKey("1234567890");
        command.setIpAddress("127.0.0.1");
        command.setInitiatedByUsername("abc");

        Supplier<DummyResponse> result = dispatcher.dispatch(command);

        assertNotNull(result, "Response should not be null.");

        var response = result.get();

        assertNotNull(response, "Response should not be null.");

        assertInstanceOf(DummyResponse.class, response, "Response is of wrong type.");

        log.info("Result: {}", response);
        log.info("Command ID: {}", command.getCommandId());

        assertNotNull(response.getContent(), "Response body should not be null.");
        assertEquals(content.toUpperCase(Locale.ROOT), response.getContent(), "Wrong response content.");
    }

    @Test
    void error() {
        var content = "hello error";
        var command = new DummyErrorCommand();

        command.setPayload(DummyErrorRequest.builder().content(content).build());

        Supplier<DummyErrorResponse> result = dispatcher.dispatch(command);

        var exception = assertThrows(DummyException.class, () -> {
            var response = result.get();
        });

        assertNotNull(exception, "Expected exception!");
    }
}
