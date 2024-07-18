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

import static org.apache.fineract.integrationtests.common.Utils.DEFAULT_TENANT;
import static org.apache.fineract.integrationtests.common.Utils.TENANT_PARAM_NAME;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.fineract.client.models.GetSavingsAccountsAccountIdResponse;
import org.apache.fineract.client.models.PagedLocalRequestAdvancedQueryRequest;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsRequest;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsResponse;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdRequest;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdResponse;
import org.apache.fineract.client.models.PostSavingsAccountsRequest;
import org.apache.fineract.client.models.PostSavingsAccountsResponse;
import org.apache.fineract.client.models.SavingsAccountTransactionsSearchResponse;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "rawtypes" })
public class SavingsAccountHelper extends IntegrationTest {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;
    private static final Gson GSON = new JSON().getGson();
    private static final Logger LOG = LoggerFactory.getLogger(SavingsAccountHelper.class);

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
    private static final String GSIM_SAVINGS = "/gsim";
    private static final String GSIM_SAVINGS_COMMAND = "/gsimcommands";
    private static final String GSIM_DEPOSIT_SAVINGS_COMMAND = "gsimDeposit";
    private static final String MODIFY_TRASACTION_COMMAND = "modify";
    private static final String UNDO_TRASACTION_COMMAND = "undo";
    private static final String REVERSE_TRASACTION_COMMAND = "reverse";

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
    public static final String TRANSACTION_DATE_PLUS_ONE = "02 March 2013";
    public static final String LAST_TRANSACTION_DATE = "01 March 2013";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final Long PAYMENT_TYPE_ID = 1L;

    public static final String DATE_TIME_FORMAT = "dd MMMM yyyy HH:mm";
    private static final Boolean IS_BLOCK = false;

    public SavingsAccountHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public RequestSpecification getRequestSpec() {
        return requestSpec;
    }

    public ResponseSpecification getResponseSpec() {
        return responseSpec;
    }

    public static String getFutureDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(CommonConstants.DATE_FORMAT, Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
        return sdf.format(calendar.getTime());
    }

    public Integer applyForSavingsApplication(final Integer clientOrGroupId, final Integer savingsProductID, final String accountType) {
        return applyForSavingsApplicationOnDate(clientOrGroupId, savingsProductID, accountType, CREATED_DATE);
    }

    public Integer applyForSavingsApplicationOnDate(final Integer clientOrGroupId, final Integer savingsProductID, final String accountType,
            final String submittedOnDate) {
        return applyForSavingsApplicationOnDate(clientOrGroupId, savingsProductID, accountType, null, false, submittedOnDate);
    }

    public Integer applyForSavingsApplicationWithExternalId(final Integer clientOrGroupId, final Integer savingsProductID,
            final String accountType, String externalId, boolean withdrawalFeeForTransfers) {
        return applyForSavingsApplicationOnDate(clientOrGroupId, savingsProductID, accountType, externalId, withdrawalFeeForTransfers,
                CREATED_DATE);
    }

    public Integer applyForSavingsApplicationOnDate(final Integer clientOrGroupId, final Integer savingsProductID, final String accountType,
            String externalId, boolean withdrawalFeeForTransfers, final String submittedOnDate) {
        final String savingsApplicationJSON = new SavingsApplicationTestBuilder() //
                .withExternalId(externalId) //
                .withWithdrawalFeeForTransfers(withdrawalFeeForTransfers) //
                .withSubmittedOnDate(submittedOnDate) //
                .build(clientOrGroupId.toString(), savingsProductID.toString(), accountType);
        return applyForSavingsApplicationOnDate(savingsApplicationJSON);
    }

    public Integer applyForSavingsApplicationOnDate(String savingsApplicationJson) {
        LOG.info("--------------------------------APPLYING FOR SAVINGS APPLICATION--------------------------------");
        return Utils.performServerPost(this.requestSpec, this.responseSpec, SAVINGS_ACCOUNT_URL + "?" + Utils.TENANT_IDENTIFIER,
                savingsApplicationJson, "savingsId");
    }

    public Integer applyForSavingsApplicationWithDatatables(final Integer id, final Integer savingsProductID, final String accountType,
            final String submittedOnDate, final String datatableName) {
        LOG.info("----------------------------APPLYING FOR SAVINGS APPLICATION WITH DATATABLES----------------------------");
        final String savingsApplicationJSON = new SavingsApplicationTestBuilder() //
                .withSubmittedOnDate(submittedOnDate) //
                .withDatatables(getTestDatatableAsJson(datatableName)) //
                .build(id.toString(), savingsProductID.toString(), accountType);
        return Utils.performServerPost(this.requestSpec, this.responseSpec, SAVINGS_ACCOUNT_URL + "?" + Utils.TENANT_IDENTIFIER,
                savingsApplicationJSON, "savingsId");
    }

    public Object applyForSavingsApplicationWithFailure(final Integer id, final Integer savingsProductID, final String accountType,
            final String submittedOnDate, final String responseAttribute) {
        LOG.info("----------------------------APPLYING FOR SAVINGS APPLICATION WITH ERROR----------------------------");
        final String savingsApplicationJSON = new SavingsApplicationTestBuilder() //
                .withSubmittedOnDate(submittedOnDate) //
                .build(id.toString(), savingsProductID.toString(), accountType);
        return Utils.performServerPost(this.requestSpec, this.responseSpec, SAVINGS_ACCOUNT_URL + "?" + Utils.TENANT_IDENTIFIER,
                savingsApplicationJSON, responseAttribute);
    }

