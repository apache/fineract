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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetCentersCenterIdResponse;
import org.apache.fineract.client.models.GetCentersGroupMembers;
import org.apache.fineract.client.models.GetCentersPageItems;
import org.apache.fineract.client.models.PutCentersCenterIdRequest;
import org.apache.fineract.client.models.PutCentersChanges;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCenterHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.common.CenterHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Center integration tests. */
public class CenterIntegrationTest {

    private static final LocalDate OFFICE_OPENING_DATE = LocalDate.of(2007, 7, 1);
    private static final String GROUP_ACTIVATION_DATE = "04 March 2011";
    private static final int NOT_FOUND = 404;
    private RequestSpecification requestSpec;
    private ResponseSpecification expectBadRequest;
    private FeignCenterHelper centerHelper;
    private FeignStaffHelper staffHelper;
    private FeignGroupHelper groupHelper;
    private FeignOfficeHelper officeHelper;

    @BeforeEach
    public void setup() {
        FineractFeignClient fineractClient = FineractFeignClientHelper.getFineractFeignClient();
        this.centerHelper = new FeignCenterHelper(fineractClient);
        this.staffHelper = new FeignStaffHelper(fineractClient);
        this.groupHelper = new FeignGroupHelper(fineractClient);
        this.officeHelper = new FeignOfficeHelper(fineractClient);
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.expectBadRequest = new ResponseSpecBuilder().expectStatusCode(400).build();
    }

    @Test
    public void testBasicCenterCreation() {
        Long officeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getResourceId();

        String name = "TestBasicCreation" + new Timestamp(new Date().getTime());
        Long resourceId = centerHelper.createCenter(name, officeId).getResourceId();
        GetCentersCenterIdResponse center = centerHelper.retrieveCenter(resourceId);

        assertNotNull(center);
        assertEquals(name, center.getName());
        assertEquals(officeId, center.getOfficeId());
        assertFalse(center.getActive());

        // Test retrieval by listing all centers
        Long id = centerHelper.listCenters().get(0).getId();
        assertTrue(id > 0);

        GetCentersCenterIdResponse retrievedCenter = centerHelper.retrieveCenter(id);
        assertNotNull(retrievedCenter);
        assertNotNull(retrievedCenter.getName());
        assertNotNull(retrievedCenter.getHierarchy());
        assertNotNull(retrievedCenter.getOfficeName());
    }

    @Test
    public void testFullCenterCreation() {
        Long officeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getResourceId();
        String name = "TestFullCreation" + new Timestamp(new Date().getTime());
        String externalId = UUID.randomUUID().toString();
        Long staffId = staffHelper.createStaff().getResourceId();
        List<Long> groupMembers = generateGroupMembers(3, officeId);
        Long resourceId = centerHelper.createCenter(name, officeId, externalId, staffId, groupMembers, null).getResourceId();
        GetCentersCenterIdResponse center = centerHelper.retrieveCenter(resourceId);

        assertNotNull(center);
        assertEquals(name, center.getName());
        assertEquals(officeId, center.getOfficeId());
        assertEquals(externalId, center.getExternalId());
        assertEquals(staffId, center.getStaffId());
        assertFalse(center.getActive());
        assertEquals(groupMembers, groupMemberIds(center));
    }

    /**
     * Regression test for the SQL injection fix in CenterReadPlatformServiceImpl: a malicious {@code orderBy} value
     * that attempts to break out of the ORDER BY clause and read data from an unrelated, more sensitive table must be
     * rejected with a validation error, not silently concatenated into the SQL.
     */
    @Test
    public void testListCentersRejectsSqlInjectionInOrderBy() {
        final String maliciousOrderBy = "id, (select password from m_appuser limit 1)-- -";

        final Object response = CenterHelper.listCentersRaw(maliciousOrderBy, "ASC", false, requestSpec, expectBadRequest);
        assertNotNull(response, "Expected a validation-error response body, not a silent 200 with leaked data");
    }

    /** Same injection attempt against the paginated listing endpoint, which the patch modifies separately. */
    @Test
    public void testPaginatedListCentersRejectsSqlInjectionInOrderBy() {
        final String maliciousOrderBy = "id, (select password from m_appuser limit 1)-- -";

        final Object response = CenterHelper.listCentersRaw(maliciousOrderBy, "ASC", true, requestSpec, expectBadRequest);
        assertNotNull(response, "Expected a validation-error response body, not a silent 200 with leaked data");
    }

