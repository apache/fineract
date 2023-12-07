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
package org.apache.fineract.commands.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

import io.cucumber.java8.En;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.event.RetryEvent;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.exception.RollbackTransactionNotApprovedException;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;

public class CommandServiceStepDefinitions implements En {

    private static final Logger log = LoggerFactory.getLogger(CommandServiceStepDefinitions.class);

    @Autowired
    private CommandProcessingService processAndLogCommandService;

    @Autowired
    private RetryRegistry retryRegistry;

    private PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;

    private DummyCommand command;

    private RetryEvent retryEvent;

    private AtomicInteger counter = new AtomicInteger();

    public CommandServiceStepDefinitions() {
        Given("/^A command source write service$/", () -> {
            this.commandSourceWritePlatformService = new DummyCommandSourceWriteService(processAndLogCommandService);
            this.command = new DummyCommand();
            FineractRequestContextHolder contextHolder = Mockito.spy(FineractRequestContextHolder.class);
            ReflectionTestUtils.setField(processAndLogCommandService, "fineractRequestContextHolder", contextHolder);
            Mockito.when(contextHolder.getAttribute(any(), any())).thenThrow(new CannotAcquireLockException("BLOW IT UP!!!"))
                    .thenThrow(new ObjectOptimisticLockingFailureException("Dummy", new RuntimeException("BLOW IT UP!!!")))
                    .thenThrow(new RollbackTransactionNotApprovedException(1L, null));

            this.retryRegistry.retry("executeCommand").getEventPublisher().onRetry(event -> {
                log.warn("... retry event: {}", event);

                counter.incrementAndGet();
                CommandServiceStepDefinitions.this.retryEvent = event;
            });
        });

        When("/^The user executes the command via a command write service with exceptions$/", () -> {
            try {
                this.commandSourceWritePlatformService.logCommandSource(command);
            } catch (Exception e) {
                // TODO: this exception is OK for now; we need to fix the whole tenant based data source setup
                log.warn("At the moment mocking data access is so incredibly hard... it's easier to just ignore this exception: {}",
                        e.getMessage());
            }
        });

        Then("/^The command processing service should fallback as expected$/", () -> {
            assertNotNull(retryEvent);
            assertEquals("executeCommand", retryEvent.getName());
            assertEquals(2, retryEvent.getNumberOfRetryAttempts());
        });

        Then("/^The command processing service execute function should be called 3 times$/", () -> {
            assertEquals(2, counter.get());
        });
    }

    public static class DummyCommand extends CommandWrapper {

        public DummyCommand() {
            super(null, null, null, null, null, null, null, null, null, null, "{}", null, null, null, null, null, null,
                    UUID.randomUUID().toString());
        }

        @Override
        public String actionName() {
            return "dummy";
        }
    }

    public static class DummyCommandSourceWriteService implements PortfolioCommandSourceWritePlatformService {

        private final CommandProcessingService processAndLogCommandService;

        public DummyCommandSourceWriteService(CommandProcessingService processAndLogCommandService) {
            this.processAndLogCommandService = processAndLogCommandService;
        }

        @Override
        public CommandProcessingResult logCommandSource(CommandWrapper wrapper) {
            final String json = wrapper.getJson();
            JsonCommand command = JsonCommand.from(json, null, null, wrapper.getEntityName(), wrapper.getEntityId(),
                    wrapper.getSubentityId(), wrapper.getGroupId(), wrapper.getClientId(), wrapper.getLoanId(), wrapper.getSavingsId(),
                    wrapper.getTransactionId(), wrapper.getHref(), wrapper.getProductId(), wrapper.getCreditBureauId(),
                    wrapper.getOrganisationCreditBureauId(), wrapper.getJobName());

            return this.processAndLogCommandService.executeCommand(wrapper, command, true);
        }

        @Override
        public CommandProcessingResult approveEntry(Long id) {
            return null;
        }

        @Override
        public Long rejectEntry(Long id) {
            return null;
        }

        @Override
        public Long deleteEntry(Long makerCheckerId) {
            return null;
        }
    }
}
