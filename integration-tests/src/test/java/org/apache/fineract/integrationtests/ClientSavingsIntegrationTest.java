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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.apache.fineract.infrastructure.core.service.DateUtils;
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
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client Savings Integration Test for checking Savings Application.
 */
@SuppressWarnings({ "rawtypes" })
public class ClientSavingsIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClientSavingsIntegrationTest.class);
    public static final String DEPOSIT_AMOUNT = "2000";
    public static final String WITHDRAW_AMOUNT = "1000";
    public static final String WITHDRAW_AMOUNT_ADJUSTED = "500";
    public static final String MINIMUM_OPENING_BALANCE = "1000.0";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final String DATE_FORMAT = "dd MMMM yyyy";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private SavingsAccountHelper savingsAccountHelper;

    @BeforeEach
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
        Assertions.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(savingsProductID);

        HashMap modifications = this.savingsAccountHelper.updateSavingsAccount(clientID, savingsProductID, savingsId,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertTrue(modifications.containsKey("submittedOnDate"));

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
        Assertions.assertFalse(summaryBefore.equals(summary));

        final Object savingsInterest = this.savingsAccountHelper.getSavingsInterest(savingsId);
        // verifySavingsInterest(savingsInterest);
    }

    @Test
    public void testSavingsAccountWithMinBalanceForInterestCalculation() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = "5000";
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(savingsProductID);

        HashMap modifications = this.savingsAccountHelper.updateSavingsAccount(clientID, savingsProductID, savingsId,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertTrue(modifications.containsKey("submittedOnDate"));

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
        Assertions.assertNull(savingsInterest);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSavingsAccount_CLOSE_APPLICATION() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(400).build();
        final SavingsAccountHelper validationErrorHelper = new SavingsAccountHelper(this.requestSpec, errorResponse);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = "1000.0";
        final String enforceMinRequiredBalance = "true";
        final boolean allowOverdraft = false;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(savingsProductID);

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
        ArrayList<HashMap> savingsAccountErrorData = (ArrayList<HashMap>) validationErrorHelper
                .closeSavingsAccountAndGetBackRequiredField(savingsId, withdrawBalance, CommonConstants.RESPONSE_ERROR, CLOSEDON_DATE);
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
        Assertions.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = "1500.0";
        final String openningBalance = "1600";
        final String enforceMinRequiredBalance = "true";
        final boolean allowOverdraft = false;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, openningBalance,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(savingsProductID);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        final Integer savingsActivationChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsActivationFeeJSON());
        Assertions.assertNotNull(savingsActivationChargeId);

        this.savingsAccountHelper.addChargesForSavings(savingsId, savingsActivationChargeId, true);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        Float balance = Float.parseFloat(openningBalance);
        Float chargeAmt = 100f;
        balance -= chargeAmt;
        assertEquals(balance, summary.get("accountBalance"), "Verifying opening Balance");

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
        balance += Float.parseFloat(DEPOSIT_AMOUNT);
        assertEquals(Float.parseFloat(DEPOSIT_AMOUNT), depositTransaction.get("amount"), "Verifying Deposit Amount");
        assertEquals(balance, depositTransaction.get("runningBalance"), "Verifying Balance after Deposit");

        Integer withdrawTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, withdrawAmt,
                TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        balance -= Float.parseFloat(withdrawAmt);
        assertEquals(Float.parseFloat(withdrawAmt), withdrawTransaction.get("amount"), "Verifying Withdrawal Amount");
        assertEquals(balance, withdrawTransaction.get("runningBalance"), "Verifying Balance after Withdrawal");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSavingsAccount_DELETE_APPLICATION() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        SavingsAccountHelper savingsAccountHelperValidationError = new SavingsAccountHelper(this.requestSpec,
                new ResponseSpecBuilder().build());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(savingsProductID);

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
        Assertions.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(savingsProductID);

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
        Assertions.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(savingsProductID);

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
        Assertions.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(savingsProductID);

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
        Float balance = Float.parseFloat(MINIMUM_OPENING_BALANCE);
        assertEquals(balance, summary.get("accountBalance"), "Verifying opening Balance");

        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap depositTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, depositTransactionId);
        balance += Float.parseFloat(DEPOSIT_AMOUNT);
        assertEquals(Float.parseFloat(DEPOSIT_AMOUNT), depositTransaction.get("amount"), "Verifying Deposit Amount");
        assertEquals(balance, depositTransaction.get("runningBalance"), "Verifying Balance after Deposit");

        Integer withdrawTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, WITHDRAW_AMOUNT,
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        balance -= Float.parseFloat(WITHDRAW_AMOUNT);
        assertEquals(Float.parseFloat(WITHDRAW_AMOUNT), withdrawTransaction.get("amount"), "Verifying Withdrawal Amount");
        assertEquals(balance, withdrawTransaction.get("runningBalance"), "Verifying Balance after Withdrawal");

        Integer newWithdrawTransactionId = this.savingsAccountHelper.updateSavingsAccountTransaction(savingsId, withdrawTransactionId,
                WITHDRAW_AMOUNT_ADJUSTED);
        HashMap newWithdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, newWithdrawTransactionId);
        balance = balance + Float.parseFloat(WITHDRAW_AMOUNT) - Float.parseFloat(WITHDRAW_AMOUNT_ADJUSTED);
        assertEquals(Float.parseFloat(WITHDRAW_AMOUNT_ADJUSTED), newWithdrawTransaction.get("amount"), "Verifying adjusted Amount");
        assertEquals(balance, newWithdrawTransaction.get("runningBalance"), "Verifying Balance after adjust");
        summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals(balance, summary.get("accountBalance"), "Verifying Adjusted Balance");
        withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        Assertions.assertTrue((Boolean) withdrawTransaction.get("reversed"));

        this.savingsAccountHelper.undoSavingsAccountTransaction(savingsId, newWithdrawTransactionId);
        newWithdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        Assertions.assertTrue((Boolean) newWithdrawTransaction.get("reversed"));
        summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        balance += Float.parseFloat(WITHDRAW_AMOUNT_ADJUSTED);
        assertEquals(balance, summary.get("accountBalance"), "Verifying Balance After Undo Transaction");

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
        Assertions.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assertions.assertNotNull(savingsProductID);

        Assertions.assertNotNull(savingsProductID);
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        final Integer withdrawalChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsWithdrawalFeeJSON());
        Assertions.assertNotNull(withdrawalChargeId);

        this.savingsAccountHelper.addChargesForSavings(savingsId, withdrawalChargeId, false);
        ArrayList<HashMap> chargesPendingState = this.savingsAccountHelper.getSavingsCharges(savingsId);
        Assertions.assertEquals(1, chargesPendingState.size());

        Integer savingsChargeId = (Integer) chargesPendingState.get(0).get("id");
        HashMap chargeChanges = this.savingsAccountHelper.updateCharges(savingsChargeId, savingsId);
        Assertions.assertTrue(chargeChanges.containsKey("amount"));

        Integer deletedChargeId = this.savingsAccountHelper.deleteCharge(savingsChargeId, savingsId);
        assertEquals(savingsChargeId, deletedChargeId);

        chargesPendingState = this.savingsAccountHelper.getSavingsCharges(savingsId);
        Assertions.assertTrue(chargesPendingState == null || chargesPendingState.size() == 0);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        final Integer chargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec, ChargesHelper.getSavingsAnnualFeeJSON());
        Assertions.assertNotNull(chargeId);

        ArrayList<HashMap> charges = this.savingsAccountHelper.getSavingsCharges(savingsId);
        Assertions.assertTrue(charges == null || charges.size() == 0);

        this.savingsAccountHelper.addChargesForSavings(savingsId, chargeId, true);
        charges = this.savingsAccountHelper.getSavingsCharges(savingsId);
        Assertions.assertEquals(1, charges.size());

        HashMap savingsChargeForPay = charges.get(0);
        Integer annualSavingsChargeId = (Integer) savingsChargeForPay.get("id");

        ArrayList<HashMap> savingsAccountErrorData = (ArrayList<HashMap>) validationErrorHelper.inactivateCharge(annualSavingsChargeId,
                savingsId, CommonConstants.RESPONSE_ERROR);
        assertEquals("validation.msg.savingsaccountcharge.inactivation.of.charge.not.allowed.when.charge.is.due",
                savingsAccountErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        SimpleDateFormat sdf = new SimpleDateFormat(CommonConstants.DATE_FORMAT, Locale.US);
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
        assertEquals(annualSavingsChargeId, inactivatedChargeId, "Inactivated Savings Charges Id");

        final Integer monthlyFeechargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsMonthlyFeeJSON());
        Assertions.assertNotNull(monthlyFeechargeId);

        this.savingsAccountHelper.addChargesForSavings(savingsId, monthlyFeechargeId, true);
        charges = this.savingsAccountHelper.getSavingsCharges(savingsId);
        Assertions.assertEquals(2, charges.size());

        HashMap savingsChargeForWaive = charges.get(1);
        final Integer monthlySavingsCharge = (Integer) savingsChargeForWaive.get("id");

        savingsAccountErrorData = (ArrayList<HashMap>) validationErrorHelper.inactivateCharge(monthlySavingsCharge, savingsId,
                CommonConstants.RESPONSE_ERROR);
        assertEquals("validation.msg.savingsaccountcharge.inactivation.of.charge.not.allowed.when.charge.is.due",
                savingsAccountErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

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
        Assertions.assertNotNull(weeklyFeeId);

        this.savingsAccountHelper.addChargesForSavings(savingsId, weeklyFeeId, true);
        charges = this.savingsAccountHelper.getSavingsCharges(savingsId);
        Assertions.assertEquals(3, charges.size());

        savingsChargeForPay = charges.get(2);
        final Integer weeklySavingsFeeId = (Integer) savingsChargeForPay.get("id");

        savingsAccountErrorData = (ArrayList<HashMap>) validationErrorHelper.inactivateCharge(weeklySavingsFeeId, savingsId,
                CommonConstants.RESPONSE_ERROR);
        assertEquals("validation.msg.savingsaccountcharge.inactivation.of.charge.not.allowed.when.charge.is.due",
                savingsAccountErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        cal = Calendar.getInstance();
        dates = (List) savingsChargeForPay.get("dueDate");
        cal.set(Calendar.YEAR, (Integer) dates.get(0));
        cal.set(Calendar.MONTH, (Integer) dates.get(1) - 1);
        cal.set(Calendar.DAY_OF_MONTH, (Integer) dates.get(2));

        // Depositing huge amount as scheduler job deducts the fee amount
        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100000",
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        Assertions.assertNotNull(depositTransactionId);

        this.savingsAccountHelper.payCharge((Integer) savingsChargeForPay.get("id"), savingsId,
                ((Float) savingsChargeForPay.get("amount")).toString(), sdf.format(cal.getTime()));
        HashMap paidCharge = this.savingsAccountHelper.getSavingsCharge(savingsId, (Integer) savingsChargeForPay.get("id"));
        assertEquals(savingsChargeForPay.get("amount"), paidCharge.get("amountPaid"));
        List nextDueDates = (List) paidCharge.get("dueDate");
        LocalDate nextDueDate = LocalDate.of((Integer) nextDueDates.get(0), (Integer) nextDueDates.get(1), (Integer) nextDueDates.get(2));
        LocalDate expectedNextDueDate = LocalDate.of((Integer) dates.get(0), (Integer) dates.get(1), (Integer) dates.get(2))
                .plusWeeks((Integer) paidCharge.get("feeInterval"));
        assertEquals(expectedNextDueDate, nextDueDate);
    }

    /***
     * Test case for overdraft account functionality. Open account with zero balance, perform transactions then post
     * interest and verify posted interest
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
        Assertions.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;

        /***
         * Create savings product with zero opening balance and overdraft enabled
         */
        final String zeroOpeningBalance = "0.0";
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = true;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, zeroOpeningBalance,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assertions.assertNotNull(savingsProductID);

        /***
         * Apply for Savings account
         */
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(savingsProductID);

        HashMap modifications = this.savingsAccountHelper.updateSavingsAccount(clientID, savingsProductID, savingsId,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertTrue(modifications.containsKey("submittedOnDate"));

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

        Float balance = Float.parseFloat(zeroOpeningBalance);

        /***
         * Perform withdraw transaction, verify account balance(account balance will go to negative as no deposits are
         * there prior to this transaction)
         */
        Integer withdrawTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, WITHDRAW_AMOUNT,
                ACTIVATION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        balance -= Float.parseFloat(WITHDRAW_AMOUNT);
        assertEquals(Float.parseFloat(WITHDRAW_AMOUNT), withdrawTransaction.get("amount"), "Verifying Withdrawal Amount");
        assertEquals(balance, withdrawTransaction.get("runningBalance"), "Verifying Balance after Withdrawal");

        /***
         * Perform Deposit transaction on last day of month and verify account balance.
         *
         * @param transactionDate
         *            this value is every time last day of previous month
         */
        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
                TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap depositTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, depositTransactionId);
        balance += Float.parseFloat(DEPOSIT_AMOUNT);
        assertEquals(Float.parseFloat(DEPOSIT_AMOUNT), depositTransaction.get("amount"), "Verifying Deposit Amount");
        assertEquals(balance, depositTransaction.get("runningBalance"), "Verifying Balance after Deposit");

        /***
         * Perform Post interest transaction and verify the posted amount
         */
        this.savingsAccountHelper.postInterestForSavings(savingsId);
        HashMap accountDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
        summary = (HashMap) accountDetails.get("summary");
        Float actualInterestPosted = Float.parseFloat(summary.get("totalInterestPosted").toString());

        /***
         * Calculate expected interest to be posted, interest should be posted for one day only because deposit
         * transaction happened on last day of month before this account balance is negative.
         */
        final Float nominalAnnualInterest = Float.parseFloat(accountDetails.get("nominalAnnualInterestRate").toString());
        final HashMap interestCalculationDaysInYearType = (HashMap) accountDetails.get("interestCalculationDaysInYearType");
        final Integer daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get("id").toString());
        double interestRateInFraction = nominalAnnualInterest / 100;
        double perDay = (double) 1 / daysInYear;
        double interestPerDay = interestRateInFraction * perDay;
        Float interestPosted = (float) (interestPerDay * balance * 1);

        /***
         * Apply rounding on interestPosted, actualInterestPosted and verify both are same
         */
        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern("#.###");
        interestPosted = Float.parseFloat(decimalFormat.format(interestPosted));
        actualInterestPosted = Float.parseFloat(decimalFormat.format(actualInterestPosted));
        assertEquals(interestPosted, actualInterestPosted, "Verifying interest posted");

        todaysDate = Calendar.getInstance();
        final String CLOSEDON_DATE = dateFormat.format(todaysDate.getTime());
        String withdrawBalance = "false";
        ArrayList<HashMap> savingsAccountErrorData = (ArrayList<HashMap>) validationErrorHelper
                .closeSavingsAccountAndGetBackRequiredField(savingsId, withdrawBalance, CommonConstants.RESPONSE_ERROR, CLOSEDON_DATE);
        assertEquals("validation.msg.savingsaccount.close.results.in.balance.not.zero",
                savingsAccountErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSavingsAccountPostInterestOnLastDayWithOverdraft() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        /***
         * Create a client to apply for savings account (overdraft account).
         */
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;

        /***
         * Create savings product with zero opening balance and overdraft enabled
         */
        final String zeroOpeningBalance = "0.0";
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = true;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, zeroOpeningBalance,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assertions.assertNotNull(savingsProductID);

        /***
         * Apply for Savings account
         */
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(savingsProductID);

        HashMap modifications = this.savingsAccountHelper.updateSavingsAccount(clientID, savingsProductID, savingsId,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertTrue(modifications.containsKey("submittedOnDate"));

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

        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());

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

        final Integer lastDayOfMonth = todaysDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        todaysDate.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
        final String WITHDRAWAL_DATE = dateFormat.format(todaysDate.getTime());
        Float balance = Float.parseFloat(zeroOpeningBalance);

        // DateFormat transactionDateFormat = new SimpleDateFormat("dd MMMM
        // yyyy",Locale.US);
        Calendar transactionDate = Calendar.getInstance();
        transactionDate.set(Calendar.DAY_OF_MONTH, 2);
        String transactionDateValue = dateFormat.format(transactionDate.getTime());

        /***
         * Perform Deposit transaction on last day of month and verify account balance.
         *
         * @param transactionDate
         *            this value is every time last day of previous month
         */
        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
                WITHDRAWAL_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap depositTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, depositTransactionId);
        balance += Float.parseFloat(DEPOSIT_AMOUNT);
        assertEquals(Float.parseFloat(DEPOSIT_AMOUNT), depositTransaction.get("amount"), "Verifying Deposit Amount");
        assertEquals(balance, depositTransaction.get("runningBalance"), "Verifying Balance after Deposit");

        /***
         * Calculate expected interest to be posted, interest should be posted for one day only because deposit
         * transaction happened on last day of month before this account balance is negative.
         */
        this.savingsAccountHelper.postInterestForSavings(savingsId);
        HashMap accountDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
        summary = (HashMap) accountDetails.get("summary");
        Float accountDetailsPostInterestPosted = Float.parseFloat(summary.get("totalInterestPosted").toString());

        Float nominalAnnualInterest = Float.parseFloat(accountDetails.get("nominalAnnualInterestRate").toString());
        HashMap interestCalculationDaysInYearType = (HashMap) accountDetails.get("interestCalculationDaysInYearType");
        Integer daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get("id").toString());
        double interestRateInFraction = nominalAnnualInterest / 100;
        double perDay = (double) 1 / daysInYear;
        double interestPerDay = interestRateInFraction * perDay;
        Float interestPosted = (float) (interestPerDay * balance * 1);

        /***
         * Apply rounding on interestPosted, actualInterestPosted and verify both are same
         */
        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern("#.###");
        interestPosted = Float.parseFloat(decimalFormat.format(interestPosted));
        accountDetailsPostInterestPosted = Float.parseFloat(decimalFormat.format(accountDetailsPostInterestPosted));
        assertEquals(interestPosted, accountDetailsPostInterestPosted, "Verifying interest posted");

        this.savingsAccountHelper.postInterestAsOnSavings(savingsId, transactionDateValue);
        HashMap accountTransactionDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
        summary = (HashMap) accountDetails.get("summary");
        Float accountDetailsPostInterest = Float.parseFloat(summary.get("totalInterestPosted").toString());

        nominalAnnualInterest = Float.parseFloat(accountDetails.get("nominalAnnualInterestRate").toString());
        interestCalculationDaysInYearType = (HashMap) accountDetails.get("interestCalculationDaysInYearType");
        daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get("id").toString());
        interestRateInFraction = nominalAnnualInterest / 100;
        perDay = (double) 1 / daysInYear;
        interestPerDay = interestRateInFraction * perDay;
        interestPosted = (float) (interestPerDay * balance * 1);

        /***
         * Apply rounding on interestPosted, actualInterestPosted and verify both are same
         */
        decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern("#.###");
        interestPosted = Float.parseFloat(decimalFormat.format(interestPosted));
        accountDetailsPostInterestPosted = Float.parseFloat(decimalFormat.format(accountDetailsPostInterestPosted));
        assertEquals(interestPosted, accountDetailsPostInterestPosted, "Verifying interest posted");
        LOG.info("-----Post Interest As on Successfully Worked----------");

        transactionDate.set(Calendar.DAY_OF_MONTH, 3);
        transactionDateValue = dateFormat.format(transactionDate.getTime());

        this.savingsAccountHelper.postInterestAsOnSavings(savingsId, transactionDateValue);
        accountTransactionDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
        summary = (HashMap) accountDetails.get("summary");
        accountDetailsPostInterest = Float.parseFloat(summary.get("totalInterestPosted").toString());

        nominalAnnualInterest = Float.parseFloat(accountDetails.get("nominalAnnualInterestRate").toString());
        interestCalculationDaysInYearType = (HashMap) accountDetails.get("interestCalculationDaysInYearType");
        daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get("id").toString());
        interestRateInFraction = nominalAnnualInterest / 100;
        perDay = (double) 1 / daysInYear;
        interestPerDay = interestRateInFraction * perDay;
        interestPosted = (float) (interestPerDay * balance * 1);

        /***
         * Apply rounding on interestPosted, actualInterestPosted and verify both are same
         */
        decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern("#.###");
        interestPosted = Float.parseFloat(decimalFormat.format(interestPosted));
        accountDetailsPostInterestPosted = Float.parseFloat(decimalFormat.format(accountDetailsPostInterestPosted));
        assertEquals(interestPosted, accountDetailsPostInterestPosted, "Verifying interest posted");
        LOG.info("-----Post Interest As on Successfully Worked-------");

        // DateFormat transactionFormat = new SimpleDateFormat("dd MMMM yyyy",
        // Locale.US);
        Calendar transactionCalendarDateFormat = Calendar.getInstance();
        transactionCalendarDateFormat.add(Calendar.DAY_OF_MONTH, 0);
        transactionDate.set(Calendar.DAY_OF_MONTH, 22);
        transactionDateValue = dateFormat.format(transactionDate.getTime());
        if (Calendar.DAY_OF_MONTH >= 22) {
            this.savingsAccountHelper.postInterestAsOnSavings(savingsId, transactionDateValue);
            accountTransactionDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
            summary = (HashMap) accountTransactionDetails.get("summary");
            accountDetailsPostInterest = Float.parseFloat(summary.get("totalInterestPosted").toString());

            nominalAnnualInterest = Float.parseFloat(accountDetails.get("nominalAnnualInterestRate").toString());
            interestCalculationDaysInYearType = (HashMap) accountDetails.get("interestCalculationDaysInYearType");
            daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get("id").toString());
            interestRateInFraction = nominalAnnualInterest / 100;
            perDay = (double) 1 / daysInYear;
            interestPerDay = interestRateInFraction * perDay;
            interestPosted = (float) (interestPerDay * balance * 19);

            /***
             * Apply rounding on interestPosted, actualInterestPosted and verify both are same
             */
            decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
            decimalFormat.applyPattern("#.###");
            interestPosted = Float.parseFloat(decimalFormat.format(interestPosted));
            accountDetailsPostInterestPosted = Float.parseFloat(decimalFormat.format(accountDetailsPostInterestPosted));
            assertEquals(interestPosted, accountDetailsPostInterestPosted, "Verifying interest posted");
            LOG.info("-----Post Interest As on Successfully Worked----------");
        }
        DateFormat lastTransactionDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Calendar postedLastDate = Calendar.getInstance();
        int numberOfDateOfMonth = postedLastDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        transactionDateValue = lastTransactionDateFormat.format(transactionDate.getTime());

        if (Calendar.DAY_OF_MONTH == numberOfDateOfMonth) {

            this.savingsAccountHelper.postInterestAsOnSavings(savingsId, transactionDateValue);
            accountTransactionDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
            summary = (HashMap) accountTransactionDetails.get("summary");
            accountDetailsPostInterest = Float.parseFloat(summary.get("totalInterestPosted").toString());

            nominalAnnualInterest = Float.parseFloat(accountDetails.get("nominalAnnualInterestRate").toString());
            interestCalculationDaysInYearType = (HashMap) accountDetails.get("interestCalculationDaysInYearType");
            daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get("id").toString());
            interestRateInFraction = nominalAnnualInterest / 100;
            perDay = (double) 1 / daysInYear;
            interestPerDay = interestRateInFraction * perDay;
            interestPosted = (float) (interestPerDay * balance * 8);

            /***
             * Apply rounding on interestPosted, actualInterestPosted and verify both are same
             */
            decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
            decimalFormat.applyPattern("#.###");
            interestPosted = Float.parseFloat(decimalFormat.format(interestPosted));
            accountDetailsPostInterestPosted = Float.parseFloat(decimalFormat.format(accountDetailsPostInterestPosted));
            assertEquals(interestPosted, accountDetailsPostInterestPosted, "Verifying interest posted");
            LOG.info("-----Post Interest As on Successfully Worked----------");

        }
        transactionDate.set(Calendar.DAY_OF_MONTH, 1);
        transactionDateValue = dateFormat.format(transactionDate.getTime());
        this.savingsAccountHelper.postInterestAsOnSavings(savingsId, transactionDateValue);
        accountTransactionDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
        summary = (HashMap) accountTransactionDetails.get("summary");
        accountDetailsPostInterest = Float.parseFloat(summary.get("totalInterestPosted").toString());

        nominalAnnualInterest = Float.parseFloat(accountDetails.get("nominalAnnualInterestRate").toString());
        interestCalculationDaysInYearType = (HashMap) accountDetails.get("interestCalculationDaysInYearType");
        daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get("id").toString());
        interestRateInFraction = nominalAnnualInterest / 100;
        perDay = (double) 1 / daysInYear;
        interestPerDay = interestRateInFraction * perDay;
        interestPosted = (float) (interestPerDay * balance * 1);

        /***
         * Apply rounding on interestPosted, actualInterestPosted and verify both are same
         */
        decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern("#.###");
        interestPosted = Float.parseFloat(decimalFormat.format(interestPosted));
        accountDetailsPostInterestPosted = Float.parseFloat(decimalFormat.format(accountDetailsPostInterestPosted));
        assertEquals(interestPosted, accountDetailsPostInterestPosted, "Verifying interest posted");
        LOG.info("-----Post Interest As on Successfully Worked----------");

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSavingsAccountPostInterestOnLastDayWithdrawalWithOverdraft() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        /***
         * Create a client to apply for savings account (overdraft account).
         */
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;

        /***
         * Create savings product with zero opening balance and overdraft enabled
         */
        final String zeroOpeningBalance = "0.0";
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = true;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, zeroOpeningBalance,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assertions.assertNotNull(savingsProductID);

        /***
         * Apply for Savings account
         */
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(savingsProductID);

        HashMap modifications = this.savingsAccountHelper.updateSavingsAccount(clientID, savingsProductID, savingsId,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertTrue(modifications.containsKey("submittedOnDate"));

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

        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());

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

        final Integer lastDayOfMonth = todaysDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        todaysDate.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
        final String WITHDRAWAL_DATE = dateFormat.format(todaysDate.getTime());
        Float balance = Float.parseFloat(zeroOpeningBalance);

        // DateFormat transactionDateFormat = new SimpleDateFormat("dd MMMM
        // yyyy", Locale.US);
        Calendar transactionDate = Calendar.getInstance();
        transactionDate.set(Calendar.DAY_OF_MONTH, 2);
        String transactionDateValue = dateFormat.format(transactionDate.getTime());

        /***
         * Perform withdraw transaction, verify account balance(account balance will go to negative as no deposits are
         * there prior to this transaction)
         */
        Integer withdrawTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, WITHDRAW_AMOUNT,
                ACTIVATION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        balance -= Float.parseFloat(WITHDRAW_AMOUNT);
        assertEquals(Float.parseFloat(WITHDRAW_AMOUNT), withdrawTransaction.get("amount"), "Verifying Withdrawal Amount");
        assertEquals(balance, withdrawTransaction.get("runningBalance"), "Verifying Balance after Withdrawal");

        /***
         * Perform Deposit transaction on last day of month and verify account balance.
         *
         * @param transactionDate
         *            this value is every time last day of previous month
         */
        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
                WITHDRAWAL_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap depositTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, depositTransactionId);
        balance += Float.parseFloat(DEPOSIT_AMOUNT);
        assertEquals(Float.parseFloat(DEPOSIT_AMOUNT), depositTransaction.get("amount"), "Verifying Deposit Amount");
        assertEquals(balance, depositTransaction.get("runningBalance"), "Verifying Balance after Deposit");

        /***
         * Calculate expected interest to be posted, interest should be posted for one day only because deposit
         * transaction happened on last day of month before this account balance is negative.
         */
        this.savingsAccountHelper.postInterestForSavings(savingsId);
        HashMap accountDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
        summary = (HashMap) accountDetails.get("summary");
        Float accountDetailsPostInterestPosted = Float.parseFloat(summary.get("totalInterestPosted").toString());

        Float nominalAnnualInterest = Float.parseFloat(accountDetails.get("nominalAnnualInterestRate").toString());
        HashMap interestCalculationDaysInYearType = (HashMap) accountDetails.get("interestCalculationDaysInYearType");
        Integer daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get("id").toString());
        double interestRateInFraction = nominalAnnualInterest / 100;
        double perDay = (double) 1 / daysInYear;
        double interestPerDay = interestRateInFraction * perDay;
        Float interestPosted = (float) (interestPerDay * balance * 1);

        /***
         * Apply rounding on interestPosted, actualInterestPosted and verify both are same
         */
        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern("#.###");
        interestPosted = Float.parseFloat(decimalFormat.format(interestPosted));
        accountDetailsPostInterestPosted = Float.parseFloat(decimalFormat.format(accountDetailsPostInterestPosted));
        assertEquals(interestPosted, accountDetailsPostInterestPosted, "Verifying interest posted");

        this.savingsAccountHelper.postInterestAsOnSavings(savingsId, transactionDateValue);
        HashMap accountTransactionDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
        summary = (HashMap) accountDetails.get("summary");
        Float accountDetailsPostInterest = Float.parseFloat(summary.get("totalInterestPosted").toString());

        nominalAnnualInterest = Float.parseFloat(accountDetails.get("nominalAnnualInterestRate").toString());
        interestCalculationDaysInYearType = (HashMap) accountDetails.get("interestCalculationDaysInYearType");
        daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get("id").toString());
        interestRateInFraction = nominalAnnualInterest / 100;
        perDay = (double) 1 / daysInYear;
        interestPerDay = interestRateInFraction * perDay;
        interestPosted = (float) (interestPerDay * balance * 1);

        /***
         * Apply rounding on interestPosted, actualInterestPosted and verify both are same
         */
        decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern("#.###");
        interestPosted = Float.parseFloat(decimalFormat.format(interestPosted));
        accountDetailsPostInterestPosted = Float.parseFloat(decimalFormat.format(accountDetailsPostInterestPosted));
        assertEquals(interestPosted, accountDetailsPostInterestPosted, "Verifying interest posted");
        LOG.info("-----Post Interest As on Successfully Worked----------");

        transactionDate.set(Calendar.DAY_OF_MONTH, 3);
        transactionDateValue = dateFormat.format(transactionDate.getTime());

        this.savingsAccountHelper.postInterestAsOnSavings(savingsId, transactionDateValue);
        accountTransactionDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
        summary = (HashMap) accountDetails.get("summary");
        accountDetailsPostInterest = Float.parseFloat(summary.get("totalInterestPosted").toString());

        nominalAnnualInterest = Float.parseFloat(accountDetails.get("nominalAnnualInterestRate").toString());
        interestCalculationDaysInYearType = (HashMap) accountDetails.get("interestCalculationDaysInYearType");
        daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get("id").toString());
        interestRateInFraction = nominalAnnualInterest / 100;
        perDay = (double) 1 / daysInYear;
        interestPerDay = interestRateInFraction * perDay;
        interestPosted = (float) (interestPerDay * balance * 1);

        /***
         * Apply rounding on interestPosted, actualInterestPosted and verify both are same
         */
        decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern("#.###");
        interestPosted = Float.parseFloat(decimalFormat.format(interestPosted));
        accountDetailsPostInterestPosted = Float.parseFloat(decimalFormat.format(accountDetailsPostInterestPosted));
        assertEquals(interestPosted, accountDetailsPostInterestPosted, "Verifying interest posted");
        LOG.info("-----Post Interest As on Successfully Worked-------");

        // DateFormat transactionFormat = new SimpleDateFormat("dd MMMM yyyy",
        // Locale.US);
        Calendar transactionCalendarDateFormat = Calendar.getInstance();
        transactionCalendarDateFormat.add(Calendar.DAY_OF_MONTH, 0);
        transactionDate.set(Calendar.DAY_OF_MONTH, 22);
        transactionDateValue = dateFormat.format(transactionDate.getTime());
        if (Calendar.DAY_OF_MONTH >= 22) {
            this.savingsAccountHelper.postInterestAsOnSavings(savingsId, transactionDateValue);
            accountTransactionDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
            summary = (HashMap) accountTransactionDetails.get("summary");
            accountDetailsPostInterest = Float.parseFloat(summary.get("totalInterestPosted").toString());

            nominalAnnualInterest = Float.parseFloat(accountDetails.get("nominalAnnualInterestRate").toString());
            interestCalculationDaysInYearType = (HashMap) accountDetails.get("interestCalculationDaysInYearType");
            daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get("id").toString());
            interestRateInFraction = nominalAnnualInterest / 100;
            perDay = (double) 1 / daysInYear;
            interestPerDay = interestRateInFraction * perDay;
            interestPosted = (float) (interestPerDay * balance * 19);

            /***
             * Apply rounding on interestPosted, actualInterestPosted and verify both are same
             */
            decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
            decimalFormat.applyPattern("#.###");
            interestPosted = Float.parseFloat(decimalFormat.format(interestPosted));
            accountDetailsPostInterestPosted = Float.parseFloat(decimalFormat.format(accountDetailsPostInterestPosted));
            assertEquals(interestPosted, accountDetailsPostInterestPosted, "Verifying interest posted");
            LOG.info("-----Post Interest As on Successfully Worked----------");
        }
        DateFormat lastTransactionDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Calendar postedLastDate = Calendar.getInstance();
        int numberOfDateOfMonth = postedLastDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        transactionDateValue = lastTransactionDateFormat.format(transactionDate.getTime());

        if (Calendar.DAY_OF_MONTH == numberOfDateOfMonth) {

            this.savingsAccountHelper.postInterestAsOnSavings(savingsId, transactionDateValue);
            accountTransactionDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
            summary = (HashMap) accountTransactionDetails.get("summary");
            accountDetailsPostInterest = Float.parseFloat(summary.get("totalInterestPosted").toString());

            nominalAnnualInterest = Float.parseFloat(accountDetails.get("nominalAnnualInterestRate").toString());
            interestCalculationDaysInYearType = (HashMap) accountDetails.get("interestCalculationDaysInYearType");
            daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get("id").toString());
            interestRateInFraction = nominalAnnualInterest / 100;
            perDay = (double) 1 / daysInYear;
            interestPerDay = interestRateInFraction * perDay;
            interestPosted = (float) (interestPerDay * balance * 8);

            /***
             * Apply rounding on interestPosted, actualInterestPosted and verify both are same
             */
            decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
            decimalFormat.applyPattern("#.###");
            interestPosted = Float.parseFloat(decimalFormat.format(interestPosted));
            accountDetailsPostInterestPosted = Float.parseFloat(decimalFormat.format(accountDetailsPostInterestPosted));
            assertEquals(interestPosted, accountDetailsPostInterestPosted, "Verifying interest posted");
            LOG.info("-----Post Interest As on Successfully Worked----------");

        }
        transactionDate.set(Calendar.DAY_OF_MONTH, 1);
        transactionDateValue = dateFormat.format(transactionDate.getTime());
        this.savingsAccountHelper.postInterestAsOnSavings(savingsId, transactionDateValue);
        accountTransactionDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
        summary = (HashMap) accountTransactionDetails.get("summary");
        accountDetailsPostInterest = Float.parseFloat(summary.get("totalInterestPosted").toString());

        nominalAnnualInterest = Float.parseFloat(accountDetails.get("nominalAnnualInterestRate").toString());
        interestCalculationDaysInYearType = (HashMap) accountDetails.get("interestCalculationDaysInYearType");
        daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get("id").toString());
        interestRateInFraction = nominalAnnualInterest / 100;
        perDay = (double) 1 / daysInYear;
        interestPerDay = interestRateInFraction * perDay;
        interestPosted = (float) (interestPerDay * balance * 1);

        /***
         * Apply rounding on interestPosted, actualInterestPosted and verify both are same
         */
        decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern("#.###");
        interestPosted = Float.parseFloat(decimalFormat.format(interestPosted));
        accountDetailsPostInterestPosted = Float.parseFloat(decimalFormat.format(accountDetailsPostInterestPosted));
        assertEquals(interestPosted, accountDetailsPostInterestPosted, "Verifying interest posted");
        LOG.info("-----Post Interest As on Successfully Worked----------");
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
        Assertions.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;

        /***
         * Create savings product with zero opening balance and overdraft enabled
         */
        final String zeroOpeningBalance = "0.0";
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = true;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, zeroOpeningBalance,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assertions.assertNotNull(savingsProductID);

        /***
         * Apply for Savings account
         */
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(savingsProductID);

        HashMap modifications = this.savingsAccountHelper.updateSavingsAccount(clientID, savingsProductID, savingsId,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertTrue(modifications.containsKey("submittedOnDate"));

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

        Float balance = Float.parseFloat(zeroOpeningBalance);

        /***
         * Perform withdraw transaction, verify account balance(account balance will go to negative as no deposits are
         * there prior to this transaction)
         */
        Integer withdrawTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, WITHDRAW_AMOUNT,
                ACTIVATION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        balance -= Float.parseFloat(WITHDRAW_AMOUNT);
        assertEquals(Float.parseFloat(WITHDRAW_AMOUNT), withdrawTransaction.get("amount"), "Verifying Withdrawal Amount");
        assertEquals(balance, withdrawTransaction.get("runningBalance"), "Verifying Balance after Withdrawal");

        /***
         * Perform Deposit transaction on last day of month and verify account balance.
         *
         * @param transactionDate
         *            this value is every time last day of previous month
         */
        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
                TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap depositTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, depositTransactionId);
        balance += Float.parseFloat(DEPOSIT_AMOUNT);
        assertEquals(Float.parseFloat(DEPOSIT_AMOUNT), depositTransaction.get("amount"), "Verifying Deposit Amount");
        assertEquals(balance, depositTransaction.get("runningBalance"), "Verifying Balance after Deposit");

        /***
         * Perform Post interest transaction and verify the posted amount
         */
        this.savingsAccountHelper.postInterestForSavings(savingsId);
        HashMap accountDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
        summary = (HashMap) accountDetails.get("summary");
        Float actualInterestPosted = Float.parseFloat(summary.get("totalInterestPosted").toString());

        /***
         * Calculate expected interest to be posted, interest should be posted for one day only because deposit
         * transaction happened on last day of month before this account balance is negative.
         */
        this.savingsAccountHelper.postInterestAsOnSavings(savingsId, POSTED_TRANSACTION_DATE);
        HashMap accountDetailsPostInterest = this.savingsAccountHelper.getSavingsDetails(savingsId);
        summary = (HashMap) accountDetails.get("summary");
        ArrayList interestPostingTransaction = (ArrayList) ((HashMap) ((ArrayList) accountDetails.get("transactions")).get(0)).get("date");
        Float accountDetailsPostInterestPosted = Float.parseFloat(summary.get("totalInterestPosted").toString());

        /***
         * Calculate expected interest to be posted, interest should be posted for one day only because deposit
         * transaction happened on last day of month before this account balance is negative.
         */
        final Float nominalAnnualInterest = Float.parseFloat(accountDetails.get("nominalAnnualInterestRate").toString());
        final HashMap interestCalculationDaysInYearType = (HashMap) accountDetails.get("interestCalculationDaysInYearType");
        final Integer daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get("id").toString());
        double interestRateInFraction = nominalAnnualInterest / 100;
        double perDay = (double) 1 / daysInYear;
        double interestPerDay = interestRateInFraction * perDay;
        Float interestPosted = (float) (interestPerDay * balance * 1);

        /***
         * Apply rounding on interestPosted, actualInterestPosted and verify both are same
         */
        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern("#.###");
        interestPosted = Float.parseFloat(decimalFormat.format(interestPosted));
        // actualInterestPosted =
        // Float.parseFloat(decimalFormat.format(accountDetailsPostInterestPosted));
        assertEquals(interestPosted, accountDetailsPostInterestPosted, "Verifying interest posted");
        LOG.info("------Post Interest As On After doing a post interest Successfully worked--------");

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
            final SavingsAccountHelper validationErrorHelper = new SavingsAccountHelper(this.requestSpec, responseSpec);
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

    @Test
    public void testPostInterestAsOnSavingsAccountWithOverdraft() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        // final ResponseSpecification errorResponse = new
        // ResponseSpecBuilder().expectStatusCode(400).build();
        // final SavingsAccountHelper validationErrorHelper = new
        // SavingsAccountHelper(this.requestSpec, errorResponse);

        /***
         * Create a client to apply for savings account (overdraft account).
         */
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;

        /***
         * Create savings product with zero opening balance and overdraft enabled
         */
        final String zeroOpeningBalance = "0.0";
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = true;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, zeroOpeningBalance,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assertions.assertNotNull(savingsProductID);

        /***
         * Apply for Savings account
         */
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(savingsProductID);

        HashMap modifications = this.savingsAccountHelper.updateSavingsAccount(clientID, savingsProductID, savingsId,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertTrue(modifications.containsKey("submittedOnDate"));

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
        int countOfDate = postedDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        LOG.info("count Of Date---> {}", countOfDate);
        postedLastDate.set(Calendar.DAY_OF_MONTH, countOfDate);
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

        Float balance = Float.parseFloat(zeroOpeningBalance);

        /***
         * Perform withdraw transaction, verify account balance(account balance will go to negative as no deposits are
         * there prior to this transaction)
         */
        Integer withdrawTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, WITHDRAW_AMOUNT,
                ACTIVATION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        balance -= Float.parseFloat(WITHDRAW_AMOUNT);
        assertEquals(Float.parseFloat(WITHDRAW_AMOUNT), withdrawTransaction.get("amount"), "Verifying Withdrawal Amount");
        assertEquals(balance, withdrawTransaction.get("runningBalance"), "Verifying Balance after Withdrawal");

        /***
         * Perform Deposit transaction on last day of month and verify account balance.
         *
         * @param transactionDate
         *            this value is every time last day of previous month
         */
        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
                TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap depositTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, depositTransactionId);
        balance += Float.parseFloat(DEPOSIT_AMOUNT);
        assertEquals(Float.parseFloat(DEPOSIT_AMOUNT), depositTransaction.get("amount"), "Verifying Deposit Amount");
        assertEquals(balance, depositTransaction.get("runningBalance"), "Verifying Balance after Deposit");

        /***
         * Perform Post interest transaction and verify the posted amount
         */
        this.savingsAccountHelper.postInterestAsOnSavings(savingsId, POSTED_TRANSACTION_DATE);
        HashMap accountDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
        summary = (HashMap) accountDetails.get("summary");
        Float actualInterestPosted = Float.parseFloat(summary.get("totalInterestPosted").toString());

        /***
         * Calculate expected interest to be posted, interest should be posted for one day only because deposit
         * transaction happened on last day of month before this account balance is negative.
         */
        final Float nominalAnnualInterest = Float.parseFloat(accountDetails.get("nominalAnnualInterestRate").toString());
        final HashMap interestCalculationDaysInYearType = (HashMap) accountDetails.get("interestCalculationDaysInYearType");
        final Integer daysInYear = Integer.valueOf(interestCalculationDaysInYearType.get("id").toString());
        double interestRateInFraction = nominalAnnualInterest / 100;
        double perDay = (double) 1 / daysInYear;
        double interestPerDay = interestRateInFraction * perDay;
        Float interestPosted = (float) (interestPerDay * balance * 1);

        /***
         * Apply rounding on interestPosted, actualInterestPosted and verify both are same
         */
        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern("#.###");
        interestPosted = Float.parseFloat(decimalFormat.format(interestPosted));
        actualInterestPosted = Float.parseFloat(decimalFormat.format(actualInterestPosted));
        assertEquals(interestPosted, actualInterestPosted, "Verifying interest posted");
        LOG.info("------Post Interest As On Successful Worked--------");

        this.savingsAccountHelper.postInterestAsOnSavings(savingsId, POSTED_LAST_TRANSACTION_DATE);
        HashMap accountLastDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
        summary = (HashMap) accountLastDetails.get("summary");
        Float actualLastInterestPosted = Float.parseFloat(summary.get("totalInterestPosted").toString());

        final Float nominalLastAnnualInterest = Float.parseFloat(accountDetails.get("nominalAnnualInterestRate").toString());
        final HashMap interestLastCalculationDaysInYearType = (HashMap) accountDetails.get("interestCalculationDaysInYearType");
        final Integer daysLastInYear = Integer.valueOf(interestCalculationDaysInYearType.get("id").toString());
        double interestLastRateInFraction = nominalAnnualInterest / 100;
        double perLastDay = (double) 1 / daysInYear;
        double interestLastPerDay = interestLastRateInFraction * perLastDay;
        Float interestLastPosted = (float) (interestLastPerDay * balance * 1);

        DecimalFormat decimalLastFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalLastFormat.applyPattern("#.###");
        interestLastPosted = Float.parseFloat(decimalLastFormat.format(interestLastPosted));
        actualInterestPosted = Float.parseFloat(decimalFormat.format(actualInterestPosted));
        assertEquals(interestLastPosted, actualInterestPosted, "Verifying interest posted");
        LOG.info("------Post Interest As On Successful Worked--------");

    }

    @Test
    public void testSavingsAccount_WITH_WITHHOLD_TAX() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final String percentage = "10";
        final Integer taxGroupId = createTaxGroup(percentage);
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft, String.valueOf(taxGroupId),
                false);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(savingsProductID);

        HashMap modifications = this.savingsAccountHelper.updateSavingsAccount(clientID, savingsProductID, savingsId,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertTrue(modifications.containsKey("submittedOnDate"));

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
        Assertions.assertFalse(summaryBefore.equals(summary));
        Assertions.assertNotNull(summary.get("totalWithholdTax"));
        Float expected = (Float) summary.get("totalDeposits") + (Float) summary.get("totalInterestPosted")
                - (Float) summary.get("totalWithholdTax");
        Float actual = (Float) summary.get("accountBalance");
        Assertions.assertEquals(expected, actual, 1);

    }

    @Test
    public void testSavingsAccount_WITH_WITHHOLD_TAX_DISABLE_AT_ACCOUNT_LEVEL() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final String percentage = "10";
        final Integer taxGroupId = createTaxGroup(percentage);
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft, String.valueOf(taxGroupId),
                false);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(savingsProductID);

        HashMap modifications = this.savingsAccountHelper.updateSavingsAccount(clientID, savingsProductID, savingsId,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertTrue(modifications.containsKey("submittedOnDate"));

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
        Assertions.assertTrue(changes.containsKey("withHoldTax"));

        this.savingsAccountHelper.postInterestForSavings(savingsId);
        summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        Assertions.assertFalse(summaryBefore.equals(summary));
        Assertions.assertNull(summary.get("totalWithholdTax"));
        Float expected = (Float) summary.get("totalDeposits") + (Float) summary.get("totalInterestPosted");
        Float actual = (Float) summary.get("accountBalance");
        Assertions.assertEquals(expected, actual, 1);

    }

    @Test
    public void testSavingsAccount_DormancyTracking() throws InterruptedException {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final String percentage = "10";
        final Integer taxGroupId = createTaxGroup(percentage);
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft, String.valueOf(taxGroupId),
                true);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsNoActivityFeeJSON());
        Assertions.assertNotNull(savingsChargeId);

        ArrayList<Integer> savingsList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID,
                    ACCOUNT_TYPE_INDIVIDUAL);
            Assertions.assertNotNull(savingsProductID);

            HashMap modifications = this.savingsAccountHelper.updateSavingsAccount(clientID, savingsProductID, savingsId,
                    ACCOUNT_TYPE_INDIVIDUAL);
            Assertions.assertTrue(modifications.containsKey("submittedOnDate"));

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
        Assertions.assertNotNull(savingsProductID);

        HashMap modifications = this.savingsAccountHelper.updateSavingsAccount(clientID, savingsProductID, savingsId,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertTrue(modifications.containsKey("submittedOnDate"));

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsList.add(savingsId);

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        LocalDate transactionDate = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());
        for (int i = 0; i < 4; i++) {
            String transactionDateValue = formatter.format(transactionDate);
            Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsList.get(i), DEPOSIT_AMOUNT,
                    transactionDateValue, CommonConstants.RESPONSE_RESOURCE_ID);
            transactionDate = transactionDate.minusDays(30);
        }

        SchedulerJobHelper jobHelper = new SchedulerJobHelper(this.requestSpec);
        jobHelper.executeAndAwaitJob("Update Savings Dormant Accounts");

        // VERIFY WITHIN PROVIDED RANGE DOESN'T INACTIVATE
        savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(0));
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        savingsStatusHashMap = SavingsStatusChecker.getSubStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(0));
        SavingsStatusChecker.verifySavingsSubStatusNone(savingsStatusHashMap);
        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsList.get(0));
        Float balance = 3000f;
        Float chargeAmt = 0f;
        balance -= chargeAmt;
        assertEquals(balance, summary.get("accountBalance"), "Verifying account Balance");

        // VERIFY INACTIVE
        savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(1));
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        savingsStatusHashMap = SavingsStatusChecker.getSubStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(1));
        SavingsStatusChecker.verifySavingsSubStatusInactive(savingsStatusHashMap);
        summary = this.savingsAccountHelper.getSavingsSummary(savingsList.get(1));
        balance = 3000f;
        chargeAmt = 100f;
        balance -= chargeAmt;
        assertEquals(balance, summary.get("accountBalance"), "Verifying account Balance");

        String transactionDateValue = formatter.format(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()));
        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsList.get(1), DEPOSIT_AMOUNT,
                transactionDateValue, CommonConstants.RESPONSE_RESOURCE_ID);
        savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(1));
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        savingsStatusHashMap = SavingsStatusChecker.getSubStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(1));
        SavingsStatusChecker.verifySavingsSubStatusNone(savingsStatusHashMap);

        // VERIFY DORMANT
        savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(2));
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        savingsStatusHashMap = SavingsStatusChecker.getSubStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(2));
        SavingsStatusChecker.verifySavingsSubStatusDormant(savingsStatusHashMap);
        summary = this.savingsAccountHelper.getSavingsSummary(savingsList.get(2));
        balance = 3000f;
        chargeAmt = 100f;
        balance -= chargeAmt;
        assertEquals(balance, summary.get("accountBalance"), "Verifying account Balance");

        transactionDateValue = formatter.format(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()));
        depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsList.get(2), DEPOSIT_AMOUNT,
                transactionDateValue, CommonConstants.RESPONSE_RESOURCE_ID);
        savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(2));
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        savingsStatusHashMap = SavingsStatusChecker.getSubStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(2));
        SavingsStatusChecker.verifySavingsSubStatusNone(savingsStatusHashMap);

        // VERIFY ESCHEAT DUE TO OLD TRANSACTION
        savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(3));
        SavingsStatusChecker.verifySavingsAccountIsClosed(savingsStatusHashMap);
        savingsStatusHashMap = SavingsStatusChecker.getSubStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(3));
        SavingsStatusChecker.verifySavingsSubStatusEscheat(savingsStatusHashMap);
        summary = this.savingsAccountHelper.getSavingsSummary(savingsList.get(3));
        assertEquals(2900f, summary.get("accountBalance"), "Verifying account Balance");

        // VERIFY ESCHEAT DUE NO TRANSACTION FROM ACTIVATION
        savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(4));
        SavingsStatusChecker.verifySavingsAccountIsClosed(savingsStatusHashMap);
        savingsStatusHashMap = SavingsStatusChecker.getSubStatusOfSavings(this.requestSpec, this.responseSpec, savingsList.get(4));
        SavingsStatusChecker.verifySavingsSubStatusEscheat(savingsStatusHashMap);
        summary = this.savingsAccountHelper.getSavingsSummary(savingsList.get(4));
        assertEquals(900f, summary.get("accountBalance"), "Verifying account Balance");

        // VERIFY NON ACTIVE ACCOUNTS ARE NOT AFFECTED
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
        LOG.info("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        SavingsProductHelper savingsProductHelper = new SavingsProductHelper();
        if (allowOverdraft) {
            final String overDraftLimit = "2000.0";
            savingsProductHelper = savingsProductHelper.withOverDraft(overDraftLimit);
        }
        if (withDormancy) {
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

    private Integer createTaxGroup(final String percentage) {
        final Integer liabilityAccountId = null;
        final Integer taxComponentId = TaxComponentHelper.createTaxComponent(this.requestSpec, this.responseSpec, percentage,
                liabilityAccountId);
        return TaxGroupHelper.createTaxGroup(this.requestSpec, this.responseSpec, Arrays.asList(taxComponentId));
    }

    /*
     * private void verifySavingsInterest(final Object savingsInterest) { LOG.info(
     * "--------------------VERIFYING THE BALANCE, INTEREST --------------------------" );
     *
     * assertEquals("Verifying Interest Calculation", Float.parseFloat("238.3399"), savingsInterest); }
     */

    @SuppressWarnings("unchecked")
    @Test
    public void testSavingsAccountBlockStatus() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        SavingsAccountHelper savingsAccountHelperValidationError = new SavingsAccountHelper(this.requestSpec,
                new ResponseSpecBuilder().build());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(savingsProductID);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        Float balance = Float.parseFloat(MINIMUM_OPENING_BALANCE);

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
        balance += Float.parseFloat(DEPOSIT_AMOUNT);
        assertEquals(Float.parseFloat(DEPOSIT_AMOUNT), depositTransaction.get("amount"), "Verifying Deposit Amount");

        savingsStatusHashMap = this.savingsAccountHelper.blockDebit(savingsId);
        SavingsStatusChecker.verifySavingsSubStatusIsDebitBlocked(savingsStatusHashMap);
        error = (List) savingsAccountHelperValidationError.withdrawalFromSavingsAccount(savingsId, "100",
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.savings.account.debit.transaction.not.allowed",
                error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        depositTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, depositTransactionId);
        balance += Float.parseFloat(DEPOSIT_AMOUNT);
        assertEquals(Float.parseFloat(DEPOSIT_AMOUNT), depositTransaction.get("amount"), "Verifying Deposit Amount");

        savingsStatusHashMap = this.savingsAccountHelper.unblockDebit(savingsId);
        SavingsStatusChecker.verifySavingsSubStatusIsNone(savingsStatusHashMap);
        Integer withdrawTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, WITHDRAW_AMOUNT,
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        balance -= Float.parseFloat(WITHDRAW_AMOUNT);
        assertEquals(Float.parseFloat(WITHDRAW_AMOUNT), withdrawTransaction.get("amount"), "Verifying Withdrawal Amount");

        savingsStatusHashMap = this.savingsAccountHelper.blockCredit(savingsId);
        SavingsStatusChecker.verifySavingsSubStatusIsCreditBlocked(savingsStatusHashMap);
        error = (List) savingsAccountHelperValidationError.depositToSavingsAccount(savingsId, "100", SavingsAccountHelper.TRANSACTION_DATE,
                CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.savings.account.credit.transaction.not.allowed",
                error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        withdrawTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, WITHDRAW_AMOUNT,
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        balance -= Float.parseFloat(WITHDRAW_AMOUNT);
        assertEquals(Float.parseFloat(WITHDRAW_AMOUNT), withdrawTransaction.get("amount"), "Verifying Withdrawal Amount");

        savingsStatusHashMap = this.savingsAccountHelper.unblockCredit(savingsId);
        SavingsStatusChecker.verifySavingsSubStatusIsNone(savingsStatusHashMap);
        depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        depositTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, depositTransactionId);
        balance += Float.parseFloat(DEPOSIT_AMOUNT);
        assertEquals(Float.parseFloat(DEPOSIT_AMOUNT), depositTransaction.get("amount"), "Verifying Deposit Amount");

        Integer holdTransactionId = (Integer) this.savingsAccountHelper.holdAmountInSavingsAccount(savingsId, String.valueOf(balance - 100),
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        error = (List) savingsAccountHelperValidationError.withdrawalFromSavingsAccount(savingsId, "300",
                SavingsAccountHelper.TRANSACTION_DATE_PLUS_ONE, CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.savingsaccount.transaction.insufficient.account.balance",
                error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        Integer releaseTransactionId = this.savingsAccountHelper.releaseAmount(savingsId, holdTransactionId);
        Date today = Date.from(Utils.getLocalDateOfTenant().atStartOfDay(DateUtils.getDateTimeZoneOfTenant()).toInstant());
        String todayDate = today.toString();
        SimpleDateFormat dt1 = new SimpleDateFormat("dd MMM yyyy");
        todayDate = dt1.format(today).toString();
        withdrawTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "300", todayDate,
                CommonConstants.RESPONSE_RESOURCE_ID);
        withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        balance -= Float.parseFloat("300");
        assertEquals(Float.parseFloat("300"), withdrawTransaction.get("amount"), "Verifying Withdrawal Amount");

    }

    /**
     * incorrect savings account balance when charge transaction is reversed during an overdraft recalculate Daily
     * Balances
     */

    @Test
    public void testAccountBalanceAfterTransactionReversal() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        // SavingsAccountHelper savingsAccountHelperValidationError = new
        // SavingsAccountHelper(this.requestSpec,new
        // ResponseSpecBuilder().build());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientID);
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = "500";
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = true;
        final String MINIMUM_OPENING_BALANCE = "0";

        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(savingsId);

        HashMap savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, "500",
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);

        String chargeAmount = "300";
        String chargeCurrency = "USD";

        final Integer savingsChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsJSON(chargeAmount, chargeCurrency, ChargeTimeType.SPECIFIED_DUE_DATE));

        Assertions.assertNotNull(savingsChargeId);

        Integer amount = 300;

        final Integer chargeId = this.savingsAccountHelper.addChargesForSavingsWithDueDate(savingsId, savingsChargeId,
                SavingsAccountHelper.TRANSACTION_DATE, amount);

        Assertions.assertNotNull(chargeId);

        final Integer payChargeId = this.savingsAccountHelper.payCharge(chargeId, savingsId, chargeAmount,
                SavingsAccountHelper.TRANSACTION_DATE);

        final Integer undoSavingsTransaction = this.savingsAccountHelper.undoSavingsAccountTransaction(savingsId, depositTransactionId);
        HashMap reversedDepositTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, depositTransactionId);
        Assertions.assertTrue((Boolean) reversedDepositTransaction.get("reversed"));

        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsId);

        Float balance = Float.parseFloat("-300");

        assertEquals(balance, summary.get("accountBalance"), "Verifying opening Balance is -300");

    }
}
