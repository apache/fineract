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
package org.apache.fineract.test.data.loanproduct;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoanProductsResponse;
import org.apache.fineract.client.services.LoanProductsApi;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import retrofit2.Response;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanProductResolver {

    private final LoanProductsApi loanProductsApi;

    @Cacheable(key = "#loanProduct.getName()", value = "loanProductsByName")
    public long resolve(LoanProduct loanProduct) {
        try {
            String loanProductName = loanProduct.getName();
            log.debug("Resolving loan product by name [{}]", loanProductName);
            Response<List<GetLoanProductsResponse>> response = loanProductsApi.retrieveAllLoanProducts().execute();
            if (!response.isSuccessful()) {
                throw new IllegalStateException("Unable to get loan products. Status code was HTTP " + response.code());
            }

            List<GetLoanProductsResponse> loanProductsResponses = response.body();
            GetLoanProductsResponse foundLpr = loanProductsResponses.stream().filter(lpr -> loanProductName.equals(lpr.getName())).findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Loan product [%s] not found".formatted(loanProductName)));
            return foundLpr.getId();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
