/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.apache.http.conn.HttpHostConnectException;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.HookHelper;
import org.mifosplatform.integrationtests.common.OfficeHelper;
import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class HookIntegrationTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;

    private HookHelper hookHelper;
    private OfficeHelper officeHelper;

    @Before
    public void setUp() throws Exception {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.hookHelper = new HookHelper(this.requestSpec, this.responseSpec);
        this.officeHelper = new OfficeHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void shouldSendOfficeCreationNotification() {
        // Subject to https://echo-webhook.herokuapp.com being up
        // See
        // http://www.jamesward.com/2014/06/11/testing-webhooks-was-a-pain-so-i-fixed-the-glitch
        final String payloadURL = "http://echo-webhook.herokuapp.com:80/Z7RXoCBdLSFMDrpn?";
        this.hookHelper.createHook(payloadURL);
        final Integer createdOfficeID = this.officeHelper.createOffice("01 January 2012");
        try {

            /**
             * sleep for a three seconds after each failure to increase the
             * likelihood of the previous request for creating office completing
             **/

            for (int i = 0; i < 6; i++) {
                try {
                    final String json = RestAssured.get(payloadURL.replace("?", "")).asString();
                    final Integer notificationOfficeId = JsonPath.with(json).get("officeId");
                    assertEquals("Equality check for created officeId and hook received payload officeId", createdOfficeID,
                            notificationOfficeId);
                    System.out.println("Notification Office Id - " + notificationOfficeId);
                    i = 6;
                } catch (Exception e) {
                    TimeUnit.SECONDS.sleep(3);
                    i++;
                }
            }

        } catch (final Exception e) {
            if (e instanceof HttpHostConnectException) {
                fail("Failed to connect to https://echo-webhook.herokuapp.com platform");
            }
            throw new RuntimeException(e);
        }

    }
}
