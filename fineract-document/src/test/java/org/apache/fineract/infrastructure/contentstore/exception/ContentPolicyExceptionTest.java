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
package org.apache.fineract.infrastructure.contentstore.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
import org.apache.fineract.infrastructure.core.exceptionmapper.PlatformApiDataValidationExceptionMapper;
import org.junit.jupiter.api.Test;

class ContentPolicyExceptionTest {

    @Test
    void mapsToBadRequest() {
        var message = "Detected mime type text/plain for filename test.txt not allowed!";

        var response = new PlatformApiDataValidationExceptionMapper().toResponse(new ContentPolicyException(message));

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity()).isInstanceOf(ApiGlobalErrorResponse.class);

        var body = (ApiGlobalErrorResponse) response.getEntity();

        assertThat(body.getUserMessageGlobalisationCode()).isEqualTo("error.msg.content.policy");
        assertThat(body.getErrors()).singleElement().satisfies(error -> {
            assertThat(error.getParameterName()).isEqualTo("file");
            assertThat(error.getDefaultUserMessage()).isEqualTo(message);
        });
    }
}
