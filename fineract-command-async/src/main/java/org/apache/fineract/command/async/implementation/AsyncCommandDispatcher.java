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
package org.apache.fineract.command.async.implementation;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.command.core.CommandHandlerManager;
import org.apache.fineract.command.core.CommandHookManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// TODO: WIP - not ready yet for prime time
@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(value = "fineract.command.async.enabled", havingValue = "true")
public class AsyncCommandDispatcher implements CommandDispatcher {

    private final CommandHandlerManager handlerManager;
    private final CommandHookManager hookManager;

    @Override
    public <REQ, RES> Supplier<RES> dispatch(final Command<REQ> command) {
        requireNonNull(command, "Command must not be null");

        CompletableFuture<RES> future = CompletableFuture.supplyAsync(() -> {
            hookManager.before(command);

            RES response = handlerManager.handle(command);

            hookManager.after(command, response);

            return response;
        }).whenComplete((response, t) -> {
            if (t != null) {
                hookManager.error(command, t);
            }
        });

        return () -> {
            try {
                // TODO: make this configurable
                return future.get(3, SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
