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

import com.google.gson.Gson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShareAccountIntegrationTests {

    private static final Logger LOG = LoggerFactory.getLogger(ShareAccountIntegrationTests.class);
    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private ShareProductHelper shareProductHelper;

    @BeforeEach
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
        LOG.info("------------------------------CREATING NEW SHARE PRODUCT ---------------------------------------");
        shareProductHelper = new ShareProductHelper();
        final Integer shareProductId = createShareProduct();
        Assertions.assertNotNull(shareProductId);
        LOG.info("------------------------------CREATING SHARE PRODUCT COMPLETE---------------------------------------");

        LOG.info("------------------------------RETRIEVING SHARE PRODUCT---------------------------------------");
        Map<String, Object> shareProductData = ShareProductTransactionHelper.retrieveShareProduct(shareProductId, requestSpec,
                responseSpec);
        Assertions.assertNotNull(shareProductData);
        shareProductHelper.verifyShareProduct(shareProductData);

        LOG.info("------------------------------RETRIEVING SHARE PRODUCT COMPLETE---------------------------------------");

        LOG.info("------------------------------UPDATING SHARE PRODUCT---------------------------------------");

        Map<String, Object> shareProductDataForUpdate = new HashMap<>();

        shareProductDataForUpdate.put("totalShares", "2000");
        shareProductDataForUpdate.put("sharesIssued", "2000");

        String updateShareProductJsonString = new Gson().toJson(shareProductDataForUpdate);
        Integer updatedProductId = ShareProductTransactionHelper.updateShareProduct(shareProductId, updateShareProductJsonString,
                requestSpec, responseSpec);
        Assertions.assertNotNull(updatedProductId);
        Map<String, Object> updatedShareProductData = ShareProductTransactionHelper.retrieveShareProduct(updatedProductId, requestSpec,
                responseSpec);
        String updatedTotalShares = String.valueOf(updatedShareProductData.get("totalShares"));
        String updatedSharesIssued = String.valueOf(updatedShareProductData.get("totalSharesIssued"));
        Assertions.assertEquals("2000", updatedTotalShares);
        Assertions.assertEquals("2000", updatedSharesIssued);
        LOG.info("------------------------------UPDATING SHARE PRODUCT COMPLETE---------------------------------------");

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateShareAccount() {
        shareProductHelper = new ShareProductHelper();
        final Integer productId = createShareProduct();
        Assertions.assertNotNull(productId);
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientId);
        Integer savingsAccountId = SavingsAccountHelper.openSavingsAccount(requestSpec, responseSpec, clientId, "1000");
        Assertions.assertNotNull(savingsAccountId);
        final Integer shareAccountId = createShareAccount(clientId, productId, savingsAccountId);
        Assertions.assertNotNull(shareAccountId);
        Map<String, Object> shareProductData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec,
                responseSpec);
        Assertions.assertNotNull(shareProductData);

        Map<String, Object> shareAccountDataForUpdate = new HashMap<>();
        shareAccountDataForUpdate.put("requestedShares", 30);
        shareAccountDataForUpdate.put("applicationDate", "02 Mar 2016");
        shareAccountDataForUpdate.put("dateFormat", "dd MMMM yyyy");
        shareAccountDataForUpdate.put("locale", "en_GB");
        String updateShareAccountJsonString = new Gson().toJson(shareAccountDataForUpdate);
        ShareAccountTransactionHelper.updateShareAccount(shareAccountId, updateShareAccountJsonString, requestSpec, responseSpec);
        shareProductData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        List<Map<String, Object>> transactions = (List<Map<String, Object>>) shareProductData.get("purchasedShares");
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(1, transactions.size());
        Map<String, Object> transaction = transactions.get(0);
        Assertions.assertEquals("30", String.valueOf(transaction.get("numberOfShares")));
        Assertions.assertEquals("60.0", String.valueOf(transaction.get("amount")));
        Assertions.assertEquals("60.0", String.valueOf(transaction.get("amountPaid")));
        List<Integer> dateList = (List<Integer>) transaction.get("purchasedDate");
        Calendar cal = Calendar.getInstance();
        cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
        Date date = cal.getTime();
        DateFormat simple = new SimpleDateFormat("dd MMM yyyy");
        Assertions.assertEquals("02 Mar 2016", simple.format(date));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testShareAccountApproval() {
        DateFormat simple = new SimpleDateFormat("dd MMM yyyy");
        shareProductHelper = new ShareProductHelper();
        final Integer productId = createShareProduct();
        Assertions.assertNotNull(productId);
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientId);
        Integer savingsAccountId = SavingsAccountHelper.openSavingsAccount(requestSpec, responseSpec, clientId, "1000");
        Assertions.assertNotNull(savingsAccountId);
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
        Assertions.assertNotNull(shareAccountId);
        Map<String, Object> shareAccountData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec,
                responseSpec);
        Assertions.assertNotNull(shareAccountData);

        // Approve share Account
        Map<String, Object> approveMap = new HashMap<>();
        approveMap.put("note", "Share Account Approval Note");
        approveMap.put("dateFormat", "dd MMMM yyyy");
        approveMap.put("approvedDate", "01 Jan 2016");
        approveMap.put("locale", "en");
        String approve = new Gson().toJson(approveMap);
        ShareAccountTransactionHelper.postCommand("approve", shareAccountId, approve, requestSpec, responseSpec);
        shareAccountData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        Map<String, Object> statusMap = (Map<String, Object>) shareAccountData.get("status");
        Assertions.assertEquals("shareAccountStatusType.approved", String.valueOf(statusMap.get("code")));
        Map<String, Object> timelineMap = (Map<String, Object>) shareAccountData.get("timeline");
        List<Integer> dateList = (List<Integer>) timelineMap.get("approvedDate");
        Calendar cal = Calendar.getInstance();
        cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
        Date approvedDate = cal.getTime();
        Assertions.assertEquals("01 Jan 2016", simple.format(approvedDate));
        List<Map<String, Object>> transactions = (List<Map<String, Object>>) shareAccountData.get("purchasedShares");
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(2, transactions.size());
        for (int i = 0; i < transactions.size(); i++) {
            Map<String, Object> transaction = transactions.get(i);
            Map<String, Object> transactionTypeMap = (Map<String, Object>) transaction.get("type");
            dateList = (List<Integer>) transaction.get("purchasedDate");
            cal = Calendar.getInstance();
            cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
            Date date = cal.getTime();
            String transactionType = (String) transactionTypeMap.get("code");
            if (transactionType.equals("purchasedSharesType.purchased")) {
                Assertions.assertEquals("25", String.valueOf(transaction.get("numberOfShares")));
                Assertions.assertEquals("52.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("52.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Assertions.assertEquals("01 Jan 2016", simple.format(date));
            } else if (transactionType.equals("charge.payment")) {
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals(
                        simple.format(
                                Date.from(Utils.getLocalDateOfTenant().atStartOfDay(DateUtils.getDateTimeZoneOfTenant()).toInstant())),
                        simple.format(date));
            }
        }

        Map<String, Object> summaryMap = (Map<String, Object>) shareAccountData.get("summary");
        Assertions.assertEquals("25", String.valueOf(summaryMap.get("totalApprovedShares")));
        Assertions.assertEquals("0", String.valueOf(summaryMap.get("totalPendingForApprovalShares")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void rejectShareAccount() {
        shareProductHelper = new ShareProductHelper();
        final Integer productId = createShareProduct();
        Assertions.assertNotNull(productId);
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientId);
        Integer savingsAccountId = SavingsAccountHelper.openSavingsAccount(requestSpec, responseSpec, clientId, "1000");
        Assertions.assertNotNull(savingsAccountId);
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
        Assertions.assertNotNull(shareAccountId);
        Map<String, Object> shareAccountData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec,
                responseSpec);
        Assertions.assertNotNull(shareAccountData);

        // Reject share Account
        Map<String, Object> rejectMap = new HashMap<>();
        rejectMap.put("note", "Share Account Rejection Note");
        String rejectJson = new Gson().toJson(rejectMap);
        ShareAccountTransactionHelper.postCommand("reject", shareAccountId, rejectJson, requestSpec, responseSpec);
        shareAccountData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        DateFormat simple = new SimpleDateFormat("dd MMM yyyy");
        Map<String, Object> statusMap = (Map<String, Object>) shareAccountData.get("status");
        Assertions.assertEquals("shareAccountStatusType.rejected", String.valueOf(statusMap.get("code")));
        Map<String, Object> timelineMap = (Map<String, Object>) shareAccountData.get("timeline");
        List<Integer> dateList = (List<Integer>) timelineMap.get("rejectedDate");
        Calendar cal = Calendar.getInstance();
        cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
        Date rejectedDate = cal.getTime();
        Assertions.assertEquals(
                simple.format(Date.from(Utils.getLocalDateOfTenant().atStartOfDay(DateUtils.getDateTimeZoneOfTenant()).toInstant())),
                simple.format(rejectedDate));

        List<Map<String, Object>> transactions = (List<Map<String, Object>>) shareAccountData.get("purchasedShares");
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(2, transactions.size());
        for (int i = 0; i < transactions.size(); i++) {
            Map<String, Object> transaction = transactions.get(i);
            Map<String, Object> transactionTypeMap = (Map<String, Object>) transaction.get("type");
            dateList = (List<Integer>) transaction.get("purchasedDate");
            cal = Calendar.getInstance();
            cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
            Date date = cal.getTime();
            String transactionType = (String) transactionTypeMap.get("code");
            if (transactionType.equals("purchasedSharesType.purchased")) {
                Assertions.assertEquals("25", String.valueOf(transaction.get("numberOfShares")));
                Assertions.assertEquals("50.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("50.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Assertions.assertEquals("01 Jan 2016", simple.format(date));
            } else if (transactionType.equals("charge.payment")) {
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("0", String.valueOf(transaction.get("amountPaid")));
                Date transactionDate = Date
                        .from(Utils.getLocalDateOfTenant().atStartOfDay(DateUtils.getDateTimeZoneOfTenant()).toInstant());
                Assertions.assertEquals(simple.format(transactionDate), simple.format(date));
            }
        }

        Map<String, Object> summaryMap = (Map<String, Object>) shareAccountData.get("summary");
        Assertions.assertEquals("0", String.valueOf(summaryMap.get("totalApprovedShares")));
        Assertions.assertEquals("0", String.valueOf(summaryMap.get("totalPendingForApprovalShares")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testShareAccountUndoApproval() {
        DateFormat simple = new SimpleDateFormat("dd MMM yyyy");
        shareProductHelper = new ShareProductHelper();
        final Integer productId = createShareProduct();
        Assertions.assertNotNull(productId);
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientId);
        Integer savingsAccountId = SavingsAccountHelper.openSavingsAccount(requestSpec, responseSpec, clientId, "1000");
        Assertions.assertNotNull(savingsAccountId);
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
        Assertions.assertNotNull(shareAccountId);
        Map<String, Object> shareAccountData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec,
                responseSpec);
        Assertions.assertNotNull(shareAccountData);

        // Approve share Account
        Map<String, Object> approveMap = new HashMap<>();
        approveMap.put("note", "Share Account Approval Note");
        approveMap.put("dateFormat", "dd MMMM yyyy");
        approveMap.put("approvedDate", "01 Jan 2016");
        approveMap.put("locale", "en");
        String approve = new Gson().toJson(approveMap);
        ShareAccountTransactionHelper.postCommand("approve", shareAccountId, approve, requestSpec, responseSpec);
        shareAccountData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        Map<String, Object> statusMap = (Map<String, Object>) shareAccountData.get("status");
        Assertions.assertEquals("shareAccountStatusType.approved", String.valueOf(statusMap.get("code")));
        Map<String, Object> timelineMap = (Map<String, Object>) shareAccountData.get("timeline");
        List<Integer> dateList = (List<Integer>) timelineMap.get("approvedDate");
        Calendar cal = Calendar.getInstance();
        cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
        Date approvedDate = cal.getTime();
        Assertions.assertEquals("01 Jan 2016", simple.format(approvedDate));

        // Undo Approval share Account
        Map<String, Object> undoApprovalMap = new HashMap<>();
        String undoApprovalJson = new Gson().toJson(undoApprovalMap);
        ShareAccountTransactionHelper.postCommand("undoapproval", shareAccountId, undoApprovalJson, requestSpec, responseSpec);

        shareAccountData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec, responseSpec);

        statusMap = (Map<String, Object>) shareAccountData.get("status");
        Assertions.assertEquals("shareAccountStatusType.submitted.and.pending.approval", String.valueOf(statusMap.get("code")));

        List<Map<String, Object>> transactions = (List<Map<String, Object>>) shareAccountData.get("purchasedShares");
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(2, transactions.size());
        for (int i = 0; i < transactions.size(); i++) {
            Map<String, Object> transaction = transactions.get(i);
            Map<String, Object> transactionTypeMap = (Map<String, Object>) transaction.get("type");
            dateList = (List<Integer>) transaction.get("purchasedDate");
            cal = Calendar.getInstance();
            cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
            Date date = cal.getTime();
            String transactionType = (String) transactionTypeMap.get("code");
            if (transactionType.equals("purchasedSharesType.purchased")) {
                Assertions.assertEquals("25", String.valueOf(transaction.get("numberOfShares")));
                Assertions.assertEquals("52.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Assertions.assertEquals("01 Jan 2016", simple.format(date));
            } else if (transactionType.equals("charge.payment")) {
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals(
                        simple.format(
                                Date.from(Utils.getLocalDateOfTenant().atStartOfDay(DateUtils.getDateTimeZoneOfTenant()).toInstant())),
                        simple.format(date));
            }
        }

        Map<String, Object> summaryMap = (Map<String, Object>) shareAccountData.get("summary");
        Assertions.assertEquals("0", String.valueOf(summaryMap.get("totalApprovedShares")));
        Assertions.assertEquals("25", String.valueOf(summaryMap.get("totalPendingForApprovalShares")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateShareAccountWithCharges() {
        shareProductHelper = new ShareProductHelper();
        final Integer productId = createShareProduct();
        Assertions.assertNotNull(productId);
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(clientId);
        Integer savingsAccountId = SavingsAccountHelper.openSavingsAccount(requestSpec, responseSpec, clientId, "1000");
        Assertions.assertNotNull(savingsAccountId);
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
        Assertions.assertNotNull(shareAccountId);
        Map<String, Object> shareAccountData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec,
                responseSpec);
        Assertions.assertNotNull(shareAccountData);

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
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(2, transactions.size());
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
                Assertions.assertEquals("30", String.valueOf(transaction.get("numberOfShares")));
                Assertions.assertEquals("62.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("60.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Assertions.assertEquals("02 Mar 2016", simple.format(date));
            } else if (transactionType.equals("charge.payment")) {
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("0", String.valueOf(transaction.get("chargeAmount")));
            }
        }

        // charges verification
        List<Map<String, Object>> chargesList = (List<Map<String, Object>>) shareAccountData.get("charges");
        for (Map<String, Object> chargeDef : chargesList) {
            Map<String, Object> chargeTimeTypeMap = (Map<String, Object>) chargeDef.get("chargeTimeType");
            String chargeTimeType = String.valueOf(chargeTimeTypeMap.get("code"));
            if (chargeTimeType.equals("chargeTimeType.activation")) {
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("0", String.valueOf(chargeDef.get("amountPaid")));
            } else if (chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("0", String.valueOf(chargeDef.get("amountPaid")));
            } else if (chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assertions.assertEquals("1.0", String.valueOf(chargeDef.get("amountOrPercentage")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("0", String.valueOf(chargeDef.get("amountPaid")));
            } else {
                Assertions.fail("Other Charge defintion found");
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
        shareAccountData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        Map<String, Object> statusMap = (Map<String, Object>) shareAccountData.get("status");
        Assertions.assertEquals("shareAccountStatusType.approved", String.valueOf(statusMap.get("code")));
        Map<String, Object> timelineMap = (Map<String, Object>) shareAccountData.get("timeline");
        List<Integer> dateList = (List<Integer>) timelineMap.get("approvedDate");
        Calendar cal = Calendar.getInstance();
        cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
        Date approvedDate = cal.getTime();
        Assertions.assertEquals("01 Jan 2016", simple.format(approvedDate));

        // charges verification
        chargesList = (List<Map<String, Object>>) shareAccountData.get("charges");
        for (Map<String, Object> chargeDef : chargesList) {
            Map<String, Object> chargeTimeTypeMap = (Map<String, Object>) chargeDef.get("chargeTimeType");
            String chargeTimeType = String.valueOf(chargeTimeTypeMap.get("code"));
            if (chargeTimeType.equals("chargeTimeType.activation")) {
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("0", String.valueOf(chargeDef.get("amountPaid")));
            } else if (chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            } else if (chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assertions.assertEquals("1.0", String.valueOf(chargeDef.get("amountOrPercentage")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("0", String.valueOf(chargeDef.get("amountPaid")));
            } else {
                Assertions.fail("Other Charge defintion found");
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
        Assertions.assertEquals("shareAccountStatusType.active", String.valueOf(statusMap.get("code")));
        timelineMap = (Map<String, Object>) shareAccountData.get("timeline");
        dateList = (List<Integer>) timelineMap.get("activatedDate");
        cal = Calendar.getInstance();
        cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
        Date activatedDate = cal.getTime();
        Assertions.assertEquals("01 Jan 2016", simple.format(activatedDate));

        transactions = (List<Map<String, Object>>) shareAccountData.get("purchasedShares");
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(2, transactions.size());
        for (int i = 0; i < transactions.size(); i++) {
            Map<String, Object> transaction = transactions.get(i);
            Map<String, Object> transactionTypeMap = (Map<String, Object>) transaction.get("type");
            dateList = (List<Integer>) transaction.get("purchasedDate");
            cal = Calendar.getInstance();
            cal.set(dateList.get(0), dateList.get(1) - 1, dateList.get(2));
            Date date = cal.getTime();
            String transactionType = (String) transactionTypeMap.get("code");
            if (transactionType.equals("purchasedSharesType.purchased")) {
                Assertions.assertEquals("30", String.valueOf(transaction.get("numberOfShares")));
                Assertions.assertEquals("62.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("62.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Assertions.assertEquals("02 Mar 2016", simple.format(date));
            } else if (transactionType.equals("charge.payment")) {
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("0", String.valueOf(transaction.get("chargeAmount")));
                Assertions.assertEquals("01 Jan 2016", simple.format(date));
            }
        }

        // charges verification
        chargesList = (List<Map<String, Object>>) shareAccountData.get("charges");
        for (Map<String, Object> chargeDef : chargesList) {
            Map<String, Object> chargeTimeTypeMap = (Map<String, Object>) chargeDef.get("chargeTimeType");
            String chargeTimeType = String.valueOf(chargeTimeTypeMap.get("code"));
            if (chargeTimeType.equals("chargeTimeType.activation")) {
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            } else if (chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            } else if (chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assertions.assertEquals("1.0", String.valueOf(chargeDef.get("amountOrPercentage")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("0", String.valueOf(chargeDef.get("amountPaid")));
            } else {
                Assertions.fail("Other Charge defintion found");
            }
        }

        Map<String, Object> summaryMap = (Map<String, Object>) shareAccountData.get("summary");
        Assertions.assertEquals("30", String.valueOf(summaryMap.get("totalApprovedShares")));
        Assertions.assertEquals("0", String.valueOf(summaryMap.get("totalPendingForApprovalShares")));

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
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(3, transactions.size());
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
                Assertions.assertEquals("30", String.valueOf(transaction.get("numberOfShares")));
                Assertions.assertEquals("62.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("62.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
            } else if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("01 Apr 2016")) {
                addtionalSharesRequestId = String.valueOf(transaction.get("id"));
                Assertions.assertEquals("15", String.valueOf(transaction.get("numberOfShares")));
                Assertions.assertEquals("32.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("30.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Map<String, Object> transactionstatusMap = (Map<String, Object>) transaction.get("status");
                Assertions.assertEquals("purchasedSharesStatusType.applied", String.valueOf(transactionstatusMap.get("code")));

            } else if (transactionType.equals("charge.payment")) {
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("0", String.valueOf(transaction.get("chargeAmount")));
                Assertions.assertEquals("01 Jan 2016", transactionDate);
            }
        }

        // charges verification
        chargesList = (List<Map<String, Object>>) shareAccountData.get("charges");
        for (Map<String, Object> chargeDef : chargesList) {
            Map<String, Object> chargeTimeTypeMap = (Map<String, Object>) chargeDef.get("chargeTimeType");
            String chargeTimeType = String.valueOf(chargeTimeTypeMap.get("code"));
            if (chargeTimeType.equals("chargeTimeType.activation")) {
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            } else if (chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assertions.assertEquals("4.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            } else if (chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assertions.assertEquals("1.0", String.valueOf(chargeDef.get("amountOrPercentage")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("0", String.valueOf(chargeDef.get("amountPaid")));
            } else {
                Assertions.fail("Other Charge defintion found");
            }
        }

        summaryMap = (Map<String, Object>) shareAccountData.get("summary");
        Assertions.assertEquals("30", String.valueOf(summaryMap.get("totalApprovedShares")));
        Assertions.assertEquals("15", String.valueOf(summaryMap.get("totalPendingForApprovalShares")));

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
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(3, transactions.size());
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
                Assertions.assertEquals("30", String.valueOf(transaction.get("numberOfShares")));
                Assertions.assertEquals("62.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("62.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
            } else if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("01 Apr 2016")) {
                Assertions.assertEquals("15", String.valueOf(transaction.get("numberOfShares")));
                Assertions.assertEquals("32.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("32.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Map<String, Object> transactionstatusMap = (Map<String, Object>) transaction.get("status");
                Assertions.assertEquals("purchasedSharesStatusType.approved", String.valueOf(transactionstatusMap.get("code")));

            } else if (transactionType.equals("charge.payment")) {
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("0", String.valueOf(transaction.get("chargeAmount")));
                Assertions.assertEquals("01 Jan 2016", transactionDate);
            }
        }

        // charges verification
        chargesList = (List<Map<String, Object>>) shareAccountData.get("charges");
        for (Map<String, Object> chargeDef : chargesList) {
            Map<String, Object> chargeTimeTypeMap = (Map<String, Object>) chargeDef.get("chargeTimeType");
            String chargeTimeType = String.valueOf(chargeTimeTypeMap.get("code"));
            if (chargeTimeType.equals("chargeTimeType.activation")) {
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            } else if (chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assertions.assertEquals("4.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("4.0", String.valueOf(chargeDef.get("amountPaid")));
            } else if (chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assertions.assertEquals("1.0", String.valueOf(chargeDef.get("amountOrPercentage")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("0", String.valueOf(chargeDef.get("amountPaid")));
            } else {
                Assertions.fail("Other Charge defintion found");
            }
        }

        summaryMap = (Map<String, Object>) shareAccountData.get("summary");
        Assertions.assertEquals("45", String.valueOf(summaryMap.get("totalApprovedShares")));
        Assertions.assertEquals("0", String.valueOf(summaryMap.get("totalPendingForApprovalShares")));

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
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(4, transactions.size());
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
                Assertions.assertEquals("20", String.valueOf(transaction.get("numberOfShares")));
                Assertions.assertEquals("42.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("40.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Map<String, Object> transactionstatusMap = (Map<String, Object>) transaction.get("status");
                Assertions.assertEquals("purchasedSharesStatusType.applied", String.valueOf(transactionstatusMap.get("code")));
            }
        }

        // charges verification
        chargesList = (List<Map<String, Object>>) shareAccountData.get("charges");
        for (Map<String, Object> chargeDef : chargesList) {
            Map<String, Object> chargeTimeTypeMap = (Map<String, Object>) chargeDef.get("chargeTimeType");
            String chargeTimeType = String.valueOf(chargeTimeTypeMap.get("code"));
            if (chargeTimeType.equals("chargeTimeType.activation")) {
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            } else if (chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assertions.assertEquals("6.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("4.0", String.valueOf(chargeDef.get("amountPaid")));
            } else if (chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assertions.assertEquals("1.0", String.valueOf(chargeDef.get("amountOrPercentage")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("0", String.valueOf(chargeDef.get("amountPaid")));
            } else {
                Assertions.fail("Other Charge defintion found");
            }
        }

        summaryMap = (Map<String, Object>) shareAccountData.get("summary");
        Assertions.assertEquals("45", String.valueOf(summaryMap.get("totalApprovedShares")));
        Assertions.assertEquals("20", String.valueOf(summaryMap.get("totalPendingForApprovalShares")));

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
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(4, transactions.size());
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
                Assertions.assertEquals("20", String.valueOf(transaction.get("numberOfShares")));
                Assertions.assertEquals("40.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("40.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Map<String, Object> transactionstatusMap = (Map<String, Object>) transaction.get("status");
                Assertions.assertEquals("purchasedSharesStatusType.rejected", String.valueOf(transactionstatusMap.get("code")));
            }
        }

        // charges verification
        chargesList = (List<Map<String, Object>>) shareAccountData.get("charges");
        for (Map<String, Object> chargeDef : chargesList) {
            Map<String, Object> chargeTimeTypeMap = (Map<String, Object>) chargeDef.get("chargeTimeType");
            String chargeTimeType = String.valueOf(chargeTimeTypeMap.get("code"));
            if (chargeTimeType.equals("chargeTimeType.activation")) {
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            } else if (chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assertions.assertEquals("6.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("6.0", String.valueOf(chargeDef.get("amountPaid")));
            } else if (chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assertions.assertEquals("1.0", String.valueOf(chargeDef.get("amountOrPercentage")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("0", String.valueOf(chargeDef.get("amountPaid")));
            } else {
                Assertions.fail("Other Charge defintion found");
            }
        }

        summaryMap = (Map<String, Object>) shareAccountData.get("summary");
        Assertions.assertEquals("45", String.valueOf(summaryMap.get("totalApprovedShares")));
        Assertions.assertEquals("0", String.valueOf(summaryMap.get("totalPendingForApprovalShares")));

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
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(5, transactions.size());
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
                Assertions.assertEquals("30", String.valueOf(transaction.get("numberOfShares")));
                Assertions.assertEquals("62.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("62.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
            } else if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("01 Apr 2016")) {
                Assertions.assertEquals("15", String.valueOf(transaction.get("numberOfShares")));
                Assertions.assertEquals("32.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("32.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Map<String, Object> transactionstatusMap = (Map<String, Object>) transaction.get("status");
                Assertions.assertEquals("purchasedSharesStatusType.approved", String.valueOf(transactionstatusMap.get("code")));
            } else if (transactionType.equals("purchasedSharesType.redeemed") && transactionDate.equals("05 May 2016")) {
                Assertions.assertEquals("15", String.valueOf(transaction.get("numberOfShares")));
                Assertions.assertEquals("29.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("29.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("1.0", String.valueOf(transaction.get("chargeAmount")));
                Map<String, Object> transactionstatusMap = (Map<String, Object>) transaction.get("status");
                Assertions.assertEquals("purchasedSharesStatusType.approved", String.valueOf(transactionstatusMap.get("code")));
            } else if (transactionType.equals("charge.payment")) {
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("0", String.valueOf(transaction.get("chargeAmount")));
                Assertions.assertEquals("01 Jan 2016", transactionDate);
            }
        }

        // charges verification
        chargesList = (List<Map<String, Object>>) shareAccountData.get("charges");
        for (Map<String, Object> chargeDef : chargesList) {
            Map<String, Object> chargeTimeTypeMap = (Map<String, Object>) chargeDef.get("chargeTimeType");
            String chargeTimeType = String.valueOf(chargeTimeTypeMap.get("code"));
            if (chargeTimeType.equals("chargeTimeType.activation")) {
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            } else if (chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assertions.assertEquals("6.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("6.0", String.valueOf(chargeDef.get("amountPaid")));
            } else if (chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assertions.assertEquals("1.0", String.valueOf(chargeDef.get("amountOrPercentage")));
                Assertions.assertEquals("1.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("1.0", String.valueOf(chargeDef.get("amountPaid")));
            } else {
                Assertions.fail("Other Charge defintion found");
            }
        }
        summaryMap = (Map<String, Object>) shareAccountData.get("summary");
        Assertions.assertEquals("30", String.valueOf(summaryMap.get("totalApprovedShares")));
        Assertions.assertEquals("0", String.valueOf(summaryMap.get("totalPendingForApprovalShares")));

        // Close Share Account
        Map<String, Object> closeAccountMap = new HashMap<>();
        closeAccountMap.put("note", "Share Account Close Note");
        closeAccountMap.put("dateFormat", "dd MMMM yyyy");
        closeAccountMap.put("closedDate", "10 May 2016");
        closeAccountMap.put("locale", "en");
        String closeJson = new Gson().toJson(closeAccountMap);
        ShareAccountTransactionHelper.postCommand("close", shareAccountId, closeJson, requestSpec, responseSpec);
        shareAccountData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec, responseSpec);
        statusMap = (Map<String, Object>) shareAccountData.get("status");
        Assertions.assertEquals("shareAccountStatusType.closed", String.valueOf(statusMap.get("code")));
        transactions = (List<Map<String, Object>>) shareAccountData.get("purchasedShares");
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(6, transactions.size());
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
                Assertions.assertEquals("30", String.valueOf(transaction.get("numberOfShares")));
                Assertions.assertEquals("62.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("62.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
            } else if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("01 Apr 2016")) {
                Assertions.assertEquals("15", String.valueOf(transaction.get("numberOfShares")));
                Assertions.assertEquals("32.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("32.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("chargeAmount")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("purchasedPrice")));
                Map<String, Object> transactionstatusMap = (Map<String, Object>) transaction.get("status");
                Assertions.assertEquals("purchasedSharesStatusType.approved", String.valueOf(transactionstatusMap.get("code")));
            } else if (transactionType.equals("purchasedSharesType.redeemed") && transactionDate.equals("05 May 2016")) {
                Assertions.assertEquals("15", String.valueOf(transaction.get("numberOfShares")));
                Assertions.assertEquals("29.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("29.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("1.0", String.valueOf(transaction.get("chargeAmount")));
                Map<String, Object> transactionstatusMap = (Map<String, Object>) transaction.get("status");
                Assertions.assertEquals("purchasedSharesStatusType.approved", String.valueOf(transactionstatusMap.get("code")));
            } else if (transactionType.equals("purchasedSharesType.redeemed") && transactionDate.equals("10 May 2016")) {
                Assertions.assertEquals("30", String.valueOf(transaction.get("numberOfShares")));
                Assertions.assertEquals("59.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("59.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("1.0", String.valueOf(transaction.get("chargeAmount")));
                Map<String, Object> transactionstatusMap = (Map<String, Object>) transaction.get("status");
                Assertions.assertEquals("purchasedSharesStatusType.approved", String.valueOf(transactionstatusMap.get("code")));
            } else if (transactionType.equals("charge.payment")) {
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("amount")));
                Assertions.assertEquals("2.0", String.valueOf(transaction.get("amountPaid")));
                Assertions.assertEquals("0", String.valueOf(transaction.get("chargeAmount")));
                Assertions.assertEquals("01 Jan 2016", transactionDate);
            }
        }
        // charges verification
        chargesList = (List<Map<String, Object>>) shareAccountData.get("charges");
        for (Map<String, Object> chargeDef : chargesList) {
            Map<String, Object> chargeTimeTypeMap = (Map<String, Object>) chargeDef.get("chargeTimeType");
            String chargeTimeType = String.valueOf(chargeTimeTypeMap.get("code"));
            if (chargeTimeType.equals("chargeTimeType.activation")) {
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            } else if (chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assertions.assertEquals("6.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("6.0", String.valueOf(chargeDef.get("amountPaid")));
            } else if (chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assertions.assertEquals("1.0", String.valueOf(chargeDef.get("amountOrPercentage")));
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amount")));
                Assertions.assertEquals("0.0", String.valueOf(chargeDef.get("amountOutstanding")));
                Assertions.assertEquals("2.0", String.valueOf(chargeDef.get("amountPaid")));
            } else {
                Assertions.fail("Other Charge defintion found");
            }
        }
        summaryMap = (Map<String, Object>) shareAccountData.get("summary");
        Assertions.assertEquals("0", String.valueOf(summaryMap.get("totalApprovedShares")));
        Assertions.assertEquals("0", String.valueOf(summaryMap.get("totalPendingForApprovalShares")));
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