    /**
     * Regression test for the new strict ASC/DESC allow-list on {@code sortOrder}: any value other than exactly
     * ASC/DESC — including an injection payload appended to a nominally valid value — must be rejected.
     */
    @Test
    public void testListCentersRejectsInvalidSortOrderValue() {
        final String maliciousSortOrder = "ASC; DROP TABLE m_office; --";

        final Object response = CenterHelper.listCentersRaw("id", maliciousSortOrder, false, requestSpec, expectBadRequest);
        assertNotNull(response, "Expected a validation-error response body, not a silent 200");
    }

    /** Same invalid-sortOrder check against the paginated listing endpoint. */
    @Test
    public void testPaginatedListCentersRejectsInvalidSortOrderValue() {
        final String maliciousSortOrder = "ASC; DROP TABLE m_office; --";

        final Object response = CenterHelper.listCentersRaw("id", maliciousSortOrder, true, requestSpec, expectBadRequest);
        assertNotNull(response, "Expected a validation-error response body, not a silent 200");
    }

    @Test
    public void testListCenters() {
        List<GetCentersPageItems> paginatedList = centerHelper.paginatedListCenters();
        List<GetCentersPageItems> list = centerHelper.listCenters();

        assertNotNull(paginatedList);
        assertNotNull(list);
        // Neither listing declares an order, so compare them by id.
        assertEquals(byId(paginatedList), byId(list));
    }

    @Test
    public void testVoidCenterRetrieval() {
        List<GetCentersPageItems> arr = centerHelper.listCentersOrdered();
        long nonExistentId = arr.get(arr.size() - 1).getId() + 1;

        assertEquals(NOT_FOUND, centerHelper.retrieveCenterExpectingError(nonExistentId).getStatus(),
                "Expected 404 for non-existent center");
    }

    @Test
    public void testCenterUpdate() {
        Long officeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getResourceId();
        String name = "TestFullCreation" + new Timestamp(new Date().getTime());
        String externalId = UUID.randomUUID().toString();
        Long staffId = staffHelper.createStaff().getResourceId();
        List<Long> groupMembers = generateGroupMembers(3, officeId);
        Long resourceId = centerHelper.createCenter(name, officeId, externalId, staffId, groupMembers, null).getResourceId();

        String newName = "TestCenterUpdateNew" + new Timestamp(new Date().getTime());
        String newExternalId = UUID.randomUUID().toString();
        Long newStaffId = staffHelper.createStaff().getResourceId();
        List<Long> associateGroupMembers = generateGroupMembers(2, officeId);

        List<Long> associateResponse = centerHelper.associateGroups(resourceId, associateGroupMembers);
        assertEquals(associateGroupMembers.stream().sorted().toList(), associateResponse.stream().sorted().toList());

        List<Long> newGroupMembers = new ArrayList<>(groupMembers);
        newGroupMembers.addAll(associateGroupMembers);

        PutCentersCenterIdRequest request = new PutCentersCenterIdRequest().name(newName).externalId(newExternalId).staffId(newStaffId);
        PutCentersChanges changes = centerHelper.updateCenter(resourceId, request);
        assertNotNull(changes);
        assertEquals(newName, changes.getName());
        assertEquals(newExternalId, changes.getExternalId());
        assertEquals(newStaffId, changes.getStaffId());

        GetCentersCenterIdResponse center = centerHelper.retrieveCenter(resourceId);
        assertNotNull(center);
        assertEquals(newName, center.getName());
        assertEquals(newExternalId, center.getExternalId());
        assertEquals(newStaffId, center.getStaffId());
        assertEquals(newGroupMembers, groupMemberIds(center));
    }

    @Test
    public void testCenterDeletion() {
        Long officeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getResourceId();
        String name = "TestBasicCreation" + new Timestamp(new Date().getTime());
        Long resourceId = centerHelper.createCenter(name, officeId).getResourceId();

        centerHelper.deleteCenter(resourceId);

        // Verify the delete took effect: a subsequent retrieval must 404.
        assertEquals(NOT_FOUND, centerHelper.retrieveCenterExpectingError(resourceId).getStatus(), "Expected 404 after deletion");
    }

