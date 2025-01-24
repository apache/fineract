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
package org.apache.fineract.integrationtests.common.externalevents;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.PutExternalEventConfigurationsRequest;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.infrastructure.event.external.service.validation.ExternalEventDTO;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.apache.fineract.integrationtests.common.ExternalEventConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;

@Slf4j
public final class ExternalEventHelper extends IntegrationTest {

    private static final Gson GSON = new JSON().getGson();

    public ExternalEventHelper() {}

    @Builder
    public static class Filter {

        private final String idempotencyKey;
        private final String type;
        private final String category;
        private final Long aggregateRootId;

        public String toQueryParams() {
            StringBuilder stringBuilder = new StringBuilder();
            if (idempotencyKey != null) {
                stringBuilder.append("idempotencyKey=").append(idempotencyKey).append("&");
            }

            if (type != null) {
                stringBuilder.append("type=").append(type).append("&");
            }

            if (category != null) {
                stringBuilder.append("category=").append(category).append("&");
            }

            if (aggregateRootId != null) {
                stringBuilder.append("aggregateRootId=").append(aggregateRootId).append("&");
            }

            return stringBuilder.toString();

        }
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static List<ExternalEventDTO> getAllExternalEvents(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        final String url = "/fineract-provider/api/v1/internal/externalevents?" + Utils.TENANT_IDENTIFIER;
        log.info("---------------------------------GETTING ALL EXTERNAL EVENTS---------------------------------------------");
        String response = Utils.performServerGet(requestSpec, responseSpec, url);
        return GSON.fromJson(response, new TypeToken<List<ExternalEventDTO>>() {}.getType());
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static List<ExternalEventDTO> getAllExternalEvents(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, Filter filter) {
        final String url = "/fineract-provider/api/v1/internal/externalevents?" + filter.toQueryParams() + Utils.TENANT_IDENTIFIER;
        log.info("---------------------------------GETTING ALL EXTERNAL EVENTS---------------------------------------------");
        String response = Utils.performServerGet(requestSpec, responseSpec, url);
        return GSON.fromJson(response, new TypeToken<List<ExternalEventDTO>>() {}.getType());
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static void deleteAllExternalEvents(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        final String url = "/fineract-provider/api/v1/internal/externalevents?" + Utils.TENANT_IDENTIFIER;
        log.info("-----------------------------DELETE ALL EXTERNAL EVENTS PARTITIONS----------------------------------------");
        Utils.performServerDelete(requestSpec, responseSpec, url, null);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static void changeEventState(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, String eventName,
            boolean status) {
        final Map<String, Boolean> updatedConfigurations = ExternalEventConfigurationHelper.updateExternalEventConfigurations(requestSpec,
                responseSpec, "{\"externalEventConfigurations\":{\"" + eventName + "\":" + status + "}}\n");
        Assertions.assertEquals(updatedConfigurations.size(), 1);
        Assertions.assertTrue(updatedConfigurations.containsKey(eventName));
        Assertions.assertEquals(status, updatedConfigurations.get(eventName));
    }

    public void configureBusinessEvent(String eventName, boolean enabled) {
        CommandProcessingResult result = ok(fineract().externalEventConfigurationApi.updateExternalEventConfigurationsDetails(
                new PutExternalEventConfigurationsRequest().putExternalEventConfigurationsItem(eventName, enabled)));
        Map<String, Object> changes = result.getChanges();
        Assertions.assertNotNull(changes);
        Assertions.assertInstanceOf(Map.class, changes);
        Map<String, Boolean> updatedConfigurations = (Map<String, Boolean>) changes.get("externalEventConfigurations");
        Assertions.assertNotNull(updatedConfigurations);
        Assertions.assertEquals(1, updatedConfigurations.size());
        Assertions.assertTrue(updatedConfigurations.containsKey(eventName));
        Assertions.assertEquals(enabled, updatedConfigurations.get(eventName));
    }

    public void enableBusinessEvent(String eventName) {
        configureBusinessEvent(eventName, true);
    }

    public void disableBusinessEvent(String eventName) {
        configureBusinessEvent(eventName, false);
    }

}
