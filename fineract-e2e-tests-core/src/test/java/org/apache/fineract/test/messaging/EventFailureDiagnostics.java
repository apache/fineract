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
package org.apache.fineract.test.messaging;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.test.messaging.config.EventProperties;
import org.apache.fineract.test.messaging.store.EventStore;
import org.springframework.stereotype.Component;

/**
 * Explains why an event assertion timed out.
 *
 * <p>
 * An event only reaches the test through two independent stages: it must be persisted into {@code m_external_event} by
 * the business transaction, and it must then be published to the broker by the "Send Asynchronous Events" job. A
 * timeout alone cannot tell those apart, which makes intermittent CI failures hard to attribute. This queries the
 * server for what it actually persisted so the log states which stage lost the event.
 *
 * <p>
 * Only runs when {@code EVENT_FAILURE_DIAGNOSTICS_ENABLED} is set, and only after an assertion has already failed, so
 * it never affects passing runs. Any error raised while gathering diagnostics is swallowed - it must never replace the
 * original assertion failure.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventFailureDiagnostics {

    private static final Gson GSON = new Gson();
    private static final Type EVENT_LIST_TYPE = new TypeToken<List<Map<String, Object>>>() {}.getType();

    private final FineractFeignClient fineractClient;
    private final EventProperties eventProperties;
    private final EventStore eventStore;

    /**
     * Reports whether the missing event was persisted server-side.
     *
     * @param eventName
     *            external event type, e.g. {@code LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent}
     * @param aggregateRootId
     *            aggregate root the event belongs to (the loan id for loan events); may be null
     * @param expectedPayloadId
     *            id the assertion waited for, matched against {@code payLoad.id} - for loan transaction events this is
     *            the loan transaction id
     */
    public void reportMissingEvent(final String eventName, final Long aggregateRootId, final Long expectedPayloadId) {
        if (!eventProperties.isFailureDiagnosticsEnabled()) {
            return;
        }
        try {
            final String rawJson = ok(() -> fineractClient.internalExternalEvents().getExternalEvents(eventName, aggregateRootId));
            final List<Map<String, Object>> persisted = GSON.fromJson(rawJson, EVENT_LIST_TYPE);

            log.error("=== EVENT DIAGNOSTICS: {} (aggregateRootId={}, expected payLoad.id={}) ===", eventName, aggregateRootId,
                    expectedPayloadId);

            if (persisted == null || persisted.isEmpty()) {
                log.error("""
                        VERDICT: NOT PERSISTED - server has no '{}' event for aggregateRootId={}.
                        The event was never written to m_external_event, so the loss is upstream of the sender job \
                        (event not raised, or discarded before/at transaction commit).
                        """, eventName, aggregateRootId);
            } else if (containsPayloadId(persisted, expectedPayloadId)) {
                log.error("""
                        VERDICT: PERSISTED BUT NOT DELIVERED - server persisted a '{}' event with payLoad.id={}, \
                        but it never reached the test's JMS listener within {}ms.
                        The loss is in delivery (sender job selection, broker publish, or test-side consumption).
                        """, eventName, expectedPayloadId, eventProperties.getWaitTimeoutInMillis());
            } else {
                log.error("""
                        VERDICT: PERSISTED UNDER A DIFFERENT ID - server has {} '{}' event(s) for aggregateRootId={}, \
                        but none with payLoad.id={}.
                        The assertion may be waiting on the wrong transaction id.
                        """, persisted.size(), eventName, aggregateRootId, expectedPayloadId);
            }

            log.error("Server-persisted events (raw): {}", rawJson);
            log.error("Events of this type received by the test: {}", describeReceived(eventName));
            log.error("=== END EVENT DIAGNOSTICS ===");
        } catch (Exception e) {
            // Never let diagnostics mask the real assertion failure.
            log.error("Failed to collect event diagnostics for {} (aggregateRootId={})", eventName, aggregateRootId, e);
        }
    }

    private boolean containsPayloadId(final List<Map<String, Object>> persisted, final Long expectedPayloadId) {
        if (expectedPayloadId == null) {
            return false;
        }
        return persisted.stream().anyMatch(event -> {
            final Object payload = event.get("payLoad");
            if (!(payload instanceof Map<?, ?> payloadMap)) {
                return false;
            }
            final Object id = payloadMap.get("id");
            // Gson decodes untyped JSON numbers as Double.
            return id instanceof Number number && expectedPayloadId.longValue() == number.longValue();
        });
    }

    private String describeReceived(final String eventName) {
        return eventStore.getReceivedEvents().stream().filter(message -> eventName.equals(message.getType()))
                .map(message -> "idempotencyKey=" + message.getIdempotencyKey() + ", businessDate=" + message.getBusinessDate()).toList()
                .toString();
    }
}
