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
package org.apache.fineract.oauth2twofactortests;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Splitter;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import feign.Body;
import feign.FeignException;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ApiResponse;
import org.apache.fineract.client.models.GetTwoFactorDeliveryMethodsResponse;
import org.apache.fineract.client.models.PostTwoFactorRequestTokenResponse;
import org.apache.fineract.client.models.PostTwoFactorValidateResponse;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Runs against a server with OAuth2 and two-factor authentication enabled: an OAuth2 access token alone reaches only
 * the two-factor endpoints, and the rest of the API needs the two-factor access token as well.
 */
public class OAuth2TwoFactorAuthenticationTest {

    private static final String SERVER_URL = "https://localhost:8443/fineract-provider";
    private static final String REDIRECT_URI = "http://localhost:3000/callback";
    private static final String TFA_TOKEN_HEADER = "Fineract-Platform-TFA-Token";
    private static final String AUTHORIZE_URL = SERVER_URL + "/oauth2/authorize?response_type=code&client_id=frontend-client"
            + "&redirect_uri=http%3A%2F%2Flocalhost%3A3000%2Fcallback&scope=read&state=xyz"
            + "&code_challenge=zudG_xkz8WrPPMq2MwmFP-NRvNapCL0OD-xYWRapTsU&code_challenge_method=S256";
    private static final ServerSetup SMTP_SETUP = ServerSetupTest.SMTP.createCopy("0.0.0.0");

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(SMTP_SETUP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("support@cloudmicrofinance.com", "support81"))
            .withPerMethodLifecycle(true);

    // The server only accepts OAuth2 bearer tokens, so the client sends no Basic credentials of its own.
    private final FineractFeignClient fineractClient = FineractFeignClient.builder().baseUrl(SERVER_URL + "/api").withoutCredentials()
            .disableSslVerification(true).build();
    private final ServerPagesApi serverPagesApi = FineractFeignClient.builder().baseUrl(SERVER_URL).withoutCredentials()
            .disableSslVerification(true).build().create(ServerPagesApi.class);
    private Map<String, String> bearer;

    @BeforeEach
    public void setup() throws InterruptedException, IOException {
        awaitSpringBootActuatorHealthyUp();
        bearer = Map.of("Authorization", "Bearer " + loginAndClaimToken(AUTHORIZE_URL));
    }

    @Test
    public void testAccessWithoutTwofactor() {
        assertThrows(FeignException.Forbidden.class, () -> fineractClient.offices().retrieveOneOffice(1L, bearer));
    }

    @Test
    public void testGetTwofactorMethodsWithoutTwofactor() {
        List<GetTwoFactorDeliveryMethodsResponse> twoFactorMethods = expect200(
                () -> fineractClient.twoFactor().getOTPDeliveryMethodsWithHttpInfo(bearer));
        assertEquals("email", twoFactorMethods.get(0).getName());
    }

    @Test
    public void testTwofactorLogin() throws IOException, MessagingException {
        PostTwoFactorRequestTokenResponse tokenRequest = expect200(
                () -> fineractClient.twoFactor().requestTokenWithHttpInfo("email", false, bearer));
        assertEquals(1, greenMail.getReceivedMessages().length);
        assertFalse(tokenRequest.getExtendedAccessToken());

        String token = readToken(0);
        assertNotNull(token);
        PostTwoFactorValidateResponse accessToken = expect200(() -> fineractClient.twoFactor().validateWithHttpInfo(token, bearer));
        assertNotNull(accessToken.getToken());

        Map<String, String> twoFactorAuthentication = Map.of("Authorization", bearer.get("Authorization"), TFA_TOKEN_HEADER,
                accessToken.getToken());
        expect200(() -> fineractClient.offices().retrieveOneOfficeWithHttpInfo(1L, twoFactorAuthentication));
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

    private String loginAndClaimToken(String url) throws IOException {
        CompletableFuture<String> futureToken = new CompletableFuture<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);
        server.createContext("/callback", exchange -> {
            try {
                futureToken.complete(claimTokenOnCallback(exchange));
                exchange.sendResponseHeaders(200, 0);
            } catch (Exception e) {
                futureToken.completeExceptionally(e);
            } finally {
                exchange.close();
            }
        });
        server.start();
        WebDriver driver = null;
        try {
            // created inside the try, so the callback server is stopped even when the browser does not start
            driver = getWebDriver();
            driver.get(url);
            driver.findElement(By.name("username")).sendKeys("mifos");
            driver.findElement(By.name("password")).sendKeys("password");
            driver.findElement(By.name("tenantId")).sendKeys("default");
            driver.findElement(By.cssSelector("button[type='submit'], input[type='submit']")).click();
            return futureToken.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
            server.stop(0);
        }
    }

    @NonNull
    private static WebDriver getWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
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
                if (keyValue.size() == 2 && "code".equals(keyValue.getFirst())) {
                    code = URLDecoder.decode(keyValue.getLast(), StandardCharsets.UTF_8);
                }
            }
        }
        String authorizationCode = code;
        ApiResponse<TokenResponse> response = ok(() -> serverPagesApi.token(authorizationCode, REDIRECT_URI, "frontend-client",
                "gyQBFpozcvcosvPt7m9Q1A4SqSf1yJtPIERruioHLjQ"));
        assertEquals(200, response.getStatusCode());
        return response.getData().accessToken();
    }

    private static String readToken(int messageIndex) throws IOException, MessagingException {
        Matcher m = Pattern.compile("token is (.+).").matcher((CharSequence) greenMail.getReceivedMessages()[messageIndex].getContent());
        String token = null;
        while (m.find()) {
            token = m.group(1);
        }
        return token;
    }

    private static <T> T expect200(Supplier<ApiResponse<T>> call) {
        ApiResponse<T> response = ok(call);
        assertEquals(200, response.getStatusCode());
        return response.getData();
    }

    /**
     * The actuator and the OAuth2 token endpoint are served outside {@code /api} and are not part of the generated
     * client.
     */
    interface ServerPagesApi {

        @RequestLine("GET /actuator/health")
        Response actuatorHealth();

        @RequestLine("POST /oauth2/token")
        @Headers("Content-Type: application/x-www-form-urlencoded")
        @Body("grant_type=authorization_code&code={code}&redirect_uri={redirectUri}&client_id={clientId}&code_verifier={codeVerifier}")
        ApiResponse<TokenResponse> token(@Param("code") String code, @Param("redirectUri") String redirectUri,
                @Param("clientId") String clientId, @Param("codeVerifier") String codeVerifier);
    }

    record TokenResponse(@JsonProperty("access_token") String accessToken) {
    }
}
