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
 * Extension methods for {@link Call}. This class is recommended to be statically imported.
 *
 * @author Michael Vorburger.ch
 */
public final class Calls {

    private Calls() {}

    /**
     * Execute a Call, expecting success.
     *
     * @param call
     *            the Call to execute
     * @return the body of the successful call (never null)
     * @throws CallFailedRuntimeException
     *             thrown either if a problem occurred talking to the server, or the HTTP response code was not
     *             [200..300) successful
     */
    public static <T> T ok(Call<T> call) throws CallFailedRuntimeException {
        Response<T> response = executeU(call);
        if (response.isSuccessful()) {
            return response.body();
        }
        throw new CallFailedRuntimeException(call, response);
    }

    public static <T> Response<T> executeU(Call<T> call) throws CallFailedRuntimeException {
        try {
            return call.execute();
        } catch (IOException e) {
            throw new CallFailedRuntimeException(call, e);
        }
    }
}
