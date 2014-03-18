package org.mifosplatform.integrationtests.common.savings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.mifosplatform.integrationtests.common.CommonConstants;
import org.mifosplatform.integrationtests.common.Utils;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "rawtypes" })
public class SavingsAccountHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    private static final String SAVINGS_ACCOUNT_URL = "/mifosng-provider/api/v1/savingsaccounts";
    private static final String APPROVE_SAVINGS_COMMAND = "approve";
    private static final String UNDO_APPROVAL_SAVINGS_COMMAND = "undoApproval";
    private static final String ACTIVATE_SAVINGS_COMMAND = "activate";
    private static final String REJECT_SAVINGS_COMMAND = "reject";
    private static final String WITHDRAWN_BY_CLIENT_SAVINGS_COMMAND = "withdrawnByApplicant";
    private static final String CALCULATE_INTEREST_SAVINGS_COMMAND = "calculateInterest";
    private static final String POST_INTEREST_SAVINGS_COMMAND = "postInterest";
    private static final String CLOSE_SAVINGS_COMMAND = "close";

    private static final String DEPOSIT_SAVINGS_COMMAND = "deposit";
    private static final String WITHDRAW_SAVINGS_COMMAND = "withdrawal";
    private static final String MODIFY_TRASACTION_COMMAND = "modify";
    private static final String UNDO_TRASACTION_COMMAND = "undo";

    public static final String CREATED_DATE = "08 January 2013";
    public static final String CREATED_DATE_PLUS_ONE = "09 January 2013";
    public static final String CREATED_DATE_MINUS_ONE = "07 January 2013";
    public static final String TRANSACTION_DATE = "10 January 2013";
    public static final String LAST_TRANSACTION_DATE = "11 January 2013";

    public SavingsAccountHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }
    
    public static String getFutureDate(){
        SimpleDateFormat sdf = new SimpleDateFormat(CommonConstants.dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR)+1);
        return sdf.format(calendar.getTime());
    }

    public Integer applyForSavingsApplication(final Integer ID, final Integer savingsProductID, final String accountType) {
        System.out.println("--------------------------------APPLYING FOR SAVINGS APPLICATION--------------------------------");
        final String savingsApplicationJSON = new SavingsApplicationTestBuilder() //
                .withSubmittedOnDate(CREATED_DATE) //
                .build(ID.toString(), savingsProductID.toString(), accountType);
        return Utils.performServerPost(this.requestSpec, this.responseSpec, SAVINGS_ACCOUNT_URL + "?" + Utils.TENANT_IDENTIFIER,
                savingsApplicationJSON, "savingsId");
    }

    public HashMap updateSavingsAccount(final Integer ID, final Integer savingsProductID, final Integer savingsId, final String accountType) {
        final String savingsApplicationJSON = new SavingsApplicationTestBuilder() //
                .withSubmittedOnDate(CREATED_DATE_PLUS_ONE) //
                .build(ID.toString(), savingsProductID.toString(), accountType);

        return Utils.performServerPut(this.requestSpec, this.responseSpec, SAVINGS_ACCOUNT_URL + "/" + savingsId + "?"
                + Utils.TENANT_IDENTIFIER, savingsApplicationJSON, CommonConstants.RESPONSE_CHANGES);
    }

    public HashMap approveSavings(final Integer savingsID) {
        System.out.println("--------------------------------- APPROVING SAVINGS APPLICATION ------------------------------------");
        return performSavingApplicationActions(createSavingsOperationURL(APPROVE_SAVINGS_COMMAND, savingsID), getApproveSavingsAsJSON());
    }

    public HashMap undoApproval(final Integer savingsID) {
        System.out.println("--------------------------------- UNDO APPROVING SAVINGS APPLICATION -------------------------------");
        final String undoBodyJson = "{'note':'UNDO APPROVAL'}";
        return performSavingApplicationActions(createSavingsOperationURL(UNDO_APPROVAL_SAVINGS_COMMAND, savingsID), undoBodyJson);
    }

    public HashMap rejectApplication(final Integer savingsID) {
        System.out.println("--------------------------------- REJECT SAVINGS APPLICATION -------------------------------");
        return performSavingApplicationActions(createSavingsOperationURL(REJECT_SAVINGS_COMMAND, savingsID), getRejectedSavingsAsJSON(CREATED_DATE_PLUS_ONE));
    }
    
    public List rejectApplicationWithErrorCode(final Integer savingsId,final String date){
        System.out.println("--------------------------------- REJECT SAVINGS APPLICATION -------------------------------");
        return (List)performSavingActions(createSavingsOperationURL(REJECT_SAVINGS_COMMAND, savingsId), getRejectedSavingsAsJSON(date), CommonConstants.RESPONSE_ERROR);
    }

    public HashMap withdrawApplication(final Integer savingsID) {
        System.out.println("--------------------------------- Withdraw SAVINGS APPLICATION -------------------------------");
        return performSavingApplicationActions(createSavingsOperationURL(WITHDRAWN_BY_CLIENT_SAVINGS_COMMAND, savingsID),
                getWithdrawnSavingsAsJSON());
    }

    public HashMap activateSavings(final Integer savingsID) {
        System.out.println("---------------------------------- ACTIVATING SAVINGS APPLICATION ----------------------------------");
        return performSavingApplicationActions(createSavingsOperationURL(ACTIVATE_SAVINGS_COMMAND, savingsID), getActivatedSavingsAsJSON());
    }

    public HashMap closeSavingsAccount(final Integer savingsID) {
        System.out.println("---------------------------------- CLOSE SAVINGS APPLICATION ----------------------------------");
        return performSavingApplicationActions(createSavingsOperationURL(CLOSE_SAVINGS_COMMAND, savingsID), getCloseAccountJSON());
    }

    public Object deleteSavingsApplication(final Integer savingsId,final String jsonAttributeToGetBack) {
        System.out.println("---------------------------------- DELETE SAVINGS APPLICATION ----------------------------------");
        return Utils.performServerDelete(this.requestSpec, this.responseSpec, SAVINGS_ACCOUNT_URL + "/" + savingsId + "?"
                + Utils.TENANT_IDENTIFIER, jsonAttributeToGetBack);

    }

    public Object depositToSavingsAccount(final Integer savingsID, final String amount, String date, String jsonAttributeToGetback) {
        System.out.println("--------------------------------- SAVINGS TRANSACTION DEPOSIT --------------------------------");
        return  performSavingActions(createSavingsTransactionURL(DEPOSIT_SAVINGS_COMMAND, savingsID),
                getSavingsTransactionJSON(amount, date), jsonAttributeToGetback);
    }

    public Object withdrawalFromSavingsAccount(final Integer savingsId, final String amount, String date, String jsonAttributeToGetback) {
        System.out.println("\n--------------------------------- SAVINGS TRANSACTION WITHDRAWAL --------------------------------");
        return  performSavingActions(createSavingsTransactionURL(WITHDRAW_SAVINGS_COMMAND, savingsId),
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

    public Integer addChargesForSavings(final Integer savingsId, final Integer chargeId) {
        System.out.println("--------------------------------- ADD CHARGES FOR SAVINGS --------------------------------");
        return (Integer) performSavingActions(SAVINGS_ACCOUNT_URL + "/" + savingsId + "/charges?" + Utils.TENANT_IDENTIFIER,
                getPeriodChargeRequestJSON(chargeId), CommonConstants.RESPONSE_RESOURCE_ID);
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

    private String getApproveSavingsAsJSON() {
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("approvedOnDate", CREATED_DATE_PLUS_ONE);
        map.put("note", "Approval NOTE");
        String savingsAccountApproveJson = new Gson().toJson(map);
        System.out.println(savingsAccountApproveJson);
        return savingsAccountApproveJson;
    }
    
    private String getRejectedSavingsAsJSON(final String rejectedOnDate) {
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("rejectedOnDate", rejectedOnDate);
        map.put("note", "Rejected NOTE");
        String savingsAccountJson = new Gson().toJson(map);
        System.out.println(savingsAccountJson);
        return savingsAccountJson;
    }

    private String getWithdrawnSavingsAsJSON() {
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("withdrawnOnDate", CREATED_DATE_PLUS_ONE);
        map.put("note", "Rejected NOTE");
        String savingsAccountJson = new Gson().toJson(map);
        System.out.println(savingsAccountJson);
        return savingsAccountJson;
    }

    private String getActivatedSavingsAsJSON() {
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("activatedOnDate", "10 January 2013");
        String savingsAccountActivateJson = new Gson().toJson(map);
        System.out.println(savingsAccountActivateJson);
        return savingsAccountActivateJson;
    }

    private String getSavingsTransactionJSON(final String amount, final String transactionDate) {
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("transactionDate", transactionDate);
        map.put("transactionAmount", amount);
        String savingsAccountWithdrawalJson = new Gson().toJson(map);
        System.out.println(savingsAccountWithdrawalJson);
        return savingsAccountWithdrawalJson;
    }

    private String getCalculatedInterestForSavingsApplicationAsJSON() {
        final HashMap<String, String> map = new HashMap<String, String>();
        String savingsAccountCalculatedInterestJson = new Gson().toJson(map);
        System.out.println(savingsAccountCalculatedInterestJson);
        return savingsAccountCalculatedInterestJson;
    }

    private String getSavingsPayChargeJSON(final String amount, final String dueDate) {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("amount", amount);
        map.put("dueDate", dueDate);
        String josn = new Gson().toJson(map);
        return josn;
    }

    private String getSavingsWaiveChargeJSON() {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        String josn = new Gson().toJson(map);
        return josn;
    }

    private String getModifyChargeJSON() {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("amount", "50");
        String josn = new Gson().toJson(map);
        return josn;
    }

    private String getCloseAccountJSON() {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("closedOnDate", LAST_TRANSACTION_DATE);
        map.put("note", "Close Test");
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

    private String createChargesURL(final String command, final Integer savingsID, final Integer chargeId) {
        return SAVINGS_ACCOUNT_URL + "/" + savingsID + "/charges/" + chargeId + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
    }

    public ArrayList getSavingsCollectionAttribute(final Integer savingsID, final String jSONAttribute) {
        final String URL = SAVINGS_ACCOUNT_URL + "/" + savingsID + "?associations=all&" + Utils.TENANT_IDENTIFIER;
        final ArrayList<HashMap> response = Utils.performServerGet(requestSpec, responseSpec, URL, jSONAttribute);
        return response;
    }

    public ArrayList getSavingsCharges(final Integer savingsID) {
        final String URL = SAVINGS_ACCOUNT_URL + "/" + savingsID + "/charges?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, URL, "");
    }

    public HashMap getSavingsChanrge(final Integer savingsID, final Integer savingsChargeId) {
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

    private HashMap performSavingApplicationActions(final String postURLForSavingsTransaction, final String jsonToBeSent) {
        HashMap status = null;
        final HashMap response = Utils.performServerPost(this.requestSpec, this.responseSpec, postURLForSavingsTransaction, jsonToBeSent,
                CommonConstants.RESPONSE_CHANGES);
        if (response != null) {
            status = (HashMap) response.get("status");
        }
        return status;
    }

    private Object performSavingActions(final String postURLForSavingsTransaction, final String jsonToBeSent,
            final String jsonAttributeToGetBack) {
        return Utils.performServerPost(this.requestSpec, this.responseSpec, postURLForSavingsTransaction, jsonToBeSent,
                jsonAttributeToGetBack);
    }

    private String getPeriodChargeRequestJSON(Integer chargeId) {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("chargeId", chargeId);
        map.put("amount", 100);
        map.put("feeOnMonthDay", "15 January");
        map.put("locale", CommonConstants.locale);
        map.put("monthDayFormat", "dd MMMM");

        String json = new Gson().toJson(map);
        return json;
    }
}