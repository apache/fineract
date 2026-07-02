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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetNotificationsResponse;

public class FeignNotificationHelper {

    private final FineractFeignClient fineractClient;

    public FeignNotificationHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public GetNotificationsResponse getNotifications() {
        return ok(() -> fineractClient.notification().getAllNotifications(null, null, null, null, true));
    }

    public GetNotificationsResponse getNotifications(Boolean isRead) {
        return ok(() -> fineractClient.notification().getAllNotifications(null, null, null, null, isRead));
    }

    public GetNotificationsResponse getUnreadNotifications() {
        return ok(() -> fineractClient.notification().getAllNotifications(null, null, null, null, false));
    }

    public void markNotificationsAsRead() {
        fineractClient.notification().updateNotificationReadStatus();
    }

    public boolean areNotificationsAvailable() {
        return !getNotifications().getPageItems().isEmpty();
    }

    public void waitUntilNotificationsAreAvailable() {
        await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(5)).pollDelay(Duration.ofSeconds(5))
                .until(this::areNotificationsAvailable);
    }
}
