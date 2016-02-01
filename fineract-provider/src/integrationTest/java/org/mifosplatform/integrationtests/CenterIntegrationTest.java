/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import static org.junit.Assert.assertEquals;

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
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.common.organisation.StaffHelper;

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
        int officeId = new OfficeHelper(requestSpec, responseSpec).createOffice("01 July 2007");

        String name = "TestBasicCreation" + new Timestamp(new java.util.Date().getTime());
        int resourceId = CenterHelper.createCenter(name, officeId, requestSpec, responseSpec);
        CenterDomain center = CenterHelper.retrieveByID(resourceId, requestSpec, responseSpec);

        Assert.assertNotNull(center);
        Assert.assertTrue(center.getName().equals(name));
        Assert.assertTrue(center.getOfficeId() == officeId);
        Assert.assertTrue(center.isActive() == false);

        // Test retrieval by listing all centers
        int id = CenterHelper.listCenters(requestSpec, responseSpec).get(0).getId();
        Assert.assertTrue(id > 0);

        CenterDomain retrievedCenter = CenterHelper.retrieveByID(id, requestSpec, responseSpec);
        Assert.assertNotNull(retrievedCenter);
        Assert.assertNotNull(retrievedCenter.getName());
        Assert.assertNotNull(retrievedCenter.getHierarchy());
        Assert.assertNotNull(retrievedCenter.getOfficeName());

    }

    @Test
    public void testFullCenterCreation() {

        int officeId = new OfficeHelper(requestSpec, responseSpec).createOffice("01 July 2007");
        String name = "TestFullCreation" + new Timestamp(new java.util.Date().getTime());
        String externalId = Utils.randomStringGenerator("ID_", 7, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        int staffId = StaffHelper.createStaff(requestSpec, responseSpec);
        int[] groupMembers = generateGroupMembers(3, officeId);
        int resourceId = CenterHelper.createCenter(name, officeId, externalId, staffId, groupMembers, requestSpec, responseSpec);
        CenterDomain center = CenterHelper.retrieveByID(resourceId, requestSpec, responseSpec);

        Assert.assertNotNull(center);
        Assert.assertTrue(center.getName().equals(name));
        Assert.assertTrue(center.getOfficeId() == officeId);
        Assert.assertTrue(center.getExternalId().equals(externalId));
        Assert.assertTrue(center.getStaffId() == staffId);
        Assert.assertTrue(center.isActive() == false);
        Assert.assertArrayEquals(center.getGroupMembers(), groupMembers);
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
    public void testVoidCenterRetrieval() {
        ArrayList<CenterDomain> arr = CenterHelper.listCenters(requestSpec, responseSpec);
        int id = arr.get(arr.size() - 1).getId() + 1;
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(404).build();
        CenterDomain center = CenterHelper.retrieveByID(id, requestSpec, responseSpec);
        Assert.assertNotNull(center);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testCenterUpdate() {
        int officeId = new OfficeHelper(requestSpec, responseSpec).createOffice("01 July 2007");
        String name = "TestFullCreation" + new Timestamp(new java.util.Date().getTime());
        String externalId = Utils.randomStringGenerator("ID_", 7, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        int staffId = StaffHelper.createStaff(requestSpec, responseSpec);
        int[] groupMembers = generateGroupMembers(3, officeId);
        int resourceId = CenterHelper.createCenter(name, officeId, externalId, staffId, groupMembers, requestSpec, responseSpec);

        String newName = "TestCenterUpdateNew" + new Timestamp(new java.util.Date().getTime());
        String newExternalId = Utils.randomStringGenerator("newID_", 7, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        int newStaffId = StaffHelper.createStaff(requestSpec, responseSpec);
        int[] associateGroupMembers = generateGroupMembers(2, officeId);

        int[] associateResponse = CenterHelper.associateGroups(resourceId, associateGroupMembers, requestSpec, responseSpec);
        Arrays.sort(associateResponse);
        Arrays.sort(associateGroupMembers);
        Assert.assertArrayEquals(associateResponse, associateGroupMembers);

        int[] newGroupMembers = new int[5];
        for (int i = 0; i < 5; i++) {
            if (i < 3) {
                newGroupMembers[i] = groupMembers[i];
            } else {
                newGroupMembers[i] = associateGroupMembers[i % 3];
            }
        }

        HashMap request = new HashMap();
        request.put("name", newName);
        request.put("externalId", newExternalId);
        request.put("staffId", newStaffId);
        HashMap response = CenterHelper.updateCenter(resourceId, request, requestSpec, responseSpec);
        Assert.assertNotNull(response);
        Assert.assertEquals(newName, response.get("name"));
        Assert.assertEquals(newExternalId, response.get("externalId"));
        Assert.assertEquals(newStaffId, response.get("staffId"));

        CenterDomain center = CenterHelper.retrieveByID(resourceId, requestSpec, responseSpec);
        Assert.assertNotNull(center);
        Assert.assertEquals(newName, center.getName());
        Assert.assertEquals(newExternalId, center.getExternalId());
        Assert.assertEquals((Integer)newStaffId, center.getStaffId());
        Assert.assertArrayEquals(newGroupMembers, center.getGroupMembers());
    }

    @Test
    public void testCenterDeletion() {
        int officeId = new OfficeHelper(requestSpec, responseSpec).createOffice("01 July 2007");
        String name = "TestBasicCreation" + new Timestamp(new java.util.Date().getTime());
        int resourceId = CenterHelper.createCenter(name, officeId, requestSpec, responseSpec);

        CenterHelper.deleteCenter(resourceId, requestSpec, responseSpec);
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(404).build();
        CenterDomain center = CenterHelper.retrieveByID(resourceId, requestSpec, responseSpec);
        Assert.assertNotNull(center);
    }

    private int[] generateGroupMembers(int size, int officeId) {
        int[] groupMembers = new int[size];
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
        return groupMembers;
    }

    @Test
    public void testStaffAssignmentDuringCenterCreation() {

        Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        System.out.println("--------------creating first staff with id-------------" + staffId);
        Assert.assertNotNull(staffId);

        int centerWithStaffId = CenterHelper.createCenterWithStaffId(this.requestSpec, this.responseSpec, staffId);
        CenterDomain center = CenterHelper.retrieveByID(centerWithStaffId, requestSpec, responseSpec);
        Assert.assertNotNull(center);
        Assert.assertTrue(center.getId() == centerWithStaffId);
        Assert.assertTrue(center.getStaffId() == staffId);
        Assert.assertTrue(center.isActive() == true);
    }

    @Test
    public void testAssignStaffToCenter() {
        Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        System.out.println("--------------creating first staff with id-------------" + staffId);
        Assert.assertNotNull(staffId);

        Integer groupID = CenterHelper.createCenter(this.requestSpec, this.responseSpec);
        CenterHelper.verifyCenterCreatedOnServer(this.requestSpec, this.responseSpec, groupID);

        HashMap assignStaffToCenterResponseMap = (HashMap) CenterHelper.assignStaff(this.requestSpec, this.responseSpec, groupID.toString(),
                staffId.longValue());
        assertEquals("Verify assigned staff id is the same as id sent", assignStaffToCenterResponseMap.get("staffId"), staffId);

        CenterDomain center = CenterHelper.retrieveByID(groupID, requestSpec, responseSpec);
        Assert.assertNotNull(center);
        Assert.assertTrue(center.getId() == groupID);
        Assert.assertTrue(center.getStaffId() == staffId);

    }

    @Test
    public void testUnassignStaffToCenter() {
        Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        System.out.println("--------------creating first staff with id-------------" + staffId);
        Assert.assertNotNull(staffId);

        Integer groupID = CenterHelper.createCenter(this.requestSpec, this.responseSpec);
        CenterHelper.verifyCenterCreatedOnServer(this.requestSpec, this.responseSpec, groupID);
        
        HashMap assignStaffToCenterResponseMap = (HashMap) CenterHelper.assignStaff(this.requestSpec, this.responseSpec, groupID.toString(),
                staffId.longValue());
        assertEquals("Verify assigned staff id is the same as id sent", assignStaffToCenterResponseMap.get("staffId"), staffId);
        CenterDomain centerWithStaffAssigned = CenterHelper.retrieveByID(groupID, requestSpec, responseSpec);
        Assert.assertNotNull(centerWithStaffAssigned);
        Assert.assertTrue(centerWithStaffAssigned.getId() == groupID);
        Assert.assertTrue(centerWithStaffAssigned.getStaffId() == staffId);
        
        HashMap unassignStaffToCenterResponseMap = (HashMap) CenterHelper.unassignStaff(this.requestSpec, this.responseSpec, groupID.toString(),
                staffId.longValue());
        assertEquals("Verify staffId is null after unassigning ", unassignStaffToCenterResponseMap.get("staffId"), null);
        CenterDomain centerWithStaffUnssigned = CenterHelper.retrieveByID(groupID, requestSpec, responseSpec);
        Assert.assertNotNull(centerWithStaffUnssigned);
        Assert.assertTrue(centerWithStaffUnssigned.getId() == groupID);
        Assert.assertTrue(centerWithStaffUnssigned.getStaffId() == null);
        
    }

}
