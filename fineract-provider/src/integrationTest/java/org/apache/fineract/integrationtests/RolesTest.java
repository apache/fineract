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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
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
        final Integer roleId = RolesHelper.createRole(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(roleId);

        LOG.info("--------------------------------- Getting ROLE -------------------------------");
        HashMap<String, Object> role = RolesHelper.getRoleDetails(requestSpec, responseSpec, roleId);
        assertEquals((Integer) role.get("id"), roleId);

    }

    @SuppressWarnings("cast")
    @Test
    public void testDisableRolesStatus() {

        LOG.info("---------------------------------CREATING A ROLE---------------------------------------------");
        final Integer roleId = RolesHelper.createRole(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(roleId);

        LOG.info("--------------------------------- Getting ROLE -------------------------------");
        HashMap<String, Object> role = RolesHelper.getRoleDetails(requestSpec, responseSpec, roleId);
        assertEquals((Integer) role.get("id"), roleId);

        LOG.info("--------------------------------- DISABLING ROLE -------------------------------");
        final Integer disableRoleId = RolesHelper.disableRole(this.requestSpec, this.responseSpec, roleId);
        assertEquals(disableRoleId, roleId);
        role = RolesHelper.getRoleDetails(requestSpec, responseSpec, roleId);
        assertEquals((Integer) role.get("id"), roleId);
        assertEquals(true, (Boolean) role.get("disabled"));

    }

    @SuppressWarnings("cast")
    @Test
    public void testEnableRolesStatus() {

        LOG.info("---------------------------------CREATING A ROLE---------------------------------------------");
        final Integer roleId = RolesHelper.createRole(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(roleId);

        LOG.info("--------------------------------- Getting ROLE -------------------------------");
        HashMap<String, Object> role = RolesHelper.getRoleDetails(requestSpec, responseSpec, roleId);
        assertEquals((Integer) role.get("id"), roleId);

        LOG.info("--------------------------------- DISABLING ROLE -------------------------------");
        final Integer disableRoleId = RolesHelper.disableRole(this.requestSpec, this.responseSpec, roleId);
        assertEquals(disableRoleId, roleId);
        role = RolesHelper.getRoleDetails(requestSpec, responseSpec, roleId);
        assertEquals((Integer) role.get("id"), roleId);
        assertEquals(true, (Boolean) role.get("disabled"));

        LOG.info("--------------------------------- ENABLING ROLE -------------------------------");
        final Integer enableRoleId = RolesHelper.enableRole(this.requestSpec, this.responseSpec, roleId);
        assertEquals(enableRoleId, roleId);
        role = RolesHelper.getRoleDetails(requestSpec, responseSpec, roleId);
        assertEquals((Integer) role.get("id"), roleId);
        assertEquals(false, (Boolean) role.get("disabled"));

    }

    @SuppressWarnings("cast")
    @Test
    public void testDeleteRoleStatus() {

        LOG.info("-------------------------------- CREATING A ROLE---------------------------------------------");
        final Integer roleId = RolesHelper.createRole(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(roleId);

        LOG.info("--------------------------------- Getting ROLE -------------------------------");
        HashMap<String, Object> role = RolesHelper.getRoleDetails(requestSpec, responseSpec, roleId);
        assertEquals((Integer) role.get("id"), roleId);

        LOG.info("--------------------------------- DELETE ROLE -------------------------------");
        final Integer deleteRoleId = RolesHelper.deleteRole(this.requestSpec, this.responseSpec, roleId);
        assertEquals(deleteRoleId, roleId);
    }

    @Test
    public void testRoleShouldGetDeletedIfNoActiveUserExists() {
        final Integer roleId = RolesHelper.createRole(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(roleId);

        final Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(staffId);

        final Integer userId = UserHelper.createUser(this.requestSpec, this.responseSpec, roleId, staffId);
        Assertions.assertNotNull(userId);

        final Integer deletedUserId = UserHelper.deleteUser(this.requestSpec, this.responseSpec, userId);
        Assertions.assertEquals(deletedUserId, userId);

        final Integer deletedRoleId = RolesHelper.deleteRole(this.requestSpec, this.responseSpec, roleId);
        assertEquals(deletedRoleId, roleId);
    }

    @Test
    public void testRoleShouldNotGetDeletedIfActiveUserExists() {
        final Integer roleId = RolesHelper.createRole(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(roleId);

        final Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(staffId);

        final Integer userId = UserHelper.createUser(this.requestSpec, this.responseSpec, roleId, staffId);
        Assertions.assertNotNull(userId);

        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(403).build();
        final Integer deletedRoleId = RolesHelper.deleteRole(this.requestSpec, this.responseSpec, roleId);
        assertNotEquals(deletedRoleId, roleId);
    }

}
