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
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.apache.fineract.batch.exception.ErrorInfo;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.HookableCommand;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
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
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class WebhookCommandHookErrorTest {

    private static final String NOT_FOUND_BODY = """
            {"developerMessage":"Document with identifier 9 does not exist","httpStatusCode":"404",\
            "defaultUserMessage":"Document with identifier 9 does not exist",\
            "userMessageGlobalisationCode":"error.msg.document.id.invalid","errors":[]}""";

    private final ObjectMapper mapper = JsonMapper.builder().build();
    private final PlatformSecurityContext context = mock(PlatformSecurityContext.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final ErrorHandler errorHandler = mock(ErrorHandler.class);
    private final AppUser user = mock(AppUser.class);

    private WebhookCommandHookError underTest;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 1, 1))));
        when(context.authenticatedUser()).thenReturn(user);
        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("mifos");
        when(user.getDisplayName()).thenReturn("App Administrator");
        underTest = new WebhookCommandHookError(new WebhookCommandEventPublisher(context, eventPublisher, mapper, errorHandler));
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldPublishExceptionEventForHookableCommand() {
        final IllegalStateException error = new IllegalStateException("not found");
        when(errorHandler.handle(error)).thenReturn(new ErrorInfo(404, 4001, NOT_FOUND_BODY, null));

        underTest.onError(hookableCommand(), error);

        final HookEvent event = publishedEvent();
        final HookEventSource source = (HookEventSource) event.getSource();
        assertEquals("DOCUMENT", source.getEntityName());
        assertEquals("DELETE", source.getActionName());
        assertSame(user, event.getAppUser());

        final JsonNode payload = mapper.readTree(event.getPayload());
        assertEquals("DOCUMENT", payload.get("entityName").asString());
        assertEquals("DELETE", payload.get("actionName").asString());
        assertEquals(1L, payload.get("createdBy").asLong());
        assertEquals("mifos", payload.get("createdByName").asString());
        assertEquals("App Administrator", payload.get("createdByFullName").asString());
        assertEquals("contract.pdf", payload.get("request").get("name").asString());
        assertFalse(payload.get("request").has("stream"));
        assertEquals("Exception", payload.get("status").asString());
        assertFalse(payload.get("timestamp").asString().isEmpty());

        final JsonNode response = payload.get("response");
        assertEquals("error.msg.document.id.invalid", response.get("userMessageGlobalisationCode").asString());
        assertEquals("Document with identifier 9 does not exist", response.get("developerMessage").asString());
        assertEquals(4001, response.get("errorCode").asInt());
        assertEquals(404, response.get("statusCode").asInt());
    }

    @Test
    void shouldSendErrorMessageThatIsNotJsonAsIs() {
        when(errorHandler.handle(any())).thenReturn(new ErrorInfo(500, null, "Unexpected failure", null));

        underTest.onError(hookableCommand(), new IllegalStateException("boom"));

        final JsonNode response = mapper.readTree(publishedEvent().getPayload()).get("response");
        assertEquals("Unexpected failure", response.get("errorMessage").asString());
        assertTrue(response.get("errorCode").isNull());
        assertEquals(500, response.get("statusCode").asInt());
    }

    @Test
    void shouldPublishImmediatelyInsideEnclosingTransaction() {
        TransactionSynchronizationManager.initSynchronization();
        when(errorHandler.handle(any())).thenReturn(new ErrorInfo(404, 4001, NOT_FOUND_BODY, null));

        underTest.onError(hookableCommand(), new IllegalStateException("not found"));

        assertEquals("Exception", mapper.readTree(publishedEvent().getPayload()).get("status").asString());
        assertTrue(TransactionSynchronizationManager.getSynchronizations().isEmpty());
    }

    @Test
    void shouldIgnoreCommandThatIsNotHookable() {
        final Command<Object> command = new Command<>();
        command.setPayload(new TestRequest("contract.pdf", null));

        underTest.onError(command, new IllegalStateException("not found"));

        verifyNoInteractions(eventPublisher, errorHandler);
    }

    @Test
    void shouldNotMaskCommandErrorWhenPublishingFails() {
        when(errorHandler.handle(any())).thenReturn(new ErrorInfo(404, 4001, NOT_FOUND_BODY, null));
        doThrow(new IllegalStateException("listener failed")).when(eventPublisher).publishEvent(any(HookEvent.class));

        assertDoesNotThrow(() -> underTest.onError(hookableCommand(), new IllegalStateException("not found")));
    }

    @Test
    void shouldNotMaskCommandErrorWhenMappingTheErrorFails() {
        when(errorHandler.handle(any())).thenThrow(new IllegalStateException("no mapper"));

        assertDoesNotThrow(() -> underTest.onError(hookableCommand(), new IllegalStateException("not found")));
        verifyNoInteractions(eventPublisher);
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
            return "DELETE";
        }
    }

    private record TestRequest(String name, @JsonIgnore InputStream stream) {
    }
}
