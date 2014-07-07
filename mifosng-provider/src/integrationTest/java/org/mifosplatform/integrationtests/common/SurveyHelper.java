/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class SurveyHelper {

    private static final String FULFIL_SURVEY_URL = "/mifosng-provider/api/v1/survey/ppi_kenya_2009/clientId?" + Utils.TENANT_IDENTIFIER;

    public static Integer fulfilSurvey(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return fulfilSurvey(requestSpec, responseSpec, "04 March 2011");
    }

    public static Integer fulfilSurvey(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String activationDate) {
        System.out.println("---------------------------------FULFIL PPI ---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec,FULFIL_SURVEY_URL, getTestPPIAsJSON(), "clientId");
    }

    public static String getTestPPIAsJSON() {
        final HashMap<String, String> map = new HashMap<>();

        map.put("date", "2014-05-19 00:00:00");
        map.put("ppi_household_members_cd_q1_householdmembers","107");
        map.put("ppi_highestschool_cd_q2_highestschool","112");
        map.put("ppi_businessoccupation_cd_q3_businessoccupation","116");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("ppi_habitablerooms_cd_q4_habitablerooms", "120");

        map.put("ppi_floortype_cd_q5_floortype", "124");
        map.put("ppi_lightingsource_cd_q6_lightingsource", "126");
        map.put("ppi_irons_cd_q7_irons", "128");
        map.put("ppi_mosquitonets_cd_q8_mosquitonets", "132");
        map.put("ppi_towels_cd_q9_towels", "134");
        map.put("ppi_fryingpans_cd_q10_fryingpans", "138");

        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

    public static void verifySurveyCreatedOnServer(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedClientID) {
        System.out.println("------------------------------CHECK CLIENT DETAILS------------------------------------\n");
        final String SURVEY_URL = "/mifosng-provider/api/v1/Survey/ppi_kenya_2009/clientid/entryId" + generatedClientID + "?" + Utils.TENANT_IDENTIFIER;
        final Integer responseClientID = Utils.performServerGet(requestSpec, responseSpec, SURVEY_URL, "id");
        assertEquals("ERROR IN CREATING THE CLIENT", generatedClientID, responseClientID);
    }


}