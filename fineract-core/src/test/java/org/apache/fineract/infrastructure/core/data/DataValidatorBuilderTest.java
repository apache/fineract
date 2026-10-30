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
package org.apache.fineract.infrastructure.core.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class DataValidatorBuilderTest {

    @Test
    void isOneOfTheseStringValuesListAcceptsMatchingValueCaseInsensitively() {
        List<ApiParameterError> errors = new ArrayList<>();
        new DataValidatorBuilder(errors).resource("test").parameter("action").value("ACTIVE")
                .isOneOfTheseStringValues(List.of("ACTIVE", "INACTIVE"));
        assertThat(errors).isEmpty();

        errors.clear();
        new DataValidatorBuilder(errors).resource("test").parameter("action").value("active")
                .isOneOfTheseStringValues(List.of("ACTIVE", "INACTIVE"));
        assertThat(errors).isEmpty();
    }

    @Test
    void isOneOfTheseStringValuesVarargsAcceptsMatchingValueCaseInsensitively() {
        List<ApiParameterError> errors = new ArrayList<>();
        new DataValidatorBuilder(errors).resource("test").parameter("action").value("ACTIVE").isOneOfTheseStringValues("ACTIVE",
                "INACTIVE");
        assertThat(errors).isEmpty();

        errors.clear();
        new DataValidatorBuilder(errors).resource("test").parameter("action").value("active").isOneOfTheseStringValues("ACTIVE",
                "INACTIVE");
        assertThat(errors).isEmpty();

        errors.clear();
        new DataValidatorBuilder(errors).resource("test").parameter("action").value("Active").isOneOfTheseStringValues("ACTIVE",
                "INACTIVE");
        assertThat(errors).isEmpty();
    }

    @Test
    void isOneOfTheseStringValuesVarargsRejectsInvalidValue() {
        List<ApiParameterError> errors = new ArrayList<>();
        new DataValidatorBuilder(errors).resource("test").parameter("action").value("UNKNOWN").isOneOfTheseStringValues("ACTIVE",
                "INACTIVE");

        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst().getUserMessageGlobalisationCode())
                .isEqualTo("validation.msg.test.action.is.not.one.of.expected.enumerations");
        assertThat(errors.getFirst().getDefaultUserMessage()).isEqualTo("The parameter `action` must be one of [ ACTIVE, INACTIVE ] .");
    }

    @Test
    void isOneOfTheseStringValuesVarargsHandlesNullWhenIgnored() {
        List<ApiParameterError> errors = new ArrayList<>();
        new DataValidatorBuilder(errors).resource("test").parameter("action").value(null).ignoreIfNull().isOneOfTheseStringValues("ACTIVE",
                "INACTIVE");
        assertThat(errors).isEmpty();
    }
}
