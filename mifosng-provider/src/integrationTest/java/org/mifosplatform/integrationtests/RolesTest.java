/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.useradministration.roles.RolesHelper;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class RolesTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @SuppressWarnings("cast")
    @Test
    public void testCreateRolesStatus() {

        System.out.println("---------------------------------CREATING A ROLE---------------------------------------------");
        final Integer roleId = RolesHelper.createRole(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(roleId);

        System.out.println("--------------------------------- Getting ROLE -------------------------------");
        HashMap<String, Object> role = RolesHelper.getRoleDetails(requestSpec, responseSpec, roleId);
        assertEquals((Integer) role.get("id"), roleId);

    }

    @SuppressWarnings("cast")
    @Test
    public void testDisableRolesStatus() {

        System.out.println("---------------------------------CREATING A ROLE---------------------------------------------");
        final Integer roleId = RolesHelper.createRole(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(roleId);

        System.out.println("--------------------------------- Getting ROLE -------------------------------");
        HashMap<String, Object> role = RolesHelper.getRoleDetails(requestSpec, responseSpec, roleId);
        assertEquals((Integer) role.get("id"), roleId);

        System.out.println("--------------------------------- DISABLING ROLE -------------------------------");
        final Integer disableRoleId = RolesHelper.disableRole(this.requestSpec, this.responseSpec, roleId);
        assertEquals(disableRoleId, roleId);
        role = RolesHelper.getRoleDetails(requestSpec, responseSpec, roleId);
        assertEquals((Integer) role.get("id"), roleId);
        assertEquals((Boolean) role.get("disabled"), true);

    }

    @SuppressWarnings("cast")
    @Test
    public void testEnableRolesStatus() {

        System.out.println("---------------------------------CREATING A ROLE---------------------------------------------");
        final Integer roleId = RolesHelper.createRole(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(roleId);

        System.out.println("--------------------------------- Getting ROLE -------------------------------");
        HashMap<String, Object> role = RolesHelper.getRoleDetails(requestSpec, responseSpec, roleId);
        assertEquals((Integer) role.get("id"), roleId);

        System.out.println("--------------------------------- DISABLING ROLE -------------------------------");
        final Integer disableRoleId = RolesHelper.disableRole(this.requestSpec, this.responseSpec, roleId);
        assertEquals(disableRoleId, roleId);
        role = RolesHelper.getRoleDetails(requestSpec, responseSpec, roleId);
        assertEquals((Integer) role.get("id"), roleId);
        assertEquals((Boolean) role.get("disabled"), true);

        System.out.println("--------------------------------- ENABLING ROLE -------------------------------");
        final Integer enableRoleId = RolesHelper.enableRole(this.requestSpec, this.responseSpec, roleId);
        assertEquals(enableRoleId, roleId);
        role = RolesHelper.getRoleDetails(requestSpec, responseSpec, roleId);
        assertEquals((Integer) role.get("id"), roleId);
        assertEquals((Boolean) role.get("disabled"), false);

    }

    @SuppressWarnings("cast")
    @Test
    public void testDeleteRoleStatus() {

        System.out.println("-------------------------------- CREATING A ROLE---------------------------------------------");
        final Integer roleId = RolesHelper.createRole(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(roleId);

        System.out.println("--------------------------------- Getting ROLE -------------------------------");
        HashMap<String, Object> role = RolesHelper.getRoleDetails(requestSpec, responseSpec, roleId);
        assertEquals((Integer) role.get("id"), roleId);

        System.out.println("--------------------------------- DELETE ROLE -------------------------------");
        final Integer deleteRoleId = RolesHelper.deleteRole(this.requestSpec, this.responseSpec, roleId);
        assertEquals(deleteRoleId, roleId);
    }
}