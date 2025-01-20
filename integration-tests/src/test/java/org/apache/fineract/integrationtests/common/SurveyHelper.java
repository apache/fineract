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
package org.apache.fineract.integrationtests.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SurveyHelper {

    private SurveyHelper() {

    }

    private static final Logger LOG = LoggerFactory.getLogger(SurveyHelper.class);
    private static final String FULFIL_SURVEY_URL = "/fineract-provider/api/v1/survey/ppi_kenya_2009/clientId?" + Utils.TENANT_IDENTIFIER;

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer fulfilSurvey(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return fulfilSurvey(requestSpec, responseSpec, "04 March 2011");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer fulfilSurvey(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String activationDate) {
        LOG.info("---------------------------------FULFIL PPI ---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, FULFIL_SURVEY_URL, getTestPPIAsJSON(), "clientId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getTestPPIAsJSON() {
        final HashMap<String, String> map = new HashMap<>();

        map.put("date", "2014-05-19 00:00:00");
        map.put("ppi_household_members_cd_q1_householdmembers", "107");
        map.put("ppi_highestschool_cd_q2_highestschool", "112");
        map.put("ppi_businessoccupation_cd_q3_businessoccupation", "116");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("ppi_habitablerooms_cd_q4_habitablerooms", "120");

        map.put("ppi_floortype_cd_q5_floortype", "124");
        map.put("ppi_lightingsource_cd_q6_lightingsource", "126");
        map.put("ppi_irons_cd_q7_irons", "128");
        map.put("ppi_mosquitonets_cd_q8_mosquitonets", "132");
        map.put("ppi_towels_cd_q9_towels", "134");
        map.put("ppi_fryingpans_cd_q10_fryingpans", "138");

        LOG.info("map :  {}", map);
        return new Gson().toJson(map);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static void verifySurveyCreatedOnServer(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedClientID) {
        LOG.info("------------------------------CHECK CLIENT DETAILS------------------------------------\n");
        final String SURVEY_URL = "/fineract-provider/api/v1/Survey/ppi_kenya_2009/clientid/entryId" + generatedClientID + "?"
                + Utils.TENANT_IDENTIFIER;
        final Integer responseClientID = Utils.performServerGet(requestSpec, responseSpec, SURVEY_URL, "id");
        assertEquals(generatedClientID, responseClientID, "ERROR IN CREATING THE CLIENT");
    }

}
