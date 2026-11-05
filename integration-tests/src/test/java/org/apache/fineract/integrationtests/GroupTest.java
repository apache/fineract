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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.DatatableEntry;
import org.apache.fineract.client.models.GetGroupsGroupIdResponse;
import org.apache.fineract.client.models.GroupCreateRequest;
import org.apache.fineract.client.models.GroupCreateResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostColumnHeaderData;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCenterHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDatatableHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Group tests: creation, activation, client association, update, and staff inheritance onto client accounts. */
public class GroupTest extends FeignLoanTestBase {

    private static final String PRINCIPAL = "10000.00";
    private static final String NUMBER_OF_REPAYMENTS = "5";
    private static final String INTEREST_RATE_PER_PERIOD = "18";
    private static final String LOAN_DATE = "20 September 2014";

    private static FeignGroupHelper groupHelper;
    private static FeignStaffHelper staffHelper;
    private static FeignCenterHelper centerHelper;
    private static FeignDatatableHelper datatableHelper;

    @BeforeAll
    public static void setupGroupHelpers() {
        FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();
        groupHelper = new FeignGroupHelper(client);
        staffHelper = new FeignStaffHelper(client);
        centerHelper = new FeignCenterHelper(client);
        datatableHelper = new FeignDatatableHelper(client);
    }

    @Test
    public void checkGroupFunctions() {
        final Long clientId = createClient();
        final Long groupId = groupHelper.createGroup().getResourceId();
        assertEquals(groupId, groupHelper.retrieveGroup(groupId).getId(), "ERROR IN CREATING THE GROUP");

        groupHelper.activateGroup(groupId);
        assertTrue(groupHelper.isGroupActive(groupId), "ERROR IN ACTIVATING THE GROUP");

        groupHelper.associateClient(groupId, clientId);
        assertTrue(groupHelper.retrieveGroupMemberIds(groupId).contains(clientId), "ERROR IN GROUP MEMBER");

        groupHelper.disAssociateClient(groupId, clientId);
        assertTrue(groupHelper.retrieveGroupMemberIds(groupId).isEmpty(), "GROUP MEMBER LIST NOT EMPTY");

        final String updatedGroupName = Utils.uniqueRandomStringGenerator("Group-", 5);
        groupHelper.updateGroup(groupId, updatedGroupName);
        assertEquals(updatedGroupName, groupHelper.retrieveGroup(groupId).getName(), "ERROR IN UPDATING THE GROUP NAME");
    }

    @Test
    public void testGroupCreationWithoutExternalIdGeneratesOne() {
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
        try {
            final GroupCreateRequest request = groupRequest(null);
            final GroupCreateResponse response = ok(() -> fineractClient().groups().createGroup(request));

            final GetGroupsGroupIdResponse group = ok(() -> fineractClient().groups().retrieveOneGroup(response.getGroupId(), false, null));
            assertThat(group.getExternalId()).isNotBlank();
            assertNotEquals("null", group.getExternalId());
        } finally {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
        }
    }

    @Test
    public void testGroupCreationWithExternalIdIsPersisted() {
        final String externalId = UUID.randomUUID().toString();
        final GroupCreateRequest request = groupRequest(externalId);
        final GroupCreateResponse response = ok(() -> fineractClient().groups().createGroup(request));

        final GetGroupsGroupIdResponse group = ok(() -> fineractClient().groups().retrieveOneGroup(response.getGroupId(), false, null));
        assertThat(group.getExternalId()).isEqualTo(externalId);
    }

    @Test
    public void testActiveGroupWithoutActivationDateReturns400() {
        final GroupCreateRequest request = new GroupCreateRequest();
        request.officeId(1L);
        request.name(Utils.uniqueRandomStringGenerator("Group_Name_", 5));
        request.active(true);
        request.dateFormat("dd MMMM yyyy");
        request.locale("en");
        // activationDate intentionally omitted: the @AssertTrue constraint on GroupCreateRequest must reject this.

        final CallFailedRuntimeException error = fail(() -> fineractClient().groups().createGroup(request));
        assertEquals(400, error.getStatus());
    }

    @Test
    public void testGroupCreationUnderCenterUsesCenterOffice() {
        final Long centerOfficeId = 1L;
        final Long centerId = centerHelper.createActiveCenter().getResourceId();

        final GroupCreateRequest request = new GroupCreateRequest();
        request.centerId(centerId);
        // officeId is mandatory on the DTO even though the service derives the real office from the center.
        request.officeId(centerOfficeId);
        request.name(Utils.uniqueRandomStringGenerator("Group_Name_", 5));
        request.active(false);

        final GroupCreateResponse response = ok(() -> fineractClient().groups().createGroup(request));

        final GetGroupsGroupIdResponse group = ok(() -> fineractClient().groups().retrieveOneGroup(response.getGroupId(), false, null));
        assertThat(group.getOfficeId()).isEqualTo(centerOfficeId);
    }

    @Test
    public void testGroupCreationWithClientFromAnotherOfficeFails() {
        // The office must open before the client's activation date below, or client creation itself 400s.
        final Long otherOfficeId = new OfficeHelper().createOffice(LocalDate.of(2000, 1, 1)).getOfficeId();

        final PostClientsRequest clientRequest = new PostClientsRequest()//
                .officeId(otherOfficeId)//
                .legalFormId(1L)//
                .firstname(Utils.randomFirstNameGenerator())//
                .lastname(Utils.randomLastNameGenerator())//
                .active(true)//
                .activationDate("04 March 2011")//
                .dateFormat("dd MMMM yyyy")//
                .locale("en");
        final Long otherOfficeClientId = clientHelper.createClient(clientRequest).getClientId();

        final GroupCreateRequest request = groupRequest(null);
        request.clientMembers(Set.of(otherOfficeClientId));

        final CallFailedRuntimeException error = fail(() -> fineractClient().groups().createGroup(request));
        assertEquals(403, error.getStatus());
    }

