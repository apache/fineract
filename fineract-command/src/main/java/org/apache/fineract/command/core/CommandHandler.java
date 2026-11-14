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
package org.apache.fineract.command.core;

import com.google.common.reflect.TypeToken;
import lombok.SneakyThrows;

public interface CommandHandler<REQ, RES> {

    RES handle(Command<REQ> command);

    /**
     * The fallback a handler's {@code @Retry(fallbackMethod = "fallback")} names.
     *
     * <p>
     * Every handler that names it has to redeclare it with its own response type, even when the override does nothing
     * but call this one. resilience4j only takes a fallback whose return type fits the return type of the method it
     * guards, and this default returns {@code RES}, which erases to {@code Object}; a {@code handle} method returning a
     * concrete response does not accept that. Without the override the fallback is not found: resilience4j logs "No
     * fallback method match found" on every call and runs without one.
     */
    @SneakyThrows
    default RES fallback(Command<REQ> command, Throwable t) {
        // NOTE: any command handler can override this default to implement more specialized fallbacks.
        throw t;
    }

    default boolean matches(Command<REQ> command) {
        TypeToken<REQ> handlerType = new TypeToken<>(getClass()) {};

        return handlerType.getRawType().isAssignableFrom(command.getPayload().getClass());
    }
}
