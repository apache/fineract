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

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductMixHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ProductMixHelper.class);
    private static final String PRODUCT_MIX_URL = "/fineract-provider/api/v1/loanproducts";

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public ProductMixHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public ArrayList getProductsMixList() {
        final String GET_PRODUCT_MIX_URL = PRODUCT_MIX_URL + "?" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------------ RETRIEVING PRODUCT MIX -------------------------");
        return Utils.performServerGet(this.requestSpec, this.responseSpec, GET_PRODUCT_MIX_URL, "");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public HashMap getProductMixTemplate() {
        final String GET_PRODUCT_MIX_URL = PRODUCT_MIX_URL + "/template?isProductMixTemplate=true&" + Utils.TENANT_IDENTIFIER;
        LOG.info("-------------------- RETRIEVING PRODUCT MIX TEMPLATE ---------------------");
        return Utils.performServerGet(this.requestSpec, this.responseSpec, GET_PRODUCT_MIX_URL, "");
    }

}
