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
package org.apache.fineract.integrationtests.guarantor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuarantorTest {

    private static final Logger LOG = LoggerFactory.getLogger(GuarantorTest.class);
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private GuarantorHelper guarantorHelper;
    private SavingsAccountHelper savingsAccountHelper;
    private static final Float SELF1_BALANCE = Float.valueOf((float) 5000);
    private static final Float EXTERNAL1_BALANCE = Float.valueOf((float) 5000);
    private static final Float EXTERNAL2_BALANCE = Float.valueOf((float) 5000);
    private static final Float SELF1_GURANTEE = Float.valueOf((float) 2000);
    private static final Float EXTERNAL1_GURANTEE = Float.valueOf((float) 2000);
    private static final Float EXTERNAL2_GURANTEE = Float.valueOf((float) 1000);

    @BeforeEach
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

        Float self1_hold_funds = Float.valueOf((float) 0);
        Float external1_hold_funds = Float.valueOf((float) 0);
        Float external2_hold_funds = Float.valueOf((float) 0);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer clientID_external = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        final Integer clientID_external2 = ClientHelper.createClient(this.requestSpec, this.responseSpec);

        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID_external);

        final Integer selfSavigsId = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID,
                String.valueOf(SELF1_BALANCE));
        final Integer externalSavigsId_1 = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID_external,
                String.valueOf(EXTERNAL1_BALANCE));
        final Integer externalSavigsId_2 = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID_external2,
                String.valueOf(EXTERNAL2_BALANCE));

        final Integer loanProductID = createLoanProductWithHoldFunds("50", "20", "20");
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -7 * 4);
        final String loanDisbursementDate = dateFormat.format(todaysDate.getTime());
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, loanDisbursementDate);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        String guarantorJSON = new GuarantorTestBuilder().externalCustomer().build();
        Integer externalGuarantor = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        Assertions.assertNotNull(externalGuarantor);

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithoutGuaranteeAmount(String.valueOf(clientID_external)).build();
        Integer withoutGuaranteeAmount = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        Assertions.assertNotNull(withoutGuaranteeAmount);

        ArrayList<HashMap> errorData = (ArrayList<HashMap>) this.loanTransactionHelper.approveLoan(loanDisbursementDate, null, loanID,
                CommonConstants.RESPONSE_ERROR);
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.self.guarantee.required"));
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.external.guarantee.required"));
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.mandated.guarantee.required"));

        guarantorJSON = new GuarantorTestBuilder()
                .existingCustomerWithGuaranteeAmount(String.valueOf(clientID), String.valueOf(selfSavigsId), String.valueOf(SELF1_GURANTEE))
                .build();
        Integer selfGuarantee = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(selfSavigsId, null);
        Assertions.assertNotNull(selfGuarantee);

        errorData = (ArrayList<HashMap>) this.loanTransactionHelper.approveLoan(loanDisbursementDate, null, loanID,
                CommonConstants.RESPONSE_ERROR);
        assertFalse(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.self.guarantee.required"));
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.external.guarantee.required"));
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.mandated.guarantee.required"));

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithGuaranteeAmount(String.valueOf(clientID_external),
                String.valueOf(externalSavigsId_1), String.valueOf(EXTERNAL1_GURANTEE)).build();
        Integer externalGuarantee_1 = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(externalSavigsId_1, null);
        Assertions.assertNotNull(externalGuarantee_1);

        errorData = (ArrayList<HashMap>) this.loanTransactionHelper.approveLoan(loanDisbursementDate, null, loanID,
                CommonConstants.RESPONSE_ERROR);
        assertFalse(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.self.guarantee.required"));
        assertFalse(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.external.guarantee.required"));
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.mandated.guarantee.required"));

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithGuaranteeAmount(String.valueOf(clientID_external2),
                String.valueOf(externalSavigsId_2), String.valueOf(EXTERNAL2_GURANTEE)).build();
        Integer externalGuarantee_2 = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(externalSavigsId_2, null);
        Assertions.assertNotNull(externalGuarantee_2);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(loanDisbursementDate, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        self1_hold_funds += SELF1_GURANTEE;
        external1_hold_funds += EXTERNAL1_GURANTEE;
        external2_hold_funds += EXTERNAL2_GURANTEE;
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_2, external2_hold_funds);

        LOG.info("-----------------------------------UNDO APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.undoApproval(loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
        verifySavingsOnHoldBalance(selfSavigsId, Float.valueOf((float) 0));
        verifySavingsOnHoldBalance(externalSavigsId_1, Float.valueOf((float) 0));
        verifySavingsOnHoldBalance(externalSavigsId_2, Float.valueOf((float) 0));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(loanDisbursementDate, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_2, external2_hold_funds);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanID);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(loanDisbursementDate, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // First repayment
        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -7 * 3);
        String loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        external1_hold_funds -= Float.valueOf((float) 827.5867);
        external2_hold_funds -= Float.valueOf((float) 413.7933);
        this.loanTransactionHelper.makeRepayment(loanRepaymentDate, totalDueForCurrentPeriod, loanID);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_2, external2_hold_funds);

        // Second repayment
        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -7 * 2);
        loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        totalDueForCurrentPeriod = (Float) loanSchedule.get(2).get("totalDueForPeriod");
        external1_hold_funds -= Float.valueOf((float) 831.4067);
        external2_hold_funds -= Float.valueOf((float) 415.7033333);
        this.loanTransactionHelper.makeRepayment(loanRepaymentDate, totalDueForCurrentPeriod, loanID);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_2, external2_hold_funds);

        // third repayment
        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        totalDueForCurrentPeriod = (Float) loanSchedule.get(3).get("totalDueForPeriod");
        self1_hold_funds -= Float.valueOf((float) 741.355);
        this.loanTransactionHelper.makeRepayment(loanRepaymentDate, totalDueForCurrentPeriod, loanID);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, Float.valueOf((float) 0));
        verifySavingsOnHoldBalance(externalSavigsId_2, Float.valueOf((float) 0));

        // forth repayment
        todaysDate = Calendar.getInstance();
        loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        totalDueForCurrentPeriod = (Float) loanSchedule.get(3).get("totalDueForPeriod");
        this.loanTransactionHelper.makeRepayment(loanRepaymentDate, totalDueForCurrentPeriod, loanID);
        verifySavingsOnHoldBalance(selfSavigsId, Float.valueOf((float) 0));
        verifySavingsOnHoldBalance(externalSavigsId_1, Float.valueOf((float) 0));
        verifySavingsOnHoldBalance(externalSavigsId_2, Float.valueOf((float) 0));

        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testGuarantor_UNDO_DISBURSAL() {

        Float self1_hold_funds = Float.valueOf((float) 0);
        Float external1_hold_funds = Float.valueOf((float) 0);
        Float external2_hold_funds = Float.valueOf((float) 0);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer clientID_external = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        final Integer clientID_external2 = ClientHelper.createClient(this.requestSpec, this.responseSpec);

        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID_external);

        final Integer selfSavigsId = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID,
                String.valueOf(SELF1_BALANCE));
        final Integer externalSavigsId_1 = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID_external,
                String.valueOf(EXTERNAL1_BALANCE));
        final Integer externalSavigsId_3 = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID_external,
                String.valueOf(EXTERNAL1_BALANCE));
        final Integer externalSavigsId_2 = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID_external2,
                String.valueOf(EXTERNAL2_BALANCE));

        final Integer loanProductID = createLoanProductWithHoldFunds("50", "20", "20");
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -7 * 4);
        final String loanDisbursementDate = dateFormat.format(todaysDate.getTime());
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, loanDisbursementDate);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        String guarantorJSON = new GuarantorTestBuilder().externalCustomer().build();
        Integer externalGuarantor = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        Assertions.assertNotNull(externalGuarantor);

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithoutGuaranteeAmount(String.valueOf(clientID_external)).build();
        Integer withoutGuaranteeAmount = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        Assertions.assertNotNull(withoutGuaranteeAmount);

        ArrayList<HashMap> errorData = (ArrayList<HashMap>) this.loanTransactionHelper.approveLoan(loanDisbursementDate, null, loanID,
                CommonConstants.RESPONSE_ERROR);
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.self.guarantee.required"));
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.external.guarantee.required"));
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.mandated.guarantee.required"));

        guarantorJSON = new GuarantorTestBuilder()
                .existingCustomerWithGuaranteeAmount(String.valueOf(clientID), String.valueOf(selfSavigsId), String.valueOf(SELF1_GURANTEE))
                .build();
        Integer selfGuarantee = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(selfSavigsId, null);
        Assertions.assertNotNull(selfGuarantee);

        errorData = (ArrayList<HashMap>) this.loanTransactionHelper.approveLoan(loanDisbursementDate, null, loanID,
                CommonConstants.RESPONSE_ERROR);
        assertFalse(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.self.guarantee.required"));
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.external.guarantee.required"));
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.mandated.guarantee.required"));

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithGuaranteeAmount(String.valueOf(clientID_external),
                String.valueOf(externalSavigsId_1), String.valueOf(EXTERNAL1_GURANTEE)).build();
        Integer externalGuarantee_1 = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(externalSavigsId_1, null);
        Assertions.assertNotNull(externalGuarantee_1);

        errorData = (ArrayList<HashMap>) this.loanTransactionHelper.approveLoan(loanDisbursementDate, null, loanID,
                CommonConstants.RESPONSE_ERROR);
        assertFalse(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.self.guarantee.required"));
        assertFalse(checkForErrorCode(errorData, "validation.msg.loan.guarantor.min.external.guarantee.required"));
        assertTrue(checkForErrorCode(errorData, "validation.msg.loan.guarantor.mandated.guarantee.required"));

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithGuaranteeAmount(String.valueOf(clientID_external2),
                String.valueOf(externalSavigsId_2), String.valueOf(EXTERNAL2_GURANTEE)).build();
        Integer externalGuarantee_2 = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        Assertions.assertNotNull(externalGuarantee_2);
        verifySavingsOnHoldBalance(externalSavigsId_2, null);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(loanDisbursementDate, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        self1_hold_funds += SELF1_GURANTEE;
        external1_hold_funds += EXTERNAL1_GURANTEE;
        external2_hold_funds += EXTERNAL2_GURANTEE;
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_2, external2_hold_funds);

        LOG.info("-----------------------------------UNDO APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.undoApproval(loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
        verifySavingsOnHoldBalance(selfSavigsId, Float.valueOf((float) 0));
        verifySavingsOnHoldBalance(externalSavigsId_1, Float.valueOf((float) 0));
        verifySavingsOnHoldBalance(externalSavigsId_2, Float.valueOf((float) 0));

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(loanDisbursementDate, loanID);
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
                String.valueOf(externalSavigsId_3), String.valueOf(EXTERNAL1_GURANTEE)).build();
        Integer externalGuarantee_3 = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(externalSavigsId_3, EXTERNAL1_GURANTEE);
        Assertions.assertNotNull(externalGuarantee_3);

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

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanID);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(loanDisbursementDate, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // First repayment
        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -7 * 3);
        String loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        external1_hold_funds -= Float.valueOf((float) 827.5867);
        external2_hold_funds -= Float.valueOf((float) 413.7933);
        this.loanTransactionHelper.makeRepayment(loanRepaymentDate, totalDueForCurrentPeriod, loanID);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_3, external1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_2, external2_hold_funds);

        // Second repayment
        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -7 * 2);
        loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        totalDueForCurrentPeriod = (Float) loanSchedule.get(2).get("totalDueForPeriod");
        external1_hold_funds -= Float.valueOf((float) 831.4067);
        external2_hold_funds -= Float.valueOf((float) 415.7033333);
        this.loanTransactionHelper.makeRepayment(loanRepaymentDate, totalDueForCurrentPeriod, loanID);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_3, external1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_2, external2_hold_funds);

        // third repayment
        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        loanRepaymentDate = dateFormat.format(todaysDate.getTime());
        totalDueForCurrentPeriod = (Float) loanSchedule.get(3).get("totalDueForPeriod");
        Float self1_hold_funds_temp = self1_hold_funds - Float.valueOf((float) 741.355);
        HashMap transactionDetail = this.loanTransactionHelper.makeRepayment(loanRepaymentDate, totalDueForCurrentPeriod, loanID);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds_temp);
        verifySavingsOnHoldBalance(externalSavigsId_3, Float.valueOf((float) 0));
        verifySavingsOnHoldBalance(externalSavigsId_2, Float.valueOf((float) 0));

        // undo repayment
        this.loanTransactionHelper.adjustLoanTransaction(loanID, (Integer) transactionDetail.get(CommonConstants.RESPONSE_RESOURCE_ID),
                loanRepaymentDate, "0", "");
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_3, external1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_2, external2_hold_funds);

        // undo disbursal
        loanStatusHashMap = this.loanTransactionHelper.undoDisbursal(loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        verifySavingsOnHoldBalance(selfSavigsId, Float.valueOf((float) SELF1_GURANTEE));
        verifySavingsOnHoldBalance(externalSavigsId_3, Float.valueOf((float) EXTERNAL1_GURANTEE));
        verifySavingsOnHoldBalance(externalSavigsId_2, Float.valueOf((float) EXTERNAL2_GURANTEE));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testGuarantor_RECOVER_GUARANTEES() {

        Float self1_hold_funds = Float.valueOf((float) 0);
        Float external1_hold_funds = Float.valueOf((float) 0);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer clientID_external = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID_external);

        final Integer selfSavigsId = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID,
                String.valueOf(SELF1_BALANCE));
        final Integer externalSavigsId_1 = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID_external,
                String.valueOf(EXTERNAL1_BALANCE));

        final Integer loanProductID = createLoanProductWithHoldFunds("40", "20", "20");
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -21);
        final String loanDisbursementDate = dateFormat.format(todaysDate.getTime());
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, loanDisbursementDate);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        String guarantorJSON = new GuarantorTestBuilder()
                .existingCustomerWithGuaranteeAmount(String.valueOf(clientID), String.valueOf(selfSavigsId), String.valueOf(SELF1_GURANTEE))
                .build();
        Integer selfGuarantee = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(selfSavigsId, null);
        Assertions.assertNotNull(selfGuarantee);

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithGuaranteeAmount(String.valueOf(clientID_external),
                String.valueOf(externalSavigsId_1), String.valueOf(EXTERNAL1_GURANTEE)).build();
        Integer externalGuarantee_1 = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(externalSavigsId_1, null);
        Assertions.assertNotNull(externalGuarantee_1);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(loanDisbursementDate, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        self1_hold_funds += SELF1_GURANTEE;
        external1_hold_funds += EXTERNAL1_GURANTEE;
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanID);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(loanDisbursementDate, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // First repayment
        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        external1_hold_funds -= Float.valueOf((float) 993.104);
        this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);

        this.loanTransactionHelper.recoverFromGuarantor(loanID);
        verifySavingsBalanceAndOnHoldBalance(selfSavigsId, Float.valueOf((float) 0), SELF1_BALANCE - self1_hold_funds);
        verifySavingsBalanceAndOnHoldBalance(externalSavigsId_1, Float.valueOf((float) 0), EXTERNAL1_BALANCE - external1_hold_funds);

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testGuarantor_RECOVER_GUARANTEES_WITH_MORE_GUARANTEE() {

        Float self1_hold_funds = Float.valueOf((float) 0);
        Float external1_hold_funds = Float.valueOf((float) 0);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer clientID_external = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID_external);

        Float selfBalance = Float.valueOf((float) 10000);
        Float externalBalance = Float.valueOf((float) 10000);
        Float selfguarantee = Float.valueOf((float) 6000);
        Float externalguarantee = Float.valueOf((float) 7000);

        final Integer selfSavigsId = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID,
                String.valueOf(selfBalance));
        final Integer externalSavigsId_1 = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID_external,
                String.valueOf(externalBalance));

        final Integer loanProductID = createLoanProductWithHoldFunds("40", "20", "20");
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -21);
        final String loanDisbursementDate = dateFormat.format(todaysDate.getTime());
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, loanDisbursementDate);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        String guarantorJSON = new GuarantorTestBuilder()
                .existingCustomerWithGuaranteeAmount(String.valueOf(clientID), String.valueOf(selfSavigsId), String.valueOf(selfguarantee))
                .build();
        Integer selfGuarantee = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(selfSavigsId, null);
        Assertions.assertNotNull(selfGuarantee);

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithGuaranteeAmount(String.valueOf(clientID_external),
                String.valueOf(externalSavigsId_1), String.valueOf(externalguarantee)).build();
        Integer externalGuarantee_1 = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(externalSavigsId_1, null);
        Assertions.assertNotNull(externalGuarantee_1);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(loanDisbursementDate, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        self1_hold_funds += selfguarantee;
        external1_hold_funds += externalguarantee;
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanID);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(loanDisbursementDate, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // First repayment
        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        external1_hold_funds -= Float.valueOf((float) 3227.588);
        this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);

        this.loanTransactionHelper.recoverFromGuarantor(loanID);
        verifySavingsBalanceAndOnHoldBalance(selfSavigsId, Float.valueOf((float) 0), selfBalance - Float.valueOf((float) 4615.385));
        verifySavingsBalanceAndOnHoldBalance(externalSavigsId_1, Float.valueOf((float) 0),
                externalBalance - Float.valueOf((float) 2901.8553));

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testGuarantor_WRITE_OFF_LOAN() {

        Float self1_hold_funds = Float.valueOf((float) 0);
        Float external1_hold_funds = Float.valueOf((float) 0);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer clientID_external = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID_external);

        final Integer selfSavigsId = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID,
                String.valueOf(SELF1_BALANCE));
        final Integer externalSavigsId_1 = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID_external,
                String.valueOf(EXTERNAL1_BALANCE));

        final Integer loanProductID = createLoanProductWithHoldFunds("40", "20", "20");
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -21);
        final String loanDisbursementDate = dateFormat.format(todaysDate.getTime());
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, loanDisbursementDate);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        String guarantorJSON = new GuarantorTestBuilder()
                .existingCustomerWithGuaranteeAmount(String.valueOf(clientID), String.valueOf(selfSavigsId), String.valueOf(SELF1_GURANTEE))
                .build();
        Integer selfGuarantee = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        verifySavingsOnHoldBalance(selfSavigsId, null);
        Assertions.assertNotNull(selfGuarantee);

        guarantorJSON = new GuarantorTestBuilder().existingCustomerWithGuaranteeAmount(String.valueOf(clientID_external),
                String.valueOf(externalSavigsId_1), String.valueOf(EXTERNAL1_GURANTEE)).build();
        Integer externalGuarantee_1 = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        Assertions.assertNotNull(externalGuarantee_1);
        verifySavingsOnHoldBalance(externalSavigsId_1, null);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(loanDisbursementDate, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        self1_hold_funds += SELF1_GURANTEE;
        external1_hold_funds += EXTERNAL1_GURANTEE;
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);

        LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
        String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanID);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(loanDisbursementDate, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // First repayment
        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        external1_hold_funds -= Float.valueOf((float) 993.104);
        this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);
        verifySavingsOnHoldBalance(selfSavigsId, self1_hold_funds);
        verifySavingsOnHoldBalance(externalSavigsId_1, external1_hold_funds);

        todaysDate = Calendar.getInstance();
        final String LOAN_WRITEOFF_DATE = dateFormat.format(todaysDate.getTime());
        this.loanTransactionHelper.writeOffLoan(LOAN_WRITEOFF_DATE, loanID);
        verifySavingsBalanceAndOnHoldBalance(selfSavigsId, Float.valueOf((float) 0), SELF1_BALANCE);
        verifySavingsBalanceAndOnHoldBalance(externalSavigsId_1, Float.valueOf((float) 0), EXTERNAL1_BALANCE);

    }

    private void verifySavingsOnHoldBalance(final Integer savingsId, final Float expectedBalance) {
        Float onHoldAmount = (Float) this.savingsAccountHelper.getSavingsDetails(savingsId, "onHoldFunds");
        assertEquals(expectedBalance, onHoldAmount, "Verifying On Hold Funds");
    }

    @SuppressWarnings({ "rawtypes", "cast" })
    private void verifySavingsBalanceAndOnHoldBalance(final Integer savingsId, final Float expectedBalance, final Float accountBalance) {
        HashMap savingsDetails = (HashMap) this.savingsAccountHelper.getSavingsDetails(savingsId);
        assertEquals(expectedBalance, savingsDetails.get("onHoldFunds"), "Verifying On Hold Funds");
        HashMap summary = (HashMap) savingsDetails.get("summary");
        assertEquals(accountBalance, summary.get("accountBalance"), "Verifying Account balance");
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
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        LoanProductTestBuilder builder = new LoanProductTestBuilder().withPrincipal("10000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsWeek().withinterestRatePerPeriod("2")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withOnHoldFundDetails(mandatoryGuarantee, minimumGuaranteeFromGuarantor, minimumGuaranteeFromOwnFunds);

        final String loanProductJSON = builder.build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, final String disbursementDate) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
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