    public Integer createApproveActivateSavingsAccount(final Integer clientId, Integer savingsProductId, final String startDate) {
        final Integer savingsId = applyForSavingsApplicationOnDate(clientId, savingsProductId, ACCOUNT_TYPE_INDIVIDUAL, startDate);
        assertNotNull(savingsId);
        HashMap savingsStatusHashMap = approveSavingsOnDate(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);
        savingsStatusHashMap = activateSavingsAccount(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        return savingsId;
    }

    public HashMap updateSavingsAccount(final Integer id, final Integer savingsProductID, final Integer savingsId,
            final String accountType) {
        final String savingsApplicationJSON = new SavingsApplicationTestBuilder() //
                .withSubmittedOnDate(CREATED_DATE_PLUS_ONE) //
                .build(id.toString(), savingsProductID.toString(), accountType);

        return Utils.performServerPut(this.requestSpec, this.responseSpec,
                SAVINGS_ACCOUNT_URL + "/" + savingsId + "?" + Utils.TENANT_IDENTIFIER, savingsApplicationJSON,
                CommonConstants.RESPONSE_CHANGES);
    }

    // GLIM_GSIM_TESTING
    public Integer applyForGsimApplication(List<Map<String, Object>> clientArray) {
        LOG.info("----------------------------APPLYING FOR GSIM SAVINGS APPLICATION----------------------------");
        LOG.info("clientArray is : {} ", clientArray);
        String clientArrays = new SavingsApplicationTestBuilder() //
                .withClientArray(clientArray).build();
        return SavingsAccountHelper.applyForGsimApplication(clientArrays, requestSpec, responseSpec);
    }

    public static Integer applyForGsimApplication(final String clientArrays, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        return Utils.performServerPost(requestSpec, responseSpec, SAVINGS_ACCOUNT_URL + GSIM_SAVINGS + "?" + Utils.TENANT_IDENTIFIER,
                clientArrays, "gsimId");
    }

    public HashMap updateSavingsAccountWithHoldTaxStatus(final Integer savingsId, final boolean value) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("withHoldTax", value);
        String json = new Gson().toJson(map);

        return Utils.performServerPut(this.requestSpec, this.responseSpec,
                SAVINGS_ACCOUNT_URL + "/" + savingsId + "?command=" + UPDATE_WITHHOLD_TAX_STATUS + "&" + Utils.TENANT_IDENTIFIER, json,
                CommonConstants.RESPONSE_CHANGES);
    }

    public HashMap approveSavings(final Integer savingsID) {
        return approveSavingsOnDate(savingsID, null);
    }

    public HashMap approveSavingsOnDate(final Integer savingsID, final String approvalDate) {
        LOG.info("--------------------------------- APPROVING SAVINGS APPLICATION ------------------------------------");
        final String savingsOperationURL = createSavingsOperationURL(APPROVE_SAVINGS_COMMAND, savingsID);
        if (approvalDate == null || approvalDate.equals("")) {
            return performSavingApplicationActions(savingsOperationURL, getApproveSavingsAsJSON(), IS_BLOCK);
        }
        return performSavingApplicationActions(savingsOperationURL, getApproveSavingsAsJsonOnDate(approvalDate), IS_BLOCK);
    }

    public HashMap undoApproval(final Integer savingsID) {
        LOG.info("--------------------------------- UNDO APPROVING SAVINGS APPLICATION -------------------------------");
        final String undoBodyJson = "{'note':'UNDO APPROVAL'}";
        return performSavingApplicationActions(createSavingsOperationURL(UNDO_APPROVAL_SAVINGS_COMMAND, savingsID), undoBodyJson, IS_BLOCK);
    }

    public HashMap rejectApplication(final Integer savingsID) {
        LOG.info("--------------------------------- REJECT SAVINGS APPLICATION -------------------------------");
        return performSavingApplicationActions(createSavingsOperationURL(REJECT_SAVINGS_COMMAND, savingsID),
                getRejectedSavingsAsJSON(CREATED_DATE_PLUS_ONE), IS_BLOCK);
    }

    public List rejectApplicationWithErrorCode(final Integer savingsId, final String date) {
        LOG.info("--------------------------------- REJECT SAVINGS APPLICATION -------------------------------");
        return (List) performSavingActions(createSavingsOperationURL(REJECT_SAVINGS_COMMAND, savingsId), getRejectedSavingsAsJSON(date),
                CommonConstants.RESPONSE_ERROR);
    }

    public HashMap withdrawApplication(final Integer savingsID) {
        LOG.info("--------------------------------- Withdraw SAVINGS APPLICATION -------------------------------");
        return performSavingApplicationActions(createSavingsOperationURL(WITHDRAWN_BY_CLIENT_SAVINGS_COMMAND, savingsID),
                getWithdrawnSavingsAsJSON(), IS_BLOCK);
    }

    public HashMap activateSavings(final Integer savingsID) {
        LOG.info("---------------------------------- ACTIVATING SAVINGS APPLICATION ----------------------------------");
        return performSavingApplicationActions(createSavingsOperationURL(ACTIVATE_SAVINGS_COMMAND, savingsID), getActivatedSavingsAsJSON(),
                IS_BLOCK);
    }

