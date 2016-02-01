/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.ClientHelper;
import org.mifosplatform.integrationtests.common.GroupHelper;
import org.mifosplatform.integrationtests.common.organisation.StaffHelper;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.mifosplatform.integrationtests.common.loans.LoanProductTestBuilder;
import org.mifosplatform.integrationtests.common.loans.LoanTransactionHelper;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

/**
 * Group Test for checking Group: Creation, Activation, Client Association,
 * Updating & Deletion
 */
public class GroupTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private final String principal = "10000.00";
    private final String accountingRule = "1";
    private final String numberOfRepayments = "5";
    private final String interestRatePerPeriod = "18";

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

    }

    @Test
    public void checkGroupFunctions() {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec);
        GroupHelper.verifyGroupCreatedOnServer(this.requestSpec, this.responseSpec, groupID);

        groupID = GroupHelper.activateGroup(this.requestSpec, this.responseSpec, groupID.toString());
        GroupHelper.verifyGroupActivatedOnServer(this.requestSpec, this.responseSpec, groupID, true);

        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID.toString());
        GroupHelper.verifyGroupMembers(this.requestSpec, this.responseSpec, groupID, clientID);

        groupID = GroupHelper.disAssociateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID.toString());
        GroupHelper.verifyEmptyGroupMembers(this.requestSpec, this.responseSpec, groupID);

        final String updatedGroupName = GroupHelper.randomNameGenerator("Group-", 5);
        groupID = GroupHelper.updateGroup(this.requestSpec, this.responseSpec, updatedGroupName, groupID.toString());
        GroupHelper.verifyGroupDetails(this.requestSpec, this.responseSpec, groupID, "name", updatedGroupName);

        // NOTE: removed as consistently provides false positive result on
        // cloudbees server.
        // groupID = GroupHelper.createGroup(this.requestSpec,
        // this.responseSpec);
        // GroupHelper.deleteGroup(this.requestSpec, this.responseSpec,
        // groupID.toString());
        // GroupHelper.verifyGroupDeleted(this.requestSpec, this.responseSpec,
        // groupID);
    }

    @Test
    public void assignStaffToGroup() {
        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec);
        GroupHelper.verifyGroupCreatedOnServer(this.requestSpec, this.responseSpec, groupID);

        final String updateGroupName = Utils.randomNameGenerator("Savings Group Help_", 5);
        groupID = GroupHelper.activateGroup(this.requestSpec, this.responseSpec, groupID.toString());
        Integer updateGroupId = GroupHelper.updateGroup(this.requestSpec, this.responseSpec, updateGroupName, groupID.toString());

        // create client and add client to group
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID.toString());
        GroupHelper.verifyGroupMembers(this.requestSpec, this.responseSpec, groupID, clientID);

        // create staff
        Integer createStaffId1 = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        System.out.println("--------------creating first staff with id-------------" + createStaffId1);
        Assert.assertNotNull(createStaffId1);

        Integer createStaffId2 = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        System.out.println("--------------creating second staff with id-------------" + createStaffId2);
        Assert.assertNotNull(createStaffId2);

        // assign staff "createStaffId1" to group
        HashMap assignStaffGroupId = (HashMap) GroupHelper.assignStaff(this.requestSpec, this.responseSpec, groupID.toString(),
                createStaffId1.longValue());
        assertEquals("Verify assigned staff id is the same as id sent", assignStaffGroupId.get("staffId"), createStaffId1);

        // assign staff "createStaffId2" to client
        final HashMap assignStaffToClientChanges = (HashMap) ClientHelper.assignStaffToClient(this.requestSpec, this.responseSpec,
                clientID.toString(), createStaffId2.toString());
        assertEquals("Verify assigned staff id is the same as id sent", assignStaffToClientChanges.get("staffId"), createStaffId2);

        final Integer loanProductId = this.createLoanProduct();

        final Integer loanId = this.applyForLoanApplication(clientID, loanProductId, this.principal);

        this.loanTransactionHelper.approveLoan("20 September 2014", loanId);
        this.loanTransactionHelper.disburseLoan("20 September 2014", loanId);

        final HashMap assignStaffAndInheritStaffForClientAccounts = (HashMap) GroupHelper.assignStaffInheritStaffForClientAccounts(
                this.requestSpec, this.responseSpec, groupID.toString(), createStaffId1.toString());
        final Integer getClientStaffId = ClientHelper.getClientsStaffId(this.requestSpec, this.responseSpec, clientID.toString());

        // assert if client staff officer has change Note client was assigned
        // staff with createStaffId2
        assertNotEquals("Verify if client stuff has changed", assignStaffAndInheritStaffForClientAccounts.get("staffId"), createStaffId2);
        assertEquals("Verify if client inherited staff assigned above", assignStaffAndInheritStaffForClientAccounts.get("staffId"),
                getClientStaffId);

        // assert if clients loan officer has changed
        final Integer loanOfficerId = this.loanTransactionHelper.getLoanOfficerId(loanId.toString());
        assertEquals("Verify if client loan inherited staff", assignStaffAndInheritStaffForClientAccounts.get("staffId"), loanOfficerId);

    }

    private Integer createLoanProduct() {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(this.principal)
                .withNumberOfRepayments(this.numberOfRepayments).withinterestRatePerPeriod(this.interestRatePerPeriod)
                .withInterestRateFrequencyTypeAsYear().build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, String principal) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("4") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate("20 September 2014") //
                .withSubmittedOnDate("20 September 2014") //
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

}