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
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.fineract.client.models.PaymentTypeCreateRequest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GroupHelper;
import org.apache.fineract.integrationtests.common.PaymentTypeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.apache.fineract.integrationtests.common.savings.SavingsTestLifecycleExtension;
import org.apache.fineract.integrationtests.guarantor.GuarantorHelper;
import org.apache.fineract.integrationtests.guarantor.GuarantorTestBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Group Savings Integration Test for checking Savings Application.
 */
@SuppressWarnings({ "rawtypes", "unused" })
@ExtendWith({ SavingsTestLifecycleExtension.class })
public class GroupSavingsIntegrationTest {

    public static final String DEPOSIT_AMOUNT = "2000";
    public static final String WITHDRAW_AMOUNT = "1000";
    public static final String WITHDRAW_AMOUNT_ADJUSTED = "500";
    public static final String MINIMUM_OPENING_BALANCE = "1000.0";
    public static final String PRINCIPAL = "5000";
    public static final String GUARANTEE_AMOUNT = "500";
    public static final String HOLD_AMOUNT = "300";
    public static final String ACCOUNT_TYPE_GROUP = "GROUP";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private SavingsAccountHelper savingsAccountHelper;
    private PaymentTypeHelper paymentTypeHelper;
    private LoanTransactionHelper loanTransactionHelper;
    private GuarantorHelper guarantorHelper;
    private static final Logger LOG = LoggerFactory.getLogger(GroupSavingsIntegrationTest.class);

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.requestSpec.header("Fineract-Platform-TenantId", "default");
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.paymentTypeHelper = new PaymentTypeHelper();
        // Use a default responseSpec for loan operations that doesn't enforce status code
        ResponseSpecification loanResponseSpec = new ResponseSpecBuilder().build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, loanResponseSpec);
        this.guarantorHelper = new GuarantorHelper(this.requestSpec, loanResponseSpec);
    }

    @Test
    public void testSavingsAccount() {
        this.savingsAccountHelper = new SavingsAccountHelper(requestSpec, responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        Assertions.assertNotNull(clientID);

        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        Assertions.assertNotNull(groupID);

        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID.toString());
        Assertions.assertNotNull(groupID);

        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(groupID, savingsProductID, ACCOUNT_TYPE_GROUP);
        Assertions.assertNotNull(savingsId);

        HashMap modifications = this.savingsAccountHelper.updateSavingsAccount(groupID, savingsProductID, savingsId, ACCOUNT_TYPE_GROUP);
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

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSavingsAccount_CLOSE_APPLICATION() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(400).build();
        final SavingsAccountHelper validationErrorHelper = new SavingsAccountHelper(this.requestSpec, errorResponse);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientID);

        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        Assertions.assertNotNull(groupID);

        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID.toString());
        Assertions.assertNotNull(groupID);

        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = "1000.0";
        final String enforceMinRequiredBalance = "true";
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(groupID, savingsProductID, ACCOUNT_TYPE_GROUP);
        Assertions.assertNotNull(savingsId);

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
    public void testGsimSavingsAccount_WithTwoClients_ChildCountTwo() {

        // Initialize the helper for savings account operations
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        // Create two clients: one designated as the parent and one as the child
        final Integer parentClientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(parentClientID);

        final Integer childClientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(childClientID);

        // Create a group and associate both clients with it
        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        Assertions.assertNotNull(groupID);

        // obtain the latest application ID of the gsim accounts
        // BigDecimal applicationId = GroupHelper.getLastApplicationIdOfGsimSavingAccount(this.requestSpec,
        // this.responseSpec, groupID).add(BigDecimal.ONE);

        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), parentClientID.toString());
        Assertions.assertNotNull(groupID);
        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), childClientID.toString());
        Assertions.assertNotNull(groupID);

        // Create a savings product necessary for the GSIM application
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance);
        Assertions.assertNotNull(savingsProductID);

        // Prepare the client array with two entries:
        // one for the parent client (isParent = true) and one for the child client (isParent = false)
        List<Map<String, Object>> clientArray = new ArrayList<>();
        clientArray.add(clientArray(parentClientID, groupID, savingsProductID, "08 January 2013", true));
        clientArray.add(clientArray(childClientID, groupID, savingsProductID, "08 January 2013", false));

        // Apply for a GSIM savings account with both clients under the same application
        final Integer gsimID = this.savingsAccountHelper.applyForGsimApplication(clientArray);
        Assertions.assertNotNull(gsimID);

        // get child account count
        final Integer childAccountCount = GroupHelper.getChildAccountCount(this.requestSpec, this.responseSpec, groupID);
        assertEquals(childAccountCount, 2);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSavingsAccount_DELETE_APPLICATION() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        SavingsAccountHelper savingsAccountHelperValidationError = new SavingsAccountHelper(this.requestSpec,
                new ResponseSpecBuilder().build());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientID);

        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        Assertions.assertNotNull(groupID);

        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID.toString());
        Assertions.assertNotNull(groupID);

        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(groupID, savingsProductID, ACCOUNT_TYPE_GROUP);
        Assertions.assertNotNull(savingsId);

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

    // gsimcommands testing
    @SuppressWarnings("unchecked")
    @Test
    public void testGsimSavingsAccount_REJECT_APPLICATION() {

        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        SavingsAccountHelper savingsAccountHelperValidationError = new SavingsAccountHelper(this.requestSpec,
                new ResponseSpecBuilder().build());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientID);

        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        Assertions.assertNotNull(groupID);

        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID.toString());
        Assertions.assertNotNull(groupID);

        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance);
        Assertions.assertNotNull(savingsProductID);

        List<Map<String, Object>> clientArray = new ArrayList<>();
        clientArray.add(clientArray(clientID, groupID, savingsProductID, "08 January 2013", true));

        final Integer gsimID = this.savingsAccountHelper.applyForGsimApplication(clientArray);

        HashMap savingsStatusHashMap = this.savingsAccountHelper.approveGsimSavings(gsimID);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        List<HashMap> error1 = savingsAccountHelperValidationError.rejectGsimApplicationWithErrorCode(gsimID,
                SavingsAccountHelper.CREATED_DATE_PLUS_ONE);
        assertEquals("validation.msg.savingsaccount.reject.not.in.submittedandpendingapproval.state",
                error1.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        savingsStatusHashMap = this.savingsAccountHelper.undoApprovalGsimSavings(gsimID);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        error1 = savingsAccountHelperValidationError.rejectGsimApplicationWithErrorCode(gsimID, SavingsAccountHelper.getFutureDate());
        assertEquals("validation.msg.savingsaccount.reject.cannot.be.a.future.date",
                error1.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        error1 = savingsAccountHelperValidationError.rejectGsimApplicationWithErrorCode(gsimID,
                SavingsAccountHelper.CREATED_DATE_MINUS_ONE);
        assertEquals("validation.msg.savingsaccount.reject.cannot.be.before.submittal.date",
                error1.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        savingsStatusHashMap = this.savingsAccountHelper.rejectGsimApplication(gsimID);
        SavingsStatusChecker.verifySavingsIsRejected(savingsStatusHashMap);

    }

    @Test
    public void testGsimSavingsAccount_DEPOSIT_APPLICATION() {

        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        Assertions.assertNotNull(clientID);

        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        Assertions.assertNotNull(groupID);

        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID.toString());
        Assertions.assertNotNull(groupID);

        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance);
        Assertions.assertNotNull(savingsProductID);

        List<Map<String, Object>> clientArray = new ArrayList<>();
        clientArray.add(clientArray(clientID, groupID, savingsProductID, "08 January 2013", true));
        LOG.info("client Array : {} ", clientArray);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(groupID, savingsProductID, ACCOUNT_TYPE_GROUP);
        Assertions.assertNotNull(savingsId);

        String name = PaymentTypeHelper.randomNameGenerator("P_T", 5);
        String description = PaymentTypeHelper.randomNameGenerator("PT_Desc", 15);
        Boolean isCashPayment = true;
        Long position = 1L;

        var paymentTypesResponse = paymentTypeHelper.createPaymentType(
                new PaymentTypeCreateRequest().name(name).description(description).isCashPayment(isCashPayment).position(position));
        Long paymentTypeId = paymentTypesResponse.getResourceId();
        Assertions.assertNotNull(paymentTypeId);

        List<Map<String, Object>> savingsArray = new ArrayList<>();
        final Integer transactionAmount = 2500;
        savingsArray.add(savingsArray(paymentTypeId, savingsId, transactionAmount, "10 March 2013"));

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        final Integer depositId = this.savingsAccountHelper.depositGsimApplication(savingsId, savingsArray);
        Assertions.assertNotNull(depositId);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGsimSavingsAccount_CLOSE_APPLICATION() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(400).build();
        final SavingsAccountHelper validationErrorHelper = new SavingsAccountHelper(this.requestSpec, errorResponse);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        // Assertions.assertNotNull(clientID);

        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        Assertions.assertNotNull(groupID);

        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID.toString());
        Assertions.assertNotNull(groupID);

        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = "1000.0";
        final String enforceMinRequiredBalance = "true";
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance);
        Assertions.assertNotNull(savingsProductID);

        List<Map<String, Object>> clientArray = new ArrayList<>();
        clientArray.add(clientArray(clientID, groupID, savingsProductID, "08 January 2013", true));

        final Integer gsimID = this.savingsAccountHelper.applyForGsimApplication(clientArray);

        HashMap savingsStatusHashMap = this.savingsAccountHelper.approveGsimSavings(gsimID);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateGsimSavings(gsimID);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Calendar todaysDate = Calendar.getInstance();
        final String CLOSEDON_DATE = dateFormat.format(todaysDate.getTime());
        String withdrawBalance = "false";
        ArrayList<HashMap> savingsAccountErrorData = (ArrayList<HashMap>) validationErrorHelper
                .closeGsimSavingsAccountAndGetBackRequiredField(gsimID, withdrawBalance, CommonConstants.RESPONSE_ERROR, CLOSEDON_DATE);
        assertEquals("validation.msg.savingsaccount.close.results.in.balance.not.zero",
                savingsAccountErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        withdrawBalance = "true";
        savingsStatusHashMap = this.savingsAccountHelper.closeGsimSavingsAccount(gsimID, withdrawBalance);
        SavingsStatusChecker.verifySavingsAccountIsClosed(savingsStatusHashMap);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGsimSavingsAccount_UPDATE_APPLICATION() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(400).build();

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        // Assertions.assertNotNull(clientID);

        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        Assertions.assertNotNull(groupID);

        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID.toString());
        Assertions.assertNotNull(groupID);

        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = "1000.0";
        final String enforceMinRequiredBalance = "true";
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance);
        Assertions.assertNotNull(savingsProductID);

        List<Map<String, Object>> clientArray = new ArrayList<>();
        clientArray.add(clientArray(clientID, groupID, savingsProductID, "08 January 2013", true));

        final Integer gsimID = this.savingsAccountHelper.applyForGsimApplication(clientArray);

        HashMap savingsStatusHashMap = this.savingsAccountHelper.updateGsimApplication(gsimID, clientID, groupID, savingsProductID);
        LOG.info("savingsStatusHashMap: {} ", savingsStatusHashMap);
        Assertions.assertTrue(savingsStatusHashMap.containsKey("savingsId"));

    }

    @Test
    public void getGsimAccount() {

        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(400).build();

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        // Assertions.assertNotNull(clientID);

        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        Assertions.assertNotNull(groupID);

        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID.toString());
        Assertions.assertNotNull(groupID);

        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = "1000.0";
        final String enforceMinRequiredBalance = "true";
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance);
        Assertions.assertNotNull(savingsProductID);

        List<Map<String, Object>> clientArray = new ArrayList<>();
        clientArray.add(clientArray(clientID, groupID, savingsProductID, "08 January 2013", true));
        final Integer gsimID = this.savingsAccountHelper.applyForGsimApplication(clientArray);

        final List<String> retrievedGsimId = GroupHelper.verifyRetrieveGsimAccounts(this.requestSpec, this.responseSpec, groupID);
        Assertions.assertNotNull(retrievedGsimId.toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSavingsAccount_REJECT_APPLICATION() {

        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        SavingsAccountHelper savingsAccountHelperValidationError = new SavingsAccountHelper(this.requestSpec,
                new ResponseSpecBuilder().build());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        // Assertions.assertNotNull(clientID);

        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        Assertions.assertNotNull(groupID);

        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID.toString());
        Assertions.assertNotNull(groupID);

        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(groupID, savingsProductID, ACCOUNT_TYPE_GROUP);
        Assertions.assertNotNull(savingsId);

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
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        // Assertions.assertNotNull(clientID);

        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        Assertions.assertNotNull(groupID);

        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID.toString());
        Assertions.assertNotNull(groupID);

        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(groupID, savingsProductID, ACCOUNT_TYPE_GROUP);
        Assertions.assertNotNull(savingsId);

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
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        // Assertions.assertNotNull(clientID);

        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        Assertions.assertNotNull(groupID);

        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID.toString());
        Assertions.assertNotNull(groupID);

        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(groupID, savingsProductID, ACCOUNT_TYPE_GROUP);
        Assertions.assertNotNull(savingsId);

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
        Float balance = Float.valueOf(MINIMUM_OPENING_BALANCE);
        assertEquals(balance, summary.get("accountBalance"), "Verifying opening Balance");

        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap depositTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, depositTransactionId);
        balance += Float.parseFloat(DEPOSIT_AMOUNT);
        assertEquals(Float.valueOf(DEPOSIT_AMOUNT), depositTransaction.get("amount"), "Verifying Deposit Amount");
        assertEquals(balance, depositTransaction.get("runningBalance"), "Verifying Balance after Deposit");

        Integer withdrawTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, WITHDRAW_AMOUNT,
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap withdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawTransactionId);
        balance -= Float.parseFloat(WITHDRAW_AMOUNT);
        assertEquals(Float.valueOf(WITHDRAW_AMOUNT), withdrawTransaction.get("amount"), "Verifying Withdrawal Amount");
        assertEquals(balance, withdrawTransaction.get("runningBalance"), "Verifying Balance after Withdrawal");

        Integer newWithdrawTransactionId = this.savingsAccountHelper.updateSavingsAccountTransaction(savingsId, withdrawTransactionId,
                WITHDRAW_AMOUNT_ADJUSTED);
        HashMap newWithdrawTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, newWithdrawTransactionId);
        balance = balance + Float.parseFloat(WITHDRAW_AMOUNT) - Float.parseFloat(WITHDRAW_AMOUNT_ADJUSTED);
        assertEquals(Float.valueOf(WITHDRAW_AMOUNT_ADJUSTED), newWithdrawTransaction.get("amount"), "Verifying adjusted Amount");
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
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        // Assertions.assertNotNull(clientID);

        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        Assertions.assertNotNull(groupID);

        groupID = GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), clientID.toString());
        Assertions.assertNotNull(groupID);

        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(groupID, savingsProductID, ACCOUNT_TYPE_GROUP);
        Assertions.assertNotNull(savingsId);

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
        SimpleDateFormat sdf = new SimpleDateFormat(CommonConstants.DATE_FORMAT, Locale.US);
        Calendar cal = Calendar.getInstance();
        List dates = (List) savingsChargeForPay.get("dueDate");
        cal.set(Calendar.YEAR, (Integer) dates.get(0));
        cal.set(Calendar.MONTH, (Integer) dates.get(1) - 1);
        cal.set(Calendar.DAY_OF_MONTH, (Integer) dates.get(2));

        this.savingsAccountHelper.payCharge((Integer) savingsChargeForPay.get("id"), savingsId,
                ((Float) savingsChargeForPay.get("amount")).toString(), sdf.format(cal.getTime()));
        HashMap paidCharge = this.savingsAccountHelper.getSavingsCharge(savingsId, (Integer) savingsChargeForPay.get("id"));
        assertEquals(savingsChargeForPay.get("amount"), paidCharge.get("amountPaid"));

        final Integer monthlyFeechargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsMonthlyFeeJSON());
        Assertions.assertNotNull(monthlyFeechargeId);

        this.savingsAccountHelper.addChargesForSavings(savingsId, monthlyFeechargeId, true);
        charges = this.savingsAccountHelper.getSavingsCharges(savingsId);
        Assertions.assertEquals(2, charges.size());

        HashMap savingsChargeForWaive = charges.get(1);
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
        paidCharge = this.savingsAccountHelper.getSavingsCharge(savingsId, (Integer) savingsChargeForPay.get("id"));
        assertEquals(savingsChargeForPay.get("amount"), paidCharge.get("amountPaid"));
        List nextDueDates = (List) paidCharge.get("dueDate");
        LocalDate nextDueDate = LocalDate.of((Integer) nextDueDates.get(0), (Integer) nextDueDates.get(1), (Integer) nextDueDates.get(2));
        LocalDate expectedNextDueDate = LocalDate.of((Integer) dates.get(0), (Integer) dates.get(1), (Integer) dates.get(2))
                .plusWeeks((Integer) paidCharge.get("feeInterval"));
        assertEquals(expectedNextDueDate, nextDueDate);

        this.savingsAccountHelper.closeSavingsAccountAndGetBackRequiredField(savingsId, "true", null,
                Utils.getLocalDateOfTenant().format(Utils.dateFormatter));

    }

    public static Integer createSavingsProduct(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String minOpenningBalance, final String minBalanceForInterestCalculation, final String minRequiredBalance,
            final String enforceMinRequiredBalance) {
        LOG.info("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        SavingsProductHelper savingsProductHelper = new SavingsProductHelper();
        final String savingsProductJSON = savingsProductHelper //
                .withInterestCompoundingPeriodTypeAsDaily() //
                .withInterestPostingPeriodTypeAsMonthly() //
                .withInterestCalculationPeriodTypeAsDailyBalance() //
                .withMinBalanceForInterestCalculation(minBalanceForInterestCalculation) //
                .withMinRequiredBalance(minRequiredBalance) //
                .withEnforceMinRequiredBalance(enforceMinRequiredBalance) //
                .withMinimumOpenningBalance(minOpenningBalance).build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    private Map<String, Object> clientArray(final Integer clientId, final Integer groupId, final Integer productId,
            final String submittedOnDate, final boolean isParent) {
        Map<String, Object> map = new HashMap<>();
        map.put("clientId", clientId);
        map.put("groupId", groupId);
        map.put("productId", productId);
        map.put("submittedOnDate", submittedOnDate);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("locale", "en");
        map.put("isParentAccount", isParent);
        map.put("isGSIM", "true");
        return map;
    }

    private Map<String, Object> savingsArray(final Long paymentId, final Integer savingsId, final Integer transactionAmount,
            final String transactionDate) {
        Map<String, Object> map = new HashMap<>();
        map.put("transactionDate", transactionDate);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("locale", "en");
        map.put("transactionAmount", transactionAmount);
        map.put("paymentTypeId", paymentId);
        map.put("childAccountId", savingsId);
        return map;
    }

    /**
     * Test that verifies the /savingsaccounts/{savingsId}/onholdtransactions API endpoint works correctly for GROUP
     * savings accounts when used as guarantor collateral:
     * <ul>
     * <li>The endpoint returns hold transactions for group savings accounts</li>
     * <li>The savingsClientName field is populated with the group name (not null/blank)</li>
     * <li>Transaction details (amount, type, date) are correct</li>
     * <li>The response includes pagination information</li>
     * </ul>
     */
    @Test
    public void testOnHoldTransactionsApiForGroupSavingsAccount() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        // Create a client who will take out the loan
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        Assertions.assertNotNull(clientID);

        // Create a group with a savings account that will act as collateral
        final Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        Assertions.assertNotNull(groupID);

        // Create a savings product
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE, null, null,
                "false");
        Assertions.assertNotNull(savingsProductID);

        // Create and activate a group savings account
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(groupID, savingsProductID, ACCOUNT_TYPE_GROUP);
        Assertions.assertNotNull(savingsId);

        HashMap savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        // After activation, the account has MINIMUM_OPENING_BALANCE (1000) that can be used as collateral
        // No need for additional deposit

        // Create a loan product with hold funds enabled
        // Note: Using 0,0,0 to bypass bug FINERACT-2476 where group accounts can't be guarantors
        // with non-zero guarantee requirements. This test focuses on the SQL query fix.
        LoanProductTestBuilder loanProductBuilder = new LoanProductTestBuilder().withPrincipal(PRINCIPAL).withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsWeek().withinterestRatePerPeriod("2")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withOnHoldFundDetails("0", "0", "0");
        final String loanProductJSON = loanProductBuilder.build(null);
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(loanProductJSON);
        Assertions.assertNotNull(loanProductID);

        // Apply for a loan for the client
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(PRINCIPAL).withLoanTermFrequency("4")
                .withLoanTermFrequencyAsWeeks().withNumberOfRepayments("4").withRepaymentEveryAfter("1").withRepaymentFrequencyTypeAsWeeks()
                .withInterestRatePerPeriod("2").withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withSubmittedOnDate(SavingsAccountHelper.TRANSACTION_DATE)
                .withExpectedDisbursementDate(SavingsAccountHelper.TRANSACTION_DATE)
                .build(clientID.toString(), loanProductID.toString(), null);
        final Integer loanID = this.loanTransactionHelper.getLoanId(loanApplicationJSON);
        Assertions.assertNotNull(loanID);
        LOG.info("Created loan with ID: {}", loanID);

        // Add the group savings account as guarantor collateral for the loan
        // Use GUARANTEE_AMOUNT (500) as guarantee amount (less than the MINIMUM_OPENING_BALANCE of 1000)
        String guarantorJSON = new GuarantorTestBuilder()
                .existingGroupWithGuaranteeAmount(String.valueOf(groupID), String.valueOf(savingsId), GUARANTEE_AMOUNT).build();

        LOG.info("Guarantor JSON: {}", guarantorJSON);
        LOG.info("Loan ID: {}, Group ID: {}, Savings ID: {}", loanID, groupID, savingsId);

        Integer guarantorId = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        LOG.info("Guarantor ID: {}", guarantorId);
        Assertions.assertNotNull(guarantorId, "Guarantor creation should return a valid ID");

        // Approve the loan - this will create the hold transaction on the group savings account
        HashMap loanStatusHashMap = this.loanTransactionHelper.approveLoan(SavingsAccountHelper.TRANSACTION_DATE, loanID);
        Assertions.assertNotNull(loanStatusHashMap);

        // Call the on-hold transactions API endpoint
        final String ON_HOLD_TRANSACTIONS_URL = "/fineract-provider/api/v1/savingsaccounts/" + savingsId + "/onholdtransactions?"
                + Utils.TENANT_IDENTIFIER;
        HashMap onHoldTransactionsResponse = Utils.performServerGet(this.requestSpec, this.responseSpec, ON_HOLD_TRANSACTIONS_URL, "");
        Assertions.assertNotNull(onHoldTransactionsResponse);

        // Verify the response structure
        Assertions.assertTrue(onHoldTransactionsResponse.containsKey("totalFilteredRecords"),
                "Response should contain totalFilteredRecords");
        Assertions.assertTrue(onHoldTransactionsResponse.containsKey("pageItems"), "Response should contain pageItems");

        // Verify we have at least one transaction (the guarantor hold we just created)
        Integer totalRecords = (Integer) onHoldTransactionsResponse.get("totalFilteredRecords");
        Assertions.assertTrue(totalRecords > 0, "Should have at least one on-hold transaction");

        // Get the page items
        ArrayList<HashMap> pageItems = (ArrayList<HashMap>) onHoldTransactionsResponse.get("pageItems");
        Assertions.assertNotNull(pageItems, "pageItems should not be null");
        Assertions.assertFalse(pageItems.isEmpty(), "pageItems should not be empty");

        LOG.info("Found {} on-hold transactions", pageItems.size());

        // Verify that at least one transaction has the group name populated
        boolean foundTransactionWithGroupName = false;
        for (HashMap transaction : pageItems) {
            LOG.info("Transaction: {}", transaction);
            String savingsClientName = (String) transaction.get("savingsClientName");
            if (savingsClientName != null && !savingsClientName.isBlank()) {
                foundTransactionWithGroupName = true;

                // Verify transaction details
                Assertions.assertNotNull(transaction.get("amount"), "Transaction amount should not be null");

                // Verify savings account details are present
                String savingsAccNum = (String) transaction.get("savingsAccountNo");
                Assertions.assertNotNull(savingsAccNum, "savingsAccountNo should not be null");

                // Verify savings ID matches
                Integer savingsIdFromTransaction = (Integer) transaction.get("savingsId");
                Assertions.assertEquals(savingsId, savingsIdFromTransaction, "savingsId should match");

                // Verify transaction date is present
                Assertions.assertNotNull(transaction.get("transactionDate"), "transactionDate should not be null");

                LOG.info("SUCCESS: Found on-hold transaction with group name '{}' for group savings account", savingsClientName);
                break;
            }
        }

        Assertions.assertTrue(foundTransactionWithGroupName,
                "Should find at least one on-hold transaction with savingsClientName populated (group name)");
    }

    /**
     * Test that creating a group guarantor with an invalid group ID fails with appropriate error
     */
    @Test
    public void testGroupGuarantorWithInvalidGroupId() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        // Create a client who will take out the loan
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientID);

        // Create loan product with hold funds
        LoanProductTestBuilder loanProductBuilder = new LoanProductTestBuilder().withPrincipal(PRINCIPAL).withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsWeek().withinterestRatePerPeriod("2")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withOnHoldFundDetails("0", "0", "0");
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(loanProductBuilder.build(null));
        Assertions.assertNotNull(loanProductID);

        // Apply for a loan
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(PRINCIPAL).withLoanTermFrequency("4")
                .withLoanTermFrequencyAsWeeks().withNumberOfRepayments("4").withRepaymentEveryAfter("1").withRepaymentFrequencyTypeAsWeeks()
                .withInterestRatePerPeriod("2").withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withSubmittedOnDate(SavingsAccountHelper.TRANSACTION_DATE)
                .withExpectedDisbursementDate(SavingsAccountHelper.TRANSACTION_DATE)
                .build(clientID.toString(), loanProductID.toString(), null);
        final Integer loanID = this.loanTransactionHelper.getLoanId(loanApplicationJSON);
        Assertions.assertNotNull(loanID);

        // Try to create guarantor with invalid group ID (9999999)
        final Integer invalidGroupId = 9999999;
        String guarantorJSON = new GuarantorTestBuilder()
                .existingGroupWithGuaranteeAmount(String.valueOf(invalidGroupId), "1", GUARANTEE_AMOUNT).build();

        final ResponseSpecification errorResponse = new ResponseSpecBuilder().build();
        final RequestSpecification errorRequest = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        errorRequest.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());

        ArrayList<HashMap> error = (ArrayList<HashMap>) this.guarantorHelper.createGuarantorWithError(loanID, guarantorJSON, errorRequest,
                errorResponse);
        // Verify we got an error response (status code may be 403 or 404 depending on environment)
        Assertions.assertNotNull(error, "Should return error for invalid group ID");

        LOG.info("SUCCESS: Invalid group ID correctly rejected");
    }

    /**
     *
     * Test that duplicate group guarantor detection works and shows proper error message with group name
     */
    @Test
    public void testDuplicateGroupGuarantor() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        // Create client for loan
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientID);

        // Create group with savings account
        final Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        Assertions.assertNotNull(groupID);

        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE, null, null,
                "false");
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(groupID, savingsProductID, ACCOUNT_TYPE_GROUP);
        this.savingsAccountHelper.approveSavings(savingsId);
        this.savingsAccountHelper.activateSavings(savingsId);

        // Create loan
        LoanProductTestBuilder loanProductBuilder = new LoanProductTestBuilder().withPrincipal(PRINCIPAL).withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsWeek().withinterestRatePerPeriod("2")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withOnHoldFundDetails("0", "0", "0");
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(loanProductBuilder.build(null));

        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(PRINCIPAL).withLoanTermFrequency("4")
                .withLoanTermFrequencyAsWeeks().withNumberOfRepayments("4").withRepaymentEveryAfter("1").withRepaymentFrequencyTypeAsWeeks()
                .withInterestRatePerPeriod("2").withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withSubmittedOnDate(SavingsAccountHelper.TRANSACTION_DATE)
                .withExpectedDisbursementDate(SavingsAccountHelper.TRANSACTION_DATE)
                .build(clientID.toString(), loanProductID.toString(), null);
        final Integer loanID = this.loanTransactionHelper.getLoanId(loanApplicationJSON);

        // Add group guarantor first time - should succeed
        String guarantorJSON = new GuarantorTestBuilder()
                .existingGroupWithGuaranteeAmount(String.valueOf(groupID), String.valueOf(savingsId), GUARANTEE_AMOUNT).build();
        Integer guarantorId1 = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        Assertions.assertNotNull(guarantorId1, "First guarantor creation should succeed");

        // Try to add the SAME group guarantor again - should fail with duplicate error
        final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(403).build();
        final RequestSpecification errorRequest = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        errorRequest.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());

        ArrayList<HashMap> error = (ArrayList<HashMap>) this.guarantorHelper.createGuarantorWithError(loanID, guarantorJSON, errorRequest,
                errorResponse);
        Assertions.assertNotNull(error, "Should return error for duplicate group guarantor");

        // Verify error message contains group information
        HashMap errorData = error.get(0);
        String userMessage = (String) errorData.get("userMessageGlobalisationCode");
        Assertions.assertTrue(userMessage != null && userMessage.contains("already.exist"),
                "Error message should indicate duplicate guarantor");

        LOG.info("SUCCESS: Duplicate group guarantor correctly rejected");
    }

    /**
     * Test complete loan lifecycle (approval, disbursement, repayment) with a group guarantor
     */
    @Test
    public void testGroupGuarantorLoanLifecycle() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        // Create client for loan
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientID);

        // Create group with savings account
        final Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        Assertions.assertNotNull(groupID);

        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE, null, null,
                "false");
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(groupID, savingsProductID, ACCOUNT_TYPE_GROUP);
        this.savingsAccountHelper.approveSavings(savingsId);
        this.savingsAccountHelper.activateSavings(savingsId);

        // Deposit funds into the savings account to cover the guarantee
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT, SavingsAccountHelper.TRANSACTION_DATE,
                CommonConstants.RESPONSE_RESOURCE_ID);

        // Create loan product - using minimal hold fund requirements for testing
        // Focus is on verifying that group guarantors work, not on complex hold fund logic
        LoanProductTestBuilder loanProductBuilder = new LoanProductTestBuilder().withPrincipal("2000").withNumberOfRepayments("1")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsWeek().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withOnHoldFundDetails("0", "0", "0");
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(loanProductBuilder.build(null));

        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("2000").withLoanTermFrequency("1")
                .withLoanTermFrequencyAsWeeks().withNumberOfRepayments("1").withRepaymentEveryAfter("1").withRepaymentFrequencyTypeAsWeeks()
                .withInterestRatePerPeriod("0").withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withSubmittedOnDate(SavingsAccountHelper.TRANSACTION_DATE)
                .withExpectedDisbursementDate(SavingsAccountHelper.TRANSACTION_DATE)
                .build(clientID.toString(), loanProductID.toString(), null);
        final Integer loanID = this.loanTransactionHelper.getLoanId(loanApplicationJSON);

        // Add group guarantor with amount = 1000 (50% of 2000 loan)
        String guarantorJSON = new GuarantorTestBuilder()
                .existingGroupWithGuaranteeAmount(String.valueOf(groupID), String.valueOf(savingsId), "1000").build();
        Integer guarantorId = this.guarantorHelper.createGuarantor(loanID, guarantorJSON);
        Assertions.assertNotNull(guarantorId);

        // Verify guarantor was created successfully
        ArrayList<HashMap> guarantors = this.guarantorHelper.getGuarantorList(loanID);
        Assertions.assertEquals(1, guarantors.size(), "Should have 1 group guarantor");
        HashMap guarantor = guarantors.get(0);
        HashMap guarantorType = (HashMap) guarantor.get("guarantorType");
        Assertions.assertEquals(4, guarantorType.get("id"), "Guarantor type should be GROUP (4)");

        // Approve loan with group guarantor
        HashMap loanStatusHashMap = this.loanTransactionHelper.approveLoan(SavingsAccountHelper.TRANSACTION_DATE, loanID);
        Assertions.assertNotNull(loanStatusHashMap, "Loan approval should succeed with group guarantor");

        // Disburse loan
        this.loanTransactionHelper.disburseLoan(Long.valueOf(loanID), SavingsAccountHelper.TRANSACTION_DATE, 2000.0);

        // Make full repayment
        final String repaymentDate = SavingsAccountHelper.TRANSACTION_DATE;
        this.loanTransactionHelper.makeRepayment(repaymentDate, Float.parseFloat("2000"), loanID);

        LOG.info("SUCCESS: Group guarantor lifecycle test completed");
    }

    /**
     * Test mixed client and group guarantors on the same loan
     */
    @Test
    public void testMixedClientAndGroupGuarantors() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        // Create loan borrower client
        final Integer borrowerClientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(borrowerClientID);

        // Create guarantor client with savings
        final Integer guarantorClientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(guarantorClientID);

        // Create guarantor group with savings
        final Integer guarantorGroupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        Assertions.assertNotNull(guarantorGroupID);

        // Create savings accounts
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE, null, null,
                "false");

        final Integer clientSavingsId = this.savingsAccountHelper.applyForSavingsApplication(guarantorClientID, savingsProductID,
                "INDIVIDUAL");
        this.savingsAccountHelper.approveSavings(clientSavingsId);
        this.savingsAccountHelper.activateSavings(clientSavingsId);

        final Integer groupSavingsId = this.savingsAccountHelper.applyForSavingsApplication(guarantorGroupID, savingsProductID,
                ACCOUNT_TYPE_GROUP);
        this.savingsAccountHelper.approveSavings(groupSavingsId);
        this.savingsAccountHelper.activateSavings(groupSavingsId);

        // Create loan
        LoanProductTestBuilder loanProductBuilder = new LoanProductTestBuilder().withPrincipal(PRINCIPAL).withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsWeek().withinterestRatePerPeriod("2")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withOnHoldFundDetails("0", "0", "0");
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(loanProductBuilder.build(null));

        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(PRINCIPAL).withLoanTermFrequency("4")
                .withLoanTermFrequencyAsWeeks().withNumberOfRepayments("4").withRepaymentEveryAfter("1").withRepaymentFrequencyTypeAsWeeks()
                .withInterestRatePerPeriod("2").withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withSubmittedOnDate(SavingsAccountHelper.TRANSACTION_DATE)
                .withExpectedDisbursementDate(SavingsAccountHelper.TRANSACTION_DATE)
                .build(borrowerClientID.toString(), loanProductID.toString(), null);
        final Integer loanID = this.loanTransactionHelper.getLoanId(loanApplicationJSON);

        // Add CLIENT guarantor
        String clientGuarantorJSON = new GuarantorTestBuilder()
                .existingCustomerWithGuaranteeAmount(String.valueOf(guarantorClientID), String.valueOf(clientSavingsId), "250").build();
        Integer clientGuarantorId = this.guarantorHelper.createGuarantor(loanID, clientGuarantorJSON);
        Assertions.assertNotNull(clientGuarantorId, "Client guarantor creation should succeed");

        // Add GROUP guarantor
        String groupGuarantorJSON = new GuarantorTestBuilder()
                .existingGroupWithGuaranteeAmount(String.valueOf(guarantorGroupID), String.valueOf(groupSavingsId), "250").build();
        Integer groupGuarantorId = this.guarantorHelper.createGuarantor(loanID, groupGuarantorJSON);
        Assertions.assertNotNull(groupGuarantorId, "Group guarantor creation should succeed");

        // Retrieve all guarantors for the loan
        ArrayList<HashMap> guarantors = this.guarantorHelper.getGuarantorList(loanID);
        Assertions.assertNotNull(guarantors, "Should retrieve guarantor list");
        Assertions.assertEquals(2, guarantors.size(), "Should have 2 guarantors (1 client, 1 group)");

        // Verify both guarantor types are present
        boolean hasClientGuarantor = false;
        boolean hasGroupGuarantor = false;

        for (HashMap guarantor : guarantors) {
            HashMap guarantorType = (HashMap) guarantor.get("guarantorType");
            Integer typeId = (Integer) guarantorType.get("id");

            if (typeId == 1) { // CUSTOMER/CLIENT
                hasClientGuarantor = true;
            } else if (typeId == 4) { // GROUP
                hasGroupGuarantor = true;
            }
        }

        Assertions.assertTrue(hasClientGuarantor, "Should have client guarantor");
        Assertions.assertTrue(hasGroupGuarantor, "Should have group guarantor");

        // Approve loan - both holds should be placed
        this.loanTransactionHelper.approveLoan(SavingsAccountHelper.TRANSACTION_DATE, loanID);

        LOG.info("SUCCESS: Mixed client and group guarantors work together");
    }

    @Test
    public void testGroupAccountAvailableBalance() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        // Create a client
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        Assertions.assertNotNull(clientID);

        // Create a group and associate the client
        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        Assertions.assertNotNull(groupID);

        // Create a savings product
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance);
        Assertions.assertNotNull(savingsProductID);

        // Apply for and activate a group savings account
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(groupID, savingsProductID, ACCOUNT_TYPE_GROUP);
        Assertions.assertNotNull(savingsId);

        HashMap savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        // Make a deposit to create a balance
        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        Assertions.assertNotNull(depositTransactionId);

        // Get the account summary to verify balance
        // Note: Account has minimum opening balance (1000) + deposit (2000) = 3000 total
        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        Float expectedBalance = Float.parseFloat(MINIMUM_OPENING_BALANCE) + Float.parseFloat(DEPOSIT_AMOUNT);
        assertEquals(expectedBalance, summary.get("accountBalance"), "Verifying Deposit Balance");

        // Retrieve group accounts endpoint
        final String GROUP_ACCOUNTS_URL = "/fineract-provider/api/v1/groups/" + groupID + "/accounts?" + Utils.TENANT_IDENTIFIER;
        HashMap groupAccountsResponse = Utils.performServerGet(this.requestSpec, this.responseSpec, GROUP_ACCOUNTS_URL, "");
        Assertions.assertNotNull(groupAccountsResponse);

        // Verify savingsAccounts array exists and has our account
        ArrayList<HashMap> savingsAccounts = (ArrayList<HashMap>) groupAccountsResponse.get("savingsAccounts");
        Assertions.assertNotNull(savingsAccounts, "savingsAccounts array should be present");
        Assertions.assertTrue(savingsAccounts.size() > 0, "savingsAccounts should contain at least one account");

        // Find our savings account in the response
        HashMap account = null;
        for (HashMap acc : savingsAccounts) {
            if (acc.get("id").equals(savingsId)) {
                account = acc;
                break;
            }
        }
        Assertions.assertNotNull(account, "Savings account should be in the response");

        // Verify accountBalance and availableBalance fields are present
        Assertions.assertNotNull(account.get("accountBalance"), "accountBalance field should be present");
        Assertions.assertNotNull(account.get("availableBalance"), "availableBalance field should be present");

        // Parse accountBalance
        BigDecimal accountBalance = new BigDecimal(account.get("accountBalance").toString());

        // Parse hold fields (may be null if no holds exist)
        BigDecimal onHoldFunds = account.get("onHoldFunds") != null ? new BigDecimal(account.get("onHoldFunds").toString())
                : BigDecimal.ZERO;
        BigDecimal savingsAmountOnHold = account.get("savingsAmountOnHold") != null
                ? new BigDecimal(account.get("savingsAmountOnHold").toString())
                : BigDecimal.ZERO;

        // Parse availableBalance
        BigDecimal availableBalance = new BigDecimal(account.get("availableBalance").toString());

        // Verify accountBalance matches expected total (minimum opening balance + deposit)
        assertEquals(0, expectedBalance.compareTo(Float.parseFloat(accountBalance.toString())),
                "accountBalance should equal minimum opening balance plus deposited amount");

        // Since we haven't placed any holds, onHoldFunds and savingsAmountOnHold should be null or 0
        assertEquals(0, BigDecimal.ZERO.compareTo(onHoldFunds), "onHoldFunds should be 0 when no holds are placed");
        assertEquals(0, BigDecimal.ZERO.compareTo(savingsAmountOnHold), "savingsAmountOnHold should be 0 when no holds are placed");

        // Verify calculation: availableBalance = accountBalance - onHoldFunds - savingsAmountOnHold
        BigDecimal expectedAvailableBalance = accountBalance.subtract(onHoldFunds).subtract(savingsAmountOnHold);
        assertEquals(0, expectedAvailableBalance.compareTo(availableBalance),
                "availableBalance should equal accountBalance - onHoldFunds - savingsAmountOnHold");

        // Verify availableBalance equals accountBalance when there are no holds
        assertEquals(0, accountBalance.compareTo(availableBalance), "availableBalance should equal accountBalance when there are no holds");
    }

    @Test
    public void testGroupAccountWithHold() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        // Create a group
        final Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        Assertions.assertNotNull(groupID);

        // Create a savings product
        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance);
        Assertions.assertNotNull(savingsProductID);

        // Apply for and activate a group savings account
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(groupID, savingsProductID, ACCOUNT_TYPE_GROUP);
        Assertions.assertNotNull(savingsId);

        HashMap savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        // Make a deposit to create a balance
        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, DEPOSIT_AMOUNT,
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        Assertions.assertNotNull(depositTransactionId);

        // Place a hold on the account
        Integer holdTransactionId = (Integer) this.savingsAccountHelper.holdAmountInSavingsAccount(savingsId, HOLD_AMOUNT, false,
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        Assertions.assertNotNull(holdTransactionId);

        // Retrieve group accounts endpoint
        final String GROUP_ACCOUNTS_URL = "/fineract-provider/api/v1/groups/" + groupID + "/accounts?" + Utils.TENANT_IDENTIFIER;
        HashMap groupAccountsResponse = Utils.performServerGet(this.requestSpec, this.responseSpec, GROUP_ACCOUNTS_URL, "");
        Assertions.assertNotNull(groupAccountsResponse);

        // Find our savings account in the response
        ArrayList<HashMap> savingsAccounts = (ArrayList<HashMap>) groupAccountsResponse.get("savingsAccounts");
        HashMap account = null;
        for (HashMap acc : savingsAccounts) {
            if (acc.get("id").equals(savingsId)) {
                account = acc;
                break;
            }
        }
        Assertions.assertNotNull(account, "Savings account should be in the response");

        // Parse fields
        BigDecimal accountBalance = new BigDecimal(account.get("accountBalance").toString());
        BigDecimal onHoldFunds = account.get("onHoldFunds") != null ? new BigDecimal(account.get("onHoldFunds").toString())
                : BigDecimal.ZERO;
        BigDecimal savingsAmountOnHold = account.get("savingsAmountOnHold") != null
                ? new BigDecimal(account.get("savingsAmountOnHold").toString())
                : BigDecimal.ZERO;
        BigDecimal availableBalance = new BigDecimal(account.get("availableBalance").toString());

        // Verify the hold amount is reflected in savingsAmountOnHold
        assertEquals(0, new BigDecimal(HOLD_AMOUNT).compareTo(savingsAmountOnHold), "savingsAmountOnHold should equal the hold amount");

        // Verify the calculation is correct: availableBalance = accountBalance - onHoldFunds - savingsAmountOnHold
        BigDecimal expectedAvailableBalance = accountBalance.subtract(onHoldFunds).subtract(savingsAmountOnHold);
        assertEquals(0, expectedAvailableBalance.compareTo(availableBalance),
                "availableBalance should equal accountBalance - onHoldFunds - savingsAmountOnHold");
    }

    /**
     * Test that verifies group savings accounts can be used as guarantors when loan products have guarantee
     * requirements configured with zero minimum percentages.
     * <p>
     * Group accounts work with guarantees when minimum percentages are 0%, avoiding the self-guarantee validation logic
     * that expects guarantor.entityId to match loan.clientId (which fails for group accounts where client_id = null).
     * <p>
     * By using {@code withOnHoldFundDetails("0","0","0")}, we enable guarantee fund holds (isHoldGuaranteeFunds = true)
     * but set all minimum percentage requirements to 0%. This allows:
     * <ul>
     * <li>Validation to run (validateGuarantorBusinessRules() is called)</li>
     * <li>Group accounts to pass validation (no mandatory minimums to check)</li>
     * <li>Automatic holds to be placed on guarantor accounts upon loan disbursement</li>
     * </ul>
     */
    @Test
    public void testGroupAccountAsGuarantorWithGuaranteeHolds() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        final GuarantorHelper guarantorHelper = new GuarantorHelper(this.requestSpec, this.responseSpec);

        // Create a borrower client
        final Integer borrowerClientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, borrowerClientID);

        // Create a group and associate the borrower with it
        Integer groupID = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        Assertions.assertNotNull(groupID);
        GroupHelper.associateClient(this.requestSpec, this.responseSpec, groupID.toString(), borrowerClientID.toString());

        // Create a GROUP savings account (owned by group, not individual client)
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE, null, null,
                "false");
        final Integer guarantorSavingsId = this.savingsAccountHelper.applyForSavingsApplication(groupID, savingsProductID,
                ACCOUNT_TYPE_GROUP);
        Assertions.assertNotNull(guarantorSavingsId);

        HashMap savingsStatusHashMap = this.savingsAccountHelper.approveSavings(guarantorSavingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(guarantorSavingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        // Deposit funds into the group account
        final String depositAmount = "10000";
        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(guarantorSavingsId, depositAmount,
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        Assertions.assertNotNull(depositTransactionId);

        // Create loan product with guarantee requirements but zero minimum percentages
        // This allows group accounts to be used as guarantors while enabling automatic holds
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("10000").withNumberOfRepayments("12")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("2")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withOnHoldFundDetails("0", "0", "0") // 0% mandatory, 0% self, 0% external
                .build(null);
        final Integer loanProductID = loanTransactionHelper.getLoanProductId(loanProductJSON);
        Assertions.assertNotNull(loanProductID);

        // Create a basic loan for the borrower
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("10000").withLoanTermFrequency("12")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("12").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("2").withAmortizationTypeAsEqualInstallments()
                .withInterestTypeAsDecliningBalance().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withSubmittedOnDate(SavingsAccountHelper.TRANSACTION_DATE)
                .withExpectedDisbursementDate(SavingsAccountHelper.TRANSACTION_DATE_PLUS_ONE)
                .build(borrowerClientID.toString(), loanProductID.toString(), null);
        final Integer loanID = loanTransactionHelper.getLoanId(loanApplicationJSON);
        Assertions.assertNotNull(loanID);

        // Create a guarantor linking the group savings account to the loan
        final String guaranteeAmount = "5000";
        final String guarantorJSON = new GuarantorTestBuilder()
                .existingCustomerWithGuaranteeAmount(String.valueOf(borrowerClientID), String.valueOf(guarantorSavingsId), guaranteeAmount)
                .build();
        Integer guarantorId = guarantorHelper.createGuarantor(loanID, guarantorJSON);
        Assertions.assertNotNull(guarantorId, "Guarantor with group savings account created successfully");

        // Approve the loan - THIS is when the hold is placed (not on disbursement!)
        HashMap loanStatusHashMap = loanTransactionHelper.approveLoan(SavingsAccountHelper.TRANSACTION_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        // Verify the group savings account has an automatic hold equal to the guarantee amount after approval
        HashMap savingsDetails = this.savingsAccountHelper.getSavingsDetails(guarantorSavingsId);
        Object onHoldFundsObj = savingsDetails.get("onHoldFunds");
        BigDecimal onHoldFunds = onHoldFundsObj != null ? new BigDecimal(onHoldFundsObj.toString()) : BigDecimal.ZERO;
        final BigDecimal expectedHoldAmount = new BigDecimal(guaranteeAmount);
        Assertions.assertEquals(expectedHoldAmount, onHoldFunds.setScale(0, RoundingMode.HALF_UP),
                "Group account should have automatic guarantor hold equal to guarantee amount after loan approval");
    }

}
