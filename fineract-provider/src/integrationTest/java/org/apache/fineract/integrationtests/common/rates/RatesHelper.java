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
package org.apache.fineract.integrationtests.common.rates;

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.Utils;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RatesHelper {

  private static final String RATES_URL = "/fineract-provider/api/v1/rates";
  private static final String CREATE_RATES_URL = RATES_URL + "?" + Utils.TENANT_IDENTIFIER;
  private final static String PERCENTAGE = "10";
  private final static Integer PRODUCT_APPLY_LOAN = 1;
  private final static Boolean ACTIVE = true;

  public static ArrayList<HashMap> getRates(final RequestSpecification requestSpec,
      final ResponseSpecification responseSpec) {
    return (ArrayList) Utils
        .performServerGet(requestSpec, responseSpec, RATES_URL + "?" + Utils.TENANT_IDENTIFIER, "");
  }


  public static Integer createRates(final RequestSpecification requestSpec,
      final ResponseSpecification responseSpec,
      final String request) {
    return Utils
        .performServerPost(requestSpec, responseSpec, CREATE_RATES_URL, request, "resourceId");
  }

  public static HashMap getRateById(final RequestSpecification requestSpec,
      final ResponseSpecification responseSpec,
      final Integer rateId) {
    return Utils.performServerGet(requestSpec, responseSpec,
        RATES_URL + "/" + rateId + "?" + Utils.TENANT_IDENTIFIER, "");
  }

  public static HashMap updateRates(final RequestSpecification requestSpec,
      final ResponseSpecification responseSpec,
      final Integer rateId, final String request) {
    return Utils.performServerPut(requestSpec, responseSpec,
        RATES_URL + "/" + rateId + "?" + Utils.TENANT_IDENTIFIER, request,
        CommonConstants.RESPONSE_CHANGES);
  }

  public static String getLoanRateJSON() {
    return getLoanRateJSON(RatesHelper.PRODUCT_APPLY_LOAN, RatesHelper.PERCENTAGE);
  }

  public static String getLoanRateJSON(final Integer productApply, final String percentage) {
    final HashMap<String, Object> map = populateDefaultsForLoan();
    map.put("percentage", percentage);
    map.put("productApply", productApply);
    String crateRateJSON = new Gson().toJson(map);
    return crateRateJSON;
  }

  public static HashMap<String, Object> populateDefaultsForLoan() {
    final HashMap<String, Object> map = new HashMap<>();
    map.put("active", RatesHelper.ACTIVE);
    map.put("percentage", RatesHelper.PERCENTAGE);
    map.put("locale", "en");
    map.put("productApply", RatesHelper.PRODUCT_APPLY_LOAN);
    map.put("name", Utils.randomNameGenerator("Rate_Loans_", 6));
    return map;
  }

  public static String getModifyRateJSON() {
    final HashMap<String, Object> map = new HashMap<>();
    map.put("percentage", "15.0");
    map.put("locale", "en");
    String json = new Gson().toJson(map);
    return json;
  }

}

