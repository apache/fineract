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

import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.cucumber.java8.En;
import org.apache.fineract.commands.exception.UnsupportedCommandException;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressFBWarnings(value = "RV_EXCEPTION_NOT_THROWN", justification = "False positive")
public class CommandHandlerExceptionStepDefinitions implements En {

    @Autowired
    private CommandHandlerProvider commandHandlerProvider;

    private String entity;

    private String action;

    public CommandHandlerExceptionStepDefinitions() {
        Given("/^A missing command handler for entity (.*) and action (.*)$/", (String entity, String action) -> {
            this.entity = entity;
            this.action = action;
        });

        Then("The system should throw an exception", () -> {
            assertThrows(UnsupportedCommandException.class, () -> {
                this.commandHandlerProvider.getHandler(entity, action);
            });
        });

    }
}
