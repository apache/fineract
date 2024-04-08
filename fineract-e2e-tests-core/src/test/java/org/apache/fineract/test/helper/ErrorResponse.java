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

import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.client.util.JSON;
import retrofit2.Response;

@NoArgsConstructor
@Getter
@Setter
public class ErrorResponse {

    private static final Gson GSON = new JSON().getGson();

    private String developerMessage;
    private Integer httpStatusCode;
    private List<Error> errors;

    public Error getSingleError() {
        if (errors.size() != 1) {
            throw new IllegalStateException("Multiple errors found");
        } else {
            return errors.iterator().next();
        }
    }

    public static ErrorResponse from(Response retrofitResponse) {
        try {
            String errorBody = retrofitResponse.errorBody().string();
            return GSON.fromJson(errorBody, ErrorResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("Error while parsing the error body", e);
        }
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Error {

        private String developerMessage;
    }
}
