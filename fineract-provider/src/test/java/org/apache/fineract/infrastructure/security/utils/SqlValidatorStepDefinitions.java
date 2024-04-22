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
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.security.exception.SqlValidationException;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SqlValidatorStepDefinitions implements En {

    private static final Logger log = LoggerFactory.getLogger(SqlValidatorStepDefinitions.class);

    @Autowired
    private SqlValidator sqlValidator;

    private Executable executable;
    private String statement;
    private Integer fuzzy = 0;

    public SqlValidatorStepDefinitions() {
        Given("/^A partial SQL statement (.*) with whitespaces fuzzy degree (\\d*)$/", (String statement, Integer fuzzy) -> {
            this.statement = statement;
            if (fuzzy != null) {
                this.fuzzy = fuzzy;
            }
        });

        When("Validating the partial statement", () -> {
            if (fuzzy != null && fuzzy > 0) {
                String whitespaces = RandomStringUtils.random(fuzzy, '\n', '\r', '\t', ' ');
                statement = statement.replaceAll(" ", whitespaces);
            }

            executable = () -> sqlValidator.validate(statement);
        });

        Then("/^The validator had exception message (.*)$/", (String expectedMessage) -> {
            if (StringUtils.isBlank(expectedMessage)) {
                Assertions.assertDoesNotThrow(executable);
            } else {
                var exception = Assertions.assertThrows(SqlValidationException.class, executable);

                assertEquals(expectedMessage, exception.getMessage());

                // log.info("Validator message: {}", exception.getMessage());
            }
        });
    }
}
