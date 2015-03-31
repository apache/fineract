/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.CommonConstants;
import org.mifosplatform.integrationtests.common.PasswordPreferencesHelper;
import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class PasswordPreferencesIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ResponseSpecification generalResponseSpec;

    @Before
    public void setUp() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.generalResponseSpec = new ResponseSpecBuilder().build();

    }

    @Test
    public void updatePasswordPreferences() {
        String validationPolicyId = "2";
        PasswordPreferencesHelper.updatePasswordPreferences(requestSpec, responseSpec, validationPolicyId);
        this.validateIfThePasswordIsUpdated(validationPolicyId);
    }

    private void validateIfThePasswordIsUpdated(String validationPolicyId){
        Integer id = PasswordPreferencesHelper.getActivePasswordPreference(requestSpec, responseSpec);
        assertEquals(validationPolicyId, id.toString());
        System.out.println("---------------------------------PASSWORD PREFERENCE VALIDATED SUCCESSFULLY-----------------------------------------");

    }
    
    @Test
    public void updateWithInvalidPolicyId() {
        String invalidValidationPolicyId = "2000";
        final List<HashMap> error = (List) PasswordPreferencesHelper.updateWithInvalidValidationPolicyId(requestSpec, generalResponseSpec, invalidValidationPolicyId, 
                CommonConstants.RESPONSE_ERROR);
        assertEquals("Password Validation Policy with identifier 2000 does not exist", "error.msg.password.validation.policy.id.invalid",
                error.get(0).get("userMessageGlobalisationCode"));
    }

}
