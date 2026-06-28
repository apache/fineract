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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHandler;
import org.apache.fineract.command.core.exception.DuplicateCommandHandlerException;
import org.apache.fineract.command.implementation.DefaultCommandHandlerManager;
import org.apache.fineract.command.test.sample.command.DummyCommand;
import org.apache.fineract.command.test.sample.data.DummyRequest;
import org.apache.fineract.command.test.sample.data.DummyResponse;
import org.junit.jupiter.api.Test;

class DefaultCommandHandlerManagerTest {

    static class HandlerA implements CommandHandler<DummyRequest, DummyResponse> {

        @Override
        public DummyResponse handle(Command<DummyRequest> command) {
            return DummyResponse.builder().content("A").build();
        }
    }

    static class HandlerB implements CommandHandler<DummyRequest, DummyResponse> {

        @Override
        public DummyResponse handle(Command<DummyRequest> command) {
            return DummyResponse.builder().content("B").build();
        }
    }

    @Test
    void startupValidation_noDuplicates_passes() {
        var manager = new DefaultCommandHandlerManager(List.of(new HandlerA()));
        assertDoesNotThrow(() -> manager.validateNoDuplicateHandlers());
    }

    @Test
    void startupValidation_withDuplicates_throwsIllegalStateException() {
        var manager = new DefaultCommandHandlerManager(List.of(new HandlerA(), new HandlerB()));
        var ex = assertThrows(IllegalStateException.class, () -> manager.validateNoDuplicateHandlers());
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("Duplicate CommandHandlers detected"));
    }

    @Test
    void handle_withDuplicateHandlersMatchingCommand_throwsDuplicateCommandHandlerException() {
        var manager = new DefaultCommandHandlerManager(List.of(new HandlerA(), new HandlerB()));
        var command = new DummyCommand();
        command.setPayload(DummyRequest.builder().content("test").build());

        assertThrows(DuplicateCommandHandlerException.class, () -> manager.handle(command));
    }

    @Test
    void handle_withSingleHandler_returnsResult() {
        var manager = new DefaultCommandHandlerManager(List.of(new HandlerA()));
        var command = new DummyCommand();
        command.setPayload(DummyRequest.builder().content("test").build());

        var result = (DummyResponse) manager.handle(command);
        assertNotNull(result);
        assertTrue("A".equals(result.getContent()));
    }
}