    public HashMap activateSavings(final Integer savingsID, final String activationDate) {
        LOG.info("---------------------------------- ACTIVATING SAVINGS APPLICATION ----------------------------------");
        return performSavingApplicationActions(createSavingsOperationURL(ACTIVATE_SAVINGS_COMMAND, savingsID),
                getActivatedSavingsAsJSONOnDate(activationDate), IS_BLOCK);
    }

    public HashMap closeSavingsAccount(final Integer savingsID, String withdrawBalance) {
        LOG.info("---------------------------------- CLOSE SAVINGS APPLICATION ----------------------------------");
        return performSavingApplicationActions(createSavingsOperationURL(CLOSE_SAVINGS_COMMAND, savingsID),
                getCloseAccountJSON(withdrawBalance, LAST_TRANSACTION_DATE), IS_BLOCK);
    }

    public HashMap closeSavingsAccountOnDate(final Integer savingsID, String withdrawBalance, final String closedOnDate) {
        LOG.info("---------------------------------- CLOSE SAVINGS APPLICATION ----------------------------------");
        return performSavingApplicationActions(createSavingsOperationURL(CLOSE_SAVINGS_COMMAND, savingsID),
                getCloseAccountJSON(withdrawBalance, closedOnDate), IS_BLOCK);
    }

    public Object deleteSavingsApplication(final Integer savingsId, final String jsonAttributeToGetBack) {
        LOG.info("---------------------------------- DELETE SAVINGS APPLICATION ----------------------------------");
        return Utils.performServerDelete(this.requestSpec, this.responseSpec,
                SAVINGS_ACCOUNT_URL + "/" + savingsId + "?" + Utils.TENANT_IDENTIFIER, jsonAttributeToGetBack);

    }

    public Object depositToSavingsAccount(final Integer savingsID, final String amount, String date, String jsonAttributeToGetback) {
        LOG.info("--------------------------------- SAVINGS TRANSACTION DEPOSIT --------------------------------");
        return depositToSavingsAccount(savingsID, getSavingsTransactionJSON(amount, date), jsonAttributeToGetback);
    }

    public Object depositToSavingsAccount(final Integer savingsID, final String jsonBody, String jsonAttributeToGetback) {
        LOG.info("--------------------------------- SAVINGS TRANSACTION DEPOSIT --------------------------------");
        return performSavingActions(createSavingsTransactionURL(DEPOSIT_SAVINGS_COMMAND, savingsID), jsonBody, jsonAttributeToGetback);
    }

    public Object withdrawalFromSavingsAccount(final Integer savingsId, final String amount, String date, String jsonAttributeToGetback) {
        LOG.info("\n--------------------------------- SAVINGS TRANSACTION WITHDRAWAL --------------------------------");
        return withdrawalFromSavingsAccount(savingsId, getSavingsTransactionJSON(amount, date), jsonAttributeToGetback);
    }

    public Object withdrawalFromSavingsAccountWithPaymentType(final Integer savingsId, final String amount, String date, Long paymentTypeId,
            String jsonAttributeToGetback) {
        LOG.info("\n--------------------------------- SAVINGS TRANSACTION WITHDRAWAL WITH PAYMENT TYPE--------------------------------");
        return withdrawalFromSavingsAccount(savingsId, getSavingsTransactionPaymentTypeJSON(amount, date, paymentTypeId),
                jsonAttributeToGetback);
    }

    public Object withdrawalFromSavingsAccount(final Integer savingsId, final String jsonBody, String jsonAttributeToGetback) {
        LOG.info("\n--------------------------------- SAVINGS TRANSACTION WITHDRAWAL --------------------------------");
        return performSavingActions(createSavingsTransactionURL(WITHDRAW_SAVINGS_COMMAND, savingsId), jsonBody, jsonAttributeToGetback);
    }

    public Object payChargeToSavingsAccount(final Integer savingsID, final Integer chargeId, final String amount, String date,
            String jsonAttributeToGetback) {
        LOG.info("--------------------------------- PAY SAVINGS CHARGE --------------------------------");
        return performSavingActions(createChargesURL("paycharge", savingsID, chargeId), getSavingsPayChargeJSON(amount, date),
                jsonAttributeToGetback);
    }

