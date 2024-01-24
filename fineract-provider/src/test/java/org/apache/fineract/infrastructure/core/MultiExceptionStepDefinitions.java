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
package org.apache.fineract.infrastructure.core;

import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.cucumber.java8.En;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.exception.MultiException;

@SuppressFBWarnings(value = "RV_EXCEPTION_NOT_THROWN", justification = "False positive")
public class MultiExceptionStepDefinitions implements En {

    private List<Throwable> exceptions = new ArrayList<>();

    @SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW")
    public MultiExceptionStepDefinitions() {
        Given("/^A multi exception with exceptions (.*) and (.*)$/", (String exception1, String exception2) -> {
            if (!StringUtils.isBlank(exception1)) {
                this.exceptions.add(Class.forName(exception1).asSubclass(Throwable.class).getDeclaredConstructor().newInstance());
            }
            if (!StringUtils.isBlank(exception2)) {
                this.exceptions.add(Class.forName(exception2).asSubclass(Throwable.class).getDeclaredConstructor().newInstance());
            }
        });

        Then("/^A (.*) should be thrown$/", (String expected) -> {
            assertThrows(Class.forName(expected).asSubclass(Throwable.class), () -> {
                throw new MultiException(exceptions);
            });
        });
    }
}
