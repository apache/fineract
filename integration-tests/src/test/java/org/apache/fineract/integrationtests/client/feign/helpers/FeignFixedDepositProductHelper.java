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
import org.apache.fineract.client.models.GetFixedDepositProductsProductIdResponse;
import org.apache.fineract.client.models.GetFixedDepositProductsResponse;
import org.apache.fineract.client.models.PostFixedDepositProductsRequest;
import org.apache.fineract.client.models.PostFixedDepositProductsResponse;

public class FeignFixedDepositProductHelper {

    private final FineractFeignClient fineractClient;

    public FeignFixedDepositProductHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PostFixedDepositProductsResponse createProduct(PostFixedDepositProductsRequest request) {
        return ok(() -> fineractClient.fixedDepositProduct().createFixedDepositProduct(request));
    }

    public GetFixedDepositProductsProductIdResponse getProduct(Long productId) {
        return ok(() -> fineractClient.fixedDepositProduct().retrieveOneFixedDepositProduct(productId));
    }

    public List<GetFixedDepositProductsResponse> getAllProducts() {
        return ok(() -> fineractClient.fixedDepositProduct().retrieveAllFixedDepositProducts());
    }
}
