/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.organisation.StaffHelper;
import org.mifosplatform.integrationtests.common.Utils;

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

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpecForValidationError = new ResponseSpecBuilder().expectStatusCode(400).build();
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
        map.put("firstname", Utils.randomNameGenerator("michael_",42));
        map.put("lastname", Utils.randomNameGenerator("Doe_", 47));

        StaffHelper.createStaffWithJson(requestSpec, responseSpec, new Gson().toJson(map));
    }
}
