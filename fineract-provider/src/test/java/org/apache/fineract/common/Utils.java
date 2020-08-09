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
package org.apache.fineract.common;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util for RestAssured tests. This class here in src/test is copy/pasted :( from and identical to the one in
 * src/integrationTest; please keep it in sync. The cunning plan is that, now that we have Spring Boot + MariaDB4j,
 * eventually do completely away with src/integrationTest and have only src/test.. can you help? ;)
 */
@SuppressWarnings("unchecked")
public final class Utils {

    private Utils() {

    }

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    public static final String TENANT_IDENTIFIER = "tenantIdentifier=default";

    private static final String LOGIN_URL = "/fineract-provider/api/v1/authentication?username=mifos&password=password&"
            + TENANT_IDENTIFIER;

    public static void initializeRESTAssured() {
        RestAssured.baseURI = "https://localhost";
        RestAssured.port = 8443;
        RestAssured.keyStore("src/main/resources/keystore.jks", "openmf");
        RestAssured.useRelaxedHTTPSValidation();
    }

    public static void initializeRESTAssuredPort(int http) {
        RestAssured.port = http;
    }

    public static String loginIntoServerAndGetBase64EncodedAuthenticationKey() {
        try {
            LOG.info("-----------------------------------LOGIN-----------------------------------------");
            final String json = RestAssured.post(LOGIN_URL).asString();
            assertThat("Failed to login into fineract platform", StringUtils.isBlank(json), is(false));
            return JsonPath.with(json).get("base64EncodedAuthenticationKey");
        } catch (final Exception e) {
            if (e instanceof HttpHostConnectException) {
                final HttpHostConnectException hh = (HttpHostConnectException) e;
                fail("Failed to connect to fineract platform:" + hh.getMessage());
            }

            throw new RuntimeException(e);
        }
    }

    public static <T> T performServerGet(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String getURL, final String jsonAttributeToGetBack) {
        final String json = given().spec(requestSpec).expect().spec(responseSpec).log().ifError().when().get(getURL).andReturn().asString();
        return (T) JsonPath.from(json).get(jsonAttributeToGetBack);
    }

}
