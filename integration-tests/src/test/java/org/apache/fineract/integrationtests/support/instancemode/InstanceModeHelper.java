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
package org.apache.fineract.integrationtests.support.instancemode;

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.fineract.client.models.ChangeInstanceModeRequest;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.integrationtests.common.Utils;

@SuppressWarnings({ "HideUtilityClassConstructor" })
public class InstanceModeHelper {

    private static final Gson GSON = new JSON().getGson();

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static void changeMode(RequestSpecification requestSpec, ResponseSpecification responseSpec, boolean readEnabled,
            boolean writeEnabled, boolean batchWorkerEnabled, boolean batchManagerEnabled) {
        ChangeInstanceModeRequest request = new ChangeInstanceModeRequest().readEnabled(readEnabled).writeEnabled(writeEnabled)
                .batchWorkerEnabled(batchWorkerEnabled).batchManagerEnabled(batchManagerEnabled);
        String requestStr = GSON.toJson(request);
        Utils.performServerPut(requestSpec, responseSpec, "/fineract-provider/api/v1/instance-mode?" + Utils.TENANT_IDENTIFIER, requestStr);
    }
}
