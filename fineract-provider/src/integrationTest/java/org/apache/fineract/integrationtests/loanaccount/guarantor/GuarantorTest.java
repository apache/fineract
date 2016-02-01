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
package org.apache.fineract.integrationtests.loanaccount.guarantor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class GuarantorTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private GuarantorHelper guarantorHelper;
    private SavingsAccountHelper savingsAccountHelper;
    private final Float self1_balance = new Float(5000);
    private final Float external1_balance = new Float(5000);
    private final Float external2_balance = new Float(5000);
    private final Float self1_guarantee = new Float(2000);
    private final Float external1_guarantee = new Float(2000);
    private final Float external2_guarantee = new Float(1000);

    @Before
    public void setUp() throws Exception {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.guarantorHelper = new GuarantorHelper(this.requestSpec, this.responseSpec);
        savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testGuarantor() {

        Float self1_hold_funds = new Float(0);
        Float external1_hold_funds = new Float(0);
        Float external2_hold_funds = new Float(0);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer clientID_external = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        final Integer clientID_external2 = ClientHelper.createClient(this.requestSpec, this.responseSpec);

        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID_external);

        final Integer selfSavigsId = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID,
                String.valueOf(self1_balance));
        final Integer externalSavigsId_1 = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID_external,
                String.valueOf(external1_balance));
        final Integer externalSavigsId_2 = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID_external2,
                String.valueOf(external2_balance));

        final Integer loanProductID = createLoanProductWithHoldFunds("50", "20", "20");
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -7 * 4);
        String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, LOAN_DISBURSEMENT_DATE);
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        String guarantorJSON = new GuarantorTestBuilder().externalCustomer().build();
        Integer externalGuarantor = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        Assert.assertNotNull(externalGuarantor);

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithoutGuaranteeAmount(String.valueOf(clientID_external)).build();
        Integer withoutGuaranteeAmount = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        Assert.assertNotNull(withoutGuaranteeAmount);

        ArrayList<HashMap> errorData = (ArrayList<HashMap>) this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, null, loanID,
                CommonConstants.RESPONSE_ERROR);
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.self.guarantee.required"));
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.external.guarantee.required"));
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.mandated.guarantee.required"));

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithGuaranteeAmount(String.valueOf(clientID),
                String.valueOf(selfSavigsId), String.valueOf(self1_guarantee)).build();
        Integer selfGuarantee = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(selfSavigsId, null);
        Assert.assertNotNull(selfGuarantee);

        errorData = (ArrayList<HashMap>) this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, null, loanID,
                CommonConstants.RESPONSE_ERROR);
        assertFalse(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.self.guarantee.required"));
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.external.guarantee.required"));
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.mandated.guarantee.required"));

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithGuaranteeAmount(String.valueOf(clientID_external),
                String.valueOf(externalSavigsId_1), String.valueOf(external1_guarantee)).build();
        Integer externalGuarantee_1 = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(externalSavigsId_1, null);
        Assert.assertNotNull(externalGuarantee_1);

        errorData = (ArrayList<HashMap>) this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, null, loanID,
                CommonConstants.RESPONSE_ERROR);
        assertFalse(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.self.guarantee.required"));
        assertFalse(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.external.guarantee.required"));
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.mandated.guarantee.required"));

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithGuaranteeAmount(String.valueOf(clientID_external2),
                String.valueOf(externalSavigsId_2), String.valueOf(external2_guarantee)).build();
        Integer externalGuarantee_2 = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(externalSavigsId_2, null);
        Assert.assertNotNull(externalGuarantee_2);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        self1_hold_funds += self1_guarantee;
        external1_hold_funds += external1_guarantee;
        external2_hold_funds += external2_guarantee;
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_2, external2_hold_funds);

        System.out.println("-----------------------------------UNDO APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.undoApproval(loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
        verifySavingsOnHoldBalance(selfSavigsId, new Float(0));
        verifySavingsOnHoldBalance(externalSavigsId_1, new Float(0));
        verifySavingsOnHoldBalance(externalSavigsId_2, new Float(0));

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_2, external2_hold_funds);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // First repayment
        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -7 * 3);
        String LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        external1_hold_funds -= new Float(827.5867);
        external2_hold_funds -= new Float(413.7933);
        this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_2, external2_hold_funds);

        // Second repayment
        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -7 * 2);
        LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        totalDueForCurrentPeriod = (Float) loanSchedule.get(2).get("totalDueForPeriod");
        external1_hold_funds -= new Float(831.4067);
        external2_hold_funds -= new Float(415.7033333);
        this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_2, external2_hold_funds);

        // third repayment
        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        totalDueForCurrentPeriod = (Float) loanSchedule.get(3).get("totalDueForPeriod");
        self1_hold_funds -= new Float(741.355);
        this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, new Float(0));
        verifySavingsOnHoldBalance(externalSavigsId_2, new Float(0));

        // forth repayment
        todaysDate = Calendar.getInstance();
        LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        totalDueForCurrentPeriod = (Float) loanSchedule.get(3).get("totalDueForPeriod");
        this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);
        verifySavingsOnHoldBalance(selfSavigsId, new Float(0));
        verifySavingsOnHoldBalance(externalSavigsId_1, new Float(0));
        verifySavingsOnHoldBalance(externalSavigsId_2, new Float(0));

        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testGuarantor_UNDO_DISBURSAL() {

        Float self1_hold_funds = new Float(0);
        Float external1_hold_funds = new Float(0);
        Float external2_hold_funds = new Float(0);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer clientID_external = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        final Integer clientID_external2 = ClientHelper.createClient(this.requestSpec, this.responseSpec);

        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID_external);

        final Integer selfSavigsId = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID,
                String.valueOf(self1_balance));
        final Integer externalSavigsId_1 = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID_external,
                String.valueOf(external1_balance));
        final Integer externalSavigsId_3 = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID_external,
                String.valueOf(external1_balance));
        final Integer externalSavigsId_2 = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID_external2,
                String.valueOf(external2_balance));

        final Integer loanProductID = createLoanProductWithHoldFunds("50", "20", "20");
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -7 * 4);
        String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, LOAN_DISBURSEMENT_DATE);
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        String guarantorJSON = new GuarantorTestBuilder().externalCustomer().build();
        Integer externalGuarantor = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        Assert.assertNotNull(externalGuarantor);

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithoutGuaranteeAmount(String.valueOf(clientID_external)).build();
        Integer withoutGuaranteeAmount = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        Assert.assertNotNull(withoutGuaranteeAmount);

        ArrayList<HashMap> errorData = (ArrayList<HashMap>) this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, null, loanID,
                CommonConstants.RESPONSE_ERROR);
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.self.guarantee.required"));
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.external.guarantee.required"));
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.mandated.guarantee.required"));

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithGuaranteeAmount(String.valueOf(clientID),
                String.valueOf(selfSavigsId), String.valueOf(self1_guarantee)).build();
        Integer selfGuarantee = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(selfSavigsId, null);
        Assert.assertNotNull(selfGuarantee);

        errorData = (ArrayList<HashMap>) this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, null, loanID,
                CommonConstants.RESPONSE_ERROR);
        assertFalse(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.self.guarantee.required"));
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.external.guarantee.required"));
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.mandated.guarantee.required"));

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithGuaranteeAmount(String.valueOf(clientID_external),
                String.valueOf(externalSavigsId_1), String.valueOf(external1_guarantee)).build();
        Integer externalGuarantee_1 = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(externalSavigsId_1, null);
        Assert.assertNotNull(externalGuarantee_1);

        errorData = (ArrayList<HashMap>) this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, null, loanID,
                CommonConstants.RESPONSE_ERROR);
        assertFalse(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.self.guarantee.required"));
        assertFalse(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.external.guarantee.required"));
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.mandated.guarantee.required"));

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithGuaranteeAmount(String.valueOf(clientID_external2),
                String.valueOf(externalSavigsId_2), String.valueOf(external2_guarantee)).build();
        Integer externalGuarantee_2 = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        Assert.assertNotNull(externalGuarantee_2);
        verifySavingsOnHoldBalance(externalSavigsId_2, null);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        self1_hold_funds += self1_guarantee;
        external1_hold_funds += external1_guarantee;
        external2_hold_funds += external2_guarantee;
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_2, external2_hold_funds);

        System.out.println("-----------------------------------UNDO APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.undoApproval(loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
        verifySavingsOnHoldBalance(selfSavigsId, new Float(0));
        verifySavingsOnHoldBalance(externalSavigsId_1, new Float(0));
        verifySavingsOnHoldBalance(externalSavigsId_2, new Float(0));

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_2, external2_hold_funds);
        List<HashMap> guarantors = this.guarantorHelper.getAllGuarantor(loanID);
        this.guarantorHelper.deleteGuarantor(externalGuarantor, loanID);
        assertFalse((Boolean) this.guarantorHelper.getGuarantor(externalGuarantor, loanID, "status"));
        HashMap errorlog = this.guarantorHelper.deleteGuarantor(withoutGuaranteeAmount, loanID);
        ArrayList<HashMap> error = (ArrayList<HashMap>) errorlog.get(CommonConstants.RESPONSE_ERROR);
        assertTrue(checkForErrorCode(error, "error.msg.loan.guarantor.not.found"));
        guarantors = this.guarantorHelper.getAllGuarantor(loanID);
        assertEquals(4, guarantors.size());
        List<HashMap> externalGuarantee_1_details = (List<HashMap>) this.guarantorHelper.getGuarantor(externalGuarantee_1, loanID,
                "guarantorFundingDetails");
        Integer fundDetailId = (Integer) externalGuarantee_1_details.get(0).get("id");
        errorlog = this.guarantorHelper.deleteGuarantor(externalGuarantee_1, fundDetailId, loanID);
        error = (ArrayList<HashMap>) errorlog.get(CommonConstants.RESPONSE_ERROR);
        assertTrue(checkForErrorCode(error, "validation.msg.loan.guarantor.min.external.guarantee.required"));

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithGuaranteeAmount(String.valueOf(clientID_external),
                String.valueOf(externalSavigsId_3), String.valueOf(external1_guarantee)).build();
        Integer externalGuarantee_3 = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(externalSavigsId_3, external1_guarantee);
        Assert.assertNotNull(externalGuarantee_3);

        this.guarantorHelper.deleteGuarantor(externalGuarantee_1, fundDetailId, loanID);
        guarantors = this.guarantorHelper.getAllGuarantor(loanID);
        assertEquals(4, guarantors.size());
        externalGuarantee_1_details = (List<HashMap>) this.guarantorHelper.getGuarantor(externalGuarantee_1, loanID,
                "guarantorFundingDetails");
        assertEquals(2, externalGuarantee_1_details.size());

        for (HashMap map : externalGuarantee_1_details) {
            if (map.get("id").equals(fundDetailId)) {
                HashMap status = (HashMap) map.get("status");
                assertEquals("guarantorFundStatusType.withdrawn", status.get("code"));
            }
        }

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // First repayment
        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -7 * 3);
        String LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        external1_hold_funds -= new Float(827.5867);
        external2_hold_funds -= new Float(413.7933);
        this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_3, external1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_2, external2_hold_funds);

        // Second repayment
        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -7 * 2);
        LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        totalDueForCurrentPeriod = (Float) loanSchedule.get(2).get("totalDueForPeriod");
        external1_hold_funds -= new Float(831.4067);
        external2_hold_funds -= new Float(415.7033333);
        this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_3, external1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_2, external2_hold_funds);

        // third repayment
        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        totalDueForCurrentPeriod = (Float) loanSchedule.get(3).get("totalDueForPeriod");
        Float self1_hold_funds_temp = self1_hold_funds - new Float(741.355);
        HashMap transactionDetail = this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds_temp);
        verifySavingsOnHoldBalance(externalSavigsId_3, new Float(0));
        verifySavingsOnHoldBalance(externalSavigsId_2, new Float(0));

        // undo repayment
        this.loanTransactionHelper.adjustLoanTransaction(loanID, (Integer) transactionDetail.get(CommonConstants.RESPONSE_RESOURCE_ID),
                LOAN_REPAYMENT_DATE, "0", "");
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_3, external1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_2, external2_hold_funds);

        // undo disbursal
        loanStatusHashMap = this.loanTransactionHelper.undoDisbursal(loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        verifySavingsOnHoldBalance(selfSavigsId, new Float(self1_guarantee));
        verifySavingsOnHoldBalance(externalSavigsId_3, new Float(external1_guarantee));
        verifySavingsOnHoldBalance(externalSavigsId_2, new Float(external2_guarantee));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testGuarantor_RECOVER_GUARANTEES() {

        Float self1_hold_funds = new Float(0);
        Float external1_hold_funds = new Float(0);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer clientID_external = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID_external);

        final Integer selfSavigsId = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID,
                String.valueOf(self1_balance));
        final Integer externalSavigsId_1 = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID_external,
                String.valueOf(external1_balance));

        final Integer loanProductID = createLoanProductWithHoldFunds("40", "20", "20");
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -21);
        String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, LOAN_DISBURSEMENT_DATE);
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        String guarantorJSON = new GuarantorTestBuilder().existingCustomerWithGuaranteeAmount(String.valueOf(clientID),
                String.valueOf(selfSavigsId), String.valueOf(self1_guarantee)).build();
        Integer selfGuarantee = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(selfSavigsId, null);
        Assert.assertNotNull(selfGuarantee);

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithGuaranteeAmount(String.valueOf(clientID_external),
                String.valueOf(externalSavigsId_1), String.valueOf(external1_guarantee)).build();
        Integer externalGuarantee_1 = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(externalSavigsId_1, null);
        Assert.assertNotNull(externalGuarantee_1);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        self1_hold_funds += self1_guarantee;
        external1_hold_funds += external1_guarantee;
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // First repayment
        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        String LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        external1_hold_funds -= new Float(993.104);
        this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);

        this.loanTransactionHelper.recoverFromGuarantor(loanID);
        verifySavingsBalanceAndOnHoldBalance(selfSavigsId, new Float(0), self1_balance - self1_hold_funds);
        verifySavingsBalanceAndOnHoldBalance(externalSavigsId_1, new Float(0), external1_balance - external1_hold_funds);

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testGuarantor_RECOVER_GUARANTEES_WITH_MORE_GUARANTEE() {

        Float self1_hold_funds = new Float(0);
        Float external1_hold_funds = new Float(0);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer clientID_external = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID_external);

        Float selfBalance = new Float(10000);
        Float externalBalance = new Float(10000);
        Float selfguarantee = new Float(6000);
        Float externalguarantee = new Float(7000);
        
        final Integer selfSavigsId = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID,
                String.valueOf(selfBalance));
        final Integer externalSavigsId_1 = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID_external,
                String.valueOf(externalBalance));

        final Integer loanProductID = createLoanProductWithHoldFunds("40", "20", "20");
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -21);
        String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, LOAN_DISBURSEMENT_DATE);
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        String guarantorJSON = new GuarantorTestBuilder().existingCustomerWithGuaranteeAmount(String.valueOf(clientID),
                String.valueOf(selfSavigsId), String.valueOf(selfguarantee)).build();
        Integer selfGuarantee = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(selfSavigsId, null);
        Assert.assertNotNull(selfGuarantee);

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithGuaranteeAmount(String.valueOf(clientID_external),
                String.valueOf(externalSavigsId_1), String.valueOf(externalguarantee)).build();
        Integer externalGuarantee_1 = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(externalSavigsId_1, null);
        Assert.assertNotNull(externalGuarantee_1);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        self1_hold_funds += selfguarantee;
        external1_hold_funds += externalguarantee;
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // First repayment
        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        String LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        external1_hold_funds -= new Float(3227.588);
        this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);

        this.loanTransactionHelper.recoverFromGuarantor(loanID);
        verifySavingsBalanceAndOnHoldBalance(selfSavigsId, new Float(0), selfBalance - new Float(4615.385));
        verifySavingsBalanceAndOnHoldBalance(externalSavigsId_1, new Float(0), externalBalance - new Float(2901.8553));

    }

    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testGuarantor_WRITE_OFF_LOAN() {

        Float self1_hold_funds = new Float(0);
        Float external1_hold_funds = new Float(0);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer clientID_external = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID_external);

        final Integer selfSavigsId = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID,
                String.valueOf(self1_balance));
        final Integer externalSavigsId_1 = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID_external,
                String.valueOf(external1_balance));

        final Integer loanProductID = createLoanProductWithHoldFunds("40", "20", "20");
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -21);
        String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, LOAN_DISBURSEMENT_DATE);
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        String guarantorJSON = new GuarantorTestBuilder().existingCustomerWithGuaranteeAmount(String.valueOf(clientID),
                String.valueOf(selfSavigsId), String.valueOf(self1_guarantee)).build();
        Integer selfGuarantee = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(selfSavigsId, null);
        Assert.assertNotNull(selfGuarantee);

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithGuaranteeAmount(String.valueOf(clientID_external),
                String.valueOf(externalSavigsId_1), String.valueOf(external1_guarantee)).build();
        Integer externalGuarantee_1 = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        Assert.assertNotNull(externalGuarantee_1);
        verifySavingsOnHoldBalance(externalSavigsId_1, null);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        self1_hold_funds += self1_guarantee;
        external1_hold_funds += external1_guarantee;
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // First repayment
        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        String LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        external1_hold_funds -= new Float(993.104);
        this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);

        todaysDate = Calendar.getInstance();
        String LOAN_WRITEOFF_DATE = dateFormat.format(todaysDate.getTime());
        this.loanTransactionHelper.writeOffLoan(LOAN_WRITEOFF_DATE, loanID);
        verifySavingsBalanceAndOnHoldBalance(selfSavigsId, new Float(0), self1_balance);
        verifySavingsBalanceAndOnHoldBalance(externalSavigsId_1, new Float(0), external1_balance);

    }

    private void verifySavingsOnHoldBalance(final Integer savingsId, final Float expectedBalance) {
        Float onHoldAmount = (Float) this.savingsAccountHelper.getSavingsDetails(savingsId, "onHoldFunds");
        assertEquals("Verifying On Hold Funds", expectedBalance, onHoldAmount);
    }

    @SuppressWarnings({ "rawtypes", "cast" })
    private void verifySavingsBalanceAndOnHoldBalance(final Integer savingsId, final Float expectedBalance, final Float accountBalance) {
        HashMap savingsDetails = (HashMap) this.savingsAccountHelper.getSavingsDetails(savingsId);
        assertEquals("Verifying On Hold Funds", expectedBalance, savingsDetails.get("onHoldFunds"));
        HashMap summary = (HashMap) savingsDetails.get("summary");
        assertEquals("Verifying Account balance", accountBalance, summary.get("accountBalance"));
    }

    @SuppressWarnings("rawtypes")
    private boolean checkForErrorCode(final ArrayList<HashMap> errorData, final String errorcode) {
        boolean isExists = false;
        for (HashMap errorMap : errorData) {
            String actualErrorCode = (String) errorMap.get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE);
            if (actualErrorCode != null && actualErrorCode.equals(errorcode)) {
                isExists = true;
                break;
            }
        }
        return isExists;

    }

    private Integer createLoanProductWithHoldFunds(final String mandatoryGuarantee, final String minimumGuaranteeFromGuarantor,
            final String minimumGuaranteeFromOwnFunds) {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        LoanProductTestBuilder builder = new LoanProductTestBuilder().withPrincipal("10000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsWeek().withinterestRatePerPeriod("2")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withOnHoldFundDetails(mandatoryGuarantee, minimumGuaranteeFromGuarantor, minimumGuaranteeFromOwnFunds);

        final String loanProductJSON = builder.build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, final String disbursementDate) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal("10000.00") //
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsWeeks() //
                .withNumberOfRepayments("4") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsWeeks() //
                .withInterestRatePerPeriod("2") //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate(disbursementDate) //
                .withSubmittedOnDate(disbursementDate) //
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

}
