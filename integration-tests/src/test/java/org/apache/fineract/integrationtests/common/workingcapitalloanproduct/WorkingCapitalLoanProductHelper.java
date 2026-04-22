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
package org.apache.fineract.integrationtests.common.workingcapitalloanproduct;

import java.util.List;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.DeleteWorkingCapitalLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsTemplateResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsResponse;
import org.apache.fineract.client.models.PutWorkingCapitalLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoanProductsProductIdResponse;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;

public class WorkingCapitalLoanProductHelper {

    public WorkingCapitalLoanProductHelper() {}

    public PostWorkingCapitalLoanProductsResponse createWorkingCapitalLoanProduct(final PostWorkingCapitalLoanProductsRequest request) {
        return FeignCalls.ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoanProducts()
                .createWorkingCapitalLoanProduct(request));
    }

    public GetWorkingCapitalLoanProductsProductIdResponse retrieveWorkingCapitalLoanProductByExternalId(final String externalId) {
        return FeignCalls.ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoanProducts()
                .retrieveOneWorkingCapitalLoanProductByExternalId(externalId));
    }

    public GetWorkingCapitalLoanProductsProductIdResponse retrieveWorkingCapitalLoanProductById(final Long productId) {
        return FeignCalls.ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoanProducts()
                .retrieveOneWorkingCapitalLoanProduct(productId));
    }

    public List<GetWorkingCapitalLoanProductsResponse> retrieveAllWorkingCapitalLoanProducts() {
        return FeignCalls.ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoanProducts()
                .retrieveAllWorkingCapitalLoanProducts());
    }

    public GetWorkingCapitalLoanProductsTemplateResponse retrieveTemplate() {
        return FeignCalls.ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoanProducts()
                .retrieveTemplateWorkingCapitalLoanProduct());
    }

    public PutWorkingCapitalLoanProductsProductIdResponse updateWorkingCapitalLoanProductByExternalId(final String externalId,
            final PutWorkingCapitalLoanProductsProductIdRequest request) {
        return FeignCalls.ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoanProducts()
                .updateWorkingCapitalLoanProductByExternalId(externalId, request));
    }

    public PutWorkingCapitalLoanProductsProductIdResponse updateWorkingCapitalLoanProductById(final Long productId,
            final PutWorkingCapitalLoanProductsProductIdRequest request) {
        return FeignCalls.ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoanProducts()
                .updateWorkingCapitalLoanProduct(productId, request));
    }

    public DeleteWorkingCapitalLoanProductsProductIdResponse deleteWorkingCapitalLoanProductByExternalId(final String externalId) {
        return FeignCalls.ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoanProducts()
                .deleteWorkingCapitalLoanProductByExternalId(externalId));
    }

    public DeleteWorkingCapitalLoanProductsProductIdResponse deleteWorkingCapitalLoanProductById(final Long productId) {
        return FeignCalls.ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoanProducts()
                .deleteWorkingCapitalLoanProduct(productId));
    }
}
