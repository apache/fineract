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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHandler;
import org.apache.fineract.command.core.exception.CommandHandlerDuplicateException;
import org.apache.fineract.command.core.exception.CommandHandlerInvalidRequestTypeException;
import org.apache.fineract.command.implementation.DefaultCommandHandlerManager;
import org.apache.fineract.command.test.sample.data.DummyRequest;
import org.apache.fineract.command.test.sample.data.DummyResponse;
import org.junit.jupiter.api.Test;

public class DefaultCommandHandlerManagerTest {

    static class DummyCommandHandlerOriginal implements CommandHandler<DummyRequest, DummyResponse> {

        @Override
        public DummyResponse handle(Command<DummyRequest> command) {
            return DummyResponse.builder().content("res1").build();
        }

    }

    static class DummyCommandHandlerDuplicate implements CommandHandler<DummyRequest, DummyResponse> {

        @Override
        public DummyResponse handle(Command<DummyRequest> command) {

            return DummyResponse.builder().content("res2").build();
        }
    }

    static class DummyCommandHandlerNoType implements CommandHandler {

        @Override
        public DummyResponse handle(Command command) {

            return DummyResponse.builder().content("res2").build();
        }
    }

    /*
     * Integration test for initialization
     */
    @Test
    void processInit() {
        List<CommandHandler> handlers = List.of(new DummyCommandHandlerOriginal());

        DefaultCommandHandlerManager manager = new DefaultCommandHandlerManager(handlers);

        assertDoesNotThrow(() -> manager.init());
    }

    @Test
    void errorDuplicateCommandHandlers() {

        List<CommandHandler> handlers = List.of(new DummyCommandHandlerOriginal(), new DummyCommandHandlerDuplicate());

        DefaultCommandHandlerManager manager = new DefaultCommandHandlerManager(handlers);

        var ex = assertThrows(CommandHandlerDuplicateException.class, () -> manager.init());

        assertNotNull(ex);

    }

    @Test
    void errorInvalidCommandRequestType() {
        List<CommandHandler> handlers = List.of(new DummyCommandHandlerDuplicate(), new DummyCommandHandlerNoType());

        DefaultCommandHandlerManager manager = new DefaultCommandHandlerManager(handlers);
        var ex = assertThrows(CommandHandlerInvalidRequestTypeException.class, () -> manager.init());

        assertNotNull(ex);
    }
}
