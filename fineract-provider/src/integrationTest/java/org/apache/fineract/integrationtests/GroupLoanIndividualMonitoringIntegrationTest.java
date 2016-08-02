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



import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GroupHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.GroupLoanIndividualMonitoringHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;









import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class GroupLoanIndividualMonitoringIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpecForStatusCode403;
    private LoanTransactionHelper loanTransactionHelper;
    private GroupLoanIndividualMonitoringHelper groupLoanIndividualMonitoringHelper;
    private static String proposedPrincipalAmount = "12000.00";

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpecForStatusCode403 = new ResponseSpecBuilder().expectStatusCode(403).build();
    }

    @SuppressWarnings({ "static-access", "rawtypes" })
	@Test
    public void checkGroupLoanCreateAndDisburseFlow() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID1 = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        final Integer clientID2 = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID1.toString());
        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID2.toString());
        List<HashMap> clientMembers = getClientMemberShare(clientID1.toString(), "8000.00", clientID2.toString(), "4000.00");
        final Integer loanProductID = createLoanProduct();
        System.out.println("clientMembers :"+clientMembers);
        String loanApplicationDataAsJson = applyForLoanApplication(groupID, loanProductID, clientMembers);
        final Integer loanID = this.loanTransactionHelper.getLoanId(loanApplicationDataAsJson);
        Assert.assertNotNull(loanID);
        System.out.println("Loan created with id :"+loanID);
        this.groupLoanIndividualMonitoringHelper = new GroupLoanIndividualMonitoringHelper(this.requestSpec, this.responseSpec);
        String response = this.groupLoanIndividualMonitoringHelper.getClientMembersByLoanId(requestSpec, responseSpec, loanID);
        JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray = (JsonArray) jsonParser.parse(response);
        Float clientsProposedAmount = Float.valueOf("0.0");
        System.out.println("Toatl proposed Amount for Loan : "+((float)Float.valueOf(proposedPrincipalAmount)));
        for (int i = 0; i < jsonArray.size(); i++) {
        	JsonElement element =  jsonArray.get(i);
        	JsonObject jsonObject = element.getAsJsonObject();
        	JsonElement clientShare = jsonObject.get("proposedAmount");
        	Assert.assertNotNull(clientShare);
        	Assert.assertTrue(clientShare.getAsFloat()>0);
        	JsonElement clientName = jsonObject.get("clientName");
        	JsonElement clientId = jsonObject.get("clientId");
        	System.out.println("client "+clientName+" with id "+clientId+" have share of: "+clientShare);
        	clientsProposedAmount = clientsProposedAmount.sum(clientsProposedAmount, clientShare.getAsFloat());
		}
        System.out.println("Total share of clients is :"+clientsProposedAmount);
        Boolean  isClientShareEqualToPrincipal  = (float)Float.valueOf(proposedPrincipalAmount)== (float)Float.valueOf(clientsProposedAmount);
        Assert.assertTrue(isClientShareEqualToPrincipal);
        
    }
    
    @Test
    public void invalidProposedClientShare() {
    	this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
    	final Integer clientID1 = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        final Integer clientID2 = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID1.toString());
        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID2.toString());
        List<HashMap> clientMembers = getClientMemberShare(clientID1.toString(), "2000.00", clientID2.toString(), "3000.00");
        final Integer loanProductID = createLoanProduct();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpecForStatusCode403);
        
        System.out.println("---------- client share not equal to principal amount ----------");
        String loanApplicationJson = applyForLoanApplication(groupID, loanProductID, clientMembers);
        List<HashMap<String, Object>> errors = (List<HashMap<String, Object>>)this.loanTransactionHelper.createLoanAccount(loanApplicationJson, CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.glim.sum.of.each.clients.share.must.be.equal.to.principal.amount",
        		errors.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));
        
        System.out.println("---------- 1 client have share 0.0 ----------");
        List<HashMap> clientMembers1 = getClientMemberShare(clientID1.toString(), "0.00", clientID2.toString(), "12000.00");
        String loanApplicationJson1 = applyForLoanApplication(groupID, loanProductID, clientMembers1);
        List<HashMap<String, Object>> errors1 = (List<HashMap<String, Object>>)this.loanTransactionHelper.createLoanAccount(loanApplicationJson1, CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.glim.each.client.must.have.more.than.zero.amount",
        		errors1.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));
        
    }
    
    @Test
    public void approvedClientShare() {
    	this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
    	final Integer clientID1 = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        final Integer clientID2 = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID1.toString());
        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID2.toString());
        List<HashMap> clientMembers = getClientMemberShare(clientID1.toString(), "4000.00", clientID2.toString(), "8000.00");
        final Integer loanProductID = createLoanProduct();
        String loanApplicationJson = applyForLoanApplication(groupID, loanProductID, clientMembers);
        final Integer loanID = this.loanTransactionHelper.getLoanId(loanApplicationJson);
        System.out.println("Loan account has been created with ID : "+loanID);
        this.groupLoanIndividualMonitoringHelper = new GroupLoanIndividualMonitoringHelper(this.requestSpec, this.responseSpec);
        String response = this.groupLoanIndividualMonitoringHelper.getClientMembersByLoanId(this.requestSpec, this.responseSpec, loanID);
        clientMembers = getApprovedClientMembers(response);
        final String approveDate = "20 September 2012";
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
        loanStatusHashMap = this.loanTransactionHelper.approveLoanWithApproveAmountAndClientShare(approveDate, proposedPrincipalAmount,
                loanID, clientMembers);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
    public void invalidApprovedClientShare() {
    	this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
    	final Integer clientID1 = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        final Integer clientID2 = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID1.toString());
        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID2.toString());
        List<HashMap> clientMembers = getClientMemberShare(clientID1.toString(), "4000.00", clientID2.toString(), "8000.00");
        final Integer loanProductID = createLoanProduct();
        String loanApplicationJson = applyForLoanApplication(groupID, loanProductID, clientMembers);
        final Integer loanID = this.loanTransactionHelper.getLoanId(loanApplicationJson);
        System.out.println("Loan account has been created with ID : "+loanID);
        this.groupLoanIndividualMonitoringHelper = new GroupLoanIndividualMonitoringHelper(this.requestSpec, this.responseSpec);
        String response = this.groupLoanIndividualMonitoringHelper.getClientMembersByLoanId(this.requestSpec, this.responseSpec, loanID);
        clientMembers = getApprovedClientMembers(response);
        final String approveDate = "20 September 2012";
        final String approvedAmount = "10000.00";
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpecForStatusCode403);
        List<HashMap> errors = (List<HashMap>) this.loanTransactionHelper.approveLoan(clientMembers, approveDate, approvedAmount, loanID,
                CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.glim.sum.of.each.clients.share.must.be.equal.to.principal.amount",
        		errors.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));
        
        
    }
    
    @Test
    public void DisbursedClientShare() {
    	this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
    	final Integer clientID1 = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        final Integer clientID2 = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID1.toString());
        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID2.toString());
        List<HashMap> clientMembers = getClientMemberShare(clientID1.toString(), "4000.00", clientID2.toString(), "8000.00");
        final Integer loanProductID = createLoanProduct();
        String loanApplicationJson = applyForLoanApplication(groupID, loanProductID, clientMembers);
        final Integer loanID = this.loanTransactionHelper.getLoanId(loanApplicationJson);
        System.out.println("Loan account has been created with ID : "+loanID);
        this.groupLoanIndividualMonitoringHelper = new GroupLoanIndividualMonitoringHelper(this.requestSpec, this.responseSpec);
        String response = this.groupLoanIndividualMonitoringHelper.getClientMembersByLoanId(this.requestSpec, this.responseSpec, loanID);
        clientMembers = getApprovedClientMembers(response);
        final String approveDate = "20 September 2012";
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
        loanStatusHashMap = this.loanTransactionHelper.approveLoanWithApproveAmountAndClientShare(approveDate, proposedPrincipalAmount,
                loanID, clientMembers);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        
        // check for disbursement
        this.loanTransactionHelper.disburseLoan(approveDate, loanID, proposedPrincipalAmount, clientMembers);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
        
    }
    
    @Test
    public void invalidDisbursementClientShare() {
    	this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
    	final Integer clientID1 = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        final Integer clientID2 = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID1.toString());
        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID2.toString());
        List<HashMap> clientMembers = getClientMemberShare(clientID1.toString(), "4000.00", clientID2.toString(), "8000.00");
        final Integer loanProductID = createLoanProduct();
        String loanApplicationJson = applyForLoanApplication(groupID, loanProductID, clientMembers);
        final Integer loanID = this.loanTransactionHelper.getLoanId(loanApplicationJson);
        System.out.println("Loan account has been created with ID : "+loanID);
        this.groupLoanIndividualMonitoringHelper = new GroupLoanIndividualMonitoringHelper(this.requestSpec, this.responseSpec);
        String response = this.groupLoanIndividualMonitoringHelper.getClientMembersByLoanId(this.requestSpec, this.responseSpec, loanID);
        clientMembers = getApprovedClientMembers(response);
        final String approveDate = "20 September 2012";
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
        loanStatusHashMap = this.loanTransactionHelper.approveLoanWithApproveAmountAndClientShare(approveDate, proposedPrincipalAmount,
                loanID, clientMembers);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        
        // check for disbursement
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpecForStatusCode403);
        this.loanTransactionHelper.disburseLoan(approveDate, loanID, "10000.00", clientMembers, CommonConstants.RESPONSE_ERROR);
        List<HashMap> errors = (List<HashMap>) this.loanTransactionHelper.disburseLoan(approveDate, loanID, "10000.00", clientMembers, CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.glim.sum.of.each.clients.share.must.be.equal.to.principal.amount",
        		errors.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));
        
    }

    private Integer createLoanProduct() {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal(proposedPrincipalAmount) //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private String applyForLoanApplication(final Integer groupID, final Integer loanProductID, @SuppressWarnings("rawtypes") List<HashMap> clientMembers) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal("12,000.00") //
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("4") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate("20 September 2011") //
                .withSubmittedOnDate("20 September 2011") //
                .withLoanType("group")
                .withClientMembers(clientMembers).build(groupID.toString(), loanProductID.toString(), null);
        System.out.println(loanApplicationJSON);
       // return ;
        return loanApplicationJSON;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private List<HashMap> getClientMemberShare(String clientId1, String client1ShareAmount, String clientId2, String client2ShareAmount){
    	HashMap client1Map = new HashMap<>();
    	client1Map.put("id", clientId1);
    	client1Map.put("amount", client1ShareAmount);
    	HashMap client2Map = new HashMap<>();
    	client2Map.put("id", clientId2);
    	client2Map.put("amount", client2ShareAmount);
    	List<HashMap> clientMembers = new ArrayList<HashMap>();
    	clientMembers.add(client1Map);
    	clientMembers.add(client2Map);    	
    	return clientMembers;
    }
    
    @SuppressWarnings("unchecked")
	private List<HashMap> getApprovedClientMembers(String response){
    	List<HashMap> clientMembers = new ArrayList<HashMap>();
    	JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray = (JsonArray) jsonParser.parse(response);
        for (int i = 0; i < jsonArray.size(); i++) {
        	HashMap clientMember = new HashMap<>();
        	JsonElement element =  jsonArray.get(i);
        	JsonObject jsonObject = element.getAsJsonObject();
        	JsonElement clientShare = jsonObject.get("proposedAmount");
        	JsonElement clientId = jsonObject.get("clientId");
        	JsonElement glimId = jsonObject.get("id");
        	clientMember.put("id", clientId);
        	clientMember.put("glimId", glimId);
        	clientMember.put("amount", clientShare);
        	clientMembers.add(clientMember);
		}    	
    	return clientMembers;
    }
    
}