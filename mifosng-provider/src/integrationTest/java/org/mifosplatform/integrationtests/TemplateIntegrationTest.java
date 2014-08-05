/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.Utils;

import com.google.gson.Gson;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class TemplateIntegrationTest {

    private final String GET_TEMPLATES_URL = "/mifosng-provider/api/v1/templates?tenantIdentifier=default";
    private final String GET_TEMPLATE_ID_URL = "/mifosng-provider/api/v1/templates/%s?tenantIdentifier=default";
    private final String RESPONSE_ATTRIBUTE_NAME = "name";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @Before
    public void setup() {

        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Ignore
    @Test
    public void test() {

        final HashMap<String, String> metadata = new HashMap<>();
        metadata.put("user", "resource_url");
        final HashMap<String, Object> map = new HashMap<>();
        map.put("name", "foo");
        map.put("text", "Hello {{template}}");
        map.put("mappers", metadata);

        ArrayList<?> get = Utils.performServerGet(this.requestSpec, this.responseSpec, this.GET_TEMPLATES_URL, "");
        final int entriesBeforeTest = get.size();

        final Integer id = Utils.performServerPost(this.requestSpec, this.responseSpec, this.GET_TEMPLATES_URL, new Gson().toJson(map), "resourceId");

        final String templateUrlForId = String.format(this.GET_TEMPLATE_ID_URL, id);

        final String getrequest2 = Utils.performServerGet(this.requestSpec, this.responseSpec, templateUrlForId, this.RESPONSE_ATTRIBUTE_NAME);

        Assert.assertTrue(getrequest2.equals("foo"));

        Utils.performServerDelete(this.requestSpec, this.responseSpec, templateUrlForId, "");

        get = Utils.performServerGet(this.requestSpec, this.responseSpec, this.GET_TEMPLATES_URL, "");
        final int entriesAfterTest = get.size();

        Assert.assertEquals(entriesBeforeTest, entriesAfterTest);
    }
}
