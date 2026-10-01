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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import feign.FeignException;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ChangePwdUsersUserIdRequest;
import org.apache.fineract.client.models.ChangePwdUsersUserIdResponse;
import org.apache.fineract.client.models.PostAuthenticationRequest;
import org.apache.fineract.client.models.PostAuthenticationResponse;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.client.models.PostUsersResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGlobalConfigurationHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PasswordResetIntegrationTest extends FeignIntegrationTest {

    private FeignGlobalConfigurationHelper globalConfigurationHelper;
    private final List<Long> transientUsers = new ArrayList<>();

    @BeforeAll
    public void setup() {
        this.globalConfigurationHelper = new FeignGlobalConfigurationHelper(fineractClient());
    }

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.FORCE_PASSWORD_RESET_ON_FIRST_LOGIN,
                new PutGlobalConfigurationsRequest().value(0L).enabled(false));

        for (Long userId : this.transientUsers) {
            FeignUserHelper.deleteUser(userId);
        }
        this.transientUsers.clear();
    }

    @Test
    public void testPasswordResetEnforcement() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.FORCE_PASSWORD_RESET_ON_FIRST_LOGIN,
                new PutGlobalConfigurationsRequest().value(0L).enabled(true));

        String password = "Abcdef1#2$3%XYZ";
        PostUsersRequest userRequest = FeignUserHelper.buildUserRequest(password);
        PostUsersResponse userResponse = FeignUserHelper.createUser(userRequest);
        Long userId = userResponse.getResourceId();
        assertNotNull(userId, "User creation failed to return an ID!");
        this.transientUsers.add(userId);
        String username = userRequest.getUsername();

        assertEquals(403, assertThrows(FeignException.Forbidden.class, () -> attemptLogin(username, password)).status(),
                "User should be forced to change password");

        String newPassword = "Abcdef1#2$3%XYZ_NEW";
        assertNotNull(ok(() -> changePassword(username, password, userId, newPassword)).getResourceId(), "Password change should succeed");

        assertNotNull(ok(() -> attemptLogin(username, newPassword)), "User should be able to login after reset");
    }

    @Test
    public void testFeatureDisabledByDefault() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.FORCE_PASSWORD_RESET_ON_FIRST_LOGIN,
                new PutGlobalConfigurationsRequest().value(0L).enabled(false));

        String password = "Abcdef1#2$3%XYZ";
        PostUsersRequest userRequest = FeignUserHelper.buildUserRequest(password);
        PostUsersResponse userResponse = FeignUserHelper.createUser(userRequest);
        assertNotNull(userResponse.getResourceId(), "User creation failed!");
        this.transientUsers.add(userResponse.getResourceId());
        String username = userRequest.getUsername();

        assertNotNull(ok(() -> attemptLogin(username, password)), "User should login normally when feature is disabled");
    }

    private PostAuthenticationResponse attemptLogin(String username, String password) {
        return fineractClient().authenticationHttpBasic()
                .authenticate(new PostAuthenticationRequest().username(username).password(password));
    }

    private static ChangePwdUsersUserIdResponse changePassword(String username, String password, Long userId, String newPassword) {
        FineractFeignClient userClient = FineractFeignClientHelper.createNewFineractFeignClient(username, password);
        return userClient.users().changePasswordUser(userId,
                new ChangePwdUsersUserIdRequest().password(newPassword).repeatPassword(newPassword));
    }
}
