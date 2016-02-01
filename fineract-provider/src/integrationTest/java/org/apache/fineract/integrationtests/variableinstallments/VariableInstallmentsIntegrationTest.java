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
package org.apache.fineract.integrationtests.variableinstallments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({"rawtypes", "unchecked"})
public class VariableInstallmentsIntegrationTest {

    
    private static final String NONE = "1";
    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private LoanTransactionHelper loanTransactionHelper;
    
    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
    }
    
    @Test
    public void testVariableLoanProductCreation() {
        final String json = VariableInstallmentsDecliningBalanceHelper.createLoanProductWithVaribleConfig(false, NONE);
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(json);
        System.out.println("------------------------------RETRIEVING CREATED LOAN PRODUCT DETAILS ---------------------------------------");
        Map loanProduct = (Map)loanTransactionHelper.getLoanProductDetail(requestSpec, responseSpec, loanProductID, "") ;
        Assert.assertTrue((Boolean)loanProduct.get("allowVariableInstallments")) ;
        Assert.assertEquals(new Integer(5), loanProduct.get("minimumGap")) ;
        Assert.assertEquals(new Integer(90), loanProduct.get("maximumGap")) ;
    }
    
   
    @Test
    public void testLoanProductCreation() {
        final String  josn = VariableInstallmentsDecliningBalanceHelper.createLoanProductWithoutVaribleConfig(false, NONE);
        Integer loanProductID = this.loanTransactionHelper.getLoanProductId(josn);
        System.out.println("------------------------------RETRIEVING CREATED LOAN PRODUCT DETAILS ---------------------------------------");
        Map loanProduct = (Map)loanTransactionHelper.getLoanProductDetail(requestSpec, responseSpec, loanProductID, "") ;
        Assert.assertTrue(!(Boolean)loanProduct.get("allowVariableInstallments")) ;
    }
    
    @Test
    public void testDeleteInstallmentsWithDecliningBalanceEqualInstallments() {
        VariableIntallmentsTransactionHelper transactionHelper = new VariableIntallmentsTransactionHelper(requestSpec, responseSpec) ;
        final String  loanProductJson = VariableInstallmentsDecliningBalanceHelper.createLoanProductWithVaribleConfig(false, NONE);
        Integer loanProductID = this.loanTransactionHelper.getLoanProductId(loanProductJson);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final String json = VariableInstallmentsDecliningBalanceHelper.applyForLoanApplication(clientID, loanProductID, null, null, "1,00,000.00");
        final Integer loanID = this.loanTransactionHelper.getLoanId(json);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
        //Integer loanID = 49 ;
        Map list = transactionHelper.retrieveSchedule(loanID) ;
        Map repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        ArrayList periods = (ArrayList)repaymentSchedule.get("periods") ;
        ArrayList toDelete = new ArrayList<>() ;
        toDelete.add(periods.get(1)) ;
        String toDeletedata = VariableInstallmentsDecliningBalanceHelper.createDeleteVariations(toDelete) ;
        HashMap modifiedReschdule = transactionHelper.validateVariations(toDeletedata, loanID) ;
        ArrayList newperiods = (ArrayList) modifiedReschdule.get("periods") ;
        ArrayList toVerifyData = VariableInstallmentsDecliningBalanceHelper.constructVerifyData(new String[] {"20 November 2011", "20 December 2011", "20 January 2012"}, 
                new String[] {"34675.47", "34675.47", "36756.26"}) ;
        assertAfterSubmit(newperiods, toVerifyData) ;
        transactionHelper.submitVariations(toDeletedata, loanID) ;
        list = transactionHelper.retrieveSchedule(loanID) ;
        repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        periods = (ArrayList)repaymentSchedule.get("periods") ;
        periods.remove(0) ; //Repayments Schedule includes disbursement also. So remove this.
        assertAfterSubmit(periods, toVerifyData) ;
        
    }
    private void assertAfterSubmit(ArrayList<Map> serverData, ArrayList<Map> expectedData) {
        Assert.assertTrue(serverData.size() == expectedData.size()) ;
        for(int i = 0 ; i < serverData.size(); i++) {
            Map<String, Object> serverMap = serverData.get(i) ;
            Map<String, Object> expectedMap = expectedData.get(i) ;
            Assert.assertTrue(VariableInstallmentsDecliningBalanceHelper.formatDate((ArrayList)serverMap.get("dueDate")).equals(expectedMap.get("dueDate"))) ;
            Assert.assertTrue(serverMap.get("totalOutstandingForPeriod").toString().equals(expectedMap.get("installmentAmount"))) ;
        }
    }
    
    @Test
    public void testAddInstallmentsWithDecliningBalanceEqualInstallments() {
        //31 October 2011 - 5000
        //Result: 20 October 2011 - 21,215.84, 31 October 2011 - 5000, 20 November 2011 26,477.31, 20 December 2011 26,477.31, 20 January 2012 25,947.7
        VariableIntallmentsTransactionHelper transactionHelper = new VariableIntallmentsTransactionHelper(requestSpec, responseSpec) ;
        final String  loanProductJson = VariableInstallmentsDecliningBalanceHelper.createLoanProductWithVaribleConfig(false, NONE);
        Integer loanProductID = this.loanTransactionHelper.getLoanProductId(loanProductJson);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final String json = VariableInstallmentsDecliningBalanceHelper.applyForLoanApplication(clientID, loanProductID, null, null, "1,00,000.00");
        final Integer loanID = this.loanTransactionHelper.getLoanId(json);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
       // Integer loanID = 57 ;
        Map list = transactionHelper.retrieveSchedule(loanID) ;
        Map repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        ArrayList periods = (ArrayList)repaymentSchedule.get("periods") ;
        String addVariationsjsondata = VariableInstallmentsDecliningBalanceHelper.createAddVariations() ;
        HashMap modifiedReschdule = transactionHelper.validateVariations(addVariationsjsondata, loanID) ;
        ArrayList newperiods = (ArrayList) modifiedReschdule.get("periods") ;
        ArrayList toVerifyData = VariableInstallmentsDecliningBalanceHelper.constructVerifyData(new String[] {"20 October 2011", "31 October 2011", "20 November 2011", "20 December 2011", "20 January 2012"}, 
                new String[] {"21215.84", "5000.0", "26477.31", "26477.31", "25947.7"}) ;
        assertAfterSubmit(newperiods, toVerifyData) ;
        transactionHelper.submitVariations(addVariationsjsondata, loanID) ;
        list = transactionHelper.retrieveSchedule(loanID) ;
        repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        periods = (ArrayList)repaymentSchedule.get("periods") ;
        periods.remove(0) ; //Repayments Schedule includes disbursement also. So remove this.
        assertAfterSubmit(periods, toVerifyData) ;
    }
    
    @Test
    public void testModifyInstallmentWithDecliningBalanceEqualInstallments() {
        //20 October 2011 - 30000 modify
        //Result 20 October 2011 - 30000.0, 20 November 2011 - 24,966.34, 20 December 2011 - 24,966.34, 20 January 2012 - 24,966.33
        VariableIntallmentsTransactionHelper transactionHelper = new VariableIntallmentsTransactionHelper(requestSpec, responseSpec) ;
        final String  loanProductJson = VariableInstallmentsDecliningBalanceHelper.createLoanProductWithVaribleConfig(false, NONE);
        Integer loanProductID = this.loanTransactionHelper.getLoanProductId(loanProductJson);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final String json = VariableInstallmentsDecliningBalanceHelper.applyForLoanApplication(clientID, loanProductID, null, null, "1,00,000.00");
        final Integer loanID = this.loanTransactionHelper.getLoanId(json);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
        //Integer loanID = 57 ;
        Map list = transactionHelper.retrieveSchedule(loanID) ;
        Map repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        ArrayList periods = (ArrayList)repaymentSchedule.get("periods") ;
        String addVariationsjsondata = VariableInstallmentsDecliningBalanceHelper.createModifiyVariations((Map)periods.get(1)) ; //0th position will have disbursement 
        HashMap modifiedReschdule = transactionHelper.validateVariations(addVariationsjsondata, loanID) ;
        ArrayList newperiods = (ArrayList) modifiedReschdule.get("periods") ;
        ArrayList toVerifyData = VariableInstallmentsDecliningBalanceHelper.constructVerifyData(new String[] {"20 October 2011", "20 November 2011", "20 December 2011", "20 January 2012"}, 
                new String[] {"30000.0", "24966.34", "24966.34", "24966.33"}) ;
        assertAfterSubmit(newperiods, toVerifyData) ;
        transactionHelper.submitVariations(addVariationsjsondata, loanID) ;
        list = transactionHelper.retrieveSchedule(loanID) ;
        repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        periods = (ArrayList)repaymentSchedule.get("periods") ;
        periods.remove(0) ; //Repayments Schedule includes disbursement also. So remove this.
        assertAfterSubmit(periods, toVerifyData) ;
        
    }
    
    @Test
    public void testAllVariationsDecliningBalancewithEqualInstallments() {
         // Request: Delete 20 December 2011 26,262.38, Modify 20 November 2011 from 26,262.38 to 30000, Add 25 December 2011 5000
         // Result: 20 October 2011 - 26262.38, 20 November 2011 - 30000, 25 December 2011 - 5000, 20 January 2012 - 44077  
        VariableIntallmentsTransactionHelper transactionHelper = new VariableIntallmentsTransactionHelper(requestSpec, responseSpec) ;
        final String  loanProductJson = VariableInstallmentsDecliningBalanceHelper.createLoanProductWithVaribleConfig(false, NONE);
        Integer loanProductID = this.loanTransactionHelper.getLoanProductId(loanProductJson);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final String json = VariableInstallmentsDecliningBalanceHelper.applyForLoanApplication(clientID, loanProductID, null, null, "1,00,000.00");
        final Integer loanID = this.loanTransactionHelper.getLoanId(json);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
        //Integer loanID = 57 ;
        Map list = transactionHelper.retrieveSchedule(loanID) ;
        Map repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        ArrayList periods = (ArrayList)repaymentSchedule.get("periods") ;
        
        String addVariationsjsondata = VariableInstallmentsDecliningBalanceHelper.createAllVariations() ; //0th position will have disbursement 
        HashMap modifiedReschdule = transactionHelper.validateVariations(addVariationsjsondata, loanID) ;
        ArrayList newperiods = (ArrayList) modifiedReschdule.get("periods") ;
        ArrayList toVerifyData = VariableInstallmentsDecliningBalanceHelper.constructVerifyData(new String[] {"20 October 2011", "20 November 2011", "25 December 2011", "20 January 2012"}, 
                new String[] {"26262.38", "30000.0", "5000.0", "44077.0"}) ;
        assertAfterSubmit(newperiods, toVerifyData) ;
        transactionHelper.submitVariations(addVariationsjsondata, loanID) ;
        list = transactionHelper.retrieveSchedule(loanID) ;
        repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        periods = (ArrayList)repaymentSchedule.get("periods") ;
        periods.remove(0) ; //Repayments Schedule includes disbursement also. So remove this.
        assertAfterSubmit(periods, toVerifyData) ;
    }
    
    @Test
    public void testAllVariationsDecliningBalancewithEqualPrincipal() {
         // Request: Delete 20 December 2011 26,262.38, Modify 20 November 2011 from 26,262.38 to 30000, Add 25 December 2011 5000
         // Result: 20 October 2011 - 27000.0, 20 November 2011 - 31500.0, 25 December 2011 - 6045.16, 20 January 2012 - 40670.97  
        VariableIntallmentsTransactionHelper transactionHelper = new VariableIntallmentsTransactionHelper(requestSpec, responseSpec) ;
        final String  loanProductJson = VariableInstallmentsDecliningBalanceHelper.createLoanProductWithVaribleConfigwithEqualPrincipal(false, NONE);
        Integer loanProductID = this.loanTransactionHelper.getLoanProductId(loanProductJson);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final String json = VariableInstallmentsDecliningBalanceHelper.applyForLoanApplicationWithEqualPrincipal(clientID, loanProductID, null, null, "1,00,000.00");
        final Integer loanID = this.loanTransactionHelper.getLoanId(json);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
       // Integer loanID = 109 ;
        Map list = transactionHelper.retrieveSchedule(loanID) ;
        Map repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        ArrayList periods = (ArrayList)repaymentSchedule.get("periods") ;
        
        String addVariationsjsondata = VariableInstallmentsDecliningBalanceHelper.createAllVariationsWithEqualPrincipal() ; //0th position will have disbursement 
        HashMap modifiedReschdule = transactionHelper.validateVariations(addVariationsjsondata, loanID) ;
        ArrayList newperiods = (ArrayList) modifiedReschdule.get("periods") ;
        ArrayList toVerifyData = VariableInstallmentsDecliningBalanceHelper.constructVerifyData(new String[] {"20 October 2011", "20 November 2011", "25 December 2011", "20 January 2012"}, 
                new String[] {"27000.0", "31500.0", "6045.16", "40670.97"}) ;
        assertAfterSubmit(newperiods, toVerifyData) ;
        transactionHelper.submitVariations(addVariationsjsondata, loanID) ;
        list = transactionHelper.retrieveSchedule(loanID) ;
        repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        periods = (ArrayList)repaymentSchedule.get("periods") ;
        periods.remove(0) ; //Repayments Schedule includes disbursement also. So remove this.
        assertAfterSubmit(periods, toVerifyData) ;
    }
    
    @Test
    public void testModifyDatesWithDecliningBalanceEqualInstallments() {
        //Modify 20 December 2011:25000 -> 04 January 2012:20000
        //Modify 20 January 2012 -> 08 February 2012
        //Result 20 October 2011 -26262.38, 20 November 2011 - 26262.38, 04 January 2012 -20000, 08 February 2012 - 33242.97
        VariableIntallmentsTransactionHelper transactionHelper = new VariableIntallmentsTransactionHelper(requestSpec, responseSpec) ;
        final String  loanProductJson = VariableInstallmentsDecliningBalanceHelper.createLoanProductWithVaribleConfig(false, NONE);
        Integer loanProductID = this.loanTransactionHelper.getLoanProductId(loanProductJson);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final String json = VariableInstallmentsDecliningBalanceHelper.applyForLoanApplication(clientID, loanProductID, null, null, "1,00,000.00");
        final Integer loanID = this.loanTransactionHelper.getLoanId(json);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
        //Integer loanID = 57 ;
        
        Map list = transactionHelper.retrieveSchedule(loanID) ;
        Map repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        ArrayList periods = (ArrayList)repaymentSchedule.get("periods") ;
        //
        //
        String addVariationsjsondata = VariableInstallmentsDecliningBalanceHelper.createModifiyDateVariations(new String[]{"20 December 2011", "20 January 2012"}, new String[]{"04 January 2012", "08 February 2012"}, new String[]{"20000"}) ; //0th position will have disbursement 
        HashMap modifiedReschdule = transactionHelper.validateVariations(addVariationsjsondata, loanID) ;
        ArrayList newperiods = (ArrayList) modifiedReschdule.get("periods") ;
        ArrayList toVerifyData = VariableInstallmentsDecliningBalanceHelper.constructVerifyData(new String[] {"20 October 2011", "20 November 2011", "04 January 2012", "08 February 2012"}, 
                new String[] {"26262.38", "26262.38", "20000.0", "33242.97"}) ;
        assertAfterSubmit(newperiods, toVerifyData) ;   
        transactionHelper.submitVariations(addVariationsjsondata, loanID) ;
        list = transactionHelper.retrieveSchedule(loanID) ;
        repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        periods = (ArrayList)repaymentSchedule.get("periods") ;
        periods.remove(0) ; //Repayments Schedule includes disbursement also. So remove this.
        assertAfterSubmit(periods, toVerifyData) ;
    }
    
    // Interest Type is FLAT
    @Test
    public void testDeleteInstallmentsWithInterestTypeFlat() {
        VariableIntallmentsTransactionHelper transactionHelper = new VariableIntallmentsTransactionHelper(requestSpec, responseSpec) ;
        final String  loanProductJson = VariableInstallmentsFlatHelper.createLoanProductWithVaribleConfig(false, NONE);
        Integer loanProductID = this.loanTransactionHelper.getLoanProductId(loanProductJson);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final String json = VariableInstallmentsFlatHelper.applyForLoanApplication(clientID, loanProductID, null, null, "1,00,000.00");
        final Integer loanID = this.loanTransactionHelper.getLoanId(json);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
        Map list = transactionHelper.retrieveSchedule(loanID) ;
        Map repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        ArrayList periods = (ArrayList)repaymentSchedule.get("periods") ;
        ArrayList toDelete = new ArrayList<>() ;
        toDelete.add(periods.get(1)) ;
        String toDeletedata = VariableInstallmentsFlatHelper.createDeleteVariations(toDelete) ;
        HashMap modifiedReschdule = transactionHelper.validateVariations(toDeletedata, loanID) ;
        ArrayList newperiods = (ArrayList) modifiedReschdule.get("periods") ;
        ArrayList toVerifyData = VariableInstallmentsFlatHelper.constructVerifyData(new String[] {"20 November 2011", "20 December 2011", "20 January 2012"}, 
                new String[] {"36000.0", "36000.0", "36000.0"}) ;
        assertAfterSubmit(newperiods, toVerifyData) ;
        transactionHelper.submitVariations(toDeletedata, loanID) ;
        list = transactionHelper.retrieveSchedule(loanID) ;
        repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        periods = (ArrayList)repaymentSchedule.get("periods") ;
        periods.remove(0) ; //Repayments Schedule includes disbursement also. So remove this.
        assertAfterSubmit(periods, toVerifyData) ;
    }
    
    @Test
    public void testAddInstallmentsWithInterestTypeFlat() {
        //31 October 2011 - 5000
        //Result: 20 October 2011 - 21600.0, 31 October 2011 - 6600.0, 20 November 2011 26600.0, 20 December 2011 26600.0, 20 January 2012 26600.0
        VariableIntallmentsTransactionHelper transactionHelper = new VariableIntallmentsTransactionHelper(requestSpec, responseSpec) ;
        final String  loanProductJson = VariableInstallmentsFlatHelper.createLoanProductWithVaribleConfig(false, NONE);
        Integer loanProductID = this.loanTransactionHelper.getLoanProductId(loanProductJson);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final String json = VariableInstallmentsFlatHelper.applyForLoanApplication(clientID, loanProductID, null, null, "1,00,000.00");
        final Integer loanID = this.loanTransactionHelper.getLoanId(json);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
        //Integer loanID = 67 ;
        Map list = transactionHelper.retrieveSchedule(loanID) ;
        Map repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        ArrayList periods = (ArrayList)repaymentSchedule.get("periods") ;
        String addVariationsjsondata = VariableInstallmentsFlatHelper.createAddVariations() ;
        HashMap modifiedReschdule = transactionHelper.validateVariations(addVariationsjsondata, loanID) ;
        ArrayList newperiods = (ArrayList) modifiedReschdule.get("periods") ;
        ArrayList toVerifyData = VariableInstallmentsFlatHelper.constructVerifyData(new String[] {"20 October 2011", "31 October 2011", "20 November 2011", "20 December 2011", "20 January 2012"}, 
                new String[] {"21600.0", "6600.0", "26600.0", "26600.0", "26600.0"}) ;
        assertAfterSubmit(newperiods, toVerifyData) ;
        transactionHelper.submitVariations(addVariationsjsondata, loanID) ;
        list = transactionHelper.retrieveSchedule(loanID) ;
        repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        periods = (ArrayList)repaymentSchedule.get("periods") ;
        periods.remove(0) ; //Repayments Schedule includes disbursement also. So remove this.
        assertAfterSubmit(periods, toVerifyData) ;
    }
    
    @Test
    public void testModifyInstallmentsWithInterestTypeisFlat() {
        //20 October 2011 - 30000 modify
        //Result 20 October 2011 - 32000.0, 20 November 2011 - 25333.33, 20 December 2011 - 25333.33, 20 January 2012 - 25333.34
        VariableIntallmentsTransactionHelper transactionHelper = new VariableIntallmentsTransactionHelper(requestSpec, responseSpec) ;
        final String  loanProductJson = VariableInstallmentsFlatHelper.createLoanProductWithVaribleConfig(false, NONE);
        Integer loanProductID = this.loanTransactionHelper.getLoanProductId(loanProductJson);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final String json = VariableInstallmentsFlatHelper.applyForLoanApplication(clientID, loanProductID, null, null, "1,00,000.00");
        final Integer loanID = this.loanTransactionHelper.getLoanId(json);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
        //Integer loanID = 67 ;
        Map list = transactionHelper.retrieveSchedule(loanID) ;
        Map repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        ArrayList periods = (ArrayList)repaymentSchedule.get("periods") ;
        String addVariationsjsondata = VariableInstallmentsFlatHelper.createModifiyVariations((Map)periods.get(1)) ; //0th position will have disbursement 
        HashMap modifiedReschdule = transactionHelper.validateVariations(addVariationsjsondata, loanID) ;
        ArrayList newperiods = (ArrayList) modifiedReschdule.get("periods") ;
        ArrayList toVerifyData = VariableInstallmentsFlatHelper.constructVerifyData(new String[] {"20 October 2011", "20 November 2011", "20 December 2011", "20 January 2012"}, 
                new String[] {"32000.0", "25333.33", "25333.33", "25333.34"}) ;
        assertAfterSubmit(newperiods, toVerifyData) ;
        transactionHelper.submitVariations(addVariationsjsondata, loanID) ;
        list = transactionHelper.retrieveSchedule(loanID) ;
        repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        periods = (ArrayList)repaymentSchedule.get("periods") ;
        periods.remove(0) ; //Repayments Schedule includes disbursement also. So remove this.
        assertAfterSubmit(periods, toVerifyData) ;
    }
    
    @Test
    public void testAllVariationsWithInterestTypeFlat() {
         // Request: Delete 20 December 2011 25000.0, Modify 20 November 2011 from 25,000 to 30000, Add 25 December 2011 5000
         // Result: 20 October 2011 - 27000.0, 20 November 2011 - 32000.0, 25 December 2011 - 7000.0, 20 January 2012 - 42000.0  
        VariableIntallmentsTransactionHelper transactionHelper = new VariableIntallmentsTransactionHelper(requestSpec, responseSpec) ;
        final String  loanProductJson = VariableInstallmentsFlatHelper.createLoanProductWithVaribleConfig(false, NONE);
        Integer loanProductID = this.loanTransactionHelper.getLoanProductId(loanProductJson);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final String json = VariableInstallmentsFlatHelper.applyForLoanApplication(clientID, loanProductID, null, null, "1,00,000.00");
        final Integer loanID = this.loanTransactionHelper.getLoanId(json);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
        //Integer loanID = 67 ;
        Map list = transactionHelper.retrieveSchedule(loanID) ;
        Map repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        ArrayList periods = (ArrayList)repaymentSchedule.get("periods") ;
        
        String addVariationsjsondata = VariableInstallmentsFlatHelper.createAllVariations() ; //0th position will have disbursement 
        HashMap modifiedReschdule = transactionHelper.validateVariations(addVariationsjsondata, loanID) ;
        ArrayList newperiods = (ArrayList) modifiedReschdule.get("periods") ;
        ArrayList toVerifyData = VariableInstallmentsFlatHelper.constructVerifyData(new String[] {"20 October 2011", "20 November 2011", "25 December 2011", "20 January 2012"}, 
                new String[] {"27000.0", "32000.0", "7000.0", "42000.0"}) ;
        assertAfterSubmit(newperiods, toVerifyData) ;
        transactionHelper.submitVariations(addVariationsjsondata, loanID) ;
        list = transactionHelper.retrieveSchedule(loanID) ;
        repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        periods = (ArrayList)repaymentSchedule.get("periods") ;
        periods.remove(0) ; //Repayments Schedule includes disbursement also. So remove this.
        assertAfterSubmit(periods, toVerifyData) ;
    }
    
    @Test
    public void testModifyDatesWithInterestTypeFlat() {
        //Modify 20 December 2011:25000 -> 04 January 2012:20000
        //Modify 20 January 2012 -> 08 February 2012
        //Result 20 October 2011 -27306.45, 20 November 2011 - 27306.45, 04 January 2012 -22306.45, 08 February 2012 - 32306.46
        VariableIntallmentsTransactionHelper transactionHelper = new VariableIntallmentsTransactionHelper(requestSpec, responseSpec) ;
        final String  loanProductJson = VariableInstallmentsFlatHelper.createLoanProductWithVaribleConfig(false, NONE);
        Integer loanProductID = this.loanTransactionHelper.getLoanProductId(loanProductJson);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final String json = VariableInstallmentsFlatHelper.applyForLoanApplication(clientID, loanProductID, null, null, "1,00,000.00");
        final Integer loanID = this.loanTransactionHelper.getLoanId(json);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
        //Integer loanID = 67 ;
        
        Map list = transactionHelper.retrieveSchedule(loanID) ;
        Map repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        ArrayList periods = (ArrayList)repaymentSchedule.get("periods") ;
        //
        //
        String addVariationsjsondata = VariableInstallmentsFlatHelper.createModifiyDateVariations(new String[]{"20 December 2011", "20 January 2012"}, new String[]{"04 January 2012", "08 February 2012"}, new String[]{"20000"}) ; //0th position will have disbursement 
        HashMap modifiedReschdule = transactionHelper.validateVariations(addVariationsjsondata, loanID) ;
        ArrayList newperiods = (ArrayList) modifiedReschdule.get("periods") ;
        ArrayList toVerifyData = VariableInstallmentsFlatHelper.constructVerifyData(new String[] {"20 October 2011", "20 November 2011", "04 January 2012", "08 February 2012"}, 
                new String[] {"27306.45", "27306.45", "22306.45", "32306.46"}) ;
        assertAfterSubmit(newperiods, toVerifyData) ;   
        transactionHelper.submitVariations(addVariationsjsondata, loanID) ;
        list = transactionHelper.retrieveSchedule(loanID) ;
        repaymentSchedule = (Map)list.get("repaymentSchedule") ;
        periods = (ArrayList)repaymentSchedule.get("periods") ;
        periods.remove(0) ; //Repayments Schedule includes disbursement also. So remove this.
        assertAfterSubmit(periods, toVerifyData) ;
    }
}
