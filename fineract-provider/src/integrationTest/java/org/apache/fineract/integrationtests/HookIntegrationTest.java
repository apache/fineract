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
package org.apache.fineract.integrationtests;

import org.junit.Assert;

import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.apache.fineract.integrationtests.common.HookHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.http.conn.HttpHostConnectException;
import org.junit.Before;
import org.junit.Test;

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
                    Assert.assertEquals("Equality check for created officeId and hook received payload officeId", createdOfficeID,
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
    
    @Test
    public void createUpdateAndDeleteHook(){
    	final String payloadURL = "http://echo-webhook.herokuapp.com:80/Z7RXoCBdLSFMDrpn?";
    	final String updateURL = "http://localhost";

        Long hookId = this.hookHelper.createHook(payloadURL).longValue();
        Assert.assertNotNull(hookId);
        this.hookHelper.verifyHookCreatedOnServer(hookId);
    	System.out.println("---------------------SUCCESSFULLY CREATED AND VERIFIED HOOK-------------------------"+hookId);
    	this.hookHelper.updateHook(updateURL, hookId);
    	this.hookHelper.verifyUpdateHook(updateURL, hookId);
    	System.out.println("---------------------SUCCESSFULLY UPDATED AND VERIFIED HOOK-------------------------"+hookId);
    	this.hookHelper.deleteHook(hookId);
    	this.hookHelper.verifyDeleteHook(hookId);
    	System.out.println("---------------------SUCCESSFULLY DELETED AND VERIFIED HOOK-------------------------"+hookId);

    }
}
