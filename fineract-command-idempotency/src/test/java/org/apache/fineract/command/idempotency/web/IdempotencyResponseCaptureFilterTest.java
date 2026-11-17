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
package org.apache.fineract.command.idempotency.web;

import static org.apache.fineract.command.idempotency.IdempotencyCommandConstants.COMMAND_IDEMPOTENCY_CLAIMED_DISTINCTOR_ATTRIBUTE;
import static org.apache.fineract.command.idempotency.IdempotencyCommandConstants.COMMAND_IDEMPOTENCY_CLAIMED_KEY_ATTRIBUTE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.apache.fineract.command.core.CommandStore;
import org.apache.fineract.command.idempotency.IdempotencyCommandProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Covers what the filter does with an answer it cannot store.
 *
 * <p>
 * The response column holds JSON and nothing else, so an answer of any other kind is dropped and the repeat of that key
 * is left with a status and an empty body. That is worth a log line, but only the shape of the answer: a response on
 * this API can carry customer data, and keeping that out of the logs is the concern the masking elsewhere answers.
 */
class IdempotencyResponseCaptureFilterTest {

    private static final String KEY = "a-key";

    private final CommandStore store = mock(CommandStore.class);

    private final IdempotencyResponseCaptureFilter filter = new IdempotencyResponseCaptureFilter(store,
            IdempotencyCommandProperties.builder().build());

    private final ListAppender<ILoggingEvent> captured = new ListAppender<>();

    private Logger logger;

    @BeforeEach
    void captureLogging() {
        // by name rather than by class: this is deliberately another class's logger, which is what the appender
        // has to attach to in order to read what the filter logged
        logger = (Logger) LoggerFactory.getLogger(IdempotencyResponseCaptureFilter.class.getName());

        captured.start();
        logger.addAppender(captured);
    }

    @AfterEach
    void stopCapturingLogging() {
        logger.detachAppender(captured);
    }

    @Test
    void anAnswerThatIsNotJsonIsReportedWithoutItsBody() throws Exception {
        var csv = "id,amount\n7,1.50\n";
        var response = runWith("text/csv", csv);

        verify(store).recordResponse(eq(KEY), eq(""), eq(200), isNull());

        assertFalse(logged().contains("id,amount"), "The body must not reach the log: responses here carry customer data.");
        assertTrue(
                logged().contains(KEY) && logged().contains("text/csv")
                        && logged().contains(csv.getBytes(java.nio.charset.StandardCharsets.UTF_8).length + " bytes"),
                "What is logged is the shape of the answer - key, status, content type and size - so the loss is "
                        + "diagnosable without copying the answer into the log.");
        assertEquals(csv, response.getContentAsString(), "Whatever the filter does with its copy, the client still receives the answer.");
    }

    @Test
    void anAnswerThatCannotBeRecordedIsReportedWithoutItsBody() throws Exception {
        doThrow(new IllegalStateException("the database is unavailable")).when(store).recordResponse(eq(KEY), eq(""), eq(200),
                eq("{\"amount\":1.50}"));

        runWith("application/json", "{\"amount\":1.50}");

        assertFalse(logged().contains("{\"amount\":1.50}"), "The body must not reach the log, even when recording it failed.");
        assertTrue(logged().contains(KEY) && logged().contains("application/json"),
                "A record that could not be written leaves the key unanswerable, which has to be loud - but it is the "
                        + "failure that is logged, not the answer.");
    }

    @Test
    void aJsonAnswerIsRecordedRatherThanLogged() throws Exception {
        runWith("application/json", "{\"amount\":1.50}");

        verify(store).recordResponse(KEY, "", 200, "{\"amount\":1.50}");

        assertTrue(logged().isEmpty(), "Nothing is lost on the ordinary path, so nothing is logged about it.");
    }

    @Test
    void theAnswerIsRecordedAgainstTheDistinctorTheKeyWasClaimedWith() throws Exception {
        runWith("application/json", "{\"amount\":1.50}", "PAYMENTTYPE|CREATE");

        verify(store).recordResponse(KEY, "PAYMENTTYPE|CREATE", 200, "{\"amount\":1.50}");
    }

    private MockHttpServletResponse runWith(String contentType, String body) throws Exception {
        return runWith(contentType, body, null);
    }

    private MockHttpServletResponse runWith(String contentType, String body, String distinctor) throws Exception {
        var request = new MockHttpServletRequest("POST", "/commands");

        request.addHeader(IdempotencyCommandProperties.builder().build().getKeyHeaderName(), KEY);
        request.setAttribute(COMMAND_IDEMPOTENCY_CLAIMED_KEY_ATTRIBUTE, KEY);

        if (distinctor != null) {
            request.setAttribute(COMMAND_IDEMPOTENCY_CLAIMED_DISTINCTOR_ATTRIBUTE, distinctor);
        }

        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain() {

            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) throws java.io.IOException {
                res.setContentType(contentType);
                res.getWriter().write(body);
            }
        };

        filter.doFilter(request, response, chain);

        return response;
    }

    private String logged() {
        return captured.list.stream().filter(event -> event.getLevel().isGreaterOrEqual(Level.WARN)).map(ILoggingEvent::getFormattedMessage)
                .reduce("", String::concat);
    }
}
