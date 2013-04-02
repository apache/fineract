package org.mifosplatform.integrationtests.common;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings("rawtypes")
public class LoanTransactionHelper {

    private static final String CREATE_LOAN_PRODUCT_URL = "/mifosng-provider/api/v1/loanproducts?tenantIdentifier=default";
    private static final String APPLY_LOAN_URL = "/mifosng-provider/api/v1/loans?tenantIdentifier=default";
    private static final String APPROVE_LOAN_COMMAND = "approve";
    private static final String UNDO_APPROVAL_LOAN_COMMAND = "undoApproval";
    private static final String DISBURSE_LOAN_COMMAND = "disburse";
    private static final String WRITE_OFF_LOAN_COMMAND = "writeoff";
    private static final String WAIVE_INTEREST_COMMAND = "waiveinterest";
    private static final String MAKE_REPAYMENT_COMMAND = "repayment";

    public static Integer getLoanProductId(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String loanProductJSON) {
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_LOAN_PRODUCT_URL, loanProductJSON, "resourceId");
    }

    public static Integer getLoanId(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String loanApplicationJSON) {
        return Utils.performServerPost(requestSpec, responseSpec, APPLY_LOAN_URL, loanApplicationJSON, "loanId");
    }

    public static ArrayList getLoanRepaymentSchedule(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer loanID) {
        String URL = "/mifosng-provider/api/v1/loans/" + loanID + "?associations=repaymentSchedule&tenantIdentifier=default";
        HashMap response = Utils.performServerGet(requestSpec, responseSpec, URL, "repaymentSchedule");
        return (ArrayList) response.get("periods");
    }

    public static HashMap approveLoan(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String approvalDate, final Integer loanID) {
        return performLoanTransaction(requestSpec, responseSpec, createLoanOperationURL(APPROVE_LOAN_COMMAND, loanID),
                getApproveLoanAsJSON(approvalDate));
    }

    public static HashMap undoApproval(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer loanID) {
        String undoBodyJson = "{'note':'UNDO APPROVAL'}";
        return performLoanTransaction(requestSpec, responseSpec, createLoanOperationURL(UNDO_APPROVAL_LOAN_COMMAND, loanID), undoBodyJson);
    }

    public static HashMap disburseLoan(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final String date,
            final Integer loanID) {
        return performLoanTransaction(requestSpec, responseSpec, createLoanOperationURL(DISBURSE_LOAN_COMMAND, loanID),
                getDisburseLoanAsJSON(date));
    }

    public static HashMap writeOffLoan(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final String date,
            final Integer loanID) {
        return performLoanTransaction(requestSpec, responseSpec, createLoanTransactionURL(WRITE_OFF_LOAN_COMMAND, loanID),
                getWriteOffBodyAsJSON(date));
    }

    public static HashMap waiveInterest(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String date, final String amountToBeWaived, final Integer loanID) {
        return performLoanTransaction(requestSpec, responseSpec, createLoanTransactionURL(WAIVE_INTEREST_COMMAND, loanID),
                getWaiveBodyAsJSON(date, amountToBeWaived));
    }

    public static HashMap makeRepayment(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String date, final Float amountToBePaid, final Integer loanID) {
        return performLoanTransaction(requestSpec, responseSpec, createLoanTransactionURL(MAKE_REPAYMENT_COMMAND, loanID),
                getRepaymentBodyAsJSON(date, amountToBePaid));
    }

    public static String getDisburseLoanAsJSON(final String actualDisbursementDate) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("actualDisbursementDate", actualDisbursementDate);
        map.put("note", "DISBURSE NOTE");
        return new Gson().toJson(map);
    }

    public static String getApproveLoanAsJSON(final String approvalDate) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("approvedOnDate", approvalDate);
        map.put("note", "Approval NOTE");
        return new Gson().toJson(map);
    }

    public static String getRepaymentBodyAsJSON(final String transactionDate, final Float transactionAmount) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("transactionDate", transactionDate);
        map.put("transactionAmount", transactionAmount.toString());
        map.put("note", "Repayment Made!!!");
        return new Gson().toJson(map);
    }

    public static String getWriteOffBodyAsJSON(final String transactionDate) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("note", " LOAN WRITE OFF!!!");
        map.put("transactionDate", transactionDate);
        return new Gson().toJson(map);
    }

    public static String getWaiveBodyAsJSON(final String transactionDate, final String amountToBeWaived) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("transactionDate", transactionDate);
        map.put("transactionAmount", amountToBeWaived);
        map.put("note", " Interest Waived!!!");
        return new Gson().toJson(map);
    }

    public static String getLoanCalculationBodyAsJSON(final String productID) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en_GB");
        map.put("productId", productID);
        map.put("principal", "4,500.00");
        map.put("loanTermFrequency", "4");
        map.put("loanTermFrequencyType", "2");
        map.put("numberOfRepayments", "4");
        map.put("repaymentEvery", "1");
        map.put("repaymentFrequencyType", "2");
        map.put("interestRateFrequencyType", "2");
        map.put("interestRatePerPeriod", "2");
        map.put("amortizationType", "1");
        map.put("interestType", "0");
        map.put("interestCalculationPeriodType", "1");
        map.put("expectedDisbursementDate", "20 September 2011");
        map.put("transactionProcessingStrategyId", "1");
        return new Gson().toJson(map);
    }

    private static String createLoanOperationURL(final String command, final Integer loanID) {
        return "/mifosng-provider/api/v1/loans/" + loanID + "?command=" + command + "&tenantIdentifier=default";
    }

    private static String createLoanTransactionURL(final String command, final Integer loanID) {
        return "/mifosng-provider/api/v1/loans/" + loanID + "/transactions?command=" + command + "&tenantIdentifier=default";
    }

    private static HashMap performLoanTransaction(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String postURLForLoanTransaction, final String jsonToBeSent) {

        HashMap response = Utils.performServerPost(requestSpec, responseSpec, postURLForLoanTransaction, jsonToBeSent, "changes");
        return (HashMap) response.get("status");
    }
}