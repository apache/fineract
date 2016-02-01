/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void shouldRetrieveTaxonomyList() {
        this.xbrlHelper = new XBRLIntegrationTestHelper(this.requestSpec, this.responseSpec);

        final ArrayList<HashMap> taxonomyList = this.xbrlHelper.getTaxonomyList();
        verifyTaxonomyList(taxonomyList);
    }

    private void verifyTaxonomyList(final ArrayList<HashMap> taxonomyList) {
        System.out.println("--------------------VERIFYING TAXONOMY LIST--------------------------");
        assertEquals("Checking for the 1st taxonomy", "AdministrativeExpense", taxonomyList.get(0).get("name"));
    }

}
