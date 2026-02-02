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
package org.apache.fineract.command.implementation;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandAuditor;
import org.apache.fineract.command.core.CommandHandler;
import org.apache.fineract.command.core.CommandRouter;

@Slf4j
// TODO: not ready yet for prime time
// @Component
// @ConditionalOnProperty(value = "fineract.command.executor", havingValue = "async")
public class AsynchronousCommandExecutor extends BaseCommandExecutor {

    public AsynchronousCommandExecutor(CommandRouter router, CommandAuditor auditor) {
        super(router, auditor);
    }

    @Override
    public <REQ, RES> Supplier<RES> execute(final Command<REQ> command) {
        CompletableFuture<RES> future = CompletableFuture.supplyAsync(() -> {
            auditor.processing(command);

            CommandHandler<REQ, RES> handler = router.route(command);

            return handler.handle(command);
        }).whenComplete((response, t) -> {
            if (t == null) {
                auditor.processed(command, response);
            } else {
                command.setError(t.getMessage());

                auditor.error(command);
            }
        });

        return () -> {
            try {
                // TODO: make this configurable
                return future.get(3, SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
