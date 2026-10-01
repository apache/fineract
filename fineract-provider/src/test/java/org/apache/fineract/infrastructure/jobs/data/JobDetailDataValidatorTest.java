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
package org.apache.fineract.infrastructure.jobs.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class JobDetailDataValidatorTest {

    private final JobDetailDataValidator validator = new JobDetailDataValidator(new FromJsonHelper());

    @ParameterizedTest
    @ValueSource(strings = { "{\"active\": null}", "{\"active\": \"\"}" })
    void validateForUpdateRejectsMissingActiveWithOneValidationError(final String json) {
        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateForUpdate(json));

        assertThat(exception.getErrors()).extracting(ApiParameterError::getUserMessageGlobalisationCode)
                .containsExactly("validation.msg.schedulerjob.active.cannot.be.blank");
    }
}
