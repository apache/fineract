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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.CommandStore;
import org.apache.fineract.command.idempotency.IdempotencyCommandProperties;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Records what a request that claimed an idempotency key was answered with, for the parts the command itself could not
 * record.
 *
 * <p>
 * A command that produced a result stores that result as an object, and a repeat is answered by rehydrating it and
 * letting the web layer render it again - which is what makes a replay identical to the original. So what is captured
 * here never replaces a stored result; it only fills in what is missing. The status code is always missing, because the
 * command layer has no notion of one. The body is missing only when the command produced no result of its own: a
 * failure, whose answer is rendered from an exception, and an answer that is not JSON.
 */
@Slf4j
@RequiredArgsConstructor
public class IdempotencyResponseCaptureFilter extends OncePerRequestFilter {

    private final CommandStore store;

    private final IdempotencyCommandProperties properties;

    /**
     * A request that carries no idempotency key can claim nothing, so there is nothing to record for it. Skipping it
     * here is what keeps this off the hot path: everything else on the API is served without its response being
     * buffered.
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !StringUtils.hasText(request.getHeader(properties.getKeyHeaderName()));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        var wrapper = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(request, wrapper);
        } finally {
            record(request, wrapper);

            // the body has been read out of the wrapper, so it has to be written on to the real response
            wrapper.copyBodyToResponse();
        }
    }

    private void record(HttpServletRequest request, ContentCachingResponseWrapper wrapper) {
        var key = (String) request.getAttribute(COMMAND_IDEMPOTENCY_CLAIMED_KEY_ATTRIBUTE);

        if (!StringUtils.hasText(key)) {
            // no command claimed a key during this request, so there is nothing to replay later
            return;
        }

        // the key is unique together with its distinctor, so both are needed to find the row this request claimed
        var distinctor = (String) request.getAttribute(COMMAND_IDEMPOTENCY_CLAIMED_DISTINCTOR_ATTRIBUTE);
        var body = replayableBody(key, wrapper);

        try {
            store.recordResponse(key, distinctor == null ? "" : distinctor, wrapper.getStatus(), body);
        } catch (RuntimeException e) {
            // the command itself has been processed and answered; failing here must not change that outcome. The
            // record is left claiming an outcome it cannot produce, so the failure is logged loudly - but with the
            // shape of the answer rather than the answer itself, which on this API carries customer data.
            log.error(
                    "Unable to record the response for idempotency key {} (status {}, {} {} bytes); a repeat of this key "
                            + "cannot be answered from it",
                    key, wrapper.getStatus(), describe(wrapper.getContentType()),
                    body == null ? 0 : body.getBytes(StandardCharsets.UTF_8).length, e);
        }
    }

    private static String describe(String contentType) {
        return contentType == null ? "untyped" : contentType;
    }

    /**
     * Only a JSON body is worth keeping: the response is kept in a JSON column, and anything else - a file download, an
     * empty body - could not be read back. The status code is still recorded for those.
     */
    private String replayableBody(String key, ContentCachingResponseWrapper wrapper) {
        var body = new String(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8);

        if (!StringUtils.hasText(body)) {
            return null;
        }

        var contentType = wrapper.getContentType();

        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).contains("json")) {
            // the response column holds JSON and nothing else, so this answer cannot be stored. Only its shape is
            // logged: the body of a response on this API can carry customer data, and keeping that out of places it
            // does not belong is the point of the masking this module does elsewhere.
            log.warn(
                    "Cannot store a {} answer for idempotency key {} (status {}, {} bytes); a repeat will be answered "
                            + "with the status and an empty body",
                    describe(contentType), key, wrapper.getStatus(), body.getBytes(StandardCharsets.UTF_8).length);

            return null;
        }

        return body;
    }
}
