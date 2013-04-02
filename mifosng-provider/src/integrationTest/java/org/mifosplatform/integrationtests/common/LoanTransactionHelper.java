package org.mifosplatform.integrationtests.common;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

import java.util.ArrayList;
import java.util.HashMap;

public class LoanTransactionHelper {

    private static final String CREATE_LOAN_PRODUCT_URL = "/mifosng-provider/api/v1/loanproducts?tenantIdentifier=default";
    private static final String APPLY_LOAN_URL = "/mifosng-provider/api/v1/loans?tenantIdentifier=default";
    private static final String APPROVE_LOAN_COMMAND = "approve";
    private static final String UNDO_APPROVAL_LOAN_COMMAND = "undoApproval";
    private static final String DISBURSE_LOAN_COMMAND = "disburse";
    private static final String WRITE_OFF_LOAN_COMMAND = "writeoff";
    private static final String WAIVE_INTEREST_COMMAND = "waiveinterest";
    private static final String MAKE_REPAYMENT_COMMAND = "repayment";




    public static Integer getLoanProductId(RequestSpecification requestSpec, ResponseSpecification responseSpec, String loanProductJSON) {
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_LOAN_PRODUCT_URL, loanProductJSON, "resourceId");
    }

    public static Integer getLoanId(RequestSpecification requestSpec, ResponseSpecification responseSpec, String loanApplicationJSON) {
        return Utils.performServerPost(requestSpec, responseSpec, APPLY_LOAN_URL, loanApplicationJSON, "loanId");
    }

    public static ArrayList getLoanRepaymentSchedule(RequestSpecification requestSpec, ResponseSpecification responseSpec,final Integer loanID){
        String URL = "/mifosng-provider/api/v1/loans/" + loanID +"?associations=repaymentSchedule&tenantIdentifier=default";
        HashMap response = Utils.performServerGet(requestSpec, responseSpec, URL, "repaymentSchedule");
        return (ArrayList) response.get("periods");
    }

    public static HashMap approveLoan(RequestSpecification requestSpec, ResponseSpecification responseSpec, String approvalDate, Integer loanID) {
        return performLoanTransaction(requestSpec, responseSpec, createLoanOperationURL(APPROVE_LOAN_COMMAND, loanID), getApproveLoanAsJSON(approvalDate));
    }

    public static HashMap undoApproval(RequestSpecification requestSpec, ResponseSpecification responseSpec, Integer loanID) {
        String undoBodyJson = "{'note':'UNDO APPROVAL'}";
        return performLoanTransaction(requestSpec, responseSpec, createLoanOperationURL(UNDO_APPROVAL_LOAN_COMMAND, loanID), undoBodyJson);
    }

    public static HashMap disburseLoan(RequestSpecification requestSpec, ResponseSpecification responseSpec, String date, Integer loanID) {
        return performLoanTransaction(requestSpec, responseSpec, createLoanOperationURL(DISBURSE_LOAN_COMMAND, loanID), getDisburseLoanAsJSON(date));
    }

    public static HashMap writeOffLoan(RequestSpecification requestSpec, ResponseSpecification responseSpec, String date, Integer loanID) {
        return performLoanTransaction(requestSpec, responseSpec, createLoanTransactionURL(WRITE_OFF_LOAN_COMMAND, loanID), getWriteOffBodyAsJSON(date));
    }

    public static HashMap waiveInterest(RequestSpecification requestSpec, ResponseSpecification responseSpec, String date,String amountToBeWaived, Integer loanID) {
        return performLoanTransaction(requestSpec, responseSpec, createLoanTransactionURL(WAIVE_INTEREST_COMMAND,loanID), getWaiveBodyAsJSON(date, amountToBeWaived));
    }

    public static HashMap makeRepayment(RequestSpecification requestSpec, ResponseSpecification responseSpec, String date,Float amountToBePaid, Integer loanID) {
        return performLoanTransaction(requestSpec, responseSpec, createLoanTransactionURL(MAKE_REPAYMENT_COMMAND,loanID), getRepaymentBodyAsJSON(date,amountToBePaid));
    }

    public static String getDisburseLoanAsJSON(String actualDisbursementDate) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("actualDisbursementDate", actualDisbursementDate);
        map.put("note", "DISBURSE NOTE");
        return new Gson().toJson(map);
    }

    public static String getApproveLoanAsJSON(String approvalDate) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("approvedOnDate", approvalDate);
        map.put("note", "Approval NOTE");
        return new Gson().toJson(map);
    }

    public static String getRepaymentBodyAsJSON(String transactionDate, Float transactionAmount) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("transactionDate", transactionDate);
        map.put("transactionAmount", transactionAmount.toString());
        map.put("note", "Repayment Made!!!");
        return new Gson().toJson(map);
    }

    public static String getWriteOffBodyAsJSON(String transactionDate) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("note", " LOAN WRITE OFF!!!");
        map.put("transactionDate", transactionDate);
        return new Gson().toJson(map);
    }

    public static String getWaiveBodyAsJSON(String transactionDate, String amountToBeWaived) {
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


    private static String createLoanOperationURL(String command, Integer loanID) {
        return "/mifosng-provider/api/v1/loans/" + loanID + "?command=" + command + "&tenantIdentifier=default";
    }

    private static String createLoanTransactionURL(String command, Integer loanID) {
        return "/mifosng-provider/api/v1/loans/" + loanID + "/transactions?command="+command+"&tenantIdentifier=default";
    }

    private static HashMap performLoanTransaction(RequestSpecification requestSpec, ResponseSpecification responseSpec, String postURLForLoanTransaction, String jsonToBeSent) {

        HashMap response = Utils.performServerPost(requestSpec, responseSpec, postURLForLoanTransaction, jsonToBeSent, "changes");
        return (HashMap) response.get("status");
    }

}
