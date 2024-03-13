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
package org.apache.fineract.integrationtests.common.loans;

import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoanProductsTemplateResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdResponse;
import org.apache.fineract.integrationtests.client.IntegrationTest;

public class LoanProductHelper extends IntegrationTest {

    public LoanProductHelper() {}

    public PostLoanProductsResponse createLoanProduct(PostLoanProductsRequest request) {
        return ok(fineract().loanProducts.createLoanProduct(request));
    }

    public GetLoanProductsProductIdResponse retrieveLoanProductByExternalId(String externalId) {
        return ok(fineract().loanProducts.retrieveLoanProductDetails1(externalId));
    }

    public GetLoanProductsProductIdResponse retrieveLoanProductById(Long loanProductId) {
        return ok(fineract().loanProducts.retrieveLoanProductDetails(loanProductId));
    }

    public PutLoanProductsProductIdResponse updateLoanProductByExternalId(String externalId, PutLoanProductsProductIdRequest request) {
        return ok(fineract().loanProducts.updateLoanProduct1(externalId, request));
    }

    public PutLoanProductsProductIdResponse updateLoanProductById(Long loanProductId, PutLoanProductsProductIdRequest request) {
        return ok(fineract().loanProducts.updateLoanProduct(loanProductId, request));
    }

    public GetLoanProductsTemplateResponse getLoanProductTemplate(boolean isProductMixTemplate) {
        return ok(fineract().loanProducts.retrieveTemplate11(isProductMixTemplate));
    }
}