    public Integer updateSavingsAccountTransaction(final Integer savingsId, final Integer transactionId, final String amount) {
        LOG.info("\n--------------------------------- MODIFY SAVINGS TRANSACTION  --------------------------------");
        return (Integer) performSavingActions(createAdjustTransactionURL(MODIFY_TRASACTION_COMMAND, savingsId, transactionId),
                getSavingsTransactionJSON(amount, LAST_TRANSACTION_DATE), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public Integer undoSavingsAccountTransaction(final Integer savingsId, final Integer transactionId) {
        LOG.info("\n--------------------------------- UNDO SAVINGS TRANSACTION  --------------------------------");
        return (Integer) performSavingActions(createAdjustTransactionURL(UNDO_TRASACTION_COMMAND, savingsId, transactionId),
                getSavingsTransactionJSON("0", LAST_TRANSACTION_DATE), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public Integer reverseSavingsAccountTransaction(final Integer savingsId, final Integer transactionId) {
        LOG.info("\n--------------------------------- REVERSE SAVINGS TRANSACTION  --------------------------------");
        return (Integer) performSavingActions(createAdjustTransactionURL(REVERSE_TRASACTION_COMMAND, savingsId, transactionId),
                getSavingsTransactionJSON("0", LAST_TRANSACTION_DATE), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public Integer reverseSavingsAccountTransaction(final Integer savingsId, final Integer transactionId, final boolean isBulk) {
        LOG.info("\n--------------------------------- REVERSE SAVINGS TRANSACTION  --------------------------------");
        return (Integer) performSavingActions(createAdjustTransactionURL(REVERSE_TRASACTION_COMMAND, savingsId, transactionId),
                getSavingsTransactionJSON("0", LAST_TRANSACTION_DATE, isBulk), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public void calculateInterestForSavings(final Integer savingsId) {
        LOG.info("--------------------------------- CALCULATING INTEREST FOR SAVINGS --------------------------------");
        performSavingActions(createSavingsCalculateInterestURL(CALCULATE_INTEREST_SAVINGS_COMMAND, savingsId),
                getCalculatedInterestForSavingsApplicationAsJSON(), "");
    }

    public void postInterestForSavings(final Integer savingsId) {
        LOG.info("--------------------------------- POST INTEREST FOR SAVINGS --------------------------------");
        performSavingActions(createSavingsCalculateInterestURL(POST_INTEREST_SAVINGS_COMMAND, savingsId),
                getCalculatedInterestForSavingsApplicationAsJSON(), "");
    }

    public void postInterestAsOnSavings(final Integer savingsId, final String today) {
        LOG.info("--------------------------------- POST INTEREST AS ON FOR SAVINGS --------------------------------");
        performSavingActions(createSavingsPostInterestAsOnURL(POST_INTEREST_AS_ON_SAVINGS_COMMAND, savingsId),
                getCalculatedInterestForSavingsApplicationAsJSON(today), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public Integer addChargesForSavings(final Integer savingsId, final Integer chargeId, boolean addDueDate) {
        return addChargesForSavings(savingsId, chargeId, addDueDate, BigDecimal.valueOf(100));
    }

    public Integer addChargesForSavings(final Integer savingsId, final Integer chargeId, boolean addDueDate, BigDecimal amount) {
        LOG.info("--------------------------------- ADD CHARGES FOR SAVINGS --------------------------------");
        return (Integer) performSavingActions(SAVINGS_ACCOUNT_URL + "/" + savingsId + "/charges?" + Utils.TENANT_IDENTIFIER,
                getPeriodChargeRequestJSON(chargeId, addDueDate, amount), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public Integer addChargesForSavingsWithDueDate(final Integer savingsId, final Integer chargeId, String addDueDate, Integer amount) {
        return (Integer) performSavingActions(SAVINGS_ACCOUNT_URL + "/" + savingsId + "/charges?" + Utils.TENANT_IDENTIFIER,
                getPeriodChargeRequestJSONWithDueDate(chargeId, addDueDate, amount), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public Integer addChargesForSavingsWithDueDateAndFeeOnMonthDay(final Integer savingsId, final Integer chargeId, String addDueDate,
            Integer amount, String feeOnMonthDay) {
        return (Integer) performSavingActions(SAVINGS_ACCOUNT_URL + "/" + savingsId + "/charges?" + Utils.TENANT_IDENTIFIER,
                getPeriodChargeRequestJSONWithDueDateAndFeeOnMonthDay(chargeId, addDueDate, amount, feeOnMonthDay),
                CommonConstants.RESPONSE_RESOURCE_ID);
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
        return Utils.performServerPut(this.requestSpec, this.responseSpec,
                SAVINGS_ACCOUNT_URL + "/" + savingsId + "/charges/" + chargeId + "?" + Utils.TENANT_IDENTIFIER, getModifyChargeJSON(),
                CommonConstants.RESPONSE_CHANGES);
    }

    public Integer deleteCharge(final Integer chargeId, final Integer savingsId) {
        return Utils.performServerDelete(this.requestSpec, this.responseSpec,
                SAVINGS_ACCOUNT_URL + "/" + savingsId + "/charges/" + chargeId + "?" + Utils.TENANT_IDENTIFIER,
                CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public HashMap blockSavings(final Integer savingsID) {
        LOG.info("---------------------------------- BLOCKING SAVINGS ACCOUNT ----------------------------------");
        Boolean isBlock = true;
        return performSavingApplicationActions(createSavingsOperationURL(BLOCK_SAVINGS_COMMAND, savingsID),
                getActivatedSavingsAsForHoldJSON(), isBlock);
    }

    public HashMap unblockSavings(final Integer savingsID) {
        LOG.info("---------------------------------- UNBLOCKING SAVINGS ACCOUNT ----------------------------------");
        Boolean isBlock = true;
        return performSavingApplicationActions(createSavingsOperationURL(UNBLOCK_SAVINGS_COMMAND, savingsID), getActivatedSavingsAsJSON(),
                isBlock);
    }

    public HashMap blockDebit(final Integer savingsID) {
        LOG.info("---------------------------------- BLOCKING DEBIT TRANSACTIONS ----------------------------------");
        Boolean isBlock = true;
        return performSavingApplicationActions(createSavingsOperationURL(BLOCK_DEBITS_SAVINGS_COMMAND, savingsID),
                getActivatedSavingsAsForHoldJSON(), isBlock);
    }

    public HashMap unblockDebit(final Integer savingsID) {
        LOG.info("---------------------------------- UNBLOCKING DEBIT TRANSACTIONS ----------------------------------");
        Boolean isBlock = true;
        return performSavingApplicationActions(createSavingsOperationURL(UNBLOCK_DEBITS_SAVINGS_COMMAND, savingsID),
                getActivatedSavingsAsJSON(), isBlock);
    }

    public HashMap blockCredit(final Integer savingsID) {
        LOG.info("---------------------------------- BLOCKING CREDIT TRANSACTIONS ----------------------------------");
        Boolean isBlock = true;
        return performSavingApplicationActions(createSavingsOperationURL(BLOCK_CREDITS_SAVINGS_COMMAND, savingsID),
                getActivatedSavingsAsForHoldJSON(), isBlock);
    }

    public HashMap unblockCredit(final Integer savingsID) {
        LOG.info("---------------------------------- UNBLOCKING CREDIT TRANSACTIONS ----------------------------------");
        Boolean isBlock = true;
        return performSavingApplicationActions(createSavingsOperationURL(UNBLOCK_CREDITS_SAVINGS_COMMAND, savingsID),
                getActivatedSavingsAsJSON(), isBlock);
    }

    public Object holdAmountInSavingsAccount(final Integer savingsID, final String amount, final Boolean lienAllowed, String date,
            String jsonAttributeToGetback) {
        LOG.info("--------------------------------- SAVINGS TRANSACTION HOLD AMOUNT--------------------------------");

        return performSavingActions(createSavingsTransactionURL(HOLD_AMOUNT_SAVINGS_COMMAND, savingsID),
                getLienSavingsTransactionJSON(amount, date, lienAllowed), jsonAttributeToGetback);
    }

    public Integer releaseAmount(final Integer savingsId, final Integer transactionId) {
        LOG.info("\n--------------------------------- SAVINGS TRANSACTION RELEASE AMOUNT--------------------------------");
        return (Integer) performSavingActions(createAdjustTransactionURL(RELEASE_AMOUNT_SAVINGS_COMMAND, savingsId, transactionId),
                getSavingsTransactionJSON("1000", LAST_TRANSACTION_DATE), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    private String getApproveSavingsAsJSON() {
        return getApproveSavingsAsJsonOnDate(CREATED_DATE_PLUS_ONE);
    }

    private String getApproveSavingsAsJsonOnDate(final String approvalDate) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("approvedOnDate", approvalDate);
        map.put("note", "Approval NOTE");
        String savingsAccountApproveJson = new Gson().toJson(map);
        LOG.info(savingsAccountApproveJson);
        return savingsAccountApproveJson;
    }

    private String getRejectedSavingsAsJSON(final String rejectedOnDate) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("rejectedOnDate", rejectedOnDate);
        map.put("note", "Rejected NOTE");
        String savingsAccountJson = new Gson().toJson(map);
        LOG.info(savingsAccountJson);
        return savingsAccountJson;
    }

    private String getWithdrawnSavingsAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("withdrawnOnDate", CREATED_DATE_PLUS_ONE);
        map.put("note", "Rejected NOTE");
        String savingsAccountJson = new Gson().toJson(map);
        LOG.info(savingsAccountJson);
        return savingsAccountJson;
    }

    private String getActivatedSavingsAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("activatedOnDate", TRANSACTION_DATE);
        String savingsAccountActivateJson = new Gson().toJson(map);
        LOG.info(savingsAccountActivateJson);
        return savingsAccountActivateJson;
    }

    private String getActivatedSavingsAsJSONOnDate(final String activationDate) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("activatedOnDate", activationDate);
        String savingsAccountActivateJson = new Gson().toJson(map);
        LOG.info(savingsAccountActivateJson);
        return savingsAccountActivateJson;
    }

    private String getActivatedSavingsAsForHoldJSON() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("activatedOnDate", TRANSACTION_DATE);
        map.put("reasonForBlock", "unUsualActivity");
        String savingsAccountActivateJson = new Gson().toJson(map);
        LOG.info(savingsAccountActivateJson);
        return savingsAccountActivateJson;
    }

    private String getSavingsTransactionJSON(final String amount, final String transactionDate) {
        return SavingsTransactionData.builder().transactionDate(transactionDate).transactionAmount(amount).paymentTypeId(PAYMENT_TYPE_ID)
                .build().getJson();
    }

    private String getSavingsTransactionJSON(final String amount, final String transactionDate, final boolean isBulk) {
        return SavingsTransactionData.builder().transactionDate(transactionDate).transactionAmount(amount).isBulk(isBulk).build().getJson();
    }

    private String getLienSavingsTransactionJSON(final String amount, final String transactionDate, final Boolean lienAllowed) {
        return SavingsTransactionData.builder().transactionDate(transactionDate).transactionAmount(amount).lienAllowed(lienAllowed)
                .reasonForBlock("unUsualActivity").build().getJson();
    }

    private String getSavingsTransactionPaymentTypeJSON(final String amount, final String transactionDate, final Long paymentTypeId) {
        return SavingsTransactionData.builder().transactionDate(transactionDate).transactionAmount(amount).paymentTypeId(paymentTypeId)
                .build().getJson();
    }

    private String getCalculatedInterestForSavingsApplicationAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        String savingsAccountCalculatedInterestJson = new Gson().toJson(map);
        LOG.info(savingsAccountCalculatedInterestJson);
        return savingsAccountCalculatedInterestJson;
    }

    private String getCalculatedInterestForSavingsApplicationAsJSON(final String today) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("transactionDate", today);
        map.put("postInterestManualOrAutomatic", "true");
        String savingsAccountCalculatedInterestJson = new Gson().toJson(map);
        return savingsAccountCalculatedInterestJson;
    }

    private String getSavingsPayChargeJSON(final String amount, final String dueDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("amount", amount);
        map.put("dueDate", dueDate);
        String josn = new Gson().toJson(map);
        return josn;
    }

    private String getSavingsWaiveChargeJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        String josn = new Gson().toJson(map);
        return josn;
    }

    private String getModifyChargeJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("amount", "50");
        String josn = new Gson().toJson(map);
        return josn;
    }

    private String getCloseAccountJSON(String withdrawBalance, String closedOnDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("closedOnDate", closedOnDate);
        map.put("withdrawBalance", withdrawBalance);
        map.put("note", "Close Test");

        String josn = new Gson().toJson(map);
        return josn;
    }

    private String getCloseAccountPostInterestJSON(String withdrawBalance, String closedOnDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("closedOnDate", closedOnDate);
        map.put("withdrawBalance", withdrawBalance);
        map.put("note", "Close Test");
        map.put("postInterestValidationOnClosure", "true");

        String josn = new Gson().toJson(map);
        return josn;
    }

    private String updateGsimJSON(String clientID, String groupID, String productID) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("clientId", clientID);
        map.put("groupId", groupID);
        map.put("productId", productID);
        String savingsAccountJson = new Gson().toJson(map);
        LOG.info(savingsAccountJson);
        return savingsAccountJson;
    }

    private String createSavingsOperationURL(final String command, final Integer savingsID) {
        return SAVINGS_ACCOUNT_URL + "/" + savingsID + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
    }

    private String createSavingsGsimOperationURL(final String command, final Integer gsimID) {
        return SAVINGS_ACCOUNT_URL + GSIM_SAVINGS_COMMAND + "/" + gsimID + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
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

    public SavingsAccountTransactionsSearchResponse searchSavingsTransactions(Integer savingsId, Map<String, Object> queryParams) {
        final String url = SAVINGS_ACCOUNT_URL + "/" + savingsId + "/transactions/search";
        queryParams.put(TENANT_PARAM_NAME, DEFAULT_TENANT);
        requestSpec.queryParams(queryParams);
        String response = Utils.performServerGet(this.requestSpec, this.responseSpec, url);
        return GSON.fromJson(response, SavingsAccountTransactionsSearchResponse.class);
    }

    public Map<String, Object> querySavingsTransactions(Integer savingsId, PagedLocalRequestAdvancedQueryRequest request) {
        String response = ok(fineract().savingsTransactions.advancedQuery1(savingsId.longValue(), request));
        return JsonPath.from(response).get("");
    }

    public List<HashMap> getSavingsTransactions(final Integer savingsID) {
        final Object get = getSavingsCollectionAttribute(savingsID, "transactions");
        final String json = new Gson().toJson(get);
        return new Gson().fromJson(json, new TypeToken<ArrayList<HashMap>>() {}.getType());
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
        final String URL = SAVINGS_ACCOUNT_URL + "/" + savingsID + "?associations=all&" + Utils.TENANT_IDENTIFIER;
        final Object response = Utils.performServerGet(requestSpec, responseSpec, URL, returnAttribute);
        return response;
    }

    private HashMap performSavingApplicationActions(final String postURLForSavingsTransaction, final String jsonToBeSent,
            final Boolean isBlock) {
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
        LOG.info("---------------------------------- CLOSE SAVINGS APPLICATION ----------------------------------");
        return performSavingActions(createSavingsOperationURL(CLOSE_SAVINGS_COMMAND, savingsID),
                getCloseAccountJSON(withdrawBalance, closedOnDate), jsonAttributeToGetBack);
    }

    public Object closeSavingsAccountPostInterestAndGetBackRequiredField(final Integer savingsID, String withdrawBalance,
            final String jsonAttributeToGetBack, final String closedOnDate) {
        LOG.info("---------------------------------- CLOSE SAVINGS APPLICATION ----------------------------------");
        return performSavingActions(createSavingsOperationURL(CLOSE_SAVINGS_COMMAND, savingsID),
                getCloseAccountPostInterestJSON(withdrawBalance, closedOnDate), jsonAttributeToGetBack);
    }

    private String getPeriodChargeRequestJSON(Integer chargeId, boolean addDueDate, BigDecimal amount) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("chargeId", chargeId);
        map.put("amount", amount);
        map.put("feeOnMonthDay", "15 January");
        map.put("locale", CommonConstants.LOCALE);
        map.put("monthDayFormat", "dd MMMM");
        map.put("dateFormat", "dd MMMM yyy");
        if (addDueDate) {
            map.put("dueDate", "10 January 2013");
        }
        String json = new Gson().toJson(map);
        return json;
    }

    private String getPeriodChargeRequestJSONWithDueDate(Integer chargeId, String addDueDate, Integer amount) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("chargeId", chargeId);
        map.put("amount", amount);
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", "dd MMMM yyy");
        map.put("dueDate", addDueDate);
        String json = new Gson().toJson(map);
        return json;
    }

    private String getPeriodChargeRequestJSONWithDueDateAndFeeOnMonthDay(Integer chargeId, String addDueDate, Integer amount,
            String feeOnMonthDay) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("chargeId", chargeId);
        map.put("amount", amount);
        map.put("feeOnMonthDay", feeOnMonthDay);
        map.put("locale", CommonConstants.LOCALE);
        map.put("monthDayFormat", "dd MMMM");
        map.put("dateFormat", "dd MMMM yyy");
        map.put("dueDate", addDueDate);
        String json = new Gson().toJson(map);
        return json;
    }

    private String getAccountActivationJSON(final String activationDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("activatedOnDate", activationDate);
        String savingsAccountActivateJson = new Gson().toJson(map);
        return savingsAccountActivateJson;
    }

    public HashMap activateSavingsAccount(Integer savingsID, String activationDate) {
        return performSavingApplicationActions(createSavingsOperationURL(ACTIVATE_SAVINGS_COMMAND, savingsID),
                getAccountActivationJSON(activationDate), IS_BLOCK);
    }

    public Object inactivateCharge(final Integer chargeId, final Integer savingsId, final String jsonAttributeToGetBack) {
        return performSavingActions(createChargesURL("inactivate", savingsId, chargeId), getSavingsInactivateChargeJSON(),
                jsonAttributeToGetBack);
    }

    private String getSavingsInactivateChargeJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        String josn = new Gson().toJson(map);
        return josn;
    }

    public static Integer openSavingsAccount(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer clientId, final String minimumOpeningBalance) {
        final Integer savingsProductID = createSavingsProduct(requestSpec, responseSpec, minimumOpeningBalance);
        Assertions.assertNotNull(savingsProductID);

        SavingsAccountHelper savingsAccountHelper = new SavingsAccountHelper(requestSpec, responseSpec);

        final Integer savingsId = savingsAccountHelper.applyForSavingsApplication(clientId, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(savingsProductID);

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
        LOG.info("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
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
        dataMap.put("Spouse Name", Utils.randomStringGenerator("Spouse_name", 4));
        dataMap.put("Number of Dependents", 5);
        dataMap.put("Time of Visit", "01 December 2016 04:03");
        dataMap.put("dateFormat", DATE_TIME_FORMAT);
        dataMap.put("Date of Approval", "02 December 2016 00:00");
        datatableMap.put("registeredTableName", registeredTableName);
        datatableMap.put("data", dataMap);
        datatablesListMap.add(datatableMap);
        return datatablesListMap;
    }

    public Workbook getSavingsWorkbook(String dateFormat) throws IOException {
        requestSpec.header(HttpHeaders.CONTENT_TYPE, "application/vnd.ms-excel");
        byte[] byteArray = Utils.performGetBinaryResponse(requestSpec, responseSpec,
                SAVINGS_ACCOUNT_URL + "/downloadtemplate" + "?" + Utils.TENANT_IDENTIFIER + "&dateFormat=" + dateFormat);
        InputStream inputStream = new ByteArrayInputStream(byteArray);
        Workbook workbook = new HSSFWorkbook(inputStream);
        return workbook;
    }

    public String importSavingsTemplate(File file) {
        String locale = "en";
        String dateFormat = "dd MMMM yyyy";
        String legalFormType = null;
        requestSpec.header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA);
        return Utils.performServerTemplatePost(requestSpec, responseSpec,
                SAVINGS_ACCOUNT_URL + "/uploadtemplate" + "?" + Utils.TENANT_IDENTIFIER, legalFormType, file, locale, dateFormat);
    }

    public String getOutputTemplateLocation(final String importDocumentId) {
        requestSpec.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
        return Utils.performServerOutputTemplateLocationGet(requestSpec, responseSpec,
                "/fineract-provider/api/v1/imports/getOutputTemplateLocation" + "?" + Utils.TENANT_IDENTIFIER, importDocumentId);
    }

    // gsim testing
    public HashMap approveGsimSavings(final Integer gsimID) {
        LOG.info("---------------GSIM APPROVAL---------------");
        return approveGsimSavingsOnDate(gsimID, null);
    }

    public HashMap approveGsimSavingsOnDate(final Integer gsimID, final String approvalDate) {
        LOG.info("--------------------------------- APPROVING GSIM SAVINGS APPLICATION ------------------------------------");
        final String savingsOperationURL = createSavingsGsimOperationURL(APPROVE_SAVINGS_COMMAND, gsimID);

        if (approvalDate == null || approvalDate.equals("")) {
            return performSavingApplicationActions(savingsOperationURL, getApproveSavingsAsJSON(), IS_BLOCK);
        }
        return performSavingApplicationActions(savingsOperationURL, getApproveSavingsAsJsonOnDate(approvalDate), IS_BLOCK);
    }

    public HashMap rejectGsimApplication(final Integer gsimID) {
        LOG.info("--------------------------------- REJECT SAVINGS APPLICATION -------------------------------");
        return performSavingApplicationActions(createSavingsGsimOperationURL(REJECT_SAVINGS_COMMAND, gsimID),
                getRejectedSavingsAsJSON(CREATED_DATE_PLUS_ONE), IS_BLOCK);
    }

    public List rejectGsimApplicationWithErrorCode(final Integer gsimID, final String date) {
        LOG.info("--------------------------------- REJECT SAVINGS APPLICATION -------------------------------");
        return (List) performSavingActions(createSavingsGsimOperationURL(REJECT_SAVINGS_COMMAND, gsimID), getRejectedSavingsAsJSON(date),
                CommonConstants.RESPONSE_ERROR);
    }

    public HashMap undoApprovalGsimSavings(final Integer gsimId) {
        LOG.info("--------------------------------- UNDO APPROVING GSIM SAVINGS APPLICATION -------------------------------");
        final String undoBodyJson = "{'note':'UNDO APPROVAL'}";
        return performSavingApplicationActions(createSavingsGsimOperationURL(UNDO_APPROVAL_SAVINGS_COMMAND, gsimId), undoBodyJson,
                IS_BLOCK);
    }

    public Integer depositGsimApplication(Integer savingsID, List<Map<String, Object>> savingsArray) {
        LOG.info("--------------------------------- DEPOSIT GSIM SAVINGS APPLICATION -------------------------------");
        String savingsArrays = new SavingsApplicationTestBuilder() //
                .withSavingsArray(savingsArray).build();
        LOG.info("savingsArray : {} ", savingsArrays);
        return SavingsAccountHelper.depositGsimApplication(savingsID, savingsArrays, requestSpec, responseSpec);

    }

    public static Integer depositGsimApplication(Integer savingsID, final String savingsArrays, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {

        final String depositGsimURL = SAVINGS_ACCOUNT_URL + "/" + savingsID + "/transactions" + "?" + "command="
                + GSIM_DEPOSIT_SAVINGS_COMMAND + "&" + Utils.TENANT_IDENTIFIER;
        LOG.info("depositGsimURL : {} ", depositGsimURL);
        return Utils.performServerPost(requestSpec, responseSpec, depositGsimURL, savingsArrays, CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public HashMap activateGsimSavings(final Integer gsimID) {
        LOG.info("---------------------------------- ACTIVATING GSIM SAVINGS APPLICATION ----------------------------------");
        return performSavingApplicationActions(createSavingsGsimOperationURL(ACTIVATE_SAVINGS_COMMAND, gsimID), getActivatedSavingsAsJSON(),
                IS_BLOCK);
    }

    public HashMap closeGsimSavingsAccount(final Integer gsimID, String withdrawBalance) {
        LOG.info("---------------------------------- CLOSE SAVINGS APPLICATION ----------------------------------");
        return performSavingApplicationActions(createSavingsGsimOperationURL(CLOSE_SAVINGS_COMMAND, gsimID),
                getCloseAccountJSON(withdrawBalance, LAST_TRANSACTION_DATE), IS_BLOCK);
    }

    public Object closeGsimSavingsAccountAndGetBackRequiredField(final Integer gsimId, String withdrawBalance,
            final String jsonAttributeToGetBack, final String closedOnDate) {
        LOG.info("---------------------------------- CLOSE SAVINGS APPLICATION ----------------------------------");
        return performSavingActions(createSavingsGsimOperationURL(CLOSE_SAVINGS_COMMAND, gsimId),
                getCloseAccountJSON(withdrawBalance, closedOnDate), jsonAttributeToGetBack);
    }

    public HashMap updateGsimApplication(final Integer gsimID, final Integer clientID, final Integer groupID, final Integer productID) {
        LOG.info("--------------------------------- UPDATE GSIM SAVINGS APPLICATION -------------------------------");
        final String GSIM_URL = "/fineract-provider/api/v1/savingsaccounts/gsim/" + gsimID + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPut(requestSpec, responseSpec, GSIM_URL,
                updateGsimJSON(clientID.toString(), groupID.toString(), productID.toString()), "");
    }

    public HashMap getTransactionDetails(Integer savingsId, Integer transactionId) {
        LOG.info("--------------------------------- GET savings transaction details -------------------------------");
        final String url = "/fineract-provider/api/v1/savingsaccounts/" + savingsId + "/transactions/" + transactionId + "?"
                + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, url, "");
    }

    public PostSavingsAccountsResponse createSavingsAccount(PostSavingsAccountsRequest request) {
        return ok(fineract().savingsAccounts.submitApplication2(request));
    }

    public PostSavingsAccountsAccountIdResponse approveSavingsAccount(Long savingsAccountId, String approvedOnDate) {
        PostSavingsAccountsAccountIdRequest request = new PostSavingsAccountsAccountIdRequest().approvedOnDate(approvedOnDate)
                .dateFormat(DATETIME_PATTERN).locale(LOCALE);
        return ok(fineract().savingsAccounts.handleCommands6(savingsAccountId, request, "approve"));
    }

    public PostSavingsAccountsAccountIdResponse activateSavingsAccount(Long savingsAccountId, String activatedOnDate) {
        PostSavingsAccountsAccountIdRequest request = new PostSavingsAccountsAccountIdRequest().activatedOnDate(activatedOnDate)
                .dateFormat(DATETIME_PATTERN).locale(LOCALE);
        return ok(fineract().savingsAccounts.handleCommands6(savingsAccountId, request, "activate"));
    }

    public GetSavingsAccountsAccountIdResponse getSavingsAccount(Long savingsAccountId) {
        return ok(fineract().savingsAccounts.retrieveOne25(savingsAccountId, null, null));
    }

    public PostSavingsAccountTransactionsResponse applySavingsAccountTransaction(Long savingsAccountId,
            PostSavingsAccountTransactionsRequest request, String command) {
        return ok(fineract().savingsTransactions.transaction2(savingsAccountId, request, command));
    }

    public GetSavingsAccountsAccountIdResponse getSavingsAccount(Long savingsAccountId, Map<String, Object> queryParams) {
        final String url = SAVINGS_ACCOUNT_URL + "/" + savingsAccountId;
        queryParams.put(TENANT_PARAM_NAME, DEFAULT_TENANT);
        requestSpec.queryParams(queryParams);
        String response = Utils.performServerGet(this.requestSpec, this.responseSpec, url);
        return GSON.fromJson(response, GetSavingsAccountsAccountIdResponse.class);
    }

}
