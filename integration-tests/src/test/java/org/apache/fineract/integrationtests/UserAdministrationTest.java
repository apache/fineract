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

import feign.FeignException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ChangePwdUsersUserIdRequest;
import org.apache.fineract.client.models.ChangePwdUsersUserIdResponse;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.GetUsersUserIdResponse;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.client.models.PostUsersResponse;
import org.apache.fineract.client.models.PutUsersUserIdRequest;
import org.apache.fineract.client.models.PutUsersUserIdResponse;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors.ReportedError;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.useradministration.service.AppUserConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAdministrationTest extends FeignIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(UserAdministrationTest.class);
    private final List<Long> transientUsers = new ArrayList<>();
    private FeignStaffHelper staffHelper;

    @BeforeAll
    public void setup() {
        staffHelper = new FeignStaffHelper(fineractClient());
    }

    @AfterEach
    public void tearDown() {
        for (Long userId : this.transientUsers) {
            FeignUserHelper.deleteUser(userId);
        }
        this.transientUsers.clear();
    }

    @Test
    public void testCreateNewUserBlocksDuplicateUsername() {

        final Long roleId = FeignRoleHelper.createRole();
        Assertions.assertNotNull(roleId);

        final Long staffId = staffHelper.createStaff().getResourceId();
        Assertions.assertNotNull(staffId);

        final Long userId = FeignUserHelper.createUser(roleId, staffId, "alphabet").getResourceId();
        Assertions.assertNotNull(userId);
        this.transientUsers.add(userId);

        final ReportedError reason = FeignErrors.firstError(FeignUserHelper.createUserExpectingError(roleId, staffId, "alphabet"));
        LOG.info("Reason: {}", reason.defaultUserMessage());
        LOG.info("Code: {}", reason.userMessageGlobalisationCode());
        Assertions.assertEquals("User with username alphabet already exists.", reason.defaultUserMessage());
        Assertions.assertEquals("error.msg.user.duplicate.username", reason.userMessageGlobalisationCode());
    }

    @Test
    public void testUpdateUserAcceptsNewOrSameUsername() {
        final Long roleId = FeignRoleHelper.createRole();
        Assertions.assertNotNull(roleId);

        final Long staffId = staffHelper.createStaff().getResourceId();
        Assertions.assertNotNull(staffId);

        final Long userId = FeignUserHelper.createUser(roleId, staffId, "alphabet").getResourceId();
        Assertions.assertNotNull(userId);
        this.transientUsers.add(userId);

        final Long userId2 = FeignUserHelper.updateUsername(userId, "renegade").getResourceId();
        Assertions.assertNotNull(userId2);

        final Long userId3 = FeignUserHelper.updateUsername(userId, "renegade").getResourceId();
        Assertions.assertNotNull(userId3);
    }

    @Test
    public void testUpdateUserBlockDuplicateUsername() {
        final Long roleId = FeignRoleHelper.createRole();
        Assertions.assertNotNull(roleId);

        final Long staffId = staffHelper.createStaff().getResourceId();
        Assertions.assertNotNull(staffId);

        final Long userId = FeignUserHelper.createUser(roleId, staffId, "alphabet").getResourceId();
        Assertions.assertNotNull(userId);
        this.transientUsers.add(userId);

        final Long userId2 = FeignUserHelper.createUser(roleId, staffId, "bilingual").getResourceId();
        Assertions.assertNotNull(userId2);
        this.transientUsers.add(userId2);

        final CallFailedRuntimeException exception = FeignUserHelper.updateUsernameExpectingError(userId2, "alphabet");
        Assertions.assertEquals(403, exception.getStatus());
        final ReportedError reason = FeignErrors.firstError(exception);
        Assertions.assertEquals("User with username alphabet already exists.", reason.defaultUserMessage());
        Assertions.assertEquals("error.msg.user.duplicate.username", reason.userMessageGlobalisationCode());
    }

    @Test
    public void testModifySystemUser() {
        final Long userId = FeignUserHelper.getUserIdByUsername(AppUserConstants.SYSTEM_USER_NAME);
        Assertions.assertNotNull(userId);

        Assertions.assertEquals(403, FeignUserHelper.updateUsernameExpectingError(userId, "systemtest").getStatus());
    }

    @Test
    public void testApplicationUserCanUpdateOwnPassword() {
        // Admin creates a new user with an empty role
        Long roleId = FeignRoleHelper.createRole();
        String originalPassword = "QwE!5rTy#9uP0";
        String simpleUsername = Utils.uniqueRandomStringGenerator("NotificationUser", 4);
        GetOfficesResponse headOffice = OfficeHelper.getHeadOffice();
        PostUsersRequest createUserRequest = new PostUsersRequest().username(simpleUsername).firstname(Utils.randomFirstNameGenerator())
                .lastname(Utils.randomLastNameGenerator()).email("whatever@mifos.org").password(originalPassword)
                .repeatPassword(originalPassword).sendPasswordToEmail(false).officeId(headOffice.getId()).roles(List.of(roleId));

        PostUsersResponse userCreationResponse = FeignUserHelper.createUser(createUserRequest);
        Long userId = userCreationResponse.getResourceId();
        Assertions.assertNotNull(userId);

        // User updates its own password
        String updatedPassword = "QwE!5rTy#9uP0u";
        PutUsersUserIdResponse putUsersUserIdResponse = ok(() -> clientFor(simpleUsername, originalPassword).users().updateUser(userId,
                new PutUsersUserIdRequest().password(updatedPassword).repeatPassword(updatedPassword)));
        Assertions.assertNotNull(putUsersUserIdResponse.getResourceId());

        // From then on the originalPassword is not working anymore
        FeignException.Unauthorized unauthorized = Assertions.assertThrows(FeignException.Unauthorized.class,
                () -> clientFor(simpleUsername, originalPassword).users().retrieveOneUser(userId));
        Assertions.assertEquals(401, unauthorized.status());

        // The update password is still working perfectly
        GetUsersUserIdResponse ok = ok(() -> clientFor(simpleUsername, updatedPassword).users().retrieveOneUser(userId));
    }

    @Test
    public void testApplicationUserCanChangeOwnPassword() {
        // Admin creates a new user with an empty role
        Long roleId = FeignRoleHelper.createRole();
        String originalPassword = "QwE!5rTy#9uP0";
        String simpleUsername = Utils.uniqueRandomStringGenerator("NotificationUser", 4);
        GetOfficesResponse headOffice = OfficeHelper.getHeadOffice();
        PostUsersRequest createUserRequest = new PostUsersRequest().username(simpleUsername).firstname(Utils.randomFirstNameGenerator())
                .lastname(Utils.randomLastNameGenerator()).email("whatever@mifos.org").password(originalPassword)
                .repeatPassword(originalPassword).sendPasswordToEmail(false).officeId(headOffice.getId()).roles(List.of(roleId));

        PostUsersResponse userCreationResponse = FeignUserHelper.createUser(createUserRequest);
        Long userId = userCreationResponse.getResourceId();
        Assertions.assertNotNull(userId);

        // User changes its own password

        String updatedPassword = "pX268-4Pfv|kF6";
        ChangePwdUsersUserIdResponse changePwdUsersUserIdResponse = ok(() -> clientFor(simpleUsername, originalPassword).users()
                .changePasswordUser(userId, new ChangePwdUsersUserIdRequest().password(updatedPassword).repeatPassword(updatedPassword)));
        Assertions.assertNotNull(changePwdUsersUserIdResponse.getResourceId());

        // From then on the originalPassword is not working anymore
        FeignException.Unauthorized unauthorized = Assertions.assertThrows(FeignException.Unauthorized.class,
                () -> clientFor(simpleUsername, originalPassword).users().retrieveOneUser(userId));
        Assertions.assertEquals(401, unauthorized.status());

        // The update password is still working perfectly
        GetUsersUserIdResponse ok = ok(() -> clientFor(simpleUsername, updatedPassword).users().retrieveOneUser(userId));
    }

    @Test
    public void testApplicationUserShallNotBeAbleToChangeItsOwnRoles() {
        // Admin creates a new user with one role assigned
        Long roleId = FeignRoleHelper.createRole();
        String password = "QwE!5rTy#9uP0";
        String simpleUsername = Utils.uniqueRandomStringGenerator("NotificationUser", 4);
        GetOfficesResponse headOffice = OfficeHelper.getHeadOffice();
        PostUsersRequest createUserRequest = new PostUsersRequest().username(simpleUsername).firstname(Utils.randomFirstNameGenerator())
                .lastname(Utils.randomLastNameGenerator()).email("whatever@mifos.org").password(password).repeatPassword(password)
                .sendPasswordToEmail(false).officeId(headOffice.getId()).roles(List.of(roleId));

        PostUsersResponse userCreationResponse = FeignUserHelper.createUser(createUserRequest);
        Long userId = userCreationResponse.getResourceId();
        Assertions.assertNotNull(userId);

        // Admin creates a second role
        Long roleId2 = FeignRoleHelper.createRole();

        // User tries to update it's own roles
        CallFailedRuntimeException callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class, () -> {
            ok(() -> clientFor(simpleUsername, password).users().updateUser(userId, new PutUsersUserIdRequest().roles(List.of(roleId2))));
        });

        Assertions.assertEquals(400, callFailedRuntimeException.getStatus());
        Assertions.assertTrue(callFailedRuntimeException.getMessage().contains("not.enough.permission.to.update.fields"));
    }

    @Test
    public void testUserCreationWithValidPassword() {
        String validPassword = "Abcdef1#2$3%XYZ";

        PostUsersRequest createUserRequest = FeignUserHelper.buildUserRequest(validPassword);
        PostUsersResponse userCreationResponse = FeignUserHelper.createUser(createUserRequest);

        Assertions.assertNotNull(userCreationResponse.getResourceId());
    }

    @Test
    public void testUserCreationWithInvalidPasswords() {
        Map<String, String> invalidPasswords = Map.ofEntries(Map.entry("TooShort", "Ab1#Xyz"), // Less than 12
                                                                                               // characters
                Map.entry("NoUppercase", "abcdefg1#2$3%xyz"), // Missing uppercase letter
                Map.entry("NoLowercase", "ABCDEFG1#2$3%XYZ"), // Missing lowercase letter
                Map.entry("NoDigit", "Abcdefg#@$%XYZabc"), // Missing digit
                Map.entry("NoSpecialChar", "Abcdefg123456XYZ"), // Missing special character
                Map.entry("ContainsWhitespace", "Abcdefg1# 2$3%"), // Contains whitespace
                Map.entry("RepeatedCharacters", "AAbbcc11##$$%%YY") // Contains repeated characters
        );

        invalidPasswords.forEach((description, password) -> {
            PostUsersRequest createUserRequest = FeignUserHelper.buildUserRequest(password);
            CallFailedRuntimeException exception = FeignUserHelper.createUserExpectingError(createUserRequest);
            Assertions.assertEquals(400, exception.getStatus(), "Expected HTTP 400 for: " + description);
            Assertions.assertEquals("validation.msg.validation.errors.exist", exception.getUserMessageGlobalisationCode(),
                    "Expected user message code for: " + description);

            ReportedError errorDetails = FeignErrors.firstError(exception);
            Assertions.assertEquals("password", errorDetails.parameterName(),
                    "Expected validation error parameter name for: " + description);
            Assertions.assertEquals("validation.msg.user.password.does.not.match.regexp", errorDetails.userMessageGlobalisationCode(),
                    "Expected validation code for: " + description);
        });
    }

    private static FineractFeignClient clientFor(String username, String password) {
        return FineractFeignClientHelper.createNewFineractFeignClient(username, password);
    }
}
