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
package org.apache.fineract.infrastructure.hooks.command;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.HookableCommand;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.hooks.event.HookEvent;
import org.apache.fineract.infrastructure.hooks.event.HookEventSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class WebhookCommandHookAfterTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();
    private final PlatformSecurityContext context = mock(PlatformSecurityContext.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final AppUser user = mock(AppUser.class);

    private WebhookCommandHookAfter underTest;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 1, 1))));
        when(context.authenticatedUser()).thenReturn(user);
        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("mifos");
        when(user.getDisplayName()).thenReturn("App Administrator");
        underTest = new WebhookCommandHookAfter(context, eventPublisher, mapper);
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldPublishHookEventForHookableCommand() {
        final TestResponse response = new TestResponse(7L);

        underTest.onAfter(hookableCommand(), response);

        final HookEvent event = publishedEvent();
        final HookEventSource source = (HookEventSource) event.getSource();
        assertEquals("DOCUMENT", source.getEntityName());
        assertEquals("CREATE", source.getActionName());
        assertSame(user, event.getAppUser());

        final JsonNode payload = mapper.readTree(event.getPayload());
        assertEquals("DOCUMENT", payload.get("entityName").asString());
        assertEquals("CREATE", payload.get("actionName").asString());
        assertEquals(1L, payload.get("createdBy").asLong());
        assertEquals("mifos", payload.get("createdByName").asString());
        assertEquals("App Administrator", payload.get("createdByFullName").asString());
        assertEquals("contract.pdf", payload.get("request").get("name").asString());
        // the uploaded content is not part of the notification
        assertFalse(payload.get("request").has("stream"));
        assertEquals(7L, payload.get("response").get("resourceId").asLong());
        assertFalse(payload.get("timestamp").asString().isEmpty());
    }

    @Test
    void shouldIgnoreCommandThatIsNotHookable() {
        final Command<Object> command = new Command<>();
        command.setPayload(new TestRequest("contract.pdf", null));

        underTest.onAfter(command, new TestResponse(7L));

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldDeferPublicationUntilEnclosingTransactionCommits() {
        TransactionSynchronizationManager.initSynchronization();

        underTest.onAfter(hookableCommand(), new TestResponse(7L));

        verifyNoInteractions(eventPublisher);

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);

        assertEquals("DOCUMENT", ((HookEventSource) publishedEvent().getSource()).getEntityName());
    }

    @Test
    void shouldNotFailCommandWhenPublishingFails() {
        doThrow(new IllegalStateException("listener failed")).when(eventPublisher).publishEvent(any(HookEvent.class));

        assertDoesNotThrow(() -> underTest.onAfter(hookableCommand(), new TestResponse(7L)));
    }

    private HookEvent publishedEvent() {
        final ArgumentCaptor<HookEvent> captor = ArgumentCaptor.forClass(HookEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        return captor.getValue();
    }

    private static Command<Object> hookableCommand() {
        final TestHookableCommand command = new TestHookableCommand();
        command.setPayload(new TestRequest("contract.pdf", new ByteArrayInputStream(new byte[] { 1, 2, 3 })));
        return command;
    }

    private static final class TestHookableCommand extends Command<Object> implements HookableCommand {

        @Override
        public String hookEntityName() {
            return "DOCUMENT";
        }

        @Override
        public String hookActionName() {
            return "CREATE";
        }
    }

    private record TestRequest(String name, @JsonIgnore InputStream stream) {
    }

    private record TestResponse(Long resourceId) {
    }
}
