/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.recurringdeposit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.mifosplatform.integrationtests.common.CommonConstants;
import org.mifosplatform.integrationtests.common.Utils;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "unused", "rawtypes" })
public class RecurringDepositAccountHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public RecurringDepositAccountHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    private static final String RECURRING_DEPOSIT_ACCOUNT_URL = "/mifosng-provider/api/v1/recurringdepositaccounts";
    private static final String APPLY_RECURRING_DEPOSIT_ACCOUNT_URL = RECURRING_DEPOSIT_ACCOUNT_URL + "?" + Utils.TENANT_IDENTIFIER;
    private static final String APPROVE_RECURRING_DEPOSIT_COMMAND = "approve";
    private static final String UNDO_APPROVAL_RECURRING_DEPOSIT_COMMAND = "undoapproval";
    private static final String REJECT_RECURRING_DEPOSIT_COMMAND = "reject";
    private static final String WITHDRAWN_BY_CLIENT_RECURRING_DEPOSIT_COMMAND = "withdrawnByApplicant";
    private static final String ACTIVATE_RECURRING_DEPOSIT_COMMAND = "activate";
    private static final String CLOSE_RECURRING_DEPOSIT_COMMAND = "close";
    private static final String POST_INTEREST_RECURRING_DEPOSIT_COMMAND = "postInterest";
    private static final String CALCULATE_INTEREST_RECURRING_DEPOSIT_COMMAND = "calculateInterest";
    private static final String CALCULATE_PREMATURE_AMOUNT_COMMAND = "calculatePrematureAmount";
    private static final String PREMATURE_CLOSE_COMMAND = "prematureClose";
    private static final String DEPOSIT_INTO_RECURRING_DEPOSIT_COMMAND = "deposit";
    private static final String MODIFY_TRANSACTION_COMMAND = "modify";
    private static final String UNDO_TRANSACTION_COMMAND = "undo";

    private static final String LOCALE = "en_GB";
    private static final String DIGITS_AFTER_DECIMAL = "4";
    private static final String IN_MULTIPLES_OF = "100";
    private static final String USD = "USD";
    private static final String DAYS = "0";
    private static final String WEEKS = "1";
    private static final String MONTHS = "2";
    private static final String YEARS = "3";
    private static final String DAILY = "1";
    private static final String MONTHLY = "4";
    private static final String QUARTERLY = "5";
    private static final String BI_ANNUALLY = "6";
    private static final String ANNUALLY = "7";
    private static final String INTEREST_CALCULATION_USING_DAILY_BALANCE = "1";
    private static final String INTEREST_CALCULATION_USING_AVERAGE_DAILY_BALANCE = "2";
    private static final String DAYS_360 = "360";
    private static final String DAYS_365 = "365";
    private static final String NONE = "1";
    private static final String CASH_BASED = "2";
    private static final String ACCRUAL_PERIODIC = "3";
    private static final String ACCRUAL_UPFRONT = "4";

    private String interestCompoundingPeriodType = MONTHLY;
    private String interestPostingPeriodType = MONTHLY;
    private String interestCalculationType = INTEREST_CALCULATION_USING_DAILY_BALANCE;
    private final String lockinPeriodFrequency = "1";
    private final String lockingPeriodFrequencyType = MONTHS;
    private final String minDepositTerm = "6";
    private final String minDepositTermTypeId = MONTHS;
    private final String maxDepositTerm = "10";
    private final String maxDepositTermTypeId = YEARS;
    private final String inMultiplesOfDepositTerm = "2";
    private final String inMultiplesOfDepositTermTypeId = MONTHS;
    private final String preClosurePenalInterest = "2";
    private final boolean preClosurePenalApplicable = true;
    private final boolean isActiveChart = true;
    private final String currencyCode = USD;
    private String interestCalculationDaysInYearType = DAYS_365;
    private final String depositAmount = "2000";
    private final String depositPeriod = "14";
    private final String depositPeriodFrequencyId = MONTHS;
    private final String recurringFrequency = "1";
    private final String recurringFrequencyType = MONTHS;
    private final String mandatoryRecommendedDepositAmount = "2000";
    private String submittedOnDate = "";
    private String expectedFirstDepositOnDate = "";
    private boolean isCalendarInherited = false;

    public String build(final String clientId, final String productId, final String validFrom, final String validTo,
            final String penalInterestType) {
        final HashMap<String, Object> map = new HashMap<>();

        List<HashMap<String, String>> chartSlabs = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> chartSlabsMap1 = new HashMap<>();
        chartSlabsMap1.put("description", "First");
        chartSlabsMap1.put("periodType", MONTHS);
        chartSlabsMap1.put("fromPeriod", "1");
        chartSlabsMap1.put("toPeriod", "6");
        chartSlabsMap1.put("annualInterestRate", "5");
        chartSlabsMap1.put("locale", LOCALE);
        chartSlabs.add(0, chartSlabsMap1);

        HashMap<String, String> chartSlabsMap2 = new HashMap<>();
        chartSlabsMap2.put("description", "Second");
        chartSlabsMap2.put("periodType", MONTHS);
        chartSlabsMap2.put("fromPeriod", "7");
        chartSlabsMap2.put("toPeriod", "12");
        chartSlabsMap2.put("annualInterestRate", "6");
        chartSlabsMap2.put("locale", LOCALE);
        chartSlabs.add(1, chartSlabsMap2);

        HashMap<String, String> chartSlabsMap3 = new HashMap<>();
        chartSlabsMap3.put("description", "Third");
        chartSlabsMap3.put("periodType", MONTHS);
        chartSlabsMap3.put("fromPeriod", "13");
        chartSlabsMap3.put("toPeriod", "18");
        chartSlabsMap3.put("annualInterestRate", "7");
        chartSlabsMap3.put("locale", LOCALE);
        chartSlabs.add(2, chartSlabsMap3);

        HashMap<String, String> chartSlabsMap4 = new HashMap<>();
        chartSlabsMap4.put("description", "Fourth");
        chartSlabsMap4.put("periodType", MONTHS);
        chartSlabsMap4.put("fromPeriod", "19");
        chartSlabsMap4.put("toPeriod", "24");
        chartSlabsMap4.put("annualInterestRate", "8");
        chartSlabsMap4.put("locale", LOCALE);
        chartSlabs.add(3, chartSlabsMap4);

        List<HashMap<String, Object>> charts = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> chartsMap = new HashMap<>();
        chartsMap.put("fromDate", validFrom);
        chartsMap.put("endDate", validTo);
        chartsMap.put("dateFormat", "dd MMMM yyyy");
        chartsMap.put("locale", LOCALE);
        chartsMap.put("isActiveChart", this.isActiveChart);
        chartsMap.put("chartSlabs", chartSlabs);
        charts.add(chartsMap);

        map.put("charts", charts);
        map.put("productId", productId);
        map.put("clientId", clientId);
        map.put("interestCalculationDaysInYearType", this.interestCalculationDaysInYearType);
        map.put("locale", LOCALE);
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("monthDayFormat", "dd MMM");
        map.put("interestCalculationType", this.interestCalculationType);
        map.put("interestCompoundingPeriodType", this.interestCompoundingPeriodType);
        map.put("interestPostingPeriodType", this.interestPostingPeriodType);
        map.put("lockinPeriodFrequency", this.lockinPeriodFrequency);
        map.put("lockinPeriodFrequencyType", this.lockingPeriodFrequencyType);
        map.put("preClosurePenalApplicable", "true");
        map.put("minDepositTermTypeId", this.minDepositTermTypeId);
        map.put("minDepositTerm", this.minDepositTerm);
        map.put("maxDepositTermTypeId", this.maxDepositTermTypeId);
        map.put("maxDepositTerm", this.maxDepositTerm);
        map.put("preClosurePenalApplicable", this.preClosurePenalApplicable);
        map.put("inMultiplesOfDepositTerm", this.inMultiplesOfDepositTerm);
        map.put("inMultiplesOfDepositTermTypeId", this.inMultiplesOfDepositTermTypeId);
        map.put("preClosurePenalInterest", this.preClosurePenalInterest);
        map.put("preClosurePenalInterestOnTypeId", penalInterestType);
        map.put("depositAmount", this.depositAmount);
        map.put("depositPeriod", this.depositPeriod);
        map.put("depositPeriodFrequencyId", this.depositPeriodFrequencyId);
        map.put("submittedOnDate", this.submittedOnDate);
        map.put("recurringFrequency", this.recurringFrequency);
        map.put("recurringFrequencyType", this.recurringFrequencyType);
        map.put("mandatoryRecommendedDepositAmount", this.mandatoryRecommendedDepositAmount);
        map.put("expectedFirstDepositOnDate", this.expectedFirstDepositOnDate);
        map.put("isCalendarInherited", this.isCalendarInherited);

        String recurringDepositAccountJson = new Gson().toJson(map);
        System.out.println(recurringDepositAccountJson);
        return recurringDepositAccountJson;
    }

    public static Integer applyRecurringDepositApplication(final String recurringDepositAccountAsJson,
            final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        System.out.println("--------------------- APPLYING FOR RECURRING DEPOSIT ACCOUNT ------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, APPLY_RECURRING_DEPOSIT_ACCOUNT_URL, recurringDepositAccountAsJson,
                CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public static HashMap getRecurringDepositAccountById(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer accountID) {
        final String GET_RECURRING_DEPOSIT_BY_ID_URL = RECURRING_DEPOSIT_ACCOUNT_URL + "/" + accountID + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING RECURRING DEPOSIT ACCOUNT BY ID -------------------------");
        return Utils.performServerGet(requestSpec, responseSpec, GET_RECURRING_DEPOSIT_BY_ID_URL, "");
    }

    public HashMap getRecurringDepositSummary(final Integer accountID) {
        final String URL = RECURRING_DEPOSIT_ACCOUNT_URL + "/" + accountID + "?" + Utils.TENANT_IDENTIFIER;
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, URL, "summary");
        return response;
    }

    public static Float getInterestRate(ArrayList<ArrayList<HashMap>> interestSlabData, Integer depositPeriod) {

        Float annualInterestRate = 0.0f;
        for (Integer slabIndex = 0; slabIndex < interestSlabData.get(0).size(); slabIndex++) {
            Integer fromPeriod = (Integer) interestSlabData.get(0).get(slabIndex).get("fromPeriod");
            Integer toPeriod = (Integer) interestSlabData.get(0).get(slabIndex).get("toPeriod");
            if (depositPeriod >= fromPeriod && depositPeriod <= toPeriod) {
                annualInterestRate = (Float) interestSlabData.get(0).get(slabIndex).get("annualInterestRate");
                break;
            }
        }

        return annualInterestRate;
    }

    public static Float getPrincipalAfterCompoundingInterest(Calendar currentDate, Float principal, Float depositAmount, Integer depositPeriod,
            double interestPerDay, Integer compoundingInterval, Integer postingInterval) {

        Float totalInterest = 0.0f;
        Float interestEarned = 0.0f;

        for (int i = 1; i <= depositPeriod; i++) {
            Integer daysInMonth = currentDate.getActualMaximum(Calendar.DATE);
            principal += depositAmount;
            for (int j = 0; j < daysInMonth; j++) {

                interestEarned = (float) (principal * interestPerDay);
                totalInterest += interestEarned;
                if (compoundingInterval == 0) {
                    principal += interestEarned;
                }

            }
            if ((i % postingInterval) == 0 || i == depositPeriod) {
                if (compoundingInterval != 0) {
                    principal += totalInterest;
                }
                totalInterest = 0.0f;
                System.out.println(principal);

            }
            currentDate.add(Calendar.MONTH, 1);
            interestEarned = 0.0f;
        }
        return principal;
    }

    public HashMap updateRecurringDepositAccount(final String clientID, final String productID, final String accountID,
            final String validFrom, final String validTo, final String penalInterestType, final String submittedOnDate) {

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        todaysDate.add(Calendar.DATE, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_ON_DATE = SUBMITTED_ON_DATE;
        final String recurringDepositApplicationJSON = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec)
                .withSubmittedOnDate(SUBMITTED_ON_DATE).withExpectedFirstDepositOnDate(EXPECTED_FIRST_DEPOSIT_ON_ON_DATE)
                .build(clientID, productID, validFrom, validTo, penalInterestType);

        return Utils.performServerPut(this.requestSpec, this.responseSpec, RECURRING_DEPOSIT_ACCOUNT_URL + "/" + accountID + "?"
                + Utils.TENANT_IDENTIFIER, recurringDepositApplicationJSON, CommonConstants.RESPONSE_CHANGES);
    }

    public HashMap updateInterestCalculationConfigForRecurringDeposit(final String clientID, final String productID,
            final String accountID, final String submittedOnDate, final String validFrom, final String validTo,
            final String numberOfDaysPerYear, final String penalInterestType, final String interestCalculationType,
            final String interestCompoundingPeriodType, final String interestPostingPeriodType, final String expectedFirstDepositOnDate) {

        final String recurringDepositApplicationJSON = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec) //
                .withSubmittedOnDate(submittedOnDate) //
                .withNumberOfDaysPerYear(numberOfDaysPerYear) //
                .withInterestCalculationPeriodType(interestCalculationType) //
                .withInterestCompoundingPeriodType(interestCompoundingPeriodType) //
                .withInterestPostingPeriodType(interestPostingPeriodType) //
                .withExpectedFirstDepositOnDate(expectedFirstDepositOnDate) //
                .build(clientID, productID, validFrom, validTo, penalInterestType);

        return Utils.performServerPut(this.requestSpec, this.responseSpec, RECURRING_DEPOSIT_ACCOUNT_URL + "/" + accountID + "?"
                + Utils.TENANT_IDENTIFIER, recurringDepositApplicationJSON, CommonConstants.RESPONSE_CHANGES);
    }

    public Integer updateTransactionForRecurringDeposit(final Integer accountID, final Integer transactionId, final String transactionDate,
            final Float transactionAmount) {
        System.out.println("--------------------------------- UPDATE RECURRING DEPOSIT TRANSACTION ------------------------------------");
        return Utils.performServerPost(this.requestSpec, this.responseSpec, RECURRING_DEPOSIT_ACCOUNT_URL + "/" + accountID
                + "/transactions/" + transactionId + "?command=" + MODIFY_TRANSACTION_COMMAND,
                getUpdateTransactionAsJSON(transactionDate, transactionAmount), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public Integer undoTransactionForRecurringDeposit(final Integer accountID, final Integer transactionId, final String transactionDate,
            final Float transactionAmount) {
        System.out.println("--------------------------------- UNDO RECURRING DEPOSIT TRANSACTION ------------------------------------");
        return Utils.performServerPost(this.requestSpec, this.responseSpec, RECURRING_DEPOSIT_ACCOUNT_URL + "/" + accountID
                + "/transactions/" + transactionId + "?command=" + UNDO_TRANSACTION_COMMAND,
                getUpdateTransactionAsJSON(transactionDate, transactionAmount), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public HashMap approveRecurringDeposit(final Integer recurringDepositAccountID, final String approvedOnDate) {
        System.out
                .println("--------------------------------- APPROVING RECURRING DEPOSIT APPLICATION ------------------------------------");
        return performRecurringDepositApplicationActions(
                createRecurringDepositOperationURL(APPROVE_RECURRING_DEPOSIT_COMMAND, recurringDepositAccountID),
                getApproveRecurringDepositAccountAsJSON(approvedOnDate));
    }

    public HashMap undoApproval(final Integer recurringDepositAccountID) {
        System.out
                .println("--------------------------------- UNDO APPROVING RECURRING DEPOSIT APPLICATION -------------------------------");
        final String undoBodyJson = "{'note':'UNDO APPROVAL'}";
        return performRecurringDepositApplicationActions(
                createRecurringDepositOperationURL(UNDO_APPROVAL_RECURRING_DEPOSIT_COMMAND, recurringDepositAccountID), undoBodyJson);
    }

    public HashMap rejectApplication(final Integer recurringDepositAccountID, final String rejectedOnDate) {
        System.out.println("--------------------------------- REJECT RECURRING DEPOSIT APPLICATION -------------------------------");
        return performRecurringDepositApplicationActions(
                createRecurringDepositOperationURL(REJECT_RECURRING_DEPOSIT_COMMAND, recurringDepositAccountID),
                getRejectedRecurringDepositAsJSON(rejectedOnDate));
    }

    public HashMap withdrawApplication(final Integer recurringDepositAccountID, final String withdrawApplicationOnDate) {
        System.out.println("--------------------------------- WITHDRAW RECURRING DEPOSIT APPLICATION -------------------------------");
        return performRecurringDepositApplicationActions(
                createRecurringDepositOperationURL(WITHDRAWN_BY_CLIENT_RECURRING_DEPOSIT_COMMAND, recurringDepositAccountID),
                getWithdrawnRecurringDepositAccountAsJSON(withdrawApplicationOnDate));
    }

    public HashMap activateRecurringDeposit(final Integer recurringDepositAccountID, final String activationDate) {
        System.out
                .println("---------------------------------- ACTIVATING RECURRING DEPOSIT APPLICATION ----------------------------------");
        return performRecurringDepositApplicationActions(
                createRecurringDepositOperationURL(ACTIVATE_RECURRING_DEPOSIT_COMMAND, recurringDepositAccountID),
                getActivatedRecurringDepositAccountAsJSON(activationDate));
    }

    public Object deleteRecurringDepositApplication(final Integer recurringDepositAccountID, final String jsonAttributeToGetBack) {
        System.out.println("---------------------------------- DELETE RECURRING DEPOSIT APPLICATION ----------------------------------");
        return Utils.performServerDelete(this.requestSpec, this.responseSpec, RECURRING_DEPOSIT_ACCOUNT_URL + "/"
                + recurringDepositAccountID + "?" + Utils.TENANT_IDENTIFIER, jsonAttributeToGetBack);

    }

    public Integer calculateInterestForRecurringDeposit(final Integer recurringDepositAccountId) {
        System.out.println("--------------------------------- CALCULATING INTEREST FOR RECURRING DEPOSIT --------------------------------");
        return (Integer) performRecurringDepositActions(
                createRecurringDepositCalculateInterestURL(CALCULATE_INTEREST_RECURRING_DEPOSIT_COMMAND, recurringDepositAccountId),
                getCalculatedInterestForRecurringDepositApplicationAsJSON(), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public Integer postInterestForRecurringDeposit(final Integer recurringDepositAccountId) {
        System.out.println("--------------------------------- POST INTEREST FOR RECURRING DEPOSIT --------------------------------");
        return (Integer) performRecurringDepositActions(
                createRecurringDepositCalculateInterestURL(POST_INTEREST_RECURRING_DEPOSIT_COMMAND, recurringDepositAccountId),
                getCalculatedInterestForRecurringDepositApplicationAsJSON(), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public Integer depositToRecurringDepositAccount(final Integer recurringDepositAccountId, final Float depositAmount,
            final String depositedOnDate) {
        System.out.println("--------------------------------- DEPOSIT TO RECURRING DEPOSIT ACCOUNT --------------------------------");
        return (Integer) performRecurringDepositActions(
                createDepositToRecurringDepositURL(DEPOSIT_INTO_RECURRING_DEPOSIT_COMMAND, recurringDepositAccountId),
                getDepositToRecurringDepositAccountAsJSON(depositAmount, depositedOnDate), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public HashMap calculatePrematureAmountForRecurringDeposit(final Integer recurringDepositAccountId, final String closedOnDate) {
        System.out.println("--------------------- CALCULATING PREMATURE AMOUNT FOR RECURRING DEPOSIT ----------------------------");
        return (HashMap) performRecurringDepositActions(
                createRecurringDepositCalculateInterestURL(CALCULATE_PREMATURE_AMOUNT_COMMAND, recurringDepositAccountId),
                getCalculatedPrematureAmountForRecurringDepositAccountAsJSON(closedOnDate), "");
    }

    public Object prematureCloseForRecurringDeposit(final Integer recurringDepositAccountId, final String closedOnDate,
            final String closureType, final Integer toSavingsId, final String jsonAttributeToGetBack) {
        System.out.println("--------------------- PREMATURE CLOSE FOR RECURRING DEPOSIT ----------------------------");
        return performRecurringDepositActions(
                createRecurringDepositCalculateInterestURL(PREMATURE_CLOSE_COMMAND, recurringDepositAccountId),
                getPrematureCloseForRecurringDepositAccountAsJSON(closedOnDate, closureType, toSavingsId), jsonAttributeToGetBack);
    }

    private String getApproveRecurringDepositAccountAsJSON(final String approvedOnDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("approvedOnDate", approvedOnDate);
        map.put("note", "Approval NOTE");
        String recurringDepositAccountApproveJson = new Gson().toJson(map);
        System.out.println(recurringDepositAccountApproveJson);
        return recurringDepositAccountApproveJson;
    }

    private String getUpdateTransactionAsJSON(final String transactionDate, final Float transactionAmount) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("transactionDate", transactionDate);
        map.put("transactionAmount", transactionAmount);
        String updateTransactionJson = new Gson().toJson(map);
        System.out.println(updateTransactionJson);
        return updateTransactionJson;
    }

    private String getRejectedRecurringDepositAsJSON(final String rejectedOnDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("rejectedOnDate", rejectedOnDate);
        map.put("note", "Rejected NOTE");
        String recurringDepositAccountJson = new Gson().toJson(map);
        System.out.println(recurringDepositAccountJson);
        return recurringDepositAccountJson;
    }

    private String getWithdrawnRecurringDepositAccountAsJSON(final String withdrawnApplicationOnDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("withdrawnOnDate", withdrawnApplicationOnDate);
        map.put("note", "Withdraw NOTE");
        String recurringDepositAccountJson = new Gson().toJson(map);
        System.out.println(recurringDepositAccountJson);
        return recurringDepositAccountJson;
    }

    private String getActivatedRecurringDepositAccountAsJSON(final String activationDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("activatedOnDate", activationDate);
        String recurringDepositAccountActivateJson = new Gson().toJson(map);
        System.out.println(recurringDepositAccountActivateJson);
        return recurringDepositAccountActivateJson;
    }

    private String getCalculatedInterestForRecurringDepositApplicationAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        String recurringDepositAccountCalculatedInterestJson = new Gson().toJson(map);
        System.out.println(recurringDepositAccountCalculatedInterestJson);
        return recurringDepositAccountCalculatedInterestJson;
    }

    private String getCalculatedPrematureAmountForRecurringDepositAccountAsJSON(final String closedOnDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("closedOnDate", closedOnDate);
        String recurringDepositAccountPrematureClosureJson = new Gson().toJson(map);
        System.out.println(recurringDepositAccountPrematureClosureJson);
        return recurringDepositAccountPrematureClosureJson;
    }

    private String getDepositToRecurringDepositAccountAsJSON(final Float depositAmount, final String depositedOnDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("transactionAmount", depositAmount);
        map.put("transactionDate", depositedOnDate);
        String recurringDepositAccountPrematureClosureJson = new Gson().toJson(map);
        System.out.println(recurringDepositAccountPrematureClosureJson);
        return recurringDepositAccountPrematureClosureJson;
    }

    private String getPrematureCloseForRecurringDepositAccountAsJSON(final String closedOnDate, final String closureType,
            final Integer toSavingsId) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("closedOnDate", closedOnDate);
        map.put("onAccountClosureId", closureType);
        if (toSavingsId != null) {
            map.put("toSavingsAccountId", toSavingsId);
            map.put("transferDescription", "Transferring To Savings Account");
        }
        String recurringDepositAccountPrematureCloseJson = new Gson().toJson(map);
        System.out.println(recurringDepositAccountPrematureCloseJson);
        return recurringDepositAccountPrematureCloseJson;
    }

    private String createRecurringDepositOperationURL(final String command, final Integer recurringDepositAccountID) {
        return RECURRING_DEPOSIT_ACCOUNT_URL + "/" + recurringDepositAccountID + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
    }

    private Object performRecurringDepositActions(final String postURLForRecurringDeposit, final String jsonToBeSent,
            final String jsonAttributeToGetBack) {
        return Utils.performServerPost(this.requestSpec, this.responseSpec, postURLForRecurringDeposit, jsonToBeSent,
                jsonAttributeToGetBack);
    }

    private HashMap performRecurringDepositApplicationActions(final String postURLForRecurringDepositAction, final String jsonToBeSent) {
        HashMap status = null;
        final HashMap response = Utils.performServerPost(this.requestSpec, this.responseSpec, postURLForRecurringDepositAction,
                jsonToBeSent, CommonConstants.RESPONSE_CHANGES);
        if (response != null) {
            status = (HashMap) response.get("status");
        }
        return status;
    }

    private String createRecurringDepositCalculateInterestURL(final String command, final Integer recurringDepositAccountID) {
        return RECURRING_DEPOSIT_ACCOUNT_URL + "/" + recurringDepositAccountID + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
    }

    private String createDepositToRecurringDepositURL(final String command, final Integer recurringDepositAccountID) {
        return RECURRING_DEPOSIT_ACCOUNT_URL + "/" + recurringDepositAccountID + "/transactions" + "?command=" + command + "&"
                + Utils.TENANT_IDENTIFIER;
    }

    public RecurringDepositAccountHelper withSubmittedOnDate(final String recurringDepositApplicationSubmittedDate) {
        this.submittedOnDate = recurringDepositApplicationSubmittedDate;
        return this;
    }

    public RecurringDepositAccountHelper withExpectedFirstDepositOnDate(final String recurringDepositApplicationExpectedFirstDepositOnDate) {
        this.expectedFirstDepositOnDate = recurringDepositApplicationExpectedFirstDepositOnDate;
        return this;
    }

    public RecurringDepositAccountHelper withNumberOfDaysPerYear(final String numberOfDaysPerYearTypeId) {
        this.interestCalculationDaysInYearType = numberOfDaysPerYearTypeId;
        return this;
    }

    public RecurringDepositAccountHelper withInterestCalculationPeriodType(final String interestCalculationTypeId) {
        this.interestCalculationType = interestCalculationTypeId;
        return this;
    }

    public RecurringDepositAccountHelper withInterestCompoundingPeriodType(final String interestCompoundingPeriodTypeId) {
        this.interestCompoundingPeriodType = interestCompoundingPeriodTypeId;
        return this;
    }

    public RecurringDepositAccountHelper withInterestPostingPeriodType(final String interestPostingPeriodTypeId) {
        this.interestPostingPeriodType = interestPostingPeriodTypeId;
        return this;
    }

}