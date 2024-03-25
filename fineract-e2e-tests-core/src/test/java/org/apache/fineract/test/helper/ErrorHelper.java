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
package org.apache.fineract.test.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import org.apache.fineract.client.models.BatchResponse;
import retrofit2.Response;

public final class ErrorHelper {

    private ErrorHelper() {}

    public static void checkSuccessfulApiCall(Response response) throws IOException {
        assertThat(response.isSuccessful()).as(ErrorMessageHelper.requestFailed(response)).isTrue();

        if (response.code() != 200 && response.code() != 202 && response.code() != 204) {
            throw new AssertionError(ErrorMessageHelper.requestFailedWithCode(response));
        }
    }

    public static void checkFailedApiCall(Response response, int requiredCode) throws IOException {
        assertThat(!response.isSuccessful()).as(ErrorMessageHelper.requestFailed(response)).isTrue();

        if (response.code() != requiredCode) {
            throw new AssertionError("Request success but should fail with code: " + requiredCode);
        }
    }

    public static void checkSuccessfulBatchApiCall(Response<List<BatchResponse>> batchResponseList) {
        batchResponseList.body().forEach(response -> {
            assertThat(response.getStatusCode()).as(ErrorMessageHelper.batchRequestFailedWithCode(response)).isEqualTo(200);
        });
    }
}