    @Test
    public void testStaffAssignmentDuringCenterCreation() {
        Long staffId = staffHelper.createStaff().getResourceId();
        assertNotNull(staffId);

        Long centerId = centerHelper.createActiveCenterWithStaff(staffId).getResourceId();
        GetCentersCenterIdResponse center = centerHelper.retrieveCenter(centerId);
        assertNotNull(center);
        assertEquals(centerId, center.getId());
        assertEquals(staffId, center.getStaffId());
        assertTrue(center.getActive());
    }

    @Test
    public void testAssignStaffToCenter() {
        Long staffId = staffHelper.createStaff().getResourceId();
        assertNotNull(staffId);

        Long centerId = centerHelper.createActiveCenter().getResourceId();
        assertEquals(centerId, centerHelper.retrieveCenter(centerId).getId());

        assertEquals(staffId, centerHelper.assignStaff(centerId, staffId).getStaffId(), "Verify assigned staff id is the same as id sent");

        GetCentersCenterIdResponse center = centerHelper.retrieveCenter(centerId);
        assertNotNull(center);
        assertEquals(centerId, center.getId());
        assertEquals(staffId, center.getStaffId());
    }

    @Test
    public void testUnassignStaffToCenter() {
        Long staffId = staffHelper.createStaff().getResourceId();
        assertNotNull(staffId);

        Long centerId = centerHelper.createActiveCenter().getResourceId();
        assertEquals(centerId, centerHelper.retrieveCenter(centerId).getId());

        assertEquals(staffId, centerHelper.assignStaff(centerId, staffId).getStaffId(), "Verify assigned staff id is the same as id sent");
        GetCentersCenterIdResponse centerWithStaffAssigned = centerHelper.retrieveCenter(centerId);
        assertNotNull(centerWithStaffAssigned);
        assertEquals(centerId, centerWithStaffAssigned.getId());
        assertEquals(staffId, centerWithStaffAssigned.getStaffId());

        assertNull(centerHelper.unassignStaff(centerId, staffId).getStaffId(), "Verify staffId is null after unassigning");
        GetCentersCenterIdResponse centerWithStaffUnassigned = centerHelper.retrieveCenter(centerId);
        assertNotNull(centerWithStaffUnassigned);
        assertEquals(centerId, centerWithStaffUnassigned.getId());
        assertNull(centerWithStaffUnassigned.getStaffId());
    }

    @Test
    public void testCentersOrphanGroups() {
        Long officeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getResourceId();

        String name = "TestBasicCreation" + new Timestamp(new Date().getTime());
        Long resourceId = centerHelper.createCenter(name, officeId).getResourceId();
        GetCentersCenterIdResponse center = centerHelper.retrieveCenter(resourceId);
        assertNotNull(center);

        Long id = centerHelper.listCenters().get(0).getId();
        assertTrue(id > 0);

        GetCentersCenterIdResponse retrievedCenter = centerHelper.retrieveCenter(id);
        assertNotNull(retrievedCenter);
        assertNotNull(retrievedCenter.getName());
        assertNotNull(retrievedCenter.getHierarchy());
        assertNotNull(retrievedCenter.getOfficeName());

        centerHelper.associateGroups(resourceId, generateGroupMembers(2, officeId));
        // All groups now have a center as parent, so there are no orphan groups for the office.
        assertTrue(groupHelper.retrieveOrphanGroups(officeId).isEmpty());
    }

    private List<Long> generateGroupMembers(int size, Long officeId) {
        List<Long> groupMembers = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            groupMembers.add(groupHelper.createActiveGroup(officeId, GROUP_ACTIVATION_DATE).getGroupId());
        }
        return groupMembers;
    }

    private static List<Long> groupMemberIds(GetCentersCenterIdResponse center) {
        return center.getGroupMembers().stream().map(GetCentersGroupMembers::getId).toList();
    }

    private static List<GetCentersPageItems> byId(List<GetCentersPageItems> centers) {
        return centers.stream().sorted(Comparator.comparing(GetCentersPageItems::getId)).toList();
    }
}
