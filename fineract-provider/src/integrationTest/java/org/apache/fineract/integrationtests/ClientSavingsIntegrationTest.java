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
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.TaxComponentHelper;
import org.apache.fineract.integrationtests.common.TaxGroupHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

/**
 * Client Savings Integration Test for checking Savings Application.
 */
@SuppressWarnings({ "rawtypes", "unused" })
public class ClientSavingsIntegrationTest {

    public static final String DEPOSIT_AMOUNT = "2000";
    public static final String WITHDRAW_AMOUNT = "1000";
    public static final String WITHDRAW_AMOUNT_ADJUSTED = "500";
    public static final String MINIMUM_OPENING_BALANCE = "1000.0";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private SavingsAccountHelper savingsAccountHelper;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testSavingsAccount() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
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

        final HashMap summaryBefore = this.savingsAccountHelper.getSavingsSummary(savingsId);
        this.savingsAccountHelper.calculateInterestForSavings(savingsId);
        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals(summaryBefore, summary);

        this.savingsAccountHelper.postInterestForSavings(savingsId);
        summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        Assert.assertFalse(summaryBefore.equals(summary));
        
        final Object savingsInterest = this.savingsAccountHelper.getSavingsInterest(savingsId);
        // verifySavingsInterest(savingsInterest);
    }
    
    @Test
    public void testSavingsAccountWithMinBalanceForInterestCalculation() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = "5000";
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
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

        final HashMap summaryBefore = this.savingsAccountHelper.getSavingsSummary(savingsId);
        this.savingsAccountHelper.calculateInterestForSavings(savingsId);
        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals(summaryBefore, summary);

        this.savingsAccountHelper.postInterestForSavings(savingsId);
        summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals(summaryBefore, summary);

        final Object savingsInterest = this.savingsAccountHelper.getSavingsInterest(savingsId);
        Assert.assertNull(savingsInterest);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSavingsAccount_CLOSE_APPLICATION() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(400).build();
        final SavingsAccountHelper validationErrorHelper = new SavingsAccountHelper(this.requestSpec, errorResponse);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = "1000.0";
        final String enforceMinRequiredBalance = "true";
        final boolean allowOverdraft = false;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assert.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Calendar todaysDate = Calendar.getInstance();
        final String CLOSEDON_DATE = dateFormat.format(todaysDate.getTime());
        String withdrawBalance = "false";
        ArrayList<HashMap> savingsAccountErrorData = (ArrayList<HashMap>) validationErrorHelper.closeSavingsAccountAndGetBackRequiredField(
                savingsId, withdrawBalance, CommonConstants.RESPONSE_ERROR, CLOSEDON_DATE);
        assertEquals("validation.msg.savingsaccount.close.results.in.balance.not.zero",
                savingsAccountErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        withdrawBalance = "true";
        savingsStatusHashMap = this.savingsAccountHelper.closeSavingsAccount(savingsId, withdrawBalance);
        SavingsStatusChecker.verifySavingsAccountIsClosed(savingsStatusHashMap);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSavingsAccount_WITH_ENFORCE_MIN_BALANCE() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(403).build();
        final SavingsAccountHelper validationErrorHelper = new SavingsAccountHelper(this.requestSpec, errorResponse);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = "1500.0";
        final String openningBalance = "1600";
        final String enforceMinRequiredBalance = "true";
        final boolean allowOverdraft = false;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, openningBalance,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assert.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        final Integer savingsActivationChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsActivationFeeJSON());
        Assert.assertNotNull(savingsActivationChargeId);

        this.savingsAccountHelper.addChargesForSavings(savingsId, savingsActivationChargeId, true);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        Float balance = new Float(openningBalance);
        Float chargeAmt = 100f;
        balance -= chargeAmt;
        assertEquals("Verifying opening Balance", balance, summary.get("accountBalance"));

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        Calendar todaysDate = Calendar.getInstance();
        final String TRANSACTION_DATE = dateFormat.format(todaysDate.getTime());
        final String withdrawAmt = "800";
        ArrayList<HashMap> savingsAccountErrorData = (ArrayList<HashMap>) validationErrorHelper.withdrawalFromSavingsAccount(savingsId,
                withdrawAmt, TRANSACTION_DATE, CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.savingsaccount.transaction.insufficient.account.balance",
                savingsAccountErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
                TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap depositTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, depositTransactionId);
        balance += new Float(DEPOSIT_AMOUNT);
        assertEquals("Verifying Deposit Amount", new Float(DEPOSIT_AMOUNT), depositTransaction.get("amount"));
        assertEquals("Verifying Balance after Deposit", balance, depositTransaction.get("runningBalance"));

        Integer withdrawTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, withdrawAmt,
                TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        balance -= new Float(withdrawAmt);
        assertEquals("Verifying Withdrawal Amount", new Float(withdrawAmt), withdrawTransaction.get("amount"));
        assertEquals("Verifying Balance after Withdrawal", balance, withdrawTransaction.get("runningBalance"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSavingsAccount_DELETE_APPLICATION() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        SavingsAccountHelper savingsAccountHelperValidationError = new SavingsAccountHelper(this.requestSpec,
                new ResponseSpecBuilder().build());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assert.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        List<HashMap> error1 = (List<HashMap>) savingsAccountHelperValidationError.deleteSavingsApplication(savingsId,
                CommonConstants.RESPONSE_ERROR);
        assertEquals("validation.msg.savingsaccount.delete.not.in.submittedandpendingapproval.state",
                error1.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        savingsStatusHashMap = this.savingsAccountHelper.undoApproval(savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        this.savingsAccountHelper.deleteSavingsApplication(savingsId, CommonConstants.RESPONSE_RESOURCE_ID);

        List<HashMap> error = savingsAccountHelperValidationError.getSavingsCollectionAttribute(savingsId, CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.saving.account.id.invalid", error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSavingsAccount_REJECT_APPLICATION() {

        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        SavingsAccountHelper savingsAccountHelperValidationError = new SavingsAccountHelper(this.requestSpec,
                new ResponseSpecBuilder().build());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assert.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        List<HashMap> error1 = savingsAccountHelperValidationError.rejectApplicationWithErrorCode(savingsId,
                SavingsAccountHelper.CREATED_DATE_PLUS_ONE);
        assertEquals("validation.msg.savingsaccount.reject.not.in.submittedandpendingapproval.state",
                error1.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        savingsStatusHashMap = this.savingsAccountHelper.undoApproval(savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        error1 = savingsAccountHelperValidationError.rejectApplicationWithErrorCode(savingsId, SavingsAccountHelper.getFutureDate());
        assertEquals("validation.msg.savingsaccount.reject.cannot.be.a.future.date",
                error1.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        error1 = savingsAccountHelperValidationError.rejectApplicationWithErrorCode(savingsId, SavingsAccountHelper.CREATED_DATE_MINUS_ONE);
        assertEquals("validation.msg.savingsaccount.reject.cannot.be.before.submittal.date",
                error1.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        savingsStatusHashMap = this.savingsAccountHelper.rejectApplication(savingsId);
        SavingsStatusChecker.verifySavingsIsRejected(savingsStatusHashMap);

    }

    @Test
    public void testSavingsAccount_WITHDRAW_APPLICATION() {

        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assert.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.withdrawApplication(savingsId);
        SavingsStatusChecker.verifySavingsIsWithdrawn(savingsStatusHashMap);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSavingsAccountTransactions() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        SavingsAccountHelper savingsAccountHelperValidationError = new SavingsAccountHelper(this.requestSpec,
                new ResponseSpecBuilder().build());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assert.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        List<HashMap> error = (List) savingsAccountHelperValidationError.withdrawalFromSavingsAccount(savingsId, "100",
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.savingsaccount.transaction.account.is.not.active",
                error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        error = (List) savingsAccountHelperValidationError.depositToSavingsAccount(savingsId, "100", SavingsAccountHelper.TRANSACTION_DATE,
                CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.savingsaccount.transaction.account.is.not.active",
                error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        Float balance = new Float(MINIMUM_OPENING_BALANCE);
        assertEquals("Verifying opening Balance", balance, summary.get("accountBalance"));

        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap depositTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, depositTransactionId);
        balance += new Float(DEPOSIT_AMOUNT);
        assertEquals("Verifying Deposit Amount", new Float(DEPOSIT_AMOUNT), depositTransaction.get("amount"));
        assertEquals("Verifying Balance after Deposit", balance, depositTransaction.get("runningBalance"));

        Integer withdrawTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, WITHDRAW_AMOUNT,
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        balance -= new Float(WITHDRAW_AMOUNT);
        assertEquals("Verifying Withdrawal Amount", new Float(WITHDRAW_AMOUNT), withdrawTransaction.get("amount"));
        assertEquals("Verifying Balance after Withdrawal", balance, withdrawTransaction.get("runningBalance"));

        Integer newWithdrawTransactionId = this.savingsAccountHelper.updateSavingsAccountTransaction(savingsId, withdrawTransactionId,
                WITHDRAW_AMOUNT_ADJUSTED);
        HashMap newWithdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, newWithdrawTransactionId);
        balance = balance + new Float(WITHDRAW_AMOUNT) - new Float(WITHDRAW_AMOUNT_ADJUSTED);
        assertEquals("Verifying adjusted Amount", new Float(WITHDRAW_AMOUNT_ADJUSTED), newWithdrawTransaction.get("amount"));
        assertEquals("Verifying Balance after adjust", balance, newWithdrawTransaction.get("runningBalance"));
        summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals("Verifying Adjusted Balance", balance, summary.get("accountBalance"));
        withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        Assert.assertTrue((Boolean) withdrawTransaction.get("reversed"));

        this.savingsAccountHelper.undoSavingsAccountTransaction(savingsId, newWithdrawTransactionId);
        newWithdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        Assert.assertTrue((Boolean) newWithdrawTransaction.get("reversed"));
        summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        balance += new Float(WITHDRAW_AMOUNT_ADJUSTED);
        assertEquals("Verifying Balance After Undo Transaction", balance, summary.get("accountBalance"));

        error = (List) savingsAccountHelperValidationError.withdrawalFromSavingsAccount(savingsId, "5000",
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.savingsaccount.transaction.insufficient.account.balance",
                error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        error = (List) savingsAccountHelperValidationError.withdrawalFromSavingsAccount(savingsId, "5000",
                SavingsAccountHelper.getFutureDate(), CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.savingsaccount.transaction.in.the.future", error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        error = (List) savingsAccountHelperValidationError.depositToSavingsAccount(savingsId, "5000", SavingsAccountHelper.getFutureDate(),
                CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.savingsaccount.transaction.in.the.future", error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        error = (List) savingsAccountHelperValidationError.withdrawalFromSavingsAccount(savingsId, "5000",
                SavingsAccountHelper.CREATED_DATE_MINUS_ONE, CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.savingsaccount.transaction.before.activation.date",
                error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        error = (List) savingsAccountHelperValidationError.depositToSavingsAccount(savingsId, "5000",
                SavingsAccountHelper.CREATED_DATE_MINUS_ONE, CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.savingsaccount.transaction.before.activation.date",
                error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSavingsAccountCharges() {

        final ResponseSpecification erroResponseSpec = new ResponseSpecBuilder().build();
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        final SavingsAccountHelper validationErrorHelper = new SavingsAccountHelper(this.requestSpec, erroResponseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assert.assertNotNull(savingsProductID);

        Assert.assertNotNull(savingsProductID);
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        final Integer withdrawalChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsWithdrawalFeeJSON());
        Assert.assertNotNull(withdrawalChargeId);

        this.savingsAccountHelper.addChargesForSavings(savingsId, withdrawalChargeId, false);
        ArrayList<HashMap> chargesPendingState = this.savingsAccountHelper.getSavingsCharges(savingsId);
        Assert.assertEquals(1, chargesPendingState.size());

        Integer savingsChargeId = (Integer) chargesPendingState.get(0).get("id");
        HashMap chargeChanges = this.savingsAccountHelper.updateCharges(savingsChargeId, savingsId);
        Assert.assertTrue(chargeChanges.containsKey("amount"));

        Integer deletedChargeId = this.savingsAccountHelper.deleteCharge(savingsChargeId, savingsId);
        assertEquals(savingsChargeId, deletedChargeId);

        chargesPendingState = this.savingsAccountHelper.getSavingsCharges(savingsId);
        Assert.assertTrue(chargesPendingState == null || chargesPendingState.size() == 0);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        final Integer chargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec, ChargesHelper.getSavingsAnnualFeeJSON());
        Assert.assertNotNull(chargeId);

        ArrayList<HashMap> charges = this.savingsAccountHelper.getSavingsCharges(savingsId);
        Assert.assertTrue(charges == null || charges.size() == 0);

        this.savingsAccountHelper.addChargesForSavings(savingsId, chargeId, true);
        charges = this.savingsAccountHelper.getSavingsCharges(savingsId);
        Assert.assertEquals(1, charges.size());

        HashMap savingsChargeForPay = charges.get(0);
        Integer annualSavingsChargeId = (Integer) savingsChargeForPay.get("id");

        ArrayList<HashMap> savingsAccountErrorData = (ArrayList<HashMap>) validationErrorHelper.inactivateCharge(annualSavingsChargeId,
                savingsId, CommonConstants.RESPONSE_ERROR);
        assertEquals("validation.msg.savingsaccountcharge.inactivation.of.charge.not.allowed.when.charge.is.due", savingsAccountErrorData
                .get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        SimpleDateFormat sdf = new SimpleDateFormat(CommonConstants.dateFormat, Locale.US);
        Calendar cal = Calendar.getInstance();
        List dates = (List) savingsChargeForPay.get("dueDate");
        cal.set(Calendar.YEAR, (Integer) dates.get(0));
        cal.set(Calendar.MONTH, (Integer) dates.get(1) - 1);
        cal.set(Calendar.DAY_OF_MONTH, (Integer) dates.get(2));
        int n = 0;
        Calendar current = Calendar.getInstance();
        while (cal.compareTo(current) < 0) {
            n++;
            cal.set(Calendar.YEAR, (Integer) dates.get(0) + n);
        }
        cal.set(Calendar.YEAR, (Integer) dates.get(0));
        cal.set(Calendar.MONTH, (Integer) dates.get(1) - 1);
        cal.set(Calendar.DAY_OF_MONTH, (Integer) dates.get(2));

        for (int i = 1; i <= n; i++) {
            this.savingsAccountHelper.payCharge((Integer) savingsChargeForPay.get("id"), savingsId,
                    ((Float) savingsChargeForPay.get("amount")).toString(), sdf.format(cal.getTime()));
            HashMap paidCharge = this.savingsAccountHelper.getSavingsCharge(savingsId, (Integer) savingsChargeForPay.get("id"));
            Float expectedValue = (Float) savingsChargeForPay.get("amount") * i;
            assertEquals(expectedValue, paidCharge.get("amountPaid"));
            cal.set(Calendar.YEAR, (Integer) dates.get(0) + i);
        }

        Integer inactivatedChargeId = (Integer) this.savingsAccountHelper.inactivateCharge(annualSavingsChargeId, savingsId,
                CommonConstants.RESPONSE_RESOURCE_ID);
        assertEquals("Inactivated Savings Charges Id", annualSavingsChargeId, inactivatedChargeId);

        final Integer monthlyFeechargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsMonthlyFeeJSON());
        Assert.assertNotNull(monthlyFeechargeId);

        this.savingsAccountHelper.addChargesForSavings(savingsId, monthlyFeechargeId, true);
        charges = this.savingsAccountHelper.getSavingsCharges(savingsId);
        Assert.assertEquals(2, charges.size());

        HashMap savingsChargeForWaive = charges.get(1);
        final Integer monthlySavingsCharge = (Integer) savingsChargeForWaive.get("id");

        savingsAccountErrorData = (ArrayList<HashMap>) validationErrorHelper.inactivateCharge(monthlySavingsCharge, savingsId,
                CommonConstants.RESPONSE_ERROR);
        assertEquals("validation.msg.savingsaccountcharge.inactivation.of.charge.not.allowed.when.charge.is.due", savingsAccountErrorData
                .get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        this.savingsAccountHelper.waiveCharge((Integer) savingsChargeForWaive.get("id"), savingsId);
        HashMap waiveCharge = this.savingsAccountHelper.getSavingsCharge(savingsId, (Integer) savingsChargeForWaive.get("id"));
        assertEquals(savingsChargeForWaive.get("amount"), waiveCharge.get("amountWaived"));

        this.savingsAccountHelper.waiveCharge((Integer) savingsChargeForWaive.get("id"), savingsId);
        waiveCharge = this.savingsAccountHelper.getSavingsCharge(savingsId, (Integer) savingsChargeForWaive.get("id"));
        BigDecimal totalWaiveAmount = BigDecimal.valueOf(Double.valueOf((Float) savingsChargeForWaive.get("amount")));
        totalWaiveAmount = totalWaiveAmount.add(totalWaiveAmount);
        assertEquals(totalWaiveAmount.floatValue(), waiveCharge.get("amountWaived"));

        final Integer weeklyFeeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsWeeklyFeeJSON());
        Assert.assertNotNull(weeklyFeeId);

        this.savingsAccountHelper.addChargesForSavings(savingsId, weeklyFeeId, true);
        charges = this.savingsAccountHelper.getSavingsCharges(savingsId);
        Assert.assertEquals(3, charges.size());

        savingsChargeForPay = charges.get(2);
        final Integer weeklySavingsFeeId = (Integer) savingsChargeForPay.get("id");

        savingsAccountErrorData = (ArrayList<HashMap>) validationErrorHelper.inactivateCharge(weeklySavingsFeeId, savingsId,
                CommonConstants.RESPONSE_ERROR);
        assertEquals("validation.msg.savingsaccountcharge.inactivation.of.charge.not.allowed.when.charge.is.due", savingsAccountErrorData
                .get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        cal = Calendar.getInstance();
        dates = (List) savingsChargeForPay.get("dueDate");
        cal.set(Calendar.YEAR, (Integer) dates.get(0));
        cal.set(Calendar.MONTH, (Integer) dates.get(1) - 1);
        cal.set(Calendar.DAY_OF_MONTH, (Integer) dates.get(2));

        // Depositing huge amount as scheduler job deducts the fee amount
        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100000",
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(depositTransactionId);

        this.savingsAccountHelper.payCharge((Integer) savingsChargeForPay.get("id"), savingsId,
                ((Float) savingsChargeForPay.get("amount")).toString(), sdf.format(cal.getTime()));
        HashMap paidCharge = this.savingsAccountHelper.getSavingsCharge(savingsId, (Integer) savingsChargeForPay.get("id"));
        assertEquals(savingsChargeForPay.get("amount"), paidCharge.get("amountPaid"));
        List nextDueDates = (List) paidCharge.get("dueDate");
        LocalDate nextDueDate = new LocalDate((Integer) nextDueDates.get(0), (Integer) nextDueDates.get(1), (Integer) nextDueDates.get(2));
        LocalDate expectedNextDueDate = new LocalDate((Integer) dates.get(0), (Integer) dates.get(1), (Integer) dates.get(2))
                .plusWeeks((Integer) paidCharge.get("feeInterval"));
        assertEquals(expectedNextDueDate, nextDueDate);
    }

    /***
     * Test case for overdraft account functionality. Open account with zero
     * balance, perform transactions then post interest and verify posted
     * interest
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSavingsAccountWithOverdraft() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(400).build();
        final SavingsAccountHelper validationErrorHelper = new SavingsAccountHelper(this.requestSpec, errorResponse);

        /***
         * Create a client to apply for savings account (overdraft account).
         */
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;

        /***
         * Create savings product with zero opening balance and overdraft
         * enabled
         */
        final String zeroOpeningBalance = "0.0";
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = true;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, zeroOpeningBalance,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assert.assertNotNull(savingsProductID);

        /***
         * Apply for Savings account
         */
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap modifications = this.savingsAccountHelper.updateSavingsAccount(clientID, savingsProductID, savingsId,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertTrue(modifications.containsKey("submittedOnDate"));

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        /***
         * Approve the savings account
         */
        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        todaysDate.set(Calendar.DAY_OF_MONTH, 1);
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final Integer lastDayOfMonth = todaysDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        todaysDate.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
        final String TRANSACTION_DATE = dateFormat.format(todaysDate.getTime());

        /***
         * Activate the application and verify account status
         * 
         * @param activationDate
         *            this value is every time first day of previous month
         */
        savingsStatusHashMap = activateSavingsAccount(savingsId, ACTIVATION_DATE);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        /***
         * Verify the account summary
         */
        final HashMap summaryBefore = this.savingsAccountHelper.getSavingsSummary(savingsId);
        this.savingsAccountHelper.calculateInterestForSavings(savingsId);
        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals(summaryBefore, summary);

        Float balance = Float.valueOf(zeroOpeningBalance);

        /***
         * Perform withdraw transaction, verify account balance(account balance
         * will go to negative as no deposits are there prior to this
         * transaction)
         */
        Integer withdrawTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, WITHDRAW_AMOUNT,
                ACTIVATION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        balance -= new Float(WITHDRAW_AMOUNT);
        assertEquals("Verifying Withdrawal Amount", new Float(WITHDRAW_AMOUNT), withdrawTransaction.get("amount"));
        assertEquals("Verifying Balance after Withdrawal", balance, withdrawTransaction.get("runningBalance"));

        /***
         * Perform Deposit transaction on last day of month and verify account
         * balance.
         * 
         * @param transactionDate
         *            this value is every time last day of previous month
         */
        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
                TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap depositTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, depositTransactionId);
        balance += new Float(DEPOSIT_AMOUNT);
        assertEquals("Verifying Deposit Amount", new Float(DEPOSIT_AMOUNT), depositTransaction.get("amount"));
        assertEquals("Verifying Balance after Deposit", balance, depositTransaction.get("runningBalance"));

        /***
         * Perform Post interest transaction and verify the posted amount
         */
        this.savingsAccountHelper.postInterestForSavings(savingsId);
        HashMap accountDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
        summary = (HashMap) accountDetails.get("summary");
        Float actualInterestPosted = Float.valueOf(summary.get("totalInterestPosted").toString());

        /***
         * Calculate expected interest to be posted, interest should be posted
         * for one day only because deposit transaction happened on last day of
         * month before this account balance is negative.
         */
        final Float nominalAnnualInterest = Float.valueOf(accountDetails.get("nominalAnnualInterestRate").toString());
        final HashMap interestCalculationDaysInYearType = (HashMap) accountDetails.get("interestCalculationDaysInYearType");
        final Integer daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get("id").toString());
        double interestRateInFraction = (nominalAnnualInterest / 100);
        double perDay = (double) 1 / (daysInYear);
        double interestPerDay = interestRateInFraction * perDay;
        Float interestPosted = (float) (interestPerDay * balance * 1);

        /***
         * Apply rounding on interestPosted, actualInterestPosted and verify
         * both are same
         */
        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern("#.###");
        interestPosted = new Float(decimalFormat.format(interestPosted));
        actualInterestPosted = new Float(decimalFormat.format(actualInterestPosted));
        assertEquals("Verifying interest posted", interestPosted, actualInterestPosted);

        todaysDate = Calendar.getInstance();
        final String CLOSEDON_DATE = dateFormat.format(todaysDate.getTime());
        String withdrawBalance = "false";
        ArrayList<HashMap> savingsAccountErrorData = (ArrayList<HashMap>) validationErrorHelper.closeSavingsAccountAndGetBackRequiredField(
                savingsId, withdrawBalance, CommonConstants.RESPONSE_ERROR, CLOSEDON_DATE);
        assertEquals("validation.msg.savingsaccount.close.results.in.balance.not.zero",
                savingsAccountErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));
    }
    
	@SuppressWarnings("unchecked")
	@Test
	public void testSavingsAccountPostInterestOnLastDayWithOverdraft() {
		this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec,
				this.responseSpec);

		/***
		 * Create a client to apply for savings account (overdraft account).
		 */
		final Integer clientID = ClientHelper.createClient(this.requestSpec,
				this.responseSpec);
		Assert.assertNotNull(clientID);
		final String minBalanceForInterestCalculation = null;

		/***
		 * Create savings product with zero opening balance and overdraft
		 * enabled
		 */
		final String zeroOpeningBalance = "0.0";
		final String minRequiredBalance = null;
		final String enforceMinRequiredBalance = "false";
		final boolean allowOverdraft = true;
		final Integer savingsProductID = createSavingsProduct(this.requestSpec,
				this.responseSpec, zeroOpeningBalance,
				minBalanceForInterestCalculation, minRequiredBalance,
				enforceMinRequiredBalance, allowOverdraft);
		Assert.assertNotNull(savingsProductID);

		/***
		 * Apply for Savings account
		 */
		final Integer savingsId = this.savingsAccountHelper
				.applyForSavingsApplication(clientID, savingsProductID,
						ACCOUNT_TYPE_INDIVIDUAL);
		Assert.assertNotNull(savingsProductID);

		HashMap modifications = this.savingsAccountHelper.updateSavingsAccount(
				clientID, savingsProductID, savingsId, ACCOUNT_TYPE_INDIVIDUAL);
		Assert.assertTrue(modifications.containsKey("submittedOnDate"));

		HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(
				this.requestSpec, this.responseSpec, savingsId);
		SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

		/***
		 * Approve the savings account
		 */
		savingsStatusHashMap = this.savingsAccountHelper
				.approveSavings(savingsId);
		SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

		DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
		Calendar todaysDate = Calendar.getInstance();
		todaysDate.add(Calendar.MONTH, -1);

		final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());

		/***
		 * Activate the application and verify account status
		 * 
		 * @param activationDate
		 *            this value is every time first day of previous month
		 */
		savingsStatusHashMap = activateSavingsAccount(savingsId,
				ACTIVATION_DATE);
		SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
		/***
		 * Verify the account summary
		 */
		final HashMap summaryBefore = this.savingsAccountHelper
				.getSavingsSummary(savingsId);
		this.savingsAccountHelper.calculateInterestForSavings(savingsId);
		HashMap summary = this.savingsAccountHelper
				.getSavingsSummary(savingsId);
		assertEquals(summaryBefore, summary);

		final Integer lastDayOfMonth = todaysDate
				.getActualMaximum(Calendar.DAY_OF_MONTH);
		todaysDate.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
		final String WITHDRAWAL_DATE = dateFormat.format(todaysDate.getTime());
		Float balance = Float.valueOf(zeroOpeningBalance);

		DateFormat transactionDateFormat = new SimpleDateFormat("dd MMMM yyyy",
				Locale.US);
		Calendar transactionDate = Calendar.getInstance();
		transactionDate.set(Calendar.DAY_OF_MONTH, 2);
		String TRANSACTION_DATE = dateFormat.format(transactionDate.getTime());

		/***
		 * Perform Deposit transaction on last day of month and verify account
		 * balance.
		 * 
		 * @param transactionDate
		 *            this value is every time last day of previous month
		 */
		Integer depositTransactionId = (Integer) this.savingsAccountHelper
				.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
						WITHDRAWAL_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
		HashMap depositTransaction = this.savingsAccountHelper
				.getSavingsTransaction(savingsId, depositTransactionId);
		balance += new Float(DEPOSIT_AMOUNT);
		assertEquals("Verifying Deposit Amount", new Float(DEPOSIT_AMOUNT),
				depositTransaction.get("amount"));
		assertEquals("Verifying Balance after Deposit", balance,
				depositTransaction.get("runningBalance"));

		/***
		 * Calculate expected interest to be posted, interest should be posted
		 * for one day only because deposit transaction happened on last day of
		 * month before this account balance is negative.
		 */
		this.savingsAccountHelper.postInterestForSavings(savingsId);
		HashMap accountDetails = this.savingsAccountHelper
				.getSavingsDetails(savingsId);
		summary = (HashMap) accountDetails.get("summary");
		Float accountDetailsPostInterestPosted = Float.valueOf(summary.get(
				"totalInterestPosted").toString());

		Float nominalAnnualInterest = Float.valueOf(accountDetails.get(
				"nominalAnnualInterestRate").toString());
		HashMap interestCalculationDaysInYearType = (HashMap) accountDetails
				.get("interestCalculationDaysInYearType");
		Integer daysInYear = Integer.valueOf(interestCalculationDaysInYearType
				.get("id").toString());
		double interestRateInFraction = (nominalAnnualInterest / 100);
		double perDay = (double) 1 / (daysInYear);
		double interestPerDay = interestRateInFraction * perDay;
		Float interestPosted = (float) (interestPerDay * balance * 1);

		/***
		 * Apply rounding on interestPosted, actualInterestPosted and verify
		 * both are same
		 */
		DecimalFormat decimalFormat = new DecimalFormat("",
				new DecimalFormatSymbols(Locale.US));
		decimalFormat.applyPattern("#.###");
		interestPosted = new Float(decimalFormat.format(interestPosted));
		accountDetailsPostInterestPosted = new Float(
				decimalFormat.format(accountDetailsPostInterestPosted));
		assertEquals("Verifying interest posted", interestPosted,
				accountDetailsPostInterestPosted);

		this.savingsAccountHelper.postInterestAsOnSavings(savingsId,
				TRANSACTION_DATE);
		HashMap accountTransactionDetails = this.savingsAccountHelper
				.getSavingsDetails(savingsId);
		summary = (HashMap) accountDetails.get("summary");
		Float accountDetailsPostInterest = Float.valueOf(summary.get(
				"totalInterestPosted").toString());

		nominalAnnualInterest = Float.valueOf(accountDetails.get(
				"nominalAnnualInterestRate").toString());
		interestCalculationDaysInYearType = (HashMap) accountDetails
				.get("interestCalculationDaysInYearType");
		daysInYear = Integer.valueOf(interestCalculationDaysInYearType
				.get("id").toString());
		interestRateInFraction = (nominalAnnualInterest / 100);
		perDay = (double) 1 / (daysInYear);
		interestPerDay = interestRateInFraction * perDay;
		interestPosted = (float) (interestPerDay * balance * 1);

		/***
		 * Apply rounding on interestPosted, actualInterestPosted and verify
		 * both are same
		 */
		decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(
				Locale.US));
		decimalFormat.applyPattern("#.###");
		interestPosted = new Float(decimalFormat.format(interestPosted));
		accountDetailsPostInterestPosted = new Float(
				decimalFormat.format(accountDetailsPostInterestPosted));
		assertEquals("Verifying interest posted", interestPosted,
				accountDetailsPostInterestPosted);
		System.out
				.println("-----Post Interest As on Successfully Worked----------");

		transactionDate.set(Calendar.DAY_OF_MONTH, 3);
		TRANSACTION_DATE = dateFormat.format(transactionDate.getTime());

		this.savingsAccountHelper.postInterestAsOnSavings(savingsId,
				TRANSACTION_DATE);
		accountTransactionDetails = this.savingsAccountHelper
				.getSavingsDetails(savingsId);
		summary = (HashMap) accountDetails.get("summary");
		accountDetailsPostInterest = Float.valueOf(summary.get(
				"totalInterestPosted").toString());

		nominalAnnualInterest = Float.valueOf(accountDetails.get(
				"nominalAnnualInterestRate").toString());
		interestCalculationDaysInYearType = (HashMap) accountDetails
				.get("interestCalculationDaysInYearType");
		daysInYear = Integer.valueOf(interestCalculationDaysInYearType
				.get("id").toString());
		interestRateInFraction = (nominalAnnualInterest / 100);
		perDay = (double) 1 / (daysInYear);
		interestPerDay = interestRateInFraction * perDay;
		interestPosted = (float) (interestPerDay * balance * 1);

		/***
		 * Apply rounding on interestPosted, actualInterestPosted and verify
		 * both are same
		 */
		decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(
				Locale.US));
		decimalFormat.applyPattern("#.###");
		interestPosted = new Float(decimalFormat.format(interestPosted));
		accountDetailsPostInterestPosted = new Float(
				decimalFormat.format(accountDetailsPostInterestPosted));
		assertEquals("Verifying interest posted", interestPosted,
				accountDetailsPostInterestPosted);
		System.out
				.println("-----Post Interest As on Successfully Worked-------");

		DateFormat transactionFormat = new SimpleDateFormat("dd MMMM yyyy",
				Locale.US);
		Calendar transactionCalendarDateFormat = Calendar.getInstance();
		transactionCalendarDateFormat.add(Calendar.DAY_OF_MONTH, 0);
		transactionDate.set(Calendar.DAY_OF_MONTH, 22);
		TRANSACTION_DATE = dateFormat.format(transactionDate.getTime());
		if (Calendar.DAY_OF_MONTH >= 22) {
			this.savingsAccountHelper.postInterestAsOnSavings(savingsId,
					TRANSACTION_DATE);
			accountTransactionDetails = this.savingsAccountHelper
					.getSavingsDetails(savingsId);
			summary = (HashMap) accountTransactionDetails.get("summary");
			accountDetailsPostInterest = Float.valueOf(summary.get(
					"totalInterestPosted").toString());

			nominalAnnualInterest = Float.valueOf(accountDetails.get(
					"nominalAnnualInterestRate").toString());
			interestCalculationDaysInYearType = (HashMap) accountDetails
					.get("interestCalculationDaysInYearType");
			daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get(
					"id").toString());
			interestRateInFraction = (nominalAnnualInterest / 100);
			perDay = (double) 1 / (daysInYear);
			interestPerDay = interestRateInFraction * perDay;
			interestPosted = (float) (interestPerDay * balance * 19);

			/***
			 * Apply rounding on interestPosted, actualInterestPosted and verify
			 * both are same
			 */
			decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(
					Locale.US));
			decimalFormat.applyPattern("#.###");
			interestPosted = new Float(decimalFormat.format(interestPosted));
			accountDetailsPostInterestPosted = new Float(
					decimalFormat.format(accountDetailsPostInterestPosted));
			assertEquals("Verifying interest posted", interestPosted,
					accountDetailsPostInterestPosted);
			System.out
					.println("-----Post Interest As on Successfully Worked----------");
		}
		DateFormat lastTransactionDateFormat = new SimpleDateFormat(
				"dd MMMM yyyy", Locale.US);
		Calendar postedLastDate = Calendar.getInstance();
		int numberOfDateOfMonth = postedLastDate
				.getActualMaximum(Calendar.DAY_OF_MONTH);
		TRANSACTION_DATE = lastTransactionDateFormat.format(transactionDate
				.getTime());

		if (Calendar.DAY_OF_MONTH == numberOfDateOfMonth) {

			this.savingsAccountHelper.postInterestAsOnSavings(savingsId,
					TRANSACTION_DATE);
			accountTransactionDetails = this.savingsAccountHelper
					.getSavingsDetails(savingsId);
			summary = (HashMap) accountTransactionDetails.get("summary");
			accountDetailsPostInterest = Float.valueOf(summary.get(
					"totalInterestPosted").toString());

			nominalAnnualInterest = Float.valueOf(accountDetails.get(
					"nominalAnnualInterestRate").toString());
			interestCalculationDaysInYearType = (HashMap) accountDetails
					.get("interestCalculationDaysInYearType");
			daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get(
					"id").toString());
			interestRateInFraction = (nominalAnnualInterest / 100);
			perDay = (double) 1 / (daysInYear);
			interestPerDay = interestRateInFraction * perDay;
			interestPosted = (float) (interestPerDay * balance * 8);

			/***
			 * Apply rounding on interestPosted, actualInterestPosted and verify
			 * both are same
			 */
			decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(
					Locale.US));
			decimalFormat.applyPattern("#.###");
			interestPosted = new Float(decimalFormat.format(interestPosted));
			accountDetailsPostInterestPosted = new Float(
					decimalFormat.format(accountDetailsPostInterestPosted));
			assertEquals("Verifying interest posted", interestPosted,
					accountDetailsPostInterestPosted);
			System.out
					.println("-----Post Interest As on Successfully Worked----------");

		}
		transactionDate.set(Calendar.DAY_OF_MONTH, 1);
		TRANSACTION_DATE = dateFormat.format(transactionDate.getTime());
		this.savingsAccountHelper.postInterestAsOnSavings(savingsId,
				TRANSACTION_DATE);
		accountTransactionDetails = this.savingsAccountHelper
				.getSavingsDetails(savingsId);
		summary = (HashMap) accountTransactionDetails.get("summary");
		accountDetailsPostInterest = Float.valueOf(summary.get(
				"totalInterestPosted").toString());

		nominalAnnualInterest = Float.valueOf(accountDetails.get(
				"nominalAnnualInterestRate").toString());
		interestCalculationDaysInYearType = (HashMap) accountDetails
				.get("interestCalculationDaysInYearType");
		daysInYear = Integer.valueOf(interestCalculationDaysInYearType
				.get("id").toString());
		interestRateInFraction = (nominalAnnualInterest / 100);
		perDay = (double) 1 / (daysInYear);
		interestPerDay = interestRateInFraction * perDay;
		interestPosted = (float) (interestPerDay * balance * 1);

		/***
		 * Apply rounding on interestPosted, actualInterestPosted and verify
		 * both are same
		 */
		decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(
				Locale.US));
		decimalFormat.applyPattern("#.###");
		interestPosted = new Float(decimalFormat.format(interestPosted));
		accountDetailsPostInterestPosted = new Float(
				decimalFormat.format(accountDetailsPostInterestPosted));
		assertEquals("Verifying interest posted", interestPosted,
				accountDetailsPostInterestPosted);
		System.out
				.println("-----Post Interest As on Successfully Worked----------");

	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSavingsAccountPostInterestOnLastDayWithdrawalWithOverdraft() {
		this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec,
				this.responseSpec);

		/***
		 * Create a client to apply for savings account (overdraft account).
		 */
		final Integer clientID = ClientHelper.createClient(this.requestSpec,
				this.responseSpec);
		Assert.assertNotNull(clientID);
		final String minBalanceForInterestCalculation = null;

		/***
		 * Create savings product with zero opening balance and overdraft
		 * enabled
		 */
		final String zeroOpeningBalance = "0.0";
		final String minRequiredBalance = null;
		final String enforceMinRequiredBalance = "false";
		final boolean allowOverdraft = true;
		final Integer savingsProductID = createSavingsProduct(this.requestSpec,
				this.responseSpec, zeroOpeningBalance,
				minBalanceForInterestCalculation, minRequiredBalance,
				enforceMinRequiredBalance, allowOverdraft);
		Assert.assertNotNull(savingsProductID);

		/***
		 * Apply for Savings account
		 */
		final Integer savingsId = this.savingsAccountHelper
				.applyForSavingsApplication(clientID, savingsProductID,
						ACCOUNT_TYPE_INDIVIDUAL);
		Assert.assertNotNull(savingsProductID);

		HashMap modifications = this.savingsAccountHelper.updateSavingsAccount(
				clientID, savingsProductID, savingsId, ACCOUNT_TYPE_INDIVIDUAL);
		Assert.assertTrue(modifications.containsKey("submittedOnDate"));

		HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(
				this.requestSpec, this.responseSpec, savingsId);
		SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

		/***
		 * Approve the savings account
		 */
		savingsStatusHashMap = this.savingsAccountHelper
				.approveSavings(savingsId);
		SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

		DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
		Calendar todaysDate = Calendar.getInstance();
		todaysDate.add(Calendar.MONTH, -1);

		final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());

		/***
		 * Activate the application and verify account status
		 * 
		 * @param activationDate
		 *            this value is every time first day of previous month
		 */
		savingsStatusHashMap = activateSavingsAccount(savingsId,
				ACTIVATION_DATE);
		SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
		/***
		 * Verify the account summary
		 */
		final HashMap summaryBefore = this.savingsAccountHelper
				.getSavingsSummary(savingsId);
		this.savingsAccountHelper.calculateInterestForSavings(savingsId);
		HashMap summary = this.savingsAccountHelper
				.getSavingsSummary(savingsId);
		assertEquals(summaryBefore, summary);

		final Integer lastDayOfMonth = todaysDate
				.getActualMaximum(Calendar.DAY_OF_MONTH);
		todaysDate.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
		final String WITHDRAWAL_DATE = dateFormat.format(todaysDate.getTime());
		Float balance = Float.valueOf(zeroOpeningBalance);

		DateFormat transactionDateFormat = new SimpleDateFormat("dd MMMM yyyy",
				Locale.US);
		Calendar transactionDate = Calendar.getInstance();
		transactionDate.set(Calendar.DAY_OF_MONTH, 2);
		String TRANSACTION_DATE = dateFormat.format(transactionDate.getTime());

		 /***
         * Perform withdraw transaction, verify account balance(account balance
         * will go to negative as no deposits are there prior to this
         * transaction)
         */
        Integer withdrawTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, WITHDRAW_AMOUNT,
        		ACTIVATION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        balance -= new Float(WITHDRAW_AMOUNT);
        assertEquals("Verifying Withdrawal Amount", new Float(WITHDRAW_AMOUNT), withdrawTransaction.get("amount"));
        assertEquals("Verifying Balance after Withdrawal", balance, withdrawTransaction.get("runningBalance"));
        
        /***
         * Perform Deposit transaction on last day of month and verify account
         * balance.
         * 
         * @param transactionDate
         *            this value is every time last day of previous month
         */
        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
        		WITHDRAWAL_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap depositTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, depositTransactionId);
        balance += new Float(DEPOSIT_AMOUNT);
        assertEquals("Verifying Deposit Amount", new Float(DEPOSIT_AMOUNT), depositTransaction.get("amount"));
        assertEquals("Verifying Balance after Deposit", balance, depositTransaction.get("runningBalance"));

		/***
		 * Calculate expected interest to be posted, interest should be posted
		 * for one day only because deposit transaction happened on last day of
		 * month before this account balance is negative.
		 */
		this.savingsAccountHelper.postInterestForSavings(savingsId);
		HashMap accountDetails = this.savingsAccountHelper
				.getSavingsDetails(savingsId);
		summary = (HashMap) accountDetails.get("summary");
		Float accountDetailsPostInterestPosted = Float.valueOf(summary.get(
				"totalInterestPosted").toString());

		Float nominalAnnualInterest = Float.valueOf(accountDetails.get(
				"nominalAnnualInterestRate").toString());
		HashMap interestCalculationDaysInYearType = (HashMap) accountDetails
				.get("interestCalculationDaysInYearType");
		Integer daysInYear = Integer.valueOf(interestCalculationDaysInYearType
				.get("id").toString());
		double interestRateInFraction = (nominalAnnualInterest / 100);
		double perDay = (double) 1 / (daysInYear);
		double interestPerDay = interestRateInFraction * perDay;
		Float interestPosted = (float) (interestPerDay * balance * 1);

		/***
		 * Apply rounding on interestPosted, actualInterestPosted and verify
		 * both are same
		 */
		DecimalFormat decimalFormat = new DecimalFormat("",
				new DecimalFormatSymbols(Locale.US));
		decimalFormat.applyPattern("#.###");
		interestPosted = new Float(decimalFormat.format(interestPosted));
		accountDetailsPostInterestPosted = new Float(
				decimalFormat.format(accountDetailsPostInterestPosted));
		assertEquals("Verifying interest posted", interestPosted,
				accountDetailsPostInterestPosted);

		this.savingsAccountHelper.postInterestAsOnSavings(savingsId,
				TRANSACTION_DATE);
		HashMap accountTransactionDetails = this.savingsAccountHelper
				.getSavingsDetails(savingsId);
		summary = (HashMap) accountDetails.get("summary");
		Float accountDetailsPostInterest = Float.valueOf(summary.get(
				"totalInterestPosted").toString());

		nominalAnnualInterest = Float.valueOf(accountDetails.get(
				"nominalAnnualInterestRate").toString());
		interestCalculationDaysInYearType = (HashMap) accountDetails
				.get("interestCalculationDaysInYearType");
		daysInYear = Integer.valueOf(interestCalculationDaysInYearType
				.get("id").toString());
		interestRateInFraction = (nominalAnnualInterest / 100);
		perDay = (double) 1 / (daysInYear);
		interestPerDay = interestRateInFraction * perDay;
		interestPosted = (float) (interestPerDay * balance * 1);

		/***
		 * Apply rounding on interestPosted, actualInterestPosted and verify
		 * both are same
		 */
		decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(
				Locale.US));
		decimalFormat.applyPattern("#.###");
		interestPosted = new Float(decimalFormat.format(interestPosted));
		accountDetailsPostInterestPosted = new Float(
				decimalFormat.format(accountDetailsPostInterestPosted));
		assertEquals("Verifying interest posted", interestPosted,
				accountDetailsPostInterestPosted);
		System.out
				.println("-----Post Interest As on Successfully Worked----------");

		transactionDate.set(Calendar.DAY_OF_MONTH, 3);
		TRANSACTION_DATE = dateFormat.format(transactionDate.getTime());

		this.savingsAccountHelper.postInterestAsOnSavings(savingsId,
				TRANSACTION_DATE);
		accountTransactionDetails = this.savingsAccountHelper
				.getSavingsDetails(savingsId);
		summary = (HashMap) accountDetails.get("summary");
		accountDetailsPostInterest = Float.valueOf(summary.get(
				"totalInterestPosted").toString());

		nominalAnnualInterest = Float.valueOf(accountDetails.get(
				"nominalAnnualInterestRate").toString());
		interestCalculationDaysInYearType = (HashMap) accountDetails
				.get("interestCalculationDaysInYearType");
		daysInYear = Integer.valueOf(interestCalculationDaysInYearType
				.get("id").toString());
		interestRateInFraction = (nominalAnnualInterest / 100);
		perDay = (double) 1 / (daysInYear);
		interestPerDay = interestRateInFraction * perDay;
		interestPosted = (float) (interestPerDay * balance * 1);

		/***
		 * Apply rounding on interestPosted, actualInterestPosted and verify
		 * both are same
		 */
		decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(
				Locale.US));
		decimalFormat.applyPattern("#.###");
		interestPosted = new Float(decimalFormat.format(interestPosted));
		accountDetailsPostInterestPosted = new Float(
				decimalFormat.format(accountDetailsPostInterestPosted));
		assertEquals("Verifying interest posted", interestPosted,
				accountDetailsPostInterestPosted);
		System.out
				.println("-----Post Interest As on Successfully Worked-------");

		DateFormat transactionFormat = new SimpleDateFormat("dd MMMM yyyy",
				Locale.US);
		Calendar transactionCalendarDateFormat = Calendar.getInstance();
		transactionCalendarDateFormat.add(Calendar.DAY_OF_MONTH, 0);
		transactionDate.set(Calendar.DAY_OF_MONTH, 22);
		TRANSACTION_DATE = dateFormat.format(transactionDate.getTime());
		if (Calendar.DAY_OF_MONTH >= 22) {
			this.savingsAccountHelper.postInterestAsOnSavings(savingsId,
					TRANSACTION_DATE);
			accountTransactionDetails = this.savingsAccountHelper
					.getSavingsDetails(savingsId);
			summary = (HashMap) accountTransactionDetails.get("summary");
			accountDetailsPostInterest = Float.valueOf(summary.get(
					"totalInterestPosted").toString());

			nominalAnnualInterest = Float.valueOf(accountDetails.get(
					"nominalAnnualInterestRate").toString());
			interestCalculationDaysInYearType = (HashMap) accountDetails
					.get("interestCalculationDaysInYearType");
			daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get(
					"id").toString());
			interestRateInFraction = (nominalAnnualInterest / 100);
			perDay = (double) 1 / (daysInYear);
			interestPerDay = interestRateInFraction * perDay;
			interestPosted = (float) (interestPerDay * balance * 19);

			/***
			 * Apply rounding on interestPosted, actualInterestPosted and verify
			 * both are same
			 */
			decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(
					Locale.US));
			decimalFormat.applyPattern("#.###");
			interestPosted = new Float(decimalFormat.format(interestPosted));
			accountDetailsPostInterestPosted = new Float(
					decimalFormat.format(accountDetailsPostInterestPosted));
			assertEquals("Verifying interest posted", interestPosted,
					accountDetailsPostInterestPosted);
			System.out
					.println("-----Post Interest As on Successfully Worked----------");
		}
		DateFormat lastTransactionDateFormat = new SimpleDateFormat(
				"dd MMMM yyyy", Locale.US);
		Calendar postedLastDate = Calendar.getInstance();
		int numberOfDateOfMonth = postedLastDate
				.getActualMaximum(Calendar.DAY_OF_MONTH);
		TRANSACTION_DATE = lastTransactionDateFormat.format(transactionDate
				.getTime());

		if (Calendar.DAY_OF_MONTH == numberOfDateOfMonth) {

			this.savingsAccountHelper.postInterestAsOnSavings(savingsId,
					TRANSACTION_DATE);
			accountTransactionDetails = this.savingsAccountHelper
					.getSavingsDetails(savingsId);
			summary = (HashMap) accountTransactionDetails.get("summary");
			accountDetailsPostInterest = Float.valueOf(summary.get(
					"totalInterestPosted").toString());

			nominalAnnualInterest = Float.valueOf(accountDetails.get(
					"nominalAnnualInterestRate").toString());
			interestCalculationDaysInYearType = (HashMap) accountDetails
					.get("interestCalculationDaysInYearType");
			daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get(
					"id").toString());
			interestRateInFraction = (nominalAnnualInterest / 100);
			perDay = (double) 1 / (daysInYear);
			interestPerDay = interestRateInFraction * perDay;
			interestPosted = (float) (interestPerDay * balance * 8);

			/***
			 * Apply rounding on interestPosted, actualInterestPosted and verify
			 * both are same
			 */
			decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(
					Locale.US));
			decimalFormat.applyPattern("#.###");
			interestPosted = new Float(decimalFormat.format(interestPosted));
			accountDetailsPostInterestPosted = new Float(
					decimalFormat.format(accountDetailsPostInterestPosted));
			assertEquals("Verifying interest posted", interestPosted,
					accountDetailsPostInterestPosted);
			System.out
					.println("-----Post Interest As on Successfully Worked----------");

		}
		transactionDate.set(Calendar.DAY_OF_MONTH, 1);
		TRANSACTION_DATE = dateFormat.format(transactionDate.getTime());
		this.savingsAccountHelper.postInterestAsOnSavings(savingsId,
				TRANSACTION_DATE);
		accountTransactionDetails = this.savingsAccountHelper
				.getSavingsDetails(savingsId);
		summary = (HashMap) accountTransactionDetails.get("summary");
		accountDetailsPostInterest = Float.valueOf(summary.get(
				"totalInterestPosted").toString());

		nominalAnnualInterest = Float.valueOf(accountDetails.get(
				"nominalAnnualInterestRate").toString());
		interestCalculationDaysInYearType = (HashMap) accountDetails
				.get("interestCalculationDaysInYearType");
		daysInYear = Integer.valueOf(interestCalculationDaysInYearType
				.get("id").toString());
		interestRateInFraction = (nominalAnnualInterest / 100);
		perDay = (double) 1 / (daysInYear);
		interestPerDay = interestRateInFraction * perDay;
		interestPosted = (float) (interestPerDay * balance * 1);

		/***
		 * Apply rounding on interestPosted, actualInterestPosted and verify
		 * both are same
		 */
		decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(
				Locale.US));
		decimalFormat.applyPattern("#.###");
		interestPosted = new Float(decimalFormat.format(interestPosted));
		accountDetailsPostInterestPosted = new Float(
				decimalFormat.format(accountDetailsPostInterestPosted));
		assertEquals("Verifying interest posted", interestPosted,
				accountDetailsPostInterestPosted);
		System.out
				.println("-----Post Interest As on Successfully Worked----------");
	}
       
    @SuppressWarnings("unchecked")
    @Test
    public void testSavingsAccountPostInterestWithOverdraft() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(403).build();

        /***
         * Create a client to apply for savings account (overdraft account).
         */
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;

        /***
         * Create savings product with zero opening balance and overdraft
         * enabled
         */
        final String zeroOpeningBalance = "0.0";
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = true;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, zeroOpeningBalance,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assert.assertNotNull(savingsProductID);

        /***
         * Apply for Savings account
         */
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap modifications = this.savingsAccountHelper.updateSavingsAccount(clientID, savingsProductID, savingsId,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertTrue(modifications.containsKey("submittedOnDate"));

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        /***
         * Approve the savings account
         */
        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        todaysDate.set(Calendar.DAY_OF_MONTH, 1);
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final Integer lastDayOfMonth = todaysDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        todaysDate.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
        final String TRANSACTION_DATE = dateFormat.format(todaysDate.getTime());

        Calendar postedDate = Calendar.getInstance();
        postedDate.set(Calendar.DAY_OF_MONTH, 2);

        final String POSTED_TRANSACTION_DATE = dateFormat.format(postedDate.getTime());

        /***
         * Activate the application and verify account status
         * 
         * @param activationDate
         *            this value is every time first day of previous month
         */
        savingsStatusHashMap = activateSavingsAccount(savingsId, ACTIVATION_DATE);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        /***
         * Verify the account summary
         */
        final HashMap summaryBefore = this.savingsAccountHelper.getSavingsSummary(savingsId);
        this.savingsAccountHelper.calculateInterestForSavings(savingsId);
        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals(summaryBefore, summary);

        Float balance = Float.valueOf(zeroOpeningBalance);

        /***
         * Perform withdraw transaction, verify account balance(account balance
         * will go to negative as no deposits are there prior to this
         * transaction)
         */
        Integer withdrawTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, WITHDRAW_AMOUNT,
                ACTIVATION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        balance -= new Float(WITHDRAW_AMOUNT);
        assertEquals("Verifying Withdrawal Amount", new Float(WITHDRAW_AMOUNT), withdrawTransaction.get("amount"));
        assertEquals("Verifying Balance after Withdrawal", balance, withdrawTransaction.get("runningBalance"));

        /***
         * Perform Deposit transaction on last day of month and verify account
         * balance.
         * 
         * @param transactionDate
         *            this value is every time last day of previous month
         */
        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
                TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap depositTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, depositTransactionId);
        balance += new Float(DEPOSIT_AMOUNT);
        assertEquals("Verifying Deposit Amount", new Float(DEPOSIT_AMOUNT), depositTransaction.get("amount"));
        assertEquals("Verifying Balance after Deposit", balance, depositTransaction.get("runningBalance"));

        /***
         * Perform Post interest transaction and verify the posted amount
         */
        this.savingsAccountHelper.postInterestForSavings(savingsId);
        HashMap accountDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
        summary = (HashMap) accountDetails.get("summary");
        Float actualInterestPosted = Float.valueOf(summary.get("totalInterestPosted").toString());

        /***
         * Calculate expected interest to be posted, interest should be posted
         * for one day only because deposit transaction happened on last day of
         * month before this account balance is negative.
         */
        this.savingsAccountHelper.postInterestAsOnSavings(savingsId, POSTED_TRANSACTION_DATE);
        HashMap accountDetailsPostInterest = this.savingsAccountHelper.getSavingsDetails(savingsId);
        summary = (HashMap) accountDetails.get("summary");
        ArrayList interestPostingTransaction = (ArrayList) ((HashMap) ((ArrayList) accountDetails.get("transactions")).get(0)).get("date");
        Float accountDetailsPostInterestPosted = Float.valueOf(summary.get("totalInterestPosted").toString());

        /***
         * Calculate expected interest to be posted, interest should be posted
         * for one day only because deposit transaction happened on last day of
         * month before this account balance is negative.
         */
        final Float nominalAnnualInterest = Float.valueOf(accountDetails.get("nominalAnnualInterestRate").toString());
        final HashMap interestCalculationDaysInYearType = (HashMap) accountDetails.get("interestCalculationDaysInYearType");
        final Integer daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get("id").toString());
        double interestRateInFraction = (nominalAnnualInterest / 100);
        double perDay = (double) 1 / (daysInYear);
        double interestPerDay = interestRateInFraction * perDay;
        Float interestPosted = (float) (interestPerDay * balance * 1);

        /***
         * Apply rounding on interestPosted, actualInterestPosted and verify
         * both are same
         */
        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern("#.###");
        interestPosted = new Float(decimalFormat.format(interestPosted));
        actualInterestPosted = new Float(decimalFormat.format(accountDetailsPostInterestPosted));
        assertEquals("Verifying interest posted", interestPosted, accountDetailsPostInterestPosted);
        System.out.println("------Post Interest As On After doing a post interest Successfully worked--------");

        todaysDate = Calendar.getInstance();
        final String CLOSEDON_DATE = dateFormat.format(todaysDate.getTime());

        Calendar interestPostingDate = Calendar.getInstance();
        Calendar todysDate = Calendar.getInstance();
        interestPostingDate.set((int) interestPostingTransaction.get(0), (int) interestPostingTransaction.get(1) - 1,
                (int) interestPostingTransaction.get(2));
        final String INTEREST_POSTING_DATE = dateFormat.format(interestPostingDate.getTime());
        final String TODYS_POSTING_DATE = dateFormat.format(todysDate.getTime());
        String withdrawBalance = "true";

        if (TODYS_POSTING_DATE.equalsIgnoreCase(INTEREST_POSTING_DATE)) {
            final SavingsAccountHelper validationErrorHelper = new SavingsAccountHelper(this.requestSpec, errorResponse);
            validationErrorHelper.closeSavingsAccountPostInterestAndGetBackRequiredField(savingsId, withdrawBalance,
                    CommonConstants.RESPONSE_ERROR, CLOSEDON_DATE);
        } else {
            final SavingsAccountHelper validationErrorHelper = new SavingsAccountHelper(this.requestSpec, errorResponse);
            ArrayList<HashMap> savingsAccountErrorData = (ArrayList<HashMap>) validationErrorHelper
                    .closeSavingsAccountPostInterestAndGetBackRequiredField(savingsId, withdrawBalance, CommonConstants.RESPONSE_ERROR,
                            CLOSEDON_DATE);
            assertEquals("error.msg.postInterest.notDone", savingsAccountErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));
        }

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPostInterestAsOnSavingsAccountWithOverdraft() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(400).build();
        final SavingsAccountHelper validationErrorHelper = new SavingsAccountHelper(this.requestSpec, errorResponse);

        /***
         * Create a client to apply for savings account (overdraft account).
         */
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;

        /***
         * Create savings product with zero opening balance and overdraft
         * enabled
         */
        final String zeroOpeningBalance = "0.0";
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = true;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, zeroOpeningBalance,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assert.assertNotNull(savingsProductID);

        /***
         * Apply for Savings account
         */
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap modifications = this.savingsAccountHelper.updateSavingsAccount(clientID, savingsProductID, savingsId,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertTrue(modifications.containsKey("submittedOnDate"));

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        /***
         * Approve the savings account
         */
        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        todaysDate.set(Calendar.DAY_OF_MONTH, 1);
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final Integer lastDayOfMonth = todaysDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        todaysDate.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
        final String TRANSACTION_DATE = dateFormat.format(todaysDate.getTime());
        
        Calendar postedDate = Calendar.getInstance();
        postedDate.set(Calendar.DAY_OF_MONTH, 1);
       
        final String POSTED_TRANSACTION_DATE = dateFormat.format(postedDate.getTime());        
        Calendar postedLastDate = Calendar.getInstance();
        int countOfDate=postedDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        System.out.println("count Of Date---> "+countOfDate);
        postedLastDate.set(Calendar.DAY_OF_MONTH,countOfDate);
        final String POSTED_LAST_TRANSACTION_DATE = dateFormat.format(postedLastDate.getTime());

        /***
         * Activate the application and verify account status
         * 
         * @param activationDate
         *            this value is every time first day of previous month
         */
        savingsStatusHashMap = activateSavingsAccount(savingsId, ACTIVATION_DATE);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        /***
         * Verify the account summary
         */
        final HashMap summaryBefore = this.savingsAccountHelper.getSavingsSummary(savingsId);
        this.savingsAccountHelper.calculateInterestForSavings(savingsId);
        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals(summaryBefore, summary);

        Float balance = Float.valueOf(zeroOpeningBalance);

        /***
         * Perform withdraw transaction, verify account balance(account balance
         * will go to negative as no deposits are there prior to this
         * transaction)
         */
        Integer withdrawTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, WITHDRAW_AMOUNT,
                ACTIVATION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        balance -= new Float(WITHDRAW_AMOUNT);
        assertEquals("Verifying Withdrawal Amount", new Float(WITHDRAW_AMOUNT), withdrawTransaction.get("amount"));
        assertEquals("Verifying Balance after Withdrawal", balance, withdrawTransaction.get("runningBalance"));

        /***
         * Perform Deposit transaction on last day of month and verify account
         * balance.
         * 
         * @param transactionDate
         *            this value is every time last day of previous month
         */
        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
                TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap depositTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, depositTransactionId);
        balance += new Float(DEPOSIT_AMOUNT);
        assertEquals("Verifying Deposit Amount", new Float(DEPOSIT_AMOUNT), depositTransaction.get("amount"));
        assertEquals("Verifying Balance after Deposit", balance, depositTransaction.get("runningBalance"));

        /***
         * Perform Post interest transaction and verify the posted amount
         */
        this.savingsAccountHelper.postInterestAsOnSavings(savingsId, POSTED_TRANSACTION_DATE);
        HashMap accountDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
        summary = (HashMap) accountDetails.get("summary");
        Float actualInterestPosted = Float.valueOf(summary.get("totalInterestPosted").toString());
        
    
        /***
         * Calculate expected interest to be posted, interest should be posted
         * for one day only because deposit transaction happened on last day of
         * month before this account balance is negative.
         */
        final Float nominalAnnualInterest = Float.valueOf(accountDetails.get("nominalAnnualInterestRate").toString());
        final HashMap interestCalculationDaysInYearType = (HashMap) accountDetails.get("interestCalculationDaysInYearType");
        final Integer daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get("id").toString());
        double interestRateInFraction = (nominalAnnualInterest / 100);
        double perDay = (double) 1 / (daysInYear);
        double interestPerDay = interestRateInFraction * perDay;
        Float interestPosted = (float) (interestPerDay * balance * 1);

        /***
         * Apply rounding on interestPosted, actualInterestPosted and verify
         * both are same
         */
        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern("#.###");
        interestPosted = new Float(decimalFormat.format(interestPosted));
        actualInterestPosted = new Float(decimalFormat.format(actualInterestPosted));
       assertEquals("Verifying interest posted", interestPosted, actualInterestPosted);           
       System.out.println("------Post Interest As On Successful Worked--------");
       
       this.savingsAccountHelper.postInterestAsOnSavings(savingsId, POSTED_LAST_TRANSACTION_DATE);
       HashMap accountLastDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
       summary = (HashMap) accountLastDetails.get("summary");
       Float actualLastInterestPosted = Float.valueOf(summary.get("totalInterestPosted").toString());
       
       final Float nominalLastAnnualInterest = Float.valueOf(accountDetails.get("nominalAnnualInterestRate").toString());
       final HashMap interestLastCalculationDaysInYearType = (HashMap) accountDetails.get("interestCalculationDaysInYearType");
       final Integer daysLastInYear = Integer.valueOf(interestCalculationDaysInYearType.get("id").toString());
       double interestLastRateInFraction = (nominalAnnualInterest / 100);
       double perLastDay = (double) 1 / (daysInYear);
       double interestLastPerDay = interestLastRateInFraction * perLastDay;
       Float interestLastPosted = (float) (interestLastPerDay * balance * 1);
       
       DecimalFormat decimalLastFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
       decimalLastFormat.applyPattern("#.###");
       interestLastPosted = new Float(decimalLastFormat.format(interestLastPosted));
       actualInterestPosted = new Float(decimalFormat.format(actualInterestPosted));
      assertEquals("Verifying interest posted", interestLastPosted, actualInterestPosted);           
      System.out.println("------Post Interest As On Successful Worked--------");
       
    }
    
    
    
    @Test
    public void testSavingsAccount_WITH_WITHHOLD_TAX() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final String percentage = "10";
        final Integer taxGroupId = createTaxGroup(percentage);
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft, String.valueOf(taxGroupId), false);
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

        final HashMap summaryBefore = this.savingsAccountHelper.getSavingsSummary(savingsId);
        this.savingsAccountHelper.calculateInterestForSavings(savingsId);
        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals(summaryBefore, summary);

        this.savingsAccountHelper.postInterestForSavings(savingsId);
        summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        Assert.assertFalse(summaryBefore.equals(summary));
        Assert.assertNotNull(summary.get("totalWithholdTax"));
        Float expected = (Float) summary.get("totalDeposits") + (Float) summary.get("totalInterestPosted")
                - (Float) summary.get("totalWithholdTax");
        Float actual = (Float) summary.get("accountBalance");
        Assert.assertEquals(expected, actual, 1);

    }

    @Test
    public void testSavingsAccount_WITH_WITHHOLD_TAX_DISABLE_AT_ACCOUNT_LEVEL() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final String percentage = "10";
        final Integer taxGroupId = createTaxGroup(percentage);
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft, String.valueOf(taxGroupId), false);
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

        final HashMap summaryBefore = this.savingsAccountHelper.getSavingsSummary(savingsId);
        this.savingsAccountHelper.calculateInterestForSavings(savingsId);
        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals(summaryBefore, summary);

        final HashMap changes = this.savingsAccountHelper.updateSavingsAccountWithHoldTaxStatus(savingsId, false);
        Assert.assertTrue(changes.containsKey("withHoldTax"));

        this.savingsAccountHelper.postInterestForSavings(savingsId);
        summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        Assert.assertFalse(summaryBefore.equals(summary));
        Assert.assertNull(summary.get("totalWithholdTax"));
        Float expected = (Float) summary.get("totalDeposits") + (Float) summary.get("totalInterestPosted");
        Float actual = (Float) summary.get("accountBalance");
        Assert.assertEquals(expected, actual, 1);

    }
    
    @Test
    public void testSavingsAccount_DormancyTracking() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final String percentage = "10";
        final Integer taxGroupId = createTaxGroup(percentage);
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft, String.valueOf(taxGroupId), true);
        Assert.assertNotNull(savingsProductID);
        
        final Integer savingsChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsNoActivityFeeJSON());
        Assert.assertNotNull(savingsChargeId);

        ArrayList<Integer> savingsList = new ArrayList<>();

        for(int i=0; i< 5; i++){
            final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
            Assert.assertNotNull(savingsProductID);

            HashMap modifications = this.savingsAccountHelper.updateSavingsAccount(clientID, savingsProductID, savingsId,
                    ACCOUNT_TYPE_INDIVIDUAL);
            Assert.assertTrue(modifications.containsKey("submittedOnDate"));

            this.savingsAccountHelper.addChargesForSavings(savingsId, savingsChargeId, false);

            HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
            SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

            savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
            SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

            savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
            SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
            savingsList.add(savingsId);
        }

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap modifications = this.savingsAccountHelper.updateSavingsAccount(clientID, savingsProductID, savingsId,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertTrue(modifications.containsKey("submittedOnDate"));

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsList.add(savingsId);

        final DateTimeFormatter formatter = DateTimeFormat.forPattern("dd MMMM yyyy");
        LocalDate transactionDate = new LocalDate();
        for(int i=0; i< 4; i++){
        	String TRANSACTION_DATE = formatter.print(transactionDate);
            Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsList.get(i), DEPOSIT_AMOUNT,
                    TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        	transactionDate = transactionDate.minusDays(30);
        }
        
        SchedulerJobHelper jobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);
        try {
			jobHelper.executeJob("Update Savings Dormant Accounts");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
        //VERIFY WITHIN PROVIDED RANGE DOESN'T INACTIVATE
        savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(0));
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        savingsStatusHashMap = SavingsStatusChecker.getSubStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(0));
        SavingsStatusChecker.verifySavingsSubStatusNone(savingsStatusHashMap);
        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsList.get(0));
        Float balance = 3000f;
        Float chargeAmt = 0f;
        balance -= chargeAmt;
        assertEquals("Verifying account Balance", balance, summary.get("accountBalance"));


        //VERIFY INACTIVE
        savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(1));
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        savingsStatusHashMap = SavingsStatusChecker.getSubStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(1));
        SavingsStatusChecker.verifySavingsSubStatusInactive(savingsStatusHashMap);
        summary = this.savingsAccountHelper.getSavingsSummary(savingsList.get(1));
        balance = 3000f;
        chargeAmt = 100f;
        balance -= chargeAmt;
        assertEquals("Verifying account Balance", balance, summary.get("accountBalance"));

    	String TRANSACTION_DATE = formatter.print(new LocalDate());
        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsList.get(1), DEPOSIT_AMOUNT,
                TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(1));
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        savingsStatusHashMap = SavingsStatusChecker.getSubStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(1));
        SavingsStatusChecker.verifySavingsSubStatusNone(savingsStatusHashMap);

        //VERIFY DORMANT
        savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(2));
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        savingsStatusHashMap = SavingsStatusChecker.getSubStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(2));
        SavingsStatusChecker.verifySavingsSubStatusDormant(savingsStatusHashMap);
        summary = this.savingsAccountHelper.getSavingsSummary(savingsList.get(2));
        balance = 3000f;
        chargeAmt = 100f;
        balance -= chargeAmt;
        assertEquals("Verifying account Balance", balance, summary.get("accountBalance"));
        
        TRANSACTION_DATE = formatter.print(new LocalDate());
        depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsList.get(2), DEPOSIT_AMOUNT,
                TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(2));
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        savingsStatusHashMap = SavingsStatusChecker.getSubStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(2));
        SavingsStatusChecker.verifySavingsSubStatusNone(savingsStatusHashMap);
        
        //VERIFY ESCHEAT DUE TO OLD TRANSACTION
        savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(3));
        SavingsStatusChecker.verifySavingsAccountIsClosed(savingsStatusHashMap);
        savingsStatusHashMap = SavingsStatusChecker.getSubStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(3));
        SavingsStatusChecker.verifySavingsSubStatusEscheat(savingsStatusHashMap);
        summary = this.savingsAccountHelper.getSavingsSummary(savingsList.get(3));
        assertEquals("Verifying account Balance", 2900f, summary.get("accountBalance"));
        
        //VERIFY ESCHEAT DUE NO TRANSACTION FROM ACTIVATION
        savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(4));
        SavingsStatusChecker.verifySavingsAccountIsClosed(savingsStatusHashMap);
        savingsStatusHashMap = SavingsStatusChecker.getSubStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(4));
        SavingsStatusChecker.verifySavingsSubStatusEscheat(savingsStatusHashMap);
        summary = this.savingsAccountHelper.getSavingsSummary(savingsList.get(4));
        assertEquals("Verifying account Balance", 900f, summary.get("accountBalance"));
        
        //VERIFY NON ACTIVE ACCOUNTS ARE NOT AFFECTED
        savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(5));
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);
        savingsStatusHashMap = SavingsStatusChecker.getSubStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(5));
        SavingsStatusChecker.verifySavingsSubStatusNone(savingsStatusHashMap);
        

    }

    
    private HashMap activateSavingsAccount(final Integer savingsId, final String activationDate) {
        final HashMap status = this.savingsAccountHelper.activateSavingsAccount(savingsId, activationDate);
        return status;
    }

    private Integer createSavingsProduct(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String minOpenningBalance, String minBalanceForInterestCalculation, String minRequiredBalance,
            String enforceMinRequiredBalance, final boolean allowOverdraft) {
        final String taxGroupId = null;
        return createSavingsProduct(requestSpec, responseSpec, minOpenningBalance, minBalanceForInterestCalculation, minRequiredBalance,
                enforceMinRequiredBalance, allowOverdraft, taxGroupId, false);
    }
    
    private Integer createSavingsProduct(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String minOpenningBalance, String minBalanceForInterestCalculation, String minRequiredBalance,
            String enforceMinRequiredBalance, final boolean allowOverdraft, final String taxGroupId, boolean withDormancy) {
        System.out.println("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        SavingsProductHelper savingsProductHelper = new SavingsProductHelper();
        if (allowOverdraft) {
            final String overDraftLimit = "2000.0";
            savingsProductHelper = savingsProductHelper.withOverDraft(overDraftLimit);
        }
        if(withDormancy){
        	savingsProductHelper = savingsProductHelper.withDormancy();
        }

        final String savingsProductJSON = savingsProductHelper
                //
                .withInterestCompoundingPeriodTypeAsDaily()
                //
                .withInterestPostingPeriodTypeAsMonthly()
                //
                .withInterestCalculationPeriodTypeAsDailyBalance()
                //
                .withMinBalanceForInterestCalculation(minBalanceForInterestCalculation)
                //
                .withMinRequiredBalance(minRequiredBalance).withEnforceMinRequiredBalance(enforceMinRequiredBalance)
                .withMinimumOpenningBalance(minOpenningBalance).withWithHoldTax(taxGroupId).build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }
    
    private Integer createTaxGroup(final String percentage){
        final Integer liabilityAccountId = null;
        final Integer taxComponentId = TaxComponentHelper.createTaxComponent(this.requestSpec, this.responseSpec, percentage, liabilityAccountId);
        return TaxGroupHelper.createTaxGroup(this.requestSpec, this.responseSpec, Arrays.asList(taxComponentId));
    }


    /*
     * private void verifySavingsInterest(final Object savingsInterest) {
     * System.out.println(
     * "--------------------VERIFYING THE BALANCE, INTEREST --------------------------"
     * );
     * 
     * assertEquals("Verifying Interest Calculation", new Float("238.3399"),
     * savingsInterest); }
     */
    
    @SuppressWarnings("unchecked")
    @Test
    public void testSavingsAccountBlockStatus() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        SavingsAccountHelper savingsAccountHelperValidationError = new SavingsAccountHelper(this.requestSpec,
                new ResponseSpecBuilder().build());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assert.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        Float balance = new Float(MINIMUM_OPENING_BALANCE);

        savingsStatusHashMap = this.savingsAccountHelper.blockSavings(savingsId);
        SavingsStatusChecker.verifySavingsSubStatusblock(savingsStatusHashMap);

        List<HashMap> error = (List) savingsAccountHelperValidationError.withdrawalFromSavingsAccount(savingsId, "100",
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.saving.account.blocked.transaction.not.allowed",
                error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        error = (List) savingsAccountHelperValidationError.depositToSavingsAccount(savingsId, "100", SavingsAccountHelper.TRANSACTION_DATE,
                CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.saving.account.blocked.transaction.not.allowed",
                error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        savingsStatusHashMap = this.savingsAccountHelper.unblockSavings(savingsId);
        SavingsStatusChecker.verifySavingsSubStatusIsNone(savingsStatusHashMap);
        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap depositTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, depositTransactionId);
        balance += new Float(DEPOSIT_AMOUNT);
        assertEquals("Verifying Deposit Amount", new Float(DEPOSIT_AMOUNT), depositTransaction.get("amount"));

        savingsStatusHashMap = this.savingsAccountHelper.blockDebit(savingsId);
        SavingsStatusChecker.verifySavingsSubStatusIsDebitBlocked(savingsStatusHashMap);
        error = (List) savingsAccountHelperValidationError.withdrawalFromSavingsAccount(savingsId, "100",
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.savings.account.debit.transaction.not.allowed",
                error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));
        
        depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        depositTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, depositTransactionId);
        balance += new Float(DEPOSIT_AMOUNT);
        assertEquals("Verifying Deposit Amount", new Float(DEPOSIT_AMOUNT), depositTransaction.get("amount"));

        savingsStatusHashMap = this.savingsAccountHelper.unblockDebit(savingsId);
        SavingsStatusChecker.verifySavingsSubStatusIsNone(savingsStatusHashMap);
        Integer withdrawTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, WITHDRAW_AMOUNT,
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        balance -= new Float(WITHDRAW_AMOUNT);
        assertEquals("Verifying Withdrawal Amount", new Float(WITHDRAW_AMOUNT), withdrawTransaction.get("amount"));

        savingsStatusHashMap = this.savingsAccountHelper.blockCredit(savingsId);
        SavingsStatusChecker.verifySavingsSubStatusIsCreditBlocked(savingsStatusHashMap);
        error = (List) savingsAccountHelperValidationError.depositToSavingsAccount(savingsId, "100", SavingsAccountHelper.TRANSACTION_DATE,
                CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.savings.account.credit.transaction.not.allowed",
                error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));
        
        withdrawTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, WITHDRAW_AMOUNT,
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        balance -= new Float(WITHDRAW_AMOUNT);
        assertEquals("Verifying Withdrawal Amount", new Float(WITHDRAW_AMOUNT), withdrawTransaction.get("amount"));

        savingsStatusHashMap = this.savingsAccountHelper.unblockCredit(savingsId);
        SavingsStatusChecker.verifySavingsSubStatusIsNone(savingsStatusHashMap);
        depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        depositTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, depositTransactionId);
        balance += new Float(DEPOSIT_AMOUNT);
        assertEquals("Verifying Deposit Amount", new Float(DEPOSIT_AMOUNT), depositTransaction.get("amount"));

        Integer holdTransactionId = (Integer) this.savingsAccountHelper.holdAmountInSavingsAccount(savingsId, String.valueOf(balance - 100),
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        error = (List) savingsAccountHelperValidationError.withdrawalFromSavingsAccount(savingsId, "300",
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.savingsaccount.transaction.insufficient.account.balance",
                error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        Integer releaseTransactionId = this.savingsAccountHelper.releaseAmount(savingsId, holdTransactionId);
        withdrawTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "300",
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        balance -= new Float("300");
        assertEquals("Verifying Withdrawal Amount", new Float("300"), withdrawTransaction.get("amount"));

    }
}