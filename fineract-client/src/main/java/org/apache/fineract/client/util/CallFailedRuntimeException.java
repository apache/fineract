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
package org.apache.fineract.client.util;

import java.io.IOException;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Exception thrown by {@link Calls} utility when {@link Call}s fail.
 *
 * @author Michael Vorburger.ch
 */
public class CallFailedRuntimeException extends RuntimeException {

    private final Call<?> call;
    private final Response<?> response;

    public <T> CallFailedRuntimeException(Call<T> call, Throwable t) {
        super("HTTP failed: " + call.request().toString(), t);
        this.call = call;
        this.response = null;
    }

    public <T> CallFailedRuntimeException(Call<T> call, Response<T> response) {
        super(message(call, response));
        this.call = call;
        this.response = response;
    }

    private static String message(Call<?> call, Response<?> response) {
        StringBuilder sb = new StringBuilder("HTTP failed: " + call.request() + "; " + response);
        if (response.message() != null && !response.message().isEmpty()) {
            sb.append("; message: " + response.message());
        }
        String errorBody;
        try {
            errorBody = response.errorBody().string();
            if (errorBody != null) {
                sb.append("; errorBody: " + errorBody);
            }
        } catch (IOException e) {
            // Ignore.
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public <T> Call<T> getCall() {
        return (Call<T>) call;
    }

    @SuppressWarnings("unchecked")
    public <T> Response<T> getResponse() {
        return (Response<T>) response;
    }
}
