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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.xbrl.XBRLIntegrationTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class XBRLIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(XBRLIntegrationTest.class);
    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;

    private XBRLIntegrationTestHelper xbrlHelper;

    @BeforeEach
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
        LOG.info("--------------------VERIFYING TAXONOMY LIST--------------------------");
        assertEquals("AdministrativeExpense", taxonomyList.get(0).get("name"), "Checking for the 1st taxonomy");
    }

}
