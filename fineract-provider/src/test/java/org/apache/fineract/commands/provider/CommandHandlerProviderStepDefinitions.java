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
package org.apache.fineract.commands.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.cucumber.java8.En;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;

public class CommandHandlerProviderStepDefinitions implements En {

    @Autowired
    private CommandHandlerProvider commandHandlerProvider;

    private NewCommandSourceHandler commandHandler;

    private CommandProcessingResult result;

    public CommandHandlerProviderStepDefinitions() {
        Given("/^A command handler for entity (.*) and action (.*)$/", (String entity, String action) -> {
            this.commandHandler = this.commandHandlerProvider.getHandler(entity, action);
        });

        When("The user processes the command with ID {long}", (Long id) -> {
            this.result = commandHandler
                    .processCommand(JsonCommand.fromExistingCommand(id, null, null, null, null, null, null, null, null, null, null, null));
        });

        Then("The command ID matches {long}", (Long id) -> {
            assertEquals(id, result.getCommandId());
        });
    }
}
