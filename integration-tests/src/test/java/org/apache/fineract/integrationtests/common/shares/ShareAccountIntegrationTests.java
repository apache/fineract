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

import static org.apache.fineract.integrationtests.client.feign.modules.FeignErrors.errorGlobalisationCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AccountChargesRequest;
import org.apache.fineract.client.models.AccountRequest;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.GetAccountsCharges;
import org.apache.fineract.client.models.GetAccountsPurchasedShares;
import org.apache.fineract.client.models.GetAccountsSummary;
import org.apache.fineract.client.models.GetAccountsTypeAccountIdResponse;
import org.apache.fineract.client.models.GetProductsTypeProductIdResponse;
import org.apache.fineract.client.models.PostProductsTypeRequest;
import org.apache.fineract.client.models.PutAccountsTypeAccountIdRequest;
import org.apache.fineract.client.models.PutProductsTypeProductIdRequest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignChargesHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignShareAccountHelper;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.portfolio.charge.domain.ChargeAppliesTo;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShareAccountIntegrationTests {

    private static final Logger LOG = LoggerFactory.getLogger(ShareAccountIntegrationTests.class);
    private static final String DATE_FORMAT = "dd MMMM yyyy";

    private final FeignClientHelper clientHelper = new FeignClientHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignSavingsHelper savingsHelper = new FeignSavingsHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignSavingsProductHelper savingsProductHelper = new FeignSavingsProductHelper(
            FineractFeignClientHelper.getFineractFeignClient());
    private final FeignChargesHelper chargesHelper = new FeignChargesHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignShareAccountHelper shareAccountHelper = new FeignShareAccountHelper(
            FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void testCreateShareProduct() {
        // This method will check create share product, get share product,
        // update share product.
        LOG.info("------------------------------CREATING NEW SHARE PRODUCT ---------------------------------------");
        final PostProductsTypeRequest shareProductRequest = FeignShareAccountHelper.defaultShareProduct();
        final Long shareProductId = shareAccountHelper.createShareProduct(shareProductRequest);
        Assertions.assertNotNull(shareProductId);
        LOG.info("------------------------------CREATING SHARE PRODUCT COMPLETE---------------------------------------");

        LOG.info("------------------------------RETRIEVING SHARE PRODUCT---------------------------------------");
        GetProductsTypeProductIdResponse shareProductData = shareAccountHelper.getShareProduct(shareProductId);
        Assertions.assertNotNull(shareProductData);
        verifyShareProduct(shareProductRequest, shareProductData);

        LOG.info("------------------------------RETRIEVING SHARE PRODUCT COMPLETE---------------------------------------");

        LOG.info("------------------------------UPDATING SHARE PRODUCT---------------------------------------");

        Long updatedProductId = shareAccountHelper.updateShareProduct(shareProductId,
                new PutProductsTypeProductIdRequest().totalShares(2000).sharesIssued(2000));
        Assertions.assertNotNull(updatedProductId);
        GetProductsTypeProductIdResponse updatedShareProductData = shareAccountHelper.getShareProduct(updatedProductId);
        Assertions.assertEquals(2000, updatedShareProductData.getTotalShares());
        Assertions.assertEquals(2000, updatedShareProductData.getTotalSharesIssued());
        LOG.info("------------------------------UPDATING SHARE PRODUCT COMPLETE---------------------------------------");

    }

    @Test
    public void testCreateShareAccount() {
        final Long productId = createShareProduct();
        Assertions.assertNotNull(productId);
        final Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);
        final Long savingsAccountId = openSavingsAccount(clientId);
        Assertions.assertNotNull(savingsAccountId);
        final Long shareAccountId = createShareAccount(clientId, productId, savingsAccountId);
        Assertions.assertNotNull(shareAccountId);
        GetAccountsTypeAccountIdResponse shareProductData = shareAccountHelper.getShareAccount(shareAccountId);
        Assertions.assertNotNull(shareProductData);

        shareAccountHelper.updateShareAccount(shareAccountId, new PutAccountsTypeAccountIdRequest().requestedShares(30)
                .applicationDate("02 March 2016").dateFormat(DATE_FORMAT).locale("en_GB"));
        shareProductData = shareAccountHelper.getShareAccount(shareAccountId);
        List<GetAccountsPurchasedShares> transactions = new ArrayList<>(shareProductData.getPurchasedShares());
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(1, transactions.size());
        GetAccountsPurchasedShares transaction = transactions.get(0);
        Assertions.assertEquals(30, transaction.getNumberOfShares());
        Assertions.assertEquals(60.0, transaction.getAmount());
        Assertions.assertEquals(60.0, transaction.getAmountPaid());
        Assertions.assertEquals("02 March 2016", transaction.getPurchasedDate().format(Utils.dateFormatter));
    }

    @Test
    public void testShareAccountApproval() {
        final Long productId = createShareProduct();
        Assertions.assertNotNull(productId);
        final Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);
        final Long savingsAccountId = openSavingsAccount(clientId);
        Assertions.assertNotNull(savingsAccountId);
        final Long activationChargeId = createShareCharge(ChargeTimeType.SHAREACCOUNT_ACTIVATION);
        final Long purchaseChargeId = createShareCharge(ChargeTimeType.SHARE_PURCHASE);
        final Long redeemChargeId = createShareCharge(ChargeTimeType.SHARE_REDEEM);
        final List<AccountChargesRequest> charges = List.of(accountCharge(activationChargeId, 2), accountCharge(purchaseChargeId, 2),
                accountCharge(redeemChargeId, 1));
        final Long shareAccountId = createShareAccount(clientId, productId, savingsAccountId, charges);
        Assertions.assertNotNull(shareAccountId);
        GetAccountsTypeAccountIdResponse shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        Assertions.assertNotNull(shareAccountData);

        // Approve share Account
        shareAccountHelper.approve(shareAccountId, "01 January 2016", "Share Account Approval Note", DATE_FORMAT, "en");
        shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        Assertions.assertEquals("shareAccountStatusType.approved", shareAccountData.getStatus().getCode());
        LocalDate approvedDate = shareAccountData.getTimeline().getApprovedDate();
        Assertions.assertEquals("01 January 2016", approvedDate.format(Utils.dateFormatter));
        List<GetAccountsPurchasedShares> transactions = new ArrayList<>(shareAccountData.getPurchasedShares());
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(2, transactions.size());
        for (GetAccountsPurchasedShares transaction : transactions) {
            LocalDate transactionDate = transaction.getPurchasedDate();
            String transactionType = transaction.getType().getCode();
            if (transactionType.equals("purchasedSharesType.purchased")) {
                Assertions.assertEquals(25, transaction.getNumberOfShares());
                Assertions.assertEquals(52.0, transaction.getAmount());
                Assertions.assertEquals(52.0, transaction.getAmountPaid());
                Assertions.assertEquals(2.0, transaction.getChargeAmount());
                Assertions.assertEquals(2.0, transaction.getPurchasedPrice());
                Assertions.assertEquals("01 January 2016", transactionDate.format(Utils.dateFormatter));
            } else if (transactionType.equals("charge.payment")) {
                Assertions.assertEquals(2.0, transaction.getAmount());
                Assertions.assertEquals(0.0, transaction.getAmountPaid());
                Assertions.assertEquals(Utils.getLocalDateOfTenant(), transactionDate);
            }
        }

        GetAccountsSummary summary = shareAccountData.getSummary();
        Assertions.assertEquals(25, summary.getTotalApprovedShares());
        Assertions.assertEquals(0, summary.getTotalPendingForApprovalShares());
    }

    @Test
    public void rejectShareAccount() {
        final Long productId = createShareProduct();
        Assertions.assertNotNull(productId);
        final Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);
        final Long savingsAccountId = openSavingsAccount(clientId);
        Assertions.assertNotNull(savingsAccountId);
        final Long activationChargeId = createShareCharge(ChargeTimeType.SHAREACCOUNT_ACTIVATION);
        final Long purchaseChargeId = createShareCharge(ChargeTimeType.SHARE_PURCHASE);
        final Long redeemChargeId = createShareCharge(ChargeTimeType.SHARE_REDEEM);
        final List<AccountChargesRequest> charges = List.of(accountCharge(activationChargeId, 2), accountCharge(purchaseChargeId, 2),
                accountCharge(redeemChargeId, 1));
        final Long shareAccountId = createShareAccount(clientId, productId, savingsAccountId, charges);
        Assertions.assertNotNull(shareAccountId);
        GetAccountsTypeAccountIdResponse shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        Assertions.assertNotNull(shareAccountData);

        // Reject share Account
        shareAccountHelper.reject(shareAccountId, "Share Account Rejection Note");
        shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        Assertions.assertEquals("shareAccountStatusType.rejected", shareAccountData.getStatus().getCode());
        LocalDate rejectedDate = shareAccountData.getTimeline().getRejectedDate();
        Assertions.assertEquals(Utils.getLocalDateOfTenant(), rejectedDate);

        List<GetAccountsPurchasedShares> transactions = new ArrayList<>(shareAccountData.getPurchasedShares());
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(2, transactions.size());
        for (GetAccountsPurchasedShares transaction : transactions) {
            LocalDate date = transaction.getPurchasedDate();
            String transactionType = transaction.getType().getCode();
            if (transactionType.equals("purchasedSharesType.purchased")) {
                Assertions.assertEquals(25, transaction.getNumberOfShares());
                Assertions.assertEquals(50.0, transaction.getAmount());
                Assertions.assertEquals(50.0, transaction.getAmountPaid());
                Assertions.assertEquals(2.0, transaction.getChargeAmount());
                Assertions.assertEquals(2.0, transaction.getPurchasedPrice());
                Assertions.assertEquals("01 January 2016", date.format(Utils.dateFormatter));
            } else if (transactionType.equals("charge.payment")) {
                Assertions.assertEquals(2.0, transaction.getAmount());
                Assertions.assertEquals(0.0, transaction.getAmountPaid());
                LocalDate transactionDate = Utils.getLocalDateOfTenant();
                Assertions.assertEquals(transactionDate, date);
            }
        }

        GetAccountsSummary summary = shareAccountData.getSummary();
        Assertions.assertEquals(0, summary.getTotalApprovedShares());
        Assertions.assertEquals(0, summary.getTotalPendingForApprovalShares());
    }

    @Test
    public void testShareAccountUndoApproval() {
        final Long productId = createShareProduct();
        Assertions.assertNotNull(productId);
        final Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);
        final Long savingsAccountId = openSavingsAccount(clientId);
        Assertions.assertNotNull(savingsAccountId);
        final Long activationChargeId = createShareCharge(ChargeTimeType.SHAREACCOUNT_ACTIVATION);
        final Long purchaseChargeId = createShareCharge(ChargeTimeType.SHARE_PURCHASE);
        final Long redeemChargeId = createShareCharge(ChargeTimeType.SHARE_REDEEM);
        final List<AccountChargesRequest> charges = List.of(accountCharge(activationChargeId, 2), accountCharge(purchaseChargeId, 2),
                accountCharge(redeemChargeId, 1));
        final Long shareAccountId = createShareAccount(clientId, productId, savingsAccountId, charges);
        Assertions.assertNotNull(shareAccountId);
        GetAccountsTypeAccountIdResponse shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        Assertions.assertNotNull(shareAccountData);

        // Approve share Account
        shareAccountHelper.approve(shareAccountId, "01 January 2016", "Share Account Approval Note", DATE_FORMAT, "en");
        shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        Assertions.assertEquals("shareAccountStatusType.approved", shareAccountData.getStatus().getCode());
        LocalDate approvedDate = shareAccountData.getTimeline().getApprovedDate();
        Assertions.assertEquals("01 January 2016", approvedDate.format(Utils.dateFormatter));

        // Undo Approval share Account
        shareAccountHelper.undoApproval(shareAccountId);

        shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);

        Assertions.assertEquals("shareAccountStatusType.submitted.and.pending.approval", shareAccountData.getStatus().getCode());

        List<GetAccountsPurchasedShares> transactions = new ArrayList<>(shareAccountData.getPurchasedShares());
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(2, transactions.size());
        for (GetAccountsPurchasedShares transaction : transactions) {
            LocalDate transactionDate = transaction.getPurchasedDate();
            String transactionType = transaction.getType().getCode();
            if (transactionType.equals("purchasedSharesType.purchased")) {
                Assertions.assertEquals(25, transaction.getNumberOfShares());
                Assertions.assertEquals(52.0, transaction.getAmount());
                Assertions.assertEquals(0.0, transaction.getAmountPaid());
                Assertions.assertEquals(2.0, transaction.getChargeAmount());
                Assertions.assertEquals(2.0, transaction.getPurchasedPrice());
                Assertions.assertEquals("01 January 2016", transactionDate.format(Utils.dateFormatter));
            } else if (transactionType.equals("charge.payment")) {
                Assertions.assertEquals(2.0, transaction.getAmount());
                Assertions.assertEquals(0.0, transaction.getAmountPaid());
                Assertions.assertEquals(Utils.getLocalDateOfTenant(), transactionDate);
            }
        }

        GetAccountsSummary summary = shareAccountData.getSummary();
        Assertions.assertEquals(0, summary.getTotalApprovedShares());
        Assertions.assertEquals(25, summary.getTotalPendingForApprovalShares());
    }

    @Test
    public void testCreateShareAccountWithCharges() {
        final Long productId = createShareProduct();
        Assertions.assertNotNull(productId);
        final Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);
        final Long savingsAccountId = openSavingsAccount(clientId);
        Assertions.assertNotNull(savingsAccountId);
        final Long activationChargeId = createShareCharge(ChargeTimeType.SHAREACCOUNT_ACTIVATION);
        final Long purchaseChargeId = createShareCharge(ChargeTimeType.SHARE_PURCHASE);
        final Long redeemChargeId = createShareCharge(ChargeTimeType.SHARE_REDEEM);
        final List<AccountChargesRequest> charges = List.of(accountCharge(activationChargeId, 2), accountCharge(purchaseChargeId, 2),
                accountCharge(redeemChargeId, 1));
        final Long shareAccountId = createShareAccount(clientId, productId, savingsAccountId, charges);
        Assertions.assertNotNull(shareAccountId);
        GetAccountsTypeAccountIdResponse shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        Assertions.assertNotNull(shareAccountData);

        shareAccountHelper.updateShareAccount(shareAccountId, new PutAccountsTypeAccountIdRequest().requestedShares(30)
                .applicationDate("02 March 2016").dateFormat(DATE_FORMAT).locale("en_GB").charges(charges));
        shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        List<GetAccountsPurchasedShares> transactions = new ArrayList<>(shareAccountData.getPurchasedShares());
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(2, transactions.size());
        for (GetAccountsPurchasedShares transaction : transactions) {
            String transactionType = transaction.getType().getCode();
            if (transactionType.equals("purchasedSharesType.purchased")) {
                Assertions.assertEquals(30, transaction.getNumberOfShares());
                Assertions.assertEquals(62.0, transaction.getAmount());
                Assertions.assertEquals(60.0, transaction.getAmountPaid());
                Assertions.assertEquals(2.0, transaction.getChargeAmount());
                Assertions.assertEquals(2.0, transaction.getPurchasedPrice());
                Assertions.assertEquals("02 March 2016", transaction.getPurchasedDate().format(Utils.dateFormatter));
            } else if (transactionType.equals("charge.payment")) {
                Assertions.assertEquals(2.0, transaction.getAmount());
                Assertions.assertEquals(0.0, transaction.getAmountPaid());
                Assertions.assertEquals(0.0, transaction.getChargeAmount());
            }
        }

        // charges verification
        for (GetAccountsCharges chargeDef : shareAccountData.getCharges()) {
            String chargeTimeType = chargeDef.getChargeTimeType().getCode();
            if (chargeTimeType.equals("chargeTimeType.activation")) {
                Assertions.assertEquals(2.0f, chargeDef.getAmount());
                Assertions.assertEquals(2.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(0.0f, chargeDef.getAmountPaid());
            } else if (chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assertions.assertEquals(2.0f, chargeDef.getAmount());
                Assertions.assertEquals(2.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(0.0f, chargeDef.getAmountPaid());
            } else if (chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assertions.assertEquals(1.0f, chargeDef.getAmountOrPercentage());
                Assertions.assertEquals(0.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(0.0f, chargeDef.getAmountPaid());
            } else {
                Assertions.fail("Other Charge defintion found");
            }
        }

        // Approve share Account
        shareAccountHelper.approve(shareAccountId, "01 January 2016", "Share Account Approval Note", DATE_FORMAT, "en");
        shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        Assertions.assertEquals("shareAccountStatusType.approved", shareAccountData.getStatus().getCode());
        LocalDate approvedDate = shareAccountData.getTimeline().getApprovedDate();
        Assertions.assertEquals("01 January 2016", approvedDate.format(Utils.dateFormatter));

        // charges verification
        for (GetAccountsCharges chargeDef : shareAccountData.getCharges()) {
            String chargeTimeType = chargeDef.getChargeTimeType().getCode();
            if (chargeTimeType.equals("chargeTimeType.activation")) {
                Assertions.assertEquals(2.0f, chargeDef.getAmount());
                Assertions.assertEquals(2.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(0.0f, chargeDef.getAmountPaid());
            } else if (chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assertions.assertEquals(2.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(2.0f, chargeDef.getAmountPaid());
            } else if (chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assertions.assertEquals(1.0f, chargeDef.getAmountOrPercentage());
                Assertions.assertEquals(0.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(0.0f, chargeDef.getAmountPaid());
            } else {
                Assertions.fail("Other Charge defintion found");
            }
        }

        shareAccountHelper.activate(shareAccountId, "01 January 2016", DATE_FORMAT, "en");
        shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        Assertions.assertEquals("shareAccountStatusType.active", shareAccountData.getStatus().getCode());
        LocalDate activatedDate = shareAccountData.getTimeline().getActivatedDate();
        Assertions.assertEquals("01 January 2016", activatedDate.format(Utils.dateFormatter));

        transactions = new ArrayList<>(shareAccountData.getPurchasedShares());
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(2, transactions.size());
        for (GetAccountsPurchasedShares transaction : transactions) {
            String transactionType = transaction.getType().getCode();
            if (transactionType.equals("purchasedSharesType.purchased")) {
                Assertions.assertEquals(30, transaction.getNumberOfShares());
                Assertions.assertEquals(62.0, transaction.getAmount());
                Assertions.assertEquals(62.0, transaction.getAmountPaid());
                Assertions.assertEquals(2.0, transaction.getChargeAmount());
                Assertions.assertEquals(2.0, transaction.getPurchasedPrice());
                Assertions.assertEquals("02 March 2016", transaction.getPurchasedDate().format(Utils.dateFormatter));
            } else if (transactionType.equals("charge.payment")) {
                Assertions.assertEquals(2.0, transaction.getAmount());
                Assertions.assertEquals(2.0, transaction.getAmountPaid());
                Assertions.assertEquals(0.0, transaction.getChargeAmount());
                Assertions.assertEquals("01 January 2016", transaction.getPurchasedDate().format(Utils.dateFormatter));
            }
        }

        // charges verification
        for (GetAccountsCharges chargeDef : shareAccountData.getCharges()) {
            String chargeTimeType = chargeDef.getChargeTimeType().getCode();
            if (chargeTimeType.equals("chargeTimeType.activation")) {
                Assertions.assertEquals(2.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(2.0f, chargeDef.getAmountPaid());
            } else if (chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assertions.assertEquals(2.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(2.0f, chargeDef.getAmountPaid());
            } else if (chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assertions.assertEquals(1.0f, chargeDef.getAmountOrPercentage());
                Assertions.assertEquals(0.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(0.0f, chargeDef.getAmountPaid());
            } else {
                Assertions.fail("Other Charge defintion found");
            }
        }

        GetAccountsSummary summary = shareAccountData.getSummary();
        Assertions.assertEquals(30, summary.getTotalApprovedShares());
        Assertions.assertEquals(0, summary.getTotalPendingForApprovalShares());

        // apply additional shares
        shareAccountHelper.applyAdditionalShares(shareAccountId, 15, "01 April 2016", DATE_FORMAT, "en");
        shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        transactions = new ArrayList<>(shareAccountData.getPurchasedShares());
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(3, transactions.size());
        Long addtionalSharesRequestId = null;
        for (GetAccountsPurchasedShares transaction : transactions) {
            String transactionType = transaction.getType().getCode();
            String transactionDate = transaction.getPurchasedDate().format(Utils.dateFormatter);
            if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("02 March 2016")) {
                Assertions.assertEquals(30, transaction.getNumberOfShares());
                Assertions.assertEquals(62.0, transaction.getAmount());
                Assertions.assertEquals(62.0, transaction.getAmountPaid());
                Assertions.assertEquals(2.0, transaction.getChargeAmount());
                Assertions.assertEquals(2.0, transaction.getPurchasedPrice());
            } else if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("01 April 2016")) {
                addtionalSharesRequestId = transaction.getId();
                Assertions.assertEquals(15, transaction.getNumberOfShares());
                Assertions.assertEquals(32.0, transaction.getAmount());
                Assertions.assertEquals(30.0, transaction.getAmountPaid());
                Assertions.assertEquals(2.0, transaction.getChargeAmount());
                Assertions.assertEquals(2.0, transaction.getPurchasedPrice());
                Assertions.assertEquals("purchasedSharesStatusType.applied", transaction.getStatus().getCode());

            } else if (transactionType.equals("charge.payment")) {
                Assertions.assertEquals(2.0, transaction.getAmount());
                Assertions.assertEquals(2.0, transaction.getAmountPaid());
                Assertions.assertEquals(0.0, transaction.getChargeAmount());
                Assertions.assertEquals("01 January 2016", transactionDate);
            }
        }

        // charges verification
        for (GetAccountsCharges chargeDef : shareAccountData.getCharges()) {
            String chargeTimeType = chargeDef.getChargeTimeType().getCode();
            if (chargeTimeType.equals("chargeTimeType.activation")) {
                Assertions.assertEquals(2.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(2.0f, chargeDef.getAmountPaid());
            } else if (chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assertions.assertEquals(4.0f, chargeDef.getAmount());
                Assertions.assertEquals(2.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(2.0f, chargeDef.getAmountPaid());
            } else if (chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assertions.assertEquals(1.0f, chargeDef.getAmountOrPercentage());
                Assertions.assertEquals(0.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(0.0f, chargeDef.getAmountPaid());
            } else {
                Assertions.fail("Other Charge defintion found");
            }
        }

        summary = shareAccountData.getSummary();
        Assertions.assertEquals(30, summary.getTotalApprovedShares());
        Assertions.assertEquals(15, summary.getTotalPendingForApprovalShares());

        // Approve additional Shares request
        shareAccountHelper.approveAdditionalShares(shareAccountId, addtionalSharesRequestId);

        shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        transactions = new ArrayList<>(shareAccountData.getPurchasedShares());
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(3, transactions.size());
        for (GetAccountsPurchasedShares transaction : transactions) {
            String transactionType = transaction.getType().getCode();
            String transactionDate = transaction.getPurchasedDate().format(Utils.dateFormatter);
            if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("02 March 2016")) {
                Assertions.assertEquals(30, transaction.getNumberOfShares());
                Assertions.assertEquals(62.0, transaction.getAmount());
                Assertions.assertEquals(62.0, transaction.getAmountPaid());
                Assertions.assertEquals(2.0, transaction.getChargeAmount());
                Assertions.assertEquals(2.0, transaction.getPurchasedPrice());
            } else if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("01 April 2016")) {
                Assertions.assertEquals(15, transaction.getNumberOfShares());
                Assertions.assertEquals(32.0, transaction.getAmount());
                Assertions.assertEquals(32.0, transaction.getAmountPaid());
                Assertions.assertEquals(2.0, transaction.getChargeAmount());
                Assertions.assertEquals(2.0, transaction.getPurchasedPrice());
                Assertions.assertEquals("purchasedSharesStatusType.approved", transaction.getStatus().getCode());

            } else if (transactionType.equals("charge.payment")) {
                Assertions.assertEquals(2.0, transaction.getAmount());
                Assertions.assertEquals(2.0, transaction.getAmountPaid());
                Assertions.assertEquals(0.0, transaction.getChargeAmount());
                Assertions.assertEquals("01 January 2016", transactionDate);
            }
        }

        // charges verification
        for (GetAccountsCharges chargeDef : shareAccountData.getCharges()) {
            String chargeTimeType = chargeDef.getChargeTimeType().getCode();
            if (chargeTimeType.equals("chargeTimeType.activation")) {
                Assertions.assertEquals(2.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(2.0f, chargeDef.getAmountPaid());
            } else if (chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assertions.assertEquals(4.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(4.0f, chargeDef.getAmountPaid());
            } else if (chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assertions.assertEquals(1.0f, chargeDef.getAmountOrPercentage());
                Assertions.assertEquals(0.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(0.0f, chargeDef.getAmountPaid());
            } else {
                Assertions.fail("Other Charge defintion found");
            }
        }

        summary = shareAccountData.getSummary();
        Assertions.assertEquals(45, summary.getTotalApprovedShares());
        Assertions.assertEquals(0, summary.getTotalPendingForApprovalShares());

        // apply aditional shres and reject it
        shareAccountHelper.applyAdditionalShares(shareAccountId, 20, "01 May 2016", DATE_FORMAT, "en");
        shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        transactions = new ArrayList<>(shareAccountData.getPurchasedShares());
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(4, transactions.size());
        addtionalSharesRequestId = null;
        for (GetAccountsPurchasedShares transaction : transactions) {
            String transactionType = transaction.getType().getCode();
            String transactionDate = transaction.getPurchasedDate().format(Utils.dateFormatter);
            if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("01 May 2016")) {
                addtionalSharesRequestId = transaction.getId();
                Assertions.assertEquals(20, transaction.getNumberOfShares());
                Assertions.assertEquals(42.0, transaction.getAmount());
                Assertions.assertEquals(40.0, transaction.getAmountPaid());
                Assertions.assertEquals(2.0, transaction.getChargeAmount());
                Assertions.assertEquals(2.0, transaction.getPurchasedPrice());
                Assertions.assertEquals("purchasedSharesStatusType.applied", transaction.getStatus().getCode());
            }
        }

        // charges verification
        for (GetAccountsCharges chargeDef : shareAccountData.getCharges()) {
            String chargeTimeType = chargeDef.getChargeTimeType().getCode();
            if (chargeTimeType.equals("chargeTimeType.activation")) {
                Assertions.assertEquals(2.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(2.0f, chargeDef.getAmountPaid());
            } else if (chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assertions.assertEquals(6.0f, chargeDef.getAmount());
                Assertions.assertEquals(2.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(4.0f, chargeDef.getAmountPaid());
            } else if (chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assertions.assertEquals(1.0f, chargeDef.getAmountOrPercentage());
                Assertions.assertEquals(0.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(0.0f, chargeDef.getAmountPaid());
            } else {
                Assertions.fail("Other Charge defintion found");
            }
        }

        summary = shareAccountData.getSummary();
        Assertions.assertEquals(45, summary.getTotalApprovedShares());
        Assertions.assertEquals(20, summary.getTotalPendingForApprovalShares());

        // rejectadditionalshares
        shareAccountHelper.rejectAdditionalShares(shareAccountId, addtionalSharesRequestId);
        shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        transactions = new ArrayList<>(shareAccountData.getPurchasedShares());
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(4, transactions.size());
        for (GetAccountsPurchasedShares transaction : transactions) {
            String transactionType = transaction.getType().getCode();
            String transactionDate = transaction.getPurchasedDate().format(Utils.dateFormatter);
            if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("01 May 2016")) {
                addtionalSharesRequestId = transaction.getId();
                Assertions.assertEquals(20, transaction.getNumberOfShares());
                Assertions.assertEquals(40.0, transaction.getAmount());
                Assertions.assertEquals(40.0, transaction.getAmountPaid());
                Assertions.assertEquals(2.0, transaction.getChargeAmount());
                Assertions.assertEquals(2.0, transaction.getPurchasedPrice());
                Assertions.assertEquals("purchasedSharesStatusType.rejected", transaction.getStatus().getCode());
            }
        }

        // charges verification
        for (GetAccountsCharges chargeDef : shareAccountData.getCharges()) {
            String chargeTimeType = chargeDef.getChargeTimeType().getCode();
            if (chargeTimeType.equals("chargeTimeType.activation")) {
                Assertions.assertEquals(2.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(2.0f, chargeDef.getAmountPaid());
            } else if (chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assertions.assertEquals(6.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(6.0f, chargeDef.getAmountPaid());
            } else if (chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assertions.assertEquals(1.0f, chargeDef.getAmountOrPercentage());
                Assertions.assertEquals(0.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(0.0f, chargeDef.getAmountPaid());
            } else {
                Assertions.fail("Other Charge defintion found");
            }
        }

        summary = shareAccountData.getSummary();
        Assertions.assertEquals(45, summary.getTotalApprovedShares());
        Assertions.assertEquals(0, summary.getTotalPendingForApprovalShares());

        // redeem shares
        shareAccountHelper.redeemShares(shareAccountId, 15, "05 May 2016", DATE_FORMAT, "en");
        shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        transactions = new ArrayList<>(shareAccountData.getPurchasedShares());
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(5, transactions.size());
        for (GetAccountsPurchasedShares transaction : transactions) {
            String transactionType = transaction.getType().getCode();
            String transactionDate = transaction.getPurchasedDate().format(Utils.dateFormatter);
            if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("02 March 2016")) {
                Assertions.assertEquals(30, transaction.getNumberOfShares());
                Assertions.assertEquals(62.0, transaction.getAmount());
                Assertions.assertEquals(62.0, transaction.getAmountPaid());
                Assertions.assertEquals(2.0, transaction.getChargeAmount());
                Assertions.assertEquals(2.0, transaction.getPurchasedPrice());
            } else if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("01 April 2016")) {
                Assertions.assertEquals(15, transaction.getNumberOfShares());
                Assertions.assertEquals(32.0, transaction.getAmount());
                Assertions.assertEquals(32.0, transaction.getAmountPaid());
                Assertions.assertEquals(2.0, transaction.getChargeAmount());
                Assertions.assertEquals(2.0, transaction.getPurchasedPrice());
                Assertions.assertEquals("purchasedSharesStatusType.approved", transaction.getStatus().getCode());
            } else if (transactionType.equals("purchasedSharesType.redeemed") && transactionDate.equals("05 May 2016")) {
                Assertions.assertEquals(15, transaction.getNumberOfShares());
                Assertions.assertEquals(29.0, transaction.getAmount());
                Assertions.assertEquals(29.0, transaction.getAmountPaid());
                Assertions.assertEquals(1.0, transaction.getChargeAmount());
                Assertions.assertEquals("purchasedSharesStatusType.approved", transaction.getStatus().getCode());
            } else if (transactionType.equals("charge.payment")) {
                Assertions.assertEquals(2.0, transaction.getAmount());
                Assertions.assertEquals(2.0, transaction.getAmountPaid());
                Assertions.assertEquals(0.0, transaction.getChargeAmount());
                Assertions.assertEquals("01 January 2016", transactionDate);
            }
        }

        // charges verification
        for (GetAccountsCharges chargeDef : shareAccountData.getCharges()) {
            String chargeTimeType = chargeDef.getChargeTimeType().getCode();
            if (chargeTimeType.equals("chargeTimeType.activation")) {
                Assertions.assertEquals(2.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(2.0f, chargeDef.getAmountPaid());
            } else if (chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assertions.assertEquals(6.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(6.0f, chargeDef.getAmountPaid());
            } else if (chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assertions.assertEquals(1.0f, chargeDef.getAmountOrPercentage());
                Assertions.assertEquals(1.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(1.0f, chargeDef.getAmountPaid());
            } else {
                Assertions.fail("Other Charge defintion found");
            }
        }
        summary = shareAccountData.getSummary();
        Assertions.assertEquals(30, summary.getTotalApprovedShares());
        Assertions.assertEquals(0, summary.getTotalPendingForApprovalShares());

        // Close Share Account
        shareAccountHelper.close(shareAccountId, "10 May 2016", "Share Account Close Note", DATE_FORMAT, "en");
        shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        Assertions.assertEquals("shareAccountStatusType.closed", shareAccountData.getStatus().getCode());
        transactions = new ArrayList<>(shareAccountData.getPurchasedShares());
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(6, transactions.size());
        for (GetAccountsPurchasedShares transaction : transactions) {
            String transactionType = transaction.getType().getCode();
            String transactionDate = transaction.getPurchasedDate().format(Utils.dateFormatter);
            if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("02 March 2016")) {
                Assertions.assertEquals(30, transaction.getNumberOfShares());
                Assertions.assertEquals(62.0, transaction.getAmount());
                Assertions.assertEquals(62.0, transaction.getAmountPaid());
                Assertions.assertEquals(2.0, transaction.getChargeAmount());
                Assertions.assertEquals(2.0, transaction.getPurchasedPrice());
            } else if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("01 April 2016")) {
                Assertions.assertEquals(15, transaction.getNumberOfShares());
                Assertions.assertEquals(32.0, transaction.getAmount());
                Assertions.assertEquals(32.0, transaction.getAmountPaid());
                Assertions.assertEquals(2.0, transaction.getChargeAmount());
                Assertions.assertEquals(2.0, transaction.getPurchasedPrice());
                Assertions.assertEquals("purchasedSharesStatusType.approved", transaction.getStatus().getCode());
            } else if (transactionType.equals("purchasedSharesType.redeemed") && transactionDate.equals("05 May 2016")) {
                Assertions.assertEquals(15, transaction.getNumberOfShares());
                Assertions.assertEquals(29.0, transaction.getAmount());
                Assertions.assertEquals(29.0, transaction.getAmountPaid());
                Assertions.assertEquals(1.0, transaction.getChargeAmount());
                Assertions.assertEquals("purchasedSharesStatusType.approved", transaction.getStatus().getCode());
            } else if (transactionType.equals("purchasedSharesType.redeemed") && transactionDate.equals("10 May 2016")) {
                Assertions.assertEquals(30, transaction.getNumberOfShares());
                Assertions.assertEquals(59.0, transaction.getAmount());
                Assertions.assertEquals(59.0, transaction.getAmountPaid());
                Assertions.assertEquals(1.0, transaction.getChargeAmount());
                Assertions.assertEquals("purchasedSharesStatusType.approved", transaction.getStatus().getCode());
            } else if (transactionType.equals("charge.payment")) {
                Assertions.assertEquals(2.0, transaction.getAmount());
                Assertions.assertEquals(2.0, transaction.getAmountPaid());
                Assertions.assertEquals(0.0, transaction.getChargeAmount());
                Assertions.assertEquals("01 January 2016", transactionDate);
            }
        }
        // charges verification
        for (GetAccountsCharges chargeDef : shareAccountData.getCharges()) {
            String chargeTimeType = chargeDef.getChargeTimeType().getCode();
            if (chargeTimeType.equals("chargeTimeType.activation")) {
                Assertions.assertEquals(2.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(2.0f, chargeDef.getAmountPaid());
            } else if (chargeTimeType.equals("chargeTimeType.sharespurchase")) {
                Assertions.assertEquals(6.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(6.0f, chargeDef.getAmountPaid());
            } else if (chargeTimeType.equals("chargeTimeType.sharesredeem")) {
                Assertions.assertEquals(1.0f, chargeDef.getAmountOrPercentage());
                Assertions.assertEquals(2.0f, chargeDef.getAmount());
                Assertions.assertEquals(0.0f, chargeDef.getAmountOutstanding());
                Assertions.assertEquals(2.0f, chargeDef.getAmountPaid());
            } else {
                Assertions.fail("Other Charge defintion found");
            }
        }
        summary = shareAccountData.getSummary();
        Assertions.assertEquals(0, summary.getTotalApprovedShares());
        Assertions.assertEquals(0, summary.getTotalPendingForApprovalShares());
    }

    // Refactored Test 1
    @Test
    public void testChronologicalAdditionalSharesAfterRejectedTransaction() {
        final Long productId = createShareProduct();
        Assertions.assertNotNull(productId);
        final Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);
        final Long savingsAccountId = openSavingsAccount(clientId);
        Assertions.assertNotNull(savingsAccountId);

        // Setup and activate share account with initial shares on 01 March 2016
        final Long shareAccountId = setupAndActivateShareAccount(clientId, productId, savingsAccountId, "01 March 2016");

        // Apply additional shares on 15 April 2016
        applyAdditionalShares(shareAccountId, "15 April 2016", 20);

        // Retrieve transactions and find the additional shares request
        GetAccountsTypeAccountIdResponse shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        List<GetAccountsPurchasedShares> transactions = new ArrayList<>(shareAccountData.getPurchasedShares());
        Assertions.assertNotNull(transactions);

        // Find and reject the additional shares request (15 April 2016)
        Long additionalSharesRequestId = findTransactionId(transactions, "purchasedSharesType.purchased", "15 April 2016");
        Assertions.assertNotNull(additionalSharesRequestId, "Additional shares request for 15 April 2016 should exist");

        // Reject the additional shares request
        rejectAdditionalSharesRequest(shareAccountId, additionalSharesRequestId);

        // Verify transaction is rejected
        shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        transactions = new ArrayList<>(shareAccountData.getPurchasedShares());
        verifyTransactionStatus(transactions, "purchasedSharesType.purchased", "15 April 2016", "purchasedSharesStatusType.rejected");

        // Now try to apply additional shares with a date BEFORE the rejected transaction (10 April 2016)
        // This should succeed because rejected transactions should be ignored in chronological validation
        applyAdditionalShares(shareAccountId, "10 April 2016", 15);

        // Verify the new transaction was successfully added
        shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        transactions = new ArrayList<>(shareAccountData.getPurchasedShares());
        verifyTransactionWithShares(transactions, "purchasedSharesType.purchased", "10 April 2016", 15,
                "purchasedSharesStatusType.applied");
    }

    @Test
    public void testChronologicalAccountClosureBeforeRejectedTransaction() {
        // FINERACT-2457: Account closure validation should ignore rejected/reversed transactions
        final Long productId = createShareProduct();
        Assertions.assertNotNull(productId);
        final Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);
        final Long savingsAccountId = openSavingsAccount(clientId);
        Assertions.assertNotNull(savingsAccountId);

        // Setup and activate share account with initial shares on 01 March 2016
        final Long shareAccountId = setupAndActivateShareAccount(clientId, productId, savingsAccountId, "01 March 2016");

        // Apply additional shares on 20 May 2016
        applyAdditionalShares(shareAccountId, "20 May 2016", 30);

        // Retrieve transactions and find the additional shares request
        GetAccountsTypeAccountIdResponse shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        List<GetAccountsPurchasedShares> transactions = new ArrayList<>(shareAccountData.getPurchasedShares());
        Assertions.assertNotNull(transactions);

        // Find and reject the additional shares request (20 May 2016)
        Long additionalSharesRequestId = findTransactionId(transactions, "purchasedSharesType.purchased", "20 May 2016");
        Assertions.assertNotNull(additionalSharesRequestId, "Additional shares request for 20 May 2016 should exist");

        // Reject the additional shares request
        rejectAdditionalSharesRequest(shareAccountId, additionalSharesRequestId);

        // Verify transaction is rejected
        shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        transactions = new ArrayList<>(shareAccountData.getPurchasedShares());
        verifyTransactionStatus(transactions, "purchasedSharesType.purchased", "20 May 2016", "purchasedSharesStatusType.rejected");

        // Now try to close the account with a date BEFORE the rejected transaction (15 May 2016)
        // This should succeed because rejected transactions should be ignored in chronological validation
        shareAccountHelper.close(shareAccountId, "15 May 2016", "Share Account Close Note", DATE_FORMAT, "en");

        // Verify the account was successfully closed
        shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        Assertions.assertEquals("shareAccountStatusType.closed", shareAccountData.getStatus().getCode());

        LocalDate closedDate = shareAccountData.getTimeline().getClosedDate();
        Assertions.assertEquals("15 May 2016", closedDate.format(Utils.dateFormatter));
    }

    // Additional Test 1: Verify original validation still works (Negative Test)
    @Test
    public void testChronologicalAdditionalSharesBeforeActiveTransactionShouldFail() {
        // FINERACT-2457: Verify that the fix didn't break the original chronological validation
        // Transactions BEFORE active/approved transactions should still be REJECTED
        final Long productId = createShareProduct();
        Assertions.assertNotNull(productId);
        final Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);
        final Long savingsAccountId = openSavingsAccount(clientId);
        Assertions.assertNotNull(savingsAccountId);

        // Setup and activate share account with initial shares on 01 March 2016
        final Long shareAccountId = setupAndActivateShareAccount(clientId, productId, savingsAccountId, "01 March 2016");

        // Apply additional shares on 15 April 2016 (this remains active/approved)
        applyAdditionalShares(shareAccountId, "15 April 2016", 20);

        // Verify the transaction exists and is in applied/active state
        GetAccountsTypeAccountIdResponse shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        List<GetAccountsPurchasedShares> transactions = new ArrayList<>(shareAccountData.getPurchasedShares());
        Assertions.assertNotNull(transactions);

        Long transactionId = findTransactionId(transactions, "purchasedSharesType.purchased", "15 April 2016");
        Assertions.assertNotNull(transactionId, "Transaction for 15 April 2016 should exist");

        // Try to apply additional shares BEFORE the active transaction (10 April 2016)
        // This should FAIL because the April 15 transaction is ACTIVE/APPROVED
        final CallFailedRuntimeException exception = shareAccountHelper.applyAdditionalSharesExpectingError(shareAccountId, 15,
                "10 April 2016", DATE_FORMAT, "en");
        Assertions.assertEquals(400, exception.getStatus());
        Assertions.assertEquals("validation.msg.sharesaccount.purchase.transaction.date.cannot.be.before.existing.transactions",
                errorGlobalisationCode(exception));
    }

    // Additional Test 3: Test edge case - transaction on same date as rejected transaction
    @Test
    public void testChronologicalAdditionalSharesOnSameDateAsRejectedTransaction() {
        // FINERACT-2457: Test behavior when applying shares on the SAME date as a rejected transaction
        final Long productId = createShareProduct();
        Assertions.assertNotNull(productId);
        final Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);
        final Long savingsAccountId = openSavingsAccount(clientId);
        Assertions.assertNotNull(savingsAccountId);

        // Setup and activate share account with initial shares on 01 March 2016
        final Long shareAccountId = setupAndActivateShareAccount(clientId, productId, savingsAccountId, "01 March 2016");

        // Apply and reject shares on 15 April 2016
        applyAdditionalShares(shareAccountId, "15 April 2016", 20);
        GetAccountsTypeAccountIdResponse shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        List<GetAccountsPurchasedShares> transactions = new ArrayList<>(shareAccountData.getPurchasedShares());
        Long txId = findTransactionId(transactions, "purchasedSharesType.purchased", "15 April 2016");
        Assertions.assertNotNull(txId);
        rejectAdditionalSharesRequest(shareAccountId, txId);

        // Verify transaction is rejected
        shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        transactions = new ArrayList<>(shareAccountData.getPurchasedShares());
        verifyTransactionStatus(transactions, "purchasedSharesType.purchased", "15 April 2016", "purchasedSharesStatusType.rejected");

        // Try to apply shares on the SAME date as the rejected transaction
        // This should succeed since rejected transactions are ignored
        applyAdditionalShares(shareAccountId, "15 April 2016", 15);

        // Verify the new transaction was successfully added
        shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        transactions = new ArrayList<>(shareAccountData.getPurchasedShares());

        // Count how many transactions exist for 15 April 2016
        int countForDate = 0;
        int appliedCountForDate = 0;

        for (GetAccountsPurchasedShares transaction : transactions) {
            String transactionType = transaction.getType().getCode();
            String transactionDate = transaction.getPurchasedDate().format(Utils.dateFormatter);

            if (transactionType.equals("purchasedSharesType.purchased") && transactionDate.equals("15 April 2016")) {
                countForDate++;
                String status = transaction.getStatus().getCode();
                if (status.equals("purchasedSharesStatusType.applied")) {
                    appliedCountForDate++;
                    Assertions.assertEquals(15, transaction.getNumberOfShares());
                }
            }
        }

        // Should have 2 transactions for this date: 1 rejected, 1 applied
        Assertions.assertEquals(2, countForDate, "Should have 2 transactions for 15 April 2016");
        Assertions.assertEquals(1, appliedCountForDate, "Should have 1 applied transaction for 15 April 2016");
    }

    // Additional Test 4: Test account closure before active transaction should still fail
    @Test
    public void testChronologicalAccountClosureBeforeActiveTransactionShouldFail() {
        // FINERACT-2457: Verify that closing account before active transactions is still blocked
        final Long productId = createShareProduct();
        Assertions.assertNotNull(productId);
        final Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);
        final Long savingsAccountId = openSavingsAccount(clientId);
        Assertions.assertNotNull(savingsAccountId);

        // Setup and activate share account with initial shares on 01 March 2016
        final Long shareAccountId = setupAndActivateShareAccount(clientId, productId, savingsAccountId, "01 March 2016");

        // Apply additional shares on 20 May 2016 (and keep it active/approved)
        applyAdditionalShares(shareAccountId, "20 May 2016", 30);

        // Verify the transaction exists and is active
        GetAccountsTypeAccountIdResponse shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        List<GetAccountsPurchasedShares> transactions = new ArrayList<>(shareAccountData.getPurchasedShares());
        Long transactionId = findTransactionId(transactions, "purchasedSharesType.purchased", "20 May 2016");
        Assertions.assertNotNull(transactionId, "Transaction for 20 May 2016 should exist");

        // Try to close account on 15 May 2016 (before the active transaction)
        // This should FAIL
        final CallFailedRuntimeException exception = shareAccountHelper.closeExpectingError(shareAccountId, "15 May 2016",
                "Share Account Close Note", DATE_FORMAT, "en");
        Assertions.assertEquals(400, exception.getStatus());
        Assertions.assertEquals("validation.msg.sharesaccount.share.account.cannot.be.closed.before.existing.transactions",
                errorGlobalisationCode(exception));

        // Verify account is still active (not closed)
        shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        Assertions.assertEquals("shareAccountStatusType.active", shareAccountData.getStatus().getCode(),
                "Account should still be active since closure was rejected");
    }

    private Long createShareProduct() {
        return shareAccountHelper.createShareProduct(FeignShareAccountHelper.defaultShareProduct());
    }

    private static void verifyShareProduct(PostProductsTypeRequest expected, GetProductsTypeProductIdResponse shareProductData) {
        Assertions.assertEquals(expected.getName(), shareProductData.getName());
        Assertions.assertEquals(expected.getShortName(), shareProductData.getShortName());
        Assertions.assertEquals(expected.getDescription(), shareProductData.getDescription());
        Assertions.assertEquals(expected.getCurrencyCode(), shareProductData.getCurrency().getCode());
        Assertions.assertEquals(expected.getDigitsAfterDecimal(), shareProductData.getCurrency().getDecimalPlaces());
        Assertions.assertEquals(expected.getInMultiplesOf(), shareProductData.getCurrency().getInMultiplesOf());
        Assertions.assertEquals(expected.getTotalShares(), shareProductData.getTotalShares());
        Assertions.assertEquals(expected.getSharesIssued(), shareProductData.getTotalSharesIssued());
        Assertions.assertEquals(0, BigDecimal.valueOf(expected.getUnitPrice()).compareTo(shareProductData.getUnitPrice()));
        Assertions.assertEquals(expected.getMinimumShares(), shareProductData.getMinimumShares());
        Assertions.assertEquals(expected.getNominalShares(), shareProductData.getNominalShares());
        Assertions.assertEquals(expected.getMaximumShares(), shareProductData.getMaximumShares());
        Assertions.assertEquals(expected.getAllowDividendCalculationForInactiveClients(),
                shareProductData.getAllowDividendCalculationForInactiveClients());
        Assertions.assertEquals(expected.getAccountingRule().longValue(), shareProductData.getAccountingRule().getId());
        Assertions.assertEquals(expected.getMinimumActivePeriodForDividends(), shareProductData.getMinimumActivePeriod());
        Assertions.assertEquals(expected.getMinimumactiveperiodFrequencyType().longValue(),
                shareProductData.getMinimumActivePeriodForDividendsTypeEnum().getId());
        Assertions.assertEquals(expected.getLockinPeriodFrequency(), shareProductData.getLockinPeriod());
        Assertions.assertEquals(expected.getLockinPeriodFrequencyType().longValue(), shareProductData.getLockPeriodTypeEnum().getId());
    }

    private Long openSavingsAccount(final Long clientId) {
        final Long savingsProductId = savingsProductHelper
                .createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().minRequiredOpeningBalance(BigDecimal.valueOf(1000)))
                .getResourceId();
        final Long savingsId = savingsHelper.submitApplication(clientId, savingsProductId, "08 January 2013").getSavingsId();
        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));
        savingsHelper.approveSavings(savingsId, "09 January 2013");
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));
        savingsHelper.activateSavings(savingsId, "01 March 2013");
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));
        return savingsId;
    }

    private Long createShareCharge(ChargeTimeType chargeTimeType) {
        return chargesHelper.createCharge(new ChargeRequest()//
                .name(Utils.uniqueRandomStringGenerator("Charge_Share_", 8))//
                .chargeAppliesTo(ChargeAppliesTo.SHARES.getValue())//
                .chargeTimeType(chargeTimeType.getValue())//
                .chargeCalculationType(ChargeCalculationType.FLAT.getValue())//
                .amount(100.0)//
                .currencyCode("USD")//
                .locale("en")//
                .active(true)).getResourceId();
    }

    private static AccountChargesRequest accountCharge(final Long chargeId, final int amount) {
        return new AccountChargesRequest().chargeId(chargeId).amount(BigDecimal.valueOf(amount));
    }

    private Long createShareAccount(final Long clientId, final Long productId, final Long savingsAccountId) {
        return shareAccountHelper.applyShareAccount(shareAccountRequest(clientId, productId, savingsAccountId));
    }

    private Long createShareAccount(final Long clientId, final Long productId, final Long savingsAccountId,
            final List<AccountChargesRequest> charges) {
        return shareAccountHelper.applyShareAccount(shareAccountRequest(clientId, productId, savingsAccountId).charges(charges));
    }

    private static AccountRequest shareAccountRequest(final Long clientId, final Long productId, final Long savingsAccountId) {
        return new AccountRequest().clientId(clientId).productId(productId).externalId("External1").savingsAccountId(savingsAccountId)
                .submittedDate("01 January 2016").applicationDate("01 January 2016").requestedShares(25L).dateFormat(DATE_FORMAT)
                .locale("en_GB");
    }

    private void updateShareAccountWithInitialData(Long shareAccountId, Integer requestedShares, String applicationDate) {
        shareAccountHelper.updateShareAccount(shareAccountId, new PutAccountsTypeAccountIdRequest().requestedShares(requestedShares)
                .applicationDate(applicationDate).dateFormat(DATE_FORMAT).locale("en_GB"));
    }

    private void approveShareAccount(Long shareAccountId, String approvalDate) {
        shareAccountHelper.approve(shareAccountId, approvalDate, "Share Account Approval Note", DATE_FORMAT, "en");
    }

    private void activateShareAccount(Long shareAccountId, String activationDate) {
        shareAccountHelper.activate(shareAccountId, activationDate, DATE_FORMAT, "en");
    }

    private void applyAdditionalShares(Long shareAccountId, String requestedDate, long requestedShares) {
        shareAccountHelper.applyAdditionalShares(shareAccountId, requestedShares, requestedDate, DATE_FORMAT, "en");
    }

    private static Long findTransactionId(List<GetAccountsPurchasedShares> transactions, String transactionTypeCode, String expectedDate) {
        for (GetAccountsPurchasedShares transaction : transactions) {
            String transactionType = transaction.getType().getCode();
            String transactionDate = transaction.getPurchasedDate().format(Utils.dateFormatter);

            if (transactionType.equals(transactionTypeCode) && transactionDate.equals(expectedDate)) {
                return transaction.getId();
            }
        }
        return null;
    }

    private void rejectAdditionalSharesRequest(Long shareAccountId, Long transactionId) {
        shareAccountHelper.rejectAdditionalShares(shareAccountId, transactionId);
    }

    private static void verifyTransactionStatus(List<GetAccountsPurchasedShares> transactions, String transactionTypeCode,
            String expectedDate, String expectedStatus) {
        boolean transactionFound = false;

        for (GetAccountsPurchasedShares transaction : transactions) {
            String transactionType = transaction.getType().getCode();
            String transactionDate = transaction.getPurchasedDate().format(Utils.dateFormatter);

            if (transactionType.equals(transactionTypeCode) && transactionDate.equals(expectedDate)) {
                Assertions.assertEquals(expectedStatus, transaction.getStatus().getCode());
                transactionFound = true;
                break;
            }
        }

        Assertions.assertTrue(transactionFound,
                String.format("Transaction with type %s for %s should exist", transactionTypeCode, expectedDate));
    }

    private static void verifyTransactionWithShares(List<GetAccountsPurchasedShares> transactions, String transactionTypeCode,
            String expectedDate, int expectedShares, String expectedStatus) {
        boolean transactionFound = false;

        for (GetAccountsPurchasedShares transaction : transactions) {
            String transactionType = transaction.getType().getCode();
            String transactionDate = transaction.getPurchasedDate().format(Utils.dateFormatter);

            if (transactionType.equals(transactionTypeCode) && transactionDate.equals(expectedDate)) {
                Assertions.assertEquals(expectedShares, transaction.getNumberOfShares());
                Assertions.assertEquals(expectedStatus, transaction.getStatus().getCode());
                transactionFound = true;
                break;
            }
        }

        Assertions.assertTrue(transactionFound, String.format("Transaction for %s should be successfully created", expectedDate));
    }

    private Long setupAndActivateShareAccount(Long clientId, Long productId, Long savingsAccountId, String initialDate) {
        final Long shareAccountId = createShareAccount(clientId, productId, savingsAccountId);
        Assertions.assertNotNull(shareAccountId);

        updateShareAccountWithInitialData(shareAccountId, 25, initialDate);
        approveShareAccount(shareAccountId, initialDate);
        activateShareAccount(shareAccountId, initialDate);

        // Verify account is active
        GetAccountsTypeAccountIdResponse shareAccountData = shareAccountHelper.getShareAccount(shareAccountId);
        Assertions.assertEquals("shareAccountStatusType.active", shareAccountData.getStatus().getCode());

        return shareAccountId;
    }
}
