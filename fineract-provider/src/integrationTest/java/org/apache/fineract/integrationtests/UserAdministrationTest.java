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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.organisation.StaffHelper;
import org.apache.fineract.integrationtests.useradministration.roles.RolesHelper;
import org.apache.fineract.integrationtests.useradministration.users.UserHelper;
import org.apache.fineract.useradministration.service.AppUserConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAdministrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(UserAdministrationTest.class);
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private List<Integer> transientUsers = new ArrayList<>();

    private ResponseSpecification expectStatusCode(int code) {
        return new ResponseSpecBuilder().expectStatusCode(code).build();
    }

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = expectStatusCode(200);
    }

    @AfterEach
    public void tearDown() {
        for (Integer userId : this.transientUsers) {
            UserHelper.deleteUser(this.requestSpec, this.responseSpec, userId);
        }
        this.transientUsers.clear();
    }

    @Test
    public void testCreateNewUserBlocksDuplicateUsername() {

        final Integer roleId = RolesHelper.createRole(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(roleId);

        final Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(staffId);

        final Integer userId = (Integer) UserHelper.createUser(this.requestSpec, this.responseSpec, roleId, staffId, "alphabet",
                "resourceId");
        Assertions.assertNotNull(userId);
        this.transientUsers.add(userId);

        final List errors = (List) UserHelper.createUser(this.requestSpec, expectStatusCode(403), roleId, staffId, "alphabet", "errors");
        Map reason = (Map) errors.get(0);
        LOG.info("Reason: {}", reason.get("defaultUserMessage"));
        LOG.info("Code: {}", reason.get("userMessageGlobalisationCode"));
        Assertions.assertEquals("User with username alphabet already exists.", reason.get("defaultUserMessage"));
        Assertions.assertEquals("error.msg.user.duplicate.username", reason.get("userMessageGlobalisationCode"));
    }

    @Test
    public void testUpdateUserAcceptsNewOrSameUsername() {
        final Integer roleId = RolesHelper.createRole(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(roleId);

        final Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(staffId);

        final Integer userId = (Integer) UserHelper.createUser(this.requestSpec, this.responseSpec, roleId, staffId, "alphabet",
                "resourceId");
        Assertions.assertNotNull(userId);
        this.transientUsers.add(userId);

        final Integer userId2 = (Integer) UserHelper.updateUser(this.requestSpec, this.responseSpec, userId, "renegade", "resourceId");
        Assertions.assertNotNull(userId2);

        final Integer userId3 = (Integer) UserHelper.updateUser(this.requestSpec, this.responseSpec, userId, "renegade", "resourceId");
        Assertions.assertNotNull(userId3);
    }

    @Test
    public void testUpdateUserBlockDuplicateUsername() {
        final Integer roleId = RolesHelper.createRole(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(roleId);

        final Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(staffId);

        final Integer userId = (Integer) UserHelper.createUser(this.requestSpec, this.responseSpec, roleId, staffId, "alphabet",
                "resourceId");
        Assertions.assertNotNull(userId);
        this.transientUsers.add(userId);

        final Integer userId2 = (Integer) UserHelper.createUser(this.requestSpec, this.responseSpec, roleId, staffId, "bilingual",
                "resourceId");
        Assertions.assertNotNull(userId2);
        this.transientUsers.add(userId2);

        final List errors = (List) UserHelper.updateUser(this.requestSpec, expectStatusCode(403), userId2, "alphabet", "errors");
        Map reason = (Map) errors.get(0);
        Assertions.assertEquals("User with username alphabet already exists.", reason.get("defaultUserMessage"));
        Assertions.assertEquals("error.msg.user.duplicate.username", reason.get("userMessageGlobalisationCode"));
    }

    @Test
    public void testCreateNewUserBlocksDuplicateClientId() {
        final Integer roleId = RolesHelper.createRole(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(roleId);

        final Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(staffId);

        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientId);

        final Integer userId = (Integer) UserHelper.createUserForSelfService(this.requestSpec, this.responseSpec, roleId, staffId, clientId,
                "resourceId");
        Assertions.assertNotNull(userId);
        this.transientUsers.add(userId);

        final List errors = (List) UserHelper.createUserForSelfService(this.requestSpec, expectStatusCode(403), roleId, staffId, clientId,
                "errors");
        Map reason = (Map) errors.get(0);
        Assertions.assertEquals("Self Service User Id is already created. Go to Admin->Users to edit or delete the self-service user.",
                reason.get("defaultUserMessage"));
    }

    @Test
    public void testDeleteSystemUser() {
        final Integer userId = UserHelper.getUserId(requestSpec, responseSpec, AppUserConstants.SYSTEM_USER_NAME);
        Assertions.assertNotNull(userId);

        UserHelper.deleteUser(requestSpec, expectStatusCode(403), userId.intValue());
    }

    @Test
    public void testModifySystemUser() {
        final Integer userId = UserHelper.getUserId(requestSpec, responseSpec, AppUserConstants.SYSTEM_USER_NAME);
        Assertions.assertNotNull(userId);

        final List errors = (List) UserHelper.updateUser(this.requestSpec, expectStatusCode(403), userId, "systemtest", "errors");
    }
}
