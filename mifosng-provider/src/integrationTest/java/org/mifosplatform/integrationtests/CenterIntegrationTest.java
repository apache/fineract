/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import java.sql.Timestamp;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.CenterDomain;
import org.mifosplatform.integrationtests.common.CenterHelper;
import org.mifosplatform.integrationtests.common.OfficeHelper;
import org.mifosplatform.integrationtests.common.StaffHelper;
import org.mifosplatform.integrationtests.common.Utils;

import com.google.gson.Gson;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class CenterIntegrationTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testBasicCenterCreation() {
        OfficeHelper oh = new OfficeHelper(requestSpec, responseSpec);
        int officeId = oh.createOffice("01 July 2007");

        String name = "TestBasicCreation" + new Timestamp(new java.util.Date().getTime());
        int resourceId = CenterHelper.createCenter(name, officeId, requestSpec, responseSpec);
        CenterDomain center = CenterHelper.retrieveByID(resourceId, requestSpec, responseSpec);

        Assert.assertTrue(center.getName().equals(name));
        Assert.assertTrue(center.getOfficeId() == officeId);
        Assert.assertTrue(center.isActive() == false);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFullCenterCreation() {

        int officeId = new OfficeHelper(requestSpec, responseSpec).createOffice("01 July 2007");
        String name = "TestFullCreation" + new Timestamp(new java.util.Date().getTime());
        String externalId = Utils.randomStringGenerator("ID_", 7, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        int staffId = StaffHelper.createStaff(requestSpec, responseSpec);
        int[] groupMembers = new int[3];
        for (int i = 0; i < groupMembers.length; i++) {
            final HashMap<String, String> map = new HashMap<>();
            map.put("officeId", "" + officeId);
            map.put("name", Utils.randomStringGenerator("Group_Name_", 5));
            map.put("externalId", Utils.randomStringGenerator("ID_", 7, "ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
            map.put("dateFormat", "dd MMMM yyyy");
            map.put("locale", "en");
            map.put("active", "true");
            map.put("activationDate", "04 March 2011");

            groupMembers[i] = Utils.performServerPost(requestSpec, responseSpec, "/mifosng-provider/api/v1/groups?"
                    + Utils.TENANT_IDENTIFIER, new Gson().toJson(map), "groupId");
        }
        int resourceId = CenterHelper.createCenter(name, officeId, externalId, staffId, groupMembers, requestSpec, responseSpec);
        CenterDomain center = CenterHelper.retrieveByID(resourceId, requestSpec, responseSpec);

        Assert.assertTrue(center.getName().equals(name));
        Assert.assertTrue(center.getOfficeId() == officeId);
        Assert.assertTrue(center.getExternalId().equals(externalId));
        Assert.assertTrue(center.getStaffId() == staffId);
        Assert.assertTrue(center.isActive() == false);
    }
}
