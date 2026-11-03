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
package org.apache.fineract.twofactortests;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import feign.FeignException;
import feign.RequestLine;
import feign.Response;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ApiResponse;
import org.apache.fineract.client.models.GetTwoFactorConfigurationResponse;
import org.apache.fineract.client.models.GetTwoFactorDeliveryMethodsResponse;
import org.apache.fineract.client.models.PostAuthenticationRequest;
import org.apache.fineract.client.models.PostAuthenticationResponse;
import org.apache.fineract.client.models.PostTwoFactorInvalidateRequest;
import org.apache.fineract.client.models.PostTwoFactorRequestTokenResponse;
import org.apache.fineract.client.models.PostTwoFactorValidateResponse;
import org.apache.fineract.client.models.PutTwoFactorConfigurationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class TwoFactorAuthenticationTest {

    private static final String SERVER_URL = "https://localhost:8443/fineract-provider";
    private static final String TFA_TOKEN_HEADER = "Fineract-Platform-TFA-Token";
    private static final ServerSetup SMTP_SETUP = ServerSetupTest.SMTP.createCopy("0.0.0.0");

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(SMTP_SETUP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("support@cloudmicrofinance.com", "support81"))
            .withPerMethodLifecycle(true);

    // Every call sets its own Authorization header: the Basic key the login returned, alone or with a two-factor token.
    private final FineractFeignClient fineractClient = FineractFeignClient.builder().baseUrl(SERVER_URL + "/api").withoutCredentials()
            .disableSslVerification(true).build();
    private final ServerPagesApi serverPagesApi = FineractFeignClient.builder().baseUrl(SERVER_URL).withoutCredentials()
            .disableSslVerification(true).build().create(ServerPagesApi.class);
    private Map<String, String> basicAuthentication;

    @BeforeEach
    public void setup() throws InterruptedException {
        awaitSpringBootActuatorHealthyUp();
        String basicAuthenticationKey = login().getBase64EncodedAuthenticationKey();
        assertFalse(StringUtils.isBlank(basicAuthenticationKey));
        this.basicAuthentication = Map.of("Authorization", "Basic " + basicAuthenticationKey);
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
    public void testAccessWithoutTwofactor() {
        assertThrows(FeignException.Forbidden.class, () -> fineractClient.offices().retrieveOneOffice(1L, basicAuthentication));
    }

    @Test
    public void testCheckTwofactorEnabled() {
        assertTrue(login().getIsTwoFactorAuthenticationRequired());
    }

    @Test
    public void testGetTwofactorMethods() {
        List<GetTwoFactorDeliveryMethodsResponse> twoFactorMethods = expect200(
                () -> fineractClient.twoFactor().getOTPDeliveryMethodsWithHttpInfo(basicAuthentication));
        assertEquals("email", twoFactorMethods.get(0).getName());
        assertEquals("demomfi@mifos.org", twoFactorMethods.get(0).getTarget());
    }

    @Test
    public void testTwofactorLogin() throws IOException, MessagingException {
        assertEquals(0, greenMail.getReceivedMessages().length);
        PostTwoFactorRequestTokenResponse tokenRequest = requestEmailToken();
        assertEquals(1, greenMail.getReceivedMessages().length);
        assertEquals("email", tokenRequest.getDeliveryMethod().getName());
        assertEquals("demomfi@mifos.org", tokenRequest.getDeliveryMethod().getTarget());
        assertFalse(tokenRequest.getExtendedAccessToken());
        assertNotNull(tokenRequest.getRequestTime());

        String token = readToken(0);
        assertNotNull(token);
        PostTwoFactorValidateResponse accessToken = validateWithTimes(token);
        String tfaToken = accessToken.getToken();
        assertNotNull(tfaToken);
        assertTrue(accessToken.getValidTo() > accessToken.getValidFrom());

        Map<String, String> twoFactorAuthentication = withTwoFactorToken(tfaToken);

        expect200(() -> fineractClient.offices().retrieveOneOfficeWithHttpInfo(1L, twoFactorAuthentication));
        assertEquals(retrieveConfiguration(twoFactorAuthentication).getOtpTokenLiveTime(), tokenRequest.getTokenLiveTimeInSec());

        assertEquals(tfaToken, invalidate(tfaToken, twoFactorAuthentication));

        assertThrows(FeignException.Unauthorized.class, () -> fineractClient.offices().retrieveOneOffice(1L, twoFactorAuthentication));
    }

    @Test
    public void testTfaConfigSettings() throws IOException, MessagingException {
        assertEquals(0, greenMail.getReceivedMessages().length);
        requestEmailToken();
        assertEquals(1, greenMail.getReceivedMessages().length);

        String token = readToken(0);
        assertNotNull(token);
        String tfaToken = validate(token);
        assertNotNull(tfaToken);

        Map<String, String> twoFactorAuthentication = withTwoFactorToken(tfaToken);

        assertEquals(token.length(), retrieveConfiguration(twoFactorAuthentication).getOtpTokenLength());

        try {
            assertEquals(10, updateOtpTokenLength(10, twoFactorAuthentication));

            requestEmailToken();
            assertEquals(2, greenMail.getReceivedMessages().length);

            String token2 = readToken(1);
            assertNotNull(token2);
            assertEquals(10, token2.length());

            tfaToken = validate(token2);
            assertNotNull(tfaToken);
            twoFactorAuthentication = withTwoFactorToken(tfaToken);

            assertEquals(token2.length(), retrieveConfiguration(twoFactorAuthentication).getOtpTokenLength());
        } finally {
            updateOtpTokenLength(token.length(), twoFactorAuthentication);

            assertEquals(token.length(), retrieveConfiguration(twoFactorAuthentication).getOtpTokenLength());

            invalidate(tfaToken, twoFactorAuthentication);
        }
    }

    private PostAuthenticationResponse login() {
        return ok(() -> fineractClient.authenticationHttpBasic()
                .authenticate(new PostAuthenticationRequest().username("mifos").password("password")));
    }

    private PostTwoFactorRequestTokenResponse requestEmailToken() {
        return expect200(() -> fineractClient.twoFactor().requestTokenWithHttpInfo("email", false, basicAuthentication));
    }

    private String validate(String token) {
        return validateWithTimes(token).getToken();
    }

    private PostTwoFactorValidateResponse validateWithTimes(String token) {
        return expect200(() -> fineractClient.twoFactor().validateWithHttpInfo(token, basicAuthentication));
    }

    private String invalidate(String tfaToken, Map<String, String> twoFactorAuthentication) {
        return expect200(() -> fineractClient.twoFactor()
                .updateConfigurationWithHttpInfo(new PostTwoFactorInvalidateRequest().token(tfaToken), twoFactorAuthentication))
                .getResourceIdentifier();
    }

    private GetTwoFactorConfigurationResponse retrieveConfiguration(Map<String, String> twoFactorAuthentication) {
        return expect200(() -> fineractClient.defaultApi().retrieveAll4WithHttpInfo(twoFactorAuthentication));
    }

    private Integer updateOtpTokenLength(int otpTokenLength, Map<String, String> twoFactorAuthentication) {
        return expect200(() -> fineractClient.defaultApi().updateConfiguration1WithHttpInfo(
                new PutTwoFactorConfigurationRequest().otpTokenLength(otpTokenLength), twoFactorAuthentication)).getChanges()
                .getOtpTokenLength();
    }

    private Map<String, String> withTwoFactorToken(String tfaToken) {
        return Map.of("Authorization", basicAuthentication.get("Authorization"), TFA_TOKEN_HEADER, tfaToken);
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

    /**
     * The actuator and the legacy API docs are served outside {@code /api} and are not part of the OpenAPI spec, so the
     * generated client has no operations for them.
     */
    interface ServerPagesApi {

        @RequestLine("GET /actuator/health")
        Response actuatorHealth();

        @RequestLine("GET /actuator/info")
        Response actuatorInfo();

        @RequestLine("GET /legacy-docs/apiLive.htm")
        Response legacyApiDocs();
    }
}
