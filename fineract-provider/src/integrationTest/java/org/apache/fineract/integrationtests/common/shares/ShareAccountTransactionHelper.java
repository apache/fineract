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
package org.apache.fineract.integrationtests.common.shares;

import java.util.Map;

import org.apache.fineract.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class ShareAccountTransactionHelper {

    private static final String SHARE_ACCOUNT_URL = "/fineract-provider/api/v1/accounts/share";
    private static final String CREATE_SHARE_ACCOUNT_URL = SHARE_ACCOUNT_URL + "?" + Utils.TENANT_IDENTIFIER;

    public static Integer createShareAccount(final String shareProductJSON, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_SHARE_ACCOUNT_URL, shareProductJSON, "resourceId");
    }

    public static Map<String, Object> retrieveShareAccount(final Integer shareProductId, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        String url = SHARE_ACCOUNT_URL + "/" + shareProductId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, url, "");
    }

    public static Integer updateShareAccount(final Integer shareAccountId, final String shareAccountJson,
            final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        String url = SHARE_ACCOUNT_URL + "/" + shareAccountId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPut(requestSpec, responseSpec, url, shareAccountJson, "resourceId");
    }

    public static Integer postCommand(final String command, final Integer shareAccountId, String jsonBody, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        String url = SHARE_ACCOUNT_URL + "/" + shareAccountId + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, url, jsonBody, "resourceId");
    }
}
