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
import static org.junit.jupiter.api.Assertions.fail;

import com.google.common.base.Splitter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.lang.NonNull;

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
        performServerGet(requestSpec, responseSpec401, "/fineract-provider/api/v1/offices/1?" + TENANT_IDENTIFIER, null);
    }

    @Test
    public void testGetOAuth2UserDetails() throws IOException, InterruptedException {
        performServerGet(requestSpec, responseSpec401, "/fineract-provider/api/v1/offices/1?" + TENANT_IDENTIFIER, null);

        String token = loginAndClaimToken(
                "https://localhost:8443/fineract-provider/oauth2/authorize" + "?response_type=code&client_id=frontend-client"
                        + "&redirect_uri=http%3A%2F%2Flocalhost%3A3000%2Fcallback&scope=read&state=xyz"
                        + "&code_challenge=zudG_xkz8WrPPMq2MwmFP-NRvNapCL0OD-xYWRapTsU" + "&code_challenge_method=S256",
                requestFormSpec);

        RequestSpecification requestSpecWithToken = new RequestSpecBuilder() //
                .setContentType(ContentType.JSON) //
                .addHeader("Authorization", "Bearer " + token) //
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
            attempt++;
        } while (attempt < max_attempts);

        fail(HEALTH_URL + " returned " + response.prettyPrint());
    }

    public String loginAndClaimToken(String url, RequestSpecification requestSpec) throws IOException {
        CompletableFuture<String> futureToken = new CompletableFuture<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);
        server.createContext("/callback", exchange -> {
            try {
                String token = claimTokenOnCallback(requestSpec, exchange);
                futureToken.complete(token); // complete future with value
                exchange.sendResponseHeaders(200, 0);
            } catch (Exception e) {
                futureToken.completeExceptionally(e); // propagate exception
            } finally {
                exchange.close();
            }
        });
        server.start();
        WebDriver driver = getWebDriver();
        try {
            driver.get(url);
            driver.findElement(By.name("username")).sendKeys("mifos");
            driver.findElement(By.name("password")).sendKeys("password");
            driver.findElement(By.name("tenantId")).sendKeys("default");
            driver.findElement(By.cssSelector("button[type='submit'], input[type='submit']")).click();
            return futureToken.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
            server.stop(0);
        }
    }

    @NonNull
    private static WebDriver getWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // run in headless mode
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--ignore-certificate-errors");
        return new ChromeDriver(options);
    }

    private String claimTokenOnCallback(RequestSpecification requestSpec, HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        String code = null;
        if (query != null) {
            for (String param : Splitter.on("&").split(query)) {
                List<String> keyValue = Splitter.on("=").splitToList(param);
                if (keyValue.size() == 2) {
                    String key = keyValue.getFirst();
                    if ("code".equals(key)) {
                        code = URLDecoder.decode(keyValue.getLast(), StandardCharsets.UTF_8);
                    }
                }
            }
        }
        Map<String, String> formParams = Map.of("grant_type", "authorization_code", "code", code, "redirect_uri",
                "http://localhost:3000/callback", "client_id", "frontend-client", "code_verifier",
                "gyQBFpozcvcosvPt7m9Q1A4SqSf1yJtPIERruioHLjQ");
        return performServerPost(requestSpec, responseSpec, "/fineract-provider/oauth2/token", formParams, "access_token");
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

    public static <T> T performServerPost(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String putURL, final Map<String, String> formBody, final String jsonAttributeToGetBack) {
        final String response = given().spec(requestSpec).header("Content-Type", "application/x-www-form-urlencoded").formParams(formBody)
                .expect().spec(responseSpec).log().ifError().when().post(putURL).andReturn().asString();
        return (T) JsonPath.from(response).get(jsonAttributeToGetBack);
    }
}
