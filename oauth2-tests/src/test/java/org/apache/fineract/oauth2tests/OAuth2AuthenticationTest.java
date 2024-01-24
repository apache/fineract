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
package org.apache.fineract.oauth2tests;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import jakarta.mail.MessagingException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OAuth2AuthenticationTest {

    private ResponseSpecification responseSpec;
    private ResponseSpecification responseSpec401;
    private RequestSpecification requestSpec;
    private RequestSpecification requestFormSpec;

    public static final String TENANT_PARAM_NAME = "tenantIdentifier";
    public static final String DEFAULT_TENANT = "default";
    public static final String TENANT_IDENTIFIER = TENANT_PARAM_NAME + '=' + DEFAULT_TENANT;
    private static final String HEALTH_URL = "/fineract-provider/actuator/health";

    @BeforeEach
    public void setup() throws InterruptedException {
        initializeRestAssured();

        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();

        // Login with basic authentication
        awaitSpringBootActuatorHealthyUp();

        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestFormSpec = new RequestSpecBuilder().setContentType(ContentType.URLENC).build();
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpec401 = new ResponseSpecBuilder().expectStatusCode(401).build();
    }

    @Test
    public void testActuatorAccess() {
        performServerGet(requestSpec, responseSpec, "/fineract-provider/actuator/info", null);
    }

    @Test
    public void testApiDocsAccess() {
        performServerGet(requestSpec, responseSpec, "/fineract-provider/legacy-docs/apiLive.htm", null);
    }

    @Test
    public void testAccessWithoutAuthentication() {
        performServerGet(requestSpec, responseSpec401, "/fineract-provider/api/v1/offices/1?" + TENANT_IDENTIFIER, "");
    }

    @Test
    public void testOAuth2Login() throws IOException, MessagingException {

        performServerGet(requestSpec, responseSpec401, "/fineract-provider/api/v1/offices/1?" + TENANT_IDENTIFIER, "");

        String accessToken = performServerPost(requestFormSpec, responseSpec, "http://localhost:9000/auth/realms/fineract/token",
                "grant_type=client_credentials&client_id=community-app&client_secret=123123", "access_token");
        assertNotNull(accessToken);

        String bearerToken = performServerPost(requestFormSpec, responseSpec, "http://localhost:9000/auth/realms/fineract/token",
                "grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=" + accessToken
                        + "&client_id=community-app&scope=fineract",
                "access_token");
        assertNotNull(bearerToken);

        RequestSpecification requestSpecWithToken = new RequestSpecBuilder() //
                .setContentType(ContentType.JSON) //
                .addHeader("Authorization", "Bearer " + bearerToken) //
                .build();

        performServerGet(requestSpecWithToken, responseSpec, "/fineract-provider/api/v1/offices/1?" + TENANT_IDENTIFIER, "");
    }

    @Test
    public void testGetOAuth2UserDetails() {
        performServerGet(requestSpec, responseSpec401, "/fineract-provider/api/v1/offices/1?" + TENANT_IDENTIFIER, "");

        String accessToken = performServerPost(requestFormSpec, responseSpec, "http://localhost:9000/auth/realms/fineract/token",
                "grant_type=client_credentials&client_id=community-app&client_secret=123123", "access_token");
        assertNotNull(accessToken);

        String bearerToken = performServerPost(requestFormSpec, responseSpec, "http://localhost:9000/auth/realms/fineract/token",
                "grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=" + accessToken
                        + "&client_id=community-app&scope=fineract",
                "access_token");
        assertNotNull(bearerToken);

        RequestSpecification requestSpecWithToken = new RequestSpecBuilder() //
                .setContentType(ContentType.JSON) //
                .addHeader("Authorization", "Bearer " + bearerToken) //
                .build();

        Boolean authenticationCheck = performServerGet(requestSpecWithToken, responseSpec,
                "/fineract-provider/api/v1/userdetails?" + TENANT_IDENTIFIER, "authenticated");
        assertEquals(authenticationCheck, true);
    }

    private static void initializeRestAssured() {
        RestAssured.baseURI = "https://localhost";
        RestAssured.port = 8443;
        RestAssured.keyStore("src/main/resources/keystore.jks", "openmf");
        RestAssured.useRelaxedHTTPSValidation();
    }

    private static void awaitSpringBootActuatorHealthyUp() throws InterruptedException {
        int attempt = 0;
        final int max_attempts = 10;
        Response response = null;

        do {
            try {
                response = RestAssured.get(HEALTH_URL);

                if (response.statusCode() == 200) {
                    return;
                }

                Thread.sleep(3000);
            } catch (Exception e) {
                Thread.sleep(3000);
            }
        } while (attempt < max_attempts);

        fail(HEALTH_URL + " returned " + response.prettyPrint());
    }

    @SuppressWarnings("unchecked")
    private static <T> T performServerGet(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String getURL, final String jsonAttributeToGetBack) {
        final String json = given().spec(requestSpec).expect().spec(responseSpec).log().ifError().when().get(getURL).andReturn().asString();
        if (jsonAttributeToGetBack == null) {
            return (T) json;
        }
        return (T) JsonPath.from(json).get(jsonAttributeToGetBack);
    }

    @SuppressWarnings("unchecked")
    public static <T> T performServerPost(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String putURL, final String formBody, final String jsonAttributeToGetBack) {
        final String json = given().spec(requestSpec).body(formBody).expect().spec(responseSpec).log().ifError().when().post(putURL)
                .andReturn().asString();
        return (T) JsonPath.from(json).get(jsonAttributeToGetBack);
    }
}
