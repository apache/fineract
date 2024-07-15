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
package org.apache.fineract.integrationtests;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.BUSINESS_DATE;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.internal.RequestSpecificationImpl;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.fineract.client.models.BusinessDateRequest;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.useradministration.users.UserHelper;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public abstract class BaseIntegrationTest {

    protected static final String DATETIME_PATTERN = "dd MMMM yyyy";
    protected static final String MONTH_DATE_PATTERN = "dd MMMM";
    protected static final String LOCALE = "en_GB";

    static {
        Utils.initializeRESTAssured();
    }

    private final String fullAdminAuthKey = getFullAdminAuthKey();

    protected BusinessDateHelper businessDateHelper = new BusinessDateHelper();
    protected final ResponseSpecification responseSpec = createResponseSpecification(Matchers.is(200));
    protected final RequestSpecification requestSpec = createRequestSpecification(fullAdminAuthKey);

    private final String nonByPassUserAuthKey = getNonByPassUserAuthKey(requestSpec, responseSpec);

    protected void runAt(String date, Runnable runnable) {
        try {
            GlobalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(requestSpec, responseSpec, 42, true);
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, TRUE);
            businessDateHelper.updateBusinessDate(
                    new BusinessDateRequest().type(BUSINESS_DATE.getName()).date(date).dateFormat(DATETIME_PATTERN).locale("en"));
            runnable.run();
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, FALSE);
            GlobalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(requestSpec, responseSpec, 42, false);
        }
    }

    protected void runAsNonByPass(Runnable runnable) {
        RequestSpecificationImpl requestSpecImpl = (RequestSpecificationImpl) requestSpec;
        try {
            requestSpecImpl.replaceHeader("Authorization", "Basic " + nonByPassUserAuthKey);
            runnable.run();
        } finally {
            requestSpecImpl.replaceHeader("Authorization", "Basic " + fullAdminAuthKey);
        }
    }

    protected RequestSpecification createRequestSpecification(String authKey) {
        RequestSpecification requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + authKey);
        requestSpec.header("Fineract-Platform-TenantId", "default");
        return requestSpec;
    }

    protected ResponseSpecification createResponseSpecification(Matcher<Integer> statusCodeMatcher) {
        return new ResponseSpecBuilder().expectStatusCode(statusCodeMatcher).build();
    }

    protected String getFullAdminAuthKey() {
        return Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey();
    }

    protected String getNonByPassUserAuthKey(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        // creates the user
        UserHelper.getSimpleUserWithoutBypassPermission(requestSpec, responseSpec);
        return Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey(UserHelper.SIMPLE_USER_NAME, UserHelper.SIMPLE_USER_PASSWORD);
    }

}
