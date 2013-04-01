package org.mifosplatform.integrationtests.common;

import com.google.gson.Gson;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.post;
import static com.jayway.restassured.path.json.JsonPath.from;

import junit.framework.Assert;


public class Utils {
    private static final String CREATE_LOAN_PRODUCT_URL ="/mifosng-provider/api/v1/loanproducts?tenantIdentifier=default";
    private static final String LOGIN_URL = "/mifosng-provider/api/v1/authentication?username=mifos&password=password&tenantIdentifier=default";
    private static final String CREATE_CLIENT_URL = "/mifosng-provider/api/v1/clients?tenantIdentifier=default";
    private static final String APPLY_LOAN_URL = "/mifosng-provider/api/v1/loans?tenantIdentifier=default";
    private static final String REPAYMENT_SCHEDULE_URL ="/mifosng-provider/api/v1/loans?command=calculateLoanSchedule&tenantIdentifier=default";

    public static String randomStringGenerator(final String prefix, final int len, final String sourceSetString) {
        int lengthOfSource = sourceSetString.length();
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append((sourceSetString).charAt(rnd.nextInt(lengthOfSource)));
        return (prefix+(sb.toString()));
    }

    public static String randomNameGenerator(final String prefix, final int lenOfRandomSuffix) {
        return randomStringGenerator(prefix, lenOfRandomSuffix, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    public static String randomIDGenerator(final String prefix, final int lenOfRandomSuffix) {
        return randomStringGenerator(prefix, lenOfRandomSuffix, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    public static String getTestClientAsJSON(String dateOfJoining) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("officeId", "1");
        map.put("firstname", Utils.randomNameGenerator("Client_FirstName_", 5));
        map.put("lastname", Utils.randomNameGenerator("Client_LastName_", 4));
        map.put("externalId", Utils.randomIDGenerator("ID_", 7));
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("joinedDate", dateOfJoining);
        System.out.println("map : "+map);
        return new Gson().toJson(map);
    }

    public static String getDisburseLoanAsJSON(String actualDisbursementDate){
        HashMap<String,String> map = new HashMap<String, String>();
        map.put("locale","en");
        map.put("dateFormat","dd MMMM yyyy");
        map.put("actualDisbursementDate",actualDisbursementDate);
        map.put("note", "DISBURSE NOTE");
        return new Gson().toJson(map);
    }

    public static String getApproveLoanAsJSON(String approvalDate){
        HashMap<String,String> map = new HashMap<String, String>();
        map.put("locale","en");
        map.put("dateFormat","dd MMMM yyyy");
        map.put("approvedOnDate",approvalDate);
        map.put("note", "Approval NOTE");
        return new Gson().toJson(map);
    }

    public static String getRepaymentBodyAsJSON(String transactionDate,Float transactionAmount){
        HashMap<String,String> map = new HashMap<String, String>();
        map.put("locale","en");
        map.put("dateFormat","dd MMMM yyyy");
        map.put("transactionDate",transactionDate);
        map.put("transactionAmount",transactionAmount.toString());
        map.put("note", "Repayment Made!!!");
        return new Gson().toJson(map);
    }

    public static String getWriteOffBodyAsJSON(String transactionDate){
        HashMap<String,String> map = new HashMap<String, String>();
        map.put("dateFormat","dd MMMM yyyy");
        map.put("locale","en");
        map.put("note", " LOAN WRITE OFF!!!");
        map.put("transactionDate",transactionDate);
        return new Gson().toJson(map);
    }
    public static String getWaiveBodyAsJSON(String transactionDate,String amountToBeWaived){
        HashMap<String,String> map = new HashMap<String, String>();
        map.put("locale","en");
        map.put("dateFormat","dd MMMM yyyy");
        map.put("transactionDate",transactionDate);
        map.put("transactionAmount",amountToBeWaived);
        map.put("note", " Interest Waived!!!");
        return new Gson().toJson(map);
    }

    private static String getLoanCalculationBodyAsJSON(final String productID){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en_GB");
        map.put("productId", productID);
        map.put("principal", "12,000.00");
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


    public static String loginIntoServerAndGetBase64EncodedAuthenticationKey() {
        System.out.println("-----------------------------------LOGIN-----------------------------------------");
        String json = post(LOGIN_URL).asString();
        return JsonPath.with(json).get("base64EncodedAuthenticationKey");
    }



    public static Integer createClient(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        return createClient(requestSpec,responseSpec,"04 March 2012");
    }

    public static Integer createClient(RequestSpecification requestSpec, ResponseSpecification responseSpec,String dateOfJoining) {
        System.out.println("---------------------------------CREATING A CLIENT---------------------------------------------");
        return performServerPost(requestSpec,responseSpec,CREATE_CLIENT_URL,getTestClientAsJSON(dateOfJoining),"clientId");
    }


    public static void verifyClientCreatedOnServer(RequestSpecification requestSpec, ResponseSpecification responseSpec, Integer generatedClientID) {
        System.out.println("------------------------------CHECK CLIENT DETAILS------------------------------------\n");
        String  CLIENT_URL="/mifosng-provider/api/v1/clients/" + generatedClientID + "?tenantIdentifier=default";
        Integer responseClientID = performServerGet(requestSpec,responseSpec,CLIENT_URL,"id");
        Assert.assertEquals(generatedClientID,responseClientID);
    }

    public static Integer getLoanProductId(RequestSpecification requestSpec, ResponseSpecification responseSpec, String loanProductJSON) {
        return performServerPost(requestSpec, responseSpec, CREATE_LOAN_PRODUCT_URL, loanProductJSON, "resourceId");
    }

    public static Integer getLoanId(RequestSpecification requestSpec, ResponseSpecification responseSpec, String loanApplicationJSON) {
        return performServerPost(requestSpec, responseSpec, APPLY_LOAN_URL, loanApplicationJSON, "loanId");
    }


    public static ArrayList getRepaymentSchedule(RequestSpecification requestSpec, ResponseSpecification responseSpec,Integer productID){
         return performServerPost(requestSpec,responseSpec,REPAYMENT_SCHEDULE_URL,getLoanCalculationBodyAsJSON(productID.toString()),"periods");
    }

    public static HashMap approveLoan(RequestSpecification requestSpec, ResponseSpecification responseSpec,String approvalDate,Integer loanID){
        String approveLoanURL ="/mifosng-provider/api/v1/loans/"+ loanID +"?command=approve&tenantIdentifier=default";
        HashMap response = performServerPost(requestSpec,responseSpec,approveLoanURL,getApproveLoanAsJSON(approvalDate),"changes");
        return (HashMap)response.get("status");
    }

    public static HashMap undoApproval(RequestSpecification requestSpec, ResponseSpecification responseSpec,Integer loanID){
        String undoBodyJson =   "{'note':'UNDO APPROVAL'}";
        String approveLoanURL ="/mifosng-provider/api/v1/loans/"+ loanID +"?command=undoApproval&tenantIdentifier=default";
        HashMap response = performServerPost(requestSpec,responseSpec,approveLoanURL,undoBodyJson,"changes");
        return (HashMap)response.get("status");
    }

    public static HashMap disburseLoan(RequestSpecification requestSpec, ResponseSpecification responseSpec,Integer loanID,String date){
        String disburseBodyJson =getDisburseLoanAsJSON(date);
        String disburseLoanURL ="/mifosng-provider/api/v1/loans/" + loanID + "?command=disburse&tenantIdentifier=default";
        HashMap response=performServerPost(requestSpec,responseSpec,disburseLoanURL,disburseBodyJson,"changes");
        return (HashMap)response.get("status");
    }

    public static HashMap writeOffLoan(RequestSpecification requestSpec, ResponseSpecification responseSpec,String date,Integer loanID){
        String writeOffJsonBody =getWriteOffBodyAsJSON(date);
        String writeOffURL = "/mifosng-provider/api/v1/loans/" + loanID + "/transactions?command=writeoff&tenantIdentifier=default";
        HashMap response = performServerPost(requestSpec,responseSpec,writeOffURL,writeOffJsonBody,"changes");
        return (HashMap)response.get("status");
    }

    private static <T> T performServerPost(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
                                            final String postURL, final String jsonBodyToSend, final String jsonAttributeToGetBack){
        String json = given().spec(requestSpec).body(jsonBodyToSend)
                .expect().spec(responseSpec).log().ifError()
                .when().post(postURL)
                .andReturn().asString();
        return (T) from(json).get(jsonAttributeToGetBack);
    }

    private static Integer performServerGet(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
                                            final String postURL, final String jsonAttributeToGetBack){
        String json = given().spec(requestSpec)
                .expect().spec(responseSpec).log().ifError()
                .when().get(postURL)
                .andReturn().asString();
        return from(json).get(jsonAttributeToGetBack);
    }


}
