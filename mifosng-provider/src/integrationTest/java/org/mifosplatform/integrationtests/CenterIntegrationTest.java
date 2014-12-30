/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
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

        Assert.assertNotNull(center);
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

        Assert.assertNotNull(center);
        Assert.assertTrue(center.getName().equals(name));
        Assert.assertTrue(center.getOfficeId() == officeId);
        Assert.assertTrue(center.getExternalId().equals(externalId));
        Assert.assertTrue(center.getStaffId() == staffId);
        Assert.assertTrue(center.isActive() == false);
        int[] groupMemberList = new int[center.getGroupMembers().size()];
        for (int i = 0; i < groupMemberList.length; i++) {
            groupMemberList[i] = ((Double) center.getGroupMembers().get(i).get("id")).intValue();
        }

        Assert.assertArrayEquals(groupMemberList, groupMembers);
    }

    @Test
    public void testListCenters() {
        ArrayList<CenterDomain> paginatedList = CenterHelper.paginatedListCenters(requestSpec, responseSpec);
        ArrayList<CenterDomain> list = CenterHelper.listCenters(requestSpec, responseSpec);

        Assert.assertNotNull(paginatedList);
        Assert.assertNotNull(list);
        Assert.assertTrue(Arrays.equals(paginatedList.toArray(new CenterDomain[paginatedList.size()]),
                list.toArray(new CenterDomain[list.size()])));
    }

    @Test
    public void testCenterRetrieval() {
        int id = CenterHelper.listCenters(requestSpec, responseSpec).get(0).getId();
        Assert.assertTrue(id > 0);

        CenterDomain center = CenterHelper.retrieveByID(id, requestSpec, responseSpec);
        Assert.assertNotNull(center);
        Assert.assertNotNull(center.getName());
        Assert.assertNotNull(center.getHierarchy());
        Assert.assertNotNull(center.getOfficeName());
    }

    @Test
    public void testVoidCenterRetrieval() {
        ArrayList<CenterDomain> arr = CenterHelper.listCenters(requestSpec, responseSpec);
        int id = arr.get(arr.size() - 1).getId() + 1;

        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(404).build();
        CenterDomain center = CenterHelper.retrieveByID(id, requestSpec, responseSpec);
        Assert.assertNotNull(center);
    }
}
