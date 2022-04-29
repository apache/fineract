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
package org.apache.fineract.infrastructure.security.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.cucumber.java8.En;

public class LogParameterEscapeUtilTest implements En {

    private String logParameter;

    private String escapedLogParameter;

    public LogParameterEscapeUtilTest() {
        Given("A simple log message without any special character", () -> {
            logParameter = "This is a very simple String without any special character.";
        });
        Given("A log message with new line, carriage return and tab characters", () -> {
            logParameter = "This String contains new line\n, carriage return\r and tab\t characters.";
        });

        When("Log parameter escape util escaping the special characters", () -> {
            escapedLogParameter = LogParameterEscapeUtil.escapeLogParameter(logParameter);
        });

        Then("The log message stays as it is", () -> {
            assertEquals(logParameter, escapedLogParameter);
        });
        Then("The escape util changes the special characters to `_`", () -> {
            assertEquals("This String contains new line_, carriage return_ and tab_ characters.", escapedLogParameter);
        });
    }

}
