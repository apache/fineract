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

import com.google.gson.Gson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.organisation.StaffHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Deprecated // TODO move this into new org.apache.fineract.integrationtests.client.StaffTest
public class StaffTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private ResponseSpecification responseSpecForValidationError;
    private ResponseSpecification responseSpecForNotFoundError;

    @BeforeEach
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
        Map<String, Object> response = StaffHelper.createStaffMap(requestSpec, responseSpec);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(1, response.get("officeId"));
        Assertions.assertNotNull(response.get("resourceId"));
    }

    @Test
    public void testStaffCreateValidationError() {
        final String noOfficeJson = StaffHelper.createStaffWithJSONFields("firstname", "lastname");
        final String noFirstnameJson = StaffHelper.createStaffWithJSONFields("officeId", "lastname");
        final String noLastnameJson = StaffHelper.createStaffWithJSONFields("officeId", "firstname");

        StaffHelper.createStaffWithJson(requestSpec, responseSpecForValidationError, noOfficeJson);
        StaffHelper.createStaffWithJson(requestSpec, responseSpecForValidationError, noFirstnameJson);
        StaffHelper.createStaffWithJson(requestSpec, responseSpecForValidationError, noLastnameJson);

        final Map<String, Object> map = StaffHelper.getMapWithJoiningDate();

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
        map.put("lastname", Utils.randomNameGenerator("Doe_", 4));

        /** Long mobileNo test */
        map.put("mobileNo", Utils.randomNameGenerator("num_", 47));
        StaffHelper.createStaffWithJson(requestSpec, responseSpecForValidationError, new Gson().toJson(map));
    }

    @Test
    public void testStaffCreateMaxNameLength() {

        final Map<String, Object> map = StaffHelper.getMapWithJoiningDate();

        map.put("officeId", 1);
        map.put("firstname", Utils.randomNameGenerator("michael_", 42));
        map.put("lastname", Utils.randomNameGenerator("Doe_", 46));

        StaffHelper.createStaffWithJson(requestSpec, responseSpec, new Gson().toJson(map));
    }

    @Test
    public void testStaffCreateExternalIdValidationError() {
        final Map<String, Object> map = StaffHelper.getMapWithJoiningDate();

        map.put("officeId", 1);
        map.put("firstname", Utils.randomNameGenerator("michael_", 5));
        map.put("lastname", Utils.randomNameGenerator("Doe_", 4));

        map.put("externalId", Utils.randomStringGenerator("EXT", 98));
        StaffHelper.createStaffWithJson(requestSpec, responseSpecForValidationError, new Gson().toJson(map));
    }

    @Test
    public void testStaffFetch() {
        Map<String, Object> response = StaffHelper.getStaff(requestSpec, responseSpec, 1);
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.get("id"));
        Assertions.assertEquals(1, response.get("id"));
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
        List<Map<String, Object>> responseActive = StaffHelper.getStaffListWithState(requestSpec, responseSpec, "active");
        for (final Map<String, Object> staff : responseActive) {
            Assertions.assertNotNull(staff.get("id"));
            Assertions.assertEquals(true, staff.get("isActive"));
        }
    }

    @Test
    public void testStaffListStatusInactive() {
        List<Map<String, Object>> responseInactive = StaffHelper.getStaffListWithState(requestSpec, responseSpec, "inactive");
        for (final Map<String, Object> staff : responseInactive) {
            Assertions.assertNotNull(staff.get("id"));
            Assertions.assertEquals(false, staff.get("isActive"));
        }
    }

    @Test // because "xyz" will return an error, not a List
    public void testStaffListFetchWrongState() throws ClassCastException {
        Assertions.assertThrows(ClassCastException.class, () -> {
            StaffHelper.getStaffListWithState(requestSpec, responseSpecForValidationError, "xyz");
        });
    }

    @Test
    public void testStaffFetchNotFound() {
        StaffHelper.getStaff(requestSpec, responseSpecForNotFoundError, Integer.MAX_VALUE);
    }

    @Test
    public void testStaffUpdate() {
        final Map<String, Object> map = new HashMap<>();
        final String firstname = Utils.randomNameGenerator("michael_", 10);
        final String lastname = Utils.randomNameGenerator("Doe_", 10);
        final String externalId = Utils.randomStringGenerator("EXT", 97);
        final String mobileNo = Utils.randomStringGenerator("num_", 10);

        map.put("firstname", firstname);
        map.put("lastname", lastname);
        map.put("externalId", externalId);
        map.put("mobileNo", mobileNo);

        Map<String, Object> response = StaffHelper.updateStaff(requestSpec, responseSpec, 1, map);
        @SuppressWarnings("unchecked")
        Map<String, Object> changes = (Map<String, Object>) response.get("changes");

        Assertions.assertEquals(1, response.get("resourceId"));
        Assertions.assertEquals(firstname, changes.get("firstname"));
        Assertions.assertEquals(lastname, changes.get("lastname"));
        Assertions.assertEquals(externalId, changes.get("externalId"));
        Assertions.assertEquals(mobileNo, changes.get("mobileNo"));
    }

    @Test
    public void testStaffUpdateLongExternalIdError() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("externalId", Utils.randomStringGenerator("EXT", 98));

        StaffHelper.updateStaff(requestSpec, responseSpecForValidationError, 1, map);
    }

    @Test
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
