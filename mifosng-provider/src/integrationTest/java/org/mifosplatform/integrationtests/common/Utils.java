package org.mifosplatform.integrationtests.common;

import com.google.gson.Gson;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

import java.util.HashMap;
import java.util.Random;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.post;
import static com.jayway.restassured.path.json.JsonPath.from;

public class Utils {

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


    public static Integer createClient(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        return createClient(requestSpec,responseSpec,"04 March 2012");

    }
    public static Integer createClient(RequestSpecification requestSpec, ResponseSpecification responseSpec,String dateOfJoining) {
        System.out.println("---------------------------------CREATING A CLIENT---------------------------------------------");
        String json = given().spec(requestSpec).body(getTestClientAsJSON(dateOfJoining))
                     .expect().spec(responseSpec)
                     .when().post("/mifosng-provider/api/v1/clients?tenantIdentifier=default")
                     .andReturn().asString();

        return  from(json).get("clientId");
    }

    public static String loginIntoServerAndGetBase64EncodedAuthenticationKey() {
        System.out.println("-----------------------------------LOGIN-----------------------------------------");
        String json = post("/mifosng-provider/api/v1/authentication?username=mifos&password=password&tenantIdentifier=default").asString();
        return JsonPath.with(json).get("base64EncodedAuthenticationKey");
    }
}
