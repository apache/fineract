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
package org.apache.fineract.integrationtests.crypt;

import static org.junit.Assert.assertNotNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author manoj
 */
public class CryptographyTest {
    private final static Logger LOG = LoggerFactory.getLogger(CryptographyTest.class);
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

    }

    @Test
    public void checkPublicRSAKey(){
        String keyType = "test";

        String publicKey = getpublicRSAKey(requestSpec, responseSpec, keyType, "publicKey");

        assertNotNull("Public key for type test is null", publicKey);

    }

    public static String getpublicRSAKey(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
                                   final String keyType, final String jsonReturn) {
        final String GET_PUBLIC_RSA_URL = "/fineract-provider/api/v1/crypt/publickey/"+ keyType + "?" + Utils.TENANT_IDENTIFIER;
        LOG.info("---------------------------------GET A PUBLIC KEY---------------------------------------------");
        return Utils.performServerGet(requestSpec, responseSpec, GET_PUBLIC_RSA_URL, jsonReturn);

    }
}
