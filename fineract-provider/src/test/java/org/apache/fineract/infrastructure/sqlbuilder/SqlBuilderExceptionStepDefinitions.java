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
package org.apache.fineract.infrastructure.sqlbuilder;

import static org.apache.fineract.infrastructure.sqlbuilder.SqlBuilderUtil.transform;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.cucumber.java8.En;
import org.apache.fineract.infrastructure.security.utils.SQLBuilder;

public class SqlBuilderExceptionStepDefinitions implements En {

    private SQLBuilder sqlBuilder;

    private Throwable throwable;

    public SqlBuilderExceptionStepDefinitions() {
        Given("/^An illegal criteria (.*) with argument (.*)$/", (String criteria, String argument) -> {
            sqlBuilder = new SQLBuilder();
            try {
                sqlBuilder.addCriteria(criteria, transform(argument));
            } catch (Throwable t) {
                this.throwable = t;
            }
        });

        Then("/^The builder should throw an exception (.*) with message (.*)$/", (String exception, String message) -> {
            assertEquals(Class.forName(exception), throwable.getClass());
        });
    }
}
