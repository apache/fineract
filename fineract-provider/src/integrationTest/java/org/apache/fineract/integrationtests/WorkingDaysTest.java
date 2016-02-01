/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.CommonConstants;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.common.WorkingDaysHelper;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class WorkingDaysTest {

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
    public void updateWorkingDays() {
        HashMap response = (HashMap) WorkingDaysHelper.updateWorkingDays(requestSpec, responseSpec);
        Assert.assertNotNull(response.get("resourceId"));
    }

    @Test
    public void updateWorkingDaysWithWrongRecurrencePattern() {
        final List<HashMap> error = (List) WorkingDaysHelper.updateWorkingDaysWithWrongRecurrence(requestSpec, generalResponseSpec,
                CommonConstants.RESPONSE_ERROR);
        assertEquals("Verify wrong recurrence pattern error", "error.msg.recurring.rule.parsing.error",
                error.get(0).get("userMessageGlobalisationCode"));
    }

}
