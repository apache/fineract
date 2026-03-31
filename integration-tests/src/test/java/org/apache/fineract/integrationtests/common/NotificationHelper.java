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
package org.apache.fineract.integrationtests.common;

import static org.awaitility.Awaitility.await;

import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetNotificationsResponse;
import org.apache.fineract.client.util.Calls;

@Slf4j
public final class NotificationHelper {

    private NotificationHelper() {}

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static GetNotificationsResponse getNotifications(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        log.info("-----------------------------GET NOTIFICATIONS-----------------------------------");

        FilterableRequestSpecification filterableRequestSpec = (FilterableRequestSpecification) requestSpec;

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", filterableRequestSpec.getHeaders().getValue("Authorization"));
        headers.put("Fineract-Platform-TenantId", filterableRequestSpec.getHeaders().getValue("Fineract-Platform-TenantId"));

        return Calls.ok(FineractClientHelper.getFineractClient().notifications.getAllNotifications(null, 1, 0, null, null, headers));
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static boolean areNotificationsAvailable(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return getNotifications(requestSpec, responseSpec).getPageItems().size() > 0;
    }

    // Waiting for notifications to be available is needed due to the asynchronous event processing
    public static void waitUntilNotificationsAreAvailable(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        await().atMost(Duration.ofSeconds(30)) //
                .pollInterval(Duration.ofSeconds(5)) //
                .pollDelay(Duration.ofSeconds(5)) //
                .until(() -> NotificationHelper.areNotificationsAvailable(requestSpec, responseSpec));
    }
}
