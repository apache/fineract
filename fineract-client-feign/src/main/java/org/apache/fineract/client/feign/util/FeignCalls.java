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
package org.apache.fineract.client.feign.util;

import feign.FeignException;
import java.util.function.Supplier;

/**
 * Extension methods for Feign calls. This class is recommended to be statically imported.
 */
public final class FeignCalls {

    private FeignCalls() {}

    /**
     * Execute a Feign call, expecting success, returning strongly typed body. This covers the most typical use and is
     * thus most used.
     *
     * @param feignCall
     *            the Feign call to execute
     * @return the result of the successful call (never null)
     * @throws CallFailedRuntimeException
     *             thrown if a problem occurred talking to the server, or the HTTP response code was not [200..300)
     *             successful
     */
    public static <T> T ok(Supplier<T> feignCall) throws CallFailedRuntimeException {
        try {
            return feignCall.get();
        } catch (FeignException e) {
            throw new CallFailedRuntimeException(e);
        }
    }

    /**
     * Execute a Feign call that returns void. This is useful for operations that don't return a response body.
     *
     * @param feignCall
     *            the Feign call to execute
     * @throws CallFailedRuntimeException
     *             thrown if a problem occurred talking to the server, or the HTTP response code was not [200..300)
     *             successful
     */
    public static void executeVoid(Runnable feignCall) throws CallFailedRuntimeException {
        try {
            feignCall.run();
        } catch (FeignException e) {
            throw new CallFailedRuntimeException(e);
        }
    }

    /**
     * Execute a Feign call, returning the result without throwing CallFailedRuntimeException. This is useful when you
     * want to handle FeignException yourself.
     *
     * @param feignCall
     *            the Feign call to execute
     * @return the result of the call
     * @throws FeignException
     *             thrown if a problem occurred talking to the server
     */
    public static <T> T execute(Supplier<T> feignCall) throws FeignException {
        return feignCall.get();
    }
}
