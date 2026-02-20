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
package org.apache.fineract.test.initializer.global;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsResponse;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostPaymentAllocation;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsResponse;
import org.apache.fineract.test.data.workingcapitalproduct.DefaultWorkingCapitalLoanProduct;
import org.apache.fineract.test.factory.WorkingCapitalLoanProductRequestFactory;
import org.apache.fineract.test.support.TestContext;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class WorkingCapitalLoanProductInitializerStep implements FineractGlobalInitializerStep {

    private final FineractFeignClient fineractClient;
    private final WorkingCapitalLoanProductRequestFactory workingCapitalLoanProductRequestFactory;

    @Override
    public void initialize() throws Exception {

        final String workingCapitalProductDefaultName = DefaultWorkingCapitalLoanProduct.WCLP.getName();
        final PostWorkingCapitalLoanProductsRequest defaultWCPLRequest = workingCapitalLoanProductRequestFactory
                .defaultWorkingCapitalLoanProductRequest() //
                .name(workingCapitalProductDefaultName) //
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", List.of("PENALTY", "FEE", "PRINCIPAL"))));//
        final PostWorkingCapitalLoanProductsResponse responseDefaultWCPL = createWorkingCapitalLoanProductIdempotent(defaultWCPLRequest);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP, responseDefaultWCPL);
    }

    private static PostPaymentAllocation createPaymentAllocation(String transactionType, List<String> paymentAllocationRules) {
        PostPaymentAllocation.TransactionTypeEnum transactionTypeName = PostPaymentAllocation.TransactionTypeEnum.valueOf(transactionType);
        PostPaymentAllocation paymentAllocationData = new PostPaymentAllocation();
        paymentAllocationData.setTransactionType(transactionTypeName);

        List<PaymentAllocationOrder> paymentAllocationOrders = new ArrayList<>();
        for (int i = 0; i < paymentAllocationRules.size(); i++) {
            PaymentAllocationOrder e = new PaymentAllocationOrder();
            e.setOrder(i + 1);
            e.setPaymentAllocationRule(paymentAllocationRules.get(i));
            paymentAllocationOrders.add(e);
        }

        paymentAllocationData.setPaymentAllocationOrder(paymentAllocationOrders);
        return paymentAllocationData;
    }

    private PostWorkingCapitalLoanProductsResponse createWorkingCapitalLoanProductIdempotent(
            PostWorkingCapitalLoanProductsRequest workingCapitalProductRequest) {
        String workingCapitalProductName = workingCapitalProductRequest.getName();
        log.debug("Attempting to create working capital product: {}", workingCapitalProductName);
        try {
            List<GetWorkingCapitalLoanProductsResponse> existingWorkingCapitalProducts = fineractClient.workingCapitalLoanProducts()
                    .retrieveAllWorkingCapitalLoanProducts(Map.of());
            GetWorkingCapitalLoanProductsResponse existingWorkingCapitalProduct = existingWorkingCapitalProducts.stream()
                    .filter(p -> workingCapitalProductName.equals(p.getName())).findFirst().orElse(null);

            if (existingWorkingCapitalProduct != null) {
                log.debug("Working capital product '{}' already exists with ID: {}", workingCapitalProductName,
                        existingWorkingCapitalProduct.getId());
                PostWorkingCapitalLoanProductsResponse response = new PostWorkingCapitalLoanProductsResponse();
                response.setResourceId(existingWorkingCapitalProduct.getId());
                return response;
            }
        } catch (Exception e) {
            log.warn("Error checking if working capital product '{}' exists", workingCapitalProductName, e);
        }

        log.debug("Creating new working capital product: {}", workingCapitalProductName);
        try {
            PostWorkingCapitalLoanProductsResponse response = ok(() -> fineractClient.workingCapitalLoanProducts()
                    .createWorkingCapitalLoanProduct(workingCapitalProductRequest, Map.of()));
            log.debug("Successfully created working capital product '{}' with ID: {}", workingCapitalProductName, response.getResourceId());
            return response;
        } catch (Exception e) {
            log.error("FAILED to create working capital product '{}'", workingCapitalProductName, e);
            throw e;
        }
    }

}
