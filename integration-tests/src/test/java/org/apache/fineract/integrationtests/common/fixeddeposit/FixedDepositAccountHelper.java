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
package org.apache.fineract.integrationtests.common.fixeddeposit;

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "unused", "rawtypes" })
public class FixedDepositAccountHelper {

    private static final Logger LOG = LoggerFactory.getLogger(FixedDepositAccountHelper.class);
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public FixedDepositAccountHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    private static final String FIXED_DEPOSIT_ACCOUNT_URL = "/fineract-provider/api/v1/fixeddepositaccounts";
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
    public static final String DEPOSIT_AMOUNT = "100000";
    private String newDepositAmount = null;

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

    private String depositPeriod = "14";
    private final String depositPeriodFrequencyId = MONTHS;
    private String submittedOnDate = "";
    private String savingsId = null;
    private boolean transferInterest = false;
    private Integer maturityInstructionId;
    private List<HashMap<String, String>> charges;

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public String build(final String clientId, final String productId, final String penalInterestType) {
        final HashMap<String, Object> map = new HashMap<>();

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
        map.put("depositAmount", getDepositAmount());
        map.put("depositPeriod", this.depositPeriod);
        map.put("depositPeriodFrequencyId", this.depositPeriodFrequencyId);
        map.put("submittedOnDate", this.submittedOnDate);
        map.put("linkAccountId", savingsId);
        map.put("transferInterestToSavings", transferInterest);
        map.put("maturityInstructionId", maturityInstructionId);
        map.put("charges", charges);

        String fixedDepositAccountJson = new Gson().toJson(map);
        LOG.info("{}", fixedDepositAccountJson);
        return fixedDepositAccountJson;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer applyFixedDepositApplicationGetId(final String fixedDepositAccountAsJson, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        LOG.info("--------------------- APPLYING FOR FIXED DEPOSIT ACCOUNT ------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, APPLY_FIXED_DEPOSIT_ACCOUNT_URL, fixedDepositAccountAsJson,
                CommonConstants.RESPONSE_RESOURCE_ID);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String applyFixedDepositApplication(final String fixedDepositAccountAsJson, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        LOG.info("--------------------- APPLYING FOR FIXED DEPOSIT ACCOUNT ------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, APPLY_FIXED_DEPOSIT_ACCOUNT_URL, fixedDepositAccountAsJson);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static HashMap getFixedDepositAccountById(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer accountID) {
        final String GET_FIXED_DEPOSIT_BY_ID_URL = FIXED_DEPOSIT_ACCOUNT_URL + "/" + accountID + "?" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------------ RETRIEVING FIXED DEPOSIT ACCOUNT BY ID -------------------------");
        return Utils.performServerGet(requestSpec, responseSpec, GET_FIXED_DEPOSIT_BY_ID_URL, "");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public HashMap getFixedDepositSummary(final Integer accountID) {
        return getFixedDepositDetails(accountID, "summary");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public HashMap getFixedDepositDetails(final Integer accountID) {
        return getFixedDepositDetails(accountID, "");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private HashMap getFixedDepositDetails(final Integer accountID, final String jsonAttributeToGetBack) {
        final String URL = FIXED_DEPOSIT_ACCOUNT_URL + "/" + accountID + "?" + Utils.TENANT_IDENTIFIER;
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, URL, jsonAttributeToGetBack);
        return response;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
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

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
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
                LOG.info("{}", principal.toString());

            }
            currentDate.add(Calendar.MONTH, 1);
            interestEarned = 0.0f;
        }
        return principal;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public HashMap updateFixedDepositAccount(final String clientID, final String productID, final String accountID, final String validFrom,
            final String validTo, final String penalInterestType, final String submittedOnDate) {

        final String fixedDepositApplicationJSON = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec) //
                .withSubmittedOnDate(submittedOnDate) //
                .build(clientID, productID, penalInterestType);

        return Utils.performServerPut(this.requestSpec, this.responseSpec,
                FIXED_DEPOSIT_ACCOUNT_URL + "/" + accountID + "?" + Utils.TENANT_IDENTIFIER, fixedDepositApplicationJSON,
                CommonConstants.RESPONSE_CHANGES);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
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
                .build(clientID, productID, penalInterestType);

        return Utils.performServerPut(this.requestSpec, this.responseSpec,
                FIXED_DEPOSIT_ACCOUNT_URL + "/" + accountID + "?" + Utils.TENANT_IDENTIFIER, fixedDepositApplicationJSON,
                CommonConstants.RESPONSE_CHANGES);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public HashMap approveFixedDeposit(final Integer fixedDepositAccountID, final String approvedOnDate) {
        LOG.info("--------------------------------- APPROVING FIXED DEPOSIT APPLICATION ------------------------------------");
        return performFixedDepositApplicationActions(createFixedDepositOperationURL(APPROVE_FIXED_DEPOSIT_COMMAND, fixedDepositAccountID),
                getApproveFixedDepositAccountAsJSON(approvedOnDate));
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public HashMap undoApproval(final Integer fixedDepositAccountID) {
        LOG.info("--------------------------------- UNDO APPROVING FIXED DEPOSIT APPLICATION -------------------------------");
        final String undoBodyJson = "{'note':'UNDO APPROVAL'}";
        return performFixedDepositApplicationActions(
                createFixedDepositOperationURL(UNDO_APPROVAL_FIXED_DEPOSIT_COMMAND, fixedDepositAccountID), undoBodyJson);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public HashMap rejectApplication(final Integer fixedDepositAccountID, final String rejectedOnDate) {
        LOG.info("--------------------------------- REJECT FIXED DEPOSIT APPLICATION -------------------------------");
        return performFixedDepositApplicationActions(createFixedDepositOperationURL(REJECT_FIXED_DEPOSIT_COMMAND, fixedDepositAccountID),
                getRejectedFixedDepositAsJSON(rejectedOnDate));
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public HashMap withdrawApplication(final Integer fixedDepositAccountID, final String withdrawApplicationOnDate) {
        LOG.info("--------------------------------- Withdraw FIXED DEPOSIT APPLICATION -------------------------------");
        return performFixedDepositApplicationActions(
                createFixedDepositOperationURL(WITHDRAWN_BY_CLIENT_FIXED_DEPOSIT_COMMAND, fixedDepositAccountID),
                getWithdrawnFixedDepositAccountAsJSON(withdrawApplicationOnDate));
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public HashMap activateFixedDeposit(final Integer fixedDepositAccountID, final String activationDate) {
        LOG.info("---------------------------------- ACTIVATING FIXED DEPOSIT APPLICATION ----------------------------------");
        return performFixedDepositApplicationActions(createFixedDepositOperationURL(ACTIVATE_FIXED_DEPOSIT_COMMAND, fixedDepositAccountID),
                getActivatedFixedDepositAccountAsJSON(activationDate));
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Object deleteFixedDepositApplication(final Integer fixedDepositAccountID, final String jsonAttributeToGetBack) {
        LOG.info("---------------------------------- DELETE FIXED DEPOSIT APPLICATION ----------------------------------");
        return Utils.performServerDelete(this.requestSpec, this.responseSpec,
                FIXED_DEPOSIT_ACCOUNT_URL + "/" + fixedDepositAccountID + "?" + Utils.TENANT_IDENTIFIER, jsonAttributeToGetBack);

    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Integer calculateInterestForFixedDeposit(final Integer fixedDepositAccountId) {
        LOG.info("--------------------------------- CALCULATING INTEREST FOR FIXED DEPOSIT --------------------------------");
        return (Integer) performFixedDepositActions(
                createFixedDepositCalculateInterestURL(CALCULATE_INTEREST_FIXED_DEPOSIT_COMMAND, fixedDepositAccountId),
                getCalculatedInterestForFixedDepositApplicationAsJSON(), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Integer postInterestForFixedDeposit(final Integer fixedDepositAccountId) {
        LOG.info("--------------------------------- POST INTEREST FOR FIXED DEPOSIT --------------------------------");
        return (Integer) performFixedDepositActions(
                createFixedDepositCalculateInterestURL(POST_INTEREST_FIXED_DEPOSIT_COMMAND, fixedDepositAccountId),
                getCalculatedInterestForFixedDepositApplicationAsJSON(), CommonConstants.RESPONSE_RESOURCE_ID);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public HashMap calculatePrematureAmountForFixedDeposit(final Integer fixedDepositAccountId, final String closedOnDate) {
        LOG.info("--------------------- CALCULATING PREMATURE AMOUNT FOR FIXED DEPOSIT ----------------------------");
        return (HashMap) performFixedDepositActions(
                createFixedDepositCalculateInterestURL(CALCULATE_PREMATURE_AMOUNT_COMMAND, fixedDepositAccountId),
                getCalculatedPrematureAmountForFixedDepositAccountAsJSON(closedOnDate), "");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Object prematureCloseForFixedDeposit(final Integer fixedDepositAccountId, final String closedOnDate, final String closureType,
            final Integer toSavingsId, final String jsonAttributeToGetBack) {
        LOG.info("--------------------- PREMATURE CLOSE FOR FIXED DEPOSIT ----------------------------");
        return performFixedDepositActions(createFixedDepositCalculateInterestURL(PREMATURE_CLOSE_COMMAND, fixedDepositAccountId),
                getPrematureCloseForFixedDepositAccountAsJSON(closedOnDate, closureType, toSavingsId), jsonAttributeToGetBack);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private String getApproveFixedDepositAccountAsJSON(final String approvedOnDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("approvedOnDate", approvedOnDate);
        map.put("note", "Approval NOTE");
        String fixedDepositAccountApproveJson = new Gson().toJson(map);
        LOG.info(fixedDepositAccountApproveJson);
        return fixedDepositAccountApproveJson;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private String getRejectedFixedDepositAsJSON(final String rejectedOnDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("rejectedOnDate", rejectedOnDate);
        map.put("note", "Rejected NOTE");
        String fixedDepositAccountJson = new Gson().toJson(map);
        LOG.info("{}", fixedDepositAccountJson);
        return fixedDepositAccountJson;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private String getWithdrawnFixedDepositAccountAsJSON(final String withdrawnApplicationOnDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("withdrawnOnDate", withdrawnApplicationOnDate);
        map.put("note", "Withdraw NOTE");
        String fixedDepositAccountJson = new Gson().toJson(map);
        LOG.info("{}", fixedDepositAccountJson);
        return fixedDepositAccountJson;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private String getActivatedFixedDepositAccountAsJSON(final String activationDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("activatedOnDate", activationDate);
        String fixedDepositAccountActivateJson = new Gson().toJson(map);
        LOG.info("{}", fixedDepositAccountActivateJson);
        return fixedDepositAccountActivateJson;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private String getCalculatedInterestForFixedDepositApplicationAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        String fixedDepositAccountCalculatedInterestJson = new Gson().toJson(map);
        LOG.info(fixedDepositAccountCalculatedInterestJson);
        return fixedDepositAccountCalculatedInterestJson;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private String getCalculatedPrematureAmountForFixedDepositAccountAsJSON(final String closedOnDate) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("closedOnDate", closedOnDate);
        String fixedDepositAccountPrematureClosureJson = new Gson().toJson(map);
        LOG.info(fixedDepositAccountPrematureClosureJson);
        return fixedDepositAccountPrematureClosureJson;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private String getPrematureCloseForFixedDepositAccountAsJSON(final String closedOnDate, final String closureType,
            final Integer toSavingsId) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", CommonConstants.LOCALE);
        map.put("dateFormat", CommonConstants.DATE_FORMAT);
        map.put("closedOnDate", closedOnDate);
        map.put("onAccountClosureId", closureType);
        if (toSavingsId != null) {
            map.put("toSavingsAccountId", toSavingsId);
            map.put("transferDescription", "Transferring To Savings Account");
        }
        String fixedDepositAccountPrematureCloseJson = new Gson().toJson(map);
        LOG.info(fixedDepositAccountPrematureCloseJson);
        return fixedDepositAccountPrematureCloseJson;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private String createFixedDepositOperationURL(final String command, final Integer fixedDepositAccountID) {
        return FIXED_DEPOSIT_ACCOUNT_URL + "/" + fixedDepositAccountID + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private Object performFixedDepositActions(final String postURLForFixedDeposit, final String jsonToBeSent,
            final String jsonAttributeToGetBack) {
        return Utils.performServerPost(this.requestSpec, this.responseSpec, postURLForFixedDeposit, jsonToBeSent, jsonAttributeToGetBack);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private HashMap performFixedDepositApplicationActions(final String postURLForFixedDepositAction, final String jsonToBeSent) {
        HashMap status = null;
        final HashMap response = Utils.performServerPost(this.requestSpec, this.responseSpec, postURLForFixedDepositAction, jsonToBeSent,
                CommonConstants.RESPONSE_CHANGES);
        if (response != null) {
            status = (HashMap) response.get("status");
        }
        return status;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private String createFixedDepositCalculateInterestURL(final String command, final Integer fixedDepositAccountID) {
        return FIXED_DEPOSIT_ACCOUNT_URL + "/" + fixedDepositAccountID + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static ArrayList retrieveAllFixedDepositAccounts(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        LOG.info("-------------------- RETRIEVING ALL FIXED DEPOSIT ACCOUNTS ---------------------");
        final ArrayList response = Utils.performServerGet(requestSpec, responseSpec,
                FIXED_DEPOSIT_ACCOUNT_URL + "?" + Utils.TENANT_IDENTIFIER, "");
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

    public FixedDepositAccountHelper withLockinPeriodFrequency(final String lockingPeriodFrequencyType,
            final String lockinPeriodFrequency) {
        this.lockingPeriodFrequencyType = lockingPeriodFrequencyType;
        this.lockinPeriodFrequency = lockinPeriodFrequency;
        return this;
    }

    public FixedDepositAccountHelper withDepositPeriod(final String depositPeriod) {
        this.depositPeriod = depositPeriod;
        return this;
    }

    public FixedDepositAccountHelper withDepositAmount(final String depositAmount) {
        this.newDepositAmount = depositAmount;
        return this;
    }

    private String getDepositAmount() {
        if (this.newDepositAmount == null) {
            return DEPOSIT_AMOUNT;
        }
        return this.newDepositAmount;
    }

    public FixedDepositAccountHelper withMaturityInstructionId(Integer maturityInstructionId) {
        this.maturityInstructionId = maturityInstructionId;
        return this;
    }

    public FixedDepositAccountHelper withCharges(List<HashMap<String, String>> charges) {
        this.charges = charges;
        return this;
    }
}