    @Test
    public void testGroupCreationSavesDatatableEntry() {
        final String datatableName = Utils.uniqueRandomStringGenerator("dt_group_", 5).toLowerCase();
        final PostDataTablesRequest createDatatableRequest = new PostDataTablesRequest().datatableName(datatableName)
                .apptableName(EntityTables.GROUP.getName()).multiRow(false).addColumnsItem(new PostColumnHeaderData().name("aNote")
                        .type("String").mandatory(false).length(50L).code("").unique(false).indexed(false));
        datatableHelper.createDatatable(createDatatableRequest);

        Long groupId = null;
        try {
            final GroupCreateRequest request = groupRequest(null);
            request.datatables(List.of(new DatatableEntry().registeredTableName(datatableName).data(Map.of("aNote", "note"))));

            final GroupCreateResponse response = ok(() -> fineractClient().groups().createGroup(request));
            groupId = response.getGroupId();

            assertThat(datatableHelper.getDatatableEntries(datatableName, groupId).path("data").size()).isEqualTo(1);
        } finally {
            if (groupId != null) {
                datatableHelper.deleteDatatableEntries(datatableName, groupId);
            }
            datatableHelper.deleteDatatable(datatableName);
        }
    }

    @Test
    public void testGroupCreationWithNonDefaultLocaleAndDateFormat() {
        final GroupCreateRequest request = new GroupCreateRequest();
        request.officeId(1L);
        request.name(Utils.uniqueRandomStringGenerator("Group_Name_", 5));
        request.active(true);
        request.activationDate("04 mars 2011");
        request.dateFormat("dd MMMM yyyy");
        request.locale("fr");

        final GroupCreateResponse response = ok(() -> fineractClient().groups().createGroup(request));

        final GetGroupsGroupIdResponse group = ok(() -> fineractClient().groups().retrieveOneGroup(response.getGroupId(), false, null));
        assertThat(group.getActive()).isTrue();
    }

    private static GroupCreateRequest groupRequest(final String externalId) {
        final GroupCreateRequest request = new GroupCreateRequest();
        request.officeId(1L);
        request.name(Utils.uniqueRandomStringGenerator("Group_Name_", 5));
        request.externalId(externalId);
        request.active(true);
        request.activationDate("04 March 2011");
        request.dateFormat("dd MMMM yyyy");
        request.locale("en");
        return request;
    }

    @Test
    public void assignStaffToGroup() {
        final Long groupId = groupHelper.createGroup().getResourceId();
        assertEquals(groupId, groupHelper.retrieveGroup(groupId).getId(), "ERROR IN CREATING THE GROUP");

        groupHelper.activateGroup(groupId);
        groupHelper.updateGroup(groupId, Utils.uniqueRandomStringGenerator("Savings Group Help_", 5));

        // create client and add client to group
        final Long clientId = createClient();
        groupHelper.associateClient(groupId, clientId);
        assertTrue(groupHelper.retrieveGroupMemberIds(groupId).contains(clientId), "ERROR IN GROUP MEMBER");

        // create staff
        final Long staffId1 = staffHelper.createStaff().getResourceId();
        assertNotNull(staffId1);
        final Long staffId2 = staffHelper.createStaff().getResourceId();
        assertNotNull(staffId2);

        // assign staff "staffId1" to the group
        assertEquals(staffId1, groupHelper.assignStaff(groupId, staffId1).getStaffId(), "Verify assigned staff id is the same as id sent");

        // assign staff "staffId2" to the client
        assertEquals(staffId2, clientHelper.assignStaffToClient(clientId, staffId2).getStaffId(),
                "Verify assigned staff id is the same as id sent");

        // create a client loan and disburse it (loan officer starts unset)
        final Long loanProductId = createLoanProduct(
                new LoanProductTestBuilder().withPrincipal(PRINCIPAL).withNumberOfRepayments(NUMBER_OF_REPAYMENTS)
                        .withinterestRatePerPeriod(INTEREST_RATE_PER_PERIOD).withInterestRateFrequencyTypeAsYear().buildRequest(null));
        final Long loanId = applyForLoan(LoanRequestBuilders.applyLoan(clientId, loanProductId, LOAN_DATE, 10000.0, 4));
        approveLoan(LOAN_DATE, loanId.intValue());
        disburseLoanWithNetDisbursalAmount(loanId, LOAN_DATE, getLoanDetails(loanId).getNetDisbursalAmount().toPlainString());

        // assign staff "staffId1" to the group and cascade it to member client accounts
        final Long inheritedStaffId = groupHelper.assignStaffInheritStaffForClientAccounts(groupId, staffId1).getStaffId();

        // the client's staff officer changed away from staffId2 and now matches the inherited staff
        assertNotEquals(staffId2, inheritedStaffId, "Verify if client staff has changed");
        assertEquals(inheritedStaffId, clientHelper.getClientStaffId(clientId), "Verify if client inherited staff assigned above");

        // the client loan's officer also inherited the staff
        assertEquals(inheritedStaffId, getLoanDetails(loanId).getLoanOfficerId(), "Verify if client loan inherited staff");
    }
}
