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

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Splitter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import feign.Body;
import feign.FeignException;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ApiResponse;
import org.apache.fineract.client.models.GetUserDetailsResponse;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class OAuth2AuthenticationTest {

    private static final String SERVER_URL = "https://localhost:8443/fineract-provider";
    private static final String REDIRECT_URI = "http://localhost:3000/callback";

    // The server only accepts OAuth2 bearer tokens, so the client sends no Basic credentials of its own.
    private final FineractFeignClient fineractClient = FineractFeignClient.builder().baseUrl(SERVER_URL + "/api").withoutCredentials()
            .disableSslVerification(true).build();
    private final ServerPagesApi serverPagesApi = FineractFeignClient.builder().baseUrl(SERVER_URL).withoutCredentials()
            .disableSslVerification(true).build().create(ServerPagesApi.class);

    @BeforeEach
    public void setup() throws InterruptedException {
        awaitSpringBootActuatorHealthyUp();
    }

    @Test
    public void testActuatorAccess() {
        try (Response response = serverPagesApi.actuatorInfo()) {
            assertEquals(200, response.status());
        }
    }

    @Test
    public void testApiDocsAccess() {
        try (Response response = serverPagesApi.legacyApiDocs()) {
            assertEquals(200, response.status());
        }
    }

    @Test
    public void testAccessWithoutAuthentication() {
        assertThrows(FeignException.Unauthorized.class, () -> fineractClient.offices().retrieveOneOffice(1L));
    }

    @Test
    public void testGetOAuth2UserDetails() throws IOException, InterruptedException {
        assertThrows(FeignException.Unauthorized.class, () -> fineractClient.offices().retrieveOneOffice(1L));

        String token = loginAndClaimToken(SERVER_URL + "/oauth2/authorize" + "?response_type=code&client_id=frontend-client"
                + "&redirect_uri=http%3A%2F%2Flocalhost%3A3000%2Fcallback&scope=read&state=xyz"
                + "&code_challenge=zudG_xkz8WrPPMq2MwmFP-NRvNapCL0OD-xYWRapTsU" + "&code_challenge_method=S256");

        ApiResponse<GetUserDetailsResponse> response = ok(
                () -> fineractClient.fetchAuthenticatedUserDetails().fetchAuthenticatedUserDataWithHttpInfo(bearer(token)));
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getData().getAuthenticated());
    }

    private void awaitSpringBootActuatorHealthyUp() throws InterruptedException {
        final int maxAttempts = 10;
        Integer lastStatus = null;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try (Response response = serverPagesApi.actuatorHealth()) {
                if (response.status() == 200) {
                    return;
                }
                lastStatus = response.status();
            } catch (RuntimeException e) {
                // the server is not accepting connections yet
            }
            Thread.sleep(3000);
        }

        fail("/actuator/health returned " + (lastStatus == null ? "no response" : lastStatus));
    }

    public String loginAndClaimToken(String url) throws IOException {
        CompletableFuture<String> futureToken = new CompletableFuture<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);
        server.createContext("/callback", exchange -> {
            try {
                String token = claimTokenOnCallback(exchange);
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

    private String claimTokenOnCallback(HttpExchange exchange) {
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
        String authorizationCode = code;
        ApiResponse<TokenResponse> response = ok(() -> serverPagesApi.token(authorizationCode, REDIRECT_URI, "frontend-client",
                "gyQBFpozcvcosvPt7m9Q1A4SqSf1yJtPIERruioHLjQ"));
        assertEquals(200, response.getStatusCode());
        return response.getData().accessToken();
    }

    private static Map<String, String> bearer(String token) {
        return Map.of("Authorization", "Bearer " + token);
    }

    /**
     * The actuator, the legacy API docs and the OAuth2 token endpoint are served outside {@code /api} and are not part
     * of the OpenAPI spec, so the generated client has no operations for them.
     */
    interface ServerPagesApi {

        @RequestLine("GET /actuator/health")
        Response actuatorHealth();

        @RequestLine("GET /actuator/info")
        Response actuatorInfo();

        @RequestLine("GET /legacy-docs/apiLive.htm")
        Response legacyApiDocs();

        @RequestLine("POST /oauth2/token")
        @Headers("Content-Type: application/x-www-form-urlencoded")
        @Body("grant_type=authorization_code&code={code}&redirect_uri={redirectUri}&client_id={clientId}&code_verifier={codeVerifier}")
        ApiResponse<TokenResponse> token(@Param("code") String code, @Param("redirectUri") String redirectUri,
                @Param("clientId") String clientId, @Param("codeVerifier") String codeVerifier);
    }

    record TokenResponse(@JsonProperty("access_token") String accessToken) {
    }
}
