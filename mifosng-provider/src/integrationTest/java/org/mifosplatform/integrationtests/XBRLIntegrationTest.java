package org.mifosplatform.integrationtests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.common.xbrl.XBRLIntegrationTestHelper;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class XBRLIntegrationTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;

    private XBRLIntegrationTestHelper xbrlHelper;

    @Before
    public void setUp() throws Exception {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void shouldRetrieveTaxonomyList() {
        xbrlHelper = new XBRLIntegrationTestHelper(requestSpec, responseSpec);

        ArrayList<HashMap> taxonomyList = xbrlHelper.getTaxonomyList();
        this.verifyTaxonomyList(taxonomyList);
    }

    private void verifyTaxonomyList(ArrayList<HashMap> taxonomyList) {
        System.out.println("--------------------VERIFYING TAXONOMY LIST--------------------------");
        assertEquals("Checking for the 1st taxonomy", "AdministrativeExpense", taxonomyList.get(0).get("name"));
    }

}
