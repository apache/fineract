package org.mifosplatform.integrationtests;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
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
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }
	
	@Test
	public void test() {
		
		HashMap<String, String> metadata = new HashMap<String, String>();
		metadata.put("user", "resource_url");
		HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("name", "foo");
        map.put("text", "Hello {{template}}");
        map.put("mappers", metadata);
		
        ArrayList<?> get = Utils.performServerGet(requestSpec, responseSpec, GET_TEMPLATES_URL, "");
        int entriesBeforeTest = get.size();
        
		Integer id = Utils.performServerPost(requestSpec, responseSpec, GET_TEMPLATES_URL, new Gson().toJson(map), "resourceId");
		
		String templateUrlForId = String.format(GET_TEMPLATE_ID_URL, id);
		
		String getrequest2 = Utils.performServerGet(requestSpec, responseSpec, templateUrlForId, RESPONSE_ATTRIBUTE_NAME);
        
        Assert.assertTrue(getrequest2.equals("foo"));
        
        Utils.performServerDelete(requestSpec, responseSpec, templateUrlForId, "");
        
        get = Utils.performServerGet(requestSpec, responseSpec, GET_TEMPLATES_URL, "");
        int entriesAfterTest = get.size();
        
        Assert.assertEquals(entriesBeforeTest, entriesAfterTest);
	}
}
