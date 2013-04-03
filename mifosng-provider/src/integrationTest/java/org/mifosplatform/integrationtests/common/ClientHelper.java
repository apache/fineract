package org.mifosplatform.integrationtests.common;

import java.util.HashMap;
import java.util.Random;


import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

import static org.junit.Assert.assertEquals;

public class ClientHelper {

    private static final String CREATE_CLIENT_URL = "/mifosng-provider/api/v1/clients?tenantIdentifier=default";

    public static Integer createClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return createClient(requestSpec, responseSpec, "04 March 2012");
    }

    public static Integer createClient(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String dateOfJoining) {
        System.out.println("---------------------------------CREATING A CLIENT---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL, getTestClientAsJSON(dateOfJoining), "clientId");
    }

    public static String getTestClientAsJSON(final String dateOfJoining) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("officeId", "1");
        map.put("firstname", randomNameGenerator("Client_FirstName_", 5));
        map.put("lastname", randomNameGenerator("Client_LastName_", 4));
        map.put("externalId", randomIDGenerator("ID_", 7));
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("joinedDate", dateOfJoining);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

    public static void verifyClientCreatedOnServer(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedClientID) {
        System.out.println("------------------------------CHECK CLIENT DETAILS------------------------------------\n");
        String CLIENT_URL = "/mifosng-provider/api/v1/clients/" + generatedClientID + "?tenantIdentifier=default";
        Integer responseClientID = Utils.performServerGet(requestSpec, responseSpec, CLIENT_URL, "id");
        assertEquals("ERROR IN CREATING THE CLIENT",generatedClientID, responseClientID);
    }

    private static String randomStringGenerator(final String prefix, final int len, final String sourceSetString) {
        int lengthOfSource = sourceSetString.length();
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append((sourceSetString).charAt(rnd.nextInt(lengthOfSource)));
        return (prefix + (sb.toString()));
    }

    public static String randomNameGenerator(final String prefix, final int lenOfRandomSuffix) {
        return randomStringGenerator(prefix, lenOfRandomSuffix, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    private static String randomIDGenerator(final String prefix, final int lenOfRandomSuffix) {
        return randomStringGenerator(prefix, lenOfRandomSuffix, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }
}