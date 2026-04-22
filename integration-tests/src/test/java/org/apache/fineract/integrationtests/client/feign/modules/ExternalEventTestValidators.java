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
package org.apache.fineract.integrationtests.client.feign.modules;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;

/**
 * Static assertion methods for verifying external business event payloads.
 */
public final class ExternalEventTestValidators {

    private ExternalEventTestValidators() {}

    /**
     * Finds the event matching the given loan (by aggregateRootId) from a list of events.
     */
    public static ExternalEventResponse findEventForLoan(List<ExternalEventResponse> events, Long loanId) {
        assertThat(events).as("Expected at least one external event").isNotEmpty();
        ExternalEventResponse event = events.stream().filter(e -> loanId.equals(e.getAggregateRootId())).findFirst().orElse(null);
        assertThat(event).as("Expected an event for loan %d", loanId).isNotNull();
        return event;
    }

    /**
     * Asserts that the event payload contains originator details with the given external IDs at the top level
     */
    public static void assertOriginators(ExternalEventResponse event, String... expectedExternalIds) {
        assertOriginatorsAtPath(event.getPayLoad(), expectedExternalIds);
    }

    /**
     * Asserts that the event payload contains originator details at a nested path (e.g.&nbsp;"transactionToAdjust" for
     * LoanTransactionAdjustmentDataV1).
     */
    public static void assertOriginatorsInField(ExternalEventResponse event, String nestedField, String... expectedExternalIds) {
        Map<String, Object> nested = getNestedMap(event.getPayLoad(), nestedField);
        assertOriginatorsAtPath(nested, expectedExternalIds);
    }

    /**
     * Asserts that originators is null at the top level of the event payload.
     */
    public static void assertNoOriginators(ExternalEventResponse event) {
        assertThat(event.getPayLoad().get("originators")).as("Expected no originators in event payload").isNull();
    }

    /**
     * Asserts that originators is null at a nested path in the event payload.
     */
    public static void assertNoOriginatorsInField(ExternalEventResponse event, String nestedField) {
        Map<String, Object> nested = getNestedMap(event.getPayLoad(), nestedField);
        assertThat(nested.get("originators")).as("Expected no originators in '%s'", nestedField).isNull();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getNestedMap(Map<String, Object> payload, String field) {
        Object nested = payload.get(field);
        assertThat(nested).as("Expected field '%s' in payload", field).isNotNull().isInstanceOf(Map.class);
        return (Map<String, Object>) nested;
    }

    @SuppressWarnings("unchecked")
    private static void assertOriginatorsAtPath(Map<String, Object> data, String... expectedExternalIds) {
        Object originators = data.get("originators");
        assertThat(originators).as("Expected originators to be present").isNotNull().isInstanceOf(List.class);

        List<Map<String, Object>> originatorList = (List<Map<String, Object>>) originators;
        assertThat(originatorList).hasSize(expectedExternalIds.length);

        for (int i = 0; i < expectedExternalIds.length; i++) {
            assertThat(originatorList.get(i).get("externalId")).as("Originator[%d].externalId", i).isEqualTo(expectedExternalIds[i]);
        }
    }
}
