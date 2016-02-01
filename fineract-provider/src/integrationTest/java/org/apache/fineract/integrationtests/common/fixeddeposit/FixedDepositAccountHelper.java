/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.fixeddeposit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.mifosplatform.integrationtests.common.CommonConstants;
import org.mifosplatform.integrationtests.common.Utils;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "unused", "rawtypes" })
public class FixedDepositAccountHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public FixedDepositAccountHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    private static final String FIXED_DEPOSIT_ACCOUNT_URL = "/mifosng-provider/api/v1/fixeddepositaccounts";
    private static final String APPLY_FIXED_DEPOSIT_ACCOUNT_URL = FIXED_DEPOSIT_ACCOUNT_URL + "?" + Utils.TENANT_IDENTIFIER;
    private static final String APPROVE_FIXED_DEPOSIT_COMMAND = "approve";
    private static final String UNDO_APPROVAL_FIXED_DEPOSIT_COMMAND = "undoapproval";
    private static final String REJECT_FIXED_DEPOSIT_COMMAND = "reject";
    private static final String WITHDRAWN_BY_CLIENT_FIXED_DEPOSIT_COMMAND = "withdrawnByApplicant";
    private static final String ACTIVATE_FIXED_DEPOSIT_COMMAND = "activate";
    private static final String CLOSE_FIXED_DEPOSIT_COMMAND = "close";
    private static final String POST_INTEREST_FIXED_DEPOSIT_COMMAND = "postInterest";
    private static final String CALCULATE_INTEREST_FIXED_DEPOSIT_COMMAND = "calculateInterest";
    private static final String CALCULATE_PREMATURE_AMOUNT_COMMAND = "calculatePrematureAmount";
    private static final String PREMATURE_CLOSE_COMMAND = "prematureClose";

    private static final String LOCALE = "en_GB";
    private static final String DIGITS_AFTER_DECIMAL = "4";
    private static final String IN_MULTIPLES_OF = "100";
    private static final String USD = "USD";
    public static final String DAYS = "0";
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
    public final static String depositAmount = "100000";

    private String interestCompoundingPeriodType = MONTHLY;
    private String interestPostingPeriodType = MONTHLY;
    private String interestCalculationType = INTEREST_CALCULATION_USING_DAILY_BALANCE;
    private String lockinPeriodFrequency = "1";
    private String lockingPeriodFrequencyType = MONTHS;
    private final String minDepositTerm = "6";
    private final String minDepositTermTypeId = MONTHS;
    private final String maxDepositTerm = "10";
    private final String maxDepositTermTypeId = YEARS;
    private final String inMultiplesOfDepositTerm = "2";
    private final String inMultiplesOfDepositTermTypeId = MONTHS;
    private final String preClosurePenalInterest = "2";
    private String interestCalculationDaysInYearType = DAYS_365;
    private final boolean preClosurePenalApplicable = true;
    private final boolean isActiveChart = true;
    private final String currencyCode = USD;

    private final String depositPeriod = "14";
    private final String depositPeriodFrequencyId = MONTHS;
    private String submittedOnDate = "";
    private String savingsId = null;
    private boolean transferInterest = false;

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
        map.put("depositAmount", depositAmount);
        map.put("depositPeriod", this.depositPeriod);
        map.put("depositPeriodFrequencyId", this.depositPeriodFrequencyId);
        map.put("submittedOnDate", this.submittedOnDate);
        map.put("linkAccountId", savingsId);
        map.put("transferInterestToSavings", transferInterest);

        String fixedDepositAccountJson = new Gson().toJson(map);
        System.out.println(fixedDepositAccountJson);
        return fixedDepositAccountJson;
    }

    public static Integer applyFixedDepositApplication(final String fixedDepositAccountAsJson, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        System.out.println("--------------------- APPLYING FOR FIXED DEPOSIT ACCOUNT ------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, APPLY_FIXED_DEPOSIT_ACCOUNT_URL, fixedDepositAccountAsJson,
                CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public static HashMap getFixedDepositAccountById(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer accountID) {
        final String GET_FIXED_DEPOSIT_BY_ID_URL = FIXED_DEPOSIT_ACCOUNT_URL + "/" + accountID + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING FIXED DEPOSIT ACCOUNT BY ID -------------------------");
        return Utils.performServerGet(requestSpec, responseSpec, GET_FIXED_DEPOSIT_BY_ID_URL, "");
    }

    public HashMap getFixedDepositSummary(final Integer accountID) {
        final String URL = FIXED_DEPOSIT_ACCOUNT_URL + "/" + accountID + "?" + Utils.TENANT_IDENTIFIER;
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

    public static Float getPrincipalAfterCompoundingInterest(Calendar currentDate, Float principal, Integer depositPeriod,
            double interestPerDay, Integer compoundingInterval, Integer postingInterval) {

        Float totalInterest = 0.0f;
        Float interestEarned = 0.0f;

        for (int i = 1; i <= depositPeriod; i++) {
            Integer daysInMonth = currentDate.getActualMaximum(Calendar.DATE);
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

    public HashMap updateFixedDepositAccount(final String clientID, final String productID, final String accountID, final String validFrom,
            final String validTo, final String penalInterestType, final String submittedOnDate) {

        final String fixedDepositApplicationJSON = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec) //
                .withSubmittedOnDate(submittedOnDate) //
                .build(clientID, productID, validFrom, validTo, penalInterestType);

        return Utils.performServerPut(this.requestSpec, this.responseSpec, FIXED_DEPOSIT_ACCOUNT_URL + "/" + accountID + "?"
                + Utils.TENANT_IDENTIFIER, fixedDepositApplicationJSON, CommonConstants.RESPONSE_CHANGES);
    }

    public HashMap updateInterestCalculationConfigForFixedDeposit(final String clientID, final String productID, final String accountID,
            final String submittedOnDate, final String validFrom, final String validTo, final String numberOfDaysPerYear,
            final String penalInterestType, final String interestCalculationType, final String interestCompoundingPeriodType,
            final String interestPostingPeriodType) {

        final String fixedDepositApplicationJSON = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec) //
                .withSubmittedOnDate(submittedOnDate) //
                .withNumberOfDaysPerYear(numberOfDaysPerYear) //
                .withInterestCalculationPeriodType(interestCalculationType) //
                .withInterestCompoundingPeriodType(interestCompoundingPeriodType) //
                .withInterestPostingPeriodType(interestPostingPeriodType) //
                .build(clientID, productID, validFrom, validTo, penalInterestType);

        return Utils.performServerPut(this.requestSpec, this.responseSpec, FIXED_DEPOSIT_ACCOUNT_URL + "/" + accountID + "?"
                + Utils.TENANT_IDENTIFIER, fixedDepositApplicationJSON, CommonConstants.RESPONSE_CHANGES);
    }

    public HashMap approveFixedDeposit(final Integer fixedDepositAccountID, final String approvedOnDate) {
        System.out.println("--------------------------------- APPROVING FIXED DEPOSIT APPLICATION ------------------------------------");
        return performFixedDepositApplicationActions(createFixedDepositOperationURL(APPROVE_FIXED_DEPOSIT_COMMAND, fixedDepositAccountID),
                getApproveFixedDepositAccountAsJSON(approvedOnDate));
    }

    public HashMap undoApproval(final Integer fixedDepositAccountID) {
        System.out.println("--------------------------------- UNDO APPROVING FIXED DEPOSIT APPLICATION -------------------------------");
        final String undoBodyJson = "{'note':'UNDO APPROVAL'}";
        return performFixedDepositApplicationActions(
                createFixedDepositOperationURL(UNDO_APPROVAL_FIXED_DEPOSIT_COMMAND, fixedDepositAccountID), undoBodyJson);
    }

    public HashMap rejectApplication(final Integer fixedDepositAccountID, final String rejectedOnDate) {
        System.out.println("--------------------------------- REJECT FIXED DEPOSIT APPLICATION -------------------------------");
        return performFixedDepositApplicationActions(createFixedDepositOperationURL(REJECT_FIXED_DEPOSIT_COMMAND, fixedDepositAccountID),
                getRejectedFixedDepositAsJSON(rejectedOnDate));
    }

    public HashMap withdrawApplication(final Integer fixedDepositAccountID, final String withdrawApplicationOnDate) {
        System.out.println("--------------------------------- Withdraw FIXED DEPOSIT APPLICATION -------------------------------");
        return performFixedDepositApplicationActions(
                createFixedDepositOperationURL(WITHDRAWN_BY_CLIENT_FIXED_DEPOSIT_COMMAND, fixedDepositAccountID),
                getWithdrawnFixedDepositAccountAsJSON(withdrawApplicationOnDate));
    }

    public HashMap activateFixedDeposit(final Integer fixedDepositAccountID, final String activationDate) {
        System.out.println("---------------------------------- ACTIVATING FIXED DEPOSIT APPLICATION ----------------------------------");
        return performFixedDepositApplicationActions(createFixedDepositOperationURL(ACTIVATE_FIXED_DEPOSIT_COMMAND, fixedDepositAccountID),
                getActivatedFixedDepositAccountAsJSON(activationDate));
    }

    public Object deleteFixedDepositApplication(final Integer fixedDepositAccountID, final String jsonAttributeToGetBack) {
        System.out.println("---------------------------------- DELETE FIXED DEPOSIT APPLICATION ----------------------------------");
        return Utils.performServerDelete(this.requestSpec, this.responseSpec, FIXED_DEPOSIT_ACCOUNT_URL + "/" + fixedDepositAccountID + "?"
                + Utils.TENANT_IDENTIFIER, jsonAttributeToGetBack);

    }

    public Integer calculateInterestForFixedDeposit(final Integer fixedDepositAccountId) {
        System.out.println("--------------------------------- CALCULATING INTEREST FOR FIXED DEPOSIT --------------------------------");
        return (Integer) performFixedDepositActions(
                createFixedDepositCalculateInterestURL(CALCULATE_INTEREST_FIXED_DEPOSIT_COMMAND, fixedDepositAccountId),
                getCalculatedInterestForFixedDepositApplicationAsJSON(), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public Integer postInterestForFixedDeposit(final Integer fixedDepositAccountId) {
        System.out.println("--------------------------------- POST INTEREST FOR FIXED DEPOSIT --------------------------------");
        return (Integer) performFixedDepositActions(
                createFixedDepositCalculateInterestURL(POST_INTEREST_FIXED_DEPOSIT_COMMAND, fixedDepositAccountId),
                getCalculatedInterestForFixedDepositApplicationAsJSON(), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    public HashMap calculatePrematureAmountForFixedDeposit(final Integer fixedDepositAccountId, final String closedOnDate) {
        System.out.println("--------------------- CALCULATING PREMATURE AMOUNT FOR FIXED DEPOSIT ----------------------------");
        return (HashMap) performFixedDepositActions(
                createFixedDepositCalculateInterestURL(CALCULATE_PREMATURE_AMOUNT_COMMAND, fixedDepositAccountId),
                getCalculatedPrematureAmountForFixedDepositAccountAsJSON(closedOnDate), "");
    }

    public Object prematureCloseForFixedDeposit(final Integer fixedDepositAccountId, final String closedOnDate, final String closureType,
            final Integer toSavingsId, final String jsonAttributeToGetBack) {
        System.out.println("--------------------- PREMATURE CLOSE FOR FIXED DEPOSIT ----------------------------");
        return performFixedDepositActions(createFixedDepositCalculateInterestURL(PREMATURE_CLOSE_COMMAND, fixedDepositAccountId),
                getPrematureCloseForFixedDepositAccountAsJSON(closedOnDate, closureType, toSavingsId), jsonAttributeToGetBack);
    }

    private String getApproveFixedDepositAccountAsJSON(final String approvedOnDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("approvedOnDate", approvedOnDate);
        map.put("note", "Approval NOTE");
        String fixedDepositAccountApproveJson = new Gson().toJson(map);
        System.out.println(fixedDepositAccountApproveJson);
        return fixedDepositAccountApproveJson;
    }

    private String getRejectedFixedDepositAsJSON(final String rejectedOnDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("rejectedOnDate", rejectedOnDate);
        map.put("note", "Rejected NOTE");
        String fixedDepositAccountJson = new Gson().toJson(map);
        System.out.println(fixedDepositAccountJson);
        return fixedDepositAccountJson;
    }

    private String getWithdrawnFixedDepositAccountAsJSON(final String withdrawnApplicationOnDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("withdrawnOnDate", withdrawnApplicationOnDate);
        map.put("note", "Withdraw NOTE");
        String fixedDepositAccountJson = new Gson().toJson(map);
        System.out.println(fixedDepositAccountJson);
        return fixedDepositAccountJson;
    }

    private String getActivatedFixedDepositAccountAsJSON(final String activationDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("activatedOnDate", activationDate);
        String fixedDepositAccountActivateJson = new Gson().toJson(map);
        System.out.println(fixedDepositAccountActivateJson);
        return fixedDepositAccountActivateJson;
    }

    private String getCalculatedInterestForFixedDepositApplicationAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        String fixedDepositAccountCalculatedInterestJson = new Gson().toJson(map);
        System.out.println(fixedDepositAccountCalculatedInterestJson);
        return fixedDepositAccountCalculatedInterestJson;
    }

    private String getCalculatedPrematureAmountForFixedDepositAccountAsJSON(final String closedOnDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.locale);
        map.put("dateFormat", CommonConstants.dateFormat);
        map.put("closedOnDate", closedOnDate);
        String fixedDepositAccountPrematureClosureJson = new Gson().toJson(map);
        System.out.println(fixedDepositAccountPrematureClosureJson);
        return fixedDepositAccountPrematureClosureJson;
    }

    private String getPrematureCloseForFixedDepositAccountAsJSON(final String closedOnDate, final String closureType,
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
        String fixedDepositAccountPrematureCloseJson = new Gson().toJson(map);
        System.out.println(fixedDepositAccountPrematureCloseJson);
        return fixedDepositAccountPrematureCloseJson;
    }

    private String createFixedDepositOperationURL(final String command, final Integer fixedDepositAccountID) {
        return FIXED_DEPOSIT_ACCOUNT_URL + "/" + fixedDepositAccountID + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
    }

    private Object performFixedDepositActions(final String postURLForFixedDeposit, final String jsonToBeSent,
            final String jsonAttributeToGetBack) {
        return Utils.performServerPost(this.requestSpec, this.responseSpec, postURLForFixedDeposit, jsonToBeSent, jsonAttributeToGetBack);
    }

    private HashMap performFixedDepositApplicationActions(final String postURLForFixedDepositAction, final String jsonToBeSent) {
        HashMap status = null;
        final HashMap response = Utils.performServerPost(this.requestSpec, this.responseSpec, postURLForFixedDepositAction, jsonToBeSent,
                CommonConstants.RESPONSE_CHANGES);
        if (response != null) {
            status = (HashMap) response.get("status");
        }
        return status;
    }

    private String createFixedDepositCalculateInterestURL(final String command, final Integer fixedDepositAccountID) {
        return FIXED_DEPOSIT_ACCOUNT_URL + "/" + fixedDepositAccountID + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
    }

    public static ArrayList retrieveAllFixedDepositAccounts(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        System.out.println("-------------------- RETRIEVING ALL FIXED DEPOSIT ACCOUNTS ---------------------");
        final ArrayList response = Utils.performServerGet(requestSpec, responseSpec, FIXED_DEPOSIT_ACCOUNT_URL + "?"
                + Utils.TENANT_IDENTIFIER, "");
        return response;
    }

    public FixedDepositAccountHelper withSubmittedOnDate(final String fixedDepositApplicationSubmittedDate) {
        this.submittedOnDate = fixedDepositApplicationSubmittedDate;
        return this;
    }

    public FixedDepositAccountHelper withNumberOfDaysPerYear(final String numberOfDaysPerYearTypeId) {
        this.interestCalculationDaysInYearType = numberOfDaysPerYearTypeId;
        return this;
    }

    public FixedDepositAccountHelper withInterestCalculationPeriodType(final String interestCalculationTypeId) {
        this.interestCalculationType = interestCalculationTypeId;
        return this;
    }

    public FixedDepositAccountHelper withInterestCompoundingPeriodType(final String interestCompoundingPeriodTypeId) {
        this.interestCompoundingPeriodType = interestCompoundingPeriodTypeId;
        return this;
    }

    public FixedDepositAccountHelper withInterestPostingPeriodType(final String interestPostingPeriodTypeId) {
        this.interestPostingPeriodType = interestPostingPeriodTypeId;
        return this;
    }

    public FixedDepositAccountHelper withSavings(final String savingsId) {
        this.savingsId = savingsId;
        return this;
    }

    public FixedDepositAccountHelper transferInterest(final boolean transferInterest) {
        this.transferInterest = transferInterest;
        return this;
    }

    public FixedDepositAccountHelper withLockinPeriodFrequency(final String lockingPeriodFrequencyType, final String lockinPeriodFrequency) {
        this.lockingPeriodFrequencyType = lockingPeriodFrequencyType;
        this.lockinPeriodFrequency = lockinPeriodFrequency;
        return this;
    }
}