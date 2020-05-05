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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.rates.RatesHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings({"rawtypes"})
public class RatesTest {

  private ResponseSpecification responseSpec;
  private RequestSpecification requestSpec;

  @Before
  public void setup() {
    Utils.initializeRESTAssured();
    this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
    this.requestSpec.header("Authorization",
        "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
    this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
  }

  @Test
  public void testRatesForLoans() {

    // Retrieving all Rates
    ArrayList<HashMap> allRatesData = RatesHelper.getRates(this.requestSpec, this.responseSpec);
    Assert.assertNotNull(allRatesData);

    // Testing Creation and Update of Loan Rate
    final Integer loanRateId = RatesHelper.createRates(this.requestSpec, this.responseSpec,
        RatesHelper.getLoanRateJSON());
    Assert.assertNotNull(loanRateId);

    //Update Rate percentage
    HashMap changes = RatesHelper.updateRates(this.requestSpec, this.responseSpec, loanRateId,
        RatesHelper.getModifyRateJSON());

    HashMap rateDataAfterChanges = RatesHelper
        .getRateById(this.requestSpec, this.responseSpec, loanRateId);
    Assert.assertEquals("Verifying Rate after modification", rateDataAfterChanges.get("percentage"),
        changes.get("percentage"));

  }


}
