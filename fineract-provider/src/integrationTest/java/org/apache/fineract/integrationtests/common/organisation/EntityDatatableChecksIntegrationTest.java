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
package org.apache.fineract.integrationtests.common.organisation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GroupHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.system.DatatableHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

/**
 * Entity Datatable Checks Integration Test for checking Creation, Deletion and
 * Retrieval of Entity-Datatable Check
 */

public class EntityDatatableChecksIntegrationTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private EntityDatatableChecksHelper entityDatatableChecksHelper;
    private DatatableHelper datatableHelper;
    private SavingsAccountHelper savingsAccountHelper;
    private LoanTransactionHelper loanTransactionHelper;
    private LoanTransactionHelper validationErrorHelper;

    private static final String CLIENT_APP_TABLE_NAME = "m_client";
    private static final String GROUP_APP_TABLE_NAME = "m_group";
    private static final String SAVINGS_APP_TABLE_NAME = "m_savings_account";
    private static final String LOAN_APP_TABLE_NAME = "m_loan";

    public static final String MINIMUM_OPENING_BALANCE = "1000.0";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";

    public static final String DATE_TIME_FORMAT = "dd MMMM yyyy HH:mm";

    public EntityDatatableChecksIntegrationTest() {
        // TODO Auto-generated constructor stub
    }

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.entityDatatableChecksHelper = new EntityDatatableChecksHelper(this.requestSpec, this.responseSpec);
        this.datatableHelper = new DatatableHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void validateCreateDeleteEntityDatatableCheck() {
        // creating datatable
        String datatableName = this.datatableHelper.createDatatable(CLIENT_APP_TABLE_NAME, false);
        DatatableHelper.verifyDatatableCreatedOnServer(this.requestSpec, this.responseSpec, datatableName);

        // creating new entity datatable check
        Integer entityDatatableCheckId = this.entityDatatableChecksHelper.createEntityDatatableCheck(CLIENT_APP_TABLE_NAME, datatableName,
                100, null);
        assertNotNull("ERROR IN CREATING THE ENTITY DATATABLE CHECK", entityDatatableCheckId);

        // deleting entity datatable check
        entityDatatableCheckId = this.entityDatatableChecksHelper.deleteEntityDatatableCheck(entityDatatableCheckId);
        assertNotNull("ERROR IN DELETING THE ENTITY DATATABLE CHECK", entityDatatableCheckId);

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(datatableName);
        assertEquals("ERROR IN DELETING THE DATATABLE", datatableName, deletedDataTableName);
    }

    @Test
    public void validateRetriveEntityDatatableChecksList() {
        // retrieving entity datatable check
        String entityDatatableChecksList = this.entityDatatableChecksHelper.retrieveEntityDatatableCheck();
        assertNotNull("ERROR IN RETRIEVING THE ENTITY DATATABLE CHECKS", entityDatatableChecksList);
    }

    @Test
    public void validateCreateClientWithEntityDatatableCheck() {

        // creating datatable
        String registeredTableName = this.datatableHelper.createDatatable(CLIENT_APP_TABLE_NAME, false);
        DatatableHelper.verifyDatatableCreatedOnServer(this.requestSpec, this.responseSpec, registeredTableName);

        // creating new entity datatable check
        Integer entityDatatableCheckId = this.entityDatatableChecksHelper.createEntityDatatableCheck(CLIENT_APP_TABLE_NAME,
                registeredTableName, 100, null);
        assertNotNull("ERROR IN CREATING THE ENTITY DATATABLE CHECK", entityDatatableCheckId);

        // creating client with datatables
        final Integer clientID = ClientHelper.createClientPendingWithDatatable(requestSpec, responseSpec, registeredTableName);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // deleting entity datatable check
        entityDatatableCheckId = this.entityDatatableChecksHelper.deleteEntityDatatableCheck(entityDatatableCheckId);
        assertNotNull("ERROR IN DELETING THE ENTITY DATATABLE CHECK", entityDatatableCheckId);

        // deleting datatable entries
        Integer appTableId = this.datatableHelper.deleteDatatableEntries(registeredTableName, clientID, "clientId");
        assertEquals("ERROR IN DELETING THE DATATABLE ENTRIES", clientID, appTableId);

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(registeredTableName);
        assertEquals("ERROR IN DELETING THE DATATABLE", registeredTableName, deletedDataTableName);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void validateCreateClientWithEntityDatatableCheckWithFailure() {
        // building error response with status code 403
        final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(403).build();
        final ClientHelper validationErrorHelper = new ClientHelper(this.requestSpec, errorResponse);

        // creating datatable
        String registeredTableName = this.datatableHelper.createDatatable(CLIENT_APP_TABLE_NAME, false);
        DatatableHelper.verifyDatatableCreatedOnServer(this.requestSpec, this.responseSpec, registeredTableName);

        // creating new entity datatable check
        Integer entityDatatableCheckId = this.entityDatatableChecksHelper.createEntityDatatableCheck(CLIENT_APP_TABLE_NAME,
                registeredTableName, 100, null);
        assertNotNull("ERROR IN CREATING THE ENTITY DATATABLE CHECK", entityDatatableCheckId);

        // creating client with datatables with error
        ArrayList<HashMap<Object, Object>> clientErrorData = (ArrayList<HashMap<Object, Object>>) validationErrorHelper
                .createClientPendingWithError(CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.entry.required.in.datatable.[" + registeredTableName + "]",
                clientErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        // deleting entity datatable check
        entityDatatableCheckId = this.entityDatatableChecksHelper.deleteEntityDatatableCheck(entityDatatableCheckId);
        assertNotNull("ERROR IN DELETING THE ENTITY DATATABLE CHECK", entityDatatableCheckId);

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(registeredTableName);
        assertEquals("ERROR IN DELETING THE DATATABLE", registeredTableName, deletedDataTableName);
    }

    @Test
    public void validateCreateGroupWithEntityDatatableCheck() {

        // creating datatable
        String registeredTableName = this.datatableHelper.createDatatable(GROUP_APP_TABLE_NAME, false);
        DatatableHelper.verifyDatatableCreatedOnServer(this.requestSpec, this.responseSpec, registeredTableName);

        // creating new entity datatable check
        Integer entityDatatableCheckId = this.entityDatatableChecksHelper.createEntityDatatableCheck(GROUP_APP_TABLE_NAME,
                registeredTableName, 100, null);
        assertNotNull("ERROR IN CREATING THE ENTITY DATATABLE CHECK", entityDatatableCheckId);

        // creating group with datatables
        final Integer groupId = GroupHelper.createGroupPendingWithDatatable(this.requestSpec, this.responseSpec, registeredTableName);
        GroupHelper.verifyGroupCreatedOnServer(this.requestSpec, this.responseSpec, groupId);

        // deleting entity datatable check
        entityDatatableCheckId = this.entityDatatableChecksHelper.deleteEntityDatatableCheck(entityDatatableCheckId);
        assertNotNull("ERROR IN DELETING THE ENTITY DATATABLE CHECK", entityDatatableCheckId);

        // deleting datatable entries
        Integer appTableId = this.datatableHelper.deleteDatatableEntries(registeredTableName, groupId, "groupId");
        assertEquals("ERROR IN DELETING THE DATATABLE ENTRIES", groupId, appTableId);

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(registeredTableName);
        assertEquals("ERROR IN DELETING THE DATATABLE", registeredTableName, deletedDataTableName);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void validateCreateGroupWithEntityDatatableCheckWithFailure() {
        // building error response with status code 403
        final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(403).build();
        final GroupHelper validationErrorHelper = new GroupHelper(this.requestSpec, errorResponse);

        // creating datatable
        String registeredTableName = this.datatableHelper.createDatatable(GROUP_APP_TABLE_NAME, false);
        DatatableHelper.verifyDatatableCreatedOnServer(this.requestSpec, this.responseSpec, registeredTableName);

        // creating new entity datatable check
        Integer entityDatatableCheckId = this.entityDatatableChecksHelper.createEntityDatatableCheck(GROUP_APP_TABLE_NAME,
                registeredTableName, 100, null);
        assertNotNull("ERROR IN CREATING THE ENTITY DATATABLE CHECK", entityDatatableCheckId);

        // creating group with datatables with error
        ArrayList<HashMap<Object, Object>> groupErrorData = (ArrayList<HashMap<Object, Object>>) validationErrorHelper
                .createGroupWithError(CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.entry.required.in.datatable.[" + registeredTableName + "]",
                groupErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        // deleting entity datatable check
        entityDatatableCheckId = this.entityDatatableChecksHelper.deleteEntityDatatableCheck(entityDatatableCheckId);
        assertNotNull("ERROR IN DELETING THE ENTITY DATATABLE CHECK", entityDatatableCheckId);

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(registeredTableName);
        assertEquals("ERROR IN DELETING THE DATATABLE", registeredTableName, deletedDataTableName);
    }

    @Test
    public void validateCreateSavingsWithEntityDatatableCheck() {

        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;

        // creating datatable
        String registeredTableName = this.datatableHelper.createDatatable(SAVINGS_APP_TABLE_NAME, false);
        DatatableHelper.verifyDatatableCreatedOnServer(this.requestSpec, this.responseSpec, registeredTableName);

        // creating new entity datatable check
        Integer entityDatatableCheckId = this.entityDatatableChecksHelper.createEntityDatatableCheck(SAVINGS_APP_TABLE_NAME,
                registeredTableName, 100, null);
        assertNotNull("ERROR IN CREATING THE ENTITY DATATABLE CHECK", entityDatatableCheckId);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assert.assertNotNull(savingsProductID);

        // creating savings with datatables
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplicationWithDatatables(clientID, savingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL, "01 December 2016", registeredTableName);
        Assert.assertNotNull(savingsId);

        // deleting entity datatable check
        entityDatatableCheckId = this.entityDatatableChecksHelper.deleteEntityDatatableCheck(entityDatatableCheckId);
        assertNotNull("ERROR IN DELETING THE ENTITY DATATABLE CHECK", entityDatatableCheckId);

        // deleting datatable entries
        Integer appTableId = this.datatableHelper.deleteDatatableEntries(registeredTableName, savingsId, "savingsId");
        assertEquals("ERROR IN DELETING THE DATATABLE ENTRIES", savingsId, appTableId);

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(registeredTableName);
        assertEquals("ERROR IN DELETING THE DATATABLE", registeredTableName, deletedDataTableName);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void validateCreateSavingsWithEntityDatatableCheckWithFailure() {
        // building error response with status code 403
        final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(403).build();
        final SavingsAccountHelper validationErrorHelper = new SavingsAccountHelper(this.requestSpec, errorResponse);

        final String minBalanceForInterestCalculation = null;
        final String minRequiredBalance = null;
        final String enforceMinRequiredBalance = "false";
        final boolean allowOverdraft = false;

        // creating datatable
        String registeredTableName = this.datatableHelper.createDatatable(SAVINGS_APP_TABLE_NAME, false);
        DatatableHelper.verifyDatatableCreatedOnServer(this.requestSpec, this.responseSpec, registeredTableName);

        // creating new entity datatable check
        Integer entityDatatableCheckId = this.entityDatatableChecksHelper.createEntityDatatableCheck(SAVINGS_APP_TABLE_NAME,
                registeredTableName, 100, null);
        assertNotNull("ERROR IN CREATING THE ENTITY DATATABLE CHECK", entityDatatableCheckId);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                minBalanceForInterestCalculation, minRequiredBalance, enforceMinRequiredBalance, allowOverdraft);
        Assert.assertNotNull(savingsProductID);

        // creating savings with datatables with error
        ArrayList<HashMap<Object, Object>> groupErrorData = (ArrayList<HashMap<Object, Object>>) validationErrorHelper
                .applyForSavingsApplicationWithFailure(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL, "01 December 2016",
                        CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.entry.required.in.datatable.[" + registeredTableName + "]",
                groupErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        // deleting entity datatable check
        entityDatatableCheckId = this.entityDatatableChecksHelper.deleteEntityDatatableCheck(entityDatatableCheckId);
        assertNotNull("ERROR IN DELETING THE ENTITY DATATABLE CHECK", entityDatatableCheckId);

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(registeredTableName);
        assertEquals("ERROR IN DELETING THE DATATABLE", registeredTableName, deletedDataTableName);
    }

    @Test
    public void validateCreateLoanWithEntityDatatableCheck() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        // creating client
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // creating loan product
        final Integer loanProductID = createLoanProduct("100", "0", LoanProductTestBuilder.DEFAULT_STRATEGY);
        Assert.assertNotNull(loanProductID);

        // creating datatable
        String registeredTableName = this.datatableHelper.createDatatable(LOAN_APP_TABLE_NAME, false);
        DatatableHelper.verifyDatatableCreatedOnServer(this.requestSpec, this.responseSpec, registeredTableName);

        // creating new entity datatable check
        Integer entityDatatableCheckId = this.entityDatatableChecksHelper.createEntityDatatableCheck(LOAN_APP_TABLE_NAME,
                registeredTableName, 100, loanProductID);
        assertNotNull("ERROR IN CREATING THE ENTITY DATATABLE CHECK", entityDatatableCheckId);

        // creating new loan application
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, "5", registeredTableName);
        Assert.assertNotNull(loanID);

        // deleting entity datatable check
        entityDatatableCheckId = this.entityDatatableChecksHelper.deleteEntityDatatableCheck(entityDatatableCheckId);
        assertNotNull("ERROR IN DELETING THE ENTITY DATATABLE CHECK", entityDatatableCheckId);

        // deleting datatable entries
        Integer appTableId = this.datatableHelper.deleteDatatableEntries(registeredTableName, loanID, "loanId");
        assertEquals("ERROR IN DELETING THE DATATABLE ENTRIES", loanID, appTableId);

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(registeredTableName);
        assertEquals("ERROR IN DELETING THE DATATABLE", registeredTableName, deletedDataTableName);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void validateCreateLoanWithEntityDatatableCheckWithFailure() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        // building error response with status code 403
        final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(403).build();
        this.validationErrorHelper = new LoanTransactionHelper(this.requestSpec, errorResponse);

        // creating client
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // creating loan product
        final Integer loanProductID = createLoanProduct("100", "0", LoanProductTestBuilder.DEFAULT_STRATEGY);
        Assert.assertNotNull(loanProductID);

        // creating datatable
        String registeredTableName = this.datatableHelper.createDatatable(LOAN_APP_TABLE_NAME, false);
        DatatableHelper.verifyDatatableCreatedOnServer(this.requestSpec, this.responseSpec, registeredTableName);

        // creating new entity datatable check
        Integer entityDatatableCheckId = this.entityDatatableChecksHelper.createEntityDatatableCheck(LOAN_APP_TABLE_NAME,
                registeredTableName, 100, loanProductID);
        assertNotNull("ERROR IN CREATING THE ENTITY DATATABLE CHECK", entityDatatableCheckId);

        // creating new loan application with error
        ArrayList<HashMap<Object, Object>> loanErrorData = (ArrayList<HashMap<Object, Object>>) applyForLoanApplicationWithError(clientID,
                loanProductID, "5", CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.entry.required.in.datatable.[" + registeredTableName + "]",
                loanErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        // deleting entity datatable check
        entityDatatableCheckId = this.entityDatatableChecksHelper.deleteEntityDatatableCheck(entityDatatableCheckId);
        assertNotNull("ERROR IN DELETING THE ENTITY DATATABLE CHECK", entityDatatableCheckId);

        // deleting the datatable
        String deletedDataTableName = this.datatableHelper.deleteDatatable(registeredTableName);
        assertEquals("ERROR IN DELETING THE DATATABLE", registeredTableName, deletedDataTableName);
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

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, String graceOnPrincipalPayment,
            final String registeredTableName) {
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
                .withDatatables(getTestDatatableAsJson(registeredTableName)) //
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private Object applyForLoanApplicationWithError(final Integer clientID, final Integer loanProductID, String graceOnPrincipalPayment,
            final String responseAttribute) {
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
        return this.validationErrorHelper.getLoanError(loanApplicationJSON, responseAttribute);
    }

    public static List<HashMap<String, Object>> getTestDatatableAsJson(final String registeredTableName) {
        List<HashMap<String, Object>> datatablesListMap = new ArrayList<>();
        HashMap<String, Object> datatableMap = new HashMap<>();
        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("locale", "en");
        dataMap.put("Spouse Name", Utils.randomNameGenerator("Spouse_name", 4));
        dataMap.put("Number of Dependents", 5);
        dataMap.put("Time of Visit", "01 December 2016 04:03");
        dataMap.put("dateFormat", DATE_TIME_FORMAT);
        dataMap.put("Date of Approval", "02 December 2016 00:00");
        datatableMap.put("registeredTableName", registeredTableName);
        datatableMap.put("data", dataMap);
        datatablesListMap.add(datatableMap);
        return datatablesListMap;
    }
}
