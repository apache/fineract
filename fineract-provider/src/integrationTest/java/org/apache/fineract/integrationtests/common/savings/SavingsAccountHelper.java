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
package org.apache.fineract.integrationtests.common.savings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.Assert;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "rawtypes" })
public class SavingsAccountHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    private static final String SAVINGS_ACCOUNT_URL = "/fineract-provider/api/v1/savingsaccounts";
    private static final String APPROVE_SAVINGS_COMMAND = "approve";
    private static final String UNDO_APPROVAL_SAVINGS_COMMAND = "undoApproval";
    private static final String ACTIVATE_SAVINGS_COMMAND = "activate";
    private static final String REJECT_SAVINGS_COMMAND = "reject";
    private static final String WITHDRAWN_BY_CLIENT_SAVINGS_COMMAND = "withdrawnByApplicant";
    private static final String CALCULATE_INTEREST_SAVINGS_COMMAND = "calculateInterest";
    private static final String POST_INTEREST_SAVINGS_COMMAND = "postInterest";
    private static final String POST_INTEREST_AS_ON_SAVINGS_COMMAND = "postInterestAsOn";
    private static final String CLOSE_SAVINGS_COMMAND = "close";
    private static final String UPDATE_WITHHOLD_TAX_STATUS = "updateWithHoldTax";

    private static final String DEPOSIT_SAVINGS_COMMAND = "deposit";
    private static final String WITHDRAW_SAVINGS_COMMAND = "withdrawal";
    private static final String MODIFY_TRASACTION_COMMAND = "modify";
    private static final String UNDO_TRASACTION_COMMAND = "undo";
    
    private static final String BLOCK_SAVINGS_COMMAND = "block";
    private static final String UNBLOCK_SAVINGS_COMMAND = "unblock";
    private static final String BLOCK_DEBITS_SAVINGS_COMMAND = "blockDebit";
    private static final String UNBLOCK_DEBITS_SAVINGS_COMMAND = "unblockDebit";
    private static final String BLOCK_CREDITS_SAVINGS_COMMAND = "blockCredit";
    private static final String UNBLOCK_CREDITS_SAVINGS_COMMAND = "unblockCredit";
    private static final String HOLD_AMOUNT_SAVINGS_COMMAND = "holdAmount";
    private static final String RELEASE_AMOUNT_SAVINGS_COMMAND = "releaseAmount";

    public static final String CREATED_DATE = "08 January 2013";
    public static final String CREATED_DATE_PLUS_ONE = "09 January 2013";
    public static final String CREATED_DATE_MINUS_ONE = "07 January 2013";
    public static final String TRANSACTION_DATE = "01 March 2013";
    public static final String LAST_TRANSACTION_DATE = "01 March 2013";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";

    public static final String DATE_TIME_FORMAT = "dd MMMM yyyy HH:mm";
    private static final Boolean isBlock = false;

    public SavingsAccountHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public static String getFutureDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(CommonConstants.dateFormat, Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
        return sdf.format(calendar.getTime());
    }

    public Integer applyForSavingsApplication(final Integer ID, final Integer savingsProductID, final String accountType) {
        return applyForSavingsApplicationOnDate(ID, savingsProductID, accountType, CREATED_DATE);
    }

    public Integer applyForSavingsApplicationOnDate(final Integer ID, final Integer savingsProductID, final String accountType,
            final String submittedOnDate) {
        System.out.println("--------------------------------APPLYING FOR SAVINGS APPLICATION--------------------------------");
        final String savingsApplicationJSON = new SavingsApplicationTestBuilder() //
                .withSubmittedOnDate(submittedOnDate) //
                .build(ID.toString(), savingsProductID.toString(), accountType);
        return Utils.performServerPost(this.requestSpec, this.responseSpec, SAVINGS_ACCOUNT_URL + "?" + Utils.TENANT_IDENTIFIER,
                savingsApplicationJSON, "savingsId");
    }

    public Integer applyForSavingsApplicationWithDatatables(final Integer ID, final Integer savingsProductID, final String accountType,
            final String submittedOnDate, final String datatableName) {
        System.out.println("----------------------------APPLYING FOR SAVINGS APPLICATION WITH DATATABLES----------------------------");
        final String savingsApplicationJSON = new SavingsApplicationTestBuilder() //
                .withSubmittedOnDate(submittedOnDate) //
                .withDatatables(getTestDatatableAsJson(datatableName)) //
                .build(ID.toString(), savingsProductID.toString(), accountType);
        return Utils.performServerPost(this.requestSpec, this.responseSpec, SAVINGS_ACCOUNT_URL + "?" + Utils.TENANT_IDENTIFIER,
                savingsApplicationJSON, "savingsId");
    }

    public Object applyForSavingsApplicationWithFailure(final Integer ID, final Integer savingsProductID, final String accountType,
            final String submittedOnDate, final String responseAttribute) {
        System.out.println("----------------------------APPLYING FOR SAVINGS APPLICATION WITH ERROR----------------------------");
        final String savingsApplicationJSON = new SavingsApplicationTestBuilder() //
                .withSubmittedOnDate(submittedOnDate) //
                .build(ID.toString(), savingsProductID.toString(), accountType);
        return Utils.performServerPost(this.requestSpec, this.responseSpec, SAVINGS_ACCOUNT_URL + "?" + Utils.TENANT_IDENTIFIER,
                savingsApplicationJSON, responseAttribute);
    }

    public HashMap updateSavingsAccount(final Integer ID, final Integer savingsProductID, final Integer savingsId, final String accountType) {
        final String savingsApplicationJSON = new SavingsApplicationTestBuilder() //
                .withSubmittedOnDate(CREATED_DATE_PLUS_ONE) //
                .build(ID.toString(), savingsProductID.toString(), accountType);

        return Utils.performServerPut(this.requestSpec, this.responseSpec, SAVINGS_ACCOUNT_URL + "/" + savingsId + "?"
                + Utils.TENANT_IDENTIFIER, savingsApplicationJSON, CommonConstants.RESPONSE_CHANGES);
    }

    public HashMap updateSavingsAccountWithHoldTaxStatus(final Integer savingsId, final boolean value) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("withHoldTax", value);
        String json = new Gson().toJson(map);

        return Utils.performServerPut(this.requestSpec, this.responseSpec, SAVINGS_ACCOUNT_URL + "/" + savingsId + "?command="
                + UPDATE_WITHHOLD_TAX_STATUS + "&" + Utils.TENANT_IDENTIFIER, json, CommonConstants.RESPONSE_CHANGES);
    }

    public HashMap approveSavings(final Integer savingsID) {
        return approveSavingsOnDate(savingsID, null);
    }

    public HashMap approveSavingsOnDate(final Integer savingsID, final String approvalDate) {
        System.out.println("--------------------------------- APPROVING SAVINGS APPLICATION ------------------------------------");
        final String savingsOperationURL = createSavingsOperationURL(APPROVE_SAVINGS_COMMAND, savingsID);
        if (approvalDate == null || approvalDate == "")
            return performSavingApplicationActions(savingsOperationURL, getApproveSavingsAsJSON(), isBlock);
        return performSavingApplicationActions(savingsOperationURL, getApproveSavingsAsJsonOnDate(approvalDate), isBlock);
    }

    public HashMap undoApproval(final Integer savingsID) {
        System.out.println("--------------------------------- UNDO APPROVING SAVINGS APPLICATION -------------------------------");
        final String undoBodyJson = "{'note':'UNDO APPROVAL'}";
        return performSavingApplicationActions(createSavingsOperationURL(UNDO_APPROVAL_SAVINGS_COMMAND, savingsID), undoBodyJson, isBlock);
    }

    public HashMap rejectApplication(final Integer savingsID) {
        System.out.println("--------------------------------- REJECT SAVINGS APPLICATION -------------------------------");
        return performSavingApplicationActions(createSavingsOperationURL(REJECT_SAVINGS_COMMAND, savingsID),
                getRejectedSavingsAsJSON(CREATED_DATE_PLUS_ONE), isBlock);
    }

    public List rejectApplicationWithErrorCode(final Integer savingsId, final String date) {
        System.out.println("--------------------------------- REJECT SAVINGS APPLICATION -------------------------------");
        return (List) performSavingActions(createSavingsOperationURL(REJECT_SAVINGS_COMMAND, savingsId), getRejectedSavingsAsJSON(date),
                CommonConstants.RESPONSE_ERROR);
    }

    public HashMap withdrawApplication(final Integer savingsID) {
        System.out.println("--------------------------------- Withdraw SAVINGS APPLICATION -------------------------------");
        return performSavingApplicationActions(createSavingsOperationURL(WITHDRAWN_BY_CLIENT_SAVINGS_COMMAND, savingsID),
                getWithdrawnSavingsAsJSON(), isBlock);
    }

    public HashMap activateSavings(final Integer savingsID) {
        System.out.println("---------------------------------- ACTIVATING SAVINGS APPLICATION ----------------------------------");
        return performSavingApplicationActions(createSavingsOperationURL(ACTIVATE_SAVINGS_COMMAND, savingsID), getActivatedSavingsAsJSON(), isBlock);
    }

    public HashMap closeSavingsAccount(final Integer savingsID, String withdrawBalance) {
        System.out.println("---------------------------------- CLOSE SAVINGS APPLICATION ----------------------------------");
        return performSavingApplicationActions(createSavingsOperationURL(CLOSE_SAVINGS_COMMAND, savingsID),
                getCloseAccountJSON(withdrawBalance, LAST_TRANSACTION_DATE), isBlock);
    }

    public Object deleteSavingsApplication(final Integer savingsId, final String jsonAttributeToGetBack) {
        System.out.println("---------------------------------- DELETE SAVINGS APPLICATION ----------------------------------");
        return Utils.performServerDelete(this.requestSpec, this.responseSpec, SAVINGS_ACCOUNT_URL + "/" + savingsId + "?"
                + Utils.TENANT_IDENTIFIER, jsonAttributeToGetBack);

    }

    public Object depositToSavingsAccount(final Integer savingsID, final String amount, String date, String jsonAttributeToGetback) {
        System.out.println("--------------------------------- SAVINGS TRANSACTION DEPOSIT --------------------------------");
        return performSavingActions(createSavingsTransactionURL(DEPOSIT_SAVINGS_COMMAND, savingsID),
                getSavingsTransactionJSON(amount, date), jsonAttributeToGetback);
    }

    public Object withdrawalFromSavingsAccount(final Integer savingsId, final String amount, String date, String jsonAttributeToGetback) {
        System.out.println("\n--------------------------------- SAVINGS TRANSACTION WITHDRAWAL --------------------------------");
        return performSavingActions(createSavingsTransactionURL(WITHDRAW_SAVINGS_COMMAND, savingsId),
                getSavingsTransactionJSON(amount, date), jsonAttributeToGetback);
    }

    public Integer updateSavingsAccountTransaction(final Integer savingsId, final Integer transactionId, final String amount) {
        System.out.println("\n--------------------------------- MODIFY SAVINGS TRANSACTION  --------------------------------");
        return (Integer) performSavingActions(createAdjustTransactionURL(MODIFY_TRASACTION_COMMAND, savingsId, transactionId),
                getSavingsTransactionJSON(amount, LAST_TRANSACTION_DATE), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public Integer undoSavingsAccountTransaction(final Integer savingsId, final Integer transactionId) {
        System.out.println("\n--------------------------------- UNDO SAVINGS TRANSACTION  --------------------------------");
        return (Integer) performSavingActions(createAdjustTransactionURL(UNDO_TRASACTION_COMMAND, savingsId, transactionId),
                getSavingsTransactionJSON("0", LAST_TRANSACTION_DATE), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public void calculateInterestForSavings(final Integer savingsId) {
        System.out.println("--------------------------------- CALCULATING INTEREST FOR SAVINGS --------------------------------");
        performSavingActions(createSavingsCalculateInterestURL(CALCULATE_INTEREST_SAVINGS_COMMAND, savingsId),
                getCalculatedInterestForSavingsApplicationAsJSON(), "");
    }

    public void postInterestForSavings(final Integer savingsId) {
        System.out.println("--------------------------------- POST INTEREST FOR SAVINGS --------------------------------");
        performSavingActions(createSavingsCalculateInterestURL(POST_INTEREST_SAVINGS_COMMAND, savingsId),
                getCalculatedInterestForSavingsApplicationAsJSON(), "");
    }
    
    public void postInterestAsOnSavings(final Integer savingsId, final String today) {
        System.out.println("--------------------------------- POST INTEREST AS ON FOR SAVINGS --------------------------------");
        performSavingActions(createSavingsPostInterestAsOnURL(POST_INTEREST_AS_ON_SAVINGS_COMMAND, savingsId),
                getCalculatedInterestForSavingsApplicationAsJSON(today), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public Integer addChargesForSavings(final Integer savingsId, final Integer chargeId, boolean addDueDate) {
        System.out.println("--------------------------------- ADD CHARGES FOR SAVINGS --------------------------------");
        return (Integer) performSavingActions(SAVINGS_ACCOUNT_URL + "/" + savingsId + "/charges?" + Utils.TENANT_IDENTIFIER,
                getPeriodChargeRequestJSON(chargeId, addDueDate), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public Integer payCharge(final Integer chargeId, final Integer savingsId, String amount, String dueDate) {
        return (Integer) performSavingActions(createChargesURL("paycharge", savingsId, chargeId), getSavingsPayChargeJSON(amount, dueDate),
                CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public Integer waiveCharge(final Integer chargeId, final Integer savingsId) {
        return (Integer) performSavingActions(createChargesURL("waive", savingsId, chargeId), getSavingsWaiveChargeJSON(),
                CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public HashMap updateCharges(final Integer chargeId, final Integer savingsId) {
        return Utils.performServerPut(this.requestSpec, this.responseSpec, SAVINGS_ACCOUNT_URL + "/" + savingsId + "/charges/" + chargeId
                + "?" + Utils.TENANT_IDENTIFIER, getModifyChargeJSON(), CommonConstants.RESPONSE_CHANGES);
    }

    public Integer deleteCharge(final Integer chargeId, final Integer savingsId) {
        return Utils.performServerDelete(this.requestSpec, this.responseSpec, SAVINGS_ACCOUNT_URL + "/" + savingsId + "/charges/"
                + chargeId + "?" + Utils.TENANT_IDENTIFIER, CommonConstants.RESPONSE_RESOURCE_ID);
    }
    
    public HashMap blockSavings(final Integer savingsID) {
        System.out.println("---------------------------------- BLOCKING SAVINGS ACCOUNT ----------------------------------");
        Boolean isBlock = true;
        return performSavingApplicationActions(createSavingsOperationURL(BLOCK_SAVINGS_COMMAND, savingsID),
                getActivatedSavingsAsJSON(), isBlock);
    }

    public HashMap unblockSavings(final Integer savingsID) {
        System.out.println("---------------------------------- UNBLOCKING SAVINGS ACCOUNT ----------------------------------");
        Boolean isBlock = true;
        return performSavingApplicationActions(createSavingsOperationURL(UNBLOCK_SAVINGS_COMMAND, savingsID),
                getActivatedSavingsAsJSON(), isBlock);
    }

    public HashMap blockDebit(final Integer savingsID) {
        System.out.println("---------------------------------- BLOCKING DEBIT TRANSACTIONS ----------------------------------");
        Boolean isBlock = true;
        return performSavingApplicationActions(createSavingsOperationURL(BLOCK_DEBITS_SAVINGS_COMMAND, savingsID),
                getActivatedSavingsAsJSON(), isBlock);
    }

    public HashMap unblockDebit(final Integer savingsID) {
        System.out.println("---------------------------------- UNBLOCKING DEBIT TRANSACTIONS ----------------------------------");
        Boolean isBlock = true;
        return performSavingApplicationActions(createSavingsOperationURL(UNBLOCK_DEBITS_SAVINGS_COMMAND, savingsID),
                getActivatedSavingsAsJSON(), isBlock);
    }

    public HashMap blockCredit(final Integer savingsID) {
        System.out.println("---------------------------------- BLOCKING CREDIT TRANSACTIONS ----------------------------------");
        Boolean isBlock = true;
        return performSavingApplicationActions(createSavingsOperationURL(BLOCK_CREDITS_SAVINGS_COMMAND, savingsID),
                getActivatedSavingsAsJSON(), isBlock);
    }

    public HashMap unblockCredit(final Integer savingsID) {
        System.out.println("---------------------------------- UNBLOCKING CREDIT TRANSACTIONS ----------------------------------");
        Boolean isBlock = true;
        return performSavingApplicationActions(createSavingsOperationURL(UNBLOCK_CREDITS_SAVINGS_COMMAND, savingsID),
                getActivatedSavingsAsJSON(), isBlock);
    }

    public Object holdAmountInSavingsAccount(final Integer savingsID, final String amount, String date, String jsonAttributeToGetback) {
        System.out.println("--------------------------------- SAVINGS TRANSACTION HOLD AMOUNT--------------------------------");
        return performSavingActions(createSavingsTransactionURL(HOLD_AMOUNT_SAVINGS_COMMAND, savingsID),
                getSavingsTransactionJSON(amount, date), jsonAttributeToGetback);
    }

    public Integer releaseAmount(final Integer savingsId, final Integer transactionId) {
        System.out.println("\n--------------------------------- SAVINGS TRANSACTION RELEASE AMOUNT--------------------------------");
        return (Integer) performSavingActions(createAdjustTransactionURL(RELEASE_AMOUNT_SAVINGS_COMMAND, savingsId, transactionId),
                getSavingsTransactionJSON("1000", LAST_TRANSACTION_DATE), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    private String getApproveSavingsAsJSON() {
        return getApproveSavingsAsJsonOnDate(CREATED_DATE_PLUS_ONE);
    }

    private String getApproveSavingsAsJsonOnDate(final String approvalDate) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("approvedOnDate", approvalDate);
        map.put("note", "Approval NOTE");
        String savingsAccountApproveJson = new Gson().toJson(map);
        System.out.println(savingsAccountApproveJson);
        return savingsAccountApproveJson;
    }

    private String getRejectedSavingsAsJSON(final String rejectedOnDate) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("rejectedOnDate", rejectedOnDate);
        map.put("note", "Rejected NOTE");
        String savingsAccountJson = new Gson().toJson(map);
        System.out.println(savingsAccountJson);
        return savingsAccountJson;
    }

    private String getWithdrawnSavingsAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("withdrawnOnDate", CREATED_DATE_PLUS_ONE);
        map.put("note", "Rejected NOTE");
        String savingsAccountJson = new Gson().toJson(map);
        System.out.println(savingsAccountJson);
        return savingsAccountJson;
    }

    private String getActivatedSavingsAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("activatedOnDate", TRANSACTION_DATE);
        String savingsAccountActivateJson = new Gson().toJson(map);
        System.out.println(savingsAccountActivateJson);
        return savingsAccountActivateJson;
    }

    private String getSavingsTransactionJSON(final String amount, final String transactionDate) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("transactionDate", transactionDate);
        map.put("transactionAmount", amount);
        String savingsAccountWithdrawalJson = new Gson().toJson(map);
        System.out.println(savingsAccountWithdrawalJson);
        return savingsAccountWithdrawalJson;
    }

    private String getCalculatedInterestForSavingsApplicationAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        String savingsAccountCalculatedInterestJson = new Gson().toJson(map);
        System.out.println(savingsAccountCalculatedInterestJson);
        return savingsAccountCalculatedInterestJson;
    }
    
    private String getCalculatedInterestForSavingsApplicationAsJSON(final String today) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("transactionDate", today);
        map.put("postInterestManualOrAutomatic", "true");
        String savingsAccountCalculatedInterestJson = new Gson().toJson(map);
        return savingsAccountCalculatedInterestJson;
    }

    private String getSavingsPayChargeJSON(final String amount, final String dueDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("amount", amount);
        map.put("dueDate", dueDate);
        String josn = new Gson().toJson(map);
        return josn;
    }

    private String getSavingsWaiveChargeJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        String josn = new Gson().toJson(map);
        return josn;
    }

    private String getModifyChargeJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("amount", "50");
        String josn = new Gson().toJson(map);
        return josn;
    }

    private String getCloseAccountJSON(String withdrawBalance, String closedOnDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("closedOnDate", closedOnDate);
        map.put("withdrawBalance", withdrawBalance);
        map.put("note", "Close Test");
        
        String josn = new Gson().toJson(map);
        return josn;
    }
    
    private String getCloseAccountPostInterestJSON(String withdrawBalance, String closedOnDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("closedOnDate", closedOnDate);
        map.put("withdrawBalance", withdrawBalance);
        map.put("note", "Close Test");
        map.put("postInterestValidationOnClosure", "true");
        
        String josn = new Gson().toJson(map);
        return josn;
    }

    private String createSavingsOperationURL(final String command, final Integer savingsID) {
        return SAVINGS_ACCOUNT_URL + "/" + savingsID + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
    }

    private String createSavingsTransactionURL(final String command, final Integer savingsID) {
        return SAVINGS_ACCOUNT_URL + "/" + savingsID + "/transactions?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
    }

    private String createAdjustTransactionURL(final String command, final Integer savingsID, final Integer transactionId) {
        return SAVINGS_ACCOUNT_URL + "/" + savingsID + "/transactions/" + transactionId + "?command=" + command + "&"
                + Utils.TENANT_IDENTIFIER;
    }

    private String createSavingsCalculateInterestURL(final String command, final Integer savingsID) {
        return SAVINGS_ACCOUNT_URL + "/" + savingsID + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
    }
    
    private String createSavingsPostInterestAsOnURL(final String command, final Integer savingsID) {
        return SAVINGS_ACCOUNT_URL + "/" + savingsID + "/transactions/" + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
    }

    private String createChargesURL(final String command, final Integer savingsID, final Integer chargeId) {
        return SAVINGS_ACCOUNT_URL + "/" + savingsID + "/charges/" + chargeId + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
    }

    public ArrayList getSavingsCollectionAttribute(final Integer savingsID, final String jSONAttribute) {
        final String URL = SAVINGS_ACCOUNT_URL + "/" + savingsID + "?associations=all&" + Utils.TENANT_IDENTIFIER;
        final ArrayList<HashMap> response = Utils.performServerGet(requestSpec, responseSpec, URL, jSONAttribute);
        return response;
    }

    public Object getSavingsAccountDetail(final Integer savingsID, final String jsonAttribute) {
        final String URL = SAVINGS_ACCOUNT_URL + "/" + savingsID + "?associations=all&" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, URL, jsonAttribute);
    }

    public ArrayList getSavingsCharges(final Integer savingsID) {
        final String URL = SAVINGS_ACCOUNT_URL + "/" + savingsID + "/charges?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, URL, "");
    }

    public HashMap getSavingsCharge(final Integer savingsID, final Integer savingsChargeId) {
        final String URL = SAVINGS_ACCOUNT_URL + "/" + savingsID + "/charges/" + savingsChargeId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, URL, "");
    }

    public HashMap getSavingsTransaction(final Integer savingsID, final Integer savingsTransactionId) {
        final String URL = SAVINGS_ACCOUNT_URL + "/" + savingsID + "/transactions/" + savingsTransactionId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, URL, "");
    }

    public Object getSavingsInterest(final Integer savingsID) {
        final String URL = SAVINGS_ACCOUNT_URL + "/" + savingsID + "?associations=summary&" + Utils.TENANT_IDENTIFIER;
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, URL, "summary");
        return response.get("totalInterestEarned");
    }

    public HashMap getSavingsSummary(final Integer savingsID) {
        final String URL = SAVINGS_ACCOUNT_URL + "/" + savingsID + "?associations=summary&" + Utils.TENANT_IDENTIFIER;
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, URL, "summary");
        return response;
    }

    public HashMap getSavingsDetails(final Integer savingsID) {
        final String URL = SAVINGS_ACCOUNT_URL + "/" + savingsID + "?associations=all&" + Utils.TENANT_IDENTIFIER;
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, URL, "");
        return response;
    }

    public Object getSavingsDetails(final Integer savingsID, final String returnAttribute) {
        final String URL = SAVINGS_ACCOUNT_URL + "/" + savingsID + "?" + Utils.TENANT_IDENTIFIER;
        final Object response = Utils.performServerGet(requestSpec, responseSpec, URL, returnAttribute);
        return response;
    }

    private HashMap performSavingApplicationActions(final String postURLForSavingsTransaction, final String jsonToBeSent, final Boolean isBlock) {
        HashMap status = null;
        final HashMap response = Utils.performServerPost(this.requestSpec, this.responseSpec, postURLForSavingsTransaction, jsonToBeSent,
                CommonConstants.RESPONSE_CHANGES);
        if (response != null) {
            status = (HashMap) response.get("status");
            if (isBlock != null && isBlock) {
                status = (HashMap) response.get("subStatus");
            }
        }
        return status;
    }

    private Object performSavingActions(final String postURLForSavingsTransaction, final String jsonToBeSent,
            final String jsonAttributeToGetBack) {
        return Utils.performServerPost(this.requestSpec, this.responseSpec, postURLForSavingsTransaction, jsonToBeSent,
                jsonAttributeToGetBack);
    }

    public Object closeSavingsAccountAndGetBackRequiredField(final Integer savingsID, String withdrawBalance,
            final String jsonAttributeToGetBack, final String closedOnDate) {
        System.out.println("---------------------------------- CLOSE SAVINGS APPLICATION ----------------------------------");
        return performSavingActions(createSavingsOperationURL(CLOSE_SAVINGS_COMMAND, savingsID),
                getCloseAccountJSON(withdrawBalance, closedOnDate), jsonAttributeToGetBack);
    }
    
    public Object closeSavingsAccountPostInterestAndGetBackRequiredField(final Integer savingsID, String withdrawBalance,
            final String jsonAttributeToGetBack, final String closedOnDate) {
        System.out.println("---------------------------------- CLOSE SAVINGS APPLICATION ----------------------------------");
        return performSavingActions(createSavingsOperationURL(CLOSE_SAVINGS_COMMAND, savingsID),
                getCloseAccountPostInterestJSON(withdrawBalance, closedOnDate), jsonAttributeToGetBack);
    }

    private String getPeriodChargeRequestJSON(Integer chargeId, boolean addDueDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("chargeId", chargeId);
        map.put("amount", 100);
        map.put("feeOnMonthDay", "15 January");
        map.put("locale", CommonConstants.locale);
        map.put("monthDayFormat", "dd MMMM");
        map.put("dateFormat", "dd MMMM yyy");
        if(addDueDate){
            map.put("dueDate", "10 January 2013");
        }
        String json = new Gson().toJson(map);
        return json;
    }

    private String getAccountActivationJSON(final String activationDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("activatedOnDate", activationDate);
        String savingsAccountActivateJson = new Gson().toJson(map);
        return savingsAccountActivateJson;
    }

    public HashMap activateSavingsAccount(Integer savingsID, String activationDate) {
        return performSavingApplicationActions(createSavingsOperationURL(ACTIVATE_SAVINGS_COMMAND, savingsID),
                getAccountActivationJSON(activationDate), isBlock);
    }

    public Object inactivateCharge(final Integer chargeId, final Integer savingsId, final String jsonAttributeToGetBack) {
        return performSavingActions(createChargesURL("inactivate", savingsId, chargeId), getSavingsInactivateChargeJSON(),
                jsonAttributeToGetBack);
    }

    private String getSavingsInactivateChargeJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        String josn = new Gson().toJson(map);
        return josn;
    }

    public static Integer openSavingsAccount(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer clientId, final String minimumOpeningBalance) {
        final Integer savingsProductID = createSavingsProduct(requestSpec, responseSpec, minimumOpeningBalance);
        Assert.assertNotNull(savingsProductID);

        SavingsAccountHelper savingsAccountHelper = new SavingsAccountHelper(requestSpec, responseSpec);

        final Integer savingsId = savingsAccountHelper.applyForSavingsApplication(clientId, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(requestSpec, responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        return savingsId;
    }

    private static Integer createSavingsProduct(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String minOpenningBalance) {
        System.out.println("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        SavingsProductHelper savingsProductHelper = new SavingsProductHelper();
        final String savingsProductJSON = savingsProductHelper //
                .withInterestCompoundingPeriodTypeAsDaily() //
                .withInterestPostingPeriodTypeAsMonthly() //
                .withInterestCalculationPeriodTypeAsDailyBalance() //
                .withMinimumOpenningBalance(minOpenningBalance).build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
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