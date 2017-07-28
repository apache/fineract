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

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntry;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.accounting.PeriodicAccrualAccountingHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.savings.AccountTransferHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

/**
 * Client Loan Integration Test for checking Loan Application Repayments
 * Schedule, loan charges, penalties, loan repayments and verifying accounting
 * transactions
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ClientLoanIntegrationTest {

    public static final String MINIMUM_OPENING_BALANCE = "1000.0";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";

    private static final String NONE = "1";
    private static final String CASH_BASED = "2";
    private static final String ACCRUAL_PERIODIC = "3";
    private static final String ACCRUAL_UPFRONT = "4";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private JournalEntryHelper journalEntryHelper;
    private AccountHelper accountHelper;
    private SchedulerJobHelper schedulerJobHelper;
    private PeriodicAccrualAccountingHelper periodicAccrualAccountingHelper;
    private SavingsAccountHelper savingsAccountHelper;
    private AccountTransferHelper accountTransferHelper;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void checkClientLoanCreateAndDisburseFlow() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct(false, NONE);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, null, null, "12,000.00");
        final ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec,
                loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

    }

    @Test
    public void testLoanCharges_DISBURSEMENT_FEE() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct(false, NONE);

        List<HashMap> charges = new ArrayList<>();
        Integer flatDisbursement = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper.getLoanDisbursementJSON());

        Integer amountPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1"));
        addCharges(charges, amountPercentage, "1", null);
        Integer amountPlusInterestPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1"));
        addCharges(charges, amountPlusInterestPercentage, "1", null);
        Integer interestPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_INTEREST, "1"));
        addCharges(charges, interestPercentage, "1", null);

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap disbursementDetail = loanSchedule.get(0);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);

        validateCharge(amountPercentage, loanCharges, "1.0", "120.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "6.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "126.06", "0.0", "0.0");

        validateNumberForEqual("252.12", String.valueOf(disbursementDetail.get("feeChargesDue")));

        this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getDisbursementChargesForLoanAsJSON(String.valueOf(flatDisbursement)));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        disbursementDetail = loanSchedule.get(0);

        validateCharge(flatDisbursement, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("352.12", String.valueOf(disbursementDetail.get("feeChargesDue")));

        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(amountPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(interestPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(amountPlusInterestPercentage, loanCharges)
                .get("id"), LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(flatDisbursement, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("150"));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        disbursementDetail = loanSchedule.get(0);
        validateCharge(amountPercentage, loanCharges, "2.0", "240.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "2.0", "12.12", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "2.0", "252.12", "0.0", "0.0");
        validateCharge(flatDisbursement, loanCharges, "150.0", "150.0", "0.0", "0.0");
        validateNumberForEqual("654.24", String.valueOf(disbursementDetail.get("feeChargesDue")));

        this.loanTransactionHelper.updateLoan(loanID,
                updateLoanJson(clientID, loanProductID, copyChargesForUpdate(loanCharges, null, null), null));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        disbursementDetail = loanSchedule.get(0);
        validateCharge(amountPercentage, loanCharges, "2.0", "200.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "2.0", "10.1", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "2.0", "210.1", "0.0", "0.0");
        validateCharge(flatDisbursement, loanCharges, "150.0", "150.0", "0.0", "0.0");
        validateNumberForEqual("570.2", String.valueOf(disbursementDetail.get("feeChargesDue")));

        this.loanTransactionHelper.updateLoan(loanID,
                updateLoanJson(clientID, loanProductID, copyChargesForUpdate(loanCharges, flatDisbursement, "1"), null));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        disbursementDetail = loanSchedule.get(0);
        validateCharge(amountPercentage, loanCharges, "1.0", "100.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "5.05", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "105.05", "0.0", "0.0");
        validateNumberForEqual("210.1", String.valueOf(disbursementDetail.get("feeChargesDue")));

        charges.clear();
        addCharges(charges, flatDisbursement, "100", null);
        this.loanTransactionHelper.updateLoan(loanID, updateLoanJson(clientID, loanProductID, charges, null));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        disbursementDetail = loanSchedule.get(0);
        validateCharge(flatDisbursement, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("100.0", String.valueOf(disbursementDetail.get("feeChargesDue")));

        this.loanTransactionHelper.deleteChargesForLoan(loanID, (Integer) getloanCharge(flatDisbursement, loanCharges).get("id"));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        disbursementDetail = loanSchedule.get(0);
        Assert.assertEquals(0, loanCharges.size());
        validateNumberForEqual("0.0", String.valueOf(disbursementDetail.get("feeChargesDue")));

    }

    @Test
    public void testLoanCharges_DISBURSEMENT_FEE_WITH_AMOUNT_CHANGE() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct(false, NONE);

        List<HashMap> charges = new ArrayList<>();
        Integer amountPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1"));
        addCharges(charges, amountPercentage, "1", null);
        Integer amountPlusInterestPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1"));
        addCharges(charges, amountPlusInterestPercentage, "1", null);
        Integer interestPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_INTEREST, "1"));
        addCharges(charges, interestPercentage, "1", null);

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap disbursementDetail = loanSchedule.get(0);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);

        validateCharge(amountPercentage, loanCharges, "1.0", "120.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "6.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "126.06", "0.0", "0.0");
        validateNumberForEqual("252.12", String.valueOf(disbursementDetail.get("feeChargesDue")));

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID, "10000");
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        disbursementDetail = loanSchedule.get(0);

        validateCharge(amountPercentage, loanCharges, "1.0", "0.0", "100.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "0.0", "5.05", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "0.0", "105.05", "0.0");
        validateNumberForEqual("210.1", String.valueOf(disbursementDetail.get("feeChargesDue")));

    }

    @Test
    public void testLoanCharges_SPECIFIED_DUE_DATE_FEE() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct(false, NONE);

        List<HashMap> charges = new ArrayList<>();
        Integer flat = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", false));
        Integer flatAccTransfer = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateWithAccountTransferJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", false));

        Integer amountPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, amountPercentage, "1", "29 September 2011");
        Integer amountPlusInterestPercentage = ChargesHelper
                .createCharges(requestSpec, responseSpec, ChargesHelper.getLoanSpecifiedDueDateJSON(
                        ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentage, "1", "29 September 2011");
        Integer interestPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_INTEREST, "1", false));
        addCharges(charges, interestPercentage, "1", "29 September 2011");

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap firstInstallment = loanSchedule.get(1);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);

        validateCharge(amountPercentage, loanCharges, "1.0", "120.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "6.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "126.06", "0.0", "0.0");

        validateNumberForEqual("252.12", String.valueOf(firstInstallment.get("feeChargesDue")));

        this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flat), "29 September 2011", "100"));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);

        validateCharge(flat, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("352.12", String.valueOf(firstInstallment.get("feeChargesDue")));

        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(amountPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(interestPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(amountPlusInterestPercentage, loanCharges)
                .get("id"), LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(flat, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("150"));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "2.0", "240.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "2.0", "12.12", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "2.0", "252.12", "0.0", "0.0");
        validateCharge(flat, loanCharges, "150.0", "150.0", "0.0", "0.0");
        validateNumberForEqual("654.24", String.valueOf(firstInstallment.get("feeChargesDue")));

        final Integer savingsId = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID,
                MINIMUM_OPENING_BALANCE);
        this.loanTransactionHelper.updateLoan(loanID,
                updateLoanJson(clientID, loanProductID, copyChargesForUpdate(loanCharges, null, null), String.valueOf(savingsId)));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "2.0", "200.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "2.0", "10.1", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "2.0", "210.1", "0.0", "0.0");
        validateCharge(flat, loanCharges, "150.0", "150.0", "0.0", "0.0");
        validateNumberForEqual("570.2", String.valueOf(firstInstallment.get("feeChargesDue")));

        this.loanTransactionHelper.updateLoan(loanID,
                updateLoanJson(clientID, loanProductID, copyChargesForUpdate(loanCharges, flat, "1"), null));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "1.0", "100.0", "0.0", "0.0");
        validateCharge(interestPercentage, loanCharges, "1.0", "5.05", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentage, loanCharges, "1.0", "105.05", "0.0", "0.0");
        validateNumberForEqual("210.1", String.valueOf(firstInstallment.get("feeChargesDue")));

        charges.clear();
        addCharges(charges, flat, "100", "29 September 2011");
        this.loanTransactionHelper.updateLoan(loanID, updateLoanJson(clientID, loanProductID, charges, null));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(flat, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.get("feeChargesDue")));

        this.loanTransactionHelper.deleteChargesForLoan(loanID, (Integer) getloanCharge(flat, loanCharges).get("id"));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        Assert.assertEquals(0, loanCharges.size());
        validateNumberForEqual("0", String.valueOf(firstInstallment.get("feeChargesDue")));

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatAccTransfer), "29 September 2011", "100"));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(flatAccTransfer, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.get("feeChargesDue")));

        // DISBURSE
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(amountPercentage), "29 September 2011", "1"));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "1.0", "100.0", "0.0", "0.0");
        validateCharge(flatAccTransfer, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("200.0", String.valueOf(firstInstallment.get("feeChargesDue")));

        this.loanTransactionHelper.waiveChargesForLoan(loanID, (Integer) getloanCharge(amountPercentage, loanCharges).get("id"), "");
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "1.0", "0.0", "0.0", "100.0");
        validateCharge(flatAccTransfer, loanCharges, "100.0", "100.0", "0.0", "0.0");
        validateNumberForEqual("200.0", String.valueOf(firstInstallment.get("feeChargesDue")));
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.get("feeChargesOutstanding")));
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.get("feeChargesWaived")));

        this.loanTransactionHelper.payChargesForLoan(loanID, (Integer) getloanCharge(flatAccTransfer, loanCharges).get("id"),
                LoanTransactionHelper.getPayChargeJSON(SavingsAccountHelper.TRANSACTION_DATE, null));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        validateCharge(amountPercentage, loanCharges, "1.0", "0.0", "0.0", "100.0");
        validateCharge(flatAccTransfer, loanCharges, "100.0", "0.0", "100.0", "0.0");
        validateNumberForEqual("200.0", String.valueOf(firstInstallment.get("feeChargesDue")));
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.get("feeChargesWaived")));
        validateNumberForEqual("100.0", String.valueOf(firstInstallment.get("feeChargesPaid")));
        validateNumberForEqual("0.0", String.valueOf(firstInstallment.get("feeChargesOutstanding")));
    }

    @Test
    public void testLoanCharges_INSTALMENT_FEE() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct(false, NONE);

        List<HashMap> charges = new ArrayList<>();
        Integer flat = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
        Integer flatAccTransfer = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentWithAccountTransferJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));

        Integer amountPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, amountPercentage, "1", "29 September 2011");
        Integer amountPlusInterestPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentage, "1", "29 September 2011");
        Integer interestPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_INTEREST, "1", false));
        addCharges(charges, interestPercentage, "1", "29 September 2011");

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanSchedule.remove(0);
        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);

        Float totalPerOfAmout = 0F;
        Float totalPerOfAmoutPlusInt = 0F;
        Float totalPerOfint = 0F;
        for (HashMap installment : loanSchedule) {
            Float principalDue = (Float) installment.get("principalDue");
            Float interestDue = (Float) installment.get("interestDue");
            Float principalFee = principalDue / 100;
            Float interestFee = interestDue / 100;
            Float totalInstallmentFee = (principalFee * 2) + (interestFee * 2);
            validateNumberForEqualExcludePrecission(String.valueOf(totalInstallmentFee), String.valueOf(installment.get("feeChargesDue")));
            totalPerOfAmout = totalPerOfAmout + principalFee;
            totalPerOfAmoutPlusInt = totalPerOfAmoutPlusInt + principalFee + interestFee;
            totalPerOfint = totalPerOfint + interestFee;
        }

        validateChargeExcludePrecission(amountPercentage, loanCharges, "1.0", String.valueOf(totalPerOfAmout), "0.0", "0.0");
        validateChargeExcludePrecission(interestPercentage, loanCharges, "1.0", String.valueOf(totalPerOfint), "0.0", "0.0");
        validateChargeExcludePrecission(amountPlusInterestPercentage, loanCharges, "1.0", String.valueOf(totalPerOfAmoutPlusInt), "0.0",
                "0.0");

        this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getInstallmentChargesForLoanAsJSON(String.valueOf(flat), "50"));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanSchedule.remove(0);
        totalPerOfAmout = 0F;
        totalPerOfAmoutPlusInt = 0F;
        totalPerOfint = 0F;
        for (HashMap installment : loanSchedule) {
            Float principalDue = (Float) installment.get("principalDue");
            Float interestDue = (Float) installment.get("interestDue");
            Float principalFee = principalDue / 100;
            Float interestFee = interestDue / 100;
            Float totalInstallmentFee = (principalFee * 2) + (interestFee * 2) + 50;
            validateNumberForEqualExcludePrecission(String.valueOf(totalInstallmentFee), String.valueOf(installment.get("feeChargesDue")));
            totalPerOfAmout = totalPerOfAmout + principalFee;
            totalPerOfAmoutPlusInt = totalPerOfAmoutPlusInt + principalFee + interestFee;
            totalPerOfint = totalPerOfint + interestFee;
        }

        validateChargeExcludePrecission(amountPercentage, loanCharges, "1.0", String.valueOf(totalPerOfAmout), "0.0", "0.0");
        validateChargeExcludePrecission(interestPercentage, loanCharges, "1.0", String.valueOf(totalPerOfint), "0.0", "0.0");
        validateChargeExcludePrecission(amountPlusInterestPercentage, loanCharges, "1.0", String.valueOf(totalPerOfAmoutPlusInt), "0.0",
                "0.0");
        validateChargeExcludePrecission(flat, loanCharges, "50.0", "200", "0.0", "0.0");

        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(amountPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(interestPercentage, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(amountPlusInterestPercentage, loanCharges)
                .get("id"), LoanTransactionHelper.getUpdateChargesForLoanAsJSON("2"));
        this.loanTransactionHelper.updateChargesForLoan(loanID, (Integer) getloanCharge(flat, loanCharges).get("id"),
                LoanTransactionHelper.getUpdateChargesForLoanAsJSON("100"));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanSchedule.remove(0);
        totalPerOfAmout = 0F;
        totalPerOfAmoutPlusInt = 0F;
        totalPerOfint = 0F;
        for (HashMap installment : loanSchedule) {
            Float principalDue = (Float) installment.get("principalDue");
            Float interestDue = (Float) installment.get("interestDue");
            Float principalFee = principalDue * 2 / 100;
            Float interestFee = interestDue * 2 / 100;
            Float totalInstallmentFee = (principalFee * 2) + (interestFee * 2) + 100;
            validateNumberForEqualExcludePrecission(String.valueOf(totalInstallmentFee), String.valueOf(installment.get("feeChargesDue")));
            totalPerOfAmout = totalPerOfAmout + principalFee;
            totalPerOfAmoutPlusInt = totalPerOfAmoutPlusInt + principalFee + interestFee;
            totalPerOfint = totalPerOfint + interestFee;
        }

        validateChargeExcludePrecission(amountPercentage, loanCharges, "2.0", String.valueOf(totalPerOfAmout), "0.0", "0.0");
        validateChargeExcludePrecission(interestPercentage, loanCharges, "2.0", String.valueOf(totalPerOfint), "0.0", "0.0");
        validateChargeExcludePrecission(amountPlusInterestPercentage, loanCharges, "2.0", String.valueOf(totalPerOfAmoutPlusInt), "0.0",
                "0.0");
        validateChargeExcludePrecission(flat, loanCharges, "100.0", "400", "0.0", "0.0");

        final Integer savingsId = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID,
                MINIMUM_OPENING_BALANCE);
        this.loanTransactionHelper.updateLoan(loanID,
                updateLoanJson(clientID, loanProductID, copyChargesForUpdate(loanCharges, null, null), String.valueOf(savingsId)));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanSchedule.remove(0);
        totalPerOfAmout = 0F;
        totalPerOfAmoutPlusInt = 0F;
        totalPerOfint = 0F;
        for (HashMap installment : loanSchedule) {
            Float principalDue = (Float) installment.get("principalDue");
            Float interestDue = (Float) installment.get("interestDue");
            Float principalFee = principalDue * 2 / 100;
            Float interestFee = interestDue * 2 / 100;
            Float totalInstallmentFee = (principalFee * 2) + (interestFee * 2) + 100;
            validateNumberForEqualExcludePrecission(String.valueOf(totalInstallmentFee), String.valueOf(installment.get("feeChargesDue")));
            totalPerOfAmout = totalPerOfAmout + principalFee;
            totalPerOfAmoutPlusInt = totalPerOfAmoutPlusInt + principalFee + interestFee;
            totalPerOfint = totalPerOfint + interestFee;
        }

        validateChargeExcludePrecission(amountPercentage, loanCharges, "2.0", String.valueOf(totalPerOfAmout), "0.0", "0.0");
        validateChargeExcludePrecission(interestPercentage, loanCharges, "2.0", String.valueOf(totalPerOfint), "0.0", "0.0");
        validateChargeExcludePrecission(amountPlusInterestPercentage, loanCharges, "2.0", String.valueOf(totalPerOfAmoutPlusInt), "0.0",
                "0.0");
        validateChargeExcludePrecission(flat, loanCharges, "100.0", "400", "0.0", "0.0");

        this.loanTransactionHelper.updateLoan(loanID,
                updateLoanJson(clientID, loanProductID, copyChargesForUpdate(loanCharges, flat, "1"), null));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanSchedule.remove(0);
        totalPerOfAmout = 0F;
        totalPerOfAmoutPlusInt = 0F;
        totalPerOfint = 0F;
        for (HashMap installment : loanSchedule) {
            Float principalDue = (Float) installment.get("principalDue");
            Float interestDue = (Float) installment.get("interestDue");
            Float principalFee = principalDue / 100;
            Float interestFee = interestDue / 100;
            Float totalInstallmentFee = (principalFee * 2) + (interestFee * 2);
            validateNumberForEqualExcludePrecission(String.valueOf(totalInstallmentFee), String.valueOf(installment.get("feeChargesDue")));
            totalPerOfAmout = totalPerOfAmout + principalFee;
            totalPerOfAmoutPlusInt = totalPerOfAmoutPlusInt + principalFee + interestFee;
            totalPerOfint = totalPerOfint + interestFee;
        }

        validateChargeExcludePrecission(amountPercentage, loanCharges, "1.0", String.valueOf(totalPerOfAmout), "0.0", "0.0");
        validateChargeExcludePrecission(interestPercentage, loanCharges, "1.0", String.valueOf(totalPerOfint), "0.0", "0.0");
        validateChargeExcludePrecission(amountPlusInterestPercentage, loanCharges, "1.0", String.valueOf(totalPerOfAmoutPlusInt), "0.0",
                "0.0");

        charges.clear();
        addCharges(charges, flat, "50", "29 September 2011");
        this.loanTransactionHelper.updateLoan(loanID, updateLoanJson(clientID, loanProductID, charges, null));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("50", String.valueOf(installment.get("feeChargesDue")));
        }
        validateChargeExcludePrecission(flat, loanCharges, "50.0", "200", "0.0", "0.0");

        this.loanTransactionHelper.deleteChargesForLoan(loanID, (Integer) getloanCharge(flat, loanCharges).get("id"));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("0", String.valueOf(installment.get("feeChargesDue")));
        }

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getInstallmentChargesForLoanAsJSON(String.valueOf(flatAccTransfer), "100"));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("100", String.valueOf(installment.get("feeChargesDue")));
        }
        validateChargeExcludePrecission(flatAccTransfer, loanCharges, "100.0", "400", "0.0", "0.0");

        // DISBURSE
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getInstallmentChargesForLoanAsJSON(String.valueOf(flat), "50"));

        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("150", String.valueOf(installment.get("feeChargesDue")));
        }
        validateChargeExcludePrecission(flatAccTransfer, loanCharges, "100.0", "400", "0.0", "0.0");
        validateChargeExcludePrecission(flat, loanCharges, "50.0", "200", "0.0", "0.0");

        Integer waivePeriodnum = 1;
        this.loanTransactionHelper.waiveChargesForLoan(loanID, (Integer) getloanCharge(flat, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(waivePeriodnum)));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("150", String.valueOf(installment.get("feeChargesDue")));
            if (waivePeriodnum == installment.get("period")) {
                validateNumberForEqualExcludePrecission("100.0", String.valueOf(installment.get("feeChargesOutstanding")));
                validateNumberForEqualExcludePrecission("50.0", String.valueOf(installment.get("feeChargesWaived")));
            } else {
                validateNumberForEqualExcludePrecission("150.0", String.valueOf(installment.get("feeChargesOutstanding")));
                validateNumberForEqualExcludePrecission("0.0", String.valueOf(installment.get("feeChargesWaived")));

            }
        }
        validateChargeExcludePrecission(flatAccTransfer, loanCharges, "100.0", "400", "0.0", "0.0");
        validateChargeExcludePrecission(flat, loanCharges, "50.0", "150", "0.0", "50.0");

        Integer payPeriodnum = 2;
        this.loanTransactionHelper.payChargesForLoan(loanID, (Integer) getloanCharge(flatAccTransfer, loanCharges).get("id"),
                LoanTransactionHelper.getPayChargeJSON(SavingsAccountHelper.TRANSACTION_DATE, String.valueOf(payPeriodnum)));
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanSchedule.remove(0);
        for (HashMap installment : loanSchedule) {
            validateNumberForEqualExcludePrecission("150", String.valueOf(installment.get("feeChargesDue")));
            if (payPeriodnum == installment.get("period")) {
                validateNumberForEqualExcludePrecission("50.0", String.valueOf(installment.get("feeChargesOutstanding")));
                validateNumberForEqualExcludePrecission("100.0", String.valueOf(installment.get("feeChargesPaid")));
            } else if (waivePeriodnum == installment.get("period")) {
                validateNumberForEqualExcludePrecission("100.0", String.valueOf(installment.get("feeChargesOutstanding")));
                validateNumberForEqualExcludePrecission("50.0", String.valueOf(installment.get("feeChargesWaived")));
            } else {
                validateNumberForEqualExcludePrecission("150.0", String.valueOf(installment.get("feeChargesOutstanding")));
                validateNumberForEqualExcludePrecission("0.0", String.valueOf(installment.get("feeChargesPaid")));

            }
        }
        validateChargeExcludePrecission(flatAccTransfer, loanCharges, "100.0", "300", "100.0", "0.0");
        validateChargeExcludePrecission(flat, loanCharges, "50.0", "150", "0.0", "50.0");

    }

    @Test
    public void testLoanCharges_DISBURSEMENT_TO_SAVINGS() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        SavingsAccountHelper savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct(false, NONE);

        final Integer savingsId = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID,
                MINIMUM_OPENING_BALANCE);

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, null, savingsId.toString(), "12,000.00");
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        HashMap summary = savingsAccountHelper.getSavingsSummary(savingsId);
        Float balance = new Float(MINIMUM_OPENING_BALANCE);
        assertEquals("Verifying opening Balance", balance, summary.get("accountBalance"));

        // DISBURSE
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanToSavings(SavingsAccountHelper.TRANSACTION_DATE, loanID);
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        summary = savingsAccountHelper.getSavingsSummary(savingsId);
        balance = new Float(MINIMUM_OPENING_BALANCE) + new Float("12000");
        assertEquals("Verifying opening Balance", balance, summary.get("accountBalance"));

        loanStatusHashMap = this.loanTransactionHelper.undoDisbursal(loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        summary = savingsAccountHelper.getSavingsSummary(savingsId);
        balance = new Float(MINIMUM_OPENING_BALANCE);
        assertEquals("Verifying opening Balance", balance, summary.get("accountBalance"));

    }

    @Test
    public void testLoanCharges_DISBURSEMENT_WITH_TRANCHES() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct(true, NONE);

        List<HashMap> tranches = new ArrayList<>();
        tranches.add(createTrancheDetail("1 March 2014", "25000"));
        tranches.add(createTrancheDetail("23 April 2014", "20000"));

        final Integer loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, null, null, "45,000.00", tranches);
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("1 March 2014", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE first Tranche
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("1 March 2014", loanID);
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // DISBURSE Second Tranche
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("23 April 2014", loanID);
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.undoDisbursal(loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

    }

    @Test
    public void testLoanCharges_DISBURSEMENT_TO_SAVINGS_WITH_TRANCHES() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        SavingsAccountHelper savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct(true, NONE);

        final Integer savingsId = SavingsAccountHelper.openSavingsAccount(this.requestSpec, this.responseSpec, clientID,
                MINIMUM_OPENING_BALANCE);

        List<HashMap> tranches = new ArrayList<>();
        tranches.add(createTrancheDetail("1 March 2014", "25000"));
        tranches.add(createTrancheDetail("23 April 2014", "20000"));

        final Integer loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, null, savingsId.toString(), "45,000.00",
                tranches);
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("1 March 2014", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        HashMap summary = savingsAccountHelper.getSavingsSummary(savingsId);
        Float balance = new Float(MINIMUM_OPENING_BALANCE);
        assertEquals("Verifying opening Balance", balance, summary.get("accountBalance"));

        // DISBURSE first Tranche
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanToSavings("1 March 2014", loanID);
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        summary = savingsAccountHelper.getSavingsSummary(savingsId);
        balance = new Float(MINIMUM_OPENING_BALANCE) + new Float("25000");
        assertEquals("Verifying opening Balance", balance, summary.get("accountBalance"));

        // DISBURSE Second Tranche
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanToSavings("23 April 2014", loanID);
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        summary = savingsAccountHelper.getSavingsSummary(savingsId);
        balance = new Float(MINIMUM_OPENING_BALANCE) + new Float("25000") + new Float("20000");
        assertEquals("Verifying opening Balance", balance, summary.get("accountBalance"));

        loanStatusHashMap = this.loanTransactionHelper.undoDisbursal(loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        summary = savingsAccountHelper.getSavingsSummary(savingsId);
        balance = new Float(MINIMUM_OPENING_BALANCE);
        assertEquals("Verifying opening Balance", balance, summary.get("accountBalance"));

    }

    private void validateCharge(Integer amountPercentage, final List<HashMap> loanCharges, final String amount, final String outstanding,
            String amountPaid, String amountWaived) {
        HashMap chargeDetail = getloanCharge(amountPercentage, loanCharges);
        Assert.assertTrue(new Float(amount).compareTo(new Float(String.valueOf(chargeDetail.get("amountOrPercentage")))) == 0);
        Assert.assertTrue(new Float(outstanding).compareTo(new Float(String.valueOf(chargeDetail.get("amountOutstanding")))) == 0);
        Assert.assertTrue(new Float(amountPaid).compareTo(new Float(String.valueOf(chargeDetail.get("amountPaid")))) == 0);
        Assert.assertTrue(new Float(amountWaived).compareTo(new Float(String.valueOf(chargeDetail.get("amountWaived")))) == 0);
    }

    private void validateChargeExcludePrecission(Integer amountPercentage, final List<HashMap> loanCharges, final String amount,
            final String outstanding, String amountPaid, String amountWaived) {
        DecimalFormat twoDForm = new DecimalFormat("#");
        HashMap chargeDetail = getloanCharge(amountPercentage, loanCharges);
        Assert.assertTrue(new Float(twoDForm.format(new Float(amount))).compareTo(new Float(twoDForm.format(new Float(String
                .valueOf(chargeDetail.get("amountOrPercentage")))))) == 0);
        Assert.assertTrue(new Float(twoDForm.format(new Float(outstanding))).compareTo(new Float(twoDForm.format(new Float(String
                .valueOf(chargeDetail.get("amountOutstanding")))))) == 0);
        Assert.assertTrue(new Float(twoDForm.format(new Float(amountPaid))).compareTo(new Float(twoDForm.format(new Float(String
                .valueOf(chargeDetail.get("amountPaid")))))) == 0);
        Assert.assertTrue(new Float(twoDForm.format(new Float(amountWaived))).compareTo(new Float(twoDForm.format(new Float(String
                .valueOf(chargeDetail.get("amountWaived")))))) == 0);
    }

    public void validateNumberForEqual(String val, String val2) {
        Assert.assertTrue(new Float(val).compareTo(new Float(val2)) == 0);
    }

    public void validateNumberForEqualWithMsg(String msg, String val, String val2) {
        Assert.assertTrue(msg + "expected " + val + " but was " + val2, new Float(val).compareTo(new Float(val2)) == 0);
    }

    public void validateNumberForEqualExcludePrecission(String val, String val2) {
        DecimalFormat twoDForm = new DecimalFormat("#");
        Assert.assertTrue(new Float(twoDForm.format(new Float(val))).compareTo(new Float(twoDForm.format(new Float(val2)))) == 0);
    }

    private Integer createLoanProduct(final boolean multiDisburseLoan, final String accountingRule, final Account... accounts) {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        LoanProductTestBuilder builder = new LoanProductTestBuilder() //
                .withPrincipal("12,000.00") //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withTranches(multiDisburseLoan) //
                .withAccounting(accountingRule, accounts);
        if (multiDisburseLoan) {
            builder = builder.withInterestCalculationPeriodTypeAsRepaymentPeriod(true);
        }
        final String loanProductJSON = builder.build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer createLoanProduct(final String inMultiplesOf, final String digitsAfterDecimal, final String repaymentStrategy) {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("10000000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withRepaymentStrategy(repaymentStrategy) //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails(digitsAfterDecimal, inMultiplesOf).build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer createLoanProduct(final String inMultiplesOf, final String digitsAfterDecimal, final String repaymentStrategy,
            final String accountingRule, final Account... accounts) {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("10000000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withRepaymentStrategy(repaymentStrategy) //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails(digitsAfterDecimal, inMultiplesOf).withAccounting(accountingRule, accounts).build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, String graceOnPrincipalPayment) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal("10000000.00") //
                .withLoanTermFrequency("24") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("24") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withAmortizationTypeAsEqualPrincipalPayments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withPrincipalGrace(graceOnPrincipalPayment).withExpectedDisbursementDate("2 June 2014") //
                .withSubmittedOnDate("2 June 2014") //
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, List<HashMap> charges,
            final String savingsId, String principal) {
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
                .withExpectedDisbursementDate("20 September 2011") //
                .withSubmittedOnDate("20 September 2011") //
                .withCharges(charges).build(clientID.toString(), loanProductID.toString(), savingsId);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private Integer applyForLoanApplicationWithTranches(final Integer clientID, final Integer loanProductID, List<HashMap> charges,
            final String savingsId, String principal, List<HashMap> tranches) {
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
                .withExpectedDisbursementDate("1 March 2014") //
                .withTranches(tranches) //
                .withSubmittedOnDate("1 March 2014") //

                .withCharges(charges).build(clientID.toString(), loanProductID.toString(), savingsId);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private String updateLoanJson(final Integer clientID, final Integer loanProductID, List<HashMap> charges, String savingsId) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal("10,000.00") //
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
                .withCharges(charges).build(clientID.toString(), loanProductID.toString(), savingsId);
        return loanApplicationJSON;
    }

    private Integer applyForLoanApplicationWithPaymentStrategy(final Integer clientID, final Integer loanProductID, List<HashMap> charges,
            final String savingsId, String principal, final String repaymentStrategy) {
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
                .withExpectedDisbursementDate("20 September 2011") //
                .withSubmittedOnDate("20 September 2011") //
                .withwithRepaymentStrategy(repaymentStrategy) //
                .withCharges(charges).build(clientID.toString(), loanProductID.toString(), savingsId);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private Integer applyForLoanApplicationWithPaymentStrategyAndPastMonth(final Integer clientID, final Integer loanProductID,
            List<HashMap> charges, final String savingsId, String principal, final String repaymentStrategy, final int month) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");

        Calendar fourMonthsfromNowCalendar = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        fourMonthsfromNowCalendar.add(Calendar.MONTH, month);
        DateFormat dateFormat = new SimpleDateFormat("dd MMMMMM yyyy");
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());
        String fourMonthsfromNow = dateFormat.format(fourMonthsfromNowCalendar.getTime());
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withLoanTermFrequency("6") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("6") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsFlatBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate(fourMonthsfromNow) //
                .withSubmittedOnDate(fourMonthsfromNow) //
                .withwithRepaymentStrategy(repaymentStrategy) //
                .withCharges(charges).build(clientID.toString(), loanProductID.toString(), savingsId);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private void verifyLoanRepaymentSchedule(final ArrayList<HashMap> loanSchedule) {
        System.out.println("--------------------VERIFYING THE PRINCIPAL DUES,INTEREST DUE AND DUE DATE--------------------------");

        assertEquals("Checking for Due Date for 1st Month", new ArrayList<>(Arrays.asList(2011, 10, 20)), loanSchedule.get(1)
                .get("dueDate"));
        assertEquals("Checking for Principal Due for 1st Month", new Float("2911.49"), loanSchedule.get(1).get("principalOriginalDue"));
        assertEquals("Checking for Interest Due for 1st Month", new Float("240.00"), loanSchedule.get(1).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 2nd Month", new ArrayList<>(Arrays.asList(2011, 11, 20)), loanSchedule.get(2)
                .get("dueDate"));
        assertEquals("Checking for Principal Due for 2nd Month", new Float("2969.72"), loanSchedule.get(2).get("principalDue"));
        assertEquals("Checking for Interest Due for 2nd Month", new Float("181.77"), loanSchedule.get(2).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 3rd Month", new ArrayList<>(Arrays.asList(2011, 12, 20)), loanSchedule.get(3)
                .get("dueDate"));
        assertEquals("Checking for Principal Due for 3rd Month", new Float("3029.11"), loanSchedule.get(3).get("principalDue"));
        assertEquals("Checking for Interest Due for 3rd Month", new Float("122.38"), loanSchedule.get(3).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 4th Month", new ArrayList<>(Arrays.asList(2012, 1, 20)), loanSchedule.get(4).get("dueDate"));
        assertEquals("Checking for Principal Due for 4th Month", new Float("3089.68"), loanSchedule.get(4).get("principalDue"));
        assertEquals("Checking for Interest Due for 4th Month", new Float("61.79"), loanSchedule.get(4).get("interestOriginalDue"));
    }

    private void verifyLoanRepaymentScheduleForEqualPrincipal(final ArrayList<HashMap> loanSchedule) {
        System.out.println("--------------------VERIFYING THE PRINCIPAL DUES,INTEREST DUE AND DUE DATE--------------------------");

        assertEquals("Checking for Due Date for 1st Month", new ArrayList<>(Arrays.asList(2014, 7, 2)), loanSchedule.get(1).get("dueDate"));
        assertEquals("Checking for Principal Due for 1st Month", new Float("416700"), loanSchedule.get(1).get("principalOriginalDue"));
        assertEquals("Checking for Interest Due for 1st Month", new Float("200000"), loanSchedule.get(1).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 2nd Month", new ArrayList<>(Arrays.asList(2014, 8, 2)), loanSchedule.get(2).get("dueDate"));
        assertEquals("Checking for Principal Due for 2nd Month", new Float("416700"), loanSchedule.get(2).get("principalDue"));
        assertEquals("Checking for Interest Due for 2nd Month", new Float("191700"), loanSchedule.get(2).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 3rd Month", new ArrayList<>(Arrays.asList(2014, 9, 2)), loanSchedule.get(3).get("dueDate"));
        assertEquals("Checking for Principal Due for 3rd Month", new Float("416700"), loanSchedule.get(3).get("principalDue"));
        assertEquals("Checking for Interest Due for 3rd Month", new Float("183300"), loanSchedule.get(3).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 4th Month", new ArrayList<>(Arrays.asList(2014, 10, 2)), loanSchedule.get(4).get("dueDate"));
        assertEquals("Checking for Principal Due for 4th Month", new Float("416700"), loanSchedule.get(4).get("principalDue"));
        assertEquals("Checking for Interest Due for 4th Month", new Float("175000"), loanSchedule.get(4).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 5th Month", new ArrayList<>(Arrays.asList(2014, 11, 2)), loanSchedule.get(5).get("dueDate"));
        assertEquals("Checking for Principal Due for 5th Month", new Float("416700"), loanSchedule.get(5).get("principalDue"));
        assertEquals("Checking for Interest Due for 5th Month", new Float("166700"), loanSchedule.get(5).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 6th Month", new ArrayList<>(Arrays.asList(2014, 12, 2)), loanSchedule.get(6).get("dueDate"));
        assertEquals("Checking for Principal Due for 6th Month", new Float("416700"), loanSchedule.get(6).get("principalDue"));
        assertEquals("Checking for Interest Due for 6th Month", new Float("158300"), loanSchedule.get(6).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 10th Month", new ArrayList<>(Arrays.asList(2015, 4, 2)), loanSchedule.get(10)
                .get("dueDate"));
        assertEquals("Checking for Principal Due for 10th Month", new Float("416700"), loanSchedule.get(10).get("principalDue"));
        assertEquals("Checking for Interest Due for 10th Month", new Float("125000"), loanSchedule.get(10).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 20th Month", new ArrayList<>(Arrays.asList(2016, 2, 2)), loanSchedule.get(20)
                .get("dueDate"));
        assertEquals("Checking for Principal Due for 20th Month", new Float("416700"), loanSchedule.get(20).get("principalDue"));
        assertEquals("Checking for Interest Due for 20th Month", new Float("41700"), loanSchedule.get(20).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 24th Month", new ArrayList<>(Arrays.asList(2016, 6, 2)), loanSchedule.get(24)
                .get("dueDate"));
        assertEquals("Checking for Principal Due for 24th Month", new Float("415900"), loanSchedule.get(24).get("principalDue"));
        assertEquals("Checking for Interest Due for 24th Month", new Float("8300"), loanSchedule.get(24).get("interestOriginalDue"));

    }

    private void verifyLoanRepaymentScheduleForEqualPrincipalWithGrace(final ArrayList<HashMap> loanSchedule) {
        System.out.println("--------------------VERIFYING THE PRINCIPAL DUES,INTEREST DUE AND DUE DATE--------------------------");

        assertEquals("Checking for Due Date for 1st Month", new ArrayList<>(Arrays.asList(2014, 7, 2)), loanSchedule.get(1).get("dueDate"));
        validateNumberForEqualWithMsg("Checking for Principal Due for 1st Month", "0.0",
                String.valueOf(loanSchedule.get(1).get("principalOriginalDue")));
        assertEquals("Checking for Interest Due for 1st Month", new Float("200000"), loanSchedule.get(1).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 2nd Month", new ArrayList<>(Arrays.asList(2014, 8, 2)), loanSchedule.get(2).get("dueDate"));
        validateNumberForEqualWithMsg("Checking for Principal Due for 2nd Month", "0.0",
                String.valueOf(loanSchedule.get(2).get("principalOriginalDue")));
        assertEquals("Checking for Interest Due for 2nd Month", new Float("200000"), loanSchedule.get(2).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 3rd Month", new ArrayList<>(Arrays.asList(2014, 9, 2)), loanSchedule.get(3).get("dueDate"));
        validateNumberForEqualWithMsg("Checking for Principal Due for 3rd Month", "0.0",
                String.valueOf(loanSchedule.get(3).get("principalDue")));
        assertEquals("Checking for Interest Due for 3rd Month", new Float("200000"), loanSchedule.get(3).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 4th Month", new ArrayList<>(Arrays.asList(2014, 10, 2)), loanSchedule.get(4).get("dueDate"));
        validateNumberForEqualWithMsg("Checking for Principal Due for 4th Month", "0",
                String.valueOf(loanSchedule.get(4).get("principalDue")));
        assertEquals("Checking for Interest Due for 4th Month", new Float("200000"), loanSchedule.get(4).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 5th Month", new ArrayList<>(Arrays.asList(2014, 11, 2)), loanSchedule.get(5).get("dueDate"));
        validateNumberForEqualWithMsg("Checking for Principal Due for 5th Month", "0",
                String.valueOf(loanSchedule.get(5).get("principalDue")));
        assertEquals("Checking for Interest Due for 5th Month", new Float("200000"), loanSchedule.get(5).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 6th Month", new ArrayList<>(Arrays.asList(2014, 12, 2)), loanSchedule.get(6).get("dueDate"));
        assertEquals("Checking for Principal Due for 6th Month", new Float("526300"), loanSchedule.get(6).get("principalDue"));
        assertEquals("Checking for Interest Due for 6th Month", new Float("200000"), loanSchedule.get(6).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 7th Month", new ArrayList<>(Arrays.asList(2015, 1, 2)), loanSchedule.get(7).get("dueDate"));
        assertEquals("Checking for Principal Due for 7th Month", new Float("526300"), loanSchedule.get(7).get("principalDue"));
        assertEquals("Checking for Interest Due for 7th Month", new Float("189500"), loanSchedule.get(7).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 10th Month", new ArrayList<>(Arrays.asList(2015, 4, 2)), loanSchedule.get(10)
                .get("dueDate"));
        assertEquals("Checking for Principal Due for 10th Month", new Float("526300"), loanSchedule.get(10).get("principalDue"));
        assertEquals("Checking for Interest Due for 10th Month", new Float("157900"), loanSchedule.get(10).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 20th Month", new ArrayList<>(Arrays.asList(2016, 2, 2)), loanSchedule.get(20)
                .get("dueDate"));
        assertEquals("Checking for Principal Due for 20th Month", new Float("526300"), loanSchedule.get(20).get("principalDue"));
        assertEquals("Checking for Interest Due for 20th Month", new Float("52600"), loanSchedule.get(20).get("interestOriginalDue"));

        assertEquals("Checking for Due Date for 24th Month", new ArrayList<>(Arrays.asList(2016, 6, 2)), loanSchedule.get(24)
                .get("dueDate"));
        assertEquals("Checking for Principal Due for 24th Month", new Float("526600"), loanSchedule.get(24).get("principalDue"));
        assertEquals("Checking for Interest Due for 24th Month", new Float("10500"), loanSchedule.get(24).get("interestOriginalDue"));

    }

    private void addCharges(List<HashMap> charges, Integer chargeId, String amount, String duedate) {
        charges.add(charges(chargeId, amount, duedate));
    }

    private HashMap charges(Integer chargeId, String amount, String duedate) {
        HashMap charge = new HashMap(2);
        charge.put("chargeId", chargeId.toString());
        charge.put("amount", amount);
        if (duedate != null) {
            charge.put("dueDate", duedate);
        }
        return charge;
    }

    private HashMap getloanCharge(Integer chargeId, List<HashMap> charges) {
        HashMap charge = null;
        for (HashMap loancharge : charges) {
            if (loancharge.get("chargeId").equals(chargeId)) {
                charge = loancharge;
            }
        }
        return charge;
    }

    private List<HashMap> copyChargesForUpdate(List<HashMap> charges, Integer deleteWithChargeId, String amount) {
        List<HashMap> loanCharges = new ArrayList<>();
        for (HashMap charge : charges) {
            if (!charge.get("chargeId").equals(deleteWithChargeId)) {
                loanCharges.add(copyForUpdate(charge, amount));
            }
        }
        return loanCharges;
    }

    private HashMap copyForUpdate(HashMap charge, String amount) {
        HashMap map = new HashMap();
        map.put("id", charge.get("id"));
        if (amount == null) {
            map.put("amount", charge.get("amountOrPercentage"));
        } else {
            map.put("amount", amount);
        }
        if (charge.get("dueDate") != null) {
            map.put("dueDate", charge.get("dueDate"));
        }
        map.put("chargeId", charge.get("chargeId"));
        return map;
    }

    private HashMap createTrancheDetail(final String date, final String amount) {
        HashMap detail = new HashMap();
        detail.put("expectedDisbursementDate", date);
        detail.put("principal", amount);

        return detail;
    }

    /***
     * Test case for checking CashBasedAccounting functionality adding charges
     * with calculation type flat
     */
    @Test
    public void loanWithFlatCahargesAndCashBasedAccountingEnabled() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();
        Integer flatDisbursement = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper.getLoanDisbursementJSON());
        addCharges(charges, flatDisbursement, "100", null);
        Integer flatSpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", false));
        addCharges(charges, flatSpecifiedDueDate, "100", "29 September 2011");
        Integer flatInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
        addCharges(charges, flatInstallmentFee, "50", null);

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct(false, CASH_BASED, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "100.00", "0.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "100.00", "0.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "200.00", "0.0", "0.0");

        // check for disbursement fee
        HashMap disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("100.00", String.valueOf(disbursementDetail.get("feeChargesDue")));

        // check for charge at specified date and installment fee
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("150.00", String.valueOf(firstInstallment.get("feeChargesDue")));

        // check for installment fee
        HashMap secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("50.00", String.valueOf(secondInstallment.get("feeChargesDue")));

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.CREDIT));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");

        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("20 October 2011", Float.valueOf("3301.49"), loanID);
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "150.00", "50.0", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3301.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2911.49"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                new JournalEntry(Float.valueOf("150.00"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("240.00"),
                        JournalEntry.TransactionType.CREDIT));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatSpecifiedDueDate), "29 October 2011", "100"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("150.00", String.valueOf(secondInstallment.get("feeChargesDue")));
        this.loanTransactionHelper.waiveChargesForLoan(loanID, (Integer) getloanCharge(flatInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatInstallmentFee, loanCharges, "50", "100.00", "50.0", "50.0");

        System.out.println("----------Make repayment 2------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3251.49"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3251.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2969.72"),
                        JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("181.77"),
                        JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        System.out.println("--------------Waive interest---------------");
        this.loanTransactionHelper.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        Integer flatPenaltySpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", true));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatPenaltySpecifiedDueDate), "29 September 2011", "100"));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatPenaltySpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("100", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3301.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2811.49"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("150.00"),
                        JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("240"), JournalEntry.TransactionType.CREDIT));

        System.out.println("----------Make repayment 3 advance------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3301.49"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3301.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3129.11"),
                        JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("50.00"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("122.38"),
                        JournalEntry.TransactionType.CREDIT));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatPenaltySpecifiedDueDate), "10 January 2012", "100"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("100", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3239.68", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Pay applied penalty ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("100"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(Float.valueOf("100"),
                JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012",
                new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3139.68", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Make repayment 4 ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("3139.68"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("3139.68"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3089.68"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("50.00"), JournalEntry.TransactionType.CREDIT));
    }

    /***
     * Test case for checking CashBasedAccounting functionality adding charges
     * with calculation type percentage of amount
     */
    @Test
    public void loanWithCahargesOfTypeAmountPercentageAndCashBasedAccountingEnabled() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();
        Integer percentageDisbursementCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1"));
        addCharges(charges, percentageDisbursementCharge, "1", null);

        Integer percentageSpecifiedDueDateCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, percentageSpecifiedDueDateCharge, "1", "29 September 2011");

        Integer percentageInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, percentageInstallmentFee, "1", "29 September 2011");

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct(false, CASH_BASED, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "120.00", "0.0", "0.0");
        validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "120.00", "0.0", "0.0");
        validateCharge(percentageInstallmentFee, loanCharges, "1", "120.00", "0.0", "0.0");

        // check for disbursement fee
        HashMap disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("120.00", String.valueOf(disbursementDetail.get("feeChargesDue")));

        // check for charge at specified date and installment fee
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("149.11", String.valueOf(firstInstallment.get("feeChargesDue")));

        // check for installment fee
        HashMap secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("29.70", String.valueOf(secondInstallment.get("feeChargesDue")));

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.CREDIT));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.0", "120.00", "0.0");

        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("20 October 2011", Float.valueOf("3300.60"), loanID);
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.00", "120.00", "0.0");
        validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "120.0", "0.0");
        validateCharge(percentageInstallmentFee, loanCharges, "1", "90.89", "29.11", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3300.60"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2911.49"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                new JournalEntry(Float.valueOf("149.11"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("240.00"),
                        JournalEntry.TransactionType.CREDIT));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(percentageSpecifiedDueDateCharge), "29 October 2011", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("149.70", String.valueOf(secondInstallment.get("feeChargesDue")));
        this.loanTransactionHelper.waiveChargesForLoan(loanID, (Integer) getloanCharge(percentageInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageInstallmentFee, loanCharges, "1", "61.19", "29.11", "29.70");

        System.out.println("----------Make repayment 2------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3271.49"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3271.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2969.72"),
                        JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("181.77"),
                        JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        System.out.println("--------------Waive interest---------------");
        this.loanTransactionHelper.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        Integer percentagePenaltySpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", true));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(percentagePenaltySpecifiedDueDate), "29 September 2011", "1"));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentagePenaltySpecifiedDueDate, loanCharges, "1", "0.00", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3300.60"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2791.49"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("149.11"),
                        JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("240"), JournalEntry.TransactionType.CREDIT));

        System.out.println("----------Make repayment 3 advance------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3301.78"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3301.78"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3149.11"),
                        JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("30.29"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("122.38"),
                        JournalEntry.TransactionType.CREDIT));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(percentagePenaltySpecifiedDueDate), "10 January 2012", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3240.58", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Pay applied penalty ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("120"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(Float.valueOf("120"),
                JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012",
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3120.58", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Make repayment 4 ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("3120.58"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("3120.58"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3089.68"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("30.90"), JournalEntry.TransactionType.CREDIT));
    }

    /***
     * Test case for checking CashBasedAccounting functionality adding charges
     * with calculation type percentage of amount plus interest
     */
    @Test
    public void loanWithCahargesOfTypeAmountPlusInterestPercentageAndCashBasedAccountingEnabled() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();
        Integer amountPlusInterestPercentageDisbursementCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1"));
        addCharges(charges, amountPlusInterestPercentageDisbursementCharge, "1", null);

        Integer amountPlusInterestPercentageSpecifiedDueDateCharge = ChargesHelper
                .createCharges(requestSpec, responseSpec, ChargesHelper.getLoanSpecifiedDueDateJSON(
                        ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentageSpecifiedDueDateCharge, "1", "29 September 2011");

        Integer amountPlusInterestPercentageInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentageInstallmentFee, "1", "29 September 2011");

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct(false, CASH_BASED, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "126.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "126.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "126.04", "0.0", "0.0");

        // check for disbursement fee
        HashMap disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("126.06", String.valueOf(disbursementDetail.get("feeChargesDue")));

        // check for charge at specified date and installment fee
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("157.57", String.valueOf(firstInstallment.get("feeChargesDue")));

        // check for installment fee
        HashMap secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("31.51", String.valueOf(secondInstallment.get("feeChargesDue")));

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("126.06"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("126.06"), JournalEntry.TransactionType.CREDIT));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.0", "126.06", "0.0");

        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("20 October 2011", Float.valueOf("3309.06"), loanID);
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "94.53", "31.51", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3309.06"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2911.49"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                new JournalEntry(Float.valueOf("157.57"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("240.00"),
                        JournalEntry.TransactionType.CREDIT));
        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                        String.valueOf(amountPlusInterestPercentageSpecifiedDueDateCharge), "29 October 2011", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("157.57", String.valueOf(secondInstallment.get("feeChargesDue")));
        this.loanTransactionHelper.waiveChargesForLoan(loanID,
                (Integer) getloanCharge(amountPlusInterestPercentageInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "63.02", "31.51", "31.51");

        System.out.println("----------Make repayment 2------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3277.55"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3277.55"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2969.72"),
                        JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("126.06"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("181.77"),
                        JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        System.out.println("--------------Waive interest---------------");
        this.loanTransactionHelper.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        Integer amountPlusInterestPercentagePenaltySpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", true));
        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                        String.valueOf(amountPlusInterestPercentagePenaltySpecifiedDueDate), "29 September 2011", "1"));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentagePenaltySpecifiedDueDate, loanCharges, "1", "0.0", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3309.06"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2791.49"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 October 2011",
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("157.57"),
                        JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("240"), JournalEntry.TransactionType.CREDIT));

        System.out.println("----------Make repayment 3 advance------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3303"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011", new JournalEntry(Float.valueOf("3303"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3149.11"), JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("31.51"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("122.38"),
                        JournalEntry.TransactionType.CREDIT));
        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                        String.valueOf(amountPlusInterestPercentagePenaltySpecifiedDueDate), "10 January 2012", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3241.19", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Pay applied penalty ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("120"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(Float.valueOf("120"),
                JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012",
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3121.19", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Make repayment 4 ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("3121.19"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("3121.19"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3089.68"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("31.51"), JournalEntry.TransactionType.CREDIT));
    }

    /***
     * Test case for checking AccuralUpfrontAccounting functionality adding
     * charges with calculation type flat
     */
    @Test
    public void loanWithFlatCahargesAndUpfrontAccrualAccountingEnabled() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();
        Integer flatDisbursement = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper.getLoanDisbursementJSON());
        addCharges(charges, flatDisbursement, "100", null);
        Integer flatSpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", false));

        Integer flatInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
        addCharges(charges, flatInstallmentFee, "50", null);

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct(false, ACCRUAL_UPFRONT, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "100.00", "0.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "200.00", "0.0", "0.0");

        // check for disbursement fee
        HashMap disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("100.00", String.valueOf(disbursementDetail.get("feeChargesDue")));

        // check for charge at specified date and installment fee
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("50.00", String.valueOf(firstInstallment.get("feeChargesDue")));

        // check for installment fee
        HashMap secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("50.00", String.valueOf(secondInstallment.get("feeChargesDue")));

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("605.94"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("200.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("605.94"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("100.00"),
                        JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("200.00"), JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatSpecifiedDueDate), "29 September 2011", "100"));

        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "100.00", "0.0", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "29 September 2011",
                new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "29 September 2011",
                new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.CREDIT));

        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("20 October 2011", Float.valueOf("3301.49"), loanID);
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "150.00", "50.0", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3301.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3301.49"),
                JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatSpecifiedDueDate), "29 October 2011", "100"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("150.00", String.valueOf(secondInstallment.get("feeChargesDue")));
        System.out.println("----------- Waive installment charge for 2nd installment ---------");
        this.loanTransactionHelper.waiveChargesForLoan(loanID, (Integer) getloanCharge(flatInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatInstallmentFee, loanCharges, "50", "100.00", "50.0", "50.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011", new JournalEntry(Float.valueOf("50.0"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("50.0"), JournalEntry.TransactionType.DEBIT));

        System.out.println("----------Make repayment 2------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3251.49"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3251.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3251.49"),
                        JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        System.out.println("--------------Waive interest---------------");
        this.loanTransactionHelper.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 December 2011", new JournalEntry(Float.valueOf("61.79"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                new JournalEntry(Float.valueOf("61.79"), JournalEntry.TransactionType.DEBIT));

        Integer flatPenaltySpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", true));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatPenaltySpecifiedDueDate), "29 September 2011", "100"));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatPenaltySpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("100", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // checking the journal entry as applied penalty has been collected
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3301.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3301.49"),
                JournalEntry.TransactionType.CREDIT));

        System.out.println("----------Make repayment 3 advance------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3301.49"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3301.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3301.49"),
                        JournalEntry.TransactionType.CREDIT));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatPenaltySpecifiedDueDate), "10 January 2012", "100"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("100", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3239.68", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Pay applied penalty ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("100"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(Float.valueOf("100"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("100"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3139.68", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Make over payment for repayment 4 ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("3220.60"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("3220.60"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3139.68"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(overpaymentAccount, "20 January 2012",
                new JournalEntry(Float.valueOf("80.92"), JournalEntry.TransactionType.CREDIT));
        loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanID, "status");
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);
    }

    /***
     * Test case for checking AccuralUpfrontAccounting functionality adding
     * charges with calculation type percentage of amount
     */
    @Test
    public void loanWithCahargesAndUpfrontAccrualAccountingEnabled() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();
        Integer percentageDisbursementCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1"));
        addCharges(charges, percentageDisbursementCharge, "1", null);

        Integer percentageSpecifiedDueDateCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, percentageSpecifiedDueDateCharge, "1", "29 September 2011");

        Integer percentageInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, percentageInstallmentFee, "1", "29 September 2011");

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct(false, ACCRUAL_UPFRONT, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "120.00", "0.0", "0.0");
        validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "120.00", "0.0", "0.0");
        validateCharge(percentageInstallmentFee, loanCharges, "1", "120.00", "0.0", "0.0");

        // check for disbursement fee
        HashMap disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("120.00", String.valueOf(disbursementDetail.get("feeChargesDue")));

        // check for charge at specified date and installment fee
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("149.11", String.valueOf(firstInstallment.get("feeChargesDue")));

        // check for installment fee
        HashMap secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("29.70", String.valueOf(secondInstallment.get("feeChargesDue")));

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("605.94"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("605.94"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("120.00"),
                        JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("120.00"),
                        JournalEntry.TransactionType.CREDIT));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.0", "120.00", "0.0");

        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("20 October 2011", Float.valueOf("3300.60"), loanID);
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.00", "120.00", "0.0");
        validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "120.0", "0.0");
        validateCharge(percentageInstallmentFee, loanCharges, "1", "90.89", "29.11", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3300.60"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3300.60"),
                JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(percentageSpecifiedDueDateCharge), "29 October 2011", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("149.70", String.valueOf(secondInstallment.get("feeChargesDue")));
        System.out.println("----------- Waive installment charge for 2nd installment ---------");
        this.loanTransactionHelper.waiveChargesForLoan(loanID, (Integer) getloanCharge(percentageInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageInstallmentFee, loanCharges, "1", "61.19", "29.11", "29.70");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011", new JournalEntry(Float.valueOf("29.7"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("29.7"), JournalEntry.TransactionType.DEBIT));

        System.out.println("----------Make repayment 2------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3271.49"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3271.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3271.49"),
                        JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        System.out.println("--------------Waive interest---------------");
        this.loanTransactionHelper.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 December 2011", new JournalEntry(Float.valueOf("61.79"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                new JournalEntry(Float.valueOf("61.79"), JournalEntry.TransactionType.DEBIT));

        Integer percentagePenaltySpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", true));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(percentagePenaltySpecifiedDueDate), "29 September 2011", "1"));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentagePenaltySpecifiedDueDate, loanCharges, "1", "0.00", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // checking the journal entry as applied penalty has been collected
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3300.60"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3300.60"),
                JournalEntry.TransactionType.CREDIT));

        System.out.println("----------Make repayment 3 advance------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3301.78"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3301.78"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3301.78"),
                        JournalEntry.TransactionType.CREDIT));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(percentagePenaltySpecifiedDueDate), "10 January 2012", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3240.58", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Pay applied penalty ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("120"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(Float.valueOf("120"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("120"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3120.58", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Make over payment for repayment 4 ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("3220.58"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("3220.58"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3120.58"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(overpaymentAccount, "20 January 2012",
                new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.CREDIT));
        loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanID, "status");
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);
    }

    /***
     * Test case for checking AccuralUpfrontAccounting functionality adding
     * charges with calculation type percentage of amount plus interest
     */
    @Test
    public void loanWithCahargesOfTypeAmountPlusInterestPercentageAndUpfrontAccrualAccountingEnabled() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();
        Integer amountPlusInterestPercentageDisbursementCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1"));
        addCharges(charges, amountPlusInterestPercentageDisbursementCharge, "1", null);

        Integer amountPlusInterestPercentageSpecifiedDueDateCharge = ChargesHelper
                .createCharges(requestSpec, responseSpec, ChargesHelper.getLoanSpecifiedDueDateJSON(
                        ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));

        Integer amountPlusInterestPercentageInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentageInstallmentFee, "1", "29 September 2011");

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct(false, ACCRUAL_UPFRONT, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "126.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "126.04", "0.0", "0.0");

        // check for disbursement fee
        HashMap disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("126.06", String.valueOf(disbursementDetail.get("feeChargesDue")));

        // check for charge at specified date and installment fee
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("31.51", String.valueOf(firstInstallment.get("feeChargesDue")));

        // check for installment fee
        HashMap secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("31.51", String.valueOf(secondInstallment.get("feeChargesDue")));

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("605.94"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("126.06"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("126.04"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("605.94"), JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("126.06"),
                        JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("126.04"), JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                        String.valueOf(amountPlusInterestPercentageSpecifiedDueDateCharge), "29 September 2011", "1"));

        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.0", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "126.06", "0.0", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "29 September 2011",
                new JournalEntry(Float.valueOf("126.06"), JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "29 September 2011",
                new JournalEntry(Float.valueOf("126.06"), JournalEntry.TransactionType.CREDIT));

        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("20 October 2011", Float.valueOf("3309.06"), loanID);
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "94.53", "31.51", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3309.06"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3309.06"),
                JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                        String.valueOf(amountPlusInterestPercentageSpecifiedDueDateCharge), "29 October 2011", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("157.57", String.valueOf(secondInstallment.get("feeChargesDue")));
        System.out.println("----------- Waive installment charge for 2nd installment ---------");
        this.loanTransactionHelper.waiveChargesForLoan(loanID,
                (Integer) getloanCharge(amountPlusInterestPercentageInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "63.02", "31.51", "31.51");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011", new JournalEntry(
                Float.valueOf("31.51"), JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("31.51"), JournalEntry.TransactionType.DEBIT));

        System.out.println("----------Make repayment 2------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3277.55"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3277.55"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3277.55"),
                        JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        System.out.println("--------------Waive interest---------------");
        this.loanTransactionHelper.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 December 2011", new JournalEntry(Float.valueOf("61.79"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                new JournalEntry(Float.valueOf("61.79"), JournalEntry.TransactionType.DEBIT));

        Integer amountPlusInterestPercentagePenaltySpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", true));
        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                        String.valueOf(amountPlusInterestPercentagePenaltySpecifiedDueDate), "29 September 2011", "1"));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentagePenaltySpecifiedDueDate, loanCharges, "1", "0.0", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // checking the journal entry as applied penalty has been collected
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3309.06"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3309.06"),
                JournalEntry.TransactionType.CREDIT));

        System.out.println("----------Make repayment 3 advance------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3303"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011", new JournalEntry(Float.valueOf("3303"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3303"), JournalEntry.TransactionType.CREDIT));
        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                        String.valueOf(amountPlusInterestPercentagePenaltySpecifiedDueDate), "10 January 2012", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3241.19", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Pay applied penalty ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("120"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(Float.valueOf("120"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("120"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3121.19", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Make over payment for repayment 4 ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("3221.61"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("3221.61"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3121.19"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(overpaymentAccount, "20 January 2012", new JournalEntry(Float.valueOf("100.42"),
                JournalEntry.TransactionType.CREDIT));
        loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanID, "status");
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);
    }

    /***
     * Test case for checking AccuralPeriodicAccounting functionality adding
     * charges with calculation type flat
     */
    @Test
    public void loanWithFlatCahargesAndPeriodicAccrualAccountingEnabled() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();
        Integer flatDisbursement = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper.getLoanDisbursementJSON());
        addCharges(charges, flatDisbursement, "100", null);
        Integer flatSpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", false));
        addCharges(charges, flatSpecifiedDueDate, "100", "29 September 2011");
        Integer flatInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
        addCharges(charges, flatInstallmentFee, "50", null);

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct(false, ACCRUAL_PERIODIC, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "100.00", "0.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "100.00", "0.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "200.00", "0.0", "0.0");

        // check for disbursement fee
        HashMap disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("100.00", String.valueOf(disbursementDetail.get("feeChargesDue")));

        // check for charge at specified date and installment fee
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("150.00", String.valueOf(firstInstallment.get("feeChargesDue")));

        // check for installment fee
        HashMap secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("50.00", String.valueOf(secondInstallment.get("feeChargesDue")));

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("100.00"), JournalEntry.TransactionType.CREDIT));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");

        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("20 October 2011", Float.valueOf("3301.49"), loanID);
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatDisbursement, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatSpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");
        validateCharge(flatInstallmentFee, loanCharges, "50", "150.00", "50.0", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3301.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3301.49"),
                JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatSpecifiedDueDate), "29 October 2011", "100"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("150.00", String.valueOf(secondInstallment.get("feeChargesDue")));
        System.out.println("----------- Waive installment charge for 2nd installment ---------");
        this.loanTransactionHelper.waiveChargesForLoan(loanID, (Integer) getloanCharge(flatInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatInstallmentFee, loanCharges, "50", "100.00", "50.0", "50.0");

        /*
         * this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount,
         * "20 September 2011", new JournalEntry(Float.valueOf("50.0"),
         * JournalEntry.TransactionType.CREDIT));
         * this.journalEntryHelper.checkJournalEntryForExpenseAccount
         * (expenseAccount, "20 September 2011", new
         * JournalEntry(Float.valueOf("50.0"),
         * JournalEntry.TransactionType.DEBIT));
         */
        final String jobName = "Add Accrual Transactions";
        try {
            this.schedulerJobHelper.executeJob(jobName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        checkAccrualTransactions(loanSchedule, loanID);

        System.out.println("----------Make repayment 2------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3251.49"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3251.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3251.49"),
                        JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        System.out.println("--------------Waive interest---------------");
        this.loanTransactionHelper.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 December 2011", new JournalEntry(Float.valueOf("61.79"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                new JournalEntry(Float.valueOf("61.79"), JournalEntry.TransactionType.DEBIT));

        Integer flatPenaltySpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", true));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatPenaltySpecifiedDueDate), "29 September 2011", "100"));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(flatPenaltySpecifiedDueDate, loanCharges, "100", "0.00", "100.0", "0.0");

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("100", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // checking the journal entry as applied penalty has been collected
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3301.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3301.49"),
                JournalEntry.TransactionType.CREDIT));

        System.out.println("----------Make repayment 3 advance------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3301.49"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3301.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3301.49"),
                        JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatPenaltySpecifiedDueDate), "10 January 2012", "100"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("100", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3239.68", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Pay applied penalty ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("100"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(Float.valueOf("100"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("100"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3139.68", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Make repayment 4 ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("3139.68"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("3139.68"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3139.68"),
                JournalEntry.TransactionType.CREDIT));
        loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanID, "status");
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
    }

    /**
     * Test case for checking AccuralPeriodicAccounting functionality adding
     * charges with calculation type percentage of amount
     */
    @Test
    public void loanWithCahargesOfTypeAmountPercentageAndPeriodicAccrualAccountingEnabled() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();
        Integer percentageDisbursementCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1"));
        addCharges(charges, percentageDisbursementCharge, "1", null);

        Integer percentageSpecifiedDueDateCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, percentageSpecifiedDueDateCharge, "1", "29 September 2011");

        Integer percentageInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", false));
        addCharges(charges, percentageInstallmentFee, "1", "29 September 2011");

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct(false, ACCRUAL_PERIODIC, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "120.00", "0.0", "0.0");
        validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "120.00", "0.0", "0.0");
        validateCharge(percentageInstallmentFee, loanCharges, "1", "120.00", "0.0", "0.0");

        // check for disbursement fee
        HashMap disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("120.00", String.valueOf(disbursementDetail.get("feeChargesDue")));

        // check for charge at specified date and installment fee
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("149.11", String.valueOf(firstInstallment.get("feeChargesDue")));

        // check for installment fee
        HashMap secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("29.70", String.valueOf(secondInstallment.get("feeChargesDue")));

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("120.00"), JournalEntry.TransactionType.CREDIT));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.0", "120.00", "0.0");

        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("20 October 2011", Float.valueOf("3300.60"), loanID);
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageDisbursementCharge, loanCharges, "1", "0.00", "120.00", "0.0");
        validateCharge(percentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "120.0", "0.0");
        validateCharge(percentageInstallmentFee, loanCharges, "1", "90.89", "29.11", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3300.60"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3300.60"),
                JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(percentageSpecifiedDueDateCharge), "29 October 2011", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("149.70", String.valueOf(secondInstallment.get("feeChargesDue")));
        System.out.println("----------- Waive installment charge for 2nd installment ---------");
        this.loanTransactionHelper.waiveChargesForLoan(loanID, (Integer) getloanCharge(percentageInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentageInstallmentFee, loanCharges, "1", "61.19", "29.11", "29.70");

        /*
         * this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount,
         * "20 September 2011", new JournalEntry(Float.valueOf("29.7"),
         * JournalEntry.TransactionType.CREDIT));
         * this.journalEntryHelper.checkJournalEntryForExpenseAccount
         * (expenseAccount, "20 September 2011", new
         * JournalEntry(Float.valueOf("29.7"),
         * JournalEntry.TransactionType.DEBIT));
         */

        final String jobName = "Add Accrual Transactions";
        try {
            this.schedulerJobHelper.executeJob(jobName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        checkAccrualTransactions(loanSchedule, loanID);

        System.out.println("----------Make repayment 2------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3271.49"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3271.49"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3271.49"),
                        JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        System.out.println("--------------Waive interest---------------");
        this.loanTransactionHelper.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 December 2011", new JournalEntry(Float.valueOf("61.79"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                new JournalEntry(Float.valueOf("61.79"), JournalEntry.TransactionType.DEBIT));

        Integer percentagePenaltySpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", true));
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(percentagePenaltySpecifiedDueDate), "29 September 2011", "1"));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(percentagePenaltySpecifiedDueDate, loanCharges, "1", "0.00", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // checking the journal entry as applied penalty has been collected
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3300.60"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3300.60"),
                JournalEntry.TransactionType.CREDIT));

        System.out.println("----------Make repayment 3 advance------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3301.78"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3301.78"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3301.78"),
                        JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(percentagePenaltySpecifiedDueDate), "10 January 2012", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3240.58", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Pay applied penalty ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("120"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(Float.valueOf("120"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("120"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3120.58", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Make repayment 4 ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("3120.58"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("3120.58"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3120.58"),
                JournalEntry.TransactionType.CREDIT));
        loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanID, "status");
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
    }

    /***
     * Test case for checking AccuralPeriodicAccounting functionality adding
     * charges with calculation type percentage of amount and interest
     */
    @Test
    public void loanWithCahargesOfTypeAmountPlusInterestPercentageAndPeriodicAccrualAccountingEnabled() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();
        Integer amountPlusInterestPercentageDisbursementCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanDisbursementJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1"));
        addCharges(charges, amountPlusInterestPercentageDisbursementCharge, "1", null);

        Integer amountPlusInterestPercentageSpecifiedDueDateCharge = ChargesHelper
                .createCharges(requestSpec, responseSpec, ChargesHelper.getLoanSpecifiedDueDateJSON(
                        ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentageSpecifiedDueDateCharge, "1", "29 September 2011");

        Integer amountPlusInterestPercentageInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", false));
        addCharges(charges, amountPlusInterestPercentageInstallmentFee, "1", "29 September 2011");

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct(false, ACCRUAL_PERIODIC, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "12,000.00");
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        verifyLoanRepaymentSchedule(loanSchedule);

        List<HashMap> loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "126.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "126.06", "0.0", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "126.04", "0.0", "0.0");

        // check for disbursement fee
        HashMap disbursementDetail = loanSchedule.get(0);
        validateNumberForEqual("126.06", String.valueOf(disbursementDetail.get("feeChargesDue")));

        // check for charge at specified date and installment fee
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("157.57", String.valueOf(firstInstallment.get("feeChargesDue")));

        // check for installment fee
        HashMap secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("31.51", String.valueOf(secondInstallment.get("feeChargesDue")));

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("126.06"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 September 2011", assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "20 September 2011",
                new JournalEntry(Float.valueOf("126.06"), JournalEntry.TransactionType.CREDIT));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.0", "126.06", "0.0");

        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("20 October 2011", Float.valueOf("3309.06"), loanID);
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageDisbursementCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageSpecifiedDueDateCharge, loanCharges, "1", "0.00", "126.06", "0.0");
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "94.53", "31.51", "0.0");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3309.06"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3309.06"),
                JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                        String.valueOf(amountPlusInterestPercentageSpecifiedDueDateCharge), "29 October 2011", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);

        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("157.57", String.valueOf(secondInstallment.get("feeChargesDue")));
        System.out.println("----------- Waive installment charge for 2nd installment ---------");
        this.loanTransactionHelper.waiveChargesForLoan(loanID,
                (Integer) getloanCharge(amountPlusInterestPercentageInstallmentFee, loanCharges).get("id"),
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentageInstallmentFee, loanCharges, "1", "63.02", "31.51", "31.51");

        /*
         * this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount,
         * "20 September 2011", new JournalEntry( Float.valueOf("31.51"),
         * JournalEntry.TransactionType.CREDIT));
         * this.journalEntryHelper.checkJournalEntryForExpenseAccount
         * (expenseAccount, "20 September 2011", new
         * JournalEntry(Float.valueOf("31.51"),
         * JournalEntry.TransactionType.DEBIT));
         */

        final String jobName = "Add Accrual Transactions";
        try {
            this.schedulerJobHelper.executeJob(jobName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        checkAccrualTransactions(loanSchedule, loanID);

        System.out.println("----------Make repayment 2------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3277.55"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011",
                new JournalEntry(Float.valueOf("3277.55"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3277.55"),
                        JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        System.out.println("--------------Waive interest---------------");
        this.loanTransactionHelper.waiveInterest("20 December 2011", String.valueOf(61.79), loanID);

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("60.59", String.valueOf(thirdInstallment.get("interestOutstanding")));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 December 2011", new JournalEntry(Float.valueOf("61.79"),
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "20 December 2011",
                new JournalEntry(Float.valueOf("61.79"), JournalEntry.TransactionType.DEBIT));

        Integer amountPlusInterestPercentagePenaltySpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "1", true));
        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                        String.valueOf(amountPlusInterestPercentagePenaltySpecifiedDueDate), "29 September 2011", "1"));
        loanCharges.clear();
        loanCharges = this.loanTransactionHelper.getLoanCharges(loanID);
        validateCharge(amountPlusInterestPercentagePenaltySpecifiedDueDate, loanCharges, "1", "0.0", "120.0", "0.0");

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("120", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // checking the journal entry as applied penalty has been collected
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 October 2011", new JournalEntry(
                Float.valueOf("3309.06"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3309.06"),
                JournalEntry.TransactionType.CREDIT));

        System.out.println("----------Make repayment 3 advance------------");
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3303"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 November 2011", new JournalEntry(Float.valueOf("3303"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3303"), JournalEntry.TransactionType.CREDIT));

        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                        String.valueOf(amountPlusInterestPercentagePenaltySpecifiedDueDate), "10 January 2012", "1"));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("120", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3241.19", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Pay applied penalty ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("120"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(Float.valueOf("120"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("120"), JournalEntry.TransactionType.CREDIT));
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("0", String.valueOf(fourthInstallment.get("penaltyChargesOutstanding")));
        validateNumberForEqual("3121.19", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));

        System.out.println("----------Make repayment 4 ------------");
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("3121.19"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "20 January 2012", new JournalEntry(
                Float.valueOf("3121.19"), JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("3121.19"),
                JournalEntry.TransactionType.CREDIT));
        loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanID, "status");
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
    }

    @Test
    public void testClientLoanScheduleWithCurrencyDetails() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct("100", "0", LoanProductTestBuilder.DEFAULT_STRATEGY);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, null);
        final ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec,
                loanID);
        verifyLoanRepaymentScheduleForEqualPrincipal(loanSchedule);

    }

    @Test
    public void testClientLoanScheduleWithCurrencyDetails_with_grace() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct("100", "0", LoanProductTestBuilder.DEFAULT_STRATEGY);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, "5");
        final ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec,
                loanID);
        verifyLoanRepaymentScheduleForEqualPrincipalWithGrace(loanSchedule);

    }

    /***
     * Test case to verify RBI payment strategy
     */
    @Test
    public void testRBIPaymentStrategy() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        /***
         * Create loan product with RBI strategy
         */
        final Integer loanProductID = createLoanProduct("100", "0", LoanProductTestBuilder.RBI_INDIA_STRATEGY);
        Assert.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */
        final String savingsId = null;
        final String principal = "12,000.00";
        final Integer loanID = applyForLoanApplicationWithPaymentStrategy(clientID, loanProductID, null, savingsId, principal,
                LoanApplicationTestBuilder.RBI_INDIA_STRATEGY);
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("3200", String.valueOf(firstInstallment.get("totalOutstandingForPeriod")));

        /***
         * Make payment for installment #1
         */
        this.loanTransactionHelper.makeRepayment("20 October 2011", Float.valueOf("3200"), loanID);
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("0.00", String.valueOf(firstInstallment.get("totalOutstandingForPeriod")));

        /***
         * Verify 2nd and 3rd repayments dues before making excess payment for
         * installment no 2
         */
        HashMap secondInstallment = loanSchedule.get(2);
        HashMap thirdInstallment = loanSchedule.get(3);

        validateNumberForEqual("3200", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));
        validateNumberForEqual("3200", String.valueOf(thirdInstallment.get("totalOutstandingForPeriod")));

        validateNumberForEqual("3000", String.valueOf(secondInstallment.get("principalOutstanding")));
        validateNumberForEqual("3100", String.valueOf(thirdInstallment.get("principalOutstanding")));

        /***
         * Make payment for installment #2
         */
        this.loanTransactionHelper.makeRepayment("20 November 2011", Float.valueOf("3200"), loanID);
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        /***
         * Verify 2nd and 3rd repayments after making excess payment for
         * installment no 2
         */
        secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0.00", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        /***
         * According to RBI Excess payment should go to principal portion of
         * next installment, but as interest recalculation is not implemented,
         * it wont make any difference to schedule even though if we made excess
         * payment, so excess payments will behave the same as regular payment
         * with the excess amount
         */
        thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("3200", String.valueOf(thirdInstallment.get("totalOutstandingForPeriod")));
        validateNumberForEqual("3100", String.valueOf(thirdInstallment.get("principalOutstanding")));
        validateNumberForEqual("0", String.valueOf(thirdInstallment.get("principalPaid")));
        validateNumberForEqual("0", String.valueOf(thirdInstallment.get("interestPaid")));
        validateNumberForEqual("100.00", String.valueOf(thirdInstallment.get("interestOutstanding")));

        /***
         * Make payment with due amount of 3rd installment on 4th installment
         * date
         */
        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("3200"), loanID);
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);

        /***
         * Verify overdue interests are deducted first and then remaining amount
         * for interest portion of due installment
         */
        thirdInstallment = loanSchedule.get(3);
        HashMap fourthInstallment = loanSchedule.get(4);

        validateNumberForEqual("100", String.valueOf(thirdInstallment.get("totalOutstandingForPeriod")));
        validateNumberForEqual("100", String.valueOf(thirdInstallment.get("principalOutstanding")));

        validateNumberForEqual("2900", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));
        validateNumberForEqual("100", String.valueOf(fourthInstallment.get("interestPaid")));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.get("interestOutstanding")));

        this.loanTransactionHelper.makeRepayment("20 January 2012", Float.valueOf("3000"), loanID);

        /***
         * verify loan is closed as we paid full amount
         */
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);

    }

    private void checkAccrualTransactions(final ArrayList<HashMap> loanSchedule, final Integer loanID) {

        for (int i = 1; i < loanSchedule.size(); i++) {

            final HashMap repayment = loanSchedule.get(i);

            final ArrayList<Integer> dueDateAsArray = (ArrayList<Integer>) repayment.get("dueDate");
            final LocalDate transactionDate = new LocalDate(dueDateAsArray.get(0), dueDateAsArray.get(1), dueDateAsArray.get(2));

            final Float interestPortion = BigDecimal.valueOf(Double.valueOf(repayment.get("interestDue").toString()))
                    .subtract(BigDecimal.valueOf(Double.valueOf(repayment.get("interestWaived").toString())))
                    .subtract(BigDecimal.valueOf(Double.valueOf(repayment.get("interestWrittenOff").toString()))).floatValue();

            final Float feePortion = BigDecimal.valueOf(Double.valueOf(repayment.get("feeChargesDue").toString()))
                    .subtract(BigDecimal.valueOf(Double.valueOf(repayment.get("feeChargesWaived").toString())))
                    .subtract(BigDecimal.valueOf(Double.valueOf(repayment.get("feeChargesWrittenOff").toString()))).floatValue();

            final Float penaltyPortion = BigDecimal.valueOf(Double.valueOf(repayment.get("penaltyChargesDue").toString()))
                    .subtract(BigDecimal.valueOf(Double.valueOf(repayment.get("penaltyChargesWaived").toString())))
                    .subtract(BigDecimal.valueOf(Double.valueOf(repayment.get("penaltyChargesWrittenOff").toString()))).floatValue();

            this.loanTransactionHelper.checkAccrualTransactionForRepayment(transactionDate, interestPortion, feePortion, penaltyPortion,
                    loanID);
        }
    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_SAME_AS_REPAYMENT_INTEREST_COMPOUND_NONE_STRATEGY_REDUCE_EMI() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProductWithInterestRecalculation(LoanProductTestBuilder.DEFAULT_STRATEGY,
                LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_EMI_AMOUN,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD, "0", null,
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null, null, null);

        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE, null,
                LoanApplicationTestBuilder.DEFAULT_STRATEGY, new ArrayList<HashMap>(0));

        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2528.81", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        this.loanTransactionHelper.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        Float earlyPayment = new Float("4000");
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -5);
        final String LOAN_SECOND_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        this.loanTransactionHelper.makeRepayment(LOAN_SECOND_REPAYMENT_DATE, earlyPayment, loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "3965.31", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "1771.88", "16.39", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "1780.05", "8.22", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        HashMap prepayDetail = this.loanTransactionHelper.getPrepayAmount(this.requestSpec, this.responseSpec, loanID);
        String prepayAmount = String.valueOf(prepayDetail.get("amount"));
        validateNumberForEqualWithMsg("verify pre-close amount", "3551.93", prepayAmount);
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        String LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, new Float(prepayAmount), loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_SAME_AS_REPAYMENT_INTEREST_COMPOUND_NONE_STRATEGY_REDUCE_EMI_PRE_CLOSE_INTEREST_PRE_CLOSE_DATE() {
        String preCloseInterestStrategy = LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE;
        String preCloseAmount = "7561.84";
        testLoanScheduleWithInterestRecalculation_WITH_REST_SAME_AS_REPAYMENT_INTEREST_COMPOUND_NONE_STRATEGY_REDUCE_EMI_PRE_CLOSE_INTEREST(
                preCloseInterestStrategy, preCloseAmount);
    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_SAME_AS_REPAYMENT_INTEREST_COMPOUND_NONE_STRATEGY_REDUCE_EMI_PRE_CLOSE_INTEREST_REST_DATE() {
        String preCloseInterestStrategy = LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_REST_DATE;
        String preCloseAmount = "7586.62";
        testLoanScheduleWithInterestRecalculation_WITH_REST_SAME_AS_REPAYMENT_INTEREST_COMPOUND_NONE_STRATEGY_REDUCE_EMI_PRE_CLOSE_INTEREST(
                preCloseInterestStrategy, preCloseAmount);
    }

    private void testLoanScheduleWithInterestRecalculation_WITH_REST_SAME_AS_REPAYMENT_INTEREST_COMPOUND_NONE_STRATEGY_REDUCE_EMI_PRE_CLOSE_INTEREST(
            String preCloseInterestStrategy, String preCloseAmount) {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -16);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProductWithInterestRecalculation(LoanProductTestBuilder.DEFAULT_STRATEGY,
                LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_EMI_AMOUN,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD, "0", null, preCloseInterestStrategy, null,
                null, null);

        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE, null,
                LoanApplicationTestBuilder.DEFAULT_STRATEGY, new ArrayList<HashMap>(0));

        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -9, true, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -9, true, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2551.72", "11.78", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -9);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        this.loanTransactionHelper.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -9, true, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2528.8", "11.67", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        HashMap prepayDetail = this.loanTransactionHelper.getPrepayAmount(this.requestSpec, this.responseSpec, loanID);
        String prepayAmount = String.valueOf(prepayDetail.get("amount"));
        validateNumberForEqualWithMsg("verify pre-close amount", preCloseAmount, prepayAmount);
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        String LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, new Float(prepayAmount), loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_SAME_AS_REPAYMENT_INTEREST_COMPOUND_NONE_STRATEGY_REDUCE_EMI_WITH_INSTALLMENT_CHARGE() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProductWithInterestRecalculation(LoanProductTestBuilder.DEFAULT_STRATEGY,
                LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_EMI_AMOUN,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD, "0", null,
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null, null, null);

        List<HashMap> charges = new ArrayList<>();
        Integer installmentCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_INTEREST, "10", false));
        addCharges(charges, installmentCharge, "10", null);
        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE, null,
                LoanApplicationTestBuilder.DEFAULT_STRATEGY, charges);

        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "4.62", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "3.47", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "2.32", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "1.16", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "4.62", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.76", "46.15", "4.62", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "2.32", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2528.81", "11.67", "1.17", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        this.loanTransactionHelper.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "4.62", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "3.47", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "2.32", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "1.16", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        Float earlyPayment = new Float("4000");
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -5);
        final String LOAN_SECOND_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        this.loanTransactionHelper.makeRepayment(LOAN_SECOND_REPAYMENT_DATE, earlyPayment, loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "4.62", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "3961.84", "34.69", "3.47", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "1773.61", "16.41", "1.64", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "1781.79", "8.22", "0.82", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        HashMap prepayDetail = this.loanTransactionHelper.getPrepayAmount(this.requestSpec, this.responseSpec, loanID);
        String prepayAmount = String.valueOf(prepayDetail.get("amount"));
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        String LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, new Float(prepayAmount), loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_DAILY_INTEREST_COMPOUND_INTEREST_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());
        Integer dayOfMonth = getDayOfMonth(todaysDate);
        Integer dayOfWeek = getDayOfWeek(todaysDate);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProductWithInterestRecalculationAndCompoundingDetails(
                LoanProductTestBuilder.RBI_INDIA_STRATEGY, LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_INTEREST,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY, "1", LOAN_DISBURSEMENT_DATE,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_WEEKLY, "1", LOAN_DISBURSEMENT_DATE,
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null, null, dayOfWeek, null, dayOfWeek);

        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                LoanApplicationTestBuilder.RBI_INDIA_STRATEGY, new ArrayList<HashMap>(0));

        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.54", "46.37", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.03", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        loanSchedule = this.loanTransactionHelper.getLoanFutureRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, 0, false, "4965.3", "92.52", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.03", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues, 0);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        this.loanTransactionHelper.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        Float earlyPayment = new Float("4000");
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -5);
        final String LOAN_SECOND_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        this.loanTransactionHelper.makeRepayment(LOAN_SECOND_REPAYMENT_DATE, earlyPayment, loanID);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        Calendar today = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        Map<String, Object> paymentday = new HashMap<>(3);
        paymentday.put("dueDate", getDateAsArray(today, -5, Calendar.DAY_OF_MONTH));
        paymentday.put("principalDue", "3990.09");
        paymentday.put("interestDue", "9.91");
        paymentday.put("feeChargesDue", "0");
        paymentday.put("penaltyChargesDue", "0");
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        expectedvalues.add(paymentday);
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.31", "11.6", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "1009.84", "4.66", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        HashMap prepayDetail = this.loanTransactionHelper.getPrepayAmount(this.requestSpec, this.responseSpec, loanID);
        String prepayAmount = String.valueOf(prepayDetail.get("amount"));
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        String LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, new Float(prepayAmount), loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_WEEKLY_INTEREST_COMPOUND_INTEREST_FEE_STRATEGY_REDUCE_NEXT_INSTALLMENTS() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        Integer compoundingDayOfMonth = getDayOfMonth(todaysDate);
        Integer compoundingDayOfWeek = getDayOfWeek(todaysDate);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.DAY_OF_MONTH, -2);
        Integer restDayOfMonth = getDayOfMonth(todaysDate);
        Integer restDayOfWeek = getDayOfWeek(todaysDate);
        final String REST_START_DATE = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        todaysDate.add(Calendar.DAY_OF_MONTH, 2);
        final String LOAN_FLAT_CHARGE_DATE = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.DAY_OF_MONTH, 14);
        final String LOAN_INTEREST_CHARGE_DATE = dateFormat.format(todaysDate.getTime());
        List<HashMap> charges = new ArrayList<>(2);
        Integer flat = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", false));
        Integer principalPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "2", false));

        addCharges(charges, flat, "100", LOAN_FLAT_CHARGE_DATE);
        addCharges(charges, principalPercentage, "2", LOAN_INTEREST_CHARGE_DATE);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProductWithInterestRecalculationAndCompoundingDetails(
                LoanProductTestBuilder.DEFAULT_STRATEGY, LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_INTEREST_AND_FEE,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_RESCHEDULE_NEXT_REPAYMENTS,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_WEEKLY, "1", REST_START_DATE,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_WEEKLY, "1", LOAN_DISBURSEMENT_DATE,
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null, compoundingDayOfMonth, compoundingDayOfWeek,
                restDayOfMonth, restDayOfWeek);

        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                LOAN_DISBURSEMENT_DATE, LoanApplicationTestBuilder.DEFAULT_STRATEGY, charges);

        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "100.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "200", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "100.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.08", "46.83", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "200", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.49", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        Calendar repaymentDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        repaymentDate.add(Calendar.DAY_OF_MONTH, -7);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(repaymentDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        this.loanTransactionHelper.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "100.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "200", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        Float earlyPayment = new Float("5100");
        repaymentDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        repaymentDate.add(Calendar.DAY_OF_MONTH, -5);
        final String LOAN_SECOND_REPAYMENT_DATE = dateFormat.format(repaymentDate.getTime());
        this.loanTransactionHelper.makeRepayment(LOAN_SECOND_REPAYMENT_DATE, earlyPayment, loanID);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "100.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "5065.31", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "0", "11.32", "200", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2451.93", "11.32", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        HashMap prepayDetail = this.loanTransactionHelper.getPrepayAmount(this.requestSpec, this.responseSpec, loanID);
        String prepayAmount = String.valueOf(prepayDetail.get("amount"));
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        String LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, new Float(prepayAmount), loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_WEEKLY_INTEREST_COMPOUND_INTEREST_FEE_STRATEGY_REDUCE_NEXT_INSTALLMENTS_PRE_CLOSE_INTEREST_PRE_CLOSE_DATE() {
        String preCloseInterestStrategy = LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE;
        String preCloseAmount = "7761.89";
        testLoanScheduleWithInterestRecalculation_WITH_REST_WEEKLY_INTEREST_COMPOUND_INTEREST_FEE_STRATEGY_REDUCE_NEXT_INSTALLMENTS_PRE_CLOSE_INTEREST(
                preCloseInterestStrategy, preCloseAmount);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_WEEKLY_INTEREST_COMPOUND_INTEREST_FEE_STRATEGY_REDUCE_NEXT_INSTALLMENTS_PRE_CLOSE_INTEREST_REST_DATE() {
        String preCloseInterestStrategy = LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_REST_DATE;
        String preCloseAmount = "7786.79";
        testLoanScheduleWithInterestRecalculation_WITH_REST_WEEKLY_INTEREST_COMPOUND_INTEREST_FEE_STRATEGY_REDUCE_NEXT_INSTALLMENTS_PRE_CLOSE_INTEREST(
                preCloseInterestStrategy, preCloseAmount);

    }

    private void testLoanScheduleWithInterestRecalculation_WITH_REST_WEEKLY_INTEREST_COMPOUND_INTEREST_FEE_STRATEGY_REDUCE_NEXT_INSTALLMENTS_PRE_CLOSE_INTEREST(
            String preCloseInterestStrategy, String preCloseAmount) {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -16);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.DAY_OF_MONTH, -4);
        Integer restDateOfMonth = getDayOfMonth(todaysDate);
        Integer restDateOfWeek = getDayOfWeek(todaysDate);
        final String REST_START_DATE = null;

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -16);
        todaysDate.add(Calendar.DAY_OF_MONTH, 2);
        final String LOAN_FLAT_CHARGE_DATE = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.DAY_OF_MONTH, 14);
        final String LOAN_INTEREST_CHARGE_DATE = dateFormat.format(todaysDate.getTime());
        List<HashMap> charges = new ArrayList<>(2);
        Integer flat = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", false));
        Integer principalPercentage = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT, "2", false));

        addCharges(charges, flat, "100", LOAN_FLAT_CHARGE_DATE);
        addCharges(charges, principalPercentage, "2", LOAN_INTEREST_CHARGE_DATE);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProductWithInterestRecalculationAndCompoundingDetails(
                LoanProductTestBuilder.DEFAULT_STRATEGY, LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_INTEREST_AND_FEE,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_RESCHEDULE_NEXT_REPAYMENTS,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_WEEKLY, "1", REST_START_DATE,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD, null, null, preCloseInterestStrategy, null,
                null, null, restDateOfMonth, restDateOfWeek);

        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                REST_START_DATE, LoanApplicationTestBuilder.DEFAULT_STRATEGY, charges);

        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -9, true, "2482.76", "46.15", "100.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "200", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -9, true, "2482.76", "46.15", "100.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.08", "46.83", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2481.87", "47.04", "200", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2553.29", "11.78", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        Calendar repaymentDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        repaymentDate.add(Calendar.DAY_OF_MONTH, -9);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(repaymentDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        this.loanTransactionHelper.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -9, true, "2482.76", "46.15", "100.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.05", "34.86", "200", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2528.97", "11.67", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        HashMap prepayDetail = this.loanTransactionHelper.getPrepayAmount(this.requestSpec, this.responseSpec, loanID);
        String prepayAmount = String.valueOf(prepayDetail.get("amount"));
        validateNumberForEqualWithMsg("verify pre-close amount", preCloseAmount, prepayAmount);
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        String LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, new Float(prepayAmount), loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_REST_DAILY_INTEREST_COMPOUND_INTEREST_FEE_STRATEGY_WITH_OVERDUE_CHARGE()
            throws InterruptedException {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7 * 3);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.DAY_OF_MONTH, -2);
        final String REST_START_DATE = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());

        Integer overdueFeeChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getLoanOverdueFeeJSONWithCalculattionTypePercentage());
        Assert.assertNotNull(overdueFeeChargeId);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final String recalculationCompoundingFrequencyInterval = null;
        final String recalculationCompoundingFrequencyDate = null;
        final Integer loanProductID = createLoanProductWithInterestRecalculation(LoanProductTestBuilder.DEFAULT_STRATEGY,
                LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_INTEREST_AND_FEE,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_RESCHEDULE_NEXT_REPAYMENTS,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY, "1", REST_START_DATE,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD, recalculationCompoundingFrequencyInterval,
                recalculationCompoundingFrequencyDate, LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null,
                overdueFeeChargeId.toString(), false, null, null, null, null);

        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                REST_START_DATE, LoanApplicationTestBuilder.DEFAULT_STRATEGY, null);


        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -2, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());

        addRepaymentValues(expectedvalues, todaysDate, -2, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.54", "46.37", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.33", "46.58", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2552.37", "11.78", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        String JobName = "Apply penalty to overdue loans";
        this.schedulerJobHelper.executeJob(JobName);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -2, false, "2482.76", "46.15", "0.0", "252.89");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2481.38", "47.53", "0.0", "252.89");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2479.99", "48.92", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2555.87", "11.8", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        Calendar repaymentDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        repaymentDate.add(Calendar.DAY_OF_MONTH, -7 * 2);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(repaymentDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        totalDueForCurrentPeriod = totalDueForCurrentPeriod - new Float("252.89");
        this.loanTransactionHelper.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -2, false, "2482.76", "46.15", "0.0", "252.89");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2493.05", "35.86", "0.0", "252.89");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2491.72", "37.19", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2532.47", "11.69", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        repaymentDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        repaymentDate.add(Calendar.DAY_OF_MONTH, -3);
        final String LOAN_SECOND_REPAYMENT_DATE = dateFormat.format(repaymentDate.getTime());
        totalDueForCurrentPeriod = (Float) loanSchedule.get(2).get("totalDueForPeriod");
        this.loanTransactionHelper.makeRepayment(LOAN_SECOND_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -2, false, "2482.76", "46.15", "0.0", "252.89");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2493.05", "35.86", "0.0", "252.89");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2497.22", "31.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2526.97", "11.66", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_PERIODIC_ACCOUNTING() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.periodicAccrualAccountingHelper = new PeriodicAccrualAccountingHelper(this.requestSpec, this.responseSpec);
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        System.out.println("Disbursal Date Calendar " + todaysDate.getTime());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        Account[] accounts = { assetAccount, incomeAccount, expenseAccount, overpaymentAccount };
        final Integer loanProductID = createLoanProductWithInterestRecalculation(LoanProductTestBuilder.DEFAULT_STRATEGY,
                LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_EMI_AMOUN,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD, "0", null,
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, accounts, null, null);

        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE, null,
                LoanApplicationTestBuilder.DEFAULT_STRATEGY, new ArrayList<HashMap>(0));

        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        System.out.println("Date during repayment schedule" + todaysDate.getTime());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2528.81", "11.67", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(10000.0f, JournalEntry.TransactionType.CREDIT),
                new JournalEntry(10000.0f, JournalEntry.TransactionType.DEBIT), };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, LOAN_DISBURSEMENT_DATE, assetAccountInitialEntry);
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        String runOndate = dateFormat.format(todaysDate.getTime());
        System.out.println("runOndate : " + runOndate);
        this.periodicAccrualAccountingHelper.runPeriodicAccrualAccounting(runOndate);
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(Utils.getLocalDateOfTenant().minusDays(7), 46.15f, 0f, 0f, loanID);
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(Utils.getLocalDateOfTenant(), 46.15f, 0f, 0f, loanID);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        this.loanTransactionHelper.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        this.periodicAccrualAccountingHelper.runPeriodicAccrualAccounting(runOndate);
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(Utils.getLocalDateOfTenant().minusDays(7), 46.15f, 0f, 0f, loanID);
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(Utils.getLocalDateOfTenant(), 34.69f, 0f, 0f, loanID);

        HashMap prepayDetail = this.loanTransactionHelper.getPrepayAmount(this.requestSpec, this.responseSpec, loanID);
        String prepayAmount = String.valueOf(prepayDetail.get("amount"));
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        String LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, new Float(prepayAmount), loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);

        this.loanTransactionHelper.checkAccrualTransactionForRepayment(Utils.getLocalDateOfTenant().minusDays(7), 46.15f, 0f, 0f, loanID);
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(Utils.getLocalDateOfTenant(), 34.69f, 0f, 0f, loanID);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_CURRENT_REPAYMENT_BASED_ARREARS_AGEING() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProductWithInterestRecalculationAndCompoundingDetails(
                LoanProductTestBuilder.RBI_INDIA_STRATEGY, LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_INTEREST,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_RESCHEDULE_NEXT_REPAYMENTS,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY, "1", LOAN_DISBURSEMENT_DATE,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD, "1", LOAN_DISBURSEMENT_DATE,
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null, getDayOfMonth(todaysDate),
                getDayOfWeek(todaysDate), getDayOfMonth(todaysDate), getDayOfWeek(todaysDate));

        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                LOAN_DISBURSEMENT_DATE, LoanApplicationTestBuilder.RBI_INDIA_STRATEGY, new ArrayList<HashMap>(0));

        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.54", "46.37", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.03", "11.67", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        HashMap loanSummary = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, loanID);
        List dates = (List) loanSummary.get("overdueSinceDate");
        assertEquals(todaysDate.get(Calendar.YEAR), dates.get(0));
        assertEquals(todaysDate.get(Calendar.MONTH) + 1, dates.get(1));
        assertEquals(todaysDate.get(Calendar.DAY_OF_MONTH), dates.get(2));

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -8);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        this.loanTransactionHelper.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        loanSummary = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, loanID);
        dates = (List) loanSummary.get("overdueSinceDate");
        assertEquals(todaysDate.get(Calendar.YEAR), dates.get(0));
        assertEquals(todaysDate.get(Calendar.MONTH) + 1, dates.get(1));
        assertEquals(todaysDate.get(Calendar.DAY_OF_MONTH), dates.get(2));

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_WITH_ORIGINAL_REPAYMENT_BASED_ARREARS_AGEING() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        System.out.println("----timeeeeeeeeeeeeee------>" + dateFormat.format(todaysDate.getTime()));
        todaysDate.add(Calendar.DAY_OF_MONTH, -14);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final String recalculationCompoundingFrequencyInterval = null;
        final String recalculationCompoundingFrequencyDate = null;
        final Integer loanProductID = createLoanProductWithInterestRecalculation(LoanProductTestBuilder.RBI_INDIA_STRATEGY,
                LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_INTEREST,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_RESCHEDULE_NEXT_REPAYMENTS,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY, "1", LOAN_DISBURSEMENT_DATE,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD, recalculationCompoundingFrequencyInterval,
                recalculationCompoundingFrequencyDate, LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, null, null,
                true, null, null, getDayOfMonth(todaysDate), getDayOfWeek(todaysDate));

        final Integer loanID = applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, LOAN_DISBURSEMENT_DATE,
                LOAN_DISBURSEMENT_DATE, LoanApplicationTestBuilder.RBI_INDIA_STRATEGY, new ArrayList<HashMap>(0));

        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "34.69", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        addRepaymentValues(expectedvalues, todaysDate, -1, false, "2482.76", "46.15", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.54", "46.37", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.67", "23.24", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2529.03", "11.67", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        HashMap loanSummary = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, loanID);
        List dates = (List) loanSummary.get("overdueSinceDate");
        assertEquals(todaysDate.get(Calendar.YEAR), dates.get(0));
        assertEquals(todaysDate.get(Calendar.MONTH) + 1, dates.get(1));
        assertEquals(todaysDate.get(Calendar.DAY_OF_MONTH), dates.get(2));

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -8);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        Float totalDueForCurrentPeriod = (Float) loanSchedule.get(1).get("totalDueForPeriod");
        this.loanTransactionHelper.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -7);
        loanSummary = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, loanID);
        dates = (List) loanSummary.get("overdueSinceDate");
        Assert.assertNull(dates);

    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_FOR_PRE_CLOSE_WITH_MORATORIUM_INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE() {
        testLoanScheduleWithInterestRecalculation_FOR_PRE_CLOSE_WITH_MORATORIUM(
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE, "10006.59");
    }

    @Test
    public void testLoanScheduleWithInterestRecalculation_FOR_PRE_CLOSE_WITH_MORATORIUM_INTEREST_APPLICABLE_STRATEGY_REST_DATE() {
        testLoanScheduleWithInterestRecalculation_FOR_PRE_CLOSE_WITH_MORATORIUM(
                LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_REST_DATE, "10046.15");
    }

    private void testLoanScheduleWithInterestRecalculation_FOR_PRE_CLOSE_WITH_MORATORIUM(final String preCloseStrategy,
            final String preCloseAmount) {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());

        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -1);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProductWithInterestRecalculation(LoanProductTestBuilder.DEFAULT_STRATEGY,
                LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_EMI_AMOUN,
                LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_SAME_AS_REPAYMENT_PERIOD, "0", null, preCloseStrategy, null, null, null);

        final Integer loanID = applyForLoanApplicationForInterestRecalculationWithMoratorium(clientID, loanProductID,
                LOAN_DISBURSEMENT_DATE, LoanApplicationTestBuilder.DEFAULT_STRATEGY, new ArrayList<HashMap>(0), "1", null);

        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        List<Map<String, Object>> expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -1);
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.76", "0.0", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "80.84", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        expectedvalues = new ArrayList<>();
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        todaysDate.add(Calendar.DAY_OF_MONTH, -1);
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2482.76", "0.0", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2494.22", "80.84", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2505.73", "23.18", "0.0", "0.0");
        addRepaymentValues(expectedvalues, todaysDate, 1, false, "2517.29", "11.62", "0.0", "0.0");

        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues);

        HashMap prepayDetail = this.loanTransactionHelper.getPrepayAmount(this.requestSpec, this.responseSpec, loanID);
        String prepayAmount = String.valueOf(prepayDetail.get("amount"));
        validateNumberForEqualWithMsg("verify pre-close amount", preCloseAmount, prepayAmount);
        todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        String LOAN_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());
        this.loanTransactionHelper.makeRepayment(LOAN_REPAYMENT_DATE, new Float(prepayAmount), loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
    }

    private void addRepaymentValues(List<Map<String, Object>> expectedvalues, Calendar todaysDate, int addPeriod, boolean isAddDays,
            String principalDue, String interestDue, String feeChargesDue, String penaltyChargesDue) {
        Map<String, Object> values = new HashMap<>(3);
        if (isAddDays) {
            values.put("dueDate", getDateAsArray(todaysDate, addPeriod));
        } else {
            values.put("dueDate", getDateAsArray(todaysDate, addPeriod * 7));
        }
        System.out.println("Updated date " + values.get("dueDate"));
        values.put("principalDue", principalDue);
        values.put("interestDue", interestDue);
        values.put("feeChargesDue", feeChargesDue);
        values.put("penaltyChargesDue", penaltyChargesDue);
        expectedvalues.add(values);
    }

    private List getDateAsArray(Calendar todaysDate, int addPeriod) {
        return getDateAsArray(todaysDate, addPeriod, Calendar.DAY_OF_MONTH);
    }

    private List getDateAsArray(Calendar todaysDate, int addvalue, int type) {
        todaysDate.add(type, addvalue);
        return new ArrayList<>(Arrays.asList(todaysDate.get(Calendar.YEAR), todaysDate.get(Calendar.MONTH) + 1,
                todaysDate.get(Calendar.DAY_OF_MONTH)));
    }

    private Integer createLoanProductWithInterestRecalculation(final String repaymentStrategy,
            final String interestRecalculationCompoundingMethod, final String rescheduleStrategyMethod,
            final String recalculationRestFrequencyType, final String recalculationRestFrequencyInterval,
            final String recalculationRestFrequencyDate, final String preCloseInterestCalculationStrategy, final Account[] accounts,
            final Integer recalculationRestFrequencyOnDayType, final Integer recalculationRestFrequencyDayOfWeekType) {
        final String recalculationCompoundingFrequencyType = null;
        final String recalculationCompoundingFrequencyInterval = null;
        final String recalculationCompoundingFrequencyDate = null;
        final Integer recalculationCompoundingFrequencyOnDayType = null;
        final Integer recalculationCompoundingFrequencyDayOfWeekType = null;
        return createLoanProductWithInterestRecalculation(repaymentStrategy, interestRecalculationCompoundingMethod,
                rescheduleStrategyMethod, recalculationRestFrequencyType, recalculationRestFrequencyInterval,
                recalculationRestFrequencyDate, recalculationCompoundingFrequencyType, recalculationCompoundingFrequencyInterval,
                recalculationCompoundingFrequencyDate, preCloseInterestCalculationStrategy, accounts, null, false,
                recalculationCompoundingFrequencyOnDayType, recalculationCompoundingFrequencyDayOfWeekType,
                recalculationRestFrequencyOnDayType, recalculationRestFrequencyDayOfWeekType);
    }

    private Integer createLoanProductWithInterestRecalculationAndCompoundingDetails(final String repaymentStrategy,
            final String interestRecalculationCompoundingMethod, final String rescheduleStrategyMethod,
            final String recalculationRestFrequencyType, final String recalculationRestFrequencyInterval,
            final String recalculationRestFrequencyDate, final String recalculationCompoundingFrequencyType,
            final String recalculationCompoundingFrequencyInterval, final String recalculationCompoundingFrequencyDate,
            final String preCloseInterestCalculationStrategy, final Account[] accounts,
            final Integer recalculationCompoundingFrequencyOnDayType, final Integer recalculationCompoundingFrequencyDayOfWeekType,
            final Integer recalculationRestFrequencyOnDayType, final Integer recalculationRestFrequencyDayOfWeekType) {
        return createLoanProductWithInterestRecalculation(repaymentStrategy, interestRecalculationCompoundingMethod,
                rescheduleStrategyMethod, recalculationRestFrequencyType, recalculationRestFrequencyInterval,
                recalculationRestFrequencyDate, recalculationCompoundingFrequencyType, recalculationCompoundingFrequencyInterval,
                recalculationCompoundingFrequencyDate, preCloseInterestCalculationStrategy, accounts, null, false,
                recalculationCompoundingFrequencyOnDayType, recalculationCompoundingFrequencyDayOfWeekType,
                recalculationRestFrequencyOnDayType, recalculationRestFrequencyDayOfWeekType);
    }

    private Integer createLoanProductWithInterestRecalculation(final String repaymentStrategy,
            final String interestRecalculationCompoundingMethod, final String rescheduleStrategyMethod,
            final String recalculationRestFrequencyType, final String recalculationRestFrequencyInterval,
            final String recalculationRestFrequencyDate, final String recalculationCompoundingFrequencyType,
            final String recalculationCompoundingFrequencyInterval, final String recalculationCompoundingFrequencyDate,
            final String preCloseInterestCalculationStrategy, final Account[] accounts, final String chargeId,
            boolean isArrearsBasedOnOriginalSchedule, final Integer recalculationCompoundingFrequencyOnDayType,
            final Integer recalculationCompoundingFrequencyDayOfWeekType, final Integer recalculationRestFrequencyOnDayType,
            final Integer recalculationRestFrequencyDayOfWeekType) {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        LoanProductTestBuilder builder = new LoanProductTestBuilder()
                .withPrincipal("10000000.00")
                .withNumberOfRepayments("24")
                .withRepaymentAfterEvery("1")
                .withRepaymentTypeAsWeek()
                .withinterestRatePerPeriod("2")
                .withInterestRateFrequencyTypeAsMonths()
                .withRepaymentStrategy(repaymentStrategy)
                .withAmortizationTypeAsEqualPrincipalPayment()
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                .withInterestTypeAsDecliningBalance()
                .withInterestRecalculationDetails(interestRecalculationCompoundingMethod, rescheduleStrategyMethod,
                        preCloseInterestCalculationStrategy)
                .withInterestRecalculationRestFrequencyDetails(recalculationRestFrequencyType, recalculationRestFrequencyInterval,
                        recalculationRestFrequencyOnDayType, recalculationRestFrequencyDayOfWeekType)
                .withInterestRecalculationCompoundingFrequencyDetails(recalculationCompoundingFrequencyType,
                        recalculationCompoundingFrequencyInterval, recalculationCompoundingFrequencyOnDayType,
                        recalculationCompoundingFrequencyDayOfWeekType);
        if (accounts != null) {
            builder = builder.withAccountingRulePeriodicAccrual(accounts);
        }

        if (isArrearsBasedOnOriginalSchedule) builder = builder.withArrearsConfiguration();

        final String loanProductJSON = builder.build(chargeId);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplicationForInterestRecalculation(final Integer clientID, final Integer loanProductID,
            final String disbursementDate, final String repaymentStrategy, final List<HashMap> charges) {
        return applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, disbursementDate, repaymentStrategy, charges, null,
                null);
    }

    private Integer applyForLoanApplicationForInterestRecalculationWithMoratorium(final Integer clientID, final Integer loanProductID,
            final String disbursementDate, final String repaymentStrategy, final List<HashMap> charges,
            final String graceOnInterestPayment, final String graceOnPrincipalPayment) {
        return applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, disbursementDate, repaymentStrategy, charges,
                graceOnInterestPayment, graceOnPrincipalPayment);
    }

    private Integer applyForLoanApplicationForInterestRecalculation(final Integer clientID, final Integer loanProductID,
            final String disbursementDate, final String compoundingStartDate, final String repaymentStrategy, final List<HashMap> charges) {
        return applyForLoanApplicationForInterestRecalculation(clientID, loanProductID, disbursementDate, repaymentStrategy, charges, null,
                null);
    }

    private Integer applyForLoanApplicationForInterestRecalculation(final Integer clientID, final Integer loanProductID,
            final String disbursementDate, final String repaymentStrategy, final List<HashMap> charges,
            final String graceOnInterestPayment, final String graceOnPrincipalPayment) {
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
                .withwithRepaymentStrategy(repaymentStrategy) //
                .withPrincipalGrace(graceOnPrincipalPayment) //
                .withInterestGrace(graceOnInterestPayment)//
                .withCharges(charges)//
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private void verifyLoanRepaymentSchedule(final ArrayList<HashMap> loanSchedule, List<Map<String, Object>> expectedvalues) {
        int index = 1;
        verifyLoanRepaymentSchedule(loanSchedule, expectedvalues, index);

    }

    private void verifyLoanRepaymentSchedule(final ArrayList<HashMap> loanSchedule, List<Map<String, Object>> expectedvalues, int index) {
        System.out.println("--------------------VERIFYING THE PRINCIPAL DUES,INTEREST DUE AND DUE DATE--------------------------");
        for (Map<String, Object> values : expectedvalues) {
            assertEquals("Checking for Due Date for  installment " + index, values.get("dueDate"), loanSchedule.get(index).get("dueDate"));
            validateNumberForEqualWithMsg("Checking for Principal Due for installment " + index,
                    String.valueOf(values.get("principalDue")), String.valueOf(loanSchedule.get(index).get("principalDue")));
            validateNumberForEqualWithMsg("Checking for Interest Due for installment " + index, String.valueOf(values.get("interestDue")),
                    String.valueOf(loanSchedule.get(index).get("interestDue")));
            validateNumberForEqualWithMsg("Checking for Fee charge Due for installment " + index,
                    String.valueOf(values.get("feeChargesDue")), String.valueOf(loanSchedule.get(index).get("feeChargesDue")));
            validateNumberForEqualWithMsg("Checking for Penalty charge Due for installment " + index,
                    String.valueOf(values.get("penaltyChargesDue")), String.valueOf(loanSchedule.get(index).get("penaltyChargesDue")));
            index++;
        }
    }

    /***
     * Test case to verify default Style payment strategy
     */
    @Test
    public void testLoanRefundByCashCashBasedAccounting() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);

        Calendar fourMonthsfromNowCalendar = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        fourMonthsfromNowCalendar.add(Calendar.MONTH, -4);

        String fourMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        /***
         * Create loan product with Default STYLE strategy
         */

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct("0", "0", LoanProductTestBuilder.DEFAULT_STRATEGY, CASH_BASED, assetAccount,
                incomeAccount, expenseAccount, overpaymentAccount);
        Assert.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */
        final String savingsId = null;
        final String principal = "12,000.00";

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();

        Integer flatInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
        addCharges(charges, flatInstallmentFee, "50", null);

        final Integer loanID = applyForLoanApplicationWithPaymentStrategyAndPastMonth(clientID, loanProductID, charges, savingsId,
                principal, LoanApplicationTestBuilder.DEFAULT_STRATEGY, -4);
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(fourMonthsfromNow, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(fourMonthsfromNow, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, fourMonthsfromNow, assetAccountInitialEntry);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("2290", String.valueOf(firstInstallment.get("totalOutstandingForPeriod")));

        // Make payment for installment #1

        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String threeMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        this.loanTransactionHelper.makeRepayment(threeMonthsfromNow, Float.valueOf("2290"), loanID);
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("0.00", String.valueOf(firstInstallment.get("totalOutstandingForPeriod")));

        // Make payment for installment #2
        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String twoMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        this.loanTransactionHelper.makeRepayment(twoMonthsfromNow, Float.valueOf("2290"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, twoMonthsfromNow, new JournalEntry(Float.valueOf("2290"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2000"), JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, twoMonthsfromNow, new JournalEntry(Float.valueOf("50"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("240"), JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        Map secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0.00", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // Make payment for installment #3
        // Pay 2290 more than expected
        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String oneMonthfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        this.loanTransactionHelper.makeRepayment(oneMonthfromNow, Float.valueOf("4580"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, oneMonthfromNow, new JournalEntry(Float.valueOf("4580"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("4000"), JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, oneMonthfromNow, new JournalEntry(Float.valueOf("100"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("480"), JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("0.00", String.valueOf(thirdInstallment.get("totalOutstandingForPeriod")));

        // Make refund of 20
        // max 2290 to refund. Pay 20 means only principal
        // Default style refund order(principal, interest, fees and penalties
        // paid: principal 2000, interest 240, fees 50, penalty 0
        // refund 20 means paid: principal 1980, interest 240, fees 50, penalty
        // 0

        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);
        final String now = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        this.loanTransactionHelper.makeRefundByCash(now, Float.valueOf("20"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, now, new JournalEntry(Float.valueOf("20"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("20"), JournalEntry.TransactionType.DEBIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.get("principalOutstanding")));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.get("interestOutstanding")));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.get("feeChargesOutstanding")));

        // Make refund of 2000
        // max 2270 to refund. Pay 2000 means only principal
        // paid: principal 1980, interest 240, fees 50, penalty 0
        // refund 2000 means paid: principal 0, interest 220, fees 50, penalty 0

        this.loanTransactionHelper.makeRefundByCash(now, Float.valueOf("2000"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, now, new JournalEntry(Float.valueOf("2000"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("1980"), JournalEntry.TransactionType.DEBIT));

        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, now, new JournalEntry(Float.valueOf("20"),
                JournalEntry.TransactionType.DEBIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("2020.00", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));
        validateNumberForEqual("2000.00", String.valueOf(fourthInstallment.get("principalOutstanding")));
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.get("interestOutstanding")));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.get("feeChargesOutstanding")));

    }

    /***
     * Test case to verify Default style payment strategy
     */
    @Test
    public void testLoanRefundByCashAccrualBasedAccounting() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);

        Calendar fourMonthsfromNowCalendar = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        fourMonthsfromNowCalendar.add(Calendar.MONTH, -4);

        String fourMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        /***
         * Create loan product with Default STYLE strategy
         */

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct("0", "0", LoanProductTestBuilder.DEFAULT_STRATEGY, ACCRUAL_UPFRONT, assetAccount,
                incomeAccount, expenseAccount, overpaymentAccount);// ,
                                                                   // LoanProductTestBuilder.EQUAL_INSTALLMENTS,
        // LoanProductTestBuilder.FLAT_BALANCE);
        Assert.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */
        final String savingsId = null;
        final String principal = "12,000.00";

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();

        Integer flatInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
        addCharges(charges, flatInstallmentFee, "50", null);

        final Integer loanID = applyForLoanApplicationWithPaymentStrategyAndPastMonth(clientID, loanProductID, charges, savingsId,
                principal, LoanApplicationTestBuilder.DEFAULT_STRATEGY, -4);
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(fourMonthsfromNow, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(fourMonthsfromNow, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("1440"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("300.00"), JournalEntry.TransactionType.DEBIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, fourMonthsfromNow, assetAccountInitialEntry);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("2290", String.valueOf(firstInstallment.get("totalOutstandingForPeriod")));

        // Make payment for installment #1

        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String threeMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        this.loanTransactionHelper.makeRepayment(threeMonthsfromNow, Float.valueOf("2290"), loanID);
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("0.00", String.valueOf(firstInstallment.get("totalOutstandingForPeriod")));

        // Make payment for installment #2
        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String twoMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        this.loanTransactionHelper.makeRepayment(twoMonthsfromNow, Float.valueOf("2290"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, twoMonthsfromNow, new JournalEntry(Float.valueOf("2290"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2290"), JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        Map secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0.00", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // Make payment for installment #3
        // Pay 2290 more than expected
        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String oneMonthfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        this.loanTransactionHelper.makeRepayment(oneMonthfromNow, Float.valueOf("4580"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, oneMonthfromNow, new JournalEntry(Float.valueOf("4580"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("4580"), JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("0.00", String.valueOf(thirdInstallment.get("totalOutstandingForPeriod")));

        // Make refund of 20
        // max 2290 to refund. Pay 20 means only principal
        // Default style refund order(principal, interest, fees and penalties
        // paid: principal 2000, interest 240, fees 50, penalty 0
        // refund 20 means paid: principal 1980, interest 240, fees 50, penalty
        // 0

        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);
        final String now = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        this.loanTransactionHelper.makeRefundByCash(now, Float.valueOf("20"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, now, new JournalEntry(Float.valueOf("20"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("20"), JournalEntry.TransactionType.DEBIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.get("principalOutstanding")));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.get("interestOutstanding")));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.get("feeChargesOutstanding")));

        // Make refund of 2000
        // max 2270 to refund. Pay 2000 means only principal
        // paid: principal 1980, interest 240, fees 50, penalty 0
        // refund 2000 means paid: principal 0, interest 220, fees 50, penalty 0

        this.loanTransactionHelper.makeRefundByCash(now, Float.valueOf("2000"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, now, new JournalEntry(Float.valueOf("2000"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("1980"), JournalEntry.TransactionType.DEBIT));

        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, now, new JournalEntry(Float.valueOf("20"),
                JournalEntry.TransactionType.DEBIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("2020.00", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));
        validateNumberForEqual("2000.00", String.valueOf(fourthInstallment.get("principalOutstanding")));
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.get("interestOutstanding")));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.get("feeChargesOutstanding")));

    }

    @Test
    public void testLoanRefundByTransferCashBasedAccounting() {
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.accountTransferHelper = new AccountTransferHelper(this.requestSpec, this.responseSpec);

        Calendar fourMonthsfromNowCalendar = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        fourMonthsfromNowCalendar.add(Calendar.MONTH, -4);

        String fourMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE);
        Assert.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap modifications = this.savingsAccountHelper.updateSavingsAccount(clientID, savingsProductID, savingsId,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertTrue(modifications.containsKey("submittedOnDate"));

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        /***
         * Create loan product with Default STYLE strategy
         */

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProduct("0", "0", LoanProductTestBuilder.DEFAULT_STRATEGY, CASH_BASED, assetAccount,
                incomeAccount, expenseAccount, overpaymentAccount);
        Assert.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */

        final String principal = "12,000.00";

        // Add charges with payment mode regular
        List<HashMap> charges = new ArrayList<>();

        Integer flatInstallmentFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
        addCharges(charges, flatInstallmentFee, "50", null);

        final Integer loanID = applyForLoanApplicationWithPaymentStrategyAndPastMonth(clientID, loanProductID, charges, null, principal,
                LoanApplicationTestBuilder.DEFAULT_STRATEGY, -4);
        Assert.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(fourMonthsfromNow, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("-------------------------------DISBURSE LOAN-------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(fourMonthsfromNow, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.CREDIT),
                new JournalEntry(Float.valueOf("12000.00"), JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, fourMonthsfromNow, assetAccountInitialEntry);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("2290", String.valueOf(firstInstallment.get("totalOutstandingForPeriod")));

        // Make payment for installment #1

        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String threeMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        this.loanTransactionHelper.makeRepayment(threeMonthsfromNow, Float.valueOf("2290"), loanID);
        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        firstInstallment = loanSchedule.get(1);
        validateNumberForEqual("0.00", String.valueOf(firstInstallment.get("totalOutstandingForPeriod")));

        // Make payment for installment #2
        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String twoMonthsfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        this.loanTransactionHelper.makeRepayment(twoMonthsfromNow, Float.valueOf("2290"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, twoMonthsfromNow, new JournalEntry(Float.valueOf("2290"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("2000"), JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, twoMonthsfromNow, new JournalEntry(Float.valueOf("50"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("240"), JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        Map secondInstallment = loanSchedule.get(2);
        validateNumberForEqual("0.00", String.valueOf(secondInstallment.get("totalOutstandingForPeriod")));

        // Make payment for installment #3
        // Pay 2290 more than expected
        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);

        final String oneMonthfromNow = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        this.loanTransactionHelper.makeRepayment(oneMonthfromNow, Float.valueOf("4580"), loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, oneMonthfromNow, new JournalEntry(Float.valueOf("4580"),
                JournalEntry.TransactionType.DEBIT), new JournalEntry(Float.valueOf("4000"), JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, oneMonthfromNow, new JournalEntry(Float.valueOf("100"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("480"), JournalEntry.TransactionType.CREDIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap thirdInstallment = loanSchedule.get(3);
        validateNumberForEqual("0.00", String.valueOf(thirdInstallment.get("totalOutstandingForPeriod")));

        // Make refund of 20
        // max 2290 to refund. Pay 20 means only principal
        // Default style refund order(principal, interest, fees and penalties
        // paid: principal 2000, interest 240, fees 50, penalty 0
        // refund 20 means paid: principal 1980, interest 240, fees 50, penalty
        // 0

        Float TRANSFER_AMOUNT = 20f;

        fourMonthsfromNowCalendar.add(Calendar.MONTH, 1);
        final String now = Utils.convertDateToURLFormat(fourMonthsfromNowCalendar);

        final String FROM_LOAN_ACCOUNT_TYPE = "1";
        final String TO_SAVINGS_ACCOUNT_TYPE = "2";

        this.accountTransferHelper.refundLoanByTransfer(now, clientID, loanID, clientID, savingsId, FROM_LOAN_ACCOUNT_TYPE,
                TO_SAVINGS_ACCOUNT_TYPE, TRANSFER_AMOUNT.toString());

        Float toSavingsBalance = new Float(MINIMUM_OPENING_BALANCE);

        HashMap toSavingsSummaryAfter = this.savingsAccountHelper.getSavingsSummary(savingsId);

        toSavingsBalance += TRANSFER_AMOUNT;

        // Verifying toSavings Account Balance after Account Transfer
        assertEquals("Verifying From Savings Account Balance after Account Transfer", toSavingsBalance,
                toSavingsSummaryAfter.get("accountBalance"));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, now, new JournalEntry(Float.valueOf("20"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("20"), JournalEntry.TransactionType.DEBIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        HashMap fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.get("principalOutstanding")));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.get("interestOutstanding")));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.get("feeChargesOutstanding")));

        // Make refund of 2000
        // max 2270 to refund. Pay 2000 means only principal
        // paid: principal 1980, interest 240, fees 50, penalty 0
        // refund 2000 means paid: principal 0, interest 220, fees 50, penalty 0
        // final String now = Utils.convertDate(fourMonthsfromNowCalendar);

        TRANSFER_AMOUNT = 2000f;

        this.accountTransferHelper.refundLoanByTransfer(now, clientID, loanID, clientID, savingsId, FROM_LOAN_ACCOUNT_TYPE,
                TO_SAVINGS_ACCOUNT_TYPE, TRANSFER_AMOUNT.toString());

        toSavingsSummaryAfter = this.savingsAccountHelper.getSavingsSummary(savingsId);

        toSavingsBalance += TRANSFER_AMOUNT;

        // Verifying toSavings Account Balance after Account Transfer
        assertEquals("Verifying From Savings Account Balance after Account Transfer", toSavingsBalance,
                toSavingsSummaryAfter.get("accountBalance"));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, now, new JournalEntry(Float.valueOf("2000"),
                JournalEntry.TransactionType.CREDIT), new JournalEntry(Float.valueOf("1980"), JournalEntry.TransactionType.DEBIT));

        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, now, new JournalEntry(Float.valueOf("20"),
                JournalEntry.TransactionType.DEBIT));

        loanSchedule.clear();
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        fourthInstallment = loanSchedule.get(4);
        validateNumberForEqual("2020.00", String.valueOf(fourthInstallment.get("totalOutstandingForPeriod")));
        validateNumberForEqual("2000.00", String.valueOf(fourthInstallment.get("principalOutstanding")));
        validateNumberForEqual("20.00", String.valueOf(fourthInstallment.get("interestOutstanding")));
        validateNumberForEqual("0.00", String.valueOf(fourthInstallment.get("feeChargesOutstanding")));

    }

    private Integer createSavingsProduct(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String minOpenningBalance) {
        System.out.println("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        SavingsProductHelper savingsProductHelper = new SavingsProductHelper();

        final String savingsProductJSON = savingsProductHelper
        //
                .withInterestCompoundingPeriodTypeAsDaily()
                //
                .withInterestPostingPeriodTypeAsMonthly()
                //
                .withInterestCalculationPeriodTypeAsDailyBalance()

                .withMinimumOpenningBalance(minOpenningBalance).build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    @Test
    public void testLoanProductConfiguration() {
        final String proposedAmount = "5000";
        JsonObject loanProductConfigurationAsTrue = new JsonObject();
        loanProductConfigurationAsTrue = this.createLoanProductConfigurationDetail(loanProductConfigurationAsTrue, true);

        JsonObject loanProductConfigurationAsFalse = new JsonObject();
        loanProductConfigurationAsFalse = this.createLoanProductConfigurationDetail(loanProductConfigurationAsFalse, false);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder()
                .withAmortizationTypeAsEqualInstallments().withRepaymentTypeAsMonth().withRepaymentAfterEvery("1")
                .withRepaymentStrategy(LoanProductTestBuilder.DEFAULT_STRATEGY).withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeAsDays().withInArrearsTolerance("10").withMoratorium("2", "3")
                .withLoanProductConfiguration(loanProductConfigurationAsTrue).build(null));
        System.out.println("-----------------------LOAN PRODUCT CREATED WITH ATTRIBUTE CONFIGURATION AS TRUE--------------------------"
                + loanProductID);
        Integer loanID = applyForLoanApplicationWithProductConfigurationAsTrue(clientID, loanProductID, proposedAmount);
        System.out.println("------------------------LOAN CREATED WITH ID------------------------------" + loanID);

        loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder().withAmortizationTypeAsEqualInstallments()
                .withRepaymentTypeAsMonth().withRepaymentAfterEvery("1").withRepaymentStrategy(LoanProductTestBuilder.DEFAULT_STRATEGY)
                .withInterestTypeAsDecliningBalance().withInterestCalculationPeriodTypeAsDays().withInArrearsTolerance("10")
                .withMoratorium("2", "3").withLoanProductConfiguration(loanProductConfigurationAsFalse).build(null));
        System.out.println("-------------------LOAN PRODUCT CREATED WITH ATTRIBUTE CONFIGURATION AS FALSE----------------------"
                + loanProductID);
        /*
         * Try to override attribute values in loan account when attribute
         * configurations are set to false at product level
         */
        loanID = applyForLoanApplicationWithProductConfigurationAsFalse(clientID, loanProductID, proposedAmount);
        System.out.println("--------------------------LOAN CREATED WITH ID-------------------------" + loanID);
        this.validateIfValuesAreNotOverridden(loanID, loanProductID);
    }

    /**
     * Test case to verify Loan Foreclosure.
     */
    @Test
    public void testLoanForeclosure() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct(false, NONE);

        List<HashMap> charges = new ArrayList<>();

        Integer flatAmountChargeOne = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
        addCharges(charges, flatAmountChargeOne, "50", "01 October 2011");
        Integer flatAmountChargeTwo = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "100", true));
        addCharges(charges, flatAmountChargeTwo, "100", "15 December 2011");

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, null, "10,000.00");
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("----------------------------------- APPROVE LOAN -----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        System.out.println("----------------------------------- DISBURSE LOAN ----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID, "10,000.00");
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        System.out.println("---------------------------------- Make repayment 1 --------------------------------------");
        this.loanTransactionHelper.makeRepayment("20 October 2011", Float.valueOf("2676.24"), loanID);

        System.out.println("---------------------------------- FORECLOSE LOAN ----------------------------------------");
        this.loanTransactionHelper.forecloseLoan("08 November 2011", loanID);

        // retrieving the loan status
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        // verifying the loan status is closed
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
        // retrieving the loan sub-status
        loanStatusHashMap = LoanStatusChecker.getSubStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        // verifying the loan sub-status is foreclosed
        LoanStatusChecker.verifyLoanAccountForeclosed(loanStatusHashMap);

    }

    private void validateIfValuesAreNotOverridden(Integer loanID, Integer loanProductID) {
        String loanProductDetails = this.loanTransactionHelper.getLoanProductDetails(this.requestSpec, this.responseSpec, loanProductID);
        String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanID);
        List<String> comparisonAttributes = Arrays.asList("amortizationType", "interestType", "transactionProcessingStrategyId",
                "interestCalculationPeriodType", "repaymentFrequencyType", "graceOnPrincipalPayment", "graceOnInterestPayment",
                "inArrearsTolerance", "graceOnArrearsAgeing");

        for (String comparisonAttribute : comparisonAttributes) {
            Object val1 = JsonPath.from(loanProductDetails).get(comparisonAttribute);
            Object val2 = JsonPath.from(loanDetails).get(comparisonAttribute);
            assertEquals(val1, val2);
        }
    }

    private JsonObject createLoanProductConfigurationDetail(JsonObject loanProductConfiguration, Boolean bool) {
        loanProductConfiguration.addProperty("amortizationType", bool);
        loanProductConfiguration.addProperty("interestType", bool);
        loanProductConfiguration.addProperty("transactionProcessingStrategyId", bool);
        loanProductConfiguration.addProperty("interestCalculationPeriodType", bool);
        loanProductConfiguration.addProperty("inArrearsTolerance", bool);
        loanProductConfiguration.addProperty("repaymentEvery", bool);
        loanProductConfiguration.addProperty("graceOnPrincipalAndInterestPayment", bool);
        loanProductConfiguration.addProperty("graceOnArrearsAgeing", bool);
        return loanProductConfiguration;
    }

    private Integer applyForLoanApplicationWithProductConfigurationAsTrue(final Integer clientID, final Integer loanProductID,
            String principal) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withRepaymentEveryAfter("1") //
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("4") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate("1 March 2014") //
                .withSubmittedOnDate("1 March 2014") //
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private Integer applyForLoanApplicationWithProductConfigurationAsFalse(final Integer clientID, final Integer loanProductID,
            String principal) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder()
                //
                .withPrincipal(principal)
                //
                .withRepaymentEveryAfter("2")
                //
                .withAmortizationTypeAsEqualPrincipalPayments().withRepaymentFrequencyTypeAsWeeks()
                .withwithRepaymentStrategy(LoanProductTestBuilder.RBI_INDIA_STRATEGY).withInterestTypeAsFlatBalance()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withPrincipalGrace("1").withInterestGrace("1")
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("4") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate("1 March 2014") //
                .withSubmittedOnDate("1 March 2014") //
                .build(clientID.toString(), loanProductID.toString(), null);

        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    public Integer getDayOfWeek(Calendar date) {
        Integer dayOfWeek = null;
        if (null != date) {
            dayOfWeek = date.get(Calendar.DAY_OF_WEEK) - 1;
            if (dayOfWeek.compareTo(0) == 0) {
                dayOfWeek = 7;
            }
        }
        return dayOfWeek;
    }

    public Integer getDayOfMonth(Calendar date) {
        Integer dayOfMonth = null;
        if (null != date) {
            dayOfMonth = date.get(Calendar.DAY_OF_MONTH);
            if (dayOfMonth.compareTo(28) > 0) {
                dayOfMonth = 28;
            }
        }
        return dayOfMonth;
    }
}