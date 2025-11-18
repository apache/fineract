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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SavingsProductGlobalInitializer implements FineractGlobalInitializerStep {

    @Override
    public void initialize() {
        // TODO uncomment and check when PS-1088 is done
        // //EUR
        // PostSavingsProductsRequest savingsProductsRequestEUR =
        // SavingsProductRequestFactory.defaultSavingsProductRequest();
        //
        // Response<PostSavingsProductsResponse> savingsProductsResponseEUR =
        // savingsProductApi.create13(savingsProductsRequestEUR).execute();
        // testContext().set(TestContextKey.DEFAULT_SAVINGS_PRODUCT_CREATE_RESPONSE_EUR, savingsProductsResponseEUR);
        //
        // //USD
        // PostSavingsProductsRequest savingsProductsRequestUSD =
        // SavingsProductRequestFactory.defaultSavingsProductRequest()
        // .name("CUSD")
        // .shortName("CUS")
        // .currencyCode("USD");
        //
        // Response<PostSavingsProductsResponse> savingsProductsResponseUSD =
        // savingsProductApi.create13(savingsProductsRequestUSD).execute();
        // testContext().set(TestContextKey.DEFAULT_SAVINGS_PRODUCT_CREATE_RESPONSE_USD, savingsProductsResponseUSD);

    }
}
