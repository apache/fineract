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

import java.util.HashMap;
import java.util.List;

import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.organisation.StaffHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class StaffTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private ResponseSpecification responseSpecForValidationError;
    private ResponseSpecification responseSpecForNotFoundError;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpecForValidationError = new ResponseSpecBuilder().expectStatusCode(400).build();
        this.responseSpecForNotFoundError = new ResponseSpecBuilder().expectStatusCode(404).build();
    }

    @Test
    public void testStaffCreate() {
        final HashMap response = StaffHelper.createStaffMap(requestSpec, responseSpec);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.get("officeId"), 1);
        Assert.assertNotNull(response.get("resourceId"));
    }

    @Test
    public void testStaffCreateValidationError() {

        final String noOfficeJson = StaffHelper.createStaffWithJSONFields("firstname", "lastname");
        final String noFirstnameJson = StaffHelper.createStaffWithJSONFields("officeId", "lastname");
        final String noLastnameJson = StaffHelper.createStaffWithJSONFields("officeId", "firstname");

        StaffHelper.createStaffWithJson(requestSpec, responseSpecForValidationError, noOfficeJson);
        StaffHelper.createStaffWithJson(requestSpec, responseSpecForValidationError, noFirstnameJson);
        StaffHelper.createStaffWithJson(requestSpec, responseSpecForValidationError, noLastnameJson);

        final HashMap<String, Object> map = new HashMap<>();

        map.put("officeId", 1);
        map.put("firstname", Utils.randomNameGenerator("michael_", 5));
        map.put("lastname", Utils.randomNameGenerator("Doe_", 4));

        /** Long firstname test */
        map.put("firstname", Utils.randomNameGenerator("michael_", 43));
        StaffHelper.createStaffWithJson(requestSpec, responseSpecForValidationError, new Gson().toJson(map));
        map.put("firstname", Utils.randomNameGenerator("michael_", 5));

        /** Long lastname test */
        map.put("lastname", Utils.randomNameGenerator("Doe_", 47));
        StaffHelper.createStaffWithJson(requestSpec, responseSpecForValidationError, new Gson().toJson(map));
        map.put("lastname", Utils.randomNameGenerator("Doe_",4));

        /** Long mobileNo test */
        map.put("mobileNo", Utils.randomNameGenerator("num_", 47));
        StaffHelper.createStaffWithJson(requestSpec, responseSpecForValidationError, new Gson().toJson(map));
    }

    public void testStaffCreateMaxNameLength() {

        final HashMap<String, Object> map = new HashMap<>();

        map.put("officeId", 1);
        map.put("firstname", Utils.randomNameGenerator("michael_", 42));
        map.put("lastname", Utils.randomNameGenerator("Doe_", 46));

        StaffHelper.createStaffWithJson(requestSpec, responseSpec, new Gson().toJson(map));
    }

    public void testStaffCreateExternalIdValidationError() {
        final HashMap<String, Object> map = new HashMap<>();

        map.put("officeId", 1);
        map.put("firstname", Utils.randomNameGenerator("michael_", 5));
        map.put("lastname", Utils.randomNameGenerator("Doe_", 4));

        map.put("externalId", Utils.randomStringGenerator("EXT", 98));
        StaffHelper.createStaffWithJson(requestSpec, responseSpecForValidationError, new Gson().toJson(map));
    }

    @Test
    public void testStaffFetch() {
        final HashMap response = StaffHelper.getStaff(requestSpec, responseSpec, 1);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.get("id"));
        Assert.assertEquals(response.get("id"), 1);
    }

    @Test
    public void testStaffListFetch() {
        StaffHelper.getStaffList(requestSpec, responseSpec);
    }

    @Test
    public void testStaffListStatusAll() {
        StaffHelper.getStaffListWithState(requestSpec, responseSpec, "all");
    }

    @Test
    public void testStaffListStatusActive() {
        final List<HashMap> responseActive = (List<HashMap>) StaffHelper.getStaffListWithState(requestSpec, responseSpec, "active");
        for(final HashMap staff : responseActive) {
            Assert.assertNotNull(staff.get("id"));
            Assert.assertEquals(staff.get("isActive"), true);
        }
    }

    @Test
    public void testStaffListStatusInactive() {
        final List<HashMap> responseInactive = (List<HashMap>) StaffHelper.getStaffListWithState(requestSpec, responseSpec, "inactive");

        for(final HashMap staff : responseInactive) {
            Assert.assertNotNull(staff.get("id"));
            Assert.assertEquals(staff.get("isActive"), false);
        }
    }

    @Test
    public void testStaffListFetchWrongState() {
        StaffHelper.getStaffListWithState(requestSpec, responseSpecForValidationError, "xyz");
    }

    @Test
    public void testStaffFetchNotFound() {
        StaffHelper.getStaff(requestSpec, responseSpecForNotFoundError, Integer.MAX_VALUE);
    }

    @Test
    public void testStaffUpdate() {
        final HashMap<String, Object> map = new HashMap<>();
        final String firstname = Utils.randomNameGenerator("michael_", 10);
        final String lastname = Utils.randomNameGenerator("Doe_", 10);
        final String externalId = Utils.randomStringGenerator("EXT", 97);
        final String mobileNo = Utils.randomStringGenerator("num_", 10);

        map.put("firstname", firstname);
        map.put("lastname", lastname);
        map.put("externalId", externalId);
        map.put("mobileNo", mobileNo);

        final HashMap response = (HashMap) StaffHelper.updateStaff(requestSpec, responseSpec, 1, map);
        final HashMap changes = (HashMap)  response.get("changes");

        Assert.assertEquals(1, response.get("resourceId"));
        Assert.assertEquals(firstname, changes.get("firstname"));
        Assert.assertEquals(lastname, changes.get("lastname"));
        Assert.assertEquals(externalId, changes.get("externalId"));
        Assert.assertEquals(mobileNo, changes.get("mobileNo"));
    }

    public void testStaffUpdateLongExternalIdError() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("externalId", Utils.randomStringGenerator("EXT", 98));

        StaffHelper.updateStaff(requestSpec, responseSpecForValidationError, 1, map);
    }

    public void testStaffUpdateWrongActiveState() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("isActive", "xyz");

        StaffHelper.updateStaff(requestSpec, responseSpecForValidationError, 1, map);
    }

    @Test
    public void testStaffUpdateNotFoundError() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("firstname", Utils.randomNameGenerator("michael_", 5));

        StaffHelper.updateStaff(requestSpec, responseSpecForNotFoundError, Integer.MAX_VALUE, map);
    }

    @Test
    public void testStaffUpdateValidationError() {
        final HashMap<String, Object> map = new HashMap<>();
        final String firstname = Utils.randomNameGenerator("michael_", 5);
        final String lastname = Utils.randomNameGenerator("Doe_", 4);
        final String firstnameLong = Utils.randomNameGenerator("michael_", 43);
        final String lastnameLong = Utils.randomNameGenerator("Doe_", 47);

        map.put("firstname", firstname);
        map.put("lastname", lastname);

        /** Test long firstname */
        map.put("firstname", firstnameLong);
        StaffHelper.updateStaff(requestSpec, responseSpecForValidationError, 1, map);
        map.put("firstname", firstname);

        /** Test long lastname */
        map.put("lastname", lastnameLong);
        StaffHelper.updateStaff(requestSpec, responseSpecForValidationError, 1, map);
        map.put("lastname", lastname);

        /** Long mobileNo test */
        map.put("mobileNo", Utils.randomNameGenerator("num_", 47));
        StaffHelper.updateStaff(requestSpec, responseSpecForValidationError, 1, map);
        map.remove("mobileNo");

        /** Test unsupported parameter */
        map.put("xyz", "xyz");
        StaffHelper.updateStaff(requestSpec, responseSpecForValidationError, 1, map);
    }
}
