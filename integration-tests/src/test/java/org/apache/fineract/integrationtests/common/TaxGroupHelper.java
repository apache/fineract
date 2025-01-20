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
package org.apache.fineract.integrationtests.common;

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TaxGroupHelper {

    private TaxGroupHelper() {

    }

    private static final Logger LOG = LoggerFactory.getLogger(TaxGroupHelper.class);
    private static final String CREATE_TAX_COMPONENT_URL = "/fineract-provider/api/v1/taxes/group?" + Utils.TENANT_IDENTIFIER;

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer createTaxGroup(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Collection<Integer> taxComponentIds) {
        LOG.info("---------------------------------CREATING A TAX GROUP---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_TAX_COMPONENT_URL, getTaxGroupAsJSON(taxComponentIds),
                "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getTaxGroupAsJSON(final Collection<Integer> taxComponentIds) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("name", randomNameGenerator("Tax_component_Name_", 5));
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("taxComponents", getTaxGroupComponents(taxComponentIds));
        return new Gson().toJson(map);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static List<HashMap<String, String>> getTaxGroupComponents(final Collection<Integer> taxComponentIds) {
        List<HashMap<String, String>> taxGroupComponents = new ArrayList<>();
        for (Integer taxComponentId : taxComponentIds) {
            taxGroupComponents.add(getTaxComponentMap(taxComponentId));
        }
        return taxGroupComponents;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static HashMap<String, String> getTaxComponentMap(final Integer taxComponentId) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("taxComponentId", String.valueOf(taxComponentId));
        map.put("startDate", "01 January 2013");
        return map;
    }

    public static String randomNameGenerator(final String prefix, final int lenOfRandomSuffix) {
        return Utils.randomStringGenerator(prefix, lenOfRandomSuffix);
    }

}
