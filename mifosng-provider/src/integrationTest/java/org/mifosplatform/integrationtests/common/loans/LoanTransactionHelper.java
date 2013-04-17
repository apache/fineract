package org.mifosplatform.integrationtests.common.loans;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;

import org.mifosplatform.integrationtests.common.Utils;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings("rawtypes")
public class LoanTransactionHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    private static final String CREATE_LOAN_PRODUCT_URL = "/mifosng-provider/api/v1/loanproducts?tenantIdentifier=default";
    private static final String APPLY_LOAN_URL = "/mifosng-provider/api/v1/loans?tenantIdentifier=default";
    private static final String APPROVE_LOAN_COMMAND = "approve";
    private static final String UNDO_APPROVAL_LOAN_COMMAND = "undoApproval";
    private static final String DISBURSE_LOAN_COMMAND = "disburse";
    private static final String UNDO_DISBURSE_LOAN_COMMAND = "undoDisbursal";
    private static final String WRITE_OFF_LOAN_COMMAND = "writeoff";
    private static final String WAIVE_INTEREST_COMMAND = "waiveinterest";
    private static final String MAKE_REPAYMENT_COMMAND = "repayment";
    private static final String WITHDRAW_LOAN_APPLICATION_COMMAND = "withdrawnByApplicant";


    public LoanTransactionHelper(final RequestSpecification requestSpec,final ResponseSpecification responseSpec){
        this.requestSpec  = requestSpec;
        this.responseSpec = responseSpec;
    }
    public Integer getLoanProductId(final String loanProductJSON) {
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_LOAN_PRODUCT_URL, loanProductJSON, "resourceId");
    }

    public Integer getLoanId(final String loanApplicationJSON) {
        return Utils.performServerPost(requestSpec, responseSpec, APPLY_LOAN_URL, loanApplicationJSON, "loanId");
    }

    public ArrayList getLoanRepaymentSchedule(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer loanID) {
        String URL = "/mifosng-provider/api/v1/loans/" + loanID + "?associations=repaymentSchedule&tenantIdentifier=default";
        HashMap response = Utils.performServerGet(requestSpec, responseSpec, URL, "repaymentSchedule");
        return (ArrayList) response.get("periods");
    }

    public  HashMap approveLoan(final String approvalDate, final Integer loanID) {
        return performLoanTransaction(createLoanOperationURL(APPROVE_LOAN_COMMAND, loanID),
                getApproveLoanAsJSON(approvalDate));
    }

    public  HashMap undoApproval(final Integer loanID) {
        String undoBodyJson = "{'note':'UNDO APPROVAL'}";
        return performLoanTransaction(createLoanOperationURL(UNDO_APPROVAL_LOAN_COMMAND, loanID), undoBodyJson);
    }

    public HashMap disburseLoan(final String date,
                                final Integer loanID) {
        return performLoanTransaction(createLoanOperationURL(DISBURSE_LOAN_COMMAND, loanID),
                getDisburseLoanAsJSON(date));
    }

    public HashMap undoDisbursal(final Integer loanID){
        String undoDisburseJson = "{'note' : 'UNDO DISBURSAL'}";
        System.out.println("IN DISBURSE LOAN");
        String url =createLoanOperationURL(UNDO_DISBURSE_LOAN_COMMAND,loanID);
        System.out.println("IN DISBURSE LOAN URL " + url);
        return performLoanTransaction(createLoanOperationURL(UNDO_DISBURSE_LOAN_COMMAND, loanID), undoDisburseJson);
    }
    public HashMap writeOffLoan(final String date,
                                final Integer loanID) {
        return performLoanTransaction(createLoanTransactionURL(WRITE_OFF_LOAN_COMMAND, loanID),
                getWriteOffBodyAsJSON(date));
    }

    public  HashMap waiveInterest(final String date, final String amountToBeWaived, final Integer loanID) {
        return performLoanTransaction(createLoanTransactionURL(WAIVE_INTEREST_COMMAND, loanID),
                getWaiveBodyAsJSON(date, amountToBeWaived));
    }

    public HashMap makeRepayment(final String date, final Float amountToBePaid, final Integer loanID) {
        return performLoanTransaction(createLoanTransactionURL(MAKE_REPAYMENT_COMMAND, loanID),
                getRepaymentBodyAsJSON(date, amountToBePaid));
    }

    public HashMap withdrawLoanApplicationByClient(final String date, final Integer loanID) {
        return performLoanTransaction(createLoanOperationURL(WITHDRAW_LOAN_APPLICATION_COMMAND,loanID),
                getWithdrawLoanApplicationBodyAsJSON(date));
    }

    private String getDisburseLoanAsJSON(final String actualDisbursementDate) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("actualDisbursementDate", actualDisbursementDate);
        map.put("note", "DISBURSE NOTE");
        return new Gson().toJson(map);
    }

    private String getApproveLoanAsJSON(final String approvalDate) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("approvedOnDate", approvalDate);
        map.put("note", "Approval NOTE");
        return new Gson().toJson(map);
    }

    private String getRepaymentBodyAsJSON(final String transactionDate, final Float transactionAmount) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("transactionDate", transactionDate);
        map.put("transactionAmount", transactionAmount.toString());
        map.put("note", "Repayment Made!!!");
        return new Gson().toJson(map);
    }

    private String getWriteOffBodyAsJSON(final String transactionDate) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("note", " LOAN WRITE OFF!!!");
        map.put("transactionDate", transactionDate);
        return new Gson().toJson(map);
    }

    private String getWaiveBodyAsJSON(final String transactionDate, final String amountToBeWaived) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("transactionDate", transactionDate);
        map.put("transactionAmount", amountToBeWaived);
        map.put("note", " Interest Waived!!!");
        return new Gson().toJson(map);
    }

    private String getWithdrawLoanApplicationBodyAsJSON(final String withdrawDate){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("withdrawnOnDate", withdrawDate);
        map.put("note", " Loan Withdrawn By Client!!!");
        return new Gson().toJson(map);

    }
    public String getLoanCalculationBodyAsJSON(final String productID) {
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

    private  String createLoanOperationURL(final String command, final Integer loanID) {
        return "/mifosng-provider/api/v1/loans/" + loanID + "?command=" + command + "&tenantIdentifier=default";
    }

    private  String createLoanTransactionURL(final String command, final Integer loanID) {
        return "/mifosng-provider/api/v1/loans/" + loanID + "/transactions?command=" + command + "&tenantIdentifier=default";
    }

    private  HashMap performLoanTransaction(final String postURLForLoanTransaction, final String jsonToBeSent) {

        HashMap response = Utils.performServerPost(requestSpec, responseSpec, postURLForLoanTransaction, jsonToBeSent, "changes");
        return (HashMap) response.get("status");
    }

    @SuppressWarnings("unchecked")
    public void verifyRepaymentScheduleEntryFor(final int repaymentNumber, final float expectedPrincipalOutstanding, final Integer loanID) {
        System.out.println("---------------------------GETTING LOAN REPAYMENT SCHEDULE--------------------------------");
        ArrayList<HashMap> repaymentPeriods = getLoanRepaymentSchedule(requestSpec, responseSpec, loanID);
        assertEquals("Mismatch in Principal Loan Balance Outstanding ", expectedPrincipalOutstanding, repaymentPeriods.get(repaymentNumber).get("principalLoanBalanceOutstanding"));
    }
}