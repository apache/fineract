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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.fineract.client.models.GetRolesRoleIdResponse;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.organisation.StaffHelper;
import org.apache.fineract.integrationtests.useradministration.roles.RolesHelper;
import org.apache.fineract.integrationtests.useradministration.users.UserHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RolesTest {

    private static final Logger LOG = LoggerFactory.getLogger(RolesTest.class);
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @SuppressWarnings("cast")
    @Test
    public void testCreateRolesStatus() {

        LOG.info("---------------------------------CREATING A ROLE---------------------------------------------");
        final Long roleId = RolesHelper.createRole().getResourceId();
        Assertions.assertNotNull(roleId);

        LOG.info("--------------------------------- Getting ROLE -------------------------------");
        final GetRolesRoleIdResponse role = RolesHelper.getRoleDetails(roleId);
        assertEquals(role.getId(), roleId);

    }

    @SuppressWarnings("cast")
    @Test
    public void testDisableRolesStatus() {

        LOG.info("---------------------------------CREATING A ROLE---------------------------------------------");
        final Long roleId = RolesHelper.createRole().getResourceId();
        Assertions.assertNotNull(roleId);

        LOG.info("--------------------------------- Getting ROLE -------------------------------");
        GetRolesRoleIdResponse role = RolesHelper.getRoleDetails(roleId);
        assertEquals(role.getId(), roleId);

        LOG.info("--------------------------------- DISABLING ROLE -------------------------------");
        final Long disableRoleId = RolesHelper.disableRole(roleId).getResourceId();
        assertEquals(disableRoleId, roleId);
        role = RolesHelper.getRoleDetails(roleId);
        assertEquals(role.getId(), roleId);
        assertEquals(true, role.getDisabled());

    }

    @SuppressWarnings("cast")
    @Test
    public void testEnableRolesStatus() {

        LOG.info("---------------------------------CREATING A ROLE---------------------------------------------");
        final Long roleId = RolesHelper.createRole().getResourceId();
        assertNotNull(roleId);

        LOG.info("--------------------------------- Getting ROLE -------------------------------");
        GetRolesRoleIdResponse role = RolesHelper.getRoleDetails(roleId);
        assertEquals(role.getId(), roleId);

        LOG.info("--------------------------------- DISABLING ROLE -------------------------------");
        final Long disableRoleId = RolesHelper.disableRole(roleId).getResourceId();
        assertEquals(disableRoleId, roleId);
        role = RolesHelper.getRoleDetails(roleId);
        assertEquals(role.getId(), roleId);
        assertEquals(true, role.getDisabled());

        LOG.info("--------------------------------- ENABLING ROLE -------------------------------");
        final Long enableRoleId = RolesHelper.enableRole(roleId).getResourceId();
        assertEquals(enableRoleId, roleId);
        role = RolesHelper.getRoleDetails(roleId);
        assertEquals(role.getId(), roleId);
        assertEquals(false, role.getDisabled());

    }

    @SuppressWarnings("cast")
    @Test
    public void testDeleteRoleStatus() {

        LOG.info("-------------------------------- CREATING A ROLE---------------------------------------------");
        final Long roleId = RolesHelper.createRole().getResourceId();
        Assertions.assertNotNull(roleId);

        LOG.info("--------------------------------- Getting ROLE -------------------------------");
        GetRolesRoleIdResponse role = RolesHelper.getRoleDetails(roleId);
        assertEquals(role.getId(), roleId);

        LOG.info("--------------------------------- DELETE ROLE -------------------------------");
        final Long deleteRoleId = RolesHelper.deleteRole(roleId).getResourceId();
        assertEquals(deleteRoleId, roleId);
    }

    @Test
    public void testRoleShouldGetDeletedIfNoActiveUserExists() {
        final Long roleId = RolesHelper.createRole().getResourceId();
        Assertions.assertNotNull(roleId);

        final Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(staffId);

        final Long userId = UserHelper.createUser(roleId, staffId.longValue()).getResourceId();
        Assertions.assertNotNull(userId);

        final Long deletedUserId = UserHelper.deleteUser(userId).getResourceId();
        Assertions.assertEquals(deletedUserId, userId);

        final Long deletedRoleId = RolesHelper.deleteRole(roleId).getResourceId();
        assertEquals(deletedRoleId, roleId);
    }

    @Test
    public void testRoleShouldNotGetDeletedIfActiveUserExists() {
        final Long roleId = RolesHelper.createRole().getResourceId();
        Assertions.assertNotNull(roleId);

        final Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(staffId);

        final Long userId = UserHelper.createUser(roleId, staffId.longValue()).getResourceId();
        Assertions.assertNotNull(userId);

        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(403).build();
        final Long deletedRoleId = RolesHelper.deleteRole(roleId).getResourceId();
        assertNotEquals(deletedRoleId, roleId);
    }

}
