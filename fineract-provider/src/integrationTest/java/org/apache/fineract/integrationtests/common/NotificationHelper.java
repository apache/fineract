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

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class NotificationHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    private static final String NOTIFICATION_API_URL = "/fineract-provider/api/v1/notifications?" + Utils.TENANT_IDENTIFIER;

    public NotificationHelper(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public static Object getNotifications(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
                                          final String jsonReturn) {
        final String GET_NOTIFICATIONS_URL = NOTIFICATION_API_URL;
        System.out.println("-----------------------------GET NOTIFICATIONS-----------------------------------");
        return Utils.performServerGet(requestSpec, responseSpec, GET_NOTIFICATIONS_URL, "");
    }
}
