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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.List;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetSavingsProductsProductIdResponse;
import org.apache.fineract.client.models.GetSavingsProductsResponse;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PostSavingsProductsResponse;
import org.apache.fineract.client.models.PutSavingsProductsProductIdRequest;
import org.apache.fineract.client.models.PutSavingsProductsProductIdResponse;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;

public class FeignSavingsProductHelper {

    private final FineractFeignClient fineractClient;

    public FeignSavingsProductHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PostSavingsProductsResponse createSavingsProduct(PostSavingsProductsRequest request) {
        return ok(() -> fineractClient.savingsProduct().createSavingsProduct(request));
    }

    public PostSavingsProductsResponse createDefaultSavingsProduct() {
        return createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct());
    }

    public GetSavingsProductsProductIdResponse getSavingsProduct(Long productId) {
        return ok(() -> fineractClient.savingsProduct().retrieveOneSavingsProduct(productId));
    }

    public PutSavingsProductsProductIdResponse updateSavingsProduct(Long productId, PutSavingsProductsProductIdRequest request) {
        return ok(() -> fineractClient.savingsProduct().updateSavingsProduct(productId, request));
    }

    public List<GetSavingsProductsResponse> getAllSavingsProducts() {
        return ok(() -> fineractClient.savingsProduct().retrieveAllSavingsProducts());
    }
}
