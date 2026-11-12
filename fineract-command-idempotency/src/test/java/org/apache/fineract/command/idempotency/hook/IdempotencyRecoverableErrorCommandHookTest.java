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
package org.apache.fineract.command.idempotency.hook;

import static org.apache.fineract.command.idempotency.IdempotencyCommandConstants.COMMAND_IDEMPOTENCY_CLAIMED_KEY_ATTRIBUTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletionException;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandContext;
import org.apache.fineract.command.core.CommandStore;
import org.apache.fineract.command.core.RecoverableCommandErrorClassifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class IdempotencyRecoverableErrorCommandHookTest {

    private static final String KEY = "idk";

    private static final long COMMAND_ID = 42L;

    private final CommandStore store = mock(CommandStore.class);

    private final MockHttpServletRequest request = new MockHttpServletRequest();

    @BeforeEach
    void bindRequest() {
        request.setAttribute(COMMAND_IDEMPOTENCY_CLAIMED_KEY_ATTRIBUTE, KEY);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void unbindRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void aRecoverableFailureReleasesTheKey() {
        var ctx = failed(new IllegalStateException("try again"));

        hook(error -> true).onError(ctx);

        assertThat(ctx.getCommand().getIdempotencyKey()).as("The failure moves aside, so the audit hook stores it there.")
                .isEqualTo("idk_RE42");
        assertThat(request.getAttribute(COMMAND_IDEMPOTENCY_CLAIMED_KEY_ATTRIBUTE))
                .as("The answer the client is sent has to be recorded against the moved row.").isEqualTo("idk_RE42");
        verify(store).existsByKey("idk_RE42", "LOAN|CREATE");
    }

    @Test
    void anyOtherFailureKeepsTheKey() {
        var ctx = failed(new IllegalStateException("no"));

        hook(error -> false).onError(ctx);

        assertThat(ctx.getCommand().getIdempotencyKey()).isEqualTo(KEY);
        assertThat(request.getAttribute(COMMAND_IDEMPOTENCY_CLAIMED_KEY_ATTRIBUTE)).isEqualTo(KEY);
    }

    @Test
    void aRefusedRepeatReleasesNothing() {
        var ctx = failed(new IllegalStateException("try again"));

        ctx.setSkipExecution(true);
        hook(error -> true).onError(ctx);

        assertThat(ctx.getCommand().getIdempotencyKey()).as("A repeat never claimed the key, so it is not its to release.").isEqualTo(KEY);
        verifyNoInteractions(store);
    }

    @Test
    void aMovedKeyThatIsAlreadyTakenKeepsTheFailureUnderTheOriginalKey() {
        var ctx = failed(new IllegalStateException("try again"));

        when(store.existsByKey(anyString(), anyString())).thenReturn(true);
        hook(error -> true).onError(ctx);

        assertThat(ctx.getCommand().getIdempotencyKey()).isEqualTo(KEY);
    }

    @Test
    void aClassifierThatFailsKeepsTheKeyAndLetsTheOriginalErrorThrough() {
        var ctx = failed(new IllegalStateException("try again"));

        hook(error -> {
            throw new IllegalArgumentException("the configuration could not be read");
        }).onError(ctx);

        assertThat(ctx.getCommand().getIdempotencyKey()).isEqualTo(KEY);
    }

    @Test
    void aFailureHandedBackWrappedIsClassifiedByWhatItWraps() {
        var ctx = failed(new CompletionException(new UnsupportedOperationException("try again")));

        hook(error -> error instanceof UnsupportedOperationException).onError(ctx);

        assertThat(ctx.getCommand().getIdempotencyKey()).isEqualTo("idk_RE42");
    }

    private IdempotencyRecoverableErrorCommandHook hook(RecoverableCommandErrorClassifier classifier) {
        return new IdempotencyRecoverableErrorCommandHook(store, classifier);
    }

    private static CommandContext<Object, Object> failed(Throwable error) {
        var command = new TestCommand();

        command.setIdempotencyKey(KEY);
        command.setCustomIdempotencyDistinctor("LOAN|CREATE");
        command.setCommandId(COMMAND_ID);

        var ctx = CommandContext.builder().command(command).build();

        ctx.setError(error);

        return ctx;
    }

    private static final class TestCommand extends Command<Object> {

        @Override
        public String entityName() {
            return "LOAN";
        }

        @Override
        public String actionName() {
            return "CREATE";
        }
    }
}
