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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.feign.services.StaffApi.RetrieveAllStaffQueryParams;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.StaffCreateRequest;
import org.apache.fineract.client.models.StaffCreateResponse;
import org.apache.fineract.client.models.StaffData;
import org.apache.fineract.client.models.StaffUpdateRequest;
import org.apache.fineract.client.models.StaffUpdateResponse;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class StaffTest extends FeignIntegrationTest {

    private static final Long HEAD_OFFICE_ID = 1L;
    private static final Long UNKNOWN_STAFF_ID = (long) Integer.MAX_VALUE;

    private FeignStaffHelper staffHelper;

    @BeforeAll
    public void setup() {
        staffHelper = new FeignStaffHelper(fineractClient());
    }

    @Test
    public void testStaffCreate() {
        StaffCreateResponse response = staffHelper.createStaff();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HEAD_OFFICE_ID, response.getOfficeId());
        Assertions.assertNotNull(response.getResourceId());
    }

    @Test
    public void testStaffCreateValidationError() {
        assertValidationError(staffHelper.createStaffExpectingError(requestWithJoiningDate().firstname(firstname()).lastname(lastname())));
        assertValidationError(
                staffHelper.createStaffExpectingError(requestWithJoiningDate().officeId(HEAD_OFFICE_ID).lastname(lastname())));
        assertValidationError(
                staffHelper.createStaffExpectingError(requestWithJoiningDate().officeId(HEAD_OFFICE_ID).firstname(firstname())));

        final StaffCreateRequest request = requestWithJoiningDate().officeId(HEAD_OFFICE_ID).firstname(firstname()).lastname(lastname());

        /** Long firstname test */
        request.firstname(Utils.uniqueRandomStringGenerator("michael_", 43));
        assertValidationError(staffHelper.createStaffExpectingError(request));
        request.firstname(firstname());

        /** Long lastname test */
        request.lastname(Utils.uniqueRandomStringGenerator("Doe_", 47));
        assertValidationError(staffHelper.createStaffExpectingError(request));
        request.lastname(lastname());

        /** Long mobileNo test */
        request.mobileNo(Utils.uniqueRandomStringGenerator("num_", 47));
        assertValidationError(staffHelper.createStaffExpectingError(request));
    }

    @Test
    public void testStaffCreateMaxNameLength() {
        staffHelper.createStaff(requestWithJoiningDate().officeId(HEAD_OFFICE_ID)
                .firstname(Utils.uniqueRandomStringGenerator("michael_", 42)).lastname(Utils.uniqueRandomStringGenerator("Doe_", 46)));
    }

    @Test
    public void testStaffCreateExternalIdValidationError() {
        final StaffCreateRequest request = requestWithJoiningDate().officeId(HEAD_OFFICE_ID).firstname(firstname()).lastname(lastname())
                .externalId(Utils.randomStringGenerator("EXT", 98));
        assertValidationError(staffHelper.createStaffExpectingError(request));
    }

    @Test
    public void testStaffFetch() {
        StaffData response = staffHelper.retrieveStaff(1L);
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getId());
        Assertions.assertEquals(1L, response.getId());
    }

    @Test
    public void testStaffListFetch() {
        Assertions.assertNotNull(staffHelper.retrieveAllStaff(new RetrieveAllStaffQueryParams()));
    }

    @Test
    public void testStaffListStatusAll() {
        Assertions.assertNotNull(staffHelper.retrieveAllStaff(new RetrieveAllStaffQueryParams().status("all")));
    }

    @Test
    public void testStaffListStatusActive() {
        List<StaffData> responseActive = staffHelper.retrieveAllStaff(new RetrieveAllStaffQueryParams().status("active"));
        for (final StaffData staff : responseActive) {
            Assertions.assertNotNull(staff.getId());
            Assertions.assertEquals(true, staff.getIsActive());
        }
    }

    @Test
    public void testStaffListStatusInactive() {
        List<StaffData> responseInactive = staffHelper.retrieveAllStaff(new RetrieveAllStaffQueryParams().status("inactive"));
        for (final StaffData staff : responseInactive) {
            Assertions.assertNotNull(staff.getId());
            Assertions.assertEquals(false, staff.getIsActive());
        }
    }

    @Test
    public void testStaffListFetchWrongState() {
        assertValidationError(staffHelper.retrieveAllStaffExpectingError(new RetrieveAllStaffQueryParams().status("xyz")));
    }

    @Test
    public void testStaffFetchNotFound() {
        Assertions.assertEquals(404, staffHelper.retrieveStaffExpectingError(UNKNOWN_STAFF_ID).getStatus());
    }

    @Test
    public void testStaffUpdate() {
        final String firstname = Utils.uniqueRandomStringGenerator("michael_", 10);
        final String lastname = Utils.uniqueRandomStringGenerator("Doe_", 10);
        final String externalId = UUID.randomUUID().toString();
        final String mobileNo = "+14155552671";

        StaffUpdateResponse response = staffHelper.updateStaff(1L,
                new StaffUpdateRequest().firstname(firstname).lastname(lastname).externalId(externalId).mobileNo(mobileNo));
        Map<String, Object> changes = response.getChanges();

        Assertions.assertEquals(1L, response.getResourceId());
        Assertions.assertEquals(firstname, changes.get("firstname"));
        Assertions.assertEquals(lastname, changes.get("lastname"));
        Assertions.assertEquals(externalId, changes.get("externalId"));
        Assertions.assertEquals(mobileNo, changes.get("mobileNo"));
    }

    @Test
    public void testStaffUpdateLongExternalIdError() {
        assertValidationError(
                staffHelper.updateStaffExpectingError(1L, new StaffUpdateRequest().externalId(Utils.randomStringGenerator("EXT", 98))));
    }

    @Test
    public void testStaffUpdateWrongActiveState() {
        assertValidationError(staffHelper.updateStaffActiveStateExpectingError(1L, "xyz"));
    }

    @Test
    public void testStaffUpdateNotFoundError() {
        Assertions.assertEquals(404,
                staffHelper.updateStaffExpectingError(UNKNOWN_STAFF_ID, new StaffUpdateRequest().firstname(firstname())).getStatus());
    }

    @Test
    public void testStaffUpdateValidationError() {
        final String firstname = Utils.uniqueRandomStringGenerator("michael_", 5);
        final String lastname = Utils.uniqueRandomStringGenerator("Doe_", 4);
        final String firstnameLong = Utils.uniqueRandomStringGenerator("michael_", 43);
        final String lastnameLong = Utils.uniqueRandomStringGenerator("Doe_", 47);

        final StaffUpdateRequest request = new StaffUpdateRequest().firstname(firstname).lastname(lastname);

        /** Test long firstname */
        request.firstname(firstnameLong);
        assertValidationError(staffHelper.updateStaffExpectingError(1L, request));
        request.firstname(firstname);

        /** Test long lastname */
        request.lastname(lastnameLong);
        assertValidationError(staffHelper.updateStaffExpectingError(1L, request));
        request.lastname(lastname);

        /** Long mobileNo test */
        request.mobileNo(Utils.uniqueRandomStringGenerator("num_", 47));
        assertValidationError(staffHelper.updateStaffExpectingError(1L, request));
    }

    @Test
    public void testStaffLoanOfficer() {
        staffHelper.createStaff(requestWithJoiningDate().officeId(HEAD_OFFICE_ID).firstname(firstname())
                .lastname(Utils.uniqueRandomStringGenerator("Doe_", 5)).isLoanOfficer(true));

        List<StaffData> responseActive = staffHelper.retrieveAllStaff(new RetrieveAllStaffQueryParams().loanOfficersOnly(true));
        for (final StaffData staff : responseActive) {
            Assertions.assertNotNull(staff.getId());
            Assertions.assertEquals(true, staff.getIsLoanOfficer());
        }
    }

    private static StaffCreateRequest requestWithJoiningDate() {
        return new StaffCreateRequest().locale("en").dateFormat("dd MMMM yyyy").joiningDate("20 September 2011");
    }

    private static String firstname() {
        return Utils.uniqueRandomStringGenerator("michael_", 5);
    }

    private static String lastname() {
        return Utils.uniqueRandomStringGenerator("Doe_", 4);
    }

    private static void assertValidationError(CallFailedRuntimeException exception) {
        Assertions.assertEquals(400, exception.getStatus());
    }
}
