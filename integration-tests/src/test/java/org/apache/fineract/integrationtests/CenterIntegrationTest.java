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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCenterHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.common.CenterDomain;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Center integration tests, migrated from REST-assured to the typed Feign client. Center create/delete are typed via
 * {@link FeignCenterHelper}; retrieval/list/update/associate reuse the {@link CenterDomain} POJO over
 * {@code FeignRawHttpHelper} because the generated response models don't expose externalId/staffId/groupMembers (see
 * the helper's javadoc).
 */
public class CenterIntegrationTest {

    private static final LocalDate OFFICE_OPENING_DATE = LocalDate.of(2007, 7, 1);
    private static final String GROUP_ACTIVATION_DATE = "04 March 2011";

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
    }

    @Test
    public void testBasicCenterCreation() {
        Long officeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getResourceId();

        String name = "TestBasicCreation" + new Timestamp(new Date().getTime());
        Long resourceId = centerHelper.createCenter(name, officeId).getResourceId();
        CenterDomain center = centerHelper.retrieveCenter(resourceId);

        assertNotNull(center);
        assertEquals(name, center.getName());
        assertEquals(officeId.longValue(), center.getOfficeId().longValue());
        assertFalse(center.isActive());

        // Test retrieval by listing all centers
        int id = centerHelper.listCenters().get(0).getId();
        assertTrue(id > 0);

        CenterDomain retrievedCenter = centerHelper.retrieveCenter((long) id);
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
        int[] groupMembers = generateGroupMembers(3, officeId);
        Long resourceId = centerHelper.createCenter(name, officeId, externalId, staffId, toLongList(groupMembers), null).getResourceId();
        CenterDomain center = centerHelper.retrieveCenter(resourceId);

        assertNotNull(center);
        assertEquals(name, center.getName());
        assertEquals(officeId.longValue(), center.getOfficeId().longValue());
        assertEquals(externalId, center.getExternalId());
        assertEquals(staffId.longValue(), center.getStaffId().longValue());
        assertFalse(center.isActive());
        assertArrayEquals(groupMembers, center.getGroupMembers());
    }

    @Test
    public void testListCenters() {
        List<CenterDomain> paginatedList = centerHelper.paginatedListCenters();
        List<CenterDomain> list = centerHelper.listCenters();

        assertNotNull(paginatedList);
        assertNotNull(list);
        assertArrayEquals(paginatedList.toArray(new CenterDomain[0]), list.toArray(new CenterDomain[0]));
    }

    @Test
    public void testVoidCenterRetrieval() {
        List<CenterDomain> arr = centerHelper.listCentersOrdered();
        int nonExistentId = arr.get(arr.size() - 1).getId() + 1;

        RuntimeException error = centerHelper.retrieveCenterExpectingError((long) nonExistentId);
        assertNotNull(error);
        assertTrue(error.getMessage().contains("HTTP 404"), "Expected 404 for non-existent center, got: " + error.getMessage());
    }

    @Test
    public void testCenterUpdate() {
        Long officeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getResourceId();
        String name = "TestFullCreation" + new Timestamp(new Date().getTime());
        String externalId = UUID.randomUUID().toString();
        Long staffId = staffHelper.createStaff().getResourceId();
        int[] groupMembers = generateGroupMembers(3, officeId);
        Long resourceId = centerHelper.createCenter(name, officeId, externalId, staffId, toLongList(groupMembers), null).getResourceId();

        String newName = "TestCenterUpdateNew" + new Timestamp(new Date().getTime());
        String newExternalId = UUID.randomUUID().toString();
        Long newStaffId = staffHelper.createStaff().getResourceId();
        int[] associateGroupMembers = generateGroupMembers(2, officeId);

        int[] associateResponse = centerHelper.associateGroups(resourceId, associateGroupMembers);
        Arrays.sort(associateResponse);
        Arrays.sort(associateGroupMembers);
        assertArrayEquals(associateGroupMembers, associateResponse);

        int[] newGroupMembers = new int[5];
        for (int i = 0; i < 5; i++) {
            if (i < 3) {
                newGroupMembers[i] = groupMembers[i];
            } else {
                newGroupMembers[i] = associateGroupMembers[i % 3];
            }
        }

        Map<String, Object> request = new HashMap<>();
        request.put("name", newName);
        request.put("externalId", newExternalId);
        request.put("staffId", newStaffId);
        JsonObject changes = centerHelper.updateCenter(resourceId, request);
        assertNotNull(changes);
        assertEquals(newName, changes.get("name").getAsString());
        assertEquals(newExternalId, changes.get("externalId").getAsString());
        assertEquals(newStaffId.longValue(), changes.get("staffId").getAsLong());

        CenterDomain center = centerHelper.retrieveCenter(resourceId);
        assertNotNull(center);
        assertEquals(newName, center.getName());
        assertEquals(newExternalId, center.getExternalId());
        assertEquals(newStaffId.longValue(), center.getStaffId().longValue());
        assertArrayEquals(newGroupMembers, center.getGroupMembers());
    }

    @Test
    public void testCenterDeletion() {
        Long officeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getResourceId();
        String name = "TestBasicCreation" + new Timestamp(new Date().getTime());
        Long resourceId = centerHelper.createCenter(name, officeId).getResourceId();

        centerHelper.deleteCenter(resourceId);

        // Verify the delete took effect: a subsequent retrieval must 404.
        RuntimeException error = centerHelper.retrieveCenterExpectingError(resourceId);
        assertNotNull(error);
        assertTrue(error.getMessage().contains("HTTP 404"), "Expected 404 after deletion, got: " + error.getMessage());
    }

    @Test
    public void testStaffAssignmentDuringCenterCreation() {
        Long staffId = staffHelper.createStaff().getResourceId();
        assertNotNull(staffId);

        Long centerId = centerHelper.createActiveCenterWithStaff(staffId).getResourceId();
        CenterDomain center = centerHelper.retrieveCenter(centerId);
        assertNotNull(center);
        assertEquals(centerId.longValue(), center.getId().longValue());
        assertEquals(staffId.longValue(), center.getStaffId().longValue());
        assertTrue(center.isActive());
    }

    @Test
    public void testAssignStaffToCenter() {
        Long staffId = staffHelper.createStaff().getResourceId();
        assertNotNull(staffId);

        Long centerId = centerHelper.createActiveCenter().getResourceId();
        assertEquals(centerId.longValue(), centerHelper.retrieveCenter(centerId).getId().longValue());

        JsonObject assignChanges = centerHelper.assignStaff(centerId, staffId);
        assertEquals(staffId.longValue(), assignChanges.get("staffId").getAsLong(), "Verify assigned staff id is the same as id sent");

        CenterDomain center = centerHelper.retrieveCenter(centerId);
        assertNotNull(center);
        assertEquals(centerId.longValue(), center.getId().longValue());
        assertEquals(staffId.longValue(), center.getStaffId().longValue());
    }

    @Test
    public void testUnassignStaffToCenter() {
        Long staffId = staffHelper.createStaff().getResourceId();
        assertNotNull(staffId);

        Long centerId = centerHelper.createActiveCenter().getResourceId();
        assertEquals(centerId.longValue(), centerHelper.retrieveCenter(centerId).getId().longValue());

        JsonObject assignChanges = centerHelper.assignStaff(centerId, staffId);
        assertEquals(staffId.longValue(), assignChanges.get("staffId").getAsLong(), "Verify assigned staff id is the same as id sent");
        CenterDomain centerWithStaffAssigned = centerHelper.retrieveCenter(centerId);
        assertNotNull(centerWithStaffAssigned);
        assertEquals(centerId.longValue(), centerWithStaffAssigned.getId().longValue());
        assertEquals(staffId.longValue(), centerWithStaffAssigned.getStaffId().longValue());

        JsonObject unassignChanges = centerHelper.unassignStaff(centerId, staffId);
        assertTrue(isNullMember(unassignChanges.get("staffId")), "Verify staffId is null after unassigning");
        CenterDomain centerWithStaffUnassigned = centerHelper.retrieveCenter(centerId);
        assertNotNull(centerWithStaffUnassigned);
        assertEquals(centerId.longValue(), centerWithStaffUnassigned.getId().longValue());
        assertNull(centerWithStaffUnassigned.getStaffId());
    }

    @Test
    public void testCentersOrphanGroups() {
        Long officeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getResourceId();

        String name = "TestBasicCreation" + new Timestamp(new Date().getTime());
        Long resourceId = centerHelper.createCenter(name, officeId).getResourceId();
        CenterDomain center = centerHelper.retrieveCenter(resourceId);
        assertNotNull(center);

        int id = centerHelper.listCenters().get(0).getId();
        assertTrue(id > 0);

        CenterDomain retrievedCenter = centerHelper.retrieveCenter((long) id);
        assertNotNull(retrievedCenter);
        assertNotNull(retrievedCenter.getName());
        assertNotNull(retrievedCenter.getHierarchy());
        assertNotNull(retrievedCenter.getOfficeName());

        int[] groupMembers = generateGroupMembers(2, officeId);
        centerHelper.associateGroups(resourceId, groupMembers);
        // All groups now have a center as parent, so there are no orphan groups for the office.
        assertEquals("[]", groupHelper.retrieveOrphanGroups(officeId));
    }

    private int[] generateGroupMembers(int size, Long officeId) {
        int[] groupMembers = new int[size];
        for (int i = 0; i < groupMembers.length; i++) {
            groupMembers[i] = groupHelper.createActiveGroup(officeId, GROUP_ACTIVATION_DATE).getGroupId().intValue();
        }
        return groupMembers;
    }

    private static List<Long> toLongList(int[] values) {
        List<Long> result = new ArrayList<>(values.length);
        for (int value : values) {
            result.add((long) value);
        }
        return result;
    }

    private static boolean isNullMember(JsonElement element) {
        return element == null || element.isJsonNull();
    }
}
