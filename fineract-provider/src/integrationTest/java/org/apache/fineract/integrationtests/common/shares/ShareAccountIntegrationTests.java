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
package org.apache.fineract.integrationtests.common.shares;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class ShareAccountIntegrationTests {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private ShareProductHelper shareProductHelper;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testCreateShareProduct() {
        // This method will check create share product, get share product,
        // update share product.
        System.out.println("------------------------------CREATING NEW SHARE PRODUCT ---------------------------------------");
        shareProductHelper = new ShareProductHelper();
        final Integer shareProductId = createShareProduct();
        Assert.assertNotNull(shareProductId);
        System.out.println("------------------------------CREATING SHARE PRODUCT COMPLETE---------------------------------------");

        System.out.println("------------------------------RETRIEVING SHARE PRODUCT---------------------------------------");
        Map<String, Object> shareProductData = ShareProductTransactionHelper
                .retrieveShareProduct(shareProductId, requestSpec, responseSpec);
        Assert.assertNotNull(shareProductData);
        shareProductHelper.verifyShareProduct(shareProductData);

        System.out.println("------------------------------RETRIEVING SHARE PRODUCT COMPLETE---------------------------------------");

        System.out.println("------------------------------UPDATING SHARE PRODUCT---------------------------------------");

        Map<String, Object> shareProductDataForUpdate = new HashMap<>();

        shareProductDataForUpdate.put("totalShares", "2000");
        shareProductDataForUpdate.put("sharesIssued", "2000");

        String updateShareProductJsonString = new Gson().toJson(shareProductDataForUpdate);
        Integer updatedProductId = ShareProductTransactionHelper.updateShareProduct(shareProductId, updateShareProductJsonString,
                requestSpec, responseSpec);
        Assert.assertNotNull(updatedProductId);
        Map<String, Object> updatedShareProductData = ShareProductTransactionHelper.retrieveShareProduct(updatedProductId, requestSpec,
                responseSpec);
        String updatedTotalShares = String.valueOf(updatedShareProductData.get("totalShares"));
        String updatedSharesIssued = String.valueOf(updatedShareProductData.get("totalSharesIssued"));
        Assert.assertEquals("2000", updatedTotalShares);
        Assert.assertEquals("2000", updatedSharesIssued);
        System.out.println("------------------------------UPDATING SHARE PRODUCT COMPLETE---------------------------------------");

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateShareAccount() {
        shareProductHelper = new ShareProductHelper();
        final Integer productId = createShareProduct();
        Assert.assertNotNull(productId);
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);
        Integer savingsAccountId = SavingsAccountHelper.openSavingsAccount(requestSpec, responseSpec, clientId, "1000");
        Assert.assertNotNull(savingsAccountId);
        final Integer shareAccountId = createShareAccount(clientId, productId, savingsAccountId);
        Assert.assertNotNull(shareAccountId);
        Map<String, Object> shareProductData = ShareAccountTransactionHelper
                .retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        Assert.assertNotNull(shareProductData);

        Map<String, Object> shareAccountDataForUpdate = new HashMap<>();
        shareAccountDataForUpdate.put("requestedShares", 30);
        shareAccountDataForUpdate.put("applicationDate", "02 Mar 2016");
        shareAccountDataForUpdate.put("dateFormat", "dd MMMM yyyy");
        shareAccountDataForUpdate.put("locale", "en_GB");
        String updateShareAccountJsonString = new Gson().toJson(shareAccountDataForUpdate);
        ShareAccountTransactionHelper.updateShareAccount(shareAccountId, updateShareAccountJsonString, requestSpec, responseSpec);
        shareProductData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        List<Map<String, Object>> transactions = (List<Map<String, Object>>) shareProductData.get("purchasedShares");
        Assert.assertNotNull(transactions);
        Assert.assertEquals(1, transactions.size());
        Map<String, Object> transaction = transactions.get(0);
        Assert.assertEquals("30", String.valueOf(transaction.get("numberOfShares")));
        Assert.assertEquals("60.0", String.valueOf(transaction.get("amount")));
        Assert.assertEquals("60.0", String.valueOf(transaction.get("amountPaid")));
        List<Integer> dateList = (List<Integer>) transaction.get("purchasedDate");
        Calendar cal = Calendar.getInstance();
        cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
        Date date = cal.getTime();
        DateFormat simple = new SimpleDateFormat("dd MMM yyyy");
        Assert.assertEquals("02 Mar 2016", simple.format(date));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testShareAccountApproval() {
        DateFormat simple = new SimpleDateFormat("dd MMM yyyy");
        shareProductHelper = new ShareProductHelper();
        final Integer productId = createShareProduct();
        Assert.assertNotNull(productId);
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);
        Integer savingsAccountId = SavingsAccountHelper.openSavingsAccount(requestSpec, responseSpec, clientId, "1000");
        Assert.assertNotNull(savingsAccountId);
        String activationCharge = ChargesHelper.getShareAccountActivationChargeJson();
        Integer activationChargeId = ChargesHelper.createCharges(requestSpec, responseSpec, activationCharge);
        String purchaseCharge = ChargesHelper.getShareAccountPurchaseChargeJson();
        Integer purchaseChargeId = ChargesHelper.createCharges(requestSpec, responseSpec, purchaseCharge);
        String redeemCharge = ChargesHelper.getShareAccountRedeemChargeJson();
        Integer redeemChargeId = ChargesHelper.createCharges(requestSpec, responseSpec, redeemCharge);
        List<Map<String, Object>> charges = new ArrayList<>();
        charges.add(createCharge(activationChargeId, "2"));
        charges.add(createCharge(purchaseChargeId, "2"));
        charges.add(createCharge(redeemChargeId, "1"));
        final Integer shareAccountId = createShareAccount(clientId, productId, savingsAccountId, charges);
        Assert.assertNotNull(shareAccountId);
        Map<String, Object> shareAccountData = ShareAccountTransactionHelper
                .retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        Assert.assertNotNull(shareAccountData);
        
     // Approve share Account
        Map<String, Object> approveMap = new HashMap<>();
        approveMap.put("note", "Share Account Approval Note");
        approveMap.put("dateFormat", "dd MMMM yyyy");
        approveMap.put("approvedDate", "01 Jan 2016");
        approveMap.put("locale", "en");
        String approve = new Gson().toJson(approveMap);
        ShareAccountTransactionHelper.postCommand("approve", shareAccountId, approve, requestSpec, responseSpec);
        shareAccountData = ShareAccountTransactionHelper
                .retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        Map<String, Object> statusMap = (Map<String, Object>) shareAccountData.get("status");
        Assert.assertEquals("shareAccountStatusType.approved", String.valueOf(statusMap.get("code")));
        Map<String, Object> timelineMap = (Map<String, Object>) shareAccountData.get("timeline");
        List<Integer> dateList = (List<Integer>) timelineMap.get("approvedDate");
        Calendar cal = Calendar.getInstance();
        cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
        Date approvedDate = cal.getTime();
        Assert.assertEquals("01 Jan 2016", simple.format(approvedDate));
        List<Map<String, Object>> transactions = (List<Map<String, Object>>) shareAccountData.get("purchasedShares");
        Assert.assertNotNull(transactions);
        Assert.assertEquals(2, transactions.size());
        for (int i = 0; i < transactions.size(); i++) {
            Map<String, Object> transaction = transactions.get(i);
            Map<String, Object> transactionTypeMap = (Map<String, Object>) transaction.get("type");
            dateList = (List<Integer>) transaction.get("purchasedDate");
            cal = Calendar.getInstance();
            cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
            Date date = cal.getTime();
            String transactionType = (String) transactionTypeMap.get("code");
            if (transactionType.equals("purchasedSharesType.purchased")) {
                Assert.assertEquals("25", String.valueOf(transaction.get("numberOfShares")));
                Assert.assertEquals("52.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("52.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Assert.assertEquals("01 Jan 2016", simple.format(date));
            } else if (transactionType.equals("charge.payment")) {
                Assert.assertEquals("2.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("0", String.valueOf(transaction.get("amountPaid")));
                Date transactionDate = DateUtils.getDateOfTenant() ;
                Assert.assertEquals(simple.format(transactionDate), simple.format(date));
            }
        }

        Map<String, Object> summaryMap = (Map<String, Object>) shareAccountData.get("summary");
        Assert.assertEquals("25", String.valueOf(summaryMap.get("totalApprovedShares")));
        Assert.assertEquals("0", String.valueOf(summaryMap.get("totalPendingForApprovalShares")));
    }
    @Test
    @SuppressWarnings("unchecked")
    public void rejectShareAccount() {
        shareProductHelper = new ShareProductHelper();
        final Integer productId = createShareProduct();
        Assert.assertNotNull(productId);
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);
        Integer savingsAccountId = SavingsAccountHelper.openSavingsAccount(requestSpec, responseSpec, clientId, "1000");
        Assert.assertNotNull(savingsAccountId);
        String activationCharge = ChargesHelper.getShareAccountActivationChargeJson();
        Integer activationChargeId = ChargesHelper.createCharges(requestSpec, responseSpec, activationCharge);
        String purchaseCharge = ChargesHelper.getShareAccountPurchaseChargeJson();
        Integer purchaseChargeId = ChargesHelper.createCharges(requestSpec, responseSpec, purchaseCharge);
        String redeemCharge = ChargesHelper.getShareAccountRedeemChargeJson();
        Integer redeemChargeId = ChargesHelper.createCharges(requestSpec, responseSpec, redeemCharge);
        List<Map<String, Object>> charges = new ArrayList<>();
        charges.add(createCharge(activationChargeId, "2"));
        charges.add(createCharge(purchaseChargeId, "2"));
        charges.add(createCharge(redeemChargeId, "1"));
        final Integer shareAccountId = createShareAccount(clientId, productId, savingsAccountId, charges);
        Assert.assertNotNull(shareAccountId);
        Map<String, Object> shareAccountData = ShareAccountTransactionHelper
                .retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        Assert.assertNotNull(shareAccountData);
        
        // Reject share Account
        Map<String, Object> rejectMap = new HashMap<>();
        rejectMap.put("note", "Share Account Rejection Note");
        String rejectJson = new Gson().toJson(rejectMap);
        ShareAccountTransactionHelper.postCommand("reject", shareAccountId, rejectJson, requestSpec, responseSpec);
        shareAccountData = ShareAccountTransactionHelper
                .retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        DateFormat simple = new SimpleDateFormat("dd MMM yyyy");
        Map<String, Object> statusMap = (Map<String, Object>) shareAccountData.get("status");
        Assert.assertEquals("shareAccountStatusType.rejected", String.valueOf(statusMap.get("code")));
        Map<String, Object> timelineMap = (Map<String, Object>) shareAccountData.get("timeline");
        List<Integer> dateList = (List<Integer>) timelineMap.get("rejectedDate");
        Calendar cal = Calendar.getInstance();
        cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
        Date rejectedDate = cal.getTime();
        Date currentTenantDate = DateUtils.getDateOfTenant() ;
        Assert.assertEquals(simple.format(currentTenantDate), simple.format(rejectedDate));
        
        List<Map<String, Object>> transactions = (List<Map<String, Object>>) shareAccountData.get("purchasedShares");
        Assert.assertNotNull(transactions);
        Assert.assertEquals(2, transactions.size());
        for (int i = 0; i < transactions.size(); i++) {
            Map<String, Object> transaction = transactions.get(i);
            Map<String, Object> transactionTypeMap = (Map<String, Object>) transaction.get("type");
            dateList = (List<Integer>) transaction.get("purchasedDate");
            cal = Calendar.getInstance();
            cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
            Date date = cal.getTime();
            String transactionType = (String) transactionTypeMap.get("code");
            if (transactionType.equals("purchasedSharesType.purchased")) {
                Assert.assertEquals("25", String.valueOf(transaction.get("numberOfShares")));
                Assert.assertEquals("52.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("50.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Assert.assertEquals("01 Jan 2016", simple.format(date));
            } else if (transactionType.equals("charge.payment")) {
                Assert.assertEquals("2.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("0", String.valueOf(transaction.get("amountPaid")));
                Date transactionDate = DateUtils.getDateOfTenant() ;
                Assert.assertEquals(simple.format(transactionDate), simple.format(date));
            }
        }

        Map<String, Object> summaryMap = (Map<String, Object>) shareAccountData.get("summary");
        Assert.assertEquals("0", String.valueOf(summaryMap.get("totalApprovedShares")));
        Assert.assertEquals("0", String.valueOf(summaryMap.get("totalPendingForApprovalShares")));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testShareAccountUndoApproval() {
        DateFormat simple = new SimpleDateFormat("dd MMM yyyy");
        shareProductHelper = new ShareProductHelper();
        final Integer productId = createShareProduct();
        Assert.assertNotNull(productId);
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);
        Integer savingsAccountId = SavingsAccountHelper.openSavingsAccount(requestSpec, responseSpec, clientId, "1000");
        Assert.assertNotNull(savingsAccountId);
        String activationCharge = ChargesHelper.getShareAccountActivationChargeJson();
        Integer activationChargeId = ChargesHelper.createCharges(requestSpec, responseSpec, activationCharge);
        String purchaseCharge = ChargesHelper.getShareAccountPurchaseChargeJson();
        Integer purchaseChargeId = ChargesHelper.createCharges(requestSpec, responseSpec, purchaseCharge);
        String redeemCharge = ChargesHelper.getShareAccountRedeemChargeJson();
        Integer redeemChargeId = ChargesHelper.createCharges(requestSpec, responseSpec, redeemCharge);
        List<Map<String, Object>> charges = new ArrayList<>();
        charges.add(createCharge(activationChargeId, "2"));
        charges.add(createCharge(purchaseChargeId, "2"));
        charges.add(createCharge(redeemChargeId, "1"));
        final Integer shareAccountId = createShareAccount(clientId, productId, savingsAccountId, charges);
        Assert.assertNotNull(shareAccountId);
        Map<String, Object> shareAccountData = ShareAccountTransactionHelper
                .retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        Assert.assertNotNull(shareAccountData);
        
     // Approve share Account
        Map<String, Object> approveMap = new HashMap<>();
        approveMap.put("note", "Share Account Approval Note");
        approveMap.put("dateFormat", "dd MMMM yyyy");
        approveMap.put("approvedDate", "01 Jan 2016");
        approveMap.put("locale", "en");
        String approve = new Gson().toJson(approveMap);
        ShareAccountTransactionHelper.postCommand("approve", shareAccountId, approve, requestSpec, responseSpec);
        shareAccountData = ShareAccountTransactionHelper
                .retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        Map<String, Object> statusMap = (Map<String, Object>) shareAccountData.get("status");
        Assert.assertEquals("shareAccountStatusType.approved", String.valueOf(statusMap.get("code")));
        Map<String, Object> timelineMap = (Map<String, Object>) shareAccountData.get("timeline");
        List<Integer> dateList = (List<Integer>) timelineMap.get("approvedDate");
        Calendar cal = Calendar.getInstance();
        cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
        Date approvedDate = cal.getTime();
        Assert.assertEquals("01 Jan 2016", simple.format(approvedDate));
        
        // Undo Approval share Account
        Map<String, Object> undoApprovalMap = new HashMap<>();
        String undoApprovalJson = new Gson().toJson(undoApprovalMap);
        ShareAccountTransactionHelper.postCommand("undoapproval", shareAccountId, undoApprovalJson, requestSpec, responseSpec);
        
        shareAccountData = ShareAccountTransactionHelper
                .retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        
        statusMap = (Map<String, Object>) shareAccountData.get("status");
        Assert.assertEquals("shareAccountStatusType.submitted.and.pending.approval", String.valueOf(statusMap.get("code")));
        
        List<Map<String, Object>> transactions = (List<Map<String, Object>>) shareAccountData.get("purchasedShares");
        Assert.assertNotNull(transactions);
        Assert.assertEquals(2, transactions.size());
        for (int i = 0; i < transactions.size(); i++) {
            Map<String, Object> transaction = transactions.get(i);
            Map<String, Object> transactionTypeMap = (Map<String, Object>) transaction.get("type");
            dateList = (List<Integer>) transaction.get("purchasedDate");
            cal = Calendar.getInstance();
            cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
            Date date = cal.getTime();
            String transactionType = (String) transactionTypeMap.get("code");
            if (transactionType.equals("purchasedSharesType.purchased")) {
                Assert.assertEquals("25", String.valueOf(transaction.get("numberOfShares")));
                Assert.assertEquals("52.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Assert.assertEquals("01 Jan 2016", simple.format(date));
            } else if (transactionType.equals("charge.payment")) {
                Assert.assertEquals("2.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("0", String.valueOf(transaction.get("amountPaid")));
                Date transactionDate = DateUtils.getDateOfTenant() ;
                Assert.assertEquals(simple.format(transactionDate), simple.format(date));
            }
        }

        Map<String, Object> summaryMap = (Map<String, Object>) shareAccountData.get("summary");
        Assert.assertEquals("0", String.valueOf(summaryMap.get("totalApprovedShares")));
        Assert.assertEquals("25", String.valueOf(summaryMap.get("totalPendingForApprovalShares")));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateShareAccountWithCharges() {
        shareProductHelper = new ShareProductHelper();
        final Integer productId = createShareProduct();
        Assert.assertNotNull(productId);
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);
        Integer savingsAccountId = SavingsAccountHelper.openSavingsAccount(requestSpec, responseSpec, clientId, "1000");
        Assert.assertNotNull(savingsAccountId);
        String activationCharge = ChargesHelper.getShareAccountActivationChargeJson();
        Integer activationChargeId = ChargesHelper.createCharges(requestSpec, responseSpec, activationCharge);
        String purchaseCharge = ChargesHelper.getShareAccountPurchaseChargeJson();
        Integer purchaseChargeId = ChargesHelper.createCharges(requestSpec, responseSpec, purchaseCharge);
        String redeemCharge = ChargesHelper.getShareAccountRedeemChargeJson();
        Integer redeemChargeId = ChargesHelper.createCharges(requestSpec, responseSpec, redeemCharge);
        List<Map<String, Object>> charges = new ArrayList<>();
        charges.add(createCharge(activationChargeId, "2"));
        charges.add(createCharge(purchaseChargeId, "2"));
        charges.add(createCharge(redeemChargeId, "1"));
        final Integer shareAccountId = createShareAccount(clientId, productId, savingsAccountId, charges);
        Assert.assertNotNull(shareAccountId);
        Map<String, Object> shareAccountData = ShareAccountTransactionHelper
                .retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        Assert.assertNotNull(shareAccountData);

        Map<String, Object> shareAccountDataForUpdate = new HashMap<>();
        shareAccountDataForUpdate.put("requestedShares", 30);
        shareAccountDataForUpdate.put("applicationDate", "02 Mar 2016");
        shareAccountDataForUpdate.put("dateFormat", "dd MMMM yyyy");
        shareAccountDataForUpdate.put("locale", "en_GB");
        shareAccountDataForUpdate.put("charges", charges);

        String updateShareAccountJsonString = new Gson().toJson(shareAccountDataForUpdate);
        ShareAccountTransactionHelper.updateShareAccount(shareAccountId, updateShareAccountJsonString, requestSpec, responseSpec);
        shareAccountData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        List<Map<String, Object>> transactions = (List<Map<String, Object>>) shareAccountData.get("purchasedShares");
        Assert.assertNotNull(transactions);
        Assert.assertEquals(2, transactions.size());
        DateFormat simple = new SimpleDateFormat("dd MMM yyyy");
        for (int i = 0; i < transactions.size(); i++) {
            Map<String, Object> transaction = transactions.get(i);
            Map<String, Object> transactionTypeMap = (Map<String, Object>) transaction.get("type");
            List<Integer> dateList = (List<Integer>) transaction.get("purchasedDate");
            Calendar cal = Calendar.getInstance();
            cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
            Date date = cal.getTime();
            String transactionType = (String) transactionTypeMap.get("code");
            if (transactionType.equals("purchasedSharesType.purchased")) {
                Assert.assertEquals("30", String.valueOf(transaction.get("numberOfShares")));
                Assert.assertEquals("62.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("60.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Assert.assertEquals("02 Mar 2016", simple.format(date));
            } else if (transactionType.equals("charge.payment")) {
                Assert.assertEquals("2.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("0", String.valueOf(transaction.get("chargeAmount")));
            }
        }

        //charges verification
        List<Map<String, Object>> chargesList = (List<Map<String, Object>>) shareAccountData.get("charges") ;
        for(Map<String, Object> chargeDef: chargesList) {
            Map<String, Object> chargeTimeTypeMap = (Map<String, Object>) chargeDef.get("chargeTimeType") ;
            String chargeTimeType = String.valueOf(chargeTimeTypeMap.get("code")) ;
            if(chargeTimeType.equals("chargeTimeType.activation")) {
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("0", String.valueOf(chargeDef.get("amountPaid")));
            }else if(chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("0", String.valueOf(chargeDef.get("amountPaid")));
            }else if(chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assert.assertEquals("1.0", String.valueOf(chargeDef.get("amountOrPercentage")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("0", String.valueOf(chargeDef.get("amountPaid")));
            }else {
                Assert.fail("Other Charge defintion found");
            }
        }
        
        // Approve share Account
        Map<String, Object> approveMap = new HashMap<>();
        approveMap.put("note", "Share Account Approval Note");
        approveMap.put("dateFormat", "dd MMMM yyyy");
        approveMap.put("approvedDate", "01 Jan 2016");
        approveMap.put("locale", "en");
        String approve = new Gson().toJson(approveMap);
        ShareAccountTransactionHelper.postCommand("approve", shareAccountId, approve, requestSpec, responseSpec);
        shareAccountData = ShareAccountTransactionHelper
                .retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        Map<String, Object> statusMap = (Map<String, Object>) shareAccountData.get("status");
        Assert.assertEquals("shareAccountStatusType.approved", String.valueOf(statusMap.get("code")));
        Map<String, Object> timelineMap = (Map<String, Object>) shareAccountData.get("timeline");
        List<Integer> dateList = (List<Integer>) timelineMap.get("approvedDate");
        Calendar cal = Calendar.getInstance();
        cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
        Date approvedDate = cal.getTime();
        Assert.assertEquals("01 Jan 2016", simple.format(approvedDate));

        //charges verification
        chargesList = (List<Map<String, Object>>) shareAccountData.get("charges") ;
        for(Map<String, Object> chargeDef: chargesList) {
            Map<String, Object> chargeTimeTypeMap = (Map<String, Object>) chargeDef.get("chargeTimeType") ;
            String chargeTimeType = String.valueOf(chargeTimeTypeMap.get("code")) ;
            if(chargeTimeType.equals("chargeTimeType.activation")) {
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("0", String.valueOf(chargeDef.get("amountPaid")));
            }else if(chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            }else if(chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assert.assertEquals("1.0", String.valueOf(chargeDef.get("amountOrPercentage")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("0", String.valueOf(chargeDef.get("amountPaid")));
            }else {
                Assert.fail("Other Charge defintion found");
            }
        }
        
        Map<String, Object> activateMap = new HashMap<>();
        activateMap.put("dateFormat", "dd MMMM yyyy");
        activateMap.put("activatedDate", "01 Jan 2016");
        activateMap.put("locale", "en");
        String activateJson = new Gson().toJson(activateMap);
        ShareAccountTransactionHelper.postCommand("activate", shareAccountId, activateJson, requestSpec, responseSpec);
        shareAccountData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        statusMap = (Map<String, Object>) shareAccountData.get("status");
        Assert.assertEquals("shareAccountStatusType.active", String.valueOf(statusMap.get("code")));
        timelineMap = (Map<String, Object>) shareAccountData.get("timeline");
        dateList = (List<Integer>) timelineMap.get("activatedDate");
        cal = Calendar.getInstance();
        cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
        Date activatedDate = cal.getTime();
        Assert.assertEquals("01 Jan 2016", simple.format(activatedDate));

        transactions = (List<Map<String, Object>>) shareAccountData.get("purchasedShares");
        Assert.assertNotNull(transactions);
        Assert.assertEquals(2, transactions.size());
        for (int i = 0; i < transactions.size(); i++) {
            Map<String, Object> transaction = transactions.get(i);
            Map<String, Object> transactionTypeMap = (Map<String, Object>) transaction.get("type");
            dateList = (List<Integer>) transaction.get("purchasedDate");
            cal = Calendar.getInstance();
            cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
            Date date = cal.getTime();
            String transactionType = (String) transactionTypeMap.get("code");
            if (transactionType.equals("purchasedSharesType.purchased")) {
                Assert.assertEquals("30", String.valueOf(transaction.get("numberOfShares")));
                Assert.assertEquals("62.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("62.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Assert.assertEquals("02 Mar 2016", simple.format(date));
            } else if (transactionType.equals("charge.payment")) {
                Assert.assertEquals("2.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("0", String.valueOf(transaction.get("chargeAmount")));
                Assert.assertEquals("01 Jan 2016", simple.format(date));
            }
        }

      //charges verification
        chargesList = (List<Map<String, Object>>) shareAccountData.get("charges") ;
        for(Map<String, Object> chargeDef: chargesList) {
            Map<String, Object> chargeTimeTypeMap = (Map<String, Object>) chargeDef.get("chargeTimeType") ;
            String chargeTimeType = String.valueOf(chargeTimeTypeMap.get("code")) ;
            if(chargeTimeType.equals("chargeTimeType.activation")) {
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            }else if(chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            }else if(chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assert.assertEquals("1.0", String.valueOf(chargeDef.get("amountOrPercentage")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("0", String.valueOf(chargeDef.get("amountPaid")));
            }else {
                Assert.fail("Other Charge defintion found");
            }
        }
        
        Map<String, Object> summaryMap = (Map<String, Object>) shareAccountData.get("summary");
        Assert.assertEquals("30", String.valueOf(summaryMap.get("totalApprovedShares")));
        Assert.assertEquals("0", String.valueOf(summaryMap.get("totalPendingForApprovalShares")));

        // apply additional shares
        Map<String, Object> additionalSharesRequestMap = new HashMap<>();
        additionalSharesRequestMap.put("requestedDate", "01 Apr 2016");
        additionalSharesRequestMap.put("dateFormat", "dd MMMM yyyy");
        additionalSharesRequestMap.put("locale", "en");
        additionalSharesRequestMap.put("requestedShares", "15");
        String additionalSharesRequestJson = new Gson().toJson(additionalSharesRequestMap);
        ShareAccountTransactionHelper.postCommand("applyadditionalshares", shareAccountId, additionalSharesRequestJson, requestSpec,
                responseSpec);
        shareAccountData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        transactions = (List<Map<String, Object>>) shareAccountData.get("purchasedShares");
        Assert.assertNotNull(transactions);
        Assert.assertEquals(3, transactions.size());
        String addtionalSharesRequestId = null;
        for (int i = 0; i < transactions.size(); i++) {
            Map<String, Object> transaction = transactions.get(i);
            Map<String, Object> transactionTypeMap = (Map<String, Object>) transaction.get("type");
            dateList = (List<Integer>) transaction.get("purchasedDate");
            cal = Calendar.getInstance();
            cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
            Date date = cal.getTime();
            String transactionType = (String) transactionTypeMap.get("code");
            String transactionDate = simple.format(date);
            if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("02 Mar 2016")) {
                Assert.assertEquals("30", String.valueOf(transaction.get("numberOfShares")));
                Assert.assertEquals("62.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("62.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
            } else if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("01 Apr 2016")) {
                addtionalSharesRequestId = String.valueOf(transaction.get("id"));
                Assert.assertEquals("15", String.valueOf(transaction.get("numberOfShares")));
                Assert.assertEquals("32.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("30.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Map<String, Object> transactionstatusMap = (Map<String, Object>) transaction.get("status");
                Assert.assertEquals("purchasedSharesStatusType.applied", String.valueOf(transactionstatusMap.get("code")));

            } else if (transactionType.equals("charge.payment")) {
                Assert.assertEquals("2.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("0", String.valueOf(transaction.get("chargeAmount")));
                Assert.assertEquals("01 Jan 2016", transactionDate);
            }
        }

      //charges verification
        chargesList = (List<Map<String, Object>>) shareAccountData.get("charges") ;
        for(Map<String, Object> chargeDef: chargesList) {
            Map<String, Object> chargeTimeTypeMap = (Map<String, Object>) chargeDef.get("chargeTimeType") ;
            String chargeTimeType = String.valueOf(chargeTimeTypeMap.get("code")) ;
            if(chargeTimeType.equals("chargeTimeType.activation")) {
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            }else if(chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assert.assertEquals("4.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            }else if(chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assert.assertEquals("1.0", String.valueOf(chargeDef.get("amountOrPercentage")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("0", String.valueOf(chargeDef.get("amountPaid")));
            }else {
                Assert.fail("Other Charge defintion found");
            }
        }
        
        summaryMap = (Map<String, Object>) shareAccountData.get("summary");
        Assert.assertEquals("30", String.valueOf(summaryMap.get("totalApprovedShares")));
        Assert.assertEquals("15", String.valueOf(summaryMap.get("totalPendingForApprovalShares")));

        // Approve additional Shares request
        Map<String, List<Map<String, Object>>> approveadditionalsharesMap = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> idsMap = new HashMap<>();
        idsMap.put("id", addtionalSharesRequestId);
        list.add(idsMap);
        approveadditionalsharesMap.put("requestedShares", list);
        String approveadditionalsharesJson = new Gson().toJson(approveadditionalsharesMap);
        ShareAccountTransactionHelper.postCommand("approveadditionalshares", shareAccountId, approveadditionalsharesJson, requestSpec,
                responseSpec);

        shareAccountData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        transactions = (List<Map<String, Object>>) shareAccountData.get("purchasedShares");
        Assert.assertNotNull(transactions);
        Assert.assertEquals(3, transactions.size());
        for (int i = 0; i < transactions.size(); i++) {
            Map<String, Object> transaction = transactions.get(i);
            Map<String, Object> transactionTypeMap = (Map<String, Object>) transaction.get("type");
            dateList = (List<Integer>) transaction.get("purchasedDate");
            cal = Calendar.getInstance();
            cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
            Date date = cal.getTime();
            String transactionType = (String) transactionTypeMap.get("code");
            String transactionDate = simple.format(date);
            if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("02 Mar 2016")) {
                Assert.assertEquals("30", String.valueOf(transaction.get("numberOfShares")));
                Assert.assertEquals("62.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("62.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
            } else if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("01 Apr 2016")) {
                Assert.assertEquals("15", String.valueOf(transaction.get("numberOfShares")));
                Assert.assertEquals("32.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("32.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Map<String, Object> transactionstatusMap = (Map<String, Object>) transaction.get("status");
                Assert.assertEquals("purchasedSharesStatusType.approved", String.valueOf(transactionstatusMap.get("code")));

            } else if (transactionType.equals("charge.payment")) {
                Assert.assertEquals("2.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("0", String.valueOf(transaction.get("chargeAmount")));
                Assert.assertEquals("01 Jan 2016", transactionDate);
            }
        }

      //charges verification
        chargesList = (List<Map<String, Object>>) shareAccountData.get("charges") ;
        for(Map<String, Object> chargeDef: chargesList) {
            Map<String, Object> chargeTimeTypeMap = (Map<String, Object>) chargeDef.get("chargeTimeType") ;
            String chargeTimeType = String.valueOf(chargeTimeTypeMap.get("code")) ;
            if(chargeTimeType.equals("chargeTimeType.activation")) {
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            }else if(chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assert.assertEquals("4.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("4.0", String.valueOf(chargeDef.get("amountPaid")));
            }else if(chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assert.assertEquals("1.0", String.valueOf(chargeDef.get("amountOrPercentage")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("0", String.valueOf(chargeDef.get("amountPaid")));
            }else {
                Assert.fail("Other Charge defintion found");
            }
        }
        
        summaryMap = (Map<String, Object>) shareAccountData.get("summary");
        Assert.assertEquals("45", String.valueOf(summaryMap.get("totalApprovedShares")));
        Assert.assertEquals("0", String.valueOf(summaryMap.get("totalPendingForApprovalShares")));

        // apply aditional shres and reject it
        additionalSharesRequestMap = new HashMap<>();
        additionalSharesRequestMap.put("requestedDate", "01 May 2016");
        additionalSharesRequestMap.put("dateFormat", "dd MMMM yyyy");
        additionalSharesRequestMap.put("locale", "en");
        additionalSharesRequestMap.put("requestedShares", "20");
        additionalSharesRequestJson = new Gson().toJson(additionalSharesRequestMap);
        ShareAccountTransactionHelper.postCommand("applyadditionalshares", shareAccountId, additionalSharesRequestJson, requestSpec,
                responseSpec);
        shareAccountData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        transactions = (List<Map<String, Object>>) shareAccountData.get("purchasedShares");
        Assert.assertNotNull(transactions);
        Assert.assertEquals(4, transactions.size());
        addtionalSharesRequestId = null;
        for (int i = 0; i < transactions.size(); i++) {
            Map<String, Object> transaction = transactions.get(i);
            Map<String, Object> transactionTypeMap = (Map<String, Object>) transaction.get("type");
            dateList = (List<Integer>) transaction.get("purchasedDate");
            cal = Calendar.getInstance();
            cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
            Date date = cal.getTime();
            String transactionType = (String) transactionTypeMap.get("code");
            String transactionDate = simple.format(date);
            if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("01 May 2016")) {
                addtionalSharesRequestId = String.valueOf(transaction.get("id"));
                Assert.assertEquals("20", String.valueOf(transaction.get("numberOfShares")));
                Assert.assertEquals("42.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("40.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Map<String, Object> transactionstatusMap = (Map<String, Object>) transaction.get("status");
                Assert.assertEquals("purchasedSharesStatusType.applied", String.valueOf(transactionstatusMap.get("code")));
            }
        }

      //charges verification
        chargesList = (List<Map<String, Object>>) shareAccountData.get("charges") ;
        for(Map<String, Object> chargeDef: chargesList) {
            Map<String, Object> chargeTimeTypeMap = (Map<String, Object>) chargeDef.get("chargeTimeType") ;
            String chargeTimeType = String.valueOf(chargeTimeTypeMap.get("code")) ;
            if(chargeTimeType.equals("chargeTimeType.activation")) {
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            }else if(chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assert.assertEquals("6.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("4.0", String.valueOf(chargeDef.get("amountPaid")));
            }else if(chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assert.assertEquals("1.0", String.valueOf(chargeDef.get("amountOrPercentage")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("0", String.valueOf(chargeDef.get("amountPaid")));
            }else {
                Assert.fail("Other Charge defintion found");
            }
        }
        
        summaryMap = (Map<String, Object>) shareAccountData.get("summary");
        Assert.assertEquals("45", String.valueOf(summaryMap.get("totalApprovedShares")));
        Assert.assertEquals("20", String.valueOf(summaryMap.get("totalPendingForApprovalShares")));

        // rejectadditionalshares
        Map<String, List<Map<String, Object>>> rejectadditionalsharesMap = new HashMap<>();
        list = new ArrayList<>();
        idsMap = new HashMap<>();
        idsMap.put("id", addtionalSharesRequestId);
        list.add(idsMap);
        rejectadditionalsharesMap.put("requestedShares", list);
        String rejectadditionalsharesJson = new Gson().toJson(rejectadditionalsharesMap);
        ShareAccountTransactionHelper.postCommand("rejectadditionalshares", shareAccountId, rejectadditionalsharesJson, requestSpec,
                responseSpec);
        shareAccountData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        transactions = (List<Map<String, Object>>) shareAccountData.get("purchasedShares");
        Assert.assertNotNull(transactions);
        Assert.assertEquals(4, transactions.size());
        for (int i = 0; i < transactions.size(); i++) {
            Map<String, Object> transaction = transactions.get(i);
            Map<String, Object> transactionTypeMap = (Map<String, Object>) transaction.get("type");
            dateList = (List<Integer>) transaction.get("purchasedDate");
            cal = Calendar.getInstance();
            cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
            Date date = cal.getTime();
            String transactionType = (String) transactionTypeMap.get("code");
            String transactionDate = simple.format(date);
            if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("01 May 2016")) {
                addtionalSharesRequestId = String.valueOf(transaction.get("id"));
                Assert.assertEquals("20", String.valueOf(transaction.get("numberOfShares")));
                Assert.assertEquals("40.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("40.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Map<String, Object> transactionstatusMap = (Map<String, Object>) transaction.get("status");
                Assert.assertEquals("purchasedSharesStatusType.rejected", String.valueOf(transactionstatusMap.get("code")));
            }
        }

      //charges verification
        chargesList = (List<Map<String, Object>>) shareAccountData.get("charges") ;
        for(Map<String, Object> chargeDef: chargesList) {
            Map<String, Object> chargeTimeTypeMap = (Map<String, Object>) chargeDef.get("chargeTimeType") ;
            String chargeTimeType = String.valueOf(chargeTimeTypeMap.get("code")) ;
            if(chargeTimeType.equals("chargeTimeType.activation")) {
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            }else if(chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assert.assertEquals("6.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("6.0", String.valueOf(chargeDef.get("amountPaid")));
            }else if(chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assert.assertEquals("1.0", String.valueOf(chargeDef.get("amountOrPercentage")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("0", String.valueOf(chargeDef.get("amountPaid")));
            }else {
                Assert.fail("Other Charge defintion found");
            }
        }
        
        summaryMap = (Map<String, Object>) shareAccountData.get("summary");
        Assert.assertEquals("45", String.valueOf(summaryMap.get("totalApprovedShares")));
        Assert.assertEquals("0", String.valueOf(summaryMap.get("totalPendingForApprovalShares")));

        // redeem shares
        Map<String, Object> redeemRequestMap = new HashMap<>();
        redeemRequestMap.put("requestedDate", "05 May 2016");
        redeemRequestMap.put("dateFormat", "dd MMMM yyyy");
        redeemRequestMap.put("locale", "en");
        redeemRequestMap.put("requestedShares", "15");
        String redeemRequestJson = new Gson().toJson(redeemRequestMap);
        ShareAccountTransactionHelper.postCommand("redeemshares", shareAccountId, redeemRequestJson, requestSpec, responseSpec);
        shareAccountData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        transactions = (List<Map<String, Object>>) shareAccountData.get("purchasedShares");
        Assert.assertNotNull(transactions);
        Assert.assertEquals(5, transactions.size());
        for (int i = 0; i < transactions.size(); i++) {
            Map<String, Object> transaction = transactions.get(i);
            Map<String, Object> transactionTypeMap = (Map<String, Object>) transaction.get("type");
            dateList = (List<Integer>) transaction.get("purchasedDate");
            cal = Calendar.getInstance();
            cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
            Date date = cal.getTime();
            String transactionType = (String) transactionTypeMap.get("code");
            String transactionDate = simple.format(date);
            if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("02 Mar 2016")) {
                Assert.assertEquals("30", String.valueOf(transaction.get("numberOfShares")));
                Assert.assertEquals("62.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("62.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
            } else if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("01 Apr 2016")) {
                Assert.assertEquals("15", String.valueOf(transaction.get("numberOfShares")));
                Assert.assertEquals("32.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("32.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Map<String, Object> transactionstatusMap = (Map<String, Object>) transaction.get("status");
                Assert.assertEquals("purchasedSharesStatusType.approved", String.valueOf(transactionstatusMap.get("code")));
            } else if (transactionType.equals("purchasedSharesType.redeemed") && transactionDate.equals("05 May 2016")) {
                Assert.assertEquals("15", String.valueOf(transaction.get("numberOfShares")));
                Assert.assertEquals("29.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("29.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("1.0", String.valueOf(transaction.get("chargeAmount")));
                Map<String, Object> transactionstatusMap = (Map<String, Object>) transaction.get("status");
                Assert.assertEquals("purchasedSharesStatusType.approved", String.valueOf(transactionstatusMap.get("code")));
            } else if (transactionType.equals("charge.payment")) {
                Assert.assertEquals("2.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("0", String.valueOf(transaction.get("chargeAmount")));
                Assert.assertEquals("01 Jan 2016", transactionDate);
            }
        }

      //charges verification
        chargesList = (List<Map<String, Object>>) shareAccountData.get("charges") ;
        for(Map<String, Object> chargeDef: chargesList) {
            Map<String, Object> chargeTimeTypeMap = (Map<String, Object>) chargeDef.get("chargeTimeType") ;
            String chargeTimeType = String.valueOf(chargeTimeTypeMap.get("code")) ;
            if(chargeTimeType.equals("chargeTimeType.activation")) {
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            }else if(chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assert.assertEquals("6.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("6.0", String.valueOf(chargeDef.get("amountPaid")));
            }else if(chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assert.assertEquals("1.0", String.valueOf(chargeDef.get("amountOrPercentage")));
                Assert.assertEquals("1.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("1.0", String.valueOf(chargeDef.get("amountPaid")));
            }else {
                Assert.fail("Other Charge defintion found");
            }
        }
        summaryMap = (Map<String, Object>) shareAccountData.get("summary");
        Assert.assertEquals("30", String.valueOf(summaryMap.get("totalApprovedShares")));
        Assert.assertEquals("0", String.valueOf(summaryMap.get("totalPendingForApprovalShares")));
        
        //Close Share Account
        Map<String, Object> closeAccountMap = new HashMap<>();
        closeAccountMap.put("note", "Share Account Close Note");
        closeAccountMap.put("dateFormat", "dd MMMM yyyy");
        closeAccountMap.put("closedDate", "10 May 2016");
        closeAccountMap.put("locale", "en");
        String closeJson = new Gson().toJson(closeAccountMap);
        ShareAccountTransactionHelper.postCommand("close", shareAccountId, closeJson, requestSpec, responseSpec);
        shareAccountData = ShareAccountTransactionHelper
                .retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        statusMap = (Map<String, Object>) shareAccountData.get("status");
        Assert.assertEquals("shareAccountStatusType.closed", String.valueOf(statusMap.get("code")));
        transactions = (List<Map<String, Object>>) shareAccountData.get("purchasedShares");
        Assert.assertNotNull(transactions);
        Assert.assertEquals(6, transactions.size());
        for (int i = 0; i < transactions.size(); i++) {
            Map<String, Object> transaction = transactions.get(i);
            Map<String, Object> transactionTypeMap = (Map<String, Object>) transaction.get("type");
            dateList = (List<Integer>) transaction.get("purchasedDate");
            cal = Calendar.getInstance();
            cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
            Date date = cal.getTime();
            String transactionType = (String) transactionTypeMap.get("code");
            String transactionDate = simple.format(date);
            if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("02 Mar 2016")) {
                Assert.assertEquals("30", String.valueOf(transaction.get("numberOfShares")));
                Assert.assertEquals("62.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("62.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
            } else if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("01 Apr 2016")) {
                Assert.assertEquals("15", String.valueOf(transaction.get("numberOfShares")));
                Assert.assertEquals("32.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("32.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Map<String, Object> transactionstatusMap = (Map<String, Object>) transaction.get("status");
                Assert.assertEquals("purchasedSharesStatusType.approved", String.valueOf(transactionstatusMap.get("code")));
            } else if (transactionType.equals("purchasedSharesType.redeemed") && transactionDate.equals("05 May 2016")) {
                Assert.assertEquals("15", String.valueOf(transaction.get("numberOfShares")));
                Assert.assertEquals("29.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("29.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("1.0", String.valueOf(transaction.get("chargeAmount")));
                Map<String, Object> transactionstatusMap = (Map<String, Object>) transaction.get("status");
                Assert.assertEquals("purchasedSharesStatusType.approved", String.valueOf(transactionstatusMap.get("code")));
            }else if (transactionType.equals("purchasedSharesType.redeemed") && transactionDate.equals("10 May 2016")) {
                Assert.assertEquals("30", String.valueOf(transaction.get("numberOfShares")));
                Assert.assertEquals("59.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("59.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("1.0", String.valueOf(transaction.get("chargeAmount")));
                Map<String, Object> transactionstatusMap = (Map<String, Object>) transaction.get("status");
                Assert.assertEquals("purchasedSharesStatusType.approved", String.valueOf(transactionstatusMap.get("code")));
            }else if (transactionType.equals("charge.payment")) {
                Assert.assertEquals("2.0", String.valueOf(transaction.get("amount")));
                Assert.assertEquals("2.0", String.valueOf(transaction.get("amountPaid")));
                Assert.assertEquals("0", String.valueOf(transaction.get("chargeAmount")));
                Assert.assertEquals("01 Jan 2016", transactionDate);
            }
        }
      //charges verification
        chargesList = (List<Map<String, Object>>) shareAccountData.get("charges") ;
        for(Map<String, Object> chargeDef: chargesList) {
            Map<String, Object> chargeTimeTypeMap = (Map<String, Object>) chargeDef.get("chargeTimeType") ;
            String chargeTimeType = String.valueOf(chargeTimeTypeMap.get("code")) ;
            if(chargeTimeType.equals("chargeTimeType.activation")) {
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            }else if(chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assert.assertEquals("6.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("6.0", String.valueOf(chargeDef.get("amountPaid")));
            }else if(chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assert.assertEquals("1.0", String.valueOf(chargeDef.get("amountOrPercentage")));
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assert.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assert.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            }else {
                Assert.fail("Other Charge defintion found");
            }
        }
        summaryMap = (Map<String, Object>) shareAccountData.get("summary");
        Assert.assertEquals("0", String.valueOf(summaryMap.get("totalApprovedShares")));
        Assert.assertEquals("0", String.valueOf(summaryMap.get("totalPendingForApprovalShares")));
    }

    private Integer createShareProduct() {
        String shareProductJson = shareProductHelper.build();
        return ShareProductTransactionHelper.createShareProduct(shareProductJson, requestSpec, responseSpec);
    }

    private Integer createShareAccount(final Integer clientId, final Integer productId, final Integer savingsAccountId) {
        String josn = new ShareAccountHelper().withClientId(String.valueOf(clientId)).withProductId(String.valueOf(productId))
                .withExternalId("External1").withSavingsAccountId(String.valueOf(savingsAccountId)).withSubmittedDate("01 Jan 2016")
                .withApplicationDate("01 Jan 2016").withRequestedShares("25").build();
        return ShareAccountTransactionHelper.createShareAccount(josn, requestSpec, responseSpec);
    }

    private Integer createShareAccount(final Integer clientId, final Integer productId, final Integer savingsAccountId,
            List<Map<String, Object>> charges) {
        String josn = new ShareAccountHelper().withClientId(String.valueOf(clientId)).withProductId(String.valueOf(productId))
                .withExternalId("External1").withSavingsAccountId(String.valueOf(savingsAccountId)).withSubmittedDate("01 Jan 2016")
                .withApplicationDate("01 Jan 2016").withRequestedShares("25").withCharges(charges).build();
        return ShareAccountTransactionHelper.createShareAccount(josn, requestSpec, responseSpec);
    }

    private Map<String, Object> createCharge(final Integer chargeId, String amount) {
        Map<String, Object> map = new HashMap<>();
        map.put("chargeId", chargeId);
        map.put("amount", amount);
        return map;
    }
}
