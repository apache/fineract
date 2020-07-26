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

import com.google.gson.Gson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TemplateIntegrationTest {

    private static final String GET_TEMPLATES_URL = "/fineract-provider/api/v1/templates?tenantIdentifier=default";

    private static final String RESPONSE_ATTRIBUTE_NAME = "name";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @BeforeEach
    public void setup() {

        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Disabled
    @Test
    public void test() {

        final HashMap<String, String> metadata = new HashMap<>();
        metadata.put("user", "resource_url");
        final HashMap<String, Object> map = new HashMap<>();
        map.put("name", "foo");
        map.put("text", "Hello {{template}}");
        map.put("mappers", metadata);

        ArrayList<?> get = Utils.performServerGet(this.requestSpec, this.responseSpec, GET_TEMPLATES_URL, "");
        final int entriesBeforeTest = get.size();

        final Integer id = Utils.performServerPost(this.requestSpec, this.responseSpec, GET_TEMPLATES_URL, new Gson().toJson(map),
                "resourceId");

        final String templateUrlForId = String.format("/fineract-provider/api/v1/templates/%s?tenantIdentifier=default", id);

        final String getrequest2 = Utils.performServerGet(this.requestSpec, this.responseSpec, templateUrlForId, RESPONSE_ATTRIBUTE_NAME);

        Assertions.assertTrue(getrequest2.equals("foo"));

        Utils.performServerDelete(this.requestSpec, this.responseSpec, templateUrlForId, "");

        get = Utils.performServerGet(this.requestSpec, this.responseSpec, GET_TEMPLATES_URL, "");
        final int entriesAfterTest = get.size();

        Assertions.assertEquals(entriesBeforeTest, entriesAfterTest);
    }
}
