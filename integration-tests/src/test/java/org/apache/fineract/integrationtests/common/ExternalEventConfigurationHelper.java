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

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.Map;

public class ExternalEventConfigurationHelper {

    private static final String EXTERNAL_EVENT_CONFIGURATION = "externalEventConfiguration";
    private static final String EXTERNAL_EVENT_CONFIGURATION_RESPONSE = "externalEventConfigurations";

    protected ExternalEventConfigurationHelper() {}

    private static final String EXTERNAL_EVENT_CONFIGURATION_URL = "/fineract-provider/api/v1/externalevents/configuration?"
            + Utils.TENANT_IDENTIFIER;

    // TODO: Rewrite to use fineract-client instead!
    // Example:
    // org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static ArrayList<Map<String, Object>> getAllExternalEventConfigurations(RequestSpecification requestSpec,
            ResponseSpecification responseSpec) {
        Map<String, ArrayList<Map<String, Object>>> response = Utils.performServerGet(requestSpec, responseSpec,
                EXTERNAL_EVENT_CONFIGURATION_URL, "");
        return response.get(EXTERNAL_EVENT_CONFIGURATION);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example:
    // org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Map<String, Boolean> updateExternalEventConfigurations(RequestSpecification requestSpec,
            ResponseSpecification responseSpec, String json) {
        Map<String, Map<String, Boolean>> response = Utils.performServerPut(requestSpec, responseSpec, EXTERNAL_EVENT_CONFIGURATION_URL,
                json, "changes");
        return response.get(EXTERNAL_EVENT_CONFIGURATION_RESPONSE);
    }
}
